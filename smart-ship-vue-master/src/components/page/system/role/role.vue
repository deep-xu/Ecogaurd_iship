<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="roleName">
            <el-input v-model="queryParams.roleName" placeholder="Please enter roleName" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="roleKey">
            <el-input v-model="queryParams.roleKey" placeholder="Please enter roleKey" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="status">
            <el-input v-model="queryParams.platform" placeholder="Please select" clearable @keyup.enter="handleQuery" />
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

      <el-table v-loading="loading" :data="roleData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="Role Name" align="center" prop="roleName" />
        <el-table-column label="Permission Characters" align="center" prop="roleKey" />
        <el-table-column label="Order" align="center" prop="roleSort" />
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
          <template #default="scope" >
            <el-tooltip content="Modify" placement="top">
              <el-button v-show="scope.row.roleKey !== 'superadmin'" link type="text" @click="handleUpdate(scope.row)">
                <i class="z-icon-edit"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button v-show="scope.row.roleKey !== 'superadmin'" link type="text" @click="handleDelete(scope.row.roleId)">
                <i class="z-icon-del"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Assigning Users" placement="top">
              <el-button v-show="scope.row.roleKey !== 'superadmin'" link type="text" @click="handleAssign(scope.row.roleId)">
                <i class="el-icon-price-tag"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Data permissions" placement="top">
              <el-button v-show="scope.row.roleKey !== 'superadmin'" link type="text" @click="handleDataScope(scope.row.roleId)">
                <i class="el-icon-s-tools"></i>
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
    <el-dialog class="dialogCls" title="Add Role" :visible.sync="dialogVisible" width="35%" append-to-body
      @close="cancel">
      <el-form ref="roleFormRef" :model="roleForm" :rules="rules" label-width="130px">
        <el-form-item label="Role Name" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="Please enter the role name" />
        </el-form-item>
        <el-form-item prop="roleKey" label="Permission Chars">
          <el-input v-model="roleForm.roleKey" placeholder="Please enter the permission character" />
        </el-form-item>
        <el-form-item label="Order" prop="roleSort">
          <el-input-number v-model="roleForm.roleSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="roleForm.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Menu Permission">
          <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">Expand/Collapse</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">Select All/Select None</el-checkbox>
          <el-checkbox v-model="roleForm.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">Father son linkage</el-checkbox>
          <el-tree
            ref="menuRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!roleForm.menuCheckStrictly"
            empty-text="Loading..."
            :default-checked-keys="checkedMenus"
            :props="{ label: 'label', children: 'children' }"
          ></el-tree>
        </el-form-item>
        <el-form-item label="Remark">
          <el-input v-model="roleForm.remark" type="textarea" placeholder="Please enter a remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">Confirm</el-button>
          <el-button @click="cancel">Cancel</el-button>
        </div>
      </template>
    </el-dialog>
    <el-drawer
      append-to-body
      size="60%"
      title="Assigning Users"
      :visible.sync="drawer"
      direction="rtl"
      :before-close="handleClose">
      <div class="search">
        <el-form ref="drawerFormRef" :model="drawerParams" :inline="true">
          <el-form-item prop="userName">
            <el-input v-model="drawerParams.userName"
              placeholder="Please enter a user name" clearable @keyup.enter="handleDrawerQuery" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="drawerParams.email"
              placeholder="Please enter the email" clearable @keyup.enter="handleDrawerQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleDrawerQuery">Search</el-button>
            <el-button icon="Refresh" @click="resetDrawerQuery">Reset</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-card shadow="never">
        <template #header>
          <el-row :gutter="10" type="flex" justify="end">
            <el-col :span="1.5">
              <el-button type="primary" plain  @click="openSelectUser">Select Users</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="danger" plain :disabled="selectList.length === 0"  @click="cancelAuthUser()">
                Batch Cancel Authorization
              </el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="warning" plain icon="Close" @click="handleClose">Close</el-button>
            </el-col>
          </el-row>
        </template>
        <el-table v-loading="loading" :data="userList"
          @selection-change="(val) => { selectList = val.map(item => item.userId) }">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="User Name" prop="userName" :show-overflow-tooltip="true" />
          <el-table-column label="User Nickname" prop="nickName" :show-overflow-tooltip="true" />
          <el-table-column label="Email" prop="email" :show-overflow-tooltip="true" />
          <el-table-column label="Status" align="center" prop="status">
            <template #default="scope">
              <el-tag mini effect="dark">
                {{ scope.row.status == '1' ? 'Deactivated' : 'Normal' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Create Time" align="center" prop="createTime" width="180">
            <template #default="scope">
              <span>{{ scope.row.createTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Options" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="Cancel Auth" placement="top">
                <el-button link type="text" icon="CircleClose" @click="cancelAuthUser(scope.row.userId)">Cancel Auth</el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            background
            @size-change="(val) => { queryParams.pageSize = val; handleDrawerQuery() }"
            @current-change="(val) => { queryParams.pageNum = val; handleDrawerQuery() }"
            :current-page="queryParams.pageNum"
            :page-sizes="[10, 15, 20, 30, 40, 50]"
            :page-size="queryParams.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total1"
          />
        </div>
      </el-card>
      <el-dialog title="Select User" :visible.sync="userDialogVisible" width="50%" append-to-body>
        <div class="search">
          <el-form ref="userDialogFormRef" :model="userParams" :inline="true">
            <el-form-item prop="userName">
              <el-input v-model="userParams.userName"
                placeholder="Please enter a user name" clearable @keyup.enter="handleUserQuery" />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="userParams.email"
                placeholder="Please enter the email" clearable @keyup.enter="handleUserQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleUserQuery">Search</el-button>
              <el-button icon="Refresh" @click="resetUserQuery">Reset</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table v-loading="loading" :data="unAuthUserList"
          @selection-change="(val) => { selectedUserList = val.map((item) => item.userId) }">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="User Name" prop="userName" :show-overflow-tooltip="true" />
          <el-table-column label="User Nickname" prop="nickName" :show-overflow-tooltip="true" />
          <el-table-column label="Email" prop="email" :show-overflow-tooltip="true" />
          <el-table-column label="Status" align="center" prop="status">
            <template #default="scope">
              <el-tag mini effect="dark">
                {{ scope.row.status == '1' ? 'Deactivated' : 'Normal' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Create Time" align="center" prop="createTime" width="180">
            <template #default="scope">
              <span>{{ scope.row.createTime }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            background
            @size-change="(val) => { userParams.pageSize = val; handleUserQuery() }"
            @current-change="(val) => { userParams.pageNum = val; handleUserQuery() }"
            :current-page="userParams.pageNum"
            :page-sizes="[10, 15, 20, 30, 40, 50]"
            :page-size="userParams.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total2"
          />
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button :disabled="selectedUserList.length === 0"  :loading="buttonLoading" type="primary" @click="authUserRole">Confirm</el-button>
            <el-button @click="userDialogVisible = false">Cancel</el-button>
          </div>
        </template>
      </el-dialog>
    </el-drawer>
    <!-- 分配角色数据权限对话框 -->
    <el-dialog :visible.sync="permissionVisible" title="Assign data permissions" width="500px" append-to-body @close="cancelDataScope">
      <el-form ref="dataScopeRef" :model="dataScope" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="dataScope.roleName" :disabled="true" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="dataScope.roleKey" :disabled="true" />
        </el-form-item>
        <el-form-item label="权限范围">
          <el-select v-model="dataScope.dataScope" @change="dataScopeSelectChange">
            <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-show="dataScope.dataScope === '2'" label="数据权限">
          <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠</el-checkbox>
          <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选</el-checkbox>
          <el-checkbox v-model="dataScope.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">父子联动</el-checkbox>
          <el-tree
            ref="deptRef"
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            node-key="id"
            :check-strictly="!dataScope.deptCheckStrictly"
            empty-text="加载中，请稍候"
            :props="{ label: 'label', children: 'children' }"
          ></el-tree>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitDataScope">确 定</el-button>
          <el-button @click="cancelDataScope">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script>
import { roleApis } from '@/api/role.js'
import { userManagerApis } from '@/api/userManager.js'
  export default {
    data() {
      return {
        menuExpand: false,
        menuNodeAll: false,
        deptExpand: true,
        deptNodeAll: false,
        sys_normal_disable: [{label: "Normal", value: "0"}, {label: "Deactivated", value: "1"}],
        menuOptions: [],
        buttonLoading: false,
        dialogVisible: false,
        roleForm: {
          status: '0',
          menuCheckStrictly: true
        },
        rules: {
          roleName: [
            { required: true, message: 'Please enter role name', trigger: 'blur' }
          ],
          roleKey: [
            { required: true, message: 'Please enter role key', trigger: 'blur' }
          ],
          roleSort: [
            { required: true, message: 'Please enter order', trigger: 'blur' }
          ]
        },
        checkedMenus: [],
        dateValues: [],
        deleteList: [],
        total: 0,
        queryParams: {
          pageNum: 1,
          pageSize: 10,
        },
        roleData: [],
        loading: false,
        // 分配用户
        drawer: false,
        drawerParams: {
          pageNum: 1,
          pageSize: 10,
        },
        userList: [],
        total1: 0,
        selectList: [],
        // user
        userDialogVisible: false,
        unAuthUserList: [],
        userParams: {
          pageNum: 1,
          pageSize: 10,
        },
        selectedUserList: [],
        total2: 0,
        permissionVisible: false,
        dataScopeOptions: [
          { value: '1', label: '全部数据权限' },
          { value: '2', label: '自定数据权限' },
          { value: '3', label: '本部门数据权限' },
          { value: '4', label: '本部门及以下数据权限' },
          { value: '5', label: '仅本人数据权限' }
        ],
        dataScope: {
          deptCheckStrictly: true
        },
        deptOptions: []
      }
    },
    methods: {
      async authUserRole() {
        try {
          this.buttonLoading = true;
          const res = await roleApis.authUser(this.userParams.roleId, this.selectedUserList.join(',') )
          if (res.code == 200) {
            this.$message.success(res.msg);
            this.userDialogVisible = false;
            this.handleDrawerQuery();
          }
        } finally {
          this.buttonLoading = false;
        }
      },
      resetUserQuery() {
        this.userParams = {
          pageNum: this.userParams.pageNum,
          pageSize: this.userParams.pageSize,
          roleId: this.userParams.roleId
        }
        this.handleUserQuery();
      },
      async handleUserQuery() {
        try {
          this.loading = true;
          const { total = 0, rows = [] } = await userManagerApis.notAuthRoleUser(this.userParams);
          this.unAuthUserList = rows;
          this.total2 = total;
        } finally { 
          this.loading = false;
        }
      },
      handleClose() {
        this.drawer = false;
        this.drawerParams = {
          pageNum: 1,
          pageSize: 10,
        }
      },
      resetDrawerQuery() {
        this.drawerParams = {
          pageNum: this.drawerParams.pageNum,
          pageSize: this.drawerParams.pageSize,
          roleId: this.drawerParams.roleId
        }
        this.handleDrawerQuery();
      },
      openSelectUser() {
        this.userDialogVisible = true;
        this.userParams = {
          ...this.userParams, roleId: this.drawerParams.roleId
        }
        this.handleUserQuery();
      }, 
      cancelAuthUser(userId) {
        this.$confirm('确定取消授权吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            const res = userId ? 
            await roleApis.cancelAuthUser({ userId, roleId: this.drawerParams.roleId })
              : await roleApis.batchCancelAuthUser(this.drawerParams.roleId, this.selectList.join(','));
            if (res.code == 200) {
              this.$message.success(res.msg);
              this.handleDrawerQuery();
            }
          } catch (error) {
            this.$message.error(error.msg || '取消授权失败');
          }
        })
      },
      async handleDrawerQuery() {
        try {
          this.loading = true;
          const { total = 0, rows = [] } = await userManagerApis.authRoleUsers(this.drawerParams);
          this.userList = rows;
          this.total1 = total;
        } finally { 
          this.loading = false;
        }
      },
      async handleDataScope(roleId) {
        const role = await roleApis.getRoleDetail(roleId);
        this.dataScope = role;
        this.permissionVisible = true;
        const data = await roleApis.getRoleDeptTree(roleId);
        this.deptOptions = data.depts;
        await this.$nextTick(() => {
          this.$refs.deptRef.setCheckedKeys(data.checkedKeys);
        });
      },
      async handleCheckedTreeExpand(value, type) {
        if (type == 'menu') {
          let treeList = this.menuOptions;
          for (let i = 0; i < treeList.length; i++) {
            if (this.$refs.menuRef) {
              this.$refs.menuRef.store.nodesMap[treeList[i].id].expanded = value;
            }
          }
        } else if (type == 'dept') {
          let treeList = this.deptOptions;
          for (let i = 0; i < treeList.length; i++) {
            if (this.$refs.deptRef) {
              this.$refs.deptRef.store.nodesMap[treeList[i].id].expanded = value;
            }
          }
        }
      },
      dataScopeSelectChange(value) {
        if (value !== '2') {
          this.$refs.deptRef.setCheckedKeys([]);
        }
      },
      handleCheckedTreeNodeAll(value, type) {
        if (type == 'menu') {
          this.$refs.menuRef.setCheckedNodes(value || []);
        } else if (type == 'dept') {
          this.$refs.deptRef.setCheckedNodes(value || []);
        }
      },
      /** 树权限（父子联动） */
      handleCheckedTreeConnect(value, type) {
        if (type == 'menu') {
          this.roleForm.menuCheckStrictly = value;
        } else if (type == 'dept') {
          this.dataScope.deptCheckStrictly = value;
        }
      },
      async getMenuTreeselect() {
        const res = await roleApis.treeSelect()
        this.menuOptions = res;
      },
      async handleQuery() {
        try {
          this.loading = true;
          let { total, rows } = await roleApis.getTableData(this.queryParams);
          this.total = total;
          this.roleData = rows;
        } catch (e) { }
        finally {
          this.loading = false;
        }
      },
      resetQuery() {
        this.queryParams = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
        }
        this.handleQuery();
      },
      handleAssign(roleId) {
        this.drawerParams = { ...this.drawerParams, roleId }
        this.drawer = true;
        this.handleDrawerQuery();
      },
      async handleAdd() {
        await this.getMenuTreeselect();
        this.dialogVisible = true;
      },
      handleDelete(id) {
        if (!id && this.deleteList.length === 0) {
          this.$message.warning('请选择要删除的数据');
          return;
        }
        this.$confirm('确定删除选中的数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(async () => {
          try {
            const res = await roleApis.deleteRole(id || this.deleteList.join(','));
            if (res.code == 200) {
              this.$message.success('删除成功');
              this.handleQuery();
            } else {
              this.$message.error(res.msg || '删除失败');
            }
          } catch (error) {
            this.$message.error(error.message || '删除失败');
          }
        })
      },
      async handleExport() {
        try {
          this.buttonLoading = true;
          const res = await roleApis.roleExport();
          if (res instanceof Blob) {
            var blob = new Blob([res]);
            var url = URL.createObjectURL(blob);
            var a = document.createElement("a");
            a.href = url;
            a.download = `role_${new Date().getTime()}.xlsx`; 
            a.click();
            window.URL.revokeObjectURL(url);
          }
        } catch (error) {}
        finally {
          this.buttonLoading = false;
        }
      },
      handleSelectionChange(val) {
        this.deleteList = val.map(({ roleId }) => roleId);
      },
      async handleUpdate(row) {
        const data = await roleApis.getSelectedTree(row.roleId)
        await this.getMenuTreeselect();
        this.checkedMenus = data.checkedKeys || []
        this.dialogVisible = true;
        this.roleForm = { ...row };
      },
      handleSizeChange(val) {
        this.queryParams.pageSize = val;
        this.handleQuery();
      },
      handlePageChange() {
        this.queryParams.pageNum = val;
        this.handleQuery();
      },
      getMenuAllCheckedKeys() {
        // 目前被选中的菜单节点
        let checkedKeys = this.$refs.menuRef.getCheckedKeys();
        // 半选中的菜单节点
        let halfCheckedKeys = this.$refs.menuRef.getHalfCheckedKeys();
        if (halfCheckedKeys) {
          checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
        }
        return checkedKeys;
      },
      async submitForm() {
        this.$refs.roleFormRef.validate().then(async (valid) => {
          if (valid) {
            try {
              this.roleForm = { ...this.roleForm, menuIds: this.getMenuAllCheckedKeys() };
              this.buttonLoading = true;
              let res = {};
              let msg = this.roleForm.roleId ? '修改成功' : '添加成功';
              if (this.roleForm.roleId) {
                res = await roleApis.updateRole(this.roleForm);
              } else {
                res = await roleApis.addRole(this.roleForm);
              }
              if (res.code == 200) {
                this.$message.success(msg);
                this.dialogVisible = false;
                this.handleQuery();
                this.roleForm = { status: '0' }
              } else {
                this.$message.error(res.msg || '操作失败');
              }          
            } catch (e) {
              this.$message.error(e.msg || '操作失败');
            }
            finally {
              this.buttonLoading = false;
            }
          }
        });
      },
      cancel() {
        this.dialogVisible = false;
        this.roleForm = {
          status: '0'
        }
      },
      async submitDataScope() {
        if (this.dataScope.roleId) {
          this.dataScope.deptIds = this.getDeptAllCheckedKeys();
          try {
            const res = await roleApis.dataScope(this.dataScope);
            if (res.code == 200) {
              this.$message.success(res.msg);
              this.permissionVisible = false;
              this.handleQuery();
            } else {
              this.$message.error(res.msg || '操作失败');
            }  
          } catch (e) {
            this.$message.error(e.msg || '操作失败');
          }
        }
      },
      getDeptAllCheckedKeys() {
        // 目前被选中的部门节点
        let checkedKeys = this.$refs.deptRef.getCheckedKeys() || [];
        // 半选中的部门节点
        let halfCheckedKeys = this.$refs.deptRef.getHalfCheckedKeys() || [];
        if (halfCheckedKeys) {
          checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
        }
        return checkedKeys;
      },
      cancelDataScope() {
        this.$refs.dataScopeRef.resetFields();
        this.dataScope = {};
        this.permissionVisible = false;
      },
      async changeStatus({ status, roleId }, index) {
        this.$confirm('确定要修改状态吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(async () => {;
          try {
            const res = await roleApis.updateRoleStatus({ userId, status });
            if (res.code == 200) {
              this.$message.success('修改成功');
            } else {
              this.$message.error(res.msg || '修改失败');
              this.roleData.splice(index, 1, { ...this.roleData[index], status: status == '1' ? '0' : '1' });
            }
          } catch (error) {
            this.$message.error(error.msg || '修改失败');
          }
        }).catch (() => {
          this.roleData.splice(index, 1, { ...this.roleData[index], status: status == '1' ? '0' : '1' });
        })
      },
    },
    mounted() {
      this.handleQuery();
    }
  }
</script>

<style lang="less" scoped>
.el-pagination {
  position: relative;
}
.pagination {
  text-align: right;
  margin-top: 20px;
}
.tree-border {
  margin-top: 5px;
  border: 1px solid #e5e6e7;
  background: #ffffff none;
  border-radius: 4px;
  width: 100%;
}
</style>
