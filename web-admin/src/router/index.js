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
        meta: { title: '系统概览' }
      },
      {
        path: 'house/building',
        component: () => import('@/views/house/RealEstateArchive.vue'),
        meta: { title: '住户信息管理' }
      },
      {
        path: 'house/property',
        component: () => import('@/views/house/RealEstateArchive.vue'),
        meta: { title: '住户信息管理' }
      },
      {
        path: 'house/archive',
        component: () => import('@/views/house/RealEstateArchive.vue'),
        meta: { title: '住户信息管理' }
      },
      {
        path: 'house/owner',
        redirect: '/house/archive'
      },
      {
        path: 'repair/list',
        component: () => import('@/views/repair/SmartWorkOrder.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'repair/work-order',
        component: () => import('@/views/repair/SmartWorkOrder.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'repair/smart',
        component: () => import('@/views/repair/SmartWorkOrder.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'parking/manage',
        component: () => import('@/views/parking/ParkingManage.vue'),
        meta: { title: '停车管理' }
      },
      {
        path: 'operation/tasks',
        component: () => import('@/views/operation/TaskList.vue'),
        meta: { title: '催缴任务管理' }
      },
      {
        path: 'credit/log',
        component: () => import('@/views/credit/CreditLog.vue'),
        meta: { title: '信用记录管理' }
      },
      {
        path: 'recommend/rules',
        component: () => import('@/views/recommend/RuleConfig.vue'),
        meta: { title: '推荐规则管理' }
      },
      {
        path: 'activity/manage',
        component: () => import('@/views/activity/ActivityManage.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'neighbor/image-audit',
        component: () => import('@/views/neighbor/SecondHandImageAudit.vue'),
        meta: { title: '二手商品图片审核' }
      },
      {
        path: 'fee/manage',
        component: () => import('@/views/fee/FeeManage.vue'),
        meta: { title: '费用管理' }
      },
      {
        path: 'innovation/ops',
        component: () => import('@/views/innovation/InnovationOps.vue'),
        meta: { title: '运营管理' }
      },
      {
        path: 'notice/manage',
        component: () => import('@/views/notice/NoticeManage.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'visitor/record',
        component: () => import('@/views/visitor/VisitorRecord.vue'),
        meta: { title: '访客管理' }
      },
      {
        path: 'service/additional',
        component: () => import('@/views/service/AdditionalServiceManage.vue'),
        meta: { title: '附加服务管理' }
      },
      {
        path: 'worker/manage',
        component: () => import('@/views/worker/WorkerManage.vue'),
        meta: { title: '维修人员管理' }
      },
      {
        path: 'express/manage',
        component: () => import('@/views/express/ExpressManage.vue'),
        meta: { title: '快递管理' }
      },
      {
        path: 'facility/manage',
        component: () => import('@/views/facility/FacilityManage.vue'),
        meta: { title: '设施管理' }
      },
      {
        path: 'system/role',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色权限管理' }
      },
      {
        path: 'system/log',
        component: () => import('@/views/system/Log.vue'),
        meta: { title: '系统日志管理' }
      },
      {
        path: 'system/points-recharge',
        component: () => import('@/views/system/PointsRechargeRecord.vue'),
        meta: { title: '积分充值管理' }
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
  const isPropertyAdmin = [2, 4].includes(Number(userInfo?.role))
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
