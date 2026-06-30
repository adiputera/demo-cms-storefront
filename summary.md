# CMS Changes Summary: Generic Search, Articles, and Events Integration

This document outlines the major architecture enhancements and features added to the Headless CMS.

## 1. What We Enabled (Capabilities)
- **Generic / Domain-Agnostic Entity Search**: Enabled the frontend to search for any backend entity type (e.g., `product`, `article`, `event`) dynamically without writing domain-specific UI components or search APIs for each new entity.
- **Dynamic Search Metadata**: Allowed the backend to describe which fields of an entity are searchable (via annotation-based metadata). The CMS UI reads this schema to build query fields dynamically.
- **Article Catalog & CRUD**: Fully enabled creation, reading, updating, and deleting of articles from the CMS Admin Panel.
- **Event Catalog & Component Selection**: Added support for an `Event` entity and allowed slots on a page to use `LatestEventComponent` where events can be dynamically selected via the generic multi-item selector.
- **Single Entity Generic Relation**: Expanded the schema engine to support `item:<entity>` allowing a component to relate to exactly *one* item (e.g., `TopEventComponent`), automatically rendering UI radio buttons instead of checkboxes.

---

## 2. What Are the Changes

### Backend (`shared-entities` & `cms-backend`)
- **New Entities**:
  - `Article` (in `articles` table): Title, Body, Slug, and a unique business key `uid`.
  - `Event` (in `events` table): Title, Description, Location, Slug, and `uid`.
- **New Annotations**:
  - `@CmsSearchable` and `@CmsSearchables`: Declares fields in Java entities that are exposed as searchable to the frontend.
- **Generic Search & Metadata Endpoints**:
  - `GET /api/cms/items/{type}/search-metadata`: Reflects the searchable fields declared via `@CmsSearchable`.
  - `POST /api/cms/items/{type}/search`: Generic JPA-criteria-based search that filters the requested entity class dynamically by title, location, code, etc.
- **New Components**:
  - `TrendingArticleComponent`: References a dynamic list of articles by ID (`multiple_items:article`).
  - `LatestEventComponent`: References selected events via the `event_ids` string (`multiple_items:event`).
  - `TopEventComponent`: References a single selected event via `event_id` (`item:event`).
- **Polymorphic DTOs & Mapping**:
  - Configured `CreateComponentRequest` sub-types and `EntityMapper` to map `TrendingArticleComponent` and `LatestEventComponent` from request payloads to JPA models.

### Frontend (`cms-frontend`)
- **Dynamic Search Components**:
  - Created `ItemSearchField.tsx` handling both `multiple_items:itemType` and `item:itemType` syntax (e.g., `multiple_items:article`, `item:event`).
  - The UI parses the field type suffix, queries `/api/cms/items/{type}/search-metadata` to render appropriate inputs, and calls `POST /api/cms/items/{type}/search` to display matching results. It smartly toggles between checkboxes (multi) and radio buttons (single) based on the schema mapping.
- **Article Admin Section**:
  - Added new routes `/cms/articles`, `/cms/articles/new`, and `/cms/articles/[id]/edit` for full CRUD capabilities.
  - Exposed sidebar menu option for "Articles".

---

## 3. Why the Changes (Rationale)
- **Extensibility**: By making search domain-agnostic, adding a new model to the CMS in the future (e.g., `JobPosting`, `StoreLocation`) will no longer require writing unique frontend search panels. Annotating the model in Java automatically exposes it to the UI.
- **Data Integrity & Consistency**: Replaced manual mapping hacks with formal JPA inheritance mappings, and ensured `uid` columns are present for cross-catalog synchronization matching.
