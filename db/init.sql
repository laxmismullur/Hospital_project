-- LMHospital MySQL initialization
-- This runs only on first container start

CREATE DATABASE IF NOT EXISTS lm_hospital_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE lm_hospital_db;

-- Grant privileges to app user
GRANT ALL PRIVILEGES ON lm_hospital_db.* TO 'lmuser'@'%';
FLUSH PRIVILEGES;

-- Tables are auto-created by Spring JPA (ddl-auto=update)
-- This file is for any custom initialization only
