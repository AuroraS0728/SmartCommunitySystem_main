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
        <h3>费用账单</h3>
        <el-input v-model="keyword" placeholder="搜索账期或住户" clearable style="max-width: 260px" />
      </div>
      <el-table :data="displayList" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="billPeriod" label="账期" min-width="140" />
        <el-table-column prop="payer" label="缴费主体" min-width="180" />
        <el-table-column label="应收金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="已收金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="resolveStatusType(row.status)">{{ row.status || resolveStatus(row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getBills } from '@/api/fee'

const keyword = ref('')
const list = ref([])

const fallbackData = [
  { id: 2001, billPeriod: '2026-04', payer: '1号楼1单元101', amount: 520, paidAmount: 520, status: '已缴清' },
  { id: 2002, billPeriod: '2026-04', payer: '8号楼2单元201', amount: 680, paidAmount: 400, status: '部分缴费' },
  { id: 2003, billPeriod: '2026-04', payer: '12号楼1单元502', amount: 560, paidAmount: 0, status: '待缴费' }
]

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter(
    (item) => String(item.billPeriod || '').includes(text) || String(item.payer || '').includes(text)
  )
})

const totalAmount = computed(() => list.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const totalPaid = computed(() => list.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0))
const paidRate = computed(() => {
  if (!totalAmount.value) return 0
  return Number(((totalPaid.value / totalAmount.value) * 100).toFixed(1))
})

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function resolveStatus(row) {
  if (Number(row.paidAmount || 0) <= 0) return '待缴费'
  if (Number(row.paidAmount || 0) < Number(row.amount || 0)) return '部分缴费'
  return '已缴清'
}

function resolveStatusType(text) {
  if (String(text).includes('已')) return 'success'
  if (String(text).includes('部分')) return 'warning'
  return 'danger'
}

async function loadData() {
  try {
    const res = await getBills()
    list.value = Array.isArray(res.data) && res.data.length ? res.data : fallbackData
  } catch (error) {
    list.value = fallbackData
  }
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
