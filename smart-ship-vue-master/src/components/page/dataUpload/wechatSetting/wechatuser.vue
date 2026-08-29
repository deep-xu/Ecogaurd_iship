<template>
  <div class="dinguser-page">
    <div
      style="
        position: absolute;
        left: 16rem;
        top: 20rem;
        z-index: 20;
        display: flex;
        align-items: center;
      "
    >
      <label style="margin-right: 10rem; font-size: 14rem">{{
        settingData.status ? "启用" : "停用"
      }}</label>
      <el-switch
        v-model="settingData.status"
        @on-change="handleStatusChg"
        active-color="var(--btnprimary)"
        size="small"
      >
      </el-switch>
    </div>
    <div style="position: absolute; left: 110rem; top: 20rem; z-index: 1">
      <el-tooltip content="高级设置" placement="top">
        <span
          class="mouse-on"
          @click="advanceSetting = true"
          style="cursor: pointer"
        >
          <i class="z-icon-system" style="font-size: 20rem"></i>
        </span>
      </el-tooltip>
    </div>
    <div
      class="model"
      v-if="!settingData || !settingData.status"
      style="
        position: absolute;
        width: 100%;
        height: 100%;
        left: 0px;
        top: 0px;
        z-index: 10;
        background-color: rgba(0, 0, 0, 0.3);
      "
    ></div>
    <div class="search-container">
      <div class="search-box">
        <!-- <Input
            clearable
            style="width: 200px"
            v-model="search.name"
            placeholder="请输入姓名"
            @on-change="getTableData"
          />
          <Button icon="ios-search" class="time" @click="getTableData"
            >搜索</Button
          > -->
      </div>

      <div class="btn-box">
        <el-button
          style="margin-right: 10px"
          @click="handleAddUser"
          class="button"
          type="primary"
          icon="md-add"
          >添加推送目标</el-button
        >
      </div>
    </div>
    <div class="table-container height1">
      <el-table
        v-loading="tableLoading"
        element-loading-text="拼命加载中"
        element-loading-spinner="el-icon-loading"
        :data="tableData"
        :border="false"
        stripe
        @selection-change="handleSelectionChange"
      >
        <!-- <el-table-column type="selection" width="55"> </el-table-column> -->
        <el-table-column prop="username" label="用户名"> </el-table-column>
        <el-table-column prop="mobile" label="手机号"> </el-table-column>
        <el-table-column prop="device_names" label="设备"> </el-table-column>
        <el-table-column label="操作" class-name="operate-column" width="150px">
          <template slot-scope="scope">
            <i class="z-icon-edit" @click="handleEdit(scope.row)"></i>
            <i class="z-icon-del" @click="handleDelete(scope.row)"></i>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加、修改人员 -->
    <el-drawer
      :title="formData.act == 'add' ? '新增推送目标' : '编辑推送目标'"
      :before-close="beforeCloseForm"
      :visible.sync="formData.show"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
      :modal="false"
    >
      <el-form
        ref="formData"
        :model="formData"
        :rules="formRule"
        style="padding: 0 20rem"
      >
        <el-form-item label="姓名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入姓名"
          ></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input
            v-model="formData.mobile"
            :disabled="formData.act == 'edit'"
            placeholder="请输入手机号"
          ></el-input>
        </el-form-item>
        <el-form-item style="text-align: right">
          <el-button type="primary" :loading="saveloading" @click="handleSave"
            >保存</el-button
          >
        </el-form-item>
      </el-form>
    </el-drawer>
    <el-dialog
      :visible.sync="advanceSetting"
      title="高级设置"
      :append-to-body="true"
      v-dialogDrag
      class="advance-modal"
    >
      <wechatsetting
        v-if="advanceSetting"
        @close-edit-wechat="advanceSetting = false"
      ></wechatsetting>
    </el-dialog>
  </div>
</template>
  <script>
// import { wechatUser } from "@/api/dataupload.js";
// import { uploadApi } from "@/api/dataupload.js";
import { wechatUser, wechatSetting } from "@/api/dataupload.js";
import wechatsetting from "./settings.vue";
import langmixin from "@/utils/langmixin.js"
export default {
  mixins: [langmixin],
  components: { wechatsetting },
  data() {
    return {
      api: wechatUser,
      tableData: [],
      tableLoading: false,
      saveloading: false,
      search: {
        name: "",
      },
      formData: {
        title: "新增推送目标",
        show: false,
        act: "add",
        username: "",
        mobile: "",
      },
      formRule: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        mobile: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            message: "请输入正确的手机号",
            trigger: "blur",
            validator: (rule, value, callback) => {
              let str = /^[1][3,4,5,6,7,8,9][0-9]{9}$/;
              if (str.test(value) == false) {
                callback(new Error());
              } else {
                callback();
              }
            },
          },
        ],
      },

      settingData: {
        status: false,
      },
      advanceSetting: false,
    };
  },
  mounted() {
    this.getSettingData();
    this.getTableData();
  },
  methods: {
    getSettingData() {
      wechatUser
        .getTableData()
        .then((res) => {
          this.loading = false;
          if (res.data.error_code == 0) {
            res.data.data.forEach((item) => {
              if (item.method == "wechat") {
                item.args = JSON.parse(item.args);
                item.status = item.status ? true : false;
                this.settingData = Object.assign(this.settingData, item);
              }
            });
          } else {
            this.$message({
              type: "error",
              message: "查询微信推送状态失败",
            });
          }
        })
        .catch((err) => {
          this.loading = false;
          this.$message({
            type: "error",
            message: "查询微信推送状态失败",
          });
        });
    },
    getTableData() {
      this.api
        .getTableData()
        .then((res) => {
          this.loading = false;
          if (res.data.error_code == 0) {
            this.tableData = res.data.data ? [...res.data.data] : [];
          } else {
            this.$message({
              type: "error",
              message: "查询微信用户失败",
            });
          }
        })
        .catch((err) => {
          this.loading = false;
          this.$message({
            type: "error",
            message: "查询微信用户失败",
          });
        });
    },
    handleAddUser() {
      this.formData = Object.assign(this.formData, {
        title: "新增推送目标",
        show: true,
        username: "",
        mobile: "",
        act: "add",
      });
    },
    handleEditUser(row, index) {
      this.formData = Object.assign(this.formData, {
        title: "编辑推送目标",
        show: true,
        id: row.id,
        username: row.username,
        mobile: row.mobile,
        act: "edit",
      });
    },
    handleDelUser(row, index) {
      this.$confirm({
        show: true,
        type: "删除",
        msg: "是否确定删除人员？",
        btn: {
          ok: "确定",
          no: "取消",
        },
      }).then(() => {
        this.api
          .del({
            id: row.id,
          })
          .then((res) => {
            if (res.data.error_code != 0) {
              this.$message({
                type: "error",
                message: res.data.message[this.lang],
              });
              return;
            }
            this.getTableData();
            this.$message({
              type: "success",
              message: res.data.message[this.lang],
            });
          })
          .catch((err) => {
            this.$message({
              type: "error",
              message: "删除人员失败",
            });
          });
      });
    },

    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          if (this.formData.act == "add") {
            this.api
              .add({
                id: this.formData.mobile,
                username: this.formData.username,
                mobile: this.formData.mobile,
              })
              .then((res) => {
                if (res.data.error_code == 0) {
                  this.formData.show = false;
                  this.getTableData();
                  this.$message({
                    type: "success",
                    message: res.data.message[this.lang],
                  });
                } else {
                  this.$message({
                    type: "error",
                    message: "添加人员失败",
                  });
                }
              })
              .catch((err) => {
                this.$message({
                  type: "error",
                  message: "添加人员失败",
                });
              });
          } else {
            this.api
              .edit({
                id: this.formData.id,
                username: this.formData.username,
              })
              .then((res) => {
                if (res.data.error_code == 0) {
                  this.formData.show = false;
                  this.getTableData();
                  this.$message({
                    type: "success",
                    message: res.data.message[this.lang],
                  });
                } else {
                  this.$message({
                    type: "error",
                    message: res.data.message[this.lang]
                  });
                }
              })
              .catch((err) => {
                this.$message({
                  type: "error",
                  message: "修改人员失败",
                });
              });
          }
        } else {
          return false;
        }
      });
    },
    beforeCloseForm(done) {
      this.$confirm("离开后您已填写的内容不会保存。", "是否确定离开？", {
        confirmButtonText: "离开",
        cancelButtonText: "取消",
      })
        .then(() => {
          done();
        })
        .catch(() => {});
    },
    handleStatusChg() {
      wechatUser
        .edit({
          id: this.settingData.id,
          method: "wechat",
          args: this.settingData.args,
          status: this.settingData.status ? 1 : 0,
        })
        .then((res) => {
          if (res.data.error_code == 0) {
            this.getSettingData();
          } else {
            this.settingData.status = !this.settingData.status;
            this.$message({
              type: "error",
              message: res.data.message[this.lang],
            });
          }
        })
        .catch((err) => {
          this.settingData.status = !this.settingData.status;
          this.$message({
            type: "error",
            message: "修改人员信息失败",
          });
        });
    },
    handleSelectionChange(rows) {
      this.tableSel = [...rows];
    },
  },
};
</script>
  
<style lang="less" scoped>
.dinguser-page {
  padding: 0 10px 10px;
  box-sizing: border-box;
  height: 100%;
}
.search-container {
  display: flex;
  justify-content: space-between;
  .ivu-input-wrapper {
    margin-right: 10px;
  }
}
.user-table {
  height: calc(100% - 35px);
}
</style>
<style lang="less">
.advance-modal {
  display: flex;
  align-items: center;
  justify-content: center;
  .el-dialog{
    width: calc(100% - 40rem);
    max-width: 1300px;
    margin-top: 0 !important;
  }
}
</style>