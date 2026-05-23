<template>
  <div class="page-grid">
    <section class="toolbar-card card">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索工单ID/类型/描述"
          clearable
          style="width: 280px"
          @keyup.enter="searchData"
          @clear="searchData"
        />
        <el-select v-model="statusFilter" clearable placeholder="全部状态" style="width: 130px" @change="searchData">
          <el-option label="待派单" :value="1" />
          <el-option label="处理中" :value="2" />
          <el-option label="待评价" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
        <el-select v-model="priorityFilter" clearable placeholder="全部优先级" style="width: 130px" @change="searchData">
          <el-option label="紧急" :value="1" />
          <el-option label="普通" :value="2" />
          <el-option label="低" :value="3" />
        </el-select>
        <el-checkbox v-model="overdueOnly" @change="searchData">仅看超时</el-checkbox>
        <el-button @click="loadData">刷新</el-button>
        <el-button @click="resetFilters">重置筛选</el-button>
        <el-button type="warning" :loading="scanLoading" @click="handleScanSla">SLA扫描</el-button>
      </div>
    </section>

    <section class="panel card">
      <div class="panel-header">
        <div>
          <h3>报修管理</h3>
          <span class="hint">工单列表、智能优先级、推荐维修员、派单和状态流转统一在此处理</span>
        </div>
      </div>

      <el-skeleton v-if="loading && !rows.length" :rows="5" animated />

      <div v-else-if="rows.length" class="work-order-preview">
        <article
          v-for="(row, index) in rows.slice(0, 4)"
          :key="row.id"
          class="work-order-card card fade-list-item"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="openDetail(row.id)"
        >
          <div class="work-order-top">
            <span class="order-id">#{{ row.id }}</span>
            <el-tag :type="priorityMeta(row.priority).type">{{ row.priorityText || priorityMeta(row.priority).text }}</el-tag>
          </div>
          <div class="order-title">{{ row.category || '维修工单' }}</div>
          <p>{{ row.description || '暂无问题描述' }}</p>
          <div class="order-meta">
            <span>{{ row.ownerName || '未知业主' }}</span>
            <span>{{ row.suggestedWorkerName || '待推荐维修员' }}</span>
            <span>{{ formatTime(row.slaDeadline) }}</span>
          </div>
        </article>
      </div>

      <el-table
        :data="rows"
        stripe
        v-loading="loading && rows.length > 0"
        empty-text="暂无匹配工单，请检查筛选条件或稍后重试"
      >
        <el-table-column prop="id" label="工单ID" width="88" />
        <el-table-column prop="ownerName" label="业主" min-width="120" />
        <el-table-column prop="category" label="类型" min-width="110" />
        <el-table-column prop="description" label="问题描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="优先级" width="112">
          <template #default="{ row }">
            <el-tag :type="priorityMeta(row.priority).type">{{ row.priorityText || priorityMeta(row.priority).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐维修员" min-width="140">
          <template #default="{ row }">{{ row.suggestedWorkerName || '--' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type">{{ row.statusText || statusMeta(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="SLA" min-width="180">
          <template #default="{ row }">
            <div class="sla-cell">
              <span>{{ formatTime(row.slaDeadline) }}</span>
              <el-tag v-if="row.slaOverdue" type="danger">超时 {{ row.overdueMinutes }} 分钟</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row.id)">详情</el-button>
            <el-button
              type="primary"
              link
              :disabled="Number(row.status) === 4 || Number(row.status) === 5"
              @click="openDispatch(row)"
            >
              派单
            </el-button>
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
          @size-change="loadData"
        />
      </div>
    </section>

    <el-dialog v-model="dispatchVisible" title="派单处理" width="560px">
      <el-form label-width="108px">
        <el-form-item label="推荐维修员">
          <span>{{ currentRow?.suggestedWorkerName || '暂无推荐' }}</span>
        </el-form-item>
        <el-form-item label="选择维修员">
          <el-select v-model="dispatchForm.assigneeIds" multiple collapse-tags style="width: 100%">
            <el-option
              v-for="worker in workers"
              :key="worker.id"
              :label="worker.nickname || worker.account"
              :value="worker.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dispatchForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatchLoading" @click="submitDispatch">确认派单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="860px">
      <div v-if="detail" class="detail-grid">
        <section class="detail-card">
          <div class="detail-title">工单信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工单ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="业主">{{ detail.ownerName }}</el-descriptions-item>
            <el-descriptions-item label="主类">{{ detail.serviceMajor || '--' }}</el-descriptions-item>
            <el-descriptions-item label="子类">{{ detail.serviceSubType || '--' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
            <el-descriptions-item label="优先级">{{ detail.priorityText }}</el-descriptions-item>
            <el-descriptions-item label="推荐维修员">{{ detail.suggestedWorkerName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="当前负责人">{{ detail.assigneeName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ detail.description || '--' }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="detail-card">
          <div class="detail-title">状态流转</div>
          <div class="status-actions">
            <el-button
              v-for="option in detail.nextStatuses || []"
              :key="option.status"
              type="primary"
              plain
              :loading="statusLoading"
              @click="changeStatus(option.status)"
            >
              {{ option.label }}
            </el-button>
          </div>
          <div class="detail-title detail-subtitle">执行人员</div>
          <el-table :data="detail.participants || []" size="small" stripe>
            <el-table-column prop="workerName" label="维修员" min-width="120" />
            <el-table-column label="角色" min-width="100">
              <template #default="{ row }">{{ Number(row.roleType) === 1 ? '主负责人' : '协作人' }}</template>
            </el-table-column>
            <el-table-column label="核验" min-width="90">
              <template #default="{ row }">{{ Number(row.verifyPassed) === 1 ? '已通过' : '未通过' }}</template>
            </el-table-column>
            <el-table-column label="完工" min-width="90">
              <template #default="{ row }">{{ Number(row.finishConfirmed) === 1 ? '已确认' : '未确认' }}</template>
            </el-table-column>
          </el-table>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  dispatchSmartWorkOrder,
  getSmartWorkOrderDetail,
  getSmartWorkOrders,
  scanSmartWorkOrderSla,
  updateSmartWorkOrderStatus
} from '@/api/smartWorkOrder'
import { getWorkerList } from '@/api/worker'

const loading = ref(false)
const dispatchLoading = ref(false)
const statusLoading = ref(false)
const scanLoading = ref(false)
const rows = ref([])
const workers = ref([])
const keyword = ref('')
const statusFilter = ref(null)
const priorityFilter = ref(null)
const overdueOnly = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const dispatchVisible = ref(false)
const detail = ref(null)
const currentRow = ref(null)
const dispatchForm = ref({ assigneeIds: [], remark: '' })

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
    5: { text: '已取消', type: 'danger' }
  }
  return map[Number(value)] || { text: '未知', type: 'info' }
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadWorkers() {
  try {
    const res = await getWorkerList()
    workers.value = Array.isArray(res.data) ? res.data.filter((item) => Number(item.role) === 3) : []
  } catch (error) {
    ElMessage.error(error?.message || '维修员列表加载失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSmartWorkOrders({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined,
      priority: priorityFilter.value || undefined,
      keyword: keyword.value.trim() || undefined,
      overdueOnly: overdueOnly.value || undefined
    })
    rows.value = Array.isArray(res.data?.records) ? res.data.records : []
    total.value = Number(res.data?.total || 0)
  } catch (error) {
    rows.value = []
    total.value = 0
    ElMessage.error(error?.message || '工单列表加载失败')
  } finally {
    loading.value = false
  }
}

function searchData() {
  pageNum.value = 1
  loadData()
}

function resetFilters() {
  keyword.value = ''
  statusFilter.value = null
  priorityFilter.value = null
  overdueOnly.value = false
  searchData()
}

async function openDetail(id) {
  try {
    const res = await getSmartWorkOrderDetail(id)
    detail.value = {
      ...(res.data?.order || {}),
      ownerName: res.data?.ownerName,
      assigneeName: res.data?.assigneeName,
      suggestedWorkerName: res.data?.suggestedWorkerName,
      statusText: res.data?.statusText,
      priorityText: res.data?.priorityText,
      participants: res.data?.participants || [],
      nextStatuses: res.data?.nextStatuses || []
    }
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error?.message || '工单详情加载失败')
  }
}

function openDispatch(row) {
  currentRow.value = row
  dispatchForm.value = {
    assigneeIds: row.suggestedWorkerId ? [row.suggestedWorkerId] : [],
    remark: ''
  }
  dispatchVisible.value = true
}

async function submitDispatch() {
  if (!currentRow.value?.id) return
  dispatchLoading.value = true
  try {
    await dispatchSmartWorkOrder(currentRow.value.id, dispatchForm.value)
    ElMessage.success('派单成功')
    dispatchVisible.value = false
    await loadData()
    await openDetail(currentRow.value.id)
  } catch (error) {
    ElMessage.error(error?.message || '派单失败')
  } finally {
    dispatchLoading.value = false
  }
}

async function changeStatus(status) {
  if (!detail.value?.id) return
  statusLoading.value = true
  try {
    await updateSmartWorkOrderStatus(detail.value.id, { status })
    ElMessage.success('状态已更新')
    await loadData()
    await openDetail(detail.value.id)
  } catch (error) {
    ElMessage.error(error?.message || '状态更新失败')
  } finally {
    statusLoading.value = false
  }
}

async function handleScanSla() {
  scanLoading.value = true
  try {
    const res = await scanSmartWorkOrderSla()
    ElMessage.success(`已扫描，触发 ${res.data?.delayCount || 0} 条超时催办`)
    await loadData()
  } catch (error) {
    ElMessage.error(error?.message || 'SLA扫描失败')
  } finally {
    scanLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadWorkers(), loadData()])
})
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
}

.toolbar-card,
.panel,
.detail-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.toolbar-card,
.panel {
  padding: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header h3,
.detail-title {
  margin: 0;
  font-size: 16px;
}

.hint {
  display: inline-block;
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.work-order-preview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.work-order-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  background: #f8fafc;
  animation: listFadeIn 0.25s ease both;
  cursor: pointer;
}

.work-order-top,
.order-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.order-id {
  color: #64748b;
  font-size: 12px;
}

.order-title {
  margin-top: 10px;
  color: #0f172a;
  font-weight: 700;
}

.work-order-card p {
  height: 42px;
  margin: 8px 0 12px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  overflow: hidden;
}

.order-meta {
  color: #64748b;
  font-size: 12px;
}

.sla-cell {
  display: grid;
  gap: 6px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.detail-grid {
  display: grid;
  gap: 16px;
}

.detail-card {
  padding: 16px;
}

.detail-subtitle {
  margin-top: 16px;
}

.status-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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
  .work-order-preview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .work-order-preview {
    grid-template-columns: 1fr;
  }
}
</style>
