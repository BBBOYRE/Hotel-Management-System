// v-perm 指令：根据当前登录用户权限隐藏元素
// 用法：
//   v-perm="'bill:add'"            单权限
//   v-perm="['bill:add','bill:delete']"  任一具备即可
import { useAuthStore } from "../stores/auth";

function evaluate(el, binding) {
  const auth = useAuthStore();
  const codes = Array.isArray(binding.value) ? binding.value : [binding.value];
  const ok = codes.some(c => !c || auth.has(c));
  if (!ok) {
    el.style.display = "none";
  } else {
    if (el.style.display === "none") el.style.display = "";
  }
}

export default {
  install(app) {
    app.directive("perm", {
      mounted: evaluate,
      updated: evaluate
    });
  }
};
