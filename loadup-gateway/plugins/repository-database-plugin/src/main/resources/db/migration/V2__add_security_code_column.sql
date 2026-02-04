-- Add securityCode column to gateway_routes table
-- This column stores the authentication/authorization strategy code
-- Valid values: OFF, default, signature, internal, or custom codes

ALTER TABLE gateway_routes
ADD COLUMN security_code VARCHAR(32) NULL COMMENT 'Security strategy code (OFF/default/signature/internal)'
AFTER target;

-- Set default value for existing rows
UPDATE gateway_routes
SET security_code = 'default'
WHERE security_code IS NULL;

-- You can optionally make it NOT NULL after setting defaults
-- ALTER TABLE gateway_routes MODIFY COLUMN security_code VARCHAR(32) NOT NULL DEFAULT 'default';
