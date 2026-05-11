<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>工单管理</h3>
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索工单ID/业主/描述" clearable @keyup.enter="search" @clear="search" />
          <el-select v-model="priorityFilter" clearable placeholder="优先级" style="width: 150px" @change="search">
            <el-option :value="1" label="紧急" />
            <el-option :value="2" label="普通" />
            <el-option :value="3" label="低" />
          </el-select>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="id" label="工单ID" width="96" />
        <el-table-column label="业主姓名" min-width="130">
          <template #default="{ row }">{{ ownerName(row) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
        <el-table-column label="优先级" width="110">
          <template #default="{ row }">
            <el-tag :type="priorityMeta(row.priority).type">{{ priorityMeta(row.priority).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="派单时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.assignedTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="工单详情" width="720px" destroy-on-close>
      <el-descriptions v-if="detailOrder" :column="2" border>
        <el-descriptions-item label="工单ID">{{ detailOrder.id }}</el-descriptions-item>
        <el-descriptions-item label="业主">{{ ownerName(detailOrder) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailOrder.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityMeta(detailOrder.priority).type">{{ priorityMeta(detailOrder.priority).text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMeta(detailOrder.status).type">{{ statusMeta(detailOrder.status).text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="派单时间">{{ formatTime(detailOrder.assignedTime) }}</el-descriptions-item>
        <el-descriptions-item label="系统推荐维修员" :span="2">
          {{ suggestedWorkerName(detailOrder) }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailOrder.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :loading="assignLoading"
          :disabled="!detailOrder || Number(detailOrder.status) !== 1"
          @click="handleAutoAssign(detailOrder)"
        >
          一键派单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { autoAssignWorkOrder, getWorkOrderDetail, getWorkOrders } from '@/api/adminOps'

const loading = ref(false)
const assignLoading = ref(false)
const keyword = ref('')
const priorityFilter = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])
const detailVisible = ref(false)
const detailOrder = ref(null)

function ownerName(row) {
  if (row && typeof row === 'object') {
    return row.ownerName || `业主${row.userId || '-'}`
  }
  return `业主${row || '-'}`
}

function suggestedWorkerName(row) {
  if (row && typeof row === 'object') {
    if (!row.suggestedWorkerId) return '暂无推荐'
    return row.suggestedWorkerName || `ID:${row.suggestedWorkerId}`
  }
  if (!row) return '暂无推荐'
  return `ID:${row}`
}

function priorityMeta(value) {
  const map = {
    1: { text: '紧急', type: 'danger' },
    2: { text: '普通', type: 'warning' },
    3: { text: '低', type: 'info' }
  }
  return map[Number(value)] || { text: '未分级', type: 'info' }
}

function statusMeta(value) {
  const map = {
    1: { text: '待派单', type: 'warning' },
    2: { text: '处理中', type: 'primary' },
    3: { text: '待评价', type: 'success' },
    4: { text: '已完成', type: 'info' },
    5: { text: '已取消', type: 'info' }
  }
  return map[Number(value)] || { text: '未知', type: 'info' }
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function search() {
  pageNum.value = 1
  loadData()
}

function handleSizeChange() {
  pageNum.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const orderRes = await getWorkOrders({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      priority: priorityFilter.value || undefined,
      keyword: keyword.value.trim() || undefined
    })
    rows.value = Array.isArray(orderRes.data?.records) ? orderRes.data.records : []
    total.value = Number(orderRes.data?.total || 0)
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  const res = await getWorkOrderDetail(row.id)
  detailOrder.value = {
    ...row,
    ...(res.data?.order || res.data || {}),
    ownerName: res.data?.ownerName || row.ownerName,
    assigneeName: res.data?.assigneeName || row.assigneeName,
    suggestedWorkerName: res.data?.suggestedWorkerName || row.suggestedWorkerName
  }
  detailVisible.value = true
}

async function handleAutoAssign(row) {
  if (!row?.id) return
  assignLoading.value = true
  try {
    await autoAssignWorkOrder(row.id)
    ElMessage.success('派单成功')
    await loadData()
    await openDetail(row)
  } finally {
    assignLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.panel-header {
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters .el-input {
  width: 260px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
