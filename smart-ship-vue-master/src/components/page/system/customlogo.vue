<template>
  <div class="chglogo">
    <div class="table-container">
      <el-form
        ref="formValidate"
        :model="formData"
        :rules="formRule"
        label-width="100px"
        style="padding-top: 30px"
      >
        <el-form-item label="LOGO" prop="iconurl">
          <el-upload
            class="upload-logo"
            drag
            action="#"
            accept=".png,.jpg,.jpeg"
            :before-upload="handleUpload"
            :show-file-list="false"
          >
            <i v-if="!formData.iconurl" class="z-icon-pic"></i>
            <img
              v-else
              :src="formData.iconurl"
              alt=""
              style="width: 100px; height: 100px; object-fit: contain"
            />
          </el-upload>
        </el-form-item>
        <el-form-item label="Title" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="Please input title"
            style="width: 300px"
            maxlength="14"
          ></el-input>
        </el-form-item>
        <!-- <el-form-item label="简介"  prop="desc">
          <el-input
            v-model="formData.desc"
            type="textarea"
            :autosize="true"
            placeholder="请输入系统介绍"
            style="width: 300px"
            maxlength="200"
          ></el-input>
        </el-form-item> -->
        <el-form-item>
          <el-button
            type="primary"
            style="margin-right: 8px"
            :loading="loading"
            @click="handleSubmit"
            >Save</el-button
          >
          <el-button
            style="margin-right: 8px; background-color: #d61717"
            class="btn-delete"
            :loading="delloading"
            @click="handleDelLogo"
            >Delete LOGO</el-button
          >
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { customLogo } from "@/api/system";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  data() {
    return {
      loading: false,
      delloading: false,
      oriData: {
        title: "",
        desc: "",
      },
      formData: {
        icon: null,
        iconurl: "",
        bigLogo: null,
        bigLogoUrl: "",
        title: "",
        desc: "",
      },
      formRule: {
        // iconurl:[
        //   { required: true, message: '请上传LOGO1', trigger: 'blur' }
        // ],
        // bigLogoUrl:[
        //   { required: true, message: '请上传LOGO2', trigger: 'blur' }
        // ],
      },
    };
  },
  beforeMount() {
    this.getCustomLogo();
  },
  methods: {
    getCustomLogo() {
      customLogo
        .get()
        .then((res) => {
          this.formData.title = res.data.name;
          this.formData.desc = res.data.desc;
          this.oriData.title = res.data.name;
          this.oriData.desc = res.data.desc;
          if (res.data.icon) {
            this.formData.iconurl = "data:img/png;base64," + res.data.icon;
          } else {
            this.formData.iconurl = "";
          }
        })
        .catch((err) => {
          this.$message({
            type: "error",
            message: "Getting information failure",
          });
        });
    },
    handleUpload(file) {
      if (file.size > 1024 * 1024) {
        this.$message({
          type: "error",
          message: "Upload files with a size not exceeding 1M",
        });
        return false;
      }
      // let type = file.name.slice(file.name.length - 4);
      let type = file.name.split(".").pop();
      if (
        type.toLowerCase() != "png" &&
        type.toLowerCase() != "jpg" &&
        type.toLowerCase() != "jpeg"
      ) {
        this.$message({
          type: "error",
          message: "File type error! Please upload again!",
        });
        return false;
      }
      this.formData.icon = file;
      this.formData.iconurl = URL.createObjectURL(file);
      return false;
    },
    handleSubmit() {
      this.$refs.formValidate.validate((valid) => {
        if (valid) {
          this.loading = true;
          if (
            this.formData.title != this.oriData.title ||
            this.formData.desc != this.oriData.desc
          ) {
            customLogo
              .edit({
                name: this.formData.title,
                desc: this.formData.desc,
              })
              .then((res) => {
                this.loading = false;
                this.$message({
                  type: "success",
                  message: res.message[this.lang],
                });
                setTimeout(() => {
                  window.location.reload();
                }, 2000);
              })
              .catch((err) => {
                this.loading = false;
                if (err && err.message && err.message[this.lang]) {
                  this.$message({
                    type: "error",
                    message: err.message[this.lang],
                  });
                }
              });
          }

          if (this.formData.icon) {
            let formData = new FormData();
            formData.append("icon", this.formData.icon);
            customLogo
              .editIcon(formData)
              .then((res) => {
                this.loading = false;
                this.$message({
                  type: "success",
                  message: "Logo modification successful! CTRL+F5 refresh page takes effect!",
                });
                setTimeout(() => {
                  window.location.reload();
                }, 2000);
              })
              .catch((err) => {
                this.loading = false;
                this.$message({
                  type: "error",
                  message: "Failed to modify logo！",
                });
              });
          }

          if (
            this.formData.title == this.oriData.title &&
            this.formData.desc == this.oriData.desc &&
            !this.formData.icon
          ) {
            this.loading = false;
          }
        }
      });
    },
    handleDelLogo() {
      this.delloading = true;
      this.$confirm("Are you sure to delete LOGO?", "Delete LOGO", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          customLogo
            .delLogo()
            .then((res) => {
              this.delloading = false;
              this.getCustomLogo();
              this.$message({
                type: "success",
                message: "Delete successfully! CTRL+F5 refresh page takes effect!",
              });
              setTimeout(() => {
                window.location.reload();
              }, 2000);
            })
            .catch((err) => {
              this.delloading = false;
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              }
            });
        })
        .catch(() => {
          this.delloading = false;
        });
    },
  },
};
</script>

<style lang="less" scoped>
.chglogo {
  padding: 8rem 8rem 8rem;
}
.btn-delete {
  color: #fff;
  border-radius: 4px;
}
.upload-logo {
  width: 100px;
  height: 100px;
  /deep/.el-upload-dragger {
    width: 100px;
    height: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    i {
      font-size: 24rem;
    }
  }
  // /deep/.el-upload-list{
  //   display: none !important;
  // }
}
</style>