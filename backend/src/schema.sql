CREATE TABLE IF NOT EXISTS clients (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,
    email    VARCHAR(150) NOT NULL,
    name     VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS consultants (
    id        SERIAL PRIMARY KEY,
    username  VARCHAR(100) UNIQUE NOT NULL,
    password  VARCHAR(200) NOT NULL,
    email     VARCHAR(150) NOT NULL,
    name      VARCHAR(150) NOT NULL,
    specialty VARCHAR(200),
    approved  BOOLEAN NOT NULL DEFAULT FALSE,
    status    VARCHAR(50) NOT NULL DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS timeslots (
    id            SERIAL PRIMARY KEY,
    consultant_id INT NOT NULL REFERENCES consultants(id) ON DELETE CASCADE,
    date          DATE NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    booked        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS bookings (
    id               SERIAL PRIMARY KEY,
    client_id        INT NOT NULL REFERENCES clients(id),
    consultant_id    INT NOT NULL REFERENCES consultants(id),
    service_id       INT NOT NULL,
    service_name     VARCHAR(200),
    consultant_name  VARCHAR(150),
    timeslot_id      INT REFERENCES timeslots(id),
    date             VARCHAR(50),
    status           VARCHAR(50) NOT NULL DEFAULT 'Requested',
    final_price      DOUBLE PRECISION,
    cancellation_fee DOUBLE PRECISION,
    payment_method   VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS payment_methods (
    id             VARCHAR(50) PRIMARY KEY,
    client_id      INT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    type           VARCHAR(50) NOT NULL,
    card_number    VARCHAR(20),
    cvv            VARCHAR(10),
    expiry_date    VARCHAR(20),
    last_four      VARCHAR(10),
    email          VARCHAR(150),
    account_number VARCHAR(50),
    routing_number VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS payment_history (
    id             SERIAL PRIMARY KEY,
    client_id      INT NOT NULL REFERENCES clients(id),
    booking_id     INT REFERENCES bookings(id),
    amount         DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(100),
    type           VARCHAR(50),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);
