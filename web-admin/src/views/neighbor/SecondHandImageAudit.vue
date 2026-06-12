<template>
  <div class="audit-page">
    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>二手商品图片审核</h3>
        </div>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索商品标题" clearable style="width: 220px" @keyup.enter="loadData" />
          <el-select v-model="statusFilter" clearable placeholder="商品状态" style="width: 150px" @change="loadData">
            <el-option label="待审核未发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已售出" :value="2" />
            <el-option label="已下架" :value="3" />
            <el-option label="审核未通过" :value="4" />
          </el-select>
          <el-select v-model="auditFilter" clearable placeholder="审核结果" style="width: 150px" @change="loadData">
            <el-option label="待人工审核" value="MANUAL_REVIEW" />
            <el-option label="图片存疑" value="SUSPICIOUS" />
            <el-option label="通过" value="PASS" />
          </el-select>
          <el-button @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table :data="filteredItems" stripe v-loading="loading">
        <el-table-column label="图片" width="96">
          <template #default="{ row }">
            <el-image
              v-if="coverOf(row)"
              class="cover"
              :src="coverOf(row)"
              :preview-src-list="imageUrls(row)"
              preview-teleported
              fit="cover"
            >
              <template #error>
                <div class="image-error">加载失败</div>
              </template>
            </el-image>
            <span v-else class="muted">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品" min-width="180" show-overflow-tooltip />
        <el-table-column label="商品状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模型结果" width="130">
          <template #default="{ row }">
            <el-tag :type="auditTag(row.imageAuditStatus)">{{ auditText(row.imageAuditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最高 fake 概率" width="140">
          <template #default="{ row }">{{ percent(row.maxFakeProbability) }}</template>
        </el-table-column>
        <el-table-column label="风险" width="100">
          <template #default="{ row }">
            <el-tag :type="riskTag(row.imageRiskLevel)">{{ riskText(row.imageRiskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ formatTime(row.updateTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openResults(row)">查看结果</el-button>
            <el-button type="success" link :disabled="Number(row.status) === 1" @click="approve(row)">通过</el-button>
            <el-button type="danger" link :disabled="Number(row.status) === 4" @click="reject(row)">不通过</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          layout="total, prev, pager, next, sizes"
          :total="total"
          :page-sizes="[10, 20, 50]"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </section>

    <el-drawer v-model="drawerVisible" title="图片检测结果" size="640px">
      <div v-if="currentItem" class="drawer-body">
        <h4>{{ currentItem.title }}</h4>
        <p class="muted">商品状态：{{ statusText(currentItem.status) }}，模型结果：{{ auditText(currentItem.imageAuditStatus) }}</p>
        <div v-if="auditRows(currentItem).length" class="result-list">
          <div v-for="row in auditRows(currentItem)" :key="row.id || row.imageUrl" class="result-card">
            <el-image
              v-if="assetUrl(row.imageUrl)"
              class="result-image"
              :src="assetUrl(row.imageUrl)"
              :preview-src-list="[assetUrl(row.imageUrl)]"
              fit="cover"
            >
              <template #error>
                <div class="image-error">加载失败</div>
              </template>
            </el-image>
            <div v-else class="result-image image-error">无图</div>
            <div class="result-info">
              <div class="result-line">
                <span>标签</span>
                <strong>{{ row.detectLabel || '--' }}</strong>
              </div>
              <div class="result-line">
                <span>审核</span>
                <el-tag :type="auditTag(row.auditStatus)">{{ auditText(row.auditStatus) }}</el-tag>
              </div>
              <div class="result-line">
                <span>风险</span>
                <el-tag :type="riskTag(row.riskLevel)">{{ riskText(row.riskLevel) }}</el-tag>
              </div>
              <div class="result-line">
                <span>fake 概率</span>
                <strong>{{ percent(row.fakeProbability) }}</strong>
              </div>
              <div class="result-line">
                <span>real 概率</span>
                <strong>{{ percent(row.realProbability) }}</strong>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无图片检测结果" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveSecondHandImageAudit, getSecondHandList, rejectSecondHandImageAudit } from '@/api/neighbor'

const loading = ref(false)
const items = ref([])
const keyword = ref('')
const statusFilter = ref(0)
const auditFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const drawerVisible = ref(false)
const currentItem = ref(null)
const invalidImageValues = new Set(['FAILED', 'ERROR', 'DETECT_FAILED', 'NULL', 'UNDEFINED'])

const filteredItems = computed(() => {
  if (!auditFilter.value) return items.value
  return items.value.filter((item) => item.imageAuditStatus === auditFilter.value)
})

function baseAssetUrl() {
  const base = (import.meta.env.VITE_API_BASE_URL || '').trim().replace(/\/$/, '')
  if (!base || base === '/api') return window.location.origin
  return base.endsWith('/api') ? base.slice(0, -4) : base
}

// 后端保存的图片可能是相对路径，这里统一补成浏览器可以访问的完整地址。
function assetUrl(value) {
  const text = typeof value === 'string' ? value.trim() : ''
  if (!text || invalidImageValues.has(text.toUpperCase())) return ''
  if (/^(https?:|data:|blob:)/i.test(text)) return text
  if (text.startsWith('/files/') || text.startsWith('/api/')) return `${baseAssetUrl()}${text}`
  if (text.startsWith('files/') || text.startsWith('api/')) return `${baseAssetUrl()}/${text}`
  return text
}

// 兼容老数据：images字段可能是JSON数组，也可能是单个字符串。
function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return typeof images === 'string' ? [images] : []
  }
}

function imageUrls(row) {
  return parseImages(row.images).map(assetUrl).filter(Boolean)
}

function coverOf(row) {
  return imageUrls(row)[0] || ''
}

function auditRows(row) {
  return Array.isArray(row?.imageAuditResults) ? row.imageAuditResults : []
}

function statusText(status) {
  const map = {
    0: '待审核未发布',
    1: '已发布',
    2: '已售出',
    3: '已下架',
    4: '审核未通过'
  }
  return map[Number(status)] || '未知'
}

function statusTag(status) {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'info',
    4: 'danger'
  }
  return map[Number(status)] || 'info'
}

function auditText(status) {
  const map = {
    PASS: '通过',
    SUSPICIOUS: '图片存疑',
    MANUAL_REVIEW: '待人工审核'
  }
  return map[status] || '未检测'
}

function auditTag(status) {
  const map = {
    PASS: 'success',
    SUSPICIOUS: 'warning',
    MANUAL_REVIEW: 'danger'
  }
  return map[status] || 'info'
}

function riskText(level) {
  const map = { LOW: '低', MEDIUM: '中', HIGH: '高' }
  return map[level] || '未知'
}

function riskTag(level) {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[level] || 'info'
}

// 模型返回的是0到1之间的小数，后台页面展示成百分比更直观。
function percent(value) {
  if (value === null || value === undefined || value === '') return '--'
  const num = Number(value)
  if (Number.isNaN(num)) return '--'
  return `${(num * 100).toFixed(1)}%`
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSecondHandList({
      page: page.value,
      size: size.value,
      status: statusFilter.value,
      keyword: keyword.value.trim() || undefined
    })
    const data = res.data || {}
    items.value = Array.isArray(data.items) ? data.items : []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function openResults(row) {
  currentItem.value = row
  drawerVisible.value = true
}

async function approve(row) {
  await approveSecondHandImageAudit(row.id)
  ElMessage.success('已通过，商品已自动发布')
  await loadData()
}

async function reject(row) {
  await ElMessageBox.confirm('不通过后商品不会发布，业主需要重新上传商品图片。', '确认不通过', {
    type: 'warning'
  })
  await rejectSecondHandImageAudit(row.id)
  ElMessage.success('已驳回，等待业主重新上传')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.audit-page {
  display: grid;
  gap: 16px;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.panel-header,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-header {
  margin-bottom: 14px;
}

.panel-header h3,
.drawer-body h4 {
  margin: 0;
}

.muted {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.cover {
  width: 58px;
  height: 58px;
  border-radius: 8px;
  background: #f1f5f9;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.drawer-body {
  display: grid;
  gap: 14px;
}

.result-list {
  display: grid;
  gap: 12px;
}

.result-card {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 14px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.result-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  background: #f1f5f9;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 12px;
  background: #f1f5f9;
}

.result-info {
  display: grid;
  align-content: center;
  gap: 8px;
}

.result-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #334155;
  font-size: 13px;
}
</style>
