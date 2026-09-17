-- V2: PostgreSQL 전용 부분 일치 검색 인덱스 (REQ-NF-002, Could)
-- Neon에 pg_trgm 확장이 없거나 권한이 없으면 배포 문서 절차에 따라 수동 생성한다.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_store_name_trgm ON store USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_store_region_trgm ON store USING GIN (region gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_review_title_trgm ON review USING GIN (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_review_content_trgm ON review USING GIN (content gin_trgm_ops);
