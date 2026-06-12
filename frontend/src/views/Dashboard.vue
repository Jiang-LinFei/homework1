<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">仪表盘</h2>
      <span class="muted">机房预约总览</span>
    </div>

    <el-row :gutter="16">
      <el-col :xs="12" :sm="6" v-for="c in cards" :key="c.label">
        <div class="stat-card" :style="{ '--accent': c.color }">
          <div class="stat-icon"><el-icon :size="22"><component :is="c.icon" /></el-icon></div>
          <div>
            <div class="stat-value">{{ c.value }}</div>
            <div class="stat-label muted">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="14">
        <div class="page-card">
          <div class="card-head">近 7 日预约趋势</div>
          <div ref="trendEl" class="chart"></div>
        </div>
      </el-col>
      <el-col :xs="24" :md="10">
        <div class="page-card">
          <div class="card-head">预约状态分布</div>
          <div ref="statusEl" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <div class="page-card">
          <div class="card-head">机房预约 Top5</div>
          <div ref="roomEl" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { Tickets, CircleCheck, Clock, TrendCharts } from '@element-plus/icons-vue'
import { statsApi } from '@/api'

const data = ref<any>({ total: 0, by_status: {}, approval_rate: 0, top_rooms: [], trend: [] })
const trendEl = ref<HTMLElement>()
const statusEl = ref<HTMLElement>()
const roomEl = ref<HTMLElement>()
const charts = shallowRef<echarts.ECharts[]>([])

const cards = computed(() => [
  { label: '预约总数', value: data.value.total, icon: Tickets, color: '#2563eb' },
  { label: '已通过', value: data.value.by_status.approved || 0, icon: CircleCheck, color: '#059669' },
  { label: '待审批', value: data.value.by_status.pending || 0, icon: Clock, color: '#e6a23c' },
  { label: '通过率', value: data.value.approval_rate + '%', icon: TrendCharts, color: '#3b82f6' },
])

function render() {
  const trend = echarts.init(trendEl.value!)
  trend.setOption({
    grid: { left: 36, right: 16, top: 24, bottom: 28 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.value.trend.map((t: any) => t.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line', smooth: true, data: data.value.trend.map((t: any) => t.count),
      areaStyle: { color: 'rgba(37,99,235,0.12)' }, lineStyle: { color: '#2563eb' },
      itemStyle: { color: '#2563eb' },
    }],
  })

  const status = echarts.init(statusEl.value!)
  const s = data.value.by_status
  status.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
      data: [
        { name: '待审批', value: s.pending || 0, itemStyle: { color: '#e6a23c' } },
        { name: '已通过', value: s.approved || 0, itemStyle: { color: '#059669' } },
        { name: '已驳回', value: s.rejected || 0, itemStyle: { color: '#dc2626' } },
        { name: '已取消', value: s.cancelled || 0, itemStyle: { color: '#94a3b8' } },
      ],
    }],
  })

  const room = echarts.init(roomEl.value!)
  room.setOption({
    grid: { left: 100, right: 24, top: 16, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: data.value.top_rooms.map((r: any) => r.name) },
    series: [{
      type: 'bar', data: data.value.top_rooms.map((r: any) => r.count),
      itemStyle: { color: '#2563eb', borderRadius: [0, 6, 6, 0] }, barWidth: 18,
    }],
  })

  charts.value = [trend, status, room]
}

function onResize() {
  charts.value.forEach((c) => c.resize())
}

onMounted(async () => {
  const { data: d } = await statsApi.dashboard()
  data.value = d
  render()
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => window.removeEventListener('resize', onResize))
</script>

<style scoped>
.stat-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, white);
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
}
.stat-label {
  font-size: 13px;
}
.card-head {
  font-weight: 600;
  margin-bottom: 12px;
}
.chart {
  height: 300px;
}
</style>
