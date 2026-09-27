-- 模式二改造：添加 update_time 字段用于自动保存功能
-- 执行时间：2026-04-26

-- 1. 添加 update_time 字段
ALTER TABLE t_exam_paper_answer 
ADD COLUMN update_time DATETIME NULL COMMENT '最后更新时间（用于自动保存）' AFTER create_time;

-- 2. 为已有数据初始化 update_time = create_time
UPDATE t_exam_paper_answer 
SET update_time = create_time 
WHERE update_time IS NULL;

-- 3. 添加索引优化查询性能（如果还没有的话）
-- 检查是否已存在索引
SELECT COUNT(1) INTO @idx_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
  AND table_name = 't_exam_paper_answer' 
  AND index_name = 'idx_exam_user';

-- 如果不存在则创建
SET @sql = IF(@idx_exists = 0, 
    'ALTER TABLE t_exam_paper_answer ADD INDEX idx_exam_user (exam_paper_id, create_user)', 
    'SELECT "Index idx_exam_user already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 验证修改结果
DESCRIBE t_exam_paper_answer;
