<template>
  <div class="daqing-live-page">
    <div class="live-container">
      <multivideo
        :playingCameraList="playingCameraList"
        :videonum="playingCameraList.videonum"
        :camObj="camObj"
        :mqttclient="mqttclient"
        id="home-video-container"
      ></multivideo>
    </div>
    <div class="tree-container">
      <div style="width: 100%; height: 50%; margin-bottom: 10px">
        <i class="z-icon-onevideo" @click="playingCameraList.videonum = 1"></i>
        <i class="z-icon-fourvideo" @click="playingCameraList.videonum = 4"></i>
        <camera
          :deviceObj="deviceObj"
          @change-source="handleSelCameraChg"
          :videonum="playingCameraList.videonum"
          :playingCameraList="playingCameraList"
        ></camera>
      </div>
      <div style="width: 100%; height: calc(50% - 10px)">
        <Alert :alertList="alertList"></Alert>
      </div>
    </div>
  </div>
</template>

<script>
import Multivideo from "./multivideo.vue";
import camera from "./camera.vue";
import Alert from "./alert.vue";
import { deviceApis, sourceApis, forwardSourceApis } from "@/api/device.js";
import { alertApis } from "@/api/alert.js";
import { projConfig } from "@/utils/config.js";
import mqttmixin from "@/utils/mqttmixin.js";
export default {
  mixins: [mqttmixin],
  components: { Multivideo, camera, Alert },
  data() {
    return {
      playingCameraList: {
        0: null,
        1: null,
        2: null,
        3: null,
        4: null,
        5: null,
        6: null,
        7: null,
        8: null,
        9: null,
        10: null,
        11: null,
        12: null,
        13: null,
        14: null,
        15: null,
        curPosition: -1, // 需要播放的位置 0，1，2，3，4，5，6，7，8
        videonum: sessionStorage.getItem("videoNum") || 1,
      },
      selcameras: {}, // 正在播放的所有摄像头的id,用于查询告警信息
      camObj: {},
      deviceObj: {},
      camlist: [],

      alertList: [],

      refreshInterval: null,
    };
  },
  watch: {
    "playingCameraList.videonum"(n, o) {
      sessionStorage.setItem("videoNum", n);
      if (n < o) {
        let playingCameraList = {
          0: null,
          1: null,
          2: null,
          3: null,
        };
        this.selcameras = {};

        for (let i = 0; i < o; i++) {
          if (this.playingCameraList[i]) {
            let id = this.playingCameraList[i];
            for (let j = 0; j < n; j++) {
              if (!playingCameraList[j]) {
                playingCameraList[j] = id;
                this.selcameras[id] = {
                  index: j,
                };
                break;
              }
            }
          }
        }
        this.playingCameraList = Object.assign(
          this.playingCameraList,
          playingCameraList
        );
        sessionStorage.setItem("sources", JSON.stringify(this.selcameras));
      }
    },
  },
  mounted() {
    this.getVideoList();
    this.getAlertList();
    // this.refreshInterval = setInterval(() => {
    //   this.getVideoList();
    // }, 10000);
  },
  beforeDestroy() {
    clearInterval(this.refreshInterval);
    this.refreshInterval = null;
  },
  beforeMount() {
    this.connectMqtt();
    this.mqttSubscribe();
  },
  methods: {
    mqttSubscribe() {
      this.mqttclient.subscribe("ks/proxy/alert/+", { qos: 0 }, (error) => {
        if (error) {
          console.log("subscribe error:", error);
          return;
        }
      });
      this.mqttclient.on("message", (topic, payload) => {
        let msg = JSON.parse(payload.toString());
        if (msg.msg_type == "alert" && this.$route.path == "/live") {
          let alert = JSON.parse(JSON.stringify(msg.data));
          alert.imgpath = projConfig.picprev + alert.image;

          this.alertList.unshift(alert);

          if (this.alertList.length > 9) {
            this.alertList.pop();
          }
        }
      });
    },
    getVideoList() {
      deviceApis.getTableData().then((deviceList) => {
        let deviceObj = {};
        for (let i = 0; i < deviceList.length; i++) {
          deviceList[i].device_id = deviceList[i].id;
          deviceObj[deviceList[i].id] = deviceList[i];
        }
        Promise.all([sourceApis.getTableData(),forwardSourceApis.getTableData()]).then(res => {
          for (let device_id in res[0]) {
            if (deviceObj[device_id] && JSON.stringify(res[0][device_id]) != '{}') {
              deviceObj[device_id].source = res[0][device_id];
              let source = res[0][device_id];
              for (let source_id in source) {
                let deviceId_sourceId = device_id + "_" + source_id;
                source[source_id].source_id = source_id;
                source[source_id].device_id = device_id;
                source[source_id].device_name = deviceObj[device_id].name;
                this.camObj[deviceId_sourceId] = source[source_id];
              }
            }
          }
          for (let device_id in res[1]) {
            if (deviceObj[device_id] && JSON.stringify(res[1][device_id]) != '{}') {
              deviceObj[device_id].source = res[1][device_id];
              let source = res[1][device_id];
              for (let source_id in source) {
                let deviceId_sourceId = device_id + "_" + source_id;
                source[source_id].source_id = source_id;
                source[source_id].device_id = device_id;
                source[source_id].device_name = deviceObj[device_id].name;
                this.camObj[deviceId_sourceId] = source[source_id];
              }
            }
          }
          this.deviceObj = JSON.parse(JSON.stringify(deviceObj));
        })
        
      });
    },
    handleSelCameraChg(checkedSource, newSource) {
      if (newSource) {
        if (this.playingCameraList.videonum == 1) {
          if (newSource.checked == true) {
            this.playingCameraList[0] = newSource.id; // deviceId_sourceId
            this.playingCameraList.curPosition = 0;
            this.selcameras = {};
            this.selcameras[newSource.id] = {
              index: 0,
            };
          } else {
            this.playingCameraList[0] = null; // deviceId_sourceId
            this.playingCameraList.curPosition = 0;
            this.selcameras = {};
          }
        } else {
          if (checkedSource[newSource.id]) {
            // 选中
            for (let i = 0; i < this.playingCameraList.videonum; i++) {
              if (!this.playingCameraList[i]) {
                this.playingCameraList[i] = newSource.id; // deviceId_sourceId
                this.playingCameraList.curPosition = i;
                this.selcameras[newSource.id] = {
                  index: i,
                };
                break;
              }
            }
          } else {
            // 取消选择
            for (let i = 0; i < this.playingCameraList.videonum; i++) {
              if (this.playingCameraList[i] == newSource.id) {
                this.playingCameraList[i] = null;
                this.playingCameraList.curPosition = i;
                delete this.selcameras[newSource.id];
                break;
              }
            }
          }
        }
      } else {
        this.selcameras = {};
        let sources = sessionStorage.getItem("sources");
        if (sources) sources = JSON.parse(sources);
        let playingCameraList = {
          0: null,
          1: null,
          2: null,
          3: null,
          curPosition: -1,
        };
        for (let source_id in checkedSource) {
          if (Object.keys(sources).indexOf(source_id) >= 0) {
            let index = sources[source_id].index;
            playingCameraList[index] = source_id;
            this.selcameras[source_id] = { index: index };
          }
        }
        this.playingCameraList = Object.assign(
          this.playingCameraList,
          playingCameraList
        );
      }
      sessionStorage.setItem("sources", JSON.stringify(this.selcameras));

      if (newSource && newSource.checked == false) {
        if (sessionStorage.getItem("curalgs")) {
          let curalgs = JSON.parse(sessionStorage.getItem("curalgs"));
          if (curalgs[newSource.id]) {
            delete curalgs[newSource.id];
          }
          sessionStorage.setItem("curalgs", JSON.stringify(curalgs));
        }
      }
    },
    getAlertList() {
      alertApis
        .getTableData({
          page: 1,
          size: 10,
        })
        .then((res) => {
          this.alertList = [...res.data].map((item) => {
            if (item.image) {
              item.imgpath = "";
            }
            return item;
          });
          this.alertList.forEach((item) => {
            getImage({ image_path: item.image }).then((imgres) => {
              if (imgres.error_code == 0) {
                item.imgpath = "data:image/png;base64," + imgres.data;
              }
            });
          });
        });
    },
  },
};
</script>

<style lang="less" scoped>
.daqing-live-page {
  display: flex;
  justify-content: space-between;
  width: 100vw;
  height: 100vh;
  padding: 10px;
  box-sizing: border-box;
  .live-container {
    width: calc(100% - 350px);
  }
  .tree-container {
    width: 340px;
    .z-icon-onevideo {
      margin-right: 5px;
    }
    .z-icon-onevideo,
    .z-icon-fourvideo {
      cursor: pointer;
    }
  }
}
</style>