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
mvn spring-boot:run -Dspring.profiles.active=local
```

A aplicação sobe em `http://localhost:8080/api/v1`.

---

## Como Executar os Testes

```bash
mvn test
```

> Os testes ainda não estão implementados — ver item no Roadmap. A estrutura `src/test/java/` está reservada para testes futuros de unit e integração.

---

## Troubleshooting

| Sintoma | Causa provável | Solução |
|---|---|---|
| `403` no POST do webhook | HMAC inválido | Confirme que `WHATSAPP_APP_SECRET` e `WHATSAPP_VERIFY_TOKEN` estão corretos e que o payload não foi modificado por proxy |
| Token Hubsoft expira antes de 50 min | Self-invocation do `@Cacheable` | **Nunca** chame `getToken()` dentro de `HubsoftClient`; use apenas via `HubsoftAuthService` injetado |
| Banco não criado na primeira execução | Falta do parâmetro JDBC | Verifique se `DATASOURCE_URL` contém `createDatabaseIfNotExist=true` |
| Bot não responde mas webhook recebe | Mensagem duplicada bloqueada | Normal — idempotência via `MessageLog`; verifique se `message_id` já consta na tabela |
| Transferência não abre conversa no Chatwoot | Fora do horário de atendimento | Verifique os horários configurados em `HorarioAtendimentoService` e o fuso `America/Sao_Paulo` |
| `application-local.properties` não carregado | Profile não ativo | Execute com `-Dspring.profiles.active=local` |

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

---

## Roadmap de Melhorias (Futuro)

Esta seção consolida melhorias sugeridas para evolução do projeto, priorizando confiabilidade, operação e experiência do time.

### Prioridade Alta

1. **Testes automatizados (unit + integração)**
      - Criar testes unitários para todos os handlers com Mockito, cobrindo fluxos felizes e de erro.
      - Criar testes de integração com `@SpringBootTest` e banco H2/Testcontainers para os fluxos completos de webhook.
      - Configurar JaCoCo com cobertura mínima obrigatória no build (meta inicial: 70%).
      - Benefício: detecta regressões antes de produção e dá confiança para evoluir o fluxo.

2. **Frontend operacional para o bot (painel visual)**
      - Criar um frontend (Angular 20) para **visualizar o histórico de mensagens por conversa** e o **estado atual do fluxo**.
      - Permitir **edição controlada de mensagens de menu/roteiros** (templates), com versionamento e auditoria.
      - Exibir indicadores operacionais: conversas em atendimento humano, encerradas, em erro e fora de horário.
      - Benefício: reduz dependência de ajuste direto em código para mudanças simples de texto e melhora a operação diária.

2. **Contrato backend/frontend sempre sincronizado**
      - Atualizar continuamente o `bot-whatsapp-frontend-context.json` conforme endpoints, DTOs e enums evoluírem.
      - Incluir validação em CI para detectar divergência entre contrato e implementação.
      - Benefício: evita quebra de integração no frontend e acelera desenvolvimento em paralelo.

3. **Observabilidade e monitoração**
      - Publicar métricas com Actuator/Prometheus (latência por handler, taxa de erro por integração, duplicatas bloqueadas por idempotência).
      - Adicionar correlationId em logs para rastrear ponta a ponta (webhook -> handler -> integrações externas).
      - Criar dashboards e alertas (ex.: falhas Hubsoft, pico de erros de assinatura, tempo de resposta elevado).

### Prioridade Média

1. **Persistência de histórico conversacional para análise**
      - Hoje a idempotência persiste `messageId`, mas não o histórico completo do diálogo.
      - Criar tabela/event store para mensagens recebidas/enviadas (com política de retenção), viabilizando trilha operacional e análises futuras.
      - Essa base também viabiliza melhor o frontend visual de atendimento.

2. **Resiliência de integrações externas**
      - Adicionar políticas de retry com backoff, timeout por operação e circuit breaker para Hubsoft/Chatwoot.
      - Considerar fila assíncrona para operações não críticas e reprocessamento seguro em caso de indisponibilidade.

3. **Qualidade de deploy e ambientes**
      - Padronizar CI/CD com etapas de build, testes, cobertura e validações de contrato.
      - Revisar configuração por ambiente para evitar `ddl-auto=update` fora de desenvolvimento, mantendo evolução do schema apenas via Flyway.

4. **Rate limiting e segurança no webhook**
      - Limitar requisições por IP para evitar abuso do endpoint público `/api/v1/webhook/whatsapp`.
      - Considerar allowlist de IPs da Meta (faixas publicadas pela Meta) como camada adicional de defesa além do HMAC.

5. **Gestão de templates de mensagem**
      - Hoje os textos dos menus e respostas estão hardcoded nos handlers.
      - Externalizar para banco ou arquivo de configuração, permitindo alteração sem redeploy e viabilizando o painel administrativo futuro.

### Prioridade Baixa

1. **Expansão de canais e funcionalidades**
      - Preparar base para multi-canal (ex.: Webchat) mantendo o core de fluxo desacoplado do provedor.
      - Evoluir para testes de carga e caos em integrações para validar comportamento em cenários extremos.

2. **Melhorias de produto no atendimento**
      - Biblioteca de respostas/FAQ administrável no painel.
      - Relatórios de funil: entrada no menu, abandono por etapa, transferência por motivo e tempo médio por fluxo.

3. **Graceful shutdown**
      - Garantir que mensagens em processamento sejam concluídas antes do encerramento da JVM, evitando estado inconsistente no `ConversationState`.

4. **ADRs (Architecture Decision Records)**
      - Registrar formalmente decisões tomadas (ex.: migração de labels Chatwoot → `ConversationState` em banco) em arquivos `docs/adr/`.
      - Facilita onboarding e justifica escolhas para novos membros do time.
