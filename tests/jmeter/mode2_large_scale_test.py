import pymysql
import time
import statistics
import csv
import os
import sys
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

# 设置控制台编码为 UTF-8
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'xzs',
    'password': 'xzs123456',
    'database': 'ems',
    'charset': 'utf8mb4'
}

# 大规模阶梯式并发测试配置
THREAD_LEVELS = [50, 100, 200, 500, 1000, 2000, 5000, 10000]

# 全局计数器
counter_lock = threading.Lock()
user_id_counter = 0

def get_next_user_id():
    """生成唯一的用户ID"""
    global user_id_counter
    with counter_lock:
        user_id_counter += 1
        return 300000 + user_id_counter

def prepare_test_data(count):
    """预创建测试用的答卷记录"""
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    batch_size = 1000
    total_inserted = 0
    
    for batch_start in range(0, count, batch_size):
        batch_end = min(batch_start + batch_size, count)
        batch_count = batch_end - batch_start
        
        values = []
        for i in range(batch_count):
            exam_paper_id = (i % 10) + 1
            user_id = 300000 + batch_start + i + 1
            now = time.strftime('%Y-%m-%d %H:%M:%S')
            
            values.append((
                f'测试试卷{exam_paper_id}',
                0,
                exam_paper_id,
                user_id,
                now,
                now,
                1,
                50,
                100,
                1,
                0,
                0,
                None,
                0,
                0
            ))
        
        insert_sql = """
            INSERT INTO t_exam_paper_answer 
            (paper_name, do_time, exam_paper_id, create_user, create_time, update_time, subject_id, 
             question_count, paper_score, paper_type, system_score, user_score, 
             task_exam_id, question_correct, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        
        cursor.executemany(insert_sql, values)
        conn.commit()
        total_inserted += batch_count
    
    cursor.close()
    conn.close()
    
    return total_inserted

def run_update_by_exam_user_test(thread_id, exam_paper_id, user_id):
    """模拟模式二自动保存操作（UPDATE）"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        start_time = time.time()
        
        # 模式二实际场景：通过 exam_paper_id 和 create_user 定位记录并更新
        update_sql = """
            UPDATE t_exam_paper_answer 
            SET do_time = %s, update_time = %s, system_score = %s, user_score = %s, 
                question_correct = %s
            WHERE exam_paper_id = %s AND create_user = %s
        """
        
        cursor.execute(update_sql, (
            600,  # do_time (10分钟)
            time.strftime('%Y-%m-%d %H:%M:%S'),
            25,  # system_score
            25,  # user_score
            15,  # question_correct
            exam_paper_id,
            user_id
        ))
        
        affected_rows = cursor.rowcount
        conn.commit()
        
        end_time = time.time()
        elapsed_ms = (end_time - start_time) * 1000
        
        cursor.close()
        conn.close()
        
        return {
            'thread_id': thread_id,
            'elapsed_ms': elapsed_ms,
            'success': True,
            'affected_rows': affected_rows
        }
        
    except Exception as e:
        return {
            'thread_id': thread_id,
            'elapsed_ms': 0,
            'success': False,
            'error': str(e)
        }

def run_concurrent_update_test(thread_count, test_name, description):
    """运行并发UPDATE测试"""
    print(f"\n{'='*80}")
    print(f"测试: {test_name}")
    print(f"并发数: {thread_count}")
    print(f"描述: {description}")
    print(f"{'='*80}")
    
    results = []
    
    with ThreadPoolExecutor(max_workers=thread_count) as executor:
        future_to_id = {}
        for i in range(thread_count):
            exam_paper_id = (i % 10) + 1
            user_id = 300000 + i + 1
            future = executor.submit(run_update_by_exam_user_test, i+1, exam_paper_id, user_id)
            future_to_id[future] = i+1
        
        for future in as_completed(future_to_id):
            result = future.result()
            results.append(result)
    
    success_count = sum(1 for r in results if r['success'])
    error_count = sum(1 for r in results if not r['success'])
    response_times = [r['elapsed_ms'] for r in results if r['success']]
    
    if response_times:
        avg_rt = statistics.mean(response_times)
        min_rt = min(response_times)
        max_rt = max(response_times)
        median_rt = statistics.median(response_times)
        p95_rt = sorted(response_times)[int(len(response_times) * 0.95)] if len(response_times) > 1 else response_times[0]
        p99_rt = sorted(response_times)[int(len(response_times) * 0.99)] if len(response_times) > 1 else response_times[0]
    else:
        avg_rt = min_rt = max_rt = median_rt = p95_rt = p99_rt = 0
    
    total_time = sum(response_times) / 1000 if response_times else 0
    tps = success_count / total_time if total_time > 0 else 0
    error_rate = (error_count / thread_count) * 100
    
    print(f"\n结果:")
    print(f"  总请求数: {thread_count}")
    print(f"  成功: {success_count}, 失败: {error_count}")
    print(f"  错误率: {error_rate:.2f}%")
    print(f"  平均响应时间: {avg_rt:.2f}ms")
    print(f"  最小响应时间: {min_rt:.2f}ms")
    print(f"  最大响应时间: {max_rt:.2f}ms")
    print(f"  中位数响应时间: {median_rt:.2f}ms")
    print(f"  P95响应时间: {p95_rt:.2f}ms")
    print(f"  P99响应时间: {p99_rt:.2f}ms")
    print(f"  TPS: {tps:.2f}/s")
    
    return {
        'test_name': test_name,
        'threads': thread_count,
        'total_requests': thread_count,
        'success': success_count,
        'errors': error_count,
        'error_rate': error_rate,
        'avg_rt': avg_rt,
        'min_rt': min_rt,
        'max_rt': max_rt,
        'median_rt': median_rt,
        'p95_rt': p95_rt,
        'p99_rt': p99_rt,
        'tps': tps
    }

def cleanup_test_data():
    """清理测试数据"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("  清理测试数据...")
        cursor.execute("DELETE FROM t_exam_paper_answer WHERE create_user > 100000")
        conn.commit()
        cursor.close()
        conn.close()
        print("  [OK] 测试数据已清理")
    except Exception as e:
        print(f"  [FAIL] 清理测试数据失败: {e}")

def add_index():
    """添加联合索引"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("  创建联合索引...")
        cursor.execute("ALTER TABLE t_exam_paper_answer ADD INDEX idx_exam_user (exam_paper_id, create_user);")
        conn.commit()
        cursor.close()
        conn.close()
        print("  [OK] 联合索引已创建: idx_exam_user (exam_paper_id, create_user)")
    except Exception as e:
        print(f"  [FAIL] 创建索引失败: {e}")

def drop_index():
    """删除联合索引"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("  删除联合索引...")
        cursor.execute("ALTER TABLE t_exam_paper_answer DROP INDEX idx_exam_user;")
        conn.commit()
        cursor.close()
        conn.close()
        print("  [OK] 联合索引已删除")
    except Exception as e:
        print(f"  [FAIL] 删除索引失败: {e}")

def check_index_exists():
    """检查索引是否存在"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("""
            SELECT COUNT(*) 
            FROM information_schema.statistics 
            WHERE table_schema = 'ems' 
            AND table_name = 't_exam_paper_answer' 
            AND index_name = 'idx_exam_user'
        """)
        result = cursor.fetchone()
        cursor.close()
        conn.close()
        return result[0] > 0
    except Exception as e:
        print(f"[FAIL] 检查索引失败: {e}")
        return False

def print_comparison_table(with_index_results, without_index_results):
    """打印对比表格"""
    print(f"\n{'='*120}")
    print(f"【模式二自动保存】UPDATE操作性能对比报告（有索引 vs 无索引）")
    print(f"{'='*120}")
    
    print(f"\n| 并发数 | 索引状态 | 平均RT | 最小RT | 最大RT | 中位数RT | P95 RT | P99 RT | 错误率 | TPS | 性能提升 |")
    print(f"|--------|----------|--------|--------|--------|----------|--------|--------|--------|-----|----------|")
    
    for i in range(len(with_index_results)):
        with_r = with_index_results[i]
        without_r = without_index_results[i]
        
        speedup = without_r['avg_rt'] / with_r['avg_rt'] if with_r['avg_rt'] > 0 else 0
        
        print(f"| {with_r['threads']:,} | 有索引 | {with_r['avg_rt']:.2f}ms | {with_r['min_rt']:.2f}ms | {with_r['max_rt']:.2f}ms | {with_r['median_rt']:.2f}ms | {with_r['p95_rt']:.2f}ms | {with_r['p99_rt']:.2f}ms | {with_r['error_rate']:.2f}% | {with_r['tps']:.2f}/s | {speedup:.1f}x |")
        print(f"| {without_r['threads']:,} | 无索引 | {without_r['avg_rt']:.2f}ms | {without_r['min_rt']:.2f}ms | {without_r['max_rt']:.2f}ms | {without_r['median_rt']:.2f}ms | {without_r['p95_rt']:.2f}ms | {without_r['p99_rt']:.2f}ms | {without_r['error_rate']:.2f}% | {without_r['tps']:.2f}/s | - |")
        print(f"|--------|----------|--------|--------|--------|----------|--------|--------|--------|-----|----------|")

def main():
    global user_id_counter
    
    print("="*120)
    print("模式二（实时落盘模式）大规模阶梯压测：自动保存（UPDATE操作）性能对比")
    print("="*120)
    print(f"\n测试阶梯: {THREAD_LEVELS}")
    print(f"测试表: t_exam_paper_answer")
    print(f"测试索引: idx_exam_user (exam_paper_id, create_user)")
    print(f"\n测试场景: UPDATE ... WHERE exam_paper_id = ? AND create_user = ?")
    print(f"（模拟学生答题过程中每60秒自动保存答案）")
    
    # 清理之前的测试数据
    print("\n【准备】清理测试数据...")
    cleanup_test_data()
    
    # 检查索引状态
    has_index = check_index_exists()
    print(f"\n当前索引状态: {'已存在' if has_index else '不存在'}")
    
    # 如果索引存在，先删除
    if has_index:
        print("\n【准备】删除现有索引...")
        drop_index()
        time.sleep(3)
    
    # ==================== 第一阶段：无索引测试 ====================
    print("\n\n" + "="*120)
    print("【第一阶段】无索引测试（全表扫描）")
    print("="*120)
    
    without_index_results = []
    
    for threads in THREAD_LEVELS:
        user_id_counter = 0
        
        # 预创建测试数据
        print(f"\n预创建 {threads:,} 条测试数据...")
        start_prepare = time.time()
        prepare_test_data(threads)
        prepare_time = time.time() - start_prepare
        print(f"数据创建完成，耗时: {prepare_time:.2f}秒")
        time.sleep(2)
        
        result = run_concurrent_update_test(
            threads,
            f"无索引-{threads}并发",
            "无索引 - 联合条件更新 (WHERE exam_paper_id = ? AND create_user = ?)"
        )
        without_index_results.append(result)
        time.sleep(3)
    
    # ==================== 第二阶段：有索引测试 ====================
    print("\n\n" + "="*120)
    print("【第二阶段】有联合索引测试")
    print("="*120)
    
    print("\n【准备】创建联合索引...")
    add_index()
    time.sleep(5)
    
    cleanup_test_data()
    time.sleep(2)
    
    with_index_results = []
    
    for threads in THREAD_LEVELS:
        user_id_counter = 0
        
        # 预创建测试数据
        print(f"\n预创建 {threads:,} 条测试数据...")
        start_prepare = time.time()
        prepare_test_data(threads)
        prepare_time = time.time() - start_prepare
        print(f"数据创建完成，耗时: {prepare_time:.2f}秒")
        time.sleep(2)
        
        result = run_concurrent_update_test(
            threads,
            f"有索引-{threads}并发",
            "有索引 - 联合条件更新 (WHERE exam_paper_id = ? AND create_user = ?)"
        )
        with_index_results.append(result)
        time.sleep(3)
    
    # ==================== 生成对比报告 ====================
    print_comparison_table(with_index_results, without_index_results)
    
    # ==================== 保存CSV ====================
    output_path = os.path.join(os.path.dirname(__file__), 'reports', 'mode2_large_scale_update_comparison.csv')
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['索引状态', '并发数', '总请求', '成功', '失败', '错误率%', 
                        '平均RT(ms)', '最小RT(ms)', '最大RT(ms)', '中位数RT(ms)', 'P95RT(ms)', 'P99RT(ms)', 'TPS'])
        
        for r in with_index_results:
            writer.writerow(['有索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}", f"{r['p99_rt']:.2f}",
                           f"{r['tps']:.2f}"])
        
        for r in without_index_results:
            writer.writerow(['无索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}", f"{r['p99_rt']:.2f}",
                           f"{r['tps']:.2f}"])
    
    print(f"\n\n{'='*120}")
    print(f"详细结果已保存到: {output_path}")
    print(f"{'='*120}")

if __name__ == '__main__':
    main()
