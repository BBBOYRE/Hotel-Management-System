<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.orderId" placeholder="订单ID" clearable style="width:120px" @keyup.enter="load" />
      <el-select v-model="query.itemType" placeholder="款项类型" clearable style="width:130px" @change="load">
        <el-option v-for="d in dictStore.items('BILL_TYPE')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" @change="onDateChange" style="width:260px" />
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>录入账目</el-button>
      <el-button @click="openSettle"><el-icon><Money /></el-icon>结算</el-button>
      <el-button @click="openShift"><el-icon><Printer /></el-icon>交接班</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="itemId" label="ID" min-width="70" />
        <el-table-column prop="orderId" label="订单ID" min-width="90" />
        <el-table-column label="类型" min-width="100">
          <template #default="{ row }">{{ dictStore.label('BILL_TYPE', row.itemType) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" min-width="120">
          <template #default="{ row }">
            <span :class="row.amount < 0 ? 'text-red' : 'text-green'" class="bold">{{ row.amount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="operatorName" label="收银员" min-width="100" />
        <el-table-column prop="recordTime" label="时间" min-width="170" />
        <el-table-column label="操作" fixed="right" min-width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt" layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="load"
      />
    </el-card>

    <!-- 录入 -->
    <el-dialog v-model="addVisible" title="录入账目" width="480px" append-to-body>
      <el-form :model="addForm" label-width="90px">
        <el-form-item label="订单ID"><el-input-number v-model="addForm.orderId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="款项类型">
          <el-select v-model="addForm.itemType" style="width:100%">
            <el-option v-for="d in dictStore.items('BILL_TYPE')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="addForm.amount" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="addForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 结算 -->
    <el-dialog v-model="settleVisible" title="订单结算" width="400px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="订单ID"><el-input-number v-model="settleOrderId" :min="1" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSettle">结算</el-button>
      </template>
    </el-dialog>

    <!-- 交接班 -->
    <el-dialog v-model="shiftVisible" title="交接班报表" width="600px" append-to-body>
      <el-form :inline="true" label-width="80px" style="margin-bottom:12px">
        <el-form-item label="开始时间">
          <el-date-picker v-model="shiftForm.startDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:200px" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="shiftForm.endDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:200px" />
        </el-form-item>
        <el-button type="primary" @click="loadShift">查询</el-button>
      </el-form>
      <div v-if="shiftData" class="shift-report">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总收入">¥{{ shiftData.income?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="总退款">¥{{ shiftData.refund?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="净收入">¥{{ shiftData.net?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="笔数">{{ shiftData.count }}</el-descriptions-item>
        </el-descriptions>
        <el-button class="print-btn no-print" type="primary" @click="window.print()"><el-icon><Printer /></el-icon>打印</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import http from "../api/http";
import { useDictStore } from "../stores/dict";
import { ElMessage, ElMessageBox } from "element-plus";

const dictStore = useDictStore();
const list  = ref([]);
const total = ref(0);
const dateRange = ref(null);
const query = reactive({ orderId: null, itemType: null, startDate: null, endDate: null, pageNum: 1, pageSize: 10 });

const addVisible = ref(false);
const addForm = ref({ orderId: null, itemType: null, amount: 0, remark: "" });

const settleVisible = ref(false);
const settleOrderId = ref(null);

const shiftVisible = ref(false);
const shiftForm = ref({ startDate: "", endDate: "" });
const shiftData = ref(null);

onMounted(load);

async function load() {
  const r = await http.get("/bills", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function onDateChange(v) {
  query.startDate = v?.[0] || null;
  query.endDate   = v?.[1] || null;
  load();
}

function openAdd() {
  addForm.value = { orderId: null, itemType: null, amount: 0, remark: "" };
  addVisible.value = true;
}

async function submitAdd() {
  await http.post("/bills", addForm.value);
  ElMessage.success("录入成功");
  addVisible.value = false;
  load();
}

async function del(row) {
  await ElMessageBox.confirm("确定删除该账目？", "提示", { type: "warning" });
  await http.delete(`/bills/${row.itemId}`);
  ElMessage.success("已删除");
  load();
}

function openSettle() {
  settleOrderId.value = null;
  settleVisible.value = true;
}

async function submitSettle() {
  if (!settleOrderId.value) return ElMessage.warning("请输入订单ID");
  const r = await http.post("/bills/settle", { orderId: settleOrderId.value });
  ElMessage.success(`结算完成，总额 ¥${r.data?.toFixed(2)}`);
  settleVisible.value = false;
  load();
}

function openShift() {
  shiftData.value = null;
  const now = new Date();
  const today = now.toISOString().slice(0, 10);
  shiftForm.value = { startDate: `${today} 00:00:00`, endDate: `${today} 23:59:59` };
  shiftVisible.value = true;
}

async function loadShift() {
  const r = await http.get("/bills/shift", { params: shiftForm.value });
  shiftData.value = r.data;
}
</script>

<style scoped>
.mt { margin-top: 14px; }
.text-red { color: #f56c6c; }
.text-green { color: #67c23a; }
.shift-report { margin-top: 14px; }
.print-btn { margin-top: 14px; }
</style>
