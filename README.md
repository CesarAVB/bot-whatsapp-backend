# Bot WhatsApp — ASB Telecom

Backend Spring Boot que orquestra o atendimento automatizado via WhatsApp para a ASB Telecom.
Recebe mensagens diretamente da **WhatsApp Cloud API**, processa o fluxo de atendimento por meio de uma máquina de estados persistida em banco de dados, e aciona o **Chatwoot** apenas quando é necessário transferir o cliente para um atendente humano.

---

## Visão Geral

```
Cliente (WhatsApp)
        │
        ▼
WhatsApp Cloud API  ──────────────────────────────────────────────────────────┐
        │ POST /api/v1/webhook/whatsapp                                        │
        ▼                                                                      │
WebhookController                                                              │
  └─ Valida assinatura HMAC-SHA256 (X-Hub-Signature-256)                      │
        │                                                                      │
        ▼                                                                      │
WebhookService                                                                 │
  └─ Filtros (somente texto) + Idempotência (MessageLog)                      │
  └─ Carrega ConversationState do banco                                        │
  └─ Roteia para o Handler correto pelo BotState                              │
        │                                                                      │
        ├──► MenuInicialHandler                                                │
        ├──► SouClienteHandler                                                 │
        ├──► FinanceiroHandler                                                 │
        ├──► ProcessaCpfCnpjHandler ──► Hubsoft API                           │
        ├──► ConfirmaCpfHandler     ──► Hubsoft API (fatura / desbloqueio)    │
        └──► EncerrarHandler                                                   │
                │                                                              │
                ▼ (apenas na transferência)                                    │
          ChatwootService ──► Chatwoot API (cria conversa + atribui equipe)   │
                │                                                              │
                └──────────────────────────────────────────────────────────────┘
                      WhatsAppService ──► WhatsApp Cloud API (envia resposta)
```

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.0 |
| Spring Data JPA | Gerenciado pelo Spring Boot |
| Spring Cache + Caffeine | Gerenciado pelo Spring Boot |
| MySQL | 8.x |
| SpringDoc OpenAPI | 2.7.0 |
| Lombok | Gerenciado pelo Spring Boot |

---

## Fluxo de Estados

O bot é **stateless por design** — o estado de cada conversa é persistido na tabela `conversation_state` do banco, sem sessão em memória.

```
                    [nova mensagem]
                          │
                    MENU_INICIAL ◄────────────── "sair" / "cancelar" (qualquer estado)
                     ┌────┴────┐
                    "1"       "2"
                     │         │
               SOU_CLIENTE  TRANSFERIDO (Comercial)
            ┌───┬───┬───┐
           "1" "2" "3" "4"
            │   │   │   │
         TRANS FIN TRANS TRANS
         (Sup) │  (Dúv) (Can)
               │
           FINANCEIRO
        ┌───┬───┬───┐
       "1" "2" "3" "4"
        │   │   │   │
 AGUARDA  AGUARDA inf  TRANS
 CPF_FAT  CPF_DES     (Fin)
        │   │
        ▼   ▼
  CONFIRMA_IDENTIDADE
    (FATURA | DESBLOQUEIO)
        │
       "1" ──► ação no Hubsoft → MENU_INICIAL
       "2" ──► MENU_INICIAL
```

| Estado | Descrição |
|---|---|
| `MENU_INICIAL` | Exibe boas-vindas e aguarda opção |
| `SOU_CLIENTE` | Submenu de cliente cadastrado |
| `FINANCEIRO` | Submenu financeiro |
| `AGUARDA_CPF_FATURA` | Aguardando CPF/CNPJ para segunda via |
| `AGUARDA_CPF_DESBLOQUEIO` | Aguardando CPF/CNPJ para desbloqueio |
| `CONFIRMA_IDENTIDADE_FATURA` | Aguardando confirmação do titular (fatura) |
| `CONFIRMA_IDENTIDADE_DESBLOQUEIO` | Aguardando confirmação do titular (desbloqueio) |
| `TRANSFERIDO` | Conversa com atendente humano — bot silenciado |
| `ENCERRADO` | Conversa encerrada — próxima mensagem reinicia em `MENU_INICIAL` |

> **Comando global:** digitar `sair` ou `cancelar` em qualquer estado encerra o atendimento imediatamente.

---

## Integrações Externas

| Sistema | Finalidade | Quando acionado |
|---|---|---|
| **WhatsApp Cloud API** | Envio de mensagens e documentos (boleto) | Em toda resposta do bot |
| **Hubsoft** | Consulta de cliente, faturas e desbloqueio de confiança | Nos fluxos financeiros |
| **Chatwoot** | Abertura de conversa e atribuição de equipe | Somente na transferência para humano |

### Regras de horário (fuso: America/Sao_Paulo)

| Equipe | Horário |
|---|---|
| Suporte Técnico | Domingo a domingo, 09h às 21h |
| Financeiro / Comercial / Cancelamento | Segunda a sábado, 09h às 18h |

Fora do horário o bot informa o cliente e retorna ao menu inicial, sem abrir conversa no Chatwoot.

---

## Endpoints

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/api/v1/webhook/whatsapp` | Verificação do webhook pela Meta (`hub.challenge`) |
| `POST` | `/api/v1/webhook/whatsapp` | Recebe eventos de mensagem da WhatsApp Cloud API |

Documentação interativa (Swagger):

```
http://localhost:8080/api/v1/swagger-ui/index.html
```

---

## Como Rodar Localmente

### Pré-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8.x rodando na porta 3306

### 1. Clone o repositório

```bash
git clone <url-do-repositorio>
cd bot-whatsapp-backend
```

### 2. Configure as variáveis de ambiente

Crie ou edite `src/main/resources/application-local.properties` com os valores reais (este arquivo **não é commitado**):

```properties
# WhatsApp Cloud API (Meta)
WHATSAPP_PHONE_NUMBER_ID=<phone_number_id>
WHATSAPP_ACCESS_TOKEN=<access_token_permanente>
WHATSAPP_APP_SECRET=<app_secret_do_app_meta>
WHATSAPP_VERIFY_TOKEN=<token_que_voce_define_no_painel_meta>

# Chatwoot (usado apenas na transferência para humano)
CHATWOOT_BASE_URL=https://chat.asbtelecom.com.br/api/v1/accounts/1
CHATWOOT_API_KEY=<api_key>
CHATWOOT_INBOX_ID=<id_do_inbox_whatsapp>

# Hubsoft
HUBSOFT_BASE_URL=https://api.asbnetwork.hubsoft.com.br
HUBSOFT_CLIENT_ID=6
HUBSOFT_CLIENT_SECRET=<client_secret>
HUBSOFT_USERNAME=manager@asbtelecom.com.br
HUBSOFT_PASSWORD=<password>
HUBSOFT_GRANT_TYPE=password
```

O banco `bot_whatsapp` é criado automaticamente na primeira execução (`createDatabaseIfNotExist=true`).
Credenciais padrão locais: usuário `root`, senha `root`.

### 3. Execute

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080/api/v1`.

---

## Variáveis de Ambiente (Produção)

Configuradas via `application-prod.properties` ou variáveis do sistema:

| Variável | Descrição |
|---|---|
| `SERVER_PORT` | Porta do servidor (padrão: `8080`) |
| `DATASOURCE_URL` | URL JDBC do banco MySQL |
| `DATASOURCE_USERNAME` | Usuário do banco |
| `DATASOURCE_PASSWORD` | Senha do banco |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas, separadas por vírgula |
| `WHATSAPP_PHONE_NUMBER_ID` | Phone Number ID da Meta |
| `WHATSAPP_ACCESS_TOKEN` | Token permanente da WhatsApp Cloud API |
| `WHATSAPP_APP_SECRET` | App Secret do aplicativo Meta |
| `WHATSAPP_VERIFY_TOKEN` | Token de verificação do webhook |
| `CHATWOOT_BASE_URL` | URL base da conta Chatwoot |
| `CHATWOOT_API_KEY` | Chave de API do Chatwoot |
| `CHATWOOT_INBOX_ID` | ID do inbox WhatsApp no Chatwoot |
| `HUBSOFT_BASE_URL` | URL base da API Hubsoft |
| `HUBSOFT_CLIENT_ID` | Client ID OAuth2 |
| `HUBSOFT_CLIENT_SECRET` | Client Secret OAuth2 |
| `HUBSOFT_USERNAME` | Usuário Hubsoft |
| `HUBSOFT_PASSWORD` | Senha Hubsoft |
| `HUBSOFT_GRANT_TYPE` | Tipo de grant (`password`) |

---

## Estrutura de Pacotes

```
br.com.sistema.bot
  ├── config/       Configurações (CORS, cache Caffeine, OpenAPI, Properties)
  ├── controller/   WebhookController (GET verificação + POST eventos)
  ├── client/       Clientes HTTP para APIs externas (WhatsApp, Chatwoot, Hubsoft)
  ├── dtos/
  │   ├── request/  Payloads do webhook WhatsApp Cloud API
  │   └── response/ Respostas das APIs externas
  ├── entity/       ConversationState + MessageLog (JPA)
  ├── enums/        BotState, TeamId
  ├── exception/    GlobalExceptionHandler
  ├── handler/      Lógica de fluxo por estado (um handler por BotState)
  ├── model/        ConversationContext (record interno entre camadas)
  ├── repository/   ConversationStateRepository + MessageLogRepository
  └── service/      WebhookService, ChatwootService, WhatsAppService,
                    HubsoftService, HubsoftAuthService,
                    ConversationStateService, HorarioAtendimentoService,
                    MessageLogService
```

---

## Decisões de Arquitetura

| Decisão | Motivo |
|---|---|
| Webhook direto da Meta (sem Chatwoot no meio) | Elimina intermediário desnecessário, reduz latência e dependências |
| Estado em banco (`ConversationState`) em vez de labels do Chatwoot | Evita round-trip de API para cada mensagem; estado fica sob controle total do backend |
| CPF/CNPJ em `contextData` da entidade | Dispensa busca no histórico de mensagens; dado disponível imediatamente no handler seguinte |
| `HubsoftAuthService` separado de `HubsoftClient` | Evita self-invocation do `@Cacheable` (Spring proxy não intercepta chamadas internas) |
| Chatwoot acionado somente na transferência | Mantém o Chatwoot como ferramenta de atendimento humano, não como orquestrador |
