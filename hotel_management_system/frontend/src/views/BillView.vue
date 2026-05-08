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
      <el-button type="primary" @click="openAdd" v-perm="'bill:add'"><el-icon><Plus /></el-icon>录入账目</el-button>
      <el-button @click="openShift" v-perm="'bill:shift'"><el-icon><Printer /></el-icon>交接班</el-button>
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
        <el-table-column prop="recordTime" label="时间" min-width="200" />
        <el-table-column label="操作" fixed="right" min-width="100">
          <template #default="{ row }">
            <el-button link type="danger" v-perm="'bill:delete'" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt" layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="load"
      />
    </el-card>

    <!-- 录入：订单从下拉选择，避免外键违规 -->
    <el-dialog v-model="addVisible" title="录入账目" width="520px" append-to-body>
      <el-form :model="addForm" label-width="90px">
        <el-form-item label="订单">
          <el-select
            v-model="addForm.orderId"
            filterable remote :remote-method="searchOrders" :loading="orderLoading"
            placeholder="输入订单号 / 客户名搜索"
            style="width:100%"
          >
            <el-option
              v-for="o in orderOptions"
              :key="o.orderId"
              :value="o.orderId"
              :label="`${o.orderNo} - ${o.customerName || ''} (¥${(o.totalAmount||0).toFixed ? o.totalAmount.toFixed(2) : o.totalAmount})`"
            />
          </el-select>
        </el-form-item>
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
          <el-descriptions-item label="笔数">{{ shiftData.txnCount }}</el-descriptions-item>
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
const orderOptions = ref([]);
const orderLoading = ref(false);

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

async function searchOrders(keyword) {
  orderLoading.value = true;
  try {
    const r = await http.get("/orders/active", { params: { keyword: keyword || "" } });
    orderOptions.value = r.data || [];
  } finally {
    orderLoading.value = false;
  }
}

async function openAdd() {
  addForm.value = { orderId: null, itemType: null, amount: 0, remark: "" };
  await searchOrders("");
  addVisible.value = true;
}

async function submitAdd() {
  if (!addForm.value.orderId) return ElMessage.warning("请选择订单");
  if (addForm.value.itemType == null) return ElMessage.warning("请选择款项类型");
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
