<template>
  <div class="login-wrapper">
    <div class="login-bg-particles"></div>
    <div class="login-bg-glow"></div>
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon-wrap">
          <el-icon :size="42" color="#fff"><OfficeBuilding /></el-icon>
        </div>
        <h1 class="brand-title">Oracle 酒店管理系统</h1>
        <p class="brand-sub">MODERN HOTEL EXPERIENCE</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入您的账号..." size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="请输入密码..." type="password" size="large" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button class="login-btn" type="primary" size="large" :loading="loading" @click="handleLogin">
          <span class="btn-text">进入系统</span>
          <el-icon class="btn-icon"><ArrowRight /></el-icon>
        </el-button>
      </el-form>
    </div>
    <div class="login-footer">© 2026 Oracle Hotel Management System · Crafted with Vue 3</div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { firstAccessiblePath } from "../router";
import { ElMessage } from "element-plus";
import { User, Lock, OfficeBuilding, ArrowRight } from "@element-plus/icons-vue";

const router = useRouter();
const auth   = useAuthStore();

const formRef = ref(null);
const loading = ref(false);
const form = ref({ username: "", password: "" });
const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await auth.login(form.value.username, form.value.password);
    const target = firstAccessiblePath(auth);
    if (!target) {
      await auth.logout();
      ElMessage.error("当前账号未配置任何菜单权限，请联系管理员");
      return;
    }
    router.replace(target);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: url('https://images.unsplash.com/photo-1542314831-c6a4d14fff88?auto=format&fit=crop&w=1920&q=80') center/cover no-repeat;
  background-color: #0f172a;
  position: relative; overflow: hidden;
}
.login-wrapper::before {
  content: ''; position: absolute; inset: 0;
  background: linear-gradient(135deg, rgba(15,23,42,0.9) 0%, rgba(30,27,75,0.7) 100%);
  z-index: 1;
}
.login-bg-glow {
  position: absolute; width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%);
  top: 50%; left: 50%; transform: translate(-50%, -50%);
  border-radius: 50%; z-index: 2;
  animation: pulse-glow 8s ease-in-out infinite alternate;
}
.login-bg-particles {
  position: absolute; inset: 0; z-index: 2;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(99,102,241,.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(236,72,153,.2) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(56,189,248,.15) 0%, transparent 50%);
  animation: float-particles 15s ease-in-out infinite alternate;
}
@keyframes pulse-glow {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; }
  100% { transform: translate(-50%, -50%) scale(1.2); opacity: 1; }
}
@keyframes float-particles {
  0%   { background-position: 0% 0%; }
  100% { background-position: 100% 100%; }
}
.login-card {
  position: relative; z-index: 10;
  width: 440px; padding: 50px 40px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(40px); -webkit-backdrop-filter: blur(40px);
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 40px 100px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(255,255,255,0.05);
  animation: card-appear 0.8s cubic-bezier(0.25, 0.8, 0.25, 1) forwards;
  opacity: 0; transform: translateY(30px);
}
@keyframes card-appear {
  to { opacity: 1; transform: translateY(0); }
}
.login-brand { text-align: center; margin-bottom: 40px; }
.brand-icon-wrap {
  width: 76px; height: 76px; margin: 0 auto 20px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 24px;
  background: linear-gradient(135deg, #4f46e5, #ec4899);
  box-shadow: 0 15px 35px rgba(79,70,229,0.4);
  position: relative;
}
.brand-icon-wrap::after {
  content: ''; position: absolute; inset: -4px; border-radius: 28px;
  background: linear-gradient(135deg, #4f46e5, #ec4899);
  filter: blur(12px); opacity: 0.6; z-index: -1;
}
.brand-title { 
  color: #fff; font-size: 28px; font-weight: 800; margin: 0; 
  letter-spacing: 0.5px; font-family: 'Outfit', sans-serif;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}
.brand-sub { 
  color: #94a3b8; font-size: 12px; margin-top: 8px; 
  letter-spacing: 3px; font-weight: 600; font-family: 'Inter', sans-serif;
}

.login-form :deep(.el-form-item) { margin-bottom: 24px; }
.login-form :deep(.el-input__wrapper) {
  background-color: rgba(15,23,42,0.4) !important; 
  border: 1px solid rgba(255,255,255,0.08) !important;
  border-radius: 14px !important; 
  padding: 8px 16px !important;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.2) !important;
  transition: all 0.3s ease;
}
.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(255,255,255,0.2) !important; 
  background-color: rgba(15,23,42,0.6) !important;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #818cf8 !important; 
  background-color: rgba(15,23,42,0.8) !important;
  box-shadow: 0 0 0 3px rgba(129,140,248,0.2) !important;
}
.login-form :deep(.el-input__inner) { 
  color: #f8fafc !important; 
  font-size: 15px !important; 
  font-weight: 500 !important; 
}
.login-form :deep(.el-input__inner)::placeholder { 
  color: #94a3b8 !important; 
}
.login-form :deep(.el-input__prefix .el-icon) { 
  color: #818cf8 !important; 
  font-size: 18px !important; 
  margin-right: 4px !important; 
}

.login-btn {
  width: 100%; height: 54px; border-radius: 14px; margin-top: 10px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  border: none; color: #fff; display: flex; align-items: center; justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative; overflow: hidden;
}
.login-btn::before {
  content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}
.login-btn:hover::before { left: 100%; }
.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 30px rgba(79,70,229,0.5);
}
.btn-text { font-size: 16px; font-weight: 700; letter-spacing: 2px; }
.btn-icon { margin-left: 8px; font-size: 18px; transition: transform 0.3s; }
.login-btn:hover .btn-icon { transform: translateX(4px); }

.login-footer {
  position: absolute; bottom: 24px; left: 0; right: 0; z-index: 10;
  text-align: center; font-size: 13px; color: rgba(255,255,255,.4);
  font-family: 'Inter', sans-serif; letter-spacing: 0.5px;
}
</style>
