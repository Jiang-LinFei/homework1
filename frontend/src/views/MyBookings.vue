<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">我的预约</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/apply')">申请预约</el-button>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="room_name" label="机房" min-width="130" />
        <el-table-column label="机位" width="90">
          <template #default="{ row }">{{ row.seat_code || '整间' }}</template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="时间" min-width="150">
          <template #default="{ row }">{{ timeText(row) }}</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status_display }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="review_comment" label="审批意见" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="['pending','approved'].includes(row.status)"
              link type="danger" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { bookingsApi } from '@/api'

const rows = ref<any[]>([])
const loading = ref(false)

function statusType(s: string) {
  return { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }[s] || ''
}
function timeText(row: any) {
  if (row.timeslot_name) return row.timeslot_name
  if (row.start_time) return `${row.start_time}-${row.end_time}`
  return '-'
}

async function load() {
  loading.value = true
  try {
    const { data } = await bookingsApi.list({ page_size: 100, ordering: '-created_at' })
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

async function cancel(row: any) {
  await ElMessageBox.confirm('确定取消该预约？', '提示', { type: 'warning' })
  await bookingsApi.cancel(row.id)
  ElMessage.success('已取消')
  load()
}

onMounted(load)
</script>
