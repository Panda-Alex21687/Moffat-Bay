CREATE DATABASE IF NOT EXISTS moffat_bay
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE moffat_bay;

CREATE TABLE IF NOT EXISTS customers (
  customer_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(60) NOT NULL,
  last_name VARCHAR(60) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  street VARCHAR(120) NOT NULL,
  city VARCHAR(80) NOT NULL,
  state CHAR(2) NOT NULL,
  zip VARCHAR(10) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS boats (
  boat_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  customer_id INT UNSIGNED NOT NULL,
  boat_name VARCHAR(100) NOT NULL,
  boat_length_ft DECIMAL(6,1) NOT NULL,
  boat_type VARCHAR(60) NULL,
  registration_number VARCHAR(80) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_boats_customer
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservations (
  reservation_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  customer_id INT UNSIGNED NOT NULL,
  boat_id INT UNSIGNED NOT NULL,
  start_date DATE NOT NULL,
  expected_term VARCHAR(30) NOT NULL,
  notes TEXT NULL,
  monthly_estimate DECIMAL(10,2) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'Requested',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_reservations_customer
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_reservations_boat
    FOREIGN KEY (boat_id) REFERENCES boats(boat_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_boats_customer ON boats(customer_id);
CREATE INDEX idx_reservations_customer ON reservations(customer_id);
CREATE INDEX idx_reservations_boat ON reservations(boat_id);
