CREATE DATABASE IF NOT EXISTS vcs_db;
USE vcs_db;

-- 1. Таблица Users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 2. Таблица Documents
CREATE TABLE documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    metadata TEXT
);

-- 3. Таблица Document Versions
CREATE TABLE document_versions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    document_id INT NOT NULL,
    version_number INT NOT NULL,
    author_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    content LONGBLOB, -- LONGBLOB позволява запис на големи файлове в MySQL
    
    FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);

INSERT INTO users (username, password_hash, role) 
VALUES ('admin', '$2a$10$wE9q.I/HqGgW9K.G/HqGg.wE9q.I/HqGgW9K.G/HqGgW9K.G/HqGg', 'ADMIN');