/**
 * Mobile / Android WebView shell helpers:
 * - --app-vh tracks visualViewport (keyboard-aware)
 * - data-keyboard-open on <html> when soft keyboard likely visible
 * - data-route for route-specific chrome (e.g. hide bottom nav on writer)
 */

let installed = false
let lastInnerHeight = 0

function measureViewport() {
  const vv = window.visualViewport
  const h = (vv && vv.height) || window.innerHeight || 0
  const w = (vv && vv.width) || window.innerWidth || 0
  const offsetTop = (vv && vv.offsetTop) || 0

  document.documentElement.style.setProperty('--app-vh', `${h}px`)
  document.documentElement.style.setProperty('--app-vw', `${w}px`)
  document.documentElement.style.setProperty('--vv-offset-top', `${offsetTop}px`)

  // Keyboard heuristic: visual viewport much shorter than layout viewport
  const layoutH = window.innerHeight || lastInnerHeight || h
  if (layoutH > 0) lastInnerHeight = layoutH
  const shrink = layoutH - h
  const keyboardOpen = shrink > 120 || (vv && layoutH > 0 && h / layoutH < 0.78)

  document.documentElement.classList.toggle('keyboard-open', !!keyboardOpen)
  document.documentElement.setAttribute(
    'data-keyboard',
    keyboardOpen ? 'open' : 'closed'
  )

  // Focused field scroll-into-view on Android (helps fixed toolbars)
  if (keyboardOpen) {
    const el = document.activeElement
    if (
      el &&
      (el.tagName === 'INPUT' ||
        el.tagName === 'TEXTAREA' ||
        el.isContentEditable)
    ) {
      try {
        el.scrollIntoView({ block: 'center', behavior: 'smooth' })
      } catch {
        /* ignore */
      }
    }
  }
}

function detectAndroid() {
  try {
    const ua = navigator.userAgent || ''
    if (
      /Android/i.test(ua) ||
      window.Writing91Android ||
      window.__WRITING91_ANDROID__
    ) {
      document.documentElement.classList.add('writing91-android')
      document.documentElement.setAttribute('data-platform', 'android')
      return true
    }
  } catch {
    /* ignore */
  }
  return false
}

function detectStandaloneMobile() {
  const coarse =
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(pointer: coarse)').matches
  const narrow = window.innerWidth <= 1024
  if (coarse || narrow || document.documentElement.classList.contains('writing91-android')) {
    document.documentElement.classList.add('is-touch-shell')
  }
}

/**
 * Install viewport + keyboard listeners (idempotent).
 */
export function installMobileShell() {
  if (installed || typeof window === 'undefined') return
  installed = true

  detectAndroid()
  detectStandaloneMobile()
  measureViewport()

  window.addEventListener('resize', measureViewport, { passive: true })
  window.addEventListener('orientationchange', () => {
    setTimeout(measureViewport, 80)
    setTimeout(measureViewport, 320)
  })

  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', measureViewport, {
      passive: true
    })
    window.visualViewport.addEventListener('scroll', measureViewport, {
      passive: true
    })
  }

  // Focus changes often correlate with keyboard
  document.addEventListener('focusin', () => setTimeout(measureViewport, 50), true)
  document.addEventListener('focusout', () => setTimeout(measureViewport, 50), true)
}

/**
 * Sync route path onto <html> for CSS (writer immersion etc.).
 */
export function setRouteChrome(path = '') {
  const p = String(path || '')
  document.documentElement.setAttribute('data-route', p)
  const isWriter = p === '/writer' || p.startsWith('/writer')
  document.documentElement.classList.toggle('route-writer', isWriter)
  document.documentElement.classList.toggle('hide-bottom-nav', isWriter)
}

export default {
  installMobileShell,
  setRouteChrome
}
