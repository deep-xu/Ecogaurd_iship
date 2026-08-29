<template>
  <div style="position: relative">
    <el-form
      ref="formValidate"
      :model="formData"
      :rules="formRule"
      label-position="top"
    >
      <el-form-item label="算法名称" prop="ch_name">
        <el-input
          v-model="formData.ch_name"
          readonly
          :maxlength="20"
        ></el-input>
      </el-form-item>
      <el-form-item label="算法描述" prop="desc">
        <el-input
          type="textarea"
          clearable
          class="descarea"
          v-model="formData.desc"
          :maxlength="400"
          :autosize="{ minRows: 5, maxRows: 10 }"
        ></el-input>
      </el-form-item>

      <div v-for="(item, key) in formData.model" :key="key">
        <template v-if="item.args && item.args.conf_thres !== undefined">
          <el-form-item
            :label="
              item.ch_name +
              '置信度(建议值：' +
              default_conf[key].conf_thres +
              ')'
            "
            :prop="'model.' + key + '.args.conf_thres'"
            :rules="{
              required: true,
              validator: validateConf,
              trigger: 'blur',
            }"
          >
            <el-input-number
              clearable
              v-model="item.args.conf_thres"
              :min="0"
              :max="1"
            ></el-input-number>
          </el-form-item>
        </template>
      </div>

      <el-form-item>
        <el-button
          type="primary"
          style="margin-right: 8px"
          :loading="loading"
          @click="handleSubmit('formValidate')"
          >确定</el-button
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
  props: ["formDataInit", "device_id"],
  data() {
    return {
      loading: false,
      formData: {
        id: "",
        name: "",
        ch_name: "",
        desc: "",
        model: null,
        conf: "",
        // sound_text: "",
      },
      default_conf: null,
      formRule: {},
      fileList: [],
    };
  },
  mounted() {
    this.formData.id = this.$props.formDataInit.id;
    this.formData.name = this.$props.formDataInit.name;
    this.formData.ch_name = this.$props.formDataInit.ch_name;
    this.formData.desc =
      this.$props.formDataInit.desc &&
      this.$props.formDataInit.desc.replaceAll(";", "\n");
    let default_model = JSON.parse(this.formDataInit.default_model);
    let default_conf = {};

    Object.keys(default_model).forEach((alg) => {
      if (default_model[alg].args) {
        default_conf[alg] = {
          conf_thres: default_model[alg].args.conf_thres,
        };
      }
    });

    this.default_conf = Object.assign({}, default_conf);

    this.formData.model = JSON.parse(
      JSON.stringify(this.$props.formDataInit.model)
    );
    this.$forceUpdate();
  },
  methods: {
    getDay(key) {
      let day = [
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
        "星期日",
      ];
      return day[key - 1];
    },
    getTime(time) {
      let tmp = time.map((item) => {
        let hour = parseInt(item / 3600);
        let minute = parseInt((item % 3600) / 60);
        let second = parseInt((item % 3600) % 60);
        hour = hour < 10 ? `0${hour}` : hour;
        minute = minute < 10 ? `0${minute}` : minute;
        second = second < 10 ? `0${second}` : second;
        return `${hour}:${minute}:${second}`;
      });
      return tmp;
    },
    handleAddPlan(item) {
      item.push(["00:00:00", "24:00:00"]);
      this.$forceUpdate();
    },
    handleCutPlan(item, index) {
      item.splice(index, 1);
      this.$forceUpdate();
    },
    handleSubmit() {
      this.$refs["formValidate"].validate((valid) => {
        if (valid) {
          this.loading = true;
          let conf_thres = {};
          let args = {};
          Object.keys(this.formData.model).forEach((alg) => {
            // args = this.formData.model[alg].args;
            args[alg] = this.formData.model[alg].args;
            // console.log(this.default_conf)
            // if (!conf_thres[alg]) {
            //   conf_thres[alg] = this.default_conf[alg].conf_thres;
            // }
          });
          algApis
            .edit({
              // id: this.formData.id,
              device_ids: [this.$props.device_id],
              name: this.formData.name,
              // ch_name: this.formData.ch_name,
              desc: this.formData.desc,
              // sound_text: this.formData.sound_text,
              // model: this.formData.model,
              args: args, //{conf_thres: conf_thres},
            })
            .then((res) => {
              this.loading = false;
              console.log("save alg", res);
              this.$message({
                type: "success",
                message: "更新算法成功!",
              });
              this.$emit("close-edit-alg");
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
      });
    },

    handleUpload(file) {
      this.formData.package = file;
      if (this.fileList.length > 0) {
        this.fileList.pop();
      }
      this.fileList.push(file);
      return false;
    },
    handleReset() {
      this.fileList.pop();
      this.formData.package = null;
    },
    handleNumchg(args) {
      console.log(args.conf_thres);
      args.conf_thres = parseFloat(args.conf_thres.toFixed(2));
      // console.log(args.conf_thres)
      this.$forceUpdate();
    },
    validateConf(rule, value, callback) {
      console.log(value);
      if (value === null || value == undefined) {
        callback(new Error("请输入置信度"));
      } else if (value === 0) {
        callback(new Error("置信度不能为0"));
      } else {
        callback();
      }
      // callback()
    },
    validateFaceConf(rule, value, callback) {
      console.log(value);
      if (value === null || value == undefined) {
        callback(new Error("请输入置信度"));
      } else if (value < 0.4) {
        callback(new Error("置信度不能小于0.4"));
      } else {
        callback();
      }
      // callback()
    },
  },
};
</script>
    
<style scoped lang="less">
.planItem {
  .ivu-form-item {
    margin-bottom: 0;
  }
}
.descarea /deep/.ivu-input {
  height: 120px;
}
</style>