\# Clustered Data Warehouse — Technical Assignment (ProgressSoft)



\*\*Author:\*\* Ayyadi Imane  

\*\*Technologies:\*\* Java 17, Spring Boot 3, PostgreSQL, Docker, Maven



---



\## Project Overview



This project implements a small Data Warehouse component that imports FX (Foreign Exchange) deals from a CSV file and stores them in a PostgreSQL database.



It fulfills the assignment requirements from \*\*ProgressSoft Corporation\*\*:



\- Accept a CSV file with deal records.

\- Validate each row (missing fields, invalid formats).

\- Prevent importing the same deal twice.

\- No rollback: valid rows are saved even if other rows fail.

\- Provide logging, error handling and clear code structure.

\- Be runnable via Docker and Docker Compose.

\- Include documentation, sample data, a Makefile, and a Maven build.



---



\## Key Features



\- \*\*CSV Import API:\*\* `POST /api/deals/import` (multipart/form-data, field `file`)  

\- \*\*Row-by-row validation\*\*

&nbsp; - Required fields present  

&nbsp; - Timestamp format (ISO\_LOCAL\_DATE\_TIME)  

&nbsp; - Numeric format for amounts  

\- \*\*Duplicate prevention\*\* using `dealUniqueId` (unique constraint)  

\- \*\*Partial success:\*\* valid rows are persisted even if others fail  

\- \*\*Detailed import report\*\* returned per CSV line  

\- \*\*Containerized\*\* with Docker and Docker Compose  

\- \*\*Sample CSV\*\* included for quick testing  

\- \*\*Makefile\*\* with convenient shortcuts  



---



\## Project Structure





clustered-data-warehouse/

│

├── Dockerfile

├── docker-compose.yml

├── Makefile

├── pom.xml

├── README.md

│

├── sample-data/

│ └── deals-sample.csv

│

├── src/

│ └── main/

│ ├── java/

│ │ └── com/example/cdw/

│ │ ├── ClusteredDataWarehouseApplication.java

│ │ ├── controller/DealController.java

│ │ ├── dto/DealDto.java

│ │ ├── model/Deal.java

│ │ ├── repository/DealRepository.java

│ │ └── service/DealService.java

│ └── resources/

│ └── application.yml

│

└── target/

└── cdw-app-0.0.1.jar



---



\## Prerequisites



\- Docker Desktop (or Docker + Docker Compose)  

\- Java 17 (if you want to build locally)  

\- Maven 3.8+ (optional, for building locally)



---



\## Quick Start



\### 1. (Optional) Build the JAR locally



```bash

mvn clean package

```



\### 2.Start the application and database



docker compose up --build -d



\### 3.Stop everything



docker compose down



---

\## API — Import Deals



Endpoint



&nbsp;  POST http://localhost:8080/api/deals/import



---

Request



multipart/form-data



field name: file



\*\*content:\*\* CSV file



---

Response

```json

\[

&nbsp; { "line": "2", "status": "inserted" },

&nbsp; { "line": "3", "status": "duplicate" },

&nbsp; { "line": "4", "status": "error", "message": "invalid timestamp" }

]

```





---

Testing the API

For Windows PowerShell :



``` powerShell

$filePath = "C:\\Users\\pc\\clustered-data-warehouse\\sample-data\\deals-sample.csv"

$boundary = \[System.Guid]::NewGuid().ToString()

$LF = "`r`n"



$body = "--$boundary$LF" +

&nbsp;       "Content-Disposition: form-data; name=`"file`"; filename=`"deals-sample.csv`"$LF" +

&nbsp;       "Content-Type: text/csv$LF$LF" +

&nbsp;       \[System.IO.File]::ReadAllText($filePath) + "$LF" +

&nbsp;       "--$boundary--$LF"



Invoke-WebRequest -Uri "http://localhost:8080/api/deals/import" `

&nbsp;   -Method Post `

&nbsp;   -ContentType "multipart/form-data; boundary=$boundary" `

&nbsp;   -Body $body

```





For Linux / macOS / WSL :



```bash



curl -X POST -F "file=@sample-data/deals-sample.csv" http://localhost:8080/api/deals/import



```





---

\## Sample CSV



```csv

dealUniqueId,fromCurrencyIsoCode,toCurrencyIsoCode,dealTimestamp,dealAmount

D001,USD,EUR,2023-11-20T10:00:00,1000

D002,EUR,JPY,2023-11-20T12:30:00,500

D003,GBP,USD,2023-11-20T15:45:00,750





\### Makefile Shortcuts





```makefile

build:

&nbsp;	mvn clean package



run:

&nbsp;	docker compose up --build



stop:

&nbsp;	docker compose down

```



---



Run shortcuts:



make build

make run



---



Implementation Notes



-Spring Boot 3 application using JPA/Hibernate.



-PostgreSQL database running in a Docker container.



-dealUniqueId enforces unique FX deals.



-CSV processed line-by-line with partial success (no global rollback).



-Detailed logging for debugging and auditability.



---

Troubleshooting



&nbsp;  no main manifest attribute

&nbsp;   Run:



&nbsp;     mvn clean package

&nbsp;     docker compose up --build



&nbsp;  disk full during build - Clear disk space or remove :

&nbsp;     C:\\Users\\<you>\\.m2\\repository

&nbsp;  Docker Desktop issues ,Restart Docker, verify context:

&nbsp;     docker context ls

&nbsp;  Then Run :

&nbsp;     docker compose up --build -d

&nbsp;  PowerShell multipart issues

&nbsp;     Use the provided PowerShell script above.



---

Contact



Email: imaneayydi6@gmail.com 



