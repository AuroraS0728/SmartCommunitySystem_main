import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/views/login/Login.vue') },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '系统概览', subtitle: '查看社区核心运营数据与服务状态' }
      },
      {
        path: 'house/building',
        component: () => import('@/views/house/RealEstateArchive.vue'),
        meta: { title: '房间管理', subtitle: '统一管理楼栋、房屋状态、业主与租户档案' }
      },
      {
        path: 'house/property',
        component: () => import('@/views/house/RealEstateArchive.vue'),
        meta: { title: '房间管理', subtitle: '统一管理楼栋、房屋状态、业主与租户档案' }
      },
      {
        path: 'house/archive',
        component: () => import('@/views/house/RealEstateArchive.vue'),
        meta: { title: '房间管理', subtitle: '统一管理楼栋、房屋状态、业主与租户档案' }
      },
      {
        path: 'house/owner',
        component: () => import('@/views/house/Owner.vue'),
        meta: { title: '住户管理', subtitle: '统一管理业主基础资料、积分余额与住户画像' }
      },
      {
        path: 'repair/list',
        component: () => import('@/views/repair/SmartWorkOrder.vue'),
        meta: { title: '报修管理', subtitle: '统一处理报修工单、派单建议、状态流转与 SLA 超时监控' }
      },
      {
        path: 'repair/work-order',
        component: () => import('@/views/repair/SmartWorkOrder.vue'),
        meta: { title: '报修管理', subtitle: '统一处理报修工单、派单建议、状态流转与 SLA 超时监控' }
      },
      {
        path: 'repair/smart',
        component: () => import('@/views/repair/SmartWorkOrder.vue'),
        meta: { title: '报修管理', subtitle: '统一处理报修工单、派单建议、状态流转与 SLA 超时监控' }
      },
      {
        path: 'parking/manage',
        component: () => import('@/views/parking/ParkingManage.vue'),
        meta: { title: '停车管理', subtitle: '查看车辆、停车订单与月卡缴费状态' }
      },
      {
        path: 'operation/tasks',
        component: () => import('@/views/operation/TaskList.vue'),
        meta: { title: '催缴任务管理', subtitle: '处理物业费催缴生成的客服待办任务' }
      },
      {
        path: 'credit/log',
        component: () => import('@/views/credit/CreditLog.vue'),
        meta: { title: '信用记录管理', subtitle: '查询业主信用分与变动记录' }
      },
      {
        path: 'recommend/rules',
        component: () => import('@/views/recommend/RuleConfig.vue'),
        meta: { title: '推荐规则管理', subtitle: '维护小程序首页个性化推荐规则' }
      },
      {
        path: 'activity/manage',
        component: () => import('@/views/activity/ActivityManage.vue'),
        meta: { title: '活动管理', subtitle: '发布活动、查看报名记录并审核参与资格' }
      },
      {
        path: 'fee/manage',
        component: () => import('@/views/fee/FeeManage.vue'),
        meta: { title: '费用管理', subtitle: '查看账单收缴状态与欠费风险' }
      },
      {
        path: 'innovation/ops',
        component: () => import('@/views/innovation/InnovationOps.vue'),
        meta: { title: '运营管理', subtitle: '查看投诉分析、信用体系、催缴任务与 SLA 监控' }
      },
      {
        path: 'notice/manage',
        component: () => import('@/views/notice/NoticeManage.vue'),
        meta: { title: '公告管理', subtitle: '统一维护社区公告发布' }
      },
      {
        path: 'visitor/record',
        component: () => import('@/views/visitor/VisitorRecord.vue'),
        meta: { title: '访客管理', subtitle: '查看访客预约与核验记录' }
      },
      {
        path: 'worker/manage',
        component: () => import('@/views/worker/WorkerManage.vue'),
        meta: { title: '维修人员管理', subtitle: '管理服务人员、排班状态与绩效表现' }
      },
      {
        path: 'express/manage',
        component: () => import('@/views/express/ExpressManage.vue'),
        meta: { title: '快递管理', subtitle: '预留快递收发、包裹入库与取件管理入口' }
      },
      {
        path: 'facility/manage',
        component: () => import('@/views/facility/FacilityManage.vue'),
        meta: { title: '设施管理', subtitle: '预留社区设施台账、状态巡检与维护记录入口' }
      },
      {
        path: 'system/role',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色权限管理', subtitle: '配置系统角色与资源访问范围' }
      },
      {
        path: 'system/log',
        component: () => import('@/views/system/Log.vue'),
        meta: { title: '系统日志管理', subtitle: '审计后台关键操作行为' }
      },
      {
        path: 'system/points-recharge',
        component: () => import('@/views/system/PointsRechargeRecord.vue'),
        meta: { title: '积分充值管理', subtitle: '按业主与时间范围查询充值流水' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, _, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const isPropertyAdmin = Number(userInfo?.role) === 2
  if (token && !isPropertyAdmin) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    if (to.path !== '/login') return next('/login')
  }
  if (to.path !== '/login' && !token) return next('/login')
  if (to.path === '/login' && token && isPropertyAdmin) return next('/dashboard')
  next()
})

export default router
