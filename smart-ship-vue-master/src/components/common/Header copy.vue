<template>
  <div class="header">
    <h3><span>智慧矿山监控平台</span></h3>
    <div class="right-container">
      <div class="username">
        <i class="icon-user"></i>
        <span>{{ username }}</span>
        |
      </div>
      <div style="display: flex; align-items: center" @click="handleQuit">
        <i class="z-icon-tuichu"></i>
      </div>
    </div>
  </div>
</template>

<script>
import { resetTokenAndClearUser } from "@/utils/index.js";
export default {
  data() {
    return {
      username: localStorage.getItem("user"),
    };
  },
  mounted() {},
  methods: {
    handleQuit() {
      resetTokenAndClearUser();
      this.$router.push("/login");
    },
  },
};
</script>

<style lang="less" scoped>
.header {
  width: 100%;
  min-width: 1200px;
  height: 60rem;
  background-color: #fff;
  position: relative;
  h3 {
    width: fit-content;
    height: 100%;
    display: flex;
    align-items: center;
    font-size: 40rem;
    letter-spacing: 2rem;
    color: #00e2fd;
    position: relative;
    span {
      transform: scale(1, 0.8);
      text-shadow: 2px 4px 6px #000000;
    }
  }

  .right-container {
    display: flex;
    color: #333;
    position: absolute;
    right: 30rem;
    height: 100%;
    align-items: center;
    top: 0;

    .username {
      display: flex;
      align-items: center;
      font-size: 18rem;
      margin-right: 30rem;
      cursor: default;
      i {
        margin-right: 10rem;
      }
      span {
        margin-right: 30rem;
      }
    }
    .z-icon-tuichu {
      font-size: 24rem;
      cursor: pointer;
    }
  }
}
</style>