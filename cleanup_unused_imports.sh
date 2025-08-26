#!/bin/bash

# Script to find and remove unused imports from Java files

BACKEND_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend"

echo "Starting cleanup of unused imports..."

# Find all Java files and check for unused imports
find "$BACKEND_DIR" -name "*.java" -type f | grep -v "/target/" | while read -r file; do
    echo "Checking: $file"
    
    # Common unused imports to check for DataStructure-related cleanup
    grep -l "import.*DataStructure" "$file" 2>/dev/null && echo "  Found DataStructure import in: $file"
    grep -l "import.*XsdDependencyResolver" "$file" 2>/dev/null && echo "  Found XsdDependencyResolver import in: $file"
done

echo "Done checking for unused imports."