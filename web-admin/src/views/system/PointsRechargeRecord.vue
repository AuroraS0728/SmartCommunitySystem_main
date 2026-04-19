<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>积分充值记录</h3>
      </div>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="业主姓名">
          <el-input v-model="filters.ownerName" placeholder="输入业主昵称/账号" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecords">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="userName" label="业主" min-width="140" />
        <el-table-column prop="operatorName" label="操作员" min-width="140" />
        <el-table-column prop="amount" label="充值积分" min-width="120" />
        <el-table-column prop="beforePoints" label="充值前" min-width="120" />
        <el-table-column prop="afterPoints" label="充值后" min-width="120" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="充值时间" min-width="180" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getRechargeRecords } from '@/api/points'

const loading = ref(false)
const rows = ref([])
const filters = ref({
  ownerName: '',
  timeRange: []
})

function toQueryParams() {
  const params = {}
  if (filters.value.ownerName.trim()) {
    params.ownerName = filters.value.ownerName.trim()
  }
  if (Array.isArray(filters.value.timeRange) && filters.value.timeRange.length === 2) {
    params.startTime = filters.value.timeRange[0]
    params.endTime = filters.value.timeRange[1]
  }
  return params
}

function formatTime(value) {
  if (!value) return '--'
  if (typeof value === 'string') return value.replace('T', ' ').slice(0, 19)
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '--'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getRechargeRecords(toQueryParams())
    const list = Array.isArray(res?.data) ? res.data : []
    rows.value = list.map((item) => ({
      ...item,
      createTime: formatTime(item.createTime)
    }))
  } catch (error) {
    ElMessage.error(error?.message || '充值记录加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value.ownerName = ''
  filters.value.timeRange = []
  loadRecords()
}

onMounted(loadRecords)
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
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

.filter-form {
  margin-bottom: 10px;
}
</style>
