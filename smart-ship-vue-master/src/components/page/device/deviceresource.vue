<template>
  <div class="chart-container" v-loading="chartLoading" >
    <div class="chart-item">
      <h3 class="title">CPU使用概况</h3>
      <div id="cpuPie" style="width: 95%; height: 300px; margin: 0 auto;"></div>
    </div>
    <div class="chart-item">
      <h3 class="title">内存使用概况</h3>
      <div id="memPie" style="width: 95%; height: 300px; margin: 0 auto;"></div>
    </div>
    <div class="chart-item">
      <h3 class="title">存储空间使用概况</h3>
      <div id="diskPie" style="width: 95%; height: 300px; margin: 0 auto;"></div>
    </div>
  </div>
</template>
  <script>
import * as echarts from "echarts";
import { deviceApis } from "@/api/device.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["device_id"],
  data() {
    return {
      chartLoading: false,
      charts: {
        cpuPie: null,
        memPie: null,
        diskPie: null,
      },
    };
  },
  mounted() {
    this.initChart();
    this.getResource();
  },
  methods: {
    initChart() {
      this.charts.cpuPie = echarts.init(document.getElementById("cpuPie"));
      this.charts.memPie = echarts.init(document.getElementById("memPie"));
      this.charts.diskPie = echarts.init(document.getElementById("diskPie"));
    },
    getResource() {
      this.chartLoading = true;
      deviceApis
        .resource({ device_id: this.$props.device_id })
        .then((data) => {
          this.chartLoading = false;
          this.initPie(
            "CPU使用概况",
            parseFloat(data.cpu_percent),
            this.charts.cpuPie
          );
          this.initPie(
            "内存使用概况",
            parseFloat(data.memory_percent),
            this.charts.memPie
          );
          this.initPie(
            "存储空间使用概况",
            parseFloat(data.disk_percent),
            this.charts.diskPie
          );
        })
        .catch((error) => {
          this.chartLoading = false;

          if(error && error.message && error.message[this.lang]){
            this.$message({
              type: "error",
              message: error.message[this.lang],
            });
          }
          
        });
    },
    initPie(title, data, chart) {
      let used = data ? data : 0;
      let option = {
        // title: {
        //   text: title,
        //   left: "center",

        //   textStyle: {
        //     color: "#898C94",
        //   },
        // },
        tooltip: {
          trigger: "item",
          formatter: function (val) {
            // console.log(a,b,c)
            return val.percent + "%";
          },
        },
        legend: {
          show: false,
          orient: "vertical",
          right: 30,
          top: "middle",
          textStyle: {
            color: "#898C94",
          },
        },
        series: [
          {
            type: "pie",
            radius: "50%",
            center: ["50%", "50%"],
            data: [
              { value: used, name: "已使用" },
              { value: 100 - used, name: "未使用" },
            ],

            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(255, 255, 255, 0.5)",
              },
            },
          },
        ],
      };
      chart.setOption(option);
    },
  },
};
</script>
  <style lang="less" scoped>
.chart-container {
  display: flex;
  height: 320px; 
  position: relative;
  .chart-item {
    width: calc(100% / 3);
    .title {
      text-align: center;
      font-size: 16rem;
    }
  }
}
</style>