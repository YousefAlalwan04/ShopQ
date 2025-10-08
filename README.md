# ShopQ — Backend API

A Spring Boot e‑commerce backend for products, carts, and orders with JWT-based authentication and role-based authorization.

- Runtime: Spring Boot 3, Spring Security 6, Spring Data JPA (Hibernate)
- DB: MySQL
- Auth: JWT (Bearer token)

Base URL (local): http://localhost:8080


## Setup (Windows)

Prerequisites
- JDK 17 installed (verify with: `java -version`)
- MySQL 8.x running locally
- No need to install Maven globally — project uses Maven Wrapper (mvnw.cmd)
- A REST client (Postman, Insomnia) for testing

1) Create the MySQL database and user
- In MySQL Workbench or mysql shell, run:
  - CREATE DATABASE shopq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  - CREATE USER 'springstudent'@'%' IDENTIFIED BY 'springstudent';
  - GRANT ALL PRIVILEGES ON shopq.* TO 'springstudent'@'%';
  - FLUSH PRIVILEGES;

2) Configure application.properties
- Edit `src/main/resources/application.properties` and set the following (adjust if needed):
  - spring.datasource.url=jdbc:mysql://localhost:3306/shopq?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  - spring.datasource.username=springstudent
  - spring.datasource.password=springstudent
  - spring.jpa.hibernate.ddl-auto=update   # for local dev; use validated migrations in prod
  - secret.jwt.secretKey=<Base64-encoded 256-bit key>
  - secret.jwt.expiration=360000000        # token lifetime in ms (~100 hours)

3) Generate a secure JWT secret (Base64, HS256)
- The app expects a Base64-encoded key (decoded length >= 32 bytes for HS256). In Windows PowerShell:

```
# PowerShell: generate a 32-byte random key and print Base64 string
$bytes = New-Object 'System.Byte[]' 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

- Copy the output and set it as `secret.jwt.secretKey` in application.properties.

4) Allow CORS for your frontend (optional)
- If your frontend runs on http://localhost:6969, add it to allowed origins.
- File `src/main/java/com/shopQ/MainShopQ/auth/config/SecurityConfig.java` → `configuration.setAllowedOrigins(...)`:
  - Include: "http://localhost:6969" (keep existing origins like "http://localhost:4200").

5) Build & run (no chained commands)
- From the project root (this folder):

```
# compile & run (Windows)
mvnw.cmd clean package
mvnw.cmd spring-boot:run
```

- Alternatively, run the packaged jar:

```
# after package
java -jar target/MainShopQ-0.0.1-SNAPSHOT.jar
```

First-time notes
- Hibernate will create/update tables automatically due to `spring.jpa.hibernate.ddl-auto=update`.
- If you see MySQL connection errors, re-check URL, username/password, and that MySQL is running.
- If you see JWT parsing errors, ensure `secret.jwt.secretKey` is Base64 and at least 32 bytes when decoded.


## Quick start (Windows)

1) Configure database in `src/main/resources/application.properties` (MySQL URL, username, password).  
2) Run with Maven Wrapper:

```
# from the project root (this folder)
mvnw.cmd clean package
mvnw.cmd spring-boot:run
```

Once running, the API will be served on port 8080.


## Authentication & Authorization

- Sign up to create a user, then log in to get a JWT token.
- Send the token on secured endpoints in the header:
  - Authorization: Bearer <your-jwt>
- Roles used in this project: ROLE_USER, ROLE_ADMIN.

Security annotations you will see:
- @PreAuthorize("hasRole('USER')") — endpoints for authenticated regular users
- @PreAuthorize("hasRole('ADMIN')") — admin-only endpoints

Note: Some controllers may rely on global security rules in configuration even if an endpoint isn’t explicitly annotated.


## Data models (selected DTOs)

- RegisterRequest: { username, email, password, role }
- LoginRequest: { email, password }
- LoginResponse: { token, role, expiresIn }
- OrderItem: { fullName, fullAdress, orderDate?, orderProductQuantity: [ { productId, quantity } ] }
- OrderConfirmationRequest: { fullName, fullAdress, orderProductQuantity: [ { productId, quantity } ], acceptPartialOrder }
- StockCheckResult: { productId, productName, requestedQuantity, availableStock, hasStockShortage, message }

Entities (simplified):
- Product: { id, productName, description, price, discountedPrice?, quantity, images[] }
- Order: { orderId, customerName, cutomerAddress, customerEmail, productName, quantity, price, orderStatus, orderDate, product(ref), user(ref) }
- Cart: links User to Product with a quantity
- User: { id, username, email, password, role, ... }


## Endpoints

### 1) Auth

- POST /auth/signup
  - Body: RegisterRequest
  - Response: User (created)
  - Example body:
    {
      "username": "yousef",
      "email": "test@example.com",
      "password": "Secret123",
      "role": "USER"
    }

- POST /auth/login
  - Body: LoginRequest
  - Response: LoginResponse { token, role, expiresIn }
  - Use token as: Authorization: Bearer <token>


### 2) Users

- GET /users/me
  - Secured: requires valid JWT
  - Response: current User

- GET /users/
  - Response: List<User>
  - Note: No explicit role annotation in controller; actual access may be enforced via security config.


### 3) Products

- GET /product/all?pageNumber=0&filter=
  - Response: Iterable<Product>
  - filter is a free-text filter (e.g., by name); pageNumber is 0-based.

- GET /product/{id}
  - Response: Product

- GET /product/name/{name}
  - Response: Product

- GET /product/get-products-to-checkout/?isSingleProduct=true|false&productId={id}
  - Response: product details for checkout (shape depends on ProductService)
  - Important: parameter name must be isSingleProduct (not isSingleCheckout)

- POST /product/add
  - Secured: @PreAuthorize('hasRole('ADMIN)')
  - Content-Type: multipart/form-data
  - Parts:
    - product: JSON of Product (without images)
    - image: one or more files
  - Response: Product (created)
  - Tip: Use Postman form-data. For the product part, set type = application/json and paste the Product JSON.

- PUT /product/update
  - Body: Product
  - Response: Product (updated)

- PUT /product/update-with-images
  - Secured: @PreAuthorize('hasRole('ADMIN')')
  - Content-Type: multipart/form-data
  - Parts:
    - product: JSON of Product (includes id of the product to update)
    - addImages: 0..n image files to add (optional)
    - removeImageIds: 0..n Long ids of images to remove (optional, can repeat)
  - Response: Product (updated)

- DELETE /product/delete/{id}
  - Response: 204 No Content on success
  - 409 Conflict if the product is referenced by an existing order (foreign key constraint)

Price rule note: All order amounts use an “effective price” helper — if discountedPrice is present and > 0 and less than price, it is used; otherwise the original price is used.


### 4) Cart

- GET /cart/add/{productId}/{quantity}
  - Secured: @PreAuthorize('hasRole('USER')')
  - Response: ResponseEntity<String> (status message)
  - Adds a product to the current user’s cart, or increases quantity.
  - Note: The controller mapping uses path variables; call it as shown above.

- GET /cart/my-cart
  - Secured: @PreAuthorize('hasRole('USER')')
  - Response: Either a list of cart items or a message when empty/not authorized.

- DELETE /cart/remove/{cartItemId}
  - Secured: @PreAuthorize('hasRole('USER')')
  - Response: status message


### 5) Orders (Checkout)

Workflow options:
- Validate -> Confirm (two-step)
- Direct place (auto-confirm) if stock is sufficient
- Can also place orders from the cart contents (cart checkout)

- POST /orders/validate-order/{isCartCheckout}
  - Secured: @PreAuthorize('hasRole('USER')')
  - Body: OrderItem
  - Response: { success, canProceed, message, stockIssues[], instruction }
  - Use this to check stock and get a list of issues before placing.

- POST /orders/confirm-order
  - Secured: @PreAuthorize('hasRole('USER')')
  - Body: OrderConfirmationRequest
  - Response: { success, message, orderDetails }
  - Use after validate to finalize with adjusted quantities.

- POST /orders/place-order/{isCartCheckout}
  - Secured: @PreAuthorize('hasRole('USER')')
  - Body:
    - If isCartCheckout=false: OrderItem is required
    - If isCartCheckout=true: OrderItem is optional, but you must provide fullName and fullAdress (used for the order)
  - Behavior:
    - Validates stock; if any issue is found, returns 409 with guidance to use validate/confirm flow
    - If no issues: creates Orders, decrements Product.quantity, and if cart checkout, clears purchased items from the user’s cart
  - Response: { success, message, details }

- GET /orders/order-history
  - Secured: @PreAuthorize('hasRole('USER')')
  - Response: List<Order> for the current user

- GET /orders/all-orders
  - Secured: @PreAuthorize('hasRole('ADMIN')')
  - Response: List<Order> (wrapped) or a message when empty


## Example payloads

- OrderItem (single-product checkout):
  {
    "fullName": "Jane Doe",
    "fullAdress": "Amman, Jordan",
    "orderProductQuantity": [
      { "productId": 52, "quantity": 2 }
    ]
  }

- OrderConfirmationRequest (after validate):
  {
    "fullName": "Jane Doe",
    "fullAdress": "Amman, Jordan",
    "orderProductQuantity": [
      { "productId": 52, "quantity": 1 },
      { "productId": 53, "quantity": 2 }
    ],
    "acceptPartialOrder": true
  }


## Common errors & notes

- 400 Bad Request: missing required parameters (e.g., use isSingleProduct query name for product checkout preview; provide OrderItem when required).
- 401/403: invalid/expired JWT, or insufficient role.
- 409 Conflict: deleting a product that is referenced in orders will fail due to a foreign key constraint. You must cancel/delete dependent orders before removing the product.
- Validation and stock control:
  - Quantity must be > 0
  - Orders from cart will remove the purchased cart items upon success
  - Effective price is applied automatically (discounted vs original)


## Tips for testing

- Use Postman or a similar client for multipart requests:
  - For `/product/add` and `/product/update-with-images`, set Body to form-data.
  - Add `product` as a form part with type `application/json` and paste the product JSON.
  - Add one or more `image` (or `addImages`) file parts.
- For `/product/get-products-to-checkout/`, pass `isSingleProduct` and `productId` as query params exactly with those names.
- Cart add uses path variables: `GET /cart/add/{productId}/{quantity}`.


## License

MIT (see LICENSE)
