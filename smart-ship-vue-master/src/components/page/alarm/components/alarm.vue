
<template>
  <div class="alarm">
    <div class="search-container">
      <div class="search-item">
        <el-select
          v-model="search.deviceId"
          clearable
          placeholder="Device"
          @change="getTableData"
          collapse-tags
          filterable
        >
          <el-option
            v-for="item in devices"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
          </el-option>
        </el-select>
      </div>
      <div class="search-item">
        <!-- <el-select
          v-model="search.source_desc"
          clearable
          placeholder="数据源"
          @change="getTableData"
        >
          <template
            v-if="
              search.device_id &&
              devices[search.device_id] &&
              devices[search.device_id].source_desc_list
            "
          >
            <div style="padding: 0 20px; box-sizing: border-box">
              <el-input
                v-model="search.source_desc"
                @change="getTableData"
                style="width: 100%"
              ></el-input>
            </div>
            <el-option
              v-for="(item, index) in devices[search.device_id]
                .source_desc_list"
              :key="index + Math.random()"
              :label="item"
              :value="item"
            >
            </el-option>
          </template>
          <template v-else>
            <div style="padding: 0 20px; box-sizing: border-box">
              <el-input
                v-model="search.source_desc"
                @change="getTableData"
                style="width: 100%"
              ></el-input>
            </div>
          </template>
        </el-select> -->
        <el-input
          v-model="search.sourceDesc"
          clearable
          placeholder="Source"
          @input="
            search.pageNum = 1;
            getTableData();
          "
        ></el-input>
      </div>
      <div class="search-item">
        <el-input
          v-model="search.algChName"
          clearable
          placeholder="Alarm Type"
          @input="
            search.pageNum = 1;
            getTableData();
          "
        ></el-input>
        <!-- <el-select
          v-model="search.alg_ch_name"
          clearable
          placeholder="告警类型"
          @change="getTableData"
        >
          <div style="padding: 0 20px; box-sizing: border-box">
            <el-input
              v-model="search.alg_ch_name"
              @change="getTableData"
              style="width: 150rem; height: 32rem"
            ></el-input>
          </div>
          <el-option
            v-for="(item, index) in allAlgs"
            :key="index + Math.random()"
            :label="item.ch_name"
            :value="item.ch_name"
          >
          </el-option>
        </el-select> -->
      </div>
      <div class="search-item">
        <el-input
          v-model="search.hazardLevel"
          clearable
          placeholder="Alarm Level"
          @input="
            search.pageNum = 1;
            getTableData();
          "
        ></el-input>
      </div>
      <div class="search-item">
        <el-select
          v-model="search.status"
          clearable
          placeholder="Status"
          @change="
            search.pageNum = 1;
            getTableData();
          "
        >
          <el-option label="Confirmed" :value="2"></el-option>
          <el-option label="Pending" :value="1"></el-option>
        </el-select>
      </div>
      <div class="search-item">
        <el-date-picker
          v-model="search.startTime"
          type="datetime"
          @change="
            search.pageNum = 1;
            getTableData();
          "
          placeholder="Start Time"
          default-time="00:00:00"
        >
        </el-date-picker>
      </div>
      <div class="search-item">
        <el-date-picker
          v-model="search.endTime"
          type="datetime"
          @change="
            search.pageNum = 1;
            getTableData();
          "
          placeholder="End Time"
          default-time="23:59:59"
        >
        </el-date-picker>
      </div>
      <div class="search-btns">
        <el-button
          title="Search"
          type="primary"
          class="searchbtn"
          icon="z-icon-search"
          @click="
            search.pageNum = 1;
            getTableData();
          "
        ></el-button>
      </div>
    </div>
    <div class="btns-container">
      <el-tooltip content="Export" placement="top">
        <el-button
          type="primary"
          @click="handleExport()"
          :loading="alertExportLoading"
          v-if="permission == 20 || permission == 10"
          ><i class="z-icon-xiazai"></i
        ></el-button>
      </el-tooltip>
      <!-- <el-tooltip content="Batch Confirm" placement="top">
        <el-button
          type="warning"
          @click="handleAlertDeal()"
          :loading="alertDealLoading"
          v-if="permission == 20 || permission == 10"
          ><i class="z-icon-daichuli"></i
        ></el-button>
      </el-tooltip>
      <el-tooltip content="Batch Delete" placement="top">
        <el-button
          title="Batch Delete"
          type="danger"
          :loading="alertDelLoading"
          @click="handleDel()"
          v-if="permission == 20 || permission == 10"
          ><i class="z-icon-del"></i
        ></el-button>
      </el-tooltip> -->
    </div>
    <div class="table-container height1">
      <el-table
        v-loading="listLoading"
        element-loading-text="Loading"
        element-loading-spinner="el-icon-loading"
        :data="tableData"
        :border="false"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          v-if="permission == 20 || permission == 10"
        >
        </el-table-column>
        <el-table-column prop="alertTime" label="Alarm Time"> </el-table-column>
        <el-table-column prop="sourceDesc" label="Source"> </el-table-column>
        <el-table-column prop="deviceName" label="Device Name"></el-table-column>
        <el-table-column prop="hazardLevel" label="Alarm Level"></el-table-column>
        <el-table-column label="Alarm Type">
          <template slot-scope="scope">
            <span>{{ scope.row.algChName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Snapshot">
          <template slot-scope="scope">
            <div
              style="width: 150px; height: 100px; margin: 0 auto"
              @click="handleShowAlertDetail(scope.row)"
            >
              <img
                style="
                  max-width: 150px;
                  max-height: 100px;
                  margin: 0 auto;
                  cursor: pointer;
                "
                :src="scope.row.imgpath"
                alt=""
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Video">
          <template slot-scope="scope">
            <div
              v-if="scope.row.videopath"
              style="width: 150px; height: 100px; margin: 0 auto"
              @click="handleShowVideo(scope.row)"
            >
              <img
                style="max-width: 150px; max-height: 100px; margin: 0 auto"
                :src="scope.row.imgpath"
                alt=""
              />
              <!-- <i class="z-icon-play1" style="position:absolute;"></i> -->
            </div>
          </template>
        </el-table-column>
        <!-- <el-table-column label="Alarm Handling" width="140px">
          <template slot-scope="scope">
            <el-tooltip
              class="item"
              effect="dark"
              content="下载原图"
              placement="top"
            >
              <i
                v-if="scope.row.imgpath"
                class="z-icon-xiazaitupian"
                @click="handleDownloadPic(scope.row)"
                style="margin-right: 20px; cursor: pointer"
              ></i>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="下载视频"
              placement="top"
            >
              <i
                class="z-icon-xiazaishipin"
                @click="handleDownloadvideo(scope.row)"
                v-if="scope.row.videopath && !scope.row.loading"
                style="margin-right: 20px; cursor: pointer"
              ></i>
              <i
                class="z-icon-xiazaishipin disabled"
                v-if="scope.row.videopath && scope.row.loading"
                style="margin-right: 20px; cursor: pointer"
              ></i>
            </el-tooltip> -->
            <!-- <el-tooltip
              class="item"
              effect="dark"
              content="Confirmed"
              placement="top"
            >
              <i
                v-show="scope.row.status == 2"
                class="z-icon-queren"
                @click="handleAlertStatusRestet(scope.row)"
                style="margin-right: 20px; cursor: pointer; color: #11b111"
              ></i>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="Pending"
              placement="top"
            >
              <i
                v-show="scope.row.status == 1"
                class="z-icon-daichuli"
                @click="handleAlertDeal(scope.row)"
                style="margin-right: 20px; cursor: pointer; color: #e6a23c"
              ></i>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="Delete"
              placement="top"
            >
              <i
                class="z-icon-del"
                style="cursor: pointer"
                @click="handleDel(scope.row)"
                v-if="permission == 20 || permission == 10"
              ></i>
            </el-tooltip>
          </template>
        </el-table-column> -->
      </el-table>
      <div class="pagination-model" v-show="listLoading"></div>
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="search.pageNum"
        :page-sizes="[10, 15, 20, 30, 40, 50]"
        :page-size="search.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        popper-class="pagination-popper"
      >
      </el-pagination>
    </div>

    <el-dialog
      :title="'Alarm Snapshot'"
      :append-to-body="true"
      :visible.sync="showAlert.show"
      class="alarmDetail-dialog"
      v-dialogDrag
      :class="{
        commondetail:
          showAlert.alg_name != 'face' ||
          (showAlert.alg_name == 'face' &&
            showAlert.reserved_data.face &&
            showAlert.reserved_data.face.length == 0),
        facedetail:
          showAlert.alg_name == 'face' &&
          showAlert.reserved_data.face &&
          showAlert.reserved_data.face.length > 0,
      }"
    >
      <ul class="alarm-infos">
        <li>
          <label for="">Alarm Time：</label
          ><span>{{ showAlert.alert_time }}</span>
        </li>
        <li>
          <label for="">Alarm Type：</label
          ><span>{{ showAlert.alg_ch_name }}</span>
        </li>
        <li>
          <label for="">Alarm Level：</label
          ><span>{{ showAlert.hazard_level }}</span>
        </li>
        <li>
          <label for="">Source：</label><span>{{ showAlert.source_desc }}</span>
        </li>
        <li>
          <label for="">Device Name：</label
          ><span>{{ showAlert.device_name }}</span>
        </li>
        <li
          v-if="
            showAlert &&
            showAlert.reserved_data &&
            showAlert.reserved_data.group
          "
        >
          <label for="">分组名称：</label>
          <span>{{ showAlert.reserved_data.group.name }}</span>
        </li>
      </ul>
      <div class="img-container" style="width: 100%; margin-top: 40px">
        <div class="img">
          <img :src="showAlert.imgpath" v-show="!showAlert.showOripic" />
          <img :src="showAlert.oriimg" v-show="showAlert.showOripic" />
          <el-button
            class="switchimg"
            type="primary"
            @click="showAlert.showOripic = !showAlert.showOripic"
            v-if="showAlert.oriimg"
            >Switch Image</el-button
          >
          <el-button
            class="downimg"
            type="primary"
            v-if="showAlert.oriimg"
            @click="handleDownloadPic(showAlert)"
            >Download Image</el-button
          >
          <el-button
            class="alert-deal"
            type="primary"
            v-if="showAlert.status == 1"
            @click="handleAlertDeal(showAlert)"
            style="background-color: orange; border-color: orange"
            >Pending</el-button
          >
          <el-button
            class="alert-deal"
            type="primary"
            v-if="showAlert.status == 2"
            style="background-color: green; border-color: green"
            @click="handleAlertStatusRestet(showAlert)"
            >Confirm</el-button
          >
        </div>

        <div
          class="faceInfo"
          v-if="
            showAlert.reserved_data.face &&
            showAlert.reserved_data.face.length > 0
          "
        >
          <ul>
            <li
              v-for="(infos, index) in showAlert.reserved_data.face"
              :key="index"
            >
              <div class="cap">
                <img :src="projConfig.picprev + infos.cap_image" alt="" />
              </div>
              <div class="libImage">
                <img :src="projConfig.picprev + infos.lib_image" alt="" />
              </div>
              <div class="libInfo">
                <p v-if="infos.conf">
                  <label>相似度：</label
                  ><span>{{ (infos.conf * 100).toFixed(0) }}%</span>
                </p>
                <p>
                  <label>姓名：</label><span>{{ infos.name }}</span>
                </p>
                <p>
                  <label>年龄：</label><span>{{ infos.age }}</span>
                </p>
                <p>
                  <label>性别：</label><span>{{ infos.sex }}</span>
                </p>
                <p>
                  <label>简介：</label><span>{{ infos.desc }}</span>
                </p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      :title="'Video'"
      :append-to-body="true"
      :visible.sync="showVideo.show"
      class="alarmDetail-dialog commondetail video-dialog"
      v-dialogDrag
    >
      <el-button
        class="downvideo"
        type="primary"
        v-if="showVideo.videopath"
        @click="handleDownloadvideo(showVideo)"
        >Download Video</el-button
      >
      <video controls :src="showVideo.videopath" style="width: 100%"></video>
    </el-dialog>
  </div>
</template>

<script>
import { projConfig } from "@/utils/config.js";
import { alertV2Apis, alertApis } from "@/api/alert.js";
import { deviceV2Apis } from "@/api/device.js";
import { algApis } from "@/api/alg.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  data() {
    return {
      permission: 10,
      projConfig: projConfig,
      api: alertApis,
      search: {
        deviceId: "",
        sourceDesc: "",
        algChName: "",
        hazardLevel: "",
        startTime: "",
        endTime: "",
        status: "",
        pageNum: 1,
        pageSize: 10,
      },
      alertDealLoading: false,
      alertDelLoading: false,
      alertExportLoading: false,
      devices: {},
      sourceDesc: [],
      listLoading: false,
      total: 0,
      tableData: [],
      tableSel: [],
      allAlgs: [],

      showAlert: {
        show: false,
        id: "",
        status: 0,
        alert_time: "",
        source_desc: "",
        device_id: "",
        device_name: "",
        hazard_level: "",
        imgpath: "",
        oriimg: "",
        showOripic: false,
        reserved_data: {},
      },
      showVideo: {
        show: false,
        videopath: "",
      },
    };
  },
  watch: {
    "search.device_id"() {
      this.search.sourceDesc = "";
      this.search.algChName = "";
    },
  },
  beforeMount() {
    this.permission = localStorage.getItem("permission");
  },
  async mounted() {
    this.getTableData();
    const { rows } = await deviceV2Apis.getTableList();
    this.devices = rows || [];

    // alertApis.device().then((devices) => {
    //   this.devices = {};
    //   for (let deviceName in devices) {
    //     this.devices[deviceName] = {
    //       ids: devices[deviceName].join(","),
    //       name: deviceName,
    //     };
    //   }
    // });
  },
  methods: {
    getTableData() {
      let params = {
        pageNum: this.search.pageNum,
        pageSize: this.search.pageSize,
      };
      this.search.startTime &&
        (params.startTime = new Date(this.search.startTime).format('yyyy-MM-dd HH:mm:ss'));
      this.search.endTime &&
        (params.endTime = Date.parse(this.search.endTime) / 1000);
      if (
        this.search.startTime &&
        this.search.endTime &&
        params.start_time > params.endTime
      ) {
        this.$message({
          type: "error",
          message: "The start time is greater than the end time",
        });
        return;
      }
      this.search.sourceDesc && (params.sourceDesc = this.search.sourceDesc);
      this.search.algChName && (params.algChName = this.search.algChName);
      this.search.status && (params.status = this.search.status);
      this.search.hazardLevel && (params.hazardLevel = this.search.hazardLevel);
      this.search.deviceId && (params.deviceId = this.search.deviceId)
      // this.search.device_ids.length > 0 &&
      //   (params.device_ids = JSON.stringify(
      //     this.search.device_ids.join(",").split(",")
      //   ));
        alertV2Apis.getTableData(params).then((res) => {
        this.tableData = [...res.rows].map((item) => {
          item.alertTime = new Date(item.alertTime * 1000).format(
            "YYYY-MM-DD HH:mm:ss"
          );
          if (item.image) {
            item.imgpath = projConfig.picprev + item.image;
            item.oriimg =
              projConfig.picprev + item.image.replace("alert.jpg", "raw.jpg");
          }

          if (item.video) {
            item.videopath = projConfig.picprev + item.video;
          } else {
            item.videopath = "";
          }

          // item.alert = JSON.parse(item.objs)[0];
          // item.roi = JSON.parse(item.roi);
          // item.polys = JSON.parse(item.polys);
          item.loading = false;
          return item;
        });
        this.total = res.total;
      });
    },
    handleSelectionChange(rows) {
      this.tableSel = [...rows];
    },
    handleSizeChange(val) {
      this.search.pageSize = val;
      this.search.pageNum = 1;
      this.getTableData();
    },
    handlePageChange(val) {
      this.search.pageNum = val;
      this.getTableData();
    },
    handleShowAlertDetail(item) {
      this.showAlert.show = true;
      this.showAlert = Object.assign(this.showAlert, item);
      this.showAlert.reserved_data =
        JSON.stringify(this.showAlert.reserved_data) == "{}"
          ? {}
          : JSON.parse(this.showAlert.reserved_data);
    },
    handleShowVideo(row) {
      if (row.videopath) {
        this.showVideo.show = true;
        this.showVideo.videopath = row.videopath;
      }
    },
    handleAlertpicChange(item) {
      this.showAlert.imgIndex = item + 1;
    },
    handleDel(row) {
      let msg = "";
      if (row) {
        msg = "Are you sure to delete this alarm record？";
      } else if (this.tableSel.length > 0) {
        msg = "Are you sure to delete the selected alarm record？";
      } else {
        msg = "Are you sure to delete all alarm records under the current query conditions？";
        if (
          this.search.start_time &&
          this.search.end_time &&
          new Date(this.search.start_time) > new Date(this.search.end_time)
        ) {
          this.$message({
            type: "error",
            message: "The start time is greater than the end time",
          });
          return;
        }
        this.alertDelLoading = true;
      }
      this.$confirm(msg, "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      }).then(() => {
        let params = {};
        if (row) {
          params.alert_ids = [row.id];
        } else if (this.tableSel.length > 0) {
          params.alert_ids = this.tableSel.map((item) => item.id);
        } else {
          this.search.source_desc &&
            (params.source_desc = this.search.source_desc);
          this.search.status && (params.status = this.search.status);
          this.search.alg_ch_name &&
            (params.alg_ch_name = this.search.alg_ch_name);
          this.search.hazard_level &&
            (params.hazard_level = this.search.hazard_level);
          this.search.device_ids.length > 0 &&
            (params.device_ids = this.search.device_ids.join(",").split(","));

          this.search.start_time &&
            (params.start_time = Date.parse(this.search.start_time) / 1000);
          this.search.end_time &&
            (params.end_time = Date.parse(this.search.end_time) / 1000);
        }
        this.api
          .delete({ condition: params })
          .then((res) => {
            this.getTableData();
            this.$message({
              type: "success",
              message: res.message[this.lang],
            });
            this.alertDelLoading = false;
          })
          .catch((err) => {
            this.alertDelLoading = false;
            if (err && err.message && err.message[this.lang]) {
              let msg = err.message[this.lang];
              if (err.data) {
                for (let devId in err.data) {
                  if (err.data[devId].result == false) {
                    let deviceName = "";
                    for (let devName in this.devices) {
                      if (this.devices[devName].ids.indexOf(devId) >= 0) {
                        deviceName = devName;
                      }
                    }
                    msg =
                      msg +
                      '<br style="margin:5px 0;"/>' +
                      deviceName +
                      ":" +
                      err.data[devId].reason[this.lang];
                  }
                }
                this.$message({
                  type: "error",
                  message: msg,
                  duration: 0,
                  showClose: true,
                  dangerouslyUseHTMLString: true,
                });
              } else {
                this.$message({
                  type: "error",
                  message: msg,
                });
              }

              this.getTableData();
            }
          });
      });
    },
    handleAlertDeal(row) {
      let msg = "";
      if (row) {
        msg = "Do you confirm this alarm？";
      } else if (this.tableSel.length > 0) {
        msg = "Are you sure the selected item is an alarm？";
      } else {
        msg = "Are you sure that all records under the current query conditions are alarms？";
        if (
          this.search.start_time &&
          this.search.end_time &&
          new Date(this.search.start_time) > new Date(this.search.end_time)
        ) {
          this.$message({
            type: "error",
            message: "The start time is greater than the end time",
          });
          return;
        }
        this.alertDealLoading = true;
      }
      this.$confirm(msg, "Alarm Confirm", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      }).then(() => {
        let params = {};
        if (row) {
          params.alert_ids = [row.id];
        } else if (this.tableSel.length > 0) {
          params.alert_ids = this.tableSel.map((item) => item.id);
        } else {
          this.search.source_desc &&
            (params.source_desc = this.search.source_desc);
          this.search.status && (params.status = this.search.status);
          this.search.alg_ch_name &&
            (params.alg_ch_name = this.search.alg_ch_name);
          this.search.hazard_level &&
            (params.hazard_level = this.search.hazard_level);
          this.search.device_ids.length > 0 &&
            (params.device_ids = this.search.device_ids.join(",").split(","));

          this.search.start_time &&
            (params.start_time = Date.parse(this.search.start_time) / 1000);
          this.search.end_time &&
            (params.end_time = Date.parse(this.search.end_time) / 1000);
        }
        this.api
          .deal({ condition: params, status: 2 })
          .then((res) => {
            this.alertDealLoading = false;
            this.getTableData();
            this.$message({
              type: "success",
              message: "Update alarm success！",
            });
            if (this.showAlert.show) {
              this.showAlert.status = 2;
            }
          })
          .catch((err) => {
            this.alertDealLoading = false;
            if (err && err.message && err.message[this.lang]) {
              let msg = err.message[this.lang];
              if (err.data) {
                for (let devId in err.data) {
                  if (err.data[devId].result == false) {
                    let deviceName = "";
                    for (let devName in this.devices) {
                      if (this.devices[devName].ids.indexOf(devId) >= 0) {
                        deviceName = devName;
                      }
                    }
                    msg =
                      msg +
                      '<br style="margin:5px 0;"/>' +
                      deviceName +
                      ":" +
                      err.data[devId].reason[this.lang];
                  }
                }
                this.$message({
                  type: "error",
                  message: msg,
                  showClose: true,
                  duration: 0,
                  dangerouslyUseHTMLString: true,
                });
              } else {
                this.$message({
                  type: "error",
                  message: msg,
                });
              }

              this.getTableData();
            }
          });
      });
    },
    handleAlertStatusRestet(row) {
      let msg = "";
      if (row) {
        msg = "Do you cancel this alarm？";
      } else {
        msg = "Are you sure that all records under the current query conditions have been restored to unconfirmed？";
      }
      this.$confirm(msg, "Restore to pending", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      }).then(() => {
        this.api
          .deal({
            condition: {
              alert_ids: [row.id],
              // device_id: row.device_id,
            },
            status: 1,
          })
          .then((res) => {
            this.getTableData();
            this.$message({
              type: "success",
              message: res.message[this.lang],
            });
            if (this.showAlert.show == true) {
              this.showAlert.status = 1;
            }
          })
          .catch((err) => {
            if (err && err.message && err.message[this.lang]) {
              let msg = err.message[this.lang];
              for (let devId in err.data) {
                if (err.data[devId].result == false) {
                  let deviceName = "";
                  for (let devName in this.devices) {
                    if (this.devices[devName].ids.indexOf(devId) >= 0) {
                      deviceName = devName;
                    }
                  }
                  msg =
                    msg +
                    '<br style="margin:5px 0;"/>' +
                    deviceName +
                    ":" +
                    err.data[devId].reason[this.lang];
                }
              }
              this.$message({
                type: "error",
                message: msg,
                showClose: true,
                duration: 0,
                dangerouslyUseHTMLString: true,
              });
              this.getTableData();
            }
          });
      });
    },
    handleDownloadPic(row) {
      if (row.oriimg) {
        this.download(row.oriimg, "raw.jpg");
      } else if (row.imgpath) {
        let path = row.imgpath.replace("alert.jpg", "raw.jpg");
        this.download(path, "raw.jpg");
      }
    },
    handleDownloadvideo(row) {
      if (row.videopath) {
        row.loading = true;
        this.download(row.videopath, "video.mp4", row);
      }
    },
    download(fileUrl, fileName, row) {
      if ("undefined" == typeof fileUrl || !fileUrl) {
        this.$message({
          type: "error",
          message: "The file path is null",
        });
        return false;
      }
      // 发送http请求，将文件链接转换成文件流
      fileAjaxFun(
        fileUrl,
        function (xhr) {
          downloadFun(xhr.response, fileName);
        },
        {
          responseType: "blob",
        }
      );
      // 发起请求
      function fileAjaxFun(url, callback, options) {
        let xhr = new XMLHttpRequest();
        xhr.open("get", url, true);
        if (options.responseType) {
          xhr.responseType = options.responseType;
        }
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4 && xhr.status === 200) {
            callback(xhr);
          }
        };
        xhr.send();
      }
      // 进行下载
      function downloadFun(content, filename) {
        window.URL = window.URL || window.webkitURL;
        let a = document.createElement("a");
        let blob = new Blob([content]);
        // 通过二进制文件创建url
        let url = window.URL.createObjectURL(blob);
        a.href = url;
        a.download = filename;
        a.click();
        // 销毁创建的url
        window.URL.revokeObjectURL(url);
        if (row) {
          row.loading = false;
        }
      }
    },
    async handleExport() {
      // let msg = "";
      // if (this.tableSel.length > 0) {
      //   msg = "Are you sure to export the selected alarm records？";
      // } else {
      //   msg = "Are you sure to export all alarm records under the current query conditions？";
      //   if (this.total > 1000) {
      //     this.$message({
      //       type: "error",
      //       message:
      //         "Up to 1000 alarm records can be exported at a time. Under the current query conditions, there are more than 1000 alarm records. Please modify the query conditions before exporting！",
      //     });
      //     return 
      //   }
      // }

      // this.$confirm(msg, "Export", {
      //   confirmButtonText: "Confirm",
      //   cancelButtonText: "Cancel",
      // }).then(() => {
        this.alertExportLoading = true;
        let params = {};
        // if (this.tableSel.length > 0) {
        //   params.alert_ids = JSON.stringify(this.tableSel.map((item) => item.id));
        // } else {
        this.search.sourceDesc && (params.sourceDesc = this.search.sourceDesc);
        this.search.status && (params.status = this.search.status);
        this.search.algChName && (params.algChName = this.search.algChName);
        this.search.hazardLevel && (params.hazardLevel = this.search.hazardLevel);
        this.search.deviceId && (params.deviceId = this.search.deviceId);
        // this.search.deviceId.length > 0 && (params.deviceId = this.search.deviceId);
          // (params.device_ids = JSON.stringify(this.search.device_ids))//this.search.device_ids.join(",").split(","));

        // this.search.startTime && (params.start_time = Date.parse(this.search.start_time) / 1000);
        // this.search.end_time &&  (params.end_time = Date.parse(this.search.end_time) / 1000);
        // }
        try {
        const res = await alertV2Apis.alertExport(params);
        if (res instanceof Blob) {
          var blob = new Blob([res]);
          var url = URL.createObjectURL(blob);
          var a = document.createElement("a");
          a.href = url;
          a.download = "alert.zip";
          a.click();
          window.URL.revokeObjectURL(url);
        }
      } catch (error) {}
      finally {
        this.alertExportLoading = false;
      }
    },
  },
};
</script>

<style lang="less" scoped>
.btns-container {
  top: 52rem;
  /deep/.el-loading-spinner {
    margin-top: -15rem;
  }
}
.alarm-infos {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
  li {
    width: 50%;
    label {
      color: #333;
    }
  }
}
.table-container {
  /deep/.cell {
    text-overflow: clip;
  }
}
.z-icon-xiazaishipin {
  cursor: pointer;
}
.z-icon-xiazaishipin.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
<style lang="less">
.alarmDetail-dialog {
  .el-dialog {
    border-radius: 4px;
    .el-dialog__header {
      border-bottom: none;
      text-align: center;
      .el-dialog__title {
        // color: #00e2fd;
      }
      .el-dialog__headerbtn {
        top: 20rem;
        right: 20rem;
      }
    }
  }
  .alarm-infos {
    display: flex;
    flex-wrap: wrap;
    font-size: 14rem;
    li {
      width: 50%;
      margin-bottom: 5rem;
      label {
        // color: #659ea5;
      }
      span {
        // color: #a3d7dd;
      }
    }
  }
}
.alarmDetail-dialog.commondetail {
  .el-dialog {
    width: calc(100% - 40rem) !important;
    max-width: 1000px;
  }
  .img-container {
    display: flex;
    justify-content: center;
    // padding: 0 30rem;
    box-sizing: border-box;
    .img {
      width: 100%;
      position: relative;
      img {
        width: 100%;
      }
      .switchimg {
        position: absolute;
        top: -40rem;
        right: 0rem;
      }
      .downimg {
        position: absolute;
        top: -40rem;
        right: 120rem;
      }
      .alert-deal {
        position: absolute;
        top: -40rem;
        right: 262rem;
      }
    }
  }
}
.alarmDetail-dialog.facedetail {
  .el-dialog {
    width: calc(100% - 40rem) !important;
    max-width: 1200px;
  }
  .img-container {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    // padding: 0 30rem;
    box-sizing: border-box;
    height: 100%;
    .img {
      width: calc(100% - 350rem);
      margin-right: 20rem;
      position: relative;
      img {
        width: 100%;
      }
      .switchimg {
        position: absolute;
        top: -40rem;
        right: 0rem;
      }
      .downimg {
        position: absolute;
        top: -40rem;
        right: 120rem;
      }
      .alert-deal {
        position: absolute;
        top: -40rem;
        right: 262rem;
      }
    }
    .faceInfo {
      width: 330rem;
      max-height: calc(100vh - 350rem);
      overflow: auto;
      font-size: 14rem;
      li {
        display: flex;
        margin-bottom: 10rem;
        img {
          width: 100rem;
          height: 100rem;
          object-fit: contain;
        }
      }
    }
  }
}
.alarmDetail-dialog.video-dialog {
  .el-dialog__body {
    overflow: visible;
    .downvideo {
      position: absolute;
      top: -40px;
      right: 50px;
    }
  }
}
</style>