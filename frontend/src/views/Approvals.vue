<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">审批管理</h2>
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="pending">待审批</el-radio-button>
        <el-radio-button value="approved">已通过</el-radio-button>
        <el-radio-button value="rejected">已驳回</el-radio-button>
        <el-radio-button value="">全部</el-radio-button>
      </el-radio-group>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="applicant_name" label="申请人" min-width="110" />
        <el-table-column prop="room_name" label="机房" min-width="130" />
        <el-table-column label="机位" width="90">
          <template #default="{ row }">{{ row.seat_code || '整间' }}</template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="时间" min-width="150">
          <template #default="{ row }">{{ timeText(row) }}</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status_display }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button link type="success" @click="act(row, 'approve')">通过</el-button>
              <el-button link type="danger" @click="act(row, 'reject')">驳回</el-button>
            </template>
            <span v-else class="muted">{{ row.reviewer_name || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookingsApi } from '@/api'

const rows = ref<any[]>([])
const loading = ref(false)
const status = ref('pending')

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
    const params: any = { page_size: 100, ordering: '-created_at' }
    if (status.value) params.status = status.value
    const { data } = await bookingsApi.list(params)
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

async function act(row: any, type: 'approve' | 'reject') {
  const { value } = await ElMessageBox.prompt(
    type === 'approve' ? '通过该预约？可填写意见' : '驳回该预约？请填写原因',
    type === 'approve' ? '通过预约' : '驳回预约',
    { inputType: 'textarea', inputValue: '', confirmButtonText: '确定', cancelButtonText: '取消' },
  )
  if (type === 'approve') await bookingsApi.approve(row.id, value || '')
  else await bookingsApi.reject(row.id, value || '')
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
