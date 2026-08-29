import request from '@/utils/request';
import { projConfig } from "@/utils/config";
import {exportExcel} from "@/utils/exportExcel";

let api = projConfig.curBaseUrl;

export const shipApis = {
  getTableData: (params) => {
    return request.get(`${api}/ship/ship/list`,{params})
  },
  deleteShip: (ids) => {
    return request.delete(`${api}/ship/ship/${ids}`)
  },
  exportShip: (ship) => {
    return request.post(`${api}/ship/ship/export`, ship, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      responseType: 'blob'
    })
  },
  updateShip: (ship) => {
    return request.put(`${api}/ship/ship`, ship)
  },
  addShip: (ship) => {
    return request.post(`${api}/ship/ship`, ship)
  },
  treeShip: () => {
    return request.get(`${api}/ship/ship/tree`)
  },
  isPortAvailable:(host, port)=>{
    return request.get(`${api}/ship/ship/isPortAvailable`, {params:{host, port}})
  },
  isServerAvailable:(host, ak, sk, port)=>{
    return request.get(`${api}/ship/ship/isServerAvailable`, {params:{ak,sk,host, port}})
  },
  downloadLog:()=>{

    return request.get(`${api}/resource/oss/download/log`, { responseType: 'blob' })
      .then(response => {
        const url = window.URL.createObjectURL(new Blob([response]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'System.log');
        document.body.appendChild(link);
        link.click();
        link.remove();
      });
  }
}