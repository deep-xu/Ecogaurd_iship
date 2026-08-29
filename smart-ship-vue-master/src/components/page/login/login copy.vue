<template>
  <div class="login-container">
    <div class="some-space">
      <div class="form">
        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          auto-complete="on"
          label-position="left"
        >
          <div class="title-container">
            <h3 class="title">智能管理平台</h3>
          </div>
          <el-form-item prop="username">
            <span class="svg-container">
              <!-- <svg-icon icon-class="user" /> -->
            </span>
            <el-input
              ref="username"
              v-model="loginForm.username"
              placeholder="用户名"
              name="username"
              type="text"
              tabindex="1"
              auto-complete="on"
            />
          </el-form-item>
          <el-form-item prop="password">
            <span class="svg-container">
              <!-- <svg-icon icon-class="password" /> -->
            </span>
            <el-input
              :key="passwordType"
              ref="password"
              v-model="loginForm.password"
              :type="passwordType"
              placeholder="密码"
              name="password"
              tabindex="2"
              auto-complete="on"
              @keyup.enter.native="handleLogin"
            />
            <span class="show-pwd" @click="showPwd">
              <!-- <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" /> -->
            </span>
          </el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            style="width: 100%"
            @click.native.prevent="handleLogin"
            >登录</el-button
          >
        </el-form>
      </div>
    </div>

    <!-- <vue-particles
      color="#6495ED"
      :particleOpacity="0.7"
      :particlesNumber="80"
      shapeType="circle"
      :particleSize="4"
      linesColor="#6495ED"
      :linesWidth="1"
      :lineLinked="true"
      :lineOpacity="0.6"
      :linesDistance="150"
      :moveSpeed="3"
      :hoverEffect="true"
      hoverMode="grab"
      :clickEffect="true"
      clickMode="push"
    >
    </vue-particles> -->
    <bgAnimation />
  </div>
</template>

<script>
import bgAnimation from "./bgAnimation"; // 登录界面背景图动画
import { resetTokenAndClearUser } from "@/utils/index.js";
import { login } from "@/api/system.js";
import md5 from "js-md5";
let test_account = {
  zql: "zhiquli",
  admin: "Admin123",
};
export default {
  name: "Login",
  components: {
    bgAnimation,
  },
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
      },
      loginRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
      loading: false,
      passwordType: "password",
      redirect: undefined,
    };
  },
  mounted() {
    resetTokenAndClearUser();
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true,
    },
  },
  methods: {
    showPwd() {
      if (this.passwordType === "password") {
        this.passwordType = "";
      } else {
        this.passwordType = "password";
      }
      this.$nextTick(() => {
        this.$refs.password.focus();
      });
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          let formdata = new FormData();
          formdata.append("username", this.loginForm.username);
          formdata.append("password", this.loginForm.password);
          login(formdata).then((res) => {
            localStorage.setItem("t", res.token);
            if (this.loginForm.username == "admin") {
              localStorage.setItem("role", "admin");
              this.$store.commit("menu/setMenuRole", "admin");
              localStorage.setItem("permission", 30);
            } else {
              if (res.permission == 20) {
                localStorage.setItem("role", "common");
                this.$store.commit("menu/setMenuRole", "common");
                localStorage.setItem("permission", 20);
              } else {
                localStorage.setItem("permission", 10);
                localStorage.setItem("role", "visit");
                this.$store.commit("menu/setMenuRole", "visit");
              }
            }
            this.loading = true;
            localStorage.setItem("user", this.loginForm.username);
            this.$router.push("/");
          });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    handleLogin2() {
      localStorage.setItem("role", "admin");
      localStorage.setItem("user", "admin");
      this.$router.push("/");
      // this.$refs.loginForm.validate((valid) => {
      //   if (valid) {
      //     if (
      //       this.loginForm.username != "" &&
      //       test_account[this.loginForm.username]
      //     ) {
      //       if (
      //         this.loginForm.password == test_account[this.loginForm.username]
      //       ) {
      //         if (this.loginForm.username == "admin") {
      //           localStorage.setItem("role", "admin");
      //           this.$store.commit("menu/setMenuRole", "admin");
      //         } else {
      //           localStorage.setItem("role", "common");
      //           this.$store.commit("menu/setMenuRole", "common");
      //         }
      //         console.log(this.$store);
      //         this.loading = true;

      //         localStorage.setItem("user", this.loginForm.username);
      //         // this.$store.dispatch('user/login', this.loginForm).then(() => {
      //         // this.$router.push({ path: this.redirect || '/' })
      //         setTimeout(() => {
      //           this.$router.push("/");
      //         }, 100);

      //         // this.loading = false
      //         // }).catch(() => {
      //         //   this.loading = false
      //         // })
      //       } else {
      //         this.$message.error("密码错误");
      //       }
      //     } else {
      //       this.$message.error("密码错误");
      //     }
      //   } else {
      //     console.log("error submit!!");
      //     return false;
      //   }
      // });
    },
  },
};
</script>

<style lang="less">
/* 修复input 背景不协调 和光标变色 */
/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

@bg: #283443;
@light_gray: #fff;
@cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: @cursor)) {
  .login-container .el-input input.el-input--small {
    color: @cursor;
  }
}

/* reset element-ui css */
.login-container {
  .el-input {
    display: inline-block;
    // height: 47px;
    width: 85%;

    input {
      background: transparent !important;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: @light_gray;
      height: 47px;
      caret-color: @cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px @bg inset !important;
        -webkit-text-fill-color: @cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="less" scoped>
@bg: #2d3a4b;
@dark_gray: #889aa4;
@light_gray: #eee;
.form {
  width: 520px;
  height: auto;
  background: rgba(36, 36, 85, 0.5);
  margin: 0 auto;
  padding: 35px 30px 25px;
  box-shadow: 0 0 25px rgba(255, 255, 255, 0.5);
  border-radius: 10px;
}
.login-container {
  height: 100vh;
  width: 100vw;
  background-color: @bg;
  overflow: hidden;
  position: relative;
  .login-form {
    width: 520px;
    max-width: 100%;
    margin: 0 auto;
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: @dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: @light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: @dark_gray;
    cursor: pointer;
  }
}
.some-space {
  color: white;
  font-weight: 100;
  letter-spacing: 2px;
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 1001;
  -webkit-transform: translate3d(-50%, -50%, 0);
  transform: translate3d(-50%, -50%, 0);

  // -ms-animation: cloud 2s 3s ease-in infinite alternate;
  // -moz-animation: cloud 2s 3s ease-in infinite alternate;
  // -webkit-animation: cloud 2s 3s ease-in infinite alternate;
  // -o-animation: cloud 2s 3s ease-in infinite alternate;
  // -webkit-animation: cloud 2s 3s ease-in infinite alternate;
  // animation: cloud 2s 3s ease-in infinite alternate;
}
@-ms-keyframes cloud {
  0% {
    -ms-transform: translate(-50%, -50%);
  }
  40% {
    opacity: 1;
  }
  60% {
    opacity: 1;
  }
  100% {
    -ms-transform: translate(-50%, -40%);
  }
}
@-moz-keyframes cloud {
  0% {
    -moz-transform: translate(-50%, -50%);
  }
  40% {
    opacity: 1;
  }
  60% {
    opacity: 1;
  }
  100% {
    -moz-transform: translate(-50%, -40%);
  }
}
@-o-keyframes cloud {
  0% {
    -o-transform: translate(-50%, -50%);
  }
  40% {
    opacity: 1;
  }
  60% {
    opacity: 1;
  }
  100% {
    -o-transform: translate(-50%, -40%);
  }
}
@-webkit-keyframes cloud {
  0% {
    -webkit-transform: translate(-50%, -50%);
  }
  40% {
    opacity: 1;
  }
  60% {
    opacity: 1;
  }
  100% {
    -webkit-transform: translate(-50%, -40%);
  }
}
@keyframes cloud {
  0% {
    transform: translate(-50%, -50%);
  }
  40% {
    opacity: 1;
  }
  60% {
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -40%);
  }
}
</style>
