<template>
  <div class="dashboard">
    <!-- 虚拟时钟工具条（仅管理员可见） -->
    <el-card v-if="auth.isAdmin" shadow="never" class="clock-bar">
      <div class="clock-row">
        <div class="clock-info">
          <el-icon :size="18"><Clock /></el-icon>
          <span class="clock-label">虚拟时间</span>
          <span class="clock-value">{{ clockNowText }}</span>
          <el-tag v-if="Math.abs(offsetDays) > 0.0001" size="small" type="warning" effect="plain">
            偏移 {{ offsetDays > 0 ? '+' : '' }}{{ offsetDays.toFixed(2) }} 天
          </el-tag>
          <el-tag v-else size="small" type="success" effect="plain">实时</el-tag>
        </div>
        <div class="clock-actions">
          <el-button size="small" @click="advanceClock(1/24)">+1小时</el-button>
          <el-button size="small" @click="advanceClock(1)">+1天</el-button>
          <el-button size="small" @click="advanceClock(7)">+7天</el-button>
          <el-button size="small" @click="advanceClock(30)">+30天</el-button>
          <el-button size="small" @click="openSetClock">设为...</el-button>
          <el-button size="small" type="danger" plain @click="resetClock">重置</el-button>
        </div>
      </div>
    </el-card>

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
        <template #header><span class="chart-title">近30天房型收益占比</span></template>
        <div ref="pieRef" class="chart-canvas"></div>
      </el-card>
    </div>

    <!-- 设定虚拟时间 -->
    <el-dialog v-model="setVisible" title="设定虚拟时间" width="420px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="目标时间">
          <el-date-picker
            v-model="setTarget"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择目标时间"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="setVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSetClock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from "vue";
import * as echarts from "echarts";
import http from "../api/http";
import { useAuthStore } from "../stores/auth";
import { ElMessage } from "element-plus";

const auth = useAuthStore();

const lineRef = ref(null);
const pieRef  = ref(null);
let lineChart = null;
let pieChart  = null;

const data = ref({});
const kpis = computed(() => [
  { label: "客房总数", value: data.value.totalRooms ?? "--", icon: "House", bg: "linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)" },
  { label: "当前在住", value: data.value.inHouseCount ?? "--", icon: "User", bg: "linear-gradient(135deg, #f59e0b 0%, #d97706 100%)" },
  { label: "今日新单", value: data.value.todayOrderCount ?? "--", icon: "Document", bg: "linear-gradient(135deg, #10b981 0%, #059669 100%)" },
  { label: "30天入住率", value: occupancyText.value, icon: "TrendCharts", bg: "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)" }
]);

const occupancyText = computed(() => {
  const r = data.value.occupancy30;
  if (r == null) return "--";
  return (r * 100).toFixed(1) + "%";
});

/* ---------- 虚拟时钟 ---------- */
const offsetDays = ref(0);
const clockNow   = ref("");
const clockNowText = computed(() => clockNow.value || "--");
const setVisible = ref(false);
const setTarget  = ref("");
let clockTimer = null;

async function loadClock() {
  try {
    const r = await http.get("/system/clock");
    offsetDays.value = r.data?.offsetDays || 0;
    clockNow.value = r.data?.now || "";
  } catch (e) { /* 非管理员或未登录 */ }
}
async function advanceClock(days) {
  const r = await http.post("/system/clock/advance", { days });
  offsetDays.value = r.data?.offsetDays || 0;
  clockNow.value = r.data?.now || "";
  ElMessage.success("时钟已推进");
  await reloadDashboard();
}
function openSetClock() {
  setTarget.value = "";
  setVisible.value = true;
}
async function submitSetClock() {
  if (!setTarget.value) return ElMessage.warning("请选择目标时间");
  const r = await http.post("/system/clock/set", { target: setTarget.value });
  offsetDays.value = r.data?.offsetDays || 0;
  clockNow.value = r.data?.now || "";
  setVisible.value = false;
  ElMessage.success("时钟已设定");
  await reloadDashboard();
}
async function resetClock() {
  await http.post("/system/clock/reset");
  await loadClock();
  ElMessage.success("已恢复实时");
  await reloadDashboard();
}

async function reloadDashboard() {
  const res = await http.get("/reports/dashboard");
  data.value = res.data;
  await nextTick();
  renderLine(data.value.daily30 || []);
  renderPie(data.value.roomTypeRevenue || []);
}

onMounted(async () => {
  await reloadDashboard();
  if (auth.isAdmin) {
    await loadClock();
    // 每 30 秒刷新一次时钟显示
    clockTimer = setInterval(loadClock, 30000);
  }
});

function renderLine(daily) {
  if (lineChart) lineChart.dispose();
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
  if (pieChart) pieChart.dispose();
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
      type: "pie", radius: ["40%", "55%"], center: ["50%", "42%"],
      label: { formatter: "{b}\n{d}%", fontSize: 12, fontFamily: 'Outfit', overflow: 'break' },
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
  if (clockTimer) clearInterval(clockTimer);
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

.clock-bar { margin-bottom: 16px; border-radius: 12px; }
.clock-row { display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap; }
.clock-info { display: flex; align-items: center; gap: 10px; }
.clock-label { color: #64748b; font-size: 13px; }
.clock-value { font-weight: 700; color: #0f172a; font-family: 'Outfit', sans-serif; font-size: 15px; }
.clock-actions { display: flex; gap: 6px; flex-wrap: wrap; }

@media (max-width: 1100px) {
  .chart-row { grid-template-columns: 1fr; }
}
</style>
