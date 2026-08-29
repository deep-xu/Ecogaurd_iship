<template>
  <div class="editDevice">
    <el-form
      ref="formData"
      :model="formData"
      label-position="top"
      :rules="formRule"
      style="margin: 0 auto"
      label-width="100"
    >
      <!-- <el-form-item label="设备名称" prop="device_name">
        <el-input
          clearable
          v-model="formData.device_name"
          style="width: 150px"
          :maxlength="20"
        ></el-input>
      </el-form-item> -->
      <div style="font-size: 14rem">算法置信度配置</div>
      <ul>
        <li v-for="item1,index in formData.algList" :key="item1.key + index">
          <label v-if="item1.hasConf">{{ item1.name }}</label>
          <template v-if="formData.name != 'face'">
            <div v-for="(item, key) in formData.algList[index].model" :key="key">
              <template v-if="item.args && item.args.conf_thres !== undefined">
                <el-form-item
                  :label="
                    item.ch_name +
                    '置信度'
                  "
                  :prop="'algList[' + index + '].model.' + key + '.args.conf_thres'"
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
          </template>
          <template v-else>
            <div v-for="(item, key) in formData.algList[index].model" :key="key">
              <template v-if="item.args && item.args.similarity !== undefined">
                <el-form-item
                  :label="
                    item.ch_name +
                    '置信度'
                  "
                  :prop="'algList[' + index + '].model.' + key + '.args.similarity'"
                  :rules="{
                    required: true,
                    validator: validateFaceConf,
                    trigger: 'blur',
                  }"
                >
                  <el-input-number
                    clearable
                    v-model="item.args.similarity"
                    :max="0.95"
                  ></el-input-number>
                </el-form-item>
              </template>
            </div>
          </template>
        </li>
      </ul>
      <el-form-item>
        <el-button
          :disabled="formData.loading"
          :loading="formData.loading"
          type="primary"
          @click="handleSave('formData')"
          >保存</el-button
        >
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
export default {
  props: ["editDeviceInfo"],
  data() {
    return {
      formData: {
        loading: false,
        device_name: "",
        algList: [],
      },
      formRule: {},
    };
  },
  mounted() {
    this.formData.device_name = this.$props.editDeviceInfo.name;
    let algList = JSON.parse(JSON.stringify(this.$props.editDeviceInfo.algList));
    
    this.formData.algList = algList.map(item => {
      item.hasConf = false;
      for(let key in item.model){
        if(item.model[key].args){
          item.hasConf = true;
          break
        }
      }
      return item
    })
    console.log(this.formData.algList);
  },
  methods: {
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
        }
      });
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
    },
  },
};
</script>
<style lang="less" scoped>
</style>