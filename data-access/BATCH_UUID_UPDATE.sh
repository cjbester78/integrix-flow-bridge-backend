#!/bin/bash

# Script to batch update entities and repositories to use UUID

# Function to update entity files
update_entity() {
    local file=$1
    echo "Updating entity: $file"
    
    # Add UUID import
    sed -i '' '/import java.time.LocalDateTime;/a\
import java.util.UUID;' "$file" 2>/dev/null || true
    
    # Update @Id field
    sed -i '' '/@Id/{
        N;N;N;N;
        s/@Id[[:space:]]*\n[[:space:]]*@GeneratedValue(generator = "uuid2")[[:space:]]*\n[[:space:]]*@GenericGenerator.*\n[[:space:]]*@Column(columnDefinition = "char(36)")[[:space:]]*\n[[:space:]]*@EqualsAndHashCode.Include[[:space:]]*\n[[:space:]]*private String id;/@Id\
    @GeneratedValue(strategy = GenerationType.UUID)\
    @EqualsAndHashCode.Include\
    private UUID id;/
    }' "$file"
    
    # Update business component ID references
    sed -i '' 's/private String businessComponentId;/private UUID businessComponentId;/g' "$file"
    
    # Update createdBy/updatedBy to use User entity
    sed -i '' '/@Column(name = "created_by")/{
        N;
        s/@Column(name = "created_by")[[:space:]]*\n[[:space:]]*private String createdBy;/@ManyToOne(fetch = FetchType.LAZY)\
    @JoinColumn(name = "created_by")\
    private User createdBy;/
    }' "$file"
    
    sed -i '' '/@Column(name = "updated_by")/{
        N;
        s/@Column(name = "updated_by")[[:space:]]*\n[[:space:]]*private String updatedBy;/@ManyToOne(fetch = FetchType.LAZY)\
    @JoinColumn(name = "updated_by")\
    private User updatedBy;/
    }' "$file"
    
    # Remove GenericGenerator import if present
    sed -i '' '/import org.hibernate.annotations.GenericGenerator;/d' "$file"
    
    # Update @PrePersist to remove UUID generation
    sed -i '' '/if (id == null) {/{
        N;
        s/if (id == null) {[[:space:]]*\n[[:space:]]*id = java.util.UUID.randomUUID().toString();[[:space:]]*\n[[:space:]]*}//
    }' "$file"
}

# Function to update repository files
update_repository() {
    local file=$1
    echo "Updating repository: $file"
    
    # Add UUID import
    sed -i '' '/import java.util.Optional;/a\
import java.util.UUID;' "$file" 2>/dev/null || true
    
    # Update JpaRepository generic type
    sed -i '' 's/JpaRepository<\([^,]*\), String>/JpaRepository<\1, UUID>/g' "$file"
    
    # Update method parameters from String to UUID
    sed -i '' 's/(String id)/(UUID id)/g' "$file"
    sed -i '' 's/String businessComponentId/UUID businessComponentId/g' "$file"
    sed -i '' 's/String flowId/UUID flowId/g' "$file"
    sed -i '' 's/String adapterId/UUID adapterId/g' "$file"
    sed -i '' 's/String userId/UUID userId/g' "$file"
    sed -i '' 's/String transformationId/UUID transformationId/g' "$file"
}

# Entities to update
entities=(
    "IntegrationFlow.java"
    "CommunicationAdapter.java"
    "FlowTransformation.java"
    "SystemLog.java"
    "AdapterPayload.java"
    "Role.java"
    "AuditTrail.java"
    "Certificate.java"
    "DataStructure.java"
    "UserSession.java"
    "FlowExecution.java"
    "Message.java"
    "FlowStructureMessage.java"
)

# Repositories to update
repositories=(
    "IntegrationFlowRepository.java"
    "CommunicationAdapterRepository.java"
    "FlowTransformationRepository.java"
    "SystemLogRepository.java"
    "AdapterPayloadRepository.java"
    "RoleRepository.java"
    "AuditTrailRepository.java"
    "CertificateRepository.java"
    "DataStructureRepository.java"
    "UserSessionRepository.java"
    "FlowExecutionRepository.java"
    "MessageRepository.java"
    "FlowStructureMessageRepository.java"
)

# Change to model directory
cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/data-access/src/main/java/com/integrixs/data/model

# Update entities
for entity in "${entities[@]}"; do
    if [ -f "$entity" ]; then
        update_entity "$entity"
    else
        echo "Warning: Entity $entity not found"
    fi
done

# Change to repository directory
cd ../repository

# Update repositories
for repo in "${repositories[@]}"; do
    if [ -f "$repo" ]; then
        update_repository "$repo"
    else
        echo "Warning: Repository $repo not found"
    fi
done

echo "UUID migration batch update complete!"