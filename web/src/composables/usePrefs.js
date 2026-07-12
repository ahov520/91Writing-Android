/**
 * UI prefs — localStorage key writing91_prefs
 */
import { ref, watch } from 'vue'

const KEY = 'writing91_prefs'
const prefs = ref({
  theme: 'dark', // dark | light
  fontSize: 17,
  lineHeight: 1.75,
  readingMode: false
})
let loaded = false

function load() {
  if (loaded) return
  try {
    const raw = localStorage.getItem(KEY)
    if (raw) prefs.value = { ...prefs.value, ...JSON.parse(raw) }
  } catch {
    /* ignore */
  }
  loaded = true
  applyTheme()
}

function save() {
  try {
    localStorage.setItem(KEY, JSON.stringify(prefs.value))
  } catch {
    /* ignore */
  }
  applyTheme()
}

function applyTheme() {
  if (typeof document === 'undefined') return
  const t = prefs.value.theme === 'light' ? 'light' : 'dark'
  document.documentElement.setAttribute('data-theme', t)
  document.documentElement.style.setProperty('--editor-font-size', `${prefs.value.fontSize || 17}px`)
  document.documentElement.style.setProperty('--editor-line-height', String(prefs.value.lineHeight || 1.75))
}

export function usePrefs() {
  load()

  function setTheme(theme) {
    prefs.value.theme = theme === 'light' ? 'light' : 'dark'
    save()
  }

  function setFontSize(n) {
    prefs.value.fontSize = Math.min(28, Math.max(14, Number(n) || 17))
    save()
  }

  function setLineHeight(n) {
    prefs.value.lineHeight = Math.min(2.4, Math.max(1.3, Number(n) || 1.75))
    save()
  }

  function toggleReadingMode() {
    prefs.value.readingMode = !prefs.value.readingMode
    save()
  }

  return {
    prefs,
    setTheme,
    setFontSize,
    setLineHeight,
    toggleReadingMode,
    applyTheme,
    load
  }
}

// apply early if imported from main
if (typeof window !== 'undefined') {
  load()
}
