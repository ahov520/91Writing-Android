<template>
  <el-dialog
    v-model="dialogVisible"
    :title="announcement.title"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    top="5vh"
    class="announcement-dialog"
  >
    <div class="announcement-content">
      <div class="announcement-meta">
        <el-tag type="info" size="small">{{ announcement.date }}</el-tag>
        <el-tag type="success" size="small">v{{ announcement.version }}</el-tag>
      </div>
      
      <div class="announcement-body" v-html="renderedContent"></div>
      
      <div class="announcement-footer">
        <div class="footer-buttons">
          <el-button type="primary" @click="closeDialog">
            我知道了
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  announcement: {
    type: Object,
    default: () => ({
      version: '1.0.0',
      title: '系统公告',
      date: '2024-01-01',
      content: ''
    })
  }
})

const emit = defineEmits(['update:visible', 'close'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})



// 渲染markdown内容
const renderedContent = computed(() => {
  return marked(props.announcement.content || '')
})

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false
  emit('close')
}

onMounted(() => {
  // 配置marked选项
  marked.setOptions({
    breaks: true,
    gfm: true
  })
})
</script>

<style scoped>
.announcement-dialog {
  border-radius: 12px;
}

.announcement-content {
  padding: 0;
}

.announcement-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(124, 58, 237, 0.1);
}

.announcement-body {
  max-height: 60vh;
  overflow-y: auto;
  padding: 16px 0;
  line-height: 1.6;
}

.announcement-body :deep(h1) {
  font-size: 24px;
  color: #1e1b4b;
  margin: 20px 0 16px 0;
  border-bottom: 2px solid #7c3aed;
  padding-bottom: 8px;
}

.announcement-body :deep(h2) {
  font-size: 20px;
  color: #7c3aed;
  margin: 16px 0 12px 0;
}

.announcement-body :deep(h3) {
  font-size: 18px;
  color: #606266;
  margin: 14px 0 10px 0;
}

.announcement-body :deep(p) {
  margin: 8px 0;
  color: #606266;
}

.announcement-body :deep(ul),
.announcement-body :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.announcement-body :deep(li) {
  margin: 4px 0;
  color: #606266;
}

.announcement-body :deep(code) {
  background: transparent;
  padding: 2px 6px;
  border-radius: 10px;
  font-family: 'Courier New', monospace;
  color: #e6a23c;
}

.announcement-body :deep(pre) {
  background: transparent;
  padding: 12px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 12px 0;
}

.announcement-body :deep(blockquote) {
  border-left: 4px solid #7c3aed;
  background: #f5f3ff;
  padding: 12px 16px;
  margin: 12px 0;
  color: #606266;
}

.announcement-body :deep(a) {
  color: #7c3aed;
  text-decoration: none;
}

.announcement-body :deep(a:hover) {
  text-decoration: underline;
}

.announcement-body :deep(strong) {
  color: #1e1b4b;
  font-weight: 600;
}

.announcement-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.announcement-body :deep(th),
.announcement-body :deep(td) {
  border: 1px solid rgba(124, 58, 237, 0.1);
  padding: 8px 12px;
  text-align: left;
}

.announcement-body :deep(th) {
  background: transparent;
  font-weight: 600;
}

.announcement-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(124, 58, 237, 0.1);
}

.footer-buttons {
  display: flex;
  gap: 12px;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #4c1d95 0%, #7c3aed 55%, #db2777 100%);
  color: white;
  border-radius: 16px 16px 0 0;
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

/* Round 9 announcement */
.announcement-dialog :deep(.el-dialog) {
  border-radius: 20px !important;
  overflow: hidden;
  border: 1px solid rgba(124,58,237,0.12);
  box-shadow: 0 24px 60px rgba(76,29,149,0.22) !important;
}
.announcement-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #4c1d95, #7c3aed 55%, #db2777);
  margin-right: 0 !important;
  padding: 18px 22px !important;
}
.announcement-dialog :deep(.el-dialog__title) {
  color: #fff !important;
  font-weight: 800 !important;
}
.announcement-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.announcement-body {
  line-height: 1.7;
  color: #334155;
}
.announcement-body :deep(h1),
.announcement-body :deep(h2),
.announcement-body :deep(h3) {
  color: #1e1b4b;
  font-weight: 800;
}
.announcement-body :deep(a) {
  color: #7c3aed;
  font-weight: 700;
}
.footer-buttons .el-button--primary {
  border-radius: 12px !important;
  font-weight: 800;
  min-width: 120px;
}

/* Round 34 */
.announcement-body {
  padding: 8px 2px 4px;
}
.announcement-meta {
  border-bottom-color: rgba(124,58,237,0.1) !important;
}

/* Round 102 bulk radius/surface */

/* Round 139 */
:deep(.el-button--primary.is-plain) {
  --el-button-bg-color: rgba(124,58,237,0.08);
  --el-button-border-color: rgba(124,58,237,0.25);
  --el-button-text-color: #6d28d9;
  font-weight: 700;
}

/* Round 162 */
:deep(.el-button) { font-weight: 700; }

/* Round 271 */
:deep(.el-card){border-radius:14px!important;}

/* Round 346 */
:deep(.el-button--small){border-radius:10px;font-weight:700;}

/* Round 416 */
:deep(.el-loading-mask) {
  border-radius: inherit;
}

/* Round 433 */
:deep(.el-button--primary) {
  letter-spacing: 0.01em;
}

/* Round 520 */
:deep(.el-empty) {
  padding: 24px 8px;
}

/* Round 576 */
:deep(.el-alert__title) { font-weight: 800; }

/* Round 617 */
:deep(.el-tooltip__trigger:focus-visible){outline:2px solid rgba(124,58,237,.4);outline-offset:2px;}

/* Round 656 */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
}

/* Round 718 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 768 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 813 */
.announcement-body :deep(img) {
  max-width: 100%;
  border-radius: 12px;
}

/* Round 854 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 911 */
:deep(.el-message-box__message) {
  line-height: 1.55;
  color: #334155;
  font-weight: 600;
}

/* Round 998 */
:deep(.el-loading-text){font-weight:800;}

/* Round 1052 */
@media (max-width: 768px) {
  .announcement-body {
    max-height: 55vh;
  }
}
</style> 