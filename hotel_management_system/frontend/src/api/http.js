import axios from "axios";
import { ElMessage } from "element-plus";
import router from "../router";

const http = axios.create({
  baseURL: "/api",
  timeout: 15000,
  withCredentials: true
});

http.interceptors.response.use(
  (resp) => {
    const data = resp.data;
    if (resp.config.responseType === "blob") return resp;
    if (data && data.code !== undefined) {
      if (data.code === 200) return data;
      if (data.code === 401) {
        // 静默处理首次检查 session 时返回的 401 状态，避免一打开网页就弹红色错误提示
        if (!resp.config.url.includes("/auth/me")) {
          ElMessage.warning(data.msg || "登录已过期，请重新登录");
        }
        router.replace("/login");
        return Promise.reject(new Error(data.msg));
      }
      ElMessage.error(data.msg || "操作失败");
      return Promise.reject(new Error(data.msg));
    }
    return data;
  },
  (err) => {
    if (err.response && err.response.status === 401) {
      if (err.config && !err.config.url.includes("/auth/me")) {
         ElMessage.warning("登录已过期，请重新登录");
      }
      router.replace("/login");
      return Promise.reject(err);
    }
    ElMessage.error(err.message || "网络异常");
    return Promise.reject(err);
  }
);

export default http;
