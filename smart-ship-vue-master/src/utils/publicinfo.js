import { getsysargsToken } from "@/api/restart.js";
export default {
  data() {
    return {
    }
  },
  
  methods: {
    getStreamPort() {
      return new Promise((resolve,reject) => {
        getsysargsToken().then(res => {
          localStorage.setItem('streamports', JSON.stringify(res.map))
          KSPROXYGLOBAL.srs_server = res.map.srs_server;
          KSPROXYGLOBAL.srs_http_api = res.map.srs_http_api;
          KSPROXYGLOBAL.srs_http_server = res.map.srs_http_server;
          KSPROXYGLOBAL.websocket = res.map.websocket;
          resolve()
        })
      })
      
    },
    setStreamPort(){
      return new Promise((resolve,reject) => {
        if (!localStorage.getItem("streamports")) {
          this.getStreamPort().then(() => {
            resolve()
          });
        } else {
          let streamports = JSON.parse(localStorage.getItem("streamports"));
          KSPROXYGLOBAL.srs_server = streamports.srs_server;
          KSPROXYGLOBAL.srs_http_api = streamports.srs_http_api;
          KSPROXYGLOBAL.srs_http_server = streamports.srs_http_server;
          KSPROXYGLOBAL.websocket = streamports.websocket;
          resolve()
        }
      })
    }
  }
}