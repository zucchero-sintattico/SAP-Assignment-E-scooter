# SAP-Assignment-E-scooter


## Use Cases
- **User Registration**: Users should be able to register for the service by providing their email and creating a secure password.

- **User Authentication**: Registered users must be able to log in securely using their email and password.

- **E-Scooter Reservation**: Users should have the option to reserve an e-scooter for a specific duration.

- **E-Scooter Pickup**: Users should be able to locate and unlock an e-scooter using the app.

- **E-Scooter Drop-off**: Users should be able to end their ride and securely lock the e-scooter, making it available for others.

- **User Account Management**: Users should be able to manage their account, including profile updates and password resets.

- **E-Scooter Maintenance**: The system should automate the regular collection, charging, and repair of e-scooters.

- **E-Scooter Tracking**: The system must accurately track the location and status of all e-scooters.

- **E-Scooter Station Management**: Administrators should be able to manage e-scooter stations, including adding, removing, or relocating them.

- **User Billing and Payments**: The system should calculate fares and handle payments for users.

![Use Cases](/img/e-scooter-UseCase.png)

## User Stories
- *As a user*, *I want* to register for the service using my email and password *so that* I can start using e-scooters.

- *As a user*, *I want* to log in to my account securely *so that* I can access the service.

- *As a user*, *I want* to find the nearest available e-scooter using the app *so that* I can quickly start my ride.

- *As a user*, *I want* to reserve an e-scooter for a specific time *so that* I can ensure its availability when I need it.

- *As a user*, *I want* to unlock an e-scooter using the app *so that* I can start my ride easily and efficiently.

- *As a user*, *I want* to end my ride and lock the e-scooter securely *so that* I can complete my trip and avoid additional charges.

- *As a user*, *I want* to view my ride history and receipts *so that* I can track my usage and expenses.

- *As an admin*, *I want* to analyze the usage patterns of e-scooters *so that* I can optimize their distribution and maintenance.

- *As an admin*, *I want* to automate maintenance tasks, including charging and repairing e-scooters *so that* the fleet remains operational with minimal manual intervention.

- *As an admin*, *I want* to track user activity and history *so that* I can monitor the system for administrative and reporting purposes.

## Domain Stories

![Domain Stories](/img/e-scooter-Domain-stories.png)

## Quality Attribute Scenarios
Feature: Quick response time for finding the nearest e-scooter

when a user searches for the nearest e-scooter\
caused by a user using the mobile app\
occurs in the search function of the mobile application\
operating in normal operation with a high number of users\
then the system processes the request and returns results\
so that the search results are displayed within 2 seconds

\
Feature: High availability of authentication service

when a server responsible for user authentication fails\
caused by server downtime or crash\
occurs in the authentication module\
operating in normal operation\
then the system redirects authentication requests to a backup server\
so that users can log in without downtime

\
Feature: Small Latency in the case of overload

when multiple requests initiated in k seconds interval cause overload\
caused by n users\
occur in the system\
operating in normal operation\
then the system processes all requests\
so that the average latency < m seconds
