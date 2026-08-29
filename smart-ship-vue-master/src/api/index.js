import store from '../store'
import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const charts = {
  day:() => {
    return request.get(`${api}/alert/statistics/day`)
  },
  category: () => {
    return request.get(`${api}/alert/statistics/category`)
  }
}

export const alert = {
  getTableData: (params) => {
    return request.get(`${api}/alert`, { params })
  },
  delete: (params) => { // ids
    return request.delete(`${api}/alert`, {data: params})
  },
  deleteAll: () => {
    return request.delete(`${api}/alert/all`)
  }
}

export const device = {
  getTableData: () => {
    return request.get(`${api}/device`)
  },
  delete: (params) => { // ids
    return request.delete(`${api}/device`, {data: params})
  },
}



