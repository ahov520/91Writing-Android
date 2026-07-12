import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)


// Android WebView polish hook
try {
  const ua = navigator.userAgent || ''
  if (/Android/i.test(ua) || window.Writing91Android) {
    document.documentElement.classList.add('writing91-android'); document.documentElement.setAttribute('data-platform', 'android') // Round 874
  }
} catch (e) {}


// Round 421: expose viewport height CSS var for mobile browser chrome
try {
  const setVH = () => {
    const h = (window.visualViewport && window.visualViewport.height) || window.innerHeight
    document.documentElement.style.setProperty('--app-vh', h + 'px')
  }
  setVH()
  window.addEventListener('resize', setVH)
  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', setVH)
  }
} catch (e) {}

app.mount('#app')