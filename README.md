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
- **Dynamic Schema-Driven Form Generation**: The CMS Admin panel fetches component schemas from the backend via reflection (`@CmsComponent`) and dynamically renders input fields (strings, rich textareas, checkboxes, searchable product selectors, and drag-and-drop image uploaders). It features a robust item lookup dialog with real-time state deduplication across catalog versions and dynamic operators (`CONTAINS`, `EQUALS`, `MORE_THAN`, `LESS_THAN`) constrained by field type.
- **Next.js Proxy Media Uploads**: Simplifies media management by utilizing Next.js API `rewrites`. Images uploaded via the CMS are proxied securely to the backend API and served seamlessly through native relative paths (`/uploads/*`), eliminating CORS issues and broken image previews without configuring an external CDN.
- **Maintainable & Customizable Product Details**: Product detail pages (`/products/[code]`) are mapped to a CMS page layout (using `/products/detail` as the template). This allows editors to place any components (banners, carousels, text blocks) around the product info, and the storefront binds the loaded product context down to child components at runtime.
- **Separate Read & Write Services**: 
  - **Storefront Backend (8080)**: Fast read endpoints with aggressive Redis caching, scoped strictly to the `ONLINE` catalog.
  - **CMS Backend (8081)**: Write operations, scoped to the `STAGED` catalog, with automatic Redis cache eviction and publishing controls.
- **Component Reordering & Linking**: Slots use `@OrderColumn(name = "sort_order")` in `slot_components` join table, enabling drag-and-drop component reordering while maintaining component reusability across multiple slots.
- **SyncKey-Based References**: Components reference domain entities via stable business keys (`product_codes`, `article_uids`, `event_uids`) rather than database IDs, ensuring sync operations work correctly across catalog versions.

---

## 📦 Tech Stack

### Backend
- **Java 25** with **Spring Boot 4.0.0-M1**
- **Maven 3.9+** multi-module project structure (`shared-entities`, `cms-backend`, `storefront-backend`)
- **Spring Data JPA** with **Hibernate 7**
- **Flyway** for database schema migrations with auto-baseline configuration
- **PostgreSQL 16** & **Redis 7**
- **Jackson** for polymorphic JSON serialization

### Frontend
- **Next.js 16.2.7** with App Router (Two independent services: `storefront-frontend` and `cms-frontend`)
- **React 19.2.4**
- **TypeScript 5**
- **Tailwind CSS 4**
- **React Server Components** for storefront pages
- Dynamic, polymorphic **ComponentRenderer** registry

### Infrastructure
- **Docker & Docker Compose** for full-stack orchestration
- **PostgreSQL 16 Alpine** for persistent storage
- **Redis 7 Alpine** for caching layer
- Shared volume mounting for media uploads

---

## ⚙️ Configuration

### Environment Variables

The application uses the following environment variables (see `.env.example`):

**Database Configuration:**
- `DATABASE_URL` - PostgreSQL JDBC connection string (default: `jdbc:postgresql://postgres:5432/headless_cms`)
- `DATABASE_USERNAME` - Database user (default: `cms_user`)
- `DATABASE_PASSWORD` - Database password (default: `cms_password`)

**Redis Configuration:**
- `REDIS_HOST` - Redis server host (default: `redis`)
- `REDIS_PORT` - Redis server port (default: `6379`)

**Backend Ports:**
- `SERVER_PORT` - Service port (8080 for storefront, 8081 for CMS)

**Logging:**
- `LOG_LEVEL` - Application log level (INFO, DEBUG, WARN, ERROR)

For local development outside Docker, copy `.env.example` to `.env.local` and adjust connection strings to use `localhost` instead of service names.

---

## 🚀 Quick Start & Running Locally

The entire stack is containerized and managed via Docker Compose.

### Prerequisites
- Docker & Docker Compose
- Java 25 & Maven 3.9+ (if you wish to build the backend locally outside of Docker)
- Node.js 20+ (optional, for local frontend development)

### Step 1: Build the Application
Build the backend Java binaries - the Dockerfiles will copy the built `.jar` files into containers.
```bash
mvn clean package -DskipTests
```

### Step 2: Start the Full Stack Environment
Use Docker Compose to build the Node.js frontend images and start all 6 services (PostgreSQL, Redis, CMS Backend, Storefront Backend, CMS Frontend, Storefront Frontend).

```bash
docker compose up -d --build
```
*Note: The frontend containers perform a multi-stage production build upon first run. This might take a few minutes.*

### Step 3: Seed Demo Data (Optional)
After all services are up and healthy, seed the database with sample products, articles, events, pages, slots, and components:

```bash
chmod +x seed.sh
./seed.sh
```

This creates:
- 2 Products (MacBook Pro 16, iPhone 16 Pro)
- 3 Articles (MacBook intro, iPhone photography, Mac accessories)
- 1 Event (Apple Tech Summit 2026)
- 1 Homepage with hero slot, banner component, trending articles, and product carousel

### Step 4: Access the Applications
- **CMS Admin UI**: [http://localhost:3001/cms](http://localhost:3001/cms)
- **Storefront UI**: [http://localhost:3000](http://localhost:3000)

### Step 5: Publish Content to Storefront
1. Open the CMS Admin UI
2. Navigate to **Pages** or **Products**
3. Click **Sync Staged to Online** button
4. Verify content is now visible on the Storefront UI

---

## 🔌 Core API Endpoints

### Storefront Read-Only API (Port 8080)
- `GET /api/pages/{slug}`: Fetch page by slug (e.g. `index`, `about-us`) from the ONLINE catalog.
- `GET /api/products/{code}`: Fetch product by code (e.g. `macbook-pro`).

### CMS Administrative Write API (Port 8081)
- `GET /api/cms/pages`, `POST /api/cms/pages`: Manage STAGED pages along with their real-time sync statuses.
- `GET /api/cms/products`, `POST /api/cms/products`: Manage STAGED products in the catalog.
- `GET /api/cms/articles`, `POST /api/cms/articles`: Manage STAGED articles.
- `GET /api/cms/events`, `POST /api/cms/events`: Manage STAGED events.
- `GET /api/cms/items/{type}/search-metadata`: Discover metadata and allowed operators for item search across domain entities (`product`, `article`, `event`, `page`, `slot`, `component`).
- `POST /api/cms/items/{type}/search`: Query items of a given type with dynamic search criteria (supports `CONTAINS`, `EQUALS`, `MORE_THAN`, `LESS_THAN` operators).
- `POST /api/sync/{catalogId}`: Deep copy and publish all STAGED content to the ONLINE catalog.
- `POST /api/sync/item/{entityType}/{itemId}`: Granular, single-item synchronization from STAGED to ONLINE.
- `GET /api/cms/components/types`: Get list of registered, reflection-discovered component types.
- `GET /api/cms/components/types/{type}/schema`: Get dynamic form schema definition for a specific component type.
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

---

## 📁 Project Structure

```
dummy-storefront-cms/
├── shared-entities/              # Shared JPA entities & DTOs
│   └── src/main/java/id/adiputera/demo/cms/
│       ├── entity/              # Page, Slot, Component, Product, Article, Event
│       ├── model/               # ItemModel, CatalogAwareModel base classes
│       └── dto/                 # Request/Response DTOs
├── cms-backend/                 # Admin write API (Port 8081)
│   └── src/main/resources/
│       └── db/migration/        # Flyway SQL migrations
├── storefront-backend/          # Read-only API (Port 8080)
├── cms-frontend/                # Next.js CMS Admin (Port 3001)
│   └── src/
│       ├── app/cms/            # Admin dashboard pages
│       └── components/cms/     # Reusable CMS UI components
├── storefront-frontend/         # Next.js Storefront (Port 3000)
│   └── src/
│       ├── app/                # Public pages & routes
│       └── components/         # ComponentRenderer, SlotRenderer
├── uploads/                     # Docker shared volume for media
├── docker-compose.yml           # Multi-service orchestration
├── pom.xml                      # Maven Root POM
└── seed.sh                      # Demo data seeding script
```

---

## 🔄 Development Workflow

### Local Backend Development
To run backend services outside Docker:

```bash
# Start PostgreSQL & Redis
docker compose up -d postgres redis

# Run CMS Backend
cd cms-backend
mvn spring-boot:run

# Run Storefront Backend (in another terminal)
cd storefront-backend
mvn spring-boot:run
```

### Local Frontend Development
To run Next.js apps in development mode:

```bash
# CMS Frontend
cd cms-frontend
npm install
npm run dev

# Storefront Frontend
cd storefront-frontend
npm install
npm run dev
```

### Database Migrations
Flyway migrations are located in `cms-backend/src/main/resources/db/migration/`. They run automatically on CMS backend startup.

To manually reset the database:
```bash
docker compose down -v
docker compose up -d postgres
docker compose up cms-backend
```

---

## 🛠️ Troubleshooting

### Containers Won't Start
```bash
# Check container logs
docker compose logs cms-backend
docker compose logs storefront-backend

# Verify all containers are healthy
docker compose ps
```

### Database Connection Issues
Ensure PostgreSQL is fully initialized before backends start:
```bash
docker compose up -d postgres
# Wait 10 seconds
docker compose up -d cms-backend storefront-backend
```

### Port Conflicts
If ports 3000, 3001, 8080, 8081, 5432, or 6379 are already in use, modify `docker-compose.yml` port mappings.

### Frontend Build Failures
```bash
# Rebuild frontend containers
docker compose up -d --build cms-frontend storefront-frontend

# Check Node.js build logs
docker compose logs cms-frontend
```

### Cache Issues
Clear Redis cache and rebuild:
```bash
docker compose exec redis redis-cli FLUSHALL
docker compose restart storefront-backend
```

### Sync Not Working
Verify the CMS backend can reach Redis:
```bash
docker compose exec cms-backend curl http://redis:6379
```

---

## 🔑 Key Features Explained

### Catalog-Aware Architecture
All content entities inherit from `CatalogAwareModel` which provides:
- `catalogId`: References either STAGED (ID: 1) or ONLINE (ID: 2)
- Automatic isolation: CMS writes to STAGED, Storefront reads from ONLINE
- Bi-directional sync tracking with `syncVersion` fields

### Sync Status Tracking
Each entity tracks three states:
- **SYNCED**: Content matches between STAGED and ONLINE
- **OUT_OF_SYNC**: Content exists in both but differs
- **NOT_SYNCED**: Content only exists in STAGED

### Dynamic Schema Discovery
Component schemas are generated at runtime via Java reflection:
- `@CmsComponent` annotation marks component types
- Backend introspects field types and generates JSON schemas
- Frontend dynamically renders form inputs based on schemas
- Supports: text, textarea, number, boolean, image upload, item search

### Polymorphic Search Engine
Unified search API across all domain entities:
- Single endpoint: `POST /api/cms/items/{type}/search`
- Supports: product, article, event, page, slot, component
- Dynamic operators based on field type (CONTAINS, EQUALS, MORE_THAN, LESS_THAN)
- Real-time filtering with catalog version awareness

---

## 📚 Additional Documentation

- [QUICKSTART.md](QUICKSTART.md) - Detailed setup guide with troubleshooting
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Complete REST API reference
- [SLOT_COMPONENT_MANAGEMENT.md](SLOT_COMPONENT_MANAGEMENT.md) - Slot & component architecture
- [VERIFICATION_REPORT.md](VERIFICATION_REPORT.md) - Recent schema migrations & fixes

---

## 🤝 Contributing

This is a demonstration project. For questions or improvements:
1. Review existing documentation
2. Check [VERIFICATION_REPORT.md](VERIFICATION_REPORT.md) for recent changes
3. Test changes with `mvn clean package && docker compose up --build`

---

## 📄 License

This project is for educational and demonstration purposes.
