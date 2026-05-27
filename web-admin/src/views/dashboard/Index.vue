<template>
  <div class="dashboard-content">
    <section class="metrics-grid">
      <article class="metric-card">
        <p class="metric-label">业主总数</p>
        <h3 class="metric-value">{{ formatNumber(ownerTotal) }}</h3>
        <p class="metric-note positive">较上月增长 {{ ownerGrowth }}%</p>
      </article>

      <article class="metric-card">
        <p class="metric-label">本月物业费实收</p>
        <h3 class="metric-value">￥{{ formatNumber(feeIncome) }}</h3>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: `${feeCompletion}%` }"></div>
        </div>
        <p class="metric-note">当前完成率 {{ feeCompletion }}%</p>
      </article>

      <article class="metric-card">
        <p class="metric-label">待处理报修工单</p>
        <h3 class="metric-value">{{ repairWaiting }}</h3>
        <p class="metric-note warning">紧急工单 {{ repairUrgent }}</p>
      </article>

      <article class="metric-card">
        <p class="metric-label">设备在线率</p>
        <h3 class="metric-value">{{ onlineRate.toFixed(1) }}%</h3>
        <p class="metric-note positive">实时计算</p>
      </article>

      <article class="metric-card">
        <p class="metric-label">待取件快递</p>
        <h3 class="metric-value">{{ pendingExpress }}</h3>
        <p class="metric-note">今日新增 {{ todayExpress }}</p>
      </article>

      <article class="metric-card">
        <p class="metric-label">维护中设施</p>
        <h3 class="metric-value">{{ facilityMaintenance }}</h3>
        <p class="metric-note warning">停用设施 {{ facilityDisabled }}</p>
      </article>
    </section>

    <section class="panel-grid">
      <article class="panel">
        <div class="panel-header">
          <h4>近 7 天报修趋势</h4>
        </div>
        <div class="trend-table">
          <div class="trend-row trend-head">
            <span>日期</span>
            <span>新增</span>
            <span>完成</span>
          </div>
          <div v-for="(label, index) in trendLabels" :key="label" class="trend-row">
            <span>{{ label }}</span>
            <span>{{ newRepairs[index] || 0 }}</span>
            <span>{{ finishedRepairs[index] || 0 }}</span>
          </div>
        </div>
      </article>

      <article class="panel">
        <div class="panel-header">
          <h4>收缴结构统计</h4>
        </div>
        <div class="segment-list">
          <div v-for="item in feeSegments" :key="item.name" class="segment-item">
            <div class="segment-left">
              <i :style="{ backgroundColor: item.color }"></i>
              <span>{{ item.name }}</span>
            </div>
            <strong>{{ item.percent }}%</strong>
          </div>
        </div>
      </article>
    </section>

    <section class="panel-grid">
      <article class="panel">
        <div class="panel-header">
          <h4>社区在线动态</h4>
          <span class="panel-note">当前活跃访客 {{ activeVisitors }}</span>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else class="news-list">
          <div
            v-for="(item, index) in liveActivities"
            :key="`${item.text}-${item.time}-${index}`"
            class="news-item is-clickable"
            role="button"
            tabindex="0"
            @click="openActivity(item)"
            @keyup.enter="openActivity(item)"
          >
            <div class="news-top">
              <span class="news-title">{{ item.text }}</span>
              <span class="news-time">{{ item.time }}</span>
            </div>
          </div>
          <div v-if="!liveActivities.length" class="news-empty">暂无在线动态</div>
        </div>
      </article>

      <article class="panel">
        <div class="panel-header">
          <h4>最新社区公告</h4>
          <router-link class="panel-link" to="/notice/manage">查看全部</router-link>
        </div>
        <el-skeleton v-if="loading" :rows="4" animated />
        <div v-else class="news-list">
          <div v-for="item in notices" :key="`${item.title}-${item.time}`" class="news-item">
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

    <el-drawer v-model="activityDrawerVisible" title="社区动态详情" size="420px">
      <div v-if="selectedActivity" class="activity-detail">
        <el-tag>{{ selectedActivity.typeName || typeName(selectedActivity.type) }}</el-tag>
        <h3>{{ selectedActivity.title || selectedActivity.text }}</h3>
        <p class="activity-time">{{ selectedActivity.time }}</p>
        <p class="activity-text">{{ selectedActivity.detail || selectedActivity.text }}</p>

        <dl class="activity-meta">
          <template v-if="selectedActivity.publisher">
            <dt>发布人</dt>
            <dd>{{ selectedActivity.publisher }}</dd>
          </template>
          <template v-if="selectedActivity.community">
            <dt>小区</dt>
            <dd>{{ selectedActivity.community }}</dd>
          </template>
          <template v-if="selectedActivity.price !== undefined && selectedActivity.price !== null">
            <dt>价格</dt>
            <dd>￥{{ formatNumber(selectedActivity.price) }}</dd>
          </template>
          <template v-if="selectedActivity.contact">
            <dt>联系方式</dt>
            <dd>{{ selectedActivity.contact }}</dd>
          </template>
          <template v-if="selectedActivity.visitorName">
            <dt>访客</dt>
            <dd>{{ selectedActivity.visitorName }}</dd>
          </template>
          <template v-if="selectedActivity.visitorPhone">
            <dt>访客电话</dt>
            <dd>{{ selectedActivity.visitorPhone }}</dd>
          </template>
        </dl>

        <el-button
          v-if="selectedActivity.targetPath"
          type="primary"
          @click="goActivityTarget"
        >
          进入相关模块
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getOverview } from '@/api/statistics'

const router = useRouter()
const overview = ref({})
const loading = ref(true)
const activityDrawerVisible = ref(false)
const selectedActivity = ref(null)

const trendLabels = computed(() => {
  const labels = overview.value.trendLabels
  return Array.isArray(labels) && labels.length ? labels : ['--', '--', '--', '--', '--', '--', '--']
})
const newRepairs = computed(() => {
  const values = overview.value.newRepairs
  return Array.isArray(values) && values.length ? values.map((value) => Number(value || 0)) : [0, 0, 0, 0, 0, 0, 0]
})
const finishedRepairs = computed(() => {
  const values = overview.value.finishedRepairs
  return Array.isArray(values) && values.length ? values.map((value) => Number(value || 0)) : [0, 0, 0, 0, 0, 0, 0]
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
  return Array.isArray(list) ? list : []
})

const ownerTotal = computed(() => Number(overview.value.ownerTotal ?? 0))
const ownerGrowth = computed(() => Number(overview.value.ownerGrowthRate ?? 0))
const feeIncome = computed(() => Number(overview.value.feeIncome ?? 0))
const feeCompletion = computed(() => Number(overview.value.feeCompletionRate ?? 0))
const repairWaiting = computed(() => Number(overview.value.repairWaiting ?? 0))
const repairUrgent = computed(() => Number(overview.value.repairUrgent ?? 0))
const onlineRate = computed(() => Number(overview.value.deviceOnlineRate ?? 0))
const activeVisitors = computed(() => Number(overview.value.activeVisitors ?? 0))
const pendingExpress = computed(() => Number(overview.value.pendingExpress ?? 0))
const todayExpress = computed(() => Number(overview.value.todayExpress ?? 0))
const facilityMaintenance = computed(() => Number(overview.value.facilityMaintenance ?? 0))
const facilityDisabled = computed(() => Number(overview.value.facilityDisabled ?? 0))

function formatNumber(value) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: Number(value || 0) % 1 === 0 ? 0 : 2,
    maximumFractionDigits: 2
  })
}

function openActivity(item) {
  selectedActivity.value = item
  activityDrawerVisible.value = true
}

function goActivityTarget() {
  if (!selectedActivity.value?.targetPath) return
  activityDrawerVisible.value = false
  router.push(selectedActivity.value.targetPath)
}

function typeName(type) {
  const map = {
    notice: '社区公告',
    visitor: '访客通行',
    'second-hand': '二手物品'
  }
  return map[type] || '社区动态'
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-card,
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
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

.panel-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.panel {
  padding: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header h4 {
  margin: 0;
  font-size: 16px;
}

.panel-note,
.news-time {
  color: #64748b;
  font-size: 12px;
}

.panel-link {
  color: #2563eb;
  font-size: 13px;
  text-decoration: none;
}

.trend-table {
  display: grid;
  gap: 8px;
}

.trend-row {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr 0.8fr;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
}

.trend-head {
  color: #64748b;
  font-size: 12px;
}

.segment-list {
  display: grid;
  gap: 12px;
}

.segment-item,
.segment-left,
.news-top {
  display: flex;
  align-items: center;
}

.segment-item,
.news-top {
  justify-content: space-between;
}

.segment-left {
  gap: 8px;
}

.segment-left i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.news-list {
  border-top: 1px solid #f1f5f9;
}

.news-item {
  padding: 12px 0;
  border-bottom: 1px solid #f8fafc;
}

.news-item.is-clickable {
  cursor: pointer;
  border-radius: 6px;
  padding-left: 8px;
  padding-right: 8px;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

.news-item.is-clickable:hover,
.news-item.is-clickable:focus-visible {
  background: #f8fafc;
  border-color: #e2e8f0;
  outline: none;
}

.news-item:last-child {
  border-bottom: none;
}

.news-title {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
}

.news-item p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.news-empty {
  padding: 12px 0;
  color: #64748b;
  font-size: 13px;
}

.activity-detail {
  display: grid;
  gap: 12px;
}

.activity-detail h3 {
  margin: 0;
  font-size: 20px;
  line-height: 1.35;
  color: #0f172a;
}

.activity-time {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.activity-text {
  margin: 0;
  color: #334155;
  line-height: 1.7;
}

.activity-meta {
  display: grid;
  grid-template-columns: 82px minmax(0, 1fr);
  gap: 10px 12px;
  margin: 0;
  padding: 14px 0;
  border-top: 1px solid #eef2f7;
  border-bottom: 1px solid #eef2f7;
}

.activity-meta dt {
  color: #64748b;
}

.activity-meta dd {
  margin: 0;
  color: #0f172a;
  word-break: break-word;
}

@media (max-width: 1200px) {
  .metrics-grid,
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
