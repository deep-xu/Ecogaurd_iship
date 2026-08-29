<template>
  <div
    class="algcfg-container"
    :class="{ error: validateError == true }"
    @click="triggerCloseTree"
  >
    <el-form
      ref="formData"
      :model="formData"
      label-position="top"
      :rules="formRule"
      style="margin: 0 auto"
      v-if="formData.reserved_args"
    >
      <div style="margin: 10px 10px 10px 0; display: flex">
        {{ formData.reserved_args.ch_name }}
        <el-tooltip placement="bottom" v-if="algname == 'face'">
          <div style="margin-left: 2px; cursor: pointer">
            <i class="z-iconfont z-icon-bangzhu"></i>
          </div>
          <div slot="content">一张图片最多支持识别5个人脸</div>
        </el-tooltip>
      </div>
      <div class="advance-settings">
        <i
          class="z-iconfont z-icon-shijian"
          @click="handleSetPlan(algname)"
        ></i>
        <i
          class="z-iconfont z-icon-system"
          style="margin-left: 10rem"
          @click="handleSetModelargs(algname)"
          v-if="showAdvanceArgs"
        ></i>
      </div>

      <div v-if="renderParams.bbox.polygons || renderParams.bbox.lines">
        <el-button @click="handleSetArea(algname)">
          <span
            class="must"
            v-if="
              (renderParams.bbox.polygons &&
                renderParams.bbox.polygons.exits == 'must') ||
              (renderParams.bbox.lines &&
                renderParams.bbox.lines.exits == 'must')
            "
            >*</span
          >
          标定检测区域
        </el-button>
        <el-button
          style="margin-left: 10px"
          @click="handleSetArea(algname, 'see')"
          >查看检测区域</el-button
        >
      </div>
      <div v-if="formData.alg_type.indexOf('match_') >= 0">
        <el-form-item
          style="margin-bottom: 0"
          prop="reserved_args['group_id']"
          :rules="{
            validator: validateGroup,
            trigger: 'blur',
          }"
        >
          <div class="algcfg-item">
            <div class="label"><span class="must">*</span>选择底库组</div>
            <el-select
              v-model="formData.reserved_args.group_id"
              style="width: 150px"
              @change="handleGroupChg"
              placeholder=""
            >
              <el-option
                v-for="option in libGroup"
                :key="option.id"
                :value="option.id"
                :label="option.name"
                >{{ option.name }}</el-option
              >
            </el-select>
          </div>
        </el-form-item>
      </div>
      <el-form-item
        v-for="(item, key) in renderParams.alert_window"
        :key="key + '_alert_window'"
        style="margin-bottom: 0"
        :prop="'alert_window[' + key + ']'"
        :rules="{
          validator: validates['alert_window_' + key](
            formData.alert_window,
            key,
            'alert_window'
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
            v-model="formData.alert_window[key]"
            :placeholder="defaultFormData.alert_window[key] + ''"
            style="width: 150px"
          ></el-input-number>
          <el-input
            v-if="item.type == 'text'"
            clearable
            v-model="formData.alert_window[key]"
            style="width: 150px"
            :maxlength="item.maxLength"
            :ref="key"
            @input="handleValueChg('formData.reserved_args', key)"
            :placeholder="defaultFormData.alert_window[key] + ''"
          ></el-input>
          <el-select
            v-if="item.type == 'select'"
            v-model="formData.alert_window[key]"
            style="width: 150px"
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
          <div v-if="item.unit">{{ item.unit }}</div>
        </div>
      </el-form-item>
      <div
        v-for="(item, key) in renderParams.reserved_args"
        :key="key + '_reserved_args'"
      >
        <template v-if="key != 'extra_model'">
          <el-form-item
            v-if="!item.hide"
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
                style="width: 150px"
              ></el-input-number>
              <el-input
                v-if="item.type == 'text'"
                clearable
                v-model="formData.reserved_args[key]"
                style="width: 150px"
                :maxlength="item.maxLength"
                :ref="key"
                @input="handleValueChg('formData.reserved_args', key)"
                :placeholder="defaultFormData.reserved_args[key] + ''"
              ></el-input>
              <el-select
                v-if="item.type == 'select'"
                v-model="formData.reserved_args[key]"
                style="width: 150px"
                @change="handleValueChg"
              >
                <el-option
                  v-for="(option, index) in item.options"
                  :key="index + Math.random()"
                  :value="option.value"
                  :label="option.label"
                ></el-option>
              </el-select>
              <div v-if="item.unit">{{ item.unit }}</div>
            </div>
          </el-form-item>
        </template>
        <template v-if="key == 'extra_model'">
          <div
            v-for="(item1, key1) in renderParams.reserved_args.extra_model"
            :key="key1 + '_reserved_args_extra_model'"
          >
            <el-form-item
              v-if="!item1.hide"
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
                  <el-tooltip placement="bottom" v-if="item1.tooltip">
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
                  style="width: 150px"
                ></el-input-number>
                <div v-if="item1.unit">{{ item1.unit }}</div>
              </div>
            </el-form-item>
          </div>
        </template>
      </div>
      <el-form-item style="margin-bottom: 0" prop="hazard_level">
        <div class="algcfg-item">
          <div class="label">危险等级</div>
          <el-input
            v-model="formData.hazard_level"
            style="width: 150px"
            @input="handleValueChg"
          ></el-input>
        </div>
      </el-form-item>
    </el-form>

    <!-- 区域划分 -->
    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="drawVisible"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="808px"
    >
      <i class="z-icon-close" @click="drawVisible = false"></i>
      <lineAndPolygon
        v-if="drawVisible"
        @close="handleDrawClose"
        :areaInfo="areaInfo"
        :algInfo="formData"
        :deviceInfo="deviceInfo"
        :size="sourceinfo.draw_size"
        :renderParams="renderParams"
      ></lineAndPolygon>
    </el-dialog>

    <!-- 配置置信度 -->
    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="modelArgsVisible"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="300px"
    >
      <i class="z-icon-close" @click="modelArgsVisible = false"></i>
      <modelArgs
        v-if="modelArgsVisible"
        :modelArgs="modelArgs"
        :renderParams="renderParams"
        @modelargsclose="handleSetModelargsClose"
      ></modelArgs>
    </el-dialog>

    <!-- 布控计划 -->
    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="planVisible"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="375px"
    >
      <i class="z-icon-close" @click="planVisible = false"></i>
      <plan
        v-if="planVisible"
        @planclose="handlePlanClose"
        :planInfo="planInfo"
      ></plan>
    </el-dialog>
  </div>
</template>

<script>
// 画框等组件的信息都需要保存在算法配置中
import lineAndPolygon from "./lineAndPolygon.vue";
import modelArgs from "./modelArgs";
import plan from "./plan.vue";
import { Message } from "element-ui";
import { group } from "@/api/libs.js";
import { getAlgCfg } from "@/api/device.js";
export default {
  props: ["algname", "algcfg", "sourceinfo", "deviceInfo", "renderAlgs"],
  components: { plan, lineAndPolygon, modelArgs },
  data() {
    const validate_threshold = (item, key, type) => {
      return (rule, value, callback) => {
        if (item.threshold == null) {
          callback(new Error("告警阈值不能为空"));
        }
        let range = this.renderParams[type].threshold.range;
        if (item.threshold < range.min) {
          callback(new Error(`不能小于${range.min}`));
        }
        if (item.threshold > range.max) {
          callback(new Error(`不能大于${range.max}`));
        }
        if (item.length < item.threshold) {
          callback(new Error("告警阈值应小于等于告警窗口长度"));
        } else {
          callback();
        }
      };
    };
    return {
      drawVisible: false, // 画框显示
      areaInfo: {
        type: "",
        areas: null,
        rtsp: "", // 获取图片
        action: "see", // draw/see
        allAreas: null,
      },

      planVisible: false,
      planInfo: {
        type: "",
        plan: {},
      },

      modelArgsVisible: false,
      modelArgs: {},

      defaultFormData: {},
      formData: {},
      formRule: {},
      validates: {
        alert_window_threshold: validate_threshold,
        reserved_args_threshold: validate_threshold,
      },
      renderParams: {
        alert_window: {},
        reserved_args: {},
        model_args: {},
        bbox: {
          polygons: {
            exits: "optional",
          },
        },
      },

      validateError: false,
      libGroup: [],
      showAdvanceArgs: false
    };
  },
  watch: {
    formData: {
      deep: true,
      handler() {
        this.validateError = false;
      },
    },
  },
  beforeMount() {
    this.funRenderParams();
  },
  methods: {
    funRenderParams() {
      getAlgCfg(
        `/algsjson/${this.$props.deviceInfo.device_id}/${this.$props.algname}.json`
      ).then((res) => {
        this.defaultFormData = JSON.parse(JSON.stringify(res.basicParams));
        if (!this.$props.algcfg) {
          this.formData = Object.assign(
            this.formData,
            JSON.parse(JSON.stringify(res.basicParams))
          );
          if (!this.formData.model_args) {
            this.formData.model_args = {};
          }
          this.$emit("getalgcfg", {
            type: this.$props.algname,
            cfg: this.formData,
          });
        } else {
          this.formData.reserved_args = JSON.parse(
            JSON.stringify(res.basicParams.reserved_args)
          );
          let algcfg = JSON.parse(JSON.stringify(this.$props.algcfg));
          if (!algcfg.model_args) {
            algcfg.model_args = {};
          }
          for (let key in this.formData.reserved_args) {
            if (!algcfg.reserved_args[key]) {
              algcfg.reserved_args[key] = this.formData.reserved_args[key];
            }
          }
          this.formData = Object.assign(this.formData, algcfg);
        }

        this.renderParams = Object.assign(
          this.renderParams,
          JSON.parse(JSON.stringify(res.renderParams))
        );
        let alert_window_keys = Object.keys(this.formData.alert_window);
        for (let i = 0; i < alert_window_keys.length; i++) {
          let validateKey = "alert_window_" + alert_window_keys[i];
          if (!this.validates[validateKey]) {
            this.$set(this.validates, validateKey, this.validateCommon);
          }
        }
        let reserved_args_keys = Object.keys(this.formData.reserved_args);
        for (let i = 0; i < reserved_args_keys.length; i++) {
          if (reserved_args_keys[i] != "extra_model") {
            let validateKey = "reserved_args_" + reserved_args_keys[i];
            if (!this.validates[validateKey]) {
              this.$set(this.validates, validateKey, this.validateCommon);
            }
          } else {
            for (let tmp in this.formData.reserved_args.extra_model) {
              let validateKey = "reserved_args_extra_model_" + tmp;
              if (!this.validates[validateKey]) {
                this.$set(this.validates, validateKey, this.validateCommon);
              }
            }
          }
        }

        // 判断是否显示高级设置按钮
        if (Object.keys(this.formData.model_args).length != 0) {
          this.showAdvanceArgs = true;
        }
        for (let key in this.renderParams.reserved_args) {
          if (key != "extra_model") {
            if (this.renderParams.reserved_args[key].hide) {
              this.showAdvanceArgs = true;
            }
          } else {
            for (let key1 in this.renderParams.reserved_args["extra_model"]) {
              if (this.renderParams.reserved_args.extra_model[key1].hide) {
                this.showAdvanceArgs = true;
              }
            }
          }
        }

        this.$forceUpdate();
        this.getLibInfo();
      });
    },
    handleValueChg(e, key) {
      this.$forceUpdate();
      this.$nextTick(() => {
        if (key) {
          let ref = this.$refs[key][0];
          ref.$el.focus();
        }
      });
    },
    validateAll() {
      return new Promise((resolve, reject) => {
        this.$refs["formData"].validate((valid) => {
          if (valid) {
            if (
              this.renderParams.bbox.polygons &&
              this.renderParams.bbox.polygons.exits == "must"
            ) {
              if (
                this.formData.bbox.polygons &&
                this.formData.bbox.polygons.length == 0
              ) {
                let message = `${this.formData.reserved_args.ch_name}未绘制多边形区域`;
                this.$message({
                  type: "error",
                  message: message,
                });
                this.validateError = true;
                reject();
                return;
              }
            }

            if (
              this.renderParams.bbox.lines &&
              this.renderParams.bbox.lines.exits == "must"
            ) {
              if (
                this.formData.bbox.lines &&
                this.formData.bbox.lines.length == 0
              ) {
                let message = `${this.formData.reserved_args.ch_name}未绘制直线`;
                this.$message({
                  type: "error",
                  message: message,
                });
                this.validateError = true;
                reject();
                return;
              }
            }
            resolve({ type: this.$props.algname, cfg: this.formData });
          } else {
            this.validateError = true;
            reject();
          }
        });
      });
    },
    validateCommon(item, key, type) {
      if (type == "alert_window" || type == "reserved_args") {
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
    handleSetArea(alg, issee) {
      if (this.$props.sourceinfo.type == "camera") {
        if (this.$props.sourceinfo.protocol == "rtsp") {
          if (
            this.$props.sourceinfo.rtsptype == "gen" &&
            (!this.$props.sourceinfo.ipv4 ||
              !this.$props.sourceinfo.username ||
              !this.$props.sourceinfo.password ||
              !this.$props.sourceinfo.codeStream)
          ) {
            this.$message({
              type: "error",
              message: "请先将摄像头相关信息补齐",
            });
            return;
          } else if (
            this.$props.sourceinfo.rtsptype == "input" &&
            (!this.$props.sourceinfo.ipv4 || !this.$props.sourceinfo.stream)
          ) {
            this.$message({
              type: "error",
              message: "请先将摄像头相关信息补齐",
            });
            return;
          }
        } else {
          if (!this.$props.sourceinfo.stream) {
            this.$message({
              type: "error",
              message: "请检测摄像头状态",
            });
            return;
          }
        }
      } else {
        if (!this.$props.sourceinfo.stream) {
          this.$message({
            type: "error",
            message: "请先将视频流信息补齐",
          });
          return;
        }
      }
      this.areaInfo = {
        type: alg,
        areas:
          this.formData.bbox.polygons && this.formData.bbox.polygons.length > 0
            ? this.formData.bbox.polygons
            : null, // type对应的多边形标定区域
        lines:
          this.formData.bbox.lines && this.formData.bbox.lines.length > 0
            ? this.formData.bbox.lines
            : null, // 直线,
        allAreas: this.$props.sourceinfo.algscfg, //  所有算法的标定区域
        rtsp: this.$props.sourceinfo.stream, // 获取图片
        action: issee ? "see" : "draw", // draw/see
      };
      if (alg == "tarpaulin") {
        this.areaInfo.rects =
          this.formData && this.formData.rects ? this.formData.rects : null; // 矩形
      }
      this.drawVisible = true;
    },
    handleDrawClose(areaInfo) {
      // type、areas
      this.drawVisible = false;
      // 子组件传给父组件的数据
      if (areaInfo.lines) {
        this.formData.bbox.lines = areaInfo.lines;
      }
      if (areaInfo.areas) {
        this.formData.bbox.polygons =
          areaInfo.areas && areaInfo.areas.length > 0 ? areaInfo.areas : [];
      }
      this.validateError = false;
    },
    handleSetPlan(type) {
      let plan = {};
      plan = JSON.parse(JSON.stringify(this.formData.plan));
      this.planInfo = {
        type: type,
        plan: plan,
      };
      this.planVisible = true;
    },
    handlePlanClose(planInfo) {
      this.planVisible = false;
      this.formData.plan = JSON.parse(JSON.stringify(planInfo.plan));
      this.validateError = false;
    },
    handleSetModelargs(type) {
      let model_args = {},
        reserved_args = {};
      model_args = JSON.parse(JSON.stringify(this.formData.model_args));
      reserved_args = JSON.parse(JSON.stringify(this.formData.reserved_args));
      this.modelArgs = {
        type: type,
        model_args: model_args,
        reserved_args,
      };
      this.modelArgsVisible = true;
    },
    handleSetModelargsClose(Modelargs) {
      this.modelArgsVisible = false;
      this.formData.model_args = JSON.parse(
        JSON.stringify(Modelargs.model_args)
      );
      this.formData.reserved_args = Object.assign(this.formData.reserved_args, Modelargs.reserved_args)
      this.validateError = false;
    },
    triggerCloseTree() {
      this.$emit("closetree");
    },

    validateGroup(rule, value, callback) {
      if (this.formData.reserved_args.group_id) {
        callback();
      } else {
        callback("请选择底库组");
      }
    },
    getLibInfo() {
      if (this.formData.alg_type.indexOf("match_") >= 0) {
        let alg = this.formData.alg_type.replace("match_", "");
        group
          .getGroupList({
            alg: alg,
            device_id: this.$props.deviceInfo.device_id,
          })
          .then((res) => {
            this.libGroup = res.map((item) => {
              item.ext = JSON.parse(item.ext);
              return item;
            });
          });
      }
    },
    handleGroupChg(id) {
      this.formData.reserved_args.group_id = id;
      if (this.formData.alg_type == "match_open_lib") {
        for (let i = 0; i < this.libGroup.length; i++) {
          if (this.libGroup[i].id == this.formData.reserved_args.group_id) {
            this.formData.reserved_args.roi =
              this.libGroup[i].ext && this.libGroup[i].ext.roi
                ? this.libGroup[i].ext.roi
                : [];
            break;
          }
        }
      }
      this.$forceUpdate();
    },
  },
};
</script>

<style lang="less" scoped>
.algcfg-container {
  border: 1rem solid rgb(137, 140, 148);
  margin-bottom: 5rem;
  border-radius: 5rem;
  padding-left: 5rem;
  padding-bottom: 10rem;
  font-size: 14rem;
  position: relative;
  .must {
    color: red;
  }
  .advance-settings {
    position: absolute;
    right: 7rem;
    top: 5rem;
    cursor: pointer;
    font-size: 20rem;
  }
  .z-icon-bangzhu {
    font-size: 16rem;
  }
}
.algcfg-container.error {
  border-color: red;
}
.algcfg-container .algcfg-item {
  display: flex;
  margin-top: 10rem;
  overflow: hidden;
  font-size: 14rem;
  /deep/.el-input-number,
  /deep/.el-select {
    width: 200rem;
    .el-input {
      font-size: 14rem;
    }
  }
  /deep/.el-input__inner {
    font-size: 14rem;
  }
}

.algcfg-container .algcfg-item .label {
  width: 185rem;
  display: flex;
  font-size: 14rem;
}

.algcfg-container .algcfg-item.setpic {
  flex-direction: column;
}

.algcfg-container .algcfg-item.setpic .setpic-body {
  display: flex;
}

.algcfg-container.cfgerr {
  border-color: red;
}
</style>