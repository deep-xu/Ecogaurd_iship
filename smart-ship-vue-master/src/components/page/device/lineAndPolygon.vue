<template>
  <div style="position: relative; padding-top: 20px" v-loading="spinShow">
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
    <div
      v-show="drawparams.action != 'see'"
      style="margin-top: 20px; display: flex; align-items: center"
    >
      <el-button
        type="primary"
        @click="handleSetMode('POLYGON')"
        v-if="renderParams.bbox.polygons"
        ><span
          class="must"
          v-if="
            renderParams.bbox.polygons &&
            renderParams.bbox.polygons.exits == 'must'
          "
          >*</span
        >编辑多边形</el-button
      >
      <el-button
        type="primary"
        style="margin: 0 10px"
        @click="handleSetMode('LINE')"
        v-if="
          renderParams.bbox.lines &&
          (renderParams.bbox.lines.exits == 'optional' ||
            renderParams.bbox.lines.exits == 'must')
        "
        ><span
          class="must"
          v-if="
            renderParams.bbox.lines && renderParams.bbox.lines.exits == 'must'
          "
          >*</span
        >编辑直线</el-button
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
      <div class="finishTip" style="margin-left: 10px">
        {{ finishTip }}
      </div>
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
      <div
        style="display: flex; align-items: center"
        v-if="
          polygonInfo.featureId.indexOf('line_') >= 0 &&
          renderParams.bbox.lines &&
          renderParams.bbox.lines.cross == true
        "
      >
        <div style="width: 70px"><span class="must">*</span>方向选择</div>
        <el-select
          v-model="polygonInfo.direction"
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
      <div
        v-if="
          polygonInfo.featureId.indexOf('line_') >= 0 &&
          polygonInfo.direction &&
          polygonInfo.direction.length == 2
        "
      >
        <div style="width: 70px"><span class="must">*</span>行为描述</div>
        <div style="display: flex; align-items: center">
          <label style="width: 70px; text-align: center">计数</label>
          <el-input
            v-model="polygonInfo.action.count"
            style="width: 150px; margin-bottom: 5px"
            @input="handleActionChg(polygonInfo)"
            placeholder="统计"
          />
        </div>
      </div>
      <div
        v-if="
          polygonInfo.featureId.indexOf('line_') >= 0 &&
          polygonInfo.direction &&
          polygonInfo.direction.length == 4
        "
      >
        <div style="width: 70px"><span class="must">*</span>行为描述</div>
        <div
          style="display: flex; flex-direction: column"
          v-if="polygonInfo.action"
        >
          <div style="display: flex; align-items: center">
            <label style="width: 70px; text-align: center">增加</label>
            <el-input
              v-model="polygonInfo.action.increase"
              style="width: 150px; margin-bottom: 5px"
              @input="handleActionChg(polygonInfo)"
              placeholder="进入"
            />
          </div>
          <div style="display: flex; align-items: center">
            <label style="width: 70px; text-align: center">减少</label>
            <el-input
              v-model="polygonInfo.action.decrease"
              style="width: 150px; margin-bottom: 5px"
              @input="handleActionChg(polygonInfo)"
              placeholder="离开"
            />
          </div>
          <div style="display: flex; align-items: center">
            <label style="width: 70px; text-align: center">净增</label>
            <el-input
              v-model="polygonInfo.action.delta"
              style="width: 150px; margin-bottom: 5px"
              @input="handleActionChg(polygonInfo)"
              placeholder="统计"
            />
          </div>
        </div>
      </div>
      <div v-if="polygonInfo.errorInfo" style="margin-left:70px; color: red">
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
          >{{ polygonInfo.optype == "add" ? "取消" : "删除" }}</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import AILabel from "ailabel";
import { getId } from "@/api/common.js";
import { sourceApis } from "@/api/device.js";
let gMap = null; // 全局画布对象坐标与图片坐标一直 向下为y正方向，向右为x正方向
let gFeatureLayer = null; // 矩形图层
let imageLayer = null;
let gTextLayer = null;
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
        rtsp: "", // 获取图片
        action: "see", // draw/see  画框/仅查看
      },

      roiFeature: null, // {id, points, feature}
      polygonFeatures: {},
      activeFeatureId: null,
      finishTip: "",
      textFeatures: {},

      // 多边形名称、id等
      polygonInfo: {
        show: false,
        errorInfo: "",
        featureId: "",
        name: "",
        optype: "add",
        direction: "r+",
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
      this.drawparams.areas = JSON.parse(
        JSON.stringify(this.$props.areaInfo.areas)
      );
      this.drawparams.lines = JSON.parse(
        JSON.stringify(this.$props.areaInfo.lines)
      );
      this.drawparams.action = this.$props.areaInfo.action;

      if (gMap) {
        // 恢复画布
        this.roiFeature = null;
        this.polygonFeatures = {};
        gMap.setActiveFeature(null);
        gMap.removeAllLayers();
        this.clearAllText();
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
      this.initPolygons();
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
          if (this.$props.renderParams.bbox.lines.cross) {
            this.$set(this.polygonFeatures, id, {
              id: id,
              points: lines[i].line,
              feature: lineFeature,
              direction: lines[i].direction,
              name: lines[i].name,
              action: lines[i].action,
            });
            this.clearAllText();
            this.setAllText();
          } else {
            this.$set(this.polygonFeatures, id, {
              id: id,
              points: lines[i].line,
              feature: lineFeature,
              name: lines[i].name,
            });
          }
        }
      }
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
              name: areas[i].name,
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
        if (type === "POLYGON") {
          _this.updatePolygon(data);
        }
        if (type === "LINE") {
          let points = [
            { x: data.start.x, y: data.start.y },
            { x: data.end.x, y: data.end.y },
          ];
          _this.updatePolygon(points);
        }
      });

      // 设置选中
      gMap.events.on("featureSelected", (feature) => {
        if (this.areaInfo.action == "see") return;
        this.polygonInfo.featureId = feature.id;
        this.polygonInfo.name =
          this.polygonFeatures[this.polygonInfo.featureId].name;
        this.polygonInfo.optype = "edit";
        if (
          feature.id.indexOf("line") >= 0 &&
          this.$props.renderParams.bbox.lines.cross == true
        ) {
          this.polygonInfo.direction =
            this.polygonFeatures[this.polygonInfo.featureId].direction;
          this.polygonInfo.action =
            this.polygonFeatures[this.polygonInfo.featureId].action;
        }

        gMap.setActiveFeature(feature);
        this.activeFeatureId = feature.id;
        this.polygonInfo.show = true;
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
          this.polygonInfo.show = false;
          this.polygonInfo.featureId = "";
          this.polygonInfo.name = "";
        }
      });

      // 编辑后更新图形
      gMap.events.on("featureUpdated", (feature, data) => {
        if (feature.type == "POLYGON") {
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

      if (feature) {
        if (points.length > 2) {
          // 更新多边形
          feature.updateShape({ points: points });
          this.polygonInfo.show = true;
          this.polygonInfo.name = this.polygonFeatures[feature.id].name;
          this.polygonInfo.featureId = feature.id;

          this.polygonFeatures[feature.id].points = points.map((item) => [
            Math.abs(Math.floor(item.x)),
            Math.floor(item.y),
          ]);
        } else {
          // 更新直线
          feature.updateShape({
            start: { x: points[0].x, y: points[0].y },
            end: { x: points[1].x, y: points[1].y },
            width: 2,
          });
          this.polygonFeatures[feature.id].points = points.map((item) => [
            Math.abs(Math.floor(item.x)),
            Math.floor(item.y),
          ]);

          this.polygonInfo.show = true;
          this.polygonInfo.name = this.polygonFeatures[feature.id].name;
          this.polygonInfo.featureId = feature.id;
          if (this.$props.renderParams.bbox.lines.cross) {
            this.polygonInfo.direction =
              this.polygonFeatures[feature.id].direction;
            this.polygonInfo.action = JSON.parse(
              JSON.stringify(this.polygonFeatures[feature.id].action)
            );
            this.clearAllText();
            this.setAllText();
          }
        }
      } else {
        if (points.length > 2) {
          // 画多边形
          // let id = "polygon" + new Date().getTime();
          let id = "polygon_" + getId();
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
          let id = "line_" + getId();
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
          if (this.$props.renderParams.bbox.lines.cross) {
            this.$set(this.polygonFeatures, id, {
              id: id,
              points: points.map((item) => [
                Math.floor(item.x),
                Math.floor(item.y),
              ]),
              feature: lineFeature,
              name: "",
              direction: "r+",
              action: {
                count: "统计",
                increase: "进入",
                decrease: "离开",
                delta: "统计",
              },
            });
            this.polygonInfo.optype = "add";
            this.polygonInfo.show = true;
            this.polygonInfo.direction = "r+";
            this.polygonInfo.name = "";
            this.polygonInfo.action = {
              count: "统计",
              increase: "进入",
              decrease: "离开",
              delta: "统计",
            };
            this.polygonInfo.featureId = lineFeature.id;

            this.clearAllText();
            this.setAllText();
          } else {
            this.$set(this.polygonFeatures, id, {
              id: id,
              points: points.map((item) => [
                Math.floor(item.x),
                Math.floor(item.y),
              ]),
              feature: lineFeature,
              name: "",
            });
            this.polygonInfo.optype = "add";
            this.polygonInfo.show = true;
            this.polygonInfo.name = "";
            this.polygonInfo.featureId = lineFeature.id;
            delete this.polygonInfo.direction;
          }

          // 清除所有多边形重新画一遍，后画的在下层，应保证线在上层
          this.clearAllPolygon();
          this.setAllPolygon();
        }
      }
      // 最后画一次矩形roi区域，否则多边形不能编辑
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

      this.clearAllText();
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
      let polygons = Object.keys(this.polygonFeatures).filter(
        (key) => key.indexOf("polygon") >= 0
      );
      let lines = Object.keys(this.polygonFeatures).filter(
        (key) => key.indexOf("line") >= 0
      );
      if (polygons.length == 0) {
        if (
          this.$props.renderParams.bbox.polygons &&
          this.$props.renderParams.bbox.polygons.exits == "must"
        ) {
          this.$message({
            type: "error",
            message: `${this.$props.algInfo.reserved_args.ch_name}未绘制多边形区域`,
          });
          return;
        }
      } else {
        let max =
          this.$props.renderParams.bbox.polygons &&
          this.$props.renderParams.bbox.polygons.max;
        if (
          this.$props.renderParams.bbox.polygons &&
          max != -1 &&
          max < polygons.length
        ) {
          this.$message({
            type: "error",
            message: `最多绘制${this.$props.renderParams.bbox.polygons.max}个区域，请重新绘制！`,
          });
          return;
        }
        let edge =
          this.$props.renderParams.bbox.polygons &&
          this.$props.renderParams.bbox.polygons.edge;
        if (this.$props.renderParams.bbox.polygons && edge != -1) {
          for (let polygonId in this.polygonFeatures) {
            if (
              polygonId.indexOf("polygon") >= 0 &&
              this.polygonFeatures[polygon].points.length != edge
            ) {
              this.$message({
                type: "error",
                message: `每个多边形最多${edge}条边，请重新绘制！`,
              });
              return;
            }
          }
        }
      }

      if (lines.length == 0) {
        if (
          this.$props.renderParams.bbox &&
          this.$props.renderParams.bbox.lines &&
          this.$props.renderParams.bbox.lines.exits == "must"
        ) {
          this.$message({
            type: "error",
            message: `${this.$props.algInfo.reserved_args.ch_name}未绘制直线`,
          });
          return;
        }
      } else {
        let max =
          this.$props.renderParams.bbox &&
          this.$props.renderParams.bbox.lines &&
          this.$props.renderParams.bbox.lines.max;

        if (
          max != -1 &&
          this.$props.renderParams.bbox &&
          this.$props.renderParams.bbox.lines &&
          this.$props.renderParams.bbox.lines.max &&
          lines.length > max
        ) {
          let max = this.$props.renderParams.bbox.lines.max;
          this.$message({
            type: "error",
            message: `最多绘制${this.$props.renderParams.bbox.lines.max}条直线`,
          });
          return;
        }
      }

      this.$emit("close", {
        type: this.areaInfo.type,
        roi: this.roiFeature && this.roiFeature.points,
        areas: Object.keys(this.polygonFeatures)
          .filter((id) => {
            return id.indexOf("polygon") >= 0;
          })
          .map((id) => {
            return {
              id: id,
              name: this.polygonFeatures[id].name,
              polygon: this.polygonFeatures[id].points,
            };
          }),
        lines: Object.keys(this.polygonFeatures)
          .filter((id) => {
            return id.indexOf("line") >= 0;
          })
          .map((id) => {
            let props = {
              id: id,
              name: this.polygonFeatures[id].name,
              line: this.polygonFeatures[id].points,
            };
            if (this.polygonFeatures[id].direction) {
              let action = {};
              if (this.polygonFeatures[id].direction.length == 2) {
                action.count = this.polygonFeatures[id].action.count;
              } else {
                action.increase = this.polygonFeatures[id].action.increase;
                action.decrease = this.polygonFeatures[id].action.decrease;
                action.delta = this.polygonFeatures[id].action.delta;
              }
              props.direction = this.polygonFeatures[id].direction;
              props.action = action;
            }
            return props;
          }),
      });
    },
    handleNameChg(polygonInfo) {
      this.polygonFeatures[polygonInfo.featureId].name = polygonInfo.name || "";
    },
    handleSavePolygonInfo() {
      this.polygonInfo.errorInfo = "";
      if (this.$props.renderParams.bbox.lines && this.$props.renderParams.bbox.lines.cross && this.polygonInfo.featureId.indexOf('line') >= 0) {
        let actions = "";
        if (this.polygonInfo.direction.length == 2) {
          actions = this.polygonInfo.action.count;
        } else {
          let tmpobj = JSON.parse(JSON.stringify(this.polygonInfo.action));
          delete tmpobj.count;
          actions = Object.values(tmpobj).join("");
        }
        if (!actions) {
          this.polygonInfo.errorInfo = "行为描述不能为空";
          return;
        }
      }
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
    handleDirectionChg(polygonInfo) {
      if (polygonInfo.direction) {
        this.polygonFeatures[polygonInfo.featureId].direction =
          polygonInfo.direction;

        this.clearAllText();
        this.setAllText();
      }
      this.polygonInfo.errorInfo = "";
    },
    clearAllPolygon() {
      for (let id in this.polygonFeatures) {
        if (id.indexOf("polygon") >= 0) {
          gFeatureLayer.removeFeatureById(id);
          // this.$delete(this.polygonFeatures, id);
        }
      }
    },
    setAllPolygon() {
      for (let id in this.polygonFeatures) {
        if (id.indexOf("polygon") >= 0) {
          let points = this.polygonFeatures[id].points.map((item) => {
            return {
              x: item[0],
              y: item[1],
            };
          });
          const polygonFeature = new AILabel.Feature.Polygon(
            id, // id
            { points: points }, // shape
            { name: "第一个多边形" }, // props
            polygonStyle // style
          );
          gFeatureLayer.addFeature(polygonFeature);
        }
      }
    },
    clearAllText() {
      for (let id in this.textFeatures) {
        gTextLayer.removeTextById(id);
        this.$delete(this.textFeatures, id);
      }
    },
    setAllText() {
      let polygonFeatures = this.polygonFeatures;
      let lines = Object.keys(this.polygonFeatures)
        .filter((id) => id.indexOf("line") >= 0)
        .map((id) => {
          let action = this.polygonFeatures[id].action;
          return {
            line: this.polygonFeatures[id].points,
            direction: this.polygonFeatures[id].direction,
            name: this.polygonFeatures[id].name,
            action: JSON.parse(JSON.stringify(action)),
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
        let id = "text_" + +getId();
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
    handleActionChg(polygonInfo) {
      this.polygonFeatures[polygonInfo.featureId].action = JSON.parse(
        JSON.stringify(polygonInfo.action)
      );
      let actions = "";
      if (this.polygonInfo.direction.length == 2) {
        actions = this.polygonInfo.action.count;
      } else {
        let tmpobj = JSON.parse(JSON.stringify(this.polygonInfo.action));
        delete tmpobj.count;
        actions = Object.values(tmpobj).join("");
      }
      if (!actions) {
        this.polygonInfo.errorInfo = "行为描述不能为空";
      } else {
        this.polygonInfo.errorInfo = "";
      }
      this.$forceUpdate()
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
</style>