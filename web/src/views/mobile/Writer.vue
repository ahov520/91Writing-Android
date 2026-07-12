<template>
  <div class="m-writer">
    <header class="m-writer__top">
      <button type="button" class="m-icon-btn" aria-label="返回" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6" />
        </svg>
      </button>
      <button type="button" class="m-icon-btn" aria-label="章节" @click="drawerOpen = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="8" y1="6" x2="21" y2="6" />
          <line x1="8" y1="12" x2="21" y2="12" />
          <line x1="8" y1="18" x2="21" y2="18" />
          <line x1="3" y1="6" x2="3.01" y2="6" />
          <line x1="3" y1="12" x2="3.01" y2="12" />
          <line x1="3" y1="18" x2="3.01" y2="18" />
        </svg>
      </button>
      <div class="m-writer__title">
        <h2>{{ novel?.title || '写作' }}</h2>
        <span>{{ chapter?.title || '未选章节' }} · {{ wordCount }} 字</span>
      </div>
      <button type="button" class="m-icon-btn" aria-label="更多" @click="showMore = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="1" />
          <circle cx="12" cy="5" r="1" />
          <circle cx="12" cy="19" r="1" />
        </svg>
      </button>
      <button
        type="button"
        class="m-icon-btn"
        :disabled="saving || !dirty"
        aria-label="保存"
        @click="saveNow()"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
          <polyline points="17 21 17 13 7 13 7 21" />
          <polyline points="7 3 7 8 15 8" />
        </svg>
      </button>
    </header>

    <div v-if="!novel" class="m-empty" style="flex: 1">
      <div class="m-empty__icon">⚠️</div>
      <h3>找不到作品</h3>
      <button type="button" class="m-btn m-btn--primary" @click="$router.replace('/')">回书架</button>
    </div>

    <template v-else>
      <div class="m-writer__editor-wrap">
        <textarea
          ref="editorEl"
          v-model="draft"
          class="m-writer__textarea"
          :placeholder="isStreaming ? 'AI 生成中…' : '开始写作，或点下方 AI…'"
          :readonly="isStreaming"
          spellcheck="false"
          @input="onInput"
        />
        <div v-if="streamPreview && (aiMode === 'polish' || aiMode === 'expand')" class="m-writer__stream">
          {{ streamPreview }}
        </div>
      </div>

      <div class="m-writer__toolbar">
        <div class="m-writer__status">
          <span>{{ saveHint }}</span>
          <span v-if="isStreaming" style="color: var(--accent-2)">生成中…点停止可中断</span>
          <span v-else-if="!isConfigured" style="color: var(--warning)">未配置 API</span>
          <span v-else-if="useContext" style="color: var(--success)">已带设定</span>
        </div>
        <div class="m-writer__tools">
          <button
            v-if="isStreaming"
            type="button"
            class="m-btn m-btn--danger m-btn--sm"
            @click="stopStream"
          >
            停止
          </button>
          <template v-else>
            <button type="button" class="m-btn m-btn--primary m-btn--sm" @click="runAi('continue')">
              AI 续写
            </button>
            <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="runAi('polish')">
              润色
            </button>
            <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="runAi('expand')">
              扩写
            </button>
            <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="openPromptPick">
              提示词
            </button>
            <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="openPromptSheet">
              自定义
            </button>
          </template>
        </div>
      </div>
    </template>

    <!-- chapter drawer -->
    <div class="m-drawer-mask" :class="{ 'is-open': drawerOpen }" @click="drawerOpen = false" />
    <aside class="m-drawer" :class="{ 'is-open': drawerOpen }">
      <div class="m-drawer__head">
        <h3>章节</h3>
        <button type="button" class="m-btn m-btn--primary m-btn--sm" @click="addNewChapter">+ 新章</button>
        <button type="button" class="m-icon-btn" @click="drawerOpen = false">✕</button>
      </div>
      <div class="m-drawer__list">
        <div
          v-for="(c, i) in chapters"
          :key="c.id"
          class="m-chapter-item"
          :class="{ 'is-active': String(c.id) === String(chapterId) }"
        >
          <button type="button" class="m-chapter-item__main" @click="selectChapter(c.id)">
            <div class="m-chapter-item__title">{{ i + 1 }}. {{ c.title }}</div>
            <div class="m-chapter-item__meta">{{ c.wordCount || 0 }} 字</div>
          </button>
          <div class="m-chapter-item__ops">
            <button type="button" class="m-chip-btn" :disabled="i === 0" @click="moveCh(c.id, 'up')">↑</button>
            <button
              type="button"
              class="m-chip-btn"
              :disabled="i === chapters.length - 1"
              @click="moveCh(c.id, 'down')"
            >
              ↓
            </button>
            <button type="button" class="m-chip-btn" @click="renameChapter(c)">✎</button>
          </div>
        </div>
        <div v-if="!chapters.length" class="m-empty" style="padding: 24px 8px"><p>暂无章节</p></div>
      </div>
      <div style="padding: 10px; border-top: 1px solid var(--border); display: flex; flex-direction: column; gap: 8px">
        <button type="button" class="m-btn m-btn--ghost m-btn--block m-btn--sm" @click="exportChapter">
          导出本章
        </button>
        <button type="button" class="m-btn m-btn--ghost m-btn--block m-btn--sm" @click="exportBook">
          导出全书
        </button>
        <button
          type="button"
          class="m-btn m-btn--danger m-btn--block m-btn--sm"
          :disabled="!chapterId"
          @click="removeChapter"
        >
          删除当前章节
        </button>
      </div>
    </aside>

    <!-- more menu -->
    <div v-if="showMore" class="m-sheet-mask" @click.self="showMore = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">作品与设定</h2>
        <div class="m-list-actions">
          <button type="button" class="m-action-row" @click="openNovelEdit">编辑作品信息</button>
          <button type="button" class="m-action-row" @click="goExtras">世界观 / 角色 / 语料 / 事件</button>
          <button type="button" class="m-action-row" @click="toggleContext">
            AI 引用设定：{{ useContext ? '开' : '关' }}
          </button>
          <button type="button" class="m-action-row" @click="exportChapter">导出本章 TXT</button>
          <button type="button" class="m-action-row" @click="exportBook">导出全书 TXT</button>
          <button type="button" class="m-action-row" @click="openChapterMeta">重命名当前章节</button>
        </div>
        <button type="button" class="m-btn m-btn--ghost m-btn--block" style="margin-top: 12px" @click="showMore = false">
          关闭
        </button>
      </div>
    </div>

    <!-- novel edit -->
    <div v-if="showNovelEdit" class="m-sheet-mask" @click.self="showNovelEdit = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">作品信息</h2>
        <div class="m-field">
          <label>标题</label>
          <input v-model="novelForm.title" class="m-input" maxlength="80" />
        </div>
        <div class="m-field">
          <label>作者</label>
          <input v-model="novelForm.author" class="m-input" maxlength="40" />
        </div>
        <div class="m-field">
          <label>类型</label>
          <select v-model="novelForm.genre" class="m-select">
            <option v-for="g in genreOptions" :key="g.code" :value="g.code">{{ g.name }}</option>
          </select>
        </div>
        <div class="m-field">
          <label>状态</label>
          <select v-model="novelForm.status" class="m-select">
            <option value="writing">创作中</option>
            <option value="paused">已暂停</option>
            <option value="completed">已完成</option>
          </select>
        </div>
        <div class="m-field">
          <label>简介</label>
          <textarea v-model="novelForm.description" class="m-textarea" rows="3" />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showNovelEdit = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" @click="saveNovelInfo">保存</button>
        </div>
      </div>
    </div>

    <!-- custom prompt -->
    <div v-if="showPrompt" class="m-sheet-mask" @click.self="showPrompt = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">自定义 AI 指令</h2>
        <div class="m-field">
          <textarea v-model="customPrompt" class="m-textarea" rows="4" placeholder="描述你希望 AI 做什么…" />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showPrompt = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" :disabled="!customPrompt.trim()" @click="runCustom">
            开始生成
          </button>
        </div>
      </div>
    </div>

    <!-- prompt library pick -->
    <div v-if="showPromptPick" class="m-sheet-mask" @click.self="showPromptPick = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">选用提示词</h2>
        <div class="m-field">
          <select v-model="promptCat" class="m-select">
            <option v-for="c in PROMPT_CATEGORIES" :key="c.key" :value="c.key">
              {{ c.icon }} {{ c.name }}
            </option>
          </select>
        </div>
        <div class="m-list" style="max-height: 40vh; overflow: auto">
          <button
            v-for="p in filteredPrompts"
            :key="p.id"
            type="button"
            class="m-novel-card"
            style="margin-bottom: 8px"
            @click="usePrompt(p)"
          >
            <div class="m-novel-card__title">{{ p.title }}</div>
            <p class="m-novel-card__desc">{{ p.description || p.content?.slice(0, 80) }}</p>
          </button>
          <div v-if="!filteredPrompts.length" class="m-muted" style="padding: 12px">暂无提示词</div>
        </div>
        <button type="button" class="m-btn m-btn--ghost m-btn--block" @click="$router.push('/prompts')">
          管理提示词库
        </button>
      </div>
    </div>

    <!-- chapter rename -->
    <div v-if="showMeta" class="m-sheet-mask" @click.self="showMeta = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">章节标题</h2>
        <div class="m-field">
          <input v-model="metaTitle" class="m-input" maxlength="80" />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showMeta = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" @click="saveMeta">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNovels } from '../../composables/useNovels.js'
import { useApiConfig } from '../../composables/useApiConfig.js'
import { usePrompts, PROMPT_CATEGORIES } from '../../composables/usePrompts.js'
import { useGenres } from '../../composables/useGenres.js'
import { useGoals } from '../../composables/useGoals.js'
import { useNovelExtras } from '../../composables/useNovelExtras.js'
import apiService from '../../services/api.js'
import toast from '../../services/toast.js'
import { downloadText } from '../../utils/download.js'

const route = useRoute()
const router = useRouter()
const {
  load,
  getById,
  updateNovel,
  updateChapter,
  addChapter,
  deleteChapter,
  moveChapter,
  exportNovelText,
  exportChapterText,
  countWords
} = useNovels()
const { isConfigured, applyToService } = useApiConfig()
const { byCategory, bumpUsage, applyTemplate, load: loadPrompts } = usePrompts()
const { options: genreOptions } = useGenres()
const { recordWords } = useGoals()

const novelId = computed(() => route.params.id)
const extras = useNovelExtras(novelId)

const novel = ref(null)
const chapterId = ref(null)
const draft = ref('')
const dirty = ref(false)
const saving = ref(false)
const saveHint = ref('已保存')
const drawerOpen = ref(false)
const showPrompt = ref(false)
const showPromptPick = ref(false)
const showMeta = ref(false)
const showMore = ref(false)
const showNovelEdit = ref(false)
const customPrompt = ref('')
const metaTitle = ref('')
const isStreaming = ref(false)
const streamPreview = ref('')
const aiMode = ref('continue')
const editorEl = ref(null)
const useContext = ref(true)
const promptCat = ref('all')
const novelForm = reactive({
  title: '',
  author: '',
  genre: 'fantasy',
  status: 'writing',
  description: ''
})

let abortController = null
let saveTimer = null
let streamBuffer = ''
let lastSavedWords = 0

const chapters = computed(() => novel.value?.chapterList || [])
const chapter = computed(
  () => chapters.value.find((c) => String(c.id) === String(chapterId.value)) || null
)
const wordCount = computed(() => countWords(draft.value))
const filteredPrompts = computed(() => byCategory(promptCat.value))

function refreshNovel() {
  novel.value = getById(novelId.value)
  if (!novel.value) return
  extras.loadAll()
  const list = novel.value.chapterList || []
  if (!list.length) {
    chapterId.value = null
    draft.value = ''
    return
  }
  if (!chapterId.value || !list.some((c) => String(c.id) === String(chapterId.value))) {
    chapterId.value = list[0].id
  }
  loadChapterDraft()
}

function loadChapterDraft() {
  const c = chapter.value
  draft.value = c?.content || ''
  lastSavedWords = countWords(draft.value)
  dirty.value = false
  saveHint.value = '已保存'
}

function onInput() {
  dirty.value = true
  saveHint.value = '编辑中…'
  scheduleSave()
}

function scheduleSave() {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => saveNow(true), 800)
}

async function saveNow(silent = false) {
  if (!novel.value || !chapterId.value || saving.value) return
  saving.value = true
  try {
    const wc = countWords(draft.value)
    await updateChapter(novelId.value, chapterId.value, {
      content: draft.value,
      wordCount: wc
    })
    const delta = wc - lastSavedWords
    if (delta > 0) {
      try {
        await recordWords(delta)
      } catch {
        /* ignore */
      }
    }
    lastSavedWords = wc
    novel.value = getById(novelId.value)
    dirty.value = false
    saveHint.value =
      '已保存 ' +
      new Date().toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    if (!silent) toast.success('已保存')
  } catch {
    saveHint.value = '保存失败'
    if (!silent) toast.error('保存失败')
  } finally {
    saving.value = false
  }
}

function selectChapter(id) {
  if (String(id) === String(chapterId.value)) {
    drawerOpen.value = false
    return
  }
  if (dirty.value) saveNow(true)
  chapterId.value = id
  loadChapterDraft()
  drawerOpen.value = false
  streamPreview.value = ''
}

async function addNewChapter() {
  const n = (chapters.value.length || 0) + 1
  try {
    if (dirty.value) await saveNow(true)
    const c = await addChapter(novelId.value, `第${n}章`)
    novel.value = getById(novelId.value)
    if (c) {
      chapterId.value = c.id
      loadChapterDraft()
    }
    toast.success('已添加章节')
  } catch {
    toast.error('添加失败')
  }
}

async function removeChapter() {
  if (!chapterId.value || !confirm('确定删除当前章节？')) return
  await deleteChapter(novelId.value, chapterId.value)
  novel.value = getById(novelId.value)
  chapterId.value = null
  refreshNovel()
  toast.success('已删除')
}

async function moveCh(id, dir) {
  if (dirty.value) await saveNow(true)
  await moveChapter(novelId.value, id, dir)
  novel.value = getById(novelId.value)
}

function renameChapter(c) {
  chapterId.value = c.id
  loadChapterDraft()
  openChapterMeta()
}

function openChapterMeta() {
  metaTitle.value = chapter.value?.title || ''
  showMeta.value = true
  showMore.value = false
}

async function saveMeta() {
  const t = metaTitle.value.trim()
  if (!t || !chapterId.value) return
  await updateChapter(novelId.value, chapterId.value, { title: t })
  novel.value = getById(novelId.value)
  showMeta.value = false
  toast.success('已更新标题')
}

function openNovelEdit() {
  showMore.value = false
  Object.assign(novelForm, {
    title: novel.value?.title || '',
    author: novel.value?.author || '',
    genre: novel.value?.genre || 'fantasy',
    status: novel.value?.status || 'writing',
    description: novel.value?.description || ''
  })
  showNovelEdit.value = true
}

async function saveNovelInfo() {
  await updateNovel(novelId.value, {
    title: novelForm.title.trim() || '未命名',
    author: novelForm.author.trim(),
    genre: novelForm.genre,
    status: novelForm.status,
    description: novelForm.description
  })
  novel.value = getById(novelId.value)
  showNovelEdit.value = false
  toast.success('作品信息已保存')
}

function goExtras() {
  showMore.value = false
  router.push(`/extras/${novelId.value}`)
}

function toggleContext() {
  useContext.value = !useContext.value
  toast.info(useContext.value ? 'AI 将引用角色/世界观等设定' : '已关闭设定引用')
}

function exportChapter() {
  if (!chapter.value) return
  const text = exportChapterText(novel.value, { ...chapter.value, content: draft.value })
  downloadText(`${novel.value.title}-${chapter.value.title}.txt`, text)
  toast.success('已导出本章')
  showMore.value = false
}

function exportBook() {
  if (dirty.value) saveNow(true)
  const n = getById(novelId.value)
  downloadText(`${n?.title || 'novel'}.txt`, exportNovelText(n))
  toast.success('已导出全书')
  showMore.value = false
}

function openPromptSheet() {
  if (!isConfigured.value) {
    toast.warning('请先配置 API')
    router.push('/settings')
    return
  }
  customPrompt.value = ''
  showPrompt.value = true
}

function openPromptPick() {
  loadPrompts()
  showPromptPick.value = true
}

function contextBlock() {
  if (!useContext.value) return ''
  const s = extras.buildContextSummary()
  return s ? `\n\n${s}\n` : ''
}

function buildPrompt(mode, extra = '') {
  const title = novel.value?.title || ''
  const genre = novel.value?.genre || ''
  const chapterTitle = chapter.value?.title || ''
  const body = draft.value || ''
  const tail = body.slice(-2500)
  const ctx = contextBlock()

  if (mode === 'continue') {
    return `你是一位网络小说作者。请根据上下文续写正文：
1. 只输出续写正文，无标题无解释
2. 保持人称与文风
3. 约 400～800 字
${ctx}
作品：《${title}》 类型：${genre}
章节：${chapterTitle}

【上文】
${tail || '（开篇）'}

请续写：`
  }
  if (mode === 'polish') {
    const sel = body.slice(-2000) || body
    return `请润色以下正文，保持原意，只输出润色结果：\n\n【原文】\n${sel}`
  }
  if (mode === 'expand') {
    const sel = body.slice(-1500) || body
    return `请扩写以下片段（约 2 倍），只输出正文：\n\n【原文】\n${sel}`
  }
  if (mode === 'template') {
    return extra
  }
  return `你是一位网络小说作者。作品：《${title}》，章节：${chapterTitle}。
${ctx}
用户指令：${extra}

【正文末尾】
${tail || '（空）'}

请按指令输出正文（不要解释）：`
}

async function runAi(mode) {
  if (!isConfigured.value) {
    toast.warning('请先配置 API')
    router.push('/settings')
    return
  }
  if (isStreaming.value) return
  if (dirty.value) await saveNow(true)
  applyToService()
  await streamGenerate(buildPrompt(mode), mode)
}

async function runCustom() {
  const p = customPrompt.value.trim()
  if (!p) return
  showPrompt.value = false
  if (dirty.value) await saveNow(true)
  applyToService()
  await streamGenerate(buildPrompt('custom', p), 'custom')
}

async function usePrompt(p) {
  showPromptPick.value = false
  if (!isConfigured.value) {
    toast.warning('请先配置 API')
    router.push('/settings')
    return
  }
  await bumpUsage(p.id)
  const filled = applyTemplate(p.content, {
    context: draft.value.slice(-2500),
    original: draft.value.slice(-2000),
    title: novel.value?.title,
    genre: novel.value?.genre,
    description: novel.value?.description,
    chapter: chapter.value?.title
  })
  if (dirty.value) await saveNow(true)
  applyToService()
  const mode = p.category === 'polish' ? 'polish' : 'template'
  await streamGenerate(
    mode === 'polish' ? buildPrompt('polish') : buildPrompt('template', filled + contextBlock()),
    mode === 'polish' ? 'polish' : 'custom'
  )
}

async function streamGenerate(prompt, type) {
  isStreaming.value = true
  aiMode.value = type
  streamPreview.value = ''
  streamBuffer = ''
  abortController = new AbortController()
  const mode = type === 'polish' || type === 'expand' ? 'replace-tail' : 'append'
  const before = draft.value

  try {
    await apiService.generateTextStream(
      prompt,
      { type: type || 'generation', signal: abortController.signal },
      (chunk, full) => {
        streamBuffer = full
        streamPreview.value = full
        if (mode === 'append') draft.value = before + full
        dirty.value = true
        saveHint.value = '生成中…'
        if (editorEl.value) editorEl.value.scrollTop = editorEl.value.scrollHeight
      }
    )

    if (type === 'polish' || type === 'expand') {
      draft.value =
        before.length > 2000 ? before.slice(0, -2000) + streamBuffer : streamBuffer
      dirty.value = true
    }
    streamPreview.value = ''
    await saveNow(true)
    toast.success('生成完成')
  } catch (e) {
    if (e?.name === 'AbortError' || e?.message === 'GENERATION_ABORTED') {
      const partial = e.partial || streamBuffer
      if (mode === 'append' && partial) {
        draft.value = before + partial
        dirty.value = true
      }
      await saveNow(true)
      toast.info('已停止，已保留已生成内容')
    } else {
      console.error(e)
      if (streamBuffer && mode === 'append') await saveNow(true)
      toast.error(e?.message || '生成失败')
    }
  } finally {
    isStreaming.value = false
    abortController = null
    streamPreview.value = ''
  }
}

function stopStream() {
  if (abortController) {
    abortController.abort()
  }
}

function goBack() {
  if (dirty.value) saveNow(true)
  router.push('/')
}

watch(novelId, async () => {
  await load()
  refreshNovel()
})

onMounted(async () => {
  await load()
  loadPrompts()
  refreshNovel()
})

onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
  if (abortController) abortController.abort()
  if (dirty.value) saveNow(true)
})
</script>
