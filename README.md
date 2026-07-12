# 📚 91写作 Android

基于 [91Writing](https://github.com/ahov520/91Writing) 数据模型的 **移动端优先** Android 客户端。

> Vue 3 轻量移动 UI + 原生 WebView 壳 + **GitHub Actions 自动构建 APK**

## 功能（v2.2 移动端）

- 📖 **书架** — 创建 / 筛选 / 封面 / 复制 / 删除 / 导出  
- ✍️ **写作页** — 章节排序、快照、大纲导入、阅读模式、自动保存  
- 🤖 **AI** — 续写 / 润色 / 扩写 / 提示词库 / 可中断流式；可引用角色世界观  
- 🎯 **目标 / 类型 / 计费 / 短篇·拆书·工坊**  
- ⚙️ **API + 深色浅色 / 字号行距**  
- 💾 **备份** — 分类导出 / 完整备份 / 旧格式兼容  
- 📱 **原生 Bridge** — 导出分享、生成时不息屏、返回键适配  

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
