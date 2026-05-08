<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openForm()" v-perm="'role:edit'"><el-icon><Plus /></el-icon>新增角色</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe>
        <el-table-column prop="roleId" label="ID" min-width="70" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="权限" min-width="240">
          <template #default="{ row }">
            <el-tag v-if="row.permissions === '*'" type="danger" size="small" effect="plain">全部权限</el-tag>
            <span v-else-if="!row.permissions" style="color:#94a3b8">未配置</span>
            <span v-else>共 {{ row.permissions.split(',').filter(Boolean).length }} 项</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="200" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="{ row }">
            <el-button link type="primary" v-perm="'role:edit'" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger"  v-perm="'role:delete'" v-if="row.roleId !== 1" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="780px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="权限">
          <div v-if="isAdminRole" class="admin-hint">
            <el-tag type="danger" effect="plain">管理员角色拥有全部权限，且不可在此修改</el-tag>
          </div>
          <div v-else class="perm-grid">
            <div v-for="g in permGroups" :key="g.module" class="perm-group">
              <div class="perm-group-title">
                <el-checkbox
                  :model-value="isGroupAll(g)"
                  :indeterminate="isGroupSome(g)"
                  @change="toggleGroup(g, $event)"
                >{{ g.label }}</el-checkbox>
              </div>
              <el-checkbox-group v-model="selectedPerms" class="perm-items">
                <el-checkbox v-for="it in g.items" :key="it.code" :value="it.code" :label="it.code">
                  {{ it.label }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </div>
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
import { ref, computed, onMounted } from "vue";
import http from "../api/http";
import { ElMessage, ElMessageBox } from "element-plus";

const list = ref([]);
const dlgVisible = ref(false);
const isEdit = ref(false);
const form = ref({});
const selectedPerms = ref([]);

const permGroups = [
  { module: "dashboard", label: "看板", items: [
    { code: "dashboard:view", label: "查看" },
  ]},
  { module: "customer", label: "客户档案", items: [
    { code: "customer:view",   label: "查看" },
    { code: "customer:edit",   label: "新增/编辑" },
    { code: "customer:delete", label: "删除" },
  ]},
  { module: "roomtype", label: "房型设置", items: [
    { code: "roomtype:view",   label: "查看" },
    { code: "roomtype:edit",   label: "新增/编辑" },
    { code: "roomtype:delete", label: "删除" },
  ]},
  { module: "room", label: "房态管理", items: [
    { code: "room:view",   label: "查看" },
    { code: "room:edit",   label: "新增/编辑/改房态" },
    { code: "room:delete", label: "删除" },
  ]},
  { module: "reservation", label: "预订", items: [
    { code: "reservation:view",   label: "查看" },
    { code: "reservation:edit",   label: "新增预订" },
    { code: "reservation:cancel", label: "取消/爽约" },
  ]},
  { module: "checkin", label: "入住/退房", items: [
    { code: "checkin:view",       label: "查看" },
    { code: "checkin:checkin",    label: "办理入住" },
    { code: "checkin:checkout",   label: "办理退房" },
    { code: "checkin:changeroom", label: "换房" },
  ]},
  { module: "order", label: "订单中心", items: [
    { code: "order:view",   label: "查看" },
    { code: "order:cancel", label: "取消订单" },
    { code: "order:settle", label: "结算订单" },
    { code: "order:export", label: "导出" },
  ]},
  { module: "bill", label: "财务账单", items: [
    { code: "bill:view",   label: "查看" },
    { code: "bill:add",    label: "录入账目" },
    { code: "bill:delete", label: "删除账目" },
    { code: "bill:shift",  label: "交接班" },
  ]},
  { module: "housekeeping", label: "客务工单", items: [
    { code: "housekeeping:view",     label: "查看" },
    { code: "housekeeping:dispatch", label: "派单" },
    { code: "housekeeping:finish",   label: "接单/完成" },
  ]},
  { module: "report", label: "统计报表", items: [
    { code: "report:view",   label: "查看" },
    { code: "report:export", label: "导出" },
  ]},
  { module: "dict", label: "数据字典", items: [
    { code: "dict:view", label: "查看" },
    { code: "dict:edit", label: "编辑" },
  ]},
  { module: "user", label: "员工管理", items: [
    { code: "user:view",   label: "查看" },
    { code: "user:edit",   label: "新增/编辑" },
    { code: "user:delete", label: "删除/禁用" },
  ]},
  { module: "role", label: "角色管理", items: [
    { code: "role:view",   label: "查看" },
    { code: "role:edit",   label: "编辑权限" },
    { code: "role:delete", label: "删除" },
  ]},
];

const isAdminRole = computed(() => isEdit.value && form.value.roleId === 1);

function isGroupAll(g) {
  return g.items.every(it => selectedPerms.value.includes(it.code));
}
function isGroupSome(g) {
  const some = g.items.some(it => selectedPerms.value.includes(it.code));
  return some && !isGroupAll(g);
}
function toggleGroup(g, checked) {
  const codes = g.items.map(it => it.code);
  if (checked) {
    const set = new Set(selectedPerms.value);
    codes.forEach(c => set.add(c));
    selectedPerms.value = Array.from(set);
  } else {
    selectedPerms.value = selectedPerms.value.filter(c => !codes.includes(c));
  }
}

onMounted(load);

async function load() {
  const r = await http.get("/sys/roles");
  list.value = r.data || [];
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
    if (row.permissions === "*") {
      selectedPerms.value = []; // 编辑界面不展开 *，UI 上由 isAdminRole 提示
    } else {
      selectedPerms.value = (row.permissions || "").split(",").map(s => s.trim()).filter(Boolean);
    }
  } else {
    isEdit.value = false;
    form.value = { roleName: "", description: "" };
    selectedPerms.value = [];
  }
  dlgVisible.value = true;
}

async function save() {
  const payload = {
    ...form.value,
    permissions: isAdminRole.value ? "*" : selectedPerms.value.join(",")
  };
  if (isEdit.value) {
    await http.put(`/sys/roles/${form.value.roleId}`, payload);
  } else {
    await http.post("/sys/roles", payload);
  }
  ElMessage.success("保存成功，已登录用户需重新登录后权限才会生效");
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

<style scoped>
.admin-hint { padding: 6px 0; }
.perm-grid {
  display: grid; grid-template-columns: repeat(2, 1fr);
  gap: 14px 18px; width: 100%;
}
.perm-group {
  border: 1px solid #e2e8f0; border-radius: 10px;
  padding: 10px 12px; background: #fafbfc;
}
.perm-group-title { font-weight: 600; margin-bottom: 6px; }
.perm-items { display: flex; flex-wrap: wrap; gap: 4px 12px; padding-left: 22px; }
@media (max-width: 900px) {
  .perm-grid { grid-template-columns: 1fr; }
}
</style>
