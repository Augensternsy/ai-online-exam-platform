"""
数据预埋脚本 - 生成 5 万条基准测试数据

功能：
1. 使用 Faker 库生成伪数据（中文姓名、学号、时间戳）
2. 内存批处理（每 1000 条为一批次）
3. 使用 executemany 批量插入，减少网络往返

数据构成：
- 考生账号（t_user 表）
- 考试记录（t_task_exam_customer_answer 表）
- 试卷信息（t_exam_paper 表）
"""

import pymysql
import random
import time
import uuid
from faker import Faker
from datetime import datetime, timedelta

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'xzs',
    'password': 'xzs123456',
    'database': 'ems',
    'charset': 'utf8mb4'
}

# 批次大小
BATCH_SIZE = 1000

# 总数据量
TOTAL_STUDENTS = 10000
TOTAL_EXAM_RECORDS = 50000

class DataSeeder:
    """数据预埋器"""
    
    def __init__(self):
        """初始化"""
        self.faker = Faker(locale='zh_CN')
        self.connection = None
        self.cursor = None
        
    def connect(self):
        """连接数据库"""
        print("正在连接数据库...")
        self.connection = pymysql.connect(**DB_CONFIG)
        self.cursor = self.connection.cursor()
        print("✅ 数据库连接成功")
        
    def disconnect(self):
        """断开数据库连接"""
        if self.cursor:
            self.cursor.close()
        if self.connection:
            self.connection.close()
        print("✅ 数据库连接已关闭")
        
    def seed_students(self, count=TOTAL_STUDENTS):
        """预埋学生数据"""
        print(f"\n{'='*60}")
        print(f"开始预埋学生数据：{count} 条")
        print(f"{'='*60}")
        
        start_time = time.time()
        
        # 获取现有学生数量
        self.cursor.execute("SELECT COUNT(*) FROM t_user WHERE role = 3")
        existing_count = self.cursor.fetchone()[0]
        print(f"📊 现有学生数量：{existing_count}")
        
        if existing_count >= count:
            print(f"✅ 学生数据已足够，跳过预埋")
            return
            
        # 需要插入的数量
        insert_count = count - existing_count
        
        # 生成数据
        students = []
        for i in range(insert_count):
            user_uuid = str(uuid.uuid4())
            user_name = f"student{existing_count + i + 1:05d}"
            password = "e10adc3949ba59abbe56e057f20f883e"  # 123456 的 MD5
            real_name = self.faker.name()
            age = random.randint(18, 25)
            sex = random.randint(1, 2)
            birth_day = self.faker.date_of_birth(minimum_age=18, maximum_age=25)
            user_level = 1
            phone = self.faker.phone_number()
            role = 3  # 学生角色
            status = 1
            image_path = ""
            create_time = datetime.now()
            modify_time = datetime.now()
            last_active_time = datetime.now()
            deleted = 0
            wx_open_id = ""
            
            students.append((
                user_uuid, user_name, password, real_name, age, sex,
                birth_day, user_level, phone, role, status,
                image_path, create_time, modify_time, last_active_time,
                deleted, wx_open_id
            ))
            
            # 批次插入
            if len(students) >= BATCH_SIZE:
                self._batch_insert_students(students)
                students = []
                
        # 插入剩余数据
        if students:
            self._batch_insert_students(students)
            
        elapsed = time.time() - start_time
        print(f"✅ 学生数据预埋完成，耗时：{elapsed:.2f} 秒")
        
    def _batch_insert_students(self, students):
        """批量插入学生数据"""
        sql = """
            INSERT INTO t_user 
            (user_uuid, user_name, password, real_name, age, sex, birth_day, 
             user_level, phone, role, status, image_path, create_time, 
             modify_time, last_active_time, deleted, wx_open_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        self.cursor.executemany(sql, students)
        self.connection.commit()
        print(f"  ✅ 已插入 {len(students)} 条学生数据")
        
    def seed_exam_records(self, count=TOTAL_EXAM_RECORDS):
        """预埋考试记录数据"""
        print(f"\n{'='*60}")
        print(f"开始预埋考试记录数据：{count} 条")
        print(f"{'='*60}")
        
        start_time = time.time()
        
        # 获取现有学生 ID
        self.cursor.execute("SELECT id FROM t_user WHERE role = 3")
        student_ids = [row[0] for row in self.cursor.fetchall()]
        
        if not student_ids:
            print("❌ 没有学生数据，请先运行 seed_students()")
            return
            
        # 获取现有试卷 ID
        self.cursor.execute("SELECT id FROM t_exam_paper WHERE deleted = 0")
        paper_ids = [row[0] for row in self.cursor.fetchall()]
        
        if not paper_ids:
            print("⚠️  没有试卷数据，使用默认试卷 ID")
            paper_ids = [1, 2, 3]
            
        # 获取现有任务考试 ID
        self.cursor.execute("SELECT id FROM t_task_exam WHERE deleted = 0")
        task_ids = [row[0] for row in self.cursor.fetchall()]
        
        # 清空现有数据
        self.cursor.execute("DELETE FROM t_exam_paper_answer")
        self.connection.commit()
        print("✅ 已清空现有考试记录数据")
        
        # 生成数据
        records = []
        statuses = [1, 2, 3]  # 1-进行中，2-已完成，3-已批改
        for i in range(count):
            user_id = random.choice(student_ids)
            paper_id = random.choice(paper_ids)
            task_exam_id = random.choice(task_ids) if task_ids else None
            do_time = random.randint(30, 7200)  # 30 秒 - 2 小时
            create_time = self.faker.date_time_between(
                start_date='-30d',
                end_date='now'
            )
            system_score = random.randint(0, 100)
            user_score = random.randint(0, 100)
            paper_score = 100
            question_correct = random.randint(0, 20)
            question_count = 20
            status = random.choice(statuses)
            
            records.append((
                paper_id, f"试卷{paper_id}", 0, 1,  # exam_paper_id, paper_name, paper_type, subject_id
                system_score, user_score, paper_score,
                question_correct, question_count, do_time,
                status, user_id, create_time, task_exam_id
            ))
            
            # 批次插入
            if len(records) >= BATCH_SIZE:
                self._batch_insert_exam_records(records)
                records = []
                
        # 插入剩余数据
        if records:
            self._batch_insert_exam_records(records)
            
        elapsed = time.time() - start_time
        print(f"✅ 考试记录数据预埋完成，耗时：{elapsed:.2f} 秒")
        
    def _batch_insert_exam_records(self, records):
        """批量插入考试记录数据"""
        sql = """
            INSERT INTO t_exam_paper_answer 
            (exam_paper_id, paper_name, paper_type, subject_id,
             system_score, user_score, paper_score,
             question_correct, question_count, do_time,
             status, create_user, create_time, task_exam_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        self.cursor.executemany(sql, records)
        self.connection.commit()
        print(f"  ✅ 已插入 {len(records)} 条考试记录数据")
        
    def create_indexes(self):
        """创建联合索引"""
        print(f"\n{'='*60}")
        print("创建联合索引")
        print(f"{'='*60}")
        
        # 检查索引是否存在
        self.cursor.execute("""
            SELECT COUNT(*) 
            FROM information_schema.statistics 
            WHERE table_schema = %s 
            AND table_name = 't_exam_paper_answer' 
            AND index_name = 'idx_exam_user'
        """, (DB_CONFIG['database'],))
        
        exists = self.cursor.fetchone()[0]
        
        if exists:
            print("✅ 联合索引 idx_exam_user 已存在")
        else:
            print("正在创建联合索引 idx_exam_user (exam_paper_id, create_user)...")
            self.cursor.execute("""
                ALTER TABLE t_exam_paper_answer 
                ADD INDEX idx_exam_user (exam_paper_id, create_user)
            """)
            self.connection.commit()
            print("✅ 联合索引 idx_exam_user 创建成功")
            
    def analyze_tables(self):
        """分析表数据"""
        print(f"\n{'='*60}")
        print("表数据分析")
        print(f"{'='*60}")
        
        # 学生数量
        self.cursor.execute("SELECT COUNT(*) FROM t_user WHERE role = 3")
        student_count = self.cursor.fetchone()[0]
        print(f"📊 学生数量：{student_count}")
        
        # 考试记录数量
        self.cursor.execute("SELECT COUNT(*) FROM t_exam_paper_answer")
        record_count = self.cursor.fetchone()[0]
        print(f"📊 考试记录数量：{record_count}")
        
        # 试卷数量
        self.cursor.execute("SELECT COUNT(*) FROM t_exam_paper WHERE deleted = 0")
        paper_count = self.cursor.fetchone()[0]
        print(f"📊 试卷数量：{paper_count}")
        
    def run_explain(self):
        """运行 EXPLAIN 分析"""
        print(f"\n{'='*60}")
        print("EXPLAIN 分析")
        print(f"{'='*60}")
        
        # 测试查询
        test_sql = """
            EXPLAIN SELECT * FROM t_exam_paper_answer 
            WHERE exam_paper_id = 1234 AND create_user = 1
        """
        
        print("\n执行计划：")
        self.cursor.execute(test_sql)
        results = self.cursor.fetchall()
        
        for row in results:
            print(f"  id: {row[0]}")
            print(f"  select_type: {row[1]}")
            print(f"  table: {row[2]}")
            print(f"  type: {row[3]}")
            print(f"  possible_keys: {row[4]}")
            print(f"  key: {row[5]}")
            print(f"  key_len: {row[6]}")
            print(f"  ref: {row[7]}")
            print(f"  rows: {row[8]}")
            print(f"  Extra: {row[9]}")
            print()
            
    def enable_slow_query_log(self):
        """开启慢查询日志"""
        print(f"\n{'='*60}")
        print("开启慢查询日志")
        print(f"{'='*60}")
        
        try:
            self.cursor.execute("SET GLOBAL slow_query_log = 'ON'")
            self.cursor.execute("SET GLOBAL long_query_time = 1")
            
            print("✅ 慢查询日志已开启")
            print("✅ 阈值：1 秒")
            print("📝 日志位置：请查看 MySQL 配置文件")
        except Exception as e:
            print(f"⚠️  开启慢查询日志失败（可能需要 SUPER 权限）：{e}")
        
    def run_all(self):
        """运行所有预埋任务"""
        print("\n" + "="*60)
        print("开始数据预埋")
        print("="*60)
        
        try:
            self.connect()
            
            # 1. 预埋学生数据
            self.seed_students()
            
            # 2. 预埋考试记录
            self.seed_exam_records()
            
            # 3. 创建联合索引
            self.create_indexes()
            
            # 4. 分析表数据
            self.analyze_tables()
            
            # 5. 运行 EXPLAIN 分析
            self.run_explain()
            
            # 6. 开启慢查询日志
            self.enable_slow_query_log()
            
            print("\n" + "="*60)
            print("✅ 数据预埋全部完成！")
            print("="*60)
            
        except Exception as e:
            print(f"\n❌ 数据预埋失败：{e}")
            raise
        finally:
            self.disconnect()

if __name__ == "__main__":
    seeder = DataSeeder()
    seeder.run_all()
