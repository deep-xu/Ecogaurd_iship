import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

/** 算法管理 */
export const algApis = {
  getTableData: (params) => {//获取所有用户
    return request.get(`${api}/alg`, { params })
  },
  import: (data) => {
    return request.post(`${api}/alg`, data)
  },
  edit: (data) => {
    return request.put(`${api}/alg`, data)
  },
  del: (params) => {
    return request.delete(`${api}/alg`, { data: params })
  }
}