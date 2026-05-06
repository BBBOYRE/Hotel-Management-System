<template>
  <div class="dashboard">
    <!-- KPI 卡片 -->
    <div class="kpi-grid">
      <div class="kpi-card" v-for="(kpi, idx) in kpis" :key="idx">
        <div class="kpi-icon-wrap" :style="{ background: kpi.bg }">
          <el-icon :size="22" color="#fff"><component :is="kpi.icon" /></el-icon>
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
        <template #header><span class="chart-title">房型收益占比</span></template>
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
  { label: "客房总数", value: data.value.totalRooms ?? "--", icon: "House", bg: "linear-gradient(135deg,#6366f1,#818cf8)" },
  { label: "当前在住", value: data.value.inHouseCount ?? "--", icon: "User", bg: "linear-gradient(135deg,#f59e0b,#fbbf24)" },
  { label: "今日新单", value: data.value.todayOrderCount ?? "--", icon: "Document", bg: "linear-gradient(135deg,#10b981,#34d399)" },
  { label: "30天入住率", value: occupancy.value, icon: "TrendCharts", bg: "linear-gradient(135deg,#ef4444,#f87171)" }
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
    tooltip: { trigger: "axis" },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: "category", data: daily.map(d => d.day), axisLabel: { fontSize: 11 } },
    yAxis: { type: "value", axisLabel: { fontSize: 11 } },
    series: [{
      data: daily.map(d => d.amount || 0),
      type: "line", smooth: true,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "rgba(99,102,241,.35)" },
        { offset: 1, color: "rgba(99,102,241,.02)" }
      ]) },
      lineStyle: { color: "#6366f1", width: 2.5 },
      itemStyle: { color: "#6366f1" }
    }]
  });
}

function renderPie(items) {
  pieChart = echarts.init(pieRef.value);
  pieChart.setOption({
    tooltip: { trigger: "item" },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    series: [{
      type: "pie", radius: ["40%", "65%"], center: ["50%", "45%"],
      label: { formatter: "{b}\n{d}%", fontSize: 11 },
      data: items.map(i => ({ name: i.typeName || i.TYPE_NAME, value: i.amount || i.AMOUNT || 0 })),
      itemStyle: { borderRadius: 6, borderColor: "#fff", borderWidth: 2 }
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
.dashboard { }
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.kpi-card {
  display: flex; align-items: center; gap: 16px;
  background: #fff; padding: 22px 24px; border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,.04);
  transition: transform .15s, box-shadow .15s;
}
.kpi-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,.08); }
.kpi-icon-wrap {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.kpi-body .label { font-size: 13px; color: #909399; }
.kpi-body .value { font-size: 28px; font-weight: 700; color: #1e293b; margin-top: 2px; }
.chart-row { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
.chart-box { border-radius: 14px; }
.chart-title { font-weight: 600; font-size: 15px; color: #1e293b; }
.chart-canvas { height: 340px; }

@media (max-width: 1100px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-row { grid-template-columns: 1fr; }
}
</style>
