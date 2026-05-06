<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.typeId" placeholder="房型" clearable style="width:140px" @change="load">
        <el-option v-for="t in types" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
      </el-select>
      <el-select v-model="query.status" placeholder="房态" clearable style="width:120px" @change="load">
        <el-option v-for="d in dictStore.items('ROOM_STATUS')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="房间号 / 楼层" clearable style="width:180px" @keyup.enter="load" />
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-radio-group v-model="viewMode" size="small">
        <el-radio-button value="board">房态盘</el-radio-button>
        <el-radio-button value="table">列表</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增客房</el-button>
    </div>

    <!-- 房态盘 -->
    <div v-if="viewMode === 'board'" class="room-grid">
      <div v-for="r in list" :key="r.roomId" class="room-card" @click="openForm(r)">
        <div class="status-bar" :class="'status-' + r.status"></div>
        <div class="room-no">{{ r.roomNo }}</div>
        <div class="room-meta">{{ r.typeName || '---' }} · {{ r.floorNum }}F</div>
        <el-tag :type="statusTag(r.status)" size="small" class="room-tag" effect="dark" round>
          {{ dictStore.label('ROOM_STATUS', r.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 列表模式 -->
    <el-card v-else shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe style="width:100%">
        <el-table-column prop="roomNo" label="房间号" width="100" sortable />
        <el-table-column prop="typeName" label="房型" width="140" />
        <el-table-column prop="floorNum" label="楼层" width="80" sortable />
        <el-table-column label="房态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small" effect="dark" round>
              {{ dictStore.label('ROOM_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
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

    <!-- 新增/编辑 -->
    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑客房' : '新增客房'" width="480px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="房间号"><el-input v-model="form.roomNo" /></el-form-item>
        <el-form-item label="房型">
          <el-select v-model="form.typeId" style="width:100%">
            <el-option v-for="t in types" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层"><el-input-number v-model="form.floorNum" :min="1" :max="99" /></el-form-item>
        <el-form-item label="房态" v-if="isEdit">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="d in dictStore.items('ROOM_STATUS')" :key="d.itemValue" :label="d.itemName" :value="d.itemValue" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import http from "../api/http";
import { useDictStore } from "../stores/dict";
import { ElMessage, ElMessageBox } from "element-plus";

const dictStore = useDictStore();
const types = ref([]);
const list  = ref([]);
const total = ref(0);
const viewMode = ref("board");
const query = reactive({ typeId: null, status: null, keyword: "", pageNum: 1, pageSize: 50 });

const dlgVisible = ref(false);
const isEdit = ref(false);
const form = ref({});

onMounted(async () => {
  const r = await http.get("/room-types");
  types.value = r.data || [];
  await load();
});

async function load() {
  const r = await http.get("/rooms", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function statusTag(s) {
  return { 1: "success", 2: "primary", 3: "danger", 4: "warning", 5: "info" }[s] || "info";
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { roomNo: "", typeId: types.value[0]?.typeId, floorNum: 1, status: 1 };
  }
  dlgVisible.value = true;
}

async function save() {
  if (isEdit.value) {
    await http.put(`/rooms/${form.value.roomId}`, form.value);
  } else {
    await http.post("/rooms", form.value);
  }
  ElMessage.success("保存成功");
  dlgVisible.value = false;
  load();
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除房间 ${row.roomNo} ？`, "提示", { type: "warning" });
  await http.delete(`/rooms/${row.roomId}`);
  ElMessage.success("删除成功");
  load();
}
</script>

<style scoped>
.room-tag { position: absolute; top: 10px; right: 10px; }
.mt { margin-top: 14px; }
</style>
