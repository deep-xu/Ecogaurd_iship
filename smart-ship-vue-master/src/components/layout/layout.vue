<template>
  <div class="layout" :class="{ collapse: isCollapse, showMain: showMain }" >
    <Header></Header>
    <div class="MainContainer">
      <Sidebar></Sidebar>
      <div class="main-container">
        <!-- <div class="subtitle-container">
          {{ title }}
          <span><i class="z-icon-alarm1"></i>7天即将到期</span>
        </div> -->
        <transition name="fade-transform">
          <router-view :key="key"></router-view>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import Header from "../common/Header";
import Sidebar from "../common/Sidebar";
import { mapGetters } from "vuex";
import { mapActions } from "vuex";
import { user } from "@/api/user.js";
import { projConfig } from "@/utils/config";
import { resetTokenAndClearUser } from "@/utils/index.js";

export default {
  components: { Header, Sidebar },
  data() {
    return {
      title: "",
      backtop: 300,
      showMain: false, // 只显示主页面，不显示侧边和header

      getTokenInterval: null,
    };
  },
  computed: {
    ...mapGetters("dailog", {
      isShow: "isShow",
    }),
    ...mapGetters("app", {
      isCollapse: "isCollapse",
    }),
    key() {
      return this.$route.path;
    },
  },
  watch: {
    $route: {
      deep: true,
      handler(newVal, oldVal) {
        this.getTitle();
      },
    },
  },
  methods: {
    ...mapActions("dailog", ["hideDailog", "showDailog"]),
    ...mapActions("app", ["setUserRole"]),
    handleClose() {
      this.$confirm("确认关闭？")
        .then((_) => {
          this.hideDailog();
          //this.$store.dispatch('dailog/hideDailog')
        })
        .bind(this)
        .catch((_) => {});
    },
    handleScroll(e) {
      let el = document.querySelector("#app");
    },
    getTitle() {
      let key = this.$route.path.split("/").pop();
      let menuInfo = this.$store.state.menu.menuObj[key];
      this.title = menuInfo.title;
      document.title = `${this.title}`;
    },

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
  beforeMount() {
    if (process.env.NODE_ENV != "development") {
      document.onkeydown = function () {
        if (window.event && window.event.keyCode == 123) {
          //alert("F12被禁用");
          return false;
        }
      };
    }
  },
  mounted() {
    // this.updateToken()
    let userRole = localStorage.getItem("role");
    if (userRole == "admin") {
      this.$store.commit("menu/setMenuRole", "admin");
    } else {
      this.$store.commit("menu/setMenuRole", userRole);
    }
    this.getTitle();
    // 更新token
    // this.handleMonitorPageActive();
    // this.tokenInterval();
    
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
.layout {
  width: 100vw;
  height: 100vh;
  position: relative;
}
.MainContainer {
  width: 100vw;
  min-width: 1200px;
  height: calc(100vh - 60rem);
  padding-left: 220rem;
  box-sizing: border-box;
  transition: all 0.3s;
  background-color: @mainBg;
  position: relative;
}

.collapse .MainContainer {
  /* width: calc(100vw - 64rem); */
  padding-left: 10rem;
  transition: all 0.3s;
}
.main-container {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  background-color: @mainBg;
  // padding: 50rem 20rem 8rem;
  // padding: 8rem 20rem 8rem;
  box-sizing: border-box;
}

.main-container > div {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  // border: 2rem solid @MaincontainerBg;
  position: relative;
  overflow: hidden;
  padding: 8rem 8rem 8rem;
}
.main-container .subtitle-container {
  position: absolute;
  top: 0;
  width: calc(100% - 40rem);
  left: 20rem;
  height: 50rem;
  border-bottom: 1rem solid #eceff8;
  font-size: 16rem;
  color: #333;
  height: 50rem;
  line-height: 50rem;
  span {
    position: absolute;
    right: 0;
    top: 0rem;
    font-size: 14rem;
  }
  .z-icon-alarm1 {
    color: red;
    margin-right: 2rem;
  }
}
.showMain .sidebar {
  display: none;
}
.showMain .header {
  display: none;
}
.showMain .MainContainer {
  padding-left: 0;
}
.showMain .main-container {
  height: 100%;
}
.showMain .z-icon-tuichu {
  display: block;
  position: absolute;
  top: 20rem;
  right: 20rem;
  font-size: 28rem;
  cursor: pointer;
  z-index: 111;
  color: #989898;
}
.showMain .z-icon-tuichu:hover {
  color: #666;
}
</style>

<style lang="less">
/* fade */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.28s;
}

.fade-enter,
.fade-leave-active {
  opacity: 0;
}

/* fade-transform */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(-30rem);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30rem);
}
/* breadcrumb transition */
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter,
.breadcrumb-leave-active {
  opacity: 0;
  transform: translateX(20rem);
}

.breadcrumb-move {
  transition: all 0.5s;
}

.breadcrumb-leave-active {
  position: absolute;
}
</style>
