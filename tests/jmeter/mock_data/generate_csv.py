import csv
import os

os.chdir(r'e:\长实习\xzs-mysql-master\tests\jmeter\mock_data')

with open('students.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['user_id', 'userName', 'password'])
    for i in range(1, 1001):
        writer.writerow([i, f'student{i:05d}', 'e10adc3949ba59abbe56e057f20f883e'])

print('CSV 文件已生成，共 1000 行数据')
