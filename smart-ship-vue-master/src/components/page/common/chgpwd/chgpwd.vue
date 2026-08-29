<template>
  <el-form :model="chgPwd" :rules="formRules" ref="formData">
    <el-form-item label="旧密码:" prop="oldPwd">
      <div style="position: relative">
        <el-input
          v-model="chgPwd.oldPwd"
          :type="chgPwd.showOldPwd ? 'text' : 'password'"
          auto-complete="false"
        ></el-input>
        <i
          class="z-icon-showpassword"
          v-if="!chgPwd.showOldPwd"
          @click="chgPwd.showOldPwd = !chgPwd.showOldPwd"
        ></i>
        <i
          class="z-icon-hiddenpassword"
          v-if="chgPwd.showOldPwd"
          @click="chgPwd.showOldPwd = !chgPwd.showOldPwd"
        ></i>
      </div>
    </el-form-item>
    <el-form-item label="新密码:" prop="newPwd">
      <div style="position: relative">
        <el-input
          v-model="chgPwd.newPwd"
          :type="chgPwd.showNewPwd ? 'text' : 'password'"
          auto-complete="false"
          @input="handleCheckPwd"
          :maxlength="16"
        ></el-input>
        <i
          class="z-icon-showpassword"
          v-if="!chgPwd.showNewPwd"
          @click="chgPwd.showNewPwd = !chgPwd.showNewPwd"
        ></i>
        <i
          class="z-icon-hiddenpassword"
          v-if="chgPwd.showNewPwd"
          @click="chgPwd.showNewPwd = !chgPwd.showNewPwd"
        ></i>
      </div>

      <div style="width: 100%">
        <ul
          class="passwordStrength"
          :class="{
            weak: passwordStrength == 'weak',
            medium: passwordStrength == 'medium',
            strong: passwordStrength == 'strong',
          }"
        >
          <li></li>
          <li></li>
          <li></li>
          <div class="passwordStrengthText">
            <span v-if="passwordStrength == 'weak'">弱</span>
            <span v-if="passwordStrength == 'medium'">中</span>
            <span v-if="passwordStrength == 'strong'">强</span>
          </div>
        </ul>
        <div class="passwordStrengthTip">
          建议密码长度大于等于8位，且同时包含字母、数字及特殊字符
        </div>
      </div>
    </el-form-item>
    <el-form-item label="确认新密码:" prop="newPwd2">
      <div style="position: relative">
        <el-input
          v-model="chgPwd.newPwd2"
          :type="chgPwd.showNewPwd2 ? 'text' : 'password'"
          auto-complete="false"
          :maxlength="16"
        ></el-input>
        <i
          class="z-icon-showpassword"
          v-if="!chgPwd.showNewPwd2"
          @click="chgPwd.showNewPwd2 = !chgPwd.showNewPwd2"
        ></i>
        <i
          class="z-icon-hiddenpassword"
          v-if="chgPwd.showNewPwd2"
          @click="chgPwd.showNewPwd2 = !chgPwd.showNewPwd2"
        ></i>
      </div>
    </el-form-item>
    <el-form-item style="margin-top: 30px; text-align: right">
      <el-button @click="handleHideChgpwd">取消</el-button>
      <el-button type="primary" @click="handleChgPwd">确认</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import {user} from "@/api/user.js";
import langmixin from "@/utils/langmixin.js";

export default {
  mixins: [langmixin],
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
      chgPwd: {
        show: false,
        oldPwd: "",
        showOldPwd: false,
        newPwd: "",
        showNewPwd: false,
        newPwd2: "",
        showNewPwd2: false,
      },
      formRules: {
        oldPwd: [
          { required: true, message: "请输入旧密码", trigger: "blur" },
          // {
          //   validator: validatePwd,
          //   trigger: "blur",
          //   message:
          //     "请输入6~16个字符，至少一个大写字母，一个小写字母和一个数字",
          // },
        ],
        newPwd: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          // {
          //   validator: validatePwd,
          //   trigger: "blur",
          //   message:
          //     "请输入6~16个字符，至少一个大写字母，一个小写字母和一个数字",
          // },
        ],
        newPwd2: [
          { required: true, message: "请再次输入", trigger: "blur" },
          // {
          //   validator: validatePwd,
          //   trigger: "blur",
          //   message:
          //     "请输入6~16个字符，至少一个大写字母，一个小写字母和一个数字",
          // },
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

      passwordStrength: "",
    };
  },
  methods: {
    handleHideChgpwd() {
      this.$emit("cancel-chgpwd");
    },
    handleChgPwd() {
      this.$refs.formData.validate((valid) => {
        if (valid) {
          user
            .chgpwd({
              oldPassword: this.chgPwd.oldPwd,
              newPassword: this.chgPwd.newPwd,
            })
            .then((res) => {
              this.$emit("cancel-chgpwd");
              this.$message({
                  type: "success",
                  message: "系统将于2s后返回登录页面",
                });
                setTimeout(() => {
                  this.$router.push("/login");
                }, 2000);
            })
            .catch((err) => {
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              }
            });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    handleCheckPwd() {
      if (!this.chgPwd.newPwd) {
        this.passwordStrength = "unknown";
        return;
      }
      // 强密码：包含字母、数字和特殊字符，且长度大于等于8位
      if (/^(?=.*[a-zA-Z])(?=.*\d)(?=.*\W).{8,}$/.test(this.chgPwd.newPwd)) {
        this.passwordStrength = "strong";
        return;
      }

      // 中等强度密码：包含字母和数字，且长度大于等于8位
      if (/^(?=.*[a-zA-Z])(?=.*\d).{8,}$/.test(this.chgPwd.newPwd)) {
        this.passwordStrength = "medium";
        return;
      }

      // 弱密码：只包含字母或数字，且长度小于8位
      if (
        /^(?:\d+|[a-zA-Z]+)$/.test(this.chgPwd.newPwd) ||
        this.chgPwd.newPwd.length < 8
      ) {
        this.passwordStrength = "weak";
        return;
      }

      // 其他情况
      this.passwordStrength = "unknown";
      return;
    },
  },
};
</script>

<style lang="less" scoped>
.passwordStrength {
  width: calc(100% - 22rem);
  list-style: none;
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  position: relative;
  li {
    width: calc((100% - 6px) / 3);
    height: 5px;
    background-color: gray;
  }
}
.passwordStrength.weak {
  li {
    background-color: rgb(252, 101, 126);
  }
}
.passwordStrength.medium {
  li {
    background-color: rgb(255, 200, 93);
  }
}
.passwordStrength.strong {
  li {
    background-color: rgb(101, 210, 93);
  }
}
.passwordStrengthText {
  position: absolute;
  right: -20px;
  bottom: -13px;
  font-size: 14rem;
}
.passwordStrengthTip {
  line-height: 1.5;
  width: 100%;
  color: #b0b1b1;
  font-size: 12rem;
  margin-top: 5px;
}

.z-icon-showpassword,
.z-icon-hiddenpassword {
  position: absolute;
  right: 20rem;
  top: 35rem;
  cursor: pointer;
}
</style>