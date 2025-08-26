#!/bin/bash

# Script to find potentially unused imports in Java files
# This is a heuristic approach - manual verification recommended

BACKEND_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend"

echo "Searching for potentially unused imports in Java files..."
echo "================================================"

# Function to check if an import is used in the file
check_import() {
    local file=$1
    local import=$2
    
    # Extract the class name from the import
    local classname=$(echo "$import" | sed 's/.*\.//' | sed 's/;$//')
    
    # Skip checking the import line itself
    local tempfile=$(mktemp)
    grep -v "^import " "$file" > "$tempfile"
    
    # Check if the class is used anywhere in the file (excluding import statements)
    if grep -q "\b$classname\b" "$tempfile"; then
        rm "$tempfile"
        return 0  # Import is used
    else
        rm "$tempfile"
        return 1  # Import appears unused
    fi
}

# Find all Java files
find "$BACKEND_DIR" -name "*.java" -type f | grep -v "/target/" | while read -r file; do
    # Get all imports from the file
    imports=$(grep "^import " "$file" | grep -v "^import static")
    
    if [ -n "$imports" ]; then
        unused_found=false
        unused_imports=""
        
        while IFS= read -r import_line; do
            # Extract the import statement
            import_stmt=$(echo "$import_line" | sed 's/^import //' | sed 's/;$//')
            
            # Skip java.lang imports (they're implicit)
            if [[ "$import_stmt" == "java.lang."* ]]; then
                continue
            fi
            
            # Check if this import is used
            if ! check_import "$file" "$import_stmt"; then
                unused_found=true
                unused_imports="${unused_imports}\n    ${import_line}"
            fi
        done <<< "$imports"
        
        # Print results if unused imports found
        if [ "$unused_found" = true ]; then
            echo -e "\n📁 $file"
            echo -e "   Potentially unused imports:$unused_imports"
        fi
    fi
done

echo -e "\n================================================"
echo "Scan complete. Please manually verify before removing any imports."
echo "Some imports might be used in annotations, generics, or other contexts."