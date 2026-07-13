import { createApp } from 'vue'
import router from './router'
import App from './App.vue'
import './mobile.css'
import { initStorage } from './services/storage.js'
import { installMobileShell, setRouteChrome } from './utils/mobileShell.js'
import { usePrefs } from './composables/usePrefs.js'
import { hashPin } from './utils/simpleHash.js'

function ensureAppLock() {
  try {
    const raw = localStorage.getItem('app_lock_prefs')
    if (!raw) return true
    const lock = JSON.parse(raw)
    if (!lock?.enabled || !lock?.pin) return true
    // Simple PIN gate (WebView-friendly)
    for (let i = 0; i < 3; i++) {
      const input = window.prompt(i === 0 ? '输入应用 PIN 解锁' : 'PIN 错误，请重试')
      if (input == null) return false
      const ok =
        lock.pinHash
          ? hashPin(input) === lock.pinHash
          : String(input) === String(lock.pin) // legacy plain pin
      if (ok) {
        // Migrate legacy plain PIN → pinHash on successful unlock
        if (!lock.pinHash && lock.pin) {
          try {
            localStorage.setItem(
              'app_lock_prefs',
              JSON.stringify({ enabled: true, pinHash: hashPin(input) })
            )
          } catch {
            /* ignore */
          }
        }
        return true
      }
    }
    return false
  } catch {
    return true
  }
}

async function bootstrap() {
  try {
    await initStorage()
  } catch (e) {
    console.warn('storage init failed, continuing', e)
  }

  if (!ensureAppLock()) {
    document.body.innerHTML =
      '<div style="padding:24px;font-family:sans-serif;text-align:center">已锁定。请重新打开应用并输入正确 PIN。</div>'
    return
  }

  usePrefs().applyTheme()
  installMobileShell()

  const app = createApp(App)
  app.use(router)

  router.afterEach((to) => {
    setRouteChrome(to.path)
  })
  setRouteChrome(router.currentRoute.value?.path || '/')

  app.mount('#app')
}

bootstrap().catch((err) => {
  console.error('App bootstrap failed', err)
  try {
    usePrefs().applyTheme()
  } catch {
    /* ignore */
  }
  installMobileShell()
  const app = createApp(App)
  app.use(router)
  app.mount('#app')
})
