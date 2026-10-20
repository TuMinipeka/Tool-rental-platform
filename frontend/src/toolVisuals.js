const equipmentVisuals = [
  { pattern: /hidrolavadora/, src: '/assets/tools/equipment/pressure-washer.png' },
  { pattern: /compactador tipo canguro|apisonador/, src: '/assets/tools/equipment/tamping-rammer.png' },
  { pattern: /aspiradora industrial/, src: '/assets/tools/equipment/wet-dry-vacuum.png' },
  { pattern: /desbrozadora|guadana/, src: '/assets/tools/equipment/brush-cutter.png' },
  { pattern: /allanadora/, src: '/assets/tools/equipment/power-trowel.png' },
  { pattern: /motosierra/, src: '/assets/tools/equipment/chainsaw.png' },
  { pattern: /escalera/, src: '/assets/tools/equipment/multi-purpose-ladder.png' },
  { pattern: /andamio/, src: '/assets/tools/equipment/mobile-scaffold.png' },
  { pattern: /vibrador de concreto/, src: '/assets/tools/equipment/concrete-vibrator.png' },
  { pattern: /generador|planta electrica/, src: '/assets/tools/equipment/portable-generator.png' },
  { pattern: /clavadora/, src: '/assets/tools/equipment/pneumatic-nail-gun.png' },
  { pattern: /mezcladora/, src: '/assets/tools/equipment/concrete-mixer.png' },
  { pattern: /cortadora de (baldosa|ceramica)/, src: '/assets/tools/equipment/manual-tile-cutter.png' },
  { pattern: /torre de iluminacion/, src: '/assets/tools/equipment/mobile-light-tower.png' },
  { pattern: /compresor/, src: '/assets/tools/equipment/air-compressor.png' },
  { pattern: /placa vibratoria|compactadora de placa/, src: '/assets/tools/equipment/plate-compactor.png' },
  { pattern: /brilladora|pulidora industrial|pulidora de piso/, src: '/assets/tools/equipment/floor-polisher.png' },
  { pattern: /soldador/, src: '/assets/tools/equipment/inverter-welder.png' },
  { pattern: /rotomartillo|demoledor|martillo combinado/, src: '/assets/hero-rotary-hammer-1200.png' },
]

function normalizeName(value = '') {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
}

export function resolveToolVisual(tool) {
  const name = normalizeName(tool?.name)
  return equipmentVisuals.find(({ pattern }) => pattern.test(name))?.src ?? null
}
