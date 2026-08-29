<template>
  <div class="wechatSetting tabs-page">
    <div class="tabs">
      <ul>
        <li @click="tag = 'hands'" :class="{ active: tag == 'hands' }">
          手部
        </li>
        <li @click="tag = 'feet'" :class="{ active: tag == 'feet' }">
          脚部
        </li>
        <li @click="tag = 'eyes'" :class="{ active: tag == 'eyes' }">
          眼部
        </li>
      </ul>
    </div>
    <div class="main">
      <ppelist :category="tag" ></ppelist>
    </div>
  </div>
</template>
  
<script>
import ppelist from "./ppelist.vue";
import { mapGetters } from "vuex";
export default {
  components: {
    ppelist
  },
  data() {
    return {
      tag: "hands",
    };
  },
  computed: {
    ...mapGetters("app", {
      userRole: "userRole",
    }),
  },
  methods: {},
};
</script>
  
<style>
</style>