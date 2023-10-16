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

- **Reporting Issues or Incidents**: Users should be able to report issues or incidents related to e-scooters, which administrators can review and resolve.

- **User Billing and Payments**: The system should calculate fares and handle payments for users.

## User Stories
- *As a user*, I want to register for the service using my email and password, so I can start using e-scooters.

- *As a user*, I want to log in to my account securely to access the service.

- *As a user*, I want to find the nearest available e-scooter using the app.

- *As a user*, I want to reserve an e-scooter for a specific time to ensure its availability.

- *As a user*, I want to unlock an e-scooter using the app when I'm ready to start my ride.

- *As a user*, I want to end my ride and lock the e-scooter securely when I'm finished.

- *As a user*, I want to view my ride history and receipts for tracking my usage.

- *As a user*, I want to report an issue with an e-scooter when I encounter problems.

- *As an admin*, I want to track the location and status of all e-scooters for management purposes.

- *As an admin*, I want to monitor the battery status of e-scooters to ensure their availability.

- *As an admin*, I want to track user activity and history for administrative purposes.

## Domain Stories
- *Registration Flow*: Users sign up, verify their email, and set a password to create their account.

- *Ride Lifecycle*: Users find, reserve, unlock, ride, and lock e-scooters, completing the ride cycle.

- *Maintenance Workflow*: The system automates the collection, charging, and repair of e-scooters to keep them in good condition.

- *Station Management*: Administrators can manage e-scooter stations, including adding, removing, or relocating them.

- *User Account Management*: Users can update their profiles and reset their passwords as needed.

- *Reporting and Issue Resolution*: Users report issues, administrators review them, and take action to resolve problems.

- *Billing and Payment Processing*: The system calculates fares and handles payments for users.

## Event Storms
- *User Registration Flow*: Events include Registration, Verification, and Password Set.

- *Ride Lifecycle Events*: Events involve Reservation, Unlock, Ride, and Lock actions.

- *Maintenance Workflow Events*: Events encompass Collection, Charging, and Repair processes.

- *Station Management Events*: Events cover Station Added, Station Removed, and Station Relocated actions.

- *User Account Management Events*: Events include Profile Update and Password Reset.

- *Issue Reporting and Resolution Events*: Events consist of Issue Report, Review, and Resolution steps.

- *Billing and Payment Events*: Events involve Fare Calculation and Payment Process stages.

## Quality Attribute Scenarios
- **Performance**: The system must support concurrent user activity, ensuring quick e-scooter reservations and unlocks even during peak hours.

- **Security**: User data and payment information must be stored securely, and only authenticated users should have access to e-scooters.

- **Scalability**: The system should easily scale as more e-scooters and users are added, accommodating growing demand.

- **Reliability**: E-scooter availability and the accuracy of tracking data should be highly reliable to maintain user trust.

- **Usability**: The user app and company dashboard should be user-friendly and intuitive to ensure a positive user experience.

- **Maintainability**: The system should be easy to maintain and update, and e-scooters should be efficiently serviced to minimize downtime.

- **Integration**: The system should seamlessly integrate with payment gateways, mapping services, and e-scooter hardware for tracking and unlocking, ensuring a cohesive user experience.
