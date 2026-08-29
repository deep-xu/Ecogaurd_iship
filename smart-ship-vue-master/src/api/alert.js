import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const alertV2Apis = {
  getTableData: (params) => {
    return request.get(`${api}/ship/alert/list`,{params})
  },
  alertDetail: (params) => {
    return request.get(`${api}/ship/alert/get`, {params})
  },
  alertExport: (data) => {
    return request.post(`${api}/ship/alert/export`, data, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      responseType: 'blob'
    })
  }
}

export const alertApis = {
  getTableData: (params) => {
    return request.get(`${api}/alert`,{params})
  },
  delete: (data) => {
    return request.delete(`${api}/alert`, {data})
  },
  count: (params) => {
    return request.get(`${api}/alert/count`,{params})
  },
  export: (params) => {
    return request.get(`${api}/alert/xlsx`,{params, responseType:'blob'})
  },
  latest: (params) => {
    return request.get(`${api}/alert/latest`,{params})
  },
  crosslinecount: (params) => {
    return request.get(`${api}/cross_line_counting`,{params})
  },
  device:() => {
    return request.get(`${api}/alert/device`)
  },
  deal:(data) => {
    return request.put(`${api}/alert`, data)
  }

}