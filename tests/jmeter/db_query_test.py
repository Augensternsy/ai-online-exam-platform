import pymysql
import time
import statistics
import csv
import os
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
import random

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'xzs',
    'password': 'xzs123456',
    'database': 'ems',
    'charset': 'utf8mb4'
}

# 测试配置
THREAD_LEVELS = [50, 100, 200, 500, 1000]
RESULTS = []

# 全局计数器
insert_counter = 0
counter_lock = threading.Lock()

def get_next_user_id():
    """生成唯一的用户ID"""
    global insert_counter
    with counter_lock:
        insert_counter += 1
        return insert_counter

def run_exam_submit(thread_id, test_mode='with_index'):
    """模拟完整的交卷流程"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 生成测试数据
        exam_paper_id = (thread_id % 10) + 1  # 1-10 试卷
        user_id = get_next_user_id()  # 唯一用户ID
        
        start_time = time.time()
        
        # 步骤1：查询是否已交卷（防重复检查）
        check_sql = "SELECT id FROM t_exam_paper_answer WHERE exam_paper_id = %s AND create_user = %s"
        cursor.execute(check_sql, (exam_paper_id, user_id))
        existing = cursor.fetchone()
        
        if existing:
            # 已交卷，直接返回
            end_time = time.time()
            elapsed_ms = (end_time - start_time) * 1000
            cursor.close()
            conn.close()
            return {
                'thread_id': thread_id,
                'elapsed_ms': elapsed_ms,
                'success': True,
                'is_duplicate': True
            }
        
        # 步骤2：插入答题记录
        insert_sql = """
            INSERT INTO t_exam_paper_answer 
            (paper_name, do_time, exam_paper_id, create_user, create_time, subject_id, 
             question_count, paper_score, paper_type, system_score, user_score, 
             task_exam_id, question_correct, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        
        now = time.strftime('%Y-%m-%d %H:%M:%S')
        cursor.execute(insert_sql, (
            f'测试试卷{exam_paper_id}',  # paper_name
            1800,  # do_time (秒)
            exam_paper_id,
            user_id,
            now,
            1,  # subject_id
            50,  # question_count
            100,  # paper_score
            1,  # paper_type
            85,  # system_score
            85,  # user_score
            None,  # task_exam_id
            45,  # question_correct
            2  # status (已完成)
        ))
        
        conn.commit()
        end_time = time.time()
        elapsed_ms = (end_time - start_time) * 1000
        
        cursor.close()
        conn.close()
        
        return {
            'thread_id': thread_id,
            'elapsed_ms': elapsed_ms,
            'success': True,
            'is_duplicate': False
        }
        
    except Exception as e:
        return {
            'thread_id': thread_id,
            'elapsed_ms': 0,
            'success': False,
            'error': str(e),
            'is_duplicate': False
        }

def run_exam_submit_test(thread_count, test_name, description):
    """运行并发交卷测试"""
    print(f"\n{'='*60}")
    print(f"测试: {test_name}")
    print(f"并发数: {thread_count}")
    print(f"描述: {description}")
    print(f"{'='*60}")
    
    results = []
    errors = []
    
    # 使用线程池执行并发交卷
    with ThreadPoolExecutor(max_workers=thread_count) as executor:
        # 提交所有任务
        future_to_id = {
            executor.submit(run_exam_submit, i+1, test_name): i+1 
            for i in range(thread_count)
        }
        
        # 收集结果
        for future in as_completed(future_to_id):
            result = future.result()
            results.append(result)
            if not result['success']:
                errors.append(result.get('error', 'Unknown error'))
    
    # 统计结果
    success_count = sum(1 for r in results if r['success'])
    error_count = sum(1 for r in results if not r['success'])
    duplicate_count = sum(1 for r in results if r.get('is_duplicate', False))
    response_times = [r['elapsed_ms'] for r in results if r['success']]
    
    # 计算指标
    if response_times:
        avg_rt = statistics.mean(response_times)
        min_rt = min(response_times)
        max_rt = max(response_times)
        median_rt = statistics.median(response_times)
        p95_rt = sorted(response_times)[int(len(response_times) * 0.95)] if len(response_times) > 1 else response_times[0]
    else:
        avg_rt = min_rt = max_rt = median_rt = p95_rt = 0
    
    total_time = sum(response_times) / 1000 if response_times else 0
    tps = success_count / total_time if total_time > 0 else 0
    error_rate = (error_count / thread_count) * 100
    
    print(f"\n结果:")
    print(f"  总请求数: {thread_count}")
    print(f"  成功: {success_count}, 失败: {error_count}")
    print(f"  重复提交: {duplicate_count}")
    print(f"  错误率: {error_rate:.2f}%")
    print(f"  平均响应时间: {avg_rt:.2f}ms")
    print(f"  最小响应时间: {min_rt:.2f}ms")
    print(f"  最大响应时间: {max_rt:.2f}ms")
    print(f"  中位数响应时间: {median_rt:.2f}ms")
    print(f"  P95响应时间: {p95_rt:.2f}ms")
    print(f"  TPS: {tps:.2f}/s")
    
    return {
        'test_name': test_name,
        'threads': thread_count,
        'total_requests': thread_count,
        'success': success_count,
        'errors': error_count,
        'duplicates': duplicate_count,
        'error_rate': error_rate,
        'avg_rt': avg_rt,
        'min_rt': min_rt,
        'max_rt': max_rt,
        'median_rt': median_rt,
        'p95_rt': p95_rt,
        'tps': tps
    }


def cleanup_test_data():
    """清理测试数据"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        # 删除测试创建的记录（create_user > 100000 的是测试数据）
        cursor.execute("DELETE FROM t_exam_paper_answer WHERE create_user > 100000")
        conn.commit()
        cursor.close()
        conn.close()
        print("测试数据已清理")
    except Exception as e:
        print(f"清理测试数据失败: {e}")


def main():
    global insert_counter
    insert_counter = 100000  # 从 100000 开始，避免与真实数据冲突
    
    print("="*60)
    print("交卷操作性能对比测试（完整流程：查询 + 插入）")
    print("="*60)
    
    # 清理之前的测试数据
    cleanup_test_data()
    
    # 测试有索引
    print("\n\n【第一阶段】有联合索引测试")
    print("索引: idx_exam_user (exam_paper_id, create_user)")
    
    idx_results = []
    for threads in THREAD_LEVELS:
        result = run_exam_submit_test(
            threads,
            f"有索引-{threads}并发",
            "有联合索引 (exam_paper_id, create_user)"
        )
        idx_results.append(result)
        time.sleep(2)  # 休息 2 秒
    
    # 删除索引
    print("\n\n【准备】删除联合索引...")
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    cursor.execute("ALTER TABLE t_exam_paper_answer DROP INDEX idx_exam_user;")
    conn.commit()
    cursor.close()
    conn.close()
    print("索引已删除！")
    time.sleep(3)
    
    # 清理数据重新测试
    cleanup_test_data()
    insert_counter = 200000  # 重置计数器
    
    # 测试无索引
    print("\n\n【第二阶段】无索引测试（全表扫描）")
    
    no_idx_results = []
    for threads in THREAD_LEVELS:
        result = run_exam_submit_test(
            threads,
            f"无索引-{threads}并发",
            "无索引（全表扫描）"
        )
        no_idx_results.append(result)
        time.sleep(2)
    
    # 恢复索引
    print("\n\n【恢复】重新创建联合索引...")
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    cursor.execute("ALTER TABLE t_exam_paper_answer ADD INDEX idx_exam_user (exam_paper_id, create_user);")
    conn.commit()
    cursor.close()
    conn.close()
    print("索引已恢复！")
    
    # 生成对比报告
    print("\n\n" + "="*80)
    print("对比分析报告")
    print("="*80)
    
    print("\n### 有联合索引")
    print("| 并发数 | 总请求 | 平均RT | 最大RT | P95 RT | 错误率 | TPS |")
    print("|--------|--------|--------|--------|--------|--------|-----|")
    for r in idx_results:
        print(f"| {r['threads']} | {r['total_requests']} | {r['avg_rt']:.2f}ms | {r['max_rt']:.2f}ms | {r['p95_rt']:.2f}ms | {r['error_rate']:.2f}% | {r['tps']:.2f}/s |")
    
    print("\n### 无联合索引（全表扫描）")
    print("| 并发数 | 总请求 | 平均RT | 最大RT | P95 RT | 错误率 | TPS |")
    print("|--------|--------|--------|--------|--------|--------|-----|")
    for r in no_idx_results:
        print(f"| {r['threads']} | {r['total_requests']} | {r['avg_rt']:.2f}ms | {r['max_rt']:.2f}ms | {r['p95_rt']:.2f}ms | {r['error_rate']:.2f}% | {r['tps']:.2f}/s |")
    
    # 保存 CSV
    output_path = os.path.join(os.path.dirname(__file__), 'reports', 'comparison_results.csv')
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['测试类型', '并发数', '总请求', '成功', '失败', '重复提交', '错误率%', 
                        '平均RT(ms)', '最小RT(ms)', '最大RT(ms)', '中位数RT(ms)', 'P95RT(ms)', 'TPS'])
        
        for r in idx_results:
            writer.writerow(['有索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           r['duplicates'], f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}",
                           f"{r['tps']:.2f}"])
        
        for r in no_idx_results:
            writer.writerow(['无索引', r['threads'], r['total_requests'], r['success'], r['errors'],
                           r['duplicates'], f"{r['error_rate']:.2f}", f"{r['avg_rt']:.2f}", f"{r['min_rt']:.2f}",
                           f"{r['max_rt']:.2f}", f"{r['median_rt']:.2f}", f"{r['p95_rt']:.2f}",
                           f"{r['tps']:.2f}"])
    
    print(f"\n详细结果已保存到: {output_path}")


if __name__ == '__main__':
    main()
