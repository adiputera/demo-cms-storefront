# Quick Start Guide

## Phase 1: Database Foundation - Complete! ✅

Successfully set up:
- ✅ Maven multi-module project structure
- ✅ Shared entities module with JPA entities and DTOs
- ✅ Storefront backend (read-only with Redis caching)
- ✅ CMS backend (write operations with Flyway migrations)
- ✅ PostgreSQL database schema
- ✅ Docker Compose for local development

## Project Structure

```
dummy-storefront-cms/
├── shared-entities/              # Shared JPA entities and DTOs
│   └── src/main/java/com/demo/cms/
│       ├── entity/               # JPA entities (Page, Slot, Component, Product)
│       └── dto/                  # Data transfer objects
├── storefront-backend/           # Read-only content delivery (Port 8080)
│   └── src/main/java/com/demo/cms/storefront/
├── cms-backend/                  # Content management (Port 8081)
│   ├── src/main/java/com/demo/cms/admin/
│   └── src/main/resources/db/migration/  # Flyway SQL scripts
├── docker-compose.yml
├── pom.xml                       # Parent POM
└── README.md
```

## Next Steps

### Option 1: Start with Docker (Recommended for testing infrastructure)

```bash
# Start PostgreSQL and Redis only
docker-compose up -d postgres redis

# Wait for services to be healthy
docker-compose ps

# Test database connection
psql -h localhost -U cms_user -d headless_cms
# Password: cms_password
```

### Option 2: Build and Run Locally

```bash
# 1. Build all modules
mvn clean install

# 2. Start CMS backend (runs Flyway migrations)
cd cms-backend
mvn spring-boot:run

# 3. In another terminal, start Storefront backend
cd storefront-backend
mvn spring-boot:run

# 4. Test the endpoints
curl http://localhost:8080/actuator/health  # Storefront
curl http://localhost:8081/actuator/health  # CMS

# 5. Test page API (after migrations run)
curl http://localhost:8080/api/pages/
```

## What's Working Now

### Database Schema
- ✅ `pages` table with SEO metadata
- ✅ `page_breadcrumbs` table for navigation
- ✅ `slots` table for content areas
- ✅ `components` table (base) with 5 subtype tables
- ✅ `products` table for demo catalog
- ✅ Indexes and constraints
- ✅ Demo seed data (homepage with components + 6 sample products)

### Backend Structure
- ✅ Spring Boot 4 with Java 25
- ✅ JPA entities with table-per-subclass inheritance
- ✅ DTOs with JSON polymorphism
- ✅ Redis integration for caching
- ✅ Flyway migrations
- ✅ Actuator health checks

## Phase 2 Tasks (Next)

To complete the storefront backend:

1. **Create repositories** in `storefront-backend/src/main/java/com/demo/cms/storefront/repository/`
   - PageRepository
   - SlotRepository
   - ProductRepository

2. **Create services** in `storefront-backend/src/main/java/com/demo/cms/storefront/service/`
   - PageService (with @Cacheable)
   - SlotService (with @Cacheable)
   - ProductService

3. **Create mappers** in `storefront-backend/src/main/java/com/demo/cms/storefront/mapper/`
   - Entity to DTO converters

4. **Create controllers** in `storefront-backend/src/main/java/com/demo/cms/storefront/controller/`
   - PageController (GET /api/pages/{slug})
   - SlotController (POST /api/slots/details)
   - ProductController (GET /api/products)

5. **Configure Redis caching**
   - CacheConfig class
   - Cache key strategies

## Verification Checklist

- [ ] Maven build completes successfully (`mvn clean install`)
- [ ] PostgreSQL starts and accepts connections
- [ ] Redis starts and accepts connections
- [ ] CMS backend starts and runs Flyway migrations
- [ ] Storefront backend starts and connects to database
- [ ] Health check endpoints respond
- [ ] Database contains demo data (6 products, homepage with components)

## Troubleshooting

### Port Conflicts
If ports 8080, 8081, 5432, or 6379 are in use:
```bash
# Change ports in .env.local or docker-compose.yml
export SERVER_PORT=9080
export DATABASE_PORT=5433
```

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# View logs
docker-compose logs postgres
```

### Build Issues
```bash
# Clean Maven cache
mvn clean

# Rebuild with dependency resolution
mvn clean install -U
```

## Demo Data Available

After running migrations, you'll have:

**Homepage (slug = "/")**
- Hero slot with banner component
- Content slot with paragraph + product carousel + paragraph
- Footer slot with 3 navigation links

**Products (6 items)**
- MacBook Pro, iPhone 15 Pro, AirPods Pro
- iPad Air, Apple Watch Series 9, Magic Keyboard

**About Us page (slug = "/about-us")**
- Empty page with breadcrumb back to home
