<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>房屋档案</h3>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索楼栋/房号/业主" clearable style="width: 260px" />
          <el-button type="primary" @click="openCreate">新增房屋</el-button>
        </div>
      </div>

      <el-table :data="displayList" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="community" label="小区" min-width="140" />
        <el-table-column prop="building" label="楼栋" min-width="90" />
        <el-table-column prop="unit" label="单元" min-width="90" />
        <el-table-column prop="room" label="房号" min-width="90" />
        <el-table-column prop="ownerName" label="业主" min-width="120" />
        <el-table-column prop="area" label="面积(m²)" min-width="110" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="resolveStatusTag(row.status)">{{ resolveStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房屋' : '新增房屋'" width="700px">
      <el-form label-width="88px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="小区">
              <el-input v-model="form.community" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼栋">
              <el-input v-model="form.building" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单元">
              <el-input v-model="form.unit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房号">
              <el-input v-model="form.room" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业主">
              <el-input v-model="form.ownerName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积">
              <el-input-number v-model="form.area" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="未售" :value="1" />
                <el-option label="已售" :value="2" />
                <el-option label="空置" :value="3" />
                <el-option label="已入住" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
import { addHouse, deleteHouse, getHouseList, updateHouse } from '@/api/house'

const keyword = ref('')
const list = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({
  id: null,
  community: '',
  building: '',
  unit: '',
  room: '',
  ownerName: '',
  area: 0,
  status: 3
})

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter(
    (item) =>
      String(item.building || '').includes(text) ||
      String(item.room || '').includes(text) ||
      String(item.ownerName || '').includes(text)
  )
})

function resetForm() {
  form.value = {
    id: null,
    community: '',
    building: '',
    unit: '',
    room: '',
    ownerName: '',
    area: 0,
    status: 3
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    community: row.community,
    building: row.building,
    unit: row.unit,
    room: row.room,
    ownerName: row.ownerName,
    area: Number(row.area || 0),
    status: Number(row.status || 3)
  }
  dialogVisible.value = true
}

function resolveStatusText(status) {
  const map = { 1: '未售', 2: '已售', 3: '空置', 4: '已入住' }
  return map[Number(status)] || '--'
}

function resolveStatusTag(status) {
  const value = Number(status)
  if (value === 4) return 'success'
  if (value === 3) return 'info'
  if (value === 2) return 'warning'
  return 'danger'
}

async function loadList() {
  const res = await getHouseList()
  list.value = Array.isArray(res.data) ? res.data : []
}

async function submit() {
  if (!form.value.community || !form.value.building || !form.value.room) {
    ElMessage.warning('请填写必填信息')
    return
  }
  saving.value = true
  try {
    const payload = {
      community: form.value.community,
      building: form.value.building,
      unit: form.value.unit,
      room: form.value.room,
      ownerName: form.value.ownerName,
      area: form.value.area,
      status: form.value.status
    }
    if (form.value.id) {
      await updateHouse(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await addHouse(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除房屋 #${row.id} 吗？`, '提示', { type: 'warning' })
  await deleteHouse(row.id)
  ElMessage.success('删除成功')
  await loadList()
}

onMounted(async () => {
  await loadList()
})
</script>

<style scoped>
.page-grid {
  display: grid;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
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

@media (max-width: 760px) {
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
