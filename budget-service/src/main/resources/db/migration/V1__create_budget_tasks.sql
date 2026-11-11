CREATE TABLE budget_item (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    wedding_id INTEGER NOT NULL,
    name VARCHAR NOT NULL,
    planned_amount NUMERIC(15, 2) DEFAULT 0 NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE payment (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    budget_item_id INTEGER NOT NULL,
    amount NUMERIC(15, 2),
    currency_code VARCHAR(3) NOT NULL,
    payment_date TIMESTAMPTZ DEFAULT now() NOT NULL
);