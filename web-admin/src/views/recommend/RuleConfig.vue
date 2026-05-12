<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>推荐规则配置</h3>
        <div class="actions">
          <el-button @click="loadRules">重新加载</el-button>
          <el-button type="primary" @click="openCreate">新增规则</el-button>
        </div>
      </div>

      <el-table :data="rules" stripe v-loading="loading">
        <el-table-column prop="ruleName" label="规则名" min-width="140" />
        <el-table-column prop="serviceName" label="推荐服务" min-width="140" />
        <el-table-column prop="ruleExpression" label="表达式" min-width="180" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="92" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-tag :type="Number(row.enabled) === 1 ? 'success' : 'info'">{{ Number(row.enabled) === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="条件 JSON" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">{{ row.conditionJson || '{}' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑推荐规则' : '新增推荐规则'" width="760px">
      <el-form label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="服务名称"><el-input v-model="form.serviceName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="服务ID"><el-input v-model="form.serviceId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="优先级"><el-input-number v-model="form.priority" :min="1" :max="999" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="价格"><el-input-number v-model="form.price" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="启用">
              <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="表达式"><el-input v-model="form.ruleExpression" placeholder="如：has_elderly == true" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="图片地址"><el-input v-model="form.imageUrl" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="条件 JSON">
              <el-input
                v-model="form.conditionJson"
                type="textarea"
                :rows="8"
                placeholder='{"reason":"家中有老人，优先推荐适老化改造","popupEnabled":true,"actionPath":"/pages/service/service"}'
              />
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
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createRecommendRule, deleteRecommendRule, getRecommendRules, updateRecommendRule } from '@/api/recommend'

const loading = ref(false)
const saving = ref(false)
const rules = ref([])
const dialogVisible = ref(false)
const form = ref({})

function resetForm() {
  form.value = {
    id: null,
    ruleName: '',
    serviceId: '',
    serviceName: '',
    ruleExpression: '',
    conditionJson: '{\n  "reason": "",\n  "popupEnabled": true,\n  "actionPath": "/pages/service/service"\n}',
    imageUrl: '',
    price: 0,
    priority: 99,
    enabled: 1
  }
}

async function loadRules() {
  loading.value = true
  try {
    const res = await getRecommendRules()
    rules.value = Array.isArray(res.data) ? res.data : []
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
    ruleName: row.ruleName || '',
    serviceId: row.serviceId || '',
    serviceName: row.serviceName || '',
    ruleExpression: row.ruleExpression || '',
    conditionJson: row.conditionJson || '{}',
    imageUrl: row.imageUrl || '',
    price: Number(row.price || 0),
    priority: Number(row.priority || 99),
    enabled: Number(row.enabled || 0)
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.ruleName || !form.value.serviceName || !form.value.ruleExpression) {
    ElMessage.warning('请填写规则名、服务名和表达式')
    return
  }
  try {
    JSON.parse(form.value.conditionJson || '{}')
  } catch {
    ElMessage.error('条件 JSON 格式不正确')
    return
  }
  saving.value = true
  try {
    const payload = { ...form.value }
    if (form.value.id) {
      await updateRecommendRule(form.value.id, payload)
      ElMessage.success('推荐规则已更新')
    } else {
      await createRecommendRule(payload)
      ElMessage.success('推荐规则已创建')
    }
    dialogVisible.value = false
    await loadRules()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除推荐规则 ${row.ruleName} 吗？`, '提示', { type: 'warning' })
  await deleteRecommendRule(row.id)
  ElMessage.success('已删除')
  await loadRules()
}

onMounted(() => {
  resetForm()
  loadRules()
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
</style>
