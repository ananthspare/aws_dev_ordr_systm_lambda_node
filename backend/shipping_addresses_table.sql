-- Shipping Addresses Table
CREATE TABLE shipping_addresses (
  shipping_address_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  customer_id         BIGINT UNSIGNED NOT NULL,
  full_name          VARCHAR(150) NOT NULL,
  address_line1      VARCHAR(255) NOT NULL,
  address_line2      VARCHAR(255) NULL,
  city               VARCHAR(100) NOT NULL,
  state              VARCHAR(100) NOT NULL,
  postal_code        VARCHAR(20) NOT NULL,
  country_code       CHAR(2) NOT NULL,
  phone_number       VARCHAR(20) NULL,
  is_default         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (shipping_address_id),
  KEY idx_shipping_addresses_customer (customer_id),
  CONSTRAINT fk_shipping_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) ENGINE=InnoDB;

-- Add shipping_address_id to orders table
ALTER TABLE orders ADD COLUMN shipping_address_id BIGINT UNSIGNED NULL AFTER country_code;
ALTER TABLE orders ADD CONSTRAINT fk_orders_shipping_address FOREIGN KEY (shipping_address_id) REFERENCES shipping_addresses(shipping_address_id);