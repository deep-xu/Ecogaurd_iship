
<template>
  <div class="alarm">
    <div class="search-container">
      <!-- <div class="search-item">
          <el-input
            v-model="search.device_name"
            clearable
            placeholder="设备名称"
            @change="getTableData"
          ></el-input>
        </div> -->
      <div class="search-item">
        <el-select
          v-model="search.device_name"
          clearable
          placeholder="Device"
          @change="
            search.page = 1;
            getTableData();
          "
        >
          <!-- <div style="padding: 0 20px; box-sizing: border-box">
            <el-input
              v-model="search.device_name"
              @change="getTableData"
              style="width: 150rem; height: 32rem"
            ></el-input>
          </div> -->
          <el-option
            v-for="(item, deviceid) in devices"
            :key="deviceid"
            :label="item.name"
            :value="item.name"
          >
          </el-option>
        </el-select>
      </div>
      <div class="search-item">
        <el-input
          v-model="search.source_desc"
          clearable
          placeholder="Source"
          @input="
            search.page = 1;
            getTableData();
          "
        ></el-input>
      </div>
      <div class="search-item">
        <el-select
          v-model="search.alg_ch_name"
          clearable
          placeholder="Detection Type"
          @change="
            search.page = 1;
            getTableData();
          "
        >
          <!-- <div style="padding: 0 20px; box-sizing: border-box">
            <el-input
              v-model="search.alg_ch_name"
              @change="getTableData"
              style="width: 150rem; height: 32rem"
            ></el-input>
          </div> -->
          <el-option
            v-for="(item, index) in allAlgs"
            :key="index + Math.random()"
            :label="item.ch_name"
            :value="item.ch_name"
          >
          </el-option>
        </el-select>
      </div>
      <div class="search-item">
        <el-date-picker
          v-model="search.start_time"
          type="datetime"
          @change="
            search.page = 1;
            getTableData();
          "
          placeholder="Start Time"
          default-time="00:00:00"
        >
        </el-date-picker>
      </div>
      <div class="search-item">
        <el-date-picker
          v-model="search.end_time"
          type="datetime"
          @change="
            search.page = 1;
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
            search.page = 1;
            getTableData();
          "
        ></el-button>
      </div>
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
        <el-table-column prop="alert_time" label="Capture Time"> </el-table-column>
        <el-table-column prop="source_desc" label="Source"> </el-table-column>
        <el-table-column prop="device_name" label="Device Name"></el-table-column>
        <el-table-column label="Detection Type">
          <template slot-scope="scope">
            <span>{{ scope.row.alg_ch_name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Capture Pictures">
          <template slot-scope="scope">
            <div
              style="width: 150px; height: 100px; margin: 0 auto"
              @click="handleShowAlertDetail(scope.row)"
            >
              <img
                style="max-width: 150px; max-height: 100px; margin: 0 auto"
                :src="scope.row.imgpath"
                alt=""
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Name"></el-table-column>
        <el-table-column prop="direction" label="Direction">
          <template slot-scope="scope">
            {{ directions[scope.row.direction] }}
          </template>
        </el-table-column>
        <el-table-column label="Counting">
          <template slot-scope="scope">
            <ul style="list-style: none">
              <li v-if="scope.row.direction.length == 2">
                {{ scope.row.action.count }}:{{ scope.row.result.count }}
              </li>
              <li v-if="scope.row.direction.length == 4">
                {{ scope.row.action.increase }}:{{ scope.row.result.increase }}
              </li>
              <li v-if="scope.row.direction.length == 4">
                {{ scope.row.action.decrease }}:{{ scope.row.result.decrease }}
              </li>
              <li v-if="scope.row.direction.length == 4">
                {{ scope.row.action.delta }}:{{ scope.row.result.delta }}
              </li>
            </ul>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-model" v-show="listLoading"></div>
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="search.page"
        :page-sizes="[10, 15, 20, 30, 40, 50]"
        :page-size="search.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        popper-class="pagination-popper"
      >
      </el-pagination>
    </div>

    <el-dialog
      :title="'1'"
      :append-to-body="true"
      :visible.sync="showAlert.show"
      class="crosscount-dialog"
      v-dialogDrag
      :class="{
        commondetail:
          showAlert.reserved_data.length == 0 ||
          !(showAlert.reserved_data.length > 0),
        facedetail:
          showAlert.reserved_data.length > 0 &&
          showAlert.reserved_data.length > 0,
      }"
    >
      <div class="img-container" style="width: 100%">
        <div class="img">
          <img :src="showAlert.imgpath" v-show="!showAlert.showOripic" />
          <img :src="showAlert.oriimg" v-show="showAlert.showOripic" />
          <el-button
            class="switchimg"
            type="primary"
            @click="showAlert.showOripic = !showAlert.showOripic"
            >Switch Image</el-button
          >
        </div>
      </div>
    </el-dialog>

    <el-dialog
      :title="'Alarm Video'"
      :append-to-body="true"
      :visible.sync="showVideo.show"
      class="crosscount-dialog"
      v-dialogDrag
    >
      <video controls :src="showVideo.videopath" style="width: 100%"></video>
    </el-dialog>
  </div>
</template>
  
  <script>
import { projConfig } from "@/utils/config.js";
import { deviceApis, sourceApis } from "@/api/device.js";
import { alertApis } from "@/api/alert.js";
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
        device_name: "",
        device_id: "",
        source_desc: "",
        alg_ch_name: "",
        hazard_level: "",
        start_time: "",
        end_time: "",
        page: 1,
        size: 10,
      },
      devices: {},
      sourceDesc: [],
      listLoading: false,
      total: 0,
      tableData: [],
      tableSel: [],
      allAlgs: [],

      showAlert: {
        show: false,
        showOripic: false,
        alert_time: "",
        source_desc: "",
        device_id: "",
        device_name: "",
        hazard_level: "",
        imgpath: "",
        oriimg: "",
        reserved_data: [],
      },
      showVideo: {
        show: false,
        videopath: "",
      },
      directions: {
        "r+": "从左到右",
        "l+": "从右到左",
        "d+": "从上到下",
        "u+": "从下到上",
        "l+r-": "左增右减",
        "l-r+": "左减右增",
        "u+d-": "上增下减",
        "u-d+": "上减下增",
      },
    };
  },
  watch: {
    "search.device_id"() {
      this.search.source_desc = "";
      this.search.alg_ch_name = "";
    },
  },
  beforeMount() {
    this.permission = localStorage.getItem("permission");
  },
  mounted() {
    this.getTableData();
    deviceApis.getTableData().then((devices) => {
      this.devices = {};
      for (let i = 0; i < devices.length; i++) {
        devices[i].device_id = devices[i].id;
        devices[i].name = devices[i].name || devices[i].id;
        this.devices[devices[i].id] = devices[i];
      }
      // algApis.getTableData().then((res) => {
      //   let allAlgsObj = {};
      //   for (let deviceid in res) {
      //     for (let i = 0; i < res[deviceid].length; i++) {
      //       if (!allAlgsObj[res[deviceid][i].name]) {
      //         allAlgsObj[res[deviceid][i].name] = res[deviceid][i];
      //       }
      //     }
      //     if (this.devices[deviceid]) {
      //       if (this.devices[deviceid]) {
      //         this.devices[deviceid].alg = res[deviceid];
      //       }
      //     }
      //   }
      //   for (let alg_name in allAlgsObj) {
      //     this.allAlgs.push(allAlgsObj[alg_name]);
      //   }
      //   this.$forceUpdate();
      // });

      if (devices.length > 0) {
        sourceApis.getTableData().then((res) => {
          let algset = new Set();
          // let descset = new Set();
          Object.keys(res).forEach((deviceid) => {
            for (let source in res[deviceid]) {
              let algs = res[deviceid][source].alg || {};
              for (let algname in algs) {
                if (algs[algname].alg_type == "cross_line_counting") {
                  // descset.add(res[deviceid].desc);
                  algset.add({
                    alg: algname,
                    ch_name: algs[algname].reserved_args.ch_name,
                  });
                  // this.devices[deviceid].source_desc_list = [...descset];
                  break;
                }
              }
            }
          });

          this.allAlgs = [...algset];
        });
      }
    });
  },
  methods: {
    getTableData() {
      let start_time = "",
        end_time = "";
      this.search.start_time &&
        (start_time = Date.parse(this.search.start_time) / 1000);
      this.search.end_time &&
        (end_time = Date.parse(this.search.end_time) / 1000);
      if (
        this.search.start_time &&
        this.search.end_time &&
        start_time > end_time
      ) {
        this.$message({
          type: "error",
          message: "The start time is greater than the end time",
        });
        return;
      }
      this.api
        .crosslinecount({
          page: this.search.page,
          size: this.search.size,
          source_desc: this.search.source_desc || null,
          alg_ch_name: this.search.alg_ch_name || null,
          start_time: start_time || null,
          end_time: end_time || null,
          device_name: this.search.device_name || null,
          device_id: this.search.device_id || null,
        })
        .then((res) => {
          this.tableData = [...res.data].map((item) => {
            item.alert_time = new Date(item.alert_time * 1000).format(
              "YYYY-MM-DD HH:mm:ss"
            );
            item.action = JSON.parse(item.action);
            item.result = JSON.parse(item.result);
            item.imgpath = item.image
              ? this.projConfig.picprev + item.image
              : "";
            item.oriimg = item.image
              ? this.projConfig.picprev +
                item.image.replace("alert.jpg", "raw.jpg")
              : "";
            return item;
          });
          this.total = res.total;
        });
    },
    handleSelectionChange(rows) {
      this.tableSel = [...rows];
    },
    handleSizeChange(val) {
      this.search.size = val;
      this.search.page = 1;
      this.getTableData();
    },
    handlePageChange(val) {
      this.search.page = val;
      this.getTableData();
    },
    handleShowAlertDetail(item) {
      this.showAlert.show = true;
      this.showAlert = Object.assign(this.showAlert, item);
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
      } else {
        msg = "Are you sure to delete the selected alarm record？";
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
      }
      this.$confirm(msg, "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      }).then(() => {
        let params = {};
        if (row) {
          params.ids = [row.id];
        } else {
          params = {
            source_desc: this.search.source_desc || null,
            alg_ch_name: this.search.alg_ch_name || null,
            device_name: this.search.device_name || null,
            device_id: this.search.device_id || null,
          };
          this.search.start_time &&
            (params.start_time = Date.parse(this.search.start_time) / 1000);
          this.search.end_time &&
            (params.end_time = Date.parse(this.search.end_time) / 1000);
        }
        this.api
          .delete(params.ids ? params : { condition: params })
          .then((res) => {
            this.getTableData();
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
      });
    },
    handleDownloadPic(row) {
      let path = row.imgpath.replace("alert.jpg", "raw.jpg");
      this.download(path, "raw.jpg");
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
  },
};
</script>
  
  <style lang="less" scoped>
.btns-container {
  top: 52rem;
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
.crosscount-dialog {
  .el-dialog {
    border-radius: 4px;
    .el-dialog__header {
      border-bottom: none;
      text-align: center;
      .el-dialog__title {
        // color: #00e2fd;
        color: #fff;
      }
      .el-dialog__headerbtn {
        top: 20rem;
        right: 20rem;
      }
    }
    .el-dialog__body {
      overflow: visible;
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

  .switchimg {
    position: absolute;
    top: -30px;
    left: 20px;
  }
}
.crosscount-dialog.commondetail {
  .el-dialog {
    width: calc(100% - 40rem) !important;
    max-width: 1000px;
  }
  .img-container {
    display: flex;
    justify-content: center;
    // padding: 0 30rem;
    box-sizing: border-box;
    img {
      width: 100%;
    }
  }
}
</style>