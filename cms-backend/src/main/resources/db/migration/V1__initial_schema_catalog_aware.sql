-- Headless CMS Database Schema
-- Version: 1.0.0
-- Description: Initial schema for catalogs, pages, slots, components, products, articles, and events

-- =====================================================
-- CATALOGS TABLE
-- =====================================================
CREATE TABLE catalogs (
    id BIGSERIAL PRIMARY KEY,
    catalog_id VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_catalogs_id_version UNIQUE (catalog_id, version)
);
CREATE INDEX idx_catalog_id_version ON catalogs(catalog_id, version);
COMMENT ON TABLE catalogs IS 'Represents a catalog version (e.g. contentCatalog STAGED or ONLINE)';

-- =====================================================
-- PAGES TABLE
-- =====================================================
CREATE TABLE pages (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    slug VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    breadcrumb_title VARCHAR(255) NOT NULL,
    
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(500),
    canonical_url VARCHAR(500),
    robots_index BOOLEAN NOT NULL DEFAULT TRUE,
    robots_follow BOOLEAN NOT NULL DEFAULT TRUE,
    
    og_title VARCHAR(255),
    og_description VARCHAR(500),
    og_image VARCHAR(500),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_pages_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT uk_pages_slug_catalog UNIQUE (slug, catalog_id)
);
CREATE INDEX idx_pages_slug ON pages(slug);
COMMENT ON TABLE pages IS 'CMS-managed pages with SEO metadata, scoped by catalog';

-- =====================================================
-- PAGE BREADCRUMBS TABLE
-- =====================================================
CREATE TABLE page_breadcrumbs (
    page_id BIGINT NOT NULL,
    breadcrumb_page_id BIGINT NOT NULL,
    breadcrumb_order INTEGER NOT NULL,
    
    CONSTRAINT fk_page_breadcrumbs_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT fk_page_breadcrumbs_breadcrumb FOREIGN KEY (breadcrumb_page_id) REFERENCES pages(id) ON DELETE CASCADE,
    
    PRIMARY KEY (page_id, breadcrumb_page_id)
);
CREATE INDEX idx_page_breadcrumbs_page ON page_breadcrumbs(page_id);

-- =====================================================
-- SLOTS TABLE
-- =====================================================
CREATE TABLE slots (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    page_id BIGINT NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_slots_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT fk_slots_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT uk_slots_page_code_catalog UNIQUE (page_id, code, catalog_id)
);
CREATE INDEX idx_slots_page_code ON slots(page_id, code);
COMMENT ON TABLE slots IS 'Fixed content areas on a page (e.g., hero, content, footer)';

-- =====================================================
-- COMPONENTS TABLE (Base Table)
-- =====================================================
CREATE TABLE components (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    uid VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_components_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT uk_components_uid_catalog UNIQUE (uid, catalog_id)
);
COMMENT ON TABLE components IS 'Base table for all component types, scoped by catalog';

-- =====================================================
-- SLOT_COMPONENTS (Many-to-Many Join Table)
-- =====================================================
CREATE TABLE slot_components (
    slot_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    
    CONSTRAINT fk_sc_slot FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_sc_component FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
    
    PRIMARY KEY (slot_id, component_id)
);
CREATE INDEX idx_slot_components_order ON slot_components(slot_id, sort_order);

-- =====================================================
-- COMPONENT SUBTYPES
-- =====================================================
CREATE TABLE paragraph_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    CONSTRAINT fk_paragraph_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE banner_components (
    id BIGINT PRIMARY KEY,
    image_url VARCHAR(500),
    alt_text VARCHAR(255),
    title VARCHAR(255),
    subtitle VARCHAR(500),
    cta_text VARCHAR(100),
    cta_url VARCHAR(500),
    CONSTRAINT fk_banner_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE product_carousel_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    product_codes TEXT,
    CONSTRAINT fk_product_carousel_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE navigation_components (
    id BIGINT PRIMARY KEY,
    display_text VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    icon VARCHAR(100),
    CONSTRAINT fk_navigation_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE quick_menu_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_quick_menu_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE product_detail_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    show_price BOOLEAN DEFAULT TRUE,
    show_description BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_product_detail_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE latest_article_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    article_count INTEGER NOT NULL DEFAULT 3,
    CONSTRAINT fk_latest_article_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE trending_article_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    article_slugs TEXT,
    CONSTRAINT fk_trending_article_components_id FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE latest_event_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    event_slugs TEXT,
    count INTEGER NOT NULL DEFAULT 3,
    CONSTRAINT fk_latest_event_components_id FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

CREATE TABLE top_event_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    event_slug VARCHAR(100),
    CONSTRAINT fk_top_event_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

-- =====================================================
-- PRODUCTS, ARTICLES, EVENTS TABLES
-- =====================================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_products_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT uk_products_code_catalog UNIQUE (code, catalog_id)
);
CREATE INDEX idx_products_code ON products(code);

CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    slug VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INT NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_articles_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT uk_articles_slug_catalog UNIQUE (slug, catalog_id)
);
CREATE INDEX idx_articles_slug ON articles(slug);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    slug VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(255),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INT NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_events_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT uk_events_slug_catalog UNIQUE (slug, catalog_id)
);
CREATE INDEX idx_events_slug ON events(slug);
