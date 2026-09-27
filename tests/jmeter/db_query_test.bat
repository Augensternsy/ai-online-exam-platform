@echo off
chcp 65001 >nul
echo ============================================================
echo 数据库查询性能对比测试
echo 测试 SQL: SELECT id, status FROM t_exam_paper_answer
echo          WHERE exam_paper_id = ? AND create_user = ?
echo ============================================================

echo.
echo 【第一阶段】有联合索引测试
echo 索引: idx_exam_user (exam_paper_id, create_user)
echo.

REM 测试有索引 - 50并发
echo --- 有索引 50 并发 ---
for /L %%i in (1,1,50) do (
  start /B mysql -uxzs -pxzs123456 ems -e "SELECT id, status FROM t_exam_paper_answer WHERE exam_paper_id = %%i AND create_user = %%i;" >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo.
echo 【准备】删除联合索引...
mysql -uxzs -pxzs123456 ems -e "ALTER TABLE t_exam_paper_answer DROP INDEX idx_exam_user;"
echo 索引已删除！
timeout /t 2 /nobreak >nul

echo.
echo 【第二阶段】无索引测试（全表扫描）
echo.

REM 测试无索引 - 50并发
echo --- 无索引 50 并发 ---
for /L %%i in (1,1,50) do (
  start /B mysql -uxzs -pxzs123456 ems -e "SELECT id, status FROM t_exam_paper_answer WHERE exam_paper_id = %%i AND create_user = %%i;" >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo.
echo 【恢复】重新创建联合索引...
mysql -uxzs -pxzs123456 ems -e "ALTER TABLE t_exam_paper_answer ADD INDEX idx_exam_user (exam_paper_id, create_user);"
echo 索引已恢复！

echo.
echo 测试完成！
