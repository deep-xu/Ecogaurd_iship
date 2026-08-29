<template>
  <div>
    <el-form
      ref="formData"
      :model="formData"
      label-position="top"
      :rules="formRule"
      style="width: 90%; margin: 0 auto"
    >
      <el-form-item label="类型" prop="type">
        <el-select v-model="formData.type">
          <el-option value="camera" label="摄像头"></el-option>
          <el-option value="stream" label="视频流"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item
        label="协议"
        prop="protocol"
        v-if="formData.type == 'camera'"
      >
        <el-select
          v-model="formData.protocol"
          :readonly="!!onvifInfo.Manufacturer"
          :disabled="!!onvifInfo.Manufacturer"
          @change="handleProrocolChg"
        >
          <el-option value="onvif">onvif</el-option>
          <el-option value="rtsp">rtsp</el-option>
        </el-select>
      </el-form-item>
      <el-form-item
        :label="formData.type == 'camera' ? '摄像头ID（自定义，不可重复，可不填）' : '视频流ID（自定义，不可重复，可不填）'"
        prop="id"
        :rules="{ validator: validateId, trigger: 'blur' }"
      >
        <el-input
          v-model="formData.id"
          :maxlength="30"
          :readonly="formData.optype == 'edit'"
        ></el-input>
      </el-form-item>
      <el-form-item :label="formData.type == 'camera' ? '摄像头描述（自定义）' : '视频流描述（自定义）'" prop="desc">
        <el-input clearable v-model="formData.desc" :maxlength="20"></el-input>
      </el-form-item>
      <el-form-item
        label="方式"
        prop="rtsptype"
        v-if="formData.type == 'camera' && formData.protocol == 'rtsp'"
      >
        <el-radio-group v-model="formData.rtsptype" @change="handleRtsptypeChg">
          <el-radio label="gen">自动生成</el-radio>
          <el-radio label="input">手动输入</el-radio>
        </el-radio-group>
      </el-form-item>
      <!-- 摄像头 onvif -->
      <div v-if="formData.type == 'camera' && formData.protocol == 'onvif'">
        <el-form-item
          label="摄像头IP"
          prop="ipv4"
          v-if="formData.type == 'camera'"
          :rules="{ required: true, validator: validateIp, trigger: 'blur' }"
        >
          <el-input
            v-if="!onvifInfo.Manufacturer"
            clearable
            v-model.trim="formData.ipv4"
          ></el-input>
          <el-input
            v-else
            clearable
            v-model.trim="formData.ipv4"
            disabled readonly
          ></el-input>
        </el-form-item>
        <el-form-item
          label="Port"
          prop="port"
          v-if="formData.type == 'camera' && formData.protocol == 'onvif'"
          :rules="{ required: true, validator: validatePort, trigger: 'blur' }"
        >
          <el-input-number
            v-if="!onvifInfo.Manufacturer"
            clearable
            v-model="formData.port"
          ></el-input-number>
          <el-input-number
            v-else
            readonly
            disabled
            v-model="formData.port"
          ></el-input-number>
        </el-form-item>
        <el-form-item label='摄像头账号（请勿包含 " / \ + : 等特殊字符）' prop="username">
          <el-input
            v-if="!onvifInfo.Manufacturer"
            clearable
            v-model="formData.username"
          ></el-input>
          <el-input
            v-else
            readonly
            disabled
            v-model.trim="formData.username"
          ></el-input>
        </el-form-item>
        <el-form-item label='摄像头密码（请勿包含 " / \ + : 等特殊字符）' prop="password">
          <el-input
            v-if="!onvifInfo.Manufacturer"
            show-password
            type="password"
            v-model.trim="formData.password"
          ></el-input>
          <el-input
            v-else
            show-password
            type="password"
            v-model.trim="formData.password"
            readonly
            disabled
          ></el-input>
        </el-form-item>
        <el-form-item label="检测是否在线" v-if="!onvifInfo.Manufacturer">
          <div style="display: flex">
            <el-button
              type="primary"
              style="margin-left: 10px"
              @click="handleGetOnvifinfo()"
              :disabled="getOnvifinfoLoading"
              :loading="getOnvifinfoLoading"
              >检测</el-button
            >
          </div>
        </el-form-item>
        <div class="cameraInfo" v-if="onvifInfo.Manufacturer">
          <h3>设备信息</h3>
          <ul style="list-style: none">
            <li style="width: 50%">
              <label>厂商:</label>
              <span>{{ onvifInfo.Manufacturer }}</span>
            </li>
            <li style="width: 50%">
              <label>型号:</label>
              <span>{{ onvifInfo.Model }}</span>
            </li>
            <li style="width: 100%">
              <label>序列号:</label>
              <span>{{ onvifInfo.SerialNumber }}</span>
            </li>
            <li style="width: 100%">
              <label>rtsp:</label>
              <span>{{ formData.stream }}</span>
            </li>
          </ul>
        </div>

        <div class="onvifInfo">
          <div
            v-for="(item, index) in onvifInfo.profiles"
            :key="index"
            @click="
              formData.stream = item.rtsp;
              getCameraSize();
            "
            :class="{ active: item.rtsp == formData.stream }"
          >
            <h3>{{ item.label }}</h3>
            <ul>
              <li>
                <label>分辨率：</label>
                <span>{{ item.width }}*{{ item.height }}</span>
              </li>
              <li>
                <label>帧率(fps)：</label>
                <span>{{ item.frame_rate }}</span>
              </li>
              <li>
                <label>码率(kbps)：</label>
                <span>{{ item.bit_rate }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <!-- 摄像头、rtsp、手动输入 -->
      <div
        v-else-if="
          formData.type == 'camera' &&
          formData.protocol == 'rtsp' &&
          formData.rtsptype == 'input'
        "
      >
        <el-form-item
          label="摄像头IP"
          prop="ipv4"
          v-if="formData.type == 'camera'"
          :rules="{ required: true, validator: validateIp, trigger: 'blur' }"
        >
          <el-input clearable v-model="formData.ipv4"></el-input>
        </el-form-item>
        <el-form-item
          label='RTSP（摄像头账号密码请勿包含 " / \ + : 等特殊字符）'
          prop="stream"
          :rules="{
            required: true,
            validator: validateStream,
            trigger: 'blur',
          }"
        >
          <el-input v-model.trim="formData.stream"></el-input>
        </el-form-item>
        <el-form-item
          label="检测是否在线"
          prop="width"
          :rules="{ required: true, validator: validateWidth, trigger: 'change' }"
        >
          <div style="display: flex">
            <el-button
              type="primary"
              style="margin-left: 10px"
              @click="
                handleGetCameraSize();
              "
              :disabled="getCameraSizeLoading"
              :loading="getCameraSizeLoading"
              >检测</el-button
            >
            <div
              class="streamStatus"
              :class="{
                online: streamStatus == '在线',
                offline: streamStatus == '离线',
              }"
            >
              {{ streamStatus }}
            </div>
          </div>
        </el-form-item>
      </div>
      <!-- 原流程 -->
      <div v-else>
        <el-form-item
          label="摄像头IP"
          prop="ipv4"
          v-if="formData.type == 'camera'"
          :rules="{ required: true, validator: validateIp, trigger: 'blur' }"
        >
          <el-input clearable v-model.trim="formData.ipv4"></el-input>
        </el-form-item>
        <el-form-item
          label='摄像头账号（请勿包含 " / \ + : 等特殊字符）'
          prop="username"
          v-if="formData.type == 'camera' && formData.brand != '8'"
        >
          <el-input clearable v-model.trim="formData.username"></el-input>
        </el-form-item>
        <el-form-item
          label='摄像头密码（请勿包含 " / \ + : 等特殊字符）'
          prop="password"
          v-if="formData.type == 'camera' && formData.brand != '8'"
        >
          <el-input
            show-password
            type="password"
            v-model.trim="formData.password"
          ></el-input>
        </el-form-item>
        <el-form-item
          label="摄像头品牌"
          prop="brand"
          v-if="formData.type == 'camera'"
        >
          <el-select v-model="formData.brand">
            <el-option
              v-for="item in cameraList"
              :value="item.value"
              :label="item.label"
              :key="item.value + '_brand'"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="formData.type == 'camera' && formData.brand != '8'"
          label="选择码流"
          prop="codeStream"
        >
          <el-select v-model="formData.codeStream">
            <el-option
              v-for="item in codeStreamList"
              :value="item.value"
              :label="item.label"
              :key="item.value + '_codeStream'"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="
            (formData.type == 'camera' && formData.brand == '8') ||
            formData.type == 'stream'
          "
          label="视频流地址"
          prop="stream"
          :rules="{
            required: true,
            validator: validateStream,
            trigger: 'blur',
          }"
        >
          <div style="position: absolute; top: -42rem; left: 85rem">
            <el-tooltip placement="bottom">
              <div style="cursor: pointer">
                <i class="z-iconfont z-icon-bangzhu"></i>
              </div>
              <div slot="content">支持rtsp、rtmp流地址</div>
            </el-tooltip>
          </div>
          <el-input v-model.trim="formData.stream"></el-input>
        </el-form-item>
        <el-form-item
          label="检测是否在线："
          prop="width"
          :rules="{ required: true, validator: validateWidth, trigger: 'change' }"
        >
          <div style="display: flex; align-items: center">
            <el-button
              type="primary"
              style="margin-left: 10rem"
              @click="
                handleGetCameraSize();
              "
              :disabled="getCameraSizeLoading"
              :loading="getCameraSizeLoading"
              >检测</el-button
            >
            <div
              class="streamStatus"
              :class="{
                online: streamStatus == '在线',
                offline: streamStatus == '离线',
              }"
            >
              {{ streamStatus }}
            </div>
          </div>
        </el-form-item>
      </div>

      <el-form-item
        style="margin-bottom: 0"
        prop="video_record"
        v-show="supportVideoRecord"
      >
        <div class="algcfg-item" style="display: flex">
          <div class="label">告警视频录制</div>
          <el-switch
            v-model="formData.video_record"
            size="small"
            style="margin-top: 6px; margin-left: 30px"
          ></el-switch>
        </div>
      </el-form-item>
      <el-form-item
        style="margin-bottom: 0"
        prop="rtsp_transport"
        v-show="supportVideoRecord"
      >
        <div class="algcfg-item" style="display: flex">
          <div class="label">开启TCP推流</div>
          <el-switch
            v-model="formData.rtsp_transport"
            size="small"
            style="margin-top: 6px; margin-left: 30px"
          ></el-switch>
        </div>
      </el-form-item>

      <el-form-item label="选择算法" prop="alg" class="selalgs-container">
        <div @click="handleSelAlg" class="algsnames">
          <ul>
            <li v-for="(item, key) in formData.algscfg" :key="key">
              {{ item && item.reserved_args && item.reserved_args.ch_name }}
              <i
                @click.stop="handleDelAlg(key)"
                class="z-iconfont z-icon-close"
                style="cursor: pointer"
              ></i>
            </li>
          </ul>
          <i class="z-iconfont z-icon-xuanze"></i>
          <!-- <i v-if="!showAlgtree" class="z-iconfont z-icon-zhedie1"></i>
          <i v-if="showAlgtree" class="z-iconfont z-icon-zhankai1"></i> -->
        </div>
        <!-- <div v-if="showAlgtree" class="algtree-container">
          <el-tree
            ref="algtree"
            :data="algTree"
            :props="algTreeProps"
            show-checkbox
            node-key="id"
            multiple
            @check-change="handleAlgsChg"
            @node-click="handleAlgfold"
            :default-expanded-keys="defaultExpended"
            :default-checked-keys="defaultCheckAlgs"
          ></el-tree>
        </div> -->
      </el-form-item>

      <div v-for="(item, key) in formData.algscfg" :key="'alg_' + key">
        <algcfg
          :ref="'alg_' + key"
          :algname="key"
          :sourceinfo="formData"
          :algcfg="item"
          :deviceInfo="deviceInfo"
          @closetree="showAlgtree = false"
          @getalgcfg="handleGetalgcfg"
        ></algcfg>
      </div>

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

    <!-- 配置置信度 -->
    <el-dialog
      title=""
      :append-to-body="true"
      :visible.sync="showAlgtree"
      class="draw-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
      width="800px"
    >
      <i class="z-icon-close" @click="showAlgtree = false"></i>
      <algSel v-if="showAlgtree" :algTree="algTree" @on-check-change="handleAlgsChg"  @on-close-sel="showAlgtree = false"></algSel>
    </el-dialog>
  </div>
</template>
  
  <script>
import algcfg from "./algcfg.vue";
import algSel from './algSel.vue';
import { sourceApis } from "@/api/device.js";
import { algApis } from "@/api/alg.js";
import { config } from "@/utils/config.js";
import { getAlgDefaultParams } from "./algparams.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["formDataInit", "locations", "deviceInfo"],
  components: { algcfg, algSel },
  data() {
    const isExistLocation = (rule, value, callback) => {
      let locations = this.$props.locations.filter(
        (item) => item != this.$props.formDataInit.desc
      );
      if (locations.indexOf(value) >= 0) {
        callback(new Error("描述已存在"));
      } else {
        callback();
      }
    };
    return {
      api: sourceApis,
      config: config,
      streamStatus: "",
      formData: {
        optype: "add", // or edit
        protocol: "rtsp",
        port: 80,
        rtsptype: "gen",
        id: "",
        encoding: "",
        alg: {},
        brand: "", // 摄像头id
        fps: 25, // 摄像头帧率
        id: null,
        ipv4: "", // ip
        desc: "",
        password: "",
        stream:
          "rtsp://admin:Admin123@192.168.1.252:554/h264/ch1/main/av_stream",
        codeStream: "", // 码流
        status: true,
        statusDesc: "",
        username: "",
        loading: false,
        video_code: "",
        draw_size: null,
        width: null,
        height: null,
        algscfg: {},
        video_record: false,
        rtsp_transport: true
      },
      formRule: {
        desc: [
          { required: true, message: "请输入描述", trigger: "blur" },
          { validator: isExistLocation, trigger: "blur" },
        ],
        username: [
          { required: true, message: "请输入摄像头账号", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入摄像头密码", trigger: "blur" },
        ],
        brand: [
          { required: true, message: "请选择摄像头品牌", trigger: "blur" },
        ],
        codeStream: [
          { required: true, message: "请输入摄像头码流", trigger: "blur" },
        ],
        width: [
          {
            validator: (rule, value, callback) => {
              if (this.formData.width) {
                callback();
              } else {
                let msg = "请检测视频流状态";
                if (this.formData.type == "camera") {
                  let msg = "请检测摄像头状态";
                }
                callback(new Error(msg));
              }
            },
            trigger: "change",
          },
        ],
      },

      cameraList: [
        { value: "2", label: "海康威视" },
        { value: "9", label: "大华" },
        { value: "4", label: "宇视" },
        { value: "1", label: "华为" },
        // { value: "10", label: "中维世纪" },
        // { value: "3", label: "英飞拓" },
        // { value: "5", label: "LG" },
        // { value: "6", label: "派尔高网络摄像机" },
        // { value: "7", label: "三星" },
        // { value: "8", label: "其他品牌" },
      ],
      codeStreamList: [], // 码流下拉选项

      algList: [], // 所有算法数组
      algTree: [], // 算法按不同组 组成树结构
      showAlgtree: false,
      algTreeProps: {
        label: "title",
        children: "children",
      },
      defaultCheckAlgs: [],
      defaultExpended: [],
      showAlgtree: false,

      drawVisible: false, // 画框显示
      areaInfo: {
        type: "",
        areas: null,
        rtsp: "", // 获取图片
        action: "see", // draw/see
        allAreas: null,
      },

      getCameraSizeLoading: false,
      planVisible: false,
      planInfo: {
        type: "",
        plan: {},
      },

      supportVideoRecord: false,

      getOnvifinfoLoading: false,
      onvifInfo: {
        profiles: [],
      },
    };
  },
  watch: {
    "formData.brand"(brand) {
      switch (brand) {
        case "1": // 华为
          this.codeStreamList = [
            { value: "/LiveMedia/ch1/Media1", label: "主码流" },
            { value: "/LiveMedia/ch1/Media2", label: "子码流" },
          ];
          break;
        case "2": // 海康
          this.codeStreamList = [
            {
              value: ":554/h264/ch1/main/av_stream",
              label: "主码流（摄像头）",
            },
            { value: ":554/h264/ch1/sub/av_stream", label: "子码流（摄像头）" },
            // {
            //   value: ":554/h264/ch34/main/av_stream",
            //   label: "主码流（硬盘录像机）",
            // },
            // {
            //   value: ":554/h264/ch34/sub/av_stream",
            //   label: "子码流（硬盘录像机）",
            // },
          ];
          break;
        case "3": // 英飞拓
          this.codeStreamList = [
            { value: "/1/1080p", label: "主码流（球机）" },
            { value: "/1/D1", label: "子码流（球机）" },
            { value: ":554/1/h264major", label: "主码流（单播摄像头）" },
            { value: ":554/1/h264minor", label: "子码流（单播摄像头）" },
            {
              value: ":554/1/h264major/multicast",
              label: "主码流（组播摄像头）",
            },
            {
              value: ":554/1/1/h264minor/multicast",
              label: "子码流（组播摄像头）",
            },
          ];
          break;
        case "4": //宇视
          this.codeStreamList = [
            { value: "/media/video1/multicast", label: "1号码流" },
            { value: "/media/video2/multicast", label: "2号码流" },
            { value: "/media/video3/multicast", label: "3号码流" },
          ];
          break;
        case "5": // LG
          this.codeStreamList = [
            { value: ":554/Master-0", label: "主码流" },
            { value: ":554/Slave-0", label: "子码流" },
          ];
          break;
        case "6": // 派尔高网络摄像机
          this.codeStreamList = [{ value: "/h264", label: "主码流" }];
          break;
        case "7": // 三星
          this.codeStreamList = [
            {
              value: ":554/onvif/profile2/media.smp（720P）",
              label: "主码流（单播）",
            },
            { value: ":554/onvif/profile3/media.smp", label: "子码流（单播）" },
            {
              value: ":554/onvif/multicast/profile2/media.smp (720p)",
              label: "主码流（组播）",
            },
            {
              value: ":554/onvif/multicast/profile3/media.smp",
              label: "子码流（组播）",
            },
          ];
          break;
        case "9": // 大华
          this.codeStreamList = [
            // {
            //   value: ":554/cam/realmonitor?channel=0&subtype=0",
            //   label: "主码流（摄像头）",
            // },
            // {
            //   value: ":554/cam/realmonitor?channel=0&subtype=1",
            //   label: "子码流（摄像头）",
            // },
            {
              value: ":554/cam/realmonitor?channel=1&subtype=0",
              label: "主码流（摄像头）",
            },
            {
              value: ":554/cam/realmonitor?channel=1&subtype=1",
              label: "子码流（摄像头）",
            },
          ];
          break;
        case "10": // 中维世纪
          this.codeStreamList = [
            { value: ":8554/live0.264", label: "主码流" },
            { value: ":8554/live1.264", label: "子码流" },
            // { value: ":8554/profile0", label: "主码流" },
          ];
          break;
        default: //其他
          this.formData.codeStream = "";
          this.formData.stream = "";
          this.codeStreamList = [];
          break;
      }
      this.formData.codeStream =
        this.codeStreamList.length > 0 ? this.codeStreamList[0].value : "";
      this.$nextTick(() => {
        if (brand && brand != 8) {
          this.$refs.formData.validateField("codeStream");
        }
        this.clearCameraSize();
      });
    },

    "formData.ipv4"() {
      if (
        this.formData.type == "camera" &&
        this.formData.protocol == "rtsp" &&
        this.formData.rtsptype == "gen"
      ) {
        this.setStream();
        // this.formData.stream = `rtsp://${this.formData.username}:${this.formData.password}@${this.formData.ipv4}${this.formData.codeStream}`;
        this.clearCameraSize();
      }
    },
    "formData.username"() {
      this.setStream();
      // this.formData.stream = `rtsp://${this.formData.username}:${this.formData.password}@${this.formData.ipv4}${this.formData.codeStream}`;
    },
    "formData.password"() {
      this.setStream();
      // this.formData.stream = `rtsp://${this.formData.username}:${this.formData.password}@${this.formData.ipv4}${this.formData.codeStream}`;
    },
    "formData.codeStream"() {
      if (this.formData.codeStream) {
        this.setStream();
        // this.formData.stream = `rtsp://${this.formData.username}:${this.formData.password}@${this.formData.ipv4}${this.formData.codeStream}`;
        this.clearCameraSize();
      }
    },
    showAlgtree(newdata, olddata) {
      if (newdata == true) {
        this.chgTreeNodeSel();
      }
    },
  },
  beforeMount() {
    let deviceType = this.$props.deviceInfo.hardware_version;
    let maxsource = this.$props.deviceInfo.max_source;
    if (
      (deviceType.toLocaleLowerCase().indexOf("ks968") >= 0 && maxsource <= 16) ||
      deviceType.toLocaleLowerCase().indexOf("ks988") >= 0 || deviceType.toLocaleLowerCase().indexOf("ks916") >= 0
    ) {
      this.supportVideoRecord = true;
    }
  },
  mounted() {
    algApis
      .getTableData({ device_id: this.$props.deviceInfo.device_id })
      .then((res) => {
        let algs = JSON.parse(
          JSON.stringify(res[this.$props.deviceInfo.device_id])
        ).sort((a,b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
        if (algs.length > 0) {
          algs = algs.map((item, index) => {
            item.id = index;
            return item;
          });
          this.algList = algs.map((item) => {
            return {
              id: item.id,
              alg: item.name,
              ch_name: item.ch_name,
            };
          }).sort((a,b) => a.alg.toLowerCase().localeCompare(b.alg.toLowerCase()));
          let selAlgs = [];
          let algTreeObj = {};
          if (this.$props.formDataInit.optype == "edit") {
            selAlgs = Object.keys(this.$props.formDataInit.alg);
          }
          let checkAlgs = [];
          for (let i = 0; i < algs.length; i++) {
            let groupname = algs[i].group_name;
            if (!algTreeObj[groupname]) {
              algTreeObj[groupname] = {
                expanded: false,
                title: groupname,
                id: groupname + "_" + i,
                children: [],
              };
            }
            // 判断是否是编辑摄像头数据中已有的算法
            let checked = false;
            if (selAlgs.indexOf(algs[i].name) >= 0) {
              checked = true
              checkAlgs.push(algs[i].id);
            }
            let alg = Object.assign({}, algs[i], {
              title: algs[i].ch_name,
              checked: checked,
            });
            algTreeObj[groupname].children.push(alg);
          }
          this.algTree = [];
          Object.keys(algTreeObj).sort((a,b) => a.localeCompare(b)).forEach(groupname => {
            this.algTree.push(algTreeObj[groupname]);
          })
          // algTreeObj["人员管理"] && this.algTree.push(algTreeObj["人员管理"]);
          // algTreeObj["车辆管理"] && this.algTree.push(algTreeObj["车辆管理"]);
          // algTreeObj["险情防控"] && this.algTree.push(algTreeObj["险情防控"]);
          // algTreeObj["行业定制"] && this.algTree.push(algTreeObj["行业定制"]);

          this.handleReset();
        }
      });
  },
  methods: {
    setStream() {
      let username = this.formData.username
        .replaceAll("@", "%40")
        .replaceAll("#", "%23")
        .replaceAll("?", "%3F");
      let password = this.formData.password
        .replaceAll("@", "%40")
        .replaceAll("#", "%23")
        .replaceAll("?", "%3F");
      let codeStream = this.formData.codeStream; //.replaceAll("?", "%3F");
      this.formData.stream = `rtsp://${username}:${password}@${this.formData.ipv4}${codeStream}`;
      this.streamStatus = null;
    },
    setOtherStream() {
      let stream = this.formData.stream.split("://");
      let portal = stream[0] + "://";
      if (stream[1]) {
        if (stream[1].indexOf(":") > 0 && stream[1].indexOf("@") > 0) {
          let infos = stream[1].split(":");
          let username = infos[0]
            .replaceAll("@", "%40")
            .replaceAll("#", "%23")
            .replaceAll("?", "%3F");
          let codeStream = infos[2].replaceAll("?", "%3F");
          let password = infos[1]
            .slice(0, infos[1].lastIndexOf("@"))
            .replaceAll("@", "%40")
            .replaceAll("#", "%23")
            .replaceAll("?", "%3F");
          let ip = infos[1].slice(
            -(infos[1].length - infos[1].lastIndexOf("@"))
          );
          this.formData.stream = `${portal}${username}:${password}${ip}:${codeStream}`;
        } else {
          this.formData.stream = this.formData.stream
            .replaceAll("@", "%40")
            .replaceAll("#", "%23")
            .replaceAll("?", "%3F");
        }
      }
    },
    validateId(rule, value, callback) {
      let reg = /[^\w_]/;
      if (reg.test(value)) {
        callback(new Error("只能输入字母、数字、下划线"));
      } else {
        callback();
      }
    },
    validateWidth(rule, value, callback) {
      if (this.formData.width) {
        callback();
      } else {
        let msg = "请检测视频流状态";
        if (this.formData.type == "camera") {
          msg = "请检测摄像头状态";
        }
        if (this.formData.optype == "add" && this.streamStatus == "离线") {
          msg = "离线状态无法添加，请添加有效的流地址";
          if (this.formData.type == "camera") {
            msg = "离线状态无法添加，请检查摄像头配置信息是否正确";
          }
        }
        callback(new Error(msg));
      }
    },
    validateIp(rule, value, callback) {
      if (this.formData.type == "camera" && !this.formData.ipv4) {
        callback(new Error("请输入IP地址"));
      }
      var pattern =
      /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
      if (this.formData.ipv4 && !pattern.test(this.formData.ipv4)) {
        callback(new Error("请输入正确格式的IP地址"));
      } else {
        callback();
      }
    },
    validatePort(rule, value, callback) {
      if (this.formData.port) {
        callback();
      } else {
        callback(new Error("请输入端口"));
      }
    },
    validateStream(rule, value, callback) {
      if (this.formData.stream) {
        // let type = this.formData.stream.slice(this.formData.stream.length - 4);
        // let head = this.formData.stream.slice(0,4)
        // if((head == 'rtmp') || head == 'rtsp'){
        callback();
        // }else{
        //   callback(new Error("格式错误！只支持rtsp、rtmp流地址！"));
        // }
      } else {
        callback(new Error("请输入视频流地址"));
      }
    },
    clearCameraSize() {
      this.formData.draw_size = [];
      this.formData.width = null;
      this.formData.height = null;
      this.streamStatus = "";
    },
    handleGetOnvifinfo() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.getOnvifinfoLoading = true;
          this.api
            .getOnvifinfo({
              device_id: this.$props.deviceInfo.device_id,
              username: this.formData.username,
              password: this.formData.password,
              ip: this.formData.ipv4,
              port: this.formData.port,
            })
            .then((res) => {
              console.log(res)
              this.getOnvifinfoLoading = false;
              this.onvifInfo = JSON.parse(JSON.stringify(res));
              this.onvifInfo.profiles.map((item, index) => {
                if (index == 0) {
                  item.label = "主码流";
                } else {
                  item.label = "辅码流";
                }
                return item;
              });
              this.formData.stream = this.onvifInfo.profiles[0].rtsp;
              this.getCameraSize();
            })
            .catch((err) => {
              this.getOnvifinfoLoading = false;
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
    handleGetCameraSize() {
      if (
        this.formData.type == "camera" &&
        this.formData.rtsptype == "input" &&
        this.formData.stream
      ) {
        this.getCameraSizeLoading = true;
        this.getCameraSize();
      } else if (
        this.formData.type == "camera" &&
        this.formData.rtsptype == "gen" &&
        this.formData.username &&
        this.formData.password &&
        this.formData.ipv4 &&
        this.formData.codeStream
      ) {
        // this.$refs.formData.validate('ipv4').then(res => {
        //   if(res == true){
            this.getCameraSizeLoading = true;
            this.getCameraSize();
        //   }
        // }).catch(err => {
        //   // this.$refs.formData.clearValidate('width')
        // })
       
      } else if (
        this.formData.type == "camera" &&
        this.formData.brand == "8" &&
        this.formData.stream
      ) {
        // this.setOtherStream();
        this.getCameraSizeLoading = true;
        this.getCameraSize();
      } else if (this.formData.type == "stream" && this.formData.stream) {
        // this.setOtherStream();
        this.getCameraSizeLoading = true;
        this.getCameraSize();
      } else {
        let msg = "摄像头信息不完整";
        if (this.formData.type == "stream") {
          msg = "请输入视频流地址";
        }
        this.getCameraSizeLoading = false;
        this.$message({
          type: "error",
          message: msg,
        });
      }
    },
    getCameraSize() {
      this.api
        .getAttr({
          device_id: this.$props.deviceInfo.device_id,
          stream: this.formData.stream,
          draw_size: 1280,
        })
        .then((res) => {
          this.getCameraSizeLoading = false;
          if (res.codec) {
            this.formData.draw_size = res.size;
            this.formData.width = this.formData.draw_size[0];
            this.formData.height = this.formData.draw_size[1];
            this.formData.encoding = res.codec;
            this.streamStatus = "在线";
          } else {
            this.streamStatus = "离线";
            console.log(res);
            this.$message({
              dangerouslyUseHTMLString: true,
              duration: 0,
              showClose: true,
              type: "error",
              message: "设备离线，请检查配置！",
            });
          }
        })
        .catch((err) => {
          this.getCameraSizeLoading = false;
          this.streamStatus = "离线";
          if(err.error_code == -1){
            this.$message({
              dangerouslyUseHTMLString: true,
              duration: 0,
              showClose: true,
              type: "error",
              message: err.data.replaceAll('\n','<br/>'),
            });
          }
          
        });
    },
    handleAlgfold(data, node, other) {
      if (!data.name) {
        data.expanded = node.expanded;
        if (node.expanded == true) {
          if (this.defaultExpended.indexOf(data.id) < 0) {
            this.defaultExpended.push(data.id);
          }
        } else {
          if (this.defaultExpended.indexOf(data.id) >= 0) {
            this.defaultExpended.splice(
              this.defaultExpended.indexOf(data.id),
              1
            );
          }
        }
      }
    },
    handleAlgsChg(selecteNodes) {
      let selectedAlg = selecteNodes.filter((item) => item.name);
      if (selectedAlg.length > 0) {
        for (let i = 0; i < selectedAlg.length; i++) {
          if (!this.formData.algscfg[selectedAlg[i].name]) {
            this.$set(this.formData.algscfg, selectedAlg[i].name, null);
          }
          let algs = selectedAlg.map((item) => item.name);
          for (let key in this.formData.algscfg) {
            if (algs.indexOf(key) < 0) {
              this.$delete(this.formData.algscfg, key);
            }
          }
        }
      } else {
        for (let key in this.formData.algscfg) {
          this.$delete(this.formData.algscfg, key);
        }
      }
      this.$forceUpdate();
    },
    handleAlgChg(data, checked, childCheck) {
      let selectedAlg = this.$refs.algtree.getCheckedNodes(true, false);
      if (selectedAlg.length > 0) {
        for (let i = 0; i < selectedAlg.length; i++) {
          if (!this.formData.algscfg[selectedAlg[i].name]) {
            this.$set(this.formData.algscfg, selectedAlg[i].name, null);
          }
          let algs = selectedAlg.map((item) => item.name);
          for (let key in this.formData.algscfg) {
            if (algs.indexOf(key) < 0) {
              this.$delete(this.formData.algscfg, key);
            }
          }
        }
      } else {
        for (let key in this.formData.algscfg) {
          this.$delete(this.formData.algscfg, key);
        }
      }
      this.$forceUpdate();
    },
    handleDelAlg(key) {
      this.$delete(this.formData.algscfg, key);
      this.chgTreeNodeSel();
      let algTree = JSON.parse(JSON.stringify(this.algTree));
      for (let i = 0; i < algTree.length; i++) {
        let children = algTree[i].children;
        let j = 0;
        for (j = 0; j < children.length; j++) {
          if (children[j].name == key) {
            children[j].checked = false;
            break;
          }
        }
      }
      this.algTree = algTree;
      // console.log(this.formData.algscfg)
      this.$forceUpdate();
    },
    handleSelAlg() {
      if (this.formData.width) {
        this.showAlgtree = !this.showAlgtree;
      } else {
        this.$message({
          type: "error",
          message: "请先检测是否在线",
        });
      }
    },
    chgTreeNodeSel() {
      let checkAlgs = [];
      for (let alg in this.formData.algscfg) {
        for (let i = 0; i < this.algList.length; i++) {
          if (alg == this.algList[i].alg) {
            checkAlgs.push(this.algList[i].id);
            break;
          }
        }
      }
      this.defaultCheckAlgs = checkAlgs;
      this.$forceUpdate()
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          let refsKeys = Object.keys(this.$refs);
          let validateAlgs = [];
          for (let i = 0; i < refsKeys.length; i++) {
            if (
              refsKeys[i].indexOf("alg_") >= 0 &&
              this.$refs[refsKeys[i]].length > 0
            ) {
              validateAlgs.push(this.$refs[refsKeys[i]][0].validateAll());
            }
          }
          Promise.all(validateAlgs)
            .then((res) => {
              this.formData.loading = true;
              let alg = {};
              for (let i = 0; i < res.length; i++) {
                let algname = res[i].type;
                alg[algname] = JSON.parse(JSON.stringify(res[i].cfg));
              }
              let params = {
                device_id: this.$props.deviceInfo.device_id,
                encoding: this.formData.encoding,
                desc: this.formData.desc,
                stream: this.formData.stream,
                ipv4: this.formData.ipv4,
                // infer_size: null,
                draw_size: this.formData.draw_size,
                // video_code: this.formData.video_code,
                alg: alg,
                type: this.formData.type,
                video_record: this.formData.video_record ? 1 : 0,
              };
              if (this.formData.type == "camera") {
                if (this.formData.protocol == "onvif") {
                  params.info = {
                    protocol: "onvif",
                    port: this.formData.port,
                    username: this.formData.username,
                    password: this.formData.password,
                    rtsp_transport: this.formData.rtsp_transport ? 'tcp' : 'udp'
                  };
                } else {
                  params.info = {
                    protocol: "rtsp",
                    rtsptype: this.formData.rtsptype,
                    brand: this.formData.brand,
                    stream: this.formData.codeStream,
                    username: this.formData.username,
                    password: this.formData.password,
                    rtsp_transport: this.formData.rtsp_transport ? 'tcp' : 'udp'
                  };
                }
              } else if (this.formData.type == "stream") {
                params.info = {
                  rtsp_transport: this.formData.rtsp_transport ? 'tcp' : 'udp'
                };
              }
              if (this.formData.optype == "add") {
                if (this.formData.id) {
                  params.id = this.formData.id;
                }
                this.api
                  .add(params)
                  .then((res) => {
                    this.$message({
                      type: "success",
                      message: res.message[this.lang],
                    });
                    this.$emit("close-edit-camera");
                  })
                  .catch((err) => {
                    this.formData.loading = false;
                    if (err && err.message && err.message[this.lang]) {
                      this.$message({
                        type: "error",
                        message: err.message[this.lang],
                      });
                    }
                  });
              } else {
                params.id = this.formData.id;
                this.api
                  .unsubscribeLive({
                    device_id: this.$props.deviceInfo.device_id,
                    source_ids: JSON.stringify([params.id]),
                  })
                  .then((res) => {
                    this.api
                      .edit(params)
                      .then((res) => {
                        this.formData.loading = false;
                        this.$message({
                          type: "success",
                          message: res.message[this.lang],
                        });
                        this.$emit("close-edit-camera");
                      })
                      .catch((err) => {
                        this.formData.loading = false;
                        if (err && err.message && err.message[this.lang]) {
                          this.$message({
                            type: "error",
                            message: err.message[this.lang],
                          });
                        }
                      });
                  })
                  .catch((err) => {
                    this.formData.loading = false;
                    this.$message({
                      type: "error",
                      message: "修改配置失败",
                    });
                  });
              }
            })
            .catch((err) => {});
        } else {
          this.$message({
            type: "error",
            message: "信息不完整，请检查配置",
          });
        }
      });
    },
    handleReset() {
      if (this.$props.formDataInit.optype == "add") {
        // this.formData.algscfg = {};
        this.formData = Object.assign(
          {},
          {
            optype: "add", // or edit
            protocol: "rtsp",
            port: 80,
            rtsptype: "gen",
            id: "",
            encoding: "",
            type: "camera",
            alg: {},
            brand: "", // 摄像头id
            id: null,
            ipv4: "", // ip
            desc: "",
            password: "",
            stream: "",
            codeStream: "", // 码流
            status: true,
            statusDesc: "",
            username: "",
            loading: false,
            video_code: "",
            draw_size: null,
            width: null,
            height: null,
            algscfg: {},
            video_record: false,
            rtsp_transport: true
          }
        );
      } else {
        let algscfg = JSON.parse(JSON.stringify(this.$props.formDataInit.alg));

        for (let key in algscfg) {
          let algInfo = this.algList.find((item) => item.alg == key);
          if (algInfo) {
            algscfg[key].reserved_args.ch_name = algInfo.ch_name;
          }
          if (!algscfg[key].plan) {
            algscfg[key].plan = {
              1: [[0, 86399]],
              2: [[0, 86399]],
              3: [[0, 86399]],
              4: [[0, 86399]],
              5: [[0, 86399]],
              6: [[0, 86399]],
              7: [[0, 86399]],
            };
          }
          if (algscfg[key].reserved_args.sound_text === undefined) {
            algscfg[key].reserved_args.sound_text =
              algscfg[key].reserved_args.ch_name + "告警";
          }
        }
        // console.log( this.$props.formDataInit)
        this.formData = Object.assign(
          {},
          {
            optype: "edit",
            protocol: this.$props.formDataInit.protocol,
            port: this.$props.formDataInit.port,
            rtsptype: this.$props.formDataInit.rtsptype,
            type: this.$props.formDataInit.type,
            id: this.$props.formDataInit.source_id,
            encoding: this.$props.formDataInit.encoding,
            alg: JSON.parse(JSON.stringify(this.$props.formDataInit.alg)), //算法选择内容
            brand: this.$props.formDataInit.brand || "", // 摄像头品牌
            ipv4: this.$props.formDataInit.ipv4 || "", // ip
            username: this.$props.formDataInit.username || "",
            password: this.$props.formDataInit.password || "",
            codeStream:
              this.$props.formDataInit.ipv4 &&
              this.$props.formDataInit.stream.split(
                this.$props.formDataInit.ipv4
              )[1],
            desc: this.$props.formDataInit.desc,
            stream: this.$props.formDataInit.stream,
            status: this.$props.formDataInit.status,
            statusDesc: this.$props.formDataInit.statusDesc,

            loading: false,
            algscfg: algscfg,
            video_record: this.$props.formDataInit.video_record ? true : false,
            rtsp_transport: this.$props.formDataInit.rtsp_transport == 'tcp' ? true : false
          }
        );
        this.$nextTick(() => {
          this.formData.stream = this.$props.formDataInit.stream;
          if (this.formData.brand && this.formData.brand != 8) {
            this.formData.codeStream = this.$props.formDataInit.stream.split(
              this.$props.formDataInit.ipv4
            )[1];
          }
        });
        setTimeout(() => {
          this.formData.draw_size = JSON.parse(
            JSON.stringify(this.$props.formDataInit.draw_size)
          );
          this.formData.width = this.$props.formDataInit.draw_size[0];
          this.formData.height = this.$props.formDataInit.draw_size[1];
          // switch (this.$props.formDataInit.status) {
          //   case 0:
          //     this.streamStatus = "离线";
          //     break;
          //   case 1:
          //     this.streamStatus = "在线";
          //     break;
          // }
          this.$forceUpdate();
        }, 1);
      }
    },
    handleProrocolChg() {
      if (this.formData.optype == "edit") {
        this.formData.rtsptype = "gen";
        if (this.formData.protocol == this.formDataInit.protocol) {
          this.formData.username = this.formDataInit.username;
          this.formData.password = this.formDataInit.password;
          this.formData.port = 80;
          this.formData.stream = this.formDataInit.stream;
          this.formData.ipv4 = this.formDataInit.ipv4;
        } else {
          this.formData.username = "";
          this.formData.password = "";
          this.formData.port = 80;
          this.formData.ipv4 = "";
          this.formData.codeStream = "";
          this.$nextTick(() => {
            this.formData.stream = "";
          });
        }
      } else {
        this.formData.stream = "";
        this.formData.username = "";
        this.formData.password = "";
        this.formData.port = 80;
        this.formData.ipv4 = "";
      }
      this.$refs['formData'].clearValidate()
    },
    handleRtsptypeChg(){
      this.$refs['formData'].clearValidate()
    },
    handleGetalgcfg(algInfo) {
      this.formData.algscfg[algInfo.type] = JSON.parse(
        JSON.stringify(algInfo.cfg)
      );
      this.$forceUpdate();
    },
  },
};
</script>
  
  <style scoped lang="less">
/deep/.ivu-form-item-error-tip {
  padding-top: 0;
}

.selalgs-container {
  .algsnames {
    width: 100%;
    min-height: 32rem;
    border: 1rem solid #2a2b33;
    border-radius: 4px;
    position: relative;
    padding-right: 20rem;
    box-sizing: border-box;
    cursor: pointer;
    .z-icon-zhedie1,
    .z-icon-zhankai1,
    .z-icon-xuanze {
      cursor: pointer;
      position: absolute;
      right: 10rem;
      top: 0rem;
      height: 32rem;
      line-height: 32rem;
    }
    ul {
      display: flex;
      flex-wrap: wrap;
      list-style: none;
    }
    li {
      margin: 2rem 5rem 2rem 5rem;
      background-color: #f1f1f1;
      border: 1rem solid #404148;
      height: 28rem;
      border-radius: 4px;
      padding: 0 5rem;
      display: flex;
      align-items: center;
      font-size: 14rem;
    }
  }
  .algtree-container {
    height: 200rem;
    overflow: auto;
    background-color: #fff;
    border: 1rem solid #2a2b33;
    box-shadow: 0 0 12rem 6rem rgba(0, 0, 0, 0.2);
    border-radius: 4rem;
    position: absolute;
    width: 100%;
    z-index: 2;
    top: -201rem;
    padding: 10rem 10rem;
    box-sizing: border-box;
    /deep/.el-tree-node__label {
      font-size: 14rem;
    }
    /deep/.el-tree-node__content:hover,
    /deep/.el-tree-node:focus > .el-tree-node__content {
      background-color: #fff;
    }
  }
}

.streamStatus {
  margin-left: 10rem;
}
.streamStatus.online {
  color: rgb(32, 218, 32);
}
.streamStatus.offline {
  color: rgb(255, 24, 24);
}

.cameraInfo {
  h3 {
    font-size: 14rem;
    font-weight: normal;
    padding: 10rem 19rem 10rem 0;
    color: #333;
  }
  ul {
    display: flex;
    flex-wrap: wrap;
    li {
      display: flex;
      line-height: 2;
      label {
        width: 50rem;
        text-align: right;
        font-size: 14rem;
      }
      span {
        width: calc(100% - 60rem);
        font-size: 12rem;
        margin-left: 10rem;
      }
    }
  }
}
.onvifInfo {
  display: flex;
  margin: 5rem 0 20rem;
  h3 {
    font-size: 14rem;
    font-weight: normal;
  }
  > div {
    width: calc((100% - 20rem) / 3);
    margin-left: 10rem;
    border: 1px solid gray;
    padding: 5rem;
    border-radius: 4px;
    cursor: pointer;
    ul {
      list-style: none;
      li {
        display: flex;
        justify-content: space-between;
        label {
          font-size: 12rem;
          cursor: pointer;
        }
        span {
          font-size: 12rem;
          // margin-left: 30px;
          text-align: right;
        }
      }
    }
  }
  > div.active {
    border: 1px solid orange;
  }
  > div:first-child {
    margin-left: 0;
  }
}
</style>

<style lang="less">
.draw-dialog {
  .el-dialog__header {
    display: none;
  }
  .z-icon-close {
    position: absolute;
    top: 20px;
    right: 20px;
    cursor: pointer;
    font-size: 20px;
    z-index: 1;
  }
}
</style>