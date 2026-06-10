<template>
  <div class="page-grid">
    <el-row class="stat-row" :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-title">停车订单总数</div>
          <div class="stat-value">{{ orders.length }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-title">已缴订单</div>
          <div class="stat-value">{{ paidCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-title">待缴订单</div>
          <div class="stat-value">{{ pendingCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-title">累计停车收入</div>
          <div class="stat-value">¥ {{ formatMoney(totalAmount) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <section class="panel">
      <div class="panel-header">
        <h3>停车管理</h3>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索车牌号/房产ID/订单类型"
            clearable
            style="width: 280px"
          />
          <el-select v-model="statusFilter" placeholder="缴费状态" clearable style="width: 140px">
            <el-option label="待缴" :value="0" />
            <el-option label="已缴" :value="1" />
          </el-select>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table :data="displayList" stripe v-loading="loading">
        <el-table-column prop="id" label="订单ID" width="96" />
        <el-table-column prop="vehicleNo" label="车牌号" min-width="140" />
        <el-table-column prop="propertyId" label="房产ID" min-width="100" />
        <el-table-column label="订单类型" min-width="100">
          <template #default="{ row }">{{ orderTypeText(row.orderType) }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="100">
          <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
        </el-table-column>
        <el-table-column label="停车时长" min-width="100">
          <template #default="{ row }">{{ row.parkHours || 0 }} 小时</template>
        </el-table-column>
        <el-table-column label="金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="开始时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'warning'">
              {{ Number(row.status) === 1 ? '已缴' : '待缴' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getParkingOrders } from '@/api/parking'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(undefined)
const orders = ref([])

const displayList = computed(() => {
  const text = keyword.value.trim()
  return orders.value.filter((item) => {
    const matchKeyword =
      !text ||
      String(item.vehicleNo || '').includes(text) ||
      String(item.propertyId || '').includes(text) ||
      orderTypeText(item.orderType).includes(text)
    const matchStatus =
      statusFilter.value === undefined || statusFilter.value === null
        ? true
        : Number(item.status || 0) === Number(statusFilter.value)
    return matchKeyword && matchStatus
  })
})

const paidCount = computed(() => orders.value.filter((item) => Number(item.status) === 1).length)
const pendingCount = computed(() => orders.value.filter((item) => Number(item.status) !== 1).length)
const totalAmount = computed(() => orders.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

function orderTypeText(type) {
  return Number(type) === 2 ? '月卡' : '临停'
}

function sourceTypeText(type) {
  return Number(type) === 2 ? '访客车' : '业主车'
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await getParkingOrders()
    orders.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
}

.stat-row {
  margin: 20px 0;
}

.stat-card {
  height: 110px;
  border-radius: 8px;
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
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

@media (max-width: 760px) {
  .panel-header {
    align-items: stretch;
    flex-direction: column;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>
