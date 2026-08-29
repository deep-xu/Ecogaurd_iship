<template>
  <div class="home-header">
    <h3><span>{{name}}</span></h3>
    <a href="/#/alarm" class="lintomain">Management</a>
    <div class="right-container">
      <div class="datetime">
        <div class="time">{{ time }}</div>
        <div style="display: flex">
          <div class="date">{{ date }}</div>
          <div class="weekday">{{ weekday }}</div>
        </div>
      </div>
      <div class="username">
        <i class="icon-user"></i>
        <span>{{ username }}</span>
        |
      </div>
      <div style="display:flex;align-items:center;cursor:pointer;" @click="handleQuit">
        <i class="z-icon-tuichu"></i>
      </div>
    </div>
  </div>
</template>

<script>
import { resetTokenAndClearUser } from "@/utils/index.js"
import { customLogo } from "@/api/system.js"
export default {
  data() {
    return {
      // name:'智慧矿山监控平台',
      name:'',
      date: "",
      time: "",
      weekday: "",
      username: localStorage.getItem("user"),
    };
  },
  mounted() {
    this.getDateTime();
    setInterval(() => {
      this.getDateTime();
    }, 1000);
    customLogo.get().then(res => {
      this.name = res.data.name
    })
  },
  methods: {
    getDateTime() {
      let weekday = [
        "Sun.",
        "Mon.",
        "Tues.",
        "Wed.",
        "Thur.",
        "Fri.",
        "Sat.",
      ];
      let cur = new Date();
      this.time = cur.format("HH:mm:ss");
      this.date = cur.format("YYYY/MM/DD");
      this.weekday = weekday[cur.getDay()];
    },
    handleQuit(){
      resetTokenAndClearUser()
      this.$router.push('/login')
    }
  },
};
</script>

<style lang="less" scoped>
.home-header {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 80rem;
  background: url(../imgs/title-bg.png) no-repeat;
  background-size: 100% 100%;
  background-position: center;
  h3 {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 40rem;
    letter-spacing: 2rem;
    color: #00e2fd;
    margin-top: -10rem;
    position: relative;
    span{
      transform: scale(1.2,1);
      text-shadow:2px 4px 6px #000000;
    }
  }
  .lintomain {
    position: absolute;
    width: 100rem;
    height: 34rem;
    top: 42rem;
    left: 20rem;
    background: url(../imgs/menu.png) no-repeat;
    background-size: 100% 100%;
    cursor: pointer;
    color: #00e2fd;
    font-size: 14rem;
    text-decoration: none;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .lintomain:hover {
    // background-position: 0 0;
    background: url(../imgs/menu-active.png) no-repeat;
    background-size: 100% 100%;
  }
  .right-container {
    display: flex;
    color: #00e2fd;
    position: absolute;
    right: 30rem;
    top: 40rem;

    .datetime {
      font-size: 12rem;
      margin-right:30rem;
      .time {
        font-size: 20rem;
        letter-spacing: 1rem;
        width: 100%;
        text-align: center;
      }
      .date {
        margin-right: 10rem;
      }
    }
    .username {
      display: flex;
      align-items: center;
      font-size: 16rem;
      margin-right:30rem;
      i{
        margin-right:10rem;
      }
      span{
        margin-right:30rem;
      }
    }
  }
}

.icon-user {
  display: block;
  width: 30rem;
  height: 30rem;
  background: url(../imgs/icon-user.png) no-repeat;
  background-size: 100% 100%;
}
.z-icon-tuichu {
  font-size: 26rem;
}
</style>