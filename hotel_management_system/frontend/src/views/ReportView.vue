<template>
  <div class="reports">
    <div class="toolbar">
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" @change="onDateChange" style="width:280px" />
      <el-button type="primary" @click="loadAll"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button @click="exportDaily"><el-icon><Download /></el-icon>导出每日营收</el-button>
      <el-button @click="window.print()" class="no-print"><el-icon><Printer /></el-icon>打印</el-button>
    </div>

    <div class="chart-row">
      <el-card shadow="never" class="chart-box">
        <template #header><span class="chart-title">每日营收趋势</span></template>
        <div ref="lineRef" class="chart-canvas"></div>
      </el-card>
      <el-card shadow="never" class="chart-box">
        <template #header><span class="chart-title">房型收益占比</span></template>
        <div ref="pieRef" class="chart-canvas"></div>
      </el-card>
    </div>

    <el-card shadow="never" style="margin-top:16px; border-radius:12px">
      <template #header><span class="chart-title">每日明细表</span></template>
      <el-table :data="daily" stripe>
        <el-table-column prop="day" label="日期" min-width="130" sortable />
        <el-table-column prop="orderCount" label="订单数" min-width="100" sortable />
        <el-table-column prop="amount" label="营收 (¥)" min-width="140" sortable>
          <template #default="{ row }">
            <span class="bold">{{ Number(row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import * as echarts from "echarts";
import http from "../api/http";

const lineRef = ref(null);
const pieRef  = ref(null);
let lineChart = null;
let pieChart  = null;

const dateRange = ref(null);
const daily = ref([]);
const byType = ref([]);

let startDate = null;
let endDate = null;

onMounted(loadAll);

function onDateChange(v) {
  startDate = v?.[0] || null;
  endDate   = v?.[1] || null;
}

async function loadAll() {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;

  const [d, t] = await Promise.all([
    http.get("/reports/daily", { params }),
    http.get("/reports/by-type", { params })
  ]);
  daily.value  = d.data || [];
  byType.value = t.data || [];

  await nextTick();
  renderLine();
  renderPie();
}

function renderLine() {
  if (!lineChart) lineChart = echarts.init(lineRef.value);
  lineChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: "category", data: daily.value.map(d => d.day), axisLabel: { fontSize: 11 } },
    yAxis: { type: "value", axisLabel: { fontSize: 11 } },
    series: [{
      data: daily.value.map(d => d.amount || 0),
      type: "bar",
      barWidth: "50%",
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: "#6366f1" },
          { offset: 1, color: "#a855f7" }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  });
}

function renderPie() {
  if (!pieChart) pieChart = echarts.init(pieRef.value);
  pieChart.setOption({
    tooltip: { trigger: "item" },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    series: [{
      type: "pie", radius: ["40%", "65%"], center: ["50%", "45%"],
      label: { formatter: "{b}\n{d}%", fontSize: 11 },
      data: byType.value.map(i => ({ name: i.typeName || i.TYPE_NAME, value: i.amount || i.AMOUNT || 0 })),
      itemStyle: { borderRadius: 6, borderColor: "#fff", borderWidth: 2 }
    }]
  });
}

function handleResize() { lineChart?.resize(); pieChart?.resize(); }
onMounted(() => window.addEventListener("resize", handleResize));
onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  lineChart?.dispose();
  pieChart?.dispose();
});

function exportDaily() {
  const params = new URLSearchParams();
  if (startDate) params.set("startDate", startDate);
  if (endDate) params.set("endDate", endDate);
  window.open(`/api/reports/daily/export?${params.toString()}`);
}
</script>

<style scoped>
.chart-row { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
.chart-box { border-radius: 14px; }
.chart-title { font-weight: 600; font-size: 15px; color: #1e293b; }
.chart-canvas { height: 340px; }
@media (max-width: 1100px) { .chart-row { grid-template-columns: 1fr; } }
</style>
