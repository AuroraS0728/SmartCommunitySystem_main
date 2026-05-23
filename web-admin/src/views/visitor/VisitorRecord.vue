<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>访客管理</h3>
        <div class="header-actions">
          <el-input v-model="keyword" placeholder="搜索姓名/手机号/邀请码" clearable class="search-input" />
          <el-select v-model="statusFilter" placeholder="状态" clearable class="status-select">
            <el-option label="未使用" value="UNUSED" />
            <el-option label="已使用" value="USED" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
          <el-button :loading="loading" @click="loadAll">刷新</el-button>
        </div>
      </div>

      <el-table :data="displayList" stripe v-loading="loading">
        <el-table-column prop="visitorName" label="访客姓名" min-width="120" />
        <el-table-column prop="visitorPhone" label="手机号" min-width="130" />
        <el-table-column prop="code" label="邀请码" min-width="120" />
        <el-table-column prop="visitTime" label="来访时间" min-width="170" />
        <el-table-column label="有效截止时间" min-width="170">
          <template #default="{ row }">
            <span>{{ expireText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusText || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="danger"
              :disabled="isPhoneBlacklisted(row.visitorPhone)"
              @click="quickBlacklist(row)"
            >
              {{ isPhoneBlacklisted(row.visitorPhone) ? '已拉黑' : '加入黑名单' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>黑名单管理</h3>
      </div>

      <div class="blacklist-form">
        <el-input v-model="blacklistPhone" placeholder="手机号" class="phone-input" />
        <el-input v-model="blacklistReason" placeholder="原因（可选）" class="reason-input" />
        <el-button type="primary" :loading="savingBlacklist" @click="submitBlacklist">添加</el-button>
      </div>

      <el-table :data="blacklist" stripe>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="createdBy" label="操作人ID" width="100" />
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeBlacklistItem(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addVisitorBlacklist,
  getVisitorBlacklist,
  getVisitorRecords,
  removeVisitorBlacklist
} from '@/api/access'

const loading = ref(false)
const savingBlacklist = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const records = ref([])
const blacklist = ref([])
const blacklistPhone = ref('')
const blacklistReason = ref('')

const displayList = computed(() => {
  const key = keyword.value.trim()
  return records.value.filter((item) => {
    const statusOk = !statusFilter.value || item.status === statusFilter.value
    if (!statusOk) return false
    if (!key) return true
    return (
      String(item.visitorName || '').includes(key) ||
      String(item.visitorPhone || '').includes(key) ||
      String(item.code || '').includes(key)
    )
  })
})

function statusTagType(status) {
  if (status === 'USED') return 'success'
  if (status === 'EXPIRED') return 'info'
  return 'warning'
}

function expireText(row) {
  return row?.expireTime || '--'
}

function isPhoneBlacklisted(phone) {
  return blacklist.value.some((item) => String(item.phone || '') === String(phone || ''))
}

async function loadRecords() {
  const res = await getVisitorRecords()
  records.value = Array.isArray(res.data) ? res.data : []
}

async function loadBlacklist() {
  const res = await getVisitorBlacklist()
  blacklist.value = Array.isArray(res.data) ? res.data : []
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadRecords(), loadBlacklist()])
  } catch (error) {
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function submitBlacklist() {
  const phone = blacklistPhone.value.trim()
  if (!phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  savingBlacklist.value = true
  try {
    await addVisitorBlacklist({
      phone,
      reason: blacklistReason.value.trim()
    })
    blacklistPhone.value = ''
    blacklistReason.value = ''
    ElMessage.success('已加入黑名单')
    await loadAll()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    savingBlacklist.value = false
  }
}

async function quickBlacklist(row) {
  try {
    await ElMessageBox.confirm(`确认将手机号 ${row.visitorPhone} 加入黑名单？`, '提示', {
      type: 'warning'
    })
    await addVisitorBlacklist({
      phone: row.visitorPhone,
      reason: `来自访客记录 ID=${row.id}`
    })
    ElMessage.success('已加入黑名单')
    await loadAll()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error(error?.message || '操作失败')
  }
}

async function removeBlacklistItem(row) {
  try {
    await ElMessageBox.confirm(`确认移除黑名单手机号 ${row.phone}？`, '提示', {
      type: 'warning'
    })
    await removeVisitorBlacklist(row.id)
    ElMessage.success('已移除')
    await loadAll()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error(error?.message || '操作失败')
  }
}

onMounted(loadAll)
</script>

<style scoped>
.page-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 280px;
}

.status-select {
  width: 140px;
}

.blacklist-form {
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.phone-input {
  width: 180px;
}

.reason-input {
  width: 280px;
}

@media (max-width: 960px) {
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions,
  .blacklist-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .status-select,
  .phone-input,
  .reason-input {
    width: 100%;
  }
}
</style>
