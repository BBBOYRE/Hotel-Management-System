<template>
  <div class="login-wrapper">
    <div class="login-bg-particles"></div>
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon-wrap">
          <el-icon :size="36" color="#fff"><House /></el-icon>
        </div>
        <h1 class="brand-title">酒店管理系统</h1>
        <p class="brand-sub">Hotel Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" type="password" size="large" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button class="login-btn" type="primary" size="large" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
    </div>
    <div class="login-footer">© 2026 Hotel Management System · Oracle Database</div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { User, Lock } from "@element-plus/icons-vue";

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
    router.replace("/dashboard");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative; overflow: hidden;
}
.login-bg-particles {
  position: absolute; inset: 0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(99,102,241,.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(168,85,247,.12) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(59,130,246,.08) 0%, transparent 60%);
  animation: pulse-bg 8s ease-in-out infinite alternate;
}
@keyframes pulse-bg {
  0%   { opacity: .6; }
  100% { opacity: 1; }
}
.login-card {
  position: relative; z-index: 1;
  width: 400px; padding: 44px 36px 36px;
  background: rgba(255,255,255,.06);
  backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px);
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,.12);
  box-shadow: 0 32px 64px rgba(0,0,0,.35);
}
.login-brand { text-align: center; margin-bottom: 32px; }
.brand-icon-wrap {
  width: 64px; height: 64px; margin: 0 auto 14px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  box-shadow: 0 8px 24px rgba(99,102,241,.4);
}
.brand-title { color: #fff; font-size: 22px; font-weight: 700; margin: 0; }
.brand-sub   { color: rgba(255,255,255,.45); font-size: 12px; margin-top: 4px; letter-spacing: 1px; }

.login-form :deep(.el-input__wrapper) {
  background: rgba(255,255,255,.08); border: 1px solid rgba(255,255,255,.15);
  border-radius: 10px; color: #fff;
}
.login-form :deep(.el-input__wrapper):hover {
  border-color: rgba(99,102,241,.5);
}
.login-form :deep(.el-input__inner) { color: #fff; }
.login-form :deep(.el-input__inner)::placeholder { color: rgba(255,255,255,.35); }
.login-form :deep(.el-input__prefix .el-icon) { color: rgba(255,255,255,.45); }

.login-btn {
  width: 100%; border-radius: 10px; margin-top: 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none; font-weight: 600; letter-spacing: 4px;
  transition: transform .15s, box-shadow .15s;
}
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(99,102,241,.45);
}

.login-footer {
  position: absolute; bottom: 24px; left: 0; right: 0;
  text-align: center; font-size: 12px; color: rgba(255,255,255,.25);
}
</style>
