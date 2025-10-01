# ShopQ — Backend API

A Spring Boot e‑commerce backend for products, carts, and orders with JWT-based authentication and role-based authorization.

- Runtime: Spring Boot 3, Spring Security 6, Spring Data JPA (Hibernate)
- DB: MySQL
- Auth: JWT (Bearer token)

Base URL (local): http://localhost:8080


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

