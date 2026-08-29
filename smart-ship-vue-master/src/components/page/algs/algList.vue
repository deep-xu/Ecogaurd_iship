<template>
  <div>
    <div class="table-container height1">
      <el-table
        v-loading="listLoading"
        element-loading-text="拼命加载中"
        element-loading-spinner="el-icon-loading"
        :data="tableData"
        :border="false"
        stripe
      >
        <el-table-column prop="name" label="算法名称"> </el-table-column>
        <el-table-column prop="desc" label="算法描述"> </el-table-column>
        <el-table-column prop="conf" label="置信度"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <button type="danger" @click="handleDel(scope.row)">删除</button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { algApis } from "@/api/alg.js"
export default {
  props:['type'],
  data() {
    return {
      listLoading:false,
      tableData:[]
    };
  },
  beforeMount(){
    // this.getTableData()
  },
  mounted(){},
  methods:{
    getTableData(){
      algApis.getTableData().then(res => {
        this.tableData = res.data.filter(item => item.type == this.$props.type)
      })
    },
    handleDel(row){

    }
  }
};
</script>

<style lang="less" scoped>
</style>