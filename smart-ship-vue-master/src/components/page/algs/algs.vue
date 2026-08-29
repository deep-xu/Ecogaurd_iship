<template>
  <div class="algs tabs-page">
    <div class="tabs">
      <ul>
        <li @click="tag = '人员管理'" :class="{ active: tag == '人员管理' }">
          人员管理
        </li>
        <li @click="tag = '车辆管理'" :class="{ active: tag == '车辆管理' }">
          车辆管理
        </li>
        <li @click="tag = '险情防控'" :class="{ active: tag == '险情防控' }">
          险情防控
        </li>
        <li @click="tag = '行业定制'" :class="{ active: tag == '行业定制' }">
          行业定制
        </li>
      </ul>
      <el-button title="算法导入" class="import" type="primary" @click="upload.show = true"
        >算法导入</el-button
      >
    </div>
    <div class="main">
      <algList :type="tag"></algList>
    </div>
    <el-dialog
      title="上传算法"
      :append-to-body="true"
      :visible.sync="upload.show"
      class="alarmDetail-dialog"
      v-dialogDrag
    >
      <el-upload
        class="upload-demo"
        drag
        action="#"
        accept=".zip"
        :on-change="handleUpload"
        :auto-upload="false"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或点击上传</div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script>
import algList from "./algList.vue";
import { mapGetters } from "vuex";
export default {
  components: {
    algList,
  },
  data() {
    return {
      tag: "人员管理",
      upload: {
        show: false,
      },
    };
  },
  computed: {
    ...mapGetters("app", {
      userRole: "userRole",
    }),
  },
  methods: {
    handleUpload() {},
  },
};
</script>

<style lang="less" scoped>
.import {
  position: absolute;
  top: -5rem;
  right: 0;
}
</style>