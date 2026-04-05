CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS consultants (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255),
    approved BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS timeslots (
    id SERIAL PRIMARY KEY,
    consultant_id INTEGER NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    booked BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL,
    consultant_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL,
    service_name VARCHAR(255),
    consultant_name VARCHAR(255),
    client_name VARCHAR(255),
    date VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'Requested',
    timeslot_id INTEGER,
    final_price DOUBLE PRECISION,
    payment_method VARCHAR(100),
    cancellation_fee DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS payment_methods (
    id VARCHAR(50) PRIMARY KEY,
    client_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    card_number VARCHAR(20),
    cvv VARCHAR(4),
    expiry_date VARCHAR(10),
    email VARCHAR(255),
    account_number VARCHAR(25),
    routing_number VARCHAR(15),
    last_four VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS payment_history (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL,
    booking_id INTEGER NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    method_type VARCHAR(100),
    transaction_id VARCHAR(100),
    status VARCHAR(50) DEFAULT 'Completed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
