<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">占用日历</h2>
      <div style="display: flex; align-items: center; gap: 12px">
        <el-select v-model="roomId" placeholder="全部机房" clearable style="width: 200px" @change="reload">
          <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <span class="legend"><i style="background:#409EFF"></i>预约</span>
        <span class="legend"><i style="background:#E6A23C"></i>排课</span>
      </div>
    </div>

    <div class="page-card">
      <FullCalendar ref="calRef" :options="options" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import zhLocale from '@fullcalendar/core/locales/zh-cn'
import { roomsApi, statsApi } from '@/api'

const rooms = ref<any[]>([])
const roomId = ref<number>()
const calRef = ref<any>()

const options = ref<any>({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  locale: zhLocale,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay',
  },
  slotMinTime: '07:00:00',
  slotMaxTime: '23:00:00',
  height: 'auto',
  allDaySlot: false,
  events: fetchEvents,
})

async function fetchEvents(info: any, success: (e: any[]) => void, failure: (e: any) => void) {
  try {
    const params: any = { start: info.startStr.slice(0, 10), end: info.endStr.slice(0, 10) }
    if (roomId.value) params.room = roomId.value
    const { data } = await statsApi.calendar(params)
    success(data)
  } catch (e) {
    failure(e)
  }
}

function reload() {
  calRef.value?.getApi().refetchEvents()
}

onMounted(async () => {
  const { data } = await roomsApi.list({ page_size: 100 })
  rooms.value = data.results
})
</script>

<style scoped>
.legend {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-muted-foreground);
}
.legend i {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  display: inline-block;
}
:deep(.fc) {
  --fc-button-bg-color: var(--color-primary);
  --fc-button-border-color: var(--color-primary);
  --fc-button-hover-bg-color: var(--color-primary-dark);
  --fc-button-hover-border-color: var(--color-primary-dark);
  --fc-button-active-bg-color: var(--color-primary-dark);
  --fc-today-bg-color: rgba(37, 99, 235, 0.06);
}
</style>
