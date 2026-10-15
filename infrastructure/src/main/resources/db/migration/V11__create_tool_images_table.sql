-- Based on V8__create_tool_images_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS tool_images (
    tool_id uuid NOT NULL REFERENCES tools (tool_id) ON DELETE CASCADE,
    image_url varchar(2048) NOT NULL,
    PRIMARY KEY (tool_id, image_url),
    CONSTRAINT ck_tool_images_url_not_blank CHECK (btrim(image_url) <> '')
);
