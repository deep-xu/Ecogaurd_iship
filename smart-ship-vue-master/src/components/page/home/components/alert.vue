<template>
  <div class="alert-list">
    <ul>
      <li v-for="(item, index) in alertList" :key="index + '_alert'">
        <div class="image-container" @click="handleShowAlertDetail(item)">
          <img :src="item.imgpath" alt="" style="cursor: pointer" />
        </div>
        <div class="info-list">
          <p>{{ item.device_name || item.device_id }}</p>
          <p>{{ item.source_desc }}</p>
          <p>{{ item.alg_ch_name }}</p>
          <p>
            {{ new Date(item.alert_time * 1000).format("YYYY-MM-DD HH:mm:ss") }}
          </p>
          <div
            class="btn-pending"
            v-show="item.status == 1"
            @click="handleAlertDeal(item)"
          >
            <i class="z-icon-daichuli"></i>Pending
          </div>
          <div
            class="btn-deal"
            v-show="item.status == 2"
            @click="handleAlertStatusRestet(item)"
          >
            <i class="z-icon-queren"></i>Confirmed
          </div>
        </div>
      </li>
    </ul>
    <el-dialog
      :title="'Alarm Details'"
      :append-to-body="true"
      :visible.sync="showAlert.show"
      class="home-alarmDetail-dialog"
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
      <div
        class="btn-pending"
        v-show="showAlert.status == 1"
        @click="handleAlertDeal(showAlert)"
      >
        <i class="z-icon-daichuli"></i>Pending
      </div>
      <div
        class="btn-deal"
        v-show="showAlert.status == 2"
        @click="handleAlertStatusRestet(showAlert)"
      >
        <i class="z-icon-queren"></i>Confirmed
      </div>
      <ul class="alarm-infos">
        <li>
          <label for="">Alarm Time：</label
          ><span>{{
            new Date(showAlert.alert_time * 1000).format("YYYY-MM-DD HH:mm:ss")
          }}</span>
        </li>
        <li>
          <label for="">Alarm Type：</label
          ><span>{{ showAlert.alg_ch_name }}</span>
        </li>
        <li>
          <label for="">Alarm Level：</label
          ><span>{{ showAlert.hazard_level }}</span>
        </li>
        <li>
          <label for="">Source：</label><span>{{ showAlert.source_desc }}</span>
        </li>
        <li>
          <label for="">Device Name：</label
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
        <div
          class="faceInfo"
          v-if="
            showAlert.reserved_data.face &&
            showAlert.reserved_data.face.length > 0
          "
        >
          <ul>
            <li
              v-for="(infos, index) in showAlert.reserved_data.face"
              :key="index + Math.random()"
            >
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
import { alertApis } from "@/api/alert.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["alertList"],
  data() {
    return {
      projConfig: projConfig,
      showAlert: {
        show: false,
        id: "",
        alert_time: "",
        source_desc: "",
        hazard_level: "",
        device_id: "",
        device_name: "",
        imgpath: "",
        status: 0,
        reserved_data: {},
      },
    };
  },
  methods: {
    handleShowAlertDetail(item) {
      this.showAlert.show = true;
      this.showAlert = Object.assign(this.showAlert, item);
      this.showAlert.reserved_data =
        JSON.stringify(this.showAlert.reserved_data) == "{}"
          ? {}
          : JSON.parse(this.showAlert.reserved_data);
    },
    handleAlertDeal(row) {
      this.$confirm("Please confirm this alarm？", "Alarm Confirm", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
        customClass: "home-confirm",
      })
        .then(() => {
          alertApis
            .deal({
              condition: {
                alert_ids: [row.id],
                device_id: row.device_id,
              },
              status: 2,
            })
            .then((res) => {
              row.status = 2;
              let alertInfo = this.$props.alertList.find(
                (item) => item.id == row.id
              );
              alertInfo.status = 2;
              this.$message({
                type: "success",
                message: res.message[this.lang],
              });
            })
            .catch((err) => {
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              }
            });
        })
        .catch((cancel) => {});
    },
    handleAlertStatusRestet(row) {
      this.$confirm("Do you cancel this alarm？", "Cancel confirmation", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
        customClass: "home-confirm",
      })
        .then(() => {
          alertApis
            .deal({
              condition: {
                alert_ids: [row.id],
                device_id: row.device_id,
              },
              status: 1,
            })
            .then((res) => {
              row.status = 1;
              let alertInfo = this.$props.alertList.find(
                (item) => item.id == row.id
              );
              alertInfo.status = 1;
              this.$message({
                type: "success",
                message: res.message[this.lang],
              });
            })
            .catch((err) => {
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              }
            });
        })
        .catch((cancel) => {});
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
      border-top: 2rem solid rgba(11 111 193 / 0.5);
      line-height: 2;
      padding: 8rem 10rem;
      display: flex;
      justify-content: space-between;
      color: @mainText;
      font-size: 14rem;
      background-color: rgb(11 111 193 / 20%);
      margin-bottom: 10rem;
      position: relative;
      .info-list {
        width: calc(73% - 20rem);
        p {
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          word-break: break-all;
          width: 95%;
          color: #a3d7dd;
        }
      }
      .btn-deal,
      .btn-pending {
        position: absolute;
        bottom: 10rem;
        right: 10rem;
        color: #fff;
        padding: 2rem 8rem;
        border-radius: 2px;
        display: flex;
        align-items: center;
        z-index: 1;
        cursor: pointer;
        i {
          margin-right: 2rem;
        }
      }
      .btn-deal {
        background-color: #11b111;
      }
      .btn-pending {
        background-color: #e6a23c;
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
.home-alarmDetail-dialog {
  .el-dialog {
    background-color: #0e1828;
    border: 1px solid #1a3157;
    border-radius: 4px;
    .el-dialog__header {
      border-bottom: none;
      text-align: center;
      .el-dialog__title {
        color: #00e2fd;
      }
      .el-dialog__headerbtn {
        top: 20rem;
        right: 20rem;
      }
    }
    .el-dialog__body {
      overflow: visible;
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
        color: #659ea5;
      }
      span {
        color: #a3d7dd;
      }
    }
  }

  .btn-deal,
  .btn-pending {
    position: absolute;
    top: 10rem;
    right: 20rem;
    color: #fff;
    padding: 5rem 8rem;
    border-radius: 2px;
    display: flex;
    align-items: center;
    z-index: 1;
    font-size: 14rem;
    cursor: pointer;
    i {
      margin-right: 2rem;
    }
  }
  .btn-deal {
    background-color: #11b111;
  }
  .btn-pending {
    background-color: #e6a23c;
  }
}
.home-alarmDetail-dialog.commondetail {
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
.home-alarmDetail-dialog.facedetail {
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
      color: #a3d7dd;
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