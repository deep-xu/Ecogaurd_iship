import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const roleApis = {
  getRoleSelectList: () => {
    return request.get(`${api}/system/role/optionselect`)
  },
  getTableData: (params) => {
    return request.get(`${api}/system/role/list`, { params })
  },
  addRole: (role) => {
    return request.post(`${api}/system/role`, role);
  },
  updateRole: (role) => { 
    return request.put(`${api}/system/role`, role);
  },
  updateRoleStatus: (data) => {
    return request.put(`${api}/system/role/changeStatus`, data);
  },
  deleteRole: (id) => {
    return request.delete(`${api}/system/role/${id}`);
  },
  roleExport: (params) => {
    return request.get(`${api}/system/role/export`, { params })
  },
  treeSelect: () => {
    return request.get(`${api}/system/menu/treeselect?status=0`)
  },
  getSelectedTree: (id) => {
    return request.get(`${api}/system/menu/roleMenuTreeselect/${id}`)
  },
  authUser: (roleId, userIds) => { // 授权角色用户
    return request.put(`${api}/system/role/authUser/selectAll?roleId=${roleId}&userIds=${userIds}`);
  },
  cancelAuthUser: (data) => { // 取消授权角色用户
    return request.put(`${api}/system/role/authUser/cancel`, data);
  },
  batchCancelAuthUser: (roleId, userIds) => { // 取消授权角色用户
    return request.put(`${api}/system/role/authUser/cancelAll?roleId=${roleId}&userIds=${userIds}`);
  },
  getRoleDeptTree: (roleId) => {
    return request.get(`${api}//system/role/deptTree/${roleId}`)
  },
  getRoleDetail: (roleId) => { 
    return request.get(`${api}/system/role/${roleId}`)
  },
  dataScope: (data) => {
    return request.put(`${api}/system/role/dataScope`, data)
  }
}