import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const deptApis = {
  getList: () => {
    return request.get(`${api}/system/dept/list`)
  },
  delDept: (id) => {
    return request.delete(`${api}/system/dept/${id}`)
  },
  updateDept: (data) => {
    return request.put(`${api}/system/dept`, data)
  },
  addDept: (data) => {
    return request.post(`${api}/system/dept`, data)
  }
}

