# Deployment

To use Trello webhooks locally, you will need a static IP address. Alternatively, you can use a tunneling service such
as [ngrok](https://ngrok.com).

## 📦 Prerequisites

Before running the application locally, ensure the following are installed on your system:

- Java 17+
- Gradle
- Docker
- Docker Compose
- Static IP address or tunneling service (e.g., ngrok)

## 🚀 Local Setup Instructions

### 1. 🔽 Clone the Repository

```shell
  git clone https://github.com/monntterro/trello-flow-bot.git
```

### 2. 🎯 Register Telegram Bot

Register your bot with [@BotFather](https://t.me/botfather) and obtain the token. Set the token in the 4th step for the
variable `BOT_TOKEN`.

### 3. 🌐 Start Ngrok Tunnel

Start the ngrok service to expose your local server to the internet.

```shell
  docker run -it \
  -e NGROK_AUTHTOKEN=your_token \
  -p 4040:4040 \
  ngrok/ngrok:3-alpine http http://host.docker.internal:8081
```

Copy the forwarding URL that is provided by ngrok and use it in the 4th step for the variable
`TRELLO_WEBHOOKS_BASE_URL`.

### 4. 🏁 Start Application

#### 🔧 4.1 Run Locally (via Gradle)

Set environments

```bash
  cp .example.env .env
```

Start the postgres:

```bash
  docker-compose up -d postgres
```

Start the application:

```bash
  ./gradlew bootRun
```

#### 🐳 4.2 Run with Docker Compose

Set environments:

```bash
  cp .example.docker.env .docker.env
```

Start application:

```bash
  docker-compose up -d
```

## 🧱 Stack

### Core

- Spring Boot Web: `3.4.1`
- Spring Boot Data JPA: `3.3.4`
- Spring Security Crypto: `6.2.2`
- Spring Cloud OpenFeign: `4.1.2`

### Databases and Migrations

- PostgreSQL Driver: `42.6.1`
- Flyway: `11.1.0`

### Integrations

- Telegram Bots Java API: `8.3.0`
- Telegram Bots Spring Boot Long Polling Starter: `8.3.0`

### Utility Libraries

- Lombok: `1.18.36`

### Testing

- JUnit Platform Launcher: `1.11.4`

