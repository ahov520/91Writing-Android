<template>
  <div class="m-page">
    <header class="m-header">
      <div class="m-header__text">
        <h1>备份</h1>
        <p>导出 / 导入本地数据</p>
      </div>
    </header>

    <div class="m-stats">
      <div class="m-stat">
        <div class="m-stat__val">{{ novels.length }}</div>
        <div class="m-stat__label">作品</div>
      </div>
      <div class="m-stat">
        <div class="m-stat__val">{{ totalChapters }}</div>
        <div class="m-stat__label">章节</div>
      </div>
      <div class="m-stat">
        <div class="m-stat__val">{{ formatWords(totalWords) }}</div>
        <div class="m-stat__label">字数</div>
      </div>
    </div>

    <div class="m-section-title">导出</div>
    <div class="m-card" style="margin-bottom: 16px">
      <p class="m-hint" style="margin: 0 0 12px">
        分类导出或完整备份。完整包含 API 配置，请勿外传。
      </p>
      <div class="m-btn-row" style="margin-bottom: 8px">
        <button type="button" class="m-btn m-btn--primary" :disabled="busy" @click="exportNovels">
          作品
        </button>
        <button type="button" class="m-btn m-btn--ghost" :disabled="busy" @click="exportCategory('prompts')">
          提示词
        </button>
      </div>
      <div class="m-btn-row" style="margin-bottom: 8px">
        <button type="button" class="m-btn m-btn--ghost" :disabled="busy" @click="exportCategory('writingGoals')">
          目标
        </button>
        <button type="button" class="m-btn m-btn--ghost" :disabled="busy" @click="exportCategory('novelGenres')">
          类型
        </button>
      </div>
      <button type="button" class="m-btn m-btn--ghost m-btn--block" :disabled="busy" @click="exportFull">
        导出完整备份（含配置）
      </button>
    </div>

    <div class="m-section-title">导入</div>
    <div class="m-card" style="margin-bottom: 16px">
      <p class="m-hint" style="margin: 0 0 12px">
        导入会<strong>合并</strong>同 id 作品（以文件为准覆盖），新 id 直接加入。建议先导出当前数据。
      </p>
      <input
        ref="fileInput"
        type="file"
        accept="application/json,.json"
        style="display: none"
        @change="onFile"
      />
      <div class="m-btn-row">
        <button type="button" class="m-btn m-btn--primary" :disabled="busy" @click="pickFile">
          选择 JSON 导入
        </button>
      </div>
      <p v-if="lastMsg" class="m-hint" style="margin-top: 12px">{{ lastMsg }}</p>
    </div>

    <div class="m-section-title">危险操作</div>
    <div class="m-card">
      <p class="m-danger-text" style="margin: 0 0 12px">
        清空全部作品不可恢复（不影响 API 配置）。
      </p>
      <button type="button" class="m-btn m-btn--danger m-btn--block" :disabled="busy" @click="clearAll">
        清空全部作品
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useNovels } from '../../composables/useNovels.js'
import { flushStorage } from '../../services/storage.js'
import toast from '../../services/toast.js'

const { novels, totalWords, load, replaceAll } = useNovels()
const busy = ref(false)
const lastMsg = ref('')
const fileInput = ref(null)

const totalChapters = computed(() =>
  novels.value.reduce((s, n) => s + (n.chapterList?.length || 0), 0)
)

function formatWords(n) {
  const v = Number(n) || 0
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return String(v)
}

function downloadJson(filename, data) {
  const text = JSON.stringify(data, null, 2)
  // Prefer data: URL on Android WebView (blob: downloads are blocked by the shell)
  const useData =
    typeof window !== 'undefined' &&
    (window.__WRITING91_ANDROID__ || /Android/i.test(navigator.userAgent || ''))

  if (useData) {
    const b64 =
      typeof btoa === 'function'
        ? btoa(unescape(encodeURIComponent(text)))
        : null
    if (b64) {
      const a = document.createElement('a')
      a.href = `data:application/json;base64,${b64}`
      a.download = filename
      a.rel = 'noopener'
      document.body.appendChild(a)
      a.click()
      a.remove()
      return
    }
  }

  const blob = new Blob([text], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.rel = 'noopener'
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 2000)
}

async function exportNovels() {
  busy.value = true
  try {
    await flushStorage()
    await load()
    const payload = {
      type: 'writing91-novels',
      version: 2,
      exportedAt: new Date().toISOString(),
      novels: novels.value
    }
    const name = `91writing-novels-${dateStamp()}.json`
    downloadJson(name, payload)
    lastMsg.value = `已导出 ${novels.value.length} 部作品`
    toast.success('导出已开始')
  } catch (e) {
    toast.error('导出失败')
  } finally {
    busy.value = false
  }
}

async function exportCategory(key) {
  busy.value = true
  try {
    await flushStorage()
    const raw = localStorage.getItem(key)
    let data
    try {
      data = raw ? JSON.parse(raw) : []
    } catch {
      data = raw
    }
    const payload = {
      type: `writing91-${key}`,
      version: 2,
      exportedAt: new Date().toISOString(),
      [key]: data
    }
    downloadJson(`91writing-${key}-${dateStamp()}.json`, payload)
    lastMsg.value = `已导出 ${key}`
    toast.success('已导出')
  } catch {
    toast.error('导出失败')
  } finally {
    busy.value = false
  }
}

async function exportFull() {
  busy.value = true
  try {
    await flushStorage()
    await load()
    const keys = [
      'novels',
      'officialApiConfig',
      'customApiConfig',
      'apiConfigType',
      'apiConfig',
      'writingGoals',
      'novelGenres',
      'prompts',
      'billing_records',
      'token_usage_stats'
    ]
    const data = {}
    for (const k of keys) {
      try {
        const v = localStorage.getItem(k)
        if (v != null) data[k] = v
      } catch {
        /* ignore */
      }
    }
    // novels already managed — ensure latest
    data.novels = JSON.stringify(novels.value)

    const payload = {
      type: 'writing91-full-backup',
      version: 2,
      exportedAt: new Date().toISOString(),
      data
    }
    downloadJson(`91writing-full-${dateStamp()}.json`, payload)
    lastMsg.value = '已导出完整备份（含配置）'
    toast.success('完整备份已导出')
  } catch (e) {
    toast.error('导出失败')
  } finally {
    busy.value = false
  }
}

function dateStamp() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}-${p(d.getHours())}${p(d.getMinutes())}`
}

function pickFile() {
  fileInput.value?.click()
}

async function onFile(ev) {
  const file = ev.target.files?.[0]
  ev.target.value = ''
  if (!file) return
  busy.value = true
  lastMsg.value = ''
  try {
    const text = await file.text()
    const json = JSON.parse(text)
    let incoming = []

    // Category-only backups
    if (json?.type === 'writing91-prompts' && json.prompts) {
      localStorage.setItem('prompts', JSON.stringify(json.prompts))
      lastMsg.value = '已导入提示词'
      toast.success('提示词已导入')
      return
    }
    if (json?.type === 'writing91-writingGoals' && json.writingGoals) {
      localStorage.setItem('writingGoals', JSON.stringify(json.writingGoals))
      lastMsg.value = '已导入写作目标'
      toast.success('目标已导入')
      return
    }
    if (json?.type === 'writing91-novelGenres' && json.novelGenres) {
      localStorage.setItem('novelGenres', JSON.stringify(json.novelGenres))
      lastMsg.value = '已导入类型'
      toast.success('类型已导入')
      return
    }
    // Legacy prompts export format
    if (json?.type === 'prompts' && Array.isArray(json.prompts)) {
      localStorage.setItem('prompts', JSON.stringify(json.prompts))
      lastMsg.value = '已导入旧版提示词格式'
      toast.success('提示词已导入')
      return
    }

    if (json?.type === 'writing91-novels' && Array.isArray(json.novels)) {
      incoming = json.novels
    } else if (json?.type === 'writing91-full-backup' && json.data) {
      // restore config keys (optional)
      const data = json.data
      for (const [k, v] of Object.entries(data)) {
        if (k === 'novels') continue
        try {
          if (typeof v === 'string') localStorage.setItem(k, v)
        } catch {
          /* ignore */
        }
      }
      if (data.novels) {
        incoming =
          typeof data.novels === 'string' ? JSON.parse(data.novels) : data.novels
      }
    } else if (Array.isArray(json)) {
      incoming = json
    } else if (Array.isArray(json.novels)) {
      incoming = json.novels
    } else {
      throw new Error('无法识别的备份格式')
    }

    await load()
    const map = new Map(novels.value.map((n) => [String(n.id), n]))
    let added = 0
    let updated = 0
    for (const n of incoming) {
      if (!n || !n.id) {
        // assign will happen in replaceAll normalize — treat as new
        map.set(`import_${Date.now()}_${added}`, { ...n, id: undefined })
        added++
        continue
      }
      if (map.has(String(n.id))) {
        map.set(String(n.id), n)
        updated++
      } else {
        map.set(String(n.id), n)
        added++
      }
    }
    await replaceAll([...map.values()])
    lastMsg.value = `导入完成：新增/写入 ${added}，覆盖 ${updated}`
    toast.success('导入成功')
  } catch (e) {
    console.error(e)
    lastMsg.value = '导入失败：' + (e.message || e)
    toast.error(e?.message || '导入失败')
  } finally {
    busy.value = false
  }
}

async function clearAll() {
  if (!confirm('确定清空全部作品？此操作不可恢复。')) return
  if (!confirm('再次确认：所有章节正文将删除。')) return
  busy.value = true
  try {
    await replaceAll([])
    lastMsg.value = '已清空作品'
    toast.success('已清空')
  } catch (e) {
    toast.error('清空失败')
  } finally {
    busy.value = false
  }
}

onMounted(() => load())
</script>
