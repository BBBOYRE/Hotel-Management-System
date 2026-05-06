<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="姓名 / 手机号 / 身份证" clearable style="width:240px" @keyup.enter="load" />
      <el-select v-model="query.vipLevel" placeholder="会员等级" clearable style="width:120px" @change="load">
        <el-option label="普通" :value="0" />
        <el-option label="银卡" :value="1" />
        <el-option label="金卡" :value="2" />
        <el-option label="钻石" :value="3" />
      </el-select>
      <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
      <div class="filler"></div>
      <el-button @click="exportExcel"><el-icon><Download /></el-icon>导出Excel</el-button>
      <el-button type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增客户</el-button>
    </div>
    <el-card shadow="never" style="border-radius:12px">
      <el-table :data="list" stripe @sort-change="onSort">
        <el-table-column prop="customerId" label="ID" width="70" sortable="custom" />
        <el-table-column prop="custName" label="姓名" width="100" sortable="custom" />
        <el-table-column prop="idCard" label="身份证号" width="190" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</template>
        </el-table-column>
        <el-table-column label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','','warning','danger'][row.vipLevel]" size="small" effect="dark" round>
              {{ ['普通','银卡','金卡','钻石'][row.vipLevel] || '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="建档时间" width="170" sortable="custom" />
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

    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑客户' : '新增客户'" width="500px" append-to-body>
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="form.custName" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCard" maxlength="18" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="form.vipLevel" style="width:100%">
            <el-option label="普通" :value="0" />
            <el-option label="银卡" :value="1" />
            <el-option label="金卡" :value="2" />
            <el-option label="钻石" :value="3" />
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
import { ElMessage, ElMessageBox } from "element-plus";

const list  = ref([]);
const total = ref(0);
const query = reactive({ keyword: "", vipLevel: null, orderBy: null, pageNum: 1, pageSize: 10 });

const dlgVisible = ref(false);
const isEdit = ref(false);
const form = ref({});

onMounted(load);

async function load() {
  const r = await http.get("/customers", { params: query });
  list.value = r.data?.records || [];
  total.value = r.data?.total || 0;
}

function onSort({ prop, order }) {
  query.orderBy = order ? `${prop} ${order === "ascending" ? "ASC" : "DESC"}` : null;
  load();
}

function openForm(row) {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { custName: "", idCard: "", phone: "", gender: 1, vipLevel: 0 };
  }
  dlgVisible.value = true;
}

async function save() {
  if (isEdit.value) {
    await http.put(`/customers/${form.value.customerId}`, form.value);
  } else {
    await http.post("/customers", form.value);
  }
  ElMessage.success("保存成功");
  dlgVisible.value = false;
  load();
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除客户「${row.custName}」？`, "提示", { type: "warning" });
  await http.delete(`/customers/${row.customerId}`);
  ElMessage.success("删除成功");
  load();
}

function exportExcel() {
  const params = new URLSearchParams();
  if (query.keyword) params.set("keyword", query.keyword);
  if (query.vipLevel != null) params.set("vipLevel", query.vipLevel);
  window.open(`/api/customers/export?${params.toString()}`);
}
</script>

<style scoped>
.mt { margin-top: 14px; }
</style>
