# Encrypted-Electronic-Health-Record-Exchange-Platform

Healthcare systems often lack secure and efficient platforms for sharing sensitive medical records between doctors and patients. Traditional data exchange methods are vulnerable to breaches, unauthorized access, and poor audit tracking, making compliance with healthcare privacy regulations challenging.

This platform provides a secure Electronic Health Record (EHR) system built with Spring Boot and MySQL to enable encrypted medical data exchange. It implements AES-256 encryption, role-based access control, secure PDF uploads, real-time audit logging, and MVC architecture with a responsive frontend to ensure confidentiality, integrity, and efficient medical record management.

## Application Screenshots

### Authentication

#### Signup
![Signup](Encryption%20EHR%20screenshots/Signup.png)

#### Login
![Login](Encryption%20EHR%20screenshots/Login.png)

---

### Doctor Module

#### Doctor Dashboard
![Doctor Dashboard](Encryption%20EHR%20screenshots/docter-dashboard.png)

#### Create Medical Record
![Create Record](Encryption%20EHR%20screenshots/docter-record.png)

#### Patient List
![Doctor Patient List](Encryption%20EHR%20screenshots/docter-patient-list.png)

#### Audit Logs
![Doctor Audit](Encryption%20EHR%20screenshots/docter-audit.png)

---

### Patient Module

#### Patient Dashboard
![Patient Dashboard](Encryption%20EHR%20screenshots/patient-dashboard.png)

#### View Records
![Patient Records](Encryption%20EHR%20screenshots/patient-record.png)

#### Activity Logs
![Patient Logs](Encryption%20EHR%20screenshots/patient-logs.png)

---

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Spring Security
- JWT Authentication
- MySQL
- JPA/Hibernate
- Lombok
- AES Encryption (javax.crypto)

## Features

- User registration with DOCTOR/PATIENT roles
- JWT-based authentication
- BCrypt password hashing
- Role-based access control
- AES encryption for medical records
- Audit logging
- Global exception handling
- Input validation

## Architecture

```
Controller → Service → Repository
```

## Database Setup

1. Install MySQL
2. Create database:
```sql
CREATE DATABASE healthcare_db;
```

3. Update credentials in `application.properties` if needed:
```properties
spring.datasource.username=root
spring.datasource.password=root
```

## Running the Application

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Register User

**POST** `/api/auth/register`

Request:
```json
{
  "name": "Dr. Yuvashree R",
  "email": "yuvashreedr@gmail.com",
  "password": "qwe123",
  "role": "DOCTOR"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "yuvashreedr@gmail.com",
  "role": "DOCTOR"
}
```

### 2. Login

**POST** `/api/auth/login`

Request:
```json
{
  "email": "yuvashreedr@gmail.com",
  "password": "qwe123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "yuvashreedr@gmail.com",
  "role": "DOCTOR"
}
```

### 3. Create Medical Record (DOCTOR only)

**POST** `/api/records/create`

Headers:
```
Authorization: Bearer <token>
```

Request:
```json
{
  "patientId": 1,
  "recordData": "Patient diagnosed with hypertension. Prescribed medication."
}
```

Response:
```json
{
  "id": 1,
  "doctorId": 1,
  "patientId": 1,
  "recordData": "Patient diagnosed with hypertension. Prescribed medication.",
  "createdAt": "2026-02-26T10:30:00"
}
```

### 4. View My Records (PATIENT only)

**GET** `/api/records/my-records`

Headers:
```
Authorization: Bearer <token>
```

Response:
```json
[
  {
    "id": 1,
    "doctorId": 1,
    "patientId": 1,
    "recordData": "Patient diagnosed with hypertension. Prescribed medication.",
    "createdAt": "2026-02-26T10:30:00"
  }
]
```

## Security Features

1. **Password Security**: BCrypt hashing with salt
2. **JWT Authentication**: Stateless token-based auth
3. **Role-Based Access**: Spring Security method-level authorization
4. **Data Encryption**: AES-256 encryption for medical records
5. **Audit Logging**: All critical actions logged with timestamps

## Database Tables

### users
- id (PK)
- name
- email (unique)
- password (hashed)
- role (DOCTOR/PATIENT)

### medical_records
- id (PK)
- doctor_id (FK)
- patient_id (FK)
- encrypted_data (TEXT)
- created_at

### audit_logs
- id (PK)
- action
- user_email
- timestamp

## Interview Talking Points

1. **Authentication Flow**: JWT generation on login, validation on each request
2. **Encryption**: AES-256 encryption before storing, decryption on retrieval
3. **Authorization**: Role-based access using Spring Security annotations
4. **Clean Architecture**: Separation of concerns with layered design
5. **Security Best Practices**: Password hashing, token expiration, input validation
6. **Audit Trail**: Comprehensive logging for compliance

## Configuration

Key configurations in `application.properties`:
- `jwt.secret`: Secret key for JWT signing
- `jwt.expiration`: Token validity period (24 hours)
- `aes.secret.key`: 256-bit key for AES encryption

## Notes

- JWT tokens expire after 24 hours
- Medical records are encrypted at rest
- Only patients can view their own records
- Only doctors can create medical records
- All actions are logged in audit_logs table
