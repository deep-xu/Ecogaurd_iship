<template>
  <div class="wechat-user">
    <div class="search-container">
      <div style="position: absolute; right: -8rem; top: 2rem">
        <el-button title="新增" type="primary" @click="handleAdd"
          >新增</el-button
        >
      </div>
    </div>
    <div class="table-container height1">
      <el-table
        v-loading="listLoading"
        element-loading-text="拼命加载中"
        element-loading-spinner="el-icon-loading"
        :data="tableData"
        :border="false"
        stripe
        @selection-change="handleSelectionChange"
      >
        <!-- <el-table-column type="selection" width="55"> </el-table-column> -->
        <el-table-column prop="user_id" label="用户ID"> </el-table-column>
        <el-table-column prop="user_name" label="用户名"> </el-table-column>
        <el-table-column prop="mobile" label="手机号"> </el-table-column>
        <el-table-column prop="device_names" label="设备"> </el-table-column>
        <el-table-column label="操作" class-name="operate-column" width="150px">
          <template slot-scope="scope">
            <i class="z-icon-edit" @click="handleEdit(scope.row)"></i>
            <i class="z-icon-del" @click="handleDelete(scope.row)"></i>
          </template>
        </el-table-column>
      </el-table>
      <!-- <div class="pagination-model" v-show="listLoading"></div> -->
      <!-- <el-pagination
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
      </el-pagination> -->
    </div>

    <el-dialog
      :title="formData.title"
      :append-to-body="true"
      :visible.sync="formData.show"
      class="alarmDetail-dialog"
      v-dialogDrag
      width="500px"
    >
      <el-form
        :model="formData"
        ref="formData"
        :rules="formRule"
        v-if="formData.show"
      >
        <el-form-item label="用户名" prop="user_name">
          <el-input v-model.trim="formData.user_name"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model.trim="formData.mobile"></el-input>
        </el-form-item>
        <el-form-item label="设备" prop="device">
          <el-select
            v-model.trim="formData.device"
            multiple
            placeholder="请选择"
          >
            <el-option
              v-for="item in devices"
              :key="item.id"
              :label="item.device_name"
              :value="item.device_id"
            >
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item style="margin-top: 30px; text-align: right">
          <el-button @click="formData.show = false">取消</el-button>
          <el-button type="primary" @click="getAccessToken().then(handleSave)"
            >保存</el-button
          >
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { dingtalkUser, dingtalkSetting } from "@/api/dataupload.js";
import { device } from "@/api/index.js";
export default {
  data() {
    return {
      api: dingtalkUser,
      search: {
        page: 1,
        size: 10,
      },
      alarmType: null,
      listLoading: false,
      total: 0,
      tableData: [],
      tableSel: [],
      devices: [],
      devicesObj: {},

      formData: {
        title: "新增推送目标",
        show: false,
        act: "add",
        user_id: "",
        user_name: "",
        mobile: "",
        device: "",

        appkey: "ww50a81e599e02e14c",
        appsecret: "_JH_U346vXIMATXwj4GRy0ajAvJP6kzeKWSp-V-IhHc",
      },
      formRule: {
        user_name: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        mobile: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            message: "请输入正确的手机号",
            trigger: "blur",
            validator: (rule, value, callback) => {
              let str = /^[1][3,5,7,8][0-9]{9}$/;
              if (str.test(value) == false) {
                callback(new Error());
              } else {
                callback();
              }
            },
          },
        ],
      },

      labelWidth: "80px",
    };
  },
  mounted() {
    dingtalkSetting.get().then((res) => {
      let user = JSON.parse(res.user);
      this.formData.appkey = user.appkey;
      this.formData.appsecret = user.appsecret;
    });
    device.getTableData().then((res) => {
      this.getTableData();
      this.devices = [...res];
      this.devices.forEach((item) => {
        this.$set(this.devicesObj, item.device_id, item);
      });
    });
  },
  methods: {
    getTableData() {
      this.api.getTableData().then((res) => {
        this.tableData = [...res].map((item) => {
          item.device = JSON.parse(item.device);
          item.device_names = item.device.map(
            (id) => this.devicesObj[id].device_name
          );
          return item;
        });
      });
    },
    handleSelectionChange(rows) {
      this.tableSel = [...rows];
    },
    handleAdd() {
      this.formData = Object.assign(this.formData, {
        title: "新增推送目标",
        show: true,
        user_name: "",
        mobile: "",
        device: this.devices.map((item) => item.device_id),
        act: "add",
      });
    },
    handleEdit(row) {
      this.formData = Object.assign(this.formData, {
        title: "编辑推送目标",
        show: true,
        id: row.id,
        user_id: row.user_id,
        user_name: row.user_name,
        mobile: row.mobile,
        device: row.device,
        act: "edit",
      });
    },
    handleDelete(row) {
      this.$confirm("是否确认删除此推送目标?", "删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      })
        .then(() => {
          let formData = new FormData();
          formData.append("id", row.id);
          this.api.del(formData).then((res) => {
            this.getTableData();
          });
          // this.getAccessToken().then(() => {
          //   this.api
          //     .delDingtalkUser({
          //       accessToken: this.formData.accessToken,
          //       userid: row.user_id,
          //     })
          //     .then((res) => {
          //       console.log(res);
          //     });
          // });
        })
        .catch(() => {});
    },
    getAccessToken() {
      return new Promise((resolve, reject) => {
        if (!this.formData.accessToken) {
          this.api
            .getAccessToken({
              appkey: this.formData.appkey,
              appsecret: this.formData.appsecret,
            })
            .then((res) => {
              this.formData.accessToken = res.access_token;
              resolve();
            });
        } else {
          resolve();
        }
      });
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          if (this.formData.act == "add") {
            let wechartParams = {
              accessToken: this.formData.accessToken,
              name: this.formData.user_name,
              mobile: this.formData.mobile,
              dept_id_list: "678694179",
              userid: this.formData.mobile,
            };
            this.api.addDingtalkUser(wechartParams).then((res) => {
              if (
                res.errcode == 0 ||
                res.errcode == 40103 ||
                res.data.errcode == 60104
              ) {
                let formData = new FormData();
                formData.append("user_id", this.formData.mobile);
                formData.append("user_name", this.formData.user_name);
                formData.append("mobile", this.formData.mobile);
                formData.append("device", JSON.stringify(this.formData.device));
                this.api.add(formData).then((res) => {
                  this.formData.show = false;
                  this.getTableData();
                  this.$message({
                    type: "success",
                    message: "添加成功",
                  });
                });
              }
            });
          } else {
            let formData = new FormData();
            formData.append("id", this.formData.id);
            formData.append("user_id", this.formData.user_id);
            formData.append("user_name", this.formData.user_name);
            formData.append("mobile", this.formData.mobile);
            formData.append("device", JSON.stringify(this.formData.device));
            this.api.edit(formData).then((res) => {
              this.formData.show = false;
              this.getTableData();
              this.$message({
                type: "success",
                message: "修改成功",
              });
            });
          }
        } else {
          return false;
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
.z-icon-edit,
.z-icon-del {
  cursor: pointer;
}
/deep/.el-select {
  width: 100%;
}
</style>