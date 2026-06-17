# Headless CMS Demo Application

A full-stack Headless CMS demonstrating **runtime-driven page composition** with dynamic slots, components, and customizable templates.

> [!NOTE]
> This is a **learning and demonstration project** showcasing advanced, schema-driven page composition architecture. Authentication, workflow approval, content synchronization, and content scheduling are omitted by design to focus on metadata schema mapping and flexible presentation flows.

---

## 🏗️ Architecture

```
┌──────────────────────┐   ┌──────────────────────┐
│  Storefront Frontend │   │     CMS Frontend     │
│      (Port 3000)     │   │      (Port 3001)     │
└──────────┬───────────┘   └──────────┬───────────┘
           │                          │
           ├──────────────────────────┘
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

### Key Design Highlights

- **Dynamic Schema-Driven Form Generation**: The CMS Admin panel fetches component schemas from the backend (`/api/cms/components/types` and `/api/cms/components/types/{type}/schema`) and dynamically renders edit/creation input fields (strings, rich textareas, checkboxes, comma-separated lists). This completely eliminates hardcoded component form code in the frontend.
- **Maintainable & Customizable Product Details**: Product detail pages (`/products/[code]`) are now mapped to a CMS page layout (using `/products/detail` or a product-specific slug like `/products/macbook-pro` as the template). This allows editors to place any components (banners, carousels, text blocks) around the product info, and the storefront binds the loaded product context down to child components (like the new `PRODUCT_DETAIL` component) at runtime.
- **Separate Read & Write Services**: 
  - **Storefront Backend (8080)**: Fast read endpoints with aggressive Redis caching.
  - **CMS Backend (8081)**: Write operations with automatic Redis cache eviction upon page/component mutations.

---

## 📦 Tech Stack

### Backend
- **Java 25** with **Spring Boot 4.0**
- **Maven** multi-module project structure
- **Spring Data JPA** with **Hibernate 7** (using `JOINED` subclass inheritance for polymorphic components)
- **PostgreSQL 17** & **Redis 7**

### Frontend
- **Next.js 16.2.7** (Two independent services: `storefront-frontend` and `cms-frontend`)
- **TypeScript** & **Tailwind CSS**
- **React Server Components** for storefront pages
- Dynamic, polymorphic **ComponentRenderer** registry

---

## 🚀 Quick Start & Running Locally

### Prerequisites
- Docker (for database and cache containers)
- Node.js 18+ & npm
- Java 25 & Maven 3.9+

### Step 1: Start Infrastructure
Ensure PostgreSQL is running on port 5432 and Redis is running on port 6379:
```bash
# Start your existing PostgreSQL docker container
docker start postgresql

# Start or create the Redis container
docker start cms-redis || docker run -d --name cms-redis -p 6379:6379 redis:7-alpine
```

### Step 2: Set Database User Password
Configure the database user role credentials:
```bash
docker exec -t postgresql psql -U postgres -c "ALTER USER cms_user WITH PASSWORD 'cms_password';"
```

### Step 3: Build & Install Modules
Build the parent Maven project and install target `.jar` files locally:
```bash
mvn clean install -DskipTests
```

### Step 4: Run Backend Services

* **Terminal 1 - CMS Backend (Port 8081)**:
  ```bash
  cd cms-backend
  DATABASE_URL=jdbc:postgresql://localhost:5432/headless_cms DATABASE_USERNAME=cms_user DATABASE_PASSWORD=cms_password mvn spring-boot:run
  ```

* **Terminal 2 - Storefront Backend (Port 8080)**:
  ```bash
  cd storefront-backend
  DATABASE_URL=jdbc:postgresql://localhost:5432/headless_cms DATABASE_USERNAME=cms_user DATABASE_PASSWORD=cms_password mvn spring-boot:run
  ```

### Step 5: Start Frontend Applications

* **Terminal 3 - Storefront Frontend (Port 3000)**:
  ```bash
  cd storefront-frontend
  npm run dev
  ```

* **Terminal 4 - CMS Admin Frontend (Port 3001)**:
  ```bash
  cd cms-frontend
  npm run dev
  ```

---

## 🔌 Core API Endpoints

### Storefront Read-Only API (Port 8080)
- `GET /api/pages/{slug}`: Fetch page by slug (e.g. `index`, `about-us`).
- `POST /api/slots/details`: Batch fetch slots and components.
- `GET /api/products`: List all products.
- `GET /api/products/{code}`: Fetch product by code (e.g. `macbook-pro`).

### CMS Administrative Write API (Port 8081)
- `GET /api/cms/pages`: List all pages.
- `POST /api/cms/pages`: Create a new page (e.g., `/hahaha`).
- `GET /api/cms/components/types`: Get list of registered component types.
- `GET /api/cms/components/types/{type}/schema`: Get field schema for a component type.

---

## 🎨 Supported Component Types

1. **Product Details (`PRODUCT_DETAIL`)**: Displays the product title, image, price, and descriptive copy derived from the page context.
2. **Hero Banner (`BANNER`)**: Full-width image banner with title, subtitle, and CTA button.
3. **Paragraph Content (`PARAGRAPH`)**: Rich text blocks with HTML support.
4. **Product Carousel (`PRODUCT_CAROUSEL`)**: A responsive grid carousel displaying selected products by codes.
5. **Navigation Link (`NAVIGATION`)**: A simple text link with an optional icon.
6. **Quick Menu Tile (`QUICK_MENU`)**: A clickable grid tile card.

---

## 🔧 Verification & Testing

1. **Verify Storefront Homepage**: Open [http://localhost:3000](http://localhost:3000).
2. **Verify CMS Admin Page Management**: Open [http://localhost:3001/cms/pages](http://localhost:3001/cms/pages).
3. **Verify Product Detail Page**: Open [http://localhost:3000/products/macbook-pro](http://localhost:3000/products/macbook-pro). Try editing the product detail template page at `/products/detail` in the CMS Admin panel and watch the storefront adapt instantly.
