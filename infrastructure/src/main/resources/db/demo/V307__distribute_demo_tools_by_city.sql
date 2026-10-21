-- Local demonstration data. Loaded only when the demo profile is active.
UPDATE tools
SET service_city = CASE
    WHEN tool_id IN (
        '20000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000009',
        '20000000-0000-0000-0000-000000000013',
        '20000000-0000-0000-0000-000000000017',
        '20000000-0000-0000-0000-000000000021',
        '20000000-0000-0000-0000-000000000025',
        '20000000-0000-0000-0000-000000000029'
    ) THEN 'Bucaramanga'
    WHEN tool_id IN (
        '20000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000006',
        '20000000-0000-0000-0000-000000000010',
        '20000000-0000-0000-0000-000000000014',
        '20000000-0000-0000-0000-000000000018',
        '20000000-0000-0000-0000-000000000022',
        '20000000-0000-0000-0000-000000000026',
        '20000000-0000-0000-0000-000000000030'
    ) THEN 'Floridablanca'
    WHEN tool_id IN (
        '20000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000007',
        '20000000-0000-0000-0000-000000000011',
        '20000000-0000-0000-0000-000000000015',
        '20000000-0000-0000-0000-000000000019',
        '20000000-0000-0000-0000-000000000023',
        '20000000-0000-0000-0000-000000000027'
    ) THEN 'Girón'
    WHEN tool_id IN (
        '20000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000008',
        '20000000-0000-0000-0000-000000000012',
        '20000000-0000-0000-0000-000000000016',
        '20000000-0000-0000-0000-000000000020',
        '20000000-0000-0000-0000-000000000024',
        '20000000-0000-0000-0000-000000000028'
    ) THEN 'Piedecuesta'
    ELSE service_city
END;

UPDATE invoice_items AS item
SET issued_description = 'Herramienta: ' || tool.name || ' | Ciudad: ' || tool.service_city
FROM invoices AS invoice
JOIN reservation_items AS reservation_item
    ON reservation_item.reservation_id = invoice.reservation_id
JOIN equipment_units AS equipment_unit
    ON equipment_unit.equipment_unit_id = reservation_item.equipment_unit_id
JOIN tools AS tool
    ON tool.tool_id = equipment_unit.tool_id
WHERE item.invoice_id = invoice.invoice_id
  AND item.line_number = 1;
