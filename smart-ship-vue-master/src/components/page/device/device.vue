<template>
  <div class="device-container">
    <div
      class="type-device"
      :class="{ fold: type == 'camera', unfold: type == 'device' }"
    >
      <template v-if="type == 'device'">
        <div class="device-title">
          <h3>
            <div
              class="device_type"
              :class="{ active: deviceType == 'normal' }"
              @click="
                deviceType = 'normal';
                filterTable();
              "
            >
            Camera
            </div>
            <span class="split"></span>
            <!-- <div
              class="device_type"
              :class="{ active: deviceType == 'forward' }"
              @click="
                deviceType = 'forward';
                filterTable();
              "
            >
            Stream
            </div> -->
          </h3>
          <div class="count">
            <p>Total：{{ tableData.length }}</p>
            <p class="online">
              Online：{{ tableData.filter((item) => item.status == 1).length }}
            </p>
            <p v-if="deviceType == 'normal'">
              <i
                class="z-icon-warning"
                style="color: orange; margin: 0 3px 0 10px"
              ></i
              >Compatible with the minimum device version {{ mindeviceversion }}
            </p>
          </div>
        </div>
        <div class="search-container">
          <div class="search-item">
            <el-select
              v-model="search.device_id"
              @change="filterTable"
              placeholder="Device"
              clearable
            >
              <el-option
                v-for="item in tableData"
                :key="item.device_id"
                :label="item.name"
                :value="item.device_id"
              >
              </el-option>
            </el-select>
          </div>
          <div class="search-item">
            <el-select
              v-model="search.status"
              @change="filterTable"
              placeholder="Status"
              clearable
            >
              <el-option label="Online" :value="1"></el-option>
              <el-option label="Offline" :value="0"></el-option>
            </el-select>
          </div>


          <div class="search-btns" style="margin-right: 12rem">
<!--            <el-button-->
<!--              type="primary"-->
<!--              class="searchbtn"-->
<!--              @click="importAlgVisible = true"-->
<!--              v-if="deviceType == 'normal'"-->
<!--              >Algorithm Deployment</el-button-->
<!--            >-->
<!--            <el-button-->
<!--              type="primary"-->
<!--              class="searchbtn"-->
<!--              @click="sysupgradeVisible = true"-->
<!--              >Device Upgrade</el-button-->
<!--            >-->

            <el-button
              type="primary"
              :loading="sycnLoading"
              class="searchbtn"
              @click="sycnData"
              >Data Synchronization</el-button>
          </div>
        </div>
        <div class="table-container">
          <el-table
              :ref="tableRef"
            v-loading="listLoading"
            element-loading-text="Loading"
            element-loading-spinner="el-icon-loading"
            :data="tableData"
            :border="false"
            @row-click="handleRowSel"
            :row-class-name="activeRow"
          >
            <el-table-column prop="name" label="Device Name">
              <template slot-scope="scope">
                <div style="cursor: pointer">
                  <span>{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="ipv4" label="IP"></el-table-column>
            <el-table-column prop="description" label="Device Description"></el-table-column>
            <el-table-column label="Device Status">
              <template slot-scope="scope">
                <span v-if="scope.row.status == 0">Offline</span>
                <span v-if="scope.row.status == 1">Online</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="hardwareVersion"
              label="Hardware Version"
            ></el-table-column>
            <el-table-column
              prop="softwareVersion"
              label="Software Version"
            ></el-table-column>
            <el-table-column
              label="Ship Name"
              prop="shipName">
            </el-table-column>
            <el-table-column
              label="Bind Ship">
              <template slot-scope="{ row }">
                <span v-if="row.shipId">binding</span>
                <span v-else>unbinding</span>
              </template>
            </el-table-column>
            <el-table-column
              label="Operation"
              v-if="permission == 10 || permission == 20"
              width="120px"
            >
              <template slot-scope="{ row }">
                <el-button v-show="!row.shipId" type="text" @click.stop="bindShip(row)">bindingShip</el-button>
                <el-button v-show="row.shipId" type="text" @click.stop="unbindShipOpt(row)">unbindingShip</el-button>
                <!-- <div class="table-btns" @click.stop="() => {}">
                  <el-tooltip
                    class="item"
                    effect="dark"
                    content="Algorithm repository"
                    placement="top"
                  >
                    <i
                      v-if="deviceType == 'normal'"
                      class="z-icon-cangku"
                      @click.stop="handleRowSel(scope.row);handleEditDevice(scope.row)"
                      style="margin-right: 20px; cursor: pointer"
                    ></i>
                  </el-tooltip>
                  <el-tooltip
                    class="item"
                    effect="dark"
                    content="System resource"
                    placement="top"
                  >
                    <i
                      v-if="deviceType == 'normal'"
                      class="z-icon-xitongziyuan"
                      @click.stop="handleRowSel(scope.row);handleShowResource(scope.row)"
                      style="margin-right: 20px; cursor: pointer"
                    ></i>
                  </el-tooltip>
                  <el-tooltip
                    class="item"
                    effect="dark"
                    content="Download log"
                    placement="top"
                  >
                    <i
                      v-if="!scope.row.downlogloading"
                      class="z-icon-icon-xiazairizhi"
                      @click.stop="handleRowSel(scope.row);handleDownloadLog(scope.row)"
                      style="margin-right: 20px; cursor: pointer"
                    ></i>
                    <i
                      v-if="scope.row.downlogloading"
                      class="z-icon-icon-xiazairizhi disabled"
                      style="
                        margin-right: 20px;
                        cursor: pointer;
                        position: relative;
                      "
                      v-loading="scope.row.downlogloading"
                      element-loading-spinner="el-icon-loading"
                      element-loading-background="rgba(0, 0, 0, 0)"
                    ></i>
                  </el-tooltip>
                  <el-tooltip
                    class="item"
                    effect="dark"
                    content="Reboot"
                    placement="top"
                  >
                    <i
                      class="z-icon-quit"
                      @click.stop="handleRowSel(scope.row);handleRestart(scope.row)"
                      style="margin-right: 20px; cursor: pointer"
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
                      @click.stop="handleDelDevice(scope.row)"
                      style="margin-right: 20px; cursor: pointer"
                    ></i>
                  </el-tooltip>
                </div> -->
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
      <template v-else>
        <div class="device-title">
          <h3>
            <div
              class="device_type"
              :class="{ active: deviceType == 'normal' }"
              @click="
                deviceType = 'normal';
                filterTable();
              "
            >
              Camera
            </div>
            <span class="split"></span>
            <!-- <div
              class="device_type"
              :class="{ active: deviceType == 'forward' }"
              @click="
                deviceType = 'forward';
                filterTable();
              "
            >
              Stream
            </div> -->
          </h3>
          <div class="count">
            <p>Total：{{ tableData.length }}</p>
            <p class="online">
              Online：{{ tableData.filter((item) => item.status == 1).length }}
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
            :row-class-name="activeRow"
            @row-click="handleRowSel"
          >
            <el-table-column label="Device Name">
              <template slot-scope="scope">
                <div style="cursor: pointer">
                  <span>{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </div>
    <div
      class="type-camera"
      :class="{ fold: type == 'device', unfold: type == 'camera' }"
    >
      <div
        class="btn-switch"
        @click="type = type == 'device' ? 'camera' : 'device'"
      >
        <i class="z-icon-arrow-double-left" v-if="type == 'device'"></i>
        <i class="z-icon-arrow-double-right" v-else></i>
      </div>
      <camera :deviceInfo="curdevice" :type="type"></camera>
    </div>

    <el-dialog
      :title="editDeviceInfo && editDeviceInfo.name + 'Algorithm Modify'"
      :append-to-body="true"
      :visible.sync="editDeviceVisible"
      class="edit-algconf-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
    >
      <algList
        :editDeviceInfo="editDeviceInfo"
        v-if="editDeviceVisible"
      ></algList>
      <!-- <deviceEdit
        :editDeviceInfo="editDeviceInfo"
        v-if="editDeviceVisible"
      ></deviceEdit> -->
    </el-dialog>
    <el-dialog
      title="Algorithm Distribution"
      :append-to-body="true"
      :visible.sync="importAlgVisible"
      class="edit-device-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
    >
      <algImport
        v-if="importAlgVisible"
        :devices="tableData"
        @close-importalg="
          importAlgVisible = false;
          getTableData();
        "
      ></algImport>
    </el-dialog>
    <el-dialog
      title="System resource"
      :append-to-body="true"
      :visible.sync="resourceVisible.show"
      class="device-resource-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
    >
      <deviceresource
        v-if="resourceVisible.show"
        :device_id="resourceVisible.device_id"
        @close-resource="resourceVisible.show = false"
      ></deviceresource>
    </el-dialog>
    <el-dialog
      title="Device upgrade"
      :append-to-body="true"
      :visible.sync="sysupgradeVisible"
      class="edit-device-dialog"
      v-dialogDrag
      :close-on-click-modal="false"
    >
      <deviceUpgrade
        v-if="sysupgradeVisible"
        :devices="tableData"
        @close-sysupgrade="
          sysupgradeVisible = false;
          getTableData();
        "
      ></deviceUpgrade>
    </el-dialog>
    <el-dialog
      title="绑定轮船"
      append-to-body
      :visible.sync="captureShow"
      class="capture-dialog"
      v-dialogDrag
      width="500px"
    >
      <el-form :rules="rules" ref="captureFormRef" :model="capture">
        <el-form-item label="Device Name:" label-width="95px">
          <el-input v-model="capture.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="选择轮船:" label-width="95px" prop="shipId">
          <el-select
            v-model="capture.shipId"
            placeholder="请选择"
          >
            <el-option
              v-for="item in shipList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="buttonLoading" @click="save">确 定</el-button>
          <el-button @click="captureShow = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { deviceApis, getmindevversion, deviceV2Apis } from "@/api/device.js";
import { shipApis } from "@/api/ship.js";
import { algApis } from "@/api/alg.js";
import camera from "./camera.vue";
import deviceEdit from "./deviceEdit.vue";
import algList from "./algList.vue";
import algImport from "./algImport.vue";
import deviceUpgrade from "./deviceUpgrade.vue";
import deviceresource from "./deviceresource.vue";
import { projConfig } from "@/utils/config";
import mqttmixin from "@/utils/mqttmixin.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [mqttmixin, langmixin],
  components: {
    camera,
    deviceEdit,
    algList,
    algImport,
    deviceUpgrade,
    deviceresource,
  },
  data() {
    return {
      rules: {
        shipId: [{ required: true, message: "请选择轮船", trigger: "change" }],
      },
      tableRef: null,
      sycnLoading: false,
      buttonLoading: false,
      capture: {},
      shipList: [],
      captureShow: false,
      type: "device",
      deviceType: "normal",
      projConfig: projConfig,
      permission: 10,
      search: {
        device_id: "",
        name: "",
        status: "",
        alg_ch_name: "",
        page: 1,
        size: 10000,
      },
      allAlgs: [],
      listLoading: false,
      total: 0,
      tableData: [],
      tableDataAll: [],
      curdevice: null,
      editDeviceVisible: false,
      editDeviceInfo: null,
      importAlgVisible: false,
      sysupgradeVisible: false,
      resourceVisible: {
        show: false,
        device_id: "",
      },
      mindeviceversion: "",
    };
  },
  beforeMount() {
    getmindevversion().then((res) => {
      if (res.error_code == 0) {
        this.mindeviceversion = res.data;
      } else {
        this.$message({
          type: "error",
          message: res.message[this.lang],
        });
      }
    });
    this.permission = localStorage.getItem("permission");
    this.connectMqtt();
    this.mqttclient.subscribe("ks/proxy/message", { qos: 0 }, (error) => {
      if (error) {
        console.log("subscribe error:", error);
        return;
      }
    });
    this.mqttclient.on("message", (topic, payload) => {
      let msg = JSON.parse(payload.toString());

      if (msg.msg_type == "device_status") {
        for (let i = 0; i < this.tableDataAll.length; i++) {
          if (msg.data.device_id == this.tableDataAll[i].device_id) {
            this.tableDataAll[i].status = msg.data.status;
            break;
          }
        }
        for (let i = 0; i < this.tableData.length; i++) {
          if (msg.data.device_id == this.tableData[i].device_id) {
            this.tableData[i].status = msg.data.status;
            break;
          }
        }
      }
    });
  },
  async mounted() {
    this.getTableData();
    const { rows } = await shipApis.getTableData();
    this.shipList = rows || [];
  },
  methods: {
    async save() {
      try {
        await this.$refs.captureFormRef.validate();
        this.buttonLoading = true;
        let data = await deviceV2Apis.bindShip(this.capture);
        if (data.code === 500) {
          throw new Error(data.msg);
        } else {
          this.getTableData();
          this.captureShow = false;
        }
      } catch (err) {
        this.$message.error(err);
      } finally {
        this.buttonLoading = false;
      }
    },
    unbindShipOpt({ shipId, id }) {
      this.$confirm("是否解绑轮船", "提示", {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: "warning",
      }).then(async () => {
        try {
          let data = await deviceV2Apis.unbindShip({
            shipId, deviceId: id,
          });
          if (data.code === 500) {
            throw new Error(data.msg);
          } else {
            this.getTableData();
            this.$message.success(data.msg);
          }
        } catch (err) {
          this.$message.error(err);
        }
      });
    },
    bindShip(row) {
      this.capture = {
        name: row.name,
        shipId: "",
        deviceId: row.id,
      };
      this.captureShow = true;
    },
    activeRow({ row }) {
      if (row.select) {
        return "active";
      } else {
        return "";
      }
    },
    filterTable() {
      this.tableData = this.tableDataAll
        // .filter((item) => {
        //   let tmp =
        //     this.deviceType == "normal"
        //       ? item.hardware_version.indexOf("forward") < 0
        //       : item.hardware_version.indexOf("forward") >= 0;
        //   return tmp;
        // })
        .filter((item) => {
          let tmp = this.search.device_id
            ? item.device_id.indexOf(this.search.device_id) >= 0
            : true;
          return tmp;
        })
        .filter((item) => {
          let tmp = this.search.alg_ch_name
            ? item.algList.filter(
                (item1) => item1.ch_name == this.search.alg_ch_name
              ).length >= 0
            : true;
          return tmp;
        })
        .filter((item) => {
          if (this.search.status === 0 || this.search.status === 1) {
            return item.status == this.search.status;
          } else {
            return true;
          }
        });
      if (this.tableData.length > 0) {
        this.curdevice = this.tableData[0];
        this.curdevice.select = true;
      } else {
        this.curdevice = null;
      }
    },
    async getTableData() {
      let data = await deviceV2Apis.getTableList();
      this.tableDataAll = data.rows;
      this.filterTable();
      // deviceApis
      //   .getTableData()
      //   .then((deviceList) => {
      //     algApis
      //       .getTableData()
      //       .then((devalgs) => {
      //         let allAlgsObj = {};
      //         for (let deviceid in devalgs) {
      //           for (let i = 0; i < devalgs[deviceid].length; i++) {
      //             if (!allAlgsObj[devalgs[deviceid][i].name]) {
      //               allAlgsObj[devalgs[deviceid][i].name] =
      //                 devalgs[deviceid][i];
      //             }
      //           }
      //         }
      //         for (let alg_name in allAlgsObj) {
      //           this.allAlgs.push(allAlgsObj[alg_name]);
      //         }
      //         this.tableDataAll = deviceList.map((item) => {
      //           item.device_id = item.id;
      //           item.select = false;
      //           item.downlogloading = false;
      //           item.delloading = false;
      //           if (devalgs[item.id]) {
      //             item.algList = Object.values(
      //               JSON.parse(JSON.stringify(devalgs[item.id]))
      //             ).map((alg) => {
      //               alg.key = item.id + "_" + alg.name;
      //               return alg;
      //             });
      //           } else {
      //             item.algList = [];
      //           }
      //           if (item.algList.length > 4) {
      //             item.algsFold = false;
      //           }
      //           item.restarting = false;
      //           return item;
      //         });
      //         // this.tableData = JSON.parse(JSON.stringify(this.tableDataAll));
      //         this.filterTable();
      //       })
      //       .catch((err) => {
      //         if (err && err.message && err.message[this.lang]) {
      //           this.$message({
      //             type: "error",
      //             message: err.message[this.lang],
      //           });
      //         }
      //       });
      //   })
      //   .catch((err) => {
      //     if (err && err.message && err.message[this.lang]) {
      //       this.$message({
      //         type: "error",
      //         message: err.message[this.lang],
      //       });
      //     }
      //   });
    },
    handleDelDevice(row) {
      row.delloading = true;
      this.$confirm("Are you sure to delete device?", "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          deviceApis
            .del({ device_ids: [row.device_id] })
            .then((res) => {
              this.getTableData();
              this.$message({
                type: "success",
                message: "Delete success",
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
        .catch((err) => {
          row.delloading = false;
        });
    },
    handleDownloadLog(row) {
      row.downlogloading = true;
      deviceApis
        .downloadLog({ device_id: row.id })
        .then((res) => {
          row.downlogloading = false;
          this.download(res, 'log');
        })
        .catch((err) => {
          row.downlogloading = false;
          this.$message({
            type: "error",
            message: err.message[this.lang],
          });
        });
    },
    download(blob, filename) {
      var blob = new Blob([blob]);
      var url = URL.createObjectURL(blob);
      var a = document.createElement("a");
      a.href = url;
      a.download = `${filename}.zip`; // 设置下载的文件名
      a.click();
      window.URL.revokeObjectURL(url);
    },
    handleShowResource(row) {
      this.resourceVisible.device_id = row.id;
      this.resourceVisible.show = true;
    },
    handleEditDevice(row) {
      this.editDeviceVisible = true;
      this.editDeviceInfo = JSON.parse(JSON.stringify(row));
    },
    handleRowSel(row) {
      this.curdevice.select = false;
      row.select = true;
      this.curdevice = row;
      this.$forceUpdate();
    },
    handleRemoveAlg(item, row) {
      this.$confirm(`Are you sure to delete ${item.ch_name}?`, "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          algApis
            .del({
              device_ids: [row.device_id],
              name: item.name,
            })
            .then((res) => {
              this.getTableData();
              console.log(res);
              this.$message({
                type: "success",
                message: "Delete success！",
              });
            })
            .catch((err) => {
              this.$message({
                type: "error",
                message: "Delete fail！",
              });
            });
        })
        .catch((err) => {});
    },
    handleRestart(row) {
      this.$confirm(`Are you sure to reboot ${row.name}?`, "Reboot", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          deviceApis
            .restart({
              device_ids: [row.device_id],
            })
            .then((res) => {
              this.$message({
                type: "success",
                message: "Reboot success！",
              });
            })
            .catch((err) => {
              this.$message({
                type: "error",
                message: "Reboot fail！",
              });
            });
        })
        .catch((err) => {});
    },
    sycnData(){
      this.sycnLoading = true;
      deviceApis.syncData().then((res) => {
        this.getTableData();
        this.$message({
          type: "success",
          message: "Synchronize success！",
        });
      }).catch((err) => {
        console.log(err)
        this.$message({
          type: "error",
          message: "Synchronize fail！",
        });
      }).finally(()=>{
        this.sycnLoading = false;
      })
    }
  },
};
</script>

<style lang="less" scoped>
/deep/.operate-column span {
  cursor: pointer;
}
.device-container {
  // display: flex;
  > div {
    height: calc(100% - 12rem);
    position: absolute;
    border-radius: 4px;
    background-color: #fff;
  }
  .type-device {
    left: 8rem;
  }
  .type-camera {
    right: 8rem;
    .btn-switch {
      width: 20rem;
      position: absolute;
      height: 100%;
      left: 0rem;
      top: 0;
      display: flex;
      align-items: center;
      font-size: 14rem;
      background-color: #f3f3f4;
      justify-content: center;
      i {
        cursor: pointer;
      }
    }
  }

  .type-device.unfold {
    width: calc(100% - 300rem - 10rem);
  }
  .type-camera.fold {
    width: 300rem;
  }
  .type-camera.unfold {
    width: calc(100% - 300rem + 20rem - 10rem);
  }
  .type-device.fold {
    width: calc(300rem - 20rem);
  }
}
.device-title {
  width: 100%;
  padding: 8rem 0 0 8rem;
  box-sizing: border-box;
  h3 {
    font-size: 16rem;
    margin-bottom: 5rem;
    // border-left: 3rem solid #ff9900;
    // padding-left: 10rem;
    display: flex;
    align-items: center;
    .device_type {
      border-bottom: 3rem solid transparent;
      padding-bottom: 5rem;
      cursor: pointer;
    }
    .device_type.active {
      border-bottom: 3rem solid orange;
    }
    .split {
      margin: 0 10px;
      width: 1rem;
      height: 20rem;
      background-color: transparent;
      display: block;
    }
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
  /deep/.el-table--enable-row-hover
    .el-table__body
    tr:hover
    > td.el-table__cell {
    background-color: transparent;
  }
  /deep/.alg-cell {
    padding: 0;
    .cell {
      padding: 0;
    }
  }
  .table-btns {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-evenly;
    width: 220rem;
    .el-button {
      margin-bottom: 5rem;
      margin-left: 0;
      width: 88rem;
      /deep/.el-loading-spinner {
        top: 0;
        margin-top: 0;
      }
    }
  }
  .algList {
    max-height: 150px;
    overflow: auto;
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    padding-top: 9rem;
    li {
      position: relative;
      width: auto;
      height: auto;
      line-height: 1.2;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      font-size: 14rem;
      padding: 5rem 10rem 0;
      border: 1px solid #333;
      border-radius: 4px;
      margin-right: 5rem;
      margin-bottom: 9rem;
      p {
        line-height: 1.5;
      }
      .z-icon-close {
        position: absolute;
        display: none;
        top: -9rem;
        right: -9rem;
        width: 18rem;
        height: 18rem;
        font-size: 16rem;
        border-radius: 150rem;
        background-color: #fff;
        border: 1px solid #333;
        cursor: pointer;
      }
    }
    li:hover {
      .z-icon-close {
        display: block;
      }
    }
  }
}

.z-icon-icon-xiazairizhi.disabled::before {
  opacity: 0.3;
}
.z-icon-icon-xiazairizhi /deep/.el-loading-spinner {
  margin-top: -10rem;
  .el-icon-loading {
    font-size: 18rem;
  }
}

.device-resource-dialog,
.edit-algconf-dialog {
  /deep/.el-dialog {
    width: calc(100% - 40rem);
    max-width: 1200px;
  }
}
.edit-algconf-dialog {
  display: flex;
  align-items: center;
  justify-content: center;
  /deep/.el-dialog {
    margin-top: 0 !important;
  }
  /deep/.el-dialog__body {
    padding: 0;
    height: 74vh;
  }
}

.capture-dialog {
  /deep/.el-input--small .el-input__icon {
    line-height: 40rem;
  }
  /deep/.el-form .el-form-item label {
    line-height: 40rem;
  }
}
</style>