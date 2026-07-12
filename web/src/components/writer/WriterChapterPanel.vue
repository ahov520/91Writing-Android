<template>
  <div class="panel-content">
    <el-card shadow="never" class="chapters-card">
      <template #header>
        <div class="card-header">
          <span>📝 章节列表</span>
          <el-dropdown @command="handleChapterCommand">
            <el-button size="small" type="primary">
              <el-icon><Plus /></el-icon>
              新增章节 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="manual">手动创建</el-dropdown-item>
                <el-dropdown-item command="ai-single">AI生成单章</el-dropdown-item>
                <el-dropdown-item command="ai-batch">AI批量生成</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
      
      <div class="chapters-list">
        <div 
          v-for="(chapter, index) in chapters" 
          :key="chapter.id"
          class="chapter-item"
          :class="{ active: currentChapter?.id === chapter.id }"
          @click="$emit('select-chapter', chapter)"
        >
          <div class="chapter-info">
            <h4>第{{ index + 1 }}章</h4>
            <p>{{ chapter.title }}</p>
            <div class="chapter-meta">
              <span>{{ chapter.wordCount || 0 }}字</span>
              <el-tag v-if="chapter.status" :type="getChapterStatusType(chapter.status)" size="small">
                {{ getChapterStatusText(chapter.status) }}
              </el-tag>
            </div>
            <el-tooltip 
              v-if="chapter.description" 
              :content="chapter.description" 
              placement="top-start"
              :disabled="chapter.description.length <= 50"
              effect="light"
              :show-after="300"
            >
              <p class="chapter-desc chapter-desc-truncated">
                {{ chapter.description.length > 50 ? chapter.description.substring(0, 50) + '...' : chapter.description }}
              </p>
            </el-tooltip>
          </div>
          <div class="chapter-actions">
            <el-dropdown @command="(cmd) => $emit('chapter-action', cmd, chapter)">
              <el-button size="small" type="text">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑信息</el-dropdown-item>
                  <el-dropdown-item command="generate">AI生成正文</el-dropdown-item>
                  <el-dropdown-item divided command="delete">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <div v-if="chapters.length === 0" class="empty-chapters">
          <p>暂无章节</p>
          <el-button size="small" type="primary" @click="$emit('add-chapter')">
            创建第一章
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Plus, ArrowDown, MoreFilled } from '@element-plus/icons-vue'

defineProps({
  chapters: {
    type: Array,
    required: true
  },
  currentChapter: {
    type: Object,
    default: null
  }
})

defineEmits([
  'select-chapter',
  'chapter-action', 
  'add-chapter',
  'chapter-command'
])

const handleChapterCommand = (command) => {
  emit('chapter-command', command)
}

const getChapterStatusType = (status) => {
  const statusTypes = {
    'draft': '',
    'completed': 'success',
    'published': 'primary'
  }
  return statusTypes[status] || ''
}

const getChapterStatusText = (status) => {
  const statusTexts = {
    'draft': '草稿',
    'completed': '完成',
    'published': '发表'
  }
  return statusTexts[status] || status
}
</script>

<style scoped>
.panel-content { height: 100%; }
.chapters-card { height: 100%; display: flex; flex-direction: column; border-radius: 16px !important; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 800; color: #1e1b4b; }
.chapters-list { flex: 1; overflow-y: auto; max-height: calc(100vh - 220px); padding-right: 2px; }
.chapter-item {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 12px 14px; border: 1px solid rgba(124,58,237,0.12); border-radius: 12px;
  margin-bottom: 8px; cursor: pointer; transition: all 0.28s cubic-bezier(0.22,1,0.36,1);
  background: linear-gradient(145deg,#fff,#faf8ff);
}
.chapter-item:hover { border-color: rgba(124,58,237,0.35); transform: translateY(-1px); box-shadow: 0 8px 18px rgba(124,58,237,0.1); }
.chapter-item.active {
  border-color: rgba(124,58,237,0.45);
  background: linear-gradient(135deg, rgba(124,58,237,0.12), rgba(236,72,153,0.08));
  box-shadow: 0 8px 20px rgba(124,58,237,0.12);
}
.chapter-info { flex: 1; min-width: 0; }
.chapter-info h4 { font-size: 12px; color: #7c3aed; margin: 0 0 4px; font-weight: 800; letter-spacing: 0.04em; }
.chapter-info p { font-size: 14px; font-weight: 700; color: #1e1b4b; margin: 0 0 8px; }
.chapter-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #64748b; font-weight: 600; }
.chapter-desc { font-size: 12px; color: #64748b; margin: 4px 0 0; line-height: 1.4; }
.chapter-desc-truncated { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chapter-actions { margin-left: 8px; }
.empty-chapters {
  text-align: center; padding: 40px 16px; color: #64748b;
  border: 1px dashed rgba(124,58,237,0.22); border-radius: 14px;
  background: linear-gradient(160deg,#fff,#f5f3ff);
}
.empty-chapters p { margin-bottom: 14px; font-weight: 600; }

/* Round 171 */
.card-header span { letter-spacing: 0.01em; }

/* Round 314 */
:deep(.el-tag) { font-weight: 700; }

/* Round 418 */
:deep(.el-loading-mask) {
  border-radius: inherit;
}

/* Round 443 */
:deep(.el-button--primary) {
  letter-spacing: 0.01em;
}

/* Round 572 */
:deep(.el-alert__title) { font-weight: 800; }

/* Round 651 */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
}

/* Round 717 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 767 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 853 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 999 */
:deep(.el-loading-text){font-weight:800;}

/* Round 1049 */
.chapter-item.active h4 {
  color: #6d28d9;
}
</style> 