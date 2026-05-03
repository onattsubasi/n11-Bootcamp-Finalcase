CREATE TABLE orders (
                        id UUID PRIMARY KEY,

                        order_number VARCHAR(80) NOT NULL UNIQUE,
                        checkout_id UUID NOT NULL UNIQUE,
                        idempotency_key VARCHAR(120),
                        request_hash VARCHAR(128) NOT NULL,

                        user_id UUID NOT NULL,
                        basket_id UUID,
                        inventory_reservation_id UUID,
                        promotion_usage_reservation_id UUID,

                        status VARCHAR(50) NOT NULL,

                        shipping_recipient_name VARCHAR(150),
                        shipping_recipient_phone VARCHAR(30),
                        shipping_country VARCHAR(100),
                        shipping_city VARCHAR(100),
                        shipping_district VARCHAR(100),
                        shipping_neighborhood VARCHAR(150),
                        shipping_address_line_1 VARCHAR(500),
                        shipping_address_line_2 VARCHAR(500),
                        shipping_postal_code VARCHAR(20),

                        billing_recipient_name VARCHAR(150),
                        billing_recipient_phone VARCHAR(30),
                        billing_country VARCHAR(100),
                        billing_city VARCHAR(100),
                        billing_district VARCHAR(100),
                        billing_neighborhood VARCHAR(150),
                        billing_address_line_1 VARCHAR(500),
                        billing_address_line_2 VARCHAR(500),
                        billing_postal_code VARCHAR(20),

                        payment_id UUID,
                        payment_provider VARCHAR(50),
                        payment_status VARCHAR(50),
                        provider_transaction_id VARCHAR(150),

                        shipment_id UUID,
                        shipment_number VARCHAR(80),
                        carrier VARCHAR(80),
                        tracking_number VARCHAR(150),
                        shipment_status VARCHAR(50),
                        shipped_at TIMESTAMP WITH TIME ZONE,
                        delivered_at TIMESTAMP WITH TIME ZONE,

                        subtotal_amount NUMERIC(19, 2) NOT NULL,
                        item_discount_amount NUMERIC(19, 2) NOT NULL,
                        promotion_discount_amount NUMERIC(19, 2) NOT NULL,
                        shipping_fee NUMERIC(19, 2) NOT NULL,
                        shipping_discount_amount NUMERIC(19, 2) NOT NULL,
                        tax_amount NUMERIC(19, 2) NOT NULL,
                        grand_total_amount NUMERIC(19, 2) NOT NULL,
                        currency VARCHAR(3) NOT NULL,

                        version BIGINT,

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_order_number
    ON orders(order_number);

CREATE INDEX idx_orders_checkout_id
    ON orders(checkout_id);

CREATE INDEX idx_orders_idempotency_key
    ON orders(idempotency_key);

CREATE INDEX idx_orders_user_id
    ON orders(user_id);

CREATE INDEX idx_orders_status
    ON orders(status);


CREATE TABLE order_items (
                             id UUID PRIMARY KEY,
                             order_id UUID NOT NULL,

                             product_id VARCHAR(100) NOT NULL,
                             sku VARCHAR(120) NOT NULL,
                             product_name VARCHAR(250) NOT NULL,
                             slug VARCHAR(180) NOT NULL,
                             main_image_url VARCHAR(1000),

                             brand_id VARCHAR(100),
                             brand_name VARCHAR(150),
                             category_id VARCHAR(100),
                             category_name VARCHAR(150),

                             unit_price NUMERIC(19, 2) NOT NULL,
                             quantity INTEGER NOT NULL,
                             line_subtotal NUMERIC(19, 2) NOT NULL,
                             line_discount NUMERIC(19, 2) NOT NULL,
                             line_total NUMERIC(19, 2) NOT NULL,
                             currency VARCHAR(3) NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);


CREATE TABLE order_discounts (
                                 id UUID PRIMARY KEY,
                                 order_id UUID NOT NULL,

                                 promotion_id UUID,
                                 promotion_name VARCHAR(200),
                                 coupon_id UUID,
                                 coupon_code VARCHAR(80),

                                 discount_amount NUMERIC(19, 2) NOT NULL,
                                 shipping_discount_amount NUMERIC(19, 2) NOT NULL,

                                 CONSTRAINT fk_order_discounts_order
                                     FOREIGN KEY (order_id)
                                         REFERENCES orders(id)
                                         ON DELETE CASCADE
);

CREATE INDEX idx_order_discounts_order_id
    ON order_discounts(order_id);

CREATE INDEX idx_order_discounts_promotion_id
    ON order_discounts(promotion_id);

CREATE INDEX idx_order_discounts_coupon_code
    ON order_discounts(coupon_code);


CREATE TABLE order_status_history (
                                      id UUID PRIMARY KEY,
                                      order_id UUID NOT NULL,

                                      from_status VARCHAR(50),
                                      to_status VARCHAR(50) NOT NULL,
                                      source VARCHAR(50) NOT NULL,
                                      changed_by VARCHAR(100),
                                      reason VARCHAR(500),
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                      CONSTRAINT fk_order_status_history_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
                                              ON DELETE CASCADE
);

CREATE INDEX idx_order_status_history_order_id
    ON order_status_history(order_id);

CREATE INDEX idx_order_status_history_created_at
    ON order_status_history(created_at);