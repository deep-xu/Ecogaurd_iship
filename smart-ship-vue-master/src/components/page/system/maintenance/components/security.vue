<template>
  <div class="security">
    <div class="pannel" v-if="userrole == 'admin'">
      <div class="pannel-header" style="padding-top: 50px; margin-bottom: 20px">
        <h3>Login Lock</h3>
      </div>
      <div class="pannel-body">
        <el-form
          ref="formData"
          :model="formData"
          :rules="formRule"
          label-position="left"
          style="width: 90%; margin: 0 auto"
        >
          <el-form-item label="Login Lock" prop="enable">
            <el-switch v-model="formData.enable" size="small"></el-switch>
          </el-form-item>
          <el-form-item label="Number of attempts" prop="max_attempts">
            <el-input-number
              :min="1"
              v-model="formData.max_attempts"
              :disabled="formData.enable == false"
              :readonly="formData.enable == false"
            ></el-input-number>
          </el-form-item>
          <el-form-item label="Lock time" prop="time">
            <el-input-number
              :min="1"
              v-model="formData.time"
              :disabled="formData.enable == false"
              :readonly="formData.enable == false"
            ></el-input-number>
            <span style="margin-left: 2px">s</span>
          </el-form-item>
          <el-form-item
            style="margin-top: 35px"
            v-if="
              oriData.enable != formData.enable ||
              oriData.max_attempts != formData.max_attempts ||
              oriData.time != formData.time
            "
          >
            <el-button
              type="primary"
              style="margin-right: 8px"
              :loading="loading"
              @click="handleSubmit('formData')"
              >Save</el-button
            >
            <el-button
              style="margin-right: 8px"
              :loading="loading"
              @click="handleCancel('formData')"
              >Cancel</el-button
            >
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="pannel" v-if="userrole == 'admin'">
      <div class="pannel-header" style="padding-top: 50px; margin-bottom: 20px">
        <h3>Login verification code</h3>
      </div>
      <div class="pannel-body">
        <el-form
          ref="formData1"
          :model="formData1"
          :rules="formRule"
          style="width: 90%; margin: 0 auto"
          label-width="200"
        >
          <el-form-item label="Login verification code" prop="enable">
            <el-switch v-model="formData1.enable" size="small"></el-switch>
          </el-form-item>
          <el-form-item label="Expiration Time" prop="expire">
            <el-input-number
              :min="1"
              v-model="formData1.expire"
              :disabled="formData1.enable == false"
              :readonly="formData1.enable == false"
            ></el-input-number>
            <span style="margin-left: 2px">s</span>
          </el-form-item>
          <el-form-item
            style="margin-top: 35px"
            v-if="
              oriData1.enable != formData1.enable ||
              oriData1.expire != formData1.expire
            "
          >
            <el-button
              type="primary"
              style="margin-right: 8px"
              :loading="loading"
              @click="handleSubmit('formData1')"
              >Save</el-button
            >
            <el-button
              style="margin-right: 8px"
              :loading="loading"
              @click="handleCancel('formData1')"
              >Cancel</el-button
            >
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="pannel" >
      <div class="pannel-header" style="padding-top: 50px; margin-bottom: 20px">
        <h3>Security certification</h3>
      </div>
      <div class="pannel-body accessInfo">
        <div class="info tip">
          <b><i class="z-icon-warning1"></i>Please do not disclose Access Key Secret</b>
        </div>
        <div class="info key">
          <label> Access Key ID: </label>
          <span>{{ ak }}</span>
        </div>
        <div class="info secret">
          <label> Access Key Secret: </label>
          <span v-if="skShow == false && sk"
            >*********************************************</span
          >
          <span v-else>{{ sk }}</span>

          <i
            v-if="skShow == false"
            class="z-icon-hiddenpassword"
            @click="skShow = !skShow"
          ></i>
          <i
            v-else
            class="z-icon-showpassword"
            @click="skShow = !skShow"
          ></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getsysargsToken, setsysargs } from "@/api/restart.js";
import { user } from "@/api/user.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  data() {
    return {
      loading: false,
      oriData: {
        enable: false,
        max_attempts: 5,
        time: 1800,
      },
      formData: {
        enable: false,
        max_attempts: 5,
        time: 1800,
      },
      oriData1: {
        enable: false,
        expire: 60,
      },
      formData1: {
        enable: false,
        expire: 60,
      },
      formRule: {},
      userrole: localStorage.getItem('role'),
      ak:'',
      sk:"",
      skShow: ""
    };
  },
  mounted() {
    if(this.userrole == 'admin'){
      this.getParams();
    }
    
    this.getAccessInfo()
  },
  methods: {
    getParams() {
      getsysargsToken()
        .then((res) => {
          let login = res.login;
          this.formData.enable = login.lockout.enable;
          this.formData.max_attempts = login.lockout.max_attempts;
          this.formData.time = login.lockout.time;
          this.formData1.enable = login.captcha.enable;
          this.formData1.expire = login.captcha.expire;
          this.oriData = Object.assign(this.oriData, this.formData);
          this.oriData1 = Object.assign(this.oriData1, this.formData1);
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
    handleSubmit(name) {
      if (name == "formData") {
        setsysargs({
          login: {
            lockout: {
              enable: this.formData.enable,
              max_attempts: this.formData.max_attempts,
              time: this.formData.time,
            },
            captcha: {
              enable: this.oriData1.enable,
              expire: this.oriData1.expire,
            },
          },
        })
          .then((res) => {
            this.$message({
              type: "success",
              message: res.message[this.lang],
            });
            this.getParams();
          })
          .catch((err) => {
            if (err && err.message && err.message[this.lang]) {
              this.$message({
                type: "error",
                message: res.message[this.lang],
              });
            }
          });
      } else {
        setsysargs({
          login: {
            captcha: {
              enable: this.formData1.enable,
              expire: this.formData1.expire,
            },
            lockout: {
              enable: this.oriData.enable,
              max_attempts: this.oriData.max_attempts,
              time: this.oriData.time,
            },
          },
        })
          .then((res) => {
            this.$message({
              type: "success",
              message: res.message[this.lang],
            });
            this.getParams();
          })
          .catch((err) => {
            if (err && err.message && err.message[this.lang]) {
              this.$message({
                type: "error",
                message: res.message[this.lang],
              });
            }
          });
      }
    },
    handleCancel(name) {
      if (name == "formData") {
        this.formData = Object.assign(this.formData, this.oriData);
      } else {
        this.formData1 = Object.assign(this.formData1, this.oriData1);
      }
    },
    getAccessInfo(){
      user.getAccessInfo().then(res => {
        if(res.length > 0){
          this.ak = res[0];
          this.sk = res[1]
        }
      })
    }
  },
};
</script>

<style lang="less" scoped>
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
  /deep/.el-input-number {
    width: 130px;
  }
}

.accessInfo{
  font-size: 14px;
  background-color: #fff;
  padding: 0 20px;
  .info{
    display: flex;
    width: 500px;
    margin-bottom: 10px;
    position: relative;
  }
  .info.tip{
    margin-left:25px;
    margin-bottom: 30px;
  }
  .z-icon-warning1{
    margin-right: 5px;
    color: orange;
    font-size: 18px;
  }
  .z-icon-showpassword,
  .z-icon-hiddenpassword{
    position: absolute;
    top: 0;
    left: 450px;
  }
  label{
    width: 150px;
    text-align: right;
    display: block;
    margin-right:10px;
  }
}
</style>