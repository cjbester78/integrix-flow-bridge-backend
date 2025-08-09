#!/bin/bash

# Script to update PreAuthorize annotations according to new role structure
# Administrator: Full admin access to all pages and content
# Developer: Same access as administrator EXCEPT no system settings and configurations
# Integrator: Only execute message flows
# Viewer: View monitoring pages and flows

echo "Updating PreAuthorize annotations..."

# Update SystemConfigController - only ADMINISTRATOR can access system settings
sed -i '' 's/@PreAuthorize("hasRole('\''ADMIN'\'')")/@PreAuthorize("hasRole('\''ADMINISTRATOR'\'')")/' \
    backend/src/main/java/com/integrationlab/backend/controller/SystemConfigController.java

# Update DevelopmentFunctionController - ADMINISTRATOR and DEVELOPER can access
sed -i '' 's/@PreAuthorize("hasAnyRole('\''ADMINISTRATOR'\'', '\''INTEGRATOR'\'')")/@PreAuthorize("hasAnyRole('\''ADMINISTRATOR'\'', '\''DEVELOPER'\'')")/' \
    backend/src/main/java/com/integrationlab/backend/controller/DevelopmentFunctionController.java

# Update FlowExportImportController - ADMINISTRATOR and DEVELOPER can export/import
sed -i '' 's/@PreAuthorize("hasAnyRole('\''ADMIN'\'', '\''INTEGRATOR'\'')")/@PreAuthorize("hasAnyRole('\''ADMINISTRATOR'\'', '\''DEVELOPER'\'')")/' \
    backend/src/main/java/com/integrationlab/backend/controller/FlowExportImportController.java

echo "PreAuthorize annotations updated!"