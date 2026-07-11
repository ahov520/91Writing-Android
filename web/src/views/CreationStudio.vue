<template>
  <div class="creation-studio" :class="{ 'focus-mode': focusMode }">
    <el-card shadow="never" class="studio-header-card">
      <div class="studio-header">
        <div>
          <h2>创作中台（Phase A/B/C）</h2>
          <p>覆盖专注写作、章节看板、版本管理、一致性检查、角色关系、质量评分、云同步与发布导出。</p>
        </div>
        <div class="studio-header-actions">
          <el-select v-model="selectedNovelId" placeholder="选择小说" style="min-width: 220px" @change="handleNovelChange">
            <el-option v-for="novel in novels" :key="novel.id" :label="novel.title || '未命名小说'" :value="novel.id" />
          </el-select>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="studio-tabs">
      <el-tab-pane label="Phase A - 专注写作" name="focus">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="16">
            <el-card>
              <template #header>
                <div class="card-title-row">
                  <span>专注写作模式</span>
                  <el-switch v-model="focusMode" active-text="专注模式" />
                </div>
              </template>
              <el-input
                v-model="focusDraft"
                type="textarea"
                :rows="focusMode ? 22 : 14"
                placeholder="在这里专注写作，支持自动保存草稿..."
              />
              <div class="focus-toolbar">
                <el-button type="primary" @click="saveFocusDraft">保存草稿</el-button>
                <el-button @click="createSnapshot">创建版本快照</el-button>
                <el-button @click="insertTimestamp">插入时间锚点</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="8">
            <el-card>
              <template #header>
                <span>写作状态</span>
              </template>
              <div class="metric-row"><span>当前字数</span><b>{{ focusDraft.length }}</b></div>
              <div class="metric-row"><span>预估阅读时长</span><b>{{ Math.max(1, Math.ceil(focusDraft.length / 500)) }} 分钟</b></div>
              <div class="metric-row"><span>最近保存</span><b>{{ lastSavedAt || '未保存' }}</b></div>
              <el-divider />
              <h4>章节看板（拖拽排序）</h4>
              <p class="hint-text">上下拖拽后点击「保存排序」。</p>
              <div class="kanban-list">
                <div
                  v-for="(chapter, idx) in chapterBoard"
                  :key="chapter.id"
                  class="kanban-item"
                  draggable="true"
                  @dragstart="onDragStart(idx)"
                  @dragover.prevent
                  @drop="onDrop(idx)"
                >
                  <span>{{ idx + 1 }}. {{ chapter.title || '未命名章节' }}</span>
                  <el-tag size="small">{{ chapter.status || 'draft' }}</el-tag>
                </div>
              </div>
              <el-button size="small" type="success" @click="saveChapterOrder" :disabled="!selectedNovel">保存排序</el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="Phase A - 版本快照" name="snapshot">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="8">
            <el-card>
              <template #header>
                <div class="card-title-row">
                  <span>版本列表（{{ snapshots.length }}）</span>
                  <el-button text type="danger" @click="clearSnapshots" :disabled="!snapshots.length">清空</el-button>
                </div>
              </template>
              <el-empty v-if="!snapshots.length" description="暂无快照" />
              <div v-else class="snapshot-list">
                <div
                  v-for="item in snapshots"
                  :key="item.id"
                  class="snapshot-item"
                  :class="{ active: selectedSnapshotIds.includes(item.id) }"
                  @click="toggleSnapshotSelection(item.id)"
                >
                  <div>{{ item.createdAt }}</div>
                  <small>{{ item.content.length }} 字</small>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="16">
            <el-card>
              <template #header>
                <span>版本对比（选择两个版本）</span>
              </template>
              <el-alert v-if="selectedSnapshots.length !== 2" type="info" :closable="false" title="请在左侧选择两个快照进行对比" />
              <template v-else>
                <el-row :gutter="12">
                  <el-col :span="12">
                    <h4>{{ selectedSnapshots[0].createdAt }}</h4>
                    <el-input :model-value="selectedSnapshots[0].content" type="textarea" :rows="16" readonly />
                  </el-col>
                  <el-col :span="12">
                    <h4>{{ selectedSnapshots[1].createdAt }}</h4>
                    <el-input :model-value="selectedSnapshots[1].content" type="textarea" :rows="16" readonly />
                  </el-col>
                </el-row>
                <el-divider />
                <p>差异摘要：{{ getSimpleDiffSummary(selectedSnapshots[0].content, selectedSnapshots[1].content) }}</p>
                <el-button type="warning" @click="restoreSnapshot(selectedSnapshots[1])">恢复到右侧版本</el-button>
              </template>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="Phase B - 一致性/角色/评分" name="quality">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="12">
            <el-card>
              <template #header><span>情节一致性检查</span></template>
              <el-button type="primary" @click="runConsistencyCheck" :disabled="!selectedNovel">运行检查</el-button>
              <el-empty v-if="!consistencyIssues.length" description="暂无冲突（或尚未检查）" />
              <el-timeline v-else>
                <el-timeline-item
                  v-for="issue in consistencyIssues"
                  :key="issue.id"
                  :type="issue.level === 'high' ? 'danger' : 'warning'"
                  :timestamp="issue.chapter"
                >
                  {{ issue.message }}
                </el-timeline-item>
              </el-timeline>
            </el-card>

            <el-card class="mt-12">
              <template #header><span>章节质量评分</span></template>
              <el-button type="success" @click="runQualityScore" :disabled="!selectedNovel">计算评分</el-button>
              <el-table v-if="qualityRows.length" :data="qualityRows" size="small" style="margin-top: 12px">
                <el-table-column prop="chapter" label="章节" min-width="140" />
                <el-table-column prop="readability" label="可读性" width="90" />
                <el-table-column prop="dialogue" label="对话" width="90" />
                <el-table-column prop="pace" label="节奏" width="90" />
                <el-table-column prop="total" label="总分" width="90" />
              </el-table>
            </el-card>
          </el-col>

          <el-col :xs="24" :lg="12">
            <el-card>
              <template #header>
                <div class="card-title-row">
                  <span>角色卡与关系图</span>
                  <el-button text type="primary" @click="addCharacter">新增角色</el-button>
                </div>
              </template>
              <div class="character-list">
                <div v-for="(item, index) in characters" :key="item.id" class="character-item">
                  <el-input v-model="item.name" placeholder="角色名" />
                  <el-input v-model="item.goal" placeholder="角色目标" />
                  <el-input v-model="item.secret" placeholder="角色秘密" />
                  <el-button text type="danger" @click="removeCharacter(index)">删除</el-button>
                </div>
              </div>
              <el-divider />
              <h4>关系编辑</h4>
              <div class="relationship-row" v-for="(link, idx) in relationships" :key="link.id">
                <el-select v-model="link.from" placeholder="角色A">
                  <el-option v-for="c in characters" :key="c.id" :label="c.name || c.id" :value="c.id" />
                </el-select>
                <el-select v-model="link.type" placeholder="关系">
                  <el-option label="盟友" value="ally" />
                  <el-option label="敌对" value="enemy" />
                  <el-option label="亲属" value="family" />
                  <el-option label="师徒" value="mentor" />
                </el-select>
                <el-select v-model="link.to" placeholder="角色B">
                  <el-option v-for="c in characters" :key="c.id" :label="c.name || c.id" :value="c.id" />
                </el-select>
                <el-button text type="danger" @click="relationships.splice(idx, 1)">删除</el-button>
              </div>
              <el-button size="small" @click="addRelationship">新增关系</el-button>
              <el-button size="small" type="primary" @click="saveCharacterData">保存角色数据</el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="Phase C - 云同步/发布" name="cloud">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="12">
            <el-card>
              <template #header><span>云同步（MVP）</span></template>
              <el-form label-width="110px">
                <el-form-item label="同步端点">
                  <el-input v-model="syncConfig.endpoint" placeholder="https://your-sync-service/api/backup" />
                </el-form-item>
                <el-form-item label="访问令牌">
                  <el-input v-model="syncConfig.token" placeholder="可选" show-password />
                </el-form-item>
              </el-form>
              <div class="sync-actions">
                <el-button @click="saveSyncConfig">保存配置</el-button>
                <el-button type="primary" @click="exportAllData">导出本地备份</el-button>
                <el-upload :show-file-list="false" :auto-upload="false" accept="application/json" :on-change="importBackup">
                  <el-button type="success">导入备份</el-button>
                </el-upload>
              </div>
              <p class="hint-text">当前实现为前端可用版：本地导入导出 + 同步端点配置，便于后续接入真实云服务。</p>
            </el-card>
          </el-col>

          <el-col :xs="24" :lg="12">
            <el-card>
              <template #header><span>发布导出助手</span></template>
              <el-form label-width="100px">
                <el-form-item label="导出模板">
                  <el-select v-model="publishTemplate">
                    <el-option label="通用平台" value="general" />
                    <el-option label="公众号" value="wechat" />
                    <el-option label="男频网文" value="male" />
                    <el-option label="女频网文" value="female" />
                  </el-select>
                </el-form-item>
              </el-form>
              <el-button type="primary" @click="exportPublishFile" :disabled="!selectedNovel">导出发布稿</el-button>
              <el-input
                v-model="publishPreview"
                type="textarea"
                :rows="14"
                readonly
                style="margin-top: 12px"
                placeholder="导出预览..."
              />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('focus')
const novels = ref([])
const selectedNovelId = ref('')
const selectedNovel = ref(null)
const chapterBoard = ref([])
const dragFromIndex = ref(null)

const focusMode = ref(false)
const focusDraft = ref('')
const lastSavedAt = ref('')

const snapshots = ref([])
const selectedSnapshotIds = ref([])

const consistencyIssues = ref([])
const qualityRows = ref([])

const characters = ref([])
const relationships = ref([])

const syncConfig = ref({ endpoint: '', token: '' })
const publishTemplate = ref('general')
const publishPreview = ref('')

const selectedSnapshots = computed(() => {
  return snapshots.value.filter(item => selectedSnapshotIds.value.includes(item.id))
})

const now = () => new Date().toLocaleString('zh-CN', { hour12: false })
const uid = (prefix) => `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`

const refreshData = () => {
  try {
    const allNovels = JSON.parse(localStorage.getItem('novels') || '[]')
    novels.value = Array.isArray(allNovels) ? allNovels : []

    const savedSync = JSON.parse(localStorage.getItem('creationStudioSyncConfig') || '{}')
    syncConfig.value = {
      endpoint: savedSync.endpoint || '',
      token: savedSync.token || ''
    }

    if (!selectedNovelId.value && novels.value.length) {
      selectedNovelId.value = novels.value[0].id
    }
    handleNovelChange(selectedNovelId.value)
  } catch (error) {
    console.error(error)
    ElMessage.error('读取本地数据失败')
  }
}

const handleNovelChange = (novelId) => {
  selectedNovel.value = novels.value.find(item => item.id === novelId) || null
  chapterBoard.value = (selectedNovel.value?.chapterList || []).map((chapter, idx) => ({
    id: chapter.id || uid('chapter'),
    title: chapter.title || `第${idx + 1}章`,
    status: chapter.status || 'draft',
    content: chapter.content || ''
  }))

  const key = `focusDraft_${novelId || 'none'}`
  focusDraft.value = localStorage.getItem(key) || chapterBoard.value[0]?.content || ''
  snapshots.value = JSON.parse(localStorage.getItem(`snapshots_${novelId || 'none'}`) || '[]')

  const characterData = JSON.parse(localStorage.getItem(`characters_${novelId || 'none'}`) || '{}')
  characters.value = characterData.characters || []
  relationships.value = characterData.relationships || []

  selectedSnapshotIds.value = []
  consistencyIssues.value = []
  qualityRows.value = []
  publishPreview.value = ''
}

const saveFocusDraft = () => {
  if (!selectedNovelId.value) {
    ElMessage.warning('请先选择小说')
    return
  }
  localStorage.setItem(`focusDraft_${selectedNovelId.value}`, focusDraft.value)
  lastSavedAt.value = now()
  ElMessage.success('专注草稿已保存')
}

const createSnapshot = () => {
  if (!selectedNovelId.value) return
  const newSnapshot = {
    id: uid('snap'),
    createdAt: now(),
    content: focusDraft.value
  }
  snapshots.value.unshift(newSnapshot)
  localStorage.setItem(`snapshots_${selectedNovelId.value}`, JSON.stringify(snapshots.value.slice(0, 40)))
  ElMessage.success('已创建版本快照')
}

const toggleSnapshotSelection = (id) => {
  const list = [...selectedSnapshotIds.value]
  const has = list.includes(id)
  if (has) {
    selectedSnapshotIds.value = list.filter(item => item !== id)
    return
  }
  if (list.length >= 2) {
    list.shift()
  }
  list.push(id)
  selectedSnapshotIds.value = list
}

const getSimpleDiffSummary = (a, b) => {
  const diff = Math.abs((a || '').length - (b || '').length)
  const sentenceA = (a || '').split(/[。！？!?\n]/).filter(Boolean).length
  const sentenceB = (b || '').split(/[。！？!?\n]/).filter(Boolean).length
  return `字数变化 ${diff}，句子数变化 ${Math.abs(sentenceA - sentenceB)}`
}

const restoreSnapshot = async (snapshot) => {
  try {
    await ElMessageBox.confirm('将覆盖当前专注草稿，是否继续？', '恢复确认', { type: 'warning' })
    focusDraft.value = snapshot.content || ''
    saveFocusDraft()
    ElMessage.success('已恢复版本')
  } catch {
    // cancel
  }
}

const clearSnapshots = async () => {
  try {
    await ElMessageBox.confirm('确认清空所有快照？', '危险操作', { type: 'warning' })
    snapshots.value = []
    selectedSnapshotIds.value = []
    if (selectedNovelId.value) {
      localStorage.setItem(`snapshots_${selectedNovelId.value}`, '[]')
    }
  } catch {
    // cancel
  }
}

const insertTimestamp = () => {
  focusDraft.value += `\n\n【时间锚点 ${now()}】\n`
}

const onDragStart = (index) => {
  dragFromIndex.value = index
}

const onDrop = (dropIndex) => {
  if (dragFromIndex.value === null || dragFromIndex.value === dropIndex) return
  const next = [...chapterBoard.value]
  const [moved] = next.splice(dragFromIndex.value, 1)
  next.splice(dropIndex, 0, moved)
  chapterBoard.value = next
  dragFromIndex.value = null
}

const saveChapterOrder = () => {
  if (!selectedNovel.value) return
  const allNovels = JSON.parse(localStorage.getItem('novels') || '[]')
  const idx = allNovels.findIndex(item => item.id === selectedNovel.value.id)
  if (idx === -1) return

  allNovels[idx].chapterList = chapterBoard.value.map((item, order) => ({
    ...item,
    order
  }))
  localStorage.setItem('novels', JSON.stringify(allNovels))
  ElMessage.success('章节排序已保存')
}

const runConsistencyCheck = () => {
  consistencyIssues.value = []
  const chapters = chapterBoard.value
  if (!chapters.length) return

  const titleSet = new Set()
  chapters.forEach((chapter, index) => {
    const title = chapter.title || `第${index + 1}章`
    if (titleSet.has(title)) {
      consistencyIssues.value.push({
        id: uid('issue'),
        chapter: title,
        level: 'medium',
        message: '检测到重复章节标题，建议调整命名。'
      })
    }
    titleSet.add(title)

    const content = chapter.content || ''
    const hasMorning = content.includes('早晨') || content.includes('清晨')
    const hasMidnight = content.includes('午夜') || content.includes('凌晨')
    if (hasMorning && hasMidnight) {
      consistencyIssues.value.push({
        id: uid('issue'),
        chapter: title,
        level: 'high',
        message: '同章节内同时出现清晨与深夜描述，建议核对时间线。'
      })
    }

    if (content.length > 0 && content.length < 120) {
      consistencyIssues.value.push({
        id: uid('issue'),
        chapter: title,
        level: 'medium',
        message: '章节内容较短，可能影响叙事完整度。'
      })
    }
  })

  if (!consistencyIssues.value.length) {
    ElMessage.success('一致性检查通过，未发现明显冲突')
  }
}

const scoreReadability = (text) => {
  const len = text.length
  if (len < 200) return 55
  if (len < 600) return 72
  if (len < 1600) return 85
  return 90
}

const scoreDialogue = (text) => {
  const quoteCount = (text.match(/[“”"「」]/g) || []).length
  return Math.min(95, 40 + quoteCount * 4)
}

const scorePace = (text) => {
  const sentence = text.split(/[。！？!?\n]/).filter(Boolean)
  if (!sentence.length) return 50
  const avgLen = text.length / sentence.length
  if (avgLen > 60) return 65
  if (avgLen > 35) return 78
  return 88
}

const runQualityScore = () => {
  qualityRows.value = chapterBoard.value.map((chapter, idx) => {
    const chapterName = chapter.title || `第${idx + 1}章`
    const text = chapter.content || ''
    const readability = scoreReadability(text)
    const dialogue = scoreDialogue(text)
    const pace = scorePace(text)
    const total = Math.round(readability * 0.4 + dialogue * 0.25 + pace * 0.35)
    return {
      chapter: chapterName,
      readability,
      dialogue,
      pace,
      total
    }
  })
}

const addCharacter = () => {
  characters.value.push({
    id: uid('char'),
    name: '',
    goal: '',
    secret: ''
  })
}

const removeCharacter = (index) => {
  characters.value.splice(index, 1)
}

const addRelationship = () => {
  relationships.value.push({
    id: uid('rel'),
    from: '',
    type: 'ally',
    to: ''
  })
}

const saveCharacterData = () => {
  if (!selectedNovelId.value) return
  localStorage.setItem(`characters_${selectedNovelId.value}`, JSON.stringify({
    characters: characters.value,
    relationships: relationships.value
  }))
  ElMessage.success('角色关系已保存')
}

const saveSyncConfig = () => {
  localStorage.setItem('creationStudioSyncConfig', JSON.stringify(syncConfig.value))
  ElMessage.success('同步配置已保存')
}

const exportAllData = () => {
  const payload = {
    exportedAt: now(),
    novels: JSON.parse(localStorage.getItem('novels') || '[]'),
    prompts: JSON.parse(localStorage.getItem('prompts') || '[]'),
    writingGoals: JSON.parse(localStorage.getItem('writingGoals') || '[]')
  }
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `91writing-backup-${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(link.href)
}

const importBackup = async (file) => {
  try {
    const text = await file.raw.text()
    const parsed = JSON.parse(text)
    if (parsed.novels) localStorage.setItem('novels', JSON.stringify(parsed.novels))
    if (parsed.prompts) localStorage.setItem('prompts', JSON.stringify(parsed.prompts))
    if (parsed.writingGoals) localStorage.setItem('writingGoals', JSON.stringify(parsed.writingGoals))
    ElMessage.success('备份导入成功')
    refreshData()
  } catch (error) {
    ElMessage.error('备份导入失败，请检查文件格式')
  }
}

const exportPublishFile = () => {
  if (!selectedNovel.value) return
  const title = selectedNovel.value.title || '未命名小说'
  const chapters = chapterBoard.value
  const header = {
    general: `# ${title}\n\n`,
    wechat: `【${title}】\n\n`,
    male: `《${title}》\n【男频发布稿】\n\n`,
    female: `《${title}》\n【女频发布稿】\n\n`
  }[publishTemplate.value]

  const body = chapters.map((c, idx) => {
    const chapterTitle = c.title || `第${idx + 1}章`
    const content = (c.content || '').replace(/\n{3,}/g, '\n\n')
    return `## ${chapterTitle}\n\n${content}`
  }).join('\n\n')

  publishPreview.value = `${header}${body}`

  const blob = new Blob([publishPreview.value], { type: 'text/plain;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${title}-${publishTemplate.value}-publish.txt`
  link.click()
  URL.revokeObjectURL(link.href)
}

refreshData()
</script>

<style scoped>
.creation-studio {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.creation-studio.focus-mode {
  position: relative;
}

.studio-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.studio-header h2 {
  margin: 0;
}

.studio-header p {
  margin: 6px 0 0;
  color: #909399;
}

.studio-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.focus-toolbar {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.kanban-list {
  margin: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 280px;
  overflow: auto;
}

.kanban-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  cursor: move;
}

.snapshot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 480px;
  overflow: auto;
}

.snapshot-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
}

.snapshot-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.hint-text {
  color: #909399;
  margin: 4px 0;
}

.character-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.character-item {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr auto;
  gap: 8px;
}

.relationship-row {
  margin-bottom: 8px;
  display: grid;
  grid-template-columns: 1fr 120px 1fr auto;
  gap: 8px;
}

.sync-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mt-12 {
  margin-top: 12px;
}

@media (max-width: 768px) {
  .character-item,
  .relationship-row {
    grid-template-columns: 1fr;
  }
}
</style>
