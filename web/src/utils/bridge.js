/**
 * Android WebView bridge helpers (no-op on pure browser).
 * Native injects: window.Writing91Bridge
 */

export function isAndroidApp() {
  return !!(
    typeof window !== 'undefined' &&
    (window.__WRITING91_ANDROID__ || window.Writing91Bridge || /Writing91Android/i.test(navigator.userAgent || ''))
  )
}

export function bridge() {
  return (typeof window !== 'undefined' && window.Writing91Bridge) || null
}

export function keepScreenOn(on) {
  try {
    bridge()?.keepScreenOn?.(!!on)
  } catch {
    /* ignore */
  }
}

export function getNativeVersion() {
  try {
    return bridge()?.getVersion?.() || ''
  } catch {
    return ''
  }
}

/**
 * Export text via native share/save when available; else browser download.
 */
export function exportViaBridge(filename, text, mime = 'text/plain') {
  const b = bridge()
  if (b?.exportText) {
    try {
      // base64 for binary-safe transfer
      const b64 = btoa(unescape(encodeURIComponent(text)))
      b.exportText(filename, b64, mime)
      return true
    } catch (e) {
      console.warn('bridge export failed', e)
    }
  }
  return false
}

export function haptic() {
  try {
    bridge()?.haptic?.()
  } catch {
    /* ignore */
  }
}
