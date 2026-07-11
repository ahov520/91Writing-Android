<template>
  <div class="dashboard-container" :class="{ 'is-mobile': isMobile }">
    <!-- 桌面端侧边栏 -->
    <div
      v-if="!isMobile"
      class="sidebar"
      :class="{ collapsed: isCollapse }"
    >
      <div class="logo">
        <h2>📚 91写作</h2>
      </div>

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
          <span class="page-title" :title="pageTitle">{{ pageTitle }}</span>
        </div>

        <div class="header-right" v-if="!isMobile">
          <div class="model-selector" v-if="isApiConfigured">
            <el-select
              v-model="currentModel"
              @change="handleModelChange"
              size="small"
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
                  <span style="float: right; color: #8492a6; font-size: 12px">
                    {{ model.price }}
                  </span>
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
                    style="float: right; color: #8492a6; font-size: 12px"
                  >
                    {{ model.description }}
                  </span>
                </el-option>
              </el-option-group>
            </el-select>
          </div>

          <el-button
            @click="openAnnouncement"
            type="primary"
            size="small"
          >
            <el-icon><Bell /></el-icon>
            公告及教程
          </el-button>

          <el-button
            @click="openApiConfigDialog()"
            :type="isApiConfigured ? 'success' : 'warning'"
            size="small"
          >
            <el-icon><Key /></el-icon>
            {{ isApiConfigured ? 'API已配置' : 'API配置' }}
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
        <router-view />
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
      title="导航菜单"
      direction="ltr"
      :size="isSmallMobile ? '84%' : '70%'"
      class="mobile-nav-drawer"
    >
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
                <span style="float: right; color: #8492a6; font-size: 12px">
                  {{ model.price }}
                </span>
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
                  style="float: right; color: #8492a6; font-size: 12px"
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
          {{ isApiConfigured ? '管理API配置' : '去配置API' }}
        </el-button>
      </div>
    </el-drawer>
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

const handleResize = () => {
  isMobile.value = window.innerWidth <= 1024
  isSmallMobile.value = window.innerWidth <= 480
  if (isMobile.value) {
    isCollapse.value = false
  } else {
    mobileMenuVisible.value = false
    mobileActionsVisible.value = false
  }
}

// 监听路由变化
watch(() => route.path, (newPath) => {
  const match = navigationMenuItems.value.find(item => item.path === newPath)
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

onMounted(() => {
  initializeModelSelector()
  handleResize()

  window.addEventListener('storage', handleStorageChange)
  window.addEventListener('resize', handleResize)

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
  background: linear-gradient(180deg, #f6f7f8 0%, #f2f3f5 100%);
}

.dashboard-container.is-mobile {
  flex-direction: column;
}

.sidebar {
  width: 250px;
  background: linear-gradient(180deg, #15181d 0%, #1f232a 100%);
  color: white;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar.collapsed { width: 64px; }
.sidebar.collapsed .logo h2 { display: none; }

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.02));
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: white;
  margin: 0;
}

.logo h2 { margin: 0; font-size: 18px; white-space: nowrap; }

.sidebar-menu { border: none; background: transparent; height: calc(100vh - 60px); }
.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title { color: #cfd5de; border-bottom: none; }
.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover { background-color: rgba(255, 255, 255, 0.1); color: #ffffff; }
.sidebar-menu .el-menu-item.is-active { background: linear-gradient(90deg, #586170 0%, #3d4450 100%); color: white; }

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.header {
  height: 60px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.header-left { display: flex; align-items: center; gap: 12px; }
.collapse-btn { font-size: 18px; padding: 6px; }

.page-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: #1f2d3d;
  max-width: min(50vw, 260px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right { display: flex; align-items: center; gap: 15px; }
.header-right.mobile { gap: 8px; }
.icon-btn { min-width: auto; padding: 6px; border: none; }
.icon-btn :deep(.el-icon) { font-size: 20px; }

.model-selector { display: flex; align-items: center; }
.model-select { min-width: 210px; }

.model-selector :deep(.el-select-group__title) {
  font-weight: 600;
  color: #3b4250;
  padding: 8px 12px;
  background-color: #f3f4f6;
  border-bottom: 1px solid #e4e7ed;
}
.model-selector :deep(.el-option-group .el-option) { padding-left: 20px; }
.model-selector :deep(.el-option-group:not(:last-child)) { border-bottom: 1px solid #e4e7ed; }

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: linear-gradient(160deg, #f8f8f9 0%, #f2f3f5 100%);
  transition: padding 0.3s;
}

.content-mobile { padding: 16px 14px 80px; }

.mobile-nav-drawer :deep(.el-drawer__body) { padding: 0; }
.mobile-nav-drawer .mobile-menu { height: 100%; }
.mobile-nav-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 18px 18px 12px;
  font-weight: 700;
  color: #334155;
}
.mobile-nav-drawer :deep(.el-drawer) {
  border-top-right-radius: 18px;
  border-bottom-right-radius: 18px;
}
.mobile-nav-drawer :deep(.mobile-menu .el-menu-item) {
  margin: 8px 10px;
  border-radius: 12px;
  height: 46px;
}
.mobile-nav-drawer :deep(.mobile-menu .el-menu-item.is-active) {
  background: linear-gradient(90deg, #5f6772 0%, #4a515d 100%);
  color: #fff;
}

.mobile-action-drawer { border-top-left-radius: 22px; border-top-right-radius: 22px; }
.mobile-action-drawer :deep(.el-drawer__body) {
  padding: 16px 20px 24px;
  background: linear-gradient(180deg, #f6f7f8 0%, #eff1f3 100%);
  border-top-left-radius: 22px;
  border-top-right-radius: 22px;
}
.mobile-actions { display: flex; flex-direction: column; gap: 16px; }
.mobile-drawer-handle { width: 100%; display: flex; justify-content: center; }
.mobile-drawer-handle span { display: inline-block; width: 36px; height: 4px; background-color: #dcdfe6; border-radius: 2px; }
.mobile-section-title { margin: 8px 0 0; font-size: 16px; font-weight: 700; color: #303133; }
.mobile-model-selector { display: flex; flex-direction: column; gap: 8px; }
.selector-label { font-size: 13px; color: #606266; }
.mobile-action-button { width: 100%; justify-content: center; height: 44px; border-radius: 12px; }

@media (max-width: 1280px) {
  .model-select { min-width: 180px; }
}

@media (max-width: 1024px) {
  .header { padding: 0 16px; }
  .page-title { font-size: 16px; }
  .content { padding: 18px; }
}

@media (max-width: 768px) {
  .header {
    height: 58px;
    padding: max(env(safe-area-inset-top), 0px) 12px 0;
    background: linear-gradient(135deg, rgba(41, 46, 54, 0.95) 0%, rgba(26, 29, 34, 0.96) 100%);
    border-bottom: none;
    box-shadow: 0 10px 18px rgba(0, 0, 0, 0.28);
    backdrop-filter: none;
  }

  .header-left,
  .header-right.mobile { height: 100%; }

  .collapse-btn,
  .icon-btn {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.16);
    color: #fff;
    padding: 0;
  }

  .page-title {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    max-width: 52vw;
  }

  .content { padding: 12px; }
  .content-mobile { padding: 12px 10px calc(84px + env(safe-area-inset-bottom)); }
}

@media (max-width: 480px) {
  .header { padding: max(env(safe-area-inset-top), 0px) 10px 0; }
  .page-title { font-size: 14px; max-width: 48vw; }
  .content { padding: 8px; }
  .content-mobile { padding: 8px 6px calc(78px + env(safe-area-inset-bottom)); }
  .mobile-action-drawer :deep(.el-drawer__body) { padding: 14px 14px 20px; }
}
</style>
