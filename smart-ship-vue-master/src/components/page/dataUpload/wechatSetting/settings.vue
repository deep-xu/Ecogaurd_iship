<template>
  <div class="settings">
    <el-form
      :model="formData"
      ref="formData"
      :rules="formRule"
      :label-width="labelWidth"
    >
      <!-- <el-form-item
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
      </el-form-item> -->
      <div class="sub-title">
        <h3>用户管理配置</h3>
      </div>
      <el-form-item label="企业id" prop="corpid">
        <el-input
          v-model.trim="formData.user_conf.corpid"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="通讯录secret" prop="corpsecret1">
        <el-input
          v-model.trim="formData.user_conf.corpsecret"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="获取access token" prop="get_token_url1">
        <el-input
          v-model.trim="formData.user_conf.get_token_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="创建用户" prop="create_url">
        <el-input
          v-model.trim="formData.user_conf.create_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="删除用户" prop="delete_url">
        <el-input
          v-model.trim="formData.user_conf.delete_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <div class="sub-title">
        <h3>消息推送配置</h3>
      </div>
      <el-form-item label="应用id" prop="agentid">
        <el-input
          v-model.trim="formData.msg_conf.agentid"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="企业id" prop="corpid2">
        <el-input
          v-model.trim="formData.msg_conf.corpid"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="应用secret" prop="corpsecret2">
        <el-input
          v-model.trim="formData.msg_conf.corpsecret"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="获取access token" prop="get_token_url2">
        <el-input
          v-model.trim="formData.msg_conf.get_token_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="上传临时素材" prop="image_url">
        <el-input
          v-model.trim="formData.msg_conf.image_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="发送消息" prop="message_url">
        <el-input
          v-model.trim="formData.msg_conf.message_url"
          :disabled="!formData.edit"
        ></el-input>
      </el-form-item>
      <el-form-item label="是否加密" prop="safe">
        <el-switch
          v-model="formData.msg_conf.safe"
          active-color="var(--btnprimary)"
          :disabled="!formData.edit"
        >
        </el-switch>
      </el-form-item>
      <el-form-item label="是否开启重复消息检查" prop="enable_duplicate_check">
        <el-switch
          v-model="formData.msg_conf.enable_duplicate_check"
          active-color="var(--btnprimary)"
          :disabled="!formData.edit"
        >
        </el-switch>
      </el-form-item>
      <el-form-item label="是否开启id转译" prop="enable_id_trans">
        <el-switch
          v-model="formData.msg_conf.enable_id_trans"
          active-color="var(--btnprimary)"
          :disabled="!formData.edit"
        >
        </el-switch>
      </el-form-item>

      <el-form-item
        v-if="formData.msg_conf.enable_duplicate_check != 0"
        label="重复消息检查间隔"
        prop="duplicate_check_interval"
      >
        <el-input-number
          v-model.trim="formData.msg_conf.duplicate_check_interval"
          :disabled="!formData.edit"
        ></el-input-number>
        <div class="unit">秒</div>
      </el-form-item>
      <div class="sub-title">
        <h3>超时时间设置</h3>
      </div>
      <el-form-item label="超时时间" prop="timeout">
        <el-input-number
          v-model.trim="formData.timeout"
          :disabled="!formData.edit"
        ></el-input-number>
        <div class="unit">秒</div>
      </el-form-item>
      <div class="sub-title">
        <h3>告警图片是否画框</h3>
      </div>
      <el-form-item label="告警图片是否画框" prop="timeout">
        <el-switch
          v-model="formData.draw_image"
          :disabled="!formData.edit"
          active-color="var(--btnprimary)"
        ></el-switch>
      </el-form-item>
      <el-form-item
        style="width: 100%; text-align: right; max-width: 100%"
        v-show="
          oriData.user_conf.corpid != formData.user_conf.corpid ||
          oriData.user_conf.get_token_url != formData.user_conf.get_token_url ||
          oriData.user_conf.corpsecret != formData.user_conf.corpsecret ||
          oriData.user_conf.create_url != formData.user_conf.create_url ||
          oriData.user_conf.delete_url != formData.user_conf.delete_url ||
          oriData.msg_conf.corpid != formData.msg_conf.corpid ||
          oriData.msg_conf.corpsecret != formData.msg_conf.corpsecret ||
          oriData.msg_conf.get_token_url != formData.msg_conf.get_token_url ||
          oriData.msg_conf.image_url != formData.msg_conf.image_url ||
          oriData.msg_conf.msg_url != formData.msg_conf.msg_url ||
          oriData.msg_conf.agentid != formData.msg_conf.agentid ||
          oriData.msg_conf.safe != formData.msg_conf.safe ||
          oriData.msg_conf.enable_id_trans !=
            formData.msg_conf.enable_id_trans ||
          oriData.msg_conf.enable_duplicate_check !=
            formData.msg_conf.enable_duplicate_check ||
          oriData.msg_conf.duplicate_check_interval !=
            formData.msg_conf.duplicate_check_interval ||
          oriData.timeout != formData.timeout ||
          oriData.draw_image != formData.draw_image
        "
      >
        <el-button @click="handleCancel">撤销</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
  
<script>
import { uploadApi } from "@/api/dataupload.js";
export default {
  data() {
    return {
      oriData: {
        user_conf: {
          corpid: "ww50a81e599e02e14c",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          corpsecret: "v7uWZKFaTFDVqxeQa-wqJ-YpLUKsLvsGf9yTmjPEBPY",
          create_url: "",
          delete_url: "",
        },
        msg_conf: {
          corpid: "ww50a81e599e02e14c",
          corpsecret: "aaKJYu8x8NU8F2CYM5XQh_AkMF0sr6Apzg5zjjP0G-Y",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          image_url: "https://qyapi.weixin.qq.com/cgi-bin/media/upload",
          msg_url: "https://qyapi.weixin.qq.com/cgi-bin/message/send",
          agentid: "1000002",
          safe: 0,
          enable_id_trans: 0,
          enable_duplicate_check: 0,
          duplicate_check_interval: 1800,
        },
        timeout: 2,
        draw_image: true,
      },
      formData: {
        edit: true,
        user_conf: {
          corpid: "wwcdddf40fe7e4abad",
          corpsecret: "IW_kozNcWtxI7Zq1xtNQiiyrHog4dwK0ro1wOtWALDg",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          create_url: "https://qyapi.weixin.qq.com/cgi-bin/user/create",
          delete_url: "https://qyapi.weixin.qq.com/cgi-bin/user/delete",
        },
        msg_conf: {
          corpid: "wwcdddf40fe7e4abad",
          corpsecret: "_JH_U346vXIMATXwj4GRy0ajAvJP6kzeKWSp-V-IhHc",
          get_token_url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
          image_url: "https://qyapi.weixin.qq.com/cgi-bin/media/upload",
          msg_url: "https://qyapi.weixin.qq.com/cgi-bin/message/send",
          agentid: "1000002",
          safe: 0,
          enable_id_trans: 0,
          enable_duplicate_check: 0,
          duplicate_check_interval: 1800,
        },
        timeout: 2,
        draw_image: true,
      },
      formRule: {
        duplicate_check_interval: [
          {
            message: "最大不能超过14400",
            trigger: "blur",
            validator: (rule, value, callback) => {
              if (value > 14400) {
                callback(new Error());
              } else {
                callback();
              }
            },
          },
        ],
      },
      labelWidth: "180px",
    };
  },
  mounted() {
    this.getSettingData();
  },
  methods: {
    getSettingData() {
      uploadApi
        .get()
        .then((res) => {
          this.loading = false;
          if (res.data.error_code == 0) {
            res.data.data.forEach((item) => {
              if (item.method == "wechat") {
                let args = JSON.parse(item.args);
                args.msg_conf.safe = args.msg_conf.safe ? true : false;
                args.msg_conf.enable_id_trans = args.msg_conf.enable_id_trans
                  ? true
                  : false;
                args.msg_conf.enable_duplicate_check = args.msg_conf
                  .enable_duplicate_check
                  ? true
                  : false;
                this.formData.id = item.id;
                this.formData.user_conf = Object.assign(
                  this.formData.user_conf,
                  args.user_conf
                );
                this.formData.msg_conf = Object.assign(
                  this.formData.msg_conf,
                  args.msg_conf
                );
                this.formData.timeout = args.timeout;
                this.formData.draw_image = args.draw_image;
                this.formData.edit = true;
                this.oriData = JSON.parse(JSON.stringify(this.formData));
              }
            });
          }
        })
        .catch((err) => {
          this.loading = false;
        });
    },
    handleSave() {
      this.$refs.formData.validate((valid) => {
        if (valid) {
          let args = {
            user_conf: Object.assign({}, this.formData.user_conf),
            msg_conf: Object.assign({}, this.formData.msg_conf, {
              safe: this.formData.msg_conf.safe ? 1 : 0,
              enable_id_trans: this.formData.msg_conf.enable_id_trans ? 1 : 0,
              enable_duplicate_check: this.formData.msg_conf
                .enable_duplicate_check
                ? 1
                : 0,
            }),
            timeout: this.formData.timeout || 3,
            draw_image: this.formData.draw_image,
            post_alert: true,
            post_result: false,
          };
          uploadApi
            .edit({
              id: this.formData.id,
              method: "wechat",
              args: args,
              status: 1,
            })
            .then((res) => {
              if (res.data.error_code == 0) {
                this.getSettingData();
                this.$emit("close-edit-wechat")
              }
            })
            .catch((err) => {
              this.$message({
                type:'error',
                message:"修改失败"
              })
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
      let args = {
        user_conf: Object.assign({}, this.formData.user_conf),
        msg_conf: Object.assign({}, this.formData.msg_conf, {
          safe: this.formData.msg_conf.safe ? 1 : 0,
          enable_id_trans: this.formData.msg_conf.enable_id_trans ? 1 : 0,
          enable_duplicate_check: this.formData.msg_conf.enable_duplicate_check
            ? 1
            : 0,
        }),
        timeout: this.formData.timeout,
        draw_image: this.formData.draw_image,
      };
      uploadApi
        .edit({
          id: this.formData.id,
          method: "wechat",
          args: args,
          status: this.formData.edit ? 1 : 0,
        })
        .then((res) => {
          if (res.data.error_code == 0) {
            this.getSettingData();
          } else {
            this.formData.edit = !this.settingData.edit;
            
          }
        })
        .catch((err) => {
          this.formData.edit = !this.settingData.edit;
          this.$message({
            type:'error',
            message:"修改失败"
          })
        });
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