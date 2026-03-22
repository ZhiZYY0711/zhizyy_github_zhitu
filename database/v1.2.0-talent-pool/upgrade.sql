-- =====================================================
-- Migration: 002_add_talent_pool_soft_delete.sql
-- Description: Add is_deleted column to talent_pool table for soft delete support
-- Date: 2024
-- Related Spec: .kiro/specs/missing-api-endpoints (Task 5.10)
-- =====================================================

-- Add is_deleted column to talent_pool table
ALTER TABLE enterprise_svc.talent_pool 
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for soft delete queries
CREATE INDEX idx_talent_deleted ON enterprise_svc.talent_pool(tenant_id) WHERE is_deleted = FALSE;

COMMENT ON COLUMN enterprise_svc.talent_pool.is_deleted IS '软删除标记';
