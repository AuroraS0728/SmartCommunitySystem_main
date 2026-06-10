<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>设施总数</p>
        <h3>{{ rows.length }}</h3>
      </article>
      <article class="summary-card">
        <p>正常开放</p>
        <h3>{{ normalCount }}</h3>
      </article>
      <article class="summary-card">
        <p>维护中</p>
        <h3>{{ maintenanceCount }}</h3>
      </article>
      <article class="summary-card">
        <p>停用</p>
        <h3>{{ disabledCount }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>设施管理</h3>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索设施名称、分类、位置" clearable style="width: 260px" />
          <el-select v-model="categoryFilter" placeholder="设施分类" clearable style="width: 160px">
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="设施状态" clearable style="width: 140px">
            <el-option label="正常开放" :value="1" />
            <el-option label="维护中" :value="2" />
            <el-option label="停用" :value="3" />
          </el-select>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增设施</el-button>
        </div>
      </div>

      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="84" />
        <el-table-column prop="name" label="设施名称" min-width="150" />
        <el-table-column prop="category" label="设施分类" min-width="120" />
        <el-table-column prop="location" label="所在位置" min-width="150" />
        <el-table-column prop="openHours" label="开放时间" min-width="140" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近巡检" min-width="170">
          <template #default="{ row }">{{ formatTime(row.lastInspectionTime) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="设施说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增设施' : '编辑设施'" width="720px">
      <el-form :model="form" label-width="100px">
        <div class="form-grid">
          <el-form-item label="设施名称">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="设施分类">
            <el-input v-model="form.category" />
          </el-form-item>
          <el-form-item label="所在位置">
            <el-input v-model="form.location" />
          </el-form-item>
          <el-form-item label="开放时间">
            <el-input v-model="form.openHours" placeholder="如 08:00-21:00" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="form.contactPhone" />
          </el-form-item>
          <el-form-item label="设施状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="正常开放" :value="1" />
              <el-option label="维护中" :value="2" />
              <el-option label="停用" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="最近巡检">
            <el-date-picker
              v-model="form.lastInspectionTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              clearable
              style="width: 100%"
            />
          </el-form-item>
        </div>
        <el-form-item label="图片地址">
          <el-input v-model="form.imageUrls" placeholder="多个地址可用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="设施说明">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createFacility, deleteFacility, getFacilities, updateFacility } from '@/api/facility'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const statusFilter = ref(undefined)
const categoryFilter = ref('')
const rows = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive(emptyForm())

const normalCount = computed(() => rows.value.filter((item) => Number(item.status) === 1).length)
const maintenanceCount = computed(() => rows.value.filter((item) => Number(item.status) === 2).length)
const disabledCount = computed(() => rows.value.filter((item) => Number(item.status) === 3).length)
const categoryOptions = computed(() => [...new Set(rows.value.map((item) => item.category).filter(Boolean))])

function emptyForm() {
  return {
    name: '',
    category: '',
    location: '',
    openHours: '',
    contactPhone: '',
    status: 1,
    sortOrder: 0,
    description: '',
    imageUrls: '',
    lastInspectionTime: ''
  }
}

function resetForm() {
  Object.assign(form, emptyForm())
}

function statusText(status) {
  if (Number(status) === 2) {
    return '维护中'
  }
  if (Number(status) === 3) {
    return '停用'
  }
  return '正常开放'
}

function statusTagType(status) {
  if (Number(status) === 2) {
    return 'warning'
  }
  if (Number(status) === 3) {
    return 'danger'
  }
  return 'success'
}

function formatTime(value) {
  if (!value) {
    return '--'
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await getFacilities({
      keyword: keyword.value || undefined,
      category: categoryFilter.value || undefined,
      status: statusFilter.value
    })
    rows.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    category: row.category || '',
    location: row.location || '',
    openHours: row.openHours || '',
    contactPhone: row.contactPhone || '',
    status: Number(row.status || 1),
    sortOrder: Number(row.sortOrder || 0),
    description: row.description || '',
    imageUrls: row.imageUrls || '',
    lastInspectionTime: formatTime(row.lastInspectionTime) === '--' ? '' : formatTime(row.lastInspectionTime)
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.name || !form.category) {
    ElMessage.warning('请至少填写设施名称和设施分类')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, lastInspectionTime: form.lastInspectionTime || null }
    if (dialogMode.value === 'create') {
      await createFacility(payload)
      ElMessage.success('设施已新增')
    } else {
      await updateFacility(editingId.value, payload)
      ElMessage.success('设施已更新')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除设施 ${row.name} 吗？`, '删除确认', { type: 'warning' })
  await deleteFacility(row.id)
  ElMessage.success('已删除')
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .panel-header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
