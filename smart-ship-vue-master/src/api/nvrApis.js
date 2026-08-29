import request from '@/utils/request';
import {projConfig} from "@/utils/config";

let api = projConfig.curBaseUrl;

export const nvrApis = {
    getTableData: (params) => {
        return request.get(`${api}/ship/nvr/list`,{params})
    },
    deleteNvr: (ids) => {
        return request.delete(`${api}/ship/nvr/${ids}`)
    },
    updateNvr: (ship) => {
        return request.put(`${api}/ship/nvr`, ship)
    },
    addNvr: (ship) => {
        return request.post(`${api}/ship/nvr`, ship)
    },
    getVoidList:(id)=>{
      return request.get(`${api}/ship/nvr/getVoidList`,{params:{id:id}})
    },
    getLiveStream:(data)=>{
        return request.post(`${api}/ship/nvr/getLiveStream`,data)
    },
    checkPlayVideo:(params)=>{
        return request.get(`${api}/ship/nvr/checkPlayVideo`,{params})
    },
}