<template>
  <div class="wechatSetting tabs-page">
    <div class="tabs">
      <ul>
        <li @click="tag = 'settings'" :class="{active: tag == 'settings'}" >数据推送</li>
      </ul>
    </div>
    <div class="main">
      <settings v-show="tag == 'settings'"></settings>
    </div>
  </div>
</template>

<script>
import settings from "./components/settings.vue"
import { mapGetters } from "vuex";
export default {
  components:{
    settings
  },
  data(){
    return {
      tag: "settings"
    }
  },
  computed: {
    ...mapGetters("app", {
      userRole: "userRole",
    }),
  },
  methods:{
  }
};
</script>

<style>
</style>