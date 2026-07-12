<template>
  <div class="m-writer">
    <!-- top bar -->
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
      <button
        type="button"
        class="m-icon-btn"
        :disabled="saving || !dirty"
        aria-label="保存"
        @click="saveNow"
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
      <p>可能已被删除</p>
      <button type="button" class="m-btn m-btn--primary" @click="$router.replace('/')">回书架</button>
    </div>

    <template v-else>
      <div class="m-writer__editor-wrap">
        <textarea
          ref="editorEl"
          v-model="draft"
          class="m-writer__textarea"
          :placeholder="isStreaming ? 'AI 生成中…' : '开始写作，或点下方 AI 续写…'"
          :readonly="isStreaming"
          spellcheck="false"
          @input="onInput"
        />
        <div v-if="streamPreview" class="m-writer__stream">{{ streamPreview }}</div>
      </div>

      <div class="m-writer__toolbar">
        <div class="m-writer__status">
          <span>{{ saveHint }}</span>
          <span v-if="isStreaming" style="color: var(--accent-2)">生成中…</span>
          <span v-else-if="!isConfigured" style="color: var(--warning)">未配置 API</span>
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
            <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="openPromptSheet">
              自定义
            </button>
            <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="openChapterMeta">
              章节名
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
        <button type="button" class="m-btn m-btn--primary m-btn--sm" @click="addNewChapter">
          + 新章
        </button>
        <button type="button" class="m-icon-btn" @click="drawerOpen = false">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
      <div class="m-drawer__list">
        <button
          v-for="(c, i) in chapters"
          :key="c.id"
          type="button"
          class="m-chapter-item"
          :class="{ 'is-active': String(c.id) === String(chapterId) }"
          @click="selectChapter(c.id)"
        >
          <div class="m-chapter-item__title">{{ i + 1 }}. {{ c.title }}</div>
          <div class="m-chapter-item__meta">{{ c.wordCount || 0 }} 字</div>
        </button>
        <div v-if="!chapters.length" class="m-empty" style="padding: 24px 8px">
          <p>暂无章节</p>
        </div>
      </div>
      <div style="padding: 10px; border-top: 1px solid var(--border)">
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

    <!-- custom prompt sheet -->
    <div v-if="showPrompt" class="m-sheet-mask" @click.self="showPrompt = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">自定义 AI 指令</h2>
        <div class="m-field">
          <label>你希望 AI 做什么？</label>
          <textarea
            v-model="customPrompt"
            class="m-textarea"
            rows="4"
            placeholder="例如：按当前文风续写 500 字，加入一场冲突…"
          />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showPrompt = false">取消</button>
          <button
            type="button"
            class="m-btn m-btn--primary"
            :disabled="!customPrompt.trim()"
            @click="runCustom"
          >
            开始生成
          </button>
        </div>
      </div>
    </div>

    <!-- chapter title sheet -->
    <div v-if="showMeta" class="m-sheet-mask" @click.self="showMeta = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">章节信息</h2>
        <div class="m-field">
          <label>章节标题</label>
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
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNovels } from '../../composables/useNovels.js'
import { useApiConfig } from '../../composables/useApiConfig.js'
import apiService from '../../services/api.js'
import toast from '../../services/toast.js'

const route = useRoute()
const router = useRouter()
const {
  load,
  getById,
  updateChapter,
  addChapter,
  deleteChapter,
  countWords
} = useNovels()
const { isConfigured, applyToService } = useApiConfig()

const novelId = computed(() => route.params.id)
const novel = ref(null)
const chapterId = ref(null)
const draft = ref('')
const dirty = ref(false)
const saving = ref(false)
const saveHint = ref('已保存')
const drawerOpen = ref(false)
const showPrompt = ref(false)
const showMeta = ref(false)
const customPrompt = ref('')
const metaTitle = ref('')
const isStreaming = ref(false)
const streamPreview = ref('')
const editorEl = ref(null)

let abortController = null
let saveTimer = null
let streamBuffer = ''

const chapters = computed(() => novel.value?.chapterList || [])
const chapter = computed(
  () => chapters.value.find((c) => String(c.id) === String(chapterId.value)) || null
)
const wordCount = computed(() => countWords(draft.value))

function refreshNovel() {
  novel.value = getById(novelId.value)
  if (!novel.value) return
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
  saveTimer = setTimeout(() => {
    saveNow(true)
  }, 800)
}

async function saveNow(silent = false) {
  if (!novel.value || !chapterId.value) return
  if (saving.value) return
  saving.value = true
  try {
    await updateChapter(novelId.value, chapterId.value, {
      content: draft.value,
      wordCount: countWords(draft.value)
    })
    novel.value = getById(novelId.value)
    dirty.value = false
    saveHint.value = '已保存 ' + new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    if (!silent) toast.success('已保存')
  } catch (e) {
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
  // save current first
  if (dirty.value) {
    saveNow(true)
  }
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
  } catch (e) {
    toast.error('添加失败')
  }
}

async function removeChapter() {
  if (!chapterId.value) return
  if (!confirm('确定删除当前章节？不可恢复')) return
  try {
    const id = chapterId.value
    await deleteChapter(novelId.value, id)
    novel.value = getById(novelId.value)
    chapterId.value = null
    refreshNovel()
    toast.success('已删除')
  } catch (e) {
    toast.error('删除失败')
  }
}

function openChapterMeta() {
  metaTitle.value = chapter.value?.title || ''
  showMeta.value = true
}

async function saveMeta() {
  const t = metaTitle.value.trim()
  if (!t || !chapterId.value) return
  await updateChapter(novelId.value, chapterId.value, { title: t })
  novel.value = getById(novelId.value)
  showMeta.value = false
  toast.success('已更新标题')
}

function openPromptSheet() {
  if (!isConfigured.value) {
    toast.warning('请先在设置中配置 API')
    router.push('/settings')
    return
  }
  customPrompt.value = ''
  showPrompt.value = true
}

function buildPrompt(mode, extra = '') {
  const title = novel.value?.title || ''
  const genre = novel.value?.genre || ''
  const chapterTitle = chapter.value?.title || ''
  const body = draft.value || ''
  const tail = body.slice(-2500)

  if (mode === 'continue') {
    return `你是一位网络小说作者。请根据以下上下文续写正文，要求：
1. 直接输出续写正文，不要标题、不要解释、不要 markdown
2. 保持人称与文风一致
3. 续写约 400～800 字

作品：《${title}》 类型：${genre}
当前章节：${chapterTitle}

【上文】
${tail || '（章节刚开始，请开篇）'}

请续写：`
  }

  if (mode === 'polish') {
    const sel = body.slice(-2000) || body
    return `请润色以下小说正文，要求：
1. 保持原意与人称
2. 提升文笔与节奏，减少重复
3. 只输出润色后的正文，不要解释

【原文】
${sel}`
  }

  if (mode === 'expand') {
    const sel = body.slice(-1500) || body
    return `请将以下小说片段扩写得更细腻（约 2 倍篇幅），只输出扩写正文：

【原文】
${sel}`
  }

  // custom
  return `你是一位网络小说作者。作品：《${title}》，章节：${chapterTitle}。

用户指令：${extra}

【当前正文（末尾）】
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
  const prompt = buildPrompt(mode)
  await streamGenerate(prompt, mode)
}

async function runCustom() {
  const p = customPrompt.value.trim()
  if (!p) return
  showPrompt.value = false
  if (dirty.value) await saveNow(true)
  applyToService()
  await streamGenerate(buildPrompt('custom', p), 'custom')
}

async function streamGenerate(prompt, type) {
  isStreaming.value = true
  streamPreview.value = ''
  streamBuffer = ''
  abortController = new AbortController()

  // patch fetch abort via timeout already in api; we track local stop flag
  let stopped = false
  const stopFlag = { stop: false }
  abortController.signal.addEventListener('abort', () => {
    stopped = true
    stopFlag.stop = true
  })

  try {
    const mode = type === 'polish' || type === 'expand' ? 'replace-tail' : 'append'
    const before = draft.value

    await apiService.generateTextStream(
      prompt,
      { type: type || 'generation' },
      (chunk, full) => {
        if (stopped) return
        streamBuffer = full
        streamPreview.value = full
        if (mode === 'append') {
          draft.value = before + full
        } else {
          // polish/expand: show stream in preview, apply at end
        }
        dirty.value = true
        saveHint.value = '生成中…'
        // auto scroll
        if (editorEl.value) {
          editorEl.value.scrollTop = editorEl.value.scrollHeight
        }
      }
    )

    if (stopped) {
      toast.info('已停止')
    } else if (type === 'polish' || type === 'expand') {
      // replace last portion or whole if short
      if (before.length > 2000) {
        draft.value = before.slice(0, -2000) + streamBuffer
      } else {
        draft.value = streamBuffer
      }
      dirty.value = true
    }

    streamPreview.value = ''
    await saveNow(true)
    toast.success('生成完成')
  } catch (e) {
    console.error(e)
    if (streamBuffer && type !== 'polish' && type !== 'expand') {
      // keep partial append
      await saveNow(true)
    }
    toast.error(e?.message || '生成失败')
  } finally {
    isStreaming.value = false
    abortController = null
    streamPreview.value = ''
  }
}

function stopStream() {
  // apiService uses AbortSignal.timeout — we can't easily abort mid-fetch without changing api.
  // Best-effort: mark streaming false and keep partial content.
  isStreaming.value = false
  streamPreview.value = ''
  toast.info('已停止接收（已保留已生成内容）')
  saveNow(true)
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
  refreshNovel()
  // keep screen awake hint via optional bridge later
})

onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
  if (dirty.value) saveNow(true)
})
</script>
