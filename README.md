# Vegan Snacks Application

A simple full-stack application for a vendor to add vegan snacks and view submitted snacks.

## Technology
- Frontend: React.js, React Router
- Backend: Java 17, Spring Boot, Spring Data JPA
- Database: H2 in-memory database

## Run the backend
```bash
cd springapp
mvn spring-boot:run
``
Backend runs on port 8080.

## Run the frontend
```bash
cd reactapp
npm install
npm start
``
Frontend runs on port 8081.

## API endpoints

### Add a snack
`POST http://localhost:8080/addVeganSnack`

Header: `Content-Type: application/json`

Example body:
```json
{
  "snackName": "Oat Cookies",
  "snackType": "Cookies",
  "quantity": "10 packs",
  "price": 120,
  "expiryInMonths": 6
}
```

Returns HTTP 201 with the saved snack. Negative expiry returns HTTP 400 with:
`Expiry in months should not be a negative value.`

### List snacks
`GET http://localhost:8080/getAllVeganSnacks`

Returns HTTP 200 with a JSON array of submitted snacks.
