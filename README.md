# Freecycle JPA Backend

Small Spring Boot backend for the freecycling assignment. This version uses Spring Data JPA and keeps the project focused on the required users, transfer sites, time slots, items, interests, appointments, state transitions, and messages.

## What Is Included

- Java Spring Boot Maven project
- Spring Web
- Spring Data JPA
- MySQL driver
- No Lombok
- No JWT/auth/images

Main package:

```text
src/main/java/com/example/freecycle
  controller
  dto
  entity
  exception
  repository
  service
```

## Database Setup

Create a MySQL database:

```sql
CREATE DATABASE freecycle;
```

Do not commit your MySQL password. The app reads the password from an environment variable.

In VS Code, open a terminal and set your local MySQL login for that terminal session:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
```

If your MySQL username is `root`, `DB_USERNAME` is optional because the app defaults to `root`.

The project already has this safe configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

Your partner should create the same `freecycle` database on their computer and set their own `DB_PASSWORD` before running the app.

JPA will create/update the tables:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Run In VS Code

Open this folder in VS Code, then run:

```bash
mvn spring-boot:run
```

The app runs at:

```text
http://localhost:8080
```

## Required Transition Endpoints

```text
POST   /api/items/{itemId}/select/{interestId}
POST   /api/items/{itemId}/deselect
POST   /api/items/{itemId}/schedule
POST   /api/items/{itemId}/complete
POST   /api/items/{itemId}/deschedule
POST   /api/items/{itemId}/cancel
DELETE /api/items/{itemId}/interests/{interestId}
```

## Testing Order In Postman

### 1. Create users

```http
POST /api/users
```

```json
{
  "email": "donor@example.com",
  "password": "password",
  "firstName": "Dana",
  "lastName": "Donor",
  "phoneNumber": "555-111-2222",
  "address": "100 Main St"
}
```

Create a second user for the recipient.

### 2. Create transfer site

```http
POST /api/transfer-sites
```

```json
{
  "name": "Community Center",
  "address": "100 Main St",
  "city": "Springfield",
  "state": "IL",
  "zipCode": "62701",
  "contactName": "Jane Smith",
  "phoneNumber": "555-111-2222",
  "email": "center@example.com",
  "description": "Safe public meeting location"
}
```

### 3. Create time slot

```http
POST /api/time-slots
```

```json
{
  "transferSiteId": 1,
  "startTime": "2026-05-20T10:00:00",
  "endTime": "2026-05-20T11:00:00",
  "maxCapacity": 10
}
```

### 4. Create item

```http
POST /api/items
```

```json
{
  "donorId": 1,
  "title": "Children's Winter Coat",
  "description": "Size 5T, barely used",
  "category": "clothes",
  "condition": "excellent",
  "size": "5T",
  "quantity": 1
}
```

### 5. Create interest

```http
POST /api/items/1/interests
```

```json
{
  "userId": 2,
  "message": "I could use this coat."
}
```

### 6. Select recipient

```http
POST /api/items/1/select/1
```

Item becomes `PENDING`, interest becomes `SELECTED`, and the recipient gets a message.

### 7. Schedule appointment

```http
POST /api/items/1/schedule
```

```json
{
  "timeSlotId": 1,
  "notes": "Meet near the front desk."
}
```

Item becomes `SCHEDULED`.

### 8. Deschedule appointment

```http
POST /api/items/1/deschedule
```

Appointment is removed, item goes back to `PENDING`, and the selected recipient stays selected.

### 9. Schedule again

Repeat:

```http
POST /api/items/1/schedule
```

### 10. Complete exchange

```http
POST /api/items/1/complete
```

Item becomes `DONE`, interests are removed, and appointment is removed.

### 11. Check messages

```http
GET /api/messages/recipient/2
```

## Other Useful Endpoints

```text
GET  /api/users
GET  /api/users/{userId}
GET  /api/users/{userId}/items
GET  /api/users/{userId}/interests

GET  /api/transfer-sites
GET  /api/transfer-sites/{siteId}
GET  /api/transfer-sites/{siteId}/time-slots
GET  /api/time-slots

GET  /api/items
GET  /api/items?state=POSTED
GET  /api/items/{itemId}
GET  /api/items/{itemId}/interests

GET  /api/appointments
GET  /api/appointments/{appointmentId}
GET  /api/appointments/item/{itemId}
GET  /api/appointments/users/{userId}

GET    /api/messages
POST   /api/messages
POST   /api/messages/{messageId}/read
DELETE /api/messages/{messageId}
```
