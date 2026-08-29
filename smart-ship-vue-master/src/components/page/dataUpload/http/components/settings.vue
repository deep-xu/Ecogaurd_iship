<template>
  <div class="settings">
    <el-form
      :model="formData"
      ref="formData"
      :rules="formRule"
      label-width="180px"
    >
      <el-form-item :label="formData.status ? '启用' : '停用'" prop="status">
        <el-switch
          v-model="formData.status"
          size="small"
          active-color="var(--btnprimary)"
        ></el-switch>
      </el-form-item>
      <el-form-item
        v-if="formData.status"
        label="url"
        prop="url"
        :rules="{
          required: true,
          validator: validateUrl,
          trigger: 'blur',
        }"
      >
        <el-input
          :readonly="!formData.status"
          :disabled="!formData.status"
          v-model="formData.url"
          style="width: 300px"
          placeholder="请输入告警推送地址URL"
        />
      </el-form-item>
      <el-form-item v-else label="url" prop="url">
        <el-input
          :readonly="!formData.status"
          :disabled="!formData.status"
          v-model="formData.url"
          style="width: 300px"
          placeholder="请输入告警推送地址URL"
        />
      </el-form-item>
      <el-form-item label="超时时间" prop="timeout">
        <el-input-number
          :readonly="!formData.status"
          :disabled="!formData.status"
          :step="1"
          :min="1"
          v-model="formData.timeout"
        ></el-input-number>
      </el-form-item>
      <el-form-item label="告警图片是否画框" prop="draw_image">
        <el-switch
          v-model="formData.draw_image"
          :readonly="!formData.status"
          :disabled="!formData.status"
          active-color="var(--btnprimary)"
          size="small"
        ></el-switch>
      </el-form-item>
      <el-form-item
        style="text-align: right"
        v-show="
          oriData.url != formData.url ||
          oriData.timeout != formData.timeout ||
          oriData.draw_image != formData.draw_image ||
          oriData.status != formData.status
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
    const isUrl = (rule, value, callback) => {
      let isTrue = new RegExp(
        "^(?!mailto:)(?:(?:http|https|ftp)://|//)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$",
        "i"
      );

      if (value && !isTrue.test(value)) {
        callback(new Error("请输入正确格式的url地址"));
      } else {
        callback();
      }
    };
    return {
      config: uploadApi,
      loading: false,
      btnLoading: false,
      oriData: {
        id: null,
        url: "",
        timeout: null,
        draw_image: null,
        status: null,
      },
      formData: {
        id: null,
        url: "",
        timeout: null,
        draw_image: null,
        status: null,
      },
      formRule: {
        url: [
          // { required: true, message: "请输入url地址", trigger: "blur" },
          { validator: isUrl, trigger: "blur" },
        ],
      },
    };
  },
  mounted() {
    this.getConfig();
  },
  methods: {
    getConfig() {
      uploadApi.get().then((res) => {
        res.data.forEach((item) => {
          if (item.method == "http") {
            let args = JSON.parse(item.args);
            this.formData.id = item.id;
            this.formData.url = args.url;
            this.formData.timeout = args.timeout;
            this.formData.draw_image = args.draw_image;
            this.formData.status = item.status ? true : false;
            this.oriData = Object.assign(this.oriData, this.formData);
          }
        });
      });
    },
    handleSave(params) {
      this.$refs["formData"]
        .validate((valid) => {
          if (valid) {
            this.btnLoading = true;
            uploadApi
              .edit({
                id: this.formData.id,
                method: "http",
                args: {
                  url: this.formData.url,
                  timeout: this.formData.timeout,
                  draw_image: this.formData.draw_image,
                  post_alert: true,
                  post_result: false,
                },
                status: this.formData.status ? 1 : 0,
              })
              .then((res) => {
                this.btnLoading = false;
                if (res.data.error_code == 0) {
                  this.getConfig();
                }
              });
          }
        })
        .catch((err) => {});
      uploadApi
        .edit({
          id: this.formData.id,
          method: "http",
          args: {
            url: this.formData.url,
            timeout: this.formData.timeout,
            draw_image: this.formData.draw_image,
            post_alert: true,
            post_result: false,
          },
          status: this.formData.status ? 1 : 0,
        })
        .then((res) => {
          this.btnLoading = false;
          if (res.data.error_code == 0) {
            this.getConfig();
          }
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
    handleCancel() {
      this.formData.alert_url = this.oriData.alert_url;
      this.$refs.formData.clearValidate();
    },
    validateUrl(rule, value, callback) {
      console.log(this.formData.url);
      if (!this.formData.url) {
        callback(new Error("请输入url"));
      }
      let isTrue = new RegExp(
        "^(?!mailto:)(?:(?:http|https|ftp)://|//)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$",
        "i"
      );

      if (this.formData.url && !isTrue.test(this.formData.url)) {
        callback(new Error("请输入正确格式的url地址"));
      } else {
        callback();
      }
    },
  },
};
</script>

<style lang="less" scoped>
.settings {
  padding: 20rem;
  box-sizing: border-box;
}
.unit {
  position: absolute;
  right: -20rem;
  top: 0;
  color: @mainText;
  font-size: 14rem;
}
.header {
  padding-top: 6rem;
  padding-left: 70rem;
  margin-bottom: 20rem;
  height: 20rem;
  line-height: 20rem;
  display: block;
}
.header .span-box {
  color: @mainText;
  padding: 2rem 10rem;
  border-left: 2rem solid @menuActiveText;
  font-size: 14rem;
}
</style>