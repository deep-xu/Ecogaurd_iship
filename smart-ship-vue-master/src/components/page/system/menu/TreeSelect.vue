<template>
  <div class="el-tree-select">
    <el-select
      ref="treeSelect"
      v-model="valueId"
      style="width: 100%"
      :filterable="true"
      :clearable="true"
      :filter-method="selectFilterData"
      :placeholder="placeholder"
      @clear="clearHandle"
    >
      <el-option :value="valueId" :label="valueTitle">
        <el-tree
          id="tree-option"
          ref="selectTree"
          :accordion="accordion"
          :data="options"
          :props="objMap"
          :node-key="objMap.value"
          :expand-on-click-node="false"
          :default-expanded-keys="defaultExpandedKey"
          :filter-node-method="filterNode"
          @node-click="handleNodeClick"
        ></el-tree>
      </el-option>
    </el-select>
  </div>
</template>

<script>

export default {
  name: 'TreeSelect',
  props: ['objMap', 'placeholder', 'accordion', 'value', 'options',],
  data() {
    return {
      valueTitle: '',
      defaultExpandedKey: [],
    }
  },
  computed: {
    valueId: {
      get() {
        return this.$props.value;
      },
      set(val) {
        this.$emit('update:value', val);
      }
    }
  },
  watch: {
    valueId(newval) {
      this.initHandle();
    }
  },
  methods: {
    initHandle() {
      this.$nextTick(() => {
        const selectedValue = this.valueId;
        if (selectedValue !== null && typeof selectedValue !== 'undefined') {
          const node = this.$refs.selectTree.getNode(selectedValue);
          if (node) {
            this.valueTitle = node.data[this.objMap.label];
            this.$refs.selectTree.setCurrentKey(selectedValue); // 设置默认选中
            this.defaultExpandedKey = [selectedValue]; // 设置默认展开
          }
        } else {
          this.clearHandle();
        }
      });
    },
    clearHandle() {
      this.valueId = '';
      this.valueTitle = '';
      this.defaultExpandedKey = [];
      this.clearSelected();
    },
    clearSelected() {
      const allNode = document.querySelectorAll('#tree-option .el-tree-node');
      allNode.forEach((element) => element.classList.remove('is-current'));
    },
    handleNodeClick(node) {
      this.valueTitle = node[this.objMap.label];
      this.valueId = node[this.objMap.value];
      this.defaultExpandedKey = [];
      this.$refs.treeSelect.blur();
      this.selectFilterData('');
      this.$emit('getValue', node[this.objMap.value]);
    },
    selectFilterData(val) {
      this.$refs.selectTree.filter(val);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data[this.objMap.label].indexOf(value) !== -1;
    }
  },
  mounted() {
    this.initHandle();
  }
}
</script>

<style lang="less" scoped>

.el-scrollbar .el-scrollbar__view .el-select-dropdown__item {
  padding: 0;
  background-color: #fff;
  height: auto;
}

.el-select-dropdown__item.selected {
  font-weight: normal;
}

ul li .el-tree .el-tree-node__content {
  height: auto;
  padding: 0 20px;
  box-sizing: border-box;
}

:deep(.el-tree-node__content:hover),
:deep(.el-tree-node__content:active),
:deep(.is-current > div:first-child),
:deep(.el-tree-node__content:focus) {
  background-color: mix(#fff, #409eff, 90%);
  color: #409eff;
}
</style>
