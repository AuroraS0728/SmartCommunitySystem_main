<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>角色权限说明</h3>
        </div>
      </div>
      <el-table :data="roles" stripe>
        <el-table-column prop="value" label="角色值" width="90" />
        <el-table-column prop="name" label="角色名称" min-width="140" />
        <el-table-column prop="scope" label="数据范围" min-width="220" />
        <el-table-column prop="modules" label="主要入口" min-width="320" show-overflow-tooltip />
        <el-table-column label="后台权限" width="120">
          <template #default="{ row }">
            <el-tag :type="row.admin ? 'success' : 'info'">{{ row.admin ? '可进入' : '不可进入' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
const roles = [
  {
    value: 1,
    name: '业主',
    scope: '本人账号、绑定房产、本人账单与服务记录',
    modules: '业主端缴费、报修、访客、活动、消息、邻里二手/失物',
    admin: false
  },
  {
    value: 2,
    name: '物业管理员',
    scope: '社区运营和物业业务数据',
    modules: '后台管理、住户、费用、工单、访客、公告、设施、活动、催缴',
    admin: true
  },
  {
    value: 3,
    name: '维修人员',
    scope: '分配给本人的维修工单和本人绩效',
    modules: '维修人员端任务、上门核验、完工处理、消息',
    admin: false
  },
  {
    value: 4,
    name: '超级管理员',
    scope: '物业后台全量数据',
    modules: '后台全部模块；后端按物业管理员权限兼容放行',
    admin: true
  }
]
</script>

<style scoped>
.page-grid {
  display: grid;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.panel-header {
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}
</style>
