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

    <div v-if="goalBanner" class="m-card m-goal-banner" @click="$router.push('/goals')">
      <div class="m-row-between">
        <strong style="font-size: 0.9rem">🎯 今日 {{ goalBanner.progress }}/{{ goalBanner.target }} 字</strong>
        <span class="m-muted" style="font-size: 0.8rem">连续 {{ goalBanner.streak }} 天 ›</span>
      </div>
      <div class="m-progress" style="margin-top: 8px">
        <div class="m-progress__bar" :style="{ width: goalBanner.pct + '%' }" />
      </div>
    </div>

    <div v-if="recentList.length" class="m-section-title" style="margin-top: 4px">最近打开</div>
    <div v-if="recentList.length" class="m-chips" style="padding-bottom: 8px">
      <button
        v-for="r in recentList"
        :key="r.id"
        type="button"
        class="m-chip is-active"
        @click="openNovel(r)"
      >
        {{ r.title }}
      </button>
    </div>

    <div class="m-field" style="margin-bottom: 8px">
      <input
        v-model="keyword"
        class="m-input"
        type="search"
        placeholder="搜索作品标题…"
        enterkeyhint="search"
      />
    </div>
    <div class="m-chips" style="padding-bottom: 10px">
      <button
        type="button"
        class="m-chip"
        :class="{ 'is-active': statusFilter === 'all' }"
        @click="statusFilter = 'all'"
      >
        全部
      </button>
      <button
        type="button"
        class="m-chip"
        :class="{ 'is-active': statusFilter === 'writing' }"
        @click="statusFilter = 'writing'"
      >
        创作中
      </button>
      <button
        type="button"
        class="m-chip"
        :class="{ 'is-active': statusFilter === 'paused' }"
        @click="statusFilter = 'paused'"
      >
        暂停
      </button>
      <button
        type="button"
        class="m-chip"
        :class="{ 'is-active': statusFilter === 'completed' }"
        @click="statusFilter = 'completed'"
      >
        完成
      </button>
      <button
        v-for="g in genreChips"
        :key="g.code"
        type="button"
        class="m-chip"
        :class="{ 'is-active': genreFilter === g.code }"
        @click="genreFilter = genreFilter === g.code ? 'all' : g.code"
      >
        {{ g.name }}
      </button>
    </div>

    <div v-if="!filtered.length" class="m-empty">
      <div class="m-empty__icon">📖</div>
      <h3>{{ novels.length ? '没有匹配的作品' : '还没有作品' }}</h3>
      <p>{{ novels.length ? '试试其他筛选' : '点右下角 + 创建第一部小说' }}</p>
      <button v-if="!novels.length" type="button" class="m-btn m-btn--primary" @click="openCreate">
        新建作品
      </button>
    </div>

    <div v-else class="m-list">
      <div v-for="n in filtered" :key="n.id" class="m-novel-card-wrap">
        <button type="button" class="m-novel-card" @click="openNovel(n)">
          <div class="m-novel-card__top">
            <div class="m-novel-card__cover" :style="coverStyle(n)">
              <span v-if="!n.cover">{{ (n.title || '书')[0] }}</span>
            </div>
            <div class="m-novel-card__body">
              <h3 class="m-novel-card__title">{{ n.title }}</h3>
              <p class="m-novel-card__desc">{{ n.description || '暂无简介' }}</p>
            </div>
          </div>
          <div class="m-novel-card__meta">
            <span class="m-badge">{{ genreName(n.genre) }}</span>
            <span
              class="m-badge"
              :class="
                n.status === 'completed'
                  ? 'm-badge--ok'
                  : n.status === 'paused'
                    ? 'm-badge--warn'
                    : ''
              "
            >
              {{ statusLabel(n.status) }}
            </span>
            <span>{{ (n.chapterList || []).length }} 章</span>
            <span>{{ formatWords(n.wordCount) }} 字</span>
          </div>
        </button>
        <div class="m-novel-card__actions">
          <button type="button" class="m-chip-btn" @click="openMenu(n)">⋯</button>
        </div>
      </div>
    </div>

    <button type="button" class="m-fab" aria-label="新建作品" @click="openCreate">+</button>

    <!-- create -->
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
          <textarea v-model="form.description" class="m-textarea" rows="3" />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showCreate = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" :disabled="!form.title.trim()" @click="create">
            创建并写作
          </button>
        </div>
      </div>
    </div>

    <!-- actions menu -->
    <div v-if="menuNovel" class="m-sheet-mask" @click.self="menuNovel = null">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">{{ menuNovel.title }}</h2>
        <div class="m-list-actions">
          <button type="button" class="m-action-row" @click="openNovel(menuNovel)">打开写作</button>
          <button type="button" class="m-action-row" @click="openDetail">详情 / 封面</button>
          <button type="button" class="m-action-row" @click="doDuplicate">复制作品</button>
          <button type="button" class="m-action-row" @click="doExport">导出 TXT</button>
          <button type="button" class="m-action-row" style="color: var(--danger)" @click="doDelete">
            删除作品
          </button>
        </div>
        <button type="button" class="m-btn m-btn--ghost m-btn--block" style="margin-top: 10px" @click="menuNovel = null">
          关闭
        </button>
      </div>
    </div>

    <!-- detail / cover -->
    <div v-if="showDetail" class="m-sheet-mask" @click.self="showDetail = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">作品详情</h2>
        <div class="m-field">
          <label>标题</label>
          <input v-model="detailForm.title" class="m-input" />
        </div>
        <div class="m-field">
          <label>作者</label>
          <input v-model="detailForm.author" class="m-input" />
        </div>
        <div class="m-field">
          <label>类型</label>
          <select v-model="detailForm.genre" class="m-select">
            <option v-for="g in GENRES" :key="g.code" :value="g.code">{{ g.name }}</option>
          </select>
        </div>
        <div class="m-field">
          <label>状态</label>
          <select v-model="detailForm.status" class="m-select">
            <option value="writing">创作中</option>
            <option value="paused">已暂停</option>
            <option value="completed">已完成</option>
          </select>
        </div>
        <div class="m-field">
          <label>简介</label>
          <textarea v-model="detailForm.description" class="m-textarea" rows="3" />
        </div>
        <div class="m-field">
          <label>封面（可选）</label>
          <input type="file" accept="image/*" @change="onCover" />
          <span class="m-hint">将压缩为小图存本地，过大可能占空间</span>
          <div v-if="detailForm.cover" class="m-cover-preview" :style="coverStyle({ cover: detailForm.cover })" />
          <button
            v-if="detailForm.cover"
            type="button"
            class="m-btn m-btn--ghost m-btn--sm"
            style="margin-top: 8px"
            @click="detailForm.cover = ''"
          >
            清除封面
          </button>
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="showDetail = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" @click="saveDetail">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNovels, statusLabel } from '../../composables/useNovels.js'
import { useGenres } from '../../composables/useGenres.js'
import { useGoals } from '../../composables/useGoals.js'
import { runDailyAutoBackup } from '../../composables/useAutoBackup.js'
import toast from '../../services/toast.js'
import { downloadText } from '../../utils/download.js'

const RECENT_KEY = 'writing91_recent_novels'

const router = useRouter()
const {
  novels,
  sorted,
  totalWords,
  load,
  createNovel,
  updateNovel,
  deleteNovel,
  duplicateNovel,
  exportNovelText,
  getById
} = useNovels()
const { options: GENRES, nameOf: genreName, bumpUsage } = useGenres()
const { todayBanner, load: loadGoals } = useGoals()

const keyword = ref('')
const statusFilter = ref('all')
const genreFilter = ref('all')
const recentIds = ref([])
const showCreate = ref(false)
const showDetail = ref(false)
const menuNovel = ref(null)
const form = reactive({ title: '', genre: 'fantasy', description: '' })
const detailForm = reactive({
  id: '',
  title: '',
  author: '',
  genre: 'fantasy',
  status: 'writing',
  description: '',
  cover: ''
})

const totalChapters = computed(() =>
  novels.value.reduce((s, n) => s + (n.chapterList?.length || 0), 0)
)

const goalBanner = computed(() => todayBanner())

const genreChips = computed(() => (GENRES.value || []).slice(0, 8))

const recentList = computed(() =>
  recentIds.value
    .map((id) => novels.value.find((n) => String(n.id) === String(id)))
    .filter(Boolean)
    .slice(0, 6)
)

const filtered = computed(() => {
  let list = sorted.value
  if (statusFilter.value !== 'all') {
    list = list.filter((n) => n.status === statusFilter.value)
  }
  if (genreFilter.value !== 'all') {
    list = list.filter((n) => n.genre === genreFilter.value)
  }
  const k = keyword.value.trim().toLowerCase()
  if (!k) return list
  return list.filter(
    (n) =>
      (n.title || '').toLowerCase().includes(k) ||
      (n.description || '').toLowerCase().includes(k)
  )
})

function loadRecent() {
  try {
    recentIds.value = JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
    if (!Array.isArray(recentIds.value)) recentIds.value = []
  } catch {
    recentIds.value = []
  }
}

function pushRecent(id) {
  const ids = [String(id), ...recentIds.value.filter((x) => String(x) !== String(id))].slice(0, 12)
  recentIds.value = ids
  try {
    localStorage.setItem(RECENT_KEY, JSON.stringify(ids))
  } catch {
    /* ignore */
  }
}

function formatWords(n) {
  const v = Number(n) || 0
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return String(v)
}

function coverStyle(n) {
  if (n?.cover) {
    return {
      backgroundImage: `url(${n.cover})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {}
}

function openCreate() {
  form.title = ''
  form.genre = GENRES.value[0]?.code || 'fantasy'
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
    try {
      await bumpUsage(form.genre)
    } catch {
      /* ignore */
    }
    showCreate.value = false
    toast.success('已创建')
    router.push(`/write/${novel.id}`)
  } catch (e) {
    toast.error('创建失败：' + (e.message || e))
  }
}

function openNovel(n) {
  menuNovel.value = null
  if (n?.id) pushRecent(n.id)
  router.push(`/write/${n.id}`)
}

function openMenu(n) {
  menuNovel.value = n
}

function openDetail() {
  const n = menuNovel.value
  if (!n) return
  Object.assign(detailForm, {
    id: n.id,
    title: n.title || '',
    author: n.author || '',
    genre: n.genre || 'fantasy',
    status: n.status || 'writing',
    description: n.description || '',
    cover: n.cover || ''
  })
  menuNovel.value = null
  showDetail.value = true
}

function onCover(ev) {
  const file = ev.target.files?.[0]
  ev.target.value = ''
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    toast.warning('请选择 2MB 以内的图片')
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    const img = new Image()
    img.onload = () => {
      const max = 320
      let w = img.width
      let h = img.height
      if (w > max || h > max) {
        const r = Math.min(max / w, max / h)
        w = Math.round(w * r)
        h = Math.round(h * r)
      }
      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, w, h)
      detailForm.cover = canvas.toDataURL('image/jpeg', 0.72)
    }
    img.src = reader.result
  }
  reader.readAsDataURL(file)
}

async function saveDetail() {
  await updateNovel(detailForm.id, {
    title: detailForm.title.trim() || '未命名',
    author: detailForm.author.trim(),
    genre: detailForm.genre,
    status: detailForm.status,
    description: detailForm.description,
    cover: detailForm.cover
  })
  showDetail.value = false
  toast.success('已保存')
}

async function doDuplicate() {
  const n = menuNovel.value
  menuNovel.value = null
  if (!n) return
  const copy = await duplicateNovel(n.id)
  toast.success(copy ? '已复制' : '复制失败')
}

function doExport() {
  const n = menuNovel.value
  menuNovel.value = null
  if (!n) return
  downloadText(`${n.title || 'novel'}.txt`, exportNovelText(n))
  toast.success('已导出')
}

async function doDelete() {
  const n = menuNovel.value
  if (!n) return
  if (!confirm(`确定删除《${n.title}》？不可恢复`)) return
  menuNovel.value = null
  await deleteNovel(n.id)
  toast.success('已删除')
}

onMounted(async () => {
  await load()
  loadGoals()
  loadRecent()
  runDailyAutoBackup(novels.value).catch(() => {})
})
</script>
