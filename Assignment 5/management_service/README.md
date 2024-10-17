# Management Service

## Overview
This service manages e-scooters, allowing users to create new e-scooters, view all available e-scooters, and toggle their repair state.

## API Endpoints

### 1. Dashboard
- **URL:** `/api/management/dashboard`
- **Method:** `GET`
- **Description:** Returns the dashboard view.
- **Response:** HTML page for the dashboard.

### 2. Create E-Scooter
- **URL:** `/api/management/create_escooter`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Description:** Creates a new e-scooter with the provided name and location.
- **Request Body:**
  ```json
  {
    "name": "string",
    "location": "string",
    "state": "ready"
  }
  ```
- **Response:** Redirects to `/api/management/escooter_created`.
  
### 3. Get All E-Scooters
- **URL:** `/api/management/all_scooters`
- **Method:** `GET`
- **Description:** Returns a list of all e-scooters.
  - **Response:** JSON array of e-scooters.
    ```json
    [
        {
          "id": "string",
          "name": "string",
          "location": "string",
          "state": "string"
        }
    ] 
    ```
### 4. Get E-Scooter state
- **URL:** `/api/management/get_scooter_state/:id`
- **Method:** `GET`
- **Description:** Retrieves the current state of the specified e-scooter.
- **Path Variable:** `id` - The ID of the e-scooter.
- **Response:** JSON object with the current state
  ```json
  {
  "state": "string"
  }
  ```

### 5. Set E-Scooter state
- **URL:** `/api/management/set_scooter_state/:id`
- **Method:** `PUT`
- **Content-Type:** `application/json`
- **Description:** Updates the state of the specified e-scooter.
- **Path Variable:** `id` - The ID of the e-scooter.
- **Request Body:**
  ```json
  {
    "state": "string"
  }
  ```
- **Response:** Status message indicating success or failure.

### 6. Get Available E-Scooters
- **URL:** `/api/management/available_scooters`
- **Method:** `GET`
- **Description:** Retrieves a list of all available e-scooters.
- **Response:** JSON array of available e-scooters.
  ```json
  [
    {
      "id": "string",
      "name": "string",
      "location": "string",
      "state": "string"
    }
  ]
  ```
  
### 7. Use E-Scooter
- **URL:** `/api/management/use_scooter/:scooterId`
- **Method:** `PUT`
- **Description:** Marks the specified e-scooter as being `in_use`.
- **Path Variable:** `scooterId` - The ID of the e-scooter.
- **Response:** Status message indicating success or failure.

## Frontend Integration
The frontend uses jQuery to interact with the backend API. The main functionalities include:
- Creating a new e-scooter.
- Populating a dropdown with all available e-scooters.
- Toggling the repair state of a selected e-scooter.

## Technologies Used
- **Backend:** Java, Spring Boot
- **Frontend:** HTML, CSS, JavaScript, jQuery
- **Build Tool:** Gradle