<template>
  <div>
    <el-form
      ref="formData"
      :model="formData"
      label-position="top"
      :rules="formRule"
      style="margin: 0 auto"
    >
      <el-form-item
        prop="files"
        :rules="{ validator: validateFile, trigger: 'blur' }"
      >
        <label>选择升级包</label>
        <el-upload
          class="upload-demo"
          drag
          action="#"
          accept=".zip"
          :show-file-list="false"
          :before-upload="handleUpload"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
        <ul class="alg-list">
          <li v-if="formData.file">
            <span>{{ formData.file.name }}</span>
            <!-- <i @click="handleDelAlg(index)" class="z-iconfont z-icon-close"></i> -->
          </li>
        </ul>
      </el-form-item>
      <el-form-item
        prop="devices"
        :rules="{ validator: validateDevices, trigger: 'blur' }"
      >
        <label>选择在线设备</label>
        <ul class="alg-list">
          <li
            v-for="(item, index) in formData.deviceList"
            :key="item.id + index"
          >
            <el-checkbox v-model="item.select">{{ item.name }}</el-checkbox>
          </li>
        </ul>
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="formData.loading"
          :loading="formData.loading"
          type="primary"
          @click="handleSave('formData')"
          >更新</el-button
        >
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { deviceApis } from "@/api/device.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["devices"],
  data() {
    return {
      formData: {
        loading: false,
        file: null,
        deviceList: [],
      },
      formRule: {},
      devicesObj: {},
    };
  },
  mounted() {
    this.formData.deviceList = JSON.parse(JSON.stringify(this.$props.devices))
      .filter((item) => item.status == 1)
      .map((item) => {
        item.select = true;
        this.devicesObj[item.id] = item;
        return item;
      });
  },
  methods: {
    validateDevices(rule, value, callback) {
      if (
        this.formData.deviceList.filter((item) => item.select == true).length ==
        0
      ) {
        callback(new Error("请选择至少一个设备"));
      } else {
        callback();
      }
    },
    validateFile(rule, value, callback) {
      if (!this.formData.file) {
        callback(new Error("请选择升级包"));
      } else {
        callback();
      }
    },
    handleUpload(file) {
      let type = file.name.slice(file.name.length - 4);
      if (type != ".zip") {
        this.$message({
          type: "error",
          message: "文件类型错误！请重新上传！",
        });
        return false;
      }
      this.formData.file = file;

      return false;
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.$confirm("更新成功后设备将自动重启，是否确定更新？", "更新", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
          })
            .then(() => {
              this.formData.loading = true;
              let formData = new FormData();
              formData.append("package", this.formData.file);
              formData.append(
                "device_ids",
                JSON.stringify(
                  this.formData.deviceList
                    .filter((item) => item.select)
                    .map((item) => item.id)
                )
              );
              deviceApis
                .sysupgrade(formData)
                .then((res) => {
                  this.formData.loading = false;
                  let successNum = 0;
                  let length = Object.keys(res).length;
                  for (let device_id in res) {
                    if (res[device_id].result == true) {
                      successNum++;
                    } else {
                      this.$message({
                        type: "error",
                        message: `设备${this.devicesObj[device_id].name}系统更新失败!`,
                      });
                    }
                  }
                  if (successNum == length) {
                    this.$message({
                      type: "success",
                      message: "系统更新成功！",
                    });
                  }

                  this.$emit("close-sysupgrade");
                })
                .catch((err) => {
                  this.formData.loading = false;
                  if (err && err.message && err.message[this.lang]) {
                    this.$message({
                      type: "error",
                      message: err.message[this.lang],
                    });
                  }
                });
            })
            .catch(() => {});
        }
      });
    },
    handleDelAlg(index) {
      this.formData.fileList.splice(index, 1);
    },
  },
};
</script>

<style lang="less" scoped>
.el-form-item {
  max-width: 100% !important;
}
.alg-list {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  max-width: 540px;
  li {
    width: 250px;
    padding-right: 20px;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
    align-items: center;
    i {
      cursor: pointer;
    }
    /deep/.el-checkbox {
      display: flex;
      align-items: center;
      .el-checkbox__label {
        font-size: 14rem;
      }
    }
  }
}
</style>