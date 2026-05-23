<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>催缴任务管理</h3>
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索标题/描述" clearable @keyup.enter="search" @clear="search" />
          <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 150px" @change="search">
            <el-option :value="0" label="待处理" />
            <el-option :value="1" label="已完成" />
          </el-select>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="id" label="任务ID" width="96" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="指派给谁" min-width="140">
          <template #default="{ row }">{{ assigneeName(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'warning'">
              {{ Number(row.status) === 1 ? '已完成' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :disabled="Number(row.status) === 1"
              :loading="completeLoadingId === row.id"
              @click="handleComplete(row)"
            >
              完成
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
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { completeTask, getTasks } from '@/api/adminOps'

const loading = ref(false)
const completeLoadingId = ref(null)
const keyword = ref('')
const statusFilter = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

function assigneeName(row) {
  if (!row?.assignedTo) return '未指派'
  return row.assignedToName || `用户${row.assignedTo}`
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
    const taskRes = await getTasks({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value ?? undefined,
      keyword: keyword.value.trim() || undefined
    })
    rows.value = Array.isArray(taskRes.data?.records) ? taskRes.data.records : []
    total.value = Number(taskRes.data?.total || 0)
  } finally {
    loading.value = false
  }
}

async function handleComplete(row) {
  completeLoadingId.value = row.id
  try {
    await completeTask(row.id)
    ElMessage.success('任务已完成')
    await loadData()
  } finally {
    completeLoadingId.value = null
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
  width: 280px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
