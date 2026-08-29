import request from '@/utils/request';
import { projConfig } from "@/utils/config";
import { getEncryptData, encryptWithAes } from "@/utils/cryptoUtils.js";

let api = projConfig.curBaseUrl;

export const userManagerApis = {
  getTableData: (params) => {
    return request.get(`${api}/system/user/list`, { params })
  },
  addUser: (user) => {
    return request.post(`${api}/system/user`, user)
  },
  updateUser: (user) => {
    return request.put(`${api}/system/user`, user)
  },
  deleteUser: (ids) => {
    return request.delete(`${api}/system/user/${ids}`)
  },
  resetPassword: (data) => {
    return request.put(`${api}/system/user/resetPwd`, encryptWithAes(JSON.stringify(data)), {
      headers: {
        'encrypt-key': getEncryptData(),
        'is-encrypt': true,
        'is-token': false,
        'repeat-submit': false,
        'Content-Type': 'application/json;charset=utf-8'
      }
    })
  },
  updateStatus: (data) => {
    return request.put(`${api}/system/user/changeStatus`, data)
  },
  exportUserData: (data) => { // 导出用户数据
    return request.post(`${api}/system/user/export`, data, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      responseType: 'blob'
    })
  },
  authRoleUsers: (params) => { // 授权角色用户
    return request.get(`${api}/system/role/authUser/allocatedList`, {params})
  },
  notAuthRoleUser: (params) => { // 未授权角色用户
    return request.get(`${api}/system/role/authUser/unallocatedList`, {params})
  },
  userDetail: (userId) => { //  用户详情
    return request.get(`${api}/system/user/${userId}`)
  }
}