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
    <div class="finishTip" style="position: absolute; bottom: 5px; left: 300px">
      {{ finishTip }}
    </div>
    <div v-show="drawparams.action != 'see'" style="margin-top: 20px">
      <el-button
        type="primary"
        @click="handleSetMode('POLYGON')"
        >{{algInfo && algInfo.reserved_args && algInfo.reserved_args.btn_name}}</el-button
      >
      <el-button style="margin: 0 10px" type="error" @click="handleClear"
        >清除</el-button
      >
      <el-tooltip content="放大图片" placement="top">
        <el-button
          style="margin: 0 5px"
          class="command-btn"
          @click="handlePicLarger"
          >+</el-button
        >
      </el-tooltip>
      <el-tooltip content="缩小图片" placement="top">
        <el-button
          style="margin: 0 5px"
          class="command-btn"
          @click="handlePicSmall"
          >-</el-button
        >
      </el-tooltip>
    </div>
    <div class="footer" v-show="drawparams.action != 'see'">
      <el-button style="margin: 0 5px" type="primary" @click="handleFinish"
        >完成</el-button
      >
    </div>

    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="polygonInfo.show"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="375px"
    >
      <div style="display: flex; align-items: center; margin: 10px 0">
        <div style="width: 70px">名称</div>
        <el-input
          v-model="polygonInfo.name"
          style="width: 150px"
          @change="handleNameChg(polygonInfo)"
          :maxlength="32"
        />
      </div>
      <div v-if="polygonInfo.errorInfo" style="text-align: center; color: red">
        {{ polygonInfo.errorInfo }}
      </div>
      <div style="margin-top: 28px; text-align: center">
        <el-button
          style="margin: 0 5px"
          type="primary"
          @click="handleSavePolygonInfo"
          >完成</el-button
        >
        <el-button
          style="margin: 0 5px"
          type="default"
          @click="handleDelPolygonInfo"
          >{{ polygonInfo.optype == 'add' ? '取消' : '删除'}}</el-button
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
      activeFeatureId: null,
      finishTip: "",
      // 多边形名称、id等
      polygonInfo: {
        show: false,
        errorInfo: "",
        featureId: "",
        name: "",
        optype: "add",
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
      this.drawparams.areas = JSON.parse(
        JSON.stringify(this.$props.areaInfo.areas)
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
      var img = document.createElement("img");
      img.src = this.imgInfo.imageUrl;
      img.onload = () => {
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
      this.initPolygons();
    },
    initPolygons() {
      if (this.areaInfo.areas) {
        // areas 按点的个数排序，先画线段再画多边形
        let areas = JSON.parse(JSON.stringify(this.areaInfo.areas)).sort(
          (a, b) => a.polygon.length > b.polygon.length
        );
        for (let i = 0; i < areas.length; i++) {
          let data = areas[i].polygon.map((item) => {
            return {
              x: item[0],
              y: item[1],
            };
          });
          if (data.length > 2) {
            let id = areas[i].id;
            const polygonFeature = new AILabel.Feature.Polygon(
              id, // id
              { points: data }, // shape
              { name: "第一个多边形" }, // props
              polygonStyle
            );
            gFeatureLayer.addFeature(polygonFeature);
            this.$set(this.polygonFeatures, id, {
              id: id,
              points: areas[i].polygon,
              feature: polygonFeature,
              name: areas[i].name
            });
          } else {
            let id = "line" + i;
            const lineFeature = new AILabel.Feature.Line(
              id, // id
              { start: data[0], end: data[1], width: 2 }, // shape
              { name: "第一个直线" }, // props
              polygonStyle
            );
            gFeatureLayer.addFeature(lineFeature);
            this.$set(this.polygonFeatures, id, {
              id: id,
              points: areas[i],
              feature: lineFeature,
            });
          }
        }
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
          console.log(gFeatureLayer);
          for (let lineId in _this.polygonFeatures) {
            _this.$delete(_this.polygonFeatures, lineId);
            gFeatureLayer.removeFeatureById(lineId);
          }
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
        this.polygonInfo.show = true;
        this.polygonInfo.featureId = feature.id;
        this.polygonInfo.name =
          this.polygonFeatures[this.polygonInfo.featureId].name;
        this.polygonInfo.optype = "edit";

        if (feature.type != "RECT") {
          gMap.setActiveFeature(feature);
          this.activeFeatureId = feature.id;
          this.polygonInfo.show = true;
        } else {
          gMap.setActiveFeature(null);
          this.activeFeatureId = null;
        }
      });
      // 设置单击取消选中
      gMap.events.on("featureUnselected", (feature) => {
        gMap.setActiveFeature(null);
        this.polygonFeatures[this.polygonInfo.featureId].name =
          this.polygonInfo.name;
        this.polygonInfo.featureId = "";
        this.polygonInfo.name = "";
      });

      // 选中feature后，点delete可删除
      window.addEventListener("keydown", (e) => {
        if (
          (e.key == "Delete" || e.key == "Backspace") &&
          this.activeFeatureId &&
          this.polygonInfo.show == false
        ) {
          gFeatureLayer.removeFeatureById(this.activeFeatureId);
          if (this.areaInfo.type == "roi") {
            this.roiFeature = null;
          } else {
            this.$delete(this.polygonFeatures, this.activeFeatureId);
          }
          this.activeFeatureId = null;
          this.polygonInfo.featureId = "";
          this.polygonInfo.name = "";
        }
      });

      // 编辑后更新图形
      gMap.events.on("featureUpdated", (feature, data) => {
        if (feature.type == "RECT") {
          _this.updateRect(data, feature);
        } else if (feature.type == "POLYGON") {
          _this.updatePolygon(data, feature);
        } else {
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
          this.polygonInfo.show = true;
          this.polygonInfo.name = this.polygonFeatures[feature.id].name;
          this.polygonInfo.featureId = feature.id;
        } else {
          // 更新直线
          feature.updateShape({
            start: { x: points[0].x, y: points[0].y },
            end: { x: points[1].x, y: points[1].y },
            width: 2,
          });
        }

        this.polygonFeatures[feature.id].points = points.map((item) => [
          Math.abs(Math.floor(item.x)),
          Math.floor(item.y),
        ]);
      } else {
        if (points.length > 2) {
          // 画多边形
          // let id = "polygon" + new Date().getTime();
          let id = getId();
          const polygonFeature = new AILabel.Feature.Polygon(
            id, // id
            { points: points }, // shape
            { name: "第一个多边形" }, // props
            polygonStyle // style
          );
          gFeatureLayer.addFeature(polygonFeature);
          this.$set(this.polygonFeatures, id, {
            id: id,
            points: points.map((item) => [
              Math.floor(item.x),
              Math.floor(item.y),
            ]),
            feature: polygonFeature,
            name: "",
          });
          this.polygonInfo.optype = "add";
          this.polygonInfo.show = true;
          this.polygonInfo.name = "";
          this.polygonInfo.featureId = polygonFeature.id;
        } else {
          let id = "line" + new Date().getTime();
          console.log(points);
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
          });
        }
      }
      // 最后画一次矩形roi区域，否则多边形不能编辑
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
      switch (mode) {
        case "POLYGON":
          this.finishTip = "双击完成";
          break;
        default:
          this.finishTip = "";
          break;
      }
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
          if(this.$props.algInfo.reserved_args.must){
            message = this.$props.algInfo.reserved_args.ch_name + "未" + this.$props.algInfo.reserved_args.btn_name
          }
          if (message) {
            this.$message({
              type: "error",
              message: message,
            });

            return;
          }
        }else{
          if (this.$props.renderParams.bbox) {
            // 判断最多几个多边形，每个几条边
            if (
              this.$props.renderParams.bbox.polygons.max &&
              Object.keys(this.polygonFeatures).length > this.$props.renderParams.bbox.polygons.max
            ) {
              this.$message({
                type:"error",
                message: `最多绘制${ this.$props.renderParams.bbox.polygons.max}个区域，请重新绘制！`
              });
              return;
            }
            if(this.$props.renderParams.bbox.polygons.pointNums){
              for (let polygon in this.polygonFeatures) {
                if (this.polygonFeatures[polygon].points.length != 4) {
                  this.$message({
                    type:"error",
                    message: "检测区域为四边形，请清除后重新绘制",
                  });
                  return;
                }
              }
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
        areas: Object.keys(this.polygonFeatures).map((id) => {
          return {
            id: id,
            name: this.polygonFeatures[id].name,
            polygon: this.polygonFeatures[id].points,
          };
        }),
      });
    },
    handleNameChg(polygonInfo) {
      this.polygonFeatures[polygonInfo.featureId].name = polygonInfo.name || "";
    },
    handleSavePolygonInfo() {
      this.polygonInfo.show = false;
    },
    handleDelPolygonInfo() {
      gFeatureLayer.removeFeatureById(this.polygonInfo.featureId);
      this.$delete(this.polygonFeatures, this.polygonInfo.featureId);

      this.activeFeatureId = null;
      this.polygonInfo.show = false;
      this.polygonInfo.featureId = "";
      this.polygonInfo.name = "";
      this.polygonInfo.errorInfo = "";
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
</style>