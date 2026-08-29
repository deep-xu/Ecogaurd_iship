<template>
  <div>
    <el-form
      ref="formData"
      :model="formData"
      :rules="formRule"
      style="padding: 50px 20rem"
    >
      <el-form-item
        label="人脸质量"
        prop="quality"
        :max="1"
        :rules="{
          required: true,
          validator: validateConf,
          trigger: 'blur',
        }"
      >
        <el-input-number
          v-model="formData.quality"
          type="number"
          placeholder="请输入人脸质量"
          style="width: 100%"
          :step="0.01"
        ></el-input-number>
      </el-form-item>
      <el-form-item style="text-align: right">
        <!-- <el-button @click="handleCancel" style="margin-right: 8px"
          >取消</el-button
        > -->
        <el-button type="primary" :loading="saveloading" @click="handleSave"
          >保存</el-button
        >
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { algApis } from "@/api/alg.js";
export default {
  props: ["deviceid"],
  data() {
    return {
      faceInfo: {
        name: "",
        ch_name: "",
        desc: "",
        sound_text: "",
        args: {
          face: { conf_thres: 0.5, quality: 0.6, similarity: 0.5 },
        },
      },
      formData: {
        quality: 0.8,
      },
      formRule: {},
      saveloading: false,
    };
  },
  mounted() {
    algApis.getTableData({ device_id: this.$props.deviceid }).then((res) => {
      let faceInfo = res[this.$props.deviceid].find((item) => item.name == "face");
      if (faceInfo) {
        faceInfo.model = JSON.parse(JSON.stringify(faceInfo.model));
        this.faceInfo.name = faceInfo.name;
        this.faceInfo.ch_name = faceInfo.ch_name;
        this.faceInfo.desc = faceInfo.desc;
        this.faceInfo.sound_text = faceInfo.sound_text;
        this.faceInfo.args.face = JSON.parse(
          JSON.stringify(faceInfo.model.face.args)
        );
        this.formData.quality = faceInfo.model.face.args.quality;
      } else {
        this.$message({
          message: "获取人脸质量失败",
          type: "error"
        });
      }
    });
  },
  methods: {
    validateConf(rule, value, callback) {
      if (value === null || value == undefined) {
        callback(new Error("请输入人人脸质量"));
      } else if (value === 0) {
        callback(new Error("人脸质量不能为0"));
      } else {
        callback();
      }
    },
    handleCancel() {
      this.$emit("cancel-facequality");
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.saveloading = true;
          this.faceInfo.args.face.quality = this.formData.quality;
          algApis
            .edit({
              device_ids:[this.$props.deviceid],
              name: this.faceInfo.name,
              ch_name: this.faceInfo.ch_name,
              desc: this.faceInfo.desc,
              sound_text: this.faceInfo.sound_text,
              args: this.faceInfo.args,
            })
            .then((res) => {
              this.saveloading = false;
              this.$emit("close-facequality");
              this.$message({
                type:"success",
                message: "修改人脸质量成功!"
              })
            })
            .catch((err) => {
              this.saveloading = false;
              this.$message({
                type:"error",
                message: "修改人脸质量失败!"
              })
            });
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
</style>