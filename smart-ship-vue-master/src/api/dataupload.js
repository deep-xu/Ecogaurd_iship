import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

/** 上传接口 */
export const uploadApi = {
  get: (params) => {
    return request.get(`${api}/api`, { params })
  },
  edit: (params) => {
    return request.put(`${api}/api`, params)
  },
}

// 微信推送用户管理
export const wechatUser = {
  getTableData: (params) => {
    return request.get(`${api}/wechat_user`, { params })
  },
  add: (params) => {
    return request.post(`${api}/wechat_user`, params)
  },
  edit: (params) => {
    return request.put(`${api}/wechat_user`, params)
  },
  del: (data) => {
    return request.delete(`${api}/wechat_user`, { data })
  },
}

// 微信高级设置
export const wechatSetting = {
  get: () => {
    return request.get(`${api}/wechat/settings`)
  },
  save: (params) => {
    return request.put(`${api}/wechat/settings`, params)
  }
}





