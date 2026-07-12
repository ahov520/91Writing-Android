<template>
  <div class="panel-content">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>🌍 世界观设定</span>
          <div class="world-actions">
            <el-button size="small" type="primary" @click="$emit('add-worldview')">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-button size="small" type="success" @click="$emit('generate-worldview')">
              🤖 AI生成
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="worldview-list">
        <div v-for="setting in worldSettings" :key="setting.id" class="worldview-item">
          <div class="worldview-content" @click="$emit('edit-worldview', setting)">
            <div class="worldview-header">
              <h4>{{ setting.title }}</h4>
              <el-tag :type="getWorldSettingTagType(setting.category)">{{ getWorldSettingTagText(setting.category) }}</el-tag>
            </div>
            <el-tooltip 
              v-if="setting.description" 
              :content="setting.description" 
              placement="right"
              :disabled="setting.description.length <= 80"
              effect="light"
              :show-after="300"
            >
              <p class="worldview-description worldview-description-truncated">
                {{ setting.description.length > 80 ? setting.description.substring(0, 80) + '...' : setting.description }}
              </p>
            </el-tooltip>
            <p v-else class="worldview-description">暂无描述</p>
            <div class="worldview-meta">
              <span class="create-time">{{ formatDate(setting.createdAt) }}</span>
              <span v-if="setting.generated" class="ai-generated">AI生成</span>
            </div>
          </div>
          <div class="worldview-actions">
            <el-dropdown @command="(cmd) => $emit('worldview-action', cmd, setting)" trigger="click">
              <el-button size="small" type="text" @click.stop>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="duplicate">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <div v-if="worldSettings.length === 0" class="empty-state">
          <p>暂无世界观设定</p>
          <el-button size="small" @click="$emit('add-worldview')">创建第一个设定</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Plus, MoreFilled, Edit, Delete, CopyDocument } from '@element-plus/icons-vue'

defineProps({
  worldSettings: {
    type: Array,
    required: true
  }
})

defineEmits([
  'add-worldview',
  'generate-worldview',
  'edit-worldview',
  'worldview-action'
])

const getWorldSettingTagType = (category) => {
  const tagTypes = {
    'setting': 'primary',
    'magic': 'success',
    'politics': 'warning',
    'geography': 'info',
    'history': 'danger'
  }
  return tagTypes[category] || ''
}

const getWorldSettingTagText = (category) => {
  const tagTexts = {
    'setting': '世界设定',
    'magic': '魔法体系',
    'politics': '政治势力',
    'geography': '地理环境',
    'history': '历史背景'
  }
  return tagTexts[category] || category
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString()
}
</script>

<style scoped>
.panel-content {
  height: 100%;
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.world-actions {
  display: flex;
  gap: 8px;
}

.worldview-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.worldview-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid rgba(124, 58, 237, 0.12);
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.worldview-item:hover {
  border-color: #7c3aed;
  background-color: #faf5ff;
}

.worldview-content {
  flex: 1;
  cursor: pointer;
}

.worldview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.worldview-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0;
  flex: 1;
  margin-right: 8px;
}

.worldview-description {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  margin-bottom: 8px;
}

.worldview-description-truncated {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.worldview-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.ai-generated {
  color: #67c23a;
  font-weight: 500;
}

.worldview-actions {
  margin-left: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.empty-state p {
  margin-bottom: 16px;
}

/* Round 12 panel polish */
:deep(.el-card) {
  border-radius: 16px !important;
  border: 1px solid rgba(124,58,237,0.1) !important;
  background: rgba(255,255,255,0.92) !important;
  box-shadow: 0 8px 24px rgba(79,70,229,0.06) !important;
}
.card-header { font-weight: 800 !important; color: #1e1b4b !important; }
.empty-state {
  text-align: center;
  padding: 36px 16px !important;
  color: #64748b !important;
  border: 1px dashed rgba(124,58,237,0.22);
  border-radius: 14px;
  background: linear-gradient(160deg,#fff,#f5f3ff);
}

/* Round 60 */
.worldview-item {
  border-radius: 12px !important;
  border: 1px solid rgba(124,58,237,0.12) !important;
  transition: all 0.28s cubic-bezier(0.22,1,0.36,1);
  background: linear-gradient(145deg,#fff,#faf8ff);
}
.worldview-item:hover {
  border-color: rgba(124,58,237,0.35) !important;
  box-shadow: 0 8px 18px rgba(124,58,237,0.1);
}

/* Round 178 */
.card-header span { letter-spacing: 0.01em; }

/* Round 316 */
:deep(.el-tag) { font-weight: 700; }

/* Round 450 */
:deep(.el-button--primary) {
  letter-spacing: 0.01em;
}

/* Round 653 */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
}

/* Round 729 */
:deep(.el-button.is-text) {
  font-weight: 700;
}

/* Round 779 */
:deep(.el-dialog__headerbtn:focus-visible) {
  outline: 2px solid rgba(124,58,237,0.45);
  border-radius: 8px;
}

/* Round 865 */
:deep(.el-popconfirm__main) {
  line-height: 1.5;
}

/* Round 1141 */
:deep(.el-input__wrapper) {
  transition: box-shadow 0.2s ease;
}
</style> 