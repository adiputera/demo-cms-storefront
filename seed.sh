#!/bin/bash
set -e

echo "Creating Products..."
curl -s -X POST http://localhost:8081/api/cms/products \
  -H "Content-Type: application/json" \
  -d '{"code":"mbp-16","name":"MacBook Pro 16","imageUrl":"/images/macbook.jpg","price":2499.00,"description":"The new MacBook Pro."}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/products \
  -H "Content-Type: application/json" \
  -d '{"code":"iphone-16","name":"iPhone 16 Pro","imageUrl":"/images/iphone.jpg","price":999.00,"description":"The latest iPhone."}' > /dev/null

echo "Creating Pages..."
PAGE_ID=$(curl -s -X POST http://localhost:8081/api/cms/pages \
  -H "Content-Type: application/json" \
  -d '{"slug":"/","title":"Home","breadcrumbTitle":"Home","metaTitle":"Homepage"}' | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "Creating Slots..."
SLOT_ID=$(curl -s -X POST http://localhost:8081/api/cms/slots \
  -H "Content-Type: application/json" \
  -d "{\"pageId\":${PAGE_ID},\"code\":\"hero\",\"name\":\"Hero Slot\"}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "Creating Components..."
COMP_ID=$(curl -s -X POST http://localhost:8081/api/cms/components/BANNER \
  -H "Content-Type: application/json" \
  -d "{\"uid\":\"hero-banner\",\"name\":\"Main Hero Banner\",\"fields\":{\"imageUrl\":\"/images/hero.jpg\",\"title\":\"Welcome to our store!\",\"ctaText\":\"Shop Now\"}}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "Linking Component to Slot..."
curl -s -X POST "http://localhost:8081/api/cms/slots/${SLOT_ID}/components/${COMP_ID}" > /dev/null

echo "Syncing Content Catalog..."
curl -s -X POST http://localhost:8081/api/sync/contentCatalog > /dev/null

echo "Syncing Product Catalog..."
curl -s -X POST http://localhost:8081/api/sync/productCatalog > /dev/null

echo "Database Seeded and Synced!"
