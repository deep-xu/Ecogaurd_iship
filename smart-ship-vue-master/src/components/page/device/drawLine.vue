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
    <div class="main-container">
      <div class="painting-container" ref="painting-container">
        <div id="gmap"></div>
      </div>
    </div>
    <div v-show="drawparams.action != 'see'" style="margin-top: 20px">
      <el-button type="primary" @click="handleSetMode('LINE')">{{
        algInfo && algInfo.reserved_args && algInfo.reserved_args.btn_name
      }}</el-button>

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
    <!-- <div
      style="
        position: absolute;
        bottom: 0;
        display: flex;
        right: 186px;
        align-items: center;
      "
      v-if="lineInfo.bottomShow == true"
    >
      <div style="margin-right: 20px; user-select: none">
        <span class="must">*</span>方向
      </div>
      <el-select
        v-model="lineInfo.direction"
        style="width: 150px"
        @change="handleDirectionChg"
      >
        <el-option value="r+" label="从左到右"></el-option>
        <el-option value="l+" label="从右到左"></el-option>
        <el-option value="d+" label="从上到下"></el-option>
        <el-option value="u+" label="从下到上"></el-option>
        <el-option value="l+r-" label="左增右减"></el-option>
        <el-option value="l-r+" label="左减右增"></el-option>
        <el-option value="u+d-" label="上增下减"></el-option>
        <el-option value="u-d+" label="上减下增"></el-option>
      </el-select>
    </div> -->
    <div class="footer" v-show="drawparams.action != 'see'">
      <el-button style="margin: 0 5px" type="primary" @click="handleFinish"
        >完成</el-button
      >
    </div>

    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="lineInfo.show"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="375px"
    >
      <div style="display: flex; align-items: center">
        <div style="width: 70px"><span class="must">*</span>方向选择</div>
        <el-select
          v-model="lineInfo.direction"
          style="width: 150px"
          @change="handleDirectionChg"
        >
          <el-option value="r+" label="从左到右"></el-option>
          <el-option value="l+" label="从右到左"></el-option>
          <el-option value="d+" label="从上到下"></el-option>
          <el-option value="u+" label="从下到上"></el-option>
          <el-option value="l+r-" label="左增右减"></el-option>
          <el-option value="l-r+" label="左减右增"></el-option>
          <el-option value="u+d-" label="上增下减"></el-option>
          <el-option value="u-d+" label="上减下增"></el-option>
        </el-select>
      </div>
      <div style="display: flex; align-items: center; margin: 10px 0">
        <div style="width: 70px"><span class="must">*</span>线条名称</div>
        <el-input
          v-model="lineInfo.name"
          style="width: 150px"
          @change="handleNameChg(lineInfo)"
          :maxlength="32"
        />
      </div>
      <div v-if="lineInfo.direction && lineInfo.direction.length == 2">
        <div style="width: 70px"><span class="must">*</span>行为描述</div>
        <div style="display: flex; align-items: center">
          <label style="width: 70px; text-align: center">计数</label>
          <el-input
            v-model="lineInfo.action.count"
            style="width: 150px; margin-bottom: 5px"
            @change="handleActionChg(lineInfo)"
            placeholder="统计"
          />
        </div>
      </div>
      <div v-else>
        <div style="width: 70px"><span class="must">*</span>行为描述</div>
        <div style="display: flex; flex-direction: column">
          <div style="display: flex; align-items: center">
            <label style="width: 70px; text-align: center">增加</label>
            <el-input
              v-model="lineInfo.action.increase"
              style="width: 150px; margin-bottom: 5px"
              @change="handleActionChg(lineInfo)"
              placeholder="进入"
            />
          </div>
          <div style="display: flex; align-items: center">
            <label style="width: 70px; text-align: center">减少</label>
            <el-input
              v-model="lineInfo.action.decrease"
              style="width: 150px; margin-bottom: 5px"
              @change="handleActionChg(lineInfo)"
              placeholder="离开"
            />
          </div>
          <div style="display: flex; align-items: center">
            <label style="width: 70px; text-align: center">净增</label>
            <el-input
              v-model="lineInfo.action.delta"
              style="width: 150px; margin-bottom: 5px"
              @change="handleActionChg(lineInfo)"
              placeholder="统计"
            />
          </div>
        </div>
      </div>
      <div v-if="lineInfo.errorInfo" style="text-align: center; color: red">
        {{ lineInfo.errorInfo }}
      </div>
      <div style="margin-top: 28px; text-align: center">
        <el-button
          style="margin: 0 5px"
          type="primary"
          @click="handleSaveLineInfo"
          >完成</el-button
        >
        <el-button
          style="margin: 0 5px"
          type="default"
          @click="handleDelLineInfo"
          >{{ lineInfo.optype == 'add' ? '取消' : '删除'}}</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import AILabel from "ailabel";
import {getId} from "@/api/common.js"
import { sourceApis } from "@/api/device.js";
let gMap = null; // 全局画布对象坐标与图片坐标一直 向下为y正方向，向右为x正方向
let gFeatureLayer = null; // 矩形图层
let gTextLayer = null;
let imageLayer = null;
let polygonStyle = { strokeStyle: "#02fc36", lineWidth: 1 }; // 多边形的颜色
let roiStyle = { strokeStyle: "#f6f61a", lineWidth: 1 }; // roi的颜色
let timoutflag = false; // 只请求一次图片标记
export default {
  props: ["areaInfo", "deviceInfo", "algInfo", "size", "renderParams"], //
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
        areas: [], // type对应的标记区域
        allAreas: null, // 所有标记区域
        rtsp: "", // 获取图片
        action: "see", // draw/see  画框/仅查看
      },

      roiFeature: null, // {id,points,feature}
      polygonFeatures: {},
      textFeatures: {},
      activeFeatureId: null,

      // 画线和方向数据，对应算法：车辆计数，人员计数，大货车计数
      lineInfo: {
        show: false,
        optype: "add",
        featureId: "",
        direction: "r+",
        bottomShow: false,
        name: "",
        errorInfo: "",
        action: {
          count: "统计",
          increase: "进入",
          decrease: "离开",
          delta: "统计",
        },
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
      // this.drawparams.roi = JSON.parse(
      //   JSON.stringify(this.$props.areaInfo.roi)
      // );
      // this.drawparams.areas = JSON.parse(
      //   JSON.stringify(this.$props.areaInfo.areas)
      // );
      this.drawparams.lines = JSON.parse(
        JSON.stringify(this.$props.areaInfo.lines)
      );
      this.drawparams.allAreas = JSON.parse(
        JSON.stringify(this.$props.areaInfo.allAreas)
      );
      this.drawparams.action = this.$props.areaInfo.action;

      if (gMap) {
        // 恢复画布
        this.roiFeature = null;
        this.polygonFeatures = {};
        gMap.setActiveFeature(null);
        gMap.removeAllLayers();
      }

      if (!this.drawparams.rtsp) {
        return;
      }

      //if (this.imgInfo && this.imgInfo.imageUrl) {
      //  this.imageChange();
      //}
    },
    getCameraImage() {
      this.handleReset();
      this.spinShow = true;
      if (timoutflag == false) {
        timoutflag = true;
        sourceApis
          .getImage({
            device_id: this.$props.deviceInfo.device_id,
            stream: this.drawparams.rtsp,
            draw_size: JSON.stringify(this.$props.size),
          })
          .then((res) => {
            timoutflag = false;
            if (res.error_code == 0) {
              this.spinShow = false;
              this.imgInfo.imageUrl = "data:image/jpg;base64," + res.data;
              this.imageChange();
            } else {
              this.spinShow = false;
              this.imgInfo.imageUrl = null;
            }
          })
          .catch((error) => {
            timoutflag = false;
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
      if (this.areaInfo.type == "roi") {
        gMap.setDrawingStyle(roiStyle);
      } else {
        gMap.setDrawingStyle(polygonStyle);
      }

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

      // 如果roi和area不为null 绘制到画布上
      // 先画多边形再画矩形，否则矩形范围内的多边形不能编辑
      this.initLines();
      this.initRect();
    },
    initLines() {
      if (this.areaInfo.lines) {
        let lines = JSON.parse(JSON.stringify(this.areaInfo.lines));
        for (let i = 0; i < lines.length; i++) {
          let data = lines[i].line.map((item) => {
            return {
              x: item[0],
              y: item[1],
            };
          });
          // let id = "line" + i;
          let id = lines[i].id;
          const lineFeature = new AILabel.Feature.Line(
            id, // id
            { start: data[0], end: data[1], width: 2 }, // shape
            { name: "第一个直线" }, // props
            polygonStyle
          );
          gFeatureLayer.addFeature(lineFeature);
          let action = lines[i].action || {
            count: "统计",
            increase: "进入",
            decrease: "离开",
            delta: "统计",
          };
          this.$set(this.polygonFeatures, id, {
            id: id,
            points: lines[i].line,
            feature: lineFeature,
            direction: lines[i].direction,
            name: lines[i].name,
            action: Object.assign(
              {
                count: "统计",
                increase: "进入",
                decrease: "离开",
                delta: "统计",
              },
              JSON.parse(JSON.stringify(action))
            ),
          });
        }
      }

      this.clearAllText();
      this.setAllText();
    },
    initRect() {
      if (this.areaInfo.roi) {
        let data = {
          x: this.areaInfo.roi[0][0],
          y: this.areaInfo.roi[0][1],
          width: this.areaInfo.roi[1][0] - this.areaInfo.roi[0][0],
          height: this.areaInfo.roi[1][1] - this.areaInfo.roi[0][1],
        };
        if (this.areaInfo.type == "roi" && this.areaInfo.action != "see") {
          this.updateRect(data);
        } else if (
          this.areaInfo.type != "roi" &&
          this.areaInfo.action == "see"
        ) {
          // 不是roi 也不是编辑区域，不需要显示roi区域
        } else {
          // 矩形不能编辑，用其他颜色
          let id = "RECT" + new Date().getTime();
          const rectFeature = new AILabel.Feature.Rect(
            id,
            data,
            { name: "矩形" },
            roiStyle
          );
          gFeatureLayer.addFeature(rectFeature);
          if (this.roiFeature) {
            gFeatureLayer.removeFeatureById(this.roiFeature.id);
          }
          this.roiFeature = Object.assign(
            {},
            {
              id: id,
              points: JSON.parse(JSON.stringify(this.areaInfo.roi)),
              feature: rectFeature,
            }
          );
        }
      }

      if (this.areaInfo.type == "roi" && this.areaInfo.action != "see") {
        // 编辑roi需要查看所有多边形标记区域
        let allAreas = this.areaInfo.allAreas;
        Object.keys(allAreas).forEach((alg, index) => {
          if (allAreas[alg].areas && allAreas[alg].areas.length > 0) {
            let areas = allAreas[alg].areas;
            for (let i = 0; i < areas.length; i++) {
              let data = areas[i].map((item) => {
                return {
                  x: item[0],
                  y: item[1],
                };
              });
              let id = "polygon" + index + i;
              const polygonFeature = new AILabel.Feature.Polygon(
                id, // id
                { points: data }, // shape
                { name: "第一个多边形" }, // props
                polygonStyle
              );
              gFeatureLayer.addFeature(polygonFeature);
              this.$set(this.polygonFeatures, id, {
                id: id,
                points: areas[i],
                feature: polygonFeature,
              });
            }
          }
        });
      }
    },
    initGmapEvent() {
      let _this = this;
      // 绘制完成事件监听
      gMap.events.on("drawDone", function (type, data) {
        if (type === "RECT") {
          _this.updateRect(data);
        }
        if (type === "POLYGON") {
          _this.updatePolygon(data);
        }
        if (type === "LINE") {
          // 判断features,删除其他直线
          // console.log(gFeatureLayer);
          // for (let lineId in _this.polygonFeatures) {
          //   _this.$delete(_this.polygonFeatures, lineId);
          //   gFeatureLayer.removeFeatureById(lineId);
          // }
          let points = [
            { x: data.start.x, y: data.start.y },
            { x: data.end.x, y: data.end.y },
          ];
          _this.updatePolygon(points);
          console.log(_this.polygonFeatures);
        }
      });

      // 设置选中
      gMap.events.on("featureSelected", (feature) => {
        if (this.areaInfo.action == "see") return;
        this.lineInfo.show = true;
        this.lineInfo.bottomShow = true;
        this.lineInfo.featureId = feature.id;
        this.lineInfo.direction =
          this.polygonFeatures[this.lineInfo.featureId].direction;
        this.lineInfo.name = this.polygonFeatures[this.lineInfo.featureId].name;
        this.lineInfo.action = JSON.parse(
          JSON.stringify(this.polygonFeatures[this.lineInfo.featureId].action)
        );
        this.lineInfo.optype = "edit";
        if (this.areaInfo.type == "roi") {
          if (feature.type == "RECT") {
            gMap.setActiveFeature(feature);
            this.activeFeatureId = feature.id;
          } else {
            gMap.setActiveFeature(null);
            this.activeFeatureId = null;
          }
        } else {
          if (feature.type != "RECT") {
            gMap.setActiveFeature(feature);
            this.activeFeatureId = feature.id;
          } else {
            gMap.setActiveFeature(null);
            this.activeFeatureId = null;
          }
        }
      });
      // 设置单击取消选中
      gMap.events.on("featureUnselected", (feature) => {
        gMap.setActiveFeature(null);
        this.polygonFeatures[this.lineInfo.featureId].direction =
          this.lineInfo.direction;
        this.polygonFeatures[this.lineInfo.featureId].name = this.lineInfo.name;
        this.polygonFeatures[this.lineInfo.featureId].action = JSON.parse(
          JSON.stringify(this.lineInfo.action)
        );
        this.lineInfo.bottomShow = false;
        this.lineInfo.featureId = "";
        this.lineInfo.direction = "";
        this.lineInfo.name = "";
        this.lineInfo.action = {
          count: "统计",
          increase: "进入",
          decrease: "离开",
          delta: "统计",
        };
      });

      // 选中feature后，点delete可删除
      window.addEventListener("keydown", (e) => {
        if (
          (e.key == "Delete" || e.key == "Backspace") &&
          this.activeFeatureId &&
          this.lineInfo.show == false
        ) {
          gFeatureLayer.removeFeatureById(this.activeFeatureId);
          if (this.areaInfo.type == "roi") {
            this.roiFeature = null;
          } else {
            this.$delete(this.polygonFeatures, this.activeFeatureId);
          }
          this.activeFeatureId = null;
          this.lineInfo.bottomShow = false;
          this.lineInfo.featureId = "";
          this.lineInfo.direction = "";
          this.lineInfo.name = "";
          this.lineInfo.action = {
            count: "统计",
            increase: "进入",
            decrease: "离开",
            delta: "统计",
          };
          this.clearAllText();
          this.setAllText();
        }
      });

      // 编辑后更新图形
      gMap.events.on("featureUpdated", (feature, data) => {
        if (feature.type == "RECT") {
          // 矩形
          _this.updateRect(data, feature);
        } else if (feature.type == "POLYGON") {
          // 多边形
          _this.updatePolygon(data, feature);
        } else {
          // 直线
          let points = [
            { x: data.start.x, y: data.start.y },
            { x: data.end.x, y: data.end.y },
          ];
          _this.updatePolygon(points, feature);
        }
      });
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
        this.roiFeature.points = [
          [Math.abs(Math.floor(data.x)), Math.floor(data.y)],
          [
            Math.abs(Math.floor(data.x)) + Math.floor(data.width),
            Math.floor(data.y) + Math.floor(data.height),
          ],
        ];
        this.roiFeature.feature = feature;
      } else {
        let id = "RECT" + new Date().getTime();
        const rectFeature = new AILabel.Feature.Rect(
          id,
          data,
          { name: "矩形" },
          roiStyle
        );
        gFeatureLayer.addFeature(rectFeature);
        if (this.roiFeature) {
          gFeatureLayer.removeFeatureById(this.roiFeature.id);
        }
        this.roiFeature = Object.assign(
          {},
          {
            id: id,
            points: [
              [Math.abs(Math.floor(data.x)), Math.floor(data.y)],
              [
                Math.abs(Math.floor(data.x)) + Math.floor(data.width),
                Math.floor(data.y) + Math.floor(data.height),
              ],
            ],
            feature: rectFeature,
          }
        );
      }

      // 判断是否所有标定区域都在矩形范围内
      let points = [];
      for (let alg in this.areaInfo.allAreas) {
        let polygons = this.areaInfo.allAreas[alg].areas; // n个区域，每个区域m个点，每个点[x,y]
        if (polygons && polygons.length > 0) {
          polygons.forEach((item) => {
            item.forEach((point) => {
              points.push({
                x: point[0],
                y: point[1],
              });
            });
          });
        }
      }
      this.areaInRoi(points, this.roiFeature.points);
    },
    updatePolygon(data, feature) {
      // 新增多边形：data:[{x,y},{x,y}] 编辑多边形：data:{points}
      // 新增直线：data:[{x,y},{x,y}]
      let points = data.points || data;
      points.forEach((point) => {
        if (point.x < 0) point.x = 0;
        if (point.y < 0) point.y = 0;
        if (point.x > this.imgInfo.width) point.x = this.imgInfo.width;
        if (point.y > this.imgInfo.height) point.y = this.imgInfo.height;
      });
      if (
        this.roiFeature &&
        this.roiFeature.points &&
        this.roiFeature.points.length > 0
      ) {
        this.areaInRoi(points, this.roiFeature.points);
      }

      if (feature) {
        if (points.length > 2) {
          // 更新多边形
          feature.updateShape({ points: points });
        } else {
          // 更新直线
          feature.updateShape({
            start: { x: points[0].x, y: points[0].y },
            end: { x: points[1].x, y: points[1].y },
            width: 2,
          });
          this.lineInfo.show = true;
          this.lineInfo.direction = this.polygonFeatures[feature.id].direction;
          this.lineInfo.name = this.polygonFeatures[feature.id].name;
          this.lineInfo.action = JSON.parse(
            JSON.stringify(this.polygonFeatures[feature.id].action)
          );
          this.lineInfo.featureId = feature.id;
        }

        this.polygonFeatures[feature.id].points = points.map((item) => [
          Math.abs(Math.floor(item.x)),
          Math.floor(item.y),
        ]);
      } else {
        if (points.length > 2) {
          // 画多边形
          let id = "polygon" + new Date().getTime();
          const polygonFeature = new AILabel.Feature.Polygon(
            id, // id
            { points: points }, // shape
            { name: "第一个多边形" }, // props
            polygonStyle // style
          );
          gFeatureLayer.addFeature(polygonFeature);
          this.$set(this.polygonFeatures, id, {
            id: "polygon" + id,
            points: points.map((item) => [
              Math.floor(item.x),
              Math.floor(item.y),
            ]),
            feature: polygonFeature,
          });
        } else {
          // let id = "line" + new Date().getTime();
          let id = getId();
          const lineFeature = new AILabel.Feature.Line(
            id, // id
            {
              start: { x: data[0].x, y: data[0].y },
              end: { x: data[1].x, y: data[1].y },
              width: 2,
            }, // shape
            { name: "第一个直线" }, // props
            polygonStyle
          );
          gFeatureLayer.addFeature(lineFeature);
          this.$set(this.polygonFeatures, id, {
            id: id,
            points: points.map((item) => [
              Math.floor(item.x),
              Math.floor(item.y),
            ]),
            feature: lineFeature,
            direction: "r+",
            name: "",
            action: {
              count: "统计",
              increase: "进入",
              decrease: "离开",
              delta: "统计",
            },
          });
          this.lineInfo.optype = "add";
          this.lineInfo.show = true;
          this.lineInfo.direction = "r+";
          this.lineInfo.name = "";
          this.lineInfo.action = {
            count: "统计",
            increase: "进入",
            decrease: "离开",
            delta: "统计",
          };
          this.lineInfo.featureId = lineFeature.id;
        }
      }
      // 最后画一次矩形roi区域，否则多边形不能编辑
      this.initRect();
      console.log("this.polygonFeatures", this.polygonFeatures);
      this.clearAllText();
      this.setAllText();
    },
    areaInRoi(points, roi) {
      // points数据结构 [{x,y},{x,y},{x,y}]  roi数据结构 [[x1,y1],[x2,y2]]
      // 多边形判断是否所有点在roi范围内，如果不在，则提示
      let iserror = false;
      if (roi && roi.length > 0) {
        points.forEach((item) => {
          if (
            item.x < roi[0][0] ||
            item.x > roi[1][0] ||
            item.y < roi[0][1] ||
            item.y > roi[1][1]
          ) {
            iserror = true;
          }
        });
        if (iserror) {
          if (this.areaInfo.type == "roi") {
            this.$message({
              type: "error",
              message: "roi检测区域应包含所有标定区域",
            });
          } else {
            this.$message({
              type: "error",
              message: "标记区域不可超出检测区域",
            });
          }
        }
      }
      return iserror;
    },
    handleSetMode(mode) {
      gMap.setMode(mode);
    },
    handleClear() {
      if (this.drawparams.type == "roi") {
        if (this.roiFeature && this.roiFeature.id) {
          gFeatureLayer.removeFeatureById(this.roiFeature.id);
        }
        this.roiFeature = null;
      } else {
        // 保留roi, 清除多边形
        for (let id in this.polygonFeatures) {
          gFeatureLayer.removeFeatureById(id);
          this.$delete(this.polygonFeatures, id);
        }
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
      let lines = Object.keys(this.polygonFeatures).map((id) => {
        return {
          line: this.polygonFeatures[id].points,
          direction: this.polygonFeatures[id].direction,
          name: this.polygonFeatures[id].name,
          action: JSON.parse(JSON.stringify(this.polygonFeatures[id].action)),
        };
      });
      for (let i = 0; i < lines.length; i++) {
        let data = lines[i].line.map((item) => {
          return {
            x: item[0],
            y: item[1],
          };
        });
        let position = {
          x: (data[0].x + data[1].x) / 2,
          y: (data[0].y + data[1].y) / 2,
        };
        let directions = {
          "r+": "从左到右",
          "l+": "从右到左",
          "d+": "从上到下",
          "u+": "从下到上",
          "l+r-": "左增右减",
          "l-r+": "左减右增",
          "u+d-": "上增下减",
          "u-d+": "上减下增",
        };
        let id = "text" + i;
        const gFirstText = new AILabel.Text(
          id, // id
          {
            text: directions[lines[i].direction],
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
          points: lines[i].line,
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
      if (this.areaInfo.type != "roi") {
        if (Object.keys(this.polygonFeatures).length == 0) {
          let message = "";
          if (this.$props.algInfo.reserved_args.must) {
            message =
              this.$props.algInfo.reserved_args.ch_name +
              "未" +
              this.$props.algInfo.reserved_args.btn_name;
          }
          this.$message({
            type: "error",
            message: message,
          });
          return;
        } else {
          if (this.$props.renderParams.bbox) {
            if (
              this.$props.renderParams.bbox.lines.max &&
              Object.keys(this.polygonFeatures).length >
                this.$props.renderParams.bbox.lines.max
            ) {
              this.$message({
                type: "error",
                message: `最多绘制${this.$props.renderParams.bbox.lines.max}条线，请重新绘制！`,
              });
              return;
            }
          }
        }
      }
      // 校验一遍多边形数据，数据错误不能关闭
      if (
        this.roiFeature &&
        this.roiFeature.points &&
        this.roiFeature.points.length > 0
      ) {
        // 判断是否所有标定区域都在矩形范围内
        let points = [];
        for (let alg in this.areaInfo.allAreas) {
          let polygons = this.areaInfo.allAreas[alg].areas; // n个区域，每个区域m个点，每个点[x,y]
          if (polygons && polygons.length > 0) {
            polygons.forEach((item) => {
              item.forEach((point) => {
                points.push({
                  x: point[0],
                  y: point[1],
                });
              });
            });
          }
        }
        let iserror = this.areaInRoi(points, this.roiFeature.points);
        if (iserror) return;
      }

      this.$emit("close", {
        type: this.areaInfo.type,
        roi: this.roiFeature && this.roiFeature.points,
        areas: null,
        lines: Object.keys(this.polygonFeatures).map((id) => {
          let action = {};
          if (this.polygonFeatures[id].direction.length == 2) {
            action.count = this.polygonFeatures[id].action.count;
          } else {
            action.increase = this.polygonFeatures[id].action.increase;
            action.decrease = this.polygonFeatures[id].action.decrease;
            action.delta = this.polygonFeatures[id].action.delta;
          }
          return {
            id: this.polygonFeatures[id].id,
            line: this.polygonFeatures[id].points,
            direction: this.polygonFeatures[id].direction,
            name: this.polygonFeatures[id].name,
            action: action,
          };
        }),
      });
    },
    handleDirectionChg() {
      if (this.lineInfo.direction) {
        this.polygonFeatures[this.lineInfo.featureId].direction =
          this.lineInfo.direction;

        this.clearAllText();
        this.setAllText();
      }
      this.lineInfo.errorInfo = "";
    },
    handleNameChg(lineInfo) {
      this.polygonFeatures[lineInfo.featureId].name = lineInfo.name;
      if (this.lineInfo.name) {
        this.lineInfo.errorInfo = "";
      } else {
        this.lineInfo.errorInfo = "线条名称不能为空";
      }
    },
    handleActionChg(lineInfo) {
      this.polygonFeatures[lineInfo.featureId].action = JSON.parse(
        JSON.stringify(lineInfo.action)
      );
      let actions = "";
      if (this.lineInfo.direction.length == 2) {
        actions = this.lineInfo.action.count;
      } else {
        let tmpobj = JSON.parse(JSON.stringify(this.lineInfo.action));
        delete tmpobj.count;
        actions = Object.values(tmpobj).join("");
      }
      if (!actions) {
        this.lineInfo.errorInfo = "行为描述不能为空";
      } else {
        this.lineInfo.errorInfo = "";
      }
    },
    handleSaveLineInfo() {
      this.lineInfo.errorInfo = "";
      if (!this.lineInfo.name) {
        this.lineInfo.errorInfo = "线条名称不能为空";
        return;
      }
      let actions = "";
      if (this.lineInfo.direction.length == 2) {
        actions = this.lineInfo.action.count;
      } else {
        let tmpobj = JSON.parse(JSON.stringify(this.lineInfo.action));
        delete tmpobj.count;
        actions = Object.values(tmpobj).join("");
      }
      if (!actions) {
        this.lineInfo.errorInfo = "行为描述不能为空";
        return;
      }
      this.lineInfo.show = false;
    },
    handleDelLineInfo() {
      gFeatureLayer.removeFeatureById(this.lineInfo.featureId);
      this.$delete(this.polygonFeatures, this.lineInfo.featureId);

      this.activeFeatureId = null;
      this.lineInfo.show = false;
      this.lineInfo.bottomShow = false;
      this.lineInfo.featureId = "";
      this.lineInfo.direction = "";
      this.lineInfo.name = "";
      this.lineInfo.errorInfo = "";
      this.lineInfo.action = {
        count: '统计',
        increase: '进入',
        decrease: '离开',
        delta: '统计',
      };
      this.clearAllText();
      this.setAllText();
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
.must {
  color: red;
}
* {
  user-select: none;
}
</style>
