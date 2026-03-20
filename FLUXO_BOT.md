# Fluxo do Bot WhatsApp — ASB Telecom

Documento técnico que descreve o caminho completo de uma mensagem, desde o momento em que o usuário digita **"Oi"** no WhatsApp até a resposta do bot.

---

## Visão geral da arquitetura

```
WhatsApp Cloud API
       │
       ▼  POST /api/v1/webhook/whatsapp
 WebhookController          ← valida assinatura HMAC-SHA256
       │
       ▼
 WebhookService             ← idempotência, roteamento
       │
       ▼
 Handler (por estado)       ← executa a lógica do fluxo
       │
       ▼
 WhatsAppService            ← envia a resposta ao usuário
```

O bot é **stateless em memória**: o estado de cada conversa é persistido no banco de dados (tabela `conversation_state`). A cada mensagem recebida, o estado é lido, o handler adequado é chamado, e o novo estado é gravado.

---

## Passo 1 — Recepção do webhook (`WebhookController`)

**Arquivo**: [WebhookController.java](src/main/java/br/com/sistema/bot/controller/WebhookController.java)

Quando o usuário envia "Oi", a Meta (WhatsApp Cloud API) faz um `POST` para:

```
POST /api/v1/webhook/whatsapp
Headers:
  x-hub-signature-256: sha256=<assinatura HMAC>
Body: (JSON com estrutura de webhook da Meta)
```

O controller executa duas ações antes de qualquer processamento:

1. **Valida a assinatura HMAC-SHA256** — lê o header `x-hub-signature-256` e recalcula usando o `app.secret` configurado. Se não bater, retorna `403 Forbidden` e descarta a mensagem.
2. **Delega ao `WebhookService`** — repassa o payload deserializado.

---

## Passo 2 — Processamento inicial (`WebhookService`)

**Arquivo**: [WebhookService.java](src/main/java/br/com/sistema/bot/service/WebhookService.java)

### 2.1 Extração dos dados

O payload da Meta tem vários níveis de aninhamento:

```
WhatsAppWebhookRequest
└─ entry[]
   └─ changes[]  (filtra field == "messages")
      └─ value
         ├─ contacts[0].profile.name  →  "João Silva"
         └─ messages[0]
            ├─ id        →  "wamid.xxx..."
            ├─ from      →  "5511999990000"
            ├─ type      →  "text"
            └─ text.body →  "Oi"
```

Somente mensagens do tipo `"text"` são processadas. Áudios, imagens e outros tipos são ignorados.

### 2.2 Idempotência

Antes de qualquer lógica, o serviço consulta a tabela `message_log`:

```java
if (messageLogService.jaProcessado(messageId)) return;
messageLogService.registrar(messageId, phone);
```

Se a Meta reenviar o mesmo webhook (retry), a mensagem já estará na tabela e será descartada silenciosamente. Isso evita respostas duplicadas.

### 2.3 Recuperação do estado

```java
ConversationState state = conversationStateService.buscarOuCriar(phone);
```

- **Número novo** → cria registro com estado `MENU_INICIAL`
- **Número existente** → carrega o estado atual e o `contextData` (ex.: CPF digitado anteriormente)

### 2.4 Guarda especial: estado `TRANSFERIDO`

Se o estado for `TRANSFERIDO`, significa que um atendente humano assumiu a conversa no Chatwoot. O bot **ignora a mensagem completamente** para não interferir no atendimento.

### 2.5 Comando global "sair" / "cancelar"

Antes de rotear para qualquer handler, o serviço verifica se o conteúdo da mensagem (em minúsculas) é `"sair"` ou `"cancelar"`. Se sim, chama `EncerrarHandler` imediatamente, **independente do estado atual**.

### 2.6 Roteamento para o handler

```
BotState atual               →  Handler chamado
─────────────────────────────────────────────────
MENU_INICIAL (padrão)        →  MenuInicialHandler
SOU_CLIENTE                  →  SouClienteHandler
FINANCEIRO                   →  FinanceiroHandler
AGUARDA_CPF_FATURA           →  ProcessaCpfCnpjHandler
AGUARDA_CPF_DESBLOQUEIO      →  ProcessaCpfCnpjHandler
CONFIRMA_IDENTIDADE_FATURA   →  ConfirmaCpfHandler
CONFIRMA_IDENTIDADE_DESBLOQUEIO → ConfirmaCpfHandler
```

---

## Passo 3 — Handlers (o coração do bot)

Todos os handlers implementam a interface `MessageHandler`:

```java
public interface MessageHandler {
    void handle(ConversationContext ctx);
}
```

O `ConversationContext` carrega tudo que o handler precisa:

| Campo | Exemplo |
|---|---|
| `phone` | `"5511999990000"` |
| `messageId` | `"wamid.xxx"` |
| `senderName` | `"João Silva"` |
| `content` | `"Oi"` |
| `currentState` | `MENU_INICIAL` |
| `contextData` | `null` (ou CPF de passo anterior) |

---

### Handler: `MenuInicialHandler`

**Arquivo**: [MenuInicialHandler.java](src/main/java/br/com/sistema/bot/handler/MenuInicialHandler.java)

**Ativado quando**: estado = `MENU_INICIAL` (inclui o primeiro "Oi")

Para qualquer mensagem que não seja "1" ou "2" (inclusive o "Oi" inicial), o bot envia o menu de boas-vindas:

```
Olá! Seja bem-vindo(a) à ASB Telecom! 🌐

Como podemos ajudar você hoje?

1️⃣ Sou cliente ASB
2️⃣ Quero me tornar cliente

Digite o número da opção desejada:
```

| Resposta do usuário | Próximo estado | Ação |
|---|---|---|
| `"1"` | `SOU_CLIENTE` | Envia menu de cliente |
| `"2"` | `TRANSFERIDO` | Cria conversa no Chatwoot → equipe Comercial |
| qualquer outro | `MENU_INICIAL` | Reenvia o menu |

---

### Handler: `SouClienteHandler`

**Arquivo**: [SouClienteHandler.java](src/main/java/br/com/sistema/bot/handler/SouClienteHandler.java)

**Ativado quando**: estado = `SOU_CLIENTE`

Menu exibido:

```
Ótimo! Como posso ajudar?

1️⃣ Assistência Técnica
2️⃣ Financeiro
3️⃣ Dúvidas/Sugestões
4️⃣ Cancelamento
5️⃣ Encerrar atendimento
```

| Opção | Próximo estado | Condição | Ação |
|---|---|---|---|
| `"1"` Assistência Técnica | `TRANSFERIDO` | Horário: dom–dom 09h–21h | Transfere para equipe Suporte no Chatwoot |
| `"1"` fora do horário | `MENU_INICIAL` | — | Informa horário de atendimento |
| `"2"` Financeiro | `FINANCEIRO` | — | Envia menu financeiro |
| `"3"` Dúvidas | `TRANSFERIDO` | Seg–sáb 09h–18h | Transfere para equipe Dúvidas/Comercial |
| `"4"` Cancelamento | `TRANSFERIDO` | Seg–sáb 09h–18h | Transfere para equipe Cancelamento |
| `"5"` Encerrar | — | — | Chama `EncerrarHandler` |

A verificação de horário é feita por `HorarioAtendimentoService` usando o fuso de Brasília (`America/Sao_Paulo`).

---

### Handler: `FinanceiroHandler`

**Arquivo**: [FinanceiroHandler.java](src/main/java/br/com/sistema/bot/handler/FinanceiroHandler.java)

**Ativado quando**: estado = `FINANCEIRO`

Menu exibido:

```
💰 Menu Financeiro

1️⃣ Segunda via de boleto
2️⃣ Desbloqueio de confiança
3️⃣ Enviar comprovante de pagamento
4️⃣ Falar com atendente financeiro
5️⃣ Encerrar atendimento
```

| Opção | Próximo estado | Ação |
|---|---|---|
| `"1"` 2ª via de boleto | `AGUARDA_CPF_FATURA` | Pede CPF/CNPJ |
| `"2"` Desbloqueio | `AGUARDA_CPF_DESBLOQUEIO` | Pede CPF/CNPJ |
| `"3"` Comprovante | `FINANCEIRO` | Instrui o usuário a anexar imagem/PDF |
| `"4"` Atendente | `TRANSFERIDO` | Transfere para equipe Financeiro (seg–sáb 09h–18h) |
| `"5"` Encerrar | — | Chama `EncerrarHandler` |

---

### Handler: `ProcessaCpfCnpjHandler`

**Arquivo**: [ProcessaCpfCnpjHandler.java](src/main/java/br/com/sistema/bot/handler/ProcessaCpfCnpjHandler.java)

**Ativado quando**: estado = `AGUARDA_CPF_FATURA` ou `AGUARDA_CPF_DESBLOQUEIO`

**Fluxo interno**:

```
1. Remove caracteres não-numéricos do texto digitado
2. Valida tamanho: 11 dígitos (CPF) ou 14 (CNPJ)
   └─ Inválido → envia erro e aguarda nova tentativa (mantém estado)
3. Consulta Hubsoft:
   HubsoftService.buscarClientePorCpfCnpj(cpfCnpj)
   └─ Não encontrado → informa o usuário e aguarda nova tentativa
4. Cliente encontrado:
   └─ Salva CPF no contextData
   └─ Muda estado para CONFIRMA_IDENTIDADE_FATURA ou CONFIRMA_IDENTIDADE_DESBLOQUEIO
   └─ Envia: "Encontramos o cadastro: *João da Silva*
              Você é o titular desta conta?
              1️⃣ Sim  2️⃣ Não"
```

A consulta ao Hubsoft usa OAuth2 com token cacheado por 50 minutos (`HubsoftAuthService`).

---

### Handler: `ConfirmaCpfHandler`

**Arquivo**: [ConfirmaCpfHandler.java](src/main/java/br/com/sistema/bot/handler/ConfirmaCpfHandler.java)

**Ativado quando**: estado = `CONFIRMA_IDENTIDADE_FATURA` ou `CONFIRMA_IDENTIDADE_DESBLOQUEIO`

**Se o usuário responde "2" (Não)**:
- Volta para `MENU_INICIAL`
- Envia: "Entendido! Verifique seus dados..."

**Se o usuário responde "1" (Sim)**:

**Fluxo para 2ª via de boleto** (`CONFIRMA_IDENTIDADE_FATURA`):
```
1. Recupera CPF do contextData
2. Chama HubsoftService.buscarFaturas(cpfCnpj)
3. Sem faturas em aberto → informa usuário
4. Com fatura:
   ├─ Tem link PDF → WhatsAppService.enviarDocumento(phone, pdfLink)
   └─ Sem PDF     → envia linha digitável como texto
5. Volta para MENU_INICIAL
```

**Fluxo para desbloqueio de confiança** (`CONFIRMA_IDENTIDADE_DESBLOQUEIO`):
```
1. Recupera CPF do contextData
2. Busca o cliente novamente para obter o ID do serviço
3. Chama HubsoftService.desbloquear(idServico)
4. Status "success" → "✅ Desbloqueio realizado com sucesso!"
5. Status diferente  → "❌ Não foi possível..." (limite: 1x a cada 25 dias)
6. Volta para MENU_INICIAL
```

---

### Handler: `EncerrarHandler`

**Arquivo**: [EncerrarHandler.java](src/main/java/br/com/sistema/bot/handler/EncerrarHandler.java)

**Ativado quando**: usuário envia `"sair"` ou `"cancelar"` (qualquer estado)

```
1. conversationStateService.resetar(phone)
   └─ Estado → MENU_INICIAL
   └─ contextData → null
   └─ chatwootConversationId → null
2. Envia:
   "Obrigado por entrar em contato com a ASB Telecom! 😊
   Seu atendimento foi encerrado. Se precisar de ajuda novamente,
   é só nos chamar! Tenha um ótimo dia! 🌟"
```

A próxima mensagem desse número recomeçará do início.

---

## Diagrama de estados completo

```
                    ┌─────────────────────────────────────────────────────┐
 "Oi" (ou qualquer) │              MENU_INICIAL (padrão)                  │
                    └──────────────────┬──────────────────────────────────┘
                                       │
                          ┌────────────┴────────────┐
                         "1"                        "2"
                          │                          │
                    SOU_CLIENTE               TRANSFERIDO (Comercial)
                          │
          ┌───────┬───────┼───────┬───────┐
         "1"     "2"     "3"     "4"     "5"
          │       │       │       │       │
       SUPORTE FINANCEIRO DÚVIDAS CANCEL. ENCERRAR
       (horário)   │     (horário)(horário)
                   │
         ┌────┬───┴────┬────┐
        "1"  "2"      "3"  "4"
         │    │         │    │
    AGU_CPF  AGU_CPF  (doc) FINANCEIRO
    _FATURA  _DEBLOQ       (horário)
         │    │
    CONFIRM CONFIRM
    _FATURA _DEBLOQ
         │    │
    (emite) (desbloqueia)
         │    │
       MENU_INICIAL ◄──── "sair"/"cancelar" (de qualquer estado)
```

---

## Integrações externas

### WhatsApp Cloud API (`WhatsAppService` / `WhatsAppClient`)

| Operação | Descrição |
|---|---|
| `enviarTexto(phone, mensagem)` | Envia mensagem de texto simples |
| `enviarDocumento(phone, url, nome, legenda)` | Envia PDF do boleto |

### Chatwoot (`ChatwootService` / `ChatwootClient`)

Usado nas transferências para atendimento humano:

```
1. criarContato(phone, nome)         → retorna contactId
2. criarConversa(contactId)          → retorna conversationId
3. adicionarNota(conversationId, resumo)
4. transferirParaEquipe(conversationId, teamId)
```

| TeamId | Equipe |
|---|---|
| `SUPORTE` | Assistência Técnica |
| `FINANCEIRO` | Financeiro |
| `DUVIDAS_COMERCIAL` | Dúvidas e Clientes Novos |
| `CANCELAMENTO` | Cancelamentos |

### Hubsoft (`HubsoftService` / `HubsoftClient`)

ERP de gerenciamento de clientes e serviços da ASB Telecom.

| Operação | Endpoint |
|---|---|
| Autenticação | `POST /oauth/token` (token cacheado 50 min) |
| Buscar cliente | `GET /api/cliente/{cpfCnpj}` |
| Buscar faturas | `GET /api/fatura/{cpfCnpj}` |
| Desbloquear serviço | `POST /api/desbloqueio/{idServico}` |

> **Importante**: o `HubsoftAuthService.getToken()` usa `@Cacheable("hubsoft_token")`. O método **nunca** deve ser chamado de dentro do próprio `HubsoftClient` (self-invocation quebra o cache do Spring).

---

## Banco de dados

### `message_log` — Idempotência

| Coluna | Descrição |
|---|---|
| `message_id` | ID único do WhatsApp (`wamid.xxx`) — chave de idempotência |
| `sender_phone` | Número do remetente |

### `conversation_state` — Estado da conversa

| Coluna | Descrição |
|---|---|
| `whatsapp_phone` | Chave natural (unique) |
| `current_state` | Enum `BotState` (ex: `FINANCEIRO`) |
| `context_data` | Dado temporário entre passos (ex: CPF digitado) |
| `chatwoot_conversation_id` | ID da conversa no Chatwoot quando transferido |

---

## Horários de atendimento humano

| Equipe | Dias | Horário (Brasília) |
|---|---|---|
| Suporte Técnico | Domingo a domingo | 09h às 21h |
| Financeiro / Comercial / Cancelamento | Segunda a sábado | 09h às 18h |

Fora do horário, o bot informa o expediente e retorna ao `MENU_INICIAL` em vez de transferir.

---

## Fluxo completo — exemplo "Oi" → 2ª via de boleto

```
Usuário: "Oi"
  → WebhookController valida HMAC
  → WebhookService: tipo text ✓, novo número, estado = MENU_INICIAL
  → MenuInicialHandler: conteúdo ≠ "1" ou "2" → envia menu de boas-vindas

Usuário: "1"
  → MenuInicialHandler: opção 1 → estado = SOU_CLIENTE → envia menu de cliente

Usuário: "2"
  → SouClienteHandler: opção 2 → estado = FINANCEIRO → envia menu financeiro

Usuário: "1"
  → FinanceiroHandler: opção 1 → estado = AGUARDA_CPF_FATURA
  → Bot: "Por favor, informe seu CPF ou CNPJ (somente números):"

Usuário: "12345678901"
  → ProcessaCpfCnpjHandler: valida (11 dígitos ✓)
  → HubsoftService.buscarCliente("12345678901") → "João da Silva"
  → Salva CPF no contextData, estado = CONFIRMA_IDENTIDADE_FATURA
  → Bot: "Encontramos o cadastro: *João da Silva*
          Você é o titular desta conta?
          1️⃣ Sim  2️⃣ Não"

Usuário: "1"
  → ConfirmaCpfHandler: confirmou ✓
  → HubsoftService.buscarFaturas("12345678901")
  → Fatura com PDF → WhatsAppService.enviarDocumento(...)
  → Estado = MENU_INICIAL
  → Bot envia o PDF do boleto
```
