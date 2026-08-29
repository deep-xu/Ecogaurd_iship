<template>
  <div class="alert-list">
    <ul>
      <li v-for="(item, index) in alertList" :key="index + '_alert'">
        <div class="image-container" @click="handleShowAlertDetail(item)">
          <img :src="item.imgpath" alt="" style="cursor:pointer"/>
        </div>
        <div class="info-list">
          <p>{{ item.device_name || item.device_id }}</p>
          <p>{{ item.source_desc }}</p>
          <p>{{ item.alg_ch_name }}</p>
          <p>
            {{ new Date(item.alert_time * 1000).format("YYYY-MM-DD HH:mm:ss") }}
          </p>
        </div>
      </li>
    </ul>
    <el-dialog
      :title="'告警详情'"
      :append-to-body="true"
      :visible.sync="showAlert.show"
      class="live-alarmDetail-dialog"
      v-dialogDrag
      :class="{
        commondetail:
          showAlert.alg_name != 'face' ||
          (showAlert.alg_name == 'face' &&
            showAlert.reserved_data.face &&
            showAlert.reserved_data.face.length == 0),
        facedetail:
          showAlert.alg_name == 'face' &&
          showAlert.reserved_data.face &&
          showAlert.reserved_data.face.length > 0,
      }"
    >
      <ul class="alarm-infos">
        <li>
          <label for="">告警时间：</label
          ><span>{{
            new Date(showAlert.alert_time * 1000).format("YYYY-MM-DD HH:mm:ss")
          }}</span>
        </li>
        <li>
          <label for="">告警类型：</label
          ><span>{{ showAlert.alg_ch_name }}</span>
        </li>
        <li>
          <label for="">危险等级：</label
          ><span>{{ showAlert.hazard_level }}</span>
        </li>
        <li>
          <label for="">数据源：</label><span>{{ showAlert.source_desc }}</span>
        </li>
        <li>
          <label for="">设备名称：</label
          ><span>{{ showAlert.device_name }}</span>
        </li>
        <li
          v-if="
            showAlert &&
            showAlert.reserved_data &&
            showAlert.reserved_data.group
          "
        >
          <label for="">分组名称：</label>
          <span>{{ showAlert.reserved_data.group.name }}</span>
        </li>
      </ul>
      <div class="img-container" style="width: 100%">
        <img :src="showAlert.imgpath" alt="" />
        <div class="faceInfo" v-if="
            showAlert.reserved_data.face &&
            showAlert.reserved_data.face.length > 0
          ">
          <ul>
            <li v-for="(infos, index) in showAlert.reserved_data.face" :key="index + Math.random()">
              <div class="cap">
                <img :src="projConfig.picprev + infos.cap_image" alt="" />
              </div>
              <div class="libImage">
                <img :src="projConfig.picprev + infos.lib_image" alt="" />
              </div>
              <div class="libInfo">
                <p v-if="infos.conf">
                  <label>相似度：</label
                  ><span>{{ (infos.conf * 100).toFixed(0) }}%</span>
                </p>
                <p>
                  <label>姓名：</label><span>{{ infos.name }}</span>
                </p>
                <p>
                  <label>年龄：</label><span>{{ infos.age }}</span>
                </p>
                <p>
                  <label>性别：</label><span>{{ infos.sex }}</span>
                </p>
                <p>
                  <label>简介：</label><span>{{ infos.desc }}</span>
                </p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { projConfig } from "@/utils/config.js";
export default {
  props: ["alertList"],
  data() {
    return {
      projConfig: projConfig,
      showAlert: {
        show: false,
        alert_time: "",
        source_desc: "",
        hazard_level:"",
        device_id: "",
        device_name: "",
        imgpath: "",
        reserved_data: {},
      },
    };
  },
  methods: {
    handleShowAlertDetail(item) {
      this.showAlert.show = true;
      this.showAlert = Object.assign(this.showAlert, item);
      this.showAlert.reserved_data = JSON.stringify(this.showAlert.reserved_data) == '{}'
        ? {} : JSON.parse(this.showAlert.reserved_data);
    },
  },
};
</script>

<style lang="less" scoped>
.alert-list {
  overflow: auto;
  // padding: 10rem 0;
  box-sizing: border-box;
  height: 100% !important;
  ul {
    width: calc(100% - 20rem);
    margin-left: 10rem;
    overflow: auto;
    li {
      border-top: 2rem solid #e0e0e0;
      line-height: 2;
      padding: 8rem 10rem;
      display: flex;
      justify-content: space-between;
      color: #333;
      font-size: 14rem;
      background-color: #fff;
      margin-bottom: 10rem;
      .info-list {
        width: calc(73% - 20rem);
        p {
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          word-break: break-all;
          width: 95%;
          color: #333;
        }
      }
    }
  }

  .image-container {
    width: 27%;
    display: flex;
    align-items: center;
    img {
      max-width: 100%;
    }
  }
}
</style>
<style lang="less">
.live-alarmDetail-dialog {
  .el-dialog {
    background-color: #fff;
    border-radius: 4px;
    .el-dialog__header {
      border-bottom: none;
      text-align: center;
      .el-dialog__title {
        color: #333;
      }
      .el-dialog__headerbtn {
        top: 20rem;
        right: 20rem;
      }
    }
  }
  .alarm-infos {
    display: flex;
    flex-wrap: wrap;
    font-size: 14rem;
    li {
      width: 50%;
      margin-bottom: 5rem;
      label {
        color: #333;
      }
      span {
        color: #666;
      }
    }
  }
}
.live-alarmDetail-dialog.commondetail {
  .el-dialog {
    width: calc(100% - 40rem) !important;
    max-width: 1000px;
  }
  .img-container {
    display: flex;
    justify-content: center;
    // padding: 0 30rem;
    box-sizing: border-box;
    img {
      width: 100%;
    }
  }
}
.live-alarmDetail-dialog.facedetail {
  .el-dialog {
    width: calc(100% - 40rem) !important;
    max-width: 1200px;
  }
  .img-container {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    // padding: 0 30rem;
    box-sizing: border-box;
    height: 100%;
    img {
      width: calc(100% - 350rem);
      margin-right: 20rem;
    }
    .faceInfo {
      width: 330rem;
      max-height: calc(100vh - 350rem);
      overflow: auto;
      font-size: 14rem;
      color: #333;
      li {
        display: flex;
        margin-bottom: 10rem;
        img {
          width: 100rem;
          height: 100rem;
          object-fit: contain;
        }
      }
    }
  }
}
</style>