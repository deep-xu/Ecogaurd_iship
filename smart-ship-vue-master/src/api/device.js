import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const getAlgCfg = (path) => {
  return request.get(path + `?t=${new Date().getTime()}`)
}

export const getmindevversion = (path) => {
  return request.get(`${api}/device/min_version`)
}

export const deviceV2Apis = {
  getTableList: () => {
    return request.get(`${api}/ship/deivce/list`)
  },
  getSourceTableList: (params) => {
    return request.get(`${api}/ship/deivce/source`, { params })
  },
  bindShip: ({ shipId, deviceId }) => {
    return request.put(`${api}/ship/deivce?deviceId=${deviceId}&shipId=${shipId}`)
  },
  unbindShip: ({ shipId, deviceId }) => {
    return request.put(`${api}/ship/deivce/unbind?deviceId=${deviceId}&shipId=${shipId}`)
  },
  getStream: (params) => {
    return request.get(`${api}/ship/deivce/stream`, { params })
  },
  getCount: (params) => {
    return request.get(`${api}/ship/deivce/count`)
  },
  streamStatus: (params) => {
    return request.get(`${api}/ship/deivce/stream/attr`, {params})
  }
}

/** 设备管理 */
export const deviceApis = {
  getTableData: (params) => {
    return request.get(`${api}/device`, { params })
  },
  del: (params) => {
    return request.delete(`${api}/device`, { data: params })
  },
  sysupgrade: (data) => {
    return request.post(`${api}/version/package/device`, data)
  },
  downloadLog: (params) => {
    return request.get(`${api}/log/device`, { params, responseType: 'blob'  })
  },
  resource: (params) => {
    return request.get(`${api}/system/resource`, { params })
  },
  restart: (data) => {
    return request.post(`/monitorstatus/device/reboot`, data)
  },
  syncData:()=>{
    return request.get(`${api}/ship/deivce/syncDevice`)
  },
}

export const sourceApis = {
  getTableData: (params) => {
    return request.get(`${api}/source`, { params })
  },
  // 添加摄像头
  add: (data) => {
    return request.post(`${api}/source`, data)
  },
  // 编辑摄像头
  edit: (data) => {
    return request.put(`${api}/source`, data)
  },
  editIndex: (data) => {
    return request.put(`${api}/source/index`, data)
  },
  // 删除摄像头
  del: (data) => {
    return request.delete(`${api}/source`, { data })
  },

  // 获取检测时间
  freq_time: () => {
    return request.get(`${api}/system/args`)
  },
  // 获取图片
  getImage: (params) => {
    return request.get(`/stream/image`, { params })
  },
  // 检测是否在线
  getAttr: (params) => {
    return request.get(`/stream/attr`, { params })
  },


  // 订阅视频流
  subscribeLive: (params) => {
    return request.get(`/stream/live/subscribe`, { params })
  },
  // 取消订阅视频流
  unsubscribeLive: (params) => {
    return request.get(`/stream/live/unsubscribe`, { params })
  },

  // srs接口
  detectStream: () => {
    let hostname =
      process.env.NODE_ENV === "production"
        ? window.location.hostname
        : "18.163.187.146";
    return request.get(`http://${hostname}:${KSPROXYGLOBAL.srs_http_api}/api/v1/streams?start=0&count=10000`)
    // return axios.request({
    //   url: `http://${hostname}:1985/api/v1/streams?start=0&count=100`,
    //   method: 'get',
    // });
  },

  getOnvifinfo: (params) => {
    return request.get(`${api}/source/onvif`, { params })
  },
}

export const forwardSourceApis = {
  getTableData: (params) => {
    return request.get(`${api}/source_forward`, { params })
  },
  // 添加摄像头
  add: (data) => {
    return request.post(`${api}/source_forward`, data)
  },
  // 编辑摄像头
  edit: (data) => {
    return request.put(`${api}/source_forward`, data)
  },
  editIndex: (data) => {
    return request.put(`${api}/source_forward/index`, data)
  },
  // 删除摄像头
  del: (data) => {
    return request.delete(`${api}/source_forward`, { data })
  },
}
