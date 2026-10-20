--
-- PostgreSQL database dump
--

\restrict ssPdALK9fRQKTudbfDo1O8FPRzOkaXoZLP6iKMeS45xJ30F1Uvo9DDlNaPhWbad

-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: enforce_supplier_inventory_code_uniqueness(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.enforce_supplier_inventory_code_uniqueness() RETURNS trigger
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


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cancellation_policies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cancellation_policies (
    cancellation_policy_id uuid NOT NULL,
    policy_version integer NOT NULL,
    cutoff_hours integer NOT NULL,
    late_cancellation_rate numeric(5,4) NOT NULL,
    no_show_rate numeric(5,4) NOT NULL,
    no_show_grace_minutes integer NOT NULL,
    active boolean NOT NULL,
    effective_at timestamp with time zone NOT NULL,
    CONSTRAINT ck_cancellation_policies_values CHECK (((policy_version > 0) AND (cutoff_hours >= 0) AND ((late_cancellation_rate >= (0)::numeric) AND (late_cancellation_rate <= (1)::numeric)) AND ((no_show_rate >= (0)::numeric) AND (no_show_rate <= (1)::numeric)) AND (no_show_grace_minutes >= 0)))
);


--
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    category_id uuid NOT NULL,
    name character varying(120) NOT NULL,
    description character varying(500),
    CONSTRAINT ck_categories_name_not_blank CHECK ((btrim((name)::text) <> ''::text))
);


--
-- Name: customers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customers (
    user_id uuid NOT NULL,
    person_type character varying(20) NOT NULL,
    document_type character varying(30) NOT NULL,
    document_number character varying(80) NOT NULL,
    billing_name character varying(200) NOT NULL,
    CONSTRAINT ck_customers_billing_name_not_blank CHECK ((btrim((billing_name)::text) <> ''::text)),
    CONSTRAINT ck_customers_document_not_blank CHECK ((btrim((document_number)::text) <> ''::text)),
    CONSTRAINT ck_customers_person_type CHECK (((person_type)::text = ANY ((ARRAY['PERSON'::character varying, 'COMPANY'::character varying])::text[])))
);


--
-- Name: damage_reports; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.damage_reports (
    damage_report_id uuid NOT NULL,
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    reported_at timestamp with time zone NOT NULL,
    description character varying(2000) NOT NULL,
    report_status character varying(20) NOT NULL,
    assessed_cost numeric(19,4),
    CONSTRAINT ck_damage_reports_assessed_cost CHECK (((assessed_cost IS NULL) OR (assessed_cost >= (0)::numeric))),
    CONSTRAINT ck_damage_reports_description_not_blank CHECK ((btrim((description)::text) <> ''::text)),
    CONSTRAINT ck_damage_reports_status CHECK (((report_status)::text = ANY ((ARRAY['OPEN'::character varying, 'ASSESSED'::character varying, 'CLOSED'::character varying])::text[])))
);


--
-- Name: delivery_rates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.delivery_rates (
    delivery_rate_id uuid NOT NULL,
    rate_version integer NOT NULL,
    amount numeric(19,4) NOT NULL,
    currency character(3) NOT NULL,
    active boolean NOT NULL,
    effective_at timestamp with time zone NOT NULL,
    CONSTRAINT ck_delivery_rates_amount CHECK ((amount >= (0)::numeric)),
    CONSTRAINT ck_delivery_rates_currency CHECK ((currency = 'COP'::bpchar)),
    CONSTRAINT ck_delivery_rates_version CHECK ((rate_version > 0))
);


--
-- Name: equipment_units; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.equipment_units (
    equipment_unit_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    inventory_code character varying(120) NOT NULL,
    serial_number character varying(160),
    current_condition character varying(40) DEFAULT 'GOOD'::character varying NOT NULL,
    operational_status character varying(20) NOT NULL,
    CONSTRAINT ck_equipment_units_current_condition_not_blank CHECK ((btrim((current_condition)::text) <> ''::text)),
    CONSTRAINT ck_equipment_units_inventory_code_not_blank CHECK ((btrim((inventory_code)::text) <> ''::text)),
    CONSTRAINT ck_equipment_units_operational_status CHECK (((operational_status)::text = ANY ((ARRAY['OPERATIONAL'::character varying, 'MAINTENANCE'::character varying, 'RETIRED'::character varying])::text[])))
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: invoice_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoice_items (
    invoice_id uuid NOT NULL,
    line_number integer NOT NULL,
    issued_description character varying(500) NOT NULL,
    quantity numeric(19,4) NOT NULL,
    issued_unit_price numeric(19,4) NOT NULL,
    CONSTRAINT ck_invoice_items_description_not_blank CHECK ((btrim((issued_description)::text) <> ''::text)),
    CONSTRAINT ck_invoice_items_line_number_positive CHECK ((line_number > 0)),
    CONSTRAINT ck_invoice_items_quantity_positive CHECK ((quantity > (0)::numeric)),
    CONSTRAINT ck_invoice_items_unit_price_non_negative CHECK ((issued_unit_price >= (0)::numeric))
);


--
-- Name: invoices; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoices (
    invoice_id uuid NOT NULL,
    reservation_id uuid NOT NULL,
    number character varying(80) NOT NULL,
    issued_at timestamp with time zone NOT NULL,
    invoice_status character varying(20) NOT NULL,
    issued_supplier_name character varying(200) NOT NULL,
    issued_supplier_document character varying(120) NOT NULL,
    issued_customer_name character varying(200) NOT NULL,
    issued_customer_document character varying(120) NOT NULL,
    CONSTRAINT ck_invoices_number_not_blank CHECK ((btrim((number)::text) <> ''::text)),
    CONSTRAINT ck_invoices_status CHECK (((invoice_status)::text = ANY ((ARRAY['ISSUED'::character varying, 'VOIDED'::character varying, 'PAID'::character varying])::text[])))
);


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    password_reset_token_id uuid NOT NULL,
    user_id uuid NOT NULL,
    token_hash character(64) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    revoked_at timestamp with time zone,
    CONSTRAINT ck_password_reset_tokens_completion CHECK (((used_at IS NULL) OR (revoked_at IS NULL))),
    CONSTRAINT ck_password_reset_tokens_time CHECK ((created_at < expires_at))
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    payment_id uuid NOT NULL,
    reservation_id uuid NOT NULL,
    processor character varying(80) NOT NULL,
    external_reference character varying(200) NOT NULL,
    amount numeric(19,4) NOT NULL,
    payment_status character varying(20) NOT NULL,
    recorded_at timestamp with time zone NOT NULL,
    confirmed_at timestamp with time zone,
    payment_method character varying(30) NOT NULL,
    payment_stage character varying(30) NOT NULL,
    idempotency_key character varying(200) NOT NULL,
    card_network character varying(20),
    card_last_four character varying(4),
    card_expiry_month smallint,
    card_expiry_year smallint,
    adjustment_type character varying(30),
    adjustment_reference uuid,
    CONSTRAINT ck_payments_adjustment_reference CHECK (((((payment_stage)::text = 'ADJUSTMENT'::text) AND (adjustment_type IS NOT NULL) AND (adjustment_reference IS NOT NULL)) OR (((payment_stage)::text <> 'ADJUSTMENT'::text) AND (adjustment_type IS NULL) AND (adjustment_reference IS NULL)))),
    CONSTRAINT ck_payments_amount_positive CHECK ((amount > (0)::numeric)),
    CONSTRAINT ck_payments_card_details CHECK (((((payment_method)::text = ANY ((ARRAY['CREDIT_CARD'::character varying, 'DEBIT_CARD'::character varying])::text[])) AND ((card_network)::text = ANY ((ARRAY['VISA'::character varying, 'MASTERCARD'::character varying])::text[])) AND ((card_last_four)::text ~ '^[0-9]{4}$'::text) AND ((card_expiry_month >= 1) AND (card_expiry_month <= 12)) AND (card_expiry_year >= 2026)) OR (((payment_method)::text = ANY ((ARRAY['PAYPAL'::character varying, 'ADMINISTRATIVE'::character varying])::text[])) AND (card_network IS NULL) AND (card_last_four IS NULL) AND (card_expiry_month IS NULL) AND (card_expiry_year IS NULL)))),
    CONSTRAINT ck_payments_method CHECK (((payment_method)::text = ANY ((ARRAY['ADMINISTRATIVE'::character varying, 'CREDIT_CARD'::character varying, 'DEBIT_CARD'::character varying, 'PAYPAL'::character varying])::text[]))),
    CONSTRAINT ck_payments_processor_not_blank CHECK ((btrim((processor)::text) <> ''::text)),
    CONSTRAINT ck_payments_reference_not_blank CHECK ((btrim((external_reference)::text) <> ''::text)),
    CONSTRAINT ck_payments_stage CHECK (((payment_stage)::text = ANY ((ARRAY['ADMINISTRATIVE'::character varying, 'FIRST_HALF'::character varying, 'FINAL_HALF'::character varying, 'ADJUSTMENT'::character varying])::text[]))),
    CONSTRAINT ck_payments_status CHECK (((payment_status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'FAILED'::character varying, 'REFUNDED'::character varying])::text[])))
);


--
-- Name: refunds; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refunds (
    refund_id uuid NOT NULL,
    payment_id uuid NOT NULL,
    reservation_termination_id uuid,
    amount numeric(19,4) NOT NULL,
    currency character(3) NOT NULL,
    refund_status character varying(20) NOT NULL,
    idempotency_key character varying(200) NOT NULL,
    recorded_at timestamp with time zone NOT NULL,
    confirmed_at timestamp with time zone,
    CONSTRAINT ck_refunds_amount CHECK ((amount > (0)::numeric)),
    CONSTRAINT ck_refunds_confirmation CHECK (((((refund_status)::text = 'CONFIRMED'::text) AND (confirmed_at IS NOT NULL)) OR (((refund_status)::text <> 'CONFIRMED'::text) AND (confirmed_at IS NULL)))),
    CONSTRAINT ck_refunds_currency CHECK ((currency ~ '^[A-Z]{3}$'::text)),
    CONSTRAINT ck_refunds_status CHECK (((refund_status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: rentals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rentals (
    reservation_id uuid NOT NULL,
    started_at timestamp with time zone NOT NULL,
    rental_status character varying(20) NOT NULL,
    CONSTRAINT ck_rentals_status CHECK (((rental_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: reservation_extensions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservation_extensions (
    reservation_extension_id uuid NOT NULL,
    reservation_id uuid NOT NULL,
    requested_by uuid NOT NULL,
    requested_return_date date NOT NULL,
    extension_status character varying(20) NOT NULL,
    requested_at timestamp with time zone NOT NULL,
    resolved_by uuid,
    resolved_at timestamp with time zone,
    additional_amount numeric(19,4) NOT NULL,
    currency character(3) NOT NULL,
    idempotency_key character varying(200) NOT NULL,
    CONSTRAINT ck_reservation_extensions_amount CHECK ((additional_amount >= (0)::numeric)),
    CONSTRAINT ck_reservation_extensions_currency CHECK ((currency ~ '^[A-Z]{3}$'::text)),
    CONSTRAINT ck_reservation_extensions_resolution CHECK (((((extension_status)::text = 'REQUESTED'::text) AND (resolved_by IS NULL) AND (resolved_at IS NULL)) OR (((extension_status)::text <> 'REQUESTED'::text) AND (resolved_by IS NOT NULL) AND (resolved_at IS NOT NULL)))),
    CONSTRAINT ck_reservation_extensions_status CHECK (((extension_status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: reservation_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservation_items (
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    agreed_daily_rate numeric(19,4) NOT NULL,
    CONSTRAINT ck_reservation_items_rate_non_negative CHECK ((agreed_daily_rate >= (0)::numeric))
);


--
-- Name: reservation_terminations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservation_terminations (
    reservation_termination_id uuid NOT NULL,
    reservation_id uuid NOT NULL,
    termination_type character varying(30) NOT NULL,
    termination_status character varying(20) NOT NULL,
    requested_by uuid NOT NULL,
    requested_at timestamp with time zone NOT NULL,
    resolved_by uuid,
    resolved_at timestamp with time zone,
    reason character varying(500),
    fee_amount numeric(19,4) NOT NULL,
    refund_amount numeric(19,4) NOT NULL,
    outstanding_amount numeric(19,4) NOT NULL,
    currency character(3) NOT NULL,
    idempotency_key character varying(200) NOT NULL,
    CONSTRAINT ck_reservation_terminations_amounts CHECK (((fee_amount >= (0)::numeric) AND (refund_amount >= (0)::numeric) AND (outstanding_amount >= (0)::numeric))),
    CONSTRAINT ck_reservation_terminations_currency CHECK ((currency ~ '^[A-Z]{3}$'::text)),
    CONSTRAINT ck_reservation_terminations_resolution CHECK (((((termination_status)::text = 'REQUESTED'::text) AND (resolved_by IS NULL) AND (resolved_at IS NULL)) OR (((termination_status)::text <> 'REQUESTED'::text) AND (resolved_at IS NOT NULL)))),
    CONSTRAINT ck_reservation_terminations_status CHECK (((termination_status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPLIED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT ck_reservation_terminations_type CHECK (((termination_type)::text = ANY ((ARRAY['STANDARD'::character varying, 'NO_SHOW'::character varying, 'WITHDRAWAL'::character varying, 'SUPPLIER_FAILURE'::character varying, 'EXTERNAL_REVIEW'::character varying])::text[])))
);


--
-- Name: reservations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservations (
    reservation_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    start_date date NOT NULL,
    return_date date NOT NULL,
    expires_at timestamp with time zone,
    reservation_status character varying(20) NOT NULL,
    currency character(3) NOT NULL,
    confirmed_at timestamp with time zone,
    scheduled_fulfillment_at timestamp with time zone NOT NULL,
    fulfillment_method character varying(30) NOT NULL,
    delivery_rate_id uuid,
    cancellation_policy_id uuid NOT NULL,
    delivery_fee numeric(19,4) NOT NULL,
    effective_return_date date NOT NULL,
    delivery_address_line character varying(300),
    delivery_city character varying(120),
    delivery_region character varying(120),
    delivery_country_code character(2),
    CONSTRAINT ck_reservations_confirmation CHECK (((((reservation_status)::text = ANY ((ARRAY['CONFIRMED'::character varying, 'CANCELLATION_REVIEW'::character varying, 'NO_SHOW'::character varying])::text[])) AND (confirmed_at IS NOT NULL)) OR (((reservation_status)::text = ANY ((ARRAY['PENDING'::character varying, 'REJECTED'::character varying])::text[])) AND (confirmed_at IS NULL)) OR ((reservation_status)::text = 'CANCELLED'::text))),
    CONSTRAINT ck_reservations_currency_uppercase CHECK (((currency)::text ~ '^[A-Z]{3}$'::text)),
    CONSTRAINT ck_reservations_effective_period CHECK ((effective_return_date >= return_date)),
    CONSTRAINT ck_reservations_fulfillment CHECK (((((fulfillment_method)::text = 'CUSTOMER_PICKUP'::text) AND (delivery_rate_id IS NULL) AND (delivery_fee = (0)::numeric) AND (delivery_address_line IS NULL) AND (delivery_city IS NULL) AND (delivery_region IS NULL) AND (delivery_country_code IS NULL)) OR (((fulfillment_method)::text = 'SUPPLIER_DELIVERY'::text) AND (delivery_rate_id IS NOT NULL) AND (delivery_fee >= (0)::numeric) AND (delivery_address_line IS NOT NULL) AND (delivery_city IS NOT NULL) AND (delivery_region IS NOT NULL) AND (delivery_country_code = 'CO'::bpchar)))),
    CONSTRAINT ck_reservations_period CHECK ((start_date <= return_date)),
    CONSTRAINT ck_reservations_status CHECK (((reservation_status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying, 'CANCELLATION_REVIEW'::character varying, 'NO_SHOW'::character varying])::text[])))
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    role_id smallint NOT NULL,
    code character varying(40) NOT NULL,
    description character varying(200) NOT NULL,
    CONSTRAINT ck_roles_code_not_blank CHECK ((btrim((code)::text) <> ''::text)),
    CONSTRAINT ck_roles_description_not_blank CHECK ((btrim((description)::text) <> ''::text))
);


--
-- Name: roles_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.roles ALTER COLUMN role_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.roles_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: suppliers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suppliers (
    user_id uuid NOT NULL,
    document_type character varying(30) NOT NULL,
    document_number character varying(80) NOT NULL,
    business_name character varying(200) NOT NULL,
    CONSTRAINT ck_suppliers_business_name_not_blank CHECK ((btrim((business_name)::text) <> ''::text)),
    CONSTRAINT ck_suppliers_document_not_blank CHECK ((btrim((document_number)::text) <> ''::text))
);


--
-- Name: tool_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tool_images (
    tool_id uuid NOT NULL,
    image_url character varying(2048) NOT NULL,
    CONSTRAINT ck_tool_images_url_not_blank CHECK ((btrim((image_url)::text) <> ''::text))
);


--
-- Name: tools; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tools (
    tool_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    category_id uuid NOT NULL,
    name character varying(160) NOT NULL,
    description character varying(2000) NOT NULL,
    current_daily_rate numeric(19,4) NOT NULL,
    rate_currency character(3) NOT NULL,
    publication_status character varying(20) NOT NULL,
    CONSTRAINT ck_tools_currency_uppercase CHECK (((rate_currency)::text ~ '^[A-Z]{3}$'::text)),
    CONSTRAINT ck_tools_daily_rate_non_negative CHECK ((current_daily_rate >= (0)::numeric)),
    CONSTRAINT ck_tools_description_not_blank CHECK ((btrim((description)::text) <> ''::text)),
    CONSTRAINT ck_tools_name_not_blank CHECK ((btrim((name)::text) <> ''::text)),
    CONSTRAINT ck_tools_publication_status CHECK (((publication_status)::text = ANY ((ARRAY['PUBLISHED'::character varying, 'UNPUBLISHED'::character varying])::text[])))
);


--
-- Name: unit_deliveries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.unit_deliveries (
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    scheduled_at timestamp with time zone NOT NULL,
    delivered_at timestamp with time zone,
    outbound_condition character varying(500),
    address_line character varying(300) NOT NULL,
    city character varying(120) NOT NULL,
    region character varying(120) NOT NULL,
    country character varying(120) NOT NULL,
    fulfillment_method character varying(30) DEFAULT 'SUPPLIER_DELIVERY'::character varying NOT NULL,
    delivery_status character varying(20) DEFAULT 'SCHEDULED'::character varying NOT NULL,
    failure_type character varying(30),
    failure_reason character varying(500),
    failed_at timestamp with time zone,
    CONSTRAINT ck_unit_deliveries_address_not_blank CHECK ((btrim((address_line)::text) <> ''::text)),
    CONSTRAINT ck_unit_deliveries_city_not_blank CHECK ((btrim((city)::text) <> ''::text)),
    CONSTRAINT ck_unit_deliveries_country_not_blank CHECK ((btrim((country)::text) <> ''::text)),
    CONSTRAINT ck_unit_deliveries_failure CHECK (((((delivery_status)::text = 'FAILED'::text) AND (failed_at IS NOT NULL) AND (failure_type IS NOT NULL) AND (failure_reason IS NOT NULL)) OR (((delivery_status)::text <> 'FAILED'::text) AND (failed_at IS NULL) AND (failure_type IS NULL) AND (failure_reason IS NULL)))),
    CONSTRAINT ck_unit_deliveries_method CHECK (((fulfillment_method)::text = ANY ((ARRAY['CUSTOMER_PICKUP'::character varying, 'SUPPLIER_DELIVERY'::character varying])::text[]))),
    CONSTRAINT ck_unit_deliveries_region_not_blank CHECK ((btrim((region)::text) <> ''::text)),
    CONSTRAINT ck_unit_deliveries_status CHECK (((delivery_status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: unit_returns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.unit_returns (
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    received_at timestamp with time zone NOT NULL,
    inbound_condition character varying(500) NOT NULL,
    notes character varying(2000),
    late_days integer DEFAULT 0 NOT NULL,
    late_fee numeric(19,4) DEFAULT 0 NOT NULL,
    late_fee_currency character(3) DEFAULT 'COP'::bpchar NOT NULL,
    CONSTRAINT ck_unit_returns_condition_not_blank CHECK ((btrim((inbound_condition)::text) <> ''::text)),
    CONSTRAINT ck_unit_returns_late_values CHECK (((late_days >= 0) AND (late_fee >= (0)::numeric) AND (late_fee_currency ~ '^[A-Z]{3}$'::text)))
);


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id smallint NOT NULL
);


--
-- Name: user_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_sessions (
    session_id uuid NOT NULL,
    family_id uuid NOT NULL,
    user_id uuid NOT NULL,
    refresh_token_hash character(64) NOT NULL,
    session_status character varying(20) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    rotated_at timestamp with time zone,
    revoked_at timestamp with time zone,
    replaced_by_session_id uuid,
    client_label character varying(300),
    CONSTRAINT ck_user_sessions_status CHECK (((session_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ROTATED'::character varying, 'REVOKED'::character varying, 'COMPROMISED'::character varying, 'EXPIRED'::character varying])::text[]))),
    CONSTRAINT ck_user_sessions_time CHECK ((created_at < expires_at)),
    CONSTRAINT ck_user_sessions_transition_time CHECK (((((session_status)::text = 'ACTIVE'::text) AND (rotated_at IS NULL) AND (revoked_at IS NULL)) OR (((session_status)::text = 'ROTATED'::text) AND (rotated_at IS NOT NULL) AND (revoked_at IS NULL)) OR (((session_status)::text = ANY ((ARRAY['REVOKED'::character varying, 'COMPROMISED'::character varying])::text[])) AND (revoked_at IS NOT NULL)) OR ((session_status)::text = 'EXPIRED'::text)))
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    user_id uuid NOT NULL,
    email character varying(320) NOT NULL,
    display_name character varying(200) NOT NULL,
    password_hash character varying(255) NOT NULL,
    account_status character varying(20) NOT NULL,
    registered_at timestamp with time zone NOT NULL,
    CONSTRAINT ck_users_account_status CHECK (((account_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SUSPENDED'::character varying, 'DISABLED'::character varying])::text[]))),
    CONSTRAINT ck_users_display_name_not_blank CHECK ((btrim((display_name)::text) <> ''::text)),
    CONSTRAINT ck_users_email_not_blank CHECK ((btrim((email)::text) <> ''::text))
);


--
-- Name: cancellation_policies cancellation_policies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cancellation_policies
    ADD CONSTRAINT cancellation_policies_pkey PRIMARY KEY (cancellation_policy_id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (category_id);


--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (user_id);


--
-- Name: damage_reports damage_reports_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.damage_reports
    ADD CONSTRAINT damage_reports_pkey PRIMARY KEY (damage_report_id);


--
-- Name: delivery_rates delivery_rates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delivery_rates
    ADD CONSTRAINT delivery_rates_pkey PRIMARY KEY (delivery_rate_id);


--
-- Name: equipment_units equipment_units_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment_units
    ADD CONSTRAINT equipment_units_pkey PRIMARY KEY (equipment_unit_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: invoice_items invoice_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice_items
    ADD CONSTRAINT invoice_items_pkey PRIMARY KEY (invoice_id, line_number);


--
-- Name: invoices invoices_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_number_key UNIQUE (number);


--
-- Name: invoices invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (invoice_id);


--
-- Name: invoices invoices_reservation_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_reservation_id_key UNIQUE (reservation_id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (password_reset_token_id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (payment_id);


--
-- Name: refunds refunds_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT refunds_pkey PRIMARY KEY (refund_id);


--
-- Name: rentals rentals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rentals
    ADD CONSTRAINT rentals_pkey PRIMARY KEY (reservation_id);


--
-- Name: reservation_extensions reservation_extensions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_extensions
    ADD CONSTRAINT reservation_extensions_pkey PRIMARY KEY (reservation_extension_id);


--
-- Name: reservation_items reservation_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_items
    ADD CONSTRAINT reservation_items_pkey PRIMARY KEY (reservation_id, equipment_unit_id);


--
-- Name: reservation_terminations reservation_terminations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_terminations
    ADD CONSTRAINT reservation_terminations_pkey PRIMARY KEY (reservation_termination_id);


--
-- Name: reservations reservations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_pkey PRIMARY KEY (reservation_id);


--
-- Name: roles roles_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_code_key UNIQUE (code);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- Name: suppliers suppliers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (user_id);


--
-- Name: tool_images tool_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool_images
    ADD CONSTRAINT tool_images_pkey PRIMARY KEY (tool_id, image_url);


--
-- Name: tools tools_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tools
    ADD CONSTRAINT tools_pkey PRIMARY KEY (tool_id);


--
-- Name: unit_deliveries unit_deliveries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit_deliveries
    ADD CONSTRAINT unit_deliveries_pkey PRIMARY KEY (reservation_id, equipment_unit_id);


--
-- Name: unit_returns unit_returns_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit_returns
    ADD CONSTRAINT unit_returns_pkey PRIMARY KEY (reservation_id, equipment_unit_id);


--
-- Name: cancellation_policies uq_cancellation_policies_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cancellation_policies
    ADD CONSTRAINT uq_cancellation_policies_version UNIQUE (policy_version);


--
-- Name: customers uq_customers_document; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT uq_customers_document UNIQUE (document_type, document_number);


--
-- Name: delivery_rates uq_delivery_rates_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delivery_rates
    ADD CONSTRAINT uq_delivery_rates_version UNIQUE (rate_version);


--
-- Name: password_reset_tokens uq_password_reset_tokens_hash; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uq_password_reset_tokens_hash UNIQUE (token_hash);


--
-- Name: payments uq_payments_processor_reference; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uq_payments_processor_reference UNIQUE (processor, external_reference);


--
-- Name: refunds uq_refunds_idempotency; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT uq_refunds_idempotency UNIQUE (idempotency_key);


--
-- Name: reservation_extensions uq_reservation_extensions_idempotency; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_extensions
    ADD CONSTRAINT uq_reservation_extensions_idempotency UNIQUE (idempotency_key);


--
-- Name: reservation_terminations uq_reservation_terminations_idempotency; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_terminations
    ADD CONSTRAINT uq_reservation_terminations_idempotency UNIQUE (idempotency_key);


--
-- Name: suppliers uq_suppliers_document; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT uq_suppliers_document UNIQUE (document_type, document_number);


--
-- Name: user_sessions uq_user_sessions_refresh_hash; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT uq_user_sessions_refresh_hash UNIQUE (refresh_token_hash);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: user_sessions user_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_pkey PRIMARY KEY (session_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: ix_damage_reports_return; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_damage_reports_return ON public.damage_reports USING btree (reservation_id, equipment_unit_id);


--
-- Name: ix_equipment_units_inventory_code_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_equipment_units_inventory_code_lower ON public.equipment_units USING btree (lower((inventory_code)::text));


--
-- Name: ix_equipment_units_tool_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_equipment_units_tool_id ON public.equipment_units USING btree (tool_id);


--
-- Name: ix_password_reset_tokens_user_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_password_reset_tokens_user_active ON public.password_reset_tokens USING btree (user_id, expires_at) WHERE ((used_at IS NULL) AND (revoked_at IS NULL));


--
-- Name: ix_payments_reservation_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_payments_reservation_id ON public.payments USING btree (reservation_id);


--
-- Name: ix_refunds_payment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_refunds_payment ON public.refunds USING btree (payment_id);


--
-- Name: ix_refunds_termination; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_refunds_termination ON public.refunds USING btree (reservation_termination_id);


--
-- Name: ix_reservation_items_equipment_unit_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_reservation_items_equipment_unit_id ON public.reservation_items USING btree (equipment_unit_id, reservation_id);


--
-- Name: ix_reservations_customer_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_reservations_customer_id ON public.reservations USING btree (customer_id);


--
-- Name: ix_reservations_status_period; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_reservations_status_period ON public.reservations USING btree (reservation_status, start_date, return_date);


--
-- Name: ix_tools_category_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_tools_category_id ON public.tools USING btree (category_id);


--
-- Name: ix_tools_supplier_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_tools_supplier_id ON public.tools USING btree (supplier_id);


--
-- Name: ix_user_sessions_family; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_user_sessions_family ON public.user_sessions USING btree (family_id, session_status);


--
-- Name: ix_user_sessions_user_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_user_sessions_user_status ON public.user_sessions USING btree (user_id, session_status, expires_at);


--
-- Name: ux_cancellation_policies_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_cancellation_policies_active ON public.cancellation_policies USING btree (active) WHERE active;


--
-- Name: ux_categories_name_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_categories_name_lower ON public.categories USING btree (lower((name)::text));


--
-- Name: ux_delivery_rates_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_delivery_rates_active ON public.delivery_rates USING btree (active) WHERE active;


--
-- Name: ux_payments_confirmed_stage; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_payments_confirmed_stage ON public.payments USING btree (reservation_id, payment_stage) WHERE (((payment_status)::text = 'CONFIRMED'::text) AND ((payment_stage)::text = ANY ((ARRAY['FIRST_HALF'::character varying, 'FINAL_HALF'::character varying])::text[])));


--
-- Name: ux_payments_idempotency_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_payments_idempotency_key ON public.payments USING btree (idempotency_key);


--
-- Name: ux_reservation_extensions_open; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_reservation_extensions_open ON public.reservation_extensions USING btree (reservation_id) WHERE ((extension_status)::text = 'REQUESTED'::text);


--
-- Name: ux_reservation_terminations_open; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_reservation_terminations_open ON public.reservation_terminations USING btree (reservation_id) WHERE ((termination_status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPLIED'::character varying, 'APPROVED'::character varying])::text[]));


--
-- Name: ux_users_email_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_users_email_lower ON public.users USING btree (lower((email)::text));


--
-- Name: equipment_units trg_equipment_units_supplier_inventory_code; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_equipment_units_supplier_inventory_code BEFORE INSERT OR UPDATE OF tool_id, inventory_code ON public.equipment_units FOR EACH ROW EXECUTE FUNCTION public.enforce_supplier_inventory_code_uniqueness();


--
-- Name: customers customers_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE RESTRICT;


--
-- Name: equipment_units equipment_units_tool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment_units
    ADD CONSTRAINT equipment_units_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES public.tools(tool_id) ON DELETE RESTRICT;


--
-- Name: damage_reports fk_damage_reports_return; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.damage_reports
    ADD CONSTRAINT fk_damage_reports_return FOREIGN KEY (reservation_id, equipment_unit_id) REFERENCES public.unit_returns(reservation_id, equipment_unit_id) ON DELETE RESTRICT;


--
-- Name: unit_deliveries fk_unit_deliveries_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit_deliveries
    ADD CONSTRAINT fk_unit_deliveries_item FOREIGN KEY (reservation_id, equipment_unit_id) REFERENCES public.reservation_items(reservation_id, equipment_unit_id) ON DELETE RESTRICT;


--
-- Name: unit_returns fk_unit_returns_delivery; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit_returns
    ADD CONSTRAINT fk_unit_returns_delivery FOREIGN KEY (reservation_id, equipment_unit_id) REFERENCES public.unit_deliveries(reservation_id, equipment_unit_id) ON DELETE RESTRICT;


--
-- Name: invoice_items invoice_items_invoice_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice_items
    ADD CONSTRAINT invoice_items_invoice_id_fkey FOREIGN KEY (invoice_id) REFERENCES public.invoices(invoice_id) ON DELETE CASCADE;


--
-- Name: invoices invoices_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.rentals(reservation_id) ON DELETE RESTRICT;


--
-- Name: password_reset_tokens password_reset_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: payments payments_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.reservations(reservation_id) ON DELETE RESTRICT;


--
-- Name: refunds refunds_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT refunds_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES public.payments(payment_id) ON DELETE RESTRICT;


--
-- Name: refunds refunds_reservation_termination_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT refunds_reservation_termination_id_fkey FOREIGN KEY (reservation_termination_id) REFERENCES public.reservation_terminations(reservation_termination_id) ON DELETE RESTRICT;


--
-- Name: rentals rentals_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rentals
    ADD CONSTRAINT rentals_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.reservations(reservation_id) ON DELETE RESTRICT;


--
-- Name: reservation_extensions reservation_extensions_requested_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_extensions
    ADD CONSTRAINT reservation_extensions_requested_by_fkey FOREIGN KEY (requested_by) REFERENCES public.users(user_id) ON DELETE RESTRICT;


--
-- Name: reservation_extensions reservation_extensions_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_extensions
    ADD CONSTRAINT reservation_extensions_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.reservations(reservation_id) ON DELETE RESTRICT;


--
-- Name: reservation_extensions reservation_extensions_resolved_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_extensions
    ADD CONSTRAINT reservation_extensions_resolved_by_fkey FOREIGN KEY (resolved_by) REFERENCES public.users(user_id) ON DELETE RESTRICT;


--
-- Name: reservation_items reservation_items_equipment_unit_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_items
    ADD CONSTRAINT reservation_items_equipment_unit_id_fkey FOREIGN KEY (equipment_unit_id) REFERENCES public.equipment_units(equipment_unit_id) ON DELETE RESTRICT;


--
-- Name: reservation_items reservation_items_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_items
    ADD CONSTRAINT reservation_items_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.reservations(reservation_id) ON DELETE CASCADE;


--
-- Name: reservation_terminations reservation_terminations_requested_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_terminations
    ADD CONSTRAINT reservation_terminations_requested_by_fkey FOREIGN KEY (requested_by) REFERENCES public.users(user_id) ON DELETE RESTRICT;


--
-- Name: reservation_terminations reservation_terminations_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_terminations
    ADD CONSTRAINT reservation_terminations_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.reservations(reservation_id) ON DELETE RESTRICT;


--
-- Name: reservation_terminations reservation_terminations_resolved_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation_terminations
    ADD CONSTRAINT reservation_terminations_resolved_by_fkey FOREIGN KEY (resolved_by) REFERENCES public.users(user_id) ON DELETE RESTRICT;


--
-- Name: reservations reservations_cancellation_policy_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_cancellation_policy_id_fkey FOREIGN KEY (cancellation_policy_id) REFERENCES public.cancellation_policies(cancellation_policy_id) ON DELETE RESTRICT;


--
-- Name: reservations reservations_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(user_id) ON DELETE RESTRICT;


--
-- Name: reservations reservations_delivery_rate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_delivery_rate_id_fkey FOREIGN KEY (delivery_rate_id) REFERENCES public.delivery_rates(delivery_rate_id) ON DELETE RESTRICT;


--
-- Name: suppliers suppliers_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE RESTRICT;


--
-- Name: tool_images tool_images_tool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool_images
    ADD CONSTRAINT tool_images_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES public.tools(tool_id) ON DELETE CASCADE;


--
-- Name: tools tools_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tools
    ADD CONSTRAINT tools_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(category_id) ON DELETE RESTRICT;


--
-- Name: tools tools_supplier_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tools
    ADD CONSTRAINT tools_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES public.suppliers(user_id) ON DELETE RESTRICT;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(role_id) ON DELETE RESTRICT;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: user_sessions user_sessions_replaced_by_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_replaced_by_session_id_fkey FOREIGN KEY (replaced_by_session_id) REFERENCES public.user_sessions(session_id) ON DELETE SET NULL;


--
-- Name: user_sessions user_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict ssPdALK9fRQKTudbfDo1O8FPRzOkaXoZLP6iKMeS45xJ30F1Uvo9DDlNaPhWbad

