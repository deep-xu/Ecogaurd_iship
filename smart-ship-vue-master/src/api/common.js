import store from '../store'
import request from '@/utils/request';
import { projConfig } from "@/utils/config";

export const getconfig = () => {
    return request.get(`${projConfig.picprev}/etc/ks_proxy_config.json?t=${new Date().getTime()}`)
}



export const getId = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0,
            v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}