<template>
  <div class="editor-panel">
    <el-card shadow="never" v-if="currentChapter">
      <template #header>
        <div class="editor-header">
          <div class="editor-header-left">
            <h3 class="chapter-title">✍️ {{ currentChapter.title }}</h3>
            <div class="chapter-meta">
              <span class="word-count">{{ contentWordCount }}字</span>
              <el-select 
                v-if="currentChapter.status" 
                v-model="currentChapter.status" 
                size="small" 
                style="width: 80px;"
                @change="$emit('update-status', currentChapter.status)"
                popper-class="chapter-status-dropdown"
              >
                <el-option label="草稿" value="draft" />
                <el-option label="完成" value="completed" />
                <el-option label="发表" value="published" />
              </el-select>
              <span v-if="isSaving" class="saving-indicator">● 保存中...</span>
            </div>
          </div>
          <div class="editor-header-right">
            <el-button-group>
              <el-button size="small" @click="$emit('generate-from-outline')" :disabled="!currentChapter.description">
                <el-icon><Star /></el-icon>
                根据大纲生成
              </el-button>
              <el-button size="small" @click="$emit('open-continue-dialog')">
                <el-icon><ArrowRight /></el-icon>
                续写
              </el-button>
              <el-button size="small" @click="$emit('enhance-content')">
                <el-icon><Tools /></el-icon>
                优化
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <div class="editor-container">
        <div class="editor-wrapper">
          <Toolbar
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
            style="border-bottom: 1px solid rgba(124, 58, 237, 0.1);"
          />
          <Editor
            :model-value="content"
            :defaultConfig="editorConfig"
            mode="default"
            @onCreated="handleCreated"
            @onChange="handleChange"
            style="overflow-y: hidden;"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 未选择章节状态 -->
    <el-card shadow="never" v-else>
      <div class="empty-editor">
        <el-icon class="empty-icon"><Document /></el-icon>
        <p>请选择或创建一个章节开始编辑</p>
        <el-button type="primary" @click="$emit('add-new-chapter')">创建第一章</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, shallowRef, onMounted } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { Star, ArrowRight, Tools, Document } from '@element-plus/icons-vue'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps({
  currentChapter: {
    type: Object,
    default: null
  },
  content: {
    type: String,
    default: ''
  },
  isSaving: {
    type: Boolean,
    default: false
  },
  toolbarConfig: {
    type: Object,
    default: () => ({})
  },
  editorConfig: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits([
  'update:content',
  'update-status',
  'generate-from-outline',
  'open-continue-dialog',
  'enhance-content',
  'add-new-chapter',
  'editor-created',
  'content-change'
])

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

// 计算字数
const contentWordCount = computed(() => {
  if (!props.content) return 0
  // 去除HTML标签并计算字数
  const textContent = props.content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')
  return textContent.length
})

const handleCreated = (editor) => {
  editorRef.value = editor
  emit('editor-created', editor)
}

const handleChange = (editor) => {
  const html = editor.getHtml()
  emit('update:content', html)
  emit('content-change', html, editor)
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
</script>

<style scoped>
.editor-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 0;
}

.editor-panel .el-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 16px !important;
  border: 1px solid rgba(124, 58, 237, 0.1) !important;
  background: rgba(255, 255, 255, 0.92) !important;
  box-shadow: 0 10px 30px rgba(79, 70, 229, 0.08) !important;
  overflow: hidden;
}

.editor-panel :deep(.el-card__header) {
  background: linear-gradient(180deg, rgba(255,255,255,0.95), rgba(248,246,255,0.7));
  border-bottom: 1px solid rgba(124, 58, 237, 0.08);
}

.editor-panel :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.editor-header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.chapter-title {
  font-size: 17px;
  font-weight: 800;
  color: #1e1b4b;
  margin: 0;
  letter-spacing: -0.01em;
}

.chapter-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #64748b;
  flex-wrap: wrap;
}

.word-count {
  font-weight: 700;
  color: #6d28d9;
  background: rgba(124, 58, 237, 0.1);
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.saving-indicator {
  color: #7c3aed;
  font-size: 12px;
  font-weight: 700;
}

.editor-header-right :deep(.el-button) {
  border-radius: 10px;
  font-weight: 700;
}

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: none;
  border-radius: 0;
  overflow: hidden;
}

.editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 360px;
}

.editor-wrapper :deep(.w-e-toolbar) {
  border-bottom: 1px solid rgba(124, 58, 237, 0.1) !important;
  background: linear-gradient(180deg, #faf8ff, #f5f3ff) !important;
}

.editor-wrapper :deep(.w-e-text-container) {
  flex: 1;
  background: #fcfbff !important;
}

.editor-wrapper :deep(.w-e-text-placeholder) {
  top: 20px;
  left: 20px;
  color: #94a3b8 !important;
}

.empty-editor {
  text-align: center;
  padding: 80px 20px;
  color: #64748b;
}

.empty-icon {
  font-size: 56px;
  color: #c4b5fd;
  margin-bottom: 16px;
}

.empty-editor p {
  font-size: 15px;
  margin-bottom: 20px;
  font-weight: 600;
}

/* Round 33 */
.empty-editor {
  background: linear-gradient(160deg, #ffffff, #f5f3ff);
  margin: 16px;
  border-radius: 16px;
  border: 1px dashed rgba(124,58,237,0.22);
}

/* Round 140 */
:deep(.el-button--primary.is-plain) {
  --el-button-bg-color: rgba(124,58,237,0.08);
  --el-button-border-color: rgba(124,58,237,0.25);
  --el-button-text-color: #6d28d9;
  font-weight: 700;
}

/* Round 174 */
.card-header span { letter-spacing: 0.01em; }

/* Round 319 */
:deep(.el-tag) { font-weight: 700; }

/* Round 417 */
:deep(.el-loading-mask) {
  border-radius: inherit;
}

/* Round 446 */
:deep(.el-button--primary) {
  letter-spacing: 0.01em;
}

/* Round 571 */
:deep(.el-alert__title) { font-weight: 800; }

/* Round 616 */
:deep(.el-tooltip__trigger:focus-visible){outline:2px solid rgba(124,58,237,.4);outline-offset:2px;}

/* Round 650 */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
}

/* Round 716 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 766 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 814 */
.editor-panel :deep(.w-e-scroll) {
  -webkit-overflow-scrolling: touch;
}

/* Round 852 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 910 */
:deep(.el-message-box__message) {
  line-height: 1.55;
  color: #334155;
  font-weight: 600;
}

/* Round 997 */
:deep(.el-loading-text){font-weight:800;}

/* Round 1051 */
.editor-wrapper :deep(.w-e-bar-divider) {
  background-color: rgba(124,58,237,0.12) !important;
}
</style> 