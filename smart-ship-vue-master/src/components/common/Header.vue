<template>
  <div class="header">
    <!-- <div class="bread">
      <i
        v-show="!isCollapse"
        @click="handleCollapseChange(true)"
        class="z-icon-zhedie"
      ></i>
      <i
        v-show="isCollapse"
        @click="handleCollapseChange(false)"
        class="z-icon-zhankai"
      ></i>
      {{ title }}
    </div> -->
    <div class="logo" style="align-items: center;">
      <router-link class="header-logo-link" to="/">
        <img src="../../assets/img/ship_icon.png" class="header-logo" />
      </router-link>
    </div>

    <div class="right-btns">
      <!-- <div class="word">
        <span style="font-size: 14px">文档</span>
        <div class="dropdown">
          <ul>
            <li style="width: 170px">
              <a :href="'内搜系统-功能介绍.pdf'" target="_blank"
                ><i class="z-icon-drive-pdf"></i>内搜系统-功能介绍</a
              >
            </li>
            <li style="width: 170px">
              <a :href="'内搜系统-使用手册.pdf'" target="_blank"
                ><i class="z-icon-drive-pdf"></i>内搜系统-使用手册</a
              >
            </li>
          </ul>
        </div>
      </div> -->
      <!-- <div class="support">
        <span style="font-size: 14px">支持</span>
        <div class="dropdown">
          <ul>
            <li style="width: 140px" class="phonenumber">
              <i class="z-icon-phone"></i> 400-1386-808
            </li>
          </ul>
        </div>
      </div> -->
      <div class="user">
        <span style="font-size: 16rem">{{ username }}</span>
        <!-- <i class="z-icon-user"></i> -->
        <div class="dropdown">
          <ul>
            <li @click="chgPwd.show = true">修改密码</li>
          </ul>
        </div>
      </div>
      <span style="font-size:18rem">|</span>
      <div style="display: flex; align-items: center;margin: 0 20rem;cursor:pointer;" @click="handleQuit">
        <i class="z-icon-tuichu"></i>
      </div>
    </div>

    <el-dialog title="修改密码" :append-to-body="true" :visible.sync="chgPwd.show" class="chgpwd-dialog" v-dialogDrag
      width="600px">
      <chgpwd v-if="chgPwd.show" @cancel-chgpwd="chgPwd.show = false"></chgpwd>
    </el-dialog>

  </div>
</template>

<script>
import { mapGetters } from "vuex";
import { projConfig } from "@/utils/config.js";
import { user } from "@/api/user.js";
import { resetTokenAndClearUser } from "@/utils/index.js";
import { customLogo } from "@/api/system.js"
import md5 from "js-md5";
export default {
  props: ['istubo'],
  data() {
    let validatePwd = (rule, value, callback) => {
      var reg = new RegExp(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]{6,16}$"
      );
      if (reg.test(value)) {
        callback();
      } else {
        callback(new Error());
      }
    };
    return {
      title: "",
      getTimeInterval: null,
      picPrev: projConfig.picPrev,
      username: "",
      permission: 30,
      logoIcon:'',
      chgPwd: {
        show: false,
        newPwd: "",
        newPwd2: "",
      },
      formRules: {
        newPwd: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          {
            validator: validatePwd,
            trigger: "blur",
            message:
              "请输入6~16个字符，至少一个大写字母，一个小写字母和一个数字",
          },
        ],
        newPwd2: [
          { required: true, message: "请再次输入", trigger: "blur" },
          {
            validator: validatePwd,
            trigger: "blur",
            message:
              "请输入6~16个字符，至少一个大写字母，一个小写字母和一个数字",
          },
          {
            validator: (rule, value, callback) => {
              if (this.chgPwd.newPwd == this.chgPwd.newPwd2) {
                callback();
              } else {
                callback(new Error());
              }
            },
            trigger: "blur",
            message: "两次输入密码不一致",
          },
        ],
      },
    };
  },
  computed: {
    ...mapGetters("app", {
      isCollapse: "isCollapse",
    }),
  },
  watch: {
    $route: {
      deep: true,
      handler(newVal, oldVal) {
        this.getTitle();
      },
    },
  },
  mounted() {
    this.username = localStorage.getItem("user");
    this.permission = localStorage.getItem("permission")
    this.getTitle();
    // customLogo.get().then(res => {
    //   this.logoIcon = res.data.icon ?  "data:image/jpg;base64," + res.data.icon : '../../assets/img/ship_icon.png'
    // })
    // this.getTime();
    // this.getTimeInterval = setInterval(this.getTime,1000)
  },
  beforeDestroy() {
    clearInterval(this.getTimeInterval);
    this.getTimeInterval = null;
  },
  methods: {
    getTitle() {
      let key = this.$route.path.split("/").pop();
      let menuInfo = this.$store.state.menu.menuObj[key];
      if (menuInfo) {
        this.title = menuInfo.title;
      }

    },
    getTime() {
      let time = new Date().format("YYYY-MM-DD HH:mm:ss");
      let weeks = ["日", "一", "二", "三", "四", "五", "六"];
      let day = new Date().getDay();
      this.title = `${time}<span style="margin-left: 10rem">星期${weeks[day]}</span>`;
    },
    handleCollapseChange(collapse) {
      this.$store.commit("app/setCollapse", collapse);
    },
    handleQuit() {
      resetTokenAndClearUser();
      this.$router.push("/login");
    },
  },
};
</script>

<style lang="less" scoped>
.header {
  background-color: #fff;
  height: 60rem;
  line-height: 60rem;
  width: 100%;
  min-width: 1200px;
  box-sizing: border-box;
  display: flex;
  justify-content: space-between;
  color: @mainText;
  z-index: 3;
  position: relative;
  box-shadow: 0 4px 6px 0 rgb(8 14 26 / 4%), 0 1px 10px 0 rgb(8 14 26 / 5%),
    0 2px 4px -1px rgb(8 14 26 / 6%);

  .word,
  .support,
  .user {
    position: relative;
    padding: 0 20rem 0 20rem;
    cursor: pointer;

    .z-icon-user {
      font-size: 25rem;
      color: @mainText;
      // cursor: pointer;
    }
  }

  .word:hover span,
  .support:hover span,
  .user:hover span {
    color: @mainText;
  }

  .support:hover .dropdown,
  .word:hover .dropdown,
  .user:hover .dropdown {
    display: block;
    width: fit-content;
  }

  .dropdown {
    display: none;
    position: absolute;
    top: 60rem;
    z-index: 3;
    right: 0;
    background: @selDropdownBg;
    min-width: 100rem;
    text-align: center;
    font-size: 14rem;
    // box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
    box-shadow: 0 2px 3px rgb(25 35 60 / 10%);
    // border-radius: 4px;
    border: 1px solid #ebebeb;
    cursor: pointer;

    li a {
      color: @mainText;
      text-decoration: none;
    }

    li a:hover {
      color: @mainText;
    }

    li a:active {
      color: @mainText;
    }

    li:hover {
      // background-color: @menuHoverBg;
      color: @mainText;
    }

    li.phonenumber:hover {
      color: @mainText;
    }

    ul li:first-child {
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
    }

    ul li:last-child {
      border-bottom-left-radius: 4px;
      border-bottom-right-radius: 4px;
    }

    .z-icon-drive-pdf {
      font-size: 14px;
      margin-right: 2px;
    }
  }
}

.right-btns {
  display: flex;
}

.quit {
  border-left: 1px solid #f5f5f5;
  width: 60px;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;

  .z-icon-hkquit {
    font-size: 24rem;
    color: #333;
  }
}

</style>