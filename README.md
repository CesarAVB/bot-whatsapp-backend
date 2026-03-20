# Bot WhatsApp - ASB Telecom

Backend Spring Boot que orquestra o atendimento automatizado no WhatsApp, com
integração com Chatwoot (atendimento humano) e Hubsoft (dados financeiros e
desbloqueio de confiança).

## Visao Geral

- Entrada principal por webhook da WhatsApp Cloud API.
- Validacao de seguranca por assinatura HMAC-SHA256 no header `X-Hub-Signature-256`.
- Idempotencia por `message_id` (evita reprocessamento de webhook duplicado).
- Roteamento por estado da conversa (`BotState`) e handlers especializados.
- Transferencia para humano via Chatwoot quando necessario.

## Stack

| Tecnologia | Versao |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.0 |
| Spring Web / Validation / Data JPA | Gerenciado pelo Spring Boot |
| Spring Cache + Caffeine | Gerenciado pelo Spring Boot |
| MySQL | 8.x |
| SpringDoc OpenAPI | 2.7.0 |
| JUnit 5 + Mockito + Surefire | spring-boot-starter-test |

## Arquitetura Resumida

```text
WhatsApp Cloud API
    -> /api/v1/webhook/whatsapp
    -> WebhookController (verifica assinatura HMAC)
    -> WebhookService (filtros + idempotencia + roteamento)
    -> Handlers de estado
    -> Services (WhatsApp, Chatwoot, Hubsoft)
    -> Persistencia (ConversationState + MessageLog)
```

## Fluxo de Estado

Estados principais (`BotState`):

- `MENU_INICIAL`
- `SOU_CLIENTE`
- `FINANCEIRO`
- `AGUARDA_CPF_FATURA`
- `AGUARDA_CPF_DESBLOQUEIO`
- `CONFIRMA_IDENTIDADE_FATURA`
- `CONFIRMA_IDENTIDADE_DESBLOQUEIO`
- `TRANSFERIDO`
- `ENCERRADO`

Comando global:

- `sair` ou `cancelar` encerra o atendimento em qualquer estado.

## Endpoints

Com `server.servlet.context-path=/api/v1`, os endpoints ficam:

- `GET /api/v1/webhook/whatsapp`
    - Endpoint de verificacao da Meta (`hub.mode`, `hub.verify_token`, `hub.challenge`).
- `POST /api/v1/webhook/whatsapp`
    - Recebe evento de mensagem e processa fluxo do bot.

Swagger/OpenAPI:

- `http://localhost:8080/api/v1/swagger-ui/index.html`

## Como Rodar Localmente

### 1) Pre-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8.x (porta 3306)

### 2) Banco de dados local

O projeto usa por padrao:

- Banco: `bot_whatsapp`
- Usuario: `root`
- Senha: `root`

Configuracao local ja existe em `src/main/resources/application-local.properties`
com `createDatabaseIfNotExist=true`.

### 3) Variaveis obrigatorias (ambiente)

Configure no sistema operacional, no terminal, ou em um arquivo de ambiente da
sua IDE:

```properties
# WhatsApp Cloud API
WHATSAPP_PHONE_NUMBER_ID=
WHATSAPP_ACCESS_TOKEN=
WHATSAPP_APP_SECRET=
WHATSAPP_VERIFY_TOKEN=

# Chatwoot
CHATWOOT_BASE_URL=
CHATWOOT_API_KEY=
CHATWOOT_INBOX_ID=

# Hubsoft
HUBSOFT_BASE_URL=
HUBSOFT_CLIENT_ID=
HUBSOFT_CLIENT_SECRET=
HUBSOFT_USERNAME=
HUBSOFT_PASSWORD=
HUBSOFT_GRANT_TYPE=password
```

### 4) Executar aplicacao

```bash
mvn spring-boot:run
```

Aplicacao sobe em:

- `http://localhost:8080/api/v1`

## Configuracao de Producao

Variaveis principais em `application-prod.properties`:

- `SERVER_PORT`
- `DATASOURCE_URL`
- `DATASOURCE_USERNAME`
- `DATASOURCE_PASSWORD`
- `CORS_ALLOWED_ORIGINS`

E todas as variaveis de integracao (WhatsApp, Chatwoot e Hubsoft).

## Testes

Executar todos os testes:

```bash
mvn test
```

Executar uma classe especifica:

```bash
mvn -Dtest=WebhookServiceTest test
```

Gerar cobertura JaCoCo (sem plugin declarado no `pom.xml`):

```bash
mvn org.jacoco:jacoco-maven-plugin:0.8.14:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.14:report
```

Relatorios:

- `target/site/jacoco/index.html`
- `target/site/jacoco/jacoco.csv`
- `target/site/jacoco/jacoco.xml`

## Estrutura do Projeto

```text
src/main/java/br/com/sistema/bot
    config/       Configuracoes (CORS, cache, OpenAPI, properties)
    controller/   Entradas HTTP
    client/       Clientes HTTP para APIs externas
    dtos/         Contratos de request/response
    entity/       Entidades JPA (estado e idempotencia)
    enums/        Enumeracoes do dominio
    exception/    Tratamento global de excecoes
    handler/      Regras de fluxo por estado
    model/        Modelos internos (ConversationContext)
    repository/   Repositorios JPA
    service/      Orquestracao de negocio
```

## Contrato com Frontend

O arquivo `bot-whatsapp-frontend-context.json` e o contrato oficial para
consumo do frontend Angular.

Sempre que houver mudanca em endpoint, DTO ou enum no backend, atualize esse
arquivo para manter sincronismo entre backend e frontend.
