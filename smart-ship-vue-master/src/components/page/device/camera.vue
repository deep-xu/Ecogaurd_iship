<template>
  <div class="camera-container">
    <template v-if="type == 'camera'">
      <div class="device-title">
        <h3>Source Management</h3>
        <div class="count">
          <p>Total：{{ tableData.length }}</p>
          <p class="online">
            Online：{{
              deviceInfo && deviceInfo.status == 1
                ? tableData.filter((item) => item.status == 1).length
                : 0
            }}
          </p>
        </div>
      </div>
      <div class="search-container">
        <div class="search-item">
          <el-input
            v-model="search.desc"
            clearable
            placeholder="Source description"
            @input="getTableData"
          ></el-input>
        </div>
        <div class="search-item">
          <el-select
            v-model="search.type"
            @change="getTableData"
            placeholder="类型"
            clearable
          >
            <el-option label="摄像头" value="camera"></el-option>
            <!-- <el-option label="视频流" value="stream"></el-option>             -->
          </el-select>
        </div>
        <div class="search-item" v-if="!deviceTypeIsForward">
          <el-select
            v-model="search.source_alg"
            @change="getTableData"
            placeholder="启用算法"
            clearable
          >
            <el-option
              v-for="item in algList"
              :key="item.name"
              :label="item.ch_name"
              :value="item.alg"
            >
            </el-option>
          </el-select>
        </div>
        <div class="search-item">
          <el-select
            v-model="search.status"
            @change="getTableData"
            placeholder="设备状态"
            clearable
          >
            <el-option label="启用" :value="1"></el-option>
            <el-option label="停用" :value="-1"></el-option>
          </el-select>
        </div>
        <!-- <i
          class="z-icon-pinlv"
          @click="showfreq = !showfreq"
          style="position: absolute; left: 550rem; color: #fff"
        ></i>
        <div class="search-btns" v-if="deviceInfo && deviceInfo.status == 1">
          <el-button type="primary" class="searchbtn" @click="formInit()"
            >添加数据源</el-button
          >
        </div> -->
      </div>
      <div class="table-container">
        <el-table
          v-loading="listLoading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          :data="tableData"
          :border="false"
          stripe
          :row-class-name="activeRow"
        >
          <!-- <el-table-column prop="source_id" label="数据源ID"></el-table-column> -->
          <el-table-column prop="desc" label="描述"></el-table-column>
          <el-table-column prop="ipv4" label="IP"></el-table-column>
          <el-table-column prop="stream" label="流地址"></el-table-column>
          <el-table-column prop="type" label="类型">
            <template slot-scope="scope">
              <span>{{
                scope.row.type == "camera" ? "摄像头" : "视频流"
              }}</span>
            </template>
          </el-table-column>
          <el-table-column label="启用算法" v-if="!deviceTypeIsForward">
            <template slot-scope="scope">
              <span
                style="margin-right: 5rem"
                v-for="(item, algname) in scope.row.alg"
                :key="scope.row.source_id + algname"
              >
                {{ item.reserved_args.ch_name }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="设备状态">
            <template slot-scope="scope">
              <div v-if="deviceInfo && deviceInfo.status == 1">
                <!-- <span v-if="scope.row.status == 0">离线</span>
                <span v-if="scope.row.status == 1">在线</span> -->
                <span v-if="scope.row.status == -1">停用</span>
                <span v-else>启用</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="encoding" label="编码格式"></el-table-column>
          <el-table-column
            prop="freq_time"
            label="检测频率"
            v-if="showfreq"
          ></el-table-column>
          <!-- <el-table-column
            label="排序"
            class-name="operate-column"
            width="70px"
            v-if="permission == 20 || permission == 10"
          >
            <template slot-scope="scope" v-if="deviceInfo.status == 1">
              <el-tooltip content="上移" placement="top"
                ><span @click="handleEditIndex(scope.row, -1)"
                  ><i class="z-icon-xiangshang"></i></span
              ></el-tooltip>
              <el-tooltip content="下移" placement="top"
                ><span @click="handleEditIndex(scope.row, 1)"
                  ><i class="z-icon-xiangxia"></i></span
              ></el-tooltip>
            </template>
          </el-table-column> -->
          <el-table-column
            label="操作"
            class-name="operate-column"
            width="80px"
            v-if="permission == 20 || permission == 10"
          >
            <template slot-scope="scope" v-if="deviceInfo.status == 1">
              <!-- <el-tooltip content="编辑" placement="top"
                ><span
                  @click="formInit(scope.row)"
                  v-if="scope.row.type != 'hik_platform'"
                  ><i class="z-icon-edit"></i></span
              ></el-tooltip>
              <el-tooltip content="删除" placement="top"
                ><span @click="handleDel(scope.row)"
                  ><i class="z-icon-del"></i></span
              ></el-tooltip> -->

              <el-tooltip content="检测是否在线" placement="top">
                <span
                  @click="handleDetect(scope.row)"
                  v-if="scope.row.type != 'hik_platform'"
                  ><i class="z-icon-jiance1"></i
                ></span>
              </el-tooltip>

              <!-- <el-tooltip content="抓拍" placement="top">
                <span
                  @click="handleCapture(scope.row)"
                  v-if="!deviceTypeIsForward"
                  ><i
                    class="z-icon-zhuapai"
                    v-loading="scope.row.captureLoading"
                    element-loading-spinner="el-icon-loading"
                  ></i
                ></span>
              </el-tooltip>

              <el-tooltip
                :content="scope.row.status_switch == true ? '启用' : '停用'"
                placement="top"
              >
                <el-switch
                  @change="handleSwitchStatus(scope.row)"
                  v-model="scope.row.status_switch"
                  size="small"
                  :disabled="switchStatusLoading"
                ></el-switch>
              </el-tooltip> -->
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
    <template v-else>
      <div class="device-title">
        <h3>Source Management</h3>
        <div class="count">
          <p>Total：{{ tableData.length }}</p>
          <p class="online">
            Online：{{
              deviceInfo && deviceInfo.status == 1
                ? tableData.filter((item) => item.status == 1).length
                : 0
            }}
          </p>
        </div>
      </div>
      <div class="search-container"></div>
      <div class="table-container">
        <el-table
          v-loading="listLoading"
          element-loading-text="Loading"
          element-loading-spinner="el-icon-loading"
          :data="tableData"
          :border="false"
          stripe
        >
          <el-table-column prop="desc" label="Description"></el-table-column>
        </el-table>
      </div>
    </template>

    <!-- 添加、修改摄像头 -->
    <el-drawer
      :title="formDataInit.act == 'add' ? '新增摄像头' : '编辑摄像头'"
      :before-close="beforeCloseForm"
      :visible.sync="formVisible"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
      :modal="false"
    >
      <cameraEditVue
        :formDataInit="formDataInit"
        :locations="locations"
        :algList="algList"
        :deviceInfo="deviceInfo"
        v-if="formVisible && !deviceTypeIsForward"
        @close-edit-camera="
          getTableData();
          formVisible = false;
        "
      ></cameraEditVue>
      <forwardCameraEditVue
        :formDataInit="formDataInit"
        :locations="locations"
        :deviceInfo="deviceInfo"
        v-if="formVisible && deviceTypeIsForward"
        @close-edit-camera="
          getForwardSourceData();
          formVisible = false;
        "
      ></forwardCameraEditVue>
    </el-drawer>

    <el-dialog
      title="检测是否在线"
      :append-to-body="true"
      :visible.sync="detecting.show"
      :close-on-click-modal="false"
      class="detecting-dialog"
      v-dialogDrag
      width="400px"
    >
      <div
        v-if="!detecting.result"
        v-loading="!detecting.result"
        style="width: 100%; height: 200rem"
        element-loading-text="检测中..."
      ></div>
      <div
        v-else
        style="
          width: 100%;
          height: 200rem;
          line-height: 200rem;
          text-align: center;
          font-size: 16rem;
          letter-spacing: 1rem;
        "
      >
        视频流<b
          v-if="detecting.result.indexOf('在线') > 0"
          style="color: #20da20"
          >{{ detecting.result.replace("视频流", "") }}</b
        >
        <b v-else style="color: red">{{
          detecting.result.replace("视频流", "")
        }}</b>
      </div>
      <div v-if="detecting.offlineErrorinfo">
        <p v-html="detecting.offlineErrorinfo" style="line-height: 1.5"></p>
      </div>
    </el-dialog>
  </div>
</template>
  
<script>
import { sourceApis, forwardSourceApis, deviceV2Apis } from "@/api/device.js";
import { algApis } from "@/api/alg.js";
import cameraEditVue from "./cameraEdit.vue";
import { Message } from "element-ui";
import { downloadBase64 } from "@/utils/utils.js";
import langmixin from "@/utils/langmixin.js";
import forwardCameraEditVue from "./forwardCameraEdit.vue";
export default {
  mixins: [langmixin],
  components: { cameraEditVue, forwardCameraEditVue },
  props: ["deviceInfo", "type"],
  data() {
    return {
      permission: 10,
      search: {
        desc: "",
        type: 'camera',
        status: "",
        source_alg: "",
      },
      listLoading: false,
      tableData: [],
      algList: [],
      algObj: {},
      locations: [],

      formVisible: false,
      formDataInit: {
        type: "add",
      },
      streamNum: 0,
      switchStatusLoading: false,
      detecting: {
        show: false,
        result: "",
        offlineErrorinfo: "",
      },
      showfreq: false,
      deviceTypeIsForward: false,
    };
  },
  beforeMount() {
    this.permission = localStorage.getItem("permission");
  },
  watch: {
    deviceInfo: {
      deep: true,
      handler() {
        console.log('===========', this.$props.deviceInfo)
        this.tableData = [];
        this.algList = [];
        this.algObj = {};
        this.locations = [];
        if (this.$props.deviceInfo) {
          this.search = Object.assign(this.search, {
            desc: "",
            status: "",
            source_alg: "",
          });
          this.getTableData();
          // if (this.$props.deviceInfo.hardware_version.indexOf("forward") < 0) {
          //   this.deviceTypeIsForward = false;
          //   this.getAlgList();
          // } else {
          //   this.deviceTypeIsForward = true;
          //   this.getForwardSourceData();
          // }
        }
      },
    },
  },
  mounted() {
    this.getTableData();
    // if (
    //   this.$props.deviceInfo &&
    //   this.$props.deviceInfo.hardware_version.indexOf("forward") < 0
    // ) {
    //   this.deviceTypeIsForward = false;
    //   this.getAlgList();
    // } else if (
    //   this.$props.deviceInfo &&
    //   this.$props.deviceInfo.hardware_version.indexOf("forward") >= 0
    // ) {
    //   this.deviceTypeIsForward = true;
    //   this.getForwardSourceData();
    // }
  },
  methods: {
    activeRow({ row }) {
      if (row.select) {
        return "active";
      } else {
        return "";
      }
    },
    getAlgList() {
      let device_id = this.$props.deviceInfo.device_id;
      algApis.getTableData({ device_id }).then((res) => {
        let algs = res[device_id];
        if (algs && algs.length > 0) {
          this.algList = algs.map((item) => {
            this.$set(this.algObj, item.name, item);

            return {
              alg: item.name,
              ch_name: item.ch_name,
            };
          });
        } else {
          this.algList = [];
        }

        this.$forceUpdate();
        this.getTableData();
      });
    },
    getTableData() {
      let params = {
        deviceId: this.$props.deviceInfo.id,
      };
      if (this.search.desc) params.desc = this.search.desc;
      if (this.search.source_alg) params.alg = this.search.source_alg;
      if (this.search.status) params.status = this.search.status + "";
      deviceV2Apis.getSourceTableList(params)
        .then((res = {}) => {
          let tableData = [];
          for (let source_id in res) {
            let source = res[source_id];
            source.source_id = source_id;
            source.status_switch = source.status == 0 || source.status == 1;
            source["alg_ch_name"] = Object.values(source.alg).map(
              (alg) => alg.reserved_args.ch_name
            );
            tableData.push(source);
          }
          let oldTableData = JSON.parse(JSON.stringify(this.tableData));

          for (let i = 0; i < tableData.length; i++) {
            let select = oldTableData.find((item) => item.select == true);
            if (select && tableData[i].source_id == select.source_id) {
              tableData[i].select = true;
            } else {
              tableData[i].select = false;
            }
          }
          this.tableData = tableData.filter(item => item.type == this.search.type);

          if (this.tableData.length > 1) {
            this.tableData = this.tableData.sort((a, b) => a.index - b.index);
          }
          this.tableData = this.tableData.map((item) => {
            let infer_freq = this.$props.deviceInfo.infer_freq;
            if (item.status != -1) {
              if (infer_freq > 1) {
                item.freq_time = `${Math.round(infer_freq)}次/秒`;
              } else if (infer_freq > 0 && infer_freq <= 1) {
                item.freq_time = `1次/${Math.round(1 / infer_freq)}秒`;
              } else {
                item.freq_time = "";
              }
            }
            let info = item.info || {};
            return Object.assign(item, {
              brand: info.brand,
              username: info.username,
              password: info.password,
              protocol: info.protocol || "rtsp",
              port: info.port || 80,
              rtsptype: info.rtsptype || "gen",
              captureLoading: false,
              rtsp_transport: info.rtsp_transport,
            });
          });
          this.locations = this.tableData.map((item) => item.desc);
        })
        .catch((err) => {
          this.tableData = [];
        });
    },
    getForwardSourceData() {
      this.tableData = [];
      let params = {
        device_id: this.$props.deviceInfo.device_id,
      };
      if (this.search.desc) params.desc = this.search.desc;
      if (this.search.status) params.status = this.search.status + "";
      forwardSourceApis.getTableData(params).then((res) => {
        for (let source_id in res[params.device_id]) {
          let source = res[params.device_id][source_id];
          source.source_id = source_id;
          source.status_switch =
            source.status == 0 || source.status == 1 ? true : false;
          this.tableData.push(source);
        }
        this.tableData = this.tableData.map((item) => {
          let info = item.info || {};
          return Object.assign(item, {
            brand: info.brand,
            username: info.username,
            password: info.password,
            protocol: info.protocol || "rtsp",
            port: info.port || 80,
            rtsptype: info.rtsptype || "gen",
            captureLoading: false,
            rtsp_transport: info.rtsp_transport,
          });
        });
        this.locations = this.tableData.map((item) => item.desc);
      });
    },
    formInit(row) {
      if (row) {
        this.formVisible = true;
        this.formDataInit = Object.assign(
          {
            optype: "edit",
          },
          row
        );
      } else {
        this.formVisible = true;
        this.formDataInit = {
          optype: "add",
        };
      }
    },
    handleEditIndex(row, offset) {
      this.tableData.forEach(item => item.select = false);
      let cur = this.tableData.find(item => item.source_id == row.source_id);
      cur.select = true;
      if (
        this.$props.deviceInfo &&
        this.$props.deviceInfo.hardware_version.indexOf("forward") < 0
      ) {
        sourceApis
          .editIndex({
            device_id: this.$props.deviceInfo.device_id,
            id: row.source_id,
            offset: offset,
          })
          .then((res) => {
            console.log(res);
            this.getTableData();
          });
      } else if (
        this.$props.deviceInfo &&
        this.$props.deviceInfo.hardware_version.indexOf("forward") >= 0
      ) {
        forwardSourceApis
          .editIndex({
            device_id: this.$props.deviceInfo.device_id,
            id: row.source_id,
            offset: offset,
          })
          .then((res) => {
            console.log(res);
            this.getForwardSourceData();
          });
      }
    },
    handleDel(row) {
      this.$confirm("是否确认删除数据源?", "删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      })
        .then(() => {
          let api = sourceApis;
          if (this.$props.deviceInfo.hardware_version.indexOf("forward") >= 0) {
            api = forwardSourceApis;
          }
          api
            .del({
              id: row.source_id,
              device_id: this.$props.deviceInfo.device_id,
            })
            .then((res) => {
              if (!this.deviceTypeIsForward) {
                this.getTableData();
              } else {
                this.getForwardSourceData();
              }

              this.$message({
                type: "success",
                message: res.message[this.lang],
              });
            })
            .catch((err) => {
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              }
            });
        })
        .catch((err) => {});
    },
    handleSwitchStatus(row, index) {
      let msg = "",
        type = "",
        status = null;
      this.switchStatusLoading = true;
      if (row.status == 0 || row.status == 1) {
        msg = "是否确认停用此摄像头";
        type = "停用";
        status = -1;
      } else {
        msg = "是否确认启用此摄像头";
        type = "启用";
        status = 1;
      }
      this.$confirm(msg, type, {
        confirmButtonText: type,
        cancelButtonText: "取消",
      })
        .then(() => {
          this.setStatus(row, status, type);
        })
        .catch(() => {
          row.status_switch = !row.status_switch;
          this.switchStatusLoading = false;
        });
    },
    setStatus(row, status, type) {
      let device_id = this.$props.deviceInfo.device_id;
      sourceApis
        .edit({
          device_id: device_id,
          id: row.source_id,
          status: status,
        })
        .then((res) => {
          this.switchStatusLoading = false;
          if (!this.deviceTypeIsForward) {
            if (status == -1) {
              //停用，取消订阅
              sourceApis
                .unsubscribeLive({
                  device_id: device_id,
                  source_ids: JSON.stringify([row.source_id]),
                })
                .then((res) => {});
            }
            this.getTableData();
          } else {
            this.getForwardSourceData();
          }

          this.$message({
            type: "success",
            message: `${type}摄像头成功！`,
          });
        })
        .catch((err) => {
          row.status_switch = !row.status_switch;
          this.switchStatusLoading = false;
          this.$message({
            type: "error",
            message: `${type}摄像头失败！`,
          });
        });
    },
    beforeCloseForm() {
      this.$confirm("离开后您已填写的内容不会保存。", "是否确定离开？", {
        confirmButtonText: "离开",
        cancelButtonText: "取消",
      })
        .then(() => {
          this.formVisible = false;
          done();
        })
        .catch(() => {});
    },
    handleDetect(row) {
      this.detecting.offlineErrorinfo = "";
      this.detecting.show = true;
      this.detecting.result = "";
      // let params = {
      //   deviceId: this.$props.deviceInfo.deviceId,
      //   stream: row.stream,
      // };
      // if (this.$props.deviceInfo.hardware_version.indexOf("forward") < 0) {
      //   params.drawSize = 1280;
      // }
      deviceV2Apis.streamStatus({ drawSize: 1280, deviceId: this.$props.deviceInfo.id, stream: row.stream })
        .then((res) => {
          if (res.codec) {
            this.detecting.result = "视频流在线";
            sourceApis
              .edit({
                device_id: this.$props.deviceInfo.device_id,
                id: row.source_id,
                draw_size: res.size,
                encoding: res.codec,
              })
              .then((res) => {
                Message.closeAll();
                this.getTableData();
              });
          } else {
            this.detecting.result = "视频流离线";
            if (res.data) {
              this.detecting.offlineErrorinfo = res.data.data.replace(
                "\n",
                "<br/>"
              );
            }
          }
        })
        .catch((err) => {
          this.detecting.result = "视频流离线";
          if (err.error_code == -1 && err.data) {
            this.detecting.offlineErrorinfo = err.data.data.replace(
              "\n",
              "<br/>"
            );
          }
        });
    },
    handleCapture(row) {
      if (row.captureLoading) {
        return;
      }
      row.captureLoading = true;
      sourceApis
        .getImage({
          device_id: this.$props.deviceInfo.device_id,
          stream: row.stream,
          draw_size: JSON.stringify(row.draw_size),
        })
        .then((res) => {
          console.log(res);
          row.captureLoading = false;
          downloadBase64(
            "data:image/jpg;base64," + res.data,
            `capture_${row.desc}`
          );
        })
        .catch((error) => {
          row.captureLoading = false;
        });
    },
  },
};
</script>
  
<style lang="less" scoped>
.camera-container {
  width: 100%;
  height: 100%;
  padding-left: 20rem;
  box-sizing: border-box;
  border-radius: 4px;
  > div {
    background-color: transparent;
  }
}
/deep/.operate-column span {
  cursor: pointer;
}
.device-title {
  width: 100%;
  padding: 8rem 0 0 8rem;
  box-sizing: border-box;
  h3 {
    font-size: 16rem;
    margin-bottom: 5rem;
    border-left: 3rem solid #ff9900;
    padding-left: 10rem;
  }
  .count {
    font-size: 14rem;
    display: flex;
    p {
      margin-right: 10rem;
    }
    .online {
      color: #20da20;
      font-weight: bold;
    }
  }
}
.search-container {
  margin-top: 0;
}
.search-btns {
  position: absolute;
  right: 0;
}
.table-container {
  height: calc(100% - 124rem);
  .el-table {
    height: 100%;
  }
  /deep/.el-table__row.active {
    // background-color: rgb(187 239 255 / 48%);
    background-color: #ecf5ff;
    // font-weight: bold;
    color: #000;
  }
  /deep/.el-table--striped .el-table__body .el-table__row--striped.active td.el-table__cell{
    background-color: #ecf5ff;
  }
  /deep/.el-table--enable-row-hover
    .el-table__body
    tr:hover
    > td.el-table__cell {
    background-color: transparent;
  }
}
.z-icon-zhuapai {
  /deep/.el-loading-mask {
    top: 10px;
    background-color: transparent;
  }
}
</style>