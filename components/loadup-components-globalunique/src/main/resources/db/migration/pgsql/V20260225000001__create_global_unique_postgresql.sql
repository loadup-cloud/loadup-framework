-- PostgreSQL Schema for Global Unique
-- Table: global_unique (可通过 tablePrefix 配置前缀)

CREATE TABLE IF NOT EXISTS global_unique (
    id VARCHAR(64) NOT NULL,
    unique_key VARCHAR(255) NOT NULL,
    biz_type VARCHAR(50) NOT NULL,
    biz_id VARCHAR(100),
    request_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_unique_key UNIQUE (unique_key)
);

CREATE INDEX IF NOT EXISTS idx_biz_type ON global_unique(biz_type);
CREATE INDEX IF NOT EXISTS idx_created_at ON global_unique(created_at);

COMMENT ON TABLE global_unique IS '全局唯一性控制表';
COMMENT ON COLUMN global_unique.id IS '主键';
COMMENT ON COLUMN global_unique.unique_key IS '唯一键(业务方自定义)';
COMMENT ON COLUMN global_unique.biz_type IS '业务类型';
COMMENT ON COLUMN global_unique.biz_id IS '业务ID(可选)';
COMMENT ON COLUMN global_unique.request_data IS '请求数据快照(可选,用于问题排查)';
COMMENT ON COLUMN global_unique.created_at IS '创建时间';
COMMENT ON COLUMN global_unique.updated_at IS '更新时间';

-- 创建触发器以自动更新 updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_global_unique_updated_at BEFORE UPDATE
    ON global_unique FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

