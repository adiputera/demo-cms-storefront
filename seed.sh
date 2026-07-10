#!/bin/bash
set -e

echo "Creating Products..."
curl -s -X POST http://localhost:8081/api/cms/products \
  -H "Content-Type: application/json" \
  -d '{"code":"mbp-16","name":"MacBook Pro 16","imageUrl":"/images/macbook.jpg","price":2499.00,"description":"The new MacBook Pro."}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/products \
  -H "Content-Type: application/json" \
  -d '{"code":"iphone-16","name":"iPhone 16 Pro","imageUrl":"/images/iphone.jpg","price":999.00,"description":"The latest iPhone."}' > /dev/null

echo "Creating Articles..."
curl -s -X POST http://localhost:8081/api/cms/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"Introducing the MacBook Pro 16","slug":"introducing-the-macbook-pro-16","body":"The new MacBook Pro 16 sets a new standard for professional laptops..."}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"iPhone 16 Pro: The Future of Mobile Photography","slug":"iphone-16-pro-the-future-of-mobile-photography","body":"With the iPhone 16 Pro, Apple redefines what a smartphone camera can do."}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"Top 5 Accessories for Your New Mac","slug":"top-5-accessories-for-your-new-mac","body":"Whether you just picked up a MacBook Pro or a Mac Studio..."}' > /dev/null

echo "Creating Events..."
curl -s -X POST http://localhost:8081/api/cms/events \
  -H "Content-Type: application/json" \
  -d '{"title":"Apple Tech Summit 2026","slug":"apple-tech-summit-2026","description":"Join us for a full day of talks...","location":"Jakarta","eventDate":"2026-09-15T09:00:00"}' > /dev/null

echo "Creating Pages..."
PAGE_ID=$(curl -s -X POST http://localhost:8081/api/cms/pages \
  -H "Content-Type: application/json" \
  -d '{"slug":"/","title":"Home","breadcrumbTitle":"Home","metaTitle":"Homepage"}' | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "Creating Slots..."
SLOT_ID=$(curl -s -X POST http://localhost:8081/api/cms/slots \
  -H "Content-Type: application/json" \
  -d "{\"pageId\":${PAGE_ID},\"code\":\"hero\",\"name\":\"Hero Slot\"}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "Creating Components..."
COMP_ID=$(curl -s -X POST http://localhost:8081/api/cms/components \
  -H "Content-Type: application/json" \
  -d "{\"uid\":\"hero-banner\",\"name\":\"Main Hero Banner\",\"type\":\"BANNER\",\"slotId\":${SLOT_ID},\"imageUrl\":\"/images/hero.jpg\",\"title\":\"Welcome to our store!\",\"ctaText\":\"Shop Now\"}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

TRENDING_COMP_ID=$(curl -s -X POST http://localhost:8081/api/cms/components \
  -H "Content-Type: application/json" \
  -d "{\"uid\":\"trending-articles-v1\",\"name\":\"Trending Articles Component\",\"type\":\"TRENDING_ARTICLE\",\"slotId\":${SLOT_ID},\"title\":\"Trending Tech News\",\"articleSlugs\":\"introducing-the-macbook-pro-16,iphone-16-pro-the-future-of-mobile-photography,top-5-accessories-for-your-new-mac\"}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

CAROUSEL_ID=$(curl -s -X POST http://localhost:8081/api/cms/components \
  -H "Content-Type: application/json" \
  -d "{\"uid\":\"this-is-carousel\",\"name\":\"this is carousel\",\"type\":\"PRODUCT_CAROUSEL\",\"slotId\":${SLOT_ID},\"title\":\"Product\",\"productCodes\":[\"mbp-16\",\"iphone-16\"]}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "Linking Components to Slot..."
curl -s -X POST "http://localhost:8081/api/cms/components/slots/${SLOT_ID}/components/${COMP_ID}" -H "Content-Type: application/json" -d '{}' > /dev/null
curl -s -X POST "http://localhost:8081/api/cms/components/slots/${SLOT_ID}/components/${TRENDING_COMP_ID}" -H "Content-Type: application/json" -d '{}' > /dev/null
curl -s -X POST "http://localhost:8081/api/cms/components/slots/${SLOT_ID}/components/${CAROUSEL_ID}" -H "Content-Type: application/json" -d '{}' > /dev/null

echo "Syncing Catalogs..."
curl -s -X POST http://localhost:8081/api/sync/productCatalog > /dev/null
curl -s -X POST http://localhost:8081/api/sync/articleCatalog > /dev/null
curl -s -X POST http://localhost:8081/api/sync/eventCatalog > /dev/null
curl -s -X POST http://localhost:8081/api/sync/contentCatalog > /dev/null

echo "Database Seeded and Synced!"
