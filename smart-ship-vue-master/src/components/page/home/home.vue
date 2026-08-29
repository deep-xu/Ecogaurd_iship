<template>
  <div class="home" id="readAlarm" @click="handleReadAlarm">
    <Header></Header>
<!--    <div class="camera-nums">-->
<!--      <i class="z-icon-onevideo" @click="currentLayout = 1"></i>-->
<!--      <i class="z-icon-fourvideo" @click="currentLayout = 4"></i>-->
<!--      <i class="z-icon-fourvideo" @click="currentLayout = 9"></i>-->
<!--      <i class="z-icon-fourvideo" @click="currentLayout = 16"></i>-->
<!--      <el-tooltip placement="bottom">-->
<!--        <i class="z-icon-quanping" @click="handleQuanping(key)"></i>-->
<!--        <div slot="content">Full screen</div>-->
<!--      </el-tooltip>-->
<!--    </div>-->
    <div class="home-main-container">
      <div class="left">
        <PannelCompVue title="Monitor" classNames="pannel1">
<!--          <camera-->
<!--            :deviceObj="deviceObj"-->
<!--            @change-source="handleSelCameraChg"-->
<!--            :videonum="playingCameraList.videonum"-->
<!--            :playingCameraList="playingCameraList"-->
<!--          ></camera>-->
          <groupTree @change-source="handleCameraSelected"  :playingCameras="playingCameras"/>
        </PannelCompVue>
        <PannelCompVue title="Device Statistics" classNames="pannel2">
          <deviceNumsVue :deviceObj="deviceObj"></deviceNumsVue>
        </PannelCompVue>
      </div>
      <div class="center">
        <PannelCompVue classNames="pannel3">
<!--          <multivideo-->
<!--            :playingCameraList="playingCameraList"-->
<!--            :videonum="playingCameraList.videonum"-->
<!--            :camObj="camObj"-->
<!--            :mqttclient="mqttclient"-->
<!--            id="home-video-container"-->
<!--          ></multivideo>-->
          <CameraVideo :playing-cameras="playingCameras"
                       @camera-removed="removeCamera"
                       @stop-all="stopAllPlayers"
                       :currentLayout="currentLayout"
            />
        </PannelCompVue>
        <PannelCompVue title="Alarm Trends" classNames="pannel4">
          <alertChartVue :devices="historyDevices"></alertChartVue>
        </PannelCompVue>
      </div>
      <div class="right">
        <PannelCompVue title="Latest Alarm" classNames="pannel5">
          <Alert :alertList="alertList"></Alert>
        </PannelCompVue>
        <div class="xiaolaba-setting">
          <i class="z-iconfont z-icon-xiaolaba"></i>
          <ul class="xiaolaba">
            <li>
              <label>Volume</label
              ><el-slider
                v-model="xiaolaba.volume"
                :max="1"
                :min="0"
                :step="0.1"
                @change="handlePlayParamsChg"
              ></el-slider>
            </li>
            <li>
              <label>Speech</label
              ><el-slider
                v-model="xiaolaba.rate"
                :max="10"
                :min="0.1"
                :step="0.1"
                @change="handlePlayParamsChg"
              ></el-slider>
            </li>
            <li>
              <label>Tone</label
              ><el-slider
                v-model="xiaolaba.pitch"
                :max="2"
                :min="0"
                :step="0.1"
                @change="handlePlayParamsChg"
              ></el-slider>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Header from "./components/header.vue";
import camera from "./components/camera.vue";
import Multivideo from "./components/multivideo.vue";
import PannelCompVue from "./components/PannelComp.vue";
import Alert from "./components/alert.vue";
import {alert} from "@/api/index.js";
import {deviceApis, deviceV2Apis} from "@/api/device.js";
import {alertApis} from "@/api/alert.js";
import {projConfig} from "@/utils/config.js";
import alertChartVue from "./components/alertChart.vue";
import deviceNumsVue from "./components/deviceNums.vue";
import mqttmixin from "@/utils/mqttmixin.js";
import publicinfo from "@/utils/publicinfo.js";
import GroupTree from "@/components/page/home/components/GroupTree.vue";
import CameraVideo from "@/components/page/home/components/CameraVideo.vue";

let synth = window.speechSynthesis; // 获取语音合成对象
let voices = null; // 创建一个变量用来存储要使用的语音库
let getVoiceInterval = null;

function getVoice() {
  let voicesList = synth.getVoices(); // 获取可用的语音库列表
  for (var i = 0; i < voicesList.length; i++) {
    //  是否是中文语音库
    if (voicesList[i].lang === "zh-CN") {
      voices = voicesList[i]; // 找到中文语音库就传给 voices
      return true;
    }
  }
  return false;
}
export default {
  mixins: [mqttmixin, publicinfo],
  components: {
    GroupTree,
    Header,
    camera,
    Multivideo,
    PannelCompVue,
    Alert,
    alertChartVue,
    deviceNumsVue,
    CameraVideo,
  },
  data() {
    return {
      playingCameras: [],
      currentLayout:4,
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
      historyDevices: {},
      camlist: [],
      alertList: [],

      refreshInterval: null,
      xiaolaba: {
        volume: 1, // 音量
        pitch: 1, //音调，
        rate: 1, //语速
      },
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
  beforeMount() {
    if (sessionStorage.getItem("xiaolaba")) {
      this.xiaolaba = Object.assign(
        this.xiaolaba,
        JSON.parse(sessionStorage.getItem("xiaolaba"))
      );
    }
    // this.setStreamPort().then(() => {
      this.connectMqtt()
      this.mqttSubscribe()
    // });
  },
  mounted() {
    document.title = `Home`;
    this.getCount();
    getVoiceInterval = setInterval(() => {
      if (getVoice()) {
        clearInterval(getVoiceInterval);
        getVoiceInterval = null;
        document.getElementById("readAlarm").click();
      }
    }, 1000);
    // this.getVideoList();
    // this.getHistoryDevice();
    this.getAlertList();

    // this.refreshInterval = setInterval(() => {
    //   this.getVideoList();
    // }, 10000);
  },
  beforeDestroy() {
    clearInterval(this.refreshInterval);
    this.refreshInterval = null;
    sessionStorage.setItem("xiaolaba", JSON.stringify(this.xiaolaba));
  },
  methods: {
    async getCount() {
      this.deviceObj = await deviceV2Apis.getCount() || {};
      this.historyDevices = {};
      this.deviceObj.deviceAlertStatVOList.forEach((item) => {
        if (this.historyDevices[item.deviceId]) {
          this.historyDevices[item.deviceId].push({
            date: item.date,
            value: item.num
          })
        } else {
          this.historyDevices[item.deviceId] = [{
            date: item.date,
            value: item.num
          }]
        }
      })
    },
    mqttSubscribe() {
      this.mqttclient.subscribe("ks/proxy/alert/+", { qos: 0 }, (error) => {
        if (error) {
          console.log("subscribe error:", error);
          return;
        }
      });
      this.mqttclient.on("message", (topic, payload) => {
        let msg = JSON.parse(payload.toString());
        // && this.deviceObj[msg.data.device_id]
        if (
          msg.msg_type == "alert" &&
          this.$route.path == "/home" 
        ) {
          let deviceId_sourceId = msg.data.device_id + "_" + msg.data.source_id;
          let alg = msg.data.alg_name;
          if (
            this.camObj[deviceId_sourceId] &&
            this.camObj[deviceId_sourceId].alg[alg] &&
            this.camObj[deviceId_sourceId].alg[alg].reserved_args &&
            this.camObj[deviceId_sourceId].alg[alg].reserved_args.sound_text
          ) {
            this.readAlarm(
              this.camObj[deviceId_sourceId].alg[alg].reserved_args.sound_text
            );
          }
          let alert = JSON.parse(JSON.stringify(msg.data));
          alert.imgpath = projConfig.picprev + alert.image;

          this.alertList.unshift(alert);

          if (this.alertList.length > 9) {
            this.alertList.pop();
          }
        }
      });
    },
    handleReadAlarm() {
      this.readAlarm("");
    },
    handlePlayParamsChg() {
      sessionStorage.setItem("xiaolaba", JSON.stringify(this.xiaolaba));
    },
    readAlarm(alarmInfo) {
      // 如果没有中文语音库
      if (voices === null) {
        return false;
      }
      // 创建语音合成接口
      let utterThis = new SpeechSynthesisUtterance(alarmInfo);
      // utterThis.text = alarmInfo;
      utterThis.voice = voices; // 设置语音库
      utterThis.pitch = this.xiaolaba.pitch; // 设置音调
      utterThis.rate = this.xiaolaba.rate; // 设置语速
      utterThis.volume = this.xiaolaba.volume; // 设置语音音量为最大
      if (synth.speaking == false) {
        synth.speak(utterThis); // 开始朗读
      }
    },
    getVideoList() {
      deviceApis.getTableData().then((deviceList) => {
        let deviceObj = {};
        for (let i = 0; i < deviceList.length; i++) {
          deviceList[i].device_id = deviceList[i].id;
          deviceObj[deviceList[i].id] = deviceList[i];
        }
        // Promise.all([
        //   sourceApis.getTableData(),
        //   forwardSourceApis.getTableData(),
        // ]).then((res) => {
        //   for (let device_id in res[0]) {
        //     if (
        //       deviceObj[device_id] &&
        //       JSON.stringify(res[0][device_id]) != "{}"
        //     ) {
        //       deviceObj[device_id].source = res[0][device_id];
        //       let source = res[0][device_id];
        //       for (let source_id in source) {
        //         let deviceId_sourceId = device_id + "_" + source_id;
        //         source[source_id].source_id = source_id;
        //         source[source_id].device_id = device_id;
        //         source[source_id].device_name = deviceObj[device_id].name;
        //         this.camObj[deviceId_sourceId] = source[source_id];
        //       }
        //     }
        //   }
        //   for (let device_id in res[1]) {
        //     if (
        //       deviceObj[device_id] &&
        //       JSON.stringify(res[1][device_id]) != "{}"
        //     ) {
        //       deviceObj[device_id].source = res[1][device_id];
        //       let source = res[1][device_id];
        //       for (let source_id in source) {
        //         let deviceId_sourceId = device_id + "_" + source_id;
        //         source[source_id].source_id = source_id;
        //         source[source_id].device_id = device_id;
        //         source[source_id].device_name = deviceObj[device_id].name;
        //         this.camObj[deviceId_sourceId] = source[source_id];
        //       }
        //     }
        //   }
        //   this.deviceObj = JSON.parse(JSON.stringify(deviceObj));
        // });
      });
    },
    getHistoryDevice() {
      alertApis.device().then((devices) => {
        this.historyDevices = {};
        for (let deviceName in devices) {
          devices[deviceName].forEach((device_id) => {
            this.historyDevices[device_id] = {
              device_id: device_id,
              name: deviceName,
            };
          });
        }
      });
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
              item.imgpath = projConfig.picprev + item.image;
            }
            return item;
          });
        });
    },
    handleSelCameraChg(checkedSource, newSource) {
      console.log(checkedSource, newSource)
      if (newSource) {
        if (this.playingCameraList.videonum == 1) {
          if (newSource.checked == true) {
            this.playingCameraList[0] = `${newSource.id}__${newSource.deviceId}`; // deviceId_sourceId
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
                this.playingCameraList[i] = `${newSource.id}__${newSource.deviceId}`; // deviceId_sourceId
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
              if (this.playingCameraList[i] == `${newSource.id}__${newSource.deviceId}`) {
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

    handleQuanping() {
      var ele = document.querySelector("#home-video-container"); // 注意 这里要填一个容器 就是你要让他全屏的容器
      if (ele.requestFullscreen) {
        ele.requestFullscreen();
      } else if (ele.mozRequestFullScreen) {
        ele.mozRequestFullScreen();
      } else if (ele.webkitRequestFullScreen) {
        ele.webkitRequestFullScreen();
      }
    },
    // 处理摄像头选择
    handleCameraSelected(camera) {
      // 检查是否已经在播放
      const isAlreadyPlaying = this.playingCameras.some(
          playingCamera => playingCamera.id === camera.id
      );

      if (isAlreadyPlaying) {
        this.$message.warning(`The camera ${camera.label} is already playing`);
        return;
      }
      // 添加到播放列表
      this.playingCameras.push(camera);
    },

    // 移除摄像头
    removeCamera(cameraId) {
      this.playingCameras = this.playingCameras.filter(
          camera => camera.id !== cameraId
      );
    },

    // 停止所有播放器
    stopAllPlayers() {
      this.playingCameras = [];
    }
  },
};
</script>
<style lang="less" scoped>
.home {
  background-color: #001845;
  width: 100vw;
  min-width: 1200px;
  overflow-x: auto;
  height: 100vh;
  background: url(./imgs/bg.png) no-repeat;
  background-size: cover;
  background-position: center;
  position: relative;
  .camera-nums {
    position: absolute;
    top: 68rem;
    left: calc(23% + 40rem);
    z-index: 1;
    color: #00e2fd;
    i {
      margin-right: 5rem;
      font-size: 18rem;
      cursor: pointer;
    }
  }
  .home-main-container {
    position: absolute;
    top: 90rem;
    width: 100%;
    height: calc(100% - 110rem);
    display: flex;
    justify-content: space-evenly;
    > .left,
    > .right {
      width: 23%;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    > .center {
      width: calc(54% - 80rem);
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
  }
}
.xiaolaba-setting {
  position: absolute;
  right: 18rem;
  width: 180rem;
  z-index: 1;
  font-size: 14rem;
  .z-icon-xiaolaba {
    position: absolute;
    right: 10rem;
    cursor: pointer;
    color: #00e2fd;
    top: 8rem;
  }
  ul {
    padding-top: 36rem;
    display: none;
    list-style: none;
    li {
      background-color: #063e79;
      padding: 8px 5px 0;
      border-bottom: 1px solid #04305f;
      text-align: center;
      color: #00e2fd;
    }
  }
}
.xiaolaba-setting:hover ul {
  display: flex;
  flex-direction: column;
}
</style>
