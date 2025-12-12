# SecureSocial Backend

A backend service built with Spring Boot and Kotlin, designed to provide a secure and predictable foundation for the [SecureSocial](https://github.com/amz202/SecureSocialApp) mobile application. The API focuses on strict authentication controls, and clean separation of responsibilities. All authentication, session handling, and post-interaction logic is enforced server-side, with a stateless design that keeps the system simple, scalable, and resilient.

---

## Features

- **Multi-Stage Authentication:** Registration places accounts in a locked state until email-based OTP verification is completed.  
- **Stateless Security:** Short-lived Access Tokens with database-backed Refresh Tokens.  
- **Activity Auditing:** Logs key security-critical events (logins, failed attempts, post creation).  
- **Social Graph:** Efficient handling of posts, tags, and like aggregations.

---

## Security Mechanisms

- **HMAC Signatures:** Critical write operations (such as likes) are verified using HMAC to prevent tampering.  
- **Data Hashing:** User identifiers in view tracking are hashed for privacy/incognito modes.  
- **Restricted Access:** Protected endpoints require authorization.  
- **Token Handling:** Implements a dual-token system (Access & Refresh) to ensure secure authentication.
- **Input Validation:** Strict validation on incoming DTOs.

---

## Tech Stack

- **Language:** Kotlin  
- **Framework:** Spring Boot
- **Security:** Spring Security
- **Database:** MongoDB  
- **Authentication:** JWT  
- **Communication:** JavaMailSender

---

## License

This project is licensed under the [MIT License](./LICENSE).
