
import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const menuApis = {
    // 获取菜单列表
    getMenuList: (params) => {
      return request.get(`${api}/system/menu/list`, { params })
    },
    // 获取菜单树
    getMenuTree: (params) => {
        return request.get(`${api}/system/menu/treeselect`);
    },
    delMenu: (id) => {
        return request.delete(`${api}/system/menu/${id}`);
    },
    addMenu: (data) => {
      return request.post(`${api}/system/menu`, data)
    },
    updateMenu: (data) => {
      return request.put(`${api}/system/menu`, data)
    },
}