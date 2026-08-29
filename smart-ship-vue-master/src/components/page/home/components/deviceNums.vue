<template>
  <div class="devicesnums">
    <div class="types">
      <div :class="{active: type == 'device'}" @click="type = 'device'">Device</div>
      <div :class="{active: type == 'source'}" @click="type = 'source'">Source</div>
    </div>
    <div class="container">
      <div class="icon-container">
        <img v-if="type == 'device'" src="../../home/imgs/home-device.png" alt="">
        <img v-else src="../../home/imgs/home-camera.png" alt="">
      </div>
      <div class="onliner-container">
        <div class="num">{{type == 'device' ? deviceObj.deviceOnlineTotal : deviceObj.sourceStat.Enable}}</div>
        <div>{{type == 'device' ? "Online" : "Enable"}} Quantity</div>
      </div>
      <div class="chart-container" ref="piechart"></div>
      <div class="offline-container">
        <div class="num">{{type == 'device' ? deviceObj.deviceOfflineTotal : deviceObj.sourceStat.Disable}}</div>
        <div>{{type == 'device' ? "Offline" : "Disable"}} Quantity</div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";
export default {
  props: ["deviceObj"],
  data(){
    return {
      piechart:null,
      type:"device",
      device:{
        online:0,
        offline:0
      },
      source:{
        online:0,
        offline:0
      }
    }
  },
  watch: {
    type(){
      this.initPieChart()
    },
    deviceObj:{
      deep:true,
      handler(){
        this.calcNums();
      }
    }
  },
  mounted(){
    this.piechart = echarts.init(this.$refs.piechart);
    this.calcNums();
  },
  methods:{
    calcNums(){
      this.device.online = 0
      this.device.offline = 0
      this.source.online = 0;
      this.source.offline = 0;
      for(let device_id in this.$props.deviceObj){
        if(this.$props.deviceObj[device_id].status == 1){
          this.device.online++;
        }else{
          this.device.offline++;
        }
        if(this.$props.deviceObj[device_id].source){
          if(this.$props.deviceObj[device_id].status == 1){
            let source = Object.values(this.$props.deviceObj[device_id].source);
            this.source.online = this.source.online + source.filter(item => item.status == 1).length;
            this.source.offline = this.source.offline + source.filter(item => item.status != 1).length;
          }else{
            let source = Object.values(this.$props.deviceObj[device_id].source);
            this.source.offline = this.source.offline + source.length;
          }
          
        }
        
      }
      this.initPieChart()
    },
    initPieChart() {
      let data = [];
      if(this.type == 'device'){
        data = [
          {type: 'Online',value:this.device.online},
          {type: 'Offline',value:this.device.offline},
        ]
      }else{
        data = [
          {type: 'Online',value:this.source.online},
          {type: 'Offline',value:this.source.offline},
        ]
      }
      let option = {
        tooltip: {
          show:false,
          trigger: "item",
          // formatter: function (a) {
          //   return a[0].axisValue + "告警 : " + a[0].value;
          // },
          // backgroundColor: this.chartColor.tooltipbg,
          borderWidth: 0,
          textStyle: {
            color: "#fff",
          },
        },
        legend: {
          show:false,
          orient: "vertical",
          top: "middle",
          left: window.innerWidth > 1600 ? '50%' : '40%',
          itemGap: 3,
          textStyle: {
            // color: this.chartColor.label,
          },
        },
        series: [
          {
            color:["#309af1","#536170"],
            type: "pie",
            center: ["50%", "50%"],
            radius: ["70%", "82%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 2,
              borderColor: "#040d2e",
              borderWidth: 0,
            },
            label: {
              show: false,
              position: "center",
            },
            labelLine: {
              show: false,
            },
            data: data.map((item) => {
              return {
                value: item.value,
                name: item.type,
                // label: {
                //   show: false,
                //   color: "#fff",
                //   position: "inside",
                //   formatter: (data) => {
                //     return data.value + "\n" + data.percent + "%";
                //   },
                // },
              };
            }),
          },
        ],
      };
      this.piechart.setOption(option);
    },
  }
};
</script>

<style lang="less" scoped>
.devicesnums{
  position: relative;
  width: 100%;
  height: 100%;
  padding-top: 8rem;
  box-sizing: border-box;
  .types{
    height: 32rem;
    line-height: 32rem;
    font-size: 14rem;
    display: flex;
    padding-left: 10rem;
    box-sizing: border-box;
    >div{
      border: 1px solid #053c75;
      padding: 0 10rem;
      color: #9ecaf7;
      cursor: pointer;
    }
    >div.active{
      background-color: #053c75;
    }
  }
  .container{
    width: 100%;
    height: calc(100% - 40rem);
    background: url(../imgs/home-tongji-bg.png) no-repeat;
    background-size: contain;
    background-position: center;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    >div{
      width: calc(100% / 3);
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .icon-container{
      position: absolute;
      width: calc(100% / 3);
      img{
        width: calc(100% - 30rem);
      }
    }
    .chart-container{
      height: 100%;
    }
    .onliner-container,.offline-container{
      height: 110rem;
      display: flex;
      flex-wrap: wrap;
      font-size: 12rem;
      justify-content: center;
      align-items: center;
      color: #9ecaf7;
      .num{
        font-size: 24rem;
      }
      div{
        width: 100%;
        text-align: center;
      }
      // div:nth-child(1){
      //   margin-bottom: 50rem;
      // }
    }
  }
}
</style>