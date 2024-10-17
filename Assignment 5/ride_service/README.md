## Ride Service API Documentation

### 1. Dashboard
- **URL:** `/api/rides/dashboard`
- **Method:** `GET`
- **Description:** Displays the dashboard for the ride service.
- **Response:** HTML page showing the dashboard.

---

### 2. Create Ride Form
- **URL:** `/api/rides/create-form`
- **Method:** `GET`
- **Description:** Returns the HTML form to create a new ride.
- **Response:** HTML page with the create ride form.

---

### 3. Get Rides
- **URL:** `/api/rides/get_rides`
- **Method:** `GET`
- **Description:** Retrieves a list of rides.
- **Response:** JSON array of rides.
  ```json
  [
    {
      "id": "string",
      "scooterId": "string",
      "startLocation": "string",
      "endLocation": "string",
      "startTime": "string",
      "endTime": "string"
    }
  ]
    ```
  
### 4. Create Ride
- **URL:** `/api/rides/create_ride`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Description:** Creates a new ride with the provided details.
- **Request Body:**
  ```json
  {
    "scooterId": "string",
    "startLocation": "string",
    "endLocation": "string",
    "startTime": "string",
    "endTime": "string"
  }
  ```
- **Response:** Status message indicating success or failure.

### 5. Delete Ride
- **URL:** `/api/rides/:id`
- **Method:** `DELETE`
- **Description:** Deletes the specified ride.
- **Path Variable:** `id` - The ID of the ride.
- **Response:** Status message indicating success or failure.