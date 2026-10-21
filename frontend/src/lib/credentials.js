export function credentialError(email, password, register = false) {
  const address = String(email ?? '').trim()
  if (address.length > 320 || !/^[^\s@<>"\\\p{C}]+@[^\s@<>"\\\p{C}]+\.[^\s@<>"\\\p{C}]+$/u.test(address)) {
    return 'Ingresa un correo válido, sin espacios internos ni caracteres de control.'
  }
  if (typeof password !== 'string' || !password.trim() || password.includes('\0')) {
    return 'Ingresa una contraseña válida. No se admiten caracteres nulos.'
  }
  if (new TextEncoder().encode(password).length > 72) {
    return 'La contraseña supera el límite de 72 bytes. Los caracteres acentuados y emojis pueden ocupar varios bytes.'
  }
  if (register && password.length < 12) return 'La contraseña debe tener al menos 12 caracteres.'
  return ''
}

export function safeReturnTo(value, origin = window.location.origin) {
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//') || /[\\\p{C}]/u.test(value)) return '/cuenta'
  try {
    const url = new URL(value, origin)
    return url.origin === origin ? url.pathname + url.search + url.hash : '/cuenta'
  } catch {
    return '/cuenta'
  }
}
