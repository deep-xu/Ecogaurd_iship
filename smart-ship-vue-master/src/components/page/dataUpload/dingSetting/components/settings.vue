<template>
  <div class="settings">
    <el-form
      :model="formData"
      ref="formData"
      :rules="formRule"
      :label-width="labelWidth"
    >
      <el-form-item
        label="启用编辑"
        prop="edit"
        label-width="100px"
        class="openedit"
      >
        <el-switch
          v-model="formData.edit"
          @change="handleEditChg"
          active-color="var(--btnprimary)"
        >
        </el-switch>
      </el-form-item>
      <div class="sub-title">
        <h3>用户管理配置</h3>
      </div>
      <el-form-item label="应用key" prop="appkey">
        <el-input
          v-model.trim="formData.user.appkey"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="应用secret" prop="appsecret1">
        <el-input
          v-model.trim="formData.user.appsecret"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="获取access token" prop="get_token_url1">
        <el-input
          v-model.trim="formData.user.get_token_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="创建用户" prop="create_url">
        <el-input
          v-model.trim="formData.user.create_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="删除用户" prop="delete_url">
        <el-input
          v-model.trim="formData.user.delete_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <div class="sub-title">
        <h3>消息推送配置</h3>
      </div>

      <el-form-item label="机器人id" prop="appkey2">
        <el-input
          v-model.trim="formData.message.appkey"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="机器人secret" prop="appsecret2">
        <el-input
          v-model.trim="formData.message.appsecret"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="获取access token" prop="get_token_url2">
        <el-input
          v-model.trim="formData.message.get_token_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="发送消息" prop="message_url">
        <el-input
          v-model.trim="formData.message.message_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <div class="sub-title">
        <h3>发送消息参数</h3>
      </div>
      <el-form-item label="机器人id" prop="robotCode">
        <el-input
          v-model.trim="formData.message_body.robotCode"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="消息类型" prop="msgKey">
        <el-input
          v-model.trim="formData.message_body.msgKey"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <div class="sub-title">
        <h3>超时时间设置</h3>
      </div>
      <el-form-item label="超时时间" prop="push_timeout">
        <el-input-number
          v-model.trim="formData.push_timeout"
          :disabled="!formData.edit"
        ></el-input-number>
        <div class="unit">秒</div>
      </el-form-item>
      <el-form-item
        style="width: 100%; text-align: right; max-width: 100%"
        v-show="
          oriData.user.appkey != formData.user.appkey ||
          oriData.user.get_token_url != formData.user.get_token_url ||
          oriData.user.appsecret != formData.user.appsecret ||
          oriData.user.create_url != formData.user.create_url ||
          oriData.user.delete_url != formData.user.delete_url ||
          oriData.message.appkey != formData.message.appkey ||
          oriData.message.appsecret != formData.message.appsecret ||
          oriData.message.get_token_url != formData.message.get_token_url ||
          oriData.message.message_url != formData.message.message_url ||
          oriData.message_body.robotCode != formData.message_body.robotCode ||
          oriData.message_body.msgKey != formData.message_body.msgKey ||
          oriData.push_timeout != formData.push_timeout
        "
      >
        <el-button @click="handleCancel">撤销</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { dingtalkSetting } from "@/api/dataupload.js";
import langmixin from "@/utils/langmixin.js"
export default {
  mixins: [langmixin],
  data() {
    return {
      api: dingtalkSetting,
      oriData: {
        user: {
          appkey: "wwcdddf40fe7e4abad",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          appsecret: "IW_kozNcWtxI7Zq1xtNQiiyrHog4dwK0ro1wOtWALDg",
          create_url: "",
          delete_url: "",
        },
        message: {
          appkey: "wwcdddf40fe7e4abad",
          appsecret: "_JH_U346vXIMATXwj4GRy0ajAvJP6kzeKWSp-V-IhHc",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          message_url: "https://qyapi.weixin.qq.com/cgi-bin/message/send",
        },
        message_body: {
          robotCode: "",
          msgKey: "",
        },
        push_timeout: 2,
      },
      formData: {
        edit: false,
        user: {
          appkey: "wwcdddf40fe7e4abad",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          appsecret: "IW_kozNcWtxI7Zq1xtNQiiyrHog4dwK0ro1wOtWALDg",
          create_url: "",
          delete_url: "",
        },
        message: {
          appkey: "wwcdddf40fe7e4abad",
          appsecret: "_JH_U346vXIMATXwj4GRy0ajAvJP6kzeKWSp-V-IhHc",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          message_url: "https://qyapi.weixin.qq.com/cgi-bin/message/send",
        },
        message_body: {
          robotCode: "1000002",
          msgKey: 0,
        },
        push_timeout: 2,
      },
      formRule: {},
      labelWidth: "180px",
    };
  },
  mounted() {
    this.getSettingData();
  },
  methods: {
    getSettingData() {
      this.api.get().then((res) => {
        this.formData.edit = false;
        let message = JSON.parse(res.message);
        let user = JSON.parse(res.user);
        this.formData.user = Object.assign(this.formData.user, user);
        this.formData.message = Object.assign(this.formData.message, {
          appkey: message.appkey,
          appsecret: message.appsecret,
          get_token_url: message.get_token_url,
          message_url: message.message_url,
        });
        this.formData.push_timeout = res.push_timeout;
        this.formData.message_body = Object.assign(
          this.formData.message_body,
          message.message_body
        );
        this.oriData.user = Object.assign(this.oriData.user, user);
        this.oriData.message = Object.assign(this.oriData.message, {
          appkey: message.appkey,
          appsecret: message.appsecret,
          get_token_url: message.get_token_url,
          message_url: message.message_url,
        });
        this.oriData.message_body = Object.assign(
          this.oriData.message_body,
          message.message_body
        );
        this.oriData.push_timeout = res.push_timeout;
      });
    },
    handleSave() {
      this.$refs.formData.validate((valid) => {
        if (valid) {
          let formData = new FormData();
          formData.append("user", JSON.stringify(this.formData.user));
          let message = Object.assign(this.formData.message, {
            message_body: this.formData.message_body,
          });
          formData.append("message", JSON.stringify(message));
          formData.append("push_timeout", this.formData.push_timeout);
          this.api.save(formData).then((res) => {
            this.getSettingData();
            this.$message({
              type: "success",
              message: res.message[this.lang],
            });
          });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    handleCancel() {
      this.formData = Object.assign(
        this.formData,
        JSON.parse(JSON.stringify(this.oriData))
      );
    },
    handleEditChg(val) {
      if (!val) {
        this.handleCancel();
      }
    },
  },
};
</script>

<style lang="less" scoped>
.settings {
  padding-top: 30rem;
  overflow: auto;
}
.unit {
  position: absolute;
  right: -20rem;
  top: 0;
  color: @mainText;
}
.sub-title {
  padding-top: 6rem;
  padding-left: 70rem;
  margin-bottom: 20rem;
  width: 100%;
  font-size: 14rem;
  h3 {
    color: #555555;
    padding: 2rem 10rem;
    border-left: 2rem solid #ff9900;
  }
}
/deep/.el-form {
  display: flex;
  flex-wrap: wrap;
  width: 1200rem;
}
/deep/.el-form .el-form-item--small.el-form-item {
  width: 50%;
}
/deep/.el-form .el-form-item--small.el-form-item.btns {
  width: 100%;
  max-width: 100%;
  .el-form-item__content {
    text-align: left;
  }
}

.openedit {
  position: absolute;
  right: 0;
  width: 180rem !important;
  top: 50rem;
}
</style>