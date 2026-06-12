import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
      {
        path: 'calendar',
        name: 'calendar',
        component: () => import('@/views/CalendarView.vue'),
        meta: { title: '占用日历', icon: 'Calendar' },
      },
      {
        path: 'apply',
        name: 'apply',
        component: () => import('@/views/BookingApply.vue'),
        meta: { title: '申请预约', icon: 'EditPen' },
      },
      {
        path: 'my-bookings',
        name: 'my-bookings',
        component: () => import('@/views/MyBookings.vue'),
        meta: { title: '我的预约', icon: 'Tickets' },
      },
      {
        path: 'approvals',
        name: 'approvals',
        component: () => import('@/views/Approvals.vue'),
        meta: { title: '审批管理', icon: 'CircleCheck', roles: ['admin', 'teacher'] },
      },
      {
        path: 'rooms',
        name: 'rooms',
        component: () => import('@/views/Rooms.vue'),
        meta: { title: '机房管理', icon: 'OfficeBuilding', roles: ['admin'] },
      },
      {
        path: 'seats',
        name: 'seats',
        component: () => import('@/views/Seats.vue'),
        meta: { title: '机位管理', icon: 'Monitor', roles: ['admin'] },
      },
      {
        path: 'timeslots',
        name: 'timeslots',
        component: () => import('@/views/TimeSlots.vue'),
        meta: { title: '节次管理', icon: 'Clock', roles: ['admin'] },
      },
      {
        path: 'schedules',
        name: 'schedules',
        component: () => import('@/views/Schedules.vue'),
        meta: { title: '排课管理', icon: 'Notebook', roles: ['admin'] },
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['admin'] },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.name !== 'login' && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.isLoggedIn) {
    return { name: 'dashboard' }
  }
  const roles = to.meta.roles as string[] | undefined
  if (roles && auth.role && !roles.includes(auth.role)) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
