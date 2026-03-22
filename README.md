# ELH — Every Losted History

Bot de Discord que guarda a memória coletiva do servidor: salva mídias automaticamente,
busca imagens, conversa via IA e cria enquetes semanais com as melhores mídias.

## Stack

| Camada | Tecnologia |
|---|---|
| Bot | Discord JDA 5 |
| Serviços | Spring Boot 3.2 + Java 21 |
| Event Streaming | Apache Kafka |
| Message Delivery | RabbitMQ |
| Storage | AWS S3 |
| Banco | PostgreSQL 16 |
| Cache / Sessões | Redis 7 |
| Busca | Elasticsearch 8 |
| IA | Claude API (Haiku) |
| Resiliência | Resilience4j |
| Observabilidade | Prometheus + Grafana + Loki |
| Orquestração | Kubernetes (VPS + AWS) |

## Estrutura do Monorepo

```
elh/
├── pom.xml                  ← Parent POM (versões centralizadas)
├── docker-compose.yml       ← Infra local completa
├── libs/
│   └── elh-commons/         ← Eventos Kafka + constantes compartilhadas
├── services/
│   ├── discord-gateway/     ← :8080 — recebe eventos do Discord
│   ├── media-ingestor/      ← :8081 — baixa e salva mídias no S3
│   ├── search-service/      ← :8082 — busca interna e Google Images
│   ├── chat-service/        ← :8083 — conversa via Claude API
│   ├── poll-scheduler/      ← :8084 — enquete toda segunda 20h
│   ├── notification-service/← :8085 — entrega notificações no Discord
│   ├── stats-service/       ← :8086 — métricas e ranking
│   └── api-gateway/         ← :8079 — roteamento HTTP interno
├── infra/
│   ├── postgres/init.sql    ← Schema inicial do banco
│   ├── prometheus/          ← Config de scraping
│   ├── grafana/             ← Dashboards e datasources
│   ├── rabbitmq/            ← Config do broker
│   └── localstack/          ← Init do S3 local
└── k8s/                     ← Manifests Kubernetes
```

## Como rodar localmente

### Pré-requisitos
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### 1. Subir a infra

```bash

git clone https://github.com/seu-user/elh.git
cd elh

docker compose up -d

docker compose logs -f kafka rabbitmq postgres
```

### 2. Verificar se tudo está OK

```bash
docker compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

docker compose exec postgres psql -U elh -c "\dt"

open http://localhost:15672  # elh/elh

open http://localhost:8090

docker compose exec localstack awslocal s3 ls
```

### 3. Buildar os módulos

```bash

mvn clean install -pl libs/elh-commons

mvn clean package -pl services/discord-gateway
```

### 4. Configurar variáveis de ambiente

Copie o `.env.example` e preencha:

```bash
cp .env.example .env
```

Variáveis obrigatórias:
```env
DISCORD_BOT_TOKEN=seu_token_aqui
ANTHROPIC_API_KEY=sua_key_aqui
AWS_ACCESS_KEY_ID=test                    # LocalStack usa "test"
AWS_SECRET_ACCESS_KEY=test
AWS_S3_BUCKET=elh-media-dev
AWS_ENDPOINT_OVERRIDE=http://localhost:4566  # LocalStack
```

## Roadmap de implementação

- [x] **Fase 0** — Monorepo + Commons + Docker Compose
- [ ] **Fase 1** — discord-gateway + media-ingestor + Kafka + S3
- [ ] **Fase 2** — search-service + Elasticsearch + /buscar + /img
- [ ] **Fase 3** — chat-service + Claude API + Redis sessions
- [ ] **Fase 4** — poll-scheduler + enquete automática
- [ ] **Fase 5** — Observabilidade completa + Resilience4j
- [ ] **Fase 6** — Kubernetes (VPS + AWS) + WireGuard

## Fluxo de eventos Kafka

```
discord-gateway  ──► discord.messages  ──► stats-service
                 ──► media.detected    ──► media-ingestor
                 ──► discord.commands  ──► search-service
                                       ──► chat-service
                                       ──► poll-scheduler

media-ingestor   ──► media.saved       ──► search-service
                                       ──► notification-service
                                       ──► stats-service

chat-service     ──► chat.responded    ──► notification-service

poll-scheduler   ──► poll.created      ──► notification-service
                 ──► poll.closed       ──► notification-service

notification-service ──► RabbitMQ ──► discord-gateway (entrega final)
```
