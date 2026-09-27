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

def run_mode2_exam_flow(thread_id, test_mode='mode2'):
    """模拟模式二：实时落盘模式的完整考试流程"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 生成测试数据
        exam_paper_id = (thread_id % 10) + 1  # 1-10 试卷
        user_id = get_next_user_id()  # 唯一用户ID
        
        flow_start_time = time.time()
        flow_results = {
            'thread_id': thread_id,
            'start_time': 0,
            'save_times': [],
            'submit_time': 0,
            'total_time': 0,
            'success': True,
            'error': None
        }
        
        try:
            # 步骤1：开始考试 - INSERT 创建空答卷记录
            start_time = time.time()
            insert_sql = """
                INSERT INTO t_exam_paper_answer 
                (paper_name, do_time, exam_paper_id, create_user, create_time, update_time, subject_id, 
                 question_count, paper_score, paper_type, system_score, user_score, 
                 task_exam_id, question_correct, status)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            
            now = time.strftime('%Y-%m-%d %H:%M:%S')
            cursor.execute(insert_sql, (
                f'测试试卷{exam_paper_id}',  # paper_name
                0,  # do_time (初始为0)
                exam_paper_id,
                user_id,
                now,
                now,  # update_time
                1,  # subject_id
                50,  # question_count
                100,  # paper_score
                1,  # paper_type
                0,  # system_score (初始为0)
                0,  # user_score (初始为0)
                None,  # task_exam_id
                0,  # question_correct (初始为0)
                0  # status (InProgress)
            ))
            
            exam_answer_id = cursor.lastrowid
            conn.commit()
            
            start_elapsed = (time.time() - start_time) * 1000
            flow_results['start_time'] = start_elapsed
            
            # 步骤2：模拟答题过程中的自动保存（3次）
            for save_round in range(3):
                save_time = time.time()
                
                # 模拟部分答案更新
                update_sql = """
                    UPDATE t_exam_paper_answer 
                    SET do_time = %s, update_time = %s, system_score = %s, user_score = %s, question_correct = %s
                    WHERE id = %s
                """
                
                current_do_time = (save_round + 1) * 600  # 每次保存增加10分钟
                current_score = (save_round + 1) * 25  # 每次保存增加25分
                current_correct = (save_round + 1) * 15  # 每次保存增加15题
                
                cursor.execute(update_sql, (
                    current_do_time,
                    time.strftime('%Y-%m-%d %H:%M:%S'),
                    current_score,
                    current_score,
                    current_correct,
                    exam_answer_id
                ))
                conn.commit()
                
                save_elapsed = (time.time() - save_time) * 1000
                flow_results['save_times'].append(save_elapsed)
                
                time.sleep(0.1)  # 模拟答题时间间隔
            
            # 步骤3：提交考试 - UPDATE 状态为完成
            submit_time = time.time()
            submit_sql = """
                UPDATE t_exam_paper_answer 
                SET do_time = %s, update_time = %s, system_score = %s, user_score = %s, 
                    question_correct = %s, status = %s
                WHERE id = %s
            """
            
            cursor.execute(submit_sql, (
                1800,  # do_time (总用时30分钟)
                time.strftime('%Y-%m-%d %H:%M:%S'),
                85,  # system_score
                85,  # user_score
                45,  # question_correct
                2,  # status (Complete)
                exam_answer_id
            ))
            conn.commit()
            
            submit_elapsed = (time.time() - submit_time) * 1000
            flow_results['submit_time'] = submit_elapsed
            
            flow_results['total_time'] = (time.time() - flow_start_time) * 1000
            
        except Exception as e:
            conn.rollback()
            flow_results['success'] = False
            flow_results['error'] = str(e)
        
        cursor.close()
        conn.close()
        
        return flow_results
        
    except Exception as e:
        return {
            'thread_id': thread_id,
            'start_time': 0,
            'save_times': [],
            'submit_time': 0,
            'total_time': 0,
            'success': False,
            'error': str(e)
        }

def run_mode1_exam_flow(thread_id, test_mode='mode1'):
    """模拟模式一：一次性提交模式（对比基准）"""
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 生成测试数据
        exam_paper_id = (thread_id % 10) + 1  # 1-10 试卷
        user_id = get_next_user_id()  # 唯一用户ID
        
        flow_start_time = time.time()
        flow_results = {
            'thread_id': thread_id,
            'submit_time': 0,
            'total_time': 0,
            'success': True,
            'error': None
        }
        
        try:
            # 步骤1：查询是否已交卷（防重复检查）
            check_sql = "SELECT id FROM t_exam_paper_answer WHERE exam_paper_id = %s AND create_user = %s"
            cursor.execute(check_sql, (exam_paper_id, user_id))
            existing = cursor.fetchone()
            
            if existing:
                # 已交卷，直接返回
                flow_results['total_time'] = (time.time() - flow_start_time) * 1000
                cursor.close()
                conn.close()
                return flow_results
            
            # 步骤2：一次性插入完整答题记录
            insert_time = time.time()
            insert_sql = """
                INSERT INTO t_exam_paper_answer 
                (paper_name, do_time, exam_paper_id, create_user, create_time, update_time, subject_id, 
                 question_count, paper_score, paper_type, system_score, user_score, 
                 task_exam_id, question_correct, status)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            
            now = time.strftime('%Y-%m-%d %H:%M:%S')
            cursor.execute(insert_sql, (
                f'测试试卷{exam_paper_id}',  # paper_name
                1800,  # do_time (秒)
                exam_paper_id,
                user_id,
                now,
                now,  # update_time
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
            insert_elapsed = (time.time() - insert_time) * 1000
            flow_results['submit_time'] = insert_elapsed
            flow_results['total_time'] = (time.time() - flow_start_time) * 1000
            
        except Exception as e:
            conn.rollback()
            flow_results['success'] = False
            flow_results['error'] = str(e)
        
        cursor.close()
        conn.close()
        
        return flow_results
        
    except Exception as e:
        return {
            'thread_id': thread_id,
            'submit_time': 0,
            'total_time': 0,
            'success': False,
            'error': str(e)
        }

def run_concurrent_test(thread_count, test_name, test_function, description):
    """运行并发测试"""
    print(f"\n{'='*60}")
    print(f"测试: {test_name}")
    print(f"并发数: {thread_count}")
    print(f"描述: {description}")
    print(f"{'='*60}")
    
    results = []
    errors = []
    
    # 使用线程池执行并发测试
    with ThreadPoolExecutor(max_workers=thread_count) as executor:
        # 提交所有任务
        future_to_id = {
            executor.submit(test_function, i+1, test_name): i+1 
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
    total_times = [r['total_time'] for r in results if r['success']]
    
    # 计算指标
    if total_times:
        avg_total = statistics.mean(total_times)
        min_total = min(total_times)
        max_total = max(total_times)
        median_total = statistics.median(total_times)
        p95_total = sorted(total_times)[int(len(total_times) * 0.95)] if len(total_times) > 1 else total_times[0]
    else:
        avg_total = min_total = max_total = median_total = p95_total = 0
    
    # 计算特定指标
    if test_function == run_mode2_exam_flow:
        start_times = [r['start_time'] for r in results if r['success'] and r['start_time'] > 0]
        save_times_list = [t for r in results if r['success'] for t in r['save_times']]
        submit_times = [r['submit_time'] for r in results if r['success'] and r['submit_time'] > 0]
        
        avg_start = statistics.mean(start_times) if start_times else 0
        avg_save = statistics.mean(save_times_list) if save_times_list else 0
        avg_submit = statistics.mean(submit_times) if submit_times else 0
    else:
        submit_times = [r['submit_time'] for r in results if r['success'] and r['submit_time'] > 0]
        avg_submit = statistics.mean(submit_times) if submit_times else 0
        avg_start = 0
        avg_save = 0
    
    total_time_sum = sum(total_times) / 1000 if total_times else 0
    tps = success_count / total_time_sum if total_time_sum > 0 else 0
    error_rate = (error_count / thread_count) * 100
    
    print(f"\n结果:")
    print(f"  总请求数: {thread_count}")
    print(f"  成功: {success_count}, 失败: {error_count}")
    print(f"  错误率: {error_rate:.2f}%")
    print(f"  平均总时间: {avg_total:.2f}ms")
    print(f"  最小总时间: {min_total:.2f}ms")
    print(f"  最大总时间: {max_total:.2f}ms")
    print(f"  中位数总时间: {median_total:.2f}ms")
    print(f"  P95总时间: {p95_total:.2f}ms")
    print(f"  TPS: {tps:.2f}/s")
    
    if test_function == run_mode2_exam_flow:
        print(f"  平均开始考试时间: {avg_start:.2f}ms")
        print(f"  平均保存时间: {avg_save:.2f}ms")
        print(f"  平均提交时间: {avg_submit:.2f}ms")
    else:
        print(f"  平均提交时间: {avg_submit:.2f}ms")
    
    return {
        'test_name': test_name,
        'threads': thread_count,
        'total_requests': thread_count,
        'success': success_count,
        'errors': error_count,
        'error_rate': error_rate,
        'avg_total': avg_total,
        'min_total': min_total,
        'max_total': max_total,
        'median_total': median_total,
        'p95_total': p95_total,
        'tps': tps,
        'avg_start': avg_start,
        'avg_save': avg_save,
        'avg_submit': avg_submit
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
    print("模式二（实时落盘模式）性能对比测试")
    print("="*60)
    
    # 清理之前的测试数据
    cleanup_test_data()
    
    # 测试模式二：实时落盘模式
    print("\n\n【第一阶段】模式二：实时落盘模式测试")
    print("流程：开始考试(INSERT) -> 自动保存(UPDATE x3) -> 提交考试(UPDATE)")
    
    mode2_results = []
    for threads in THREAD_LEVELS:
        result = run_concurrent_test(
            threads,
            f"模式二-{threads}并发",
            run_mode2_exam_flow,
            "模式二：实时落盘模式"
        )
        mode2_results.append(result)
        time.sleep(2)  # 休息 2 秒
    
    # 清理数据重新测试
    cleanup_test_data()
    insert_counter = 200000  # 重置计数器
    
    # 测试模式一：一次性提交模式
    print("\n\n【第二阶段】模式一：一次性提交模式测试（对比基准）")
    print("流程：提交考试(INSERT)")
    
    mode1_results = []
    for threads in THREAD_LEVELS:
        result = run_concurrent_test(
            threads,
            f"模式一-{threads}并发",
            run_mode1_exam_flow,
            "模式一：一次性提交模式"
        )
        mode1_results.append(result)
        time.sleep(2)
    
    # 生成对比报告
    print("\n\n" + "="*80)
    print("对比分析报告")
    print("="*80)
    
    print("\n### 模式二：实时落盘模式")
    print("| 并发数 | 总请求 | 平均总时间 | 最大总时间 | P95总时间 | 错误率 | TPS | 平均开始 | 平均保存 | 平均提交 |")
    print("|--------|--------|------------|------------|-----------|--------|-----|----------|----------|----------|")
    for r in mode2_results:
        print(f"| {r['threads']} | {r['total_requests']} | {r['avg_total']:.2f}ms | {r['max_total']:.2f}ms | {r['p95_total']:.2f}ms | {r['error_rate']:.2f}% | {r['tps']:.2f}/s | {r['avg_start']:.2f}ms | {r['avg_save']:.2f}ms | {r['avg_submit']:.2f}ms |")
    
    print("\n### 模式一：一次性提交模式")
    print("| 并发数 | 总请求 | 平均总时间 | 最大总时间 | P95总时间 | 错误率 | TPS | 平均提交 |")
    print("|--------|--------|------------|------------|-----------|--------|-----|----------|")
    for r in mode1_results:
        print(f"| {r['threads']} | {r['total_requests']} | {r['avg_total']:.2f}ms | {r['max_total']:.2f}ms | {r['p95_total']:.2f}ms | {r['error_rate']:.2f}% | {r['tps']:.2f}/s | {r['avg_submit']:.2f}ms |")
    
    # 保存 CSV
    output_path = os.path.join(os.path.dirname(__file__), 'reports', 'mode2_comparison_results.csv')
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['测试类型', '并发数', '总请求', '成功', '失败', '错误率%', 
                        '平均总时间(ms)', '最小总时间(ms)', '最大总时间(ms)', '中位数总时间(ms)', 'P95总时间(ms)', 'TPS',
                        '平均开始时间(ms)', '平均保存时间(ms)', '平均提交时间(ms)'])
        
        for r in mode2_results:
            writer.writerow(['模式二', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_total']:.2f}", f"{r['min_total']:.2f}",
                           f"{r['max_total']:.2f}", f"{r['median_total']:.2f}", f"{r['p95_total']:.2f}",
                           f"{r['tps']:.2f}", f"{r['avg_start']:.2f}", f"{r['avg_save']:.2f}", f"{r['avg_submit']:.2f}"])
        
        for r in mode1_results:
            writer.writerow(['模式一', r['threads'], r['total_requests'], r['success'], r['errors'],
                           f"{r['error_rate']:.2f}", f"{r['avg_total']:.2f}", f"{r['min_total']:.2f}",
                           f"{r['max_total']:.2f}", f"{r['median_total']:.2f}", f"{r['p95_total']:.2f}",
                           f"{r['tps']:.2f}", '', '', f"{r['avg_submit']:.2f}"])
    
    print(f"\n详细结果已保存到: {output_path}")


if __name__ == '__main__':
    main()
