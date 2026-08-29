<template>
  <div class="table-page">
    <div class="search-container">
      <div class="search-btns">
        <el-button
          @click="formInit()"
          class="button"
          type="primary"
          icon="md-add"
          >Add User</el-button
        >
      </div>
    </div>
    <div class="table-container">
      <el-table
        v-loading="listLoading"
        element-loading-text="Loading"
        element-loading-spinner="el-icon-loading"
        :data="tableData"
        :border="false"
        stripe
      >
        <el-table-column prop="username" label="User Name"></el-table-column>
        <el-table-column prop="permission" label="Permissions">
          <template slot-scope="scope">
            <span>{{scope.row.permission == 10 ? 'User' : 'Admin'}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="device" label="Device">
          <template slot-scope="scope">
            <ul v-if="scope.row.device">
              <li v-for="id in scope.row.device" :key="id">
                {{(devices[id] && devices[id].name) || id}}
              </li>
            </ul>
          </template>
        </el-table-column>
        <el-table-column
          label="Operate"
          class-name="operate-column"
          width="150px"
          v-if="permission == 20 || permission == 10"
        >
          <template slot-scope="scope">
            <span
              class="mouse-on"
              @click="handleEdit(scope.row)"
              style="padding: 5px 10px; display: inline-block; cursor: pointer"
            >
              <i class="z-iconfont z-icon-edit"></i>
            </span>
            <span
              class="mouse-on"
              v-if="scope.row.username != 'admin'"
              @click="handleDel(scope.row)"
              style="padding: 5px 10px; display: inline-block; cursor: pointer"
            >
              <i class="z-iconfont z-icon-del"></i>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 添加、修改用户权限 -->
    <el-drawer
      :title="formDataInit.act == 'add' ? '新增用户' : '编辑用户'"
      :before-close="beforeCloseForm"
      :visible.sync="formVisible"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
      :modal="false"
    >
      <userEdit
        :formDataInit="formDataInit"
        :devices="devices"
        v-if="formVisible"
        @close-edit-userpermission="
          getDevices();
          getTableData();
          formVisible = false;
        "
      ></userEdit>
    </el-drawer>
  </div>
</template>
    
  <script>
import { user } from "@/api/user.js";
import { deviceApis } from "@/api/device.js";
import userEdit from "./userEdit.vue";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  data() {
    return {
      api: user,
      devices:{},
      formVisible: false,
      formDataInit: {
        type: "add",
      },

      tableStatus: "all",
      listLoading: false,
      tableData: [],
      permission: localStorage.getItem("permission"),
    };
  },
  created() {
    this.getDevices().then((res) => {
      this.getTableData();
    });
  },
  methods: {
    formInit(row) {
      // 传参初始化，添加时都为空，编辑时为列表行内容
      if (row) {
        this.formVisible = true;
        this.formDataInit = Object.assign(
          {
            type: "edit",
          },
          row
        );
      } else {
        this.formVisible = true;
        this.formDataInit = {
          type: "add",
        };
      }
    },
    getDevices() {
      return new Promise((resolve, reject) => {
        deviceApis.getTableData().then((res) => {
          for(let i = 0; i < res.length; i++){
            this.devices[res[i].id] = {
              name: res[i].name,
              status: res[i].status
            };
          }
          resolve();
        });
      });
    },
    getTableData() {
      this.listLoading = true;
      this.api
        .getTableData()
        .then((res) => {
          this.listLoading = false;
          this.tableData = res;
          let devices = this.tableData.map(item => item.device).filter(item => item && item.length > 0)
          devices.forEach(deviceArr => {
            deviceArr.forEach(devId => {
              if(!this.devices[devId]){
                this.devices[devId] = null
              }
            })
          })
        })
        .catch((err) => {
          this.listLoading = false;
          this.$message({
            type: "error",
            message: "获取账号列表失败",
          });
        });
    },
    handleEdit(row) {
      this.formVisible = true;
      this.formInit(row);
    },
    handleDel(row) {
      this.$confirm("Are you sure to delete", "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          this.api
            .del({ username: row.username })
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
        })
        .catch((err) => {});
    },
    beforeCloseForm(done) {
      this.$confirm("The information you have filled in after leaving will not be saved.", "Are you sure you want to leave？", {
        confirmButtonText: "Leave",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          done();
        })
        .catch(() => {});
    },
  },
  components: {
    userEdit,
  },
};
</script>
    
<style lang="less" scoped>
.search-btns {
  position: absolute;
  right: 10rem;
}
</style>