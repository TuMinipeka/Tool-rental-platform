CREATE TABLE equipment_units (
    equipment_unit_id uuid PRIMARY KEY,
    tool_id uuid NOT NULL REFERENCES tools (tool_id) ON DELETE RESTRICT,
    inventory_code varchar(120) NOT NULL,
    serial_number varchar(160),
    current_condition varchar(40) NOT NULL DEFAULT 'GOOD',
    operational_status varchar(20) NOT NULL,
    CONSTRAINT ck_equipment_units_inventory_code_not_blank CHECK (btrim(inventory_code) <> ''),
    CONSTRAINT ck_equipment_units_current_condition_not_blank CHECK (
        btrim(current_condition) <> ''
    ),
    CONSTRAINT ck_equipment_units_operational_status CHECK (
        operational_status IN ('OPERATIONAL', 'MAINTENANCE', 'RETIRED')
    )
);

CREATE INDEX ix_equipment_units_tool_id ON equipment_units (tool_id);
CREATE INDEX ix_equipment_units_inventory_code_lower ON equipment_units (lower(inventory_code));

CREATE FUNCTION enforce_supplier_inventory_code_uniqueness()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    owning_supplier_id uuid;
BEGIN
    SELECT supplier_id
      INTO STRICT owning_supplier_id
      FROM tools
     WHERE tool_id = NEW.tool_id;

    PERFORM pg_advisory_xact_lock(hashtextextended(owning_supplier_id::text, 0));

    IF EXISTS (
        SELECT 1
          FROM equipment_units existing_unit
          JOIN tools existing_tool ON existing_tool.tool_id = existing_unit.tool_id
         WHERE existing_tool.supplier_id = owning_supplier_id
           AND lower(existing_unit.inventory_code) = lower(NEW.inventory_code)
           AND existing_unit.equipment_unit_id <> NEW.equipment_unit_id
    ) THEN
        RAISE EXCEPTION 'Inventory code must be unique within a supplier'
            USING ERRCODE = '23505',
                  CONSTRAINT = 'uq_equipment_units_supplier_inventory_code';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_equipment_units_supplier_inventory_code
BEFORE INSERT OR UPDATE OF tool_id, inventory_code ON equipment_units
FOR EACH ROW
EXECUTE FUNCTION enforce_supplier_inventory_code_uniqueness();
