# Grubgo Backend

## Overview

Grubgo is a backend application built with Spring Boot that provides a RESTful API for managing user authentication, menu items, and order processing. This project utilizes JWT for secure authentication and connects to a MySQL database.

## Features

- User registration and login
- Role-based access control
- JWT authentication
- Menu item management
- Order processing
- Email notifications

## Technologies Used

- Java 21
- Spring Boot 3.4.4
- Spring Security
- JPA with Hibernate
- MySQL
- JWT (JSON Web Tokens)
- Dotenv for environment variable management

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven
- MySQL Server
- An IDE (e.g., IntelliJ IDEA, Eclipse)

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/yourusername/grubgo_backend.git
   cd grubgo_backend
   ```

2. **Create a `.env` file:**

   Copy the `.env.example` file to a new file named `.env` in the root directory and fill in your credentials:

   ```plaintext
   DB_URL=jdbc:mysql://localhost:3306/grubgo
   DB_USERNAME=root
   DB_PASSWORD=your_password
   JWT_SECRET=your_jwt_secret
   EMAIL_USERNAME=your_email@example.com
   EMAIL_PASSWORD=your_email_password
   ```

3. **Set up the MySQL database:**

   Create a new database named `grubgo` and configure the necessary tables as per your requirements.

4. **Build the project:**

   Use Maven to build the project:

   ```bash
   mvn clean install
   ```

5. **Run the application:**

   You can run the application using the following command:

   ```bash
   mvn spring-boot:run
   ```

### API Endpoints

- **User Registration:** `POST /api/user/register`
- **User Login:** `POST /api/user/login`
- **Get Menu Items:** `GET /api/dashboard/menu`
- **Create Order:** `POST /api/orders`
- **Get Orders:** `GET /api/orders`

### Environment Variables

The application requires the following environment variables to be set in the `.env` file:

- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT signing
- `EMAIL_USERNAME`: Email address for sending notifications
- `EMAIL_PASSWORD`: Password for the email account

### Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for any enhancements or bug fixes.

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MySQL](https://www.mysql.com/)
- [JWT](https://jwt.io/)
- [Dotenv](https://github.com/cdimascio/dotenv-java)
