<template>
  <div class="dashboard-content">
    <section class="metrics-grid">
      <article class="metric-card card">
        <p class="metric-label">业主总数</p>
        <h3 class="metric-value">{{ formatNumber(ownerTotal) }}</h3>
        <p class="metric-note positive">较上月增长 {{ ownerGrowth }}%</p>
      </article>

      <article class="metric-card card">
        <p class="metric-label">本月物业费实收</p>
        <h3 class="metric-value">¥{{ formatNumber(feeIncome) }}</h3>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: `${feeCompletion}%` }"></div>
        </div>
        <p class="metric-note">当前完成率 {{ feeCompletion }}%</p>
      </article>

      <article class="metric-card card">
        <p class="metric-label">待处理报修工单</p>
        <h3 class="metric-value">{{ repairWaiting }}</h3>
        <p class="metric-note warning">紧急工单 {{ repairUrgent }}</p>
      </article>

      <article class="metric-card card">
        <p class="metric-label">设备在线率</p>
        <h3 class="metric-value">{{ onlineRate.toFixed(1) }}%</h3>
        <p class="metric-note positive">实时计算</p>
      </article>
    </section>

    <section class="charts-grid">
      <article class="panel panel-large card">
        <div class="panel-header">
          <h4>报修趋势（近 7 天）</h4>
        </div>
        <div class="line-chart-wrap">
          <svg class="line-chart" viewBox="0 0 640 260" preserveAspectRatio="none">
            <line
              v-for="tick in gridTicks"
              :key="`grid-${tick}`"
              :x1="chartPaddingX"
              :x2="640 - chartPaddingX"
              :y1="getY(tick)"
              :y2="getY(tick)"
              class="grid-line"
            />
            <polyline :points="newRepairPoints" class="line-blue" />
            <polyline :points="finishedRepairPoints" class="line-green" />
            <circle v-for="(dot, idx) in newRepairDots" :key="`new-${idx}`" :cx="dot.x" :cy="dot.y" r="3.5" class="dot-blue" />
            <circle v-for="(dot, idx) in finishedRepairDots" :key="`done-${idx}`" :cx="dot.x" :cy="dot.y" r="3.5" class="dot-green" />
          </svg>
          <div class="x-axis">
            <span v-for="label in trendLabels" :key="label">{{ label }}</span>
          </div>
          <div class="legend">
            <span><i class="legend-dot blue"></i>新增报修</span>
            <span><i class="legend-dot green"></i>完成处理</span>
          </div>
        </div>
      </article>

      <article class="panel card">
        <h4 class="panel-title">收缴结构统计</h4>
        <div class="pie-wrap">
          <div class="donut" :style="{ backgroundImage: pieGradient }">
            <div class="donut-inner"></div>
          </div>
        </div>
        <div class="fee-legend">
          <div v-for="item in feeSegments" :key="item.name" class="fee-item">
            <div class="fee-left">
              <i :style="{ backgroundColor: item.color }"></i>
              <span>{{ item.name }}</span>
            </div>
            <span>{{ item.percent }}%</span>
          </div>
        </div>
      </article>
    </section>

    <section class="bottom-grid">
      <article class="panel card">
        <div class="panel-header">
          <h4>社区在线动态</h4>
          <span class="active-visitors">当前活跃访客: {{ activeVisitors }}</span>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else class="live-list">
          <div
            v-for="(item, index) in liveActivities"
            :key="`${item.text}-${item.time}-${index}`"
            class="live-item"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <span class="live-dot" :class="item.type"></span>
            <div class="live-body">
              <p>{{ item.text }}</p>
              <span>{{ item.time }}</span>
            </div>
          </div>
          <div v-if="!liveActivities.length" class="news-empty">暂无在线动态</div>
        </div>
      </article>

      <article class="panel card">
        <div class="panel-header">
          <h4>最新社区公告</h4>
          <router-link class="panel-link" to="/notice/manage">查看全部</router-link>
        </div>
        <el-skeleton v-if="loading" :rows="4" animated />
        <div v-else class="news-list">
          <div
            v-for="(item, index) in notices"
            :key="`${item.title}-${item.time}`"
            class="news-item"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <div class="news-top">
              <span class="news-title">{{ item.title }}</span>
              <span class="news-time">{{ item.time }}</span>
            </div>
            <p>{{ item.summary }}</p>
          </div>
          <div v-if="!notices.length" class="news-empty">暂无公告</div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getOverview } from '@/api/statistics'

const overview = ref({})
const loading = ref(true)

const trendLabels = computed(() => {
  const labels = overview.value.trendLabels
  return Array.isArray(labels) && labels.length ? labels : ['--', '--', '--', '--', '--', '--', '--']
})
const newRepairs = computed(() => {
  const values = overview.value.newRepairs
  return Array.isArray(values) && values.length ? values.map((v) => Number(v || 0)) : [0, 0, 0, 0, 0, 0, 0]
})
const finishedRepairs = computed(() => {
  const values = overview.value.finishedRepairs
  return Array.isArray(values) && values.length ? values.map((v) => Number(v || 0)) : [0, 0, 0, 0, 0, 0, 0]
})
const feeSegments = computed(() => {
  const segments = overview.value.feeSegments
  if (Array.isArray(segments) && segments.length) {
    return segments.map((item) => ({
      name: item.name || '--',
      percent: Number(item.percent || 0),
      color: item.color || '#3b82f6'
    }))
  }
  return [
    { name: '已缴费', percent: 0, color: '#3b82f6' },
    { name: '待缴费', percent: 0, color: '#10b981' },
    { name: '逾期未缴', percent: 0, color: '#f59e0b' }
  ]
})
const notices = computed(() => (Array.isArray(overview.value.notices) ? overview.value.notices : []))
const liveActivities = computed(() => {
  const list = overview.value.liveActivities
  if (Array.isArray(list) && list.length) {
    return list
  }
  return notices.value.slice(0, 3).map((item) => ({
    text: `物业发布了社区公告《${item.title || '最新通知'}》`,
    time: item.time || '--',
    type: 'notice'
  }))
})

const ownerTotal = computed(() => Number(overview.value.ownerTotal ?? 0))
const ownerGrowth = computed(() => Number(overview.value.ownerGrowthRate ?? 0))
const feeIncome = computed(() => Number(overview.value.feeIncome ?? 0))
const feeCompletion = computed(() => Number(overview.value.feeCompletionRate ?? 0))
const repairWaiting = computed(() => Number(overview.value.repairWaiting ?? 0))
const repairUrgent = computed(() => Number(overview.value.repairUrgent ?? 0))
const onlineRate = computed(() => Number(overview.value.deviceOnlineRate ?? 0))
const activeVisitors = computed(() => Number(overview.value.activeVisitors ?? 0))

const chartPaddingX = 36
const chartPaddingY = 18
const chartWidth = 640
const chartHeight = 260

const maxTrend = computed(() => Math.max(...newRepairs.value, ...finishedRepairs.value, 1))
const gridTicks = computed(() => {
  const max = maxTrend.value
  return [0, 1, 2, 3, 4].map((idx) => (max / 4) * idx).reverse()
})

const newRepairPoints = computed(() => toPolyline(newRepairs.value))
const finishedRepairPoints = computed(() => toPolyline(finishedRepairs.value))
const newRepairDots = computed(() => toDots(newRepairs.value))
const finishedRepairDots = computed(() => toDots(finishedRepairs.value))

const pieGradient = computed(() => {
  let offset = 0
  const parts = feeSegments.value.map((item) => {
    const start = offset
    offset += Number(item.percent || 0)
    return `${item.color} ${start}% ${offset}%`
  })
  return `conic-gradient(${parts.join(', ')})`
})

function formatNumber(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function getX(index, total) {
  const usableWidth = chartWidth - chartPaddingX * 2
  if (total <= 1) return chartPaddingX
  return chartPaddingX + (usableWidth * index) / (total - 1)
}

function getY(value) {
  const usableHeight = chartHeight - chartPaddingY * 2
  return chartPaddingY + ((maxTrend.value - value) / maxTrend.value) * usableHeight
}

function toPolyline(data) {
  if (!Array.isArray(data) || !data.length) return ''
  return data.map((value, index) => `${getX(index, data.length)},${getY(value)}`).join(' ')
}

function toDots(data) {
  if (!Array.isArray(data) || !data.length) return []
  return data.map((value, index) => ({ x: getX(index, data.length), y: getY(value) }))
}

async function loadOverview() {
  loading.value = true
  try {
    const res = await getOverview()
    overview.value = res.data || {}
  } catch {
    overview.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.dashboard-content {
  display: grid;
  gap: 16px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card,
.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.metric-card {
  padding: 18px;
}

.metric-label {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.metric-value {
  margin: 9px 0 0;
  font-size: 28px;
  line-height: 1.1;
}

.metric-note {
  margin: 8px 0 0;
  font-size: 12px;
  color: #64748b;
}

.metric-note.positive {
  color: #059669;
}

.metric-note.warning {
  color: #d97706;
}

.progress-track {
  height: 8px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
  margin-top: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
}

.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.panel {
  padding: 16px 18px;
}

.panel-title {
  margin: 0 0 14px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-header h4 {
  margin: 0;
  font-size: 16px;
}

.panel-link {
  color: #2563eb;
  font-size: 13px;
  text-decoration: none;
}

.panel-link:hover {
  text-decoration: underline;
}

.line-chart {
  width: 100%;
  height: 260px;
  display: block;
}

.grid-line {
  stroke: #e2e8f0;
  stroke-width: 1;
}

.line-blue,
.line-green {
  fill: none;
  stroke-width: 2.6;
}

.line-blue {
  stroke: #3b82f6;
}

.line-green {
  stroke: #10b981;
}

.dot-blue {
  fill: #3b82f6;
}

.dot-green {
  fill: #10b981;
}

.x-axis {
  margin-top: 6px;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  color: #64748b;
  font-size: 12px;
}

.x-axis span {
  text-align: center;
}

.legend {
  margin-top: 8px;
  display: flex;
  gap: 14px;
  color: #475569;
  font-size: 12px;
}

.legend span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.legend-dot.blue {
  background: #3b82f6;
}

.legend-dot.green {
  background: #10b981;
}

.pie-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}

.donut {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  display: grid;
  place-items: center;
}

.donut-inner {
  width: 95px;
  height: 95px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.fee-legend {
  display: grid;
  gap: 10px;
  font-size: 14px;
}

.fee-item,
.fee-left,
.news-top,
.live-item {
  display: flex;
  align-items: center;
}

.fee-item,
.news-top {
  justify-content: space-between;
}

.fee-left {
  gap: 8px;
}

.fee-left i {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.live-list,
.news-list {
  border-top: 1px solid #f1f5f9;
}

.live-item,
.news-item {
  animation: listFadeIn 0.25s ease both;
}

.live-item {
  gap: 10px;
  min-height: 54px;
  padding: 12px 0;
  border-bottom: 1px solid #f8fafc;
}

.live-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #2563eb;
  flex: 0 0 auto;
}

.live-dot.second-hand {
  background: #f59e0b;
}

.live-dot.notice {
  background: #10b981;
}

.live-dot.visitor {
  background: #6366f1;
}

.live-body p {
  margin: 0;
  color: #1e293b;
  font-size: 14px;
  line-height: 1.5;
}

.live-body span,
.active-visitors {
  color: #64748b;
  font-size: 12px;
}

.news-item {
  padding: 12px 0;
  border-bottom: 1px solid #f8fafc;
}

.news-empty {
  padding: 12px 0;
  color: #64748b;
  font-size: 13px;
}

.news-item:last-child,
.live-item:last-child {
  border-bottom: none;
}

.news-title {
  font-weight: 600;
  font-size: 14px;
}

.news-time {
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
}

.news-item p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

@keyframes listFadeIn {
  from {
    opacity: 0;
    transform: scale(0.98);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@media (max-width: 1400px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .charts-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .dashboard-content {
    gap: 12px;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
