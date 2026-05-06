<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.serviceType" placeholder="类型" clearable style="width:120px" @change="load">
        <el-option v-for="d in dictStore.items('SERVICE_TYPE')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="load">
        <el-option v-for="d in dictStore.items('SERVICE_STATUS')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>派单</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="serviceId" label="ID" width="70" />
        <el-table-column prop="roomNo" label="房间" width="90" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ dictStore.label('SERVICE_TYPE', row.serviceType) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因/备注" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="svcTag(row.status)" size="small" effect="dark" round>
              {{ dictStore.label('SERVICE_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="负责人" width="100" />
        <el-table-column prop="finishTime" label="完成时间" width="170" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link type="primary" v-if="row.status === 1" @click="take(row)">接单</el-button>
            <el-button link type="success" v-if="row.status === 2" @click="finish(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt" layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="load"
      />
    </el-card>

    <el-dialog v-model="createVisible" title="派工单" width="480px" append-to-body>
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="房间">
          <el-select v-model="createForm.roomId" filterable style="width:100%">
            <el-option v-for="r in rooms" :key="r.roomId" :label="`${r.roomNo} (${r.floorNum}F)`" :value="r.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="createForm.serviceType" style="width:100%">
            <el-option v-for="d in dictStore.items('SERVICE_TYPE')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="createForm.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import http from "../api/http";
import { useDictStore } from "../stores/dict";
import { ElMessage } from "element-plus";

const dictStore = useDictStore();
const list  = ref([]);
const total = ref(0);
const rooms = ref([]);
const query = reactive({ serviceType: null, status: null, pageNum: 1, pageSize: 10 });

const createVisible = ref(false);
const createForm = ref({ roomId: null, serviceType: null, reason: "" });

onMounted(async () => {
  const r = await http.get("/rooms/all");
  rooms.value = r.data || [];
  load();
});

async function load() {
  const r = await http.get("/housekeeping", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function svcTag(s) {
  return { 1: "warning", 2: "primary", 3: "success" }[s] || "info";
}

function openCreate() {
  createForm.value = { roomId: null, serviceType: null, reason: "" };
  createVisible.value = true;
}

async function submitCreate() {
  await http.post("/housekeeping", createForm.value);
  ElMessage.success("派单成功");
  createVisible.value = false;
  load();
}

async function take(row) {
  await http.post(`/housekeeping/${row.serviceId}/take`);
  ElMessage.success("已接单");
  load();
}

async function finish(row) {
  await http.post(`/housekeeping/${row.serviceId}/finish`);
  ElMessage.success("已完成");
  load();
}
</script>

<style scoped>
.mt { margin-top: 14px; }
</style>
