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
      { path: "dashboard",   name: "Dashboard",    component: () => import("../views/DashboardView.vue"),   meta: { title: "数据看板", icon: "DataAnalysis", perm: "dashboard:view" } },
      { path: "rooms",       name: "Rooms",        component: () => import("../views/RoomView.vue"),        meta: { title: "房态管理", icon: "House",        perm: "room:view" } },
      { path: "room-types",  name: "RoomTypes",    component: () => import("../views/RoomTypeView.vue"),    meta: { title: "房型设置", icon: "SetUp",        perm: "roomtype:view" } },
      { path: "customers",   name: "Customers",    component: () => import("../views/CustomerView.vue"),    meta: { title: "客户档案", icon: "User",         perm: "customer:view" } },
      { path: "reservations",name: "Reservations", component: () => import("../views/ReservationView.vue"), meta: { title: "预订管理", icon: "Calendar",     perm: "reservation:view" } },
      { path: "check-ins",   name: "CheckIns",     component: () => import("../views/CheckInView.vue"),     meta: { title: "入住/退房", icon: "Key",          perm: "checkin:view" } },
      { path: "orders",      name: "Orders",       component: () => import("../views/OrderView.vue"),       meta: { title: "订单中心", icon: "List",         perm: "order:view" } },
      { path: "bills",       name: "Bills",        component: () => import("../views/BillView.vue"),        meta: { title: "财务账单", icon: "Money",        perm: "bill:view" } },
      { path: "housekeeping",name: "Housekeeping", component: () => import("../views/HousekeepingView.vue"),meta: { title: "客务工单", icon: "Brush",        perm: "housekeeping:view" } },
      { path: "reports",     name: "Reports",      component: () => import("../views/ReportView.vue"),      meta: { title: "统计报表", icon: "TrendCharts",  perm: "report:view" } },
      { path: "users",       name: "Users",        component: () => import("../views/UserView.vue"),        meta: { title: "员工管理", icon: "Avatar",       perm: "user:view" } },
      { path: "roles",       name: "Roles",        component: () => import("../views/RoleView.vue"),        meta: { title: "角色管理", icon: "Lock",         perm: "role:view" } },
      { path: "dicts",       name: "Dicts",        component: () => import("../views/DictView.vue"),        meta: { title: "数据字典", icon: "Collection",   perm: "dict:view" } }
    ]
  },
  { path: "/:pathMatch(.*)*", redirect: "/dashboard" }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

/** 找当前用户第一个有权访问的菜单路径，没有则返回 null */
export function firstAccessiblePath(auth) {
  const main = router.getRoutes().find(r => r.path === "/");
  const children = main?.children || [];
  for (const c of children) {
    if (!c.meta?.perm || auth.has(c.meta.perm)) {
      return "/" + (c.path || "").replace(/^\//, "");
    }
  }
  return null;
}

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore();
  if (!auth.fetched) {
    try { await auth.fetchMe(); } catch (e) { /* */ }
  }
  if (to.meta.requiresAuth && !auth.isLogin) {
    return next("/login");
  }
  if (to.meta.guest && auth.isLogin) {
    const fallback = firstAccessiblePath(auth);
    return next(fallback || "/login");
  }
  // 目标本身没权限：找第一个有权的菜单；都没有就退出登录
  if (to.meta.perm && !auth.has(to.meta.perm)) {
    const fallback = firstAccessiblePath(auth);
    if (!fallback) {
      // 当前用户什么菜单都打不开，避免重定向死循环
      await auth.logout();
      return next("/login");
    }
    if (fallback === to.path) {
      // 极端：fallback 算出来就是当前 to，直接放行避免死循环
      return next();
    }
    return next(fallback);
  }
  next();
});

export default router;
