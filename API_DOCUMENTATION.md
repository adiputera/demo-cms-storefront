# API Documentation

Complete API reference for the Headless CMS Demo Application.

## Base URLs

- **Storefront API (Read-Only):** `http://localhost:8080/api`
- **CMS Admin API (Write Operations):** `http://localhost:8081/api/cms`

---

## Storefront API (Port 8080)

### Pages

#### Get Page by Slug

```http
GET /api/pages/{slug}
```

**Description:** Retrieve a page by its URL slug. Returns the page with all slots and their components.

**Parameters:**
- `slug` (path) - Page slug (e.g., `index`, `about-us`, `contact`)

**Response:** `200 OK`
```json
{
  "id": 1,
  "slug": "/",
  "title": "Welcome to Our Store",
  "breadcrumbTitle": "Home",
  "metaTitle": "Home - Best Products Online",
  "metaDescription": "Shop the latest products...",
  "metaKeywords": ["shopping", "products"],
  "canonicalUrl": "https://example.com/",
  "robotsIndex": true,
  "robotsFollow": true,
  "ogTitle": "Welcome",
  "ogDescription": "Shop now",
  "ogImage": "https://example.com/og-image.jpg",
  "breadcrumbs": [
    {"id": 1, "label": "Home", "url": "/"}
  ],
  "slotIds": [1, 2, 3]
}
```

**Error Responses:**
- `404 Not Found` - Page does not exist
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "message": "Page not found with identifier: /invalid-page",
  "path": "/api/pages/invalid-page"
}
```

**Example:**
```bash
curl http://localhost:8080/api/pages/index
```

---

### Slots

#### Batch Fetch Slots

```http
POST /api/slots/details
```

**Description:** Fetch multiple slots with their components in a single request.

**Request Body:**
```json
{
  "slotIds": [1, 2, 3]
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "slotName": "hero",
    "components": [
      {
        "id": 1,
        "type": "BANNER",
        "position": 0,
        "title": "Welcome",
        "subtitle": "Shop the latest products",
        "imageUrl": "https://example.com/banner.jpg",
        "ctaText": "Shop Now",
        "ctaUrl": "/products"
      }
    ]
  },
  {
    "id": 2,
    "slotName": "content",
    "components": [
      {
        "id": 2,
        "type": "PARAGRAPH",
        "position": 0,
        "title": "About Us",
        "content": "<p>We are a leading retailer...</p>"
      },
      {
        "id": 3,
        "type": "PRODUCT_CAROUSEL",
        "position": 1,
        "title": "Featured Products",
        "productCodes": ["macbook-pro", "iphone-15-pro"]
      }
    ]
  }
]
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/slots/details \
  -H "Content-Type: application/json" \
  -d '{"slotIds": [1, 2, 3]}'
```

---

### Products

#### List All Products

```http
GET /api/products
```

**Description:** Get all products in the catalog.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "code": "macbook-pro",
    "name": "MacBook Pro 16\"",
    "price": 2499.00,
    "description": "Powerful laptop for professionals",
    "imageUrl": "https://example.com/macbook.jpg"
  },
  {
    "id": 2,
    "code": "iphone-15-pro",
    "name": "iPhone 15 Pro",
    "price": 999.00,
    "description": "Latest iPhone with titanium design",
    "imageUrl": "https://example.com/iphone.jpg"
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/products
```

---

#### Get Product by Code

```http
GET /api/products/{code}
```

**Description:** Retrieve a single product by its unique code.

**Parameters:**
- `code` (path) - Product code (e.g., `macbook-pro`)

**Response:** `200 OK`
```json
{
  "id": 1,
  "code": "macbook-pro",
  "name": "MacBook Pro 16\"",
  "price": 2499.00,
  "description": "Powerful laptop for professionals",
  "imageUrl": "https://example.com/macbook.jpg"
}
```

**Error Responses:**
- `404 Not Found` - Product does not exist

**Example:**
```bash
curl http://localhost:8080/api/products/macbook-pro
```

---


## CMS Admin API (Port 8081)

### Pages

#### List All Pages

```http
GET /api/cms/pages
```

**Description:** Get all pages for CMS management.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "slug": "/",
    "title": "Welcome to Our Store",
    "breadcrumbTitle": "Home",
    "metaTitle": "Home - Best Products Online",
    "metaDescription": "Shop the latest products...",
    "metaKeywords": ["shopping", "products"],
    "canonicalUrl": "https://example.com/",
    "robotsIndex": true,
    "robotsFollow": true,
    "ogTitle": "Welcome",
    "ogDescription": "Shop now",
    "ogImage": "https://example.com/og-image.jpg",
    "breadcrumbs": [],
    "slotIds": [1, 2, 3]
  }
]
```

**Example:**
```bash
curl http://localhost:8081/api/cms/pages
```

---

#### Get Page by ID

```http
GET /api/cms/pages/{id}
```

**Description:** Get a single page by ID for editing.

**Parameters:**
- `id` (path) - Page ID

**Response:** `200 OK` (same structure as List All Pages)

**Example:**
```bash
curl http://localhost:8081/api/cms/pages/1
```

---

#### Create Page

```http
POST /api/cms/pages
```

**Description:** Create a new page.

**Request Body:**
```json
{
  "slug": "/new-page",
  "title": "New Page",
  "breadcrumbTitle": "New",
  "metaTitle": "New Page - Our Store",
  "metaDescription": "Description of the new page",
  "metaKeywords": ["new", "page"],
  "canonicalUrl": "https://example.com/new-page",
  "robotsIndex": true,
  "robotsFollow": true,
  "ogTitle": "New Page",
  "ogDescription": "Check out our new page",
  "ogImage": "https://example.com/new-og.jpg"
}
```

**Validation Rules:**
- `slug` - Required, must start with `/`, unique
- `title` - Required, max 255 characters
- `metaTitle` - Optional, max 255 characters
- `metaDescription` - Optional, max 500 characters

**Response:** `201 Created`
```json
{
  "id": 4,
  "slug": "/new-page",
  "title": "New Page",
  ...
}
```

**Error Responses:**
- `400 Bad Request` - Validation error
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "message": "Validation failed",
  "errors": {
    "slug": "Slug must start with /"
  }
}
```

**Example:**
```bash
curl -X POST http://localhost:8081/api/cms/pages \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "/new-page",
    "title": "New Page",
    "breadcrumbTitle": "New",
    "metaTitle": "New Page",
    "robotsIndex": true,
    "robotsFollow": true
  }'
```

---

#### Update Page

```http
PUT /api/cms/pages/{id}
```

**Description:** Update an existing page.

**Parameters:**
- `id` (path) - Page ID

**Request Body:** Same as Create Page

**Response:** `200 OK` (updated page)

**Example:**
```bash
curl -X PUT http://localhost:8081/api/cms/pages/1 \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "/",
    "title": "Updated Home Page",
    "breadcrumbTitle": "Home",
    "metaTitle": "Home - Updated",
    "robotsIndex": true,
    "robotsFollow": true
  }'
```

---

#### Delete Page

```http
DELETE /api/cms/pages/{id}
```

**Description:** Delete a page.

**Parameters:**
- `id` (path) - Page ID

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` - Page does not exist

**Example:**
```bash
curl -X DELETE http://localhost:8081/api/cms/pages/4
```

---

### Products

#### List All Products

```http
GET /api/cms/products
```

**Description:** Get all products for CMS management.

**Response:** `200 OK` (same structure as Storefront API)

**Example:**
```bash
curl http://localhost:8081/api/cms/products
```

---

#### Get Product by ID

```http
GET /api/cms/products/{id}
```

**Description:** Get a single product by ID for editing.

**Parameters:**
- `id` (path) - Product ID

**Response:** `200 OK`

**Example:**
```bash
curl http://localhost:8081/api/cms/products/1
```

---

#### Create Product

```http
POST /api/cms/products
```

**Description:** Create a new product.

**Request Body:**
```json
{
  "code": "new-product",
  "name": "New Product",
  "price": 99.99,
  "description": "Description of the product",
  "imageUrl": "https://example.com/product.jpg"
}
```

**Validation Rules:**
- `code` - Required, unique, alphanumeric + hyphens
- `name` - Required, max 255 characters
- `price` - Required, must be positive
- `description` - Optional, max 2000 characters
- `imageUrl` - Optional, valid URL

**Response:** `201 Created`

**Example:**
```bash
curl -X POST http://localhost:8081/api/cms/products \
  -H "Content-Type: application/json" \
  -d '{
    "code": "new-product",
    "name": "New Product",
    "price": 99.99,
    "description": "Amazing product",
    "imageUrl": "https://example.com/product.jpg"
  }'
```

---

#### Update Product

```http
PUT /api/cms/products/{id}
```

**Description:** Update an existing product.

**Parameters:**
- `id` (path) - Product ID

**Request Body:** Same as Create Product

**Response:** `200 OK` (updated product)

**Example:**
```bash
curl -X PUT http://localhost:8081/api/cms/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "code": "macbook-pro",
    "name": "MacBook Pro 16\" (2024)",
    "price": 2599.00,
    "description": "Updated description",
    "imageUrl": "https://example.com/macbook-2024.jpg"
  }'
```

---

#### Delete Product

```http
DELETE /api/cms/products/{id}
```

**Description:** Delete a product.

**Parameters:**
- `id` (path) - Product ID

**Response:** `204 No Content`

**Example:**
```bash
curl -X DELETE http://localhost:8081/api/cms/products/5
```

---

### Articles

#### List / Get / Create / Update / Delete Articles
- `GET /api/cms/articles`: List all STAGED articles.
- `GET /api/cms/articles/{id}`: Get article by ID.
- `POST /api/cms/articles`: Create article (`code`, `title`, `summary`, `content`, `author`, `imageUrl`).
- `PUT /api/cms/articles/{id}`: Update article.
- `DELETE /api/cms/articles/{id}`: Delete article.

---

### Events

#### List / Get / Create / Update / Delete Events
- `GET /api/cms/events`: List all STAGED events.
- `GET /api/cms/events/{id}`: Get event by ID.
- `POST /api/cms/events`: Create event (`code`, `title`, `description`, `location`, `eventDate`, `imageUrl`).
- `PUT /api/cms/events/{id}`: Update event.
- `DELETE /api/cms/events/{id}`: Delete event.

---

### Item Search & Metadata

#### Get Item Search Metadata
```http
GET /api/cms/items/{type}/search-metadata
```
**Description:** Retrieve searchable fields and allowed operators for dynamic component item selection (types: `product`, `article`, `event`, `page`, `slot`, `component`).

#### Query Searchable Items
```http
POST /api/cms/items/{type}/search
```
**Request Body:**
```json
{
  "criteria": [
    {
      "field": "name",
      "operator": "CONTAINS",
      "value": "Pro"
    }
  ]
}
```

---

### Slot & Component Management

#### Slot Operations
- `GET /api/cms/slots/page/{pageId}`: Get all slots and components for a page.
- `GET /api/cms/slots/{id}`: Get slot by ID.
- `POST /api/cms/slots`: Create slot (`code`, `name`, `pageId`).
- `PUT /api/cms/slots/{id}`: Update slot name/code.
- `DELETE /api/cms/slots/{id}`: Delete slot.

#### Component Operations & Linking
- `POST /api/cms/components`: Create component (polymorphic request body with `type`, `slotId`, `sortOrder`).
- `POST /api/cms/components/slots/{slotId}/components/{componentId}`: Link existing component to a slot at specific index.
- `PUT /api/cms/components/{id}`: Update component fields.
- `PUT /api/cms/components/slots/{slotId}/components/{id}/reorder`: Reorder component in slot (`{"sortOrder": 2}`).
- `DELETE /api/cms/components/slots/{slotId}/components/{id}`: Remove component from slot without deleting entity.
- `DELETE /api/cms/components/{id}`: Permanently delete component entity.

---

### Catalog Synchronization

#### Sync Catalog
```http
POST /api/sync/{catalogId}
```
**Description:** Deep-copy and publish all `STAGED` content to the specified catalog version (e.g., `ONLINE`).

#### Sync Single Item
```http
POST /api/sync/item/{entityType}/{itemId}
```
**Description:** Publish an individual item (`page`, `slot`, `component`, `product`, `article`, `event`) from STAGED to ONLINE.

---

### Component Schema Discovery & Media

#### Component Types & Schemas
- `GET /api/cms/components/types`: Returns list of all 10 component type enum strings.
- `GET /api/cms/components/types/{type}/schema`: Returns reflection-scanned field definitions, types, required flags, and item picker metadata for dynamic CMS forms.

#### Upload Media
- `POST /api/cms/media/upload`: Upload multipart file. Returns relative URL (e.g., `/uploads/image.png`).

---

## Component Types

### 1. Banner Component (`BANNER`)
```json
{
  "type": "BANNER",
  "uid": "hero-banner-1",
  "name": "Hero Banner",
  "title": "Welcome to Our Store",
  "subtitle": "Shop the latest products",
  "imageUrl": "https://example.com/banner.jpg",
  "altText": "Store Banner",
  "ctaText": "Shop Now",
  "ctaUrl": "/products"
}
```

### 2. Paragraph Component (`PARAGRAPH`)
```json
{
  "type": "PARAGRAPH",
  "uid": "about-paragraph-1",
  "name": "About Paragraph",
  "title": "About Us",
  "content": "<p>We are a leading retailer...</p>"
}
```

### 3. Product Carousel Component (`PRODUCT_CAROUSEL`)
```json
{
  "type": "PRODUCT_CAROUSEL",
  "uid": "featured-products-1",
  "name": "Featured Products",
  "title": "Featured Products",
  "productCodes": "macbook-pro,iphone-15-pro"
}
```

### 4. Navigation Component (`NAVIGATION`)
```json
{
  "type": "NAVIGATION",
  "uid": "nav-link-1",
  "name": "Home Link",
  "displayText": "Home",
  "url": "/",
  "icon": "home"
}
```

### 5. Quick Menu Component (`QUICK_MENU`)
```json
{
  "type": "QUICK_MENU",
  "uid": "quick-menu-1",
  "name": "New Arrivals Tile",
  "title": "New Arrivals",
  "imageUrl": "https://example.com/new.jpg",
  "url": "/new-arrivals"
}
```

### 6. Product Detail Component (`PRODUCT_DETAIL`)
```json
{
  "type": "PRODUCT_DETAIL",
  "uid": "prod-detail-1",
  "name": "Product Detail Layout",
  "title": "Product Overview",
  "showPrice": true,
  "showDescription": true
}
```

### 7. Latest Article Component (`LATEST_ARTICLE`)
```json
{
  "type": "LATEST_ARTICLE",
  "uid": "latest-articles-1",
  "name": "Latest Tech News",
  "title": "Latest Articles",
  "articleCount": 4
}
```

### 8. Trending Article Component (`TRENDING_ARTICLE`)
```json
{
  "type": "TRENDING_ARTICLE",
  "uid": "trending-articles-1",
  "name": "Curated Trending Articles",
  "title": "Trending Now",
  "articleIds": "1,2,3"
}
```

### 9. Latest Event Component (`LATEST_EVENT`)
```json
{
  "type": "LATEST_EVENT",
  "uid": "latest-events-1",
  "name": "Upcoming Tech Events",
  "title": "Events & Webinars",
  "eventIds": "1,2"
}
```

### 10. Top Event Component (`TOP_EVENT`)
```json
{
  "type": "TOP_EVENT",
  "uid": "top-event-1",
  "name": "Featured Conference",
  "title": "Don't Miss Out",
  "eventId": "1"
}
```

---

## Error Response Format

All errors follow this structure:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "message": "Error message",
  "path": "/api/endpoint"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no response body
- `400 Bad Request` - Validation error
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Caching Behavior

**Storefront API:**
- Pages: Cached for 15 minutes
- Slots: Cached for 15 minutes
- Products: Cached for 30 minutes

**CMS API:**
- No caching (always fresh data)
- Evicts relevant cache entries on updates

**Cache Key Format:**
```
page:{slug}            # e.g., page:index
slot:{id}              # e.g., slot:1
product:{code}         # e.g., product:macbook-pro
products:all           # List of all products
```

---

## Rate Limiting

**Current Implementation:** None (development only)

**Recommended for Production:**
- 100 requests/minute for read endpoints
- 10 requests/minute for write endpoints
- Higher limits for authenticated users

---

## Testing with Postman

Import this collection to test all endpoints:

1. Create a new Postman Collection
2. Add environment variables:
   - `storefront_url`: `http://localhost:8080`
   - `cms_url`: `http://localhost:8081`
3. Import the requests from this documentation

---

## GraphQL Alternative (Future Enhancement)

Consider implementing GraphQL for more flexible querying:

```graphql
query {
  page(slug: "/") {
    title
    slots {
      components {
        ... on Banner {
          title
          imageUrl
        }
        ... on ProductCarousel {
          products {
            name
            price
          }
        }
      }
    }
  }
}
```

This would reduce over-fetching and allow clients to request exactly what they need.
