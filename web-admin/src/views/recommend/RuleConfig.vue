<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>推荐规则配置</h3>
        <div class="actions">
          <el-button @click="loadRules">重新加载</el-button>
          <el-button type="primary" :loading="saving" @click="saveRules">保存</el-button>
        </div>
      </div>

      <el-input
        v-model="ruleJson"
        type="textarea"
        :rows="18"
        resize="vertical"
        spellcheck="false"
        placeholder="请输入推荐规则 JSON"
      />
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecommendRules, saveRecommendRules } from '@/api/adminOps'

const defaultRules = {
  maxResults: 3,
  rules: [
    {
      serviceName: '适老化改造',
      price: 2999,
      priority: 1,
      condition: 'hasElderly == true 或隐式关键词包含 老人/轮椅/防滑/年迈/高龄'
    },
    {
      serviceName: '水电保养套餐',
      price: 399,
      priority: 2,
      condition: '近6个月水电/家电报修次数 >= 2'
    },
    {
      serviceName: '深度保洁',
      price: 599,
      priority: 3,
      condition: 'houseArea > 120'
    },
    {
      serviceName: '宠物除螨服务',
      price: 199,
      priority: 4,
      condition: 'hasPet == true 或隐式关键词包含 宠物/狗/猫'
    }
  ]
}

const ruleJson = ref(JSON.stringify(defaultRules, null, 2))
const saving = ref(false)

async function loadRules() {
  try {
    const res = await getRecommendRules()
    const payload = res.data || defaultRules
    ruleJson.value = typeof payload === 'string' ? payload : JSON.stringify(payload, null, 2)
  } catch {
    ruleJson.value = JSON.stringify(defaultRules, null, 2)
  }
}

async function saveRules() {
  let parsed
  try {
    parsed = JSON.parse(ruleJson.value)
  } catch {
    ElMessage.error('JSON 格式不正确')
    return
  }
  saving.value = true
  try {
    await saveRecommendRules(parsed)
    ElMessage.success('推荐规则已保存')
  } finally {
    saving.value = false
  }
}

onMounted(loadRules)
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

.actions {
  display: flex;
  gap: 10px;
}
</style>
