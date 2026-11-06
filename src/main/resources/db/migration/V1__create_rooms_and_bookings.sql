
-- =====================================================
-- V1: Creación de habitaciones y reservas
-- =====================================================

-- Necesaria para utilizar igualdad (=) sobre bigint
-- dentro de índices GiST.
CREATE EXTENSION IF NOT EXISTS btree_gist;


-- =====================================================
-- TABLA: rooms
-- =====================================================

CREATE TABLE rooms (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    number VARCHAR(10) NOT NULL UNIQUE,

    type VARCHAR(20) NOT NULL
        CHECK (type IN ('SINGLE', 'DOUBLE', 'SUITE')),

    capacity SMALLINT NOT NULL
        CHECK (capacity > 0),

    price_per_night NUMERIC(10,2) NOT NULL
        CHECK (price_per_night > 0),

    active BOOLEAN NOT NULL DEFAULT TRUE
);


-- =====================================================
-- TABLA: bookings
-- =====================================================

CREATE TABLE bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    room_id BIGINT NOT NULL
        REFERENCES rooms (id),

    guest_name VARCHAR(255) NOT NULL,

    guest_email VARCHAR(255) NOT NULL,

    check_in DATE NOT NULL,

    check_out DATE NOT NULL,

    guests SMALLINT NOT NULL
        CHECK (guests > 0),

    total_price NUMERIC(10,2) NOT NULL
        CHECK (total_price > 0),

    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
        CHECK (status IN ('CONFIRMED', 'CANCELLED')),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    version BIGINT NOT NULL DEFAULT 0,

    -- La salida debe ser posterior a la entrada.
    CONSTRAINT bookings_valid_dates
        CHECK (check_out > check_in)
);


-- =====================================================
-- RESTRICCIÓN: evitar reservas solapadas
-- =====================================================

ALTER TABLE bookings
ADD CONSTRAINT bookings_no_overlap
EXCLUDE USING GIST (
    room_id WITH =,
    daterange(check_in, check_out, '[)') WITH &&
)
WHERE (status = 'CONFIRMED');
