<template>
  <div>
    <div class="pannel setsink">
      <div class="pannel-header"><h3>Port Mapping</h3></div>
      <div class="pannel-body" style="position: relative">
        <el-form
          ref="formData"
          :model="formData"
          label-position="top"
          :rules="formRule"
          style="width: 90%; margin: 0 auto"
        >
          <el-form-item label="Streaming Media service port" prop="srs_server" :rules="{ required: true, validator: validatePort, trigger: 'blur' }">
            <el-input
              v-model="formData.srs_server"
              @input="handleInput('srs_server')"
            ></el-input>
          </el-form-item>
          <el-form-item label="Streaming Media http port" prop="srs_http_api" :rules="{ required: true, validator: validatePort, trigger: 'blur' }">
            <el-input
              v-model="formData.srs_http_api"
              @input="handleInput('srs_http_api')"
            ></el-input>
          </el-form-item>
          <el-form-item label="Streaming Media http service port" prop="srs_http_server" :rules="{ required: true, validator: validatePort, trigger: 'blur' }">
            <el-input
              v-model="formData.srs_http_server"
              @input="handleInput('srs_http_server')"
            ></el-input>
          </el-form-item>
          <el-form-item label="websocket port" prop="websocket" :rules="{ required: true, validator: validatePort, trigger: 'blur' }">
            <el-input
              v-model="formData.websocket"
              @input="handleInput('websocket')"
            ></el-input>
          </el-form-item>
          <el-button
            type="primary"
            @click="handleSave"
            v-if="
              formData.srs_server != oriData.srs_server ||
              formData.srs_http_api != oriData.srs_http_api ||
              formData.srs_http_server != oriData.srs_http_server ||
              formData.websocket != oriData.websocket
            "
            >Save</el-button
          >
          <el-button type="primary" @click="handleReset">Reset</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { getsysargsToken, setsysargs, mapReset } from "@/api/restart.js";
import langmixin from "@/utils/langmixin.js";
import publicinfo from "@/utils/publicinfo.js";
export default {
  mixins: [langmixin, publicinfo],
  data() {
    return {
      oriData: {
        srs_server: 1935,
        srs_http_api: 1985,
        srs_http_server: 8080,
        websocket: 8083,
      },
      formData: {
        srs_server: 1935,
        srs_http_api: 1985,
        srs_http_server: 8080,
        websocket: 8083,
      },
      formRule: {
        srs_server: [
          { required: true, message: "Please input Streaming Media service port", trigger: "blur" },
        ],
        srs_http_api: [
          {
            required: true,
            message: "Please input Streaming Media http port",
            trigger: "blur",
          },
        ],
        srs_http_server: [
          {
            required: true,
            message: "Please input Streaming Media http service port",
            trigger: "blur",
          },
        ],
        websocket: [
          {
            required: true,
            message: "Please input websocket port",
            trigger: "blur",
          },
        ],
      },
    };
  },
  mounted() {
    // this.getsysargs();
  },
  methods: {
    validatePort(rule, value, callback) {
      if (Number.isInteger(value) && value >= 0 && value <= 65535) {
        callback();
      } else {
        callback('Please enter the correct port number（<65535）');
      }
    },
    handleInput(key){
      this.formData[key] = this.formData[key] ? parseInt(this.formData[key]) : ""
    },
    getsysargs() {
      getsysargsToken().then((res) => {
        this.formData = Object.assign(this.formData, res.map);
        this.oriData = JSON.parse(JSON.stringify(this.formData));
      });
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          setsysargs({
            map:this.formData
          }).then((res) => {
            if (res.error_code == 0) {
              this.$message({
                type: "success",
                message: res.message[this.lang],
              });
              // this.getsysargs();
              this.getStreamPort();
            }
          });
        }
      });
    },
    handleReset() {
      this.$confirm("Are you sure to restore the default port", "Reset", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      }).then(() => {
        mapReset().then((res) => {
          if (res.error_code == 0) {
            this.$message({
              type: "success",
              message: res.message[this.lang],
            });
            this.getsysargs();
            this.getStreamPort();
          }
        });
      });
    },
  },
};
</script>

<style lang="less" scope>
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
}
</style>