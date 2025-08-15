#!/bin/bash

# Direct command to clear logs and errors
# Database credentials from application-dev.yml:
# - Host: localhost
# - Port: 3306
# - Database: integrixflowbridge
# - Username: root
# - Password: B3st3r@01

echo "Clearing system_logs and user_management_errors tables..."
mysql -h localhost -P 3306 -u root -pB3st3r@01 integrixflowbridge < clear_logs_and_errors.sql

echo "Done!"