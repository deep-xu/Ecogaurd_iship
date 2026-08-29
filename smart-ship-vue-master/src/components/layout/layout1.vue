<template>
  <div class="layout1">
    <router-view :key="key"></router-view>
  </div>
</template>

<script>
import { user } from "@/api/user.js"
import { projConfig } from "@/utils/config";
import { resetTokenAndClearUser } from "@/utils/index.js";
export default {
  data() {
    return {
      getTokenInterval: null,
    };
  },
  computed: {
    key() {
      return this.$route.path;
    },
  },
  methods: {
    // 更新token
    handleMonitorPageActive() {
      // 监控页面是否活动
      window.addEventListener("mousemove", this.recordActiveTime);
      window.addEventListener("visibilitychange", this.visibilitychange);
    },
    recordActiveTime(e) {
      localStorage.setItem("active", new Date().getTime());
      if (!this.getTokenInterval && document.visibilityState == "visible") {
        // this.updateToken();
        this.tokenInterval();
      }
    },
    updateToken() {
      if (localStorage.getItem("t")) {
        user
          .updateToken()
          .then((res) => {
            localStorage.setItem("t", res.data);
          })
      } else {
        this.logout();
      }
      // this.getTokenInterval = setInterval(() => {
      //   let activeTime = localStorage.getItem("active");
      //   if (activeTime && new Date().getTime() - activeTime < 5 * 60 * 1000) {
      //     // 5分钟不活动停止更新token
      //     // 更新token
      //     if (localStorage.getItem("t")) {
      //       user.updateToken().then((res) => {
      //         localStorage.setItem("t", res.data);
      //       });
      //     }
      //   } else {
      //     console.log("清除获取token定时器", new Date().getTime());
      //     clearInterval(this.getTokenInterval);
      //     this.getTokenInterval = null;
      //   }
      // }, 2 * 60 * 1000); // 2分钟刷新token
    },
    tokenInterval() {
      this.getTokenInterval = setInterval(() => {
        this.updateToken();
      }, 5 * 60 * 1000); // 5分钟刷新token
    },
    visibilitychange() {
      if (document.visibilityState == "visible") {
        this.updateToken();
      }
    },
    logout(){
      clearInterval(this.getTokenInterval);
      this.getTokenInterval = null;
      this.$router.push("/login");
    }
  },
  beforeCreate() {},
  beforeMount(){
  },
  mounted() {
    // this.updateToken()
    // 更新token
    // this.handleMonitorPageActive();
    // this.tokenInterval();

    // if (process.env.NODE_ENV != "development") {
    //   document.onkeydown = function () {
    //     if (window.event && window.event.keyCode == 123) {
    //       //alert("F12被禁用");
    //       return false;
    //     }
    //   };
    // }
  },
  beforeDestroy() {
    // resetTokenAndClearUser();
    clearInterval(this.getTokenInterval);
    this.getTokenInterval = null;
    window.removeEventListener("mousemove", this.recordActiveTime);
    window.removeEventListener("visibilitychange", this.visibilitychange);
  },
};
</script>
<style scoped lang="less">
.layout1 {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow-x: auto;
  overflow-y: hidden;
}
/* 滚动条 */
::-webkit-scrollbar-track-piece {
  background-color: #fff;
}

::-webkit-scrollbar {
  //滚动条的宽度
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-thumb {
  //滚动条的设置
  background-color: @menuText;
  min-height: 28px;
}
</style>
