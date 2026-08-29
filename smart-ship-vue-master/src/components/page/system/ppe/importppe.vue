<template>
  <div class="importlib">
    <h3>
      <span :class="{ active: type == 'import' }" @click="type = 'import'"
        >批量导入</span
      ><span :class="{ active: type == 'export' }" @click="type = 'export'"
        >批量导出</span
      >
    </h3>
    <div v-if="type == 'import'">
      <div class="step-download">
        <el-button type="primary" @click="handleDownload('template')"
          >下载模板</el-button
        >
        <ul>
          <li>1.点击下载系统提供的信息和照片模板</li>
          <li>2.模板解压，获取信息Excel、照片文件夹</li>
          <li>3.规范填写Excel信息，文件夹中添加已命名注册照</li>
          <li>4.注册照片支持jpg,jpeg,png,bmp格式，命名不可包含中文</li>
          <li>5.添加完毕，确认目录层级与模板保持一致，将模板重新压缩为.zip格式</li>
        </ul>
      </div>
      <div class="step-upload">
        <h4>上传压缩文件</h4>
        <el-upload
          class="upload-demo"
          drag
          action="#"
          accept=".zip"
          :before-upload="handleUpload"
          :show-file-list="false"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text" style="font-size: 14rem">
            请上传.zip文件
          </div>
        </el-upload>
        <div style="font-size: 14rem; text-align: center">{{ filename }}</div>
        <el-button
          v-if="file"
          type="primary"
          @click="handleSave"
          :loading="importloading"
          >上传</el-button
        >
      </div>
    </div>
    <div v-else class="exportface">
      <!-- <h4>选择导出区间</h4>
        <InputNumber
          v-model="exportface.start"
          style="width: 150px"
        ></InputNumber>
        <span style="margin:0 10px">~</span>
        <InputNumber
          v-model="exportface.end"
          style="width: 150px"
        ></InputNumber> -->
      <el-button
        type="primary"
        @click="handleDownload('all')"
        :loading="exportloading"
        >导出</el-button
      >
    </div>
  </div>
</template>
  
  <script>
import { ppeApis } from "@/api/libs.js";
import langmixin from "@/utils/langmixin.js";
import { projConfig } from "@/utils/config.js";
export default {
  mixins: [langmixin],
  props: ["deviceid", "group_id"],
  data() {
    return {
      config: projConfig,
      type: "import",
      file: null,
      filename: "",
      exportface: {
        start: 1,
        end: 1,
      },
      importloading: false,
      exportloading: false,
    };
  },
  methods: {
    handleSave() {
      this.importloading = true;
      let formData = new FormData();
      formData.append("package", this.file);
      formData.append("device_id", this.$props.deviceid);
      formData.append("group_id", this.$props.group_id);
      ppeApis
        .import(formData)
        .then((res) => {
          this.importloading = false;
          this.$emit("close-importppe");
          this.$message({
            type: "success",
            message: res.message[this.lang],
          });
        })
        .catch((err) => {
          this.importloading = false;
          if (err.error_code == -1) {
            let message = `<div>${err.message[this.lang]}`;
            for (let linenum in err.data) {
              message =
                message +
                `<br/>第${linenum}行，${err.data[linenum][this.lang]}`;
            }
            message = message + "</div>";
            this.$message({
              type: "error",
              message: message,
              duration: 0,
              showClose: true,
              dangerouslyUseHTMLString: true,
            });
            this.$emit("update-ppe-table");
          } else {
            this.$message({
              type: "error",
              message: err.message[this.lang],
              duration: 0,
              showClose: true,
            });
          }
        });
    },
    handleUpload(file) {
      let type = file.name.slice(file.name.length - 4);
      if (type != ".zip") {
        this.$message({
          type: "error",
          message: "文件类型错误",
        });
        return false;
      }
      this.file = file;
      this.filename = file.name;
      return false;
    },
    handleDownload(type) {
      if (type == "template") {
        ppeApis
          .export({ template: true, group_id: this.$props.group_id, device_id: this.$props.deviceid })
          .then((res) => {
            this.download(res, "ppe_template");
          })
          .catch((err) => {
            this.$message({
              type: "error",
              message: err.message[this.lang],
            });
          });
      } else {
        this.exportloading = true;
        ppeApis
          .export({
            device_id: this.$props.deviceid,
            group_id: this.$props.group_id,
          })
          .then((res) => {
            this.exportloading = false;
            this.$emit("close-importppe");
            this.download(res, `ppe_${this.$props.group_id}`);
            this.$message({
              type: "success",
              message: '下载成功',
            });
          })
          .catch((err) => {
            this.exportloading = false;
            this.$message({
              type: "error",
              message: err.message[this.lang],
            });
          });
      }
    },
    download(blob, filename) {
      var blob = new Blob([blob]);
      var url = URL.createObjectURL(blob);
      var a = document.createElement("a");
      a.href = url;
      a.download = `${filename}.zip`; // 设置下载的文件名
      a.click();
      window.URL.revokeObjectURL(url);
    },
  },
};
</script>
  
  <style lang="less" scope>
.upload-demo {
  width: calc(100% - 40rem);
}
.importlib {
  width: 100%;
  margin-left: 20rem;
  h3 {
    margin: 10rem 0px 20rem;
    font-size: 16rem;
    font-weight: normal;
    span {
      margin-right: 20rem;
      cursor: pointer;
    }
    span.active {
      border-bottom: 1px solid #2c6dd2;
      padding-bottom: 5px;
      color: #2c6dd2;
    }
  }
  .step-download {
    ul {
      list-style: none;
      margin-top: 10rem;
      font-size: 14rem;
    }
  }
  .step-upload {
    h4 {
      font-weight: normal;
      margin-top: 20rem;
      font-size: 14rem;
    }
  }
}
.exportface {
}
</style>