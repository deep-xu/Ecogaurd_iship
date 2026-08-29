<template>
  <div class="ppe-win">
    <div class="groupNameList" v-if="groupList.length > 0">
      <ul>
        <li
          v-for="(item, index) in groupList"
          :key="item.id"
          :class="{ active: formData.index == index }"
          @click="handleGroupChg(item, index)"
        >
          <div class="groupname">{{ item.name }}</div>
          <i
            class="z-iconfont z-icon-del"
            @click.stop="handleDelGroup(item)"
          ></i>
        </li>
        <li @click="handleAddGroup">
          <div class="groupname">
            <i class="z-iconfont z-icon-add"></i>
          </div>
        </li>
      </ul>
    </div>
    <div class="group-edit-container">
      <el-form
        ref="formData"
        :model="formData"
        :rules="formRule"
        style="width: 310px; padding-top: 50px"
        label-width="112"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder=""
            :maxlength="30"
            style="width: 200px"
          ></el-input>
        </el-form-item>
        <el-form-item style="text-align: center">
          <el-button type="primary" :loading="saveloading" @click="handleSave"
            >保存</el-button
          >
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { group } from "@/api/libs.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["groupList", "group_id", "device_id"],
  data() {
    return {
      saveloading: false,
      formData: {
        index: 0,
        name: "",
      },
      formRule: {
        name: [
          {
            required: true,
            message: "请输入分组名称",
            trigger: "blur",
          },
        ],
      },
    };
  },
  watch: {
    group_id(n, o) {
      this.resetInfo();
    },
    groupList: {
      deep: true,
      handler(n, o) {
        if (n.length > 0 && this.formData.optype == "add") {
          this.formData.optype = "edit";
          this.formData.id = n[n.length - 1].id;
        }
        let find = n.find((item) => item.id == this.formData.id);
        if (!find) {
          if (n.length > 0) {
            this.formData.index = 0;
            this.formData.name = n[0].name;
            this.formData.id = n[0].id;
          } else {
            this.formData.name = "";
            this.formData.id = "";
          }
        } else {
          for (let i = 0; i < n.length; i++) {
            if (n[i].id == this.formData.id) {
              this.formData.index = i;
              break;
            }
          }
        }
      },
    },
  },
  mounted() {
    this.resetInfo();
  },
  methods: {
    resetInfo() {
      if (this.$props.group_id) {
        this.formData.optype = "edit";
        for (let i = 0; i < this.$props.groupList.length; i++) {
          if (this.$props.groupList[i].id == this.$props.group_id) {
            this.formData.index = i;
            this.formData.name = this.$props.groupList[i].name;
            this.formData.id = this.$props.group_id;
            break;
          }
        }
      } else {
        this.formData.optype = "add";
        this.formData.name = "";
        this.formData.id = "";
      }
    },
    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.saveloading = true;
          let params = {
            alg: "ppe",
            name: this.formData.name,
          };
          if (this.formData.optype == "edit") {
            params.id = this.formData.id;
            params.device_id = this.$props.device_id;
            group
              .edit(params)
              .then((res) => {
                this.saveloading = false;
                this.$message({
                  type: "success",
                  message: res.message[this.lang],
                });
                this.$emit("update-ppe-group");
              })
              .catch((err) => {
                this.saveloading = false;
                if (res && res.message && res.message[this.lang]) {
                  this.$message({
                    type: "error",
                    message: res.message[this.lang],
                  });
                }
              });
          } else {
            params.device_ids = [this.$props.device_id];
            group
              .add(params)
              .then((res) => {
                this.saveloading = false;
                if (res[this.$props.device_id].result == true) {
                  this.$message({
                    type: "success",
                    message: "新增分组成功",
                  });
                  // this.$emit("update-workclothes-group");
                  this.$emit("close-ppe-group");
                }
              })
              .catch((err) => {
                this.saveloading = false;
                if (res && res.message && res.message[this.lang]) {
                  this.$message({
                    type: "error",
                    message: res.message[this.lang],
                  });
                }
              });
          }
          
        }
      });
    },
    handleDelGroup(item) {
      this.$confirm("是否确认删除?", "删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      })
        .then(() => {
          group
            .del({
              ids: [item.id],
              alg: "ppe",
              device_ids: [this.$props.device_id],
            })
            .then((res) => {
              if (item.id == this.formData.id) {
                this.formData.id = "";
                this.formData.name = "";
              }
              if (res[this.$props.device_id].result == true) {
                this.$message({
                  type: "success",
                  message: "删除分组成功",
                });
              }
              this.$emit("update-ppe-group");
            })
            .catch((error) => {
              if (error.message) {
                this.$message({
                  type: "error",
                  message: error.message[this.lang],
                });
              }
            });
        })
        .catch((error) => {
          // 取消删除
        });
    },
    handleGroupChg(item, index) {
      this.formData.optype = "edit";
      this.formData.index = index;
      this.formData.id = item.id;
      this.formData.name = item.name;
    },
    handleAddGroup() {
      this.formData.optype = "add";
      this.formData.id = "";
      this.formData.name = "";
      this.formData.index = this.groupList.length;
    },
  },
};
</script>

<style lang="less" scoped>
.ppe-win {
  display: flex;
  .groupNameList {
    width: 140px;
    max-height: 200px;
    overflow: auto;
    border-right: 1px solid #2a2b33;
    ul {
      list-style: none;
      li {
        position: relative;
        width: 100px;
        .groupname {
          width: 80px;
          text-align: center;
          padding: 6px 10px;
          border: 1px solid #2a2b33;
          border-radius: 4px;
          color: #333;
          cursor: pointer;
          margin-bottom: 10px;
          position: relative;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          word-break: break-all;
        }
        .z-icon-del {
          position: absolute;
          right: -20px;
          top: 8px;
          font-size: 14px;
          color: #959595;
          cursor: pointer;
        }
      }

      li.active .groupname {
        background-color: #e1e1e1;
      }
    }
  }
  .group-edit-container {
    width: 100%;
    flex: 1 1;
    display: flex;
    justify-content: center;
  }
}
</style>
