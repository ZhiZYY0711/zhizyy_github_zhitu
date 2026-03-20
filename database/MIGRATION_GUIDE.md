# Database Migration Guide

Guide for deploying and migrating the Zhitu database schema.

## Initial Deployment

### Step 1: Prepare PostgreSQL Server

```bash
# Install PostgreSQL 15+ (Ubuntu/Debian)
sudo apt update
sudo apt install postgresql-15 postgresql-contrib-15

# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Verify installation
psql --version
```

### Step 2: Create Database and User

```bash
# Switch to postgres user
sudo -u postgres psql

# In PostgreSQL prompt:
CREATE DATABASE zhitu_cloud 
    WITH ENCODING='UTF8' 
    LC_COLLATE='en_US.UTF-8' 
    LC_CTYPE='en_US.UTF-8' 
    TEMPLATE=template0;

# Create application user
CREATE USER zhitu_