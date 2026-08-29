<template>
  <div class="adduser-page">
    <h3>{{ formDataInit.optype == "add" ? "添加PPE" : "编辑PPE" }}</h3>
    <el-form
      ref="formData"
      :model="formData"
      :rules="formRule"
      style="padding-top: 50px"
    >
      <el-form-item
        prop="fileurl"
        :rules="[
          {
            required: true,
            message: '请选择图片',
            trigger: 'blur',
          },
        ]"
      >
        <el-upload
          class="upload-demo"
          drag
          action="#"
          accept=".png,.jpg,.jpeg,.bmp"
          :show-file-list="false"
          :before-upload="handleUpload"
        >
          <img
            style="max-height: 200px"
            v-if="formData.file"
            :src="formData.fileurl"
          />
          <img
            style="max-height: 200px"
            v-else-if="!formData.file && formData.image"
            :src="formData.image"
          />
          <div v-else>
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="外部ID" prop="external_id">
        <el-input
          v-model="formData.external_id"
          placeholder="请输入外部ID"
          :max="30"
          :readonly="formDataInit.optype == 'edit'"
          :disabled="formDataInit.optype == 'edit'"
        ></el-input>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入名称"
          :max="30"
        ></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="desc">
        <el-input
          type="textarea"
          :maxlength="400"
          v-model="formData.desc"
          placeholder="请输入描述"
        ></el-input>
      </el-form-item>
      <el-form-item style="text-align: right">
        <el-button type="primary" :loading="saveloading" @click="handleSave"
          >保存</el-button
        >
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { ppeApis } from "@/api/libs.js";
import langmixin from "@/utils/langmixin.js"
export default {
  mixins: [langmixin],
  props: ["formDataInit", "group_id"],
  data() {
    return {
      oriData: {
        name: "",
        desc: "",
      },
      formData: {
        external_id:"",
        name: "",
        desc: "",
        age: 1,
        image: "",
        file: null,
        fileurl: "",
        fileErrMsg: "",
      },
      formRule: {
        name: [
          {
            required: true,
            message: "请输入名称",
            trigger: "blur",
          },
        ],
      },
      saveloading: false,
    };
  },
  mounted() {
    this.formData.external_id = this.$props.formDataInit.external_id;
    this.formData.name = this.$props.formDataInit.name;
    this.formData.desc = this.$props.formDataInit.desc;
    this.formData.image = this.$props.formDataInit.pic;
    this.formData.fileurl = this.$props.formDataInit.image;
    this.formData.group_id = this.$props.group_id;

    this.oriData = Object.assign(this.oriData, this.formData)
  },
  methods: {
    handleUpload(file) {
      if (file.size > 1024 * 1024 * 2) {
        this.$message({
          type: "error",
          message: "图像大小不超过2M",
        });
        return false;
      }
      this.formData.file = file;
      this.formData.fileurl = URL.createObjectURL(file);
      return false
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.saveloading = true;
          if (this.$props.formDataInit.optype == "add") {
            let params = {
              name: this.formData.name,
              desc: this.formData.desc || null,
              device_ids: [this.formDataInit.device_id],
              external_id: this.formData.external_id,
              group_id: this.$props.group_id
            };
            this.saveInfo(params);
          } else {
            if (
              this.formData.name != this.oriData.name ||
              this.formData.desc != this.oriData.desc
            ) {
              let params = {
                id: this.$props.formDataInit.id,
                name: this.formData.name,
                desc: this.formData.desc || null,
                device_ids: [this.formDataInit.device_id],
                external_id: this.formData.external_id,
                group_id: this.$props.group_id
              };
              this.saveInfo(params)
            } else {
              if(this.formData.file){
                this.saveImg(this.$props.formDataInit.id)
              }else{
                this.$emit("close-add-ppe");
              }              
            }
          }
        }
      });
    },
    saveInfo(params) {
      ppeApis
        .save(params)
        .then((res) => {
          console.log("save ppe", res)
          if (this.formData.file) {
            if(this.$props.formDataInit.id){
              this.saveImg(this.$props.formDataInit.id);
            }else{
              let device_id = this.$props.formDataInit.device_id
              let id = res[device_id].result == true ? res[device_id].id : "";
              if(id){
                this.saveImg(id);
              }
            }
          } else {
            this.$emit("close-add-ppe");
            let message = this.$props.formDataInit.optype == "add" ? "新增PPE成功" : "更新PPE成功"
            this.$message({
              type: "success",
              message: message,
            });
          }
        })
        .catch((err) => {
          console.log("save ppe err", err)
          this.saveloading = false;
          this.$message({
            type: "error",
            message: err.data[this.$props.formDataInit.device_id].reason[this.lang],
          });
        });
    },
    saveImg(id) {
      ppeApis
        .saveimg({
          device_ids:[this.$props.formDataInit.device_id],
          id: id,
          image: this.formData.file,
          external_id:this.formData.external_id
        })
        .then((res) => {
          this.$emit("close-add-ppe");
          this.$message({
            type: "success",
            message: "更新PPE成功！",
          });
        })
        .catch((err) => {
          this.$emit("close-add-ppe");
          this.$message({
            type: "error",
            message: err.data[this.$props.formDataInit.device_id].reason[this.lang],
          });
        });
    },
  },
};
</script>

<style lang="less" scoped>
.adduser-page {
  padding: 20rem;
  box-sizing: border-box;
  h3 {
    height: 40rem;
    line-height: 40rem;
    font-size: 16rem;
  }
  .ivu-input-number {
    width: 100%;
  }
  .upload-content {
    height: 200rem;
    margin: 10rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }
}
</style>