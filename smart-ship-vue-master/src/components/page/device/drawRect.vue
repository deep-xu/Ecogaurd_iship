<template>
  <div style="position: relative; padding-top: 20px" v-loading="spinShow">
    <!-- <Spin v-if="spinShow" fix>
      <Icon type="ios-loading" size="18" class="demo-spin-icon-load"></Icon>
      <div class="jiazai-text">正在获取图片，请稍后......</div>
    </Spin> -->
    <el-button
      v-show="!areaInfo.imageUrl"
      class="huoqubutton"
      @click="getCameraImage"
      icon="md-eye"
      >{{ message }}</el-button
    >
    <div class="tishi">检测区域应该在画面内并且不包含边界</div>
    <div class="main-container">
      <div class="painting-container" ref="painting-container">
        <div id="gmap"></div>
      </div>
    </div>
    <div v-show="drawparams.action != 'see'" style="margin-top: 20px">
      <el-button
        v-if="drawparams.type == 'tarpaulin'"
        type="primary"
        @click="handleSetMode('RECT')"
        >标记检测区域</el-button
      >

      <el-button style="margin: 0 10px" type="error" @click="handleClear"
        >清除</el-button
      >
      <el-tooltip content="放大图片" placement="top">
        <el-button
          style="margin: 0 5px"
          id="pan"
          class="command-btn"
          @click="handlePicLarger"
          >+</el-button
        >
      </el-tooltip>
      <el-tooltip content="缩小图片" placement="top">
        <el-button
          style="margin: 0 5px"
          id="pan"
          class="command-btn"
          @click="handlePicSmall"
          >-</el-button
        >
      </el-tooltip>
    </div>
    <div
      style="
        position: absolute;
        bottom: 0;
        display: flex;
        right: 186px;
        align-items: center;
      "
      v-if="rectInfo.bottomShow == true"
    >
      <div style="margin-right: 20px; user-select: none">
        <span class="must">*</span>方向
      </div>
      <el-select
        v-model="rectInfo.direction"
        style="width: 150px"
        @on-change="handleDirectionChg"
      >
        <el-option value="left2right">从左到右</el-option>
        <el-option value="right2left">从右到左</el-option>
        <el-option value="up2down">从上到下</el-option>
        <el-option value="down2up">从下到上</el-option>
      </el-select>
    </div>
    <div class="footer" v-show="drawparams.action != 'see'">
      <el-button style="margin: 0 5px" type="primary" @click="handleFinish"
        >完成</el-button
      >
    </div>

    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="rectInfo.show"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="375px"
    >
      <div style="display: flex; align-items: center">
        <div style="margin-right: 20px"><span class="must">*</span>方向</div>
        <Select
          v-model="rectInfo.direction"
          style="width: 150px"
          @on-change="handleDirectionChg"
        >
          <Option value="left2right">从左到右</Option>
          <Option value="right2left">从右到左</Option>
          <Option value="up2down">从上到下</Option>
          <Option value="down2up">从下到上</Option>
        </Select>
      </div>
      <div style="margin-top: 28px; text-align: center">
        <el-button
          style="margin: 0 5px"
          type="primary"
          @click="rectInfo.show = false"
          >完成</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import AILabel from "ailabel";
import { sourceApis } from "@/api/device.js";
let gMap = null; // 全局画布对象坐标与图片坐标一直 向下为y正方向，向右为x正方向
let gFeatureLayer = null; // 矩形图层
let gTextLayer = null;
let imageLayer = null;
let polygonStyle = { strokeStyle: "#02fc36", lineWidth: 1 }; // 多边形的颜色
let roiStyle = { strokeStyle: "#f6f61a", lineWidth: 1 }; // roi的颜色
let timoutflag = false; // 只请求一次图片标记
export default {
  props: ["areaInfo", "deviceInfo"], //
  data() {
    return {
      spinShow: false,
      message: "获取原始图片",
      transform: 1,
      imgInfo: {
        imageUrl: "",
        width: null, // 图片宽
        height: null, // 图片高
        mapWidth: null, // 画布宽
        mapHeight: null, // 画布高
      },

      // 画框相关参数
      drawparams: {
        type: "",
        roi: [],
        rects: [],
        areas: [], // type对应的标记区域
        allAreas: null, // 所有标记区域
        rtsp: "", // 获取图片
        action: "see", // draw/see  画框/仅查看
      },

      rectFeatures: {},
      textFeatures: {},
      activeFeatureId: null,

      // 画线和方向数据，对应算法：车辆计数，人员计数，大货车计数
      rectInfo: {
        show: false,
        featureId: "",
        direction: "left2right",
        bottomShow: false,
      },
    };
  },
  watch: {
    areaInfo: {
      deep: true,
      handler(newdata) {
        this.handleReset(); // 初始化form数据
      },
    },
    "areaInfo.rtsp"() {
      this.getCameraImage();
    },

    // "areaInfo.type"() {
    //   this.getCameraImage();
    // },
    // "areaInfo.action"() {
    //   this.getCameraImage();
    // },
  },
  mounted() {
    this.handleReset(); // 初始化form数据
    if (!this.$props.areaInfo.imageUrl) {
      this.getCameraImage();
    } else {
      this.imgInfo.imageUrl = this.$props.areaInfo.imageUrl;
      this.imageChange();
    }
  },
  methods: {
    handleReset() {
      // 恢复数据状态
      this.drawparams.type = this.$props.areaInfo.type;
      this.drawparams.rtsp = this.$props.areaInfo.rtsp;
      if (this.$props.areaInfo.rects) {
        this.drawparams.rects = JSON.parse(
          JSON.stringify(this.$props.areaInfo.rects)
        );
      }
      this.drawparams.action = this.$props.areaInfo.action;

      if (gMap) {
        // 恢复画布
        this.rectFeatures = {};
        gMap.setActiveFeature(null);
        gMap.removeAllLayers();
      }

      if (!this.drawparams.rtsp) {
        return;
      }

      if (this.imgInfo && this.imgInfo.imageUrl) {
        this.imageChange();
      }
    },
    getCameraImage() {
      this.spinShow = true;
      if (timoutflag == false) {
        timoutflag = true;
        sourceApis
          .getImage({
            device_id: this.$props.deviceInfo.device_id,
            stream: this.drawparams.rtsp,
            draw_size: 1280,
          })
          .then((res) => {
            timoutflag = false;
            if (res.data.error_code == 0) {
              this.spinShow = false;
              this.imgInfo.imageUrl = "data:image/jpg;base64," + res.data.data;
              this.imageChange();
            } else {
              this.spinShow = false;
              this.imgInfo.imageUrl = null;
            }
          })
          .catch((error) => {
            this.spinShow = false;
            this.imgInfo.imageUrl = null;
          });
      }
    },
    imageChange() {
      this.spinShow = true;
      var img = document.createElement("img");
      img.src = this.imgInfo.imageUrl;
      img.onload = () => {
        this.spinShow = false;
        this.initGmap(img);
        this.initGmapEvent(img);
      };
      setTimeout(() => {
        this.$forceUpdate();
      }, 100);
    },
    // 获取图片宽高，设置画布宽高
    getImageInfo(img) {
      const container = this.$refs["painting-container"]; // width: 768 height: 768*imgheight/imgwidth
      const map = document.querySelector("#gmap");

      // 画布宽度设置为容器宽度
      map.style.width = container.offsetWidth + "px";
      map.style.height =
        parseInt(
          (container.offsetWidth / img.naturalWidth) * img.naturalHeight
        ) + "px";
      // 容器设置为和画布一样大小
      container.style.height = map.style.height;
      return {
        width: img.width,
        height: img.height,
        mapWidth: map.offsetWidth,
        mapHeight: map.offsetHeight,
      };
    },
    initGmap(img) {
      this.imgInfo = Object.assign(this.imgInfo, this.getImageInfo(img));
      const mapParams = {
        zoom: this.imgInfo.width,
        mode: "BAN",
        center: { x: this.imgInfo.width / 2, y: this.imgInfo.height / 2 },
        zoomWhenDrawing: false,
        featureCaptureWhenMove: false,
      };
      gMap = new AILabel.Map("gmap", mapParams);
      gMap.disableDrawingTip();
      gMap.disableDrawingCrosshair();

      // 添加矩形图层
      gFeatureLayer = new AILabel.Layer.Feature(
        "featureLayer",
        { name: "矢量图层" },
        { zIndex: 20 }
      );
      gMap.addLayer(gFeatureLayer);

      // 文本图层
      gTextLayer = new AILabel.Layer.Text(
        "first-layer-text", // id
        { name: "文本图层" }, // props
        { zIndex: 21, opacity: 1 } // style
      );
      gMap.addLayer(gTextLayer);

      // 绘制过程中的样式
      // if (this.areaInfo.type == "roi") {
      //   gMap.setDrawingStyle(roiStyle);
      // } else {
      gMap.setDrawingStyle(polygonStyle);
      // }

      // 添加图片
      imageLayer = new AILabel.Layer.Image(
        this.imgInfo.captureId,
        {
          src: this.imgInfo.imageUrl,
          width: this.imgInfo.width,
          height: this.imgInfo.height,
          position: { x: 0, y: 0 }, // 左上角坐标
        },
        { name: this.imgInfo.captureId },
        { zIndex: 1 }
      );
      gMap.addLayer(imageLayer);

      // 画检测区域
      this.initRect();
    },
    initGmapEvent() {
      let _this = this;
      // 绘制完成事件监听
      gMap.events.on("drawDone", function (type, data) {
        if (type === "RECT") {
          _this.updateRect(data);
        }
      });

      // 设置选中
      gMap.events.on("featureSelected", (feature) => {
        if (this.areaInfo.action == "see") return;
        this.rectInfo.bottomShow = true;
        this.rectInfo.featureId = feature.id;
        this.rectInfo.direction =
          this.rectFeatures[this.rectInfo.featureId].direction;
        if (feature.type == "RECT") {
          gMap.setActiveFeature(feature);
          this.activeFeatureId = feature.id;
        } else {
          gMap.setActiveFeature(null);
          this.activeFeatureId = null;
        }
      });
      // 设置单击取消选中
      gMap.events.on("featureUnselected", (feature) => {
        gMap.setActiveFeature(null);
        this.rectFeatures[this.rectInfo.featureId].direction =
          this.rectInfo.direction;
        this.rectInfo.bottomShow = false;
        this.rectInfo.featureId = "";
        this.rectInfo.direction = "";
      });

      // 选中feature后，点delete可删除
      window.addEventListener("keydown", (e) => {
        if (
          (e.key == "Delete" || e.key == "Backspace") &&
          this.activeFeatureId
        ) {
          gFeatureLayer.removeFeatureById(this.activeFeatureId);
          this.$delete(this.rectFeatures, this.activeFeatureId);

          this.activeFeatureId = null;
          this.rectInfo.bottomShow = false;
          this.rectInfo.featureId = "";
          this.rectInfo.direction = "";
          this.clearAllText();
          this.setAllText();
        }
      });

      // 编辑后更新图形
      gMap.events.on("featureUpdated", (feature, data) => {
        if (feature.type == "RECT") {
          // 矩形
          _this.updateRect(data, feature);
        }
      });
    },
    initRect() {
      if (this.areaInfo.rects) {
        let rects = JSON.parse(JSON.stringify(this.areaInfo.rects));
        for (let i = 0; i < rects.length; i++) {
          let data = {
            x: rects[i][0][0],
            y: rects[i][0][1],
            width: rects[i][1][0] - rects[i][0][0],
            height: rects[i][1][1] - rects[i][0][1],
          };
          let id = "rect" + i;
          const rectFeatures = new AILabel.Feature.Rect(
            id,
            data,
            { name: "矩形" },
            polygonStyle
          );
          gFeatureLayer.addFeature(rectFeatures);
          this.$set(this.rectFeaturess, id, {
            id: id,
            points: rects[i].rect,
            feature: rectFeatures,
            direction: rects[i].direction,
          });
        }
      }
    },
    updateRect(data, feature) {
      // data:{x,y,width,height}
      if (data.x < 0) data.x = 0;
      if (data.y < 0) data.y = 0;
      if (data.x + data.width > this.imgInfo.width)
        data.width = this.imgInfo.width - data.x;
      if (data.y + data.height > this.imgInfo.height)
        data.height = this.imgInfo.height - data.y;

      if (feature) {
        feature.updateShape(data);
        this.rectFeatures[feature.id].points = [
          [Math.abs(Math.floor(data.x)), Math.floor(data.y)],
          [
            Math.abs(Math.floor(data.x)) + Math.floor(data.width),
            Math.floor(data.y) + Math.floor(data.height),
          ],
        ];
        this.rectFeatures.feature = feature;
        this.rectInfo.show = true;
        this.rectInfo.direction = this.rectFeatures[feature.id].direction;
        this.rectInfo.featureId = feature.id;
      } else {
        let id = "RECT" + new Date().getTime();
        const rectFeature = new AILabel.Feature.Rect(
          id,
          data,
          { name: "矩形" },
          polygonStyle
        );
        gFeatureLayer.addFeature(rectFeature);
        this.$set(this.rectFeatures, id, {
          id: id,
          points: [
            [Math.abs(Math.floor(data.x)), Math.floor(data.y)],
            [
              Math.abs(Math.floor(data.x)) + Math.floor(data.width),
              Math.floor(data.y) + Math.floor(data.height),
            ],
          ],
          feature: rectFeature,
          direction: "left2right",
        });
        this.rectInfo.show = true;
        this.rectInfo.direction = "left2right";
        this.rectInfo.featureId = rectFeature.id;
      }

      this.clearAllText();
      this.setAllText();
    },
    handleSetMode(mode) {
      gMap.setMode(mode);
    },
    handleClear() {
      // 保留roi, 清除多边形
      for (let id in this.rectFeatures) {
        gFeatureLayer.removeFeatureById(id);
        this.$delete(this.rectFeatures, id);
      }
      this.clearAllText();
    },
    clearAllText() {
      for (let id in this.textFeatures) {
        gTextLayer.removeTextById(id);
        this.$delete(this.textFeatures, id);
      }
    },
    setAllText() {
      console.log(this.rectFeatures);
      let rects = Object.keys(this.rectFeatures).map((id) => {
        return {
          rect: this.rectFeatures[id].points,
          direction: this.rectFeatures[id].direction,
        };
      });
      for (let i = 0; i < rects.length; i++) {
        let data = rects[i].rect.map((item) => {
          return {
            x: item[0],
            y: item[1],
          };
        });
        let position = {
          x: data[0].x,
          y: data[0].y,
        };
        let directions = {
          left2right: "从左到右",
          right2left: "从右到左",
          up2down: "从上到下",
          down2up: "从下到上",
        };
        let id = "text" + i;
        const gFirstText = new AILabel.Text(
          id, // id
          {
            text: directions[rects[i].direction],
            position: position,
            offset: { x: 0, y: 0 },
          }, // shape
          { name: "文本对象" }, // props
          {
            fillStyle: "#fff",
            strokeStyle: "#fff",
            background: true,
            globalAlpha: 1,
            fontColor: "#f00",
          } // style
        );
        gTextLayer.addText(gFirstText);
        this.$set(this.textFeatures, id, {
          id: id,
          points: rects[i].rect,
          feature: gFirstText,
        });
      }
    },

    handlePicLarger() {
      if (this.transform < 3) {
        this.transform = this.transform + 0.1;
      }
      let container = document.querySelector(".painting-container");
      container.style.transform = `scale(${this.transform})`;
    },
    handlePicSmall() {
      if (this.transform > 1) {
        this.transform = this.transform - 0.1;
      }
      let container = document.querySelector(".painting-container");
      container.style.transform = `scale(${this.transform})`;
    },
    handleFinish() {
      if (Object.keys(this.rectFeatures).length == 0) {
        let message = "";
        switch (this.areaInfo.type) {
          case "tarpaulin":
            message = "未标记检测区域";
            break;
          default:
            message = "未标记区域";
            break;
        }
        this.$message({
          type: "error",
          message: message,
        });
        return;
      }

      // this.$emit("close", {
      //   type: this.areaInfo.type,
      //   roi: null,
      //   areas: null,
      //   rects: Object.keys(this.rectFeatures).map(
      //     (id) => {
      //       return {
      //         rect: this.rectFeatures[id].points,
      //         direction: this.rectFeatures[id].direction
      //       }
      //     }
      //   )
      // });
    },
    handleDirectionChg() {
      if (this.rectInfo.direction) {
        this.rectFeatures[this.rectInfo.featureId].direction =
          this.rectInfo.direction;
        this.clearAllText();
        this.setAllText();
      }
    },
  },
  beforeDestroy() {
    window.removeEventListener("keydown", (e) => {});
  },
};
</script>

<style scoped>
.main-container {
  overflow: auto;
  margin: 15px auto;
}

.main-container::-webkit-scrollbar {
  background: rgb(239, 239, 239);
  height: 5px;
  border-radius: 2px;
}

.painting-container {
  width: 768px;
  height: 432px;
  /** 高度应根据图片宽高来设置   */
  overflow: hidden;
  position: relative;
  transform-origin: 0 0;
  transform: scale(1);
}

#gmap {
  position: absolute;
}

.footer {
  position: absolute;
  bottom: 0;
  right: 0;
}

.huoqubutton {
  position: absolute;
  top: -8px;
}
.tishi {
  position: absolute;
  top: -8px;
}
* {
  user-select: none;
}
</style>