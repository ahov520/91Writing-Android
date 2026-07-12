import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'
import { initStorage } from './services/storage.js'
import { installMobileShell, setRouteChrome } from './utils/mobileShell.js'

async function bootstrap() {
  // P2: migrate large localStorage payloads into IndexedDB before any view reads data
  try {
    await initStorage()
  } catch (e) {
    console.warn('storage init failed, continuing with localStorage', e)
  }

  // P5: keyboard-aware viewport + platform classes
  installMobileShell()

  const app = createApp(App)
  const pinia = createPinia()

  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  app.use(pinia)
  app.use(router)
  app.use(ElementPlus)

  // Route chrome for CSS (hide bottom nav on writer, etc.)
  router.afterEach((to) => {
    setRouteChrome(to.path)
  })
  setRouteChrome(router.currentRoute.value?.path || '/')

  app.mount('#app')
}

bootstrap().catch((err) => {
  console.error('App bootstrap failed', err)
  // Last-resort mount so user is not stuck on blank screen
  const app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.use(ElementPlus)
  app.mount('#app')
})
