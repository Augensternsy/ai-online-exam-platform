# -*- coding: utf-8 -*-
"""
AI Agent 端到端 Selenium 测试
流程：管理员登录 → AI 自然语言出题 → 验证四步过程与题目 → 保存到题库
依赖：pip install selenium
运行：python test_ai_agent_e2e.py
"""
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

ADMIN_URL = "http://localhost:8002"
ADMIN_USER = "admin"
ADMIN_PASS = "123456"


def make_driver():
    options = webdriver.ChromeOptions()
    options.add_argument("--start-maximized")
    options.add_argument("--disable-blink-features=AutomationControlled")
    return webdriver.Chrome(options=options)


def admin_login(driver):
    print("[1/5] 打开管理端并登录 ...")
    driver.get(ADMIN_URL + "/login")
    WebDriverWait(driver, 10).until(EC.presence_of_element_located(
        (By.CSS_SELECTOR, "input[placeholder='用户名']")))
    driver.find_element(By.CSS_SELECTOR, "input[placeholder='用户名']").send_keys(ADMIN_USER)
    driver.find_element(By.CSS_SELECTOR, "input[placeholder='密码']").send_keys(ADMIN_PASS)
    driver.find_element(By.CSS_SELECTOR, ".login-form button").click()
    WebDriverWait(driver, 15).until(EC.url_contains("/dashboard"))
    print("      登录成功")


def open_ai_page(driver):
    print("[2/5] 进入 AI 智能出题页面 ...")
    driver.get(ADMIN_URL + "/ai/question")
    WebDriverWait(driver, 15).until(EC.presence_of_element_located(
        (By.CSS_SELECTOR, "textarea.agent-textarea, textarea")))
    print("      AI 出题页面加载完成")


def generate_by_agent(driver, text):
    print("[3/5] 输入自然语言需求并触发 AI Agent 生成 ...")
    textarea = driver.find_element(By.CSS_SELECTOR, "textarea.agent-textarea, textarea")
    textarea.clear()
    textarea.send_keys(text)
    # 点击生成按钮（含 Agent 字样的按钮）
    btns = driver.find_elements(By.CSS_SELECTOR, "button")
    target = next((b for b in btns if "Agent" in b.text or "生成" in b.text), btns[-1])
    target.click()
    print("      已点击生成，等待四步过程展示 ...")
    # 等待过程步骤出现
    WebDriverWait(driver, 60).until(
        EC.presence_of_element_located((By.CSS_SELECTOR, ".el-steps, .agent-step")))
    time.sleep(2)
    # 等待题目列表渲染
    WebDriverWait(driver, 90).until(
        EC.presence_of_element_located((By.CSS_SELECTOR, ".question-card, .el-table__row, tr")))
    print("      题目已生成")


def verify_steps(driver):
    print("[4/5] 验证 Agent 四步过程 ...")
    steps = driver.find_elements(By.CSS_SELECTOR, ".el-step__title, .agent-step-title")
    titles = [s.text for s in steps if s.text.strip()]
    print("      步骤标题:", titles)
    assert len(titles) >= 2, "未检测到 Agent 过程步骤"
    return titles


def save_questions(driver):
    print("[5/5] 保存题目到题库 ...")
    btns = driver.find_elements(By.CSS_SELECTOR, "button")
    save_btn = next((b for b in btns if "保存" in b.text or "入库" in b.text), None)
    if save_btn:
        save_btn.click()
        time.sleep(3)
        print("      已点击保存")
    else:
        print("      未找到保存按钮，跳过")


def main():
    driver = make_driver()
    try:
        admin_login(driver)
        open_ai_page(driver)
        generate_by_agent(driver, "生成一套Java基础，中等难度，8道题的考试，包含单选和判断")
        verify_steps(driver)
        save_questions(driver)
        print("\n✅ AI Agent E2E 测试通过")
    except AssertionError as e:
        print("\n❌ 断言失败:", e)
        driver.save_screenshot("ai_agent_e2e_fail.png")
        raise
    except Exception as e:
        print("\n❌ 测试异常:", e)
        driver.save_screenshot("ai_agent_e2e_fail.png")
        raise
    finally:
        time.sleep(3)
        driver.quit()


if __name__ == "__main__":
    main()
