<template>
  <div>
    <h3 style="margin-bottom: 10px">选择算法</h3>
    <ul class="algType">
      <li
        v-for="item in algTypes"
        :key="item.index + item.title"
        :class="{ active: item.index == active }"
        @click="handleTypeSel(item)"
      >
        <i
          class="z-checkbox"
          :class="{ 'z-checkbox-checked': item.checked }"
        ></i>
        {{ item.title }}
      </li>
    </ul>
    <ul class="algList">
      <li style="width: 100%" @click="handleCheckedAll">
        <i
          class="z-checkbox"
          :class="{
            'z-checkbox-checked':
              algTypes[active] && algTypes[active].checkedAll,
          }"
        ></i>
        全选
      </li>
      <li
        v-for="(item,index) in algList"
        :key="item.id"
        @click="handleCheckedAlg(item, index)"
      >
        <i
          class="z-checkbox"
          :class="{ 'z-checkbox-checked': item.checked }"
        ></i>
        {{ item.title }}
      </li>
    </ul>

    <div style="margin-top:10px;text-align:right;">
      <el-button class="btn-finish" type="primary" @click="handleFinish"
      >完成</el-button
    >
    </div>
  </div>
</template>

<script>
export default {
  props: ["algTree"],
  data() {
    return {
      algTypes: [],
      active: 0,
      algList: []
    };
  },
  watch: {
    // algTree: {
    //   deep: true,
    //   handler() {},
    // },
  },
  mounted() {
    this.getAlgType();
  },
  methods: {
    getAlgType() {
      let algTypes = this.$props.algTree.map((item, index) => {
        let checkedChildren = item.children.filter(
          (item1) => item1.checked == true
        );
        let checkedAll = checkedChildren.length == item.children.length ? true : false;
        return {
          index: index,
          title: item.title,
          checked: false,
          checkedAll: checkedAll,
        };
      });
      algTypes[0] && (algTypes[0].checked = true);
      this.algTypes = algTypes;
      this.getAlgList();
    },
    getAlgList() {
      this.algList = JSON.parse(
        JSON.stringify(this.$props.algTree[this.active].children)
      );
    },
    handleTypeSel(item) {
      this.active = item.index;
      this.algTypes.forEach((item) => (item.checked = false));
      item.checked = true;
      this.getAlgList();
    },
    handleCheckedAll() {
      if (this.algTypes[this.active].checkedAll == false) {
        this.algTypes[this.active].checkedAll = true;
        this.algList.forEach((item) => {
          item.checked = true;
        });
      } else {
        this.algTypes[this.active].checkedAll = false;
        this.algList.forEach((item) => {
          item.checked = false;
        });
      }
      this.handleAlgChg();
      this.$forceUpdate()
    },
    handleCheckedAlg(item, index) {
      this.algList[index].checked = item.checked == true ? false : true;
      let checkedAlg = this.algList.filter(
        (item) => item.checked == true
      );
      if (
        checkedAlg.length == this.algList.length
      ) {
        this.algTypes[this.active].checkedAll = true;
      } else {
        this.algTypes[this.active].checkedAll = false;
      }
      this.handleAlgChg();
      this.$forceUpdate()
    },
    handleAlgChg() {
      let selecteNodes = [];
      this.$props.algTree[this.active].children = JSON.parse(JSON.stringify(this.algList));

      this.$props.algTree.forEach((item) => {
        item.children.forEach((algInfo) => {
          if (algInfo.checked == true) {
            selecteNodes.push(algInfo);
          }
        });
      });
      this.$emit("on-check-change", selecteNodes);
    },
    handleFinish(){
      this.$emit("on-close-sel");
    }
  },
};
</script>

<style lang="less" scoped>
.z-checkbox {
  display: inline-block;
  width: 16px;
  height: 16px;
  position: relative;
  top: 0;
  left: 0;
  border: 1px solid #dcdee2;
  border-radius: 2px;
  margin-right: 3px;
  position: relative;
}
.z-checkbox-checked {
  background: #2c6dd2;
  border-color: #2c6dd2;
}
.z-checkbox-checked:after {
  content: "";
  display: table;
  width: 4px;
  height: 8px;
  position: absolute;
  top: 2px;
  left: 5px;
  border: 2px solid #fff;
  border-top: 0;
  border-left: 0;
  transform: rotate(45deg) scale(1);
  transition: all 0.2s ease-in-out;
}
.algType {
  display: flex;
  align-items: center;
  list-style: none;
  margin-bottom: 5px;
  li {
    display: flex;
    align-items: center;
    cursor: pointer;
    margin-right: 20px;
  }
}

.algList {
  border: 1px solid;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  list-style: none;
  margin-bottom: 5px;
  padding: 10px;
  li {
    display: flex;
    align-items: center;
    width: 25%;
    cursor: pointer;
    margin-bottom: 5px;
  }
}
</style>