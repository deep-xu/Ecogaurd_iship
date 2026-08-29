<template>
  <div class="settings">
    <el-form
      :model="formData"
      ref="formData"
      :rules="formRule"
      :label-width="labelWidth"
    >
      <div class="sub-title">
        <h3>Platform Upgrade</h3>
      </div>
      <el-form-item
        label="Platform Version"
        prop="file"
        :rules="{ validator: validateFile, trigger: 'blur' }"
        class="sysversion"
      >
        <span>{{ sysversion }}</span>
      </el-form-item>
      <el-form-item
        label="Upload the upgrade package"
        prop="file"
        :rules="{ validator: validateFile, trigger: 'blur' }"
      >
        <el-upload
          class="upload-demo"
          drag
          action="#"
          accept=".zip"
          :before-upload="handleUpload"
          :show-file-list="false"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">Drag the file here, or click upload</div>
        </el-upload>
        <div>{{ formData.file && formData.file.name }}</div>
      </el-form-item>
      <el-form-item v-show="formData.file">
        <el-button
          type="primary"
          :disabled="formData.loading"
          :loading="formData.loading"
          @click="handleSave"
          >升级</el-button
        >
        <!-- <el-button @click="formData.file = null">撤销</el-button> -->
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { sysupgrade } from "@/api/system.js";
import { reboot } from "@/api/restart.js";
import bus from "@/utils/bus.js";
import langmixin from "@/utils/langmixin.js"
export default {
  mixins: [langmixin],
  data() {
    return {
      sysversion: "",
      formData: {
        loading: false,
        file: null,
      },
      formRule: {},
      labelWidth: "140px",
    };
  },
  mounted() {
    this.getSysVersion();
  },
  methods: {
    getSysVersion() {
      sysupgrade.version().then((res) => {
        this.sysversion = res.data;
      });
    },
    validateFile(rule, value, callback) {
      if (!this.formData.file) {
        callback(new Error("Please select the platform upgrade package"));
      } else {
        callback();
      }
    },
    handleUpload(file) {
      let type = file.name.slice(file.name.length - 4);
      if (type != ".zip") {
        this.$message({
          type: "error",
          message: "File type error! Please upload again!",
        });
        return false;
      }
      this.formData.file = file;
      return false;
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.$confirm("After the upgrade is successful, the platform will automatically restart. Are you sure about the upgrade？", "upgrade", {
            confirmButtonText: "Confirm",
            cancelButtonText: "Cancel",
          })
            .then(() => {
              this.formData.loading = true;
              let formData = new FormData();
              formData.append("package", this.formData.file);
              sysupgrade
                .upgrade(formData)
                .then((res) => {
                  this.formData.loading = false;
                  this.formData.file = null;
                  setTimeout(() => {
                    reboot();
                    bus.$emit("sys-reboot");
                  }, 1000);

                  this.$message({
                    type: "success",
                    message: res.message[this.lang],
                  });
                })
                .catch((err) => {
                  this.formData.loading = false;
                  
                });
            })
            .catch(() => {});
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
.settings {
  padding-top: 50rem;
}
.sysversion {
  font-size: 14rem;
  /deep/.el-form-item__content {
    line-height: 32rem;
    font-size: 14rem;
  }
}

/deep/.el-form {
  width: 600rem;
}
/deep/.el-form .el-form-item--small .el-form-item__content {
  height: auto;
}

/deep/.el-upload-list__item:hover {
  background-color: transparent;
}
.el-upload-dragger .el-icon-upload {
  font-size: 60rem;
}
.el-upload-dragger .el-upload__text {
  font-size: 14rem;
}
</style>