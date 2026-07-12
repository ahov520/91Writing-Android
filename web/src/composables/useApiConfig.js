/**
 * API config — same localStorage keys as legacy (officialApiConfig / customApiConfig).
 */
import { ref, computed } from 'vue'
import apiService from '../services/api.js'

const OFFICIAL_BASE = 'https://ai.91hub.vip/v1'

const official = ref({
  apiKey: '',
  baseURL: OFFICIAL_BASE,
  selectedModel: 'claude-4-sonnet',
  maxTokens: 4096,
  temperature: 0.7
})

const custom = ref({
  apiKey: '',
  baseURL: 'https://api.openai.com/v1',
  selectedModel: 'gpt-3.5-turbo',
  maxTokens: 4096,
  temperature: 0.7
})

const configType = ref('official') // official | custom
const ready = ref(false)

function load() {
  try {
    configType.value = localStorage.getItem('apiConfigType') || 'official'
    const o = localStorage.getItem('officialApiConfig')
    if (o) {
      official.value = {
        ...official.value,
        ...JSON.parse(o),
        baseURL: OFFICIAL_BASE
      }
    }
    const c = localStorage.getItem('customApiConfig')
    if (c) {
      custom.value = { ...custom.value, ...JSON.parse(c) }
    }
  } catch (e) {
    console.warn('load api config failed', e)
  }
  applyToService()
  ready.value = true
}

function current() {
  return configType.value === 'official' ? official.value : custom.value
}

function applyToService() {
  const cfg = current()
  apiService.updateConfig({
    apiKey: cfg.apiKey || '',
    baseURL: (cfg.baseURL || '').replace(/\/$/, ''),
    selectedModel: cfg.selectedModel,
    defaultModel: cfg.selectedModel,
    maxTokens: cfg.maxTokens,
    temperature: cfg.temperature
  })
}

function save() {
  try {
    localStorage.setItem('apiConfigType', configType.value)
    localStorage.setItem(
      'officialApiConfig',
      JSON.stringify({ ...official.value, baseURL: OFFICIAL_BASE })
    )
    localStorage.setItem('customApiConfig', JSON.stringify(custom.value))
    // legacy key
    localStorage.setItem('apiConfig', JSON.stringify(current()))
  } catch (e) {
    console.error('save api config failed', e)
    throw e
  }
  applyToService()
}

export function useApiConfig() {
  if (!ready.value) load()

  const isConfigured = computed(() => {
    const c = current()
    return !!(c.apiKey && c.apiKey.trim() && c.baseURL && c.baseURL.trim())
  })

  const active = computed(() => current())

  function setType(type) {
    configType.value = type === 'custom' ? 'custom' : 'official'
    save()
  }

  function updateOfficial(patch) {
    official.value = {
      ...official.value,
      ...patch,
      baseURL: OFFICIAL_BASE
    }
    save()
  }

  function updateCustom(patch) {
    custom.value = { ...custom.value, ...patch }
    save()
  }

  function updateActive(patch) {
    if (configType.value === 'official') updateOfficial(patch)
    else updateCustom(patch)
  }

  return {
    official,
    custom,
    configType,
    active,
    isConfigured,
    ready,
    load,
    save,
    setType,
    updateOfficial,
    updateCustom,
    updateActive,
    applyToService
  }
}
