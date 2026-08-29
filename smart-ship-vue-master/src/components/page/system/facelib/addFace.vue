<template>
  <div class="adduser-page">
    <h3>{{ formDataInit.optype == "add" ? "添加人脸" : "编辑人脸" }}</h3>
    <el-form
      ref="formData"
      :model="formData"
      :rules="formRule"
      style="padding-top: 50px"
    >
      <el-form-item
        prop="fileurl"
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
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入姓名"
          :max="30"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="年龄"
        prop="age"
        :rules="{ validator: validateAge, trigger: 'blur' }"
      >
        <el-input-number
          v-model="formData.age"
          type="number"
          placeholder="请输入年龄"
        ></el-input-number>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-select v-model="formData.sex">
          <el-option value="男">男</el-option>
          <el-option value="女">女</el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="简介" prop="desc">
        <el-input
          type="textarea"
          :maxlength="400"
          v-model="formData.desc"
          placeholder="请输入简介"
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
import { facelibApis } from "@/api/libs.js";
import langmixin from "@/utils/langmixin.js"
export default {
  mixins: [langmixin],
  props: ["formDataInit", "group_id"],
  data() {
    return {
      oriData: {
        name: "",
        age: 1,
        sex: "",
        desc: "",
      },
      formData: {
        external_id: "",
        name: "",
        age: 1,
        sex: "",
        desc: "",
        image: "",
        file: null,
        fileurl: "",
        fileErrMsg: "",
      },
      formRule: {
        name: [
          {
            required: true,
            message: "请输入姓名",
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
    this.formData.age = this.$props.formDataInit.age || 1;
    this.formData.sex = this.$props.formDataInit.sex;
    this.formData.desc = this.$props.formDataInit.desc;
    this.formData.image = this.$props.formDataInit.pic;
    this.formData.fileurl = this.$props.formDataInit.image;
    this.formData.group_id = this.$props.group_id;

    this.oriData = Object.assign(this.oriData, this.formData)
  },
  methods: {
    validateAge(rule, value, callback) {
      if (this.formData.age === null) {
        callback(new Error("年龄不能为空"));
      } else if (this.formData.age > 200) {
        callback(new Error("年龄不能大于200"));
      } else {
        callback();
      }
    },
    handleUpload(file) {
      if (file.size > 1024 * 1024 * 2) {
        this.$message({
          type: "error",
          message: "人脸图像大小不超过2M",
        });
        return false;
      }
      this.formData.file = file;
      this.formData.fileurl = URL.createObjectURL(file);
      return false;
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.saveloading = true;
          if (this.$props.formDataInit.optype == "add") {
            let params = {
              name: this.formData.name,
              age: this.formData.age,
              sex: this.formData.sex,
              desc: this.formData.desc || null,
              device_ids: [this.formDataInit.device_id],
              external_id: this.formData.external_id,
              group_id: this.$props.group_id
            };
            this.saveInfo(params);
          } else {
            if (
              this.formData.name != this.oriData.name ||
              this.formData.age != this.oriData.age ||
              this.formData.sex != this.oriData.sex ||
              this.formData.desc != this.oriData.desc
            ) {
              let params = {
                id: this.$props.formDataInit.id,
                name: this.formData.name,
                age: this.formData.age,
                sex: this.formData.sex,
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
                this.$emit("close-add-face");
              }              
            }
          }
        }
      });
    },
    saveInfo(params) {
      facelibApis
        .save(params)
        .then((res) => {
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
            this.$emit("close-add-face");
            let message = this.$props.formDataInit.optype == "add" ? "新增人脸成功" : "更新人脸成功"
            this.$message({
              type: "success",
              message: message,
            });
          }
        })
        .catch((err) => {
          this.saveloading = false;
          this.$message({
            type: "error",
            message: err.data[this.$props.formDataInit.device_id].reason[this.lang],
          });
        });
    },
    saveImg(id) {
      facelibApis
        .saveimg({
          device_ids:[this.$props.formDataInit.device_id],
          id: id,
          image: this.formData.file,
          external_id:this.formData.external_id
        })
        .then((res) => {
          this.$emit("close-add-face");
          this.$message({
            type: "success",
            message: "更新人脸成功！",
          });
        })
        .catch((err) => {
          this.$emit("close-add-face");
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