CREATE TABLE IF NOT EXISTS products (
                          id BIGSERIAL PRIMARY KEY,
                          product_id VARCHAR(100) NOT NULL UNIQUE,
                          product_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory (
                           id BIGSERIAL PRIMARY KEY,
                           product_id BIGINT NOT NULL UNIQUE,
                           available_quantity INTEGER NOT NULL DEFAULT 0,
                           reserved_quantity INTEGER NOT NULL DEFAULT 0,
                           version BIGINT NOT NULL DEFAULT 0,

                           CONSTRAINT fk_inventory_product
                               FOREIGN KEY (product_id)
                                   REFERENCES products(id),

                           CONSTRAINT chk_available_quantity
                               CHECK (available_quantity >= 0),

                           CONSTRAINT chk_reserved_quantity
                               CHECK (reserved_quantity >= 0)
);