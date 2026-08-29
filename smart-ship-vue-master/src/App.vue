<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>
<script>
import { setTheme } from "@/assets/css/theme.js";
import mqttmixin from "@/utils/mqttmixin.js";
import { getconfig } from "@/api/common.js";
import publicinfo from "@/utils/publicinfo.js";
import bus from "@/utils/bus.js";
import { Message } from "element-ui";
export default {
  mixins: [mqttmixin, publicinfo],
  data() {
    return {
      restarting: false,
      msgTimeout: null,
      mqttclient: null,
      errmsg: "",
      restart_time: 5000,
    };
  },
  beforeMount() {
    // this.setStreamPort().then(() => {
      this.connectMqtt();
      this.mqttSubscribe();
    // });

    // getconfig().then((res) => {
    //   this.restart_time = res.web_params.restart_time;
    // });
  },
  mounted() {
    // let theme = localStorage.getItem("theme");
    // if (theme) {
    //   setTheme(theme);
    // } else {
    setTheme("white");
    // }
    bus.$on("login-success", () => {
      // this.setStreamPort().then(() => {
        this.connectMqtt();
        this.mqttSubscribe();
      // });
    });
    bus.$on("sys-reboot", () => {
      Message.closeAll();
      setTimeout(() => {
        this.restarting = true;
        //离线
        this.errmsg = "重启中，请稍候...";
        this.msgTimeout = this.$message({
          type: "info",
          message: this.errmsg,
          duration: 0,
          showClose: true,
          onClose: () => {
            this.errmsg = "";
            this.msgTimeout = null;
          },
        });
      }, 500);
    });
  },
  methods: {
    mqttSubscribe() {
      this.mqttclient.subscribe("ks/will/+", { qos: 2 }, (error) => {
        if (error) {
          console.log("subscribe error:", error);
          return;
        }
        // console.log("订阅will成功");
      });

      this.mqttclient.on("message", (topic, payload) => {
        let msg = JSON.parse(payload.toString());
        if (
          topic == "ks/will/proxy_dashboard" &&
          msg.status == 1 &&
          this.restarting == true
        ) {
          //上线
          setTimeout(() => {
            Message.closeAll();
            setTimeout(() => {
              this.$message({
                type: "success",
                message: "后台已启动",
              });
            }, 500);
          }, this.restart_time - 2000);

          setTimeout(() => {
            window.location.reload();
          }, this.restart_time);
        } else if (
          topic == "ks/will/proxy_dashboard" &&
          msg.status == 0 &&
          !this.restarting
        ) {
          this.restarting = true;
          //离线
          this.errmsg = "重启中，请稍候...";
          this.msgTimeout = this.$message({
            type: "info",
            message: this.errmsg,
            duration: 0,
            showClose: true,
            onClose: () => {
              this.errmsg = "";
              this.msgTimeout = null;
            },
          });
        }
      });
    },
  },
};
</script>
<style lang="less" scoped>
</style>