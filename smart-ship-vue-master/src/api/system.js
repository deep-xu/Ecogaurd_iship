import request from '@/utils/request';
import {projConfig} from "@/utils/config";

let api = projConfig.curBaseUrl;
export const login = (params) => {
  return request.post(`${api}/user/login`, params)
}
export const chgPwd = (params) => {
  return request.put(`${api}/user/password/self`, params)
}

/** 用户管理 */
export const user = {
  getTableData: (params) => {//获取所有用户
    return request.get(`${api}/user`, {params})
  },
  add: (params) => {
    return request.post(`${api}/user`, params)
  },
  del: (params) => {
    return request.delete(`${api}/user`, {data:params})
  },
  editPwd: (params) => {
    return request.put(`${api}/user/password`, params)
  },
  editPermission: (params) => {
    return request.put(`${api}/user/permission`, params)
  },
}

/** 日志管理 */
export const log = {
  get: (params) => {
    return request.get(`${api}/system/http/log/conf`, {params})
  },
  edit: (params) => { 
    return request.put(`${api}/system/http/log/conf`, params)
  },
  loglist:() => {
    return request.get(`${api}/system/http/log`)
  },
  download:() => {
    return request.get(`${api}/log`,{responseType:'blob'})
  }
}

/** 系统升级 */
export const sysupgrade = {
  version: () => {
    return request.get(`${api}/version`)
  },
  upgrade: (data) => { 
    return request.post(`${api}/version/package`, data)
  }
}


/** 自定义logo */
export const customLogo = {
  get: (params) => {
    return request.get(`${api}/logo`, {params})
  },
  edit: (data) => {
    return request.put(`${api}/logo`, data)
  },
  editIcon: (data) => {
    return request.put(`${api}/logo/icon`, data)
  },
  delLogo: (data) => {
    return request.delete(`${api}/logo/icon`, {data})
  }
}

export const dict = {
    getList: (params) => {
        return request.get(`${api}/system/dict/data/list`, {params})
    },
    edit: (data) => {
        return request.put(`${api}/system/dict/data`, data)
    },
    add: (data) => {
        return request.post(`${api}/system/dict/data`, data)
    },
    del: (data) => {
        return request.delete(`${api}/system/dict/data/`+data)
    }
}