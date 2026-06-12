<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '230px'" class="aside">
      <div class="logo">
        <el-icon :size="24"><Monitor /></el-icon>
        <span v-show="!collapsed">机房预约系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        router
        class="menu"
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-icon class="collapse-btn" @click="collapsed = !collapsed">
          <Fold v-if="!collapsed" /><Expand v-else />
        </el-icon>
        <span class="header-title">{{ currentTitle }}</span>
        <div class="spacer" />
        <el-dropdown @command="onCommand">
          <span class="user-chip">
            <el-avatar :size="30" class="avatar">{{ avatarText }}</el-avatar>
            <span class="user-name">{{ auth.user?.name || auth.user?.username }}</span>
            <el-tag size="small" effect="plain">{{ auth.user?.role_display }}</el-tag>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)

const menus = computed(() =>
  router.getRoutes()
    .filter((r) => r.meta?.title && r.path !== '/')
    .filter((r) => {
      const roles = r.meta.roles as string[] | undefined
      return !roles || (auth.role && roles.includes(auth.role))
    })
    .map((r) => ({
      path: r.path,
      title: r.meta.title as string,
      icon: r.meta.icon as string,
    })),
)

const currentTitle = computed(() => (route.meta.title as string) || '')
const avatarText = computed(
  () => (auth.user?.name || auth.user?.username || '?').slice(0, 1),
)

async function onCommand(cmd: string) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
    await auth.logout()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #0f172a;
  transition: width 0.2s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  white-space: nowrap;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.menu {
  border-right: none;
  padding: 8px;
}
.menu :deep(.el-menu-item) {
  border-radius: var(--radius-sm);
  margin-bottom: 4px;
  height: 44px;
}
.menu :deep(.el-menu-item.is-active) {
  background: var(--color-primary);
}
.header {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-bottom: 1px solid var(--color-border);
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--color-muted-foreground);
}
.header-title {
  font-weight: 600;
  font-size: 16px;
}
.spacer {
  flex: 1;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}
.avatar {
  background: var(--color-primary);
}
.user-name {
  font-weight: 500;
}
.main {
  background: var(--color-background);
  padding: 24px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
