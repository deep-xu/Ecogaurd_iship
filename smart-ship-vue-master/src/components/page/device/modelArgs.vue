<template>
  <div class="modelArgs-container">
    <h3 style="margin-bottom: 5px; padding-left: 2px">高级设置</h3>
    <div
      style="width: 100%; height: 300px; overflow: auto; margin-bottom: 60px"
    >
      <el-form
        ref="formData"
        :model="formData"
        :rules="formRule"
        style="margin: 0 auto"
      >
        <div v-for="(model, key1) in renderParams.model_args" :key="key1">
          <el-form-item
            v-for="(item, key2) in model"
            :key="key1 + '_' + key2"
            style="margin-bottom: 0"
            :prop="'model_args[' + key1 + '][' + key2 + ']'"
            :rules="{
              validator: validates[key1 + '_' + key2](
                formData.model_args[key1],
                key2,
                key1
              ),
              trigger: 'change',
            }"
          >
            <div class="algcfg-item">
              <div class="label">
                {{ item.label }}
                <el-tooltip placement="right" v-if="item.tooltip">
                  <div style="margin-left: 2px; cursor: pointer">
                    <i class="z-iconfont z-icon-bangzhu"></i>
                  </div>
                  <div slot="content">
                    {{ item.tooltip }}
                  </div>
                </el-tooltip>
              </div>

              <el-input-number
                v-if="item.type == 'number'"
                :step="item.range.step"
                v-model="formData.model_args[key1][key2]"
                :placeholder="defaultFormData.model_args[key1][key2] + ''"
                style="width: 240px"
              ></el-input-number>
              <el-input
                v-if="item.type == 'text'"
                clearable
                v-model="formData.model_args[key1][key2]"
                style="width: 240px"
                :maxlength="item.maxLength"
                :ref="key1 + '_' + key2"
                @input="handleValueChg(key1, key2)"
                :placeholder="defaultFormData.model_args[key1][key2] + ''"
              ></el-input>
              <el-select
                v-if="item.type == 'select'"
                v-model="formData.model_args[key1][key2]"
                style="width: 240px"
                @change="handleValueChg"
              >
                <el-option
                  v-for="(option, index) in item.options"
                  :key="index + Math.random()"
                  :value="option.value"
                  :label="option.label"
                  >{{
                }}</el-option>
              </el-select>
              <div v-if="item.unit"  style="position: absolute;right: 5px;top: 32px;">{{ item.unit }}</div>
            </div>
          </el-form-item>
        </div>

        <div
          v-for="(item, key) in renderParams.reserved_args"
          :key="key + '_reserved_args'"
        >
          <template v-if="key != 'extra_model'">
            <el-form-item
              v-if="item.hide"
              style="margin-bottom: 0"
              :prop="'reserved_args[' + key + ']'"
              :rules="{
                validator: validates['reserved_args_' + key](
                  formData.reserved_args,
                  key,
                  'reserved_args'
                ),
                trigger: 'change',
              }"
            >
              <div class="algcfg-item">
                <div class="label">
                  {{ item.label }}
                  <el-tooltip placement="bottom" v-if="item.tooltip">
                    <div style="margin-left: 2px; cursor: pointer">
                      <i class="z-iconfont z-icon-bangzhu"></i>
                    </div>
                    <div slot="content">
                      {{ item.tooltip }}
                    </div>
                  </el-tooltip>
                </div>
                <el-input-number
                  v-if="item.type == 'number'"
                  :step="item.range.step"
                  v-model="formData.reserved_args[key]"
                  :placeholder="defaultFormData.reserved_args[key] + ''"
                  style="width: 240px"
                ></el-input-number>
                <el-input
                  v-if="item.type == 'text'"
                  clearable
                  v-model="formData.reserved_args[key]"
                  style="width: 240px"
                  :maxlength="item.maxLength"
                  :ref="key"
                  @input="handleValueChg('formData.reserved_args', key)"
                  :placeholder="defaultFormData.reserved_args[key] + ''"
                ></el-input>
                <el-select
                  v-if="item.type == 'select'"
                  v-model="formData.reserved_args[key]"
                  style="width: 240px"
                  @change="handleValueChg"
                >
                  <el-option
                    v-for="(option, index) in item.options"
                    :key="index + Math.random()"
                    :value="option.value"
                    :label="option.label"
                  ></el-option>
                </el-select>
                <div v-if="item.unit"  style="position: absolute;right: 5px;top: 32px;">{{ item.unit }}</div>
              </div>
            </el-form-item>
          </template>
          <template v-if="key == 'extra_model'">
            <div
              v-for="(item1, key1) in renderParams.reserved_args.extra_model"
              :key="key1 + '_reserved_args_extra_model'"
            >
              <el-form-item
                v-if="item1.hide"
                style="margin-bottom: 0"
                :prop="'reserved_args.extra_model[' + key1 + ']'"
                :rules="{
                  validator: validates['reserved_args_extra_model_' + key1](
                    formData.reserved_args.extra_model,
                    key1,
                    'reserved_args_extra_model'
                  ),
                  trigger: 'change',
                }"
              >
                <div class="algcfg-item">
                  <div class="label">
                    {{ item1.label }}
                    <el-tooltip placement="bottom-start" v-if="item1.tooltip">
                      <div style="margin-left: 2px; cursor: pointer">
                        <i class="z-iconfont z-icon-bangzhu"></i>
                      </div>
                      <div slot="content">
                        {{ item1.tooltip }}
                      </div>
                    </el-tooltip>
                  </div>
                  <el-input-number
                    :step="item1.range.step"
                    v-model="formData.reserved_args.extra_model[key1]"
                    :placeholder="
                      defaultFormData.reserved_args.extra_model[key1] + ''
                    "
                    style="width: 240px"
                  ></el-input-number>
                  <div v-if="item1.unit" style="position: absolute;right: 5px;top: 32px;">{{ item1.unit }}</div>
                </div>
              </el-form-item>
            </div>
          </template>
        </div>
      </el-form>
    </div>
    <el-button class="btn-finish" type="primary" @click="handleFinish"
      >完成</el-button
    >
  </div>
</template>

<script>
export default {
  props: ["modelArgs", "renderParams"],
  data() {
    const validate_threshold = (item, key, type) => {
      return (rule, value, callback) => {
        if (item.threshold == null) {
          callback(new Error(this.$t("source.thresholdnotnone")));
        }
        let range = this.renderParams[type].threshold.range;
        if (item.threshold < range.min) {
          callback(new Error(this.$t("source.cannotLessthan", [range.min])));
        }
        if (item.threshold > range.max) {
          callback(new Error(this.$t("source.cannotMorethan", [range.max])));
        }
        if (item.length < item.threshold) {
          callback(new Error(this.$t("source.thresholdnotequwindow")));
        } else {
          callback();
        }
      };
    };
    return {
      defaultFormData: {},
      formData: {
        model_args: {},
        reserved_args: {},
      },
      formRule: {},
      validates: {
        reserved_args_threshold: validate_threshold,
      },
    };
  },
  beforeMount() {
    this.formData.model_args = Object.assign(
      this.formData.model_args,
      this.$props.modelArgs.model_args
    );
    this.formData.reserved_args = Object.assign(
      this.formData.reserved_args,
      this.$props.modelArgs.reserved_args
    );
    this.defaultFormData = JSON.parse(JSON.stringify(this.formData));

    let models = Object.keys(this.formData.model_args);
    for (let i = 0; i < models.length; i++) {
      let args = Object.keys(this.formData.model_args[models[i]]);
      for (let j = 0; j < args.length; j++) {
        let validateKey = models[i] + "_" + args[j];
        if (!this.validates[validateKey]) {
          this.$set(this.validates, validateKey, this.validateCommon);
        }
      }
    }

    let reserved_args_keys = Object.keys(this.formData.reserved_args);
    for (let i = 0; i < reserved_args_keys.length; i++) {
      if (reserved_args_keys[i] != "extra_model") {
        let validateKey = "reserved_args_" + reserved_args_keys[i];
        if (!this.validates[validateKey]) {
          this.$set(this.validates, validateKey, this.validateCommon1);
        }
      } else {
        for (let tmp in this.formData.reserved_args.extra_model) {
          let validateKey = "reserved_args_extra_model_" + tmp;
          if (!this.validates[validateKey]) {
            this.$set(this.validates, validateKey, this.validateCommon1);
          }
        }
      }
    }
  },
  mounted() {},
  methods: {
    validateCommon(item, key2, key1) {
      if (this.renderParams.model_args[key1][key2].type == "number") {
        let range = this.renderParams.model_args[key1][key2].range;
        return (rule, value, callback) => {
          if (item[key2] === null) {
            callback(new Error("不能设置为空值"));
          } else if (item[key2] < range.min) {
            callback(new Error(`不能小于${range.min}`));
          } else if (item[key2] > range.max) {
            callback(new Error(`不能大于${range.max}`));
          } else {
            callback();
          }
        };
      } else {
        return (rule, value, callback) => {
          if (item[key2] === null) {
            callback(new Error("不能设置为空值"));
          } else {
            callback();
          }
        };
      }
    },
    validateCommon1(item, key, type) {
      if (type == "reserved_args") {
        if (this.renderParams[type][key].type == "number") {
          let range = this.renderParams[type][key].range;
          return (rule, value, callback) => {
            if (item[key] === null) {
              callback(new Error(`不能设置为空值`));
            } else if (item[key] < range.min) {
              callback(new Error(`不能小于${range.min}`));
            } else if (item[key] > range.max) {
              callback(new Error(`不能大于${range.max}`));
            } else {
              callback();
            }
          };
        } else {
          return (rule, value, callback) => {
            if (item[key] === null) {
              callback(new Error(`不能设置为空值`));
            } else {
              callback();
            }
          };
        }
      } else if (type == "reserved_args_extra_model") {
        if (
          this.renderParams["reserved_args"]["extra_model"][key].type ==
          "number"
        ) {
          let range =
            this.renderParams["reserved_args"]["extra_model"][key].range;
          return (rule, value, callback) => {
            if (item[key] === null) {
              callback(new Error(`不能设置为空值`));
            } else if (item[key] < range.min) {
              callback(new Error(`不能小于${range.min}`));
            } else if (item[key] > range.max) {
              callback(new Error(`不能大于${range.max}`));
            } else {
              callback();
            }
          };
        } else {
          return (rule, value, callback) => {
            if (item[key] === null) {
              callback(new Error(this.$t("common.notnull")));
            } else {
              callback();
            }
          };
        }
      }
    },
    handleValueChg(e, key) {
      this.$forceUpdate();
      this.$nextTick(() => {
        if (key) {
          let ref = this.$refs[e + "_" + key][0];
          ref.$el.focus();
        }
      });
    },

    handleFinish() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          let model_args = {
            type: this.$props.modelArgs.type,
            model_args: this.formData.model_args,
            reserved_args: this.formData.reserved_args
          };
          this.$emit("modelargsclose", model_args);
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
.modelArgs-container {
  .algcfg-item {
    .label {
      display: flex;
    }
  }
  /deep/.el-form-item {
    margin-bottom: 18rem !important;
  }
}
.btn-finish {
  position: absolute;
  bottom: 10px;
  right: 10px;
}
</style>