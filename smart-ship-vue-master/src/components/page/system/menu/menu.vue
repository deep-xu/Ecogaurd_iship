<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="menuName">
            <el-input
              v-model="queryParams.menuName"
              placeholder="Please enter menu name"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item prop="status">
            <el-select
              v-model="queryParams.status"
              placeholder="Please select status"
              clearable
            >
              <el-option
                v-for="dict in sys_normal_disable"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery"
              >Search</el-button
            >
            <el-button icon="Refresh" @click="resetQuery">Reset</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd()"
              >Add
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="info"
              plain
              icon="Sort"
              @click="handleToggleExpandAll"
              >Expand/Collapse</el-button
            >
          </el-col>
        </el-row>
      </template>

      <el-table
        ref="menuTableRef"
        v-loading="loading"
        :data="menuList"
        row-key="menuId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isExpandAll"
      >
        <el-table-column
          prop="menuName"
          label="Menu Name"
          :show-overflow-tooltip="true"
          width="160"
        ></el-table-column>
        <!-- <el-table-column prop="icon" label="图标" align="center" width="100">
          <template #default="scope">
            <svg-icon :icon-class="scope.row.icon" />
          </template>
        </el-table-column> -->
        <el-table-column
          prop="orderNum"
          label="Order"
          width="60"
        ></el-table-column>
        <el-table-column
          prop="perms"
          label="Permission Char"
          :show-overflow-tooltip="true"
        ></el-table-column>
        <el-table-column
          prop="component"
          label="Component Path"
          :show-overflow-tooltip="true"
        ></el-table-column>
        <el-table-column prop="status" label="Status" width="80">
          <template #default="scope">
            <el-tag mini effect="dark">
              {{ scope.row.status == "1" ? "Deactivated" : "Normal" }}
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
              <el-button
                link
                type="text"
                icon="z-icon-edit"
                @click="handleUpdate(scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="Add" placement="top">
              <el-button
                link
                type="text"
                icon="z-icon-add"
                @click="handleAdd(scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button
                link
                type="text"
                icon="z-icon-del"
                @click="handleDelete(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      class="dialogCls"
      :visible.sync="dialog.visible"
      :title="dialog.title"
      append-to-body
      width="750px"
    >
      <el-form
        ref="menuFormRef"
        :model="form"
        :rules="rules"
        label-width="auto"
      >
        <el-row>
          <el-col :span="24">
            <el-form-item label="Parent Menu">
              <tree-select
                ref="menuTree"
                v-model="form.parentId"
                :options="menuOptions"
                :value-id="form.parentId"
                :objMap="{
                  value: 'menuId',
                  label: 'menuName',
                  children: 'children',
                }"
                value-key="menuId"
                placeholder="Please select"
                @getValue="setParentId"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Menu Type" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio label="M">Directory</el-radio>
                <el-radio label="C">Menu</el-radio>
                <el-radio label="F">Button</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <!-- <el-col v-if="form.menuType !== 'F'" :span="24">
            <el-form-item label="菜单图标" prop="icon">
              <icon-select v-model="form.icon" />
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="Menu Name" prop="menuName">
              <el-input
                v-model="form.menuName"
                placeholder="Please enter menu name"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Order" prop="orderNum">
              <el-input-number
                v-model="form.orderNum"
                controls-position="right"
                :min="0"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item prop="path" label="Router Path">
              <el-input
                v-model="form.path"
                placeholder="Please enter router path"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType === 'C'" :span="12">
            <el-form-item prop="component" label="Component Path">
              <el-input
                v-model="form.component"
                placeholder="Please enter component path"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'M'" :span="12">
            <el-form-item label="Permission Char">
              <el-input
                v-model="form.perms"
                placeholder="Please enter permission char"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType === 'C'" :span="12">
            <el-form-item>
              <el-input
                v-model="form.queryParam"
                placeholder="请输入路由参数"
                maxlength="255"
              />
              <template #label>
                <span>
                  <el-tooltip
                    content='访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`'
                    placement="top"
                  >
                    <el-icon>
                      <question-filled />
                    </el-icon>
                  </el-tooltip>
                  路由参数
                </span>
              </template>
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item label="Show Status">
              <el-radio-group v-model="form.visible">
                <el-radio
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                  >{{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Menu Status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                >
                  {{ dict.label }}
                </el-radio>
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
import TreeSelect from "./TreeSelect.vue";
import {menuApis} from "@/api/menu.js";

export default {
  components: { TreeSelect },
  data() {
    return {
      queryParams: {},
      sys_normal_disable: [
        { value: "0", label: "Normal" },
        { value: "1", label: "Deactivated" },
      ],
      loading: false,
      menuList: [],
      dialog: { title: "", visible: false },
      form: {
        menuType: "M",
        status: "0",
        visible: "0",
      },
      rules: {
        menuName: [
          {
            required: true,
            message: "Please enter menu name",
            trigger: "blur",
          },
        ],
        orderNum: [
          { required: true, message: "Please enter order", trigger: "blur" },
        ],
        path: [
          { required: true, message: "Please enter path", trigger: "blur" },
        ],
      },
      menuOptions: [],
      isExpandAll: false,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    setParentId(val) {
      this.form.parentId = val || 0;
    },
    handleTree(data, id, parentId, children) {
      const config = {
        id: id || "id",
        parentId: parentId || "parentId",
        childrenList: children || "children",
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
      let data = await menuApis.getMenuList(this.queryParams);
      this.menuList = this.handleTree(data, "menuId");
    },
    handleQuery() {
      this.getList();
    },
    resetQuery() {
      this.queryParams = {};
      this.getList();
    },
    async handleAdd({ menuId = "0" } = {}) {
      await this.getTreeSelect();
      this.dialog.visible = true;
      this.dialog.title = "Add Menu";
      this.form = {
        menuType: "M",
        status: "0",
        visible: "0",
        parentId: menuId,
      };
    },
    async handleUpdate(row) {
      await this.getTreeSelect();
      this.form = { ...row };
      this.dialog.visible = true;
      this.dialog.title = "Modify Menu";
    },
    async getTreeSelect() {
      const data = await menuApis.getMenuList();
      const menu = { menuId: "0", menuName: "Main category", children: [] };
      menu.children = this.handleTree(data, "menuId");
      this.menuOptions = [menu];
    },
    async handleDelete(row) {
      await this.$confirm(
        '是否确认删除名称为"' + row.menuName + '"的数据项?',
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      );
      const res = await menuApis.delMenu(row.menuId);
      if (res.code === 200) {
        this.getList();
        this.$message.success("删除成功");
      } else {
        this.$message.error(res.msg || "删除失败");
      }
    },
    handleToggleExpandAll() {
      this.isExpandAll = !this.isExpandAll;
      this.toggleExpandAll(this.menuList, this.isExpandAll);
    },
    toggleExpandAll(data, status) {
      data.forEach((item) => {
        this.$refs.menuTableRef.toggleRowExpansion(item, status);
        if (item.children && item.children.length > 0)
          this.toggleExpandAll(item.children, status);
      });
    },
    submitForm() {
      this.$refs.menuFormRef.validate(async (valid) => {
        if (valid) {
          const data = {
            ...this.form,
            status: parseInt(this.form.status),
            visible: parseInt(this.form.visible),
            icon: "https://avatars.githubusercontent.com/u/6485159",
          };
          const res = this.form.menuId
            ? await menuApis.updateMenu(data)
            : await menuApis.addMenu(data);
          if (res.code === 200) {
            this.$message.success("操作成功");
            this.dialog.visible = false;
            this.getList();
          } else {
            this.$message.error(res.msg || "操作失败");
          }
        }
      });
    },
    cancel() {
      this.dialog.visible = false;
      this.form = {
        menuType: "M",
        status: "0",
        visible: "0",
      };
      this.$refs.menuTree.clearHandle();
    },
  },
};
</script>

<style lang="less" scoped>
.p-2 {
  overflow-y: scroll !important;
}
.dialogCls {
  /deep/.el-dialog .el-dialog__body {
    padding: 20rem 40rem;
  }
}
</style>
