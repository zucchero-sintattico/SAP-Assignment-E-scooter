# User Service Verticle Routes Report

## Overview
This report provides an overview of the routes defined in the `UserServiceVerticle` class. The routes handle various user-related functionalities such as displaying pages, user registration, login, and serving static content.

## Routes

### 1. Dashboard Route
- **Method**: GET
- **Path**: `api/users/dashboard`
- **Handler**: `HomeHandler`
- **Description**: Handles requests to display the user dashboard.

### 2. Login Page Route
- **Method**: GET
- **Path**: `/api/users/login-form`
- **Handler**: `AuthPageHandler`
- **Description**: Handles requests to display the login page.

### 3. Registration Page Route
- **Method**: GET
- **Path**: `api/users/register-form`
- **Handler**: `AuthPageHandler`
- **Description**: Handles requests to display the registration form.

### 4. User Registration Route
- **Method**: POST
- **Path**: `/api/users/auth/register`
- **Handler**: `RegisterHandler`
- **Description**: Handles user registration by creating a new user.

### 5. User Login Route
- **Method**: POST
- **Path**: `/api/users/auth/login`
- **Handler**: `LoginHandler`
- **Description**: Handles user login by creating a user session.

### 7. User Logout Route
- **Method**: DELETE
- **Path**: `/api/users/auth/logout`
- **Handler**: `LogoutHandler`
- **Description**: Handles user logout by deleting the user session.

## Server Configuration
- **Port**: 8888
- **Description**: The HTTP server listens on port 8888 and uses the defined router to handle incoming requests.
