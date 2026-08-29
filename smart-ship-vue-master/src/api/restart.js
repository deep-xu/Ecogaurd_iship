import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;
let proxyApi = projConfig.proxyUrl;

// 获取系统参数:不需要token
export const getsysargs = () => {
  return request.get(`${api}/system/args/login`)
}
// 获取系统参数:需要token
export const getsysargsToken = () => {
  return request.get(`${proxyApi}/system/args`)
}
// 设置系统参数
export const setsysargs = (data) => {
  return request.put(`${api}/system/args`, data)
}

//设置端口，用于复杂网络环境端口映射
export const mapReset = () => {
  return request.put(`${api}/system/args/map/reset`)
}

export const restart_monitor = () => {
  return request.post(`${api}/system/monitor/restart_monitor`)
}


export const restart_dashboard = () => {
  return request.post(`/monitorstatus/system/monitor/restart_dashboard`)
}

export const restart_sink = (data) => {
  return request.post(`/monitorstatus/system/monitor/restart_sink`, data)
}

export const restart_stream = (data) => {
  return request.post(`/monitorstatus/system/monitor/restart_stream`)
}

export const reboot = () => {
  return request.post(`/monitorstatus/system/monitor/reboot`)
}



export const sink_number = {
  get: () => {
    return request.get(`/monitorstatus/system/monitor/sink_number`)
  },
  save: (data) => {
    return request.put(`/monitorstatus/system/monitor/sink_number`, data)
  }
}

export const getNetwork = () => {
  return request.get(`/monitorstatus/system/monitor/network`)
}

export const getServer = () => {
  return request.get(`/monitorstatus/system/monitor/server`)
}

