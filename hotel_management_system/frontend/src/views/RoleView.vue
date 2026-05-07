<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增角色</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="roleId" label="ID" min-width="70" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="450px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
  const r = await http.get("/sys/roles");
  list.value = r.data || [];
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { roleName: "", description: "" };
  }
  dlgVisible.value = true;
}

async function save() {
  if (isEdit.value) {
    await http.put(`/sys/roles/${form.value.roleId}`, form.value);
  } else {
    await http.post("/sys/roles", form.value);
  }
  ElMessage.success("保存成功");
  dlgVisible.value = false;
  load();
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, "提示", { type: "warning" });
  await http.delete(`/sys/roles/${row.roleId}`);
  ElMessage.success("删除成功");
  load();
}
</script>
