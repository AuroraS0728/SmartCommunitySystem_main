<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>公告管理</h3>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 240px" />
          <el-button type="primary" @click="openCreate">发布公告</el-button>
        </div>
      </div>

      <el-table :data="displayList" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="publisher" label="发布人" min-width="140" />
        <el-table-column prop="publishTime" label="发布时间" min-width="180" />
        <el-table-column label="置顶" width="100">
          <template #default="{ row }">
            <el-tag :type="row.top ? 'danger' : 'info'">{{ row.top ? '是' : '否' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="620px">
      <el-form label-width="82px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="附件">
          <el-input v-model="form.attachmentUrls" placeholder="可选，URL 或 JSON 数组字符串" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="formTopSwitch" />
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
import { deleteNotice, getNoticeList, publishNotice, updateNotice } from '@/api/notice'

const keyword = ref('')
const list = ref([])
const saving = ref(false)
const dialogVisible = ref(false)
const formTopSwitch = ref(false)
const form = ref({
  id: null,
  title: '',
  content: '',
  attachmentUrls: '',
  top: 0
})

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter((item) => String(item.title || '').includes(text))
})

function resetForm() {
  form.value = {
    id: null,
    title: '',
    content: '',
    attachmentUrls: '',
    top: 0
  }
  formTopSwitch.value = false
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    title: row.title || '',
    content: row.content || '',
    attachmentUrls: row.attachmentUrls || '',
    top: Number(row.top || 0)
  }
  formTopSwitch.value = Number(row.top || 0) === 1
  dialogVisible.value = true
}

async function loadData() {
  const res = await getNoticeList()
  list.value = Array.isArray(res.data) ? res.data : []
}

async function submit() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请填写公告标题')
    return
  }
  if (!form.value.content?.trim()) {
    ElMessage.warning('请填写公告内容')
    return
  }
  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      content: form.value.content,
      attachmentUrls: form.value.attachmentUrls,
      top: formTopSwitch.value ? 1 : 0
    }
    if (form.value.id) {
      await updateNotice(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await publishNotice(payload)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除公告 #${row.id} 吗？`, '提示', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(async () => {
  await loadData()
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
