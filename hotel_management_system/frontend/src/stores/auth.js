import { defineStore } from "pinia";
import http from "../api/http";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    fetched: false
  }),
  getters: {
    isAdmin: (s) => s.user && s.user.roleId === 1,
    isLogin: (s) => !!s.user
  },
  actions: {
    async login(username, password) {
      const res = await http.post("/auth/login", { username, password });
      this.user = res.data;
      this.fetched = true;
      return res.data;
    },
    async fetchMe() {
      try {
        const res = await http.get("/auth/me");
        this.user = res.data;
      } catch (e) {
        this.user = null;
      } finally {
        this.fetched = true;
      }
    },
    async logout() {
      try { await http.post("/auth/logout"); } catch (e) {}
      this.user = null;
    }
  }
});
