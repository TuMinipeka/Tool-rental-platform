-- Local demonstration data. Loaded only when the demo profile is explicitly active.
-- Credentials are documented in README.md and must never be enabled in production.
INSERT INTO users (
    user_id, email, display_name, password_hash, account_status, registered_at
) VALUES
    (
        '00000000-0000-0000-0000-000000000101',
        'cliente@toolrental.local',
        'Cliente ToolRental',
        '$2a$10$fa2gkqcdPVK.hRoYfUKGcOpuxD0qs6vqgPWkM2kaoD8AZujIeIeyu',
        'ACTIVE',
        '2026-10-19T12:00:00Z'
    ),
    (
        '00000000-0000-0000-0000-000000000102',
        'proveedor@toolrental.local',
        'Proveedor ToolRental',
        '$2a$10$fa2gkqcdPVK.hRoYfUKGcOpuxD0qs6vqgPWkM2kaoD8AZujIeIeyu',
        'ACTIVE',
        '2026-10-19T12:05:00Z'
    ),
    (
        '00000000-0000-0000-0000-000000000103',
        'admin@toolrental.local',
        'Administrador ToolRental',
        '$2a$10$fa2gkqcdPVK.hRoYfUKGcOpuxD0qs6vqgPWkM2kaoD8AZujIeIeyu',
        'ACTIVE',
        '2026-10-19T12:10:00Z'
    );

INSERT INTO user_roles (user_id, role_id)
SELECT assignment.user_id, role.role_id
FROM (
    VALUES
        ('00000000-0000-0000-0000-000000000101'::uuid, 'CUSTOMER'),
        ('00000000-0000-0000-0000-000000000102'::uuid, 'SUPPLIER'),
        ('00000000-0000-0000-0000-000000000103'::uuid, 'ADMIN')
) AS assignment(user_id, role_code)
JOIN roles role ON role.code = assignment.role_code;

INSERT INTO customers (
    user_id, person_type, document_type, document_number, billing_name
) VALUES (
    '00000000-0000-0000-0000-000000000101',
    'PERSON',
    'CC',
    '1099000101',
    'Cliente ToolRental'
);

INSERT INTO suppliers (
    user_id, document_type, document_number, business_name
) VALUES (
    '00000000-0000-0000-0000-000000000102',
    'NIT',
    '900999102-1',
    'Proveedor ToolRental Demo'
);

UPDATE categories
SET name = CASE category_id
        WHEN '10000000-0000-0000-0000-000000000001' THEN 'Perforación'
        WHEN '10000000-0000-0000-0000-000000000002' THEN 'Demolición'
        WHEN '10000000-0000-0000-0000-000000000003' THEN 'Corte'
        WHEN '10000000-0000-0000-0000-000000000004' THEN 'Aire comprimido'
        WHEN '10000000-0000-0000-0000-000000000005' THEN 'Concreto'
    END,
    description = CASE category_id
        WHEN '10000000-0000-0000-0000-000000000001' THEN 'Taladros y equipos de perforación profesional'
        WHEN '10000000-0000-0000-0000-000000000002' THEN 'Martillos y equipos para demolición controlada'
        WHEN '10000000-0000-0000-0000-000000000003' THEN 'Equipos portátiles para corte en obra'
        WHEN '10000000-0000-0000-0000-000000000004' THEN 'Compresores y herramientas neumáticas'
        WHEN '10000000-0000-0000-0000-000000000005' THEN 'Preparación, vibrado y acabado de concreto'
    END
WHERE category_id IN (
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000005'
);

INSERT INTO categories (category_id, name, description) VALUES
    ('10000000-0000-0000-0000-000000000006', 'Limpieza', 'Equipos de limpieza para obra, comercio y hogar'),
    ('10000000-0000-0000-0000-000000000007', 'Energía', 'Generación y distribución temporal de energía'),
    ('10000000-0000-0000-0000-000000000008', 'Altura', 'Acceso seguro y trabajo temporal en altura'),
    ('10000000-0000-0000-0000-000000000009', 'Jardinería', 'Mantenimiento de zonas verdes y corte exterior'),
    ('10000000-0000-0000-0000-000000000010', 'Compactación', 'Compactación de suelos y bases granulares'),
    ('10000000-0000-0000-0000-000000000011', 'Soldadura', 'Soldadura portátil y trabajos de metal');

-- These rates are illustrative demo values, not approved commercial prices.
INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES
    ('20000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000001', 'Taladro percutor de 850 W', 'Taladro con mandril de media pulgada y percusión para mampostería.', 28000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000001', 'Taladro inalámbrico de 20 V', 'Taladro atornillador con dos baterías para instalación y montaje.', 35000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000002', 'Demoledor hexagonal de 30 kg', 'Martillo demoledor de alto impacto para losas y cimentaciones.', 135000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000002', 'Martillo combinado SDS Plus', 'Equipo compacto para perforar, cincelar y retirar revestimientos.', 68000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000003', 'Sierra caladora de 750 W', 'Sierra de velocidad variable para cortes curvos y rectos.', 32000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000003', 'Pulidora angular de 4 1/2 pulgadas', 'Pulidora para corte y desbaste ligero con guarda ajustable.', 26000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000003', 'Cortadora de cerámica de 1.200 mm', 'Cortadora manual reforzada para cerámica y porcelanato.', 62000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000004', 'Compresor portátil de 24 L', 'Compresor de dos caballos para pintura y herramientas neumáticas ligeras.', 52000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000004', 'Clavadora neumática profesional', 'Clavadora para carpintería, acabados e instalación de molduras.', 38000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000005', 'Mezcladora de concreto de 160 L', 'Mezcladora portátil con tambor basculante para obras pequeñas.', 120000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000005', 'Vibrador de concreto de 2 HP', 'Motor con eje flexible para consolidación uniforme del concreto.', 88000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000005', 'Allanadora de concreto de 36 pulgadas', 'Equipo a gasolina para acabado de placas y pisos de concreto.', 175000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000006', 'Hidrolavadora profesional de 2.200 PSI', 'Hidrolavadora con manguera reforzada y boquillas intercambiables.', 74000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000019', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000006', 'Aspiradora industrial de 30 L', 'Aspiradora para residuos secos y húmedos con filtro lavable.', 58000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000006', 'Brilladora industrial de 17 pulgadas', 'Brilladora de baja velocidad para limpieza y mantenimiento de pisos.', 95000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000007', 'Generador eléctrico de 3,2 kW', 'Generador a gasolina con regulación automática de voltaje.', 145000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000007', 'Torre de iluminación LED', 'Torre telescópica portátil para iluminación temporal de obra.', 180000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000007', 'Planta eléctrica de 6,5 kVA', 'Planta monofásica para respaldo de herramientas y equipos de obra.', 230000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000024', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000008', 'Escalera multipropósito 4 × 4', 'Escalera articulada de aluminio con seguros reforzados.', 48000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000025', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000008', 'Andamio tubular de 2 m', 'Módulo tubular con crucetas y plataforma para trabajo temporal.', 110000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000026', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000009', 'Desbrozadora a gasolina de 52 cc', 'Equipo de corte con arnés para pasto alto y maleza.', 72000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000027', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000009', 'Motosierra a gasolina de 20 pulgadas', 'Motosierra para poda controlada y corte de madera.', 98000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000028', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000010', 'Compactador tipo canguro', 'Apisonador de impacto para zanjas y espacios reducidos.', 165000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000029', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000010', 'Placa vibratoria de 90 kg', 'Compactadora reversible para bases, senderos y adoquines.', 155000, 'COP', 'PUBLISHED'),
    ('20000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000011', 'Soldador inverter de 200 A', 'Soldador portátil para electrodo revestido con control digital.', 65000, 'COP', 'PUBLISHED');

INSERT INTO equipment_units (
    equipment_unit_id, tool_id, inventory_code, serial_number,
    current_condition, operational_status
)
SELECT
    ('30000000-0000-0000-0000-' || lpad(sequence::text, 12, '0'))::uuid,
    ('20000000-0000-0000-0000-' || lpad(sequence::text, 12, '0'))::uuid,
    'DEMO-' || lpad(sequence::text, 3, '0'),
    'TR-2026-' || lpad(sequence::text, 3, '0'),
    'GOOD',
    'OPERATIONAL'
FROM generate_series(6, 30) AS sequence;

UPDATE unit_deliveries
SET city = 'Bucaramanga', region = 'Santander'
WHERE city = 'Bogota' AND region = 'Bogota D.C.';
