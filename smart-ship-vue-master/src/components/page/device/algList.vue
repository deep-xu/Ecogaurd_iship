<template>
  <div class="algList">
    <!-- <ul class="grouptype">
      <li
        @click="groupname = '人员管理'"
        :class="{ active: groupname == '人员管理' }"
      >
        人员管理
      </li>
      <li
        @click="groupname = '车辆管理'"
        :class="{ active: groupname == '车辆管理' }"
      >
        车辆管理
      </li>
      <li
        @click="groupname = '险情防控'"
        :class="{ active: groupname == '险情防控' }"
      >
        险情防控
      </li>
      <li
        @click="groupname = '行业定制'"
        :class="{ active: groupname == '行业定制' }"
      >
        行业定制
      </li>
    </ul> -->

    <el-tabs v-model="groupname" @tab-click="handleTabClick">
      <el-tab-pane v-for="item in groupList" :key="item" :label="item" :name="item"></el-tab-pane>
    </el-tabs>

    <div style="height: calc(100% - 40rem)">
      <div class="table-container">
        <el-table
          element-loading-text="Loading"
          element-loading-spinner="el-icon-loading"
          :data="tableData"
          :border="false"
        >
          <el-table-column prop="ch_name" label="Algorithm Name"></el-table-column>
          <el-table-column label="Algorithm Description" min-width="200px">
            <template slot-scope="scope">
              <ul style="list-style: disc">
                <li
                  v-for="(item, key) in scope.row.descList"
                  :key="key"
                  style="margin-left: 20px; text-align: left"
                >
                  <span>{{ item }}</span>
                </li>
              </ul>
            </template>
          </el-table-column>
          <!-- <el-table-column label="置信度" width="200px">
            <template slot-scope="{ row }">
              <ul>
                <li
                  v-for="(item, key) in row.model"
                  :key="key"
                  style="
                    margin-left: 20px;
                    list-style: none;
                    word-break: keep-all;
                  "
                >
                  <label v-if="item.args && item.args.conf_thres"
                    >{{ item.ch_name }}:
                  </label>
                  <span v-if="item.args && item.args.conf_thres">{{
                    item.args.conf_thres
                  }}</span>
                </li>
              </ul>
            </template>
          </el-table-column> -->
          <el-table-column prop="version" label="Version"></el-table-column>

          <el-table-column
            label="Operation"
            class-name="operate-column"
            width="150px"
          >
            <template slot-scope="{ row, index }">
              <!-- <el-tooltip content="编辑" placement="top-start">
                <span
                  class="mouse-on"
                  @click="handleEdit(row, index)"
                  style="
                    padding: 5px 10px;
                    display: inline-block;
                    cursor: pointer;
                  "
                >
                  <i class="z-iconfont z-icon-edit"></i>
                </span>
              </el-tooltip> -->
              <el-tooltip content="Delete" placement="top-start">
                <span
                  class="mouse-on"
                  @click="handleDel(row, index)"
                  style="
                    padding: 5px 10px;
                    display: inline-block;
                    cursor: pointer;
                  "
                >
                  <i class="z-iconfont z-icon-del"></i>
                </span>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="edit-model" v-if="editVisible" @click="editVisible = false">
      <div class="edit-container" @click.stop="() => {}">
        <div class="header">
          编辑<i class="z-icon-close" @click.stop="editVisible = false"></i>
        </div>
        <div class="body">
          <algEdit
            :device_id="editDeviceInfo.id"
            :formDataInit="formDataInit"
            v-if="editVisible"
            @close-edit-alg="
              editVisible = false;
              getAlgList();
            "
          ></algEdit>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { algApis } from "@/api/alg.js";
import algEdit from "./algEdit.vue";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["editDeviceInfo"],
  components: { algEdit },
  data() {
    return {
      groupname: "车辆管理",
      groupList:[],
      search: {
        ch_name: "",
      },
      allTableData: [],
      tableData: [],
      formDataInit: {},
      editVisible: false,
    };
  },
  watch: {
    groupname(o, n) {
      this.filterTableData();
    },
  },
  mounted() {
    this.getAlgList();
  },
  methods: {
    filterTableData() {
      this.tableData = this.allTableData
        .filter((item) => item.group_name == this.groupname)
        .map((item) => {
          item.model = JSON.parse(JSON.stringify(item.model));
          item.descList =
            item.desc && item.desc.replaceAll(";", "\n").split("\n");
          return Object.assign({}, item, {
            showSave: false,
            upload: false,
            ori_conf_thres: item.conf_thres,
          });
        });
    },
    getAlgList() {
      let device_id = this.$props.editDeviceInfo.id;
      algApis
        .getTableData({ device_id: device_id })
        .then((res) => {
          this.allTableData = res[device_id].sort((a,b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
          let groupList = [];
          for(let i = 0; i < this.allTableData.length; i++){
            if(!groupList.find(item => item == this.allTableData[i].group_name)){
              groupList.push(this.allTableData[i].group_name)
            }
          }
          this.groupList = groupList.sort((a,b) => a.toLowerCase().localeCompare(b.toLowerCase()));
          this.filterTableData();
        })
        .catch((err) => {
          if (err && err.message && err.message[this.lang]) {
            this.$message({
              type: "error",
              message: err.message[this.lang],
            });
          }
        });
      // this.allTableData = JSON.parse(
      //   JSON.stringify(this.$props.editDeviceInfo.algList)
      // );
    },
    handleEdit(row) {
      this.editVisible = true;
      this.formDataInit = JSON.parse(JSON.stringify(row));
      this.$forceUpdate();
    },
    handleDel(row, index) {
      this.$confirm("Are you sure to delete the algorithm?", "Delete", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
      })
        .then(() => {
          algApis
            .del({
              device_ids: [this.$props.editDeviceInfo.id],
              name: row.name,
            })
            .then((res) => {
              console.log("delete alg", res);
              this.getAlgList();
              this.$message({
                type: "success",
                message: "Delete algorithm success！",
              });
            })
            .catch((error) => {
              this.$message({
                type: "error",
                message: "Delete algorithm failed！",
              });
            });
        })
        .catch((err) => {
          row.delloading = false;
        });
    },

    handleTabClick(tab){
      this.groupname = tab.name
    }
  },
};
</script>

<style lang="less" scoped>
.algList {
  position: relative;
  padding: 0 15rem 15rem;
  box-sizing: border-box;
  height: 100%;
  /deep/.el-tabs__header{
    margin-bottom: 0;
  }
  /deep/.el-tabs__item{
    height: 40rem;
    line-height: 40rem;
    font-size: 14rem;
  }
  /deep/.el-tabs__nav{
    float: none;
    margin: 0 auto;
    width: fit-content;
  }
  /deep/.el-tabs__item.is-active,
  /deep/.el-tabs__item:hover{
    color: #2c6dd2;
  }
  /deep/.el-tabs__active-bar{
    background-color:#2c6dd2 ;
  }
  .grouptype {
    display: flex;
    height: 40rem;
    width: 100%;
    padding: 0 8rem;
    box-sizing: border-box;
    border-bottom: 1px solid #f0f0f0;
    justify-content: center;
    position: relative;
    z-index: 1;
    li {
      width: 100rem;
      height: 37rem;
      font-size: 16rem;
      display: flex;
      align-items: center;
      justify-content: center;
      border-bottom: 3px solid transparent;
      cursor: pointer;
      position: relative;
    }
    li::before {
      content: "";
      position: absolute;
      height: 3px;
      width: 0;
      background-color: gray;
      transition: all 0.5s;
      bottom: -3px;
    }
    li:hover:before {
      width: 100%;
      transition: all 0.5s;
    }
    li.active {
      border-color: #2c6dd2;
    }
    li:hover {
      background-color: #f9f9f9;
    }
    li.active:hover {
      background-color: transparent;
    }
    li.active::before {
      display: none;
    }
  }
  .table-container {
    height: calc(100% - 40rem);
  }
}
.table-container .el-table {
  height: calc(100%);
}

.edit-model {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background-color: rgba(128, 128, 128, 0.37);
  z-index: 10;
  .edit-container {
    background-color: #fff;
    position: absolute;
    width: 40%;
    max-width: 600rem;
    right: 0;
    top: 0rem;
    height: 100%;
    padding: 0 20rem;
    box-sizing: border-box;
    .header {
      height: 40rem;
      position: relative;
      font-size: 16rem;
      display: flex;
      align-items: center;
      .z-icon-close {
        position: absolute;
        right: 10rem;
        font-size: 18rem;
        cursor: pointer;
      }
    }
    .body {
      height: calc(100% - 50rem);
      overflow: auto;
    }
  }
}
</style>