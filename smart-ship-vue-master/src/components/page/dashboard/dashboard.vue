<template>
  <div class="dashboard" id="readAlarm" @click="handleReadAlarm">
    <div class="left-container">
      <div class="pannel chart-pannel">
        <div class="pannel-header">
          近30天告警趋势
          <div class="sel-devices-container">
            <div class="" @click="showLineChartDevices = !showLineChartDevices">
              选择设备
            </div>
            <ul v-show="showLineChartDevices">
              <li
                v-for="item in lineChartDevices"
                :key="item.device_id"
                :class="{ active: item.sel == true }"
                @click="handleLineChartSelChg(item)"
              >
                <span>{{ item.name || item.device_id }}</span>
                <i v-show="item.sel == true">√</i>
              </li>
            </ul>
          </div>
        </div>
        <div class="pannel-body">
          <div class="chart" id="alert"></div>
        </div>
      </div>
      <div class="pannel chart-pannel">
        <div class="pannel-header">
          近30天告警类型分布
          <div class="sel-devices-container">
            <div class="" @click="showBarChartDevices = !showBarChartDevices">
              选择设备
            </div>
            <ul v-show="showBarChartDevices">
              <li
                v-for="item in barChartDevices"
                :key="item.device_id"
                :class="{ active: item.sel == true }"
                @click="handleBarChartSelChg(item)"
              >
                <span>{{ item.name || item.device_id }}</span>
                <i v-show="item.sel == true">√</i>
              </li>
            </ul>
          </div>
        </div>
        <div class="pannel-body">
          <div class="chart" id="alertType"></div>
        </div>
      </div>
    </div>
    <div class="right-container">
      <div class="pannel">
        <div class="pannel-header">最新告警信息</div>
        <div class="pannel-body alarm-list">
          <ul>
            <li v-for="(item, index) in alertList" :key="index">
              <div class="info-list">
                <p>
                  <label>告警时间：</label><span>{{ item.alert_time }}</span>
                </p>
                <p>
                  <label>告警类型：</label><span>{{ item.alg_ch_name }}</span>
                </p>
                <p>
                  <label>设备ID：</label><span>{{ item.device_id }}</span>
                </p>
                <p>
                  <label>设备名称：</label><span>{{ item.device_name }}</span>
                </p>
              </div>
              <div class="image-container" @click="handleShowAlertDetail(item)">
                <img :src="item.imgpath" alt="" />
                <!-- <alarmpic :src="item.imgpath"></alarmpic> -->
                <!-- <alarmpic
                  :src="item.imgpath"
                  :alerts="item.alert"
                  :roi="item.roi"
                  :polys="item.polys"
                ></alarmpic> -->
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <el-dialog
      :title="
        '告警详情(' + showAlert.imgIndex + '/' + showAlert.imgList.length + ')'
      "
      :append-to-body="true"
      :visible.sync="showAlert.show"
      class="alarmDetail-dialog"
      v-dialogDrag
    >
      <ul class="alarm-infos">
        <li>
          <label for="">告警时间：</label
          ><span>{{ showAlert.alertInfos.alert_time }}</span>
        </li>
        <li>
          <label for="">告警类型：</label
          ><span>{{ showAlert.alertInfos.alg_ch_name }}</span>
        </li>
        <li>
          <label for="">告警设备ID：</label
          ><span>{{ showAlert.alertInfos.device_id }}</span>
        </li>
        <li>
          <label for="">设备名称：</label
          ><span>{{ showAlert.alertInfos.device_name }}</span>
        </li>
      </ul>
      <el-carousel
        trigger="click"
        indicator-position="none"
        @change="handleAlertpicChange"
      >
        <el-carousel-item
          v-for="(item, index) in showAlert.imgList"
          :key="index"
        >
          <alarmpic :src="item"></alarmpic>
          <!-- <alarmpic
            :src="item"
            :alerts="showAlert.alertList[index]"
            :roi="showAlert.roi"
            :polys="showAlert.polys"
          ></alarmpic> -->
        </el-carousel-item>
      </el-carousel>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from "echarts";
import bus from "@/utils/bus";
import { charts, alert, device } from "@/api/index.js";
import { projConfig } from "@/utils/config.js";
let synth = window.speechSynthesis; // 获取语音合成对象
let voices = null; // 创建一个变量用来存储要使用的语音库
let getVoiceInterval = null;

function getVoice() {
  let voicesList = synth.getVoices(); // 获取可用的语音库列表
  for (var i = 0; i < voicesList.length; i++) {
    //  是否是中文语音库
    if (voicesList[i].lang === "zh-CN") {
      voices = voicesList[i]; // 找到中文语音库就传给 voices
      return true;
    }
  }
  return false;
}
function readAlarm(alarmInfo) {
  // 如果没有中文语音库
  if (voices === null) {
    return false;
  }
  // 创建语音合成接口
  let utterThis = new SpeechSynthesisUtterance(alarmInfo);
  // utterThis.text = alarmInfo;
  utterThis.voice = voices; // 设置语音库
  utterThis.pitch = 1; // 设置音调
  utterThis.rate = 1; // 设置语速
  utterThis.volume = 1; // 设置语音音量为最大
  synth.speak(utterThis); // 开始朗读
}
export default {
  data() {
    return {
      charts: {
        alert: null,
        alertType: null,
      },
      alertType: null,
      alertList: [],
      alertData: [],
      alertTypeData: [
        { type: 3, num: 9 },
        { type: 6, num: 10 },
        { type: 7, num: 13 },
        { type: 10, num: 19 },
        { type: 11, num: 25 },
        { type: 13, num: 33 },
        { type: 14, num: 40 },
      ],

      showAlert: {
        show: false,
        imgIndex: 1,
        imgList: [],
        alertList: [],
        alertInfos: {},
        roi: null,
        polys: null,
      },
      lineOriData: null,
      barOriData: null,
      lineChartDevices: {},
      barChartDevices: {},
      showLineChartDevices: false,
      showBarChartDevices: false,
      alertInterval: null,
    };
  },
  created() {
    bus.$on("chgtheme", () => {
      this.initLineChart(this.charts.alert, this.alertData);
      this.initBarChart(this.charts.alertType, this.alertTypeData);
    });
  },
  beforeMount() {
    device.getTableData().then((res) => {
      res.forEach((item) => {
        this.lineChartDevices[item.device_id] = {
          id: item.id,
          device_id: item.device_id,
          name: item.device_name,
          sel: true,
        };
        this.barChartDevices[item.device_id] = {
          id: item.id,
          device_id: item.device_id,
          name: item.device_name,
          sel: true,
        };
      });
    });
  },
  mounted() {
    getVoiceInterval = setInterval(() => {
      if (getVoice()) {
        clearInterval(getVoiceInterval);
        getVoiceInterval = null;
        document.getElementById("readAlarm").click();
      }
    }, 1000);
    this.alertType = Object.assign(
      {},
      JSON.parse(JSON.stringify(projConfig.alertType))
    );
    this.initCharts();

    this.getTableData();
    this.getChartsData();
    this.alertInterval = setInterval(() => {
      this.getTableData();
      this.getChartsData();
    }, 5000);

    window.addEventListener("resize", this.chartsResize, false);
  },
  beforeDestroy() {
    clearInterval(this.alertInterval);
    window.removeEventListener("resize", this.chartsResize);
    clearInterval(getVoiceInterval);
  },
  methods: {
    handleReadAlarm() {
      readAlarm("");
    },
    dealLineChartData() {
      let dateSet = new Set();
      for (let key in this.lineOriData) {
        if (
          this.lineChartDevices[key] &&
          this.lineChartDevices[key].sel == true
        ) {
          this.lineOriData[key].times.forEach((time) => dateSet.add(time));
        }
      }
      this.alertData = [];
      dateSet = [...dateSet];
      for (let i = 0; i < dateSet.length; i++) {
        let temp = { time: dateSet[i], num: 0 };
        for (let key in this.lineOriData) {
          if (this.lineChartDevices[key].sel == true) {
            temp.num += this.lineOriData[key][temp.time] || 0;
          }
        }
        this.alertData.push(temp);
      }
      this.alertData.sort((a, b) => {
        return a.time - b.time;
      });
      this.initLineChart(this.charts.alert, this.alertData);
    },
    dealBarChartData() {
      let dateSet = new Set();
      for (let key in this.barOriData) {
        if (
          this.barChartDevices[key] &&
          this.barChartDevices[key].sel == true
        ) {
          this.barOriData[key].types.forEach((type) => dateSet.add(type));
        }
      }
      this.alertTypeData = [];
      dateSet = [...dateSet];
      for (let i = 0; i < dateSet.length; i++) {
        let temp = { type: dateSet[i], num: 0 };
        for (let key in this.barOriData) {
          if (this.barChartDevices[key].sel == true) {
            temp.num += this.barOriData[key][temp.type] || 0;
          }
        }
        this.alertTypeData.push(temp);
      }
      this.alertTypeData.sort((a, b) => a.num - b.num);
      this.initBarChart(this.charts.alertType, this.alertTypeData);
    },
    handleLineChartSelChg(item) {
      item.sel = !item.sel;
      this.dealLineChartData();
      this.$forceUpdate();
    },
    handleBarChartSelChg(item) {
      item.sel = !item.sel;
      this.dealBarChartData();
      this.$forceUpdate();
    },
    chartsResize() {
      this.charts.alert.resize();
      this.charts.alertType.resize();
    },
    getTableData() {
      alert
        .getTableData({
          page: 1,
          size: 10,
        })
        .then((res) => {
          if (this.alertList[0]) {
            if (res.data[0].alert_time != this.alertList[0].alert_time) {
              readAlarm(res.data[0].alg_ch_name + "报警");
            }
          }

          this.alertList = [...res.data].map((item) => {
            item.imgpath = projConfig.picprev + item.imgs[0];
            item.alert = JSON.parse(item.objs)[0];
            item.roi = JSON.parse(item.roi);
            item.polys = JSON.parse(item.polys);
            return item;
          });
        });
    },
    getChartsData() {
      charts.day().then((res) => {
        Object.keys(res.data).forEach((device_id) => {
          if (!this.lineChartDevices[device_id]) {
            this.lineChartDevices[device_id] = {
              id: device_id,
              device_id: device_id,
              name: device_id,
              sel: true,
            };
          }
        });
        Object.keys(res.data).forEach((item) => {
          let times = Object.keys(res.data[item]);
          res.data[item].times = times;
        });
        this.lineOriData = res.data;
        this.dealLineChartData();
      });
      charts.category().then((res) => {
        Object.keys(res.data).forEach((device_id) => {
          if (!this.barChartDevices[device_id]) {
            this.barChartDevices[device_id] = {
              id: device_id,
              device_id: device_id,
              name: device_id,
              sel: true,
            };
          }
        });
        Object.keys(res.data).forEach((item) => {
          let types = Object.keys(res.data[item]);
          res.data[item].types = types;
        });
        this.barOriData = res.data;
        this.dealBarChartData();
      });
    },
    initCharts() {
      this.charts.alert = echarts.init(document.querySelector("#alert"));
      this.charts.alertType = echarts.init(
        document.querySelector("#alertType")
      );
    },
    initLineChart(chart, data) {
      let textcolor = document
        .getElementsByTagName("body")[0]
        .style.getPropertyValue("--mainText");
      let chartTooltipBg = document
        .getElementsByTagName("body")[0]
        .style.getPropertyValue("--chartTooltipBg");
      let option = {
        textStyle: {
          color: textcolor,
        },
        grid: {
          top: 50,
          bottom: 80,
        },
        legend: {
          show: false,
          right: 0,
          top: "center",
          orient: "vertical",
          textStyle: {
            color: "#545454",
          },
          data: ["总数"],
        },
        tooltip: {
          show: true,
          trigger: "axis",
          backgroundColor: chartTooltipBg,
          borderWidth: 0,
          textStyle: {
            color: "#fff",
          },
          formatter: function (a) {
            return a[0].axisValue + "告警 : " + a[0].value;
          },
        },
        xAxis: {
          type: "category",
          data: data.map((item) => item.time),
          axisLabel: {
            rotate: 60,
          },
        },
        yAxis: {
          type: "value",
          minInterval: 1,
        },
        series: [
          {
            name: "告警数",
            type: "line",
            // areaStyle: {},
            data: data.map((item) => item.num),
          },
        ],
      };
      chart.setOption(option);
    },
    initBarChart(chart, data) {
      let textcolor = document
        .getElementsByTagName("body")[0]
        .style.getPropertyValue("--mainText");
      let chartTooltipBg = document
        .getElementsByTagName("body")[0]
        .style.getPropertyValue("--chartTooltipBg");
      let option = {
        textStyle: {
          color: textcolor,
        },
        // color: ["#386AC4", "#F57D2E", "#A9A9A9", "#ffff00"],
        grid: {
          left: 100,
          top: 50,
          bottom: 50,
        },
        tooltip: {
          trigger: "axis",
          formatter: function (a) {
            return a[0].axisValue + "告警 : " + a[0].value;
          },
          backgroundColor: chartTooltipBg,
          borderWidth: 0,
          textStyle: {
            color: "#fff",
          },
        },
        yAxis: [
          {
            type: "category",
            axisTick: { show: false },
            // axisLabel: {
            //   rotate: 45,
            // },
            data: data.map((item) => item.type),
          },
        ],
        xAxis: [
          {
            type: "value",
            axisTick: { show: false },
            minInterval: 1,
            axisLabel: {
              // color: "FFF"
            },
          },
        ],
        legend: {
          show: false,
          type: "scroll",
          // orient: "vertical",
          bottom: 10,
          data: data.legend,
          textStyle: {
            color: "#fff",
          },
        },
        series: [
          {
            data: data.map((item) => item.num),
            type: "bar",
            colorBy: "data",
            barWidth: 10,
            label: {
              show: true,
              position: "right",
              color: textcolor,
            },
          },
        ],
      };
      chart.setOption(option);
    },
    handleShowAlertDetail(item) {
      this.showAlert.show = true;
      this.showAlert.imgIndex = 1;
      this.showAlert.imgList = item.imgs.map((item) => {
        return projConfig.picprev + item;
      });
      this.showAlert.alertList = JSON.parse(item.objs);
      this.showAlert.alertInfos = item;
      this.showAlert.roi = item.roi;
      this.showAlert.polys = item.polys;
    },
    handleAlertpicChange(item) {
      this.showAlert.imgIndex = item + 1;
    },
  },
};
</script>

<style lang="less" scoped>
.dashboard {
  display: flex;
  justify-content: space-between;
}
.chart-pannel {
  .pannel-header {
    justify-content: space-between;
    padding: 0 30px;
    box-sizing: border-box;
    position: relative;
    z-index: 1;
  }
}
.left-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: calc((100% - 8rem) / 2);
  height: 100%;

  .pannel {
    height: calc((100% - 8rem) / 2);
    width: 100%;
  }
}
.right-container {
  width: calc((100% - 8rem) / 2);
  height: 100%;
  .pannel {
    height: 100%;
    width: 100%;
  }
}

.alarm-list {
  overflow: auto;
  // padding: 10rem 0;
  box-sizing: border-box;
  height: calc(100% - 50rem) !important;
  ul {
    width: calc(100% - 20rem);
    margin-left: 10rem;
    border-top: 1px solid @mainText;
    padding-top: 2px;
    overflow: auto;
    li {
      // height: 200px;
      border-bottom: 1px solid @mainText;
      line-height: 2;
      padding: 8rem 10rem;
      display: flex;
      justify-content: space-between;
      color: @mainText;
      font-size: 14rem;
      .info-list {
        p{
          display: flex;
        }
        label {
          color: @menuActiveText;
          // font-weight: bold;
          width: 70px;
        }
      }
    }
  }
}

.alarm-infos {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
  li {
    width: 50%;
    label {
      color: @menuActiveText;
    }
  }
}
.image-container {
  width: 27%;
  display: flex;
  align-items: center;
  img {
    max-width: 100%;
  }
}

.sel-devices-container {
  > div {
    height: 30px;
    width: 110px;
    background-color: @selDropdownBg;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 4px;
    margin-top: 15px;
    font-size: 14px;
    cursor: pointer;
  }
  ul {
    position: absolute;
    // width: 110px;
    right: 30px;
    margin-top: 4px;
    max-height: 30vh;
    overflow: auto;
    li {
      height: 26px;
      line-height: 26px;
      cursor: pointer;
      font-size: 12px;
      display: flex;
      justify-content: space-between;
      margin-top: 4px;
      background-color: @selDropdownBg;
      padding: 0 10px;
      box-sizing: border-box;
      min-width: 110px;
      span {
        display: block;
        // width: 80px;
        margin-right: 10px;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
        word-break: break-all;
      }
    }
    li.active {
      color: @menuActiveText;
    }
  }
}
</style>

<style lang="less" scoped></style>