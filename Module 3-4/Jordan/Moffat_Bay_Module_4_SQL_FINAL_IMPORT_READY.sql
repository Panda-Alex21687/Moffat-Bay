SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS waitlist_entries;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS slips;
DROP TABLE IF EXISTS boats;
DROP TABLE IF EXISTS slip_types;
DROP TABLE IF EXISTS customers;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE customers (
    customer_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    street VARCHAR(120) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(30) NOT NULL,
    zip VARCHAR(10) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE slip_types (
    slip_type_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    size_ft DECIMAL(5,1) NOT NULL,
    total_capacity INT NOT NULL,
    rate_per_foot DECIMAL(8,2) NOT NULL,
    electric_fee DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE boats (
    boat_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_name VARCHAR(100) NOT NULL,
    boat_length_ft DECIMAL(5,1) NOT NULL,
    boat_type VARCHAR(80) NOT NULL,
    registration_number VARCHAR(80) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_boats_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
) ENGINE=InnoDB;

CREATE TABLE slips (
    slip_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    slip_type_id INT UNSIGNED NOT NULL,
    slip_number VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_slips_slip_type FOREIGN KEY (slip_type_id)
        REFERENCES slip_types(slip_type_id)
) ENGINE=InnoDB;

CREATE TABLE reservations (
    reservation_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_id INT UNSIGNED NOT NULL,
    slip_id INT UNSIGNED NOT NULL,
    check_in_date DATE NOT NULL,
    expected_term VARCHAR(30) NOT NULL,
    monthly_cost DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at DATETIME NULL,
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),
    CONSTRAINT fk_reservations_boat FOREIGN KEY (boat_id)
        REFERENCES boats(boat_id),
    CONSTRAINT fk_reservations_slip FOREIGN KEY (slip_id)
        REFERENCES slips(slip_id)
) ENGINE=InnoDB;

CREATE TABLE waitlist_entries (
    waitlist_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    boat_id INT UNSIGNED NOT NULL,
    slip_type_id INT UNSIGNED NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_waitlist_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),
    CONSTRAINT fk_waitlist_boat FOREIGN KEY (boat_id)
        REFERENCES boats(boat_id),
    CONSTRAINT fk_waitlist_slip_type FOREIGN KEY (slip_type_id)
        REFERENCES slip_types(slip_type_id)
) ENGINE=InnoDB;

CREATE TABLE email_verifications (
    verification_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id INT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verifications_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
) ENGINE=InnoDB;

INSERT INTO customers (
    customer_id, first_name, last_name, phone, street, city, state, zip,
    email, password_hash, email_verified, created_at
) VALUES
(1, 'Henry', 'Morrison', '(360) 555-0110', '12 Trawler Way', 'Westport', 'WA', '98595', 'henry.morrison@example.com', '$2y$12$x/MM5lqHV3KWQAmOdzYs7.K0E0RSjqsz7FcIcTDtJTl76c8E7LCKa', 1, '2026-08-22 08:00:00'),
(2, 'Priya', 'Sharma', '(425) 555-0111', '88 Overlook Ave', 'Bellevue', 'WA', '98004', 'priya.sharma@example.com', '$2y$12$GwJGBUrw7pakm4VhCubLRurCXduDLfiER2z5UGn4UP5l1N8b.zcuG', 1, '2026-08-22 08:10:00'),
(3, 'Emily', 'Tran', '(425) 555-0112', '215 Cedar Ridge Dr', 'Bellevue', 'WA', '98004', 'emily.tran@example.com', '$2y$12$0PorQ45So1ZOGiHIGILglu9TXW07YT4dHMTts98b3Zz0QhX6KK8VK', 1, '2026-08-22 08:20:00'),
(4, 'John', 'Ruiz', '(360) 555-0113', '40 Dockside Ln', 'Moffat Bay', 'WA', '98599', 'john.ruiz@example.com', '$2y$12$pBrBBC5drLGbHUVIVS9JjeR6C2c5lPhJB2ij/Fm9kSB931WsrqPI6', 1, '2026-08-22 08:30:00'),
(5, 'Casey', 'Jones', '(360) 555-0114', '77 Tideway Ct', 'Moffat Bay', 'WA', '98599', 'casey.jones@example.com', '$2y$12$JKuB0HXeD90du1uA9jLsduLjKt7JEXbafBVGVRRUWmaf6cMsZRQ2i', 0, '2026-08-22 08:40:00'),
(6, 'Sofia', 'Chen', '(360) 555-0115', '19 Marina View Rd', 'Moffat Bay', 'WA', '98599', 'sofia.chen@example.com', '$2y$12$6dzbfNOiOKsTHqsYpZZ0QOt0.rvItKs4S03NuTf.VSyvXgh1L27t.', 1, '2026-08-22 08:50:00');

INSERT INTO slip_types (
    slip_type_id, size_ft, total_capacity, rate_per_foot, electric_fee
) VALUES
(1, 26.0, 30, 10.00, 10.00),
(2, 40.0, 24, 10.00, 10.00),
(3, 50.0, 18, 10.00, 10.00);

INSERT INTO boats (
    boat_id, customer_id, boat_name, boat_length_ft, boat_type, registration_number, created_at
) VALUES
(1, 1, 'Reel Adventure', 48.0, 'Trawler', 'WA-MB-4801', '2026-08-22 09:00:00'),
(2, 2, 'Bellevue Breeze', 26.0, 'Sailboat', 'WA-MB-2602', '2026-08-22 09:10:00'),
(3, 3, 'Night Shift', 36.0, 'Power Boat', 'WA-MB-3603', '2026-08-22 09:20:00'),
(4, 4, 'Second Wind', 34.0, 'Cabin Cruiser', 'WA-MB-3404', '2026-08-22 09:30:00'),
(5, 5, 'Changing Tides', 45.0, 'Sailboat', 'WA-MB-4505', '2026-08-22 09:40:00'),
(6, 6, 'Island Star', 49.0, 'Motor Yacht', 'WA-MB-4906', '2026-08-22 09:50:00');

INSERT INTO slips (
    slip_id, slip_type_id, slip_number, status
) VALUES
(1, 1, 'A1', 'AVAILABLE'),
(2, 1, 'A2', 'AVAILABLE'),
(3, 1, 'A3', 'AVAILABLE'),
(4, 1, 'A4', 'AVAILABLE'),
(5, 1, 'A5', 'AVAILABLE'),
(6, 1, 'A6', 'AVAILABLE'),
(7, 1, 'A7', 'AVAILABLE'),
(8, 1, 'A8', 'RESERVED'),
(9, 1, 'A9', 'AVAILABLE'),
(10, 1, 'A10', 'AVAILABLE'),
(11, 1, 'A11', 'AVAILABLE'),
(12, 1, 'A12', 'AVAILABLE'),
(13, 1, 'A13', 'AVAILABLE'),
(14, 1, 'A14', 'AVAILABLE'),
(15, 1, 'A15', 'AVAILABLE'),
(16, 1, 'A16', 'AVAILABLE'),
(17, 1, 'A17', 'AVAILABLE'),
(18, 1, 'A18', 'AVAILABLE'),
(19, 1, 'A19', 'AVAILABLE'),
(20, 1, 'A20', 'AVAILABLE'),
(21, 1, 'A21', 'AVAILABLE'),
(22, 1, 'A22', 'AVAILABLE'),
(23, 1, 'A23', 'AVAILABLE'),
(24, 1, 'A24', 'AVAILABLE'),
(25, 1, 'A25', 'AVAILABLE'),
(26, 1, 'A26', 'AVAILABLE'),
(27, 1, 'A27', 'AVAILABLE'),
(28, 1, 'A28', 'AVAILABLE'),
(29, 1, 'A29', 'AVAILABLE'),
(30, 1, 'A30', 'AVAILABLE'),
(31, 2, 'B1', 'AVAILABLE'),
(32, 2, 'B2', 'AVAILABLE'),
(33, 2, 'B3', 'AVAILABLE'),
(34, 2, 'B4', 'RESERVED'),
(35, 2, 'B5', 'HELD'),
(36, 2, 'B6', 'AVAILABLE'),
(37, 2, 'B7', 'AVAILABLE'),
(38, 2, 'B8', 'AVAILABLE'),
(39, 2, 'B9', 'AVAILABLE'),
(40, 2, 'B10', 'AVAILABLE'),
(41, 2, 'B11', 'AVAILABLE'),
(42, 2, 'B12', 'AVAILABLE'),
(43, 2, 'B13', 'AVAILABLE'),
(44, 2, 'B14', 'AVAILABLE'),
(45, 2, 'B15', 'AVAILABLE'),
(46, 2, 'B16', 'AVAILABLE'),
(47, 2, 'B17', 'AVAILABLE'),
(48, 2, 'B18', 'AVAILABLE'),
(49, 2, 'B19', 'AVAILABLE'),
(50, 2, 'B20', 'AVAILABLE'),
(51, 2, 'B21', 'AVAILABLE'),
(52, 2, 'B22', 'AVAILABLE'),
(53, 2, 'B23', 'AVAILABLE'),
(54, 2, 'B24', 'AVAILABLE'),
(55, 3, 'C1', 'AVAILABLE'),
(56, 3, 'C2', 'RESERVED'),
(57, 3, 'C3', 'RESERVED'),
(58, 3, 'C4', 'RESERVED'),
(59, 3, 'C5', 'RESERVED'),
(60, 3, 'C6', 'RESERVED'),
(61, 3, 'C7', 'RESERVED'),
(62, 3, 'C8', 'RESERVED'),
(63, 3, 'C9', 'RESERVED'),
(64, 3, 'C10', 'RESERVED'),
(65, 3, 'C11', 'RESERVED'),
(66, 3, 'C12', 'RESERVED'),
(67, 3, 'C13', 'RESERVED'),
(68, 3, 'C14', 'RESERVED'),
(69, 3, 'C15', 'RESERVED'),
(70, 3, 'C16', 'RESERVED'),
(71, 3, 'C17', 'RESERVED'),
(72, 3, 'C18', 'RESERVED');

INSERT INTO reservations (
    reservation_id, customer_id, boat_id, slip_id, check_in_date,
    expected_term, monthly_cost, status, created_at, cancelled_at
) VALUES
(1, 2, 2, 8, '2026-09-01', '12 months', 270.00, 'CONFIRMED', '2026-08-22 10:00:00', NULL),
(2, 3, 3, 34, '2026-09-15', '6 months', 370.00, 'CONFIRMED', '2026-08-22 10:10:00', NULL),
(3, 4, 4, 35, '2026-10-01', '6 months', 350.00, 'PENDING', '2026-08-22 10:20:00', NULL),
(4, 5, 5, 55, '2026-10-15', '3 months', 460.00, 'CANCELLED', '2026-08-22 10:30:00', '2026-08-24 15:00:00');

INSERT INTO waitlist_entries (
    waitlist_id, customer_id, boat_id, slip_type_id, joined_at, status
) VALUES
(1, 1, 1, 3, '2026-08-22 08:00:00', 'WAITING'),
(2, 5, 5, 3, '2026-08-25 09:00:00', 'WAITING'),
(3, 6, 6, 3, '2026-08-25 09:30:00', 'WAITING');

INSERT INTO email_verifications (
    verification_id, customer_id, token_hash, expires_at, verified_at, created_at
) VALUES
(1, 1, '26459e4c19e543e1c92e91f107f73e6f18c3fea788805a3b8dc6d8d6b45451af', '2026-09-05 09:00:00', '2026-08-22 10:00:00', '2026-08-22 09:00:00'),
(2, 2, '03d58b3ad60e1af22d9e3275e61096ed0c92213dafc6887d68b56d546b7a203c', '2026-09-05 09:00:00', '2026-08-22 10:05:00', '2026-08-22 09:05:00'),
(3, 3, '390fd38b9a2122dabc915ac68b7622e2687bc8ea0688e5d5195babd70fd4e3eb', '2026-09-05 09:00:00', '2026-08-22 10:10:00', '2026-08-22 09:10:00'),
(4, 4, '4423c0924636275ea94521470096aa5fd1faf9df33b42fbffb3a7634339c1b19', '2026-09-05 09:00:00', '2026-08-22 10:15:00', '2026-08-22 09:15:00'),
(5, 5, '2cd416ca44e6e7d18b11b67870c8232ec3edcef4f7ac97bae484ceb36dca059c', '2026-09-05 09:00:00', NULL, '2026-08-22 09:20:00'),
(6, 6, 'bf4947eb5f45835876ee386ff9748ea6947c9623f29bd370a757a4512c74ad9f', '2026-09-05 09:00:00', '2026-08-22 10:25:00', '2026-08-22 09:25:00');

SELECT * FROM customers;
SELECT * FROM boats;
SELECT * FROM slip_types;
SELECT * FROM slips;
SELECT * FROM reservations;
SELECT * FROM waitlist_entries;
SELECT * FROM email_verifications;
