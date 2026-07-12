<template>
  <div class="m-page">
    <header class="m-header">
      <div class="m-header__text">
        <h1>91写作</h1>
        <p>本地书架 · 数据仅存本机</p>
      </div>
      <button type="button" class="m-icon-btn" title="设置" @click="$router.push('/settings')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
      </button>
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
        <div class="m-stat__label">总字数</div>
      </div>
    </div>

    <div class="m-field" style="margin-bottom: 12px">
      <input
        v-model="keyword"
        class="m-input"
        type="search"
        placeholder="搜索作品标题…"
        enterkeyhint="search"
      />
    </div>

    <div v-if="!filtered.length" class="m-empty">
      <div class="m-empty__icon">📖</div>
      <h3>{{ novels.length ? '没有匹配的作品' : '还没有作品' }}</h3>
      <p>{{ novels.length ? '试试其他关键词' : '点右下角 + 创建第一部小说' }}</p>
      <button v-if="!novels.length" type="button" class="m-btn m-btn--primary" @click="openCreate">
        新建作品
      </button>
    </div>

    <div v-else class="m-list">
      <button
        v-for="n in filtered"
        :key="n.id"
        type="button"
        class="m-novel-card"
        @click="openNovel(n)"
      >
        <div class="m-novel-card__top">
          <div class="m-novel-card__cover">{{ (n.title || '书')[0] }}</div>
          <div class="m-novel-card__body">
            <h3 class="m-novel-card__title">{{ n.title }}</h3>
            <p class="m-novel-card__desc">
              {{ n.description || '暂无简介' }}
            </p>
          </div>
        </div>
        <div class="m-novel-card__meta">
          <span class="m-badge">{{ genreName(n.genre) }}</span>
          <span
            class="m-badge"
            :class="n.status === 'completed' ? 'm-badge--ok' : n.status === 'paused' ? 'm-badge--warn' : ''"
          >
            {{ statusLabel(n.status) }}
          </span>
          <span>{{ (n.chapterList || []).length }} 章</span>
          <span>{{ formatWords(n.wordCount) }} 字</span>
        </div>
      </button>
    </div>

    <button type="button" class="m-fab" aria-label="新建作品" @click="openCreate">+</button>

    <!-- create sheet -->
    <div v-if="showCreate" class="m-sheet-mask" @click.self="showCreate = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">新建作品</h2>
        <div class="m-field">
          <label>标题</label>
          <input v-model="form.title" class="m-input" placeholder="作品名称" maxlength="80" />
        </div>
        <div class="m-field">
          <label>类型</label>
          <select v-model="form.genre" class="m-select">
            <option v-for="g in GENRES" :key="g.code" :value="g.code">{{ g.name }}</option>
          </select>
        </div>
        <div class="m-field">
          <label>简介（可选）</label>
          <textarea v-model="form.description" class="m-textarea" rows="3" placeholder="一句话简介" />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showCreate = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" :disabled="!form.title.trim()" @click="create">
            创建并写作
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  useNovels,
  GENRES,
  genreName,
  statusLabel
} from '../../composables/useNovels.js'
import toast from '../../services/toast.js'

const router = useRouter()
const { novels, sorted, totalWords, load, createNovel } = useNovels()
const keyword = ref('')
const showCreate = ref(false)
const form = reactive({ title: '', genre: 'fantasy', description: '' })

const totalChapters = computed(() =>
  novels.value.reduce((s, n) => s + (n.chapterList?.length || 0), 0)
)

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  const list = sorted.value
  if (!k) return list
  return list.filter(
    (n) =>
      (n.title || '').toLowerCase().includes(k) ||
      (n.description || '').toLowerCase().includes(k)
  )
})

function formatWords(n) {
  const v = Number(n) || 0
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return String(v)
}

function openCreate() {
  form.title = ''
  form.genre = 'fantasy'
  form.description = ''
  showCreate.value = true
}

async function create() {
  const title = form.title.trim()
  if (!title) return
  try {
    const novel = await createNovel({
      title,
      genre: form.genre,
      description: form.description.trim()
    })
    showCreate.value = false
    toast.success('已创建')
    router.push(`/write/${novel.id}`)
  } catch (e) {
    toast.error('创建失败：' + (e.message || e))
  }
}

function openNovel(n) {
  router.push(`/write/${n.id}`)
}

onMounted(() => {
  load()
})
</script>
