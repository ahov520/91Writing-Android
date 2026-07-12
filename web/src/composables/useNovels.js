/**
 * Novel library — same localStorage key (`novels`) as legacy UI so data is shared.
 */
import { ref, computed } from 'vue'
import { flushStorage } from '../services/storage.js'

const novels = ref([])
const loaded = ref(false)

function uid() {
  return `${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 9)}`
}

function countWords(text) {
  if (!text) return 0
  return String(text)
    .replace(/<[^>]*>/g, '')
    .replace(/\s+/g, '')
    .length
}

function recomputeNovelStats(novel) {
  const list = novel.chapterList || []
  novel.chapters = list.length
  novel.wordCount = list.reduce((s, c) => s + (c.wordCount || countWords(c.content)), 0)
  return novel
}

function normalizeNovel(raw) {
  const n = { ...raw }
  n.id = n.id || uid()
  n.title = n.title || '未命名'
  n.author = n.author || ''
  n.genre = n.genre || 'fantasy'
  n.description = n.description || ''
  n.cover = n.cover || ''
  n.tags = Array.isArray(n.tags) ? n.tags : []
  n.status = n.status || 'writing'
  n.chapterList = Array.isArray(n.chapterList)
    ? n.chapterList.map((c) => ({
        id: c.id || uid(),
        title: c.title || '新章节',
        content: c.content || '',
        description: c.description || '',
        wordCount: c.wordCount ?? countWords(c.content),
        createdAt: c.createdAt || new Date().toISOString(),
        updatedAt: c.updatedAt || new Date().toISOString()
      }))
    : []
  n.writingRecords = Array.isArray(n.writingRecords) ? n.writingRecords : []
  n.createdAt = n.createdAt || new Date().toISOString()
  n.updatedAt = n.updatedAt || n.createdAt
  return recomputeNovelStats(n)
}

export function useNovels() {
  async function load() {
    try {
      const raw = localStorage.getItem('novels')
      if (raw) {
        const parsed = JSON.parse(raw)
        novels.value = (Array.isArray(parsed) ? parsed : []).map(normalizeNovel)
      } else {
        novels.value = []
      }
    } catch (e) {
      console.error('load novels failed', e)
      novels.value = []
    }
    loaded.value = true
    return novels.value
  }

  async function save() {
    try {
      localStorage.setItem('novels', JSON.stringify(novels.value))
      await flushStorage()
    } catch (e) {
      console.error('save novels failed', e)
      throw e
    }
  }

  function getById(id) {
    return novels.value.find((n) => String(n.id) === String(id)) || null
  }

  async function createNovel(payload = {}) {
    const novel = normalizeNovel({
      id: uid(),
      title: payload.title || '未命名作品',
      author: payload.author || '',
      genre: payload.genre || 'fantasy',
      description: payload.description || '',
      status: 'writing',
      tags: payload.tags || [],
      chapterList: [
        {
          id: uid(),
          title: '第一章',
          content: '',
          description: '',
          wordCount: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      ],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    })
    novels.value.unshift(novel)
    await save()
    return novel
  }

  async function updateNovel(id, patch) {
    const idx = novels.value.findIndex((n) => String(n.id) === String(id))
    if (idx < 0) return null
    const next = normalizeNovel({
      ...novels.value[idx],
      ...patch,
      id: novels.value[idx].id,
      updatedAt: new Date().toISOString()
    })
    novels.value[idx] = next
    await save()
    return next
  }

  async function deleteNovel(id) {
    novels.value = novels.value.filter((n) => String(n.id) !== String(id))
    await save()
  }

  async function addChapter(novelId, title = '新章节') {
    const novel = getById(novelId)
    if (!novel) return null
    const chapter = {
      id: uid(),
      title,
      content: '',
      description: '',
      wordCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    const list = [...(novel.chapterList || []), chapter]
    await updateNovel(novelId, { chapterList: list })
    return chapter
  }

  async function updateChapter(novelId, chapterId, patch) {
    const novel = getById(novelId)
    if (!novel) return null
    const list = (novel.chapterList || []).map((c) => {
      if (String(c.id) !== String(chapterId)) return c
      const content = patch.content !== undefined ? patch.content : c.content
      return {
        ...c,
        ...patch,
        content,
        wordCount: patch.wordCount ?? countWords(content),
        updatedAt: new Date().toISOString()
      }
    })
    await updateNovel(novelId, { chapterList: list })
    return list.find((c) => String(c.id) === String(chapterId)) || null
  }

  async function deleteChapter(novelId, chapterId) {
    const novel = getById(novelId)
    if (!novel) return
    const list = (novel.chapterList || []).filter(
      (c) => String(c.id) !== String(chapterId)
    )
    await updateNovel(novelId, { chapterList: list })
  }

  async function reorderChapters(novelId, fromIndex, toIndex) {
    const novel = getById(novelId)
    if (!novel) return
    const list = [...(novel.chapterList || [])]
    if (
      fromIndex < 0 ||
      toIndex < 0 ||
      fromIndex >= list.length ||
      toIndex >= list.length ||
      fromIndex === toIndex
    ) {
      return
    }
    const [item] = list.splice(fromIndex, 1)
    list.splice(toIndex, 0, item)
    await updateNovel(novelId, { chapterList: list })
  }

  async function moveChapter(novelId, chapterId, direction) {
    const novel = getById(novelId)
    if (!novel) return
    const list = novel.chapterList || []
    const idx = list.findIndex((c) => String(c.id) === String(chapterId))
    if (idx < 0) return
    const to = direction === 'up' ? idx - 1 : idx + 1
    await reorderChapters(novelId, idx, to)
  }

  function exportNovelText(novel) {
    if (!novel) return ''
    const lines = []
    lines.push(`《${novel.title || '未命名'}》`)
    lines.push('='.repeat(40))
    if (novel.author) lines.push(`作者：${novel.author}`)
    if (novel.description) lines.push(`\n简介：\n${stripHtml(novel.description)}`)
    lines.push('')
    ;(novel.chapterList || []).forEach((c, i) => {
      lines.push(`第${i + 1}章 ${c.title || ''}`)
      lines.push('-'.repeat(24))
      lines.push(stripHtml(c.content || '') || '（空）')
      lines.push('')
    })
    return lines.join('\n')
  }

  function exportChapterText(novel, chapter) {
    const title = novel?.title || '未命名'
    const ct = chapter?.title || '章节'
    return `《${title}》\n${ct}\n\n${stripHtml(chapter?.content || '')}`
  }

  async function replaceAll(list) {
    novels.value = (Array.isArray(list) ? list : []).map(normalizeNovel)
    await save()
  }

  const totalWords = computed(() =>
    novels.value.reduce((s, n) => s + (n.wordCount || 0), 0)
  )

  const sorted = computed(() =>
    [...novels.value].sort(
      (a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)
    )
  )

  return {
    novels,
    sorted,
    loaded,
    totalWords,
    load,
    save,
    getById,
    createNovel,
    updateNovel,
    deleteNovel,
    addChapter,
    updateChapter,
    deleteChapter,
    reorderChapters,
    moveChapter,
    exportNovelText,
    exportChapterText,
    replaceAll,
    countWords,
    stripHtml
  }
}

function stripHtml(html) {
  if (!html) return ''
  return String(html)
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p>/gi, '\n\n')
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

export const GENRES = [
  { code: 'fantasy', name: '玄幻' },
  { code: 'urban', name: '都市' },
  { code: 'history', name: '历史' },
  { code: 'scifi', name: '科幻' },
  { code: 'wuxia', name: '武侠' },
  { code: 'romance', name: '言情' },
  { code: 'other', name: '其他' }
]

export function genreName(code) {
  return GENRES.find((g) => g.code === code)?.name || code || '未分类'
}

export function statusLabel(status) {
  return (
    {
      writing: '创作中',
      completed: '已完成',
      paused: '已暂停'
    }[status] || status || '创作中'
  )
}
