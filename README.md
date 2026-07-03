# Headless CMS Demo Application

A full-stack, Catalog-Aware Headless CMS demonstrating **runtime-driven page composition** with dynamic slots, components, and customizable templates. Content changes are isolated in a **STAGED** catalog and can be pushed to an **ONLINE** catalog via an automated sync feature.

> [!NOTE]
> This is a **learning and demonstration project** showcasing advanced, schema-driven page composition architecture. Authentication, workflow approval, and content scheduling are omitted by design to focus on metadata schema mapping and flexible presentation flows.

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

- **Multi-Version Catalog System (STAGED vs ONLINE)**: Content is isolated using a Catalog Aware schema. Editors work within a STAGED environment, ensuring work-in-progress content is invisible to customers. An automated, reflection-based deep copy synchronizes approved pages to the ONLINE storefront catalog.
- **Granular Sync Status Tracking**: All catalog-aware entities utilize a robust `syncVersion` strategy. The system intelligently computes and exposes `SYNCED`, `OUT_OF_SYNC`, and `NOT_SYNCED` statuses at runtime, allowing editors to selectively synchronize individual items or entire catalogs without relying on fragile timestamp comparisons.
- **Polymorphic Domain Modeling & Search (`ItemModel`)**: All domain entities (`Catalog`, `Product`, `Article`, `Event`) extend an abstract `@MappedSuperclass` (`ItemModel`), standardizing primary keys, creation/update timestamps, and lifecycle hooks. A unified `toItemSearchResultDTO()` polymorphic mapping powers a single, schema-agnostic search endpoint (`POST /api/cms/items/{type}/search`) across the entire CMS.
- **Dynamic Schema-Driven Form Generation**: The CMS Admin panel fetches component schemas from the backend via reflection (`@CmsComponent`) and dynamically renders input fields (strings, rich textareas, checkboxes, searchable product selectors, and drag-and-drop image uploaders). It features a robust item lookup dialog with real-time state deduplication across catalog versions and dynamic operators (`CONTAINS`, `EQUALS`, `GREATER_THAN`, `LESS_THAN`) constrained by field type.
- **Next.js Proxy Media Uploads**: Simplifies media management by utilizing Next.js API `rewrites`. Images uploaded via the CMS are proxied securely to the backend API and served seamlessly through native relative paths (`/uploads/*`), eliminating CORS issues and broken image previews without configuring an external CDN.
- **Maintainable & Customizable Product Details**: Product detail pages (`/products/[code]`) are mapped to a CMS page layout (using `/products/detail` as the template). This allows editors to place any components (banners, carousels, text blocks) around the product info, and the storefront binds the loaded product context down to child components at runtime.
- **Separate Read & Write Services**: 
  - **Storefront Backend (8080)**: Fast read endpoints with aggressive Redis caching, scoped strictly to the `ONLINE` catalog.
  - **CMS Backend (8081)**: Write operations, scoped to the `STAGED` catalog, with automatic Redis cache eviction and publishing controls.

---

## 📦 Tech Stack

### Backend
- **Java 25** with **Spring Boot 4.0**
- **Maven** multi-module project structure (`shared-entities`, `cms-backend`, `storefront-backend`)
- **Spring Data JPA** with **Hibernate 7**
- **Flyway** (`spring-boot-starter-flyway` with baseline auto-configuration) for schema migrations
- **PostgreSQL 16** & **Redis 7**

### Frontend
- **Next.js 16.2.7** (Two independent services: `storefront-frontend` and `cms-frontend`)
- **TypeScript** & **Tailwind CSS**
- **React Server Components** for storefront pages
- Dynamic, polymorphic **ComponentRenderer** registry

---

## 🚀 Quick Start & Running Locally

The entire stack is containerized and managed via Docker Compose.

### Prerequisites
- Docker & Docker Compose
- Java 25 & Maven 3.9+ (if you wish to build the backend locally outside of Docker)

### Step 1: Build the Application
You only need to build the backend Java binaries if you haven't yet, as the Dockerfile copies the built `.jar` files.
```bash
mvn clean package -DskipTests
```

### Step 2: Start the Full Stack Environment
Use Docker Compose to build the Node.js frontend images and start all 6 services (PostgreSQL, Redis, CMS Backend, Storefront Backend, CMS Frontend, Storefront Frontend).

```bash
docker compose up -d --build
```
*Note: The frontend containers perform a multi-stage production build upon first run. This might take a few minutes.*

### Step 3: Access the Applications
- **CMS Admin UI**: [http://localhost:3001/cms](http://localhost:3001/cms)
- **Storefront UI**: [http://localhost:3000](http://localhost:3000)

---

## 🔌 Core API Endpoints

### Storefront Read-Only API (Port 8080)
- `GET /api/pages/{slug}`: Fetch page by slug (e.g. `index`, `about-us`) from the ONLINE catalog.
- `GET /api/products/{code}`: Fetch product by code (e.g. `macbook-pro`).

### CMS Administrative Write API (Port 8081)
- `GET /api/cms/pages`: List all STAGED pages along with their real-time sync statuses.
- `POST /api/cms/pages`: Create a new STAGED page.
- `POST /api/cms/items/{type}/search`: Query items of a given type with dynamic search criteria (supports `CONTAINS`, `EQUALS`, `GREATER_THAN`, `LESS_THAN` operators).
- `POST /api/sync/{catalogId}`: Deep copy and publish all STAGED content to the ONLINE catalog.
- `POST /api/sync/item/{itemId}`: Granular, single-item synchronization from STAGED to ONLINE.
- `GET /api/cms/components/types`: Get list of registered, reflection-discovered component types.
- `POST /api/cms/media/upload`: Upload multipart files to the local shared volume.

---

## 🎨 Supported Component Types

1. **Product Details (`PRODUCT_DETAIL`)**: Displays the product title, image, price, and descriptive copy derived from the page context.
2. **Hero Banner (`BANNER`)**: Full-width image banner with title, subtitle, and CTA button.
3. **Paragraph Content (`PARAGRAPH`)**: Rich text blocks with HTML support.
4. **Product Carousel (`PRODUCT_CAROUSEL`)**: A responsive grid carousel displaying selected products by codes.
5. **Navigation Link (`NAVIGATION`)**: A simple text link with an optional icon.
6. **Quick Menu Tile (`QUICK_MENU`)**: A clickable grid tile card.
7. **Latest Articles (`LATEST_ARTICLE`)**: Displays a fixed number of the latest articles.
8. **Trending Articles (`TRENDING_ARTICLE`)**: Displays a curated list of trending articles using the generic `multiple_items:article` schema mapping.
9. **Latest Events (`LATEST_EVENT`)**: Displays a list of events using the generic `multiple_items:event` schema mapping.
10. **Top Event (`TOP_EVENT`)**: Displays a single featured event using the generic `item:event` schema mapping.

---

## 🔧 Verification & Testing

1. **Open the CMS Dashboard**: Navigate to [http://localhost:3001/cms](http://localhost:3001/cms).
2. **Create Content**: Add a new page, add a slot to that page, and drag a component into the slot.
3. **Verify Staging Isolation**: Check the Storefront UI at `http://localhost:3000/{slug}`. The page should return a 404 since it has not been published yet.
4. **Publish Content**: Return to the CMS Dashboard and click **Sync Staged to Online**.
5. **Verify Live Storefront**: Refresh the Storefront UI; your newly composed layout will now be globally visible.
