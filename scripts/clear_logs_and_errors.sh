#!/bin/bash

# Script to clear system logs and user management errors using credentials from application-dev.yml

echo "=== Clear System Logs and User Management Errors ==="
echo "WARNING: This will permanently delete all records from system_logs and user_management_errors tables"
echo ""

# Extract database credentials from application-dev.yml
CONFIG_FILE="../backend/src/main/resources/application-dev.yml"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: Configuration file not found at $CONFIG_FILE"
    exit 1
fi

# Extract database connection details
DB_HOST=$(grep -A 10 "datasource:" "$CONFIG_FILE" | grep "url:" | sed -E 's/.*\/\/([^:\/]+).*/\1/')
DB_PORT=$(grep -A 10 "datasource:" "$CONFIG_FILE" | grep "url:" | sed -E 's/.*:([0-9]+)\/.*/\1/')
DB_NAME=$(grep -A 10 "datasource:" "$CONFIG_FILE" | grep "url:" | sed -E 's/.*\/([^?]+).*/\1/')
DB_USER=$(grep -A 10 "datasource:" "$CONFIG_FILE" | grep "username:" | awk '{print $2}')
DB_PASS=$(grep -A 10 "datasource:" "$CONFIG_FILE" | grep "password:" | awk '{print $2}')

echo "Database: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "User: $DB_USER"
echo ""

# Ask for confirmation
read -p "Are you sure you want to delete all records? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "Operation cancelled"
    exit 0
fi

# Execute the SQL script
echo ""
echo "Executing cleanup script..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < clear_logs_and_errors.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Cleanup completed successfully"
else
    echo ""
    echo "❌ Error occurred during cleanup"
    exit 1
fi