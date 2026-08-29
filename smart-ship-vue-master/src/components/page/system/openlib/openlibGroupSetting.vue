<template>
  <div class="open_lib-win">
    <div class="groupNameList" v-if="groupList.length > 0">
      <ul>
        <li
          v-for="(item, index) in groupList"
          :key="item.id"
          :class="{ active: formData.index == index }"
          @click="handleGroupChg(item, index)"
        >
          <div class="groupname">{{ item.name }}</div>
          <i
            class="z-iconfont z-icon-del"
            @click.stop="handleDelGroup(item)"
          ></i>
        </li>
        <li @click="handleAddGroup">
          <div class="groupname">
            <i class="z-iconfont z-icon-add"></i>
          </div>
        </li>
      </ul>
    </div>
    <div class="group-edit-container">
      <el-form
        ref="formData"
        :model="formData"
        :rules="formRule"
        style="
          width: 642px;
          padding-top: 30px;
          position: relative;
          margin: 0 auto;
        "
        label-width="112"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder=""
            :maxlength="30"
            style="width: 200px"
          ></el-input>
        </el-form-item>
        <div
          class="btns"
          style="
            position: absolute;
            width: fit-content;
            display: flex;
            right: 0px;
            top: 30px;
          "
        >
          <el-upload
            action="#"
            :before-upload="handleUploadPic"
            accept=".jpg,.png,.jpeg"
            style="margin-right: 10px"
          >
            <el-button type="primary">选择图片</el-button>
          </el-upload>
          <el-button
            type="primary"
            @click="handleSetMode"
            v-if="formData.roiEnabled"
            >绘制ROI</el-button
          >
          <el-button disabled readonly v-if="!formData.roiEnabled"
            >绘制ROI</el-button
          >
        </div>
        <div style="position: relative">
          <div class="painting-parent">
            <div class="painting-container" ref="painting-container">
              <div id="gmap"></div>
            </div>
          </div>
          <div
            style="
              position: absolute;
              right: 0;
              bottom: -20px;
              height: 20px;
              line-height: 20px;
            "
          >
            {{ finishTip }}
          </div>
        </div>
        <el-form-item style="text-align: center; width: 642px">
          <el-button type="primary" :loading="saveloading" @click="handleSave"
            >保存</el-button
          >
          <el-button type="error" @click="handleClearPolygon">清除</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import AILabel from "ailabel";
import { getId } from "@/api/common.js";
import { group } from "@/api/libs.js";
let gMap = null; // 全局画布对象坐标与图片坐标一直 向下为y正方向，向右为x正方向
let gFeatureLayer = null; // 绘制图层
let imageLayer = null;
let polygonStyle = { strokeStyle: "#02fc36", lineWidth: 1 }; // 多边形的颜色
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["groupList", "group_id", "device_id"],
  data() {
    return {
      saveloading: false,
      formData: {
        index: 0,
        id: "",
        name: "",
        roi: [],
        roiEnabled: false,
      },
      formRule: {
        name: [
          {
            required: true,
            message: "请输入分组名称",
            trigger: "blur",
          },
        ],
      },

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
        areas: [], // type对应的标记区域
        action: "see", // draw/see  画框/仅查看
      },

      roiFeature: null, // {id, points, feature}
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
    group_id(n, o) {
      this.resetInfo();
    },
    groupList: {
      deep: true,
      handler(n, o) {
        if (n.length > 0 && this.formData.optype == "add") {
          this.formData.optype = "edit";
          this.formData.id = n[n.length - 1].id;
        }
        let find = n.find((item) => item.id == this.formData.id);
        if (!find) {
          if (n.length > 0) {
            this.formData.index = 0;
            this.formData.name = n[0].name;
            this.formData.id = n[0].id;
            this.formData.quality = n[0].ext.quality;
          } else {
            this.formData.name = "";
            this.formData.id = "";
            this.formData.quality = 0.65;
          }
        } else {
          for (let i = 0; i < n.length; i++) {
            if (n[i].id == this.formData.id) {
              this.formData.index = i;
              break;
            }
          }
        }
      },
    },
  },
  mounted() {
    this.resetInfo();
  },
  methods: {
    validateRoi(rule, value, callback) {
      if (this.formData.roi.length == 0) {
        callback("请绘制一个ROI区域");
      } else {
        callback();
      }
    },
    resetInfo() {
      if (this.$props.group_id) {
        this.formData.optype = "edit";
        for (let i = 0; i < this.$props.groupList.length; i++) {
          if (this.$props.groupList[i].id == this.$props.group_id) {
            this.formData.index = i;
            this.formData.name = this.$props.groupList[i].name;
            this.formData.id = this.$props.group_id;
            this.formData.roi = this.$props.groupList[i].ext.roi;
            this.imgInfo = Object.assign(
              this.imgInfo,
              this.$props.groupList[i].ext.imgInfo
            );
            this.destoryMap();
            this.setMapInfo(this.imgInfo.mapWidth, this.imgInfo.mapHeight);
            this.initGmap();
            this.initGmapEvent();
            break;
          }
        }
      } else {
        this.formData.optype = "add";
        this.handleClearInfo();
      }
    },
    handleSave() {
      let roi = [];
      if (Object.values(this.polygonFeatures).length > 0) {
        roi = Object.values(this.polygonFeatures)[0].points;
      }
      this.formData.roi = roi;
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.saveloading = true;
          let imgInfo = JSON.parse(JSON.stringify(this.imgInfo));
          delete imgInfo.imageUrl;
          let params = {
            alg: "open_lib",
            name: this.formData.name,
            ext: {
              roi: roi,
              imgInfo: imgInfo,
            },
          };
          if (this.formData.optype == "edit") {
            params.id = this.formData.id;
            params.device_id = this.$props.device_id;
            group
              .edit(params)
              .then((res) => {
                this.saveloading = false;
                this.$message({
                  type: "success",
                  message: res.message[this.lang],
                });
                this.$emit("update-open_lib-group");
              })
              .catch((err) => {
                this.saveloading = false;
                if (res && res.message && res.message[this.lang]) {
                  this.$message({
                    type: "error",
                    message: res.message[this.lang],
                  });
                }
              });
          } else {
            params.device_ids = [this.$props.device_id];
            group
              .add(params)
              .then((res) => {
                this.saveloading = false;
                if (res[this.$props.device_id].result == true) {
                  this.$message({
                    type: "success",
                    message: "新增分组成功",
                  });
                  // this.$emit("update-workclothes-group");
                  this.$emit("close-open_lib-group");
                }
              })
              .catch((err) => {
                this.saveloading = false;
                if (res && res.message && res.message[this.lang]) {
                  this.$message({
                    type: "error",
                    message: res.message[this.lang],
                  });
                }
              });
          }
        }
      });
    },
    handleDelGroup(item) {
      this.$confirm("是否确认删除?", "删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      })
        .then(() => {
          group
            .del({
              ids: [item.id],
              alg: "open_lib",
              device_ids: [this.$props.device_id],
            })
            .then((res) => {
              if (item.id == this.formData.id) {
                this.formData.id = "";
                this.formData.name = "";
              }
              if (res[this.$props.device_id].result == true) {
                this.$message({
                  type: "success",
                  message: "删除分组成功",
                });
              }
              this.$emit("update-open_lib-group");
            })
            .catch((error) => {
              if (error.message) {
                this.$message({
                  type: "error",
                  message: error.message[this.lang],
                });
              }
            });
        })
        .catch((error) => {
          // 取消删除
        });
    },
    handleGroupChg(item, index) {
      this.formData.roiEnabled = false;
      gMap && gMap.setMode("BAN");
      this.finishTip = "";
      this.formData.optype = "edit";
      this.destoryMap();
      this.formData.index = index;
      this.formData.id = item.id;
      this.formData.name = item.name;
      this.formData.roi = item.ext.roi;
      this.imgInfo = Object.assign(this.imgInfo, item.ext.imgInfo);
      this.setMapInfo(this.imgInfo.mapWidth, this.imgInfo.mapHeight);
      this.initGmap();
      this.initGmapEvent();
    },
    handleClearInfo() {
      this.formData.index = this.$props.groupList.length;
      this.formData.id = "";
      this.formData.name = "";
      this.formData.roi = [];
      this.imgInfo = Object.assign(this.imgInfo, {
        imageUrl: "",
        width: null, // 图片宽
        height: null, // 图片高
        mapWidth: null, // 画布宽
        mapHeight: null, // 画布高
      });
      this.destoryMap();
    },
    handleAddGroup() {
      this.formData.optype = "add";
      this.handleClearInfo();
      this.formData.index = this.groupList.length;
    },
    handleUploadPic(file) {
      this.imgInfo.imageUrl = URL.createObjectURL(file);
      var img = document.createElement("img");
      img.src = this.imgInfo.imageUrl;
      img.onload = () => {
        this.formData.roiEnabled = true;
        this.spinShow = false;
        this.imgInfo = Object.assign(this.imgInfo, this.getImageInfo(img));
        this.destoryMap();
        this.initGmap(img);
        this.initGmapEvent(img);
      };
      return false;
    },

    destoryMap() {
      if (gMap) {
        // 恢复画布
        this.roiFeature = null;
        this.polygonFeatures = {};
        gMap.setActiveFeature(null);
        gMap.removeAllLayers();
        gMap = null;
      }
    },
    handleClearPolygon() {
      if (Object.keys(this.polygonFeatures).length > 0) {
        for (let key in this.polygonFeatures) {
          this.polygonFeatures[key];
          gFeatureLayer.removeFeatureById(key);

          this.$delete(this.polygonFeatures, key);
        }
      }
    },
    // 绘图相关
    handleSetMode() {
      if (gMap) {
        gMap.setMode("POLYGON");
        this.finishTip = "双击结束";
      } else {
        this.$message({
          type: "error",
          message: "请选择图片",
        });
      }
    },
    getImageInfo(img) {
      // 获取图片宽高，设置画布宽高
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
    setMapInfo(mapWidth, mapHeight) {
      // 获取图片宽高，设置画布宽高
      const container = this.$refs["painting-container"]; // width: 768 height: 768*imgheight/imgwidth
      const map = document.querySelector("#gmap");
      // 画布宽度设置为容器宽度
      map.style.width = mapWidth + "px";
      map.style.height = mapHeight + "px";
      // 容器设置为和画布一样大小
      container.style.height = map.style.height;
    },
    initGmap(img) {
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
      gMap.setDrawingStyle(polygonStyle);

      if (img) {
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
      }

      // 如果roi不为null 绘制到画布上
      // 先画多边形再画矩形，否则矩形范围内的多边形不能编辑
      this.initPolygons();
    },
    initGmapEvent() {
      let _this = this;
      // 绘制完成事件监听
      gMap.events.on("drawDone", function (type, data) {
        if (type === "POLYGON") {
          _this.updatePolygon(data);
        }
      });

      // 设置选中
      gMap.events.on("featureSelected", (feature) => {
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

          this.$delete(this.polygonFeatures, this.activeFeatureId);

          this.activeFeatureId = null;
          this.polygonInfo.show = false;
          this.polygonInfo.featureId = "";
        }
      });

      // 编辑后更新图形
      gMap.events.on("featureUpdated", (feature, data) => {
        if (feature.type == "POLYGON") {
          _this.updatePolygon(data, feature);
        }
      });
    },
    initPolygons() {
      if (this.formData.roi && this.formData.roi.length == 0) {
        return;
      }
      let polygon = this.formData.roi;
      let data = polygon.map((item) => {
        return {
          x: item[0],
          y: item[1],
        };
      });
      let id = getId();
      const polygonFeature = new AILabel.Feature.Polygon(
        id, // id
        { points: data }, // shape
        { name: "第一个多边形" }, // props
        polygonStyle
      );
      gFeatureLayer.addFeature(polygonFeature);
      this.$set(this.polygonFeatures, id, {
        id: id,
        points: polygon,
        feature: polygonFeature,
      });
    },
    updatePolygon(data, feature) {
      // 新增多边形：data:[{x,y},{x,y}] 编辑多边形：data:{points}
      let points = data.points || data;
      points.forEach((point) => {
        if (point.x < 0) point.x = 0;
        if (point.y < 0) point.y = 0;
        if (point.x > this.imgInfo.width) point.x = this.imgInfo.width;
        if (point.y > this.imgInfo.height) point.y = this.imgInfo.height;
      });

      if (feature) {
        // 更新多边形
        feature.updateShape({ points: points });
        this.polygonInfo.show = true;
        this.polygonInfo.featureId = feature.id;

        this.polygonFeatures[feature.id].points = points.map((item) => [
          Math.abs(Math.floor(item.x)),
          Math.floor(item.y),
        ]);
      } else {
        if (Object.keys(this.polygonFeatures).length > 0) {
          for (let key in this.polygonFeatures) {
            this.polygonFeatures[key];
            gFeatureLayer.removeFeatureById(key);

            this.$delete(this.polygonFeatures, key);
          }
        }
        // 画多边形
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
        });
        this.polygonInfo.optype = "add";
        this.polygonInfo.show = true;
        this.polygonInfo.featureId = polygonFeature.id;
      }
      // 最后画一次矩形roi区域，否则多边形不能编辑
    },
  },
};
</script>

<style lang="less" scoped>
.open_lib-win {
  display: flex;
  .groupNameList {
    width: 140px;
    max-height: 60vh;
    overflow: auto;
    border-right: 1px solid #2a2b33;
    ul {
      list-style: none;
      li {
        position: relative;
        width: 100px;
        .groupname {
          width: 80px;
          text-align: center;
          padding: 6px 10px;
          border: 1px solid #2a2b33;
          border-radius: 4px;
          color: #333;
          cursor: pointer;
          margin-bottom: 10px;
          position: relative;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          word-break: break-all;
        }
        .z-icon-del {
          position: absolute;
          right: -20px;
          top: 8px;
          font-size: 14px;
          color: #959595;
          cursor: pointer;
        }
      }

      li.active .groupname {
        background-color: #e1e1e1;
      }
    }
  }
  .group-edit-container {
    width: 100%;
    flex: 1 1;
    display: flex;
    justify-content: center;
    max-height: 60vh;
    overflow: auto;
  }
}

.painting-parent {
  overflow: auto;
  margin: 15px auto;
}
.painting-parent::-webkit-scrollbar {
  background: rgb(239, 239, 239);
  height: 5px;
  border-radius: 2px;
}
.painting-container {
  width: 640px;
  height: 360px;
  /** 高度应根据图片宽高来设置   */
  overflow: hidden;
  position: relative;
  transform-origin: 0 0;
  transform: scale(1);
  border: 1px solid gray;
}
#gmap {
  position: absolute;
}
</style>