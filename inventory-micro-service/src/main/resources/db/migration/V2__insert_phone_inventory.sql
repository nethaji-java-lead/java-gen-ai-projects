INSERT INTO products (product_id, product_name)
VALUES
    ('IPHONE-15', 'Apple iPhone 15'),
    ('IPHONE-15-PRO', 'Apple iPhone 15 Pro'),
    ('IPHONE-16', 'Apple iPhone 16'),
    ('IPHONE-16-PRO', 'Apple iPhone 16 Pro'),
    ('SAMSUNG-S24', 'Samsung Galaxy S24'),
    ('SAMSUNG-S24-ULTRA', 'Samsung Galaxy S24 Ultra'),
    ('GOOGLE-PIXEL-9', 'Google Pixel 9'),
    ('ONEPLUS-12', 'OnePlus 12');


INSERT INTO inventory (
    product_id,
    available_quantity,
    reserved_quantity,
    version
)
SELECT
    p.id,
    v.available_quantity,
    0,
    0
FROM products p
         JOIN (
    VALUES
        ('IPHONE-15', 50),
        ('IPHONE-15-PRO', 30),
        ('IPHONE-16', 40),
        ('IPHONE-16-PRO', 25),
        ('SAMSUNG-S24', 60),
        ('SAMSUNG-S24-ULTRA', 35),
        ('GOOGLE-PIXEL-9', 45),
        ('ONEPLUS-12', 55)
) AS v(product_id, available_quantity)
              ON p.product_id = v.product_id;