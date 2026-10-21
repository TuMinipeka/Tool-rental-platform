-- Isolated academic fixtures. No processor calls, identities or credentials are real.
-- Never edit applied migrations; this adds independent historical scenarios.
DO $$
DECLARE
    i integer;
    customer_key uuid;
    supplier_key uuid;
    category_key uuid;
    tool_key uuid;
    unit_key uuid;
    completed_key uuid;
    cancelled_key uuid;
    payment_key uuid;
    cancelled_payment_key uuid;
    termination_key uuid;
    invoice_key uuid;
BEGIN
    FOR i IN 1..20 LOOP
        customer_key := ('71000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        supplier_key := ('72000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        category_key := ('73000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        tool_key := ('74000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        unit_key := ('75000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        completed_key := ('76000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        cancelled_key := ('77000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        payment_key := ('78000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        cancelled_payment_key := ('79000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        termination_key := ('7a000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        invoice_key := ('7b000000-0000-0000-0000-' || lpad(i::text,12,'0'))::uuid;
        INSERT INTO users VALUES
            (customer_key,'mvp.customer.'||i||'@example.test','Academic Customer '||i,'SEED_DISABLED_NO_LOGIN','DISABLED','2025-01-01Z'),
            (supplier_key,'mvp.supplier.'||i||'@example.test','Academic Supplier '||i,'SEED_DISABLED_NO_LOGIN','DISABLED','2025-01-01Z');
        INSERT INTO user_roles SELECT customer_key,role_id FROM roles WHERE code='CUSTOMER';
        INSERT INTO user_roles SELECT supplier_key,role_id FROM roles WHERE code='SUPPLIER';
        INSERT INTO customers SELECT (jsonb_populate_record(NULL::customers,
            (SELECT to_jsonb(s) FROM customers s ORDER BY user_id LIMIT 1) ||
            jsonb_build_object('user_id',customer_key,'document_number','MVP-C-'||i,'billing_name','Academic Customer '||i))).*;
        INSERT INTO suppliers SELECT (jsonb_populate_record(NULL::suppliers,
            (SELECT to_jsonb(s) FROM suppliers s ORDER BY user_id LIMIT 1) ||
            jsonb_build_object('user_id',supplier_key,'document_number','MVP-S-'||i,'business_name','Academic Supplier '||i))).*;
        INSERT INTO categories VALUES(category_key,'Academic equipment group '||i,'Isolated MVP demonstration category');
        INSERT INTO tools SELECT (jsonb_populate_record(NULL::tools,
            (SELECT to_jsonb(s) FROM tools s ORDER BY tool_id LIMIT 1) ||
            jsonb_build_object('tool_id',tool_key,'supplier_id',supplier_key,'category_id',category_key,
                'name','Academic tool '||i,'description','Fictional historical equipment for integration validation',
                'current_daily_rate',30000,'rate_currency','COP','publication_status','UNPUBLISHED'))).*;
        INSERT INTO tool_images VALUES(tool_key,'https://example.test/mvp/catalog/01.png');
        INSERT INTO equipment_units VALUES(unit_key,tool_key,'MVP-UNIT-'||i,'MVP-SERIAL-'||i,'GOOD','OPERATIONAL');
        INSERT INTO reservations SELECT (jsonb_populate_record(NULL::reservations,
            (SELECT to_jsonb(s) FROM reservations s ORDER BY reservation_id LIMIT 1) ||
            jsonb_build_object('reservation_id',completed_key,'customer_id',customer_key,
                'created_at','2025-01-20T12:00:00Z','start_date','2025-02-01','return_date','2025-02-03',
                'effective_return_date','2025-02-03','expires_at','2025-01-21T12:00:00Z',
                'reservation_status','CONFIRMED','currency','COP','confirmed_at','2025-01-20T13:00:00Z',
                'scheduled_fulfillment_at','2025-02-01T13:00:00Z','fulfillment_method','CUSTOMER_PICKUP',
                'delivery_rate_id',NULL,'delivery_fee',0,'delivery_address_line',NULL,'delivery_city',NULL,
                'delivery_region',NULL,'delivery_country_code',NULL))).*;
        INSERT INTO reservation_items VALUES(completed_key,unit_key,30000);
        INSERT INTO rentals VALUES(completed_key,'2025-02-01T13:00:00Z','COMPLETED');
        INSERT INTO unit_deliveries SELECT (jsonb_populate_record(NULL::unit_deliveries,
            (SELECT to_jsonb(s) FROM unit_deliveries s ORDER BY reservation_id LIMIT 1) ||
            jsonb_build_object('reservation_id',completed_key,'equipment_unit_id',unit_key,
                'scheduled_at','2025-02-01T13:00:00Z','delivered_at','2025-02-01T13:00:00Z',
                'outbound_condition','GOOD','fulfillment_method','CUSTOMER_PICKUP','delivery_status','COMPLETED',
                'failure_type',NULL,'failure_reason',NULL,'failed_at',NULL))).*;
        INSERT INTO unit_returns SELECT (jsonb_populate_record(NULL::unit_returns,
            (SELECT to_jsonb(s) FROM unit_returns s ORDER BY reservation_id LIMIT 1) ||
            jsonb_build_object('reservation_id',completed_key,'equipment_unit_id',unit_key,
                'received_at','2025-02-03T20:00:00Z','inbound_condition','GOOD','notes','Academic return without chargeable damage',
                'late_days',0,'late_fee',0,'late_fee_currency','COP'))).*;
        INSERT INTO damage_reports VALUES(('7c000000-0000-0000-0000-'||lpad(i::text,12,'0'))::uuid,
            completed_key,unit_key,'2025-02-03T20:05:00Z','Academic inspection: no chargeable damage','CLOSED',0);
        INSERT INTO payments SELECT (jsonb_populate_record(NULL::payments,
            (SELECT to_jsonb(s) FROM payments s ORDER BY payment_id LIMIT 1) ||
            jsonb_build_object('payment_id',payment_key,'reservation_id',completed_key,'processor','ACADEMIC_FIXTURE',
                'external_reference','MVP-COMPLETE-'||i,'amount',90000,'payment_status','CONFIRMED',
                'recorded_at','2025-01-20T14:00:00Z','confirmed_at','2025-01-20T14:01:00Z',
                'payment_method','ADMINISTRATIVE','payment_stage','ADMINISTRATIVE','idempotency_key','mvp-complete-'||i,
                'card_network',NULL,'card_last_four',NULL,'card_expiry_month',NULL,'card_expiry_year',NULL,
                'adjustment_type',NULL,'adjustment_reference',NULL))).*;
        INSERT INTO invoices VALUES(invoice_key,completed_key,'ACADEMIC-MVP-'||i,'2025-02-03T20:10:00Z','PAID',
            'Academic Supplier '||i,'MVP-S-'||i,'Academic Customer '||i,'MVP-C-'||i);
        INSERT INTO invoice_items VALUES(invoice_key,1,'Academic rental: three inclusive days, not a DIAN invoice',3,30000);
        INSERT INTO reservation_extensions VALUES(('7d000000-0000-0000-0000-'||lpad(i::text,12,'0'))::uuid,
            completed_key,customer_key,'2025-02-04','REJECTED','2025-02-02T13:00:00Z',supplier_key,
            '2025-02-02T14:00:00Z',30000,'COP','mvp-extension-'||i);
        INSERT INTO reservations SELECT (jsonb_populate_record(NULL::reservations,
            (SELECT to_jsonb(s) FROM reservations s WHERE reservation_id=completed_key) ||
            jsonb_build_object('reservation_id',cancelled_key,'start_date','2025-03-04','return_date','2025-03-06',
                'effective_return_date','2025-03-06','scheduled_fulfillment_at','2025-03-04T13:00:00Z',
                'reservation_status','CANCELLED'))).*;
        INSERT INTO reservation_items VALUES(cancelled_key,unit_key,30000);
        INSERT INTO payments SELECT (jsonb_populate_record(NULL::payments,
            (SELECT to_jsonb(s) FROM payments s WHERE payment_id=payment_key) ||
            jsonb_build_object('payment_id',cancelled_payment_key,'reservation_id',cancelled_key,
                'external_reference','MVP-CANCEL-'||i,'idempotency_key','mvp-cancel-payment-'||i))).*;
        INSERT INTO reservation_terminations VALUES(termination_key,cancelled_key,'STANDARD','APPLIED',customer_key,
            '2025-01-25T12:00:00Z',customer_key,'2025-01-25T12:00:00Z','Academic cancellation more than 48 hours before start',
            0,90000,0,'COP','mvp-termination-'||i);
        INSERT INTO refunds VALUES(('7e000000-0000-0000-0000-'||lpad(i::text,12,'0'))::uuid,cancelled_payment_key,
            termination_key,90000,'COP','PENDING','mvp-refund-'||i,'2025-01-25T12:00:00Z',NULL);
    END LOOP;
END $$;
