INSERT INTO roles (code, description) VALUES ('ADMIN', 'Platform administrator') ON CONFLICT (code) DO NOTHING;
