<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="房间号/客户姓名" clearable style="width:200px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="load">
        <el-option v-for="d in dictStore.items('CHECKIN_STATUS')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="入住开始" end-placeholder="入住结束" value-format="YYYY-MM-DD" @change="onDateChange" style="width:260px" />
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button type="primary" @click="openWalkIn"><el-icon><Plus /></el-icon>散客入住</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="recordId" label="入住号" min-width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="150" />
        <el-table-column prop="roomNo" label="房间" min-width="90" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="checkIn" label="入住时间" min-width="155" />
        <el-table-column prop="checkOut" label="退房时间" min-width="155" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="ciTag(row.status)" size="small" effect="dark" round>
              {{ dictStore.label('CHECKIN_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewGuests(row)">同住人</el-button>
            <el-button link type="warning" v-if="row.status === 1" @click="changeRoom(row)">换房</el-button>
            <el-button link type="danger" v-if="row.status === 1" @click="checkOut(row)">退房</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt" layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="load"
      />
    </el-card>

    <!-- 散客入住 -->
    <el-dialog v-model="walkVisible" title="办理入住" width="580px" append-to-body>
      <el-form :model="walkForm" label-width="100px">
        <el-form-item label="客户身份证">
          <el-input v-model="walkForm.idCard" @blur="lookupCustomer">
            <template #append><el-button @click="lookupCustomer">查找</el-button></template>
          </el-input>
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="walkForm.custName" disabled />
        </el-form-item>
        <el-form-item label="关联预订" v-if="walkForm.resId">
          <el-tag>预订号 {{ walkForm.resId }}</el-tag>
        </el-form-item>
        <el-form-item label="分配房间">
          <el-select v-model="walkForm.roomId" filterable style="width:100%" placeholder="选择空闲房间">
            <el-option v-for="r in availableRooms" :key="r.roomId" :label="`${r.roomNo} (${r.typeName || ''} ${r.floorNum}F)`" :value="r.roomId" />
          </el-select>
        </el-form-item>
        <el-divider>同住人登记</el-divider>
        <div v-for="(g, idx) in walkForm.guests" :key="idx" class="guest-row">
          <el-input v-model="g.guestName" placeholder="姓名" style="width:160px" />
          <el-input v-model="g.idCard" placeholder="身份证号" style="width:200px" />
          <el-button link type="danger" @click="walkForm.guests.splice(idx, 1)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button type="primary" link @click="walkForm.guests.push({ guestName: '', idCard: '' })">+ 添加同住人</el-button>
      </el-form>
      <template #footer>
        <el-button @click="walkVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckIn">确认入住</el-button>
      </template>
    </el-dialog>

    <!-- 同住人查看 -->
    <el-dialog v-model="guestVisible" title="同住人信息" width="500px" append-to-body>
      <el-table :data="guestList">
        <el-table-column prop="guestName" label="姓名" />
        <el-table-column prop="idCard" label="身份证号" />
      </el-table>
    </el-dialog>

    <!-- 换房 -->
    <el-dialog v-model="changeVisible" title="换房" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="新房间">
          <el-select v-model="changeRoomId" filterable style="width:100%">
            <el-option v-for="r in availableRooms" :key="r.roomId" :label="`${r.roomNo} (${r.typeName || ''} ${r.floorNum}F)`" :value="r.roomId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChange">确认换房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRoute } from "vue-router";
import http from "../api/http";
import { useDictStore } from "../stores/dict";
import { ElMessage, ElMessageBox } from "element-plus";

const dictStore = useDictStore();
const route = useRoute();
const list  = ref([]);
const total = ref(0);
const dateRange = ref(null);
const query = reactive({ keyword: "", status: null, startDate: null, endDate: null, pageNum: 1, pageSize: 10 });

const walkVisible = ref(false);
const walkForm = ref({ idCard: "", custName: "", customerId: null, resId: null, roomId: null, guests: [] });
const availableRooms = ref([]);

const guestVisible = ref(false);
const guestList = ref([]);

const changeVisible = ref(false);
const changeRecordId = ref(null);
const changeRoomId = ref(null);

onMounted(async () => {
  // if redirected from reservation
  if (route.query.resId) {
    walkForm.value.resId = Number(route.query.resId);
    await loadRooms();
    walkVisible.value = true;
  }
  load();
});

async function load() {
  const r = await http.get("/check-ins", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function onDateChange(v) {
  query.startDate = v?.[0] || null;
  query.endDate   = v?.[1] || null;
  load();
}

function ciTag(s) {
  return { 1: "success", 2: "info" }[s] || "info";
}

async function loadRooms() {
  const r = await http.get("/rooms/available");
  availableRooms.value = r.data || [];
}

async function openWalkIn() {
  walkForm.value = { idCard: "", custName: "", customerId: null, resId: null, roomId: null, guests: [] };
  await loadRooms();
  walkVisible.value = true;
}

async function lookupCustomer() {
  if (!walkForm.value.idCard) return;
  try {
    const r = await http.get("/customers/by-id-card", { params: { idCard: walkForm.value.idCard } });
    if (r.data) {
      walkForm.value.customerId = r.data.customerId;
      walkForm.value.custName   = r.data.custName;
    } else {
      ElMessage.warning("未找到该客户，请先在客户档案中建档");
    }
  } catch (e) { /* */ }
}

async function submitCheckIn() {
  if (!walkForm.value.roomId) return ElMessage.warning("请选择房间");
  if (!walkForm.value.customerId && !walkForm.value.resId) return ElMessage.warning("请先查找客户");
  await http.post("/check-ins", walkForm.value);
  ElMessage.success("入住成功");
  walkVisible.value = false;
  load();
}

async function checkOut(row) {
  await ElMessageBox.confirm(`确定为房间 ${row.roomNo} 办理退房？`, "提示", { type: "warning" });
  await http.post(`/check-ins/${row.recordId}/check-out`);
  ElMessage.success("退房成功");
  load();
}

async function viewGuests(row) {
  const r = await http.get(`/check-ins/${row.recordId}/guests`);
  guestList.value = r.data || [];
  guestVisible.value = true;
}

async function changeRoom(row) {
  changeRecordId.value = row.recordId;
  changeRoomId.value = null;
  await loadRooms();
  changeVisible.value = true;
}

async function submitChange() {
  if (!changeRoomId.value) return ElMessage.warning("请选择新房间");
  await http.post(`/check-ins/${changeRecordId.value}/change-room`, { roomId: changeRoomId.value });
  ElMessage.success("换房成功");
  changeVisible.value = false;
  load();
}
</script>

<style scoped>
.mt { margin-top: 14px; }
.guest-row { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
</style>
