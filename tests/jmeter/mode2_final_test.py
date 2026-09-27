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

# 测试阶梯配置
THREAD_LEVELS = [50, 100, 200, 500, 1000, 2000, 5000, 10000]

# 全局计数器
counter_lock = threading.Lock()
test_data_counter = 0

def get_test_data_id():
    """生成测试数据ID"""
    global test_data_counter
    with counter_lock:
        test_data_counter += 1
        return 400000 + test_data_counter

def cleanup_test_data():
    """清理测试数据"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("DELETE FROM t_exam_paper_answer WHERE create_user >= 400000")
        conn.commit()
        cursor.close()
        conn.close()
        print("  [OK] 测试数据已清理")
    except Exception as e:
        print(f"  [FAIL] 清理失败: {e}")

def prepare_test_data(count):
    """预创建测试数据"""
    print(f"  正在创建 {count:,} 条测试数据...")
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    batch_size = 500
    total = 0
    
    for batch_start in range(0, count, batch_size):
        batch_end = min(batch_start + batch_size, count)
        values = []
        
        for i in range(batch_start, batch_end):
            exam_paper_id = (i % 10) + 1
            user_id = 400000 + i + 1
            now = time.strftime('%Y-%m-%d %H:%M:%S')
            
            values.append((
                f'测试试卷{exam_paper_id}', 0, exam_paper_id, user_id,
                now, now, 1, 50, 100, 1, 0, 0, None, 0, 0
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
        total += len(values)
    
    cursor.close()
    conn.close()
    print(f"  [OK] 已创建 {total:,} 条数据")
    return total

def run_update_test(thread_id, exam_paper_id, user_id):
    """执行单次UPDATE测试"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        start_time = time.time()
        
        update_sql = """
            UPDATE t_exam_paper_answer 
            SET do_time = %s, update_time = %s, system_score = %s, user_score = %s, 
                question_correct = %s
            WHERE exam_paper_id = %s AND create_user = %s
        """
        
        cursor.execute(update_sql, (
            600, time.strftime('%Y-%m-%d %H:%M:%S'), 25, 25, 15,
            exam_paper_id, user_id
        ))
        
        conn.commit()
        elapsed_ms = (time.time() - start_time) * 1000
        
        cursor.close()
        conn.close()
        
        return {'success': True, 'elapsed_ms': elapsed_ms}
        
    except Exception as e:
        return {'success': False, 'elapsed_ms': 0, 'error': str(e)}

def run_test_phase(thread_count, phase_name, description):
    """运行一个测试阶段"""
    print(f"\n{'='*80}")
    print(f"测试: {phase_name}")
    print(f"并发数: {thread_count:,}")
    print(f"描述: {description}")
    print(f"{'='*80}")
    
    results = []
    
    def test_worker(i):
        exam_paper_id = (i % 10) + 1
        user_id = 400000 + i + 1
        return run_update_test(i+1, exam_paper_id, user_id)
    
    with ThreadPoolExecutor(max_workers=thread_count) as executor:
        futures = [executor.submit(test_worker, i) for i in range(thread_count)]
        
        for future in as_completed(futures):
            results.append(future.result())
    
    # 统计结果
    success_results = [r for r in results if r['success']]
    error_count = len(results) - len(success_results)
    response_times = [r['elapsed_ms'] for r in success_results]
    
    if response_times:
        avg_rt = statistics.mean(response_times)
        min_rt = min(response_times)
        max_rt = max(response_times)
        median_rt = statistics.median(response_times)
        p95_rt = sorted(response_times)[int(len(response_times) * 0.95)]
        p99_rt = sorted(response_times)[int(len(response_times) * 0.99)]
    else:
        avg_rt = min_rt = max_rt = median_rt = p95_rt = p99_rt = 0
    
    total_time = sum(response_times) / 1000
    tps = len(success_results) / total_time if total_time > 0 else 0
    error_rate = (error_count / thread_count) * 100
    
    print(f"\n结果:")
    print(f"  总请求: {thread_count:,} | 成功: {len(success_results):,} | 失败: {error_count:,}")
    print(f"  错误率: {error_rate:.2f}%")
    print(f"  平均RT: {avg_rt:.2f}ms | 最小: {min_rt:.2f}ms | 最大: {max_rt:.2f}ms")
    print(f"  中位数: {median_rt:.2f}ms | P95: {p95_rt:.2f}ms | P99: {p99_rt:.2f}ms")
    print(f"  TPS: {tps:.2f}/s")
    
    return {
        'threads': thread_count,
        'total': thread_count,
        'success': len(success_results),
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

def add_index():
    """添加索引"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("ALTER TABLE t_exam_paper_answer ADD INDEX idx_exam_user (exam_paper_id, create_user)")
        conn.commit()
        cursor.close()
        conn.close()
        print("  [OK] 索引已创建")
    except Exception as e:
        print(f"  [INFO] 索引可能已存在: {e}")

def drop_index():
    """删除索引"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("ALTER TABLE t_exam_paper_answer DROP INDEX idx_exam_user")
        conn.commit()
        cursor.close()
        conn.close()
        print("  [OK] 索引已删除")
    except Exception as e:
        print(f"  [INFO] 索引可能不存在: {e}")

def main():
    print("="*100)
    print("模式二自动保存性能压测：有索引 vs 无索引对比")
    print("="*100)
    print(f"\n测试阶梯: {[f'{x:,}' for x in THREAD_LEVELS]}")
    print(f"测试SQL: UPDATE ... WHERE exam_paper_id = ? AND create_user = ?")
    
    all_results = []
    
    # ========== 第一阶段：无索引 ==========
    print("\n\n" + "="*100)
    print("第一阶段：无索引测试（全表扫描）")
    print("="*100)
    
    print("\n准备：删除索引...")
    drop_index()
    time.sleep(2)
    
    print("\n准备：清理数据...")
    cleanup_test_data()
    time.sleep(1)
    
    for threads in THREAD_LEVELS:
        print(f"\n--- 无索引 {threads:,} 并发 ---")
        cleanup_test_data()
        prepare_test_data(threads)
        time.sleep(1)
        
        result = run_test_phase(
            threads,
            f"无索引-{threads:,}并发",
            "无索引，全表扫描"
        )
        result['index_status'] = '无索引'
        all_results.append(result)
        time.sleep(2)
    
    # ========== 第二阶段：有索引 ==========
    print("\n\n" + "="*100)
    print("第二阶段：有索引测试")
    print("="*100)
    
    print("\n准备：创建索引...")
    add_index()
    time.sleep(3)
    
    print("\n准备：清理数据...")
    cleanup_test_data()
    time.sleep(1)
    
    for threads in THREAD_LEVELS:
        print(f"\n--- 有索引 {threads:,} 并发 ---")
        cleanup_test_data()
        prepare_test_data(threads)
        time.sleep(1)
        
        result = run_test_phase(
            threads,
            f"有索引-{threads:,}并发",
            "有联合索引"
        )
        result['index_status'] = '有索引'
        all_results.append(result)
        time.sleep(2)
    
    # ========== 生成报告 ==========
    print("\n\n" + "="*120)
    print("最终对比报告")
    print("="*120)
    
    print(f"\n| 并发数 | 索引状态 | 平均RT | 最小RT | 最大RT | P95 RT | P99 RT | 错误率 | TPS | 性能比 |")
    print(f"|--------|----------|--------|--------|--------|--------|--------|--------|-----|--------|")
    
    for i in range(len(THREAD_LEVELS)):
        no_idx = all_results[i]
        with_idx = all_results[i + len(THREAD_LEVELS)]
        
        ratio = no_idx['avg_rt'] / with_idx['avg_rt'] if with_idx['avg_rt'] > 0 else 0
        
        print(f"| {THREAD_LEVELS[i]:,} | 无索引 | {no_idx['avg_rt']:.2f}ms | {no_idx['min_rt']:.2f}ms | {no_idx['max_rt']:.2f}ms | {no_idx['p95_rt']:.2f}ms | {no_idx['p99_rt']:.2f}ms | {no_idx['error_rate']:.2f}% | {no_idx['tps']:.2f}/s | - |")
        print(f"| {THREAD_LEVELS[i]:,} | 有索引 | {with_idx['avg_rt']:.2f}ms | {with_idx['min_rt']:.2f}ms | {with_idx['max_rt']:.2f}ms | {with_idx['p95_rt']:.2f}ms | {with_idx['p99_rt']:.2f}ms | {with_idx['error_rate']:.2f}% | {with_idx['tps']:.2f}/s | {ratio:.1f}x |")
        print(f"|--------|----------|--------|--------|--------|--------|--------|--------|-----|--------|")
    
    # 保存CSV
    output_path = os.path.join(os.path.dirname(__file__), 'reports', 'mode2_final_comparison.csv')
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['索引状态', '并发数', '总请求', '成功', '失败', '错误率%', 
                        '平均RT(ms)', '最小RT(ms)', '最大RT(ms)', '中位数RT(ms)', 
                        'P95RT(ms)', 'P99RT(ms)', 'TPS'])
        
        for r in all_results:
            writer.writerow([
                r['index_status'], r['threads'], r['total'], r['success'], r['errors'],
                f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}",
                f"{r['p99_rt']:.2f}", f"{r['tps']:.2f}"
            ])
    
    print(f"\n详细结果已保存到: {output_path}")

if __name__ == '__main__':
    main()
