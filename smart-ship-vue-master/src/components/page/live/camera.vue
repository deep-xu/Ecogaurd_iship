<template>
  <div class="camera-comp">
    <div class="deviceType-container">
      <div class="device_type" style="margin-right:20rem" :class="{active: deviceType == 'normal'}" @click="deviceType = 'normal'">算力设备</div>
      <div class="device_type" :class="{active: deviceType == 'forward'}" @click="deviceType = 'forward'">流转发设备</div>
    </div>

    <div class="camera-tree-container">
      <el-tree
        v-show="deviceType == 'normal'"
        ref="tree"
        class="filter-tree"
        :data="cameraTree"
        :props="defaultProps"
        show-checkbox
        node-key="id"
        @node-click="handleAlgfold"
        :default-expanded-keys="defaultExpended"
      >
        <div class="custom-tree-node" slot-scope="{ node, data }">
          <div style="display: flex">
            <div v-if="data.type == 'device'" class="icon-device">
              <i
                class="z-icon-zhankai1"
                :class="{
                  hide: !data.children || data.children.length == 0,
                }"
                v-if="defaultExpended.indexOf(data.id) >= 0"
              ></i>
              <i
                class="z-icon-zhedie1"
                :class="{
                  hide: !data.children || data.children.length == 0,
                }"
                v-else
              ></i>
              <i
                class="z-icon-shebei"
                :class="{
                  online: data.device_status == 1,
                  offline: data.device_status != 1,
                }"
              ></i>
            </div>
            <div
              v-if="data.type == 'camera' || data.type == 'stream'"
              class="icon-camera"
            >
              <i
                v-if="data.checked == true"
                class="z-icon-checked"
                :class="{
                  hide: data.status != 1,
                }"
                @click="handleNodeSelect(node, data, false)"
              ></i>
              <i
                v-if="data.checked == false"
                class="z-icon-uncheck"
                :class="{
                  hide: data.device_status != 1 || data.status != 1,
                }"
                @click="handleNodeSelect(node, data, true)"
              ></i>
              <i
                class="z-icon-camera"
                :class="{
                  online: data.device_status && data.status == 1,
                  offline: data.device_status != 1 || data.status != 1,
                }"
              ></i>
            </div>
            <label>{{ node.label }}</label>
          </div>
        </div>
      </el-tree>
      <el-tree
        v-show="deviceType == 'forward'"
        ref="tree"
        class="filter-tree"
        :data="forwardCameraTree"
        :props="defaultProps"
        show-checkbox
        node-key="id"
        @node-click="handleAlgfold"
        :default-expanded-keys="defaultExpended"
      >
        <div class="custom-tree-node" slot-scope="{ node, data }">
          <div style="display: flex">
            <div v-if="data.type == 'device'" class="icon-device">
              <i
                class="z-icon-zhankai1"
                :class="{
                  hide: !data.children || data.children.length == 0,
                }"
                v-if="defaultExpended.indexOf(data.id) >= 0"
              ></i>
              <i
                class="z-icon-zhedie1"
                :class="{
                  hide: !data.children || data.children.length == 0,
                }"
                v-else
              ></i>
              <i
                class="z-icon-shebei"
                :class="{
                  online: data.device_status == 1,
                  offline: data.device_status != 1,
                }"
              ></i>
            </div>
            <div
              v-if="data.type == 'camera' || data.type == 'stream' || data.type == 'hik_platform'"
              class="icon-camera"
            >
              <i
                v-if="data.checked == true"
                class="z-icon-checked"
                :class="{
                  hide: data.status != 1,
                }"
                @click="handleNodeSelect(node, data, false)"
              ></i>
              <i
                v-if="data.checked == false"
                class="z-icon-uncheck"
                :class="{
                  hide: data.device_status != 1 || data.status != 1,
                }"
                @click="handleNodeSelect(node, data, true)"
              ></i>
              <i
                class="z-icon-camera"
                :class="{
                  online: data.device_status && data.status == 1,
                  offline: data.device_status != 1 || data.status != 1,
                }"
              ></i>
            </div>
            <label>{{ node.label }}</label>
          </div>
        </div>
      </el-tree>
    </div>
  </div>
</template>

<script>
export default {
  props: ["deviceObj", "videonum", "playingCameraList"],
  data() {
    return {
      deviceType:'normal',
      filterText: "",
      cameraTree: [],
      forwardCameraTree: [],
      defaultProps: {
        children: "children",
        label: "label",
      },
      checkedSource: {},
      defaultExpended: [],
    };
  },
  watch: {
    filterText(val) {
      // this.$refs.tree.filter(val);
      // this.getCameraTree();
    },
    deviceObj: {
      deep: true,
      handler() {
        this.getCameraTree();
      },
    },
    videonum(newval, oldval) {
      this.getCameraTree();
      // if (newval < oldval) {
      //   for (let i = oldval - 1; i > newval; i--) {
      //     if (this.playingCameraList[i]) {
      //       this.checkedSource[this.playingCameraList[i]].checked = false;
      //       delete this.checkedSource[this.playingCameraList[i]];
      //     }
      //   }
      // }
    },
  },
  mounted() {
    this.getCameraTree();
  },
  methods: {
    getCameraTree() {
      let selcameras = sessionStorage.getItem("sources");
      if (selcameras) selcameras = JSON.parse(selcameras);
      let objs = JSON.parse(JSON.stringify(this.$props.deviceObj));
      let cameraTree = [];
      let forwardCameraTree = [];
      let checkedSource = {};
      for (let i = 0; i < Object.keys(objs).length; i++) {
        let device_id = Object.keys(objs)[i];
        objs[device_id].label = objs[device_id].name || device_id;
        objs[device_id].id = device_id;
        objs[device_id].device_id = device_id;
        objs[device_id].type = "device";
        objs[device_id].device_status = objs[device_id].status;
        if (objs[device_id].device_status && objs[device_id].source) {
          let children = [];
          Object.keys(objs[device_id].source).forEach((source_id) => {
            objs[device_id].source[source_id].id = device_id + "_" + source_id;
            objs[device_id].source[source_id].device_id = device_id;
            objs[device_id].source[source_id].device_status =
              objs[device_id].device_status;
            objs[device_id].source[source_id].source_id = source_id;
            objs[device_id].source[source_id].label =
              objs[device_id].source[source_id].desc;
            objs[device_id].source[source_id].checked = false;
            objs[device_id].source[source_id].device_status =
              objs[device_id].device_status;
            children.push(objs[device_id].source[source_id]);
          });
          if (this.filterText) {
            children = children.filter(
              (item) => item.label.indexOf(this.filterText) >= 0
            );
          }
          for (let i = 0; i < children.length; i++) {
            if (
              selcameras &&
              selcameras[children[i].id] &&
              Object.keys(checkedSource).length < this.$props.videonum &&
              children[i].status == 1
            ) {
              children[i].checked = true;
              checkedSource[children[i].id] = children[i];
            }
          }
          objs[device_id].children = children;
        }
        if (this.filterText) {
          if (
            objs[device_id].label.indexOf(this.filterText) >= 0 ||
            objs[device_id].children.length > 0
          ) {
            cameraTree.push(objs[device_id]);
          }
        } else {
          cameraTree.push(objs[device_id]);
        }
      }
      this.cameraTree = cameraTree.filter(item => item.hardware_version.indexOf('forward') < 0);
      this.forwardCameraTree = cameraTree.filter(item => item.hardware_version.indexOf('forward') >= 0);


      // 只显示有摄像头的
      // this.cameraTree = cameraTree.filter(
      //   (item) => item.children && item.children.length > 0
      // );
      if (
        JSON.stringify(Object.keys(checkedSource)) !=
        JSON.stringify(Object.keys(this.checkedSource))
      ) {
        let xiangtong = true;
        if (
          Object.keys(checkedSource).length !=
          Object.keys(this.checkedSource).length
        ) {
          xiangtong = false;
        } else {
          for (let sourceId in checkedSource) {
            if (!this.checkedSource[sourceId]) {
              xiangtong = false;
            }
          }
        }

        if (xiangtong == false) {
          this.checkedSource = JSON.parse(JSON.stringify(checkedSource));
          this.$emit("change-source", this.checkedSource);
        }
      }
    },
    handleNodeSelect(node, data, status) {
      if (data.status != 1) {
        return;
      }
      if (this.videonum > 1) {
        if (
          Object.keys(this.checkedSource).length >= this.videonum &&
          status == true
        ) {
          this.$message({
            type: "warning",
            message: "画面已满,请先取消一个选项",
          });
          return;
        }
        data.checked = status;
        if (this.checkedSource[data.id] && status == false) {
          delete this.checkedSource[data.id];
        } else if (!this.checkedSource[data.id] && status == true) {
          this.checkedSource[data.id] = data;
        }
        this.$emit("change-source", this.checkedSource, data);
      } else {
        data.checked = status;
        let ids = Object.keys(this.checkedSource);
        if (ids.length > 0 && ids[0] != data.id) {
          let source_id = ids[0];
          let device_id = source_id.split("_")[0];
          if(this.deviceType == 'normal'){
            for (let i = 0; i < this.cameraTree.length; i++) {
              if (this.cameraTree[i].device_id == device_id) {
                let children = this.cameraTree[i].children;
                for (let j = 0; j < children.length; j++) {
                  if (children[j].id == source_id) {
                    children[j].checked = false;
                    this.$forceUpdate();
                    break;
                  }
                }
                break;
              }
            }
          }else{
            for (let i = 0; i < this.forwardCameraTree.length; i++) {
              if (this.forwardCameraTree[i].device_id == device_id) {
                let children = this.forwardCameraTree[i].children;
                for (let j = 0; j < children.length; j++) {
                  if (children[j].id == source_id) {
                    children[j].checked = false;
                    this.$forceUpdate();
                    break;
                  }
                }
                break;
              }
            }
          }
          
        }
        if (status == true) {
          this.checkedSource = {};
          this.checkedSource[data.id] = data;
        } else {
          this.checkedSource = {};
        }

        this.$emit("change-source", this.checkedSource, data);
      }
    },
    handleAlgfold(data, node, other) {
      if (data.type == "device") {
        data.expanded = node.expanded;
        if (node.expanded == true) {
          if (this.defaultExpended.indexOf(data.id) < 0) {
            this.defaultExpended.push(data.id);
          }
        } else {
          if (this.defaultExpended.indexOf(data.id) >= 0) {
            this.defaultExpended.splice(
              this.defaultExpended.indexOf(data.id),
              1
            );
          }
        }
      }
    },
  },
};
</script>

<style lang="less" scoped>
.camera-comp {
  padding-top: 20rem;
  height: 100%;
  box-sizing: border-box;
  .deviceType-container{
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 14rem;
    color: #333;
    height: 50rem;
    .device_type{
      cursor: pointer;
      border-bottom: 1px solid transparent;
      padding-bottom: 5rem;
    }
    .device_type.active{
      color: #0489ff;
      border-bottom: 1px solid #0489ff;
    }
  }
  .search {
    padding: 0 20rem;
    box-sizing: border-box;
    position: relative;
    /deep/.el-input__inner {
      background-color: transparent;
      border: 1px solid rgba(72, 175, 255, 1);
      border-radius: 0;
      color: #9ecaf7;
    }
    .el-icon-search {
      position: absolute;
      right: 40rem;
      top: 8rem;
      color: #48afff;
      font-size: 18rem;
      cursor: pointer;
    }
  }
  .camera-tree-container {
    margin-top: 15rem;
    height: calc(100% - 60rem);
    padding-left: 20rem;
    box-sizing: border-box;
    overflow: auto;
    /deep/.el-tree__empty-block {
      display: none;
    }
    /deep/.el-tree {
      background: transparent;
      font-size: 14rem;
      color: #333;
      .el-tree-node__content {
        height: 30rem;
      }
      .el-tree-node__content:hover,
      .el-upload-list__item:hover,
      .el-tree-node:focus > .el-tree-node__content {
        background-color: transparent;
      }
      > .el-tree-node .el-tree-node__content .el-checkbox {
        display: none;
      }
      .el-tree-node__expand-icon {
        display: none;
      }
    }
    .custom-tree-node {
      width: calc(100% - 40rem);
      label {
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
        word-break: break-all;
        width: calc(100% - 40rem);
        color: #333;
      }
    }
    .icon-device {
      // background: linear-gradient(0deg, #336fb7, #92c3ff);
      // background-clip: text;
      // -webkit-text-fill-color: transparent;
      font-size: 14rem;
      margin-right: 5rem;
      .z-icon-zhankai1,
      .z-icon-zhedie1 {
        margin-right: 5rem;
        color: #333;
      }
    }
    .icon-camera {
      font-size: 14rem;
      color: #4a8ddd;
      margin-right: 5rem;
      display: flex;
      .z-icon-camera {
        margin-left: 5rem;
      }
    }

    .hide {
      opacity: 0;
    }
  }
  .z-icon-shebei.online,
  .z-icon-camera.online {
    color: rgb(34, 209, 34);
  }
  .z-icon-shebei.offline,
  .z-icon-camera.offline {
    color: rgb(234, 27, 27);
  }
}
</style>