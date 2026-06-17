-- Database Migration: Add Product Detail Component & Template
-- Version: 3.0.0

-- Create Table for Product Detail Component
CREATE TABLE product_detail_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    show_price BOOLEAN DEFAULT TRUE,
    show_description BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT fk_product_detail_components_base FOREIGN KEY (id) 
        REFERENCES components(id) ON DELETE CASCADE
);

COMMENT ON TABLE product_detail_components IS 'Product details component showing product data from the context';

-- =====================================================
-- SEED PRODUCT DETAIL PAGE TEMPLATE
-- =====================================================

-- Create Product Detail Template Page
INSERT INTO pages (slug, title, breadcrumb_title, meta_title, meta_description, robots_index, robots_follow)
VALUES (
    '/products/detail',
    'Product Details',
    'Product',
    'Product Details | Demo Storefront',
    'View details of the product.',
    TRUE,
    TRUE
);

-- Create Slots for the template page
INSERT INTO slots (code, name, page_id)
SELECT 'hero', 'Hero Section', id FROM pages WHERE slug = '/products/detail';

INSERT INTO slots (code, name, page_id)
SELECT 'content', 'Main Content', id FROM pages WHERE slug = '/products/detail';

INSERT INTO slots (code, name, page_id)
SELECT 'footer', 'Footer', id FROM pages WHERE slug = '/products/detail';

-- Insert Product Detail component in the Content Slot
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'product-detail-comp-1', 
    'Main Product Details', 
    'PRODUCT_DETAIL', 
    1, 
    id 
FROM slots WHERE code = 'content' AND page_id = (SELECT id FROM pages WHERE slug = '/products/detail');

-- Insert the subtype fields
INSERT INTO product_detail_components (id, title, show_price, show_description)
SELECT 
    id,
    NULL,
    TRUE,
    TRUE
FROM components WHERE uid = 'product-detail-comp-1';

-- Insert a Banner in the Hero Slot of the Product Detail template (as a demo component)
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'product-detail-hero-1', 
    'Product Page Hero Banner', 
    'BANNER', 
    1, 
    id 
FROM slots WHERE code = 'hero' AND page_id = (SELECT id FROM pages WHERE slug = '/products/detail');

INSERT INTO banner_components (id, image_url, alt_text, title, subtitle, cta_text, cta_url)
SELECT 
    id,
    'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=1600',
    'Product Detail Banner',
    'Explore Our Premium Devices',
    'Check out the technical specifications and features below.',
    NULL,
    NULL
FROM components WHERE uid = 'product-detail-hero-1';
