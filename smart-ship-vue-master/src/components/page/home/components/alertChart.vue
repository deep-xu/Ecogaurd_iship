<template>
  <div class="alertchart-container">
    <div class="nums">
      <div>
        <span class="span1">{{ todayCount }}</span>
        <span class="span2">Alarm Today</span>
      </div>
    </div>
    <div class="device-containainer">
      <el-select
        v-model="device_id"
        clearable=""
        popper-class="home-device-sel"
        placeholder="Please Choose"
        default-first-option
      >
        <el-option
          v-for="item in deviceNameList"
          :key="item.device_id"
          :label="item.name"
          :value="item.device_id"
        >
        </el-option>
      </el-select>
    </div>
    <div class="chart" ref="alertChart" id="alertChart"></div>
  </div>
</template>

<script>
import * as echarts from "echarts";
import { deviceV2Apis } from "@/api/device.js";
export default {
  props: ["devices"],
  data() {
    return {
      device_id: "",
      alertChart: null,
      todayCount: "",
      alertData: [],
      lineChartDevices: [],
      deviceNameList: [],

      refreshInterval: null
    };
  },
  watch: {
    devices: {
      deep: true,
      handler(val) {
        for (let key in val) {
          this.device_id = key;
          this.todayCount = 0;
          const today = new Date().format("MM-dd");
          for (const ins of val[key]) {
            if (new Date(ins.date).format("MM-dd") === today) {
              this.todayCount = ins.value;
              break;
            }
          }
          break;
        }
      },
    },
    device_id(val) {
      this.initLineChart(this.devices[val]);
    },
  },
  async mounted() {
    const { rows } = await deviceV2Apis.getTableList();
    this.deviceNameList = rows.map(item => ({ device_id: item.id, name: item.name }));
    this.alertChart = echarts.init(document.querySelector("#alertChart"));
    // this.getAlertData();
    // this.getAlertChartData();
    // this.getDevices();
    // this.refreshInterval = setInterval(() => {
    //   this.getAlertData();
    //   this.getAlertChartData();
    // }, 10000)
  },
  beforeDestroy(){
    clearInterval(this.refreshInterval);
    this.refreshInterval = null
  },
  methods: {
    getDevices() {
      this.deviceNameList = Object.values(this.$props.devices).map((item) => {
        return {
          device_id: item.device_id,
          name: item.name || item.device_id,
        };
      });
    },
    initLineChart(data) {
      let option = {
        textStyle: {
          color: "#fff",
        },
        grid: {
          top: 50,
          bottom: 40,
          right: 20,
        },
        legend: {
          show: false,
          right: 0,
          top: "center",
          orient: "vertical",
          textStyle: {
            color: "#545454",
          },
          data: ["Total"],
        },
        tooltip: {
          show: true,
          trigger: "axis",
          backgroundColor: "rgb(0,0,0,0.5)",
          borderWidth: 0,
          textStyle: {
            color: "#fff",
          },
          formatter: function (a) {
            return a[0].axisValue + " Alarm : " + a[0].value;
          },
        },
        xAxis: {
          type: "category",
          data: data.map((item) => new Date(item.date).format("MM-DD")),
          axisLabel: {
            rotate: 45,
          },
          axisLine: {
            lineStyle: {
              color: "rgba(45,108,175,1)",
            },
          },
          axisTick: {
            show: false,
          },
        },
        yAxis: {
          type: "value",
          minInterval: 1,
          splitLine: {
            show: true,
            lineStyle: {
              color: "rgba(45,108,175,0.5)",
            },
          },
        },
        series: [
          {
            name: "Number of Alarms",
            type: "line",
            data: data.map((item) => item.value),
            smooth: true,
            areaStyle: {
              opacity: 0.8,
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: "rgba(6,61,121, 1)",
                },
                {
                  offset: 1,
                  color: "rgba(6,61,121,0.3)",
                },
              ]),
            },
          },
        ],
      };
      this.alertChart.setOption(option);
    },
    getAlertData() {
      let params = {
        start_time: new Date(new Date().format("YYYY-MM-DD 00:00:00")).getTime() / 1000
      }
      if (this.device_id) {
        params.device_id = this.device_id;
      }
      alertApis
        .count(params)
        .then((res) => {
          this.todayCount = res.data;
        });
    },
    getAlertChartData() {
      let params = {
        group_by: "alert_time",
        start_time:
          new Date(new Date().format("YYYY-MM-DD 00:00:00")).getTime() / 1000 -
          60 * 60 * 24 * 15,
      };
      if (this.device_id) {
        params.device_id = this.device_id;
      }
      alertApis.count(params).then((res) => {
        if (Object.keys(res).length > 0) {
          let chartData = Object.keys(res).map((date) => {
            return {
              date: date,
              value: res[date],
            };
          });
          this.initLineChart(chartData);
          // this.initLineChart([
          //   { date: "12-05", value: 10 },
          //   { date: "12-06", value: 20 },
          //   { date: "12-07", value: 10 },
          //   { date: "12-08", value: 20 },
          //   { date: "12-09", value: 10 },
          // ]);
        } else {
          let chartData = [];
          this.initLineChart(chartData);
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
.alertchart-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-evenly;
  position: relative;
  .nums {
    width: 20%;
    height: 100%;
    position: relative;

    div {
      display: flex;
      flex-direction: column;
      // justify-content: center;
      align-items: center;
      color: #00e2fd;
      position: absolute;
      bottom: 60%;
      width: 100%;
      .span1 {
        font-size: 36rem;
      }
      .span2 {
        font-size: 18rem;
      }
    }
  }
  .chart {
    width: calc(80% - 60rem);
    height: 100%;
  }
  .nums::before {
    content: "";
    width: calc(100% - 0rem);
    height: calc(60%);
    position: absolute;
    bottom: 0rem;
    left: 0rem;
    background: url(../imgs/alarm-bg.png) no-repeat;
    background-size: 100% 100%;
  }

  .device-containainer {
    position: absolute;
    top: 10rem;
    right: 10rem;
    z-index: 1;
    .el-select {
      /deep/.el-input__inner {
        background-color: #063e79;
        border-color: #063e79;
        color: #00def9;
        height: 32rem !important;
        line-height: 32rem;
        font-size: 14rem;
        padding: 0 30rem 0 15rem;
      }
      /deep/.el-input__inner::placeholder {
        color: rgb(0 222 249 / 50%);
      }
      /deep/.el-input__suffix {
        height: 32rem;
        line-height: 32rem;
        i {
          color: #00def9;
        }
      }
      /deep/.el-input__suffix {
        height: 32rem;
        line-height: 32rem;
        width: 20rem;
        .el-select__caret {
          height: 32rem;
          line-height: 32rem;
          font-size: 14rem;
          width: 20rem;
        }
      }
    }
  }
}
</style>
<style lang="less">
.home-device-sel.el-select-dropdown.el-popper {
  background-color: #063e79;
  border-color: #063e79;
  .popper__arrow,
  .popper__arrow::after {
    border-bottom-color: #063e79;
    border-top-color: #063e79;
  }
  .el-select-dropdown__item {
    color: #fff;
    margin-bottom: 2rem;
    padding: 0 15rem;
    height: 32rem;
    line-height: 32rem;
  }
  .el-select-dropdown__item.hover,
  .el-select-dropdown__item:hover,
  .el-select-dropdown__item.selected {
    color: #00def9;
    background-color: rgb(4 10 16 / 46%);
  }
}
</style>