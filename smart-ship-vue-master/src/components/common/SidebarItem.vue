<template>
  <div v-if="item.children" class="menu-item">
    <template v-if="item.children.length == 0">
      <el-menu-item :index="item.index" :key="item.index" :disabled="item.disabled == true">
        <i class="menu-icon" :class="item.icon"></i>
        <span slot="title" class="sypos">{{ item.title }}</span>
      </el-menu-item>
    </template>

    <template v-else>
      <el-submenu :index="item.index" :key="item.index" :disabled="item.disabled == true">
        <template slot="title">
          <i class="menu-icon" :class="item.icon"></i>
          <span slot="title">{{ item.title }}</span>
        </template>

        <template v-for="child in item.children">
          <sidebar-item
            v-if="child.children && child.children.length > 0"
            :item="child"
            :key="child.index + Math.random()"
          />
          <el-menu-item v-else :key="child.index" :index="child.index" :disabled="child.disabled == true">
            <i class="menu-icon" :class="child.icon"></i>
            <span slot="title">{{ child.title }}</span>
          </el-menu-item>
        </template>
      </el-submenu>
    </template>
  </div>
  <div v-else class="menu-item">
    <template>
      <el-menu-item :index="item.index" :key="item.index"  :disabled="item.disabled == true">
        <i class="menu-icon" :class="item.icon"></i>
        <span slot="title">{{ item.title }}</span>
      </el-menu-item>
    </template>
  </div>
</template>

<script>
import bus from "@/utils/bus";
export default {
  name: "SidebarItem",
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
  created() {
    // 通过 Event Bus 进行组件间通信，来折叠侧边栏
    bus.$on("collapse", (msg) => {
      this.collapse = msg;
    });
  },
  mounted(){
  }
};
</script>
<style scoped>
</style>