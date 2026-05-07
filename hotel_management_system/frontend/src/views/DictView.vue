<template>
  <div>
    <div class="toolbar">
      <el-input v-model="filterType" placeholder="按类型代码过滤" clearable style="width:200px" />
      <el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增字典项</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="filteredList" stripe>
        <el-table-column prop="dictId" label="ID" min-width="70" />
        <el-table-column prop="typeCode" label="类型代码" min-width="160" />
        <el-table-column prop="itemValue" label="字典值" min-width="90" />
        <el-table-column prop="itemName" label="字典名称" min-width="140" />
        <el-table-column prop="sortOrder" label="排序号" min-width="80" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑字典项' : '新增字典项'" width="480px" append-to-body>
      <el-form :model="form" label-width="90px">
        <el-form-item label="类型代码"><el-input v-model="form.typeCode" placeholder="如 ROOM_STATUS" /></el-form-item>
        <el-form-item label="字典值"><el-input-number v-model="form.itemValue" :min="0" /></el-form-item>
        <el-form-item label="字典名称"><el-input v-model="form.itemName" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import http from "../api/http";
import { ElMessage, ElMessageBox } from "element-plus";

const allList = ref([]);
const filterType = ref("");
const filteredList = computed(() => {
  if (!filterType.value) return allList.value;
  return allList.value.filter(d => d.typeCode?.toLowerCase().includes(filterType.value.toLowerCase()));
});

const dlgVisible = ref(false);
const isEdit = ref(false);
const form = ref({});

onMounted(load);

async function load() {
  const r = await http.get("/sys/dicts");
  allList.value = r.data || [];
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { typeCode: "", itemValue: 0, itemName: "", sortOrder: 0 };
  }
  dlgVisible.value = true;
}

async function save() {
  if (isEdit.value) {
    await http.put(`/sys/dicts/${form.value.dictId}`, form.value);
  } else {
    await http.post("/sys/dicts", form.value);
  }
  ElMessage.success("保存成功");
  dlgVisible.value = false;
  load();
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除字典项「${row.itemName}」？`, "提示", { type: "warning" });
  await http.delete(`/sys/dicts/${row.dictId}`);
  ElMessage.success("删除成功");
  load();
}
</script>
