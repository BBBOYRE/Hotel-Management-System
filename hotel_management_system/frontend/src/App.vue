<template>
  <el-config-provider :locale="zhCn">
    <router-view v-slot="{ Component }">
      <transition name="fade-app" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "./stores/auth";
import { ElConfigProvider } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

const auth = useAuthStore();
onMounted(() => {
  auth.fetchMe().catch(() => {});
});
</script>

<style>
.fade-app-enter-active, .fade-app-leave-active { transition: opacity 0.3s ease; }
.fade-app-enter-from, .fade-app-leave-to { opacity: 0; }
</style>
