# README.md

# Spring Boot MongoDB Firebase Project

This project is a Spring Boot application that connects to MongoDB and uses Firebase for authentication. It is designed to manage a collection of mothers and verify their identity using Firebase ID tokens.

## Project Structure

```
spring-boot-mongodb-firebase
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── app
│       │           ├── SpringBootApplication.java
│       │           ├── config
│       │           │   └── FirebaseConfig.java
│       │           ├── controllers
│       │           │   └── AuthController.java
│       │           ├── models
│       │           │   ├── Mother.java
│       │           │   └── UserProfile.java
│       │           ├── repositories
│       │           │   └── MotherRepository.java
│       │           └── services
│       │               ├── AuthService.java
│       │               └── MotherService.java
│       └── resources
│           ├── application.properties
│           └── firebase-service-account.json
├── pom.xml
└── README.md
```

## Features

- Connects to MongoDB to manage a collection of mothers.
- Uses Firebase for user authentication.
- Verifies Firebase ID tokens and retrieves user profiles.

## Getting Started

1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Navigate to the project directory:
   ```
   cd spring-boot-mongodb-firebase
   ```

3. Update the `application.properties` file with your MongoDB connection details and Firebase credentials.

4. Build the project using Maven:
   ```
   mvn clean install
   ```

5. Run the application:
   ```
   mvn spring-boot:run
   ```

## Dependencies

- Spring Boot
- Spring Data MongoDB
- Firebase Admin SDK

## License

This project is licensed under the MIT License.