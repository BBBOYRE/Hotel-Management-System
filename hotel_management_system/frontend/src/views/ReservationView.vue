<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="客户/订单号" clearable style="width:200px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="load">
        <el-option v-for="d in dictStore.items('RES_STATUS')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="onDateChange" style="width:260px" />
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button type="primary" @click="openBook"><el-icon><Plus /></el-icon>新建预订</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="resId" label="预订号" width="90" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户" width="100" />
        <el-table-column prop="typeName" label="房型" width="120" />
        <el-table-column prop="expectIn" label="预计到店" width="115" />
        <el-table-column prop="expectOut" label="预计离店" width="115" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="resStatusTag(row.resStatus)" size="small" effect="dark" round>
              {{ dictStore.label('RES_STATUS', row.resStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" v-if="row.resStatus === 1" @click="goCheckIn(row)">办理入住</el-button>
            <el-button link type="warning" v-if="row.resStatus === 1" @click="cancel(row)">取消</el-button>
            <el-button link type="danger" v-if="row.resStatus === 1" @click="noShow(row)">No-Show</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt" layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="load"
      />
    </el-card>

    <!-- 新建预订 -->
    <el-dialog v-model="bookVisible" title="新建预订" width="520px" append-to-body>
      <el-form :model="bookForm" label-width="90px">
        <el-form-item label="客户身份证">
          <el-input v-model="bookForm.idCard" placeholder="输入身份证号查询" @blur="lookupCustomer">
            <template #append><el-button @click="lookupCustomer">查找</el-button></template>
          </el-input>
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="bookForm.custName" disabled />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="bookForm.typeId" style="width:100%">
            <el-option v-for="t in types" :key="t.typeId" :label="`${t.typeName} (¥${t.basePrice})`" :value="t.typeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker v-model="bookForm.expectIn" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="离店日期">
          <el-date-picker v-model="bookForm.expectOut" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBook">提交预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import http from "../api/http";
import { useDictStore } from "../stores/dict";
import { ElMessage, ElMessageBox } from "element-plus";

const dictStore = useDictStore();
const router = useRouter();
const types = ref([]);
const list  = ref([]);
const total = ref(0);
const dateRange = ref(null);
const query = reactive({ keyword: "", status: null, startDate: null, endDate: null, pageNum: 1, pageSize: 10 });

const bookVisible = ref(false);
const bookForm = ref({ idCard: "", custName: "", customerId: null, typeId: null, expectIn: "", expectOut: "" });

onMounted(async () => {
  const r = await http.get("/room-types");
  types.value = r.data || [];
  load();
});

async function load() {
  const r = await http.get("/reservations", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function onDateChange(v) {
  query.startDate = v?.[0] || null;
  query.endDate   = v?.[1] || null;
  load();
}

function resStatusTag(s) {
  return { 1: "warning", 2: "success", 3: "info", 4: "danger" }[s] || "info";
}

function openBook() {
  bookForm.value = { idCard: "", custName: "", customerId: null, typeId: types.value[0]?.typeId, expectIn: "", expectOut: "" };
  bookVisible.value = true;
}

async function lookupCustomer() {
  if (!bookForm.value.idCard) return;
  try {
    const r = await http.get("/customers/by-id-card", { params: { idCard: bookForm.value.idCard } });
    if (r.data) {
      bookForm.value.customerId = r.data.customerId;
      bookForm.value.custName   = r.data.custName;
    } else {
      ElMessage.warning("未找到该客户，请先建档");
    }
  } catch (e) {
    ElMessage.warning("客户查询失败");
  }
}

async function submitBook() {
  if (!bookForm.value.customerId) return ElMessage.warning("请先查找或创建客户");
  await http.post("/reservations", bookForm.value);
  ElMessage.success("预订成功");
  bookVisible.value = false;
  load();
}

async function cancel(row) {
  await ElMessageBox.confirm("确定取消该预订？", "提示", { type: "warning" });
  await http.post(`/reservations/${row.resId}/cancel`);
  ElMessage.success("已取消");
  load();
}

async function noShow(row) {
  await ElMessageBox.confirm("将此预订标记为 No-Show？", "提示", { type: "warning" });
  await http.post(`/reservations/${row.resId}/no-show`);
  ElMessage.success("已标记");
  load();
}

function goCheckIn(row) {
  router.push({ path: "/check-ins", query: { resId: row.resId } });
}
</script>

<style scoped>
.mt { margin-top: 14px; }
</style>
