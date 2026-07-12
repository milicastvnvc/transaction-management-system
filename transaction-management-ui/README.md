# Transaction Management

Transaction Management is a small full-stack application for viewing and creating financial transactions.

- Backend: Spring Boot REST API with CSV file storage
- Frontend: Angular application with Angular Material UI

This repository contains the frontend application. The matching backend project is expected beside it at:

```text
../transaction-management
```

If you submit the project as an archive, include both folders:

```text
transaction-management/
transaction-management-ui/
```

## Deliverables

1. Complete backend API source code in `transaction-management`
2. Complete frontend application source code in `transaction-management-ui`
3. Configuration files for both applications
4. Setup, running, API, and testing instructions in this README

## Technology stack

Backend:

- Java 21
- Spring Boot 4.1.0
- Gradle Wrapper
- Apache Commons CSV
- JUnit 5 and Spring test support

Frontend:

- Node.js 24.x
- npm 11.x
- Angular 21.2.x
- Angular Material 21.2.x
- TypeScript 5.9.x
- Vitest through Angular CLI test builder

## Prerequisites

Install these before starting:

1. JDK 21 for the backend. A JRE is not enough because the project must compile Java source.
2. Node.js 24.x and npm 11.x for the frontend.
3. Git, if cloning from a repository.
4. A terminal. On Windows, PowerShell is recommended.

Verify the tools:

```powershell
java -version
node --version
npm --version
```

The backend uses the Gradle Wrapper, so Gradle does not need to be installed globally.

## Installation

Open two terminal windows: one for the backend and one for the frontend.

### Backend setup

From the backend project root:

```powershell
cd ..\transaction-management
.\gradlew.bat clean build
```

On macOS/Linux:

```bash
cd ../transaction-management
chmod +x gradlew
./gradlew clean build
```

### Frontend setup

From this frontend project root:

```powershell
npm ci
```

`npm ci` installs the exact dependency versions from `package-lock.json`.

## Configuration

### Backend configuration

The backend configuration is in:

```text
../transaction-management/src/main/resources/application.yaml
```

Default values:

```yaml
server:
  servlet:
    context-path: /api
spring:
  application:
    name: transaction-management
web:
  cors:
    allowed-origins:
      - http://localhost:4200
transaction:
  file-path: ${TRANSACTION_FILE_PATH:data/transactions.csv}
```

Important defaults:

- Backend base URL: `http://localhost:8080/api`
- Frontend origin allowed by CORS: `http://localhost:4200`
- CSV storage file: `data/transactions.csv` under the backend project

Optional environment variables:

| Variable | Purpose | Example |
| --- | --- | --- |
| `SERVER_PORT` | Changes backend port | `8081` |
| `SERVER_SERVLET_CONTEXT_PATH` | Changes API base path | `/api` |
| `TRANSACTION_FILE_PATH` | Changes CSV storage path | `data/local-transactions.csv` |
| `WEB_CORS_ALLOWED_ORIGINS_0` | Allows a different frontend origin | `http://localhost:4200` |

PowerShell example:

```powershell
$env:TRANSACTION_FILE_PATH = "data/local-transactions.csv"
.\gradlew.bat bootRun
```

### Frontend configuration

The frontend API URL is configured in:

```text
src/environments/environment.ts
```

Current value:

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api'
};
```

If the backend runs on another port or context path, update `apiBaseUrl` before starting the frontend.

## Running the application

Start the backend first.

Terminal 1:

```powershell
cd ..\transaction-management
.\gradlew.bat bootRun
```

Then start the frontend.

Terminal 2:

```powershell
cd ..\transaction-management-ui
npm start
```

Open the application in a browser:

```text
http://localhost:4200
```

The Angular development server reloads automatically when frontend source files change.

## API documentation

All API endpoints are served by the backend under:

```text
http://localhost:8080/api
```

### List transactions

```http
GET /api/transactions
```

Successful response: `200 OK`

```json
[
  {
    "transactionDate": "2026-07-12",
    "accountNumber": "1234-5678-9012",
    "accountHolderName": "John Smith",
    "amount": 150.50,
    "status": "SETTLED"
  }
]
```

PowerShell test:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions"
```

### Create transaction

```http
POST /api/transactions
Content-Type: application/json
```

Request body:

```json
{
  "transactionDate": "2026-07-12",
  "accountNumber": "1234-5678-9012",
  "accountHolderName": "John Smith",
  "amount": 150.50
}
```

Successful response: `201 Created`

```json
{
  "transactionDate": "2026-07-12",
  "accountNumber": "1234-5678-9012",
  "accountHolderName": "John Smith",
  "amount": 150.50,
  "status": "PENDING"
}
```

The backend assigns the transaction status. Possible values are `PENDING`, `SETTLED`, and `FAILED`.

PowerShell test:

```powershell
$body = @{
  transactionDate = "2026-07-12"
  accountNumber = "1234-5678-9012"
  accountHolderName = "John Smith"
  amount = 150.50
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/transactions" `
  -ContentType "application/json" `
  -Body $body
```

### Validation errors

Invalid request data returns `400 Bad Request`.

Example:

```json
{
  "status": 400,
  "message": "Validation failed.",
  "fieldErrors": {
    "amount": "Amount must be greater than zero"
  }
}
```

Validation rules:

| Field | Rules |
| --- | --- |
| `transactionDate` | Required; valid date in `YYYY-MM-DD` format |
| `accountNumber` | Required; cannot be blank |
| `accountHolderName` | Required; cannot be blank |
| `amount` | Required; greater than zero; maximum 2 decimal places |

Malformed JSON or invalid value formats return:

```json
{
  "code": 400,
  "message": "Request body contains an invalid value or format"
}
```

## Frontend functionality

The Angular UI provides:

- A transaction table showing date, account number, account holder name, amount, and status
- A loading state while transactions are fetched
- Error messages when loading or saving fails
- An add-transaction dialog with client-side validation
- Backend validation error display on form fields
- A success notification after a transaction is created

## Testing

### Backend tests

From the backend project root:

```powershell
cd ..\transaction-management
.\gradlew.bat test
```

The backend test report is generated at:

```text
../transaction-management/build/reports/tests/test/index.html
```

### Frontend tests

From this frontend project root:

```powershell
npm test
```

### Frontend production build

From this frontend project root:

```powershell
npm run build
```

The compiled frontend is written to:

```text
dist/
```

### Manual smoke test

1. Start the backend with `.\gradlew.bat bootRun`.
2. Start the frontend with `npm start`.
3. Open `http://localhost:4200`.
4. Confirm the transaction table loads.
5. Click the add button and submit a valid transaction.
6. Confirm the dialog closes and the new transaction appears in the table.
7. Submit invalid data, such as an amount of `0`, and confirm validation errors are shown.
8. Stop the backend and refresh the frontend to confirm the UI displays an error state.

## Project structure

Backend:

```text
transaction-management/
+-- src/main/java/.../config        Configuration and CORS
+-- src/main/java/.../controller    REST API endpoints
+-- src/main/java/.../csv           CSV reader/writer
+-- src/main/java/.../dto           Request and response objects
+-- src/main/java/.../exception     Error handling
+-- src/main/java/.../mapper        Data mapping
+-- src/main/java/.../repository    CSV persistence
+-- src/main/java/.../service       Business logic
+-- src/main/resources              Application configuration
+-- src/test/java                   Automated tests
```

Frontend:

```text
transaction-management-ui/
+-- src/app/core/models             Shared TypeScript models
+-- src/app/core/services           API services
+-- src/app/core/utils              API error helpers
+-- src/app/features/transactions   Transaction page, table, dialog, and form helpers
+-- src/environments                Frontend environment configuration
+-- angular.json                    Angular workspace configuration
+-- package.json                    npm scripts and dependencies
+-- package-lock.json               Locked frontend dependency versions
```

## Troubleshooting

- If `java` is not recognized, install JDK 21 and configure `JAVA_HOME`.
- If port `8080` is busy, set `SERVER_PORT` for the backend and update `src/environments/environment.ts`.
- If the browser shows a CORS error, ensure the backend allows the frontend origin, usually `http://localhost:4200`.
- If frontend dependency installation fails, verify Node.js 24.x and npm 11.x are installed, then rerun `npm ci`.
- If transactions fail to save, verify the backend process can write to the configured CSV file path.

## AI usage disclosure

OpenAI Codex was used to inspect the backend and frontend source code, identify the actual commands, configuration files, API endpoints, validation behavior, and test commands, and draft this README. The application logic was not changed as part of this documentation update.
