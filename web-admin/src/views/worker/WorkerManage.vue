<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>服务人员列表</h3>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索姓名或电话" clearable style="width: 240px" />
          <el-button type="primary" @click="openCreate">新增人员</el-button>
        </div>
      </div>
      <el-table :data="displayList" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="nickname" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'danger'">
              {{ Number(row.status) === 1 ? '在岗' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="loadPerformance(row.id)">绩效</el-button>
            <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section v-if="perf" class="panel perf-panel">
      <h3>绩效详情</h3>
      <div class="perf-grid">
        <article>
          <p>人员ID</p>
          <h4>{{ perf.workerId }}</h4>
        </article>
        <article>
          <p>完成工单</p>
          <h4>{{ perf.completedCount }}</h4>
        </article>
        <article>
          <p>服务评分</p>
          <h4>{{ perf.rating }}</h4>
        </article>
        <article>
          <p>估算收入</p>
          <h4>￥ {{ formatMoney(perf.income) }}</h4>
        </article>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑人员' : '新增人员'" width="520px">
      <el-form label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addWorker, deleteWorker, getWorkerList, getWorkerPerformance, updateWorker } from '@/api/worker'

const keyword = ref('')
const list = ref([])
const perf = ref(null)
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({
  id: null,
  nickname: '',
  phone: ''
})

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter(
    (item) => String(item.nickname || '').includes(text) || String(item.phone || '').includes(text)
  )
})

function resetForm() {
  form.value = { id: null, nickname: '', phone: '' }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { id: row.id, nickname: row.nickname || '', phone: row.phone || '' }
  dialogVisible.value = true
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

async function loadWorkers() {
  const res = await getWorkerList()
  list.value = Array.isArray(res.data) ? res.data : []
}

async function submit() {
  if (!form.value.nickname?.trim()) {
    ElMessage.warning('请填写姓名')
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await updateWorker(form.value.id, { nickname: form.value.nickname, phone: form.value.phone })
      ElMessage.success('更新成功')
    } else {
      await addWorker({ nickname: form.value.nickname, phone: form.value.phone })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadWorkers()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除人员 #${row.id} 吗？`, '提示', { type: 'warning' })
  await deleteWorker(row.id)
  ElMessage.success('删除成功')
  await loadWorkers()
}

async function loadPerformance(id) {
  const res = await getWorkerPerformance(id)
  perf.value = {
    workerId: id,
    completedCount: res.data?.completedCount ?? 0,
    rating: res.data?.rating ?? '--',
    income: res.data?.income ?? 0
  }
}

onMounted(async () => {
  await loadWorkers()
})
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

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.perf-panel h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.perf-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.perf-grid article {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
}

.perf-grid p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.perf-grid h4 {
  margin: 8px 0 0;
  font-size: 22px;
}

@media (max-width: 1024px) {
  .perf-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    flex-direction: column;
  }

  .perf-grid {
    grid-template-columns: 1fr;
  }
}
</style>
