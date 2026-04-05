INSERT INTO system_policies (
    id,
    pricing_strategy,
    cancellation_policy,
    cancellation_fee,
    notifications_enabled,
    refunds_enabled
)

INSERT INTO services (id, name, consultant_id, consultant_name, duration_minutes, base_price)
VALUES
(1, 'Tax Consultation', 1, 'John Doe', 60, 150.00),
(2, 'Career Coaching', 2, 'Sarah Lee', 45, 100.00),
(3, 'Legal Advice', 5, 'Alice Johnson', 30, 200.00)
ON CONFLICT (id) DO NOTHING;

VALUES (
    1,
    'BasePrice',
    'Flexible',
    20.00,
    TRUE,
    TRUE
)
ON CONFLICT (id) DO NOTHING;