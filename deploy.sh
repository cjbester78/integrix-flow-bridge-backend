#!/bin/bash

echo "🚀 Starting deployment process..."

# 1. Kill all backend processes
echo "🛑 Stopping backend processes..."
pkill -f "spring-boot:run" || true
pkill -f "java.*backend" || true

# 2. Clean backend public directory
echo "📁 Cleaning backend public directory..."
rm -rf backend/src/main/resources/public/*

# 3. Build frontend
echo "🏗️  Building frontend..."
cd frontend-ui
npm run build

# 4. Copy frontend to backend
echo "📋 Copying frontend to backend..."
cp -r dist/* ../backend/src/main/resources/public/

# 5. Start backend
echo "🚀 Starting backend..."
cd ../backend
mvn spring-boot:run &

echo "✅ Deployment complete!"
echo "📌 Backend running on http://localhost:8080"
echo "📌 Check backend logs for JWT debugging output"