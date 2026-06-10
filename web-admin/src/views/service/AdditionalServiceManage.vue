<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>订单总数</p>
        <h3>{{ summary.total || 0 }}</h3>
      </article>
      <article class="summary-card">
        <p>待受理</p>
        <h3>{{ summary.reserved || 0 }}</h3>
      </article>
      <article class="summary-card">
        <p>服务中</p>
        <h3>{{ summary.accepted || 0 }}</h3>
      </article>
      <article class="summary-card">
        <p>积分收入</p>
        <h3>{{ summary.pointsCost || 0 }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>附加服务订单</h3>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索服务/联系人/电话/备注"
            clearable
            style="width: 260px"
            @keyup.enter="loadData"
            @clear="loadData"
          />
          <el-select v-model="statusFilter" clearable placeholder="订单状态" style="width: 140px" @change="loadData">
            <el-option label="已预约" :value="1" />
            <el-option label="已受理" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="84" />
        <el-table-column prop="serviceName" label="服务" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" min-width="110" />
        <el-table-column prop="contactPhone" label="电话" min-width="130" />
        <el-table-column prop="appointmentDate" label="预约日期" min-width="120" />
        <el-table-column prop="appointmentTimeSlot" label="时间段" min-width="120" />
        <el-table-column prop="pointsCost" label="扣除积分" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="Number(row.status) !== 1" @click="changeStatus(row, 2)">受理</el-button>
            <el-button link type="success" :disabled="Number(row.status) !== 2" @click="changeStatus(row, 3)">完成</el-button>
            <el-button link type="danger" :disabled="[3, 4].includes(Number(row.status))" @click="changeStatus(row, 4)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdditionalServiceOrders, updateAdditionalServiceOrderStatus } from '@/api/additionalService'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(undefined)
const summary = ref({})
const orders = ref([])

function statusText(status) {
  const map = {
    1: '已预约',
    2: '已受理',
    3: '已完成',
    4: '已取消'
  }
  return map[Number(status)] || '未知'
}

function statusTag(status) {
  const map = {
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'info'
  }
  return map[Number(status)] || 'info'
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdditionalServiceOrders({
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value || undefined,
      limit: 300
    })
    summary.value = res.data?.summary || {}
    orders.value = Array.isArray(res.data?.orders) ? res.data.orders : []
  } finally {
    loading.value = false
  }
}

async function changeStatus(row, status) {
  await ElMessageBox.confirm(`确认将订单 #${row.id} 更新为“${statusText(status)}”吗？`, '状态确认', { type: 'warning' })
  await updateAdditionalServiceOrderStatus(row.id, status)
  ElMessage.success('订单状态已更新')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card,
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.summary-card {
  padding: 18px;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 10px 0 0;
  font-size: 28px;
  line-height: 1.1;
}

.panel {
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .panel-header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
