import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/LoginView.vue"),
    meta: { guest: true }
  },
  {
    path: "/",
    component: () => import("../layout/MainLayout.vue"),
    meta: { requiresAuth: true },
    redirect: "/dashboard",
    children: [
      { path: "dashboard", name: "Dashboard", component: () => import("../views/DashboardView.vue"), meta: { title: "数据看板", icon: "DataAnalysis" } },
      { path: "rooms", name: "Rooms", component: () => import("../views/RoomView.vue"), meta: { title: "房态管理", icon: "House" } },
      { path: "room-types", name: "RoomTypes", component: () => import("../views/RoomTypeView.vue"), meta: { title: "房型设置", icon: "SetUp" } },
      { path: "customers", name: "Customers", component: () => import("../views/CustomerView.vue"), meta: { title: "客户档案", icon: "User" } },
      { path: "reservations", name: "Reservations", component: () => import("../views/ReservationView.vue"), meta: { title: "预订管理", icon: "Calendar" } },
      { path: "check-ins", name: "CheckIns", component: () => import("../views/CheckInView.vue"), meta: { title: "入住/退房", icon: "Key" } },
      { path: "orders", name: "Orders", component: () => import("../views/OrderView.vue"), meta: { title: "订单中心", icon: "List" } },
      { path: "bills", name: "Bills", component: () => import("../views/BillView.vue"), meta: { title: "财务账单", icon: "Money" } },
      { path: "housekeeping", name: "Housekeeping", component: () => import("../views/HousekeepingView.vue"), meta: { title: "客务工单", icon: "Brush" } },
      { path: "reports", name: "Reports", component: () => import("../views/ReportView.vue"), meta: { title: "统计报表", icon: "TrendCharts" } },
      { path: "users", name: "Users", component: () => import("../views/UserView.vue"), meta: { title: "员工管理", icon: "Avatar", admin: true } },
      { path: "roles", name: "Roles", component: () => import("../views/RoleView.vue"), meta: { title: "角色管理", icon: "Lock", admin: true } },
      { path: "dicts", name: "Dicts", component: () => import("../views/DictView.vue"), meta: { title: "数据字典", icon: "Collection", admin: true } }
    ]
  },
  { path: "/:pathMatch(.*)*", redirect: "/dashboard" }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore();
  if (!auth.fetched) {
    try { await auth.fetchMe(); } catch (e) { /* */ }
  }
  if (to.meta.requiresAuth && !auth.isLogin) {
    return next("/login");
  }
  if (to.meta.guest && auth.isLogin) {
    return next("/dashboard");
  }
  if (to.meta.admin && !auth.isAdmin) {
    return next("/dashboard");
  }
  next();
});

export default router;
