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
  -d '{"title":"Introducing the MacBook Pro 16","slug":"introducing-the-macbook-pro-16","body":"The new MacBook Pro 16 sets a new standard for professional laptops, featuring the M3 Pro chip, stunning Liquid Retina XDR display, and up to 22 hours of battery life."}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"iPhone 16 Pro: The Future of Mobile Photography","slug":"iphone-16-pro-the-future-of-mobile-photography","body":"With the iPhone 16 Pro, Apple redefines what a smartphone camera can do. The new 48MP Fusion camera system with 5x optical zoom brings DSLR-quality shots to your pocket."}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"Top 5 Accessories for Your New Mac","slug":"top-5-accessories-for-your-new-mac","body":"Whether you just picked up a MacBook Pro or a Mac Studio, these five accessories will supercharge your workflow: a quality USB-C hub, mechanical keyboard, ergonomic mouse, 4K monitor, and a cable management kit."}' > /dev/null

echo "Creating Events..."
curl -s -X POST http://localhost:8081/api/cms/events \
  -H "Content-Type: application/json" \
  -d '{"title":"Apple Tech Summit 2026","slug":"apple-tech-summit-2026","description":"Join us for a full day of talks, workshops, and hands-on demos showcasing the latest Apple products and developer tools.","location":"Jakarta Convention Center, Jakarta"}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/events \
  -H "Content-Type: application/json" \
  -d '{"title":"MacBook Pro Launch Night","slug":"macbook-pro-launch-night","description":"Be the first to experience the all-new MacBook Pro 16. Exclusive in-store demo sessions with our product specialists available throughout the evening.","location":"iStore Grand Indonesia, Jakarta"}' > /dev/null

curl -s -X POST http://localhost:8081/api/cms/events \
  -H "Content-Type: application/json" \
  -d '{"title":"Photography Masterclass with iPhone 16 Pro","slug":"photography-masterclass-with-iphone-16-pro","description":"Learn how to capture stunning photos and cinematic videos using the iPhone 16 Pro. Tips and tricks from professional photographers.","location":"Creative Hub Kemang, Jakarta"}' > /dev/null

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

echo "Syncing Article Catalog..."
curl -s -X POST http://localhost:8081/api/sync/articleCatalog > /dev/null

echo "Syncing Event Catalog..."
curl -s -X POST http://localhost:8081/api/sync/eventCatalog > /dev/null

echo "Database Seeded and Synced!"
