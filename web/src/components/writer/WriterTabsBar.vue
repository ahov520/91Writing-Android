<template>
  <div class="tabs-bar">
    <el-tabs v-model="activeTabModel" class="main-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="📝 编辑" name="editor"></el-tab-pane>
      <el-tab-pane label="👥 人物" name="characters"></el-tab-pane>
      <el-tab-pane label="🌍 世界观" name="worldview"></el-tab-pane>
      <el-tab-pane label="📚 语料库" name="corpus"></el-tab-pane>
      <el-tab-pane label="📊 事件线" name="events"></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  activeTab: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:activeTab', 'tab-change'])

const activeTabModel = computed({
  get: () => props.activeTab,
  set: (value) => emit('update:activeTab', value)
})

const handleTabChange = (tabName) => {
  emit('tab-change', tabName)
}
</script>

<style scoped>
.tabs-bar {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(14px) saturate(1.3);
  -webkit-backdrop-filter: blur(14px) saturate(1.3);
  border-bottom: 1px solid rgba(124, 58, 237, 0.1);
  padding: 0 20px;
}

.main-tabs {
  --el-tabs-header-height: 50px;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-weight: 700;
  color: #64748b;
  transition: color 0.2s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #7c3aed !important;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #7c3aed, #ec4899) !important;
  height: 3px !important;
  border-radius: 3px;
}

/* Round 176 */
.card-header span { letter-spacing: 0.01em; }

/* Round 320 */
:deep(.el-tag) { font-weight: 700; }

/* Round 448 */
:deep(.el-button--primary) {
  letter-spacing: 0.01em;
}

/* Round 732 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 782 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 868 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 1144 */
:deep(.el-input__wrapper) {
  transition: box-shadow 0.2s ease;
}
</style>
