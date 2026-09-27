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

# 阶梯式并发测试配置
THREAD_LEVELS = [10, 50, 100, 200, 500, 1000]
RESULTS = []

# 全局计数器
counter_lock = threading.Lock()
user_id_counter = 0

def get_next_user_id():
    """生成唯一的用户ID"""
    global user_id_counter
    with counter_lock:
        user_id_counter += 1
        return 100000 + user_id_counter

def run_query_test(thread_id, exam_paper_id, user_id):
    """模拟查询操作（防重复检查）"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        start_time = time.time()
        
        # 查询是否已交卷（防重复检查）
        check_sql = "SELECT id FROM t_exam_paper_answer WHERE exam_paper_id = %s AND create_user = %s"
        cursor.execute(check_sql, (exam_paper_id, user_id))
        existing = cursor.fetchone()
        
        end_time = time.time()
        elapsed_ms = (end_time - start_time) * 1000
        
        cursor.close()
        conn.close()
        
        return {
            'thread_id': thread_id,
            'elapsed_ms': elapsed_ms,
            'success': True,
            'found': existing is not None
        }
        
    except Exception as e:
        return {
            'thread_id': thread_id,
            'elapsed_ms': 0,
            'success': False,
            'error': str(e)
        }

def run_insert_test(thread_id, exam_paper_id, user_id):
    """模拟插入操作"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        start_time = time.time()
        
        # 插入答题记录
        insert_sql = """
            INSERT INTO t_exam_paper_answer 
            (paper_name, do_time, exam_paper_id, create_user, create_time, update_time, subject_id, 
             question_count, paper_score, paper_type, system_score, user_score, 
             task_exam_id, question_correct, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        
        now = time.strftime('%Y-%m-%d %H:%M:%S')
        cursor.execute(insert_sql, (
            f'测试试卷{exam_paper_id}',
            1800,
            exam_paper_id,
            user_id,
            now,
            now,
            1,
            50,
            100,
            1,
            85,
            85,
            None,
            45,
            2
        ))
        
        conn.commit()
        end_time = time.time()
        elapsed_ms = (end_time - start_time) * 1000
        
        cursor.close()
        conn.close()
        
        return {
            'thread_id': thread_id,
            'elapsed_ms': elapsed_ms,
            'success': True
        }
        
    except Exception as e:
        return {
            'thread_id': thread_id,
            'elapsed_ms': 0,
            'success': False,
            'error': str(e)
        }

def run_concurrent_query_test(thread_count, test_name, description):
    """运行并发查询测试"""
    print(f"\n{'='*70}")
    print(f"测试: {test_name}")
    print(f"并发数: {thread_count}")
    print(f"描述: {description}")
    print(f"{'='*70}")
    
    results = []
    
    with ThreadPoolExecutor(max_workers=thread_count) as executor:
        future_to_id = {}
        for i in range(thread_count):
            exam_paper_id = (i % 10) + 1
            user_id = get_next_user_id()
            future = executor.submit(run_query_test, i+1, exam_paper_id, user_id)
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

def run_concurrent_insert_test(thread_count, test_name, description):
    """运行并发插入测试"""
    print(f"\n{'='*70}")
    print(f"测试: {test_name}")
    print(f"并发数: {thread_count}")
    print(f"描述: {description}")
    print(f"{'='*70}")
    
    results = []
    
    with ThreadPoolExecutor(max_workers=thread_count) as executor:
        future_to_id = {}
        for i in range(thread_count):
            exam_paper_id = (i % 10) + 1
            user_id = get_next_user_id()
            future = executor.submit(run_insert_test, i+1, exam_paper_id, user_id)
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
        cursor.execute("DELETE FROM t_exam_paper_answer WHERE create_user > 100000")
        conn.commit()
        cursor.close()
        conn.close()
        print("✓ 测试数据已清理")
    except Exception as e:
        print(f"✗ 清理测试数据失败: {e}")

def add_index():
    """添加联合索引"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("ALTER TABLE t_exam_paper_answer ADD INDEX idx_exam_user (exam_paper_id, create_user);")
        conn.commit()
        cursor.close()
        conn.close()
        print("✓ 联合索引已创建: idx_exam_user (exam_paper_id, create_user)")
    except Exception as e:
        print(f"✗ 创建索引失败: {e}")

def drop_index():
    """删除联合索引"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("ALTER TABLE t_exam_paper_answer DROP INDEX idx_exam_user;")
        conn.commit()
        cursor.close()
        conn.close()
        print("✓ 联合索引已删除")
    except Exception as e:
        print(f"✗ 删除索引失败: {e}")

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
        print(f"✗ 检查索引失败: {e}")
        return False

def print_comparison_table(with_index_results, without_index_results, test_type):
    """打印对比表格"""
    print(f"\n{'='*100}")
    print(f"【{test_type}】性能对比报告")
    print(f"{'='*100}")
    
    print(f"\n| 并发数 | 测试类型 | 平均RT | 最小RT | 最大RT | 中位数RT | P95 RT | P99 RT | 错误率 | TPS |")
    print(f"|--------|----------|--------|--------|--------|----------|--------|--------|--------|-----|")
    
    for i in range(len(with_index_results)):
        with_r = with_index_results[i]
        without_r = without_index_results[i]
        
        print(f"| {with_r['threads']} | 有索引 | {with_r['avg_rt']:.2f}ms | {with_r['min_rt']:.2f}ms | {with_r['max_rt']:.2f}ms | {with_r['median_rt']:.2f}ms | {with_r['p95_rt']:.2f}ms | {with_r['p99_rt']:.2f}ms | {with_r['error_rate']:.2f}% | {with_r['tps']:.2f}/s |")
        print(f"| {without_r['threads']} | 无索引 | {without_r['avg_rt']:.2f}ms | {without_r['min_rt']:.2f}ms | {without_r['max_rt']:.2f}ms | {without_r['median_rt']:.2f}ms | {without_r['p95_rt']:.2f}ms | {without_r['p99_rt']:.2f}ms | {without_r['error_rate']:.2f}% | {without_r['tps']:.2f}/s |")
        print(f"|--------|----------|--------|--------|--------|----------|--------|--------|--------|-----|")
        
        if without_r['avg_rt'] > 0:
            speedup = with_r['avg_rt'] / without_r['avg_rt']
            print(f"|        | 性能提升 | {speedup:.2f}x |        |        |          |        |        |        |     |")
            print(f"|--------|----------|--------|--------|--------|----------|--------|--------|--------|-----|")

def main():
    global user_id_counter
    
    print("="*100)
    print("阶梯式压测：无索引全表查询 vs 联合索引性能对比")
    print("="*100)
    print(f"\n测试阶梯: {THREAD_LEVELS}")
    print(f"测试表: t_exam_paper_answer")
    print(f"测试索引: idx_exam_user (exam_paper_id, create_user)")
    
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
        time.sleep(2)
    
    # ==================== 第一阶段：无索引测试 ====================
    print("\n\n" + "="*100)
    print("【第一阶段】无索引测试（全表扫描）")
    print("="*100)
    
    without_index_query_results = []
    without_index_insert_results = []
    
    for threads in THREAD_LEVELS:
        user_id_counter = 0
        result = run_concurrent_query_test(
            threads,
            f"无索引-查询-{threads}并发",
            "无索引（全表扫描）- 查询操作"
        )
        without_index_query_results.append(result)
        time.sleep(2)
    
    cleanup_test_data()
    time.sleep(1)
    
    for threads in THREAD_LEVELS:
        user_id_counter = 0
        result = run_concurrent_insert_test(
            threads,
            f"无索引-插入-{threads}并发",
            "无索引（全表扫描）- 插入操作"
        )
        without_index_insert_results.append(result)
        time.sleep(2)
    
    # ==================== 第二阶段：有索引测试 ====================
    print("\n\n" + "="*100)
    print("【第二阶段】有联合索引测试")
    print("="*100)
    
    print("\n【准备】创建联合索引...")
    add_index()
    time.sleep(3)
    
    cleanup_test_data()
    time.sleep(1)
    
    with_index_query_results = []
    with_index_insert_results = []
    
    for threads in THREAD_LEVELS:
        user_id_counter = 0
        result = run_concurrent_query_test(
            threads,
            f"有索引-查询-{threads}并发",
            "有联合索引 (exam_paper_id, create_user) - 查询操作"
        )
        with_index_query_results.append(result)
        time.sleep(2)
    
    cleanup_test_data()
    time.sleep(1)
    
    for threads in THREAD_LEVELS:
        user_id_counter = 0
        result = run_concurrent_insert_test(
            threads,
            f"有索引-插入-{threads}并发",
            "有联合索引 (exam_paper_id, create_user) - 插入操作"
        )
        with_index_insert_results.append(result)
        time.sleep(2)
    
    # ==================== 生成对比报告 ====================
    print_comparison_table(with_index_query_results, without_index_query_results, "查询操作")
    print_comparison_table(with_index_insert_results, without_index_insert_results, "插入操作")
    
    # ==================== 保存CSV ====================
    output_path = os.path.join(os.path.dirname(__file__), 'reports', 'index_comparison_results.csv')
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['测试类型', '索引状态', '并发数', '总请求', '成功', '失败', '错误率%', 
                        '平均RT(ms)', '最小RT(ms)', '最大RT(ms)', '中位数RT(ms)', 'P95RT(ms)', 'P99RT(ms)', 'TPS'])
        
        for r in with_index_query_results:
            writer.writerow(['查询', '有索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}", f"{r['p99_rt']:.2f}",
                           f"{r['tps']:.2f}"])
        
        for r in without_index_query_results:
            writer.writerow(['查询', '无索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}", f"{r['p99_rt']:.2f}",
                           f"{r['tps']:.2f}"])
        
        for r in with_index_insert_results:
            writer.writerow(['插入', '有索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}", f"{r['p99_rt']:.2f}",
                           f"{r['tps']:.2f}"])
        
        for r in without_index_insert_results:
            writer.writerow(['插入', '无索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}", f"{r['p99_rt']:.2f}",
                           f"{r['tps']:.2f}"])
    
    print(f"\n\n{'='*100}")
    print(f"详细结果已保存到: {output_path}")
    print(f"{'='*100}")

if __name__ == '__main__':
    main()
