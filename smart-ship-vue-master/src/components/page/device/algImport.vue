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
        :rules="{ validator: validateFiles, trigger: 'blur' }"
      >
        <label>选择算法文件</label>
        <el-upload
          class="upload-demo"
          multiple
          drag
          action="#"
          accept=".bin"
          :show-file-list="false"
          :before-upload="handleUpload"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
        <ul class="alg-list">
          <li v-for="(item, index) in formData.fileList" :key="index">
            <span>{{ index + 1 }}. {{ item.name }}</span>
            <i @click="handleDelAlg(index)" class="z-iconfont z-icon-close"></i>
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
          >下发</el-button
        >
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { algApis } from "@/api/alg.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["devices"],
  data() {
    return {
      formData: {
        fileList: [],
        deviceList: [],
        loading: false,
      },
      formRule: {},
      devicesObj: {},
      allReturn: [],
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
    validateFiles(rule, value, callback) {
      if (this.formData.fileList.length == 0) {
        callback(new Error("请选择至少一个文件"));
      } else {
        callback();
      }
    },
    handleUpload(file) {
      // let type = file.name.slice(file.name.length - 4);
      // if (type != ".zip") {
      //   this.$message({
      //     type: "error",
      //     message: "文件类型错误！请重新上传！",
      //   });
      //   return false;
      // }
      let haveFlag = false;
      for (let i = 0; i < this.formData.fileList.length; i++) {
        if (this.formData.fileList[i].name == file.name) {
          haveFlag = true;
        }
      }
      if (!haveFlag) {
        this.formData.fileList.push(file);
      }

      return false;
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.formData.loading = true;
          this.allReturn = [];
          for (let index = 0; index < this.formData.fileList.length; index++) {
            this.handleImport(index);
            this.allReturn.push({ hasRet: false, success: false });
          }
        }
      });
    },
    handleImport(index) {
      let name = this.formData.fileList[index].name;
      let formData = new FormData();
      formData.append("package", this.formData.fileList[index]);
      formData.append(
        "device_ids",
        JSON.stringify(
          this.formData.deviceList
            .filter((item) => item.select)
            .map((item) => item.id)
        )
      );
      algApis
        .import(formData)
        .then((res) => {
          this.allReturn[index].hasRet = true;

          let length = Object.keys(res).length;
          let successNum = 0;
          for (let device_id in res) {
            if (res[device_id].result == true) {
              successNum++;
            } else {
              this.$message({
                type: "error",
                message: `${name}下发到设备${
                  this.devicesObj[device_id].name
                }失败! 失败原因：${res[device_id].reason[this.lang]}`,
                showClose: true,
                duration: 0,
              });
            }
          }
          if (successNum == length) {
            this.allReturn[index].success = true;
            this.$message({
              type: "success",
              message: `${name}下发到设备成功!`,
            });
            for (let i = 0; i < this.formData.fileList.length; i++) {
              if (this.formData.fileList[i].name == name) {
                this.formData.fileList.splice(i, 1);
                break;
              }
            }
          }

          if (
            this.allReturn.filter((item) => item.hasRet == false).length == 0
          ) {
            this.formData.loading = false;
          }
          if (
            this.allReturn.filter((item) => item.success == false).length == 0
          ) {
            this.$emit("close-importalg");
          }
        })
        .catch((err) => {
          this.allReturn[index].hasRet = true;
          if (
            this.allReturn.filter((item) => item.hasRet == false).length == 0
          ) {
            this.formData.loading = false;
          }
          for (let device_id in err.data) {
            if (err.data[device_id].result == false) {              
              this.$message({
                type: "error",
                message: `${name}下发到设备${
                  this.devicesObj[device_id].name
                }失败! 失败原因：${err.data[device_id].reason[this.lang]}`,
                showClose: true,
                duration: 0,
              });
            }
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