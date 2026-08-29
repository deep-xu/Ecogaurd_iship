<template>
  <div class="camera-comp">
    <div class="deviceType-container">
      <div class="device_type" style="margin-right:20rem" :class="{active: deviceType === 'group'}" @click="deviceType = 'group'">Group</div>
    </div>
    <div class="search">
      <el-input placeholder="" v-model="filterText"> </el-input>
      <i class="el-icon-search" @click="handleLoadGroupTree"></i>
    </div>
    <div class="camera-tree-container">
      <el-tree
        ref="tree"
        class="filter-tree"
        :data="groupTree"
        :props="groupProps"
        show-checkbox
        node-key="id"
        :filter-node-method="filterNode"
      >
        <div  class="custom-tree-node" slot-scope="{ node, data }">
          <div style="display: flex">
            <div v-if="data.type == 'device'" class="icon-device">
              <i
                class="z-icon-zhedie1"
                :class="{
                  hide: !data.children || data.children.length === 0,
                }"
              ></i>
              <i
                class="z-icon-shebei"
                :class="{
                  online: true,
                }"
              ></i>
            </div>
            <div
              v-if="data.type == 'camera' "
              class="icon-camera"
            >
              <i
                v-if="data.checked"
                class="z-icon-checked"
                :class="{
                  hide: data.status !== 1,
                }"
                @click="handleNodeSelect(node, data, false)"
              ></i>
              <i
                v-else
                class="z-icon-uncheck"
                :class="{
                  hide: data.status !== 1,
                }"
                @click="handleNodeSelect(node, data, true)"
              ></i>
              <i
                class="z-icon-camera"
                :class="{
                  online:  data.status === 1,
                  offline:  data.status !== 1,
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
import {nvrChannelApis} from "@/api/nvrChannelApis";

export default {
  name: "GroupTree",
  props: ["playingCameras"],
  data() {
    return {
      deviceType:'group',
      filterText: "",
      groupProps:{
        children: "children",
        label: "label",
      },
      groupTree:[],
    };
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },
    playingCameras:{
      handler(newCameras) {
        console.log(newCameras);
        this.groupTree.forEach((item) => {
          if(item.children){
            item.children.forEach(child => {
              child.checked = newCameras.includes(child);
            })
          }
        })
      }
    }
  },
  mounted() {
    this.handleLoadGroupTree();
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    async handleLoadGroupTree(){
      this.groupTree=await nvrChannelApis.getChannelTree();
    },
    handleNodeSelect(node, data,checked){
      if (data.status !== 1) {
        return;
      }
      data.checked = checked;
      this.$emit("change-source", data);
    }
  },
};
</script>

<style lang="less" scoped>
.camera-comp {
  height: 100%;
  box-sizing: border-box;
  .deviceType-container{
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 14rem;
    color: #fff;
    height: 50rem;
    .device_type{
      cursor: pointer;
      border-bottom: 1px solid transparent;
      padding-bottom: 5rem;
    }
    .device_type.active{
      color: #00e2fd;
      border-bottom: 1px solid #00e2fd;
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
    height: calc(100% - 120rem);
    padding-left: 20rem;
    box-sizing: border-box;
    overflow: auto;
    /deep/.el-tree__empty-block {
      display: none;
    }
    /deep/.el-tree {
      background: transparent;
      font-size: 14rem;
      color: #fff;
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
        color: #d0e1f3;
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
        color: #d0e1f3;
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
  .el-icon-s-platform.online,
  .z-icon-shebei.online,
  .z-icon-camera.online {
    color: rgb(34, 209, 34);
  }
  .el-icon-s-platform.offline,
  .z-icon-shebei.offline,
  .z-icon-camera.offline {
    color: orange;;
  }
}
</style>