/** Download text/json; prefer data: URL on Android WebView. */
export function downloadText(filename, text, mime = 'text/plain;charset=utf-8') {
  const body = typeof text === 'string' ? text : String(text ?? '')
  const useData =
    typeof window !== 'undefined' &&
    (window.__WRITING91_ANDROID__ || /Android/i.test(navigator.userAgent || ''))

  if (useData) {
    try {
      const b64 = btoa(unescape(encodeURIComponent(body)))
      const a = document.createElement('a')
      a.href = `data:${mime.split(';')[0]};base64,${b64}`
      a.download = filename
      document.body.appendChild(a)
      a.click()
      a.remove()
      return
    } catch {
      /* fall through */
    }
  }

  const blob = new Blob([body], { type: mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 2000)
}

export function downloadJson(filename, data) {
  downloadText(filename, JSON.stringify(data, null, 2), 'application/json;charset=utf-8')
}
