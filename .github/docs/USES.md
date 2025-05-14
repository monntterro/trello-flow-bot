# Uses

To use Trello webhooks locally, you will need a static IP address. Alternatively, you can use a tunneling service such
as [ngrok](https://ngrok.com) _(not appropriate for production)_.

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

### 4. 🌐 Set up public access to the app

Trello requires a public URL to send webhooks. If you have a domain, you can configure it using Nginx and SSL.

#### 4.1 Ngrok (Not appropriate for production)

Start the ngrok service, redirecting requests to the application running on `http://localhost:${APP_PORT}` _(by default
APP_PORT = 8081)_.

- **Windows/MacOS:**

```shell
  docker run -d \
  -e NGROK_AUTHTOKEN=your_token \
  -p 4040:4040 \
  ngrok/ngrok:latest http http://host.docker.internal:8081
```

- **Linux:**

```shell
  docker run -d \
  --network host \
  -e NGROK_AUTHTOKEN=your_token \
  ngrok/ngrok:latest http http://localhost:8081
```

Go to `http://localhost:4040` and you will see a forwarding URL or get it by http GET request in `PublicURL` block:

```http request
http://your.server.ip:4040/api/tunnels
```

Copy it and use in the 6th step for the environmental variable `APP_URL`.

### 5. 🔑 Prepare Trello Access

Register your application with Trello
by [this guide](https://telegra.ph/How-to-get-a-key-and-a-token-from-Trello-05-04).
Make sure that you have set the `APP_URL` to `Allowed origins` field.
And copy api key and secret to the 6th step for the
variables `TRELLO_API_KEY` and `TRELLO_API_SECRET`.

### 6. 🏁 Start Application

Copy and set environments:

```bash
  cp .example.env .env
```

#### 🔧 6.1 Run Locally

Start the postgres:

```bash
  docker-compose up -d postgres
```

Start the application:

```bash
  ./gradlew bootRun
```

#### 🐳 6.2 Run with Docker Compose

Start application:

```bash
  docker-compose up -d
```

## 🧱 Stack

### Core

- Spring Boot Web: `3.4.1`
- Spring Boot Data JPA: `3.3.4`
- Spring Security Crypto: `6.2.2`

### Databases and Migrations

- PostgreSQL Driver: `42.6.1`
- Flyway: `11.1.0`

### Integrations

- Telegram Bots Java API: `8.3.0`
- Telegram Bots Spring Boot Long Polling Starter: `8.3.0`

### Utility Libraries

- Lombok: `1.18.36`
- Scrive: `8.3.3`

### Testing

- JUnit Platform Launcher: `1.11.4`

