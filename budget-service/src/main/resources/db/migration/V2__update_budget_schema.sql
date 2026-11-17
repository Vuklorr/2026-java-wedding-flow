-- Добавляем курс конвертации в валюту статьи бюджета.
ALTER TABLE payment
    ADD COLUMN exchange_rate_to_item_currency NUMERIC(20, 10);

-- Для существующих платежей с одинаковой валютой
-- курс равен 1.
UPDATE payment p
SET exchange_rate_to_item_currency = 1
    FROM budget_item b
WHERE p.budget_item_id = b.id
  AND p.currency_code = b.currency_code;

-- Не допускаем автоматического присвоения неверного курса
-- старым платежам в другой валюте.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM payment
        WHERE exchange_rate_to_item_currency IS NULL
    ) THEN
        RAISE EXCEPTION
            'Для существующих платежей в другой валюте необходимо указать исторический курс';
END IF;
END
$$;

-- Ограничения для budget_item.
ALTER TABLE budget_item
    ADD CONSTRAINT chk_budget_item_name
        CHECK (LENGTH(TRIM(name)) > 0),

    ADD CONSTRAINT chk_budget_item_amount
        CHECK (planned_amount >= 0),

    ADD CONSTRAINT chk_budget_item_currency
        CHECK (currency_code ~ '^[A-Z]{3}$');

-- Ограничения для payment.
ALTER TABLE payment
    ALTER COLUMN amount SET NOT NULL,
ALTER COLUMN exchange_rate_to_item_currency SET NOT NULL,

    ADD CONSTRAINT fk_payment_budget_item
        FOREIGN KEY (budget_item_id)
        REFERENCES budget_item(id)
        ON DELETE RESTRICT,

    ADD CONSTRAINT chk_payment_amount
        CHECK (amount > 0),

    ADD CONSTRAINT chk_payment_currency
        CHECK (currency_code ~ '^[A-Z]{3}$'),

    ADD CONSTRAINT chk_payment_exchange_rate
        CHECK (exchange_rate_to_item_currency > 0);

CREATE INDEX idx_budget_item_wedding_id
    ON budget_item(wedding_id);

CREATE INDEX idx_payment_budget_item_id
    ON payment(budget_item_id);