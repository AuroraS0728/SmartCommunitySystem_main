<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>活动管理</h3>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索标题/地点" clearable style="width: 240px" @keyup.enter="loadData" />
          <el-select v-model="typeFilter" clearable placeholder="类型" style="width: 140px" @change="loadData">
            <el-option v-for="type in activityTypes" :key="type" :label="type" :value="type" />
          </el-select>
          <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 120px" @change="loadData">
            <el-option label="报名中" :value="0" />
            <el-option label="已结束" :value="1" />
          </el-select>
          <el-button @click="loadData">刷新</el-button>
          <el-button type="primary" @click="openCreate">发布活动</el-button>
        </div>
      </div>

      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="title" label="活动标题" min-width="180" />
        <el-table-column prop="type" label="类型" width="110" />
        <el-table-column label="时间" min-width="180">
          <template #default="{ row }">{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="140" />
        <el-table-column label="人数" width="126">
          <template #default="{ row }">{{ row.currentParticipants }}/{{ row.maxParticipants }}</template>
        </el-table-column>
        <el-table-column label="状态" width="106">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 0 ? 'success' : 'info'">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="openRegistrations(row)">报名列表</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑活动' : '发布活动'" width="820px">
      <el-form label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="活动标题"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="活动类型">
              <el-select v-model="form.type" style="width: 100%">
                <el-option v-for="type in activityTypes" :key="type" :label="type" :value="type" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="活动地点"><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="活动状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="报名中" :value="0" />
              <el-option label="已结束" :value="1" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="人数上限"><el-input-number v-model="form.maxParticipants" :min="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年龄限制"><el-input v-model="form.ageLimit" placeholder="如：>=18 或 18-60" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="需带儿童"><el-switch v-model="form.withChildRequired" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="需带宠物"><el-switch v-model="form.withPetRequired" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="活动描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="活动图片">
              <div class="upload-line">
                <el-input v-model="form.imageUrl" placeholder="可直接填写图片地址，或使用右侧上传" />
                <el-upload :show-file-list="false" :auto-upload="false" accept="image/*" :on-change="handleUploadChange">
                  <el-button type="primary" plain :loading="uploading">上传图片</el-button>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="registrationVisible" title="活动报名列表" size="720px">
      <el-table :data="registrations" stripe v-loading="registrationLoading">
        <el-table-column prop="nickname" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column label="儿童/宠物" min-width="120">
          <template #default="{ row }">{{ Number(row.hasChild) === 1 ? '带儿童' : '不带儿童' }} / {{ Number(row.hasPet) === 1 ? '带宠物' : '不带宠物' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ registrationStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :disabled="Number(row.status) !== 0" @click="review(row, 1)">通过</el-button>
            <el-button type="danger" link :disabled="Number(row.status) === 2" @click="review(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createActivity,
  getActivityRegistrations,
  getAdminActivities,
  reviewActivityRegistration,
  updateActivity,
  uploadActivityAsset
} from '@/api/activity'

const activityTypes = ['公益', '爬山', '露营', '亲子', '宠物', '夕阳红']
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const registrationLoading = ref(false)
const registrationVisible = ref(false)
const dialogVisible = ref(false)
const activities = ref([])
const registrations = ref([])
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref(null)
const currentActivity = ref(null)

const form = ref({})

function resetForm() {
  form.value = {
    id: null,
    title: '',
    description: '',
    type: '公益',
    imageUrl: '',
    startTime: '',
    endTime: '',
    location: '',
    maxParticipants: 20,
    ageLimit: '',
    withChildRequired: 0,
    withPetRequired: 0,
    status: 0
  }
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

function registrationStatusText(status) {
  if (Number(status) === 1) return '已确认'
  if (Number(status) === 2) return '已取消'
  return '待审核'
}

function statusTag(status) {
  if (Number(status) === 1) return 'success'
  if (Number(status) === 2) return 'info'
  return 'warning'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminActivities({
      keyword: keyword.value.trim() || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value
    })
    activities.value = Array.isArray(res.data) ? res.data : []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    title: row.title || '',
    description: row.description || '',
    type: row.type || '公益',
    imageUrl: row.imageUrl || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    location: row.location || '',
    maxParticipants: Number(row.maxParticipants || 20),
    ageLimit: row.ageLimit || '',
    withChildRequired: Number(row.withChildRequired || 0),
    withPetRequired: Number(row.withPetRequired || 0),
    status: Number(row.status || 0)
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.title || !form.value.location || !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请填写活动标题、地点和时间')
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await updateActivity(form.value.id, form.value)
      ElMessage.success('活动已更新')
    } else {
      await createActivity(form.value)
      ElMessage.success('活动已发布')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleUploadChange(uploadFile) {
  const raw = uploadFile?.raw
  if (!raw) return
  uploading.value = true
  try {
    const res = await uploadActivityAsset(raw)
    form.value.imageUrl = res.data?.url || ''
    ElMessage.success('图片上传成功')
  } finally {
    uploading.value = false
  }
}

async function openRegistrations(row) {
  currentActivity.value = row
  registrationVisible.value = true
  registrationLoading.value = true
  try {
    const res = await getActivityRegistrations(row.id)
    registrations.value = Array.isArray(res.data) ? res.data : []
  } finally {
    registrationLoading.value = false
  }
}

async function review(row, status) {
  await reviewActivityRegistration(row.id, { status })
  ElMessage.success(status === 1 ? '已通过报名' : '已拒绝报名')
  if (currentActivity.value?.id) {
    await Promise.all([openRegistrations(currentActivity.value), loadData()])
  }
}

onMounted(() => {
  resetForm()
  loadData()
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

.panel-header,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-header {
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.upload-line {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}
</style>
