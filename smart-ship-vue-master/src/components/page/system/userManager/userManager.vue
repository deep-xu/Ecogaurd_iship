<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="userName">
            <el-input v-model="queryParams.userName" placeholder="Please enter the userName" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="queryParams.email" placeholder="Please enter the Email" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="status">
            <el-input v-model="queryParams.platform" placeholder="Please Select" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="dateValues">
            <el-date-picker
              v-model="dateValues"
              type="daterange"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="Start date"
              end-placeholder="End date">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">Search</el-button>
            <el-button icon="Refresh" @click="resetQuery">Reset</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8" type="flex" justify="end">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">Add</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="deleteList.length === 0" @click="handleDelete()">Delete</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button :loading="buttonLoading" type="warning" plain icon="Download" @click="handleExport">Export</el-button>
          </el-col>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="userData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <!-- <el-table-column label="用户编号" align="center" prop="id" v-if="true" /> -->
        <el-table-column label="User Name" align="center" prop="userName" />
        <el-table-column label="User Nickname" align="center" prop="nickName" />
        <el-table-column label="Dept" align="center" prop="deptName" />
        <el-table-column label="Email Address" align="center" prop="email" />
        <el-table-column label="Status" align="center" prop="status">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="0" inactive-value="1"
              @change="changeStatus(scope.row, scope.$index)">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column label="Create Time" align="center" prop="createTime"></el-table-column>
        <el-table-column label="Options" align="center">
          <template #default="scope">
            <el-tooltip content="Modify" placement="top">
              <el-button v-if="scope.row.userName !== 'admin'" link type="text" @click="handleUpdate(scope.row)">
                <i class="z-icon-edit"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button v-if="scope.row.userName !== 'admin'" link type="text" @click="handleDelete(scope.row.userId)">
                <i class="z-icon-del"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="重置密码" placement="top">
              <el-button v-if="scope.row.userName !== 'admin'" link type="text" @click="resetPwd(scope.row)">
                <i class="el-icon-unlock"></i>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 15, 20, 30, 40, 50]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>
    <el-dialog class="dialogCls" title="Add User" :visible.sync="dialogVisible" width="50%" append-to-body
      @close="cancel">
      <el-form :model="userForm" :rules="rules" ref="form" label-width="auto">
        <el-row>
          <el-col :span="12">
            <el-form-item label="User Name" prop="userName">
              <el-input v-model="userForm.userName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email Address" prop="email">
              <el-input v-model="userForm.email"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="NickName" prop="nickName">
              <el-input v-model="userForm.nickName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!userForm.userId">
            <el-form-item label="User Password" prop="password">
              <el-input type="password" show-password v-model="userForm.password"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Role" prop="roleId">
              <el-select v-model="userForm.roleId" placeholder="Please select role">
                <el-option v-for="item in roleList" :key="item.roleId" :label="item.roleName" :value="item.roleId"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Status" prop="status">
              <el-radio-group v-model="userForm.status">
                <el-radio label="0">Normal</el-radio>
                <el-radio label="1">Deactivated</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Dept" prop="deptId">
              <el-select v-model="userForm.deptId" placeholder="Please select dept">
                <el-option v-for="item in deptList" :key="item.deptId" :label="item.deptName" :value="item.deptId"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-form-item label="Remark" prop="remark" style="max-width: 100%;">
            <el-input type="textarea" v-model="userForm.remark"></el-input>
          </el-form-item>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">Confirm</el-button>
          <el-button @click="cancel">Cancel</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script>
import {userManagerApis} from '@/api/userManager'
import {roleApis} from '@/api/role'
import {deptApis} from "@/api/department";

export default {
    name: 'userManager',
    data () {
      return {
        buttonLoading: false,
        dialogVisible: false,
        userForm: {
          status: '0'
        },
        rules: {
          userName: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
          nickName: [{ required: true, message: 'Please enter nickname', trigger: 'blur' }],
          password: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
          roleId: [{ required: true, message: 'Please select role', trigger: 'change' }],
          deptId: [{ required: true, message: 'Please select dept', trigger: 'change' }],
          email: [{type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change']}],
        },
        dateValues: [],
        deleteList: [],
        total: 0,
        queryParams: {
          pageNum: 1,
          pageSize: 10,
        },
        userData: [],
        loading: false,
        roleList: [],
        deptList:[],
      }
    },
    methods: {
      dealDate() {
        if (this.dateValues[0]) {
          this.queryParams['params[beginTime]'] = this.dateValues[0]
        }
        if (this.dateValues[1]) {
          this.queryParams['params[endTime]'] = this.dateValues[1]
        }
      },
      async handleQuery() {
        this.dealDate();
        try {
          this.loading = true;
          const { total, rows } = await userManagerApis.getTableData(this.queryParams);
          this.total = total || 0;
          this.userData = rows || [];
        } catch (error) {
          this.$message.error(error.message || '获取数据失败');
        } finally {
          this.loading = false;
        }
      },
      resetQuery() {
        this.dateValues = [];
        this.queryParams = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
        }
        this.handleQuery();
      },
      handleAdd() {
        this.dialogVisible = true;
      },
      async handleDelete(userId) {
        if (!userId && !this.deleteList.length) {
          this.$message.warning('请选择要删除的数据');
          return;
        }
        try {
          const res = await userManagerApis.deleteUser(userId || this.deleteList.join(','));
          if (res.code == 200) {
            this.$message.success('删除成功');
            this.handleQuery();
          } else {
            this.$message.error(res.msg || '删除失败');
          }
        } catch (error) {
          this.$message.error(error.message || '删除失败');
        }
      },
      async handleExport () {
        try {
          this.buttonLoading = true;
          const res = await userManagerApis.exportUserData();
          if (res instanceof Blob) {
                var blob = new Blob([res]);
                var url = URL.createObjectURL(blob);
                var a = document.createElement("a");
                a.href = url;
                a.download = `user_${new Date().getTime()}.xlsx`; 
                a.click();
                window.URL.revokeObjectURL(url);
              }
        } catch (error) {}
        finally {
          this.buttonLoading = false;
        }
      },
      resetPwd({ nickName, userId }) {
        this.$prompt(`Please enter the new password for "${nickName}"`, 'Tip', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
        }).then(async ({ value }) => {
          try {
            const res = await userManagerApis.resetPassword({ userId, password: value });
            if (res.code === 200) {
              this.$message.success('密码重置成功');
            } else {
              this.$message.error(res.msg || '密码重置失败');
            }
          } catch (error) {}
        })
      },
      async handleUpdate(row) {
        const data = await userManagerApis.userDetail(row.userId);
        const { roles = [] } = data.user;
        this.dialogVisible = true;
        this.userForm = { ...row, roleId: (roles[0] || {}).roleId || '' };
      },
      handleSelectionChange(val) {
        this.deleteList = val.map(({ userId }) => userId);
      },
      handleSizeChange(val) {
        this.queryParams.pageSize = val;
        this.handleQuery();
      },
      handlePageChange() {
        this.queryParams.pageNum = val;
        this.handleQuery();
      },
      async changeStatus({ status, userId }, index) {
        this.$confirm('确定要修改状态吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(async () => {;
          try {
            const res = await userManagerApis.updateStatus({ userId, status });
            if (res.code == 200) {
              this.$message.success('修改成功');
            } else {
              this.$message.error(res.msg || '修改失败');
              this.userData.splice(index, 1, { ...this.userData[index], status: status == '1' ? '0' : '1' });
            }
          } catch (error) {
            this.$message.error(error.msg || '修改失败');
          }
        })
      },
      async submitForm() {
        try {
          await this.$refs.form.validate();
          this.buttonLoading = true;
          let res = {};
          let msg = this.userForm.userId ? '修改成功' : '添加成功';
          const postData = { ...this.userForm, roleIds: [this.userForm.roleId]}
          if (this.userForm.userId) {
            res = await userManagerApis.updateUser(postData);
          } else {
            res = await userManagerApis.addUser(postData);
          }
          if (res.code == 200) {
            this.$message.success(msg);
            this.dialogVisible = false;
            this.handleQuery();
            this.userForm = { status: '0' }
          } else {
            this.$message.error(res.msg || '操作失败');
          }          
        } catch (e) {
          this.$message.error(e.msg || '操作失败');
        }
        finally {
          this.buttonLoading = false;
        }
      },
      cancel() {
        this.dialogVisible = false;
        this.userForm = { status: '0' };
      },
      async getRoleList() {
        this.roleList = await roleApis.getRoleSelectList();
      },
      async getDeptList() {
        this.deptList = await deptApis.getList();
      }
    },
    mounted() {
      this.handleQuery();
      this.getRoleList();
      this.getDeptList();
    }
  }
</script>
<style lang="less" scoped>
.dialogCls {
  /deep/.el-dialog .el-dialog__body {
    padding: 20rem 40rem;
  }
}
.el-pagination {
  position: relative;
}
.pagination {
  text-align: right;
  margin-top: 20px;
}
</style>