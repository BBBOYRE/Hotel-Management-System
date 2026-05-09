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
      <el-button type="primary" @click="openForm()" v-perm="'room:edit'"><el-icon><Plus /></el-icon>新增客房</el-button>
    </div>

    <!-- 房态盘 -->
    <div v-if="viewMode === 'board'" class="room-grid">
      <div v-for="r in list" :key="r.roomId" class="room-card" @click="openForm(r)">
        <div class="status-bar" :class="'status-' + r.status"></div>
        <div class="room-cover">
          <img v-if="r.hasImage" :src="imageUrl(r)" alt="" class="cover-img" />
          <div v-else class="cover-empty"><el-icon :size="22"><Picture /></el-icon></div>
        </div>
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
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img v-if="row.hasImage" :src="imageUrl(row)" class="thumb" alt="" />
            <div v-else class="thumb thumb-empty"><el-icon><Picture /></el-icon></div>
          </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" min-width="100" sortable />
        <el-table-column prop="typeName" label="房型" min-width="140" />
        <el-table-column prop="floorNum" label="楼层" min-width="80" sortable />
        <el-table-column label="房态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small" effect="dark" round>
              {{ dictStore.label('ROOM_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="200" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="{ row }">
            <el-button link type="primary" v-perm="'room:edit'"   @click="openForm(row)">编辑</el-button>
            <el-button link type="danger"  v-perm="'room:delete'" @click="del(row)">删除</el-button>
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
    <el-dialog v-model="dlgVisible" :title="isEdit ? '编辑客房' : '新增客房'" width="520px" append-to-body>
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
        <el-form-item label="封面图">
          <div class="upload-row">
            <div class="preview">
              <img v-if="previewSrc" :src="previewSrc" class="preview-img" alt="" />
              <div v-else class="preview-empty"><el-icon :size="22"><Picture /></el-icon><span>无图片</span></div>
            </div>
            <div class="upload-actions">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                accept=".jpg,.jpeg,.png,.webp,.gif"
                :on-change="onFilePick"
              >
                <el-button size="small">{{ pendingFile ? '重新选择' : (isEdit && form.hasImage ? '替换' : '选择文件') }}</el-button>
              </el-upload>
              <el-button v-if="isEdit && (form.hasImage || pendingFile)" size="small" type="danger" plain @click="onClearImage">清除</el-button>
              <div class="upload-hint">≤ 5MB · jpg / png / webp / gif</div>
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
import { ref, reactive, computed, onMounted } from "vue";
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

// 图片缓存破坏戳：上传/清除后变化，让 <img> 重新拉
const imgVer = ref({});
function imageUrl(row) {
  const v = imgVer.value[row.roomId] || row.updateTime || "";
  return `/api/rooms/${row.roomId}/image?v=${encodeURIComponent(v)}`;
}

// 上传相关
const pendingFile = ref(null);   // 用户选了但还没保存的文件
const pendingPreview = ref("");  // base64 preview
const wantClearImage = ref(false);

const previewSrc = computed(() => {
  if (pendingPreview.value) return pendingPreview.value;
  if (wantClearImage.value) return "";
  if (isEdit.value && form.value.hasImage && form.value.roomId) {
    return `/api/rooms/${form.value.roomId}/image?v=${imgVer.value[form.value.roomId] || form.value.updateTime || ""}`;
  }
  return "";
});

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
  return { 1: "success", 3: "danger", 4: "warning", 5: "info" }[s] || "info";
}

function openForm(row) {
  pendingFile.value = null;
  pendingPreview.value = "";
  wantClearImage.value = false;
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { roomNo: "", typeId: types.value[0]?.typeId, floorNum: 1, status: 1 };
  }
  dlgVisible.value = true;
}

function onFilePick(file) {
  const raw = file.raw;
  if (!raw) return;
  const okType = ["image/jpeg","image/png","image/webp","image/gif"].includes(raw.type);
  if (!okType) { ElMessage.error("仅支持 jpg / png / webp / gif"); return; }
  if (raw.size > 5 * 1024 * 1024) { ElMessage.error("文件超过 5MB"); return; }
  pendingFile.value = raw;
  wantClearImage.value = false;
  const reader = new FileReader();
  reader.onload = e => { pendingPreview.value = e.target.result; };
  reader.readAsDataURL(raw);
}

function onClearImage() {
  pendingFile.value = null;
  pendingPreview.value = "";
  wantClearImage.value = true;
}

async function save() {
  let roomId;
  if (isEdit.value) {
    await http.put(`/rooms/${form.value.roomId}`, form.value);
    roomId = form.value.roomId;
  } else {
    const r = await http.post("/rooms", form.value);
    roomId = r.data;
  }

  // 图片：上传新文件 / 显式清除
  if (pendingFile.value) {
    const fd = new FormData();
    fd.append("file", pendingFile.value);
    await http.post(`/rooms/${roomId}/image`, fd, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    imgVer.value[roomId] = Date.now();
  } else if (wantClearImage.value) {
    await http.delete(`/rooms/${roomId}/image`);
    imgVer.value[roomId] = Date.now();
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

/* 房态盘封面 */
.room-cover {
  width: 100%; height: 96px; border-radius: 8px; overflow: hidden;
  background: #f1f5f9; display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.cover-empty { color: #94a3b8; }

/* 列表缩略图 */
.thumb {
  width: 80px; height: 60px; object-fit: cover; border-radius: 6px;
  background: #f1f5f9; display: flex; align-items: center; justify-content: center;
}
.thumb-empty { color: #94a3b8; }

/* 编辑对话框预览 */
.upload-row { display: flex; gap: 14px; align-items: flex-start; width: 100%; }
.preview {
  width: 140px; height: 105px; border: 1px dashed #cbd5e1; border-radius: 8px;
  background: #f8fafc; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.preview-empty { color: #94a3b8; display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 12px; }
.upload-actions { display: flex; flex-direction: column; gap: 8px; }
.upload-hint { color: #94a3b8; font-size: 12px; }
</style>
