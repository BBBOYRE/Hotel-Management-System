import { defineStore } from "pinia";
import http from "../api/http";

export const useDictStore = defineStore("dict", {
  state: () => ({ map: {} }),
  actions: {
    async load() {
      if (Object.keys(this.map).length) return;
      const res = await http.get("/sys/dicts/grouped");
      this.map = res.data || {};
    },
    items(typeCode) {
      return this.map[typeCode] || [];
    },
    label(typeCode, value) {
      const item = this.items(typeCode).find((i) => i.itemValue === Number(value));
      return item ? item.itemName : value;
    }
  }
});
