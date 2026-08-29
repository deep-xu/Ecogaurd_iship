<template>
<div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="name">
            <el-input v-model="queryParams.name" placeholder="name" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="imoNum">
            <el-input v-model="queryParams.imoNum" placeholder="IMO number" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="platform">
            <el-input v-model="queryParams.platform" placeholder="PlatformIP" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <!--<el-form-item prop="ak">
            <el-input v-model="queryParams.ak" placeholder="ak" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="sk">
            <el-input v-model="queryParams.sk" placeholder="sk" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="port1">
            <el-input v-model="queryParams.port1" placeholder="port1" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="port2">
            <el-input v-model="queryParams.port2" placeholder="port2" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="port3">
            <el-input v-model="queryParams.port3" placeholder="port3" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="desc">
            <el-input v-model="queryParams.desc" placeholder="description" clearable @keyup.enter="handleQuery" />
          </el-form-item>-->
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">Search</el-button>
<!--            <el-button icon="Refresh" @click="resetQuery">Reset</el-button>-->
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8" type="flex" justify="end">
          <el-col :span="1.5">
            <el-button icon="Download" @click="downloadSystemLog">Download Log</el-button>
          </el-col>
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

      <el-table v-loading="loading" :data="shipList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" align="center" prop="id" v-if="true" />
        <el-table-column label="Dept" align="center" prop="deptName" />
        <el-table-column label="Name" align="center" prop="name" />
        <el-table-column label="IMO Number" align="center" prop="imoNum" />
        <el-table-column label="PlatformIP" align="center" prop="platform" />
        <el-table-column label="ak" align="center" prop="ak" />
        <el-table-column label="sk" align="center" prop="sk" />
        <el-table-column label="port1" align="center" prop="port1" >
          <template #default="scope">
            {{scope.row.port1}}
            <el-tooltip placement="top" v-if="portStatusMap[`${scope.row.platform}:${scope.row.port1}`] && portStatusMap[`${scope.row.platform}:${scope.row.port1}`].state">
              <div slot="content" v-for="item in portStatusMap[`${scope.row.platform}:${scope.row.port1}`].message">
                {{item}}<br/>
              </div>
              <i class="el-icon-warning" style="color: red"></i>
            </el-tooltip>
            <i class="el-icon-warning" style="color: green" v-else-if="portStatusMap[`${scope.row.platform}:${scope.row.port1}`] && !portStatusMap[`${scope.row.platform}:${scope.row.port1}`].state"></i>
          </template>
        </el-table-column>
        <el-table-column label="port2" align="center" prop="port2" >
          <template #default="scope">
            {{scope.row.port2}}
            <el-tooltip placement="top" v-if="portStatusMap[`${scope.row.platform}:${scope.row.port2}`] && portStatusMap[`${scope.row.platform}:${scope.row.port2}`].state">
              <div slot="content" v-for="item in portStatusMap[`${scope.row.platform}:${scope.row.port2}`].message">
                {{item}}<br/>
              </div>
              <i class="el-icon-warning" style="color: red"></i>
            </el-tooltip>

            <i class="el-icon-warning" style="color: green" v-else-if="portStatusMap[`${scope.row.platform}:${scope.row.port2}`]&&!portStatusMap[`${scope.row.platform}:${scope.row.port2}`].state"></i>
          </template>
        </el-table-column>
        <el-table-column label="port3" align="center" prop="port3" >
          <template #default="scope">
            {{scope.row.port3}}
            <el-tooltip placement="top" v-if="portStatusMap[`${scope.row.platform}:${scope.row.port3}`] && portStatusMap[`${scope.row.platform}:${scope.row.port3}`].state">
              <div slot="content" v-for="item in portStatusMap[`${scope.row.platform}:${scope.row.port3}`].message">
                {{item}}<br/>
              </div>
              <i class="el-icon-warning" style="color: red"></i>
            </el-tooltip>

            <i class="el-icon-warning" style="color: green" v-else-if="portStatusMap[`${scope.row.platform}:${scope.row.port3}`] && !portStatusMap[`${scope.row.platform}:${scope.row.port3}`].state"></i>
          </template>
        </el-table-column>
        <el-table-column label="desc" align="center" prop="description" />
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
            <el-tooltip content="Check" placement="top">
              <el-button link type="text" @click="handlePing(scope.row.platform,scope.row.ak, scope.row.sk, scope.row.port1, scope.row.port2, scope.row.port3)">
                <i class="el-icon-refresh"></i>
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
        <el-form-item label="Dept" prop="deptId">
          <el-select v-model="form.deptId" placeholder="Please select dept">
            <el-option v-for="item in deptList" :key="item.deptId" :label="item.deptName" :value="item.deptId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" placeholder="Please enter the name" />
        </el-form-item>
        <el-form-item label="IMO Number" prop="imoNum">
          <el-input v-model="form.imoNum" placeholder="Please enter the IMO number" />
        </el-form-item>
        <el-form-item label="IP" prop="platform">
          <el-input v-model="form.platform" placeholder="Please enter the platform address" />
        </el-form-item>
        <el-form-item label="ak" prop="ak">
          <el-input v-model="form.ak" placeholder="Please enter the ak" />
        </el-form-item>
        <el-form-item label="sk" prop="sk">
          <el-input v-model="form.sk" placeholder="Please enter the sk" />
        </el-form-item>
        <el-form-item label="port1" prop="port1">
          <el-input v-model="form.port1" placeholder="Please enter the port1" />
        </el-form-item>
        <el-form-item label="port2" prop="port2">
          <el-input v-model="form.port2" placeholder="Please enter the port2" />
        </el-form-item>
        <el-form-item label="port3" prop="port3">
          <el-input v-model="form.port3" placeholder="Please enter the port3" />
        </el-form-item>
<!--        <el-form-item label="desc" prop="description">
          <el-input v-model="form.description" placeholder="Please enter the description" />
        </el-form-item>-->
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
import {shipApis} from '@/api/ship.js'
import {deptApis} from "@/api/department";

export default {
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      portStatusMap: {},
      shipList: [],
      deleteList: [],
      loading: false,
      total: 0,
      dialog: { title: 'Add', visible: false },
      rules: {
        deptId: [{ required: true, message: 'Please enter the dept', trigger: 'blur' }],
        name: [{ required: true, message: 'Please enter the name', trigger: 'blur' }],
        imoNum: [{ required: true, message: 'Please enter the IMO number', trigger: 'blur' }],
        platform: [{ required: true, message: 'Please enter the platform IP address', trigger: 'blur' }],
        ak: [{ required: true, message: 'Please enter the ak ', trigger: 'blur' }],
        sk: [{ required: true, message: 'Please enter the sk ', trigger: 'blur' }],
        port1: [{ required: true, message: 'Please enter the port1', trigger: 'blur' }],
        port2: [{ required: true, message: 'Please enter the port2', trigger: 'blur' }],
        port3: [{ required: true, message: 'Please enter the port3', trigger: 'blur' }],

        //description: [{ required: true, message: 'Please enter the description', trigger: 'blur' }],
      },
      form: {},
      buttonLoading: false,
      deptList:[],
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
          await shipApis.deleteShip(id || this.deleteList.join(','));
          this.$message.success('Delete successfully');
          this.handleQuery();
        } catch (error) {
          this.$message.error('Delete failed');
        }
      })
    },
    async handleExport () {
      try {
        const res = await shipApis.exportShip();
        if (res instanceof Blob) {
          var blob = new Blob([res]);
          var url = URL.createObjectURL(blob);
          var a = document.createElement("a");
          a.href = url;
          a.download = `ship_${new Date().getTime()}.xlsx`; 
          a.click();
          window.URL.revokeObjectURL(url);
        }
      } catch (error) {}
    },
    handleSelectionChange(val) {
      this.deleteList = val.map(({ id }) => id);
    },
    async handleQuery() {
      let { rows, total } = await shipApis.getTableData(this.queryParams);
      this.total = total || 0;
      this.shipList = rows;
    },
    resetQuery() {
      this.queryParams = {
        page: this.queryParams.pageNum,
        size: this.queryParams.pageSize,
      }
    },
    async submitForm () {
      try {
        await this.$refs.shipFormRef.validate();
        this.buttonLoading = true;
        let data = {};
        if (this.form.id) {
          data = await shipApis.updateShip(this.form);
        } else {
          data = await shipApis.addShip(this.form);
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
      this.form = {};;
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.handleQuery();
    },
    handlePageChange(val) {
      this.queryParams.pageNum = val;
      this.handleQuery();
    },
    handlePing(ip,ak,sk, ...ports) {
      this.$message({
        message: 'Checking, please wait!',
        type: 'warning'
      });

      const requests = ports.map((port, index)=> {
        const detail = {
          state: false,
          message: []
        }

        const portCheck = shipApis.isPortAvailable(ip, port).then(res => {
            if (res.code !== 200) {
              detail.state = true;
              detail.message.push(res.msg);
            }
          })

        const serverCheck = index === 0?shipApis.isServerAvailable(ip, ak, sk, port).then(res => {
          if (res.code !== 200) {
            detail.state = true;
            detail.message.push(res.msg);
          }
        }):undefined

        return Promise.all([portCheck, serverCheck]).then(()=>{
          this.$set(this.portStatusMap, `${ip}:${port}`, detail);
        })
      })


      Promise.all(requests).then(() => {
        this.$message({
          message: 'Check Completed',
          type: 'success'
        });
      }).catch(() => {
        this.$message({
          message: 'Check Completed',
          type: 'success'
        });
      })
    },
    downloadSystemLog(){
      shipApis.downloadLog().then(res => {
        this.download(res, 'system.log');
      })
    },
    async getDeptList() {
      this.deptList = await deptApis.getList();
    }
  },

  mounted() {
    this.handleQuery();
    this.getDeptList();
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