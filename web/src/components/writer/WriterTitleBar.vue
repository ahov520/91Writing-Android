<template>
  <div class="title-bar">
    <div class="title-left">
      <el-button @click="$emit('go-back')" size="small" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <span class="novel-title">{{ novelTitle || '小说编辑' }}</span>
    </div>
  </div>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'

defineProps({
  novelTitle: {
    type: String,
    default: ''
  }
})

defineEmits(['go-back'])
</script>

<style scoped>
.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 22px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px) saturate(1.3);
  -webkit-backdrop-filter: blur(16px) saturate(1.3);
  border-bottom: 1px solid rgba(124, 58, 237, 0.12);
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.06);
}

.title-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.back-btn {
  border-radius: 10px !important;
  font-weight: 700;
  border-color: rgba(124, 58, 237, 0.2) !important;
  color: #5b21b6 !important;
}

.novel-title {
  font-size: 17px;
  font-weight: 800;
  color: #1e1b4b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Round 177 */
.card-header span { letter-spacing: 0.01em; }

/* Round 370 */
:deep(.el-empty__description p){font-weight:600;}

/* Round 449 */
:deep(.el-button--primary) {
  letter-spacing: 0.01em;
}

/* Round 733 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 783 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 869 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 1145 */
:deep(.el-input__wrapper) {
  transition: box-shadow 0.2s ease;
}
</style>
