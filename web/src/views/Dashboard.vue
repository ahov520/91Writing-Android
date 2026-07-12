<template>
  <div
    class="dashboard-container app-mesh-bg"
    :class="{
      'is-mobile': isMobile,
      'is-writer-route': isWriterRoute,
      'is-keyboard-open': keyboardOpen
    }"
  >
    <!-- 桌面端侧边栏 -->
    <div
      v-if="!isMobile"
      class="sidebar"
      :class="{ collapsed: isCollapse }"
    >
      <div class="logo">
        <div class="logo-mark" aria-hidden="true">
          <span class="logo-glyph">墨</span>
        </div>
        <div class="logo-text" v-show="!isCollapse">
          <h2>91写作</h2>
          <p>AI Novel Studio</p>
        </div>
      </div>

      <div class="sidebar-scroll">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          :collapse="isCollapse"
          :collapse-transition="false"
        >
          <el-menu-item
            v-for="item in navigationMenuItems"
            :key="item.path"
            :index="item.path"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.label }}</template>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="sidebar-footer" v-show="!isCollapse">
        <div class="status-pill" :class="isApiConfigured ? 'is-on' : 'is-off'">
          <span class="status-dot"></span>
          {{ isApiConfigured ? 'API 已就绪' : 'API 未配置' }}
        </div>
        <div class="sidebar-version">91写作 · Design System</div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-container">
      <div class="header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="collapse-btn"
          >
            <el-icon>
              <Menu v-if="isMobile" />
              <Expand v-else-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <div class="title-block">
            <span class="page-title" :title="pageTitle">{{ pageTitle }}</span>
            <span class="page-subtitle" v-if="!isMobile">墨韵 · 紫金· AI 辅助创作</span>
          </div>
        </div>

        <div class="header-right" v-if="!isMobile">
          <div class="model-selector" v-if="isApiConfigured">
            <el-select
              v-model="currentModel"
              @change="handleModelChange"
              size="default"
              placeholder="选择模型"
              class="model-select"
            >
              <el-option-group label="🏢 91写作官方模型">
                <el-option
                  v-for="model in officialModels"
                  :key="model.id"
                  :label="model.name"
                  :value="model.id"
                >
                  <span>{{ model.name }}</span>
                  <span class="option-meta">{{ model.price }}</span>
                </el-option>
              </el-option-group>

              <el-option-group label="⚙️ 自定义模型" v-if="customModels.length > 0">
                <el-option
                  v-for="model in customModels"
                  :key="model.id"
                  :label="model.name"
                  :value="model.id"
                >
                  <span>{{ model.name }}</span>
                  <span
                    v-if="model.description"
                    class="option-meta"
                  >
                    {{ model.description }}
                  </span>
                </el-option>
              </el-option-group>
            </el-select>
          </div>

          <el-button
            @click="openAnnouncement"
            class="header-btn ghost-btn"
            size="default"
          >
            <el-icon><Bell /></el-icon>
            公告
          </el-button>

          <el-button
            @click="openApiConfigDialog()"
            :type="isApiConfigured ? 'success' : 'warning'"
            class="header-btn"
            size="default"
          >
            <el-icon><Key /></el-icon>
            {{ isApiConfigured ? 'API 已配置' : 'API 配置' }}
          </el-button>
        </div>

        <div class="header-right mobile" v-else>
          <el-button type="text" class="icon-btn" @click="openAnnouncement">
            <el-icon><Bell /></el-icon>
          </el-button>
          <el-button
            type="text"
            class="icon-btn"
            @click="mobileActionsVisible = true"
          >
            <el-icon><MoreFilled /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="content" :class="{ 'content-mobile': isMobile }">
        <div class="content-inner anim-fade-up">
          <router-view />
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showApiConfig"
      title="API配置"
      width="1000px"
      :fullscreen="isMobile"
    >
      <ApiConfig @close="showApiConfig = false" />
    </el-dialog>

    <AnnouncementDialog
      v-model:visible="showAnnouncement"
      :announcement="currentAnnouncement"
      @close="handleAnnouncementClose"
    />

    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      :size="isSmallMobile ? '86%' : '72%'"
      class="mobile-nav-drawer"
    >
      <template #header>
        <div class="mobile-drawer-brand">
          <div class="logo-mark sm" aria-hidden="true">
            <span class="logo-glyph">墨</span>
          </div>
          <div>
            <strong>91写作</strong>
            <p>导航菜单</p>
          </div>
        </div>
      </template>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu mobile-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item
          v-for="item in navigationMenuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-drawer
      v-model="mobileActionsVisible"
      direction="btt"
      size="auto"
      class="mobile-action-drawer"
      :with-header="false"
    >
      <div class="mobile-actions">
        <div class="mobile-drawer-handle" @click="mobileActionsVisible = false">
          <span></span>
        </div>

        <h3 class="mobile-section-title">快速操作</h3>

        <div v-if="isApiConfigured" class="mobile-model-selector">
          <label class="selector-label">快速切换模型</label>
          <el-select
            v-model="currentModel"
            @change="handleModelChange"
            placeholder="选择模型"
            size="large"
            class="model-select"
          >
            <el-option-group label="🏢 91写作官方模型">
              <el-option
                v-for="model in officialModels"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              >
                <span>{{ model.name }}</span>
                <span class="option-meta">{{ model.price }}</span>
              </el-option>
            </el-option-group>
            <el-option-group label="⚙️ 自定义模型" v-if="customModels.length > 0">
              <el-option
                v-for="model in customModels"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              >
                <span>{{ model.name }}</span>
                <span
                  v-if="model.description"
                  class="option-meta"
                >
                  {{ model.description }}
                </span>
              </el-option>
            </el-option-group>
          </el-select>
        </div>

        <el-button
          type="primary"
          class="mobile-action-button"
          @click="openAnnouncement"
        >
          <el-icon><Bell /></el-icon>
          公告及教程
        </el-button>

        <el-button
          class="mobile-action-button"
          :type="isApiConfigured ? 'success' : 'warning'"
          @click="openApiConfigDialog(true)"
        >
          <el-icon><Key /></el-icon>
          {{ isApiConfigured ? '管理 API 配置' : '去配置 API' }}
        </el-button>
      </div>
    </el-drawer>

    <!-- 移动端底部导航：写作页沉浸 / 键盘弹出时隐藏 -->
    <nav
      v-if="isMobile && showMobileBottomNav"
      class="mobile-bottom-nav"
      :class="{ 'is-keyboard-open': keyboardOpen }"
      aria-label="主导航"
    >
      <button
        v-for="item in mobileNavItems"
        :key="item.path"
        type="button"
        class="nav-item"
        :class="{ active: isNavActive(item.path) }"
        @click="handleMenuSelect(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.short }}</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNovelStore } from '@/stores/novel'
import {
  House, Document, ChatLineSquare, Collection, Notebook, Aim,
  CreditCard, Setting, Key, Tools, EditPen, DataAnalysis,
  Expand, Fold, Bell, Menu, MoreFilled, DataBoard
} from '@element-plus/icons-vue'
import ApiConfig from '@/components/ApiConfig.vue'
import AnnouncementDialog from '@/components/AnnouncementDialog.vue'
import { getLatestAnnouncement } from '@/config/announcements.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const novelStore = useNovelStore()

const navigationItems = [
  { path: '/', label: '首页', icon: House },
  { path: '/novels', label: '小说列表', icon: Document },
  { path: '/prompts', label: '提示词库', icon: ChatLineSquare },
  { path: '/genres', label: '小说类型管理', icon: Collection },
  { path: '/chapters', label: '章节管理', icon: Notebook },
  { path: '/goals', label: '写作目标', icon: Aim },
  { path: '/billing', label: 'Token计费', icon: CreditCard },
  { path: '/tools', label: '工具库', icon: Tools },
  { path: '/short-story', label: '短文写作', icon: EditPen },
  { path: '/book-analysis', label: '拆书工具', icon: DataAnalysis },
  { path: '/studio', label: '创作中台', icon: DataBoard },
  { path: '/settings', label: '系统设置', icon: Setting },
  { path: '/writer', label: '写作工坊', icon: EditPen, showInMenu: false }
]

const navigationMenuItems = computed(() => navigationItems.filter(item => item.showInMenu !== false))

const mobileNavItems = computed(() => [
  { path: '/', short: '首页', icon: House },
  { path: '/novels', short: '小说', icon: Document },
  { path: '/writer', short: '写作', icon: EditPen },
  { path: '/tools', short: '工具', icon: Tools },
  { path: '/settings', short: '设置', icon: Setting },
])

// P5: writer immersion + keyboard — hide bottom chrome when editing
const isWriterRoute = computed(() => {
  const p = route.path || ''
  return p === '/writer' || p.startsWith('/writer')
})
const keyboardOpen = ref(false)
const showMobileBottomNav = computed(() => !isWriterRoute.value && !keyboardOpen.value)

const isNavActive = (path) => {
  const current = activeMenu.value || route.path || '/'
  if (path === '/') return current === '/'
  return current === path || current.startsWith(path + '/') || current.startsWith(path)
}

// 响应式数据
const isCollapse = ref(false)
const isMobile = ref(false)
const isSmallMobile = ref(false)
const mobileMenuVisible = ref(false)
const mobileActionsVisible = ref(false)
const showApiConfig = ref(false)
const showAnnouncement = ref(false)
const currentAnnouncement = ref({})
const activeMenu = ref('/')
const currentModel = ref('')
const configType = ref('official')
const forceUpdate = ref(0)

// 计算属性
const isApiConfigured = computed(() => novelStore.isApiConfigured)

const currentApiConfig = computed(() => {
  return novelStore.getCurrentApiConfig()
})

const pageTitle = computed(() => {
  const match = navigationItems.find(item => item.path === route.path)
  return match ? match.label : '首页'
})

// 官方模型列表（固定）
const officialModels = computed(() => [
  {
    id: 'claude-4-sonnet',
    name: 'Claude-4 Sonnet',
    description: '最新一代Claude模型，擅长创意写作和长文本处理',
    price: '￥0.1/次'
  },
  {
    id: 'claude-opus-4-20250514',
    name: 'Claude Opus 4',
    description: '最强性能Claude模型，顶级创作能力',
    price: '￥0.5/次'
  },
  {
    id: 'claude-3-7-sonnet-thinking',
    name: 'Claude-3.7 Sonnet Thinking',
    description: '具备思维链的Claude模型，逻辑推理强',
    price: '￥0.2/次'
  },
  {
    id: 'claude-3-7-sonnet-20250219',
    name: 'Claude-3.7 Sonnet',
    description: '高性能版本，平衡性能与成本',
    price: '￥0.1/次'
  }
])

// 自定义模型列表（从API配置中读取）
const customModels = computed(() => {
  forceUpdate.value

  const models = []

  try {
    const savedCustomModels = localStorage.getItem('customModels')
    if (savedCustomModels) {
      const parsed = JSON.parse(savedCustomModels)
      models.push(...parsed)
    }

    const defaultCustomModels = [
      {
        id: 'deepseek-reasoner',
        name: 'deepseek-r1',
        description: '深度思考推理模型'
      },
      {
        id: 'deepseek-chat',
        name: 'deepseek-v3',
        description: '深度求索对话模型'
      },
      {
        id: 'gpt-4o',
        name: 'GPT-4o',
        description: 'OpenAI最新多模态模型'
      },
      {
        id: 'gpt-4o-mini',
        name: 'GPT-4o mini',
        description: 'GPT-4o轻量版本'
      },
      {
        id: 'gpt-3.5-turbo',
        name: 'GPT-3.5 Turbo',
        description: 'OpenAI经典对话模型'
      }
    ]

    const allModels = [...defaultCustomModels]
    for (const model of models) {
      if (!allModels.find(m => m.id === model.id)) {
        allModels.push(model)
      }
    }

    console.log('自定义模型列表:', allModels)
    return allModels
  } catch (error) {
    console.error('读取自定义模型失败:', error)
    return []
  }
})

// 方法
const toggleSidebar = () => {
  if (isMobile.value) {
    mobileMenuVisible.value = true
  } else {
    isCollapse.value = !isCollapse.value
  }
}

const handleMenuSelect = (index) => {
  router.push(index)
  if (isMobile.value) {
    mobileMenuVisible.value = false
  }
}

const openApiConfigDialog = (fromMobile = false) => {
  showApiConfig.value = true
  if (fromMobile) {
    mobileActionsVisible.value = false
  }
}

// 公告相关功能
const openAnnouncement = () => {
  try {
    currentAnnouncement.value = getLatestAnnouncement()
    showAnnouncement.value = true
    if (isMobile.value) {
      mobileActionsVisible.value = false
    }
  } catch (error) {
    console.error('获取公告错误:', error)
  }
}

const handleAnnouncementClose = () => {
  showAnnouncement.value = false
}

// 模型相关功能
const handleModelChange = (modelId) => {
  try {
    console.log('切换模型:', modelId)

    const isOfficialModel = officialModels.value.find(m => m.id === modelId)
    const isCustomModel = customModels.value.find(m => m.id === modelId)

    let newConfig = {}
    let newConfigType = ''

    if (isOfficialModel) {
      console.log('选择了官方模型，切换到官方配置')
      newConfigType = 'official'

      const savedOfficialConfig = localStorage.getItem('officialApiConfig')
      if (savedOfficialConfig) {
        newConfig = JSON.parse(savedOfficialConfig)
      } else {
        newConfig = {
          baseURL: 'https://ai.91hub.vip/v1',
          maxTokens: 2000000,
          unlimitedTokens: false,
          temperature: 0.7,
          apiKey: ''
        }
      }
      newConfig.selectedModel = modelId

      localStorage.setItem('apiConfigType', 'official')
      localStorage.setItem('officialApiConfig', JSON.stringify(newConfig))
    } else if (isCustomModel) {
      console.log('选择了自定义模型，切换到自定义配置')
      newConfigType = 'custom'

      const savedCustomConfig = localStorage.getItem('customApiConfig')
      if (savedCustomConfig) {
        newConfig = JSON.parse(savedCustomConfig)
      } else {
        newConfig = {
          baseURL: 'https://api.openai.com/v1',
          maxTokens: 2000000,
          unlimitedTokens: false,
          temperature: 0.7,
          apiKey: ''
        }
      }
      newConfig.selectedModel = modelId

      localStorage.setItem('apiConfigType', 'custom')
      localStorage.setItem('customApiConfig', JSON.stringify(newConfig))
    } else {
      console.error('未知的模型类型:', modelId)
      ElMessage.error('未知的模型类型')
      return
    }

    configType.value = newConfigType
    novelStore.updateApiConfig(newConfig, newConfigType)
    novelStore.switchConfigType(newConfigType)
    forceUpdate.value++

    const modelName = getModelDisplayName(modelId)
    const configTypeName = newConfigType === 'official' ? '官方配置' : '自定义配置'
    const needsApiKey = !newConfig.apiKey || newConfig.apiKey.trim() === ''

    if (needsApiKey) {
      ElMessage.warning(`已切换到${configTypeName}: ${modelName}，请先配置API密钥`)
      setTimeout(() => {
        showApiConfig.value = true
      }, 1000)
    } else {
      ElMessage.success(`已切换到${configTypeName}: ${modelName}`)
    }

    if (isMobile.value) {
      mobileActionsVisible.value = false
    }

    console.log('配置切换完成:', { configType: newConfigType, config: newConfig, needsApiKey })
  } catch (error) {
    console.error('切换模型失败:', error)
    ElMessage.error('切换模型失败: ' + error.message)
  }
}

const getModelDisplayName = (modelId) => {
  let model = officialModels.value.find(m => m.id === modelId)
  if (model) return model.name

  model = customModels.value.find(m => m.id === modelId)
  if (model) return model.name

  return modelId
}

const initializeModelSelector = () => {
  try {
    const savedConfigType = localStorage.getItem('apiConfigType') || 'official'
    configType.value = savedConfigType

    if (isApiConfigured.value && currentApiConfig.value) {
      currentModel.value = currentApiConfig.value.selectedModel || ''
    }

    forceUpdate.value++

    console.log('模型选择器初始化完成, 配置类型:', savedConfigType, '当前模型:', currentModel.value)
  } catch (error) {
    console.error('初始化模型选择器失败:', error)
  }
}

const syncKeyboardState = () => {
  try {
    keyboardOpen.value =
      document.documentElement.classList.contains('keyboard-open') ||
      document.documentElement.getAttribute('data-keyboard') === 'open'
  } catch {
    keyboardOpen.value = false
  }
}

const handleResize = () => {
  // Prefer visualViewport width on Android when keyboard is open
  const vw =
    (window.visualViewport && window.visualViewport.width) || window.innerWidth
  isMobile.value = vw <= 1024
  isSmallMobile.value = vw <= 480
  if (isMobile.value) {
    isCollapse.value = false
  } else {
    mobileMenuVisible.value = false
    mobileActionsVisible.value = false
  }
  syncKeyboardState()
}

// 监听路由变化
watch(() => route.path, (newPath) => {
  const match = navigationItems.find(item => item.path === newPath)
  activeMenu.value = match ? newPath : '/'
  if (isMobile.value) {
    mobileMenuVisible.value = false
    mobileActionsVisible.value = false
  }
}, { immediate: true })

// 监听API配置变化，更新模型选择器
watch(() => [isApiConfigured.value, currentApiConfig.value], () => {
  initializeModelSelector()
}, { immediate: true })

// 监听localStorage变化
const handleStorageChange = (event) => {
  if (event.key === 'apiConfigType' || event.key === 'officialApiConfig' || event.key === 'customApiConfig' || event.key === 'customModels') {
    console.log('检测到localStorage配置变化:', event.key, event.newValue)
    setTimeout(() => {
      initializeModelSelector()
    }, 100)
  }
}

let keyboardObserver = null

onMounted(() => {
  initializeModelSelector()
  handleResize()
  syncKeyboardState()

  window.addEventListener('storage', handleStorageChange)
  window.addEventListener('resize', handleResize)
  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', handleResize)
  }

  // Watch html class toggled by mobileShell.js
  try {
    keyboardObserver = new MutationObserver(() => syncKeyboardState())
    keyboardObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class', 'data-keyboard']
    })
  } catch {
    /* ignore */
  }

  const checkConfigChange = () => {
    const currentType = localStorage.getItem('apiConfigType')
    if (currentType !== configType.value) {
      console.log('检测到配置类型变化:', configType.value, '->', currentType)
      initializeModelSelector()
    }
  }

  const intervalId = setInterval(checkConfigChange, 1000)
  window.modelSelectorInterval = intervalId
})

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
  window.removeEventListener('resize', handleResize)
  if (window.visualViewport) {
    window.visualViewport.removeEventListener('resize', handleResize)
  }
  if (keyboardObserver) {
    keyboardObserver.disconnect()
    keyboardObserver = null
  }
  if (window.modelSelectorInterval) {
    clearInterval(window.modelSelectorInterval)
    delete window.modelSelectorInterval
  }
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  height: 100vh;
  height: 100dvh;
  background: transparent;
  color: var(--text-primary);
}

.dashboard-container.is-mobile {
  flex-direction: column;
}

/* ---------- Sidebar ---------- */
.sidebar {
  width: var(--sidebar-w);
  background: var(--grad-sidebar);
  color: var(--text-1);
  display: flex;
  flex-direction: column;
  transition: width var(--dur) var(--ease-out);
  overflow: hidden;
  border-right: 1px solid var(--border-subtle);
  box-shadow: 8px 0 40px rgba(15, 10, 40, 0.28);
  position: relative;
  z-index: 20;
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 80% 40% at 20% -10%, rgba(139, 92, 246, 0.35), transparent 60%),
    radial-gradient(ellipse 60% 30% at 90% 100%, rgba(236, 72, 153, 0.18), transparent 55%);
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed);
}

.logo {
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid var(--border-subtle);
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.sidebar.collapsed .logo {
  justify-content: center;
  padding: 0;
}

.logo-mark {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  background: var(--grad-brand);
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

.logo-mark.sm {
  width: 36px;
  height: 36px;
  border-radius: 12px;
}

.logo-glyph {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1;
}

.logo-text h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.02em;
  white-space: nowrap;
  background: linear-gradient(90deg, #fff 0%, #e9d5ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.logo-text p {
  margin: 2px 0 0;
  font-size: 11px;
  color: var(--text-3);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  white-space: nowrap;
}

.sidebar-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  z-index: 1;
  padding: 12px 10px 8px;
}

.sidebar-menu {
  border: none !important;
  background: transparent !important;
  width: 100%;
}

.sidebar-menu :deep(.el-menu-item) {
  color: var(--text-2);
  border-radius: 12px;
  margin: 3px 0;
  height: 44px;
  line-height: 44px;
  border: 1px solid transparent;
  transition: all var(--dur) var(--ease-out);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: inherit;
  font-size: 18px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: var(--bg-glass) !important;
  color: #fff !important;
  border-color: var(--border-subtle);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.45), rgba(236, 72, 153, 0.28)) !important;
  color: #fff !important;
  border-color: rgba(167, 139, 250, 0.35);
  box-shadow: 0 8px 20px rgba(124, 58, 237, 0.25), inset 0 1px 0 rgba(255, 255, 255, 0.12);
  font-weight: 700;
}

.sidebar.collapsed .sidebar-menu :deep(.el-menu-item) {
  padding: 0 !important;
  justify-content: center;
}

.sidebar-footer {
  padding: 12px 16px 18px;
  border-top: 1px solid var(--border-subtle);
  position: relative;
  z-index: 1;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  background: var(--bg-glass);
  border: 1px solid var(--border-subtle);
  color: var(--text-2);
}

.status-pill.is-on {
  color: #a7f3d0;
  border-color: rgba(16, 185, 129, 0.35);
  background: rgba(16, 185, 129, 0.12);
}

.status-pill.is-off {
  color: #fde68a;
  border-color: rgba(245, 158, 11, 0.35);
  background: rgba(245, 158, 11, 0.12);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.08);
}

.status-pill.is-on .status-dot {
  animation: pulse-dot 2s ease infinite;
}

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); }
  50% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0.08); }
}

/* ---------- Main ---------- */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.header {
  height: var(--header-h);
  min-height: var(--header-h);
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid rgba(124, 58, 237, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  box-shadow: 0 10px 30px rgba(79, 70, 229, 0.06);
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(18px) saturate(1.4);
  -webkit-backdrop-filter: blur(18px) saturate(1.4);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.collapse-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px !important;
  font-size: 18px;
  padding: 0 !important;
  color: #4c1d95 !important;
  background: rgba(124, 58, 237, 0.08) !important;
  border: 1px solid rgba(124, 58, 237, 0.12) !important;
  transition: all var(--dur) var(--ease-out);
}

.collapse-btn:hover {
  background: rgba(124, 58, 237, 0.16) !important;
  transform: translateY(-1px);
}

.title-block {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 2px;
}

.page-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.01em;
  color: #1e1b4b;
  max-width: min(50vw, 280px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 11px;
  color: var(--text-secondary);
  letter-spacing: 0.04em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right.mobile {
  gap: 8px;
}

.icon-btn {
  min-width: auto;
  padding: 6px;
  border: none;
}

.icon-btn :deep(.el-icon) {
  font-size: 20px;
}

.model-selector {
  display: flex;
  align-items: center;
}

.model-select {
  min-width: 220px;
}

.header-btn {
  border-radius: 12px !important;
  font-weight: 700;
}

.ghost-btn {
  background: rgba(124, 58, 237, 0.08) !important;
  border: 1px solid rgba(124, 58, 237, 0.16) !important;
  color: #5b21b6 !important;
}

.ghost-btn:hover {
  background: rgba(124, 58, 237, 0.14) !important;
}

.option-meta {
  float: right;
  color: #94a3b8;
  font-size: 12px;
  margin-left: 12px;
}

.model-selector :deep(.el-select-group__title) {
  font-weight: 700;
  color: #5b21b6;
  padding: 8px 12px;
  background: linear-gradient(90deg, #f5f3ff, #faf5ff);
  border-bottom: 1px solid rgba(124, 58, 237, 0.1);
}

.model-selector :deep(.el-option-group .el-option) {
  padding-left: 20px;
}

.model-selector :deep(.el-option-group:not(:last-child)) {
  border-bottom: 1px solid rgba(124, 58, 237, 0.08);
}

.content {
  flex: 1;
  padding: 22px 24px 28px;
  overflow-y: auto;
  background: transparent;
  transition: padding var(--dur) var(--ease-out);
}

.content-inner {
  max-width: 1280px;
  margin: 0 auto;
}

.content-mobile {
  padding: 16px 14px 80px;
}

/* ---------- Mobile drawers ---------- */
.mobile-drawer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mobile-drawer-brand strong {
  display: block;
  font-size: 16px;
  color: #f8fafc;
}

.mobile-drawer-brand p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #c4b5fd;
  font-weight: 500;
}

.mobile-nav-drawer :deep(.el-drawer__body) {
  padding: 8px 0 16px;
  background: linear-gradient(180deg, #0c0e18 0%, #121528 60%, #1a1035 100%);
}

.mobile-nav-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 18px 16px 14px;
  background: linear-gradient(135deg, #1a1035, #0c0e18);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.mobile-nav-drawer :deep(.el-drawer__close-btn) {
  color: #e9d5ff;
}

.mobile-nav-drawer .mobile-menu {
  height: 100%;
  background: transparent !important;
  border: none !important;
  padding: 8px 10px;
}

.mobile-nav-drawer :deep(.mobile-menu .el-menu-item) {
  margin: 4px 0;
  border-radius: 12px;
  height: 46px;
  color: #cbd5e1;
}

.mobile-nav-drawer :deep(.mobile-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

.mobile-nav-drawer :deep(.mobile-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.5), rgba(236, 72, 153, 0.3)) !important;
  color: #fff !important;
  font-weight: 700;
}

.mobile-action-drawer :deep(.el-drawer__body) {
  padding: 16px 20px 24px;
  background: linear-gradient(180deg, #faf8ff 0%, #f0ecff 100%);
  border-top-left-radius: 22px;
  border-top-right-radius: 22px;
}

.mobile-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mobile-drawer-handle {
  width: 100%;
  display: flex;
  justify-content: center;
}

.mobile-drawer-handle span {
  display: inline-block;
  width: 40px;
  height: 4px;
  background: linear-gradient(90deg, #c4b5fd, #a78bfa);
  border-radius: 2px;
}

.mobile-section-title {
  margin: 4px 0 0;
  font-size: 16px;
  font-weight: 800;
  color: #1e1b4b;
}

.mobile-model-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selector-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
}

.mobile-action-button {
  width: 100%;
  justify-content: center;
  height: 46px;
  border-radius: 14px !important;
  font-weight: 700;
}

@media (max-width: 1280px) {
  .model-select {
    min-width: 180px;
  }
}

@media (max-width: 1024px) {
  .header {
    padding: 0 16px;
  }
  .page-title {
    font-size: 16px;
  }
  .content {
    padding: 18px;
  }
}

@media (max-width: 768px) {
  .header {
    height: 58px;
    min-height: calc(58px + env(safe-area-inset-top));
    padding: max(env(safe-area-inset-top), 0px) 12px 0;
    background: linear-gradient(135deg, #4c1d95 0%, #6d28d9 40%, #7c3aed 70%, #db2777 100%);
    border-bottom: none;
    box-shadow: 0 12px 28px rgba(76, 29, 149, 0.35);
    backdrop-filter: none;
  }

  .header-left,
  .header-right.mobile {
    height: 100%;
  }

  .collapse-btn,
  .icon-btn {
    width: 36px;
    height: 36px;
    border-radius: 12px !important;
    background: rgba(255, 255, 255, 0.16) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    color: #fff !important;
    padding: 0 !important;
  }

  .page-title {
    font-size: 15px;
    font-weight: 700;
    color: #fff;
    max-width: 52vw;
  }

  .content {
    padding: 12px;
  }

  .content-mobile {
    padding: 12px 10px calc(84px + env(safe-area-inset-bottom));
  }
}

@media (max-width: 480px) {
  .header {
    padding: max(env(safe-area-inset-top), 0px) 10px 0;
  }
  .page-title {
    font-size: 14px;
    max-width: 48vw;
  }
  .content {
    padding: 8px;
  }
  .content-mobile {
    padding: 8px 6px calc(78px + env(safe-area-inset-bottom));
  }
  .mobile-action-drawer :deep(.el-drawer__body) {
    padding: 14px 14px 20px;
  }
}

/* Round 17 mobile bottom nav */
.mobile-bottom-nav {
  position: fixed;
  left: 10px;
  right: 10px;
  bottom: calc(10px + env(safe-area-inset-bottom, 0px));
  z-index: 40;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 4px;
  padding: 8px 6px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(18px) saturate(1.4);
  -webkit-backdrop-filter: blur(18px) saturate(1.4);
  border: 1px solid rgba(124, 58, 237, 0.14);
  box-shadow: 0 12px 36px rgba(76, 29, 149, 0.16);
}
.mobile-bottom-nav .nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  min-height: 48px;
  border: none;
  background: transparent;
  border-radius: 14px;
  color: #64748b;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}
.mobile-bottom-nav .nav-item :deep(.el-icon) {
  font-size: 18px;
}
.mobile-bottom-nav .nav-item.active {
  color: #6d28d9;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.14), rgba(236, 72, 153, 0.1));
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.7);
}
.dashboard-container.is-mobile .content-mobile {
  padding-bottom: calc(96px + env(safe-area-inset-bottom, 0px));
}

/* Round 24 sidebar */
.sidebar-scroll::-webkit-scrollbar { width: 5px; }
.sidebar-scroll::-webkit-scrollbar-thumb {
  background: rgba(167, 139, 250, 0.45);
  border-radius: 8px;
}
.sidebar.collapsed .logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 12px;
}
.sidebar.collapsed .sidebar-scroll {
  padding: 10px 8px;
}
.sidebar.collapsed .sidebar-menu :deep(.el-menu-item) {
  margin: 4px 0;
}

/* Round 32 model */
.model-select :deep(.el-select__wrapper) {
  border-radius: 12px !important;
  background: rgba(255,255,255,0.9);
  box-shadow: 0 0 0 1px rgba(124,58,237,0.14) inset !important;
  min-height: 36px;
}
.header-btn.el-button {
  height: 36px;
}

/* Round 39 writer route */
.dashboard-container:has(.writer-container) .content,
.dashboard-container:has(.writer-container) .content-inner {
  max-width: none;
  padding: 0;
}
.dashboard-container:has(.writer-container) .content-mobile {
  padding: 0 0 calc(84px + env(safe-area-inset-bottom));
}
/* P5: writer immersion — no bottom nav, use full height */
.dashboard-container.is-writer-route .content-mobile,
.dashboard-container.is-writer-route:has(.writer-container) .content-mobile {
  padding: 0 !important;
  height: var(--app-vh, 100dvh);
  max-height: var(--app-vh, 100dvh);
  overflow: hidden;
}
.dashboard-container.is-writer-route .header {
  /* keep thin chrome; writer has its own title bar */
  flex-shrink: 0;
}
.dashboard-container.is-keyboard-open .mobile-bottom-nav {
  display: none !important;
}
.dashboard-container.is-keyboard-open.is-mobile .content-mobile {
  padding-bottom: 12px;
}
.dashboard-container.is-mobile .main-container {
  min-height: var(--app-vh, 100dvh);
  max-height: var(--app-vh, 100dvh);
}
.dashboard-container.is-mobile .content {
  overflow: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}
@media (max-width: 1024px) {
  .mobile-bottom-nav .nav-item.active {
    transform: translateY(-1px);
  }
}

/* Round 70 landscape */
@media (max-height: 500px) and (orientation: landscape) {
  .mobile-bottom-nav {
    left: auto;
    right: 10px;
    bottom: 10px;
    width: 56px;
    grid-template-columns: 1fr;
    border-radius: 16px;
  }
  .mobile-bottom-nav .nav-item span { display: none; }
  .dashboard-container.is-mobile .content-mobile {
    padding-bottom: 16px;
    padding-right: 72px;
  }
}

/* Round 87 */
@media (max-width: 1200px) {
  .page-subtitle { display: none; }
}

/* Round 103 */
.logo-mark {
  animation: float-soft 5s ease-in-out infinite;
}
@media (prefers-reduced-motion: reduce) {
  .logo-mark { animation: none; }
}
.mobile-bottom-nav .nav-item:active {
  transform: scale(0.96);
}

/* Round 131 */
.sidebar-version {
  margin-top: 10px;
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.04em;
  padding-left: 4px;
}

/* Round 141 */
@media (max-width: 360px) {
  .mobile-bottom-nav {
    left: 6px; right: 6px; padding: 6px 4px;
  }
  .mobile-bottom-nav .nav-item {
    font-size: 9px;
    min-height: 44px;
  }
}

/* Round 142 */
.dashboard-container:has(.writer-container.reading-mode) .mobile-bottom-nav {
  opacity: 0.35;
  transform: translateY(8px);
  pointer-events: none;
}
.dashboard-container:has(.writer-container.reading-mode) .mobile-bottom-nav:hover {
  opacity: 1;
  transform: none;
  pointer-events: auto;
}

/* Round 182 */
.content-inner > * {
  min-width: 0;
}
.sidebar-menu :deep(.el-menu-item .el-menu-tooltip__trigger) {
  display: flex;
  justify-content: center;
}

/* Round 195 */
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  font-weight: 500;
}

/* Round 203 */
.header{transition:background .28s ease, box-shadow .28s ease;}

/* Round 232 */
.mobile-nav-drawer :deep(.el-menu-item .el-icon) {
  margin-right: 10px;
  font-size: 18px;
}
/* Round 233 */
.status-pill {
  max-width: 100%;
}
/* Round 234 */
.header-right .el-button + .el-button {
  margin-left: 0;
}
/* Round 235 */
.collapse-btn:active {
  transform: scale(0.96);
}

/* Round 255 */
.logo-text h2{line-height:1.1;}

/* Round 331 */
:deep(.el-button--small){border-radius:10px;font-weight:700;}

/* Round 362 */
.dashboard-container:has(.writer-container) .header {
  box-shadow: none;
  border-bottom: 1px solid rgba(124,58,237,0.08);
}
.dashboard-container:has(.writer-container) .page-subtitle {
  display: none;
}

/* Round 385 */
.mobile-bottom-nav .nav-item.active :deep(.el-icon) {
  transform: translateY(-1px);
}
.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff !important;
}
/* Round 386 */
.header .model-select {
  max-width: 240px;
}

/* Round 405 */
:deep(.el-loading-mask) {
  border-radius: inherit;
}

/* Round 432 */
.dashboard-container {
  height: 100vh;
  height: var(--app-vh, 100dvh);
}

/* Round 471 */
.sidebar-menu :deep(.el-menu-item) {
  font-weight: 600;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  letter-spacing: 0.01em;
}
/* Round 472 */
.mobile-bottom-nav {
  transition: opacity 0.28s ease, transform 0.28s ease;
}
/* Round 473 */
.content-inner {
  width: 100%;
}
/* Round 474 */
.header-right .ghost-btn :deep(.el-icon) {
  margin-right: 4px;
}
/* Round 475 */
.dashboard-container.is-mobile .header {
  z-index: 30;
}

/* Round 490 */
:deep(.el-popper) {
  max-width: min(92vw, 360px);
}

/* Round 510 */
.sidebar.collapsed .sidebar-footer {
  display: none;
}
.sidebar.collapsed .logo {
  justify-content: center;
}

/* Round 524 */
.mobile-action-drawer :deep(.el-button) {
  font-weight: 800;
}
.mobile-model-selector .model-select {
  width: 100%;
}

/* Round 545 */
.logo-mark{flex-shrink:0;}
.sidebar{user-select:none;}

/* Round 570 */
:deep(.el-alert__title) { font-weight: 800; }

/* Round 601 */
:deep(.el-tooltip__trigger:focus-visible){outline:2px solid rgba(124,58,237,.4);outline-offset:2px;}

/* Round 634 */
@media (max-width: 1100px) {
  .header-btn span {
    /* keep */
  }
  .model-select {
    min-width: 160px;
  }
}
/* Round 635 */
.mobile-drawer-brand .logo-mark {
  box-shadow: 0 8px 20px rgba(124,58,237,0.4);
}
/* Round 636 */
.nav-item span {
  line-height: 1.1;
}
/* Round 673 */
.mobile-bottom-nav .nav-item.active span{color:#6d28d9;}

/* Round 701 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 742 */
@media (prefers-reduced-motion: reduce) {
  .logo-mark,
  .status-pill.is-on .status-dot,
  .mobile-bottom-nav {
    animation: none !important;
    transition: none !important;
  }
}

/* Round 765 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 796 */
.mobile-bottom-nav .nav-item {
  position: relative;
}
.mobile-bottom-nav .nav-item.active::before {
  content: '';
  position: absolute;
  top: 4px;
  width: 16px;
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(90deg, #7c3aed, #ec4899);
}
/* Round 797 */
.sidebar-scroll {
  scrollbar-gutter: stable;
}
/* Round 798 */
.content {
  overscroll-behavior-y: contain;
}

/* Round 836 */
.mobile-nav-drawer :deep(.el-menu-item) {
  font-weight: 700;
}
/* Round 837 */
.header-left {
  flex: 1;
  min-width: 0;
}
/* Round 838 */
.page-title {
  user-select: none;
}

/* Round 871 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 896 */
:deep(.el-message-box__message) {
  line-height: 1.55;
  color: #334155;
  font-weight: 600;
}

/* Round 956 */
.mobile-bottom-nav .nav-item.active::before {
  left: 50%;
  transform: translateX(-50%);
}
/* Round 957 */
.sidebar-menu :deep(.el-menu-item span) {
  overflow: hidden;
  text-overflow: ellipsis;
}
/* Round 958 */
.header-right {
  flex-shrink: 0;
}

/* Round 982 */
.dashboard-container {
  min-height: 0;
}


/* Round 1016 */
.mobile-bottom-nav .nav-item:focus-visible {
  outline: 2px solid rgba(124, 58, 237, 0.45);
  outline-offset: 2px;
}
/* Round 1017 */
.mobile-action-drawer :deep(.el-select) {
  width: 100%;
}
/* Round 1018 */
.sidebar-footer .status-pill {
  width: 100%;
  justify-content: center;
}
/* Round 1019 */
@media (max-width: 768px) {
  .content-inner {
    max-width: none;
  }
}
/* Round 1020 */
.header .icon-btn:active {
  transform: scale(0.96);
}

/* Round 1109 */
.sidebar.collapsed .sidebar-menu :deep(.el-menu-item.is-active) {
  box-shadow: none;
}
/* Round 1110 */
.mobile-bottom-nav .nav-item span {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* Round 1111 */
.header-btn {
  white-space: nowrap;
}
/* Round 1112 */
.dashboard-container.is-mobile .main-container {
  min-height: 0;
}
/* Round 1113 */
.logo-text {
  min-width: 0;
}
/* Round 1114 */
.status-pill .status-dot {
  flex-shrink: 0;
}
/* Round 1115 */
.mobile-nav-drawer :deep(.el-drawer__body) {
  overscroll-behavior: contain;
}

/* Round 1186 */
.mobile-bottom-nav .nav-item.active::before {
  opacity: 0.95;
}
/* Round 1187 */
.sidebar::before {
  opacity: 0.95;
}
/* Round 1188 */
.content-inner.anim-fade-up {
  animation-duration: 0.4s;
}
/* Round 1189 */
.header-right.mobile .icon-btn + .icon-btn {
  margin-left: 0;
}
/* Round 1190 */
.dashboard-container.is-mobile .header .page-title {
  text-shadow: 0 1px 0 rgba(0,0,0,0.12);
}


/* Round 1218 */
.mobile-bottom-nav {
  /* prevent blur jank on some android webviews */
  -webkit-transform: translateZ(0);
  transform: translateZ(0);
}

</style>
