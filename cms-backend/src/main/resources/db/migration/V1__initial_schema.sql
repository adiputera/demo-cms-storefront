-- Headless CMS Database Schema
-- Version: 1.0.0
-- Description: Initial schema for pages, slots, components, and products

-- =====================================================
-- PAGES TABLE
-- =====================================================
CREATE TABLE pages (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    breadcrumb_title VARCHAR(255) NOT NULL,
    
    -- SEO Fields
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(500),
    canonical_url VARCHAR(500),
    robots_index BOOLEAN NOT NULL DEFAULT TRUE,
    robots_follow BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- OpenGraph Fields
    og_title VARCHAR(255),
    og_description VARCHAR(500),
    og_image VARCHAR(500),
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pages_slug ON pages(slug);

COMMENT ON TABLE pages IS 'CMS-managed pages with SEO metadata';
COMMENT ON COLUMN pages.slug IS 'URL-friendly identifier, must be unique. Use "/" for homepage';

-- =====================================================
-- PAGE BREADCRUMBS TABLE
-- =====================================================
CREATE TABLE page_breadcrumbs (
    page_id BIGINT NOT NULL,
    breadcrumb_page_id BIGINT NOT NULL,
    breadcrumb_order INTEGER NOT NULL,
    
    CONSTRAINT fk_page_breadcrumbs_page FOREIGN KEY (page_id) 
        REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT fk_page_breadcrumbs_breadcrumb FOREIGN KEY (breadcrumb_page_id) 
        REFERENCES pages(id) ON DELETE CASCADE,
    
    PRIMARY KEY (page_id, breadcrumb_page_id)
);

CREATE INDEX idx_page_breadcrumbs_page ON page_breadcrumbs(page_id);

COMMENT ON TABLE page_breadcrumbs IS 'Manually managed breadcrumb navigation for pages';

-- =====================================================
-- SLOTS TABLE
-- =====================================================
CREATE TABLE slots (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    page_id BIGINT NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_slots_page FOREIGN KEY (page_id) 
        REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT uq_slots_page_code UNIQUE (page_id, code)
);

CREATE INDEX idx_slots_page_code ON slots(page_id, code);

COMMENT ON TABLE slots IS 'Fixed content areas on a page (e.g., hero, content, footer)';
COMMENT ON COLUMN slots.code IS 'Unique identifier within a page (e.g., "hero", "content")';

-- =====================================================
-- COMPONENTS TABLE (Base Table)
-- =====================================================
CREATE TABLE components (
    id BIGSERIAL PRIMARY KEY,
    uid VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL,
    slot_id BIGINT NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_components_slot FOREIGN KEY (slot_id) 
        REFERENCES slots(id) ON DELETE CASCADE
);

CREATE INDEX idx_components_slot_order ON components(slot_id, sort_order);

COMMENT ON TABLE components IS 'Base table for all component types';
COMMENT ON COLUMN components.type IS 'Component type: PARAGRAPH, BANNER, PRODUCT_CAROUSEL, NAVIGATION, QUICK_MENU';
COMMENT ON COLUMN components.sort_order IS 'Display order within the slot (ascending)';

-- =====================================================
-- PARAGRAPH COMPONENTS TABLE
-- =====================================================
CREATE TABLE paragraph_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    
    CONSTRAINT fk_paragraph_components_base FOREIGN KEY (id) 
        REFERENCES components(id) ON DELETE CASCADE
);

COMMENT ON TABLE paragraph_components IS 'Rich text content component';

-- =====================================================
-- BANNER COMPONENTS TABLE
-- =====================================================
CREATE TABLE banner_components (
    id BIGINT PRIMARY KEY,
    image_url VARCHAR(500),
    alt_text VARCHAR(255),
    title VARCHAR(255),
    subtitle VARCHAR(500),
    cta_text VARCHAR(100),
    cta_url VARCHAR(500),
    
    CONSTRAINT fk_banner_components_base FOREIGN KEY (id) 
        REFERENCES components(id) ON DELETE CASCADE
);

COMMENT ON TABLE banner_components IS 'Hero banner with image and call-to-action';

-- =====================================================
-- PRODUCT CAROUSEL COMPONENTS TABLE
-- =====================================================
CREATE TABLE product_carousel_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    product_codes TEXT,
    
    CONSTRAINT fk_product_carousel_components_base FOREIGN KEY (id) 
        REFERENCES components(id) ON DELETE CASCADE
);

COMMENT ON TABLE product_carousel_components IS 'Product showcase carousel';
COMMENT ON COLUMN product_carousel_components.product_codes IS 'Comma-separated list of product codes';

-- =====================================================
-- NAVIGATION COMPONENTS TABLE
-- =====================================================
CREATE TABLE navigation_components (
    id BIGINT PRIMARY KEY,
    display_text VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    icon VARCHAR(100),
    
    CONSTRAINT fk_navigation_components_base FOREIGN KEY (id) 
        REFERENCES components(id) ON DELETE CASCADE
);

COMMENT ON TABLE navigation_components IS 'Navigation link with optional icon';

-- =====================================================
-- QUICK MENU COMPONENTS TABLE
-- =====================================================
CREATE TABLE quick_menu_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    url VARCHAR(500) NOT NULL,
    
    CONSTRAINT fk_quick_menu_components_base FOREIGN KEY (id) 
        REFERENCES components(id) ON DELETE CASCADE
);

COMMENT ON TABLE quick_menu_components IS 'Image tile menu item';

-- =====================================================
-- PRODUCTS TABLE
-- =====================================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_code ON products(code);

COMMENT ON TABLE products IS 'Minimal product catalog for demo purposes';
COMMENT ON COLUMN products.code IS 'Unique product identifier (e.g., "macbook-pro")';
