-- Variant 24: Information system for monitoring museum exhibits.
-- CAUTION: script recreates the demo tables and removes existing demo data.
CREATE DATABASE IF NOT EXISTS museum_exhibits CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE museum_exhibits;

DROP TABLE IF EXISTS conservation_request;
DROP TABLE IF EXISTS inspection;
DROP TABLE IF EXISTS specialist;
DROP TABLE IF EXISTS exhibit;

CREATE TABLE exhibit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_number VARCHAR(60) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    material VARCHAR(120) NOT NULL,
    location VARCHAR(160) NOT NULL,
    sensitivity_score INT NOT NULL,
    exhibition_score INT NOT NULL,
    CONSTRAINT ck_exhibit_f1 CHECK(sensitivity_score BETWEEN 0 AND 100),
    CONSTRAINT ck_exhibit_f3 CHECK(exhibition_score BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE specialist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(180) NOT NULL,
    specialization VARCHAR(160) NOT NULL,
    contact VARCHAR(160) NOT NULL DEFAULT ''
) ENGINE=InnoDB;

CREATE TABLE inspection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exhibit_id BIGINT NOT NULL,
    inspection_date DATE NOT NULL,
    damage_score INT NOT NULL,
    notes TEXT NOT NULL,
    CONSTRAINT fk_inspection_exhibit FOREIGN KEY (exhibit_id) REFERENCES exhibit(id) ON DELETE RESTRICT,
    CONSTRAINT ck_inspection_f2 CHECK(damage_score BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE conservation_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exhibit_id BIGINT NOT NULL,
    inspection_id BIGINT NOT NULL,
    specialist_id BIGINT NULL,
    state VARCHAR(32) NOT NULL DEFAULT 'NEW',
    f1 INT NOT NULL,
    f2 INT NOT NULL,
    f3 INT NOT NULL,
    integral_score DECIMAL(6,2) NOT NULL,
    CONSTRAINT fk_request_exhibit FOREIGN KEY (exhibit_id) REFERENCES exhibit(id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_inspection FOREIGN KEY (inspection_id) REFERENCES inspection(id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_specialist FOREIGN KEY (specialist_id) REFERENCES specialist(id) ON DELETE RESTRICT,
    CONSTRAINT ck_request_state CHECK (state IN ('NEW','ASSESSED','IN_PROGRESS','COMPLETED')),
    CONSTRAINT ck_request_f1 CHECK(f1 BETWEEN 0 AND 100),
    CONSTRAINT ck_request_f2 CHECK(f2 BETWEEN 0 AND 100),
    CONSTRAINT ck_request_f3 CHECK(f3 BETWEEN 0 AND 100)
) ENGINE=InnoDB;

-- Initial records differ from the CSV inventory numbers: importing exhibits.csv will not create duplicates.
INSERT INTO exhibit(inventory_number,title,material,location,sensitivity_score,exhibition_score) VALUES
('EX-001','Бронзова статуетка','бронза','Зала 2',50,70),
('EX-002','Дерев’яна скриня','дерево','Фондосховище',75,50);
INSERT INTO specialist(full_name,specialization,contact) VALUES
('Ірина Коваль','Реставрація кераміки','museum@example.org'),
('Олег Петренко','Консервація металів','specialist@example.org');
INSERT INTO inspection(exhibit_id,inspection_date,damage_score,notes) VALUES
(1,'2026-10-15',0,'Пошкоджень не виявлено'),
(2,'2026-10-18',45,'Є тріщина на боковій стінці');
-- Example: F1=50 F2=0 F3=70 -> I=37.50
INSERT INTO conservation_request(exhibit_id,inspection_id,specialist_id,state,f1,f2,f3,integral_score)
VALUES (1,1,2,'NEW',50,0,70,37.50);
