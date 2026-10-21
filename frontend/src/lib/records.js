export function reservationValue(reservation, key) {
  const value = reservation?.[key]
  return value?.value ?? value ?? null
}

export function entityId(value) { return value?.value ?? value }
