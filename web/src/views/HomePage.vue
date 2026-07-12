<template>
  <div class="home-page">
    <!-- Hero 欢迎区 -->
    <section class="welcome-section anim-fade-up">
      <div class="welcome-card">
        <div class="welcome-orb orb-a" aria-hidden="true"></div>
        <div class="welcome-orb orb-b" aria-hidden="true"></div>
        <div class="welcome-content">
          <div class="welcome-text">
            <span class="welcome-badge">AI Novel Studio</span>
            <h1>欢迎回来，开始今日创作</h1>
            <p>墨韵紫金 · 用 AI 续写灵感，把每一章写得更精彩</p>
          </div>
          <div class="welcome-actions">
            <el-button type="primary" size="large" class="btn-brand hero-cta" @click="createNovel">
              <el-icon><Plus /></el-icon>
              创建新小说
            </el-button>
            <el-button size="large" class="hero-secondary" @click="viewAllNovels">
              浏览作品库
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计概览 -->
    <section class="stats-section">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="6">
          <div class="stat-card anim-fade-up anim-delay-1">
            <div class="stat-item">
              <div class="stat-icon novels">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.totalNovels }}</div>
                <div class="stat-label">总小说数</div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="6">
          <div class="stat-card anim-fade-up anim-delay-2">
            <div class="stat-item">
              <div class="stat-icon words">
                <el-icon><EditPen /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ formatNumber(stats.totalWords) }}</div>
                <div class="stat-label">总字数</div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="6">
          <div class="stat-card anim-fade-up anim-delay-3">
            <div class="stat-item">
              <div class="stat-icon chapters">
                <el-icon><Notebook /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.totalChapters }}</div>
                <div class="stat-label">总章节数</div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="6">
          <div class="stat-card anim-fade-up anim-delay-4">
            <div class="stat-item">
              <div class="stat-icon tokens">
                <el-icon><CreditCard /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ formatNumber(stats.totalTokens) }}</div>
                <div class="stat-label">已用 Token</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 目标 + 快速操作 -->
    <el-row :gutter="16" class="main-content">
      <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <el-card class="goals-card panel-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="page-section-title">今日写作目标</span>
              <el-button type="primary" link @click="showGoalsDialog = true">
                管理目标
              </el-button>
            </div>
          </template>

          <div class="goals-content">
            <div
              v-for="goal in displayedGoals"
              :key="goal.id"
              class="goal-item"
            >
              <div class="goal-info">
                <span class="goal-label">{{ goal.title }}</span>
                <span class="goal-value">{{ goal.targetValue }}{{ goal.unit }}</span>
              </div>
              <div class="goal-progress">
                <el-progress
                  :percentage="getGoalProgress(goal)"
                  :stroke-width="10"
                  :show-text="false"
                />
                <span class="progress-text">{{ goal.currentValue }}{{ goal.unit }} / {{ goal.targetValue }}{{ goal.unit }}</span>
              </div>
            </div>

            <div v-if="displayedGoals.length === 0" class="no-goals">
              <el-empty description="暂无活跃目标" :image-size="72">
                <el-button type="primary" size="small" @click="showGoalsDialog = true">
                  创建目标
                </el-button>
              </el-empty>
            </div>

            <div v-if="totalActiveGoals > maxDisplayGoals" class="view-all-goals">
              <el-button type="primary" link size="small" @click="showGoalsDialog = true">
                查看全部 {{ totalActiveGoals }} 个目标 →
              </el-button>
            </div>

            <div class="streak-info" v-if="displayedGoals.length > 0">
              <el-icon class="streak-icon"><Trophy /></el-icon>
              <span>连续写作 {{ calculateStreak() }} 天 · 保持节奏</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <el-card class="quick-actions-card panel-card" shadow="never">
          <template #header>
            <span class="page-section-title">快速操作</span>
          </template>

          <div class="quick-actions">
            <div class="action-grid">
              <button type="button" class="action-item" @click="openPrompts">
                <div class="action-icon prompts">
                  <el-icon><ChatLineSquare /></el-icon>
                </div>
                <div class="action-copy">
                  <span class="action-title">提示词库</span>
                  <span class="action-desc">大纲 · 润色 · 对话</span>
                </div>
              </button>

              <button type="button" class="action-item" @click="openChapters">
                <div class="action-icon chapters">
                  <el-icon><Notebook /></el-icon>
                </div>
                <div class="action-copy">
                  <span class="action-title">章节管理</span>
                  <span class="action-desc">结构 · 进度 · 导出</span>
                </div>
              </button>

              <button type="button" class="action-item" @click="openBilling">
                <div class="action-icon billing">
                  <el-icon><CreditCard /></el-icon>
                </div>
                <div class="action-copy">
                  <span class="action-title">Token 计费</span>
                  <span class="action-desc">用量 · 成本洞察</span>
                </div>
              </button>

              <button type="button" class="action-item" @click="createNovel">
                <div class="action-icon write">
                  <el-icon><EditPen /></el-icon>
                </div>
                <div class="action-copy">
                  <span class="action-title">写作工坊</span>
                  <span class="action-desc">开新篇 · 续写</span>
                </div>
              </button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近小说 -->
    <section class="recent-novels-section">
      <el-card class="recent-novels-card panel-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="page-section-title">最近编辑</span>
            <el-button type="primary" link @click="viewAllNovels">
              查看全部
            </el-button>
          </div>
        </template>

        <div class="novels-list">
          <div
            v-for="novel in recentNovels"
            :key="novel.id"
            class="novel-item"
            @click="openNovel(novel)"
          >
            <div class="novel-cover">
              <img v-if="novel.cover" :src="novel.cover" :alt="novel.title" />
              <div v-else class="default-cover">
                <el-icon><Document /></el-icon>
              </div>
            </div>
            <div class="novel-info">
              <h4 class="novel-title">{{ novel.title }}</h4>
              <p class="novel-desc">{{ novel.description || '暂无简介，点击继续创作' }}</p>
              <div class="novel-meta">
                <span class="meta-chip">{{ formatNumber(novel.wordCount) }} 字</span>
                <span class="meta-chip muted">{{ formatTime(novel.updatedAt) }}</span>
              </div>
            </div>
            <div class="novel-actions">
              <el-button type="primary" plain size="small" class="continue-btn">
                继续写作
              </el-button>
            </div>
          </div>

          <div v-if="recentNovels.length === 0" class="empty-novels">
            <el-empty description="暂无小说，开始创作您的第一部作品吧！">
              <el-button type="primary" class="btn-brand" @click="createNovel">创建小说</el-button>
            </el-empty>
          </div>
        </div>
      </el-card>
    </section>

    <el-dialog
      v-model="showGoalsDialog"
      title="写作目标管理"
      :width="isMobile ? '94%' : '800px'"
      :fullscreen="isSmallScreen"
    >
      <WritingGoals @close="showGoalsDialog = false" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNovelStore } from '@/stores/novel'
import { 
  Plus, Edit, Document, EditPen, Notebook, CreditCard, 
  ChatLineSquare, Trophy 
} from '@element-plus/icons-vue'
import WritingGoals from '@/components/WritingGoals.vue'
import billingService from '@/services/billing.js'

const router = useRouter()
const novelStore = useNovelStore()

// 响应式数据
const showGoalsDialog = ref(false)
const isMobile = ref(false)
const isSmallScreen = ref(false)
const stats = computed(() => {
  // 从本地存储获取真实的小说数据
  const novelsData = JSON.parse(localStorage.getItem('novels') || '[]')
  
  // 使用计费服务获取真实的token使用统计
  const usageStats = billingService.getUsageStats()
  
  // 计算真实统计数据
  const totalNovels = novelsData.length
  const totalWords = novelsData.reduce((sum, novel) => sum + (novel.wordCount || 0), 0)
  const totalChapters = novelsData.reduce((sum, novel) => sum + ((novel.chapterList || []).length), 0)
  const totalTokens = usageStats.totalInputTokens + usageStats.totalOutputTokens
  
  return {
    totalNovels,
    totalWords,
    totalChapters,
    totalTokens
  }
})

// 添加响应式的目标数据状态
const goalsRefreshTrigger = ref(0)
const maxDisplayGoals = ref(3) // 首页最多显示的目标数量

// 获取所有活跃目标
const activeGoals = computed(() => {
  // 触发重新计算（通过依赖goalsRefreshTrigger）
  goalsRefreshTrigger.value
  
  // 从本地存储获取真实的写作目标数据
  const goalsData = JSON.parse(localStorage.getItem('writingGoals') || '[]')
  const active = goalsData.filter(goal => goal.status === 'active')
  
  // 按优先级排序（priority字段，数字越小优先级越高），如果没有priority则按创建时间排序
  return active.sort((a, b) => {
    if (a.priority !== undefined && b.priority !== undefined) {
      return a.priority - b.priority
    }
    if (a.priority !== undefined) return -1
    if (b.priority !== undefined) return 1
    return new Date(a.createdAt || 0) - new Date(b.createdAt || 0)
  })
})

// 首页显示的目标（限制数量）
const displayedGoals = computed(() => {
  return activeGoals.value.slice(0, maxDisplayGoals.value)
})

// 总的活跃目标数量
const totalActiveGoals = computed(() => {
  return activeGoals.value.length
})

// 兼容旧的currentGoal计算属性（保持向后兼容）
const currentGoal = computed(() => {
  const daily = activeGoals.value.find(goal => goal.type === 'daily')
  const weekly = activeGoals.value.find(goal => goal.type === 'weekly')
  
  return {
    dailyTarget: daily?.targetValue || 2000,
    dailyWritten: daily?.currentValue || 0,
    weeklyTarget: weekly?.targetValue || 14000,
    weeklyWritten: weekly?.currentValue || 0,
    streak: 0
  }
})

const recentNovels = computed(() => {
  // 从本地存储获取真实的小说数据
  const novelsData = JSON.parse(localStorage.getItem('novels') || '[]')
  
  // 按更新时间排序，取前3个
  return novelsData
    .sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0))
    .slice(0, 3)
    .map(novel => ({
      id: novel.id,
      title: novel.title,
      description: novel.description,
      wordCount: novel.wordCount || 0,
      updatedAt: new Date(novel.updatedAt || Date.now()),
      cover: novel.cover
    }))
})

// 计算属性
const dailyProgress = computed(() => {
  return Math.min(100, Math.round((currentGoal.value.dailyWritten / currentGoal.value.dailyTarget) * 100))
})

const weeklyProgress = computed(() => {
  return Math.min(100, Math.round((currentGoal.value.weeklyWritten / currentGoal.value.weeklyTarget) * 100))
})

// 新增辅助函数
const getGoalProgress = (goal) => {
  if (!goal.targetValue || goal.targetValue === 0) return 0
  return Math.min(100, Math.round((goal.currentValue / goal.targetValue) * 100))
}

const calculateStreak = () => {
  // 简化的连续天数计算逻辑
  // 可以根据实际需求实现更复杂的逻辑
  return 0
}

const getGoalTypeText = (type) => {
  const typeMap = {
    daily: '每日',
    weekly: '每周', 
    monthly: '每月',
    custom: '自定义'
  }
  return typeMap[type] || '目标'
}

// 方法
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const formatTime = (date) => {
  const now = new Date()
  const diff = now - date
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)
  
  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else {
    return '刚刚'
  }
}

const getProgressColor = (percentage) => {
  if (percentage >= 100) return '#67c23a'
  if (percentage >= 80) return '#e6a23c'
  if (percentage >= 60) return '#7c3aed'
  return '#f56c6c'
}

const createNovel = () => {
  router.push('/novels')
}

const openNovel = (novel) => {
  // 跳转到小说编辑页面
  router.push(`/writer?novelId=${novel.id}`)
}

const viewAllNovels = () => {
  router.push('/novels')
}

const openPrompts = () => {
  router.push('/prompts')
}

const openChapters = () => {
  router.push('/chapters')
}

const openBilling = () => {
  router.push('/billing')
}

// 页面获得焦点时重新计算数据，确保数据同步
const refreshData = () => {
  goalsRefreshTrigger.value++
  console.log('首页刷新目标数据')
}

const updateScreenState = () => {
  isMobile.value = window.innerWidth <= 768
  isSmallScreen.value = window.innerWidth <= 480
}

// 暴露刷新函数给全局，以便其他页面调用
window.refreshHomeData = refreshData

// 生命周期
onMounted(() => {
  updateScreenState()

  // 监听localStorage变化，以便实时更新目标数据
  window.addEventListener('storage', (e) => {
    if (e.key === 'writingGoals') {
      refreshData()
    }
  })
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      refreshData()
    }
  })

  window.addEventListener('resize', updateScreenState)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenState)
})
</script>

<style scoped>
.home-page {
  padding: 0;
  max-width: 100%;
}

/* Hero */
.welcome-section {
  margin-bottom: 18px;
}

.welcome-card {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-xl);
  padding: 36px 40px;
  color: #fff;
  background: var(--grad-hero);
  box-shadow: 0 24px 48px rgba(76, 29, 149, 0.32), inset 0 1px 0 rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.welcome-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  pointer-events: none;
  opacity: 0.55;
}

.orb-a {
  width: 220px;
  height: 220px;
  right: -40px;
  top: -60px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.55), transparent 70%);
}

.orb-b {
  width: 180px;
  height: 180px;
  left: 18%;
  bottom: -80px;
  background: radial-gradient(circle, rgba(244, 114, 182, 0.45), transparent 70%);
}

.welcome-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.welcome-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  margin-bottom: 12px;
}

.welcome-text h1 {
  margin: 0 0 10px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.welcome-text p {
  margin: 0;
  font-size: 15px;
  opacity: 0.92;
  max-width: 420px;
  line-height: 1.55;
}

.welcome-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.hero-cta {
  height: 46px;
  padding: 0 22px !important;
  border-radius: 14px !important;
  border: none !important;
}

.hero-secondary {
  height: 46px;
  border-radius: 14px !important;
  background: rgba(255, 255, 255, 0.14) !important;
  border: 1px solid rgba(255, 255, 255, 0.28) !important;
  color: #fff !important;
  font-weight: 700;
}

.hero-secondary:hover {
  background: rgba(255, 255, 255, 0.22) !important;
}

/* Stats */
.stats-section {
  margin-bottom: 18px;
}

.stats-section :deep(.el-col) {
  margin-bottom: 12px;
}

.stat-card {
  height: 100%;
  border-radius: var(--radius-md);
  padding: 18px 16px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(14px) saturate(1.3);
  -webkit-backdrop-filter: blur(14px) saturate(1.3);
  border: 1px solid rgba(124, 58, 237, 0.1);
  box-shadow: var(--shadow-sm);
  transition: transform var(--dur) var(--ease-out), box-shadow var(--dur) var(--ease-out);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: rgba(124, 58, 237, 0.22);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.22);
}

.stat-icon.novels { background: linear-gradient(135deg, #7c3aed, #a855f7); }
.stat-icon.words { background: linear-gradient(135deg, #ec4899, #f43f5e); }
.stat-icon.chapters { background: linear-gradient(135deg, #06b6d4, #0ea5e9); }
.stat-icon.tokens { background: linear-gradient(135deg, #f59e0b, #fbbf24); }

.stat-number {
  font-size: 24px;
  font-weight: 800;
  color: #1e1b4b;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
  font-weight: 600;
}

/* Panels */
.main-content {
  margin-bottom: 18px;
}

.panel-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid rgba(124, 58, 237, 0.1) !important;
  background: rgba(255, 255, 255, 0.88) !important;
  box-shadow: var(--shadow-sm) !important;
  overflow: hidden;
}

.goals-card,
.quick-actions-card {
  height: 100%;
  min-height: 360px;
  margin-bottom: 12px;
}

.goals-card :deep(.el-card__body),
.quick-actions-card :deep(.el-card__body) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.goals-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 260px;
}

.goal-item {
  margin-bottom: 12px;
  padding: 14px 16px;
  background: linear-gradient(145deg, #faf8ff 0%, #f3f0ff 100%);
  border-radius: 14px;
  border: 1px solid rgba(124, 58, 237, 0.1);
  transition: border-color var(--dur) var(--ease-out), transform var(--dur) var(--ease-out);
}

.goal-item:hover {
  border-color: rgba(124, 58, 237, 0.28);
  transform: translateY(-1px);
}

.goal-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.goal-label {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 600;
}

.goal-value {
  font-size: 13px;
  font-weight: 800;
  color: #6d28d9;
  white-space: nowrap;
}

.progress-text {
  display: block;
  text-align: right;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
  font-weight: 600;
}

.streak-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.12), rgba(251, 191, 36, 0.08));
  border: 1px solid rgba(245, 158, 11, 0.22);
  border-radius: 12px;
  margin-top: auto;
  color: #92400e;
  font-size: 13px;
  font-weight: 700;
}

.streak-icon {
  color: #f59e0b;
  font-size: 18px;
}

.no-goals {
  padding: 12px;
  text-align: center;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-all-goals {
  text-align: center;
  padding: 8px 0 4px;
}

/* Quick actions */
.quick-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid rgba(124, 58, 237, 0.1);
  border-radius: 14px;
  cursor: pointer;
  transition: all var(--dur) var(--ease-out);
  min-height: 88px;
  background: linear-gradient(160deg, #ffffff 0%, #f8f6ff 100%);
  text-align: left;
  font: inherit;
  color: inherit;
  width: 100%;
}

.action-item:hover {
  border-color: rgba(124, 58, 237, 0.35);
  background: linear-gradient(160deg, #faf8ff, #f0ecff);
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(124, 58, 237, 0.12);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 8px 16px rgba(79, 70, 229, 0.2);
}

.action-icon.prompts { background: linear-gradient(135deg, #7c3aed, #a855f7); }
.action-icon.chapters { background: linear-gradient(135deg, #0ea5e9, #06b6d4); }
.action-icon.billing { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.action-icon.write { background: linear-gradient(135deg, #ec4899, #f43f5e); }

.action-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.action-title {
  font-size: 14px;
  font-weight: 800;
  color: #1e1b4b;
}

.action-desc {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Recent novels */
.recent-novels-section {
  margin-bottom: 8px;
}

.novels-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.novel-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid rgba(124, 58, 237, 0.1);
  border-radius: 14px;
  cursor: pointer;
  transition: all var(--dur) var(--ease-out);
  background: linear-gradient(145deg, #ffffff 0%, #faf8ff 100%);
}

.novel-item:hover {
  border-color: rgba(124, 58, 237, 0.32);
  background: linear-gradient(145deg, #faf8ff, #f3f0ff);
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(124, 58, 237, 0.1);
}

.novel-cover {
  width: 56px;
  height: 74px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}

.novel-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #ede9fe, #ddd6fe);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c3aed;
  font-size: 22px;
}

.novel-info {
  flex: 1;
  min-width: 0;
}

.novel-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 800;
  color: #1e1b4b;
}

.novel-desc {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.novel-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
  color: #6d28d9;
  background: rgba(124, 58, 237, 0.1);
}

.meta-chip.muted {
  color: var(--text-secondary);
  background: rgba(100, 116, 139, 0.1);
}

.novel-actions {
  flex-shrink: 0;
}

.continue-btn {
  border-radius: 10px !important;
  font-weight: 700;
}

.empty-novels {
  padding: 28px 0;
}

@media (max-width: 1024px) {
  .welcome-card {
    padding: 28px 24px;
  }

  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .goals-card,
  .quick-actions-card {
    min-height: auto;
  }
}

@media (max-width: 768px) {
  .welcome-section,
  .stats-section,
  .main-content {
    margin-bottom: 12px;
  }

  .welcome-card {
    padding: 22px 18px;
    border-radius: var(--radius-lg);
  }

  .welcome-text h1 {
    font-size: 22px;
  }

  .welcome-text p {
    font-size: 13px;
  }

  .welcome-actions :deep(.el-button) {
    flex: 1;
    min-width: 0;
  }

  .stat-card {
    padding: 14px 12px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
    border-radius: 12px;
  }

  .stat-number {
    font-size: 18px;
  }

  .stat-label {
    font-size: 12px;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .action-item {
    min-height: 72px;
    padding: 12px 14px;
  }

  .novel-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px;
  }

  .novel-actions {
    width: 100%;
  }

  .continue-btn {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .goal-info {
    flex-direction: column;
    gap: 4px;
  }

  .progress-text {
    text-align: left;
  }

  .novel-item {
    flex-direction: column;
    align-items: stretch;
  }

  .novel-cover {
    width: 48px;
    height: 64px;
  }
}

/* Round 20 home */
@media (max-width: 1024px) {
  .home-page {
    padding-bottom: 12px;
  }
  .welcome-badge {
    font-size: 10px;
  }
}

/* Round 29 hero */
@media (max-width: 480px) {
  .welcome-actions {
    flex-direction: column;
  }
  .welcome-actions :deep(.el-button) {
    width: 100%;
  }
  .orb-a, .orb-b { opacity: 0.35; }
}

/* Round 43 */
.stat-number {
  font-variant-numeric: tabular-nums;
}
.novel-item:active {
  transform: scale(0.995);
}

/* Round 71 */
.streak-icon {
  filter: drop-shadow(0 0 8px rgba(245,158,11,0.45));
}
.goal-item .el-progress {
  margin-top: 2px;
}

/* Round 104 */
.action-item:focus-visible {
  outline: 2px solid rgba(124,58,237,0.45);
  outline-offset: 2px;
}
.stat-card:focus-within {
  border-color: rgba(124,58,237,0.28);
}

/* Round 132 */
:deep(.el-button--primary.is-plain) {
  --el-button-bg-color: rgba(124,58,237,0.08);
  --el-button-border-color: rgba(124,58,237,0.25);
  --el-button-text-color: #6d28d9;
  font-weight: 700;
}

/* Round 181 */
.default-cover {
  box-shadow: inset 0 0 0 1px rgba(124,58,237,0.12);
}
.welcome-card {
  animation: fade-up 0.55s cubic-bezier(0.22,1,0.36,1) both;
}

/* Round 193 */
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  font-weight: 500;
}

/* Round 202 */
.goals-card :deep(.el-progress__text){font-weight:800;}

/* Round 231 */
.welcome-badge {
  backdrop-filter: blur(8px);
}
.hero-cta:active {
  transform: scale(0.98);
}

/* Round 252 */
.panel-card{overflow:hidden;} .panel-card :deep(.el-card__header){position:relative;}

/* Round 313 */
:deep(.el-tag) { font-weight: 700; }

/* Round 332 */
:deep(.el-button--small){border-radius:10px;font-weight:700;}

/* Round 381 */
.empty-novels :deep(.el-empty__image) {
  opacity: 0.9;
}
.continue-btn:hover {
  transform: translateY(-1px);
}
.meta-chip {
  transition: background 0.2s ease;
}
.novel-item:hover .meta-chip {
  background: rgba(124,58,237,0.16);
}

/* Round 403 */
:deep(.el-loading-mask) {
  border-radius: inherit;
}

/* Round 451 */
.novel-item:focus-visible,
.action-item:focus-visible {
  outline: 2px solid rgba(124,58,237,0.5);
  outline-offset: 2px;
}
.stat-card {
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute;
  right: -20px;
  top: -20px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(124,58,237,0.06);
  pointer-events: none;
}

/* Round 488 */
:deep(.el-popper) {
  max-width: min(92vw, 360px);
}

/* Round 509 */
.goal-progress :deep(.el-progress-bar__outer) {
  background: rgba(124,58,237,0.1) !important;
}

/* Round 525 */
.welcome-actions :deep(.el-button) {
  font-weight: 800;
}

/* Round 544 */
.action-desc{opacity:.9;}
.action-item:active{transform:scale(.99);}

/* Round 568 */
:deep(.el-alert__title) { font-weight: 800; }

/* Round 602 */
:deep(.el-tooltip__trigger:focus-visible){outline:2px solid rgba(124,58,237,.4);outline-offset:2px;}

/* Round 637 */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
}
/* Round 671 */
.streak-info{font-variant-numeric:tabular-nums;}

/* Round 702 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 741 */
@media (prefers-reduced-motion: reduce) {
  .welcome-card,
  .stat-card,
  .anim-fade-up {
    animation: none !important;
  }
  .welcome-orb {
    display: none;
  }
}

/* Round 763 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 799 */
.novel-cover {
  background: linear-gradient(145deg, #ede9fe, #ddd6fe);
}
.novel-item:hover .default-cover {
  background: linear-gradient(145deg, #ddd6fe, #c4b5fd);
  color: #5b21b6;
}
/* Round 800 */
.welcome-text h1 {
  text-wrap: balance;
}

/* Round 839 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 897 */
:deep(.el-message-box__message) {
  line-height: 1.55;
  color: #334155;
  font-weight: 600;
}

/* Round 959 */
.welcome-text h1 {
  max-width: 16ch;
}
@media (max-width: 768px) {
  .welcome-text h1 { max-width: none; }
}
/* Round 960 */
.stat-card::after {
  transition: transform 0.4s ease;
}
.stat-card:hover::after {
  transform: scale(1.2);
}

/* Round 983 */
.home-page {
  min-width: 0;
}


/* Round 1021 */
.goal-item .goal-label {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
/* Round 1022 */
.action-grid {
  align-content: stretch;
}
/* Round 1023 */
.recent-novels-card :deep(.el-card__body) {
  padding-top: 12px;
}
/* Round 1024 */
@media (max-width: 480px) {
  .stat-number {
    font-size: 17px;
  }
  .welcome-badge {
    margin-bottom: 10px;
  }
}
/* Round 1025 */
.panel-card :deep(.el-card__header) {
  padding: 14px 16px;
}

/* Round 1116 */
.welcome-card {
  isolation: isolate;
}
/* Round 1117 */
.stat-content {
  min-width: 0;
}
/* Round 1118 */
.novel-title {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
/* Round 1119 */
.no-goals :deep(.el-empty) {
  padding: 12px 0;
}
/* Round 1120 */
.view-all-goals {
  margin-top: 4px;
}
/* Round 1121 */
.hero-cta :deep(span) {
  font-weight: 800;
}
/* Round 1122 */
@media (max-width: 768px) {
  .main-content .el-col {
    margin-bottom: 12px;
  }
}
/* Round 1123 */
.action-copy {
  min-width: 0;
}
/* Round 1124 */
.meta-chip {
  max-width: 100%;
}
/* Round 1125 */
.streak-info span {
  min-width: 0;
}

/* Round 1191 */
.welcome-orb {
  will-change: transform;
}
/* Round 1192 */
.stat-icon {
  position: relative;
  z-index: 1;
}
/* Round 1193 */
.goal-progress {
  width: 100%;
}
/* Round 1194 */
.continue-btn {
  white-space: nowrap;
}
/* Round 1195 */
.empty-novels :deep(.el-button) {
  min-width: 120px;
}


/* Round 1219 */
.home-page {
  -webkit-font-smoothing: antialiased;
}

</style>
