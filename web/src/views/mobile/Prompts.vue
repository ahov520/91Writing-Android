<template>
  <div class="m-page">
    <header class="m-header">
      <div class="m-header__text">
        <h1>提示词库</h1>
        <p>{{ prompts.length }} 条模板</p>
      </div>
      <button type="button" class="m-btn m-btn--primary m-btn--sm" @click="openCreate">新建</button>
    </header>

    <div class="m-chips">
      <button
        v-for="c in PROMPT_CATEGORIES"
        :key="c.key"
        type="button"
        class="m-chip"
        :class="{ 'is-active': cat === c.key }"
        @click="cat = c.key"
      >
        {{ c.icon }} {{ c.name }}
      </button>
    </div>

    <div class="m-list">
      <div v-for="p in filtered" :key="p.id" class="m-card" style="margin-bottom: 10px">
        <div class="m-row-between">
          <strong>{{ p.title }}</strong>
          <span class="m-badge">{{ catName(p.category) }}</span>
        </div>
        <p class="m-muted" style="margin: 6px 0; font-size: 0.85rem">
          {{ p.description || p.content?.slice(0, 100) }}
        </p>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost m-btn--sm" @click="edit(p)">编辑</button>
          <button
            type="button"
            class="m-btn m-btn--danger m-btn--sm"
            :disabled="p.builtin"
            @click="remove(p)"
          >
            删除
          </button>
        </div>
      </div>
      <div v-if="!filtered.length" class="m-empty">
        <p>该分类暂无提示词</p>
      </div>
    </div>

    <div v-if="sheet" class="m-sheet-mask" @click.self="sheet = false">
      <div class="m-sheet">
        <div class="m-sheet__handle" />
        <h2 class="m-sheet__title">{{ form.id ? '编辑' : '新建' }}提示词</h2>
        <div class="m-field">
          <label>标题</label>
          <input v-model="form.title" class="m-input" />
        </div>
        <div class="m-field">
          <label>分类</label>
          <select v-model="form.category" class="m-select">
            <option
              v-for="c in PROMPT_CATEGORIES.filter((x) => x.key !== 'all')"
              :key="c.key"
              :value="c.key"
            >
              {{ c.name }}
            </option>
          </select>
        </div>
        <div class="m-field">
          <label>说明</label>
          <input v-model="form.description" class="m-input" />
        </div>
        <div class="m-field">
          <label>内容（可用 {'{'}上文{'}'} {'{'}书名{'}'} 等变量）</label>
          <textarea v-model="form.content" class="m-textarea" rows="6" />
        </div>
        <div class="m-btn-row">
          <button type="button" class="m-btn m-btn--ghost" @click="sheet = false">取消</button>
          <button type="button" class="m-btn m-btn--primary" @click="save">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { usePrompts, PROMPT_CATEGORIES } from '../../composables/usePrompts.js'
import toast from '../../services/toast.js'

const { prompts, byCategory, addPrompt, updatePrompt, removePrompt } = usePrompts()
const cat = ref('all')
const sheet = ref(false)
const form = reactive({
  id: '',
  title: '',
  category: 'content',
  description: '',
  content: ''
})

const filtered = computed(() => byCategory(cat.value))

function catName(key) {
  return PROMPT_CATEGORIES.find((c) => c.key === key)?.name || key
}

function openCreate() {
  Object.assign(form, { id: '', title: '', category: 'content', description: '', content: '' })
  sheet.value = true
}

function edit(p) {
  Object.assign(form, {
    id: p.id,
    title: p.title,
    category: p.category,
    description: p.description || '',
    content: p.content || ''
  })
  sheet.value = true
}

async function save() {
  if (!form.title.trim() || !form.content.trim()) {
    toast.warning('请填写标题和内容')
    return
  }
  if (form.id) {
    await updatePrompt(form.id, {
      title: form.title.trim(),
      category: form.category,
      description: form.description,
      content: form.content
    })
  } else {
    await addPrompt({ ...form })
  }
  sheet.value = false
  toast.success('已保存')
}

async function remove(p) {
  if (p.builtin) return
  if (!confirm(`删除「${p.title}」？`)) return
  await removePrompt(p.id)
  toast.success('已删除')
}
</script>
