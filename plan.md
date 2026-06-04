# Headless CMS Sample Application Design

## Objective

Design and implement a sample Headless CMS application demonstrating a maintainable, scalable, and extensible page composition architecture.

The purpose of this project is to showcase:

* Spring Boot backend architecture
* Next.js frontend architecture
* Runtime-driven page composition
* Slot-based content management
* Dynamic component rendering
* SEO management
* Clean API design
* Type-safe frontend development

This project is intended as a learning and demonstration project.

Keep the architecture simple and maintainable.

Avoid enterprise CMS complexity.

---

# Technology Stack

## Backend

* Java 25
* Spring Boot 4
* Spring Data JPA
* PostgreSQL
* Flyway
* Maven
* Jakarta Validation
* REST API

### Architectural Style

```text
Controller
    ↓
Facade
    ↓
Service
    ↓
Repository
```

Use DTOs.

Never expose JPA entities directly.

---

## Frontend

* Next.js (latest stable version)
* TypeScript
* App Router
* Tailwind CSS
* React Server Components where appropriate

---

# Demo Project Constraints

This is a demo project.

Do NOT implement:

* Authentication
* Authorization
* Login
* Registration
* JWT
* OAuth
* RBAC
* Workflow Approval
* Content Synchronization
* Content Versioning
* Content Restrictions
* Personalization
* Multi-site
* Multi-language
* Scheduled Publishing
* A/B Testing

The goal is to demonstrate page composition architecture.

---

# Domain Overview

```text
Page
 ├── Breadcrumbs
 ├── Slot (hero)
 │     ├── Banner Component
 │     └── Product Carousel Component
 │
 ├── Slot (content)
 │     ├── Paragraph Component
 │     └── Paragraph Component
 │
 └── Slot (footer)
       └── Navigation Component
```

---

# Page Model

Represents a CMS-managed page.

## Fields

```java
id
slug
title
breadcrumbTitle

metaTitle
metaDescription
metaKeywords
canonicalUrl

robotsIndex
robotsFollow

ogTitle
ogDescription
ogImage

slots
breadcrumbs
```

## Examples

```text
/
about-us
contact-us
promo
```

## Requirements

* slug must be unique
* slug must be indexed
* title required
* breadcrumbTitle required
* robotsIndex default true
* robotsFollow default true

---

# Homepage

Homepage must be managed through CMS.

Example:

```text
slug = /
```

Storefront should resolve:

```text
https://example.com/
```

to the CMS homepage.

Design should explicitly explain handling of root ("/") pages in:

* PostgreSQL
* Spring Boot routing
* Next.js routing

---

# Breadcrumb Model

Breadcrumbs are manually managed.

Relationship:

```text
Page
 └── List<Page> breadcrumbs
```

Display:

* URL generated from page.slug
* Text generated from page.breadcrumbTitle

Example:

```text
Home > Products > Laptop
```

---

# Slot Model

A slot represents a fixed location on a page.

Examples:

```text
hero
content
footer
quick-menu
```

## Fields

```java
id
code
name
components
```

## Requirements

* One page can have multiple slots
* Slot code must be unique within a page
* No slot ordering field required

Slot placement is controlled by storefront layout.

Example:

```tsx
<SlotRenderer slotCode="hero" />
<SlotRenderer slotCode="content" />
<SlotRenderer slotCode="footer" />
```

---

# Component Model

A slot contains multiple components.

## Fields

```java
id
uid
name
type
sortOrder
```

## Requirements

* Components must be rendered according to sortOrder
* Components can be reordered
* Component architecture must support future extension

Example:

```text
Hero Slot
 ├─ Banner (sortOrder=1)
 ├─ Product Carousel (sortOrder=2)
 └─ Paragraph (sortOrder=3)
```

---

# Supported Component Types

## Paragraph Component

Fields:

```java
title
content
```

content contains rich text.

---

## Banner Component

Fields:

```java
imageUrl
altText

title
subtitle

ctaText
ctaUrl
```

---

## Product Carousel Component

Fields:

```java
title
productCodes
```

Stores product references only.

---

## Navigation Component

Fields:

```java
displayText
url
icon
```

---

## Quick Menu Component

Fields:

```java
title
imageUrl
url
```

Displayed as image tiles.

---

# Product API

This is NOT an e-commerce platform.

Provide a minimal product API only to support Product Carousel.

## Product Model

```java
id
code
name
imageUrl
price
```

Example:

```json
{
  "id": 1,
  "code": "macbook-pro",
  "name": "MacBook Pro",
  "imageUrl": "https://cdn.example.com/macbook.jpg",
  "price": 2499.99
}
```

## APIs

```http
GET /api/products
GET /api/products/{code}
```

Do NOT implement:

* Inventory
* Stock
* Categories
* Reviews
* Promotions
* Cart
* Checkout
* Orders

---

# Dynamic CMS Rendering

This is one of the most important requirements.

Storefront must NOT require redeployment when CMS content changes.

Example:

Initial state:

```text
Homepage
 └── Hero Slot
      └── Banner Component
```

Editor later adds:

```text
Homepage
 └── Hero Slot
      ├── Banner Component
      └── Product Carousel Component
```

Storefront must automatically render the new component without:

* code changes
* rebuild
* redeployment

---

# API Design

## Page API

### Get Page By Slug

```http
GET /api/pages/{slug}
```

Returns:

* page information
* SEO metadata
* breadcrumbs
* slot metadata

Must NOT return:

* component details
* component configuration

Example:

```json
{
  "id": 1,
  "slug": "/about-us",
  "title": "About Us",

  "metaTitle": "About Us - Demo Site",
  "metaDescription": "Learn more about our company",
  "metaKeywords": "about,company",

  "canonicalUrl": "https://example.com/about-us",

  "robotsIndex": true,
  "robotsFollow": true,

  "breadcrumbs": [
    {
      "slug": "/",
      "breadcrumbTitle": "Home"
    }
  ],

  "slots": [
    {
      "id": 101,
      "code": "hero",
      "name": "Hero"
    },
    {
      "id": 102,
      "code": "content",
      "name": "Content"
    }
  ]
}
```

---

## Slot API

### Batch Slot Details

```http
POST /api/slots/details
```

Request:

```json
{
  "slotIds": [101,102]
}
```

Response:

```json
{
  "slots": [
    {
      "id": 101,
      "code": "hero",
      "components": [
        {
          "id": 1,
          "type": "BANNER"
        },
        {
          "id": 2,
          "type": "PRODUCT_CAROUSEL"
        }
      ]
    }
  ]
}
```

Purpose:

Avoid N+1 API calls.

---

# Database Design

Generate:

* ERD
* PostgreSQL schema
* Flyway migrations
* Constraints
* Index recommendations

Expected tables:

```text
pages
page_breadcrumbs

slots

components

paragraph_components
banner_components
product_carousel_components
navigation_components
quick_menu_components
```

Provide tradeoff analysis against:

```text
components
  └── json_config
```

approach.

---

# Validation Rules

Examples:

```text
slug unique
slug required

title required

slot code required

component type required

sortOrder required
```

---

# Performance Requirements

Assume:

```text
10,000 concurrent users
mostly read traffic
rare content updates
```

Provide:

* indexing strategy
* query optimization
* caching strategy
* scalability recommendations

---

# Cache Strategy

Page Cache:

```text
page:{slug}
```

Slot Cache:

```text
slot:{id}
```

Suggested TTL:

```text
15 minutes
```

Use Redis.

Backend should remain stateless.

---

# Frontend Architecture

## Folder Structure

```text
src
├── app
├── features
│   ├── page
│   ├── slot
│   └── component
├── services
├── types
├── components
└── lib
```

---

# Routing

Examples:

```text
/
/about-us
/contact-us
/promo
```

Use:

```text
app/[...slug]/page.tsx
```

The design should explain:

* homepage routing
* root slash handling
* nested page routing

---

# Page Rendering Flow

Step 1

```http
GET /api/pages/{slug}
```

Step 2

Collect slot IDs.

Step 3

```http
POST /api/slots/details
```

Step 4

Render components dynamically.

---

# Dynamic Component Rendering

Storefront must dynamically render every component returned by CMS.

Example registry:

```typescript
const componentRegistry = {
  PARAGRAPH: ParagraphComponent,
  BANNER: BannerComponent,
  PRODUCT_CAROUSEL: ProductCarouselComponent,
  NAVIGATION: NavigationComponent,
  QUICK_MENU: QuickMenuComponent
};
```

Renderer:

```tsx
<ComponentRenderer component={component} />
```

No deployment should be required when editors add components to slots.

---

# TypeScript Models

Provide interfaces for:

* Page
* Breadcrumb
* Slot
* Component
* Product

Use discriminated unions for component types.

---

# CMS Administration Application

Provide a simple CMS UI.

## Page Management

* Create Page
* Update Page
* Delete Page

## Slot Management

* Create Slot
* Update Slot
* Delete Slot

## Component Management

* Create Component
* Update Component
* Delete Component
* Reorder Components

## SEO Management

* Meta Title
* Meta Description
* Meta Keywords
* Canonical URL
* Robots Index
* Robots Follow
* OpenGraph fields

## Breadcrumb Management

* Add Breadcrumb
* Remove Breadcrumb
* Reorder Breadcrumb

Changes should be visible immediately in storefront.

No rebuild or redeployment required.

---

# Deliverables

Generate:

1. High-Level Architecture Diagram
2. ERD
3. PostgreSQL Schema
4. Flyway Migration Scripts
5. Spring Boot Package Structure
6. JPA Entity Design
7. DTO Hierarchy
8. REST API Contracts
9. Validation Rules
10. Cache Strategy
11. CMS UI Design
12. Next.js Folder Structure
13. TypeScript Models
14. Component Registry Implementation
15. Dynamic Slot Rendering Flow
16. Component Storage Tradeoff Analysis
17. Production Readiness Review
18. Future Extensibility Recommendations
