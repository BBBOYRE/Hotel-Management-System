<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="姓名/账号" clearable style="width:200px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="load">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增员工</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="userId" label="ID" width="70" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="warning" @click="resetPwd(row)">重置密码</el-button>
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

    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="480px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="isEdit" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="密码" v-if="!isEdit"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" style="width:100%">
            <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ElMessage, ElMessageBox } from "element-plus";

const list  = ref([]);
const total = ref(0);
const roles = ref([]);
const query = reactive({ keyword: "", status: null, pageNum: 1, pageSize: 10 });

const dlgVisible = ref(false);
const isEdit = ref(false);
const form = ref({});

onMounted(async () => {
  const r = await http.get("/sys/users/roles");
  roles.value = r.data || [];
  load();
});

async function load() {
  const r = await http.get("/sys/users", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { username: "", realName: "", password: "", roleId: roles.value[0]?.roleId, status: 1 };
  }
  dlgVisible.value = true;
}

async function save() {
  if (isEdit.value) {
    await http.put(`/sys/users/${form.value.userId}`, form.value);
  } else {
    await http.post("/sys/users", form.value);
  }
  ElMessage.success("保存成功");
  dlgVisible.value = false;
  load();
}

async function toggleStatus(row) {
  const ns = row.status === 1 ? 0 : 1;
  await http.post(`/sys/users/${row.userId}/status`, { status: ns });
  ElMessage.success(ns === 1 ? "已启用" : "已禁用");
  load();
}

async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt("请输入新密码", `重置 ${row.realName} 的密码`, {
    inputType: "password", inputPattern: /.{4,}/, inputErrorMessage: "至少4位"
  });
  await http.post(`/sys/users/${row.userId}/resetPassword`, { password: value });
  ElMessage.success("重置成功");
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除员工「${row.realName}」？`, "提示", { type: "warning" });
  await http.delete(`/sys/users/${row.userId}`);
  ElMessage.success("删除成功");
  load();
}
</script>

<style scoped>
.mt { margin-top: 14px; }
</style>
