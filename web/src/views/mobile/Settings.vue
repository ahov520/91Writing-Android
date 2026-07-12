<template>
  <div class="m-page">
    <header class="m-header">
      <div class="m-header__text">
        <h1>设置</h1>
        <p>API · 显示 · 关于</p>
      </div>
    </header>

    <div class="m-section-title">显示</div>
    <div class="m-card" style="margin-bottom: 16px">
      <div class="m-segment" style="margin-bottom: 12px">
        <button type="button" :class="{ 'is-active': prefs.theme === 'dark' }" @click="setTheme('dark')">
          深色
        </button>
        <button type="button" :class="{ 'is-active': prefs.theme === 'light' }" @click="setTheme('light')">
          浅色
        </button>
      </div>
      <div class="m-field">
        <label>编辑字号（{{ prefs.fontSize }}px）</label>
        <input
          :value="prefs.fontSize"
          class="m-input"
          type="range"
          min="14"
          max="24"
          step="1"
          @input="setFontSize(Number($event.target.value))"
        />
      </div>
      <div class="m-field">
        <label>行距（{{ prefs.lineHeight }}）</label>
        <input
          :value="prefs.lineHeight"
          class="m-input"
          type="range"
          min="1.4"
          max="2.2"
          step="0.05"
          @input="setLineHeight(Number($event.target.value))"
        />
      </div>
    </div>

    <div class="m-section-title">API 配置</div>
    <div class="m-card" style="margin-bottom: 16px">
      <div class="m-segment" style="margin-bottom: 14px">
        <button
          type="button"
          :class="{ 'is-active': configType === 'official' }"
          @click="setType('official')"
        >
          官方
        </button>
        <button
          type="button"
          :class="{ 'is-active': configType === 'custom' }"
          @click="setType('custom')"
        >
          自定义
        </button>
      </div>

      <div class="m-field">
        <label>API Key</label>
        <input
          v-model="form.apiKey"
          class="m-input"
          type="password"
          autocomplete="off"
          placeholder="sk-…"
        />
      </div>

      <div v-if="configType === 'custom'" class="m-field">
        <label>Base URL</label>
        <input
          v-model="form.baseURL"
          class="m-input"
          type="url"
          placeholder="https://api.openai.com/v1"
        />
        <span class="m-hint">OpenAI 兼容接口，路径需含 /v1（按服务商要求）</span>
      </div>
      <div v-else class="m-field">
        <label>Base URL</label>
        <input class="m-input" :value="form.baseURL" disabled />
        <span class="m-hint">官方通道地址固定</span>
      </div>

      <div class="m-field">
        <label>模型</label>
        <input v-model="form.selectedModel" class="m-input" placeholder="gpt-3.5-turbo" />
      </div>

      <div class="m-field">
        <label>Temperature（{{ form.temperature }}）</label>
        <input
          v-model.number="form.temperature"
          class="m-input"
          type="number"
          min="0"
          max="2"
          step="0.1"
        />
      </div>

      <div class="m-field">
        <label>Max Tokens（可选）</label>
        <input
          v-model.number="form.maxTokens"
          class="m-input"
          type="number"
          min="0"
          step="256"
          placeholder="4096"
        />
      </div>

      <div class="m-btn-row">
        <button type="button" class="m-btn m-btn--ghost" :disabled="testing" @click="testApi">
          {{ testing ? '测试中…' : '测试连接' }}
        </button>
        <button type="button" class="m-btn m-btn--primary" @click="save">保存</button>
      </div>

      <p class="m-hint" style="margin-top: 12px">
        状态：
        <strong :style="{ color: isConfigured ? 'var(--success)' : 'var(--warning)' }">
          {{ isConfigured ? '已配置' : '未配置' }}
        </strong>
      </p>
    </div>

    <div class="m-section-title">关于</div>
    <div class="m-card">
      <div class="m-row-between" style="margin-bottom: 8px">
        <span class="m-muted">应用</span>
        <span>91写作 Android</span>
      </div>
      <div class="m-row-between" style="margin-bottom: 8px">
        <span class="m-muted">版本</span>
        <span>{{ appVersion }}</span>
      </div>
      <div class="m-row-between" style="margin-bottom: 8px">
        <span class="m-muted">界面</span>
        <span>移动端 v2.2</span>
      </div>
      <div class="m-row-between">
        <span class="m-muted">数据</span>
        <span>仅本地存储</span>
      </div>
      <p class="m-hint" style="margin-top: 12px">
        AI 请求发往你配置的接口；作品数据默认只存本机。
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import { useApiConfig } from '../../composables/useApiConfig.js'
import { usePrefs } from '../../composables/usePrefs.js'
import apiService from '../../services/api.js'
import toast from '../../services/toast.js'
import { getNativeVersion } from '../../utils/bridge.js'

const {
  configType,
  official,
  custom,
  isConfigured,
  setType,
  updateOfficial,
  updateCustom,
  applyToService,
  load
} = useApiConfig()

const { prefs, setTheme, setFontSize, setLineHeight } = usePrefs()

const form = reactive({
  apiKey: '',
  baseURL: '',
  selectedModel: '',
  temperature: 0.7,
  maxTokens: 4096
})

const testing = ref(false)
const appVersion = ref(getNativeVersion() || '2.2.0-web')

function syncForm() {
  const src = configType.value === 'official' ? official.value : custom.value
  form.apiKey = src.apiKey || ''
  form.baseURL = src.baseURL || ''
  form.selectedModel = src.selectedModel || ''
  form.temperature = src.temperature ?? 0.7
  form.maxTokens = src.maxTokens ?? 4096
}

function save() {
  const patch = {
    apiKey: form.apiKey.trim(),
    selectedModel: form.selectedModel.trim(),
    temperature: Number(form.temperature) || 0.7,
    maxTokens: form.maxTokens ? Number(form.maxTokens) : null
  }
  if (configType.value === 'custom') {
    patch.baseURL = (form.baseURL || '').trim().replace(/\/$/, '')
    updateCustom(patch)
  } else {
    updateOfficial(patch)
  }
  syncForm()
  toast.success('已保存')
}

async function testApi() {
  save()
  applyToService()
  testing.value = true
  try {
    const text = await apiService.generateText('请只回复：ok', {
      maxTokens: 16,
      type: 'test'
    })
    toast.success('连接成功：' + String(text || '').slice(0, 40))
  } catch (e) {
    toast.error(e?.message || '连接失败')
  } finally {
    testing.value = false
  }
}

watch(configType, syncForm)
onMounted(() => {
  load()
  syncForm()
  appVersion.value = getNativeVersion() || window.__WRITING91_VERSION__ || '2.2.0-web'
})
</script>
