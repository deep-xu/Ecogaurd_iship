<template>
  <div
    class="videos"
    :class="{
      onevideo: videonum == 1,
      fourvideo: videonum == 4,
      ninevideo: videonum == 9,
      sixteenvideo: videonum == 16,
    }"
  >
    <div
      class="video-control-container"
      v-for="(item, key) in videosInfos"
      :key="key"
      v-show="key < videonum"
      :id="'video-container-' + key"
    >
      <div
        v-if="item && (item.loading || item.status)"
        style="
          width: 100%;
          height: 100%;
          display: flex;
          justify-content: center;
          align-items: center;
          position: absolute;
          z-index: 1;
          background-color: #000;
        "
      >
        <div v-if="item.loading" class="icon-dot"></div>
        <div
          v-if="item.status"
          style="
            font-size: 14rem;
            color: gray;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
          "
        >
          <i class="z-icon-jiankonglixian" style="font-size: 40rem"></i>
          <span>离线</span>
        </div>
      </div>
      <div class="location-container" v-if="playingCameraList[key]">
        <!-- <div class="device_id">设备：{{camObj[playingCameraList[key]].device_name || camObj[playingCameraList[key]].device_id}}</div> -->
        <span class="location" :title="camObj[playingCameraList[key]].desc"
          >数据源：{{
            camObj[playingCameraList[key]] &&
            camObj[playingCameraList[key]].desc
          }}</span
        >
        <div v-if="videosInfos[key] && videosInfos[key].deviceType == 'normal'" style="display: flex; align-items: center">
          <!--<div class="openWs">实时分析</div> -->
          <div class="alg-sel">
            <div
              class="algname"
              v-if="videosInfos[key].alg && videosInfos[key].alg.alg_ch_name"
            >
              {{ videosInfos[key].alg && videosInfos[key].alg.alg_ch_name }}

              <i class="z-icon-close" @click.stop="handleAlgChg(key)"></i>
            </div>
            <div v-else class="algname" style="color: gray">选择算法</div>
            <ul>
              <li
                v-for="(item, algname) in camObj[playingCameraList[key]].alg"
                :key="algname + Math.random()"
                @click="
                  videosInfos[key].algListShow = false;
                  handleAlgChg(key, item, algname);
                "
                :class="{
                  active:
                    videosInfos[key].alg &&
                    algname == videosInfos[key].alg.algname,
                }"
                :title="item.reserved_args.ch_name"
              >
                {{ item.reserved_args.ch_name }}
              </li>
            </ul>
          </div>
        </div>

        <el-tooltip placement="bottom">
          <div
            style="margin-left: 5rem; cursor: pointer"
            @click="handleRefresh(key)"
          >
            <i class="z-icon-shuaxin"></i>
          </div>
          <div slot="content">刷新</div>
        </el-tooltip>
        <el-tooltip placement="bottom">
          <div
            style="margin-left: 10px; cursor: pointer"
            @click="handleCapture(key)"
            v-if="videosInfos[key] && videosInfos[key].deviceType == 'normal'"
          >
            <i
              class="z-iconfont z-icon-zhuapai"
              v-if="videosInfos[key] && !videosInfos[key].captureLoading"
            ></i>
            <i
              class="z-iconfont z-icon-zhuapai"
              v-else
              style="opacity: 0.5"
              v-loading="videosInfos[key] && videosInfos[key].captureLoading"
              element-loading-spinner="el-icon-loading"
            ></i>
          </div>
          <div slot="content">抓拍</div>
        </el-tooltip>
      </div>

      <video
        ref="video"
        muted
        :id="'video' + key"
        class="video-js"
        autoplay="autoplay"
        preload="auto"
      ></video>
      <canvas class="canvas-shuju" :id="'canvas' + key"></canvas>
    </div>
  </div>
</template>

<script>
import { sourceApis, deviceV2Apis } from "@/api/device.js";
import { disconnect } from "echarts";
import { downloadBase64 } from "@/utils/utils.js";
export default {
  props: ["playingCameraList", "videonum", "camObj", "curAlg", "mqttclient"],
  data() {
    return {
      cameras: [],
      refreshing: -1,
      videosInfos: {
        0: null, // {srsrtc, oriWidth, oriHeight, canvas, actualWidth, actualHeight, loading}
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
      },
      stopIndex: -1,
      lastVideoNum: 0,

      detectSrsTimer: null,
    };
  },
  computed: {
    playingCameraListLoc() {
      return JSON.parse(JSON.stringify(this.$props.playingCameraList));
    },
  },
  watch: {
    selIndex(newval) {
      this.$emit("changeindex", newval);
    },
    camObj() {
      for (let i = 0; i < this.playingCameraList.videonum; i++) {
        let deviceId_cameraId = this.playingCameraList[i];
        if (this.$props.camObj[deviceId_cameraId]) {
          // 算法列表中没有当前选择的算法了，需要处理显示
          console.log(this.$props.camObj[deviceId_cameraId]);
        }
      }
    },
    playingCameraListLoc: {
      deep: true,
      handler(newdata, olddata) {
        if (newdata.videonum != olddata.videonum) {
          this.cameras = [];
          this.destroyVideo(4);
          this.$nextTick(() => {
            for (let i = 0; i < 4; i++) {
              if (newdata[i]) {
                this.getCameraSize(this.playingCameraList[i], i);
                this.videoSubscribe(this.playingCameraList[i], i);
              }
            }
          });
          return;
        }
        let pos = newdata.curPosition;
        if (pos != -1) {
          if (this.videosInfos[pos]) {
            this.destoryVideoByIndex(pos);
          }
          if (newdata[pos] && newdata[pos] != olddata[pos]) {
            // this.getCameraSize(newdata[pos], pos);
            this.videoSubscribe(newdata[pos], pos);
          }
        } else {
          // 判断哪个位置有变化
          for (let i = 0; i < 4; i++) {
            if (newdata[i] && newdata[i] != olddata[i]) {
              this.destoryVideoByIndex(i);

              // this.getCameraSize(this.playingCameraList[i], i);
              this.videoSubscribe(this.playingCameraList[i], i);
            } else if (!newdata[i] && olddata[i]) {
              this.destoryVideoByIndex(i);
            }
          }
        }
      },
    },
  },
  //方法集合
  methods: {
    videoSubscribe(cameraId, index) {
      if (
        !this.cameras[index] ||
        this.cameras[index] != cameraId ||
        this.refreshing == index
      ) {
        this.cameras[index] = cameraId;
        // this.detectVideo(cameraId, index).then((res) => {
        //   if (cameraId == this.playingCameraList[index]) {
        this.subscribeLive(cameraId, index);
        //   }
        // });
      }
    },
    detectSrs() {
      this.detectSrsTimer = setInterval(() => {
        sourceApis.detectStream().then((res) => {
          if (document.visibilityState == "visible" && res.code == 0) {
            for (let i = 0; i < this.playingCameraList.videonum; i++) {
              let deviceId_cameraId = this.playingCameraList[i];
              if (
                deviceId_cameraId &&
                this.videosInfos[i] &&
                this.videosInfos[i].status != "离线"
              ) {
                let deviceId = this.$props.camObj[deviceId_cameraId].device_id;
                let cameraId = this.$props.camObj[deviceId_cameraId].source_id;
                let streamInfo = res.streams.find(
                  (item) => item.url.indexOf(`${deviceId}/${cameraId}`) > 0
                );
                if (
                  !streamInfo ||
                  (streamInfo && streamInfo.publish.active == false)
                ) {
                  this.destoryVideoByIndex(i);
                  this.getCameraSize(deviceId_cameraId, i);
                  this.videoSubscribe(deviceId_cameraId, i);
                }
              }
            }
          }
        });
      }, 10000);
    },
    detectVideo(cameraId, index) {
      return new Promise((resolve, reject) => {
        sourceApis
          .getAttr({
            device_id: this.$props.camObj[cameraId].device_id,
            stream: encodeURI(this.$props.camObj[cameraId].stream),
            draw_size: 1280,
          })
          .then((res) => {
            if (JSON.stringify(res) == "{}") {
              if (cameraId == this.playingCameraList[index]) {
                this.videosInfos[index].status = "离线";
                this.videosInfos[index].loading = false;
              }
              reject();
            } else {
              resolve();
            }
          })
          .catch((err) => {
            this.videosInfos[index].status = "离线";
            this.videosInfos[index].loading = false;
            reject();
          });
      });
    },
    handleRefresh(index) {
      if (!this.videosInfos[index]) {
        return;
      }
      if (this.videosInfos[index].status == "离线") {
        this.destoryVideoByIndex(index);
        this.getCameraSize(this.playingCameraList[index], index);
        this.videoSubscribe(this.playingCameraList[index], index);
      } else {
        if (!this.videosInfos[index].stream) {
          return;
        }
        let video = document.getElementById("video" + index);
        video && (video.srcObject = null);
        if (this.videosInfos[index] && this.videosInfos[index].replayTimer) {
          clearTimeout(this.videosInfos[index].replayTimer);
          this.videosInfos[index].replayTimer = null;
        }
        this.videosInfos[index] &&
          this.videosInfos[index].srsrtc &&
          this.videosInfos[index].srsrtc.destroy();
        this.videosInfos[index].srsrtc = null;
        this.videosInfos[index].status = "";
        this.playVideo(this.playingCameraList[index], index);
      }
    },
    handleCapture(index) {
      this.videosInfos[index].captureLoading = true;
      let cameraInfo = this.$props.camObj[this.playingCameraList[index]];
      sourceApis
        .getImage({
          device_id: cameraInfo.device_id,
          stream: cameraInfo.stream,
          draw_size: JSON.stringify(cameraInfo.draw_size),
        })
        .then((res) => {
          this.videosInfos[index].captureLoading = false;
          downloadBase64(
            "data:image/jpg;base64," + res.data,
            `capture_${cameraInfo.desc}`
          );
        })
        .catch((err) => {
          this.videosInfos[index].captureLoading = false;
          if (err && err.message && err.message[this.lang]) {
            this.$message({
              type: "error",
              message: err.message[this.lang],
            });
          }
        });
    },
    subscribeLive(cameraId, index) {
      sourceApis
        .subscribeLive({
          device_id: this.$props.camObj[cameraId].device_id,
          source_id: this.$props.camObj[cameraId].source_id,
        })
        .then((data) => {
          let stream = data.data;
          if (data && stream) {
            this.videosInfos[index].stream = stream;
            this.playVideo(cameraId, index);
          } else {
            if (this.playingCameraList[index] == cameraId) {
              this.videosInfos[index].status = "离线";
              this.videosInfos[index].loading = false;
              // this.reSubcribe(cameraId, index);
            }
          }
        })
        .catch((err) => {
          if (
            this.playingCameraList[index] == cameraId &&
            this.videosInfos[index]
          ) {
            this.videosInfos[index].status = "离线";
            this.videosInfos[index].loading = false;
            // this.reSubcribe(cameraId, index);
          }
        });
    },
    playVideo(cameraId, index) {
      if (!this.playingCameraListLoc[index]) {
        this.videosInfos[index] = null;
        return;
      }
      if (this.videosInfos[index].srsrtc) {
        return;
      }
      this.videosInfos[index].loading = true;
      let hostname =
        process.env.NODE_ENV === "production"
          ? window.location.hostname
          : "18.163.187.146";
      this.$nextTick(() => {
        let video = document.getElementById("video" + index);
        let stream = this.videosInfos[index].stream;
        var srsrtc;
        if (stream.indexOf("webrtc") >= 0) {
          let stream1 =
            "webrtc://" + hostname + "/live" + stream.split("/live")[1];
          srsrtc = new JSWebrtc.Player(stream1, {
            video: video,
            autoplay: true,
            onPlay: (obj) => {
              this.videosInfos[index].loading = false;
              this.videosInfos[index].playerState = "success";

              // if (this.videosInfos[index].refreshTimeInterval) {
              //   clearInterval(this.videosInfos[index].refreshTimeInterval);
              // }
              // this.videosInfos[index].refreshTime =
              //   parseInt((Math.random() * 5 + 5) * 1000) * 60;
              // this.videosInfos[index].refreshTimeInterval = setInterval(() => {
              //   this.handleRefresh(index);
              // }, this.videosInfos[index].refreshTime);
            },
          });
        } else if (stream.indexOf(".flv") >= 0) {
          let stream1 = `http://${hostname}:${
            KSPROXYGLOBAL.srs_http_server
          }/live${stream.split("/live")[1]}`;
          srsrtc = mpegts.createPlayer(
            {
              type: "flv",
              url: stream1,
              isLive: true,
            },
            { enableWorker: true }
          );
          srsrtc.attachMediaElement(video);
          srsrtc.load();

          srsrtc
            .play()
            .then((res) => {
              this.videosInfos[index].playerState = "success";
              this.videosInfos[index].loading = false;
              if (this.videosInfos[index].refreshTimeInterval) {
                clearInterval(this.videosInfos[index].refreshTimeInterval);
              }
              this.videosInfos[index].refreshTime =
                parseInt((Math.random() * 5 + 5) * 1000) * 60;
              this.videosInfos[index].refreshTimeInterval = setInterval(() => {
                this.handleRefresh(index);
              }, this.videosInfos[index].refreshTime);
            })
            .catch((err) => {});
          if (this.videosInfos[index].replayTimer) {
            clearTimeout(this.videosInfos[index].replayTimer);
          }
          this.videosInfos[index].replayTimer = setTimeout(() => {
            this.replayflv(srsrtc, cameraId, index);
          }, 3000);
        } else {
          video.src = "staticdata/" + stream.split("/home/linaro/ks/")[1];
        }

        this.videosInfos[index].srsrtc = srsrtc;
      });
    },
    replayflv(srsrtc, cameraId, index) {
      if (!this.videosInfos[index]) {
        return;
      }
      if (this.videosInfos[index].playerState == "success") {
        return;
      } else {
        srsrtc.unload();
        srsrtc.load();
        srsrtc
          .play()
          .then((res) => {
            this.videosInfos[index].playerState = "success";
            this.videosInfos[index].loading = false;
            if (this.videosInfos[index].refreshTimeInterval) {
              clearInterval(this.videosInfos[index].refreshTimeInterval);
            }
            this.videosInfos[index].refreshTime =
              parseInt((Math.random() * 5 + 5) * 1000) * 60;
            this.videosInfos[index].refreshTimeInterval = setInterval(() => {
              this.handleRefresh(index);
            }, this.videosInfos[index].refreshTime);
          })
          .catch((err) => {
            // this.destoryVideoByIndex(index);
            // this.getCameraSize(cameraId, index);
            // this.videoSubscribe(cameraId, index);
          });
        if (this.videosInfos[index].replayTimer) {
          clearTimeout(this.videosInfos[index].replayTimer);
        }
        this.videosInfos[index].replayTimer = setTimeout(() => {
          this.replayflv(srsrtc, cameraId, index);
        }, 3000);
      }
    },
    reSubcribe(cameraId, index) {
      if (this.videosInfos[index].subscribeTimeout) {
        clearTimeout(this.videosInfos[index].subscribeTimeout);
        this.videosInfos[index].subscribeTimeout = null;
      }
      if (this.$route.path == "/live") {
        this.videosInfos[index].subscribeTimeout = setTimeout(() => {
          this.subscribeLive(cameraId, index);
        }, 1000);
      }
    },
    getCameraSize(id, index) {
      if (this.$props.camObj[id].draw_size) {
        this.setOrisize(
          this.$props.camObj[id].draw_size[0],
          this.$props.camObj[id].draw_size[1],
          index
        );
      } else {
        this.setOrisize(false, false, index);
      }
    },
    setOrisize(width, height, index) {
      let container = document.querySelector(".video-control-container");
      if (!container) {
        return;
      }
      if (!this.videosInfos[index]) {
        this.videosInfos[index] = {
          id: "",
          loading: true,
          openWs: true,
          algListShow: false,
          subscribeTimeout: null,
          refreshTimeInterval: null, // 定时刷新定时器
          refreshTime: null, // 定时刷新时间
          replayTimer: null,
          playerState: "pending",
          detectInterval: null,
          quanping: false,
          srsrtc: null,
          stream: "",
          status: "",
          captureLoading: false,
          deviceType:'forward'
        };
        if (width) {
          let alg = null;
          if (sessionStorage.getItem("curalgs")) {
            let cameraId = this.playingCameraList[index];
            let curalgs = JSON.parse(sessionStorage.getItem("curalgs"));
            alg = curalgs[cameraId]
              ? JSON.parse(JSON.stringify(curalgs[cameraId]))
              : null;
          }
          this.videosInfos[index].alg = alg;
          this.videosInfos[index].deviceType = 'normal'
        }
      }
      if (this.videosInfos[index] && width) {
        let oriWidth = width;
        let oriHeight = height;
        if (!this.videosInfos[index].oriWidth) {
          this.$set(this.videosInfos[index], "oriWidth", oriWidth);
          this.$set(this.videosInfos[index], "oriHeight", oriHeight);
        } else {
          this.videosInfos[index].oriWidth = oriWidth;
          this.videosInfos[index].oriHeight = oriHeight;
        }
        if (
          oriWidth / container.offsetWidth >
          oriHeight / container.offsetHeight
        ) {
          this.$set(
            this.videosInfos[index],
            "actualWidth",
            container.offsetWidth
          );
          this.$set(
            this.videosInfos[index],
            "actualHeight",
            container.offsetWidth / (oriWidth / oriHeight)
          );
        } else {
          this.$set(
            this.videosInfos[index],
            "actualHeight",
            container.offsetHeight
          );
          this.$set(
            this.videosInfos[index],
            "actualWidth",
            container.offsetHeight * (oriWidth / oriHeight)
          );
        }
        // videoWidth = this.videosInfos[index].actualWidth;
        this.setPosition(
          this.videosInfos[index].actualWidth,
          this.videosInfos[index].actualHeight,
          index
        );
      }
    },
    setPosition(width, height, index) {
      let container = document.querySelector(".video-control-container");
      let video = document.querySelector("#video" + index);
      let canvas = document.getElementById("canvas" + index);
      video.style.position = "absolute";
      video.style.width = width + "px";
      video.style.height = height + "px";
      canvas.width = width;
      canvas.height = height;
      if (width < container.offsetWidth) {
        let left = (container.offsetWidth - width) / 2;
        video.style.left = Math.floor(left) + "px";
        video.style.top = 0 + "px";
        canvas.style.left = Math.floor(left) + "px";
        canvas.style.top = "0px";
      }
      if (height < container.offsetHeight) {
        let top = (container.offsetHeight - height) / 2;
        video.style.top = Math.floor(top) + "px";
        video.style.left = 0 + "px";
        canvas.style.top = Math.floor(top) + "px";
        canvas.style.left = "0px";
      }
    },
    clearCanvas(canvasIndex) {
      let canvas = document.getElementById("canvas" + canvasIndex);
      if (canvas && canvas.getContext("2d")) {
        canvas
          .getContext("2d")
          .clearRect(0, 0, canvas.offsetWidth, canvas.offsetHeight);
      }
    },
    setAlarms(data, index) {
      this.clearCanvas(index);
      if (this.videosInfos[index] && !this.videosInfos[index].canvas) {
        this.$set(
          this.videosInfos[index],
          "canvas",
          document.getElementById("canvas" + index)
        );
      }
      if (
        !this.videosInfos[index] ||
        !this.videosInfos[index].actualWidth ||
        !this.videosInfos[index].actualHeight ||
        !this.videosInfos[index].oriWidth ||
        !this.videosInfos[index].oriHeight
      ) {
        return;
      }
      // let bbox = data.result.data.bbox;
      let bbox = data.bbox;
      if (Object.values(bbox.polygons).length > 0) {
        Object.values(bbox.polygons).forEach((item) => {
          let color = JSON.parse(JSON.stringify(item.color)).reverse();
          // let color = item.color;
          let points = item.polygon.map((point) => {
            return [
              Math.round(
                (point[0] * this.videosInfos[index].actualWidth) /
                  this.videosInfos[index].oriWidth
              ),
              Math.round(
                (point[1] * this.videosInfos[index].actualHeight) /
                  this.videosInfos[index].oriHeight
              ),
            ];
          });
          let context = this.videosInfos[index].canvas.getContext("2d");
          context.font = "20px Arial bolder";
          context.fillStyle = "transparent";
          context.strokeStyle = "rgb(" + color.join(",") + ")";
          context.lineWidth = 2;
          this.drawPolygons(points, context);
          this.drawPolygonInfo(context, Object.values(bbox.polygons), index);
        });
      }

      if (bbox.rectangles.length > 0) {
        bbox.rectangles.forEach((item, i) => {
          let color = JSON.parse(JSON.stringify(item.color)).reverse();
          let coordinates = {
            x: Math.round(
              (item.xyxy[0] * this.videosInfos[index].actualWidth) /
                this.videosInfos[index].oriWidth
            ),
            y: Math.round(
              (item.xyxy[1] * this.videosInfos[index].actualHeight) /
                this.videosInfos[index].oriHeight
            ),
            x1: Math.round(
              (item.xyxy[2] * this.videosInfos[index].actualWidth) /
                this.videosInfos[index].oriWidth
            ),
            y1: Math.round(
              (item.xyxy[3] * this.videosInfos[index].actualHeight) /
                this.videosInfos[index].oriHeight
            ),
          };
          let context = this.videosInfos[index].canvas.getContext("2d");
          context.font = "20px Arial bolder";
          context.fillStyle = "rgb(" + color.join(",") + ")";
          context.fillText(item.label || "", coordinates.x, coordinates.y - 10);
          context.strokeStyle = "rgb(" + color.join(",") + ")";
          context.lineWidth = 2;
          // context.strokeRect(
          //   coordinates.x,
          //   coordinates.y,
          //   coordinates.x1 - coordinates.x,
          //   coordinates.y1 - coordinates.y
          // );
          let lines = [];
          let lineWidth = (coordinates.x1 - coordinates.x) / 4;
          let lineHeight = (coordinates.y1 - coordinates.y) / 4;
          lines[0] = {
            x: coordinates.x,
            y: coordinates.y,
            x1: coordinates.x + lineWidth,
            y1: coordinates.y,
          };
          lines[1] = {
            x: coordinates.x,
            y: coordinates.y,
            x1: coordinates.x,
            y1: coordinates.y + lineHeight,
          };
          lines[2] = {
            x: coordinates.x1,
            y: coordinates.y,
            x1: coordinates.x1 - lineWidth,
            y1: coordinates.y,
          };
          lines[3] = {
            x: coordinates.x1,
            y: coordinates.y,
            x1: coordinates.x1,
            y1: coordinates.y + lineHeight,
          };
          lines[4] = {
            x: coordinates.x,
            y: coordinates.y1,
            x1: coordinates.x + lineWidth,
            y1: coordinates.y1,
          };
          lines[5] = {
            x: coordinates.x,
            y: coordinates.y1,
            x1: coordinates.x,
            y1: coordinates.y1 - lineHeight,
          };
          lines[6] = {
            x: coordinates.x1,
            y: coordinates.y1,
            x1: coordinates.x1 - lineWidth,
            y1: coordinates.y1,
          };
          lines[7] = {
            x: coordinates.x1,
            y: coordinates.y1,
            x1: coordinates.x1,
            y1: coordinates.y1 - lineHeight,
          };
          lines.forEach((item) => {
            this.drawLine(context, item);
          });
        });
      }
      if (Object.values(bbox.lines).length > 0) {
        Object.values(bbox.lines).forEach((item, i) => {
          let color = JSON.parse(JSON.stringify(item.color)).reverse();
          let coordinates = {
            x: Math.round(
              (item.line[0][0] * this.videosInfos[index].actualWidth) /
                this.videosInfos[index].oriWidth
            ),
            y: Math.round(
              (item.line[0][1] * this.videosInfos[index].actualHeight) /
                this.videosInfos[index].oriHeight
            ),
            x1: Math.round(
              (item.line[1][0] * this.videosInfos[index].actualWidth) /
                this.videosInfos[index].oriWidth
            ),
            y1: Math.round(
              (item.line[1][1] * this.videosInfos[index].actualHeight) /
                this.videosInfos[index].oriHeight
            ),
          };
          let context = this.videosInfos[index].canvas.getContext("2d");
          context.font = "20px Arial bolder";
          context.fillStyle = "rgb(" + color.join(",") + ")";
          if (item.ext.direction) {
            context.fillText(
              item.name,
              (coordinates.x + coordinates.x1) / 2,
              (coordinates.y + coordinates.y1) / 2 + 20
            );
          }

          context.strokeStyle = "rgb(" + color.join(",") + ")";
          context.lineWidth = 2;
          this.drawLine(context, coordinates);
          this.drawCountingInfo(context, Object.values(bbox.lines));
        });
      }
    },
    drawPolygons(points, context) {
      context.beginPath();
      context.moveTo(points[0][0], points[0][1]);

      for (var i = 1; i < points.length; i++) {
        context.lineTo(points[i][0], points[i][1]);
      }
      context.closePath();
      context.fill();
      context.stroke();
    },
    drawLine(ctx, line) {
      ctx.beginPath();
      ctx.moveTo(line.x, line.y);
      ctx.lineTo(line.x1, line.y1);
      ctx.stroke();
    },
    drawCountingInfo(context, lines) {
      lines.forEach((item, index) => {
        context.fillStyle = "rgb(255,0,0)";
        if (item.ext.direction.length == 2) {
          context.fillText(
            `[${item.name}] ${item.ext.action.count}: ${item.ext.result.count}`,
            0,
            20 * index + 20
          );
        } else {
          context.fillText(
            `[${item.name}] ${item.ext.action.increase}: ${item.ext.result.increase},${item.ext.action.decrease}: ${item.ext.result.decrease},${item.ext.action.delta}: ${item.ext.result.delta}`,
            0,
            20 * index + 20
          );
        }
      });
    },
    drawPolygonInfo(context, polygons, videoindex) {
      polygons.forEach((item, index) => {
        context.fillStyle =
          "rgb(" +
          JSON.parse(JSON.stringify(item.color)).reverse().join(",") +
          ")";
        let leftPoint = item.polygon[0];
        for (let i = 1; i < item.polygon.length; i++) {
          if (item.polygon[i][0] < leftPoint[0]) {
            leftPoint = item.polygon[i];
          }
        }
        context.fillText(
          `${item.name}`,
          (leftPoint[0] * this.videosInfos[videoindex].actualWidth) /
            this.videosInfos[videoindex].oriWidth,
          (leftPoint[1] * this.videosInfos[videoindex].actualHeight) /
            this.videosInfos[videoindex].oriHeight +
            20
        );
        if (item.ext.result) {
          context.fillStyle = "rgb(255,0,0)";
          context.fillText(
            `${item.name}: ${item.ext.result}`,
            0,
            20 * index + 20
          );
        }
      });
    },
    destroyVideo(videonum) {
      for (let i = 0; i < videonum; i++) {
        this.destoryVideoByIndex(i);
      }
    },
    destoryVideoByIndex(index) {
      this.clearCanvas(index);
      if (this.videosInfos[index]) {
        if (
          this.videosInfos[index] &&
          this.videosInfos[index].subscribeTimeout
        ) {
          clearTimeout(this.videosInfos[index].subscribeTimeout);
          this.videosInfos[index].subscribeTimeout = null;
        }
        if (this.videosInfos[index] && this.videosInfos[index].replayTimer) {
          clearTimeout(this.videosInfos[index].replayTimer);
          this.videosInfos[index].replayTimer = null;
        }
        if (this.videosInfos[index].refreshTimeInterval) {
          clearInterval(this.videosInfos[index].refreshTimeInterval);
          this.videosInfos[index].refreshTimeInterval = null;
        }
        let video = document.getElementById("video" + index);
        video && (video.srcObject = null);
        this.videosInfos[index].srsrtc &&
          this.videosInfos[index].srsrtc.destroy();
        this.clearCanvas(index);
        this.videosInfos[index] = null;
        this.cameras[index] = null;
      }
    },
    visibilityChange() {
      if (document.visibilityState == "hidden") {
        this.cameras = [];
        this.destroyVideo(this.$props.videonum);
        clearInterval(this.detectSrsTimer);
        this.detectSrsTimer = null;
      } else if (document.visibilityState == "visible") {
        if (this.$route.path == "/live") {
          //切换到该页面时执行
          for (let i = 0; i < this.$props.videonum; i++) {
            if (this.playingCameraList[i]) {
              this.getCameraSize(this.playingCameraList[i], i);
              this.videoSubscribe(this.playingCameraList[i], i);
            }
          }
          // this.detectSrs();
        }
      }
    },
    detectVideoStatus(cameraId, index) {
      this.videosInfos[index].detectInterval = setInterval(() => {
        sourceApis.detectStream().then((res) => {
          let status = "离线";
          for (let i = 0; i < res.data.streams.length; i++) {
            if (res.data.streams[i].name == cameraId) {
              status = "在线";
            }
          }
          if (status == "离线") {
            clearInterval(this.videosInfos[index].detectInterval);
            this.videosInfos[index].detectInterval = null;
            this.subscribeLive(cameraId, index);
          } else {
            status = "离线";
          }
        });
      }, 3000);
    },
    handleAlgChg(index, item, algname) {
      let curalgs = {};
      if (sessionStorage.getItem("curalgs")) {
        curalgs = JSON.parse(sessionStorage.getItem("curalgs"));
      }

      let sourceId = this.playingCameraList[index];
      if (item) {
        let alg = {
          algname: algname,
          alg_ch_name: item.reserved_args.ch_name,
        };
        this.videosInfos[index].alg = alg;
        curalgs[sourceId] = alg;
      } else {
        this.videosInfos[index].alg = null;
        curalgs[sourceId] = null;
      }
      sessionStorage.setItem("curalgs", JSON.stringify(curalgs));
    },
    mqttSubscribe() {
      if (this.$props.mqttclient) {
        this.$props.mqttclient.subscribe(
          "ks/proxy/result/+",
          { qos: 0 },
          (error) => {
            if (error) {
              console.log("subscribe error:", error);
              return;
            }
          }
        );
        this.$props.mqttclient.on("message", (topic, payload) => {
          let msg = JSON.parse(payload.toString());
          if (msg.msg_type == "result" && this.$route.path == "/live") {
            let id = msg.data.device.id + "_" + msg.data.source.id;
            for (let i = 0; i < 4; i++) {
              if (this.videosInfos[i]) {
                let alg =
                  this.videosInfos[i].alg && this.videosInfos[i].alg.algname;
                if (
                  id == this.playingCameraList[i] &&
                  msg.data.alg.name == alg
                ) {
                  this.setAlarms(msg.data.reserved_data, i);
                  if (
                    this.videosInfos[i] &&
                    this.videosInfos[i].canvasTimeout
                  ) {
                    clearTimeout(this.videosInfos[i].canvasTimeout);
                  }
                  this.videosInfos[i].canvasTimeout = setTimeout(() => {
                    this.clearCanvas(i);
                  }, 1000);
                  break;
                }
              }
            }
          }
        });
      }
    },
  },
  created() {},
  mounted() {
    // this.detectSrs();
    for (let i = 0; i < this.$props.videonum; i++) {
      if (this.playingCameraList[i]) {
        this.getCameraSize(this.playingCameraList[i], i);
        this.videoSubscribe(this.playingCameraList[i], i);
      }
    }
    document.addEventListener("visibilitychange", this.visibilityChange);

    let resizeEvt =
      "orientationchange" in window ? "orientationchange" : "resize";
    window.addEventListener(
      resizeEvt,
      () => {
        for (let i = 0; i < this.$props.videonum; i++) {
          if (this.videosInfos[i] && this.videosInfos[i].oriWidth) {
            this.setOrisize(
              this.videosInfos[i].oriWidth,
              this.videosInfos[i].oriHeight,
              i
            );
          }
        }
      },
      false
    );

    window.addEventListener("unload", () => {
      this.destroyVideo(this.$props.videonum);
    });
    window.addEventListener("beforeunload", () => {
      this.destroyVideo(this.$props.videonum);
    });
    window.addEventListener("keydown", (e) => {
      if (e.key == "F5") {
        this.destroyVideo(this.$props.videonum);
      }
    });
    this.mqttSubscribe();
  },
  beforeDestroy() {
    window.removeEventListener("unload", (e) => {});
    window.removeEventListener("beforeunload", (e) => {});
    window.removeEventListener("keydown", (e) => {});
    document.removeEventListener("visibilitychange", this.visibilityChange);
    clearInterval(this.detectSrsTimer);
    this.detectSrsTimer = null;
    this.destroyVideo(this.$props.videonum);
  },
};
</script>
<style scoped lang="less">
.icon-dot {
  position: relative;
  display: block;
  border-radius: 50%;
  background-color: #39f;
  width: 40rem;
  height: 40rem;
  animation: ani-spin-bounce 1s 0s ease-in-out infinite;
}
@keyframes ani-spin-bounce {
  0% {
    transform: scale(0);
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}
.videos {
  width: 100%;
  height: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  overflow: hidden;
}
.onevideo .video-control-container {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #000;
}
.fourvideo .video-control-container {
  width: calc((100% - 2px) / 2);
  height: calc((100% - 2px) / 2);
  position: relative;
  background-color: #000;
}
.ninevideo .video-control-container {
  width: calc((100% - 12px) / 3);
  height: calc((100% - 12px) / 3);
  position: relative;
  background-color: #000;
}
.sixteenvideo .video-control-container {
  width: calc((100% - 18px) / 4);
  height: calc((100% - 18px) / 4);
  position: relative;
  background-color: #000;
}
.video-control-container /deep/.el-loading-mask {
  background-color: transparent;
}

.video-control-container .z-icon-refresh {
  position: absolute;
  top: 14px;
  right: 50px;
  font-size: 20px;
  color: #ccc;
  cursor: pointer;
  z-index: 1;
}
.z-icon-refresh.active {
  animation: refreshing 5s linear infinite;
  transform: rotateZ(360deg);
}
.video-control-container:hover .location-container {
  opacity: 1;
}
.location-container {
  position: absolute;
  z-index: 1;
  width: 100%;
  background-color: rgb(29 44 63 / 80%);
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  opacity: 0;
  .device_id {
    width: 100%;
    height: 32rem;
    line-height: 32rem;
    color: #fff;
    font-size: 14rem;
    box-sizing: border-box;
    padding: 0 8rem;
  }
  .z-icon-quanping,
  .z-icon-tuichuquanping {
    color: #fff;
    margin-left: 10rem;
    font-size: 20rem;
    margin-right: 10rem;
    cursor: pointer;
  }
  .z-icon-shuaxin {
    color: #fff;
    margin-left: 8rem;
    font-size: 18rem;
  }
  .z-icon-zhuapai{
    color: #fff;
    font-size: 18rem;
    /deep/.el-loading-mask{
      top:20px;
    }
  }
}
.location-container:hover {
  opacity: 1;
}
// .video-control-container:hover {
//   .location-container {
//     opacity: 1;
//   }
// }
.location-container .location {
  color: #fff;
  height: 32rem;
  line-height: 32rem;
  max-width: calc(100% - 200rem);
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  padding-left: 5rem;
  font-size: 14rem;
}
.location-container .openWs {
  color: #fff;
  height: 32rem;
  line-height: 32rem;
  width: auto;
  font-size: 14rem;
}
.location-container .alg-sel {
  margin-left: 10rem;
  position: relative;
  .algname {
    height: 32rem;
    line-height: 32rem;
    text-align: left;
    width: 150rem;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    word-break: break-all;
    box-sizing: border-box;
    border: 1rem solid #053b69;
    border-radius: 0;
    padding-left: 10rem;
    padding-right: 25rem;
    color: #fff;
    font-size: 14rem;
    cursor: pointer;
    position: relative;
    padding: 0 20rem 0 10rem;
    .z-icon-close {
      position: absolute;
      right: 5rem;
      font-size: 18rem;
      z-index: 1;
      cursor: pointer;
    }
  }
  ul {
    position: absolute;
    font-size: 14rem;
    background-color: #063e79;
    border-color: #063e79;
    max-height: 200rem;
    overflow: auto;
    display: none;
    li {
      color: #fff;
      margin-bottom: 2rem;
      padding: 0 15rem;
      height: 32rem;
      line-height: 32rem;
      cursor: pointer;
      width: 150rem;
      box-sizing: border-box;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
      word-break: break-all;
    }
    li.active {
      color: #00def9;
      background-color: rgb(4 10 16 / 46%);
    }
  }
}
.location-container .alg-sel:hover {
  ul {
    display: block;
  }
}
@keyframes refreshing {
  from {
    transform: rotateZ(0);
  }
  to {
    transform: rotateZ(360);
  }
}
.video-control-container .z-icon-banbenqiehuan {
  position: absolute;
  top: 14px;
  right: 20px;
  font-size: 20px;
  color: #ccc;
  cursor: pointer;
  z-index: 1;
  display: block;
}
.video-control-container .z-icon-banbenqiehuan.active {
  color: #f99e28;
}

.video-control-container .z-icon-stop {
  position: absolute;
  top: 14px;
  right: 20px;
  font-size: 20px;
  color: #ccc;
  cursor: pointer;
  z-index: 1;
  display: block;
}
.onevideo .video-control-container .z-icon-stop {
  right: 20px;
}
.fourvideo .video-control-container,
.ninevideo .video-control-container {
  margin-bottom: 2px;
}
.sixteenvideo .video-control-container {
  margin-bottom: 0px;
}
.fourvideo .video-control-container:nth-child(3),
.fourvideo .video-control-container:nth-child(4),
.ninevideo .video-control-container:nth-child(7),
.ninevideo .video-control-container:nth-child(8),
.ninevideo .video-control-container:nth-child(9),
.ninevideo .video-control-container:nth-child(13),
.ninevideo .video-control-container:nth-child(14),
.ninevideo .video-control-container:nth-child(15),
.ninevideo .video-control-container:nth-child(16) {
  margin-bottom: 0;
}
/* .videos .video-control-container:nth-child(1),
.videos .video-control-container:nth-child(2) {
  margin-bottom: 10px;
} */
.video-js {
  max-width: 100%;
  max-height: 100%;
  /* object-fit: cover; */
}

canvas {
  position: absolute;
  top: 0;
  left: 0;
}
</style>
<style lang="less">
.home-alg-sel.el-select-dropdown.el-popper {
  background-color: #063e79;
  border-color: #063e79;
  .popper__arrow,
  .popper__arrow::after {
    border-bottom-color: #063e79;
    border-top-color: #063e79;
  }
  .el-select-dropdown__item {
    color: #fff;
    margin-bottom: 2rem;
    padding: 0 15rem;
    height: 32rem;
    line-height: 32rem;
  }
  .el-select-dropdown__item.hover,
  .el-select-dropdown__item:hover,
  .el-select-dropdown__item.selected {
    color: #00def9;
    background-color: rgb(4 10 16 / 46%);
  }
}
</style>