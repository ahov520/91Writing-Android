import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './mobile.css'
import { initStorage } from './services/storage.js'
import { installMobileShell, setRouteChrome } from './utils/mobileShell.js'
import { usePrefs } from './composables/usePrefs.js'

async function bootstrap() {
  try {
    await initStorage()
  } catch (e) {
    console.warn('storage init failed, continuing', e)
  }

  usePrefs().applyTheme()
  installMobileShell()

  const app = createApp(App)
  app.use(createPinia())
  app.use(router)

  router.afterEach((to) => {
    setRouteChrome(to.path)
  })
  setRouteChrome(router.currentRoute.value?.path || '/')

  app.mount('#app')
}

bootstrap().catch((err) => {
  console.error('App bootstrap failed', err)
  const app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.mount('#app')
})
