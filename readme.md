# MindCheck Backend

Бэкенд платформы психологического тестирования МТУСИ. Состоит из двух Ktor-сервисов и общего модуля.

## Стек

- **Kotlin 2.2** / **Ktor 3.3**
- **PostgreSQL** + **Exposed** + **Flyway** (миграции)
- **Koin** (DI) + **KSP**
- **JWT (ECDSA/es256)** — авторизация
- **koog 1.2.0-beta** — интеграция с Ollama LLM
- **detekt**, **akkurate** (валидация), **kotlinx.serialization**
- **Gradle** + **Jib** (`buildImage`) для сборки Docker-образов

## Модули

| Модуль | Назначение | Порт |
|---|---|---|
| `main` | REST API: пользователи, тесты, опросы, сессии, категории, роли, админка | `1488` |
| `llm` | Прокси к Ollama: чат-ассистент и интерпретация результатов тестов | `1489` |
| `common` | Общие DTO/типы, разделяемые между `main` и `llm` |  — |

## Структура

```
Backend/main/
├── common/                        # Общие DTO, sealed-типы, enums
├── main/                          # Основной API
│   └── src/main/kotlin/…/main/
│       ├── infrastructure/
│       │   ├── controllers/       # Ktor-роуты (auth, admin/*, testing, survey, ...)
│       │   ├── services/          # Бизнес-логика
│       │   ├── repositories/      # Exposed-репозитории
│       │   ├── models/            # Exposed-таблицы
│       │   └── dto/               # Request/Response DTO
│       ├── plugins/               # Ktor plugins (Auth, Routing, DI, Serialization)
│       └── config/
├── llm/                           # LLM-прокси
│   └── src/main/kotlin/…/llm/
│       ├── infrastructure/controllers/ollama/
│       ├── config/ollama/
│       └── plugins/Koog.kt
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile.main / Dockerfile.llm
└── gradle/libs.versions.toml
```

## Конфигурация

HOCON-конфиги в `<module>/run/{template,development,docker}/application.conf`.

Ключевые параметры `main`:
- `postgres.{host,port,user,password,database}` — подключение к БД
- `authentication.{issuer,audience,privateKey,publicKey}` — JWT
- `smtp.{...}` — рассылка писем верификации

Ключевые параметры `llm`:
- `ollama.url` — адрес Ollama
- `ollama.chatModel` / `ollama.testTranscriptionModel` — модели
- `ollama.testTranscriptionTemperature` / `testTranscriptionMaxOutputTokens`
- `ollama.chatSystemPrompt` / `testTranscriptionSystemPrompt`

## Запуск

### Через Docker Compose

Из корня проекта:

```bash
./setup-and-run.sh          # первый запуск: клонирование + сборка + up
# или
./build-and-run.sh          # сборка + up (если репозитории уже есть)
```

### Локально (без Docker)

Требуется JDK 21, запущенные PostgreSQL и Ollama.

```bash
cd Backend/main

# main API
./gradlew :main:run

# LLM-прокси
./gradlew :llm:run
```

### Сборка Docker-образов вручную

```bash
./gradlew :main:buildImage -x detekt --no-daemon
docker load < main/build/jib-image.tar

./gradlew :llm:buildImage -x detekt --no-daemon
docker load < llm/build/jib-image.tar
```

## Миграции

Flyway-скрипты лежат в `main/src/main/resources/db/migration/`. Применяются автоматически при старте `main`.

## Основные группы эндпоинтов

- `POST /auth/register`, `POST /auth/login`, `POST /auth/admin/login` — авторизация (админский логин отклоняется при отсутствии прав)
- `GET /admin/me` — текущие permissions пользователя
- `GET /user/profile`, `GET /user/profile/sessions`, `.../survey-sessions`
- `GET /tests`, `POST /testing/sessions/...`
- `GET /surveys`, `POST /survey/sessions/...`
- `POST /ollama/chat`, `POST /ollama/test`
- `admin/*` — вся админка (users, roles, tests, surveys, categories, statistics)

Swagger UI поднимается вместе с приложением (см. `configureRouting`).

## Git-workflow

- Ветки от `development`: `{feature|fix|chore|...}/PWPT-{ID}-{название}`
- Коммиты по conventional commits, шаблон enforced git-hook’ом
- MR в `development`, только «зелёная» сборка
