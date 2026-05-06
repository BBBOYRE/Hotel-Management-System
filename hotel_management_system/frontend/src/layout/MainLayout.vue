<template>
  <el-container class="layout-wrapper">
    <!-- 侧栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-area" @click="$router.push('/dashboard')">
        <el-icon :size="28" color="#fff"><House /></el-icon>
        <span v-show="!isCollapse" class="logo-text">酒店管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="transparent"
        text-color="rgba(255,255,255,.78)"
        active-text-color="#ffffff"
        :collapse-transition="false"
        router
        class="side-menu"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item :index="'/' + item.path" v-if="!item.admin || isAdmin">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主体 -->
    <el-container class="layout-main-wrapper">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="bread">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCmd">
            <span class="user-info">
              <el-avatar :size="30" class="user-avatar">
                {{ user?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ user?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="changePwd">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-body">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px" append-to-body>
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确认</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useDictStore } from "../stores/dict";
import { ElMessage, ElMessageBox } from "element-plus";
import http from "../api/http";

const route  = useRoute();
const router = useRouter();
const auth   = useAuthStore();
const dict   = useDictStore();

// load dicts once
dict.load();

const isCollapse = ref(false);
const user = computed(() => auth.user);
const isAdmin = computed(() => auth.isAdmin);
const activeMenu = computed(() => route.path);
const currentTitle = computed(() => route.meta?.title || "");

const menuItems = computed(() => {
  const all = router.getRoutes().filter(r => r.meta?.title && r.meta?.icon);
  return all.map(r => ({
    path: r.path.replace(/^\//, ""),
    title: r.meta.title,
    icon: r.meta.icon,
    admin: r.meta.admin || false
  }));
});

/* ---------- 修改密码 ---------- */
const pwdVisible = ref(false);
const pwdForm = ref({ oldPassword: "", newPassword: "" });

function handleCmd(cmd) {
  if (cmd === "changePwd") {
    pwdForm.value = { oldPassword: "", newPassword: "" };
    pwdVisible.value = true;
  } else if (cmd === "logout") {
    ElMessageBox.confirm("确定退出登录？", "提示", { type: "warning" }).then(async () => {
      await auth.logout();
      router.replace("/login");
    }).catch(() => {});
  }
}

async function submitPwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    return ElMessage.warning("请填写完整");
  }
  await http.post("/auth/changePassword", pwdForm.value);
  ElMessage.success("密码修改成功，请重新登录");
  pwdVisible.value = false;
  await auth.logout();
  router.replace("/login");
}
</script>

<style scoped>
.layout-wrapper { height: 100vh; }
.layout-aside {
  background: linear-gradient(195deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  overflow-y: auto;
  overflow-x: hidden;
  transition: width .25s ease;
  box-shadow: 4px 0 24px rgba(0,0,0,.15);
}
.logo-area {
  display: flex; align-items: center; gap: 10px;
  padding: 20px 18px; cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,.08);
}
.logo-text {
  color: #fff; font-size: 16px; font-weight: 700; white-space: nowrap;
  background: linear-gradient(135deg, #e2b0ff, #9f44d3);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.side-menu { border-right: none; }
.side-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(99,102,241,.35), transparent) !important;
  border-right: 3px solid #818cf8;
}
.side-menu .el-menu-item:hover { background: rgba(255,255,255,.06) !important; }

.layout-main-wrapper { display: flex; flex-direction: column; }
.layout-header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; box-shadow: 0 1px 6px rgba(0,0,0,.06);
  padding: 0 24px; height: 56px; z-index: 10;
}
.header-left { display: flex; align-items: center; gap: 14px; }
.collapse-btn { cursor: pointer; color: #606266; transition: color .2s; }
.collapse-btn:hover { color: #409eff; }
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-avatar {
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: #fff; font-weight: 600;
}
.user-name { font-size: 14px; color: #303133; }
.layout-body {
  background: #f0f2f5; flex: 1; overflow-y: auto;
  padding: 20px;
}

/* transition */
.fade-slide-enter-active, .fade-slide-leave-active { transition: all .22s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(12px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
