import request from '@/utils/request';
import {projConfig} from "@/utils/config";

let api = projConfig.curBaseUrl;

export const nvrChannelApis = {
    getTableData: (params) => {
        return request.get(`${api}/ship/channel/list`,{params})
    },
    updateChannel: (channel) => {
        return request.put(`${api}/ship/channel`, channel)
    },
    syncChannel:(id)=>{
      return request.get(`${api}/ship/channel/syncChannel`,{params:{id:id}})
    },
    getChannelTree:()=>{
        return request.get(`${api}/ship/channel/getChannelTree`)
    },
}