<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增房型</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="typeId" label="ID" min-width="70" />
        <el-table-column prop="typeName" label="房型名称" />
        <el-table-column prop="basePrice" label="挂牌价 (¥)" min-width="120" />
        <el-table-column prop="bedCount" label="床位数" min-width="90" />
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑房型' : '新增房型'" width="450px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.typeName" /></el-form-item>
        <el-form-item label="挂牌价"><el-input-number v-model="form.basePrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="床位数"><el-input-number v-model="form.bedCount" :min="1" :max="10" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import http from "../api/http";
import { ElMessage, ElMessageBox } from "element-plus";

const list = ref([]);
const dlgVisible = ref(false);
const isEdit = ref(false);
const form = ref({});

onMounted(load);

async function load() {
  const r = await http.get("/room-types");
  list.value = r.data || [];
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { typeName: "", basePrice: 0, bedCount: 1 };
  }
  dlgVisible.value = true;
}

async function save() {
  if (isEdit.value) {
    await http.put(`/room-types/${form.value.typeId}`, form.value);
  } else {
    await http.post("/room-types", form.value);
  }
  ElMessage.success("保存成功");
  dlgVisible.value = false;
  load();
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除房型「${row.typeName}」？`, "提示", { type: "warning" });
  await http.delete(`/room-types/${row.typeId}`);
  ElMessage.success("删除成功");
  load();
}
</script>
