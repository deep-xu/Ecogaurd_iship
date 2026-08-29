<template>
  <div class="sidebar">
    <!-- <div class="logo">
      <router-link class="sidebar-logo-link" to="/">
        <img src="../../assets/img/logo.png" class="sidebar-logo" />
        <h1 v-show="!isCollapse" class="sidebar-title" style="margin-top:20rem">
          <img src="../../assets/img/neisou.png" alt="" style="width:70rem">
        </h1>
      </router-link>
    </div>
    <img class="divide" src="../../assets/img/divide.png" alt="" /> -->
    <el-menu
      class="sidebar-el-menu"
      :default-active="onRoutes"
      :collapse="isCollapse"
      :unique-opened="true"
      :collapse-transition="false"
      menu-trigger="hover"
      router
      ref="sidebar-el-menu"
    >
      <sidebar-item v-for="menu in menuArr" :key="menu.index" :item="menu" />
    </el-menu>
    <!-- <div class="chang-theme" @click="handleChangeTheme">
      <i class="z-icon-night" v-show="!this.theme"></i>
      <i class="z-icon-Daytimemode" v-show="this.theme"></i>
      <span>切换主题</span>
    </div> -->
    <div class="sidebar-resize" @click="handleCollapseChange(!isCollapse)">
      <i class="el-icon-arrow-left" v-show="!isCollapse"></i>
      <i class="el-icon-arrow-right" v-show="isCollapse"></i>
    </div>

    <!-- <div class="bottom-menu">
      <a href="/"><i class="z-icon-home"></i>返回主页</a>
      <a href="/#/search"><i class="z-icon-menu-search"></i>融合搜索</a>
    </div> -->
  </div>
</template>

<script>
import bus from "@/utils/bus";
import SidebarItem from "./SidebarItem";
import { mapGetters } from "vuex";
import { setTheme } from "@/assets/css/theme.js";
export default {
  name: "vSidebar",
  components: { SidebarItem },
  data() {
    return {
      // isCollapse: false,
      menuItems: [],
      menuArr: [
        {
          icon: "qiye-menu-home",
          index: "index",
          title: "检测概况",
          children: [],
        },
      ],

      theme: false, // true: 默认黑色主题， false: 白色主题
    };
  },
  watch: {
    $route: {
      deep: true,
      handler(newVal, oldVal) {
        this.getMenuData();
      },
    },
  },
  methods: {
    getMenuData() {
      this.menuArr = JSON.parse(JSON.stringify(this.menuTree));
    },
    handleChangeTheme() {
      this.theme = !this.theme;
      let theme = this.theme == true ? "black" : "white";
      localStorage.setItem("theme", theme);
      setTheme(theme);
      bus.$emit("chgtheme");
    },
    handleCollapseChange(collapse) {
      this.$store.commit("app/setCollapse", collapse);
    },
  },
  computed: {
    onRoutes() {
      return this.$route.path.replace("/", "");
    },
    ...mapGetters("menu", {
      menuTree: "menuTree",
    }),
    ...mapGetters("app", {
      isCollapse: "isCollapse",
    }),
  },
  created() {},
  mounted() {
    this.getMenuData();
    let theme = localStorage.getItem("theme");
    this.theme = theme == "black" ? true : false;
  },
};
</script>

<style scoped lang="less">
.chang-theme {
  position: absolute;
  bottom: 20rem;
  height: 40rem;
  line-height: 40rem;
  width: 100%;
  padding-left: 22rem;
  font-size: 16rem;
  cursor: pointer;
  color: @menuText;
  box-sizing: border-box;
}
.chang-theme span {
  margin-left: 10rem;
}
.collapse .chang-theme {
  display: flex;
  justify-content: center;
  padding-left: 0;
}
.collapse .chang-theme span {
  display: none;
}
.divide {
  width: calc(100% - 40rem);
  height: 1rem;
  margin: 1rem 20rem 0;
  position: absolute;
}
.sidebar-el-menu{
  // height: calc(100% - 140rem) !important;
  
}
.sidebar-el-menu::-webkit-scrollbar-thumb { //滚动条的设置
    background-color: rgb(136 136 136 / 50%);
}
.bottom-menu{
  width: 100%;
  position: absolute;
  bottom: 10rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  a{
    color: #fff;
    font-size: 16rem;
    margin-bottom: 20rem;
    text-align: center;
    text-decoration: none;
    width: 80%;
    border-radius: 100rem;
    background-color: #2c6dd2;
    height: 35rem;
    display: flex;
    justify-content: center;
    align-items: center;
    i{
      margin-right: 5rem;
    }
    .z-icon-menu-search{
      font-size: 15rem;
    }
    &:hover{
      color: #fff;
    }
  }
}
</style>
