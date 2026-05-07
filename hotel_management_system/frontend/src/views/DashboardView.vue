<template>
  <div class="dashboard">
    <!-- KPI 卡片 -->
    <div class="kpi-grid">
      <div class="kpi-card" v-for="(kpi, idx) in kpis" :key="idx">
        <div class="kpi-icon-wrap" :style="{ background: kpi.bg }">
          <el-icon :size="24" color="#fff"><component :is="kpi.icon" /></el-icon>
        </div>
        <div class="kpi-body">
          <div class="label">{{ kpi.label }}</div>
          <div class="value">{{ kpi.value }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <el-card shadow="never" class="chart-box">
        <template #header><span class="chart-title">近30天每日营收趋势</span></template>
        <div ref="lineRef" class="chart-canvas"></div>
      </el-card>
      <el-card shadow="never" class="chart-box">
        <template #header><span class="chart-title">近30天每日营收趋势</span></template>
        <div ref="pieRef" class="chart-canvas"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from "vue";
import * as echarts from "echarts";
import http from "../api/http";

const lineRef = ref(null);
const pieRef  = ref(null);
let lineChart = null;
let pieChart  = null;

const data = ref({});
const kpis = computed(() => [
  { label: "客房总数", value: data.value.totalRooms ?? "--", icon: "House", bg: "linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)" },
  { label: "当前在住", value: data.value.inHouseCount ?? "--", icon: "User", bg: "linear-gradient(135deg, #f59e0b 0%, #d97706 100%)" },
  { label: "今日新单", value: data.value.todayOrderCount ?? "--", icon: "Document", bg: "linear-gradient(135deg, #10b981 0%, #059669 100%)" },
  { label: "30天入住率", value: occupancy.value, icon: "TrendCharts", bg: "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)" }
]);

const occupancy = computed(() => {
  const total = data.value.totalRooms || 0;
  const inHouse = data.value.inHouseCount || 0;
  if (!total) return "--";
  return ((inHouse / total) * 100).toFixed(1) + "%";
});

onMounted(async () => {
  const res = await http.get("/reports/dashboard");
  data.value = res.data;
  await nextTick();
  renderLine(data.value.daily30 || []);
  renderPie(data.value.roomTypeRevenue || []);
});

function renderLine(daily) {
  lineChart = echarts.init(lineRef.value);
  lineChart.setOption({
    tooltip: { 
      trigger: "axis",
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(255,255,255,1)',
      textStyle: { color: '#1e293b', fontFamily: 'Inter' },
      extraCssText: 'box-shadow: 0 4px 15px rgba(0,0,0,0.1); backdrop-filter: blur(10px); border-radius: 8px;'
    },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: { 
      type: "category", 
      data: daily.map(d => d.day), 
      axisLabel: { fontSize: 12, fontFamily: 'Outfit', color: '#64748b' },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    yAxis: { 
      type: "value", 
      axisLabel: { fontSize: 12, fontFamily: 'Outfit', color: '#64748b' },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      data: daily.map(d => d.amount || 0),
      type: "line", smooth: true,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "rgba(79,70,229,.4)" },
        { offset: 1, color: "rgba(79,70,229,.0)" }
      ]) },
      lineStyle: { color: "#4f46e5", width: 3 },
      itemStyle: { color: "#4f46e5", borderWidth: 2, borderColor: '#fff' },
      symbol: 'circle', symbolSize: 8, showSymbol: false
    }]
  });
}

function renderPie(items) {
  pieChart = echarts.init(pieRef.value);
  pieChart.setOption({
    tooltip: { 
      trigger: "item",
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(255,255,255,1)',
      textStyle: { color: '#1e293b', fontFamily: 'Inter' },
      extraCssText: 'box-shadow: 0 4px 15px rgba(0,0,0,0.1); backdrop-filter: blur(10px); border-radius: 8px;'
    },
    legend: { bottom: 0, textStyle: { fontSize: 12, fontFamily: 'Outfit', color: '#475569' }, icon: 'circle' },
    color: ['#4f46e5', '#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6'],
    series: [{
      type: "pie", radius: ["45%", "70%"], center: ["50%", "42%"],
      label: { formatter: "{b}\n{d}%", fontSize: 12, fontFamily: 'Outfit' },
      data: items.map(i => ({ name: i.typeName || i.TYPE_NAME, value: i.amount || i.AMOUNT || 0 })),
      itemStyle: { borderRadius: 8, borderColor: "#fff", borderWidth: 3 }
    }]
  });
}

function handleResize() {
  lineChart?.resize();
  pieChart?.resize();
}

onMounted(() => window.addEventListener("resize", handleResize));
onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  lineChart?.dispose();
  pieChart?.dispose();
});
</script>

<style scoped>
.kpi-icon-wrap {
  width: 60px; height: 60px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
}
.kpi-icon-wrap::after {
  content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 60%);
}
.chart-title { font-weight: 800; font-size: 17px; color: #0f172a; letter-spacing: 0.2px; font-family: 'Outfit', sans-serif;}

@media (max-width: 1100px) {
  .chart-row { grid-template-columns: 1fr; }
}
</style>
