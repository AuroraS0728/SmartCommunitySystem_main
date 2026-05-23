<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>信用记录管理</h3>
        <div class="filters">
          <el-input v-model="userKeyword" placeholder="输入业主姓名或手机号" clearable @keyup.enter="submitUserSearch" @clear="submitUserSearch" />
          <el-button type="primary" :loading="userLoading" @click="submitUserSearch">搜索用户</el-button>
        </div>
      </div>

      <el-table v-if="userRows.length" :data="userRows" stripe>
        <el-table-column prop="id" label="用户ID" width="96" />
        <el-table-column prop="nickname" label="业主姓名" min-width="150" />
        <el-table-column prop="phone" label="手机号" min-width="150" />
        <el-table-column prop="creditScore" label="当前信用分" width="130" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="selectUser(row)">查看明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="userRows.length" class="pager">
        <el-pagination
          v-model:current-page="userPage"
          v-model:page-size="userSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="userTotal"
          @current-change="searchUsers"
          @size-change="handleUserSizeChange"
        />
      </div>
      <el-empty v-else-if="searched" description="未找到匹配业主" />
    </section>

    <section v-if="selectedUser" class="panel">
      <div class="score-header">
        <div>
          <h3>{{ selectedUser.nickname || `业主${selectedUser.id}` }}</h3>
          <p>{{ selectedUser.phone || '-' }}</p>
        </div>
        <div class="score-box">
          <span>当前信用分</span>
          <strong>{{ selectedUser.creditScore ?? 100 }}</strong>
        </div>
      </div>

      <el-table v-loading="logLoading" :data="logs" stripe>
        <el-table-column label="变动值" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.changeValue) >= 0 ? 'success' : 'danger'">
              {{ Number(row.changeValue) >= 0 ? '+' : '' }}{{ row.changeValue }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="220" />
        <el-table-column prop="beforeScore" label="变动前" width="100" />
        <el-table-column prop="afterScore" label="变动后" width="100" />
        <el-table-column label="时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="logPage"
          v-model:page-size="logSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="logTotal"
          @current-change="loadLogs"
          @size-change="handleLogSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCreditLogs, getUsers } from '@/api/adminOps'

const userKeyword = ref('')
const userLoading = ref(false)
const logLoading = ref(false)
const searched = ref(false)
const userRows = ref([])
const userPage = ref(1)
const userSize = ref(10)
const userTotal = ref(0)
const selectedUser = ref(null)
const logs = ref([])
const logPage = ref(1)
const logSize = ref(10)
const logTotal = ref(0)

function submitUserSearch() {
  userPage.value = 1
  searchUsers()
}

async function searchUsers() {
  const text = userKeyword.value.trim().toLowerCase()
  if (!text) {
    ElMessage.warning('请输入业主姓名或手机号')
    return
  }
  userLoading.value = true
  searched.value = true
  try {
    const res = await getUsers({
      pageNum: userPage.value,
      pageSize: userSize.value,
      role: 1,
      keyword: text
    })
    const records = Array.isArray(res.data?.records) ? res.data.records : []
    userRows.value = records
    userTotal.value = Number(res.data?.total || 0)
    if (userRows.value.length === 1) {
      await selectUser(userRows.value[0])
    }
  } finally {
    userLoading.value = false
  }
}

function handleUserSizeChange() {
  userPage.value = 1
  searchUsers()
}

async function selectUser(row) {
  selectedUser.value = row
  logPage.value = 1
  await loadLogs()
}

async function loadLogs() {
  if (!selectedUser.value?.id) return
  logLoading.value = true
  try {
    const res = await getCreditLogs({
      userId: selectedUser.value.id,
      page: logPage.value,
      size: logSize.value
    })
    logs.value = Array.isArray(res.data?.records) ? res.data.records : []
    logTotal.value = Number(res.data?.total || 0)
  } finally {
    logLoading.value = false
  }
}

function handleLogSizeChange() {
  logPage.value = 1
  loadLogs()
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}
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

.panel-header,
.score-header {
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-header h3,
.score-header h3 {
  margin: 0;
  font-size: 16px;
}

.score-header p {
  margin: 6px 0 0;
  color: #64748b;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters .el-input {
  width: 280px;
}

.score-box {
  min-width: 150px;
  text-align: right;
}

.score-box span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.score-box strong {
  color: #2563eb;
  font-size: 30px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
