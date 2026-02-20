-- Create development database
CREATE DATABASE IF NOT EXISTS users_db_dev;
USE users_db_dev;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO users (name, email, age) VALUES
    ('John Doe', 'john@example.com', 30),
    ('Jane Smith', 'jane@example.com', 28),
    ('Bob Johnson', 'bob@example.com', 35);
