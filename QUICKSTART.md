# Quick Start Guide

Welcome to the **Headless CMS Demo Application**! This guide will help you set up, run, and explore the full-stack, Catalog-Aware Headless CMS locally.

---

## 🚀 Overview & Architecture

This application demonstrates **runtime-driven page composition** with dynamic slots, components, and customizable templates. Content changes are isolated in a **STAGED** catalog and can be synchronized to an **ONLINE** catalog via an automated deep-copy publishing engine.

### Core Services
- **Storefront Frontend (Port 3000)**: Next.js 16 App Router UI serving customer-facing pages from the `ONLINE` catalog using React Server Components.
- **CMS Admin Frontend (Port 3001)**: Next.js 16 Dashboard for content editors to manage pages, slots, components, products, articles, and events in the `STAGED` catalog.
- **Storefront Backend (Port 8080)**: Spring Boot 4 / Java 25 read-only API with aggressive Redis caching (`ONLINE` catalog).
- **CMS Backend (Port 8081)**: Spring Boot 4 / Java 25 write API with dynamic reflection-based schema discovery, Flyway migrations, and cache eviction (`STAGED` catalog).
- **PostgreSQL 16 (Port 5432)**: Relational database storing catalog versions, polymorphic domain items, and component hierarchies.
- **Redis 7 (Port 6379)**: High-performance caching layer for storefront responses.

---

## 📁 Project Structure

All Java backend code adheres to the base package convention: **`id.adiputera.demo.cms`**.

```
dummy-storefront-cms/
├── shared-entities/              # JPA entities (Page, Slot, Component, Product, Article, Event), item models, DTOs
├── storefront-backend/           # Read-only content delivery API & Redis caching (Port 8080)
├── cms-backend/                  # Admin write API, Flyway migrations, reflection schema engine (Port 8081)
├── storefront-frontend/          # Next.js customer storefront (Port 3000)
├── cms-frontend/                 # Next.js CMS Admin dashboard (Port 3001)
├── uploads/                      # Shared Docker volume for local media storage
├── docker-compose.yml            # Multi-container orchestration
├── pom.xml                       # Maven Root POM
└── seed.sh                       # Initialization and demo seeding script
```

---

## 🛠️ Prerequisites

- **Docker & Docker Compose** (Recommended for full-stack execution)
- **Java 25 & Maven 3.9+** (Required if building backend modules outside Docker)
- **Node.js 20+ & npm** (Required if running Next.js frontends locally outside Docker)

---

## 🏃 Method 1: Start with Docker Compose (Recommended)

The easiest way to run the entire stack (Database, Redis, Backends, and Frontends) is via Docker Compose.

### Step 1: Build Backend Artifacts
The Dockerfile expects built `.jar` files from Maven:
```bash
mvn clean package -DskipTests
```

### Step 2: Launch All Services
Start all 6 containers in detached mode:
```bash
docker compose up -d --build
```
*Note: Upon first run, the Next.js containers will execute a multi-stage production build which may take 2–3 minutes.*

### Step 3: Verify Container Health
Check that all services are running and healthy:
```bash
docker compose ps
```

### Step 4: Access the Applications
- **CMS Admin UI**: [http://localhost:3001/cms](http://localhost:3001/cms)
- **Storefront UI**: [http://localhost:3000](http://localhost:3000)
- **CMS API Health**: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)
- **Storefront API Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 💻 Method 2: Running Locally Without Docker

If you prefer developing locally on your host machine:

### 1. Start Infrastructure (PostgreSQL & Redis)
```bash
docker compose up -d postgres redis
```

### 2. Build and Start Backend Services
In Terminal 1 (CMS Backend - Runs Flyway migrations V1 to V6):
```bash
mvn clean install -DskipTests
cd cms-backend
mvn spring-boot:run
```

In Terminal 2 (Storefront Backend):
```bash
cd storefront-backend
mvn spring-boot:run
```

### 3. Start Frontend Applications
In Terminal 3 (CMS Admin Dashboard):
```bash
cd cms-frontend
npm install
npm run dev -- -p 3001
```

In Terminal 4 (Customer Storefront):
```bash
cd storefront-frontend
npm install
npm run dev -- -p 3000
```

---

## ✨ What's Working Now

The project is complete with features across all layers:

### Database & Schema Migrations (Flyway V1–V6)
- ✅ `catalog_versions`, `pages`, `page_breadcrumbs`, and `slots` tables with full `catalog_id` isolation.
- ✅ Base `components` table with JOINED inheritance across 10 component subtype tables (`banner_components`, `paragraph_components`, `product_carousel_components`, `navigation_components`, `quick_menu_components`, `product_detail_components`, `latest_article_components`, `trending_article_components`, `latest_event_components`, `top_event_components`).
- ✅ Polymorphic item tables (`products`, `articles`, `events`) extending `ItemModel`.
- ✅ Automated demo seeding with sample products, articles, events, and a pre-configured homepage layout.

### Backend Features
- ✅ **Multi-Version Catalog System**: Complete isolation between `STAGED` and `ONLINE` environments.
- ✅ **Dynamic Schema Discovery**: Reflection-based scan (`@CmsComponent`, `@CmsField`) exposing component schemas at `/api/cms/components/types/{type}/schema`.
- ✅ **Polymorphic Item Search API**: `/api/cms/items/{type}/search` powering dynamic component item selection with flexible operator filters (`CONTAINS`, `EQUALS`, `MORE_THAN`, `LESS_THAN`).
- ✅ **Granular Sync Status Tracking**: Real-time computation of `SYNCED`, `OUT_OF_SYNC`, and `NOT_SYNCED` statuses per item or catalog.
- ✅ **Automated Cache Eviction**: Mutations on `STAGED` content immediately evict relevant storefront Redis cache entries upon catalog publishing.

### Frontend Features
- ✅ **Dynamic CMS Admin Dashboard**: Schema-driven form modals, real-time sync badges, image upload proxying, and item lookup dialogs with catalog state deduplication.
- ✅ **Storefront Page Composition**: Runtime component resolution and rendering using Next.js Server Components.

---

## 🧪 Demo Walkthrough: Test the CMS Workflow

1. **Explore the Staged Catalog**:
   Open [http://localhost:3001/cms](http://localhost:3001/cms). You will see the list of pages, products, articles, and events.
2. **Manage Content & Add a Component**:
   Click **Manage Content** on the Homepage (`/`). Click **Add Component** inside a slot (e.g., `content`). Select **Trending Articles** (`TRENDING_ARTICLE`) or **Product Carousel** (`PRODUCT_CAROUSEL`). Use the interactive item search picker to select articles or products dynamically, then save.
3. **Verify Staging Isolation**:
   Visit [http://localhost:3000](http://localhost:3000). Notice that your newly added component does **not** appear yet! The storefront reads strictly from the `ONLINE` catalog.
4. **Publish to Online**:
   Return to the CMS Dashboard [http://localhost:3001/cms](http://localhost:3001/cms) and click **Sync Staged to Online**.
5. **Verify Live Storefront**:
   Refresh [http://localhost:3000](http://localhost:3000). Your new component is now live and cached in Redis!

---

## ❓ Troubleshooting

### Port Conflicts (8080, 8081, 3000, 3001, 5432, 6379)
If any port is already in use on your system, modify `docker-compose.yml` port mappings or override environment variables in `.env`:
```bash
export SERVER_PORT=9081
export DATABASE_PORT=5433
```

### Database Connection or Migration Errors
Ensure PostgreSQL container is healthy and reset the volume if necessary:
```bash
docker compose down -v
docker compose up -d postgres redis
```

### Rebuilding Containers After Code Changes
When modifying Java backend code or frontend components:
```bash
mvn clean package -DskipTests
docker compose up -d --build
```
