<template>
  <div class="wechatSetting tabs-page">
    <div class="tabs">
      <ul>
        <li @click="tag = 'log'" :class="{ active: tag == 'log' }">Log Management</li>
        <li @click="tag = 'networkport'" :class="{ active: tag == 'networkport' }" v-if="role == 'admin'">Port Mapping</li>
        <li @click="tag = 'service'" :class="{ active: tag == 'service' }">System Status</li>
        <li @click="tag = 'security'" :class="{ active: tag == 'security' }">Security Settings</li>
        <!-- <li @click="tag = 'license'" :class="{ active: tag == 'license' }">
          license管理
        </li> -->
      </ul>
    </div>
    <div class="main">
      <log v-if="tag == 'log'"></log>
      <networkport v-if="tag == 'networkport'"></networkport>
      <service v-if="tag == 'service'"></service>
      <security v-if="tag == 'security'"></security>
    </div>
  </div>
</template>

<script>
import log from "./components/log.vue";
import networkport from "./components/networkport.vue"
import service from "./components/service.vue";
import security from "./components/security.vue"
import { mapGetters } from "vuex";
export default {
  components: {
    log,
    networkport,
    service,
    security
  },
  data() {
    return {
      tag: "log",
      role: localStorage.getItem('role')
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