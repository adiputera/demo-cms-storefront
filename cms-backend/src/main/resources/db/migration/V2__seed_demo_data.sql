-- Seed Data for Headless CMS Demo
-- Version: 2.0.0
-- Description: Sample products and a demo homepage with components

-- =====================================================
-- SAMPLE PRODUCTS
-- =====================================================
INSERT INTO products (code, name, image_url, price, description) VALUES
('macbook-pro', 'MacBook Pro 16"', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800', 2499.99, 'Powerful laptop with M3 chip, 16GB RAM, and 512GB SSD'),
('iphone-15-pro', 'iPhone 15 Pro', 'https://images.unsplash.com/photo-1592286927505-b55e43a80daa?w=800', 1199.99, 'Latest iPhone with titanium design and A17 Pro chip'),
('airpods-pro', 'AirPods Pro 2', 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=800', 249.99, 'Active noise cancellation and spatial audio'),
('ipad-air', 'iPad Air', 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800', 599.99, '10.9-inch Liquid Retina display with M1 chip'),
('apple-watch-9', 'Apple Watch Series 9', 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800', 399.99, 'Advanced health features and always-on display'),
('magic-keyboard', 'Magic Keyboard', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800', 149.99, 'Wireless keyboard with numeric keypad');

-- =====================================================
-- HOMEPAGE SETUP
-- =====================================================

-- Create homepage (slug = "/")
INSERT INTO pages (slug, title, breadcrumb_title, meta_title, meta_description, robots_index, robots_follow, og_title, og_description)
VALUES (
    '/',
    'Welcome to Our Store',
    'Home',
    'Premium Electronics & Accessories | Demo Storefront',
    'Shop the latest electronics, smartphones, laptops, and accessories from top brands. Free shipping on orders over $50.',
    TRUE,
    TRUE,
    'Premium Electronics Store',
    'Discover amazing deals on premium electronics and accessories'
);

-- Create "About Us" page
INSERT INTO pages (slug, title, breadcrumb_title, meta_title, meta_description, robots_index, robots_follow)
VALUES (
    '/about-us',
    'About Our Company',
    'About Us',
    'About Us | Demo Storefront',
    'Learn more about our company, mission, and values.',
    TRUE,
    TRUE
);

-- Add breadcrumb to "About Us" page (Home > About Us)
INSERT INTO page_breadcrumbs (page_id, breadcrumb_page_id, breadcrumb_order)
SELECT 
    (SELECT id FROM pages WHERE slug = '/about-us'),
    (SELECT id FROM pages WHERE slug = '/'),
    0;

-- =====================================================
-- HOMEPAGE SLOTS
-- =====================================================

-- Hero Slot
INSERT INTO slots (code, name, page_id)
SELECT 'hero', 'Hero Section', id FROM pages WHERE slug = '/';

-- Content Slot
INSERT INTO slots (code, name, page_id)
SELECT 'content', 'Main Content', id FROM pages WHERE slug = '/';

-- Footer Slot
INSERT INTO slots (code, name, page_id)
SELECT 'footer', 'Footer', id FROM pages WHERE slug = '/';

-- =====================================================
-- HOMEPAGE COMPONENTS
-- =====================================================

-- Hero Slot: Banner Component
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'hero-banner-1', 
    'Main Hero Banner', 
    'BANNER', 
    1, 
    id 
FROM slots WHERE code = 'hero' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO banner_components (id, image_url, alt_text, title, subtitle, cta_text, cta_url)
SELECT 
    id,
    'https://images.unsplash.com/photo-1468495244123-6c6c332eeece?w=1600',
    'Premium Electronics Collection',
    'Welcome to the Future of Tech',
    'Discover our curated collection of premium electronics and accessories',
    'Shop Now',
    '/products'
FROM components WHERE uid = 'hero-banner-1';

-- Content Slot: Paragraph Component (Welcome Message)
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'content-paragraph-1', 
    'Welcome Message', 
    'PARAGRAPH', 
    1, 
    id 
FROM slots WHERE code = 'content' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO paragraph_components (id, title, content)
SELECT 
    id,
    'Why Choose Us?',
    '<p>We offer the latest and greatest in technology, from cutting-edge smartphones to powerful laptops and innovative accessories. Our commitment to quality and customer satisfaction sets us apart.</p><p>With <strong>free shipping on orders over $50</strong> and a <strong>30-day money-back guarantee</strong>, shopping with us is risk-free.</p>'
FROM components WHERE uid = 'content-paragraph-1';

-- Content Slot: Product Carousel Component
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'content-carousel-1', 
    'Featured Products', 
    'PRODUCT_CAROUSEL', 
    2, 
    id 
FROM slots WHERE code = 'content' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO product_carousel_components (id, title, product_codes)
SELECT 
    id,
    'Featured Products',
    'macbook-pro,iphone-15-pro,airpods-pro,ipad-air'
FROM components WHERE uid = 'content-carousel-1';

-- Content Slot: Another Paragraph (Special Offer)
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'content-paragraph-2', 
    'Special Offer Message', 
    'PARAGRAPH', 
    3, 
    id 
FROM slots WHERE code = 'content' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO paragraph_components (id, title, content)
SELECT 
    id,
    'Limited Time Offer',
    '<p class="text-lg font-bold">Get 10% off your first order!</p><p>Sign up for our newsletter and receive an exclusive discount code. Plus, be the first to know about new arrivals, sales, and special promotions.</p>'
FROM components WHERE uid = 'content-paragraph-2';

-- Footer Slot: Navigation Component (Customer Service)
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'footer-nav-1', 
    'Customer Service Link', 
    'NAVIGATION', 
    1, 
    id 
FROM slots WHERE code = 'footer' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO navigation_components (id, display_text, url, icon)
SELECT 
    id,
    'Customer Service',
    '/support',
    'support'
FROM components WHERE uid = 'footer-nav-1';

-- Footer Slot: Navigation Component (Contact Us)
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'footer-nav-2', 
    'Contact Link', 
    'NAVIGATION', 
    2, 
    id 
FROM slots WHERE code = 'footer' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO navigation_components (id, display_text, url, icon)
SELECT 
    id,
    'Contact Us',
    '/contact',
    'mail'
FROM components WHERE uid = 'footer-nav-2';

-- Footer Slot: Navigation Component (About Us)
INSERT INTO components (uid, name, type, sort_order, slot_id)
SELECT 
    'footer-nav-3', 
    'About Link', 
    'NAVIGATION', 
    3, 
    id 
FROM slots WHERE code = 'footer' AND page_id = (SELECT id FROM pages WHERE slug = '/');

INSERT INTO navigation_components (id, display_text, url, icon)
SELECT 
    id,
    'About Us',
    '/about-us',
    'info'
FROM components WHERE uid = 'footer-nav-3';
