# Slot and Component Management Feature

## Overview

This document describes the slot and component management system that allows content editors to manage page content through the CMS Admin UI.

## Architecture

### Backend Components

#### Entities
- **Component.java** - Abstract base entity with JOINED inheritance strategy
  - Fields: `id`, `uid`, `name`, `type`, `sortOrder`, `slot`, `createdAt`, `updatedAt`
  - Subclasses: BannerComponent, ParagraphComponent, ProductCarouselComponent, NavigationComponent, QuickMenuComponent
  - `type` field stores component type as String in database
  - `getType()` abstract method returns ComponentType enum

- **Slot.java** - Content slot entity
  - Fields: `id`, `code`, `name`, `page`, `components` (OneToMany)

#### DTOs
- **CreateSlotRequest** - Request DTO for creating slots
  - Fields: `code` (required), `name` (required), `pageId` (required)
  
- **UpdateSlotRequest** - Request DTO for updating slots
  - Fields: `code` (required), `name` (required)
  
- **SlotResponse** - Response DTO with slot and component details
  - Fields: `id`, `code`, `name`, `pageId`, `components` (List<ComponentDTO>)
  
- **CreateComponentRequest** - Polymorphic request DTO using Jackson @JsonTypeInfo/@JsonSubTypes
  - Base fields: `uid`, `name`, `type`, `sortOrder`, `slotId`
  - Subclasses: CreateBannerComponentRequest, CreateParagraphComponentRequest, CreateProductCarouselComponentRequest, CreateNavigationComponentRequest, CreateQuickMenuComponentRequest
  
- **ReorderComponentRequest** - Request DTO for component reordering
  - Fields: `sortOrder` (required)

#### Controllers

##### SlotManagementController
REST endpoints for slot CRUD operations:
- **GET** `/api/cms/slots/page/{pageId}` - Get all slots for a page
- **GET** `/api/cms/slots/{id}` - Get slot with components by ID
- **POST** `/api/cms/slots` - Create new slot
- **PUT** `/api/cms/slots/{id}` - Update slot
- **DELETE** `/api/cms/slots/{id}` - Delete slot

All mutation operations include `@CacheEvict(value = {"page", "slot"}, allEntries = true)` to invalidate caches.

##### ComponentManagementController
REST endpoints for component CRUD with polymorphic handling:
- **POST** `/api/cms/components` - Create new component (polymorphic)
- **PUT** `/api/cms/components/{id}` - Update component
- **PUT** `/api/cms/components/{id}/reorder` - Reorder component within slot
- **DELETE** `/api/cms/components/{id}` - Delete component

The controller uses Jackson ObjectMapper for polymorphic deserialization and manual type-specific field handling.

#### Repositories
- **SlotRepository** - JPA repository with custom query `findByIdWithComponents` using @EntityGraph for eager loading
- **ComponentRepository** - JPA repository for component entities

### Frontend Components

#### API Client
**frontend/src/lib/cms-api-client.ts** - Singleton API client for CMS backend (port 8081)
- Slot methods: `getSlotsByPage`, `getSlot`, `createSlot`, `updateSlot`, `deleteSlot`
- Component methods: `createComponent`, `updateComponent`, `reorderComponent`, `deleteComponent`

#### UI Pages

##### Page Management UI
**frontend/src/app/cms/pages/[id]/manage/page.tsx**
- Comprehensive page content management interface
- Features:
  - Slot creation and management
  - Component CRUD operations
  - Drag-and-drop reordering (visual concept)
  - Component type selection (Banner, Paragraph, Product Carousel, Navigation, Quick Menu)
  - Type-specific form fields
  - Real-time preview
  - Delete confirmations
  - Error handling

Components:
- **PageManagementPage** - Main page component
- **SlotFormModal** - Modal for creating/editing slots
- **ComponentFormModal** - Modal for creating/editing components with polymorphic form fields
- **ComponentPreview** - Visual preview of component content

##### Pages List
**frontend/src/app/cms/pages/page.tsx**
- Updated to include "Manage Content" link for each page
- Three actions per page: View, Manage Content, Edit Page

## Data Model

### Component Types
The system supports five component types:

1. **BANNER** - Hero banner with image and call-to-action
   - Fields: `imageUrl`, `altText`, `title`, `subtitle`, `ctaText`, `ctaUrl`

2. **PARAGRAPH** - Rich text content
   - Fields: `title`, `content`

3. **PRODUCT_CAROUSEL** - Displays products
   - Fields: `title`, `productCodes` (array)

4. **NAVIGATION** - Single navigation link
   - Fields: `displayText`, `url`, `icon`
   - Note: Only supports single link per component

5. **QUICK_MENU** - Single quick menu tile
   - Fields: `title`, `imageUrl`, `url`
   - Note: Only supports single tile per component

### Database Schema

#### components table (base table)
```sql
CREATE TABLE components (
    id BIGSERIAL PRIMARY KEY,
    uid VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL,
    slot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_components_slot FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE
);
```

#### Subtype tables
Each component subtype has its own table with foreign key to `components.id`:
- `banner_components`
- `paragraph_components`
- `product_carousel_components`
- `navigation_components`
- `quick_menu_components`

## API Examples

### Create Slot
```bash
curl -X POST http://localhost:8081/api/cms/slots \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "hero-section",
    "name": "Hero Section",
    "pageId": 1
  }'
```

### Create Banner Component
```bash
curl -X POST http://localhost:8081/api/cms/components \
  -H 'Content-Type: application/json' \
  -d '{
    "uid": "hero-banner",
    "name": "Hero Banner",
    "type": "BANNER",
    "sortOrder": 1,
    "slotId": 1,
    "imageUrl": "https://example.com/banner.jpg",
    "altText": "Welcome Banner",
    "title": "Welcome to Our Store",
    "subtitle": "Best deals on electronics",
    "ctaText": "Shop Now",
    "ctaUrl": "/products"
  }'
```

### Update Component
```bash
curl -X PUT http://localhost:8081/api/cms/components/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "uid": "hero-banner",
    "name": "Updated Hero Banner",
    "type": "BANNER",
    "sortOrder": 1,
    "slotId": 1,
    "imageUrl": "https://example.com/new-banner.jpg",
    "altText": "Updated Banner",
    "title": "New Season Sale"
  }'
```

### Reorder Component
```bash
curl -X PUT http://localhost:8081/api/cms/components/1/reorder \
  -H 'Content-Type: application/json' \
  -d '{"sortOrder": 5}'
```

### Delete Component
```bash
curl -X DELETE http://localhost:8081/api/cms/components/1
```

### Get Slots for Page
```bash
curl http://localhost:8081/api/cms/slots/page/1
```

## Cache Strategy

All mutation operations (POST, PUT, DELETE) include cache eviction:
```java
@CacheEvict(value = {"page", "slot"}, allEntries = true)
```

This ensures the storefront backend's cached data is invalidated when content changes.

## Implementation Notes

### Entity Type Handling
The Component entity uses a hybrid approach:
- **Database**: Stores `type` as VARCHAR(50) in components table
- **Java**: Abstract `getType()` method returns ComponentType enum
- **@PrePersist**: Automatically sets `type` field from `getType().name()` before insert
- **Controller**: Explicitly sets `type` field from request during creation

### Polymorphic Component Handling
The ComponentManagementController handles polymorphism through:
1. Jackson @JsonTypeInfo/@JsonSubTypes for request deserialization
2. Manual type-based factory method pattern for entity creation
3. Type-specific update methods for each component subclass

### NavigationComponent and QuickMenuComponent Limitations
Current entity structure supports only single link/tile per component:
- NavigationComponent: One link (displayText, url, icon)
- QuickMenuComponent: One tile (title, imageUrl, url)

For multiple links/tiles, create multiple components with different sortOrder values.

## Testing

Verified operations:
- ✅ Slot creation, update, deletion
- ✅ Component creation with all types
- ✅ Component update (fields and validation)
- ✅ Component reordering (sortOrder change)
- ✅ Component deletion
- ✅ Slot retrieval with components
- ✅ Cache eviction on mutations

## Access

- **CMS Admin UI**: http://localhost:3000/cms/pages → Click "Manage Content" for any page
- **API Base URL**: http://localhost:8081/api/cms
- **Storefront API**: http://localhost:8080/api/pages (read-only, cached)

## Known Issues and Limitations

1. **NavigationComponent and QuickMenuComponent** only support single link/tile per component
2. **Frontend drag-and-drop** is visual concept only - reordering requires manual sortOrder input
3. **Component type cannot be changed** after creation (by design)
4. **No validation** for duplicate component UIDs within same slot
5. **No soft delete** - components are permanently deleted

## Future Enhancements

1. Add support for multiple links in NavigationComponent (JSON array or @OneToMany relationship)
2. Add support for multiple tiles in QuickMenuComponent
3. Implement actual drag-and-drop reordering with automatic sortOrder calculation
4. Add component duplication feature
5. Add component preview in modal before save
6. Add version history for components
7. Add component templates/presets
8. Add bulk operations (delete multiple, reorder multiple)
9. Add component search and filtering
10. Add validation for unique UIDs within slot
