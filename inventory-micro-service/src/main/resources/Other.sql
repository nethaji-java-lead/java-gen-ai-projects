SELECT
    i.id AS inventory_id,
    p.id AS product_id,
    p.product_id AS product_code,
    p.product_name,
    i.available_quantity,
    i.reserved_quantity,
    i.version
FROM inventory i
         JOIN products p
              ON i.product_id = p.id
ORDER BY i.id;