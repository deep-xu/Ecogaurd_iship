<template>
  <div class="wechatSetting tabs-page">
    <div class="tabs">
      <ul>
        <li @click="tag = 'alarm'" :class="{ active: tag == 'alarm' }">
          History
        </li>
        <!-- <li @click="tag = 'crosslinecount'" :class="{ active: tag == 'crosslinecount' }">
          Cross Line Count
        </li> -->
      </ul>
    </div>
    <div class="main">
      <alarm v-if="tag == 'alarm'"></alarm>
      <!-- <crosslinecount v-if="tag == 'crosslinecount'"></crosslinecount> -->
    </div>
  </div>
</template>
  
  <script>
import alarm from "./components/alarm.vue";
// import crosslinecount from "./components/crosslinecount.vue";
import { mapGetters } from "vuex";
export default {
  components: {
    alarm,
    // crosslinecount
  },
  data() {
    return {
      tag: "alarm",
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