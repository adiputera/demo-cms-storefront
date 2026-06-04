# Headless CMS Demo Application

A full-stack Headless CMS demonstrating **runtime-driven page composition** with slots and dynamic component rendering.

## 🎯 Project Purpose

This is a **learning and demonstration project** showcasing:

- ✅ Clean API design with separation of read/write operations
- ✅ Dynamic page composition without requiring redeployment
- ✅ Slot-based content management architecture
- ✅ Type-safe frontend development with TypeScript
- ✅ Redis caching for horizontal scalability
- ✅ Modern Spring Boot 4 and Next.js 16 stack

**This is NOT a production CMS** - it's intentionally simplified for educational purposes.

## 🏗️ Architecture

```
┌─────────────────────┐
│   Next.js Frontend  │  (Port 3000)
│  - Storefront       │
│  - CMS Admin UI     │
└──────────┬──────────┘
           │
    ┌──────┴───────┐
    │              │
┌───▼──────────┐ ┌─▼────────────────┐
│ Storefront   │ │  CMS Backend     │
│ Backend      │ │  (Write APIs)    │
│ (Read APIs)  │ │  Port 8081       │
│ Port 8080    │ └─────────┬────────┘
└──────┬───────┘           │
       │                   │
    ┌──┴──────────────────┴───┐
    │                          │
┌───▼───────┐          ┌──────▼──────┐
│   Redis   │          │ PostgreSQL  │
│ (Cache)   │          │  (Database) │
│ Port 6379 │          │  Port 5432  │
└───────────┘          └─────────────┘
```

### Key Design Decisions

**Separate Read/Write Backends:**
- **Storefront Backend (8080)**: Read-only operations with aggressive caching
- **CMS Backend (8081)**: Write operations with automatic cache eviction
- Benefits: Different scaling profiles, clear separation of concerns, cache invalidation isolation

**Slot-Based Composition:**
- Pages contain Slots (hero, content, footer)
- Slots contain Components (banner, paragraph, product carousel, navigation, quick menu)
- Frontend dynamically renders components based on CMS data
- **No code changes needed** when editors add/remove/reorder components

**Redis Caching:**
- External cache (not in-memory) for horizontal scaling
- TTL: 15 minutes for pages/slots, 30 minutes for products
- Automatic eviction on CMS updates

## 📦 Tech Stack

### Backend
- **Java 25** with **Spring Boot 4.0-M1**
- **Maven** multi-module project (3 modules)
- **Spring Data JPA** with **Hibernate 7**
- **PostgreSQL 16** database
- **Flyway** for database migrations
- **Redis 7** for caching
- **Lombok** for boilerplate reduction
- **Jakarta Validation** for request validation

### Frontend
- **Next.js 16.2.7** with TypeScript
- **App Router** (React Server Components)
- **Tailwind CSS** for styling
- Component-based architecture with discriminated unions

## 🚀 Quick Start

### Prerequisites

- Java 25
- Maven 3.9+
- Node.js 18+ with npm
- Docker (for PostgreSQL and Redis)
- PostgreSQL 16 (or use Docker)
- Redis 7 (or use Docker)

### 1. Start Infrastructure

**Option A: Using existing Docker containers**
```bash
# If you already have PostgreSQL and Redis running
docker ps | grep -E "postgres|redis"
```

**Option B: Start with Docker Compose**
```bash
# See docker-compose.yml section below for setup
docker-compose up -d postgres redis
```

### 2. Build the Project

```bash
cd dummy-storefront-cms

# Build all modules
mvn clean install -DskipTests

# Or build individually
mvn clean install -DskipTests -pl shared-entities
mvn clean compile -DskipTests -pl storefront-backend
mvn clean compile -DskipTests -pl cms-backend
```

### 3. Run Database Migrations

```bash
cd cms-backend
DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres mvn flyway:migrate
```

### 4. Start Backend Services

**Terminal 1 - Storefront Backend (Read APIs)**
```bash
cd storefront-backend
DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres mvn spring-boot:run
```

**Terminal 2 - CMS Backend (Write APIs)**
```bash
cd cms-backend
DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres mvn spring-boot:run
```

**Verify backends are running:**
```bash
curl http://localhost:8080/actuator/health  # Should return {"status":"UP"}
curl http://localhost:8081/actuator/health  # Should return {"status":"UP"}
```

### 5. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

### 6. Access the Application

- **Storefront**: http://localhost:3000
- **CMS Admin**: http://localhost:3000/cms
- **Storefront API**: http://localhost:8080
- **CMS API**: http://localhost:8081

## 📚 Project Structure

```
dummy-storefront-cms/
├── pom.xml                          # Parent POM
├── shared-entities/                 # Shared JPA entities and DTOs
│   ├── src/main/java/
│   │   └── com/demo/cms/
│   │       ├── entity/              # JPA entities
│   │       ├── dto/                 # Data Transfer Objects
│   │       └── mapper/              # EntityMapper
│   └── pom.xml
│
├── storefront-backend/              # Read-only API (port 8080)
│   ├── src/main/java/
│   │   └── com/demo/cms/storefront/
│   │       ├── controller/          # REST controllers
│   │       ├── service/             # Business logic
│   │       ├── repository/          # JPA repositories
│   │       ├── config/              # Cache configuration
│   │       └── exception/           # Exception handling
│   └── pom.xml
│
├── cms-backend/                     # Write API (port 8081)
│   ├── src/main/java/
│   │   └── com/demo/cms/admin/
│   │       ├── controller/          # CRUD controllers
│   │       ├── service/             # Business logic
│   │       ├── repository/          # JPA repositories
│   │       ├── dto/                 # Request/Response DTOs
│   │       └── exception/           # Exception handling
│   ├── src/main/resources/
│   │   └── db/migration/            # Flyway SQL scripts
│   └── pom.xml
│
└── frontend/                        # Next.js application
    ├── src/
    │   ├── app/
    │   │   ├── page.tsx             # Homepage
    │   │   ├── [...slug]/           # Dynamic routes
    │   │   └── cms/                 # Admin UI
    │   ├── components/
    │   │   ├── cms/                 # CMS components
    │   │   ├── ComponentRenderer.tsx
    │   │   └── SlotRenderer.tsx
    │   ├── lib/
    │   │   ├── api-client.ts        # Storefront API client
    │   │   └── cms-api-client.ts    # CMS API client
    │   └── types/
    │       └── index.ts             # TypeScript types
    ├── package.json
    └── next.config.ts
```

## 🗄️ Database Schema

```sql
-- Core Tables
pages                    -- CMS pages
page_breadcrumbs        -- Page hierarchy
slots                   -- Content slots
components              -- Base component table

-- Component Type Tables (Table-Per-Subclass Inheritance)
paragraph_components    -- Text content
banner_components       -- Hero banners
product_carousel_components  -- Product lists
navigation_components   -- Navigation links
quick_menu_components   -- Quick menu tiles

-- Supporting Tables
products                -- Product catalog
```

**Key relationships:**
- Page 1:N Slots
- Slot 1:N Components
- Component → subtype (JOINED inheritance)

## 🔌 API Endpoints

### Storefront Backend (Port 8080) - Read Only

```http
# Pages
GET /api/pages/{slug}           # Get page by slug (/, /about-us, etc.)

# Slots
POST /api/slots/details          # Batch fetch slots with components
Body: {"slotIds": [1,2,3]}

# Products
GET /api/products                # List all products
GET /api/products/{code}         # Get product by code
POST /api/products/batch         # Batch fetch products
Body: {"codes": ["macbook-pro", "iphone-15-pro"]}
```

### CMS Backend (Port 8081) - Write Operations

```http
# Pages
GET /api/cms/pages               # List all pages
GET /api/cms/pages/{id}          # Get page by ID
POST /api/cms/pages              # Create page
PUT /api/cms/pages/{id}          # Update page
DELETE /api/cms/pages/{id}       # Delete page

# Products
GET /api/cms/products            # List all products
GET /api/cms/products/{id}       # Get product by ID
POST /api/cms/products           # Create product
PUT /api/cms/products/{id}       # Update product
DELETE /api/cms/products/{id}    # Delete product
```

## 🎨 Component Types

1. **Banner Component**: Hero images with title, subtitle, CTA button
2. **Paragraph Component**: Rich text content with optional title
3. **Product Carousel Component**: Grid of products from codes
4. **Navigation Component**: Links with optional icons
5. **Quick Menu Component**: Image tiles with links

## 🧪 Testing the System

### 1. View Demo Content

```bash
# Homepage
curl http://localhost:3000

# About page
curl http://localhost:3000/about-us

# View API response
curl http://localhost:8080/api/pages/index
```

### 2. Create a New Page via CMS

```bash
# Option A: Use CMS Admin UI
open http://localhost:3000/cms/pages

# Option B: Use API directly
curl -X POST http://localhost:8081/api/cms/pages \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "/test-page",
    "title": "Test Page",
    "breadcrumbTitle": "Test",
    "metaTitle": "Test Page Title",
    "robotsIndex": true,
    "robotsFollow": true
  }'
```

### 3. Verify Dynamic Rendering

```bash
# Page should appear immediately without redeployment
curl http://localhost:3000/test-page
```

### 4. Test Cache Eviction

```bash
# 1. Fetch a page (cached)
curl http://localhost:8080/api/pages/index

# 2. Update via CMS
curl -X PUT http://localhost:8081/api/cms/pages/1 \
  -H "Content-Type: application/json" \
  -d '{...updated data...}'

# 3. Fetch again (cache evicted, fresh data)
curl http://localhost:8080/api/pages/index
```

## 🎯 Key Features Demonstrated

### 1. Runtime-Driven Composition
No deployment needed when CMS updates content:
```
Editor adds component → CMS backend saves → Cache evicted → Storefront fetches fresh data → Component renders
```

### 2. Dynamic Component Rendering
```typescript
// Frontend automatically handles new component types
const componentRegistry = {
  BANNER: BannerComponent,
  PARAGRAPH: ParagraphComponent,
  PRODUCT_CAROUSEL: ProductCarouselComponent,
  // Add new types here - no other code changes needed
};
```

### 3. Type-Safe API Contracts
```typescript
// Discriminated unions ensure type safety
type Component = 
  | BannerComponent 
  | ParagraphComponent 
  | ProductCarouselComponent;

// TypeScript enforces correct props per type
```

### 4. Polymorphic JSON Serialization
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BannerComponentDTO.class, name = "BANNER"),
    // ... other types
})
public abstract class ComponentDTO { }
```

## 📈 Performance Considerations

**Caching Strategy:**
- ✅ Redis external cache (stateless backends)
- ✅ 15-minute TTL for pages/slots
- ✅ 30-minute TTL for products
- ✅ Automatic eviction on CMS updates

**Database Optimization:**
- ✅ Indexed columns: slug, code
- ✅ JOIN FETCH to avoid N+1 queries
- ✅ Batch slot/product fetching

**Scalability:**
- ✅ Stateless backends (can scale horizontally)
- ✅ External cache (shared across instances)
- ✅ Read/write separation (different scaling needs)

## 🚫 What's NOT Included (By Design)

This is a **demo project**, not production software. The following are intentionally excluded:

- ❌ Authentication/Authorization
- ❌ User management
- ❌ Content versioning
- ❌ Workflow approval
- ❌ Multi-language support
- ❌ Content scheduling
- ❌ A/B testing
- ❌ Personalization
- ❌ E-commerce features (cart, checkout, orders)

## 🐛 Troubleshooting

### Backends won't start

```bash
# Check if ports are available
lsof -i :8080
lsof -i :8081

# Check database connection
psql -h localhost -U postgres -d headless_cms

# Check Redis connection
redis-cli ping
```

### Database migration fails

```bash
# Manually run migrations
cd cms-backend
DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres \
  mvn flyway:migrate
```

### Frontend can't connect to backends

```bash
# Check environment variables
cat frontend/.env.local

# Should contain:
# NEXT_PUBLIC_STOREFRONT_API_URL=http://localhost:8080/api
# NEXT_PUBLIC_CMS_API_URL=http://localhost:8081/api/cms
```

### Component not found errors

```bash
# Rebuild shared-entities module
mvn clean install -DskipTests -pl shared-entities

# Restart backends
pkill -f "storefront-backend"
pkill -f "cms-backend"
# Then start them again
```

## 🔧 Configuration

### Backend Environment Variables

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/headless_cms
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Frontend Environment Variables

```bash
# .env.local
NEXT_PUBLIC_STOREFRONT_API_URL=http://localhost:8080/api
NEXT_PUBLIC_CMS_API_URL=http://localhost:8081/api/cms
```

## 📝 License

This is a demonstration project for educational purposes. Feel free to use it as a learning resource or starting point for your own projects.

## 🤝 Contributing

This is a demo project and not actively maintained, but feel free to fork it and adapt it for your needs!

## 📧 Questions?

This project demonstrates:
- Modern Java/Spring Boot architecture
- Next.js App Router patterns
- Dynamic content management
- Cache invalidation strategies
- Type-safe full-stack development

Use it as a reference for building your own CMS or understanding these architectural patterns.
