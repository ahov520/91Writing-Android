# 📚 91写作 Android

基于 [91Writing](https://github.com/ahov520/91Writing) 的 **完整功能** Android 客户端。

> 纯前端 Vue 3 应用 + 原生 WebView 壳 + **GitHub Actions 自动构建 APK**

## 功能（与网页版一致）

- 📖 **小说管理** — 项目创建、章节编辑、状态管理、版本自动保存  
- 🎯 **写作目标** — 日/周/月字数目标、进度与连续天数  
- 🎭 **类型管理** — 玄幻/都市/历史等预设 + 自定义类型  
- 🤖 **AI 写作助手** — 智能续写、内容润色、流式输出（OpenAI 兼容 API）  
- 💬 **提示词库** — 大纲/正文/润色/对话等分类模板  
- 🌟 **世界观 / 角色 / 语料 / 事件时间线**  
- ⚙️ **API 配置** — 官方 / 自定义 endpoint + 多模型  
- 📊 **Token 计费** — 用量与成本统计  
- 💾 **备份导入导出** — 本地数据、分类导出  
- ✍️ **短篇 / 拆书分析 / 创作工坊**  

所有数据保存在设备本地（WebView `localStorage` / 本地存储），**不上传服务器**。AI 请求走你自行配置的 API。

## 架构

```
┌─────────────────────────────────────┐
│  91写作 Android (Kotlin + WebView)  │
│  file:///android_asset/www/…        │
│  ┌───────────────────────────────┐  │
│  │  Vue 3 + Element Plus SPA     │  │
│  │  (完整 91Writing 前端)         │  │
│  └───────────────┬───────────────┘  │
└──────────────────┼──────────────────┘
                   │ HTTPS
                   ▼
            用户配置的 AI API
     (OpenAI / Claude / DeepSeek / 自定义)
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
