#!/usr/bin/env node

/**
 * Showcase Demo 一键构建脚本
 *
 * 1. 构建 xzs-admin  -> dist/admin/
 * 2. 构建 xzs-student -> dist/student/
 * 3. 生成 dist/index.html Showcase 首页
 *
 * 特性：
 * - 仅使用相对路径，不依赖任何绝对路径（Windows / Linux / Vercel 均可执行）
 * - 自动注入 VUE_APP_DEMO_MODE=true 与 NODE_OPTIONS（兼容高版本 Node + webpack4）
 * - 直接调用子项目本地的 vue-cli-service，绕过子项目 package.json 中
 *   Windows 专用的 npm 脚本（set NODE_OPTIONS=...）
 */

const { execSync } = require('child_process')
const fs = require('fs')
const path = require('path')

const VUE_ROOT = __dirname
const DIST_DIR = path.join(VUE_ROOT, 'dist')

const PROJECTS = [
  { dir: 'xzs-admin', output: 'admin' },
  { dir: 'xzs-student', output: 'student' }
]

function log (msg) {
  console.log(`[build-demo] ${msg}`)
}

function cleanDir (target) {
  if (fs.existsSync(target)) {
    fs.rmSync(target, { recursive: true, force: true })
  }
}

function copyDir (src, dest) {
  if (!fs.existsSync(src)) {
    throw new Error(`build output not found: ${src}`)
  }
  fs.mkdirSync(dest, { recursive: true })
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, entry.name)
    const destPath = path.join(dest, entry.name)
    if (entry.isDirectory()) {
      copyDir(srcPath, destPath)
    } else {
      fs.copyFileSync(srcPath, destPath)
    }
  }
}

function buildProject ({ dir }) {
  const projectDir = path.join(VUE_ROOT, dir)
  const serviceBin = path.join(projectDir, 'node_modules', '@vue', 'cli-service', 'bin', 'vue-cli-service.js')

  if (!fs.existsSync(serviceBin)) {
    throw new Error(`vue-cli-service not found in ${dir}, please run "npm install" first`)
  }

  log(`=== building ${dir} (mode: demo) ===`)
  execSync(`node "${serviceBin}" build --mode demo`, {
    stdio: 'inherit',
    cwd: projectDir,
    env: Object.assign({}, process.env, {
      VUE_APP_DEMO_MODE: 'true',
      NODE_OPTIONS: '--openssl-legacy-provider'
    })
  })
}

function writeShowcaseIndex () {
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>AI 智能在线考试平台 - Demo Showcase</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
                   "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
      background: #0f172a;
      min-height: 100vh;
      color: #e2e8f0;
    }
    .hero {
      background: radial-gradient(ellipse at top, #4338ca 0%, #0f172a 60%);
      padding: 90px 24px 70px;
      text-align: center;
    }
    h1 {
      font-size: 2.8rem;
      font-weight: 700;
      background: linear-gradient(90deg, #a5b4fc, #67e8f9);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 18px;
    }
    .intro {
      font-size: 1.1rem;
      color: #cbd5e1;
      max-width: 680px;
      margin: 0 auto 30px;
      line-height: 1.8;
    }
    .tags {
      display: flex; flex-wrap: wrap; gap: 10px;
      justify-content: center; margin-bottom: 36px;
    }
    .tag {
      background: rgba(99,102,241,0.15);
      border: 1px solid rgba(129,140,248,0.4);
      color: #c7d2fe;
      padding: 6px 16px; border-radius: 999px; font-size: 0.88rem;
    }
    .entries {
      display: flex; gap: 28px; justify-content: center; flex-wrap: wrap;
    }
    .entry {
      display: block; width: 300px; text-decoration: none;
      background: rgba(30,41,59,0.8);
      border: 1px solid #334155;
      border-radius: 16px; padding: 32px 26px;
      transition: transform .25s, border-color .25s, box-shadow .25s;
    }
    .entry:hover {
      transform: translateY(-6px);
      border-color: #818cf8;
      box-shadow: 0 18px 40px rgba(67,56,202,0.35);
    }
    .entry .icon { font-size: 2.2rem; margin-bottom: 14px; }
    .entry h2 { font-size: 1.35rem; color: #f1f5f9; margin-bottom: 10px; }
    .entry p { font-size: 0.92rem; color: #94a3b8; line-height: 1.7; }
    .accounts {
      max-width: 720px; margin: 0 auto;
      background: rgba(30,41,59,0.6);
      border: 1px solid #334155;
      border-radius: 14px; padding: 22px 30px;
    }
    .accounts h3 { font-size: 0.95rem; color: #a5b4fc; margin-bottom: 12px; letter-spacing: 1px; }
    .accounts .row { font-size: 0.95rem; color: #cbd5e1; line-height: 2; }
    .notice {
      max-width: 720px; margin: 26px auto 0;
      font-size: 0.9rem; color: #fbbf24; line-height: 1.7;
      background: rgba(245,158,11,0.08);
      border: 1px solid rgba(245,158,11,0.3);
      border-radius: 12px; padding: 16px 24px;
    }
    footer { text-align: center; padding: 40px 20px; color: #64748b; font-size: 0.85rem; }
  </style>
</head>
<body>
  <section class="hero">
    <h1>AI 智能在线考试平台</h1>
    <p class="intro">基于 Spring Boot + Vue + DeepSeek 构建的 AI 智能考试与自动化测试平台，支持自然语言出题、AI 智能评测与容器化一键部署。</p>

    <div class="tags">
      <span class="tag">Spring Boot</span>
      <span class="tag">Vue</span>
      <span class="tag">DeepSeek</span>
      <span class="tag">MySQL</span>
      <span class="tag">Selenium</span>
      <span class="tag">JMeter</span>
      <span class="tag">Docker</span>
      <span class="tag">Kubernetes</span>
    </div>

    <div class="accounts">
      <h3>DEMO 演示账号</h3>
      <div class="row">管理员：<strong>admin / 123456</strong></div>
      <div class="row">学生：<strong>Natasha / 123456</strong></div>
    </div>

    <div class="notice">
      当前在线版本为 Showcase Demo，所有数据来自前端内置 Mock；完整项目源码支持 Spring Boot + MySQL + DeepSeek 实时服务。
    </div>

    <div class="entries" style="margin-top: 44px;">
      <a class="entry" href="/student/">
        <div class="icon">🎓</div>
        <h2>学生端体验</h2>
        <p>进入试卷中心，完成 20 道 Java 基础测试题，提交后查看成绩与 AI 评测</p>
      </a>
      <a class="entry" href="/admin/">
        <div class="icon">🛠️</div>
        <h2>管理端体验</h2>
        <p>查看首页统计、试卷/题目/用户列表，体验 AI Agent 自然语言出题</p>
      </a>
    </div>
  </section>

  <footer>AI 智能在线考试平台 &copy; 2026 · Powered by Spring Boot + Vue + DeepSeek</footer>
</body>
</html>`

  fs.mkdirSync(DIST_DIR, { recursive: true })
  fs.writeFileSync(path.join(DIST_DIR, 'index.html'), html, 'utf8')
  log('showcase index generated')
}

function main () {
  log('cleaning dist ...')
  cleanDir(DIST_DIR)

  for (const project of PROJECTS) {
    buildProject(project)
    const srcOutput = path.join(VUE_ROOT, project.dir, project.output)
    const destOutput = path.join(DIST_DIR, project.output)
    copyDir(srcOutput, destOutput)
    log(`${project.dir} -> dist/${project.output}`)
  }

  writeShowcaseIndex()
  log('build finished: ' + DIST_DIR)
}

main()
