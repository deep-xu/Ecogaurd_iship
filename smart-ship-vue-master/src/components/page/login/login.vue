<template>
  <div class="login-page">
    <div class="login-container">
      <div class="bg"></div>
      <div class="FormContainer">
        <div class="form-container">
          <div class="login-logo">
            <div class="brand-mark">
              <svg viewBox="0 0 30 30">
                <circle cx="15" cy="15" r="13" fill="none" stroke="rgba(11,31,69,0.18)" stroke-width="1.3"/>
                <g class="iris-group">
                  <rect transform="rotate(0 15 15)"   x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(45 15 15)"  x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(90 15 15)"  x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(135 15 15)" x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(180 15 15)" x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(225 15 15)" x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(270 15 15)" x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                  <rect transform="rotate(315 15 15)" x="14.2" y="4.3" width="1.6" height="5.6" rx="0.8" fill="#2E7BFF"/>
                </g>
                <circle cx="15" cy="15" r="6" fill="#0B1F45" stroke="#2E7BFF" stroke-width="1"/>
                <circle cx="15" cy="15" r="1.7" fill="#5FA8FF"/>
                <circle cx="24.6" cy="5.6" r="2" fill="#E5484D" stroke="#fff" stroke-width="1"/>
              </svg>
            </div>
            <div class="brand-text">
              <div class="brand-word">ECOGUARD VIGIL</div>
              <div class="brand-sub">Fleet Safety AI</div>
            </div>
          </div>
          <el-form
            ref="loginForm"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            auto-complete="on"
            label-position="left"
          >
            <el-form-item prop="username">
              <i class="z-icon-user1"></i>
              <el-input
                ref="username"
                v-model="loginForm.username"
                placeholder="Username"
                name="username"
                type="text"
                tabindex="1"
                auto-complete="on"
              />
            </el-form-item>
            <el-form-item prop="password">
              <i class="z-icon-password"></i>
              <el-input
                :key="passwordType"
                ref="password"
                v-model="loginForm.password"
                :type="passwordType"
                placeholder="Password"
                name="password"
                tabindex="2"
                auto-complete="on"
                v-if="loginForm.showCaptcha"
              />
              <el-input
                :key="passwordType"
                ref="password"
                v-model="loginForm.password"
                :type="passwordType"
                placeholder="password"
                name="password"
                tabindex="2"
                auto-complete="on"
                @keyup.enter.native="handleLogin"
                v-else
              />
              <i
                class="z-icon-hiddenpassword"
                @click="showPwd"
                v-show="passwordType"
              ></i>
              <i
                class="z-icon-showpassword"
                @click="showPwd"
                v-show="!passwordType"
              ></i>
            </el-form-item>
            <el-form-item prop="captcha_text" v-if="loginForm.showCaptcha">
              <i class="z-icon-yanzhengma"></i>
              <el-input
                ref="password"
                v-model="loginForm.captcha_text"
                type="text"
                placeholder="验证码"
                auto-complete="on"
                @keyup.enter.native="handleLogin"
              />
              <img
                v-if="loginForm.captcha_pic"
                :src="'data:image/jpg;base64,' + loginForm.captcha_pic"
                alt=""
                @click="getCaptcha()"
                style="
                  cursor: pointer;
                  position: absolute;
                  right: 0;
                  height: calc(100% - 2px);
                  top: 1px;
                  border-top-right-radius: 4px;
                  border-bottom-right-radius: 4px;
                "
              />
            </el-form-item>
            <el-button
              :loading="loading"
              type="primary"
              class="login"
              @click="handleLogin"
              >Log in</el-button
            >
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { resetTokenAndClearUser } from "@/utils/index.js";
import { user } from "@/api/user.js";
import { customLogo } from "@/api/system.js";
import { getsysargs } from "@/api/restart.js";
import md5 from "js-md5";
import langmixin from "@/utils/langmixin.js";
import bus from "@/utils/bus.js";
export default {
  mixins: [langmixin],
  data() {
    return {
      name: "",
      loginForm: {
        username: "",
        password: "",
        showCaptcha: false,
        captcha_id: "",
        captcha_text: "",
        captcha_pic: "",
      },
      loginRules: {
        username: [
          { required: true, message: "Please input user name", trigger: "blur" },
        ],
        password: [{ required: true, message: "Please input password", trigger: "blur" }],
      },
      loading: false,
      passwordType: "password",
      redirect: undefined,
    };
  },
  mounted() {
    resetTokenAndClearUser();
    customLogo.get().then((res) => {
      this.name = res.data.name;
    });
    this.getArgs();
  },
  methods: {
    getArgs() {
      getsysargs().then((res) => {
        // localStorage.setItem("streamports", JSON.stringify(res.map));
        // KSPROXYGLOBAL.srs_server = res.map.srs_server;
        // KSPROXYGLOBAL.srs_http_api = res.map.srs_http_api;
        // KSPROXYGLOBAL.srs_http_server = res.map.srs_http_server;
        // KSPROXYGLOBAL.websocket = res.map.websocket;
        let loginParams = res;
        if (loginParams) {
          this.loginForm.showCaptcha = loginParams.captcha.enable;
        } else {
          this.loginForm.showCaptcha = false;
        }

        if (this.loginForm.showCaptcha == true) {
          this.getCaptcha();
        }
      });
    },
    getCaptcha() {
      user
        .getCaptcha()
        .then((res) => {
          this.loginForm.captcha_id = res[0];
          this.loginForm.captcha_pic = res[1];
          this.loginForm.captcha_text = "";
          this.$forceUpdate();
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
          let params = {
            username: this.loginForm.username,
            password: this.loginForm.password,
          };
          if (this.loginForm.showCaptcha) {
            params.captcha_id = this.loginForm.captcha_id;
            params.captcha_text = this.loginForm.captcha_text;
          }
          user
            .login(params)
            .then(async (res) => {
              localStorage.setItem("t", res.access_token);
              const menuList = await user.getRouterList();
              localStorage.setItem("menu1", JSON.stringify(menuList));
              bus.$emit('login-success')
              // 获取用户信息
              try {
                const { roles } = await user.userInfo() || {};
                const permission = roles[0] || '';
                if (permission === 'superadmin') {
                  localStorage.setItem("role", "admin");
                  // this.$store.commit("menu/setMenuRole", "admin");
                  localStorage.setItem("permission", 20);
                } else if (permission === 'common') {
                  localStorage.setItem("role", "common");
                  // this.$store.commit("menu/setMenuRole", "common");
                  localStorage.setItem("permission", 10);
                } else {
                  localStorage.setItem("permission", 1);
                  localStorage.setItem("role", "visit");
                  // this.$store.commit("menu/setMenuRole", "visit");
                } 
              } catch (_) {}
              this.loading = true;
              localStorage.setItem("user", this.loginForm.username);
              this.$router.push("/");
            })
            .catch((err) => {
              if (err && err.data) {
                if (
                  err.data.remaining_attempts ||
                  err.data.remaining_attempts === 0
                ) {
                  this.$message({
                    type: "error",
                    message: `密码错误！剩余尝试次数：${err.data.remaining_attempts}次`,
                    duration: 0,
                    showClose: true,
                  });
                } else if (
                  err.data.remaining_lockout_time ||
                  err.data.remaining_lockout_time === 0
                ) {
                  let second = 0;
                  let minute = 0;
                  if (err.data.remaining_lockout_time < 60) {
                    second = err.data.remaining_lockout_time;
                  } else {
                    minute = Math.floor(err.data.remaining_lockout_time / 60);
                    second = err.data.remaining_lockout_time % 60;
                  }
                  this.$message({
                    type: "error",
                    message: `账号已锁定！剩余锁定时间${minute}分${second}秒`,
                    duration: 0,
                    showClose: true,
                  });
                }
              } else if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                  duration: 0,
                  showClose: true,
                });
              }
              if (this.loginForm.showCaptcha) {
                this.getCaptcha();
              }
            });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
  },
};
</script>
<style lang="less" scoped>
/* ===== Ecoguard Vigil themed login ===== */
.login-page,
.login-container {
  width: 100vw;
  height: 100vh;
  box-sizing: border-box;
  position: relative;
}
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: "IBM Plex Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}
.login-container .bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(1200rem 700rem at 78% -10%, rgba(46,123,255,0.28), transparent 60%),
    linear-gradient(160deg, #08132c 0%, #0b1f45 55%, #123368 100%);
}
.login-container .bg::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1rem, transparent 1rem),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1rem, transparent 1rem);
  background-size: 46rem 46rem;
  mask-image: radial-gradient(circle at 50% 45%, #000 0%, transparent 75%);
}
.FormContainer {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-container {
  width: calc(100% - 32rem);
  max-width: 440rem;
  padding: 44rem 44rem 40rem;
  box-sizing: border-box;
  position: relative;
  background: #ffffff;
  border: 1rem solid #e1e7f2;
  border-radius: 16rem;
  box-shadow: 0 2rem 8rem rgba(8, 19, 44, 0.18), 0 24rem 60rem rgba(8, 19, 44, 0.28);
}
.login-logo {
  margin: 0 auto 34rem;
  display: flex;
  align-items: center;
  gap: 12rem;
}
.login-logo .brand-mark {
  width: 40rem;
  height: 40rem;
  flex: none;
}
.login-logo .brand-mark svg {
  width: 100%;
  height: 100%;
  display: block;
  overflow: visible;
}
.login-logo .iris-group {
  transform-origin: 15px 15px;
  animation: irisRotate 10s linear infinite;
}
@keyframes irisRotate {
  to {
    transform: rotate(360deg);
  }
}
.login-logo .brand-word {
  font-family: "Space Grotesk", "IBM Plex Sans", sans-serif;
  font-weight: 700;
  font-size: 22rem;
  letter-spacing: 0.5rem;
  color: #141b2e;
  line-height: 1;
}
.login-logo .brand-sub {
  font-size: 11rem;
  letter-spacing: 1.4rem;
  text-transform: uppercase;
  color: #8b96af;
  margin-top: 4rem;
}
.login-form {
  /deep/ .el-form-item--small .el-form-item__content {
    height: 100%;
    line-height: normal;
  }
  /deep/ .el-input.el-input--small .el-input__inner {
    background-color: #f5f8fd;
    color: #141b2e;
    border: 1rem solid #e1e7f2;
    border-radius: 10rem;
    font-size: 15rem;
    padding-left: 44rem;
    height: 46rem;
    line-height: 46rem;
    transition: border-color 0.15s ease, background-color 0.15s ease, box-shadow 0.15s ease;
  }
  /deep/ .el-input.el-input--small .el-input__inner:focus {
    background-color: #ffffff;
    border-color: #2e7bff;
    box-shadow: 0 0 0 3rem rgba(46, 123, 255, 0.15);
  }
  /deep/ .el-input.el-input--small .el-input__inner::placeholder {
    color: #8b96af;
  }
  /deep/ .el-form-item--small.el-form-item {
    margin-bottom: 22rem;
    height: 46rem;
    line-height: 46rem;
  }
  /deep/ .el-input.el-input--small {
    width: 100%;
    height: 100%;
  }
  /deep/ .el-form-item--small .el-form-item__error {
    padding-left: 44rem;
    padding-top: 6rem;
    font-size: 12rem;
    color: #e5484d;
  }
  .login {
    width: 100%;
    height: 46rem;
    font-size: 15rem;
    font-weight: 600;
    letter-spacing: 0.5rem;
    margin-top: 8rem;
    border-radius: 10rem;
    background-color: #123368;
    color: #ffffff;
    border: 1rem solid #123368;
    transition: background-color 0.15s ease, border-color 0.15s ease;
  }
  .login:hover,
  .login:focus {
    background-color: #2e7bff;
    border-color: #2e7bff;
  }
  .z-icon-user1,
  .z-icon-password,
  .z-icon-yanzhengma,
  .z-icon-key {
    position: absolute;
    left: 14rem;
    font-size: 18rem;
    top: 14rem;
    z-index: 1;
    color: #8b96af;
  }
  .z-icon-hiddenpassword,
  .z-icon-showpassword {
    position: absolute;
    right: 14rem;
    font-size: 18rem;
    top: 14rem;
    z-index: 1;
    cursor: pointer;
    color: #8b96af;
  }
}
</style>
<style lang="less">
.login-form input:-webkit-autofill {
  -webkit-box-shadow: 0 0 0rem 1000rem #f5f8fd inset;
  -webkit-text-fill-color: #141b2e;
  caret-color: #141b2e;
}
</style>