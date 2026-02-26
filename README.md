# VeraAI — Персонализированный AI-помощник онколога

**VeraAI (кодовое название проекта PATH)** — веб-сервис для автоматизированного анализа соответствия лечения онкологических пациентов клиническим стандартам. Система принимает медицинские документы (история болезни, план лечения), извлекает из них структурированные данные с помощью LLM и выявляет расхождения между назначенной терапией и рекомендуемыми протоколами.

---

## Содержание

1. [Быстрый запуск](#быстрый-запуск)
2. [Системные требования](#системные-требования)
3. [User Guide](#user-guide)
4. [Архитектура и логика работы](#архитектура-и-логика-работы)
5. [Структура проекта](#структура-проекта)
6. [API Reference](#api-reference)
7. [Переменные окружения](#переменные-окружения)

---

## Быстрый запуск

### Через Docker Compose (рекомендуется)

**1. Создайте файл `.env` с API-ключом:**

```env
YANDEX_CLOUD_API_KEY=<ваш_ключ>
```

**2. Запустите все сервисы:**

```bash
docker compose up --build -d
```

**3. Откройте браузер:**

```
http://localhost:8080
```

Приложение, PostgreSQL и MinIO поднимаются автоматически. При первом запуске сборка займёт ~2–3 минуты.

---

### Локальный запуск (без Docker)

**Предварительные требования:** запущены PostgreSQL и MinIO.

```bash
# Установить переменные окружения
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/path
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export MINIO_ENDPOINT=http://localhost:9000
export YANDEX_CLOUD_API_KEY=<ваш_ключ>

# Запустить приложение
./mvnw spring-boot:run
```

**Запуск только инфраструктуры через Docker:**

```bash
docker compose up postgres minio -d
./mvnw spring-boot:run
```

---

## Системные требования

### Для запуска через Docker

| Компонент | Минимум |
|---|---|
| Docker Engine | 24+ |
| Docker Compose | v2.20+ |
| RAM | 2 GB свободной |
| Диск | 5 GB свободного места |
| Сеть | Доступ к `rest-assistant.api.cloud.yandex.net` |

### Для локальной разработки

| Компонент | Версия |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9+ (или использовать `./mvnw`) |
| PostgreSQL | 15+ |
| MinIO | RELEASE.2024+ |

---

## User Guide

Руководство для врачей-онкологов и медицинских экспертов.

### Регистрация и вход

1. Откройте `http://localhost:8080` — откроется лендинг-страница.
2. Нажмите **«Начать бесплатно»** и заполните форму регистрации (логин, email, пароль).
3. После регистрации войдите в систему — откроется **Панель врача**.

---

### Добавление пациента

1. На Панели врача нажмите кнопку **«Добавить пациента»**.
2. В открывшемся модальном окне:
   - **История болезни** *(обязательно)* — загрузите файл PDF или TXT с историей болезни пациента.
   - **План лечения** *(необязательно)* — загрузите файл PDF или TXT с текущим планом лечения.
3. Нажмите **«Загрузить и обработать»**.

Система автоматически:
- сохранит документы в защищённом хранилище;
- извлечёт структурированные медицинские данные (диагноз, стадию, историю лечения, результаты биопсии и т.д.) с помощью AI;
- отобразит карточку пациента со статусом обработки.

> **Время обработки:** обычно 15–45 секунд. Статус обновляется при перезагрузке страницы.

---

### Просмотр карточки пациента

Кликните на строку пациента или нажмите **«Открыть»** — откроется карточка со структурированными данными:

| Раздел | Содержимое |
|---|---|
| **Основные данные** | ФИО/инициалы, дата рождения, диагноз, стадия, подтип |
| **История лечения** | Виды терапии, схемы, периоды, динамика |
| **Результаты биопсии** | Дата, вид исследования, результат (ER/PR/HER2/Ki67) |
| **Консультации** | Дата, рекомендация |
| **Результаты визуализации** | ПЭТ-КТ, МРТ, КТ — дата, тип, находки |

Со страницы карточки можно:
- Скачать исходные документы (кнопки **«Скачать историю болезни»** / **«Скачать план лечения»**).
- Перейти к AI-анализу (кнопка **«Открыть AI-анализ»**).

---

### AI-анализ соответствия лечения

1. На карточке пациента нажмите **«Открыть AI-анализ»** или выберите пункт **AI-анализ** в боковом меню.
2. На странице AI-анализа нажмите **«Запустить анализ»**.

AI сопоставит историю болезни с планом лечения и клиническими стандартами. Результат включает:

- **Оценка оптимальности** — общий вывод о соответствии лечения стандартам.
- **Расхождения** — таблица с конкретными несоответствиями: тип расхождения, текущее назначение, рекомендуемое.
- **Рекомендации** — список конкретных рекомендаций по изменению схемы лечения.
- **Источники** — клинические руководства, на которые ссылается AI.

> **Важно:** система является инструментом поддержки принятия решений. Все выводы AI требуют профессиональной оценки врача.

---

### Поиск и фильтрация

На Панели врача используйте строку поиска для фильтрации пациентов по имени или диагнозу — фильтрация происходит мгновенно, без перезагрузки страницы.

---

## Архитектура и логика работы

### Стек технологий

| Слой | Технология |
|---|---|
| Backend | Spring Boot 4.0.2, Java 21 |
| База данных | PostgreSQL 16, Spring Data JPA (Hibernate) |
| Хранилище файлов | MinIO (S3-совместимое объектное хранилище) |
| Безопасность | Spring Security, JWT (HMAC-SHA256, jjwt 0.12) |
| Frontend | Thymeleaf, HTML/CSS/JS (без фреймворков) |
| LLM (извлечение) | Yandex Cloud AI Assistants Responses API |
| LLM (анализ) | Yandex Cloud AI Assistants Responses API |
| Контейнеризация | Docker, Docker Compose |

---

### Архитектура системы

```
┌─────────────────────────────────────────────────────────────┐
│                        Браузер                              │
│              Thymeleaf SSR + ванильный JS                   │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────────┐
│                   Spring Boot (порт 8080)                   │
│                                                             │
│  ┌──────────────┐  ┌───────────────┐  ┌─────────────────┐  │
│  │ WebController│  │DocumentControl│  │AnalysisControll.│  │
│  │  /web/*      │  │ /api/documents│  │/api/.../analysis│  │
│  └──────┬───────┘  └───────┬───────┘  └────────┬────────┘  │
│         │                  │                    │           │
│  ┌──────▼───────────────────▼────────────────────▼────────┐ │
│  │               Spring Security (JWT / Session)          │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌──────────────────────┐    ┌───────────────────────────┐ │
│  │   DocumentService    │    │     AnalysisService        │ │
│  │  + TextExtraction    │    │   + YandexLlmService       │ │
│  │  + LlmExtraction     │    │                            │ │
│  └──────────┬───────────┘    └──────────────┬────────────┘ │
│             │                               │              │
└─────────────┼───────────────────────────────┼──────────────┘
              │                               │
   ┌──────────▼──────┐             ┌──────────▼───────────────┐
   │  MinIO (S3)     │             │  Yandex Cloud            │
   │  Файлы PDF/TXT  │             │  AI Assistants API       │
   └─────────────────┘             └──────────────────────────┘
              │
   ┌──────────▼──────┐
   │   PostgreSQL    │
   │  Данные + метад.│
   └─────────────────┘
```

---

### Логика обработки документа

```
Пользователь загружает файл(ы)
         │
         ▼
DocumentService.upload()
         │
         ├─► Сохранение файлов в MinIO (medical-history, treatment-plan)
         │
         ├─► TextExtractionService: извлечение текста из PDF (PDFBox) или TXT
         │
         ├─► LlmExtractionService: POST /responses → Yandex Cloud
         │     Prompt ID: fvt6dtmo6v50cnjh8f8n
         │     Input: текст истории болезни
         │     Output: JSON со структурированными данными
         │
         ├─► Сохранение PatientData в PostgreSQL
         │
         └─► Возврат DocumentResponse со статусом COMPLETED / FAILED
```

### Логика анализа соответствия

```
Пользователь запускает анализ
         │
         ▼
AnalysisService.analyze()
         │
         ├─► Загрузка файлов из MinIO
         │
         ├─► YandexLlmService: POST /responses → Yandex Cloud
         │     Prompt ID: fvt5923hvsftsb454n40
         │     Input: история болезни + план лечения
         │     Output: JSON с расхождениями и рекомендациями
         │
         ├─► Сохранение AnalysisResult + MismatchEntry[] в PostgreSQL
         │
         └─► Возврат AnalysisResponse
```

---

### Двойная цепочка безопасности Spring Security

Приложение использует **два** независимых `SecurityFilterChain`:

| Chain | Matcher | Аутентификация | Назначение |
|---|---|---|---|
| **API** (Order 1) | `/api/**` | Stateless JWT Bearer | REST API для интеграций |
| **Web** (Order 2) | `/**` | Session + Form Login | Браузерный интерфейс |

---

### Допущения и ограничения

| # | Ограничение |
|---|---|
| 1 | Поддерживаемые форматы файлов: **PDF** и **plain text** (.txt). DOCX, RTF и другие форматы не поддерживаются. |
| 2 | Максимальный размер файла: **50 MB** (один файл), **100 MB** (запрос целиком). |
| 3 | Схема базы данных обновляется автоматически через Hibernate `ddl-auto: update` — не подходит для production без миграций (Liquibase/Flyway). |
| 4 | Система является **инструментом поддержки принятия решений** и не заменяет клиническую экспертизу врача. |
| 5 | Качество извлечения данных зависит от структурированности входного документа и возможностей LLM. |
| 6 | Обработка документа **синхронная** — пользователь ожидает ответа. При больших файлах запрос может занять до 60 секунд. |
| 7 | Каждый пользователь видит **только свои** документы (изоляция на уровне БД по `user_id`). |
| 8 | JWT-токен действителен **24 часа** (настраивается через `JWT_EXPIRATION`). |

---

## Структура проекта

```
path/
├── Dockerfile                          # Многоэтапная сборка (Maven → JRE)
├── docker-compose.yaml                 # Оркестрация: app + postgres + minio
├── pom.xml
└── src/
    └── main/
        ├── java/com/gnegdev/path/
        │   ├── PathApplication.java
        │   │
        │   ├── auth/                   # Аутентификация и авторизация
        │   │   ├── controller/         # POST /api/auth/register, /login
        │   │   ├── dto/                # RegisterRequest, LoginRequest, AuthResponse
        │   │   ├── entity/User.java    # JPA-сущность, реализует UserDetails
        │   │   ├── filter/JwtAuthFilter.java
        │   │   ├── repository/
        │   │   └── service/            # AuthService, JwtService, UserDetailsServiceImpl
        │   │
        │   ├── config/                 # Конфигурация Spring
        │   │   ├── SecurityConfig.java # Два SecurityFilterChain
        │   │   ├── MinioConfig.java    # Bean MinioClient
        │   │   ├── RestClientConfig.java   # openRouterRestClient, yandexCloudRestClient
        │   │   └── JacksonConfiguration.java
        │   │
        │   ├── document/               # Управление документами пациентов
        │   │   ├── controller/         # POST /api/documents/upload, GET /api/documents
        │   │   ├── dto/DocumentResponse.java
        │   │   ├── entity/PatientDocument.java  # Метаданные + статус обработки
        │   │   ├── repository/
        │   │   └── service/
        │   │       ├── DocumentService.java       # Оркестрация загрузки
        │   │       ├── MinioStorageService.java   # S3 операции
        │   │       └── TextExtractionService.java # PDF → text (PDFBox)
        │   │
        │   ├── extraction/             # Извлечение медицинских данных через LLM
        │   │   ├── dto/ExtractedDataDto.java
        │   │   ├── entity/             # PatientData, TreatmentHistoryEntry,
        │   │   │                       # BiopsyResultEntry, ConsultationEntry,
        │   │   │                       # ImagingResultEntry
        │   │   ├── repository/
        │   │   └── service/LlmExtractionService.java  # → Yandex Cloud (extraction prompt)
        │   │
        │   ├── analysis/               # AI-анализ соответствия лечения
        │   │   ├── controller/         # POST/GET /api/documents/{id}/analysis
        │   │   ├── dto/                # AnalysisResponse, AnalysisResultDto
        │   │   ├── entity/             # AnalysisResult, MismatchEntry
        │   │   ├── repository/
        │   │   └── service/
        │   │       ├── AnalysisService.java     # Оркестрация анализа
        │   │       └── YandexLlmService.java    # → Yandex Cloud (analysis prompt)
        │   │
        │   └── web/                    # Браузерный UI (Thymeleaf)
        │       ├── HomeController.java # GET / → redirect /web/
        │       └── WebController.java  # Все маршруты /web/*
        │
        └── resources/
            ├── application.yaml
            ├── static/
            │   ├── css/styles.css
            │   ├── js/
            │   └── assets/             # Логотипы партнёров
            └── templates/              # Thymeleaf-шаблоны
                ├── index.html          # Лендинг
                ├── login.html
                ├── register.html
                ├── dashboard.html      # Список пациентов
                ├── patient-record.html # Карточка пациента
                └── ai-chat.html        # Страница AI-анализа
```

---

## API Reference

Все `/api/**` эндпоинты требуют заголовок:
```
Authorization: Bearer <JWT_TOKEN>
```

---

### Аутентификация

#### `POST /api/auth/register`

Регистрация нового пользователя.

**Тело запроса:**
```json
{
  "username": "doctor_ivanov",
  "email": "ivanov@clinic.ru",
  "password": "securepassword"
}
```

**Ответ `200 OK`:**
```json
{
  "token": "eyJhbGc...",
  "username": "doctor_ivanov",
  "email": "ivanov@clinic.ru"
}
```

---

#### `POST /api/auth/login`

Вход в систему, получение JWT-токена.

**Тело запроса:**
```json
{
  "username": "doctor_ivanov",
  "password": "securepassword"
}
```

**Ответ `200 OK`:**
```json
{
  "token": "eyJhbGc...",
  "username": "doctor_ivanov",
  "email": "ivanov@clinic.ru"
}
```

---

### Документы

#### `POST /api/documents/upload`

Загрузка документов пациента. Запускает извлечение данных через LLM.

**Content-Type:** `multipart/form-data`

| Параметр | Тип | Обязательный | Описание |
|---|---|---|---|
| `medicalHistory` | file | Да | История болезни (PDF или TXT) |
| `treatmentPlan` | file | Нет | План лечения (PDF или TXT) |

**Ответ `200 OK`:** `DocumentResponse` (см. ниже)

---

#### `GET /api/documents`

Список всех документов текущего пользователя.

**Ответ `200 OK`:** массив `DocumentResponse`

---

#### `GET /api/documents/{id}`

Получение конкретного документа со структурированными медицинскими данными.

**Ответ `200 OK`:** `DocumentResponse`

```json
{
  "id": 1,
  "status": "COMPLETED",
  "errorMessage": null,
  "medicalHistoryFilename": "history.pdf",
  "treatmentPlanFilename": "plan.pdf",
  "createdAt": "2026-02-26T12:00:00",
  "extractedData": {
    "id": 1,
    "fioInitials": "И.И.И.",
    "dateOfBirth": "01.01.1970",
    "diagnosisPrimary": "Рак молочной железы...",
    "stage": "IIA",
    "subtype": "Трижды негативный",
    "treatmentHistory": [
      {
        "id": 1,
        "treatmentType": "НАПХТ",
        "description": "4АС + 12P",
        "startDate": "01.2017",
        "endDate": "07.2017",
        "outcomeDynamic": "Положительная динамика",
        "outcomeDate": "08.2017",
        "details": null
      }
    ],
    "biopsyResults": [ ... ],
    "consultations": [ ... ],
    "imagingResults": [ ... ]
  }
}
```

**Статусы документа:**

| Статус | Описание |
|---|---|
| `PENDING` | Ожидает обработки |
| `PROCESSING` | Идёт извлечение данных |
| `COMPLETED` | Обработка завершена успешно |
| `FAILED` | Ошибка при обработке |

---

### Анализ

#### `POST /api/documents/{documentId}/analysis`

Запуск AI-анализа соответствия лечения клиническим стандартам. Требует наличия загруженного документа.

**Ответ `200 OK`:** `AnalysisResponse`

```json
{
  "id": 1,
  "documentId": 1,
  "optimal": "Лечение в целом соответствует актуальным рекомендациям...",
  "mismatches": [
    {
      "id": 1,
      "type": "Выбор режима химиотерапии",
      "current": "Паклитаксел + Карбоплатин",
      "recommended": "Атезолизумаб + Наб-паклитаксел при PD-L1 ≥ 1%"
    }
  ],
  "recommendations": [
    "Рассмотреть иммунотерапию с учётом PD-L1 CPS 10",
    "Провести молекулярно-генетическое тестирование на BRCA1/2"
  ],
  "sources": [
    "Клинические рекомендации RUSSCO 2024",
    "NCCN Guidelines Breast Cancer v4.2024"
  ],
  "analyzedAt": "2026-02-26T12:05:00"
}
```

---

#### `GET /api/documents/{documentId}/analysis`

Получение сохранённого результата анализа (без повторного вызова LLM).

**Ответ `200 OK`:** `AnalysisResponse` (аналогично выше)

**Ответ `404 Not Found`:** если анализ ещё не запускался.

---

## Переменные окружения

| Переменная | Дефолт | Описание |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/path` | URL подключения к PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Пользователь БД |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Пароль БД |
| `MINIO_ENDPOINT` | `http://localhost:9000` | URL MinIO S3 API |
| `MINIO_ACCESS_KEY` | `minioadmin` | Access key MinIO |
| `MINIO_SECRET_KEY` | `minioadmin` | Secret key MinIO |
| `MINIO_BUCKET` | `path-documents` | Имя бакета для файлов |
| `JWT_SECRET` | *(встроенный дефолт)* | HMAC-ключ для JWT, минимум 32 символа |
| `JWT_EXPIRATION` | `86400000` | Время жизни токена в мс (24 ч) |
| `YANDEX_CLOUD_API_KEY` | — | **Обязательно.** API-ключ Yandex Cloud |
| `YANDEX_CLOUD_PROJECT` | `b1gnpsrg9bte58p5mgf1` | ID проекта Yandex Cloud |
| `YANDEX_CLOUD_PROMPT_ID` | `fvt5923hvsftsb454n40` | ID промпта для анализа |
| `YANDEX_CLOUD_EXTRACTION_PROMPT_ID` | `fvt6dtmo6v50cnjh8f8n` | ID промпта для извлечения данных |

> **Для production** создайте файл `.env` рядом с `docker-compose.yaml` и переопределите чувствительные значения. Файл `.env` не должен попадать в систему контроля версий.

**Пример `.env`:**
```env
YANDEX_CLOUD_API_KEY=AQVNywut...
JWT_SECRET=my-super-secret-production-key-256-bits-minimum
SPRING_DATASOURCE_PASSWORD=strong_db_password
MINIO_ACCESS_KEY=strong_minio_user
MINIO_SECRET_KEY=strong_minio_password
```