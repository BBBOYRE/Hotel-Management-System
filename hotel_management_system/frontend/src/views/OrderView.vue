<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="订单号/客户" clearable style="width:200px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="load">
        <el-option v-for="d in dictStore.items('ORDER_STATUS')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="onDateChange" style="width:260px" />
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button @click="exportExcel"><el-icon><Download /></el-icon>导出Excel</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe @sort-change="onSort">
        <el-table-column prop="orderNo" label="订单号" min-width="160" sortable="custom" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="totalAmount" label="金额 (¥)" min-width="120" sortable="custom">
          <template #default="{ row }">
            <span class="bold">{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="orderTag(row.status)" size="small" effect="dark" round>
              {{ dictStore.label('ORDER_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" sortable="custom" />
        <el-table-column label="操作" fixed="right" min-width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link type="danger" v-if="row.status === 1" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt" layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="load"
      />
    </el-card>

    <!-- 订单详情 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="600px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="总额">¥{{ detail.totalAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="orderTag(detail.status)" size="small" effect="dark" round>
              {{ dictStore.label('ORDER_STATUS', detail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin:18px 0 10px">预订明细</h4>
        <el-table :data="resList" size="small" stripe>
          <el-table-column prop="resId" label="预订号" min-width="80" />
          <el-table-column prop="typeName" label="房型" />
          <el-table-column prop="expectIn" label="到店" min-width="110" />
          <el-table-column prop="expectOut" label="离店" min-width="110" />
          <el-table-column label="状态" min-width="90">
            <template #default="{ row }">{{ dictStore.label('RES_STATUS', row.resStatus) }}</template>
          </el-table-column>
        </el-table>

        <h4 style="margin:18px 0 10px">入住记录</h4>
        <el-table :data="ciList" size="small" stripe>
          <el-table-column prop="recordId" label="入住号" min-width="80" />
          <el-table-column prop="roomNo" label="房间" min-width="80" />
          <el-table-column prop="checkIn" label="入住" min-width="155" />
          <el-table-column prop="checkOut" label="退房" min-width="155" />
        </el-table>

        <h4 style="margin:18px 0 10px">账单明细</h4>
        <el-table :data="billList" size="small" stripe>
          <el-table-column prop="itemId" label="ID" min-width="60" />
          <el-table-column label="类型" min-width="80">
            <template #default="{ row }">{{ dictStore.label('BILL_TYPE', row.itemType) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" min-width="100">
            <template #default="{ row }">
              <span :style="{ color: row.amount < 0 ? '#f56c6c' : '#67c23a' }">{{ row.amount?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="recordTime" label="时间" min-width="155" />
        </el-table>
      </template>
    </el-drawer>
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
const query = reactive({ keyword: "", status: null, orderBy: null, startDate: null, endDate: null, pageNum: 1, pageSize: 10 });

const detailVisible = ref(false);
const detail = ref(null);
const resList = ref([]);
const ciList  = ref([]);
const billList = ref([]);

onMounted(load);

async function load() {
  const r = await http.get("/orders", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function onDateChange(v) {
  query.startDate = v?.[0] || null;
  query.endDate   = v?.[1] || null;
  load();
}

function onSort({ prop, order }) {
  query.orderBy = order ? `${prop} ${order === "ascending" ? "ASC" : "DESC"}` : null;
  load();
}

function orderTag(s) {
  return { 1: "primary", 2: "success", 3: "info", 4: "danger" }[s] || "info";
}

async function viewDetail(row) {
  const [o, res, ci, bill] = await Promise.all([
    http.get(`/orders/${row.orderId}`),
    http.get(`/reservations/by-order/${row.orderId}`),
    http.get(`/check-ins/by-order/${row.orderId}`),
    http.get(`/bills/by-order/${row.orderId}`)
  ]);
  detail.value = o.data;
  resList.value = res.data || [];
  ciList.value  = ci.data || [];
  billList.value = bill.data || [];
  detailVisible.value = true;
}

async function cancel(row) {
  await ElMessageBox.confirm(`确定取消订单 ${row.orderNo} ？`, "提示", { type: "warning" });
  await http.post(`/orders/${row.orderId}/cancel`);
  ElMessage.success("已取消");
  load();
}

function exportExcel() {
  const params = new URLSearchParams();
  if (query.keyword) params.set("keyword", query.keyword);
  if (query.status != null) params.set("status", query.status);
  if (query.startDate) params.set("startDate", query.startDate);
  if (query.endDate) params.set("endDate", query.endDate);
  window.open(`/api/orders/export?${params.toString()}`);
}
</script>

<style scoped>
.mt { margin-top: 14px; }
</style>
