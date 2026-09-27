import csv
import os

output_path = os.path.join(os.path.dirname(__file__), 'mock_data', 'students.csv')

with open(output_path, 'w', newline='', encoding='utf-8') as f:
    w = csv.writer(f)
    w.writerow(['user_id', 'userName', 'password'])
    for i in range(1, 50001):
        w.writerow([i, 'admin', '123456'])

print(f'Done: 50000 rows generated to {output_path}')
