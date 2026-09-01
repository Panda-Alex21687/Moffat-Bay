
-- Moffat Bay Marina
-- Green Team - CSD 460
-- Alexander Baldree
-- Max Jankowski
-- Aftabur Rahman
-- Jordan Dardar

-- Module 4 SQL file to match Module 3 ERD 



CREATE DATABASE IF NOT EXISTS moffat_bay
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE moffat_bay;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS waitlist_entries;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS slips;
DROP TABLE IF EXISTS boats;
DROP TABLE IF EXISTS slip_types;
DROP TABLE IF EXISTS customers;

SET FOREIGN_KEY_CHECKS = 1;


-- CUSTOMERS Table

CREATE TABLE customers (
    customer_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    street VARCHAR(120) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state CHAR(2) NOT NULL,
    zip VARCHAR(10) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


--  SLIP TYPES

CREATE TABLE slip_types (
    slip_type_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    size_ft DECIMAL(5,1) NOT NULL,
    total_capacity INT NOT NULL,
    rate_per_foot DECIMAL(8,2) NOT NULL,
    electric_fee DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB;


-- BOATS Table 

CREATE TABLE boats (
    boat_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_name VARCHAR(100) NOT NULL,
    boat_length_ft DECIMAL(6,1) NOT NULL,
    boat_type VARCHAR(60),
    registration_number VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_boats_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
) ENGINE=InnoDB;


-- SLIPS

CREATE TABLE slips (
    slip_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    slip_type_id INT UNSIGNED NOT NULL,
    slip_number VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_slips_slip_type
        FOREIGN KEY (slip_type_id)
        REFERENCES slip_types(slip_type_id)
) ENGINE=InnoDB;


-- RESERVATIONS Table


CREATE TABLE reservations (
    reservation_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_id INT UNSIGNED NOT NULL,
    slip_id INT UNSIGNED NOT NULL,
    check_in_date DATE NOT NULL,
    expected_term VARCHAR(30) NOT NULL,
    monthly_cost DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL,
    CONSTRAINT fk_reservations_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),
    CONSTRAINT fk_reservations_boat
        FOREIGN KEY (boat_id)
        REFERENCES boats(boat_id),
    CONSTRAINT fk_reservations_slip
        FOREIGN KEY (slip_id)
        REFERENCES slips(slip_id)
) ENGINE=InnoDB;


-- WAITLIST_ENTRY table

CREATE TABLE waitlist_entries (
    waitlist_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_id INT UNSIGNED NOT NULL,
    slip_type_id INT UNSIGNED NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_waitlist_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),
    CONSTRAINT fk_waitlist_boat
        FOREIGN KEY (boat_id)
        REFERENCES boats(boat_id),
    CONSTRAINT fk_waitlist_slip_type
        FOREIGN KEY (slip_type_id)
        REFERENCES slip_types(slip_type_id)
) ENGINE=InnoDB;


-- EMAIL_VERIFICATIONS Table using plain text 
 
CREATE TABLE email_verifications (
    verification_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verifications_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
) ENGINE=InnoDB;


-- SEED DATA
-- CUSTOMERS - 5 demo accounts
INSERT INTO customers
    (first_name, last_name, phone, street, city, state, zip,
     email, password_hash, email_verified)
VALUES
    ('Henry', 'Morrison', '(360) 555-0110', '12 Trawler Way',
     'Westport', 'WA', '98595', 'henry.morrison@example.com',
     'CaptainH2026!', TRUE),

    ('Priya', 'Sharma', '(425) 555-0111', '88 Overlook Ave',
     'Bellevue', 'WA', '98004', 'priya.sharma@example.com',
     'PriyaPM2026!', TRUE),

    ('Emily', 'Tran', '(425) 555-0112', '215 Cedar Ridge Dr',
     'Bellevue', 'WA', '98004', 'emily.tran@example.com',
     'NurseEmily26!', TRUE),

    ('John', 'Ruiz', '(360) 555-0113', '40 Dockside Ln',
     'Moffat Bay', 'WA', '98599', 'john.ruiz@example.com',
     'JohnSail26!', TRUE),

    ('Casey', 'Jones', '(360) 555-0114', '77 Tideway Ct',
     'Moffat Bay', 'WA', '98599', 'casey.jones@example.com',
     'CaseyTide26!', FALSE);


-- SLIP_TYPES - the 26 ft, 40 ft, and 50 ft 
INSERT INTO slip_types
    (size_ft, total_capacity, rate_per_foot, electric_fee)
VALUES
    (26.0, 30, 10.00, 10.00),
    (40.0, 24, 10.00, 10.00),
    (50.0, 18, 10.00, 10.00);


-- BOATS - one boat per customer for now 

INSERT INTO boats
    (customer_id, boat_name, boat_length_ft, boat_type, registration_number)
VALUES
    (1, 'Reel Adventure', 48.0, 'Trawler', 'WA-MB-4801'),
    (2, 'Bellevue Breeze', 26.0, 'Sailboat', 'WA-MB-2602'),
    (3, 'Night Shift', 36.0, 'Power Boat', 'WA-MB-3603'),
    (4, 'Second Wind', 34.0, 'Cabin Cruiser', 'WA-MB-3404'),
    (5, 'Changing Tides', 45.0, 'Sailboat', 'WA-MB-4505');

-
-- SLIPS - with map matching addresses 

INSERT INTO slips
    (slip_type_id, slip_number, status)
VALUES
    (1, 'A8', 'RESERVED'),
    (1, 'A9', 'AVAILABLE'),
    (2, 'B4', 'RESERVED'),
    (2, 'B5', 'HELD'),
    (3, 'C1', 'AVAILABLE'),
    (3, 'C2', 'AVAILABLE');


-- RESERVATIONS, includes poor Henry with no slip yet, we need to fill in 50 ft slips to demo his story 

INSERT INTO reservations
    (customer_id, boat_id, slip_id, check_in_date, expected_term,
     monthly_cost, status, cancelled_at)
VALUES
    (2, 2, 1, '2026-09-01', '12 months',
     270.00, 'CONFIRMED', NULL),

    (3, 3, 3, '2026-09-15', '6 months',
     370.00, 'CONFIRMED', NULL),

    (4, 4, 4, '2026-10-01', '6 months',
     350.00, 'PENDING', NULL),

    (5, 5, 5, '2026-10-15', '3 months',
     460.00, 'CANCELLED', '2026-08-24 15:00:00');


-- WAITLIST_ENTRIES 

INSERT INTO waitlist_entries
    (customer_id, boat_id, slip_type_id, status)
VALUES
    (1, 1, 3, 'WAITING'),
    (5, 5, 3, 'CONTACTED');


-- EMAIL_VERIFICATIONS 
INSERT INTO email_verifications
    (customer_id, token_hash, expires_at, verified_at)
VALUES
    (1, 'verify-henry-demo-token', '2026-09-05 09:00:00', '2026-08-22 10:00:00'),
    (2, 'verify-priya-demo-token', '2026-09-05 09:00:00', '2026-08-22 10:00:00'),
    (3, 'verify-emily-demo-token', '2026-09-05 09:00:00', '2026-08-22 10:00:00'),
    (4, 'verify-john-demo-token', '2026-09-05 09:00:00', '2026-08-22 10:00:00'),
    (5, 'verify-casey-demo-token', '2026-09-05 09:00:00', NULL);


-- Here are the queries for the Screen shot requirments 

SELECT * FROM customers;
SELECT * FROM boats;
SELECT * FROM slip_types;
SELECT * FROM slips;
SELECT * FROM reservations;
SELECT * FROM waitlist_entries;
SELECT * FROM email_verifications;
