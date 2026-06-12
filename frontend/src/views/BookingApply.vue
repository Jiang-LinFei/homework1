<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">申请预约</h2>
      <span class="muted">提交时自动检测时间冲突</span>
    </div>

    <div class="page-card" style="max-width: 640px">
      <el-form :model="form" label-width="100px" ref="formRef">
        <el-form-item label="机房" required>
          <el-select v-model="form.room" placeholder="选择机房" @change="onRoomChange">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="机位">
          <el-select v-model="form.seat" clearable placeholder="不选=预约整间">
            <el-option v-for="s in seats" :key="s.id" :label="s.code" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD"
            :disabled-date="(d:Date)=> d < new Date(Date.now()-86400000)" />
        </el-form-item>
        <el-form-item label="预约方式">
          <el-radio-group v-model="mode">
            <el-radio-button value="slot">按节次</el-radio-button>
            <el-radio-button value="range">自定义时间</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="节次" v-if="mode === 'slot'" required>
          <el-select v-model="form.timeslot" placeholder="选择节次">
            <el-option v-for="s in slots" :key="s.id"
              :label="`${s.name} (${s.start_time}-${s.end_time})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <template v-else>
          <el-form-item label="开始时间" required>
            <el-time-picker v-model="form.start_time" value-format="HH:mm:ss" />
          </el-form-item>
          <el-form-item label="结束时间" required>
            <el-time-picker v-model="form.end_time" value-format="HH:mm:ss" />
          </el-form-item>
        </template>
        <el-form-item label="用途">
          <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="如：课程设计、上机实验" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">提交申请</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookingsApi, roomsApi, seatsApi, timeslotsApi } from '@/api'

const router = useRouter()
const rooms = ref<any[]>([])
const seats = ref<any[]>([])
const slots = ref<any[]>([])
const mode = ref<'slot' | 'range'>('slot')
const loading = ref(false)
const form = reactive<any>({ room: undefined, seat: undefined, date: '', timeslot: undefined })

watch(mode, () => {
  form.timeslot = undefined
  form.start_time = undefined
  form.end_time = undefined
})

async function onRoomChange() {
  form.seat = undefined
  seats.value = []
  if (form.room) {
    const { data } = await seatsApi.list({ room: form.room, status: 'available', page_size: 200 })
    seats.value = data.results
  }
}

async function submit() {
  if (!form.room || !form.date) {
    ElMessage.warning('请填写机房和日期')
    return
  }
  const payload: any = {
    room: form.room, seat: form.seat || null, date: form.date, purpose: form.purpose,
  }
  if (mode.value === 'slot') payload.timeslot = form.timeslot
  else { payload.start_time = form.start_time; payload.end_time = form.end_time }

  loading.value = true
  try {
    await bookingsApi.create(payload)
    ElMessage.success('提交成功，等待审批')
    router.push('/my-bookings')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const [r, s] = await Promise.all([
    roomsApi.list({ status: 'open', page_size: 100 }),
    timeslotsApi.list({ page_size: 100 }),
  ])
  rooms.value = r.data.results
  slots.value = s.data.results
})
</script>
