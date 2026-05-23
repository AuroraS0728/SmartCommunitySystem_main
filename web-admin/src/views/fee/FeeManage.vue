<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>账单总金额</p>
        <h3>¥ {{ formatMoney(totalAmount) }}</h3>
      </article>
      <article class="summary-card">
        <p>实收金额</p>
        <h3>¥ {{ formatMoney(totalPaid) }}</h3>
      </article>
      <article class="summary-card">
        <p>待收金额</p>
        <h3>¥ {{ formatMoney(totalAmount - totalPaid) }}</h3>
      </article>
      <article class="summary-card">
        <p>收缴率</p>
        <h3>{{ paidRate }}%</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>费用管理</h3>
        <el-input
          v-model="keyword"
          placeholder="搜索房产号/房主/缴费类型/业务标识"
          clearable
          style="max-width: 280px"
        />
      </div>
      <el-alert
        title="缴费主体统一为房产号。维修费用由维修人员完单时录入，系统自动生成维修账单给业主，业主可使用积分缴纳。"
        type="info"
        :closable="false"
        style="margin-bottom: 14px"
      />
      <el-table :data="displayList" stripe>
        <el-table-column prop="businessTypeText" label="缴费类型" min-width="130" />
        <el-table-column prop="propertyCode" label="房产号" min-width="150" />
        <el-table-column prop="ownerName" label="房主" min-width="120" />
        <el-table-column prop="businessRef" label="业务标识" min-width="180" />
        <el-table-column prop="subjectName" label="缴费主体" min-width="220" />
        <el-table-column label="应收金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="已收金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="needPoints" label="应付积分" min-width="100" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="resolveStatusType(row)">{{ row.statusText || resolveStatus(row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getPaymentSubjects } from '@/api/fee'

const keyword = ref('')
const list = ref([])

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter(
    (item) =>
      String(item.propertyCode || '').includes(text) ||
      String(item.ownerName || '').includes(text) ||
      String(item.businessTypeText || '').includes(text) ||
      String(item.businessRef || '').includes(text) ||
      String(item.subjectName || '').includes(text)
  )
})

const totalAmount = computed(() => list.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const totalPaid = computed(() => list.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0))
const paidRate = computed(() => {
  if (!totalAmount.value) return 0
  return Number(((totalPaid.value / totalAmount.value) * 100).toFixed(1))
})

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function isPaid(row) {
  const type = Number(row.businessType || 0)
  const status = Number(row.status || 0)
  if (type === 1) return status === 2
  return status === 1
}

function resolveStatus(row) {
  if (isPaid(row)) return '已缴费'
  if (Number(row.businessType || 0) === 1 && Number(row.status || 0) === 1) return '部分缴费'
  return '待缴费'
}

function resolveStatusType(row) {
  const statusText = row.statusText || resolveStatus(row)
  if (String(statusText).includes('已缴')) return 'success'
  if (String(statusText).includes('部分')) return 'warning'
  return 'danger'
}

function normalizeRows(rows) {
  return (Array.isArray(rows) ? rows : []).map((item) => ({
    ...item,
    propertyCode: item.propertyCode || '--',
    ownerName: item.ownerName || '--',
    subjectName: item.subjectName || `${item.propertyCode || '--'} / ${item.ownerName || '--'}`,
    needPoints: Number(item.needPoints || 0)
  }))
}

async function loadData() {
  const res = await getPaymentSubjects()
  list.value = normalizeRows(res.data)
}

onMounted(async () => {
  await loadData()
})
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

.summary-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 10px 0 0;
  font-size: 24px;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
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
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
