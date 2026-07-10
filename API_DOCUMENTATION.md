# API Documentation

Complete API reference for the Headless CMS Demo Application.

> **Tech Stack**: Spring Boot 4.0.0-M1 | Java 25 | PostgreSQL 16 | Redis 7  
> **Base Package**: `id.adiputera.demo.cms`

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

#### List All Articles
```http
GET /api/cms/articles
```
**Description:** Get all articles in STAGED catalog.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "uid": "introducing-macbook",
    "title": "Introducing the MacBook Pro 16",
    "slug": "introducing-the-macbook-pro-16",
    "body": "The new MacBook Pro 16 sets a new standard...",
    "author": "John Doe",
    "imageUrl": "/uploads/macbook-article.jpg",
    "syncStatus": "SYNCED"
  }
]
```

#### Create Article
```http
POST /api/cms/articles
```
**Request Body:**
```json
{
  "uid": "my-article-uid",
  "title": "My Article Title",
  "slug": "my-article-slug",
  "body": "Article content...",
  "author": "Jane Smith",
  "imageUrl": "/uploads/article.jpg"
}
```

**Validation Rules:**
- `uid` - Required, unique across catalog
- `title` - Required, max 255 characters
- `slug` - Required, URL-friendly
- `body` - Required, supports HTML

**Response:** `201 Created`

#### Update / Delete Articles
- `GET /api/cms/articles/{id}`: Get article by ID
- `PUT /api/cms/articles/{id}`: Update article
- `DELETE /api/cms/articles/{id}`: Delete article

---

### Events

#### List All Events
```http
GET /api/cms/events
```
**Description:** Get all events in STAGED catalog.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "uid": "apple-summit-2026",
    "title": "Apple Tech Summit 2026",
    "slug": "apple-tech-summit-2026",
    "description": "Join us for a full day of talks...",
    "location": "Jakarta",
    "eventDate": "2026-09-15T09:00:00",
    "imageUrl": "/uploads/summit.jpg",
    "syncStatus": "NOT_SYNCED"
  }
]
```

#### Create Event
```http
POST /api/cms/events
```
**Request Body:**
```json
{
  "uid": "my-event-uid",
  "title": "My Event",
  "slug": "my-event",
  "description": "Event description",
  "location": "City Name",
  "eventDate": "2026-12-01T10:00:00",
  "imageUrl": "/uploads/event.jpg"
}
```

**Validation Rules:**
- `uid` - Required, unique across catalog
- `title` - Required, max 255 characters
- `eventDate` - Required, ISO 8601 datetime format
- `location` - Optional, max 255 characters

**Response:** `201 Created`

#### Update / Delete Events
- `GET /api/cms/events/{id}`: Get event by ID
- `PUT /api/cms/events/{id}`: Update event
- `DELETE /api/cms/events/{id}`: Delete event

---

### Item Search & Metadata

#### Get Item Search Metadata
```http
GET /api/cms/items/{type}/search-metadata
```
**Description:** Retrieve searchable fields and allowed operators for dynamic component item selection.

**Supported Types:** `product`, `article`, `event`, `page`, `slot`, `component`

**Response:** `200 OK`
```json
{
  "type": "product",
  "fields": [
    {
      "name": "name",
      "type": "STRING",
      "operators": ["CONTAINS", "EQUALS"]
    },
    {
      "name": "price",
      "type": "NUMBER",
      "operators": ["EQUALS", "MORE_THAN", "LESS_THAN"]
    },
    {
      "name": "code",
      "type": "STRING",
      "operators": ["CONTAINS", "EQUALS"]
    }
  ]
}
```

**Example:**
```bash
curl http://localhost:8081/api/cms/items/product/search-metadata
```

---

#### Query Searchable Items
```http
POST /api/cms/items/{type}/search
```
**Description:** Search items with dynamic criteria. Used by CMS Admin UI for component item pickers.

**Request Body:**
```json
{
  "criteria": [
    {
      "field": "name",
      "operator": "CONTAINS",
      "value": "Pro"
    },
    {
      "field": "price",
      "operator": "MORE_THAN",
      "value": "1000"
    }
  ]
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "syncKey": "macbook-pro",
    "displayName": "MacBook Pro 16\"",
    "catalogVersion": "STAGED",
    "syncStatus": "SYNCED"
  },
  {
    "id": 3,
    "syncKey": "iphone-15-pro",
    "displayName": "iPhone 15 Pro",
    "catalogVersion": "STAGED",
    "syncStatus": "OUT_OF_SYNC"
  }
]
```

**Supported Operators:**
- `CONTAINS` - Case-insensitive substring match (STRING fields)
- `EQUALS` - Exact match (STRING, NUMBER, BOOLEAN)
- `MORE_THAN` - Greater than (NUMBER fields)
- `LESS_THAN` - Less than (NUMBER fields)

**Example:**
```bash
curl -X POST http://localhost:8081/api/cms/items/product/search \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": [
      {"field": "name", "operator": "CONTAINS", "value": "MacBook"}
    ]
  }'
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

#### Sync Entire Catalog
```http
POST /api/sync/{catalogId}
```
**Description:** Deep-copy and publish all `STAGED` content to the specified catalog version (e.g., `ONLINE`). This is a bulk operation that synchronizes all pages, slots, components, products, articles, and events.

**Parameters:**
- `catalogId` (path) - Target catalog ID (typically `2` for ONLINE)

**Response:** `200 OK`
```json
{
  "message": "Catalog synced successfully",
  "itemsSynced": 127,
  "timestamp": "2026-07-10T14:30:00Z"
}
```

**Side Effects:**
- Evicts all Redis cache entries for storefront
- Updates `syncVersion` for all affected entities
- Creates or updates entities in target catalog

**Example:**
```bash
curl -X POST http://localhost:8081/api/sync/2
```

---

#### Sync Single Item
```http
POST /api/sync/item/{entityType}/{itemId}
```
**Description:** Granular synchronization - publish an individual item from STAGED to ONLINE. Useful for selective publishing without syncing the entire catalog.

**Parameters:**
- `entityType` (path) - Entity type: `page`, `slot`, `component`, `product`, `article`, `event`
- `itemId` (path) - Item ID in STAGED catalog

**Response:** `200 OK`
```json
{
  "message": "Item synced successfully",
  "entityType": "product",
  "itemId": 15,
  "syncStatus": "SYNCED"
}
```

**Example:**
```bash
# Sync a single product
curl -X POST http://localhost:8081/api/sync/item/product/15

# Sync a single page
curl -X POST http://localhost:8081/api/sync/item/page/3
```

---

### Component Schema Discovery & Media

#### Get Component Types
```http
GET /api/cms/components/types
```
**Description:** Get list of all registered component type enum strings discovered via `@CmsComponent` annotation.

**Response:** `200 OK`
```json
[
  "BANNER",
  "PARAGRAPH",
  "PRODUCT_CAROUSEL",
  "NAVIGATION",
  "QUICK_MENU",
  "PRODUCT_DETAIL",
  "LATEST_ARTICLE",
  "TRENDING_ARTICLE",
  "LATEST_EVENT",
  "TOP_EVENT"
]
```

---

#### Get Component Schema
```http
GET /api/cms/components/types/{type}/schema
```
**Description:** Get reflection-generated schema for dynamic form rendering. Returns field definitions, types, validation rules, and item picker metadata.

**Parameters:**
- `type` (path) - Component type (e.g., `BANNER`, `PRODUCT_CAROUSEL`)

**Response:** `200 OK`
```json
{
  "type": "PRODUCT_CAROUSEL",
  "fields": [
    {
      "name": "title",
      "type": "text",
      "required": true,
      "label": "Title"
    },
    {
      "name": "productCodes",
      "type": "item_picker",
      "required": true,
      "label": "Products",
      "itemType": "product",
      "multiple": true,
      "syncKeyField": "code"
    }
  ]
}
```

**Example:**
```bash
curl http://localhost:8081/api/cms/components/types/PRODUCT_CAROUSEL/schema
```

---

#### Upload Media
```http
POST /api/cms/media/upload
```
**Description:** Upload multipart file to shared volume. Files are stored in `/uploads/` directory and accessible via Next.js proxy.

**Request:** `multipart/form-data`
- `file` - File field (accepts images: jpg, png, gif, webp)

**Response:** `200 OK`
```json
{
  "url": "/uploads/image-1720614000.jpg",
  "filename": "image-1720614000.jpg",
  "size": 245678
}
```

**Example:**
```bash
curl -X POST http://localhost:8081/api/cms/media/upload \
  -F "file=@/path/to/image.jpg"
```

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
**Uses SyncKey**: References products via `productCodes` (comma-separated string) instead of database IDs.

```json
{
  "type": "PRODUCT_CAROUSEL",
  "uid": "featured-products-1",
  "name": "Featured Products",
  "title": "Featured Products",
  "productCodes": "macbook-pro,iphone-15-pro,macbook-air"
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
**Automatic Selection**: Displays the N most recent articles ordered by creation date.

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
**Uses SyncKey**: References articles via `articleUids` (comma-separated UIDs) instead of database IDs.

```json
{
  "type": "TRENDING_ARTICLE",
  "uid": "trending-articles-1",
  "name": "Curated Trending Articles",
  "title": "Trending Now",
  "articleUids": "introducing-macbook,iphone-photography,mac-accessories"
}
```

### 9. Latest Event Component (`LATEST_EVENT`)
**Uses SyncKey**: References events via `eventUids` (comma-separated UIDs) instead of database IDs.

```json
{
  "type": "LATEST_EVENT",
  "uid": "latest-events-1",
  "name": "Upcoming Tech Events",
  "title": "Events & Webinars",
  "eventUids": "apple-summit-2026,tech-conference-2026"
}
```

### 10. Top Event Component (`TOP_EVENT`)
**Uses SyncKey**: References a single event via `eventUid` instead of database ID.

```json
{
  "type": "TOP_EVENT",
  "uid": "top-event-1",
  "name": "Featured Conference",
  "title": "Don't Miss Out",
  "eventUid": "apple-summit-2026"
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

**Storefront API (Redis TTL):**
- Pages: Cached for 15 minutes (`page:{slug}`)
- Slots: Cached for 15 minutes (`slot:{id}`)
- Products: Cached for 30 minutes (`product:{code}`, `products:all`)
- Articles: Cached for 20 minutes (`articles:latest`, `article:{uid}`)
- Events: Cached for 20 minutes (`events:latest`, `event:{uid}`)

**CMS API:**
- No caching (always reads from database)
- Write operations evict relevant storefront cache entries
- Sync operations trigger `FLUSHALL` on Redis to clear entire cache

**Cache Key Format:**
```
page:{slug}              # e.g., page:/about-us
slot:{id}                # e.g., slot:1
product:{code}           # e.g., product:macbook-pro
products:all             # List of all products
article:{uid}            # e.g., article:introducing-macbook
articles:latest          # Latest articles list
event:{uid}              # e.g., event:apple-summit-2026
events:upcoming          # Upcoming events list
```

**Cache Eviction Strategy:**
- Manual sync: `FLUSHALL` (clears all keys)
- Individual item update: Evicts specific key and related list keys
- Component update: Evicts parent slot and page keys

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

## SyncKey Architecture

This CMS uses **SyncKey-based references** instead of database IDs to maintain referential integrity across catalog versions (STAGED vs ONLINE).

**Why SyncKeys?**
- Database IDs differ between STAGED and ONLINE catalogs
- Components need stable references that work in both catalogs
- Sync operations must preserve relationships correctly

**SyncKey Mappings:**
- Products: `code` field (e.g., `"macbook-pro"`)
- Articles: `uid` field (e.g., `"introducing-macbook"`)
- Events: `uid` field (e.g., `"apple-summit-2026"`)
- Components: `uid` field (unique across system)
- Pages: `slug` field (e.g., `"/about-us"`)

**Component SyncKey Fields:**
```
Component Type          | SyncKey Field        | Format
------------------------|----------------------|------------------
PRODUCT_CAROUSEL        | productCodes         | CSV string
TRENDING_ARTICLE        | articleUids          | CSV string
LATEST_EVENT            | eventUids            | CSV string
TOP_EVENT               | eventUid             | Single string
```

---

## Additional Resources

- **[README.md](README.md)** - Project overview and setup guide
- **[QUICKSTART.md](QUICKSTART.md)** - Detailed walkthrough with troubleshooting
- **[SLOT_COMPONENT_MANAGEMENT.md](SLOT_COMPONENT_MANAGEMENT.md)** - Architecture deep-dive
- **[VERIFICATION_REPORT.md](VERIFICATION_REPORT.md)** - Recent migrations and fixes

---

## Future Enhancements

### GraphQL Support
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

### Webhooks
Notify external systems when content is published:
```bash
POST https://external-system.com/webhook
{
  "event": "catalog.synced",
  "catalogId": 2,
  "timestamp": "2026-07-10T14:30:00Z"
}
```

### Content Versioning
Track historical versions of each content item for rollback capability.
