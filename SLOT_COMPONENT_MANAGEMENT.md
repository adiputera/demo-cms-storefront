# Slot and Component Management Feature

## Overview

This document describes the slot and component management system that allows content editors to compose, link, reorder, and manage page content through the CMS Admin UI. The architecture leverages a **Catalog-Aware** multi-version schema (`STAGED` vs `ONLINE`), dynamic reflection-based schema discovery, and a polymorphic domain item search engine.

---

## Architecture

### Backend Components

All backend Java classes reside under the base package: **`id.adiputera.demo.cms`**.

#### Entities

- **`Component.java`** - Abstract base entity with `JOINED` inheritance strategy extending `CatalogAwareModel`.
  - Core Fields: `id`, `uid`, `name`, `type` (String, length 50), `catalogId`, `createdAt`, `updatedAt`.
  - Subclasses (10 total):
    - `BannerComponent` (`BANNER`)
    - `ParagraphComponent` (`PARAGRAPH`)
    - `ProductCarouselComponent` (`PRODUCT_CAROUSEL`)
    - `NavigationComponent` (`NAVIGATION`)
    - `QuickMenuComponent` (`QUICK_MENU`)
    - `ProductDetailComponent` (`PRODUCT_DETAIL`)
    - `LatestArticleComponent` (`LATEST_ARTICLE`)
    - `TrendingArticleComponent` (`TRENDING_ARTICLE`)
    - `LatestEventComponent` (`LATEST_EVENT`)
    - `TopEventComponent` (`TOP_EVENT`)
  - `getType()` abstract method returns the corresponding `ComponentType` enum.
  - Components act as reusable, catalog-aware content blocks that can be linked to one or more slots.

- **`Slot.java`** - Content slot entity extending `CatalogAwareModel`.
  - Fields: `id`, `code`, `name`, `page` (`@ManyToOne`), `catalogId`, `components` (`@ManyToMany`).
  - **Component Ordering**: Uses an `@OrderColumn(name = "sort_order")` on a join table (`slot_components`). This allows components to maintain a specific sequence within a slot while remaining independent entities.

#### DTOs

- **`CreateSlotRequest`**: Request DTO for creating slots (`code`, `name`, `pageId`).
- **`UpdateSlotRequest`**: Request DTO for updating slots (`code`, `name`).
- **`SlotResponse`**: Response DTO containing slot details and ordered component list (`id`, `code`, `name`, `pageId`, `components`).
- **`CreateComponentRequest`**: Polymorphic request DTO mapped via Jackson `@JsonTypeInfo` and `@JsonSubTypes`.
  - Base fields: `uid`, `name`, `type`, `sortOrder` (optional index for slot placement), `slotId` (optional target slot ID).
  - Subclasses:
    - `CreateBannerComponentRequest`
    - `CreateParagraphComponentRequest`
    - `CreateProductCarouselComponentRequest`
    - `CreateNavigationComponentRequest`
    - `CreateQuickMenuComponentRequest`
    - `CreateProductDetailComponentRequest`
    - `CreateLatestArticleComponentRequest`
    - `CreateTrendingArticleComponentRequest`
    - `CreateLatestEventComponentRequest`
    - `CreateTopEventComponentRequest`
- **`ReorderComponentRequest`**: Request DTO for changing a component's position within a slot (`sortOrder`).

#### Controllers

##### `SlotManagementController` (`/api/cms/slots`)
REST endpoints for slot operations in the `STAGED` catalog:
- **GET** `/api/cms/slots/page/{pageId}`: Get all slots and components for a page.
- **GET** `/api/cms/slots/{id}`: Get slot by ID.
- **POST** `/api/cms/slots`: Create new slot.
- **PUT** `/api/cms/slots/{id}`: Update slot properties.
- **DELETE** `/api/cms/slots/{id}`: Delete slot.
All mutation endpoints use `@CacheEvict(value = {"page", "slot"}, allEntries = true)` to invalidate caches.

##### `ComponentManagementController` (`/api/cms/components`)
REST endpoints for polymorphic component lifecycle, linking, and schema discovery:
- **POST** `/api/cms/components`: Create component. If `slotId` is provided, automatically inserts the component into `slot.getComponents()` at `sortOrder`.
- **POST** `/api/cms/components/slots/{slotId}/components/{componentId}`: Link an existing component to a slot at an optional index (`{"sortOrder": index}`).
- **PUT** `/api/cms/components/{id}`: Update component properties (prevents changing component `type`).
- **PUT** `/api/cms/components/slots/{slotId}/components/{id}/reorder`: Move a component to a new index within the slot's ordered list.
- **DELETE** `/api/cms/components/slots/{slotId}/components/{id}`: Unlink and remove a component from a slot without deleting the component entity.
- **DELETE** `/api/cms/components/{id}`: Permanently delete a component entity.
- **GET** `/api/cms/components/types`: Get all 10 registered component type enum strings.
- **GET** `/api/cms/components/types/{type}/schema`: Get reflection-generated form schemas for dynamic UI rendering.

#### Repositories
- **`SlotRepository`**: JPA repository featuring `@EntityGraph` eager fetching (`findByIdWithComponents`).
- **`ComponentRepository`**: JPA repository handling polymorphic component entity persistence and catalog queries.

---

### Frontend Components

#### API Client (`cms-frontend/src/lib/cms-api-client.ts`)
Singleton API client communicating with the CMS backend (Port 8081):
- **Slot Methods**: `getSlotsByPage`, `getSlot`, `createSlot`, `updateSlot`, `deleteSlot`.
- **Component Methods**: `createComponent`, `updateComponent`, `reorderComponent`, `deleteComponent`, `removeComponentFromSlot`, `linkComponentToSlot`.
- **Schema & Search Methods**: `getComponentTypes`, `getComponentSchema`, `searchItems`, `getItemSearchMetadata`.

#### UI Management Pages (`cms-frontend/src/app/cms/pages/[id]/manage/page.tsx`)
Comprehensive interactive interface for page content editing:
- **Dynamic Schema-Driven Forms (`ComponentFormModal`)**: Fetches field definitions from `/api/cms/components/types/{type}/schema` and dynamically renders inputs (text strings, rich textareas, boolean switches, numbers, image uploaders, and item search selectors).
- **Interactive Item Search Picker**: Integrates `/api/cms/items/{type}/search` to allow content editors to search and pick products, articles, or events when configuring components like `PRODUCT_CAROUSEL`, `TRENDING_ARTICLE`, or `LATEST_EVENT`.
- **Real-Time Sync Status Badges**: Displays whether each slot and component is `SYNCED`, `OUT_OF_SYNC`, or `NOT_SYNCED` relative to the `ONLINE` catalog.
- **Component Linking & Reordering**: Supports adding new components, linking existing shared components, reordering items within a slot, and unlinking or permanently deleting components.

---

## Data Model

### Component Types & Fields

The system supports 10 distinct component types:

1. **`BANNER`** (Hero Banner)
   - Fields: `imageUrl`, `altText`, `title`, `subtitle`, `ctaText`, `ctaUrl`
2. **`PARAGRAPH`** (Rich Text Content)
   - Fields: `title`, `content` (HTML supported)
3. **`PRODUCT_CAROUSEL`** (Product Grid Carousel)
   - Fields: `title`, `productCodes` (Comma-separated list of product codes)
4. **`NAVIGATION`** (Single Navigation Link)
   - Fields: `displayText`, `url`, `icon`
5. **`QUICK_MENU`** (Clickable Tile Card)
   - Fields: `title`, `imageUrl`, `url`
6. **`PRODUCT_DETAIL`** (Product Template Layout Context)
   - Fields: `title`, `showPrice` (Boolean), `showDescription` (Boolean)
7. **`LATEST_ARTICLE`** (Recent Articles Feed)
   - Fields: `title`, `articleCount` (Integer)
8. **`TRENDING_ARTICLE`** (Curated Articles Feed)
   - Fields: `title`, `articleIds` (Comma-separated list of article IDs/codes)
9. **`LATEST_EVENT`** (Upcoming Events Feed)
   - Fields: `title`, `eventIds` (Comma-separated list of event IDs/codes)
10. **`TOP_EVENT`** (Featured Event Card)
    - Fields: `title`, `eventId` (Single event ID/code)

### Database Schema

#### Base Table (`components`)
```sql
CREATE TABLE components (
    id BIGSERIAL PRIMARY KEY,
    uid VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    catalog_id VARCHAR(50) NOT NULL DEFAULT 'STAGED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_components_uid_catalog UNIQUE (uid, catalog_id)
);
```

#### Slot Join Table (`slot_components`)
```sql
CREATE TABLE slot_components (
    slot_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_slot_components_slot FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_slot_components_component FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE
);
```

#### Subtype Tables
Each subclass has a 1-to-1 table joined on `id`:
- `banner_components`, `paragraph_components`, `product_carousel_components`, `navigation_components`, `quick_menu_components`, `product_detail_components`, `latest_article_components`, `trending_article_components`, `latest_event_components`, `top_event_components`.

---

## API Examples

### 1. Create Slot
```bash
curl -X POST http://localhost:8081/api/cms/slots \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "hero-section",
    "name": "Hero Section",
    "pageId": 1
  }'
```

### 2. Create Banner Component inside a Slot
```bash
curl -X POST http://localhost:8081/api/cms/components \
  -H 'Content-Type: application/json' \
  -d '{
    "uid": "hero-banner-1",
    "name": "Homepage Hero Banner",
    "type": "BANNER",
    "sortOrder": 0,
    "slotId": 1,
    "imageUrl": "https://example.com/banner.jpg",
    "altText": "Welcome Banner",
    "title": "Welcome to Our Store",
    "subtitle": "Best deals on electronics",
    "ctaText": "Shop Now",
    "ctaUrl": "/products"
  }'
```

### 3. Create Trending Articles Component
```bash
curl -X POST http://localhost:8081/api/cms/components \
  -H 'Content-Type: application/json' \
  -d '{
    "uid": "trending-articles-1",
    "name": "Trending Tech News",
    "type": "TRENDING_ARTICLE",
    "sortOrder": 1,
    "slotId": 1,
    "title": "Trending Tech News",
    "articleIds": "article-1,article-2,article-3"
  }'
```

### 4. Link an Existing Component to a Slot
```bash
curl -X POST http://localhost:8081/api/cms/components/slots/2/components/15 \
  -H 'Content-Type: application/json' \
  -d '{"sortOrder": 0}'
```

### 5. Reorder Component within a Slot
```bash
curl -X PUT http://localhost:8081/api/cms/components/slots/1/components/10/reorder \
  -H 'Content-Type: application/json' \
  -d '{"sortOrder": 2}'
```

### 6. Unlink Component from a Slot (Without Deleting Entity)
```bash
curl -X DELETE http://localhost:8081/api/cms/components/slots/1/components/10
```

### 7. Permanently Delete Component Entity
```bash
curl -X DELETE http://localhost:8081/api/cms/components/10
```

---

## Cache Strategy

All mutation operations on slots or components evict the storefront read caches:
```java
@CacheEvict(value = {"page", "slot"}, allEntries = true)
```
When content is published via `/api/sync/{catalogId}`, the synchronization engine deep-copies STAGED entities to ONLINE and purges affected storefront Redis keys.

---

## Known Issues and Limitations

1. **Navigation and Quick Menu Components**: Currently design-constrained to support a single link (`displayText`, `url`, `icon`) or single tile (`title`, `imageUrl`, `url`) per component instance. Multiple items should be created as separate components in the slot.
2. **Component Type Immutability**: By design, once a component is created with a specific `type` (`BANNER`, `PARAGRAPH`, etc.), its type cannot be modified via `PUT /api/cms/components/{id}`.
3. **Shared Component Deletion**: Permanently deleting a component entity (`DELETE /api/cms/components/{id}`) removes it from all slots referencing it across the database due to foreign key cascade rules. Use `DELETE /api/cms/components/slots/{slotId}/components/{id}` to unlink from a single slot.

---

## Future Enhancements

1. Add multi-link array support in `NavigationComponent` and `QuickMenuComponent`.
2. Support visual drag-and-drop reordering with automatic background position index calculations.
3. Add component duplication and template preset features.
4. Add version history and rollback capabilities for individual component entities.
