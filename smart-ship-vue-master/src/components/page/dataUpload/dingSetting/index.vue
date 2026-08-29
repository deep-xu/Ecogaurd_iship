<template>
  <div class="wechatSetting tabs-page">
    <div class="tabs">
      <ul>
        <li
          @click="tag = 'dinguser'"
          :class="{ active: tag == 'dinguser' }"
        >
          推送目标
        </li>
        <li
          @click="tag = 'settings'"
          :class="{ active: tag == 'settings' }"
          v-show="userRole == 'admin'"
        >
          高级设置
        </li>
      </ul>
    </div>
    <div class="main">
      <dinguser v-show="tag == 'dinguser'"></dinguser>
      <settings v-show="tag == 'settings'"></settings>
    </div>
  </div>
</template>

<script>
import dinguser from "./components/dinguser.vue";
import settings from "./components/settings.vue";
import { mapGetters } from "vuex";
export default {
  components: {
    dinguser,
    settings,
  },
  data() {
    return {
      tag: "dinguser",
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