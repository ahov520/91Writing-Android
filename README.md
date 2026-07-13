# 📚 91写作 Android

基于 [91Writing](https://github.com/ahov520/91Writing) 数据模型的 **移动端优先** Android 客户端。

> Vue 3 轻量移动 UI + 原生 WebView 壳 + **GitHub Actions 自动构建 APK**

## 功能（v2.7.2 移动端）

- 📖 **书架** — 创建 / 筛选 / 标签 / 置顶归档 / 封面预设 / 回收站 / **继续写作** / **进度条** / **近7日字数与本周小结**  
- ✍️ **写作页** — 章节排序、快照对比、大纲导入、阅读模式、自动保存、**章节目标/备注/待办**  
- 🔍 **查找体验** — 章内查找/替换、全书搜索定位、光标与滚动恢复  
- ⏱️ **专注** — 本次字数/用时、番茄钟、今日/本周进度、上/下章快捷切换、撤销重做  
- 📝 **章节摘要** — AI 摘要缓存，续写可优先引用  
- 🎲 **灵感骰子** — 离线冲突/反转/感官/对白/钩子，可插入或 AI 扩写  
- 📋 **待办** — 本章清单、全书看板、插入提纲、导出 Markdown  
- 💬 **AI** — 续写 / 润色 / 扩写 / 新章开写 / 空章填章 / 批量生成 / 对话 / 一致性检查  
- 📦 **导出** — TXT / Markdown（含备注待办）/ EPUB；备份槽 / 粘贴建书  
- 🎯 **目标 / 统计成就 / 类型 / 计费 / 短篇·拆书·工坊**  
- ⚙️ **API + 深色浅色 / 字号行距 / 写作纸感 / 模型档案**  
- 💾 **备份** — 安全脱敏 / 多槽历史 / 回收站 / WebDAV / 本机占用提示  
- 🔐 **应用锁** — PIN（哈希存储）  
- 📊 **桌面小组件** — 今日字数 / 连续天数  
- 📱 **原生 Bridge** — 导出分享、分享入站、选图封面、生成不息屏、返回键  
- 🔒 **WebViewAssetLoader** 虚拟 HTTPS；Release R8  
- 💾 **IndexedDB + persist + 滚动备份**（`novels_prev`）  



所有数据保存在设备本地（IndexedDB + localStorage），**不上传服务器**。AI 请求走你自行配置的 API。

## 架构

```
┌─────────────────────────────────────┐
│  91写作 Android (Kotlin + WebView)  │
│  file:///android_asset/www/…        │
│  Writing91Bridge (export/screen)    │
│  ┌───────────────────────────────┐  │
│  │  Vue 3 移动端 SPA（无 Element） │  │
│  └───────────────┬───────────────┘  │
└──────────────────┼──────────────────┘
                   │ HTTPS
                   ▼
            用户配置的 AI API
```

## 下载 APK

1. 打开仓库 [Actions](https://github.com/ahov520/91Writing-Android/actions)
2. 选择最新成功的 **Build APK** 工作流
3. 在 Artifacts 下载 `writing91-debug-apk`
4. 解压安装 APK（需允许「未知来源」）

打 `v*` 标签会自动创建 GitHub Release 并附带 APK。

## 本地开发

### 前置

- Node.js ≥ 18
- JDK 17 + Android SDK（本地打 APK 时）

### 构建 Web 并同步到 Android assets

```bash
./scripts/sync-web-assets.sh
cd android && ./gradlew assembleDebug
```

### 仅前端

```bash
cd web
npm install
npm run dev    # http://localhost:7520
npm run build
```

## GitHub Actions

工作流：`.github/workflows/build-apk.yml`

1. `npm ci && npm run build`（`web/`）
2. 将 `web/dist` 复制到 `android/app/src/main/assets/www`
3. `./gradlew assembleDebug` / `assembleRelease`
4. 上传 APK Artifact

### 可选签名 Secrets

| Secret | 说明 |
|--------|------|
| `KEYSTORE_BASE64` | release keystore 的 base64 |
| `KEYSTORE_PASSWORD` | 仓库密码 |
| `KEY_ALIAS` | 别名（默认 `writing91`） |
| `KEY_PASSWORD` | 密钥密码 |

未配置时使用 debug 签名，仍可安装调试。

## 首次使用

1. 安装 APK 打开 **91写作**
2. 进入 **API 配置**，填写你的 API Key / Base URL / 模型
3. 创建小说项目，开始 AI 辅助创作

## 目录结构

```
91Writing-Android/
├── web/                      # 完整 91Writing Vue 源码
├── android/                  # Kotlin WebView 壳
│   └── app/src/main/
│       ├── java/.../MainActivity.kt
│       └── assets/www/       # CI 注入的前端产物（gitignore）
├── .github/workflows/build-apk.yml
└── scripts/
    ├── sync-web-assets.sh
    └── push-to-github.sh
```

## 许可证

前端部分遵循原项目 [MIT License](web/LICENSE)。  
Android 壳代码同样以 MIT 许可。

## 致谢

- 原项目：[ponysb/91Writing](https://github.com/ponysb/91Writing) / [ahov520/91Writing](https://github.com/ahov520/91Writing)
