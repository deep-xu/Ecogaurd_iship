<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="menuName">
            <el-input v-model="queryParams.deptName" placeholder="Please enter department name" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="status">
            <el-select v-model="queryParams.status" placeholder="Please select status" clearable>
              <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">Search</el-button>
            <el-button icon="Refresh" @click="resetQuery">Reset</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd()">Add </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">Expand/Collapse</el-button>
          </el-col>
        </el-row>
      </template>

      <el-table
        v-loading="loading"
        ref="deptTableRef"
        :data="deptList"
        row-key="deptId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isExpandAll"
      >
        <el-table-column prop="deptName" label="Department Name" :show-overflow-tooltip="true" width="160"></el-table-column>
        <el-table-column prop="orderNum" label="Order" width="160"></el-table-column>
        <el-table-column prop="status" label="Status" width="80">
          <template #default="scope">
            <el-tag mini
              effect="dark">
              {{ scope.row.status == '1' ? 'Deactivated' : 'Normal' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Create Time" align="center" prop="createTime">
          <template #default="scope">
            <span>{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="Options" width="180">
          <template #default="scope">
            <el-tooltip content="Modify" placement="top">
              <el-button link type="text" icon="z-icon-edit" @click="handleUpdate(scope.row)" />
            </el-tooltip>
            <el-tooltip content="Add" placement="top">
              <el-button link type="text" icon="z-icon-add" @click="handleAdd(scope.row)" />
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button v-show="scope.row.parentId !== 0" link type="text" icon="z-icon-del" @click="handleDelete(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog class="dialogCls"  :visible.sync="dialog.visible" :title="dialog.title" append-to-body width="750px">
      <el-form ref="deptFormRef" :model="form" :rules="rules" label-width="160px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="Superior department" prop="parentId">
              <tree-select
                v-model="form.parentId"
                ref="deptTree"
                :value-id="form.parentId"
                :options="deptOptions"
                :objMap="{ value: 'deptId', label: 'deptName', children: 'children' }"
                value-key="deptId"
                placeholder="Please select the superior department"
                check-strictly
                @getValue="setParentId"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="DepartmentName" prop="deptName">
              <el-input v-model="form.deptName" placeholder="Please enter the department name" />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12">
            <el-form-item label="类别编码" prop="deptCategory">
              <el-input v-model="form.deptCategory" placeholder="请输入类别编码" />
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="Order" prop="orderNum" label-width="80px">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="1" :max="10"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Superintendent" prop="leader">
              <el-select v-model="form.leader" placeholder="Please select superintendent">
                <el-option v-for="item in deptUserList" :key="item.userId" :label="item.userName" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Phone" prop="phone" label-width="80px">
              <el-input v-model="form.phone" placeholder="Please enter phone" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email" prop="email">
              <el-input v-model="form.email" placeholder="Please enter email" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Status" label-width="80px">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">Confirm</el-button>
          <el-button @click="cancel">Cancel</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import TreeSelect from '../menu/TreeSelect.vue'
import { deptApis } from '@/api/department'
export default {
  name: "Department",
  components: { TreeSelect },
  data() {
    return {
      isExpandAll: false,
      queryParams: {},
      sys_normal_disable: [{ value: '0', label: 'Normal' }, { value: '1', label: 'Deactivated' }],
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 部门表格数据
      deptList: [],
      deptOptions: [],
      deptUserList: [],
      // 弹出层标题
      dialog: {
        title: "",
        visible: false,
      },
      // 表单参数
      form: {
        status: "0",
      },
      // 表单校验
      rules: {
        parentId: [{ required: true, message: "superior department can not be empty", trigger: "blur" }],
        deptName: [{ required: true, message: "department name can not be empty", trigger: "blur" }],
        orderNum: [{ required: true, message: "order number can not be empty", trigger: "blur" }],
      },
    }
  },
  mounted() {
    this.getList();
  },
  methods: { 
    setParentId(val) {
      this.form.parentId = val
    },
    handleTree(data, id, parentId, children) {
      const config = {
        id: id || 'id',
        parentId: parentId || 'parentId',
        childrenList: children || 'children'
      };

      const childrenListMap = {};
      const nodeIds = {};
      const tree = [];

      for (const d of data) {
        const parentId = d[config.parentId];
        if (childrenListMap[parentId] == null) {
          childrenListMap[parentId] = [];
        }
        nodeIds[d[config.id]] = d;
        childrenListMap[parentId].push(d);
      }

      for (const d of data) {
        const parentId = d[config.parentId];
        if (nodeIds[parentId] == null) {
          tree.push(d);
        }
      }
      const adaptToChildrenList = (o) => {
        if (childrenListMap[o[config.id]] !== null) {
          o[config.childrenList] = childrenListMap[o[config.id]];
        }
        if (o[config.childrenList]) {
          for (const c of o[config.childrenList]) {
            adaptToChildrenList(c);
          }
        }
      };

      for (const t of tree) {
        adaptToChildrenList(t);
      }

      return tree;
    },
    async getList() {
      try {
        const data = await deptApis.getList(this.queryParams);
        this.deptList = this.handleTree(data, 'deptId')
      } finally { 
        this.loading = false;
      }
    },
    handleQuery() {
      this.getList();
    },
    resetQuery() {
      this.queryParams = {};
      this.getList();
    },
    handleToggleExpandAll() {
      this.isExpandAll = !this.isExpandAll;
      this.toggleExpandAll(this.deptList, this.isExpandAll);
    },
    toggleExpandAll(data, status) {
      data.forEach((item) => {
        this.$refs.deptTableRef.toggleRowExpansion(item, status);
        if (item.children && item.children.length > 0) this.toggleExpandAll(item.children, status);
      });
    },
    async handleUpdate(row) {
      this.dialog.visible = true;
      this.dialog.title = 'Update Department';
      const data = await deptApis.getList();
      const list = this.handleTree(data, 'deptId');
      this.form = { ...row };
      if (list) {
        this.deptOptions = list;
        if (row && row.deptId) {
          this.form.parentId = row.deptId;
          console.log(this.form.parentId);
        }
      }
    },
    async handleAdd(row) {
      this.dialog.visible = true;
      this.dialog.title = 'Add Department';
      const data = await deptApis.getList();
      const list = this.handleTree(data, 'deptId');
      if (list) {
        this.deptOptions = list;
        console.log(this.deptOptions)
        if (row && row.deptId) {
          this.form.parentId = row.deptId;
        }
      }
    },
    async handleDelete(row) {
      await this.$confirm('是否确认删除名称为"' + row.deptName + '"的数据项?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        const res = await deptApis.delDept(row.deptId);
        if (res.code === 200) {
          this.getList();
          this.$message.success('删除成功');  
        } else {
          this.$message.error(res.msg || '删除失败');
        }
    },
    cancel() {
      this.dialog.visible = false;
      this.form = {
        status: "0",
      }
      this.$refs.deptTree.clearHandle();
    },
    async submitForm() {
      this.$refs.deptFormRef.validate(async (valid) => {
        if (valid) {
          const res = this.form.deptId ? await deptApis.updateDept(this.form) : await deptApis.addDept(this.form);
          if (res.code === 200) {
            this.$message.success('操作成功');
            this.dialog.visible = false;
            this.getList();
          } else {
            this.$message.error(res.msg || '操作失败');
          }
        }
      });
    }
  }

}

</script>