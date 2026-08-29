<template>
  <div class="settings">
    <el-form
      :model="formData"
      ref="formData"
      :rules="formRule"
      :label-width="labelWidth"
    >
      <!-- <div class="sub-title">
        <h3>下载日志</h3>
      </div> -->
      <el-button class="btn-download" type="primary" @click="handleDownload" :loading="loading"
        >Download Logs</el-button
      >
    </el-form>
  </div>
</template>

<script>
import { log } from "@/api/system.js";
import { projConfig } from "@/utils/config.js";
export default {
  data() {
    return {
      api: log,
      loading: false,
      oriData: {
        level: null,
        retention: "",
      },
      formData: {
        level: null,
        retention: "",
      },
      formRule: {},
      labelWidth: "140px",
    };
  },
  mounted() {},
  methods: {
    handleDownload() {
      this.loading = true
      this.api.download().then((res) => {
        this.loading = false;
        if (res instanceof Blob) {
          this.$message({
            type: 'success',
            message:'下载成功'
          })
          var blob = new Blob([res]);
          var url = URL.createObjectURL(blob);
          var a = document.createElement("a");
          a.href = url;
          a.download = "log.zip"; // 设置下载的文件名
          a.click();
          window.URL.revokeObjectURL(url);
          
        }else{
          this.$message({
            type: 'error',
            message:'下载失败'
          })
        }
      });
    },
    download(filename, link) {
      let DownloadLink = document.createElement("a");
      DownloadLink.style = "display: none"; // 创建一个隐藏的a标签
      DownloadLink.download = filename;
      DownloadLink.href = link;
      document.body.appendChild(DownloadLink);
      DownloadLink.click(); // 触发a标签的click事件
      document.body.removeChild(DownloadLink);
    },
  },
};
</script>

<style lang="less" scoped>
.settings {
  padding-top: 30rem;
}

.btn-download {
  margin-left: 70rem;
}
</style>