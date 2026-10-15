CREATE TABLE tool_images (
    tool_id uuid NOT NULL REFERENCES tools (tool_id) ON DELETE CASCADE,
    image_url varchar(2048) NOT NULL,
    PRIMARY KEY (tool_id, image_url),
    CONSTRAINT ck_tool_images_url_not_blank CHECK (btrim(image_url) <> '')
);
