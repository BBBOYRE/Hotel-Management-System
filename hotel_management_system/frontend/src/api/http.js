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
        ElMessage.error(data.msg || "未登录");
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
      router.replace("/login");
    }
    ElMessage.error(err.message || "网络异常");
    return Promise.reject(err);
  }
);

export default http;
