<template>
  <div class="wechatSetting tabs-page">
    <div class="tabs">
      <ul>
        <li @click="tag = 'sysupgrade'" :class="{ active: tag == 'sysupgrade' }">
          Platform Upgrade
        </li>
        
      </ul>
    </div>
    <div class="main">
      <sysupgrade v-show="tag == 'sysupgrade'"></sysupgrade>
    </div>
  </div>
</template>

<script>
import sysupgrade from "./components/sysupgrade.vue";
import { mapGetters } from "vuex";
export default {
  components: {
    sysupgrade,
  },
  data() {
    return {
      tag: "sysupgrade",
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