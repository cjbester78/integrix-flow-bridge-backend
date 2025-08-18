#!/bin/bash

# Script to update all entities from String id to UUID id

# Function to update a single file
update_entity() {
    local file=$1
    echo "Updating $file..."
    
    # Add UUID import if not present
    if ! grep -q "import java.util.UUID;" "$file"; then
        sed -i '' '/import java.time.LocalDateTime;/a\
import java.util.UUID;' "$file" 2>/dev/null || \
        sed -i '' '/package com.integrixs.data.model;/a\
\
import java.util.UUID;' "$file"
    fi
    
    # Update @Id field from String to UUID
    sed -i '' '/@Id/,/private String id;/{
        s/@GeneratedValue(generator = "uuid2")/@GeneratedValue(strategy = GenerationType.UUID)/
        s/@GenericGenerator.*//
        s/@Column(columnDefinition = "char(36)")//
        s/private String id;/private UUID id;/
    }' "$file"
    
    # Update User references from String to User entity
    sed -i '' 's/private String createdBy;/private User createdBy;/g' "$file"
    sed -i '' 's/private String updatedBy;/private User updatedBy;/g' "$file"
    
    # Update @Column to @ManyToOne for user references
    sed -i '' '/@Column(name = "created_by")/,/private User createdBy;/{
        s/@Column(name = "created_by")/@ManyToOne(fetch = FetchType.LAZY)\
    @JoinColumn(name = "created_by")/
    }' "$file"
    
    sed -i '' '/@Column(name = "updated_by")/,/private User updatedBy;/{
        s/@Column(name = "updated_by")/@ManyToOne(fetch = FetchType.LAZY)\
    @JoinColumn(name = "updated_by")/
    }' "$file"
    
    # Clean up empty lines
    sed -i '' '/^[[:space:]]*$/d' "$file"
}

# List of entities to update
entities=(
    "FlowStructure.java"
    "MessageStructure.java"
    "IntegrationFlow.java"
    "SystemLog.java"
    "AdapterPayload.java"
    "Role.java"
    "CommunicationAdapter.java"
    "FieldMapping.java"
    "BusinessComponent.java"
    "AuditTrail.java"
    "FlowTransformation.java"
    "SystemSetting.java"
    "Certificate.java"
    "DataStructure.java"
    "UserSession.java"
)

cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/data-access/src/main/java/com/integrixs/data/model

for entity in "${entities[@]}"; do
    if [ -f "$entity" ]; then
        update_entity "$entity"
    else
        echo "Warning: $entity not found"
    fi
done

echo "UUID migration complete!"