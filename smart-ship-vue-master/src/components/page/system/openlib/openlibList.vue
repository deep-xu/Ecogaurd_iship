<template>
  <div class="openlib">
    <div class="search-container">
      <div class="search-item">
        <el-select
          v-model="search.device_id"
          placeholder="Device"
          @change="search.page = 1;getGroupList()"
        >
          <el-option
            v-for="(item, deviceid) in devices"
            :key="deviceid"
            :label="item.name"
            :value="item.device_id"
          >
          </el-option>
        </el-select>
      </div>
      <div class="search-item">
        <el-select
          v-model="search.group_id"
          placeholder="Group"
          @change="search.page = 1;getTableData()"
        >
          <el-option
            v-for="item in groupList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
          </el-option>
        </el-select>
      </div>
      <div class="search-item">
        <el-input
          v-model="search.name"
          clearable
          placeholder="Name"
          @input="search.page = 1;getTableData()"
        ></el-input>
      </div>
      <div class="search-item times">
        <el-date-picker
          v-model="search.datetimerange"
          type="daterange"
          range-separator="-"
          start-placeholder="Start Day"
          end-placeholder="End Day"
          format="yyyy-MM-dd"
          clearable
          @change="search.page = 1;getTableData()"
        >
        </el-date-picker>
      </div>
      <div
        class="searach-item"
        style="display: flex; align-items: center; font-size: 14rem"
      >
        <i
          class="z-icon-warning1"
          style="color: #ff9900; margin-right: 2px"
        ></i>
        Note: After the operation is completed, please manually click to load the algorithm
      </div>
    </div>
    <div class="btns-container" v-if="search.device_id">
      <template v-if="search.group_id">
        <el-button
          title="Load Algorithm"
          type="primary"
          @click="handOpenlibLoad"
          v-if="permission == 20 || permission == 10"
          v-loading="loadLibing"
          :disabled="loadLibing"
          element-loading-spinner="el-icon-loading"
          >Load Algorithm</el-button
        >
        <el-button
          title="Batch Import/Export"
          type="primary"
          @click="showImport = true"
          v-if="permission == 20 || permission == 10"
          >Batch Import/Export</el-button
        >
        <el-button
          title="Add"
          type="primary"
          @click="formInit()"
          v-if="permission == 20 || permission == 10"
          >Add</el-button
        >
      </template>
      <div style="margin: 0 8rem" @click="showGroupSetting = true">
        <el-tooltip placement="top">
          <div style="margin-left: 2px; cursor: pointer">
            <i class="z-icon-system" style="cursor: pointer"></i>
          </div>

          <div slot="content">Group Management</div>
        </el-tooltip>
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
      >
        <!-- <el-table-column
          type="selection"
          width="55"
          v-if="permission == 20 || permission == 10"
        >
        </el-table-column> -->
        <el-table-column prop="device" label="Device"> </el-table-column>
        <el-table-column prop="external_id" label="External ID"> </el-table-column>
        <el-table-column prop="name" label="Name"> </el-table-column>
        <el-table-column label="Picture">
          <template slot-scope="scope">
            <div
              v-if="groupInfo.ext && groupInfo.ext.roi"
              @click="handleShowImg(scope.row)"
              style="width: 100px; height: 80px; margin: 0 auto"
            >
              <paintPic
                v-if="scope.row.image"
                :src="scope.row.pic + '?t=' + new Date().getTime()"
                :roi="groupInfo.ext.roi"
              ></paintPic>
            </div>
            <img
              v-else
              :src="scope.row.pic"
              @click="handleShowImg(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="desc" label="Description"></el-table-column>
        <el-table-column label="Update Time">
          <template slot-scope="scope">
            {{
              new Date(scope.row.update_time * 1000).format(
                "YYYY-MM-DD HH:mm:ss"
              )
            }}
          </template>
        </el-table-column>
        <el-table-column label="Operate">
          <template slot-scope="scope">
            <span
              @click="formInit(scope.row)"
              v-if="permission == 20 || permission == 10"
              ><i
                class="z-icon-edit"
                style="margin-right: 20rem; font-size: 18rem"
              ></i
            ></span>
            <span
              @click="handleDel(scope.row)"
              v-if="permission == 20 || permission == 10"
              ><i class="z-icon-del" style="font-size: 18rem"></i
            ></span>
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

    <el-drawer
      :title="formDataInit.act == 'add' ? '添加' : '编辑'"
      :before-close="beforeCloseForm"
      :visible.sync="formVisible"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
      :modal="false"
    >
      <addOpenlib
        v-if="formVisible"
        :formDataInit="formDataInit"
        :group_id="search.group_id"
        @close-add-openlib="
          getTableData();
          formVisible = false;
        "
      ></addOpenlib>
    </el-drawer>
    <el-drawer
      title="Batch Export/Import"
      :before-close="beforeCloseForm"
      :visible.sync="showImport"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
      :modal="false"
    >
      <importOpenlib
        v-if="showImport"
        :deviceid="search.device_id"
        :group_id="search.group_id"
        @close-importOpenlib="
          getTableData();
          showImport = false;
        "
        @update-openlib-table="getTableData()"
      ></importOpenlib>
    </el-drawer>
    <el-dialog
      title="Group Management"
      :append-to-body="true"
      :visible.sync="showGroupSetting"
      class="openlib-groupsetting-dialog"
      v-dialogDrag
    >
      <openlibGroupSetting
        v-if="showGroupSetting"
        :device_id="search.device_id"
        :group_id="search.group_id"
        :groupList="groupList"
        @update-open_lib-group="getGroupList"
        @close-open_lib-group="
          getGroupList();
          showGroupSetting = false;
        "
      ></openlibGroupSetting>
    </el-dialog>

    <el-dialog
      title="Picture"
      :append-to-body="true"
      :visible.sync="imageDetail.show"
      class="pic-show-dialog"
      v-dialogDrag
    >
      <div class="content_box">
        <div
          v-if="groupInfo.ext && groupInfo.ext.roi"
          style="width: 960px; height: 640px; margin: 0 auto"
        >
          <paintPic :src="imageDetail.pic" :roi="groupInfo.ext.roi"></paintPic>
        </div>
        <img v-else :src="imageDetail.pic" style="margin:0 auto;display:block;max-height: 60vh;max-width:100%;" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { deviceApis } from "@/api/device.js";
import { openlibApis, group } from "@/api/libs.js";
import addOpenlib from "./addOpenlib.vue";
import importOpenlib from "./importOpenlib.vue";
import openlibGroupSetting from "./openlibGroupSetting.vue";
import paintPic from "./paintpic.vue";
import { projConfig } from "@/utils/config.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  components: { addOpenlib, importOpenlib, openlibGroupSetting, paintPic },
  data() {
    return {
      projConfig: projConfig,
      search: {
        device_id: "",
        group_id: "",
        name: "",
        datetimerange: null,
        start_time: "",
        end_time: "",
        page: 1,
        size: 10,
      },
      devices: {},
      groupList: [],
      groupInfo: {},
      listLoading: false,
      tableData: [],
      total: 0,
      formVisible: false,
      formDataInit: {
        optype: "add",
      },
      showImport: false,
      permission: localStorage.getItem("permission"),
      loadLibing: false,
      advanceSetting: false,
      showGroupSetting: false,
      imageDetail: {
        pic: "",
        show: false,
      },
    };
  },
  mounted() {
    deviceApis.getTableData().then((res) => {
      this.devices = {};
      let deviceList = res.filter((item) => item.status == 1 && (item.hardware_version.indexOf('forward') < 0));
      for (let i = 0; i < deviceList.length; i++) {
        deviceList[i].device_id = deviceList[i].id;
        deviceList[i].name = deviceList[i].name || deviceList[i].id;
        this.devices[deviceList[i].id] = deviceList[i];
      }
      if (deviceList[0]) {
        this.search.device_id = deviceList[0].id;
        this.getGroupList();
      }
    });
  },
  methods: {
    formInit(row) {
      if (!this.search.device_id) {
        this.$message({
          type: "success",
          message: "Choose Device",
        });
        return;
      }
      if (row) {
        this.formDataInit = Object.assign(
          {
            optype: "edit",
            device_id: this.search.device_id,
          },
          row
        );
        this.formVisible = true;
      } else {
        if (this.total >= 1000) {
          this.$message({
            type: "error",
            message: "The algorithm of this device has reached the maximum limit！",
          });
          return;
        }
        this.formDataInit = {
          optype: "add",
          device_id: this.search.device_id,
        };
        this.formVisible = true;
      }
    },
    handleGroupChg() {
      this.groupInfo = this.groupList.find(
        (item) => item.id == this.search.group_id
      );
      this.getTableData();
    },
    getGroupList() {
      this.groupList = [];
      group
        .getGroupList({ alg: "open_lib", device_id: this.search.device_id })
        .then((res) => {
          this.groupList = JSON.parse(JSON.stringify(res)).map((item) => {
            item.ext = item.ext ? JSON.parse(item.ext) : {};
            return item;
          });
          if (this.groupList.length > 0) {
            this.search.group_id = this.groupList[0].id;
            this.groupInfo = this.groupList[0];
            this.getTableData();
          } else {
            this.search.group_id = "";
            this.tableData = [];
          }
        });
    },
    getTableData() {
      let params = {
        page: this.search.page,
        size: this.search.size,
        device_id: this.search.device_id,
        group_id: this.search.group_id,
      };
      if (this.search.name) {
        params.name = this.search.name;
      }
      if (this.search.datetimerange) {
        let dates = this.search.datetimerange;
        params.start_time = Date.parse(dates[0]) / 1000;
        params.end_time = Date.parse(dates[1]) / 1000 + 24 * 60 * 60;
      }
      openlibApis.getTableData(params).then((res) => {
        this.tableData = res.data.map((item) => {
          item.device =
            this.devices[item.device_id].name ||
            this.devices[item.device_id].id;
          if (item.image) {
            item.pic = this.projConfig.picprev + item.image;
          }
          return item;
        });
        this.total = res.total;
      });
    },
    handleDel(row) {
      this.$confirm("Are you sure to delete?", "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          openlibApis
            .del({
              ids: [row.id],
              device_ids: [row.device_id],
            })
            .then((res) => {
              console.log("del openlib", res);
              this.getTableData();
              this.$message({
                type: "success",
                message: "Delete success！",
              });
            })
            .catch((err) => {
              console.log("del openlib err", res);
              this.$message({
                type: "error",
                message: err.message[this.lang],
              });
            });
        })
        .catch((err) => {});
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
    beforeCloseForm() {
      this.$confirm("The information you have filled in after leaving will not be saved.", "Are you sure you want to leave？", {
        confirmButtonText: "Leave？",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          this.formVisible = false;
          this.advanceSetting = false;
          this.showImport = false;
          done();
        })
        .catch(() => {});
    },
    handOpenlibLoad() {
      if (!this.search.device_id) {
        this.$message({
          type: "error",
          message: "Please choose one device",
        });
        return;
      }
      this.loadLibing = true;
      openlibApis
        .load({
          device_ids: [this.search.device_id],
        })
        .then((res) => {
          this.loadLibing = false;
          this.$message({
            type: "success",
            message: "Successfully loaded algorithm！",
          });
        })
        .catch((err) => {
          this.loadLibing = false;
          if (err.message) {
            this.$message({
              type: "error",
              message: "Failed to load algorithm！",
            });
          }
        });
    },
    handleShowImg(row) {
      this.imageDetail.pic = row.pic;
      this.imageDetail.show = true;
    },
  },
};
</script>

<style lang="less" scoped>
.btns-container {
  display: flex;
  align-items: center;
  height: 38rem;
  /deep/.el-loading-spinner {
    top: 0;
    margin-top: 0;
  }
}
.z-icon-edit,
.z-icon-del {
  cursor: pointer;
}
</style>
<style lang="less">
.openlib-groupsetting-dialog {
  .el-dialog {
    width: 850px;
  }
}
</style>