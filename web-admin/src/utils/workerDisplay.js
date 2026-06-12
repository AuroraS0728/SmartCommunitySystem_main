const WORKER_ALIASES = {
  20: '宋庆涵',
  21: '李娜',
  22: '王静',
  23: '赵敏',
  24: '周芳',
  25: '吴洁',
  26: '孙宁',
  27: '郑欣',
  28: '刘志国',
  29: '陈建华',
  30: '王立强',
  31: '赵明'
}

const WORKER_TRADES = {
  20: '家政',
  21: '家政',
  22: '家电清洗',
  23: '家电清洗',
  24: '护理',
  25: '家政',
  26: '家政',
  27: '家政',
  28: '水工',
  29: '电工',
  30: '家电维修',
  31: '综合维修'
}

const WORKER_TEXT_MAP = {
  'housekeeping-fixed': '固定家政保洁',
  'repair-plumber': '水工维修',
  'repair-electrician': '电工维修',
  'repair-electric': '电工维修',
  'repair-appliance': '家电维修',
  'repair-outsource': '外包合作',
  housekeeping_cert: '家政服务证',
  cleaning_cert: '保洁服务证',
  appliance_clean_cert: '家电清洗证',
  caregiver_cert: '养老护理证',
  special_service_cert: '专项服务证',
  plumber_cert: '水工证',
  electrician_cert: '电工证',
  appliance_cert: '家电维修证',
  outsource_company_qualification: '外包资质',
  housekeeping: '家政服务',
  cleaning: '保洁清洗',
  appliance: '家电服务',
  caregiver: '护理员',
  care: '护理服务',
  nurse: '养老护理',
  special: '专项服务',
  plumber: '水工维修',
  water: '水路维修',
  drain: '管道疏通',
  electric: '电路维修',
  electrician: '电工维修',
  power: '强弱电维修',
  outsource: '外包服务',
  cooperation: '合作服务'
}

export function translateWorkerText(value) {
  if (value === null || value === undefined) return ''
  return String(value)
    .split(',')
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part) => WORKER_TEXT_MAP[part] || WORKER_TEXT_MAP[part.toLowerCase()] || part)
    .filter((part, index, arr) => arr.indexOf(part) === index)
    .join('，')
}

export function workerDisplayName(worker) {
  const id = Number(worker?.id)
  const rawName = String(worker?.nickname || worker?.name || '').trim()
  if (WORKER_ALIASES[id]) return WORKER_ALIASES[id]
  if (!rawName) return id ? `维修员${id}` : '维修员'
  if (/[\u4e00-\u9fa5]/.test(rawName)) return rawName

  const matchers = [
    [/^HK-Clean-(\d+)$/i, '家政保洁员'],
    [/^Repair-Plumber-(\d+)$/i, '水工维修员'],
    [/^Repair-Electric-(\d+)$/i, '电工维修员'],
    [/^Repair-Appliance-(\d+)$/i, '家电维修员'],
    [/^Repair-Outsource-(\d+)$/i, '外包维修团队']
  ]
  for (const [pattern, prefix] of matchers) {
    const match = rawName.match(pattern)
    if (match) return `${prefix}${match[1]}`
  }
  if (/^Worker-A$/i.test(rawName)) return '维修员A'
  return translateWorkerText(rawName) || `维修员${id || ''}`.trim()
}

export function workerTradeText(worker) {
  const id = Number(worker?.id)
  if (WORKER_TRADES[id]) return WORKER_TRADES[id]

  const typeMap = {
    1: '家政',
    2: '水工',
    3: '电工',
    4: '家电维修',
    5: '综合维修'
  }
  return typeMap[Number(worker?.staffType)] || '维修'
}

export function workerOptionLabel(worker) {
  if (!worker) return '维修员'
  return `${workerDisplayName(worker)}-${workerTradeText(worker)}`
}
