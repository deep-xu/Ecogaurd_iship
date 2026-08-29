<template>
  <div class="service-page" style="position: relative">
    <div class="pannel network">
      <div class="pannel-header" style="position: relative">
        <h3>Networking status</h3>
        <span
          class="networkStatus"
          :class="{
            online: network,
            offline: !network,
          }"
          >{{ network ? "Online" : "Offline" }}</span
        >
      </div>
      <div
        @click="handleDeviceRestart"
        style="position: absolute; right: 25rem; z-index: 1; top: 13rem"
      >
        <el-tooltip content="Reboot" placement="top" style="left: 7px">
          <i class="z-icon-quit" style="cursor: pointer"></i>
        </el-tooltip>
      </div>
    </div>
    <div class="pannel setsink">
      <div class="pannel-header"><h3>sink server setting</h3></div>
      <div class="pannel-body" style="position: relative">
        <label>sink count：</label>
        <el-input-number v-model="sinkNum" :min="1" :max="10"></el-input-number>
        <el-button
          type="primary"
          :loading="saveloading"
          :disabled="saveloading"
          @click="handleSaveSinknum"
          >Save</el-button
        >
      </div>
    </div>
    <div class="pannel server">
      <div class="pannel-header"><h3>Server Status</h3></div>
      <div class="pannel-body" style="position: relative">
        <ul>
          <li v-for="item in server" :key="item.index">
            <div class="label">{{ item.key }}</div>
            <div
              class="status"
              :class="{
                online: item.value == 1,
                offline: item.value == 0,
                error: item.value == 2,
              }"
            >
              <i class="z-iconfont z-icon-zhuangtai"></i
              >{{
                item.value == 1 ? "Online" : item.value == 0 ? "Offline" : "Error"
              }}
            </div>
            <div class="button">
              <el-button
                v-if="item.key != 'sink'"
                @click="handleRestart(item)"
                class="button"
                type="primary"
                :loading="item.loading"
                :disabled="item.loading"
                >Reboot</el-button
              >
              <el-button
                v-if="item.key == 'sink'"
                @click="sinkList.show = true"
                class="button"
                type="primary"
                >Details</el-button
              >
            </div>
          </li>
        </ul>
      </div>
    </div>
    <el-drawer
      title=""
      :visible.sync="sinkList.show"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
      :modal="false"
    >
      <div class="sinkList">
        <ul>
          <li v-for="item in sinkList.list" :key="item.index">
            <div class="label">{{ item.key }}</div>
            <div
              class="status"
              :class="{
                online: item.value == 1,
                offline: item.value == 0,
                error: item.value == 2,
              }"
            >
              <i class="z-iconfont z-icon-zhuangtai"></i
              >{{
                item.value == 1 ? "Online" : item.value == 0 ? "Offline" : "Error"
              }}
            </div>
            <div class="button">
              <el-button
                @click="handleRestart(item)"
                class="button"
                type="primary"
                :loading="item.loading"
                :disabled="item.loading"
                >Reboot</el-button
              >
            </div>
          </li>
        </ul>
      </div>
    </el-drawer>
  </div>
</template>
  
<script>
import {
  restart_dashboard,
  restart_sink,
  restart_monitor,
  restart_stream,
  sink_number,
  reboot,
  getNetwork,
  getServer,
} from "@/api/restart.js";
import bus from "@/utils/bus.js";
import mqttmixin from "@/utils/mqttmixin.js";
import { Message } from "element-ui";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [mqttmixin, langmixin],
  data() {
    return {
      saveloading: false,
      sinkNum: 3,
      network: 0,
      server: [
        {
          name: "",
          key: "dashboard",
          value: 0,
          index: 0,
          loading: false,
        },
        { name: "", key: "monitor", value: 0, index: 1, loading: false },
        { name: "", key: "stream", value: 0, index: 2, loading: false },
        { name: "", key: "sink", value: 0, index: 3, loading: false },
      ],
      sinkList: {
        show: false,
        list: [],
      },
    };
  },
  mounted() {
    this.getSinkNum();
    getNetwork().then((network) => {
      this.network = network.data;
    });
    this.connectMqtt();
    this.mqttSubscribe();
  },
  beforeDestroy() {},
  methods: {
    mqttSubscribe() {
      this.mqttclient.subscribe("ks/will/+", { qos: 2 }, (error) => {
        if (error) {
          console.log("subscribe error:", error);
          return;
        }
      });
      this.mqttclient.subscribe("ks/proxy/message", { qos: 2 }, (error) => {
        if (error) {
          console.log("subscribe error:", error);
          return;
        }
      });
      this.mqttclient.on("message", (topic, payload) => {
        let msg = JSON.parse(payload.toString());
        if (topic.indexOf("ks/will") >= 0) {
          if (msg.type == "proxy") {
            let key = topic.replace("ks/will/proxy_", "");
            if (key.indexOf("sink_") >= 0) {              
              for (let i = 0; i < this.sinkList.list.length; i++) {
                if (this.sinkList.list[i].key == key) {
                  this.sinkList.list[i].value = msg.status;
                }
              }
            } else {
              for (let i = 0; i < this.server.length; i++) {
                if (this.server[i].key == key) {
                  if (this.server[i].loading == true && msg.status == 1) {
                    this.server[i].loading = false;
                  }
                  this.server[i].value = msg.status;
                }
              }
            }
          }
          let sinkOnline = this.sinkList.list.filter((item) => item.value == 1);
          let proxy_sink = this.server.find((item) => item.key == "sink");
          if (sinkOnline.length == this.sinkList.list.length) {
            proxy_sink.value = 1;
          } else if (sinkOnline.length == 0) {
            proxy_sink.value = 0;
          } else {
            proxy_sink.value = 2;
          }
        }
        if (topic == "ks/proxy/message" && msg.msg_type == "network") {
          this.network = msg.data;
        }
      });
    },
    getSinkNum() {
      sink_number
        .get()
        .then((res) => {
          this.sinkList.list = [];
          this.sinkNum = res.data;
          for (let i = 0; i < this.sinkNum; i++) {
            this.sinkList.list.push({
              name: "",
              key: "sink_" + i,
              value: 0,
              index: i,
              loading: false,
            });
          }
          getServer().then((server) => {
            for (let i = 0; i < this.server.length; i++) {
              if (this.server[i].key != "sink") {
                this.server[i].value = server[this.server[i].key][0];
              }
            }
            for (let i = 0; i < this.sinkList.list.length; i++) {
              this.sinkList.list[i].value =
                server[this.sinkList.list[i].key][0];
            }
            let sinkOnline = this.sinkList.list.filter(
              (item) => item.value == 1
            );
            let proxy_sink = this.server.find((item) => item.key == "sink");
            if (sinkOnline.length == this.sinkList.list.length) {
              proxy_sink.value = 1;
            } else if (sinkOnline.length == 0) {
              proxy_sink.value = 0;
            } else {
              proxy_sink.value = 2;
            }
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
    },
    handleRestart(item) {
      let api = null,
        params = null,
        msg = "",
        index = item.index;
      let key = item.key;
      if (item.key.indexOf("sink_") >= 0) {
        key = "sink";
      }
      switch (key) {
        case "dashboard":
          api = restart_dashboard;
          msg = "Are you sure to restart dashboard？";
          break;
        case "monitor":
          api = restart_monitor;
          msg = "Are you sure to restart monitor？";
          break;
        case "stream":
          api = restart_stream;
          msg = "Are you sure to restart stream？";
          break;
        case "sink":
          api = restart_sink;
          params = {
            sink_id: index,
          };
          msg = `Are you sure to restart ${item.key}？`;
          break;
        default:
          break;
      }
      this.$confirm(msg, "Reboot", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          if (key == "sink") {
            this.sinkList.list[index].loading = true;
          } else {
            this.server[index].loading = true;
          }

          api &&
            api(params)
              .then((res) => {
                if (key == "sink") {
                  this.sinkList.list[index].loading = false;
                } else {
                  this.server[index].loading = false;
                }
              })
              .catch((err) => {
                if (key == "sink") {
                  this.sinkList.list[index].loading = false;
                } else {
                  this.server[index].loading = false;
                }
                if (err.message) {
                  Message.closeAll();
                  this.$message({
                    type: "error",
                    message: err.message[this.lang],
                  });
                }
              });
        })
        .catch((err) => {});
    },

    handleSaveSinknum() {
      this.$confirm("After modifying the sink quantity, the system will restart. Are you sure you want to confirm the modification？", "Modify", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          this.saveloading = true;
          sink_number
            .save({ number: this.sinkNum })
            .then((res) => {
              this.saveloading = false;
              if (this.sinkList.list.length < this.sinkNum) {
                let length = this.sinkNum - this.sinkList.list.length;
                for (let i = 0; i < length; i++) {
                  this.sinkList.list.push({
                    name: "",
                    key: "sink_" + this.sinkList.list.length,
                    value: 0,
                    index: this.sinkList.list.length,
                    loading: false,
                  });
                }
              } else if (this.sinkList.list.length > this.sinkNum) {
                let delLen = this.sinkList.list.length - this.sinkNum;
                for (let i = 0; i < delLen; i++) {
                  this.sinkList.list.pop();
                }
              }

              let sinkOnline = this.sinkList.list.filter((item) => item.value == 1);
              let proxy_sink = this.server.find((item) => item.key == "sink");
              if (sinkOnline.length == this.sinkList.list.length) {
                proxy_sink.value = 1;
              } else if (sinkOnline.length == 0) {
                proxy_sink.value = 0;
              } else {
                proxy_sink.value = 2;
              }
              this.$message({
                type: "success",
                message: res.message[this.lang],
              });
              setTimeout(() => {
                reboot().catch((err) => {
                  bus.$emit("sys-reboot");
                });
              }, 1000);
            })
            .catch((err) => {
              this.saveloading = false;
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              }
            });
        })
        .catch((err) => {});
    },

    handleDeviceRestart() {
      this.$confirm("Are you sure to restart?", "Reboot", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          reboot().catch((err) => {
            bus.$emit("sys-reboot");
          });
        })
        .catch((err) => {});
    },
  },
};
</script>
  
  <style lang="less" scoped>
.service-page {
  width: 100%;
  height: calc(100% - 20rem);
}
.pannel {
  width: 100%;
  .pannel-header {
    height: 40rem;
    padding-left: 20rem;
    display: flex;
    align-items: center;

    h3 {
      position: relative;
      padding-left: 12rem;
      font-weight: normal;
      font-size: 16rem;
    }

    h3:before {
      content: "";
      display: block;
      width: 3rem;
      height: 16rem;
      background-color: #ffa41c;
      position: absolute;
      left: 0;
      top: 3rem;
    }
  }

  .pannel-body {
    height: calc(100% - 40rem);
    width: 100%;
    .chart {
      width: 100%;
      height: 100%;
    }
  }
}
.pannel.network {
  height: 50rem;
  .networkStatus {
    margin-left: 20px;
    font-size: 16rem;
  }
  .networkStatus.online {
    color: rgb(32, 218, 32);
  }
  .networkStatus.offline {
    color: rgb(255, 24, 24);
  }
}
.pannel.setsink {
  height: 100rem;
  .pannel-body {
    display: flex;
    align-items: center;
    label {
      font-size: 14rem;
      margin-left: 20rem;
    }
    .el-input-number {
      height: 40rem;
      line-height: 40rem;
      width: 150rem;
      margin-right: 20rem;
    }
  }
}
.pannel.server,
.sinkList {
  height: calc(100% - 50rem);
  overflow: auto;
  ul {
    display: flex;
    flex-wrap: wrap;
    padding-left: 20rem;
    li {
      width: calc((100% - 60rem) / 3);
      height: 60rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      // background-color: #2f3037;
      border: 1px solid #2c6dd2;
      box-sizing: border-box;
      margin-bottom: 20rem;
      margin-right: 20rem;
      padding: 0 20rem;
      border-radius: 6rem;
      font-size: 14rem;
      .label {
        width: 60rem;
      }
      .status {
        margin-left: -10rem;
      }
      .button {
        width: 60rem;
        padding: 0 !important;
      }
      .status {
        display: flex;
        align-items: center;
      }
      .status i {
        margin-right: 5rem;
      }
      .online {
        color: rgb(32, 218, 32);
      }
      .offline {
        color: rgb(255, 24, 24);
      }
      .error {
        color: orange;
      }
    }
    li.transparant {
      background-color: transparent !important;
    }
  }
}
.sinkList {
  ul {
    li {
      width: 100%;
    }
  }
}
</style>
  