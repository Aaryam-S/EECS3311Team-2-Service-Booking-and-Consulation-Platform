CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS consultants (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    consultant_id INT,
    consultant_name VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 60,
    base_price NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_services_consultant
        FOREIGN KEY (consultant_id) REFERENCES consultants(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS payment_methods (
    id VARCHAR(100) PRIMARY KEY,
    client_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    card_number VARCHAR(50),
    cvv VARCHAR(10),
    expiry_date VARCHAR(20),
    email VARCHAR(255),
    account_number VARCHAR(100),
    routing_number VARCHAR(100),
    last_four VARCHAR(20),
    CONSTRAINT fk_payment_methods_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS timeslots (
    id SERIAL PRIMARY KEY,
    consultant_id INT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    booked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_timeslots_consultant
        FOREIGN KEY (consultant_id) REFERENCES consultants(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    client_id INT NOT NULL,
    consultant_id INT NOT NULL,
    service_id INT NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    consultant_name VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    booking_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    final_price NUMERIC(10,2) DEFAULT 0,
    payment_method VARCHAR(100),
    cancellation_fee NUMERIC(10,2) DEFAULT 0,
    timeslot_id INT,
    CONSTRAINT fk_bookings_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bookings_consultant
        FOREIGN KEY (consultant_id) REFERENCES consultants(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bookings_service
        FOREIGN KEY (service_id) REFERENCES services(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bookings_timeslot
        FOREIGN KEY (timeslot_id) REFERENCES timeslots(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS system_policies (
    id INT PRIMARY KEY,
    pricing_strategy VARCHAR(100) NOT NULL,
    cancellation_policy VARCHAR(100) NOT NULL,
    cancellation_fee NUMERIC(10,2) NOT NULL,
    notifications_enabled BOOLEAN NOT NULL,
    refunds_enabled BOOLEAN NOT NULL
);