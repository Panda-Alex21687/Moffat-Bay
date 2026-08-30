-- ============================================================
-- Moffat Bay Marina - Green Team Database
-- CSD 460 Module 4
-- Converted from the GreenTeamDataBaseIdea Java/JDBC prototype
-- for import into localhost/phpMyAdmin.
--
-- IMPORTANT:
-- Demo passwords below are stored in plain text in the
-- password_hash column because the source prototype has not
-- implemented password hashing yet. Do NOT use these passwords
-- in a real production environment.
-- ============================================================

CREATE DATABASE IF NOT EXISTS moffat_bay
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE moffat_bay;

-- ------------------------------------------------------------
-- DEVELOPMENT RESET
-- Remove these DROP statements if you do not want an import to
-- replace existing Moffat Bay tables/data.
-- ------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS waitlist_entries;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS slips;
DROP TABLE IF EXISTS boats;
DROP TABLE IF EXISTS slip_types;
DROP TABLE IF EXISTS customers;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- TABLE: customers
-- ============================================================

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

-- ============================================================
-- TABLE: slip_types
-- ============================================================

CREATE TABLE slip_types (
    slip_type_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    size_ft INT NOT NULL,
    total_capacity INT NOT NULL,
    rate_per_foot DECIMAL(8,2) NOT NULL,
    electric_fee DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: boats
-- ============================================================

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

-- ============================================================
-- TABLE: slips
-- ============================================================

CREATE TABLE slips (
    slip_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    slip_type_id INT UNSIGNED NOT NULL,
    slip_number VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,

    CONSTRAINT fk_slips_slip_type
        FOREIGN KEY (slip_type_id)
        REFERENCES slip_types(slip_type_id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: reservations
-- ============================================================

CREATE TABLE reservations (
    reservation_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_id INT UNSIGNED NOT NULL,
    slip_id INT UNSIGNED NOT NULL,
    check_in_date DATE NOT NULL,
    expected_term VARCHAR(30) NOT NULL,
    monthly_cost DECIMAL(10,2) NOT NULL,
    notes TEXT,
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

-- ============================================================
-- TABLE: waitlist_entries
-- ============================================================

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

-- ============================================================
-- TABLE: email_verifications
-- ============================================================

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

-- ============================================================
-- SEED DATA: slip_types
-- $10 per foot + $10 electric fee
-- ============================================================

INSERT INTO slip_types
    (size_ft, total_capacity, rate_per_foot, electric_fee)
VALUES
    (26, 30, 10.00, 10.00),
    (40, 24, 10.00, 10.00),
    (50, 18, 10.00, 10.00);

-- ============================================================
-- SEED DATA: customers
-- WARNING: passwords are plain text demo values.
-- ============================================================

INSERT INTO customers
    (
        first_name,
        last_name,
        phone,
        street,
        city,
        state,
        zip,
        email,
        password_hash,
        email_verified
    )
VALUES
    (
        'Henry',
        'Morrison',
        '(360) 555-0110',
        '12 Trawler Way',
        'Westport',
        'WA',
        '98595',
        'henry.morrison@example.com',
        'CaptainH2026!',
        TRUE
    ),
    (
        'Priya',
        'Sharma',
        '(425) 555-0111',
        '88 Overlook Ave',
        'Bellevue',
        'WA',
        '98004',
        'priya.sharma@example.com',
        'PriyaPM2026!',
        TRUE
    ),
    (
        'Emily',
        'Tran',
        '(425) 555-0112',
        '215 Cedar Ridge Dr',
        'Bellevue',
        'WA',
        '98004',
        'emily.tran@example.com',
        'NurseEmily26!',
        TRUE
    ),
    (
        'John',
        'Ruiz',
        '(360) 555-0113',
        '40 Dockside Ln',
        'Moffat Bay',
        'WA',
        '98599',
        'john.ruiz@example.com',
        'JohnSail26!',
        TRUE
    ),
    (
        'Casey',
        'Jones',
        '(360) 555-0114',
        '77 Tideway Ct',
        'Moffat Bay',
        'WA',
        '98599',
        'casey.jones@example.com',
        'CaseyTide26!',
        FALSE
    );

-- ============================================================
-- SEED DATA: boats
-- ============================================================

INSERT INTO boats
    (
        customer_id,
        boat_name,
        boat_length_ft,
        boat_type,
        registration_number
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'henry.morrison@example.com'),
        'Reel Adventure',
        48.0,
        'Trawler',
        'WA-MB-4801'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'priya.sharma@example.com'),
        'Bellevue Breeze',
        26.0,
        'Sailboat',
        'WA-MB-2602'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'emily.tran@example.com'),
        'Night Shift',
        36.0,
        'Power Boat',
        'WA-MB-3603'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'john.ruiz@example.com'),
        'Second Wind',
        34.0,
        'Cabin Cruiser',
        'WA-MB-3404'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'casey.jones@example.com'),
        'Changing Tides',
        45.0,
        'Sailboat',
        'WA-MB-4505'
    );

-- ============================================================
-- SEED DATA: slips
-- ============================================================

INSERT INTO slips
    (slip_type_id, slip_number, status)
VALUES
    (
        (SELECT slip_type_id FROM slip_types WHERE size_ft = 26 LIMIT 1),
        'A8',
        'RESERVED'
    ),
    (
        (SELECT slip_type_id FROM slip_types WHERE size_ft = 26 LIMIT 1),
        'A9',
        'AVAILABLE'
    ),
    (
        (SELECT slip_type_id FROM slip_types WHERE size_ft = 40 LIMIT 1),
        'B4',
        'RESERVED'
    ),
    (
        (SELECT slip_type_id FROM slip_types WHERE size_ft = 40 LIMIT 1),
        'B5',
        'HELD'
    ),
    (
        (SELECT slip_type_id FROM slip_types WHERE size_ft = 50 LIMIT 1),
        'C1',
        'AVAILABLE'
    ),
    (
        (SELECT slip_type_id FROM slip_types WHERE size_ft = 50 LIMIT 1),
        'C2',
        'AVAILABLE'
    );

-- ============================================================
-- SEED DATA: reservations
-- ============================================================

-- Priya: registered and reserved in one session.
INSERT INTO reservations
    (
        customer_id,
        boat_id,
        slip_id,
        check_in_date,
        expected_term,
        monthly_cost,
        notes,
        status,
        cancelled_at
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'priya.sharma@example.com'),
        (SELECT boat_id FROM boats
         WHERE registration_number = 'WA-MB-2602'
         LIMIT 1),
        (SELECT slip_id FROM slips
         WHERE slip_number = 'A8'),
        '2026-09-01',
        '12 months',
        270.00,
        'Registered and reserved in a single session.',
        'CONFIRMED',
        NULL
    );

-- Emily: returning customer with an existing reservation.
INSERT INTO reservations
    (
        customer_id,
        boat_id,
        slip_id,
        check_in_date,
        expected_term,
        monthly_cost,
        notes,
        status,
        cancelled_at
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'emily.tran@example.com'),
        (SELECT boat_id FROM boats
         WHERE registration_number = 'WA-MB-3603'
         LIMIT 1),
        (SELECT slip_id FROM slips
         WHERE slip_number = 'B4'),
        '2026-09-15',
        '6 months',
        370.00,
        'Returning customer; slip was already reserved on a prior visit.',
        'CONFIRMED',
        NULL
    );

-- John: reservation is still pending.
INSERT INTO reservations
    (
        customer_id,
        boat_id,
        slip_id,
        check_in_date,
        expected_term,
        monthly_cost,
        notes,
        status,
        cancelled_at
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'john.ruiz@example.com'),
        (SELECT boat_id FROM boats
         WHERE registration_number = 'WA-MB-3404'
         LIMIT 1),
        (SELECT slip_id FROM slips
         WHERE slip_number = 'B5'),
        '2026-10-01',
        '6 months',
        350.00,
        'Awaiting payment confirmation before the slip is finalized.',
        'PENDING',
        NULL
    );

-- Casey: cancelled after a change of plans.
INSERT INTO reservations
    (
        customer_id,
        boat_id,
        slip_id,
        check_in_date,
        expected_term,
        monthly_cost,
        notes,
        status,
        cancelled_at
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'casey.jones@example.com'),
        (SELECT boat_id FROM boats
         WHERE registration_number = 'WA-MB-4505'
         LIMIT 1),
        (SELECT slip_id FROM slips
         WHERE slip_number = 'C1'),
        '2026-10-15',
        '3 months',
        460.00,
        'Customer cancelled after a change of plans.',
        'CANCELLED',
        '2026-08-24 15:00:00'
    );

-- ============================================================
-- SEED DATA: waitlist_entries
-- ============================================================

-- Henry: requested a 50 ft slip and joined the wait list.
INSERT INTO waitlist_entries
    (
        customer_id,
        boat_id,
        slip_type_id,
        status
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'henry.morrison@example.com'),
        (SELECT boat_id FROM boats
         WHERE registration_number = 'WA-MB-4801'
         LIMIT 1),
        (SELECT slip_type_id FROM slip_types
         WHERE size_ft = 50
         LIMIT 1),
        'WAITING'
    );

-- Casey: cancelled reservation, then joined the wait list.
INSERT INTO waitlist_entries
    (
        customer_id,
        boat_id,
        slip_type_id,
        status
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'casey.jones@example.com'),
        (SELECT boat_id FROM boats
         WHERE registration_number = 'WA-MB-4505'
         LIMIT 1),
        (SELECT slip_type_id FROM slip_types
         WHERE size_ft = 50
         LIMIT 1),
        'CONTACTED'
    );

-- ============================================================
-- SEED DATA: email_verifications
-- ============================================================

INSERT INTO email_verifications
    (
        customer_id,
        token_hash,
        expires_at,
        verified_at
    )
VALUES
    (
        (SELECT customer_id FROM customers
         WHERE email = 'henry.morrison@example.com'),
        'verify-henry-demo-token',
        '2026-09-05 09:00:00',
        '2026-08-22 10:00:00'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'priya.sharma@example.com'),
        'verify-priya-demo-token',
        '2026-09-05 09:00:00',
        '2026-08-22 10:00:00'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'emily.tran@example.com'),
        'verify-emily-demo-token',
        '2026-09-05 09:00:00',
        '2026-08-22 10:00:00'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'john.ruiz@example.com'),
        'verify-john-demo-token',
        '2026-09-05 09:00:00',
        '2026-08-22 10:00:00'
    ),
    (
        (SELECT customer_id FROM customers
         WHERE email = 'casey.jones@example.com'),
        'verify-casey-demo-token',
        '2026-09-05 09:00:00',
        NULL
    );

-- ============================================================
-- QUICK CHECKS
-- These SELECT statements make it easy to verify the import
-- inside phpMyAdmin.
-- ============================================================

SELECT 'customers' AS table_name, COUNT(*) AS row_count FROM customers
UNION ALL
SELECT 'slip_types', COUNT(*) FROM slip_types
UNION ALL
SELECT 'boats', COUNT(*) FROM boats
UNION ALL
SELECT 'slips', COUNT(*) FROM slips
UNION ALL
SELECT 'reservations', COUNT(*) FROM reservations
UNION ALL
SELECT 'waitlist_entries', COUNT(*) FROM waitlist_entries
UNION ALL
SELECT 'email_verifications', COUNT(*) FROM email_verifications;

-- Expected row counts:
-- customers:              5
-- slip_types:             3
-- boats:                  5
-- slips:                  6
-- reservations:           4
-- waitlist_entries:       2
-- email_verifications:    5
