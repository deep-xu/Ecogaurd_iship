<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="name">
            <el-input v-model="queryParams.name" placeholder="name" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="id">
            <el-input v-model="queryParams.id" placeholder="NVR ID" clearable @keyup.enter="handleQuery" />
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

      <el-table v-loading="loading" :data="nvrList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" align="center" prop="id" />
        <el-table-column label="Key" align="center" prop="password" />
        <el-table-column label="Name" align="center" prop="name" />
        <el-table-column label="remark" align="center" prop="remark" />
        <el-table-column label="ship Name" align="center" prop="shipName" />
        <el-table-column label="status" align="center" prop="ip" >
          <template #default="{row}">
            <el-tag v-if="row.ip" type="success">online</el-tag>
            <el-tag v-else type="danger">offline</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Options" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="Modify" placement="top">
              <el-button link type="text" @click="handleUpdate(scope.row)">
                <i class="z-icon-edit"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button link type="text" @click="handleDelete(scope.row.id)">
                <i class="z-icon-del"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="View" placement="top">
              <el-button link type="text" icon="el-icon-view" @click="handLoadVoids(scope.row.id)">
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
        <el-form-item label="NVR ID" prop="id">
          <el-input v-model="form.id" placeholder="Please enter the ID" />
        </el-form-item>
        <el-form-item label="NVR Key" prop="password">
          <el-input v-model="form.password" placeholder="Please enter the key" />
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" placeholder="Please enter the name" />
        </el-form-item>
        <el-form-item label="remark" prop="remark">
          <el-input v-model="form.remark" placeholder="Please enter the remark" />
        </el-form-item>
        <el-form-item label="Ship" prop="ship">
          <el-select
              v-model="form.shipId"
              placeholder="Please select the ship"
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
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">Confirm</el-button>
          <el-button @click="cancel">Cancel</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog title="Camera List" :visible.sync="dialogVoideVisible" append-to-body width="65%">
      <NvrChannel :nvr-id="viewNvrId" ref="nvrChannel"/>
    </el-dialog>
  </div>
</template>

<script>
import {nvrApis} from "@/api/nvrApis";
import {shipApis} from "@/api/ship";
import NvrChannel from "@/components/page/nvrChannel/index.vue";


export default {
  components: {
    NvrChannel
  },
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      shipList: [],
      portStatusMap: {},
      nvrList: [],
      deleteList: [],
      loading: false,
      total: 0,
      dialog: { title: 'Add', visible: false },
      rules: {
        id: [{ required: true, message: 'Please enter the ID', trigger: 'blur' }],
        password: [{ required: true, message: 'Please enter the key', trigger: 'blur' }],
        name: [{ required: true, message: 'Please enter the name', trigger: 'blur' }],
        shipId: [{ required: true, message: 'Please enter the ship', trigger: 'blur' }],
      },
      form: {
      },
      buttonLoading: false,
      dialogVoideVisible:false,
      viewNvrId:undefined,
    }
  },
  methods: {
    handleAdd() {
      this.dialog.visible = true;
    },
    handleUpdate (row) {
      this.dialog.visible = true;
      this.dialog.title = 'Modify';
      this.form = row;
      shipApis.getTableData().then(res=>{
        this.shipList = res.rows || [];
      });
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
          await nvrApis.deleteNvr(id || this.deleteList.join(','));
          this.$message.success('Delete successfully');
          this.handleQuery();
        } catch (error) {
          this.$message.error('Delete failed');
        }
      })
    },
    handLoadVoids(id){
      this.dialogVoideVisible=true;
      this.viewNvrId=id;
      const nvrChannel = this.$refs.nvrChannel;
      if (nvrChannel) {
        nvrChannel.handleLoadChannel(id);
      }
    },
    handleSelectionChange(val) {
      this.deleteList = val.map(({ id }) => id);
    },
    async handleQuery() {
      let { rows, total } = await nvrApis.getTableData(this.queryParams);
      this.total = total || 0;
      this.nvrList = rows;
    },
    async submitForm () {
      try {
        await this.$refs.shipFormRef.validate();
        this.buttonLoading = true;
        let data = {};
        if (this.form.id) {
          data = await nvrApis.updateNvr(this.form);
        } else {
          data = await nvrApis.addNvr(this.form);
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
      this.form = {};
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