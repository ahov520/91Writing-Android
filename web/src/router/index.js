import { createRouter, createWebHashHistory } from 'vue-router'
import MobileShell from '../layouts/MobileShell.vue'

const routes = [
  {
    path: '/',
    component: MobileShell,
    children: [
      {
        path: '',
        name: 'Library',
        component: () => import('../views/mobile/Library.vue'),
        meta: { title: '书架' }
      },
      {
        path: 'backup',
        name: 'Backup',
        component: () => import('../views/mobile/Backup.vue'),
        meta: { title: '备份' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/mobile/Settings.vue'),
        meta: { title: '设置' }
      },
      {
        path: 'write/:id',
        name: 'Writer',
        component: () => import('../views/mobile/Writer.vue'),
        meta: { title: '写作', immersive: true }
      }
    ]
  },
  // legacy paths → new UI
  { path: '/novels', redirect: '/' },
  { path: '/writer', redirect: '/' },
  { path: '/config', redirect: '/settings' },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} · 91写作`
  }
})

export default router
