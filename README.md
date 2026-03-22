# ELH — Every Losted History

Bot de Discord que guarda a memoria coletiva do servidor: salva midias automaticamente,
busca imagens, conversa via IA e cria enquetes semanais com as melhores midias.

## Stack

| Camada | Tecnologia |
|---|---|
| Bot | Discord JDA 5 |
| Servicos | Spring Boot 3.2 + Java 21 |
| Event Streaming | Apache Kafka (KRaft) |
| Message Delivery | RabbitMQ |
| Storage | AWS S3 (LocalStack p/ dev) |
| Banco | PostgreSQL 16 |
| Cache / Sessoes | Redis 7 |
| Busca | Elasticsearch 8 |
| IA | Claude API (Haiku) |
| API Gateway | Spring Cloud Gateway |
| Resiliencia | Resilience4j |
| Observabilidade | Prometheus + Grafana |
| Orquestracao | Kubernetes (futuro) |

## Estrutura do Monorepo

```
elh/
├── pom.xml                     ← Parent POM (versoes centralizadas)
├── Dockerfile                  ← Multi-stage build (todos os servicos)
├── docker-compose.yml          ← Infraestrutura (Kafka, PG, Redis, etc)
├── docker-compose.override.yml ← Servicos da aplicacao (auto-mergeado)
├── .env                        ← Tokens e secrets (nao versionado)
├── libs/
│   └── elh-commons/            ← Eventos Kafka + constantes compartilhadas
├── services/
│   ├── discord-gateway/        ← :8080 — recebe eventos do Discord
│   ├── media-ingestor/         ← :8081 — baixa e salva midias no S3
│   ├── search-service/         ← :8082 — busca interna e Google Images
│   ├── chat-service/           ← :8083 — conversa via Claude API
│   ├── poll-scheduler/         ← :8084 — enquete semanal automatica
│   ├── notification-service/   ← :8085 — entrega notificacoes no Discord
│   ├── stats-service/          ← :8086 — metricas e ranking
│   └── api-gateway/            ← :8000 — roteamento HTTP (Spring Cloud Gateway)
├── infra/
│   ├── postgres/init.sql       ← Schema inicial do banco
│   ├── prometheus/             ← Config de scraping
│   ├── grafana/                ← Dashboards e datasources
│   ├── rabbitmq/               ← Config do broker
│   └── localstack/             ← Init do S3 local
└── k8s/                        ← Manifests Kubernetes (futuro)
```

## Como rodar localmente

### Pre-requisitos
- Docker + Docker Compose (so isso — o build roda dentro do Docker)

### 1. Configurar variaveis de ambiente

```bash
cp .env.example .env
```

Edite o `.env` com seus tokens:
```env
DISCORD_BOT_TOKEN=seu_token_aqui       # obrigatorio
ANTHROPIC_API_KEY=sua_key_aqui         # necessario pro chat-service
```

### 2. Subir tudo (infra + servicos)

```bash
docker compose up -d --build
```

O primeiro build demora (Maven baixa dependencias), depois fica cacheado.

### 3. Acompanhar logs

```bash
# Todos os servicos
docker compose logs -f

# Apenas o bot
docker compose logs -f discord-gateway

# Apenas a infra
docker compose logs -f kafka rabbitmq postgres
```

### 4. Subir apenas servicos especificos

```bash
# So o bot (sobe dependencias automaticamente: Kafka, RabbitMQ)
docker compose up -d discord-gateway

# Bot + chat (sobe Kafka, RabbitMQ, Postgres, Redis)
docker compose up -d discord-gateway chat-service
```

### 5. Verificar se tudo esta OK

```bash
# Kafka
docker compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Postgres
docker compose exec postgres psql -U elh -c "\dt"

# RabbitMQ UI: http://localhost:15672 (elh/elh)
# Kafka UI:    http://localhost:8090
# Grafana:     http://localhost:3000 (admin/admin)
# Prometheus:  http://localhost:9090

# S3 local
docker compose exec localstack awslocal s3 ls
```

### 6. Derrubar tudo

```bash
docker compose down       # para os containers
docker compose down -v    # para e limpa volumes (banco, kafka, etc)
```

## Portas

| Servico | Porta |
|---|---|
| api-gateway | 8000 |
| discord-gateway | 8080 |
| media-ingestor | 8081 |
| search-service | 8082 |
| chat-service | 8083 |
| poll-scheduler | 8084 |
| notification-service | 8085 |
| stats-service | 8086 |
| Kafka | 9092 |
| Kafka UI | 8090 |
| RabbitMQ | 5672 / 15672 (UI) |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Elasticsearch | 9200 |
| Prometheus | 9090 |
| Grafana | 3000 |
| LocalStack (S3) | 4566 |

## Roadmap

- [x] **Fase 0** — Monorepo + Commons + Docker Compose
- [x] **Fase 1** — discord-gateway (JDA + Kafka + RabbitMQ)
- [x] **Fase 2** — media-ingestor (S3 + Postgres + thumbnails)
- [x] **Fase 3** — search-service (Elasticsearch + Google Images)
- [x] **Fase 4** — chat-service (Claude API + Redis sessions)
- [x] **Fase 5** — poll-scheduler (enquete semanal automatica)
- [x] **Fase 6** — notification-service (entrega de notificacoes)
- [x] **Fase 7** — stats-service (metricas + Prometheus counters)
- [x] **Fase 8** — api-gateway (Spring Cloud Gateway + rate limiting)
- [ ] **Fase 9** — Testes locais + Docker build completo
- [ ] **Fase 10** — Kubernetes (VPS + AWS)

## Fluxo de eventos Kafka

```
discord-gateway  ──► discord.messages   ──► stats-service
                 ──► discord.reactions  ──► stats-service
                 ──► media.detected     ──► media-ingestor
                 ──► discord.commands   ──► search-service
                                        ──► chat-service
                                        ──► poll-scheduler
                                        ──► stats-service

media-ingestor   ──► media.saved        ──► search-service
                                        ──► notification-service
                                        ──► stats-service

chat-service     ──► chat.responded     ──► notification-service

poll-scheduler   ──► poll.created       ──► notification-service
                 ──► poll.closed        ──► notification-service
```

## Fluxo de entrega (RabbitMQ)

```
Qualquer servico ──► RabbitMQ (discord.outbound) ──► discord-gateway ──► Discord API
```
