<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="dictLabel">
            <el-input v-model="queryParams.dictLabel" placeholder="groupName" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="dictValue">
            <el-input v-model="queryParams.dictValue" placeholder="Code" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">Search</el-button>
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
        </el-row>
      </template>

      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="Code" align="center" prop="dictValue" />
        <el-table-column label="Group Name" align="center" prop="dictLabel" />
        <el-table-column label="remark" align="center" prop="remark" />
        <el-table-column label="Options" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="Modify" placement="top">
              <el-button link type="text" @click="handleUpdate(scope.row)">
                <i class="z-icon-edit"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button link type="text" @click="handleDelete(scope.row.dictCode)">
                <i class="z-icon-del"></i>
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
        >
        </el-pagination>
      </div>
    </el-card>
    <el-dialog :title="dialog.title" :visible.sync="dialog.visible" width="600px" append-to-body @close="cancel">
      <el-form ref="shipFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="code" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="Please enter the code" />
        </el-form-item>
        <el-form-item label="GroupName" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="Please enter the GroupName" />
        </el-form-item>
        <el-form-item label="remark" prop="remark">
          <el-input v-model="form.remark" placeholder="Please enter the remark" />
        </el-form-item>
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
import {dict} from "@/api/system";

export default {
  data() {
    return {
      queryParams: {
        dictType:'channel_group',
        pageNum: 1,
        pageSize: 10,
        dictValue:undefined,
        dictLabel:undefined,
      },
      dataList: [],
      deleteList: [],
      loading: false,
      total: 0,
      dialog: { title: 'Add', visible: false },
      rules: {
        dictLabel: [{ required: true, message: 'Please enter the GroupName', trigger: 'blur' }],
        dictValue: [{ required: true, message: 'Please enter the Code', trigger: 'blur' }],
      },
      form: {
        dictType:'channel_group',
      },
      buttonLoading: false,
    }
  },
  methods: {
    handleAdd() {
      this.dialog.visible = true;
    },
    handleUpdate (row) {
      console.log(row)
      this.dialog.visible = true;
      this.dialog.title = 'Modify';
      this.form = row;
    },
    handleDelete (id) {
      if (!id && this.deleteList.length === 0) {
        return;
      }
      this.$confirm('Are you sure to delete this data?', 'Tip', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(async () => {
        try {
          await dict.del(id || this.deleteList.join(','));
          this.$message.success('Delete successfully');
          await this.handleQuery();
        } catch (error) {
          this.$message.error('Delete failed');
        }
      })
    },
    handleSelectionChange(val) {
      this.deleteList = val.map(({ dictCode }) => dictCode);
    },
    async handleQuery() {
      let { rows, total } = await dict.getList(this.queryParams);
      this.total = total || 0;
      this.dataList = rows;
    },
    async submitForm () {
      try {
        await this.$refs.shipFormRef.validate();
        this.buttonLoading = true;
        let data = {};
        if (this.form.dictCode) {
          data = await dict.edit(this.form);
        } else {
          data = await dict.add(this.form);
        }
        this.dialog.visible = false;
        this.handleQuery();
        this.$message.success(data.msg);
      } catch (error) {
      } finally {
        this.buttonLoading = false;
      }
    },
    cancel () {
      this.dialog.visible = false;
      this.form = {
        dictType: 'channel_group',
      };
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.handleQuery();
    },
    handlePageChange(val) {
      this.queryParams.pageNum = val;
      this.handleQuery();
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
</style>