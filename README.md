# Transaction Management

Transaction Management is a small full-stack application for viewing and creating financial transactions.

- **Backend:** Spring Boot REST API with CSV file storage
- **Frontend:** Angular application with Angular Material UI

Folder structure:

```text
transaction-management/     #Backend
transaction-management-ui/  #Frontend
```

The backend runs on `http://localhost:8080/api` and the frontend runs on `http://localhost:4200`.

## Prerequisites

Install the following before running the project:

### Backend

- **JDK 21**

After installation, verify it:

```bash
java -version
```

If the command is not recognized, make sure `JAVA_HOME` points to your JDK installation and that the JDK bin directory is added to your PATH.

Gradle does not need to be installed manually because the backend includes the Gradle Wrapper.

### Frontend
- **Node.js 24.x**
- **npm 11.x**

Install Node.js 24.x from the official Node.js website. npm is installed together with Node.js.

Check installed versions:

```powershell
node --version
npm --version
```

### Both
- **Git** if cloning the repositories
- A terminal, for example PowerShell on Windows

## Installation

Open a terminal in each project directory.

### Backend

Windows:

```powershell
cd transaction-management
.\gradlew.bat clean build
```

macOS/Linux:

```bash
cd transaction-management
chmod +x gradlew
./gradlew clean build
```

### Frontend

```powershell
cd transaction-management-ui
npm install
```

## Configuration

### Backend

Backend configuration is stored in:

```text
transaction-management/src/main/resources/application.yaml
```

Default configuration:

```yaml
server:
  servlet:
    context-path: /api

web:
  cors:
    allowed-origins:
      - http://localhost:4200

transaction:
  file-path: ${TRANSACTION_FILE_PATH:data/transactions.csv}
```
The default CSV file is:

```text
transaction-management/data/transactions.csv
```

The application includes the sample transactions provided with the assignment. They are loaded from the configured CSV file when the backend starts.

If the file or its parent directories do not exist, they are created automatically. New or empty CSV files receive the following header:

```csv
Transaction Date,Account Number,Account Holder Name,Amount,Status
```

Optional backend environment variables:

| Variable | Purpose | Example |
| --- | --- | --- |
| `SERVER_PORT` | Changes backend port | `8081` |
| `TRANSACTION_FILE_PATH` | Changes CSV file location | `data/local-transactions.csv` |
| `WEB_CORS_ALLOWED_ORIGINS_0` | Allows another frontend origin | `http://localhost:4200` |

Configuration can be changed either by editing `application.yaml` or by setting Spring Boot environment variables before starting the server.

PowerShell example:

```powershell
$env:TRANSACTION_FILE_PATH = "data/local-transactions.csv"
.\gradlew.bat bootRun
```

Bash example:
```bash
TRANSACTION_FILE_PATH=data/local-transactions.csv ./gradlew bootRun
```

The backend process must have permission to read and write the configured CSV path.

### Frontend

Frontend API configuration is stored in:

```text
transaction-management-ui/src/environments/environment.ts
```

Default value:

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api'
};
```

If the backend runs on another port, update `apiBaseUrl`.

## Running the Application

Start the backend first, then start the frontend in a second terminal.

### Start the Backend

Windows:

```powershell
cd transaction-management
.\gradlew.bat bootRun
```

Or macOS/Linux:
```bash
./gradlew bootRun
```

### Start the Frontend

In another terminal:

```powershell
cd transaction-management-ui
npm start
```

Open:

```text
http://localhost:4200
```

Stop either application with `Ctrl+C` in its terminal.

## API documentation

All endpoints are under:

```text
http://localhost:8080/api
```

### List Transactions

```http
GET /api/transactions
```

Successful response: `200 OK`

```json
[
  {
    "transactionDate": "2025-03-01",
    "accountNumber": "7289-3445-1121",
    "accountHolderName": "Maria Johnson",
    "amount": 150.00,
    "status": "SETTLED"
  }
]
```

PowerShell test:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions"
```

Or curl test:
```bash
curl http://localhost:8080/api/transactions
```

### Create a Transaction

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

The backend randomly assigns one of the following statuses to every newly created transaction: 
- `PENDING` 
- `SETTLED`
- `FAILED`

The assigned status is saved to the CSV file and returned in the response.

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

Or curl test (macOS/Linux):

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"transactionDate":"2026-07-12","accountNumber":"1234-5678-9012","accountHolderName":"John Smith","amount":150.50}'
```

### Validation

| Field | Rules |
| --- | --- |
| `transactionDate` | Required; valid `YYYY-MM-DD` date |
| `accountNumber` | Required; cannot be blank |
| `accountHolderName` | Required; cannot be blank |
| `amount` | Required; greater than zero; maximum 15 integer digits and 2 decimal places |

Invalid request data returns `400 Bad Request` with validation details. For example:
```json
{
  "status": 400,
  "message": "Validation failed.",
  "fieldErrors": {
    "amount": "Amount must be greater than zero"
  }
}
```

Malformed JSON or invalid value formats, such as an invalid date string, also return `400 Bad Request`.

CSV read/write failures or invalid persisted records return `500 Internal Server Error`.

## Frontend Features

The Angular UI includes:

- A table displaying all transactions
- Status-based colors: 
  - Pending: yellow
  - Settled: green
  - Failed: red
- A modal form for creating transactions
- Client-side validation
- Backend validation error display
- Loading, success, and error states

After creating a transaction, the table updates with the new row.

## Testing

### Backend Tests

Windows:

```powershell
cd transaction-management
.\gradlew.bat test
```

The generated test report is available at:

```text
transaction-management/build/reports/tests/test/index.html
```

### Manual smoke test

1. Start the backend.
2. Start the frontend.
3. Open `http://localhost:4200`.
4. Confirm that the transaction table loads.
5. Add a valid transaction.
6. Confirm that the dialog closes and the new transaction appears.
7. Submit invalid data, such as amount `0`, and confirm validation errors are shown.
8. Stop the backend and refresh the frontend to confirm the UI shows an error state.

Manual POST requests modify the configured CSV file. Use `TRANSACTION_FILE_PATH` if you want to test with a separate CSV file.

## Troubleshooting

- If `java` is not recognized, install JDK 21 and configure `JAVA_HOME`.
- If port `8080` is busy, set `SERVER_PORT` and update the frontend `apiBaseUrl`.
- If the browser shows a CORS error, make sure the backend allows the exact frontend origin.
- If `npm ci` fails, verify Node.js 24.x and npm 11.x.
- If transactions cannot be saved, check the CSV path and file permissions.

## AI usage disclosure

OpenAI ChatGPT/Codex was used as a development assistant to review code structure, tests, configuration, error handling, and documentation wording. 
The final implementation and submitted code were reviewed and controlled by the author.