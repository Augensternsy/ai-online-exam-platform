#!/usr/bin/env node

/**
 * 自动安装 xzs-admin / xzs-student 两个子项目的依赖
 * - 已安装（node_modules 非空）则跳过，本地重复执行不耗时
 * - Vercel 执行根目录 `npm install` 时通过 postinstall 自动触发
 * - 全部使用相对路径，Windows / Linux 均可执行
 */

const { execSync } = require('child_process')
const fs = require('fs')
const path = require('path')

const SUB_PROJECTS = ['xzs-admin', 'xzs-student']

function isInstalled (projectDir) {
  const nm = path.join(projectDir, 'node_modules')
  try {
    return fs.existsSync(nm) && fs.readdirSync(nm).length > 0
  } catch {
    return false
  }
}

for (const name of SUB_PROJECTS) {
  const projectDir = path.join(__dirname, name)
  if (isInstalled(projectDir)) {
    console.log(`[install-deps] ${name}: node_modules already exists, skipping`)
    continue
  }
  console.log(`[install-deps] installing dependencies for ${name} ...`)
  execSync('npm install --no-audit --no-fund', {
    stdio: 'inherit',
    cwd: projectDir
  })
}

console.log('[install-deps] all sub-project dependencies are ready')
