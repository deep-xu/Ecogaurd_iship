<template>
  <div class="plan-container">
    <h3 style="margin-bottom: 5px; padding-left: 2px">布控计划</h3>
    <div
      style="
        width: 100%;
        height: calc(100% - 90px);
        overflow: auto;
        margin-bottom: 60px;
      "
    >
      <ul v-for="(item, key) in plan" :key="key">
        <li>
          <label :class="{ error: key == errInfo.day }">{{
            getDay(key)
          }}</label>
          <p
            v-for="(timerange, index) in item"
            :key="key + index"
            :class="{
              error:
                key == errInfo.day && errInfo.repeatIndex.indexOf(index) >= 0,
            }"
          >
            <el-time-picker
              is-range
              v-model="item[index]"
              format="HH:mm:ss"
              placeholder="选择时间"
              style="width: 180px"
              @change="handleCheckRepeat"
            ></el-time-picker>
            <i
              v-if="item.length > 1"
              class="z-iconfont z-icon-close"
              @click="handleDelPlan(item, index)"
            ></i>
            <i
              v-if="index == 0"
              class="z-iconfont z-icon-add"
              @click="handleAddPlan(item)"
            ></i>
          </p>
        </li>
      </ul>
    </div>

    <el-button class="btn-finish" type="primary" @click="handleFinish"
      >完成</el-button
    >
  </div>
</template>

<script>
export default {
  props: ["planInfo"],
  data() {
    return {
      plan: {
        1: [],
        2: [],
        3: [],
        4: [],
        5: [],
        6: [],
        7: [],
      },
      errInfo: {
        day: "",
        repeatIndex: [],
      },
    };
  },
  mounted() {
    // this.plan = JSON.parse(JSON.stringify(this.$props.planInfo.plan))
    // console.log(this.$props.planInfo)
    Object.keys(this.plan).forEach((day) => {
      let plan = this.$props.planInfo.plan[day];
      if (plan.length == 0) {
        plan.push(["", ""]);
      }
      this.plan[day] = plan.map((item) => {
        // return [new Date(),new Date()]
        return this.getTime(item).map(time => new Date("2023-12-19 " + time));
      });
      
      this.$forceUpdate()
    });
  },
  methods: {
    getDay(key) {
      let day = [
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
        "星期日",
      ];
      return day[key - 1];
    },
    getTime(time) {
      if (!time[0] && !time[1]) {
        return ["", ""];
      }
      let tmp = time.map((item) => {
        let hour = parseInt(item / 3600);
        let minute = parseInt((item % 3600) / 60);
        let second = parseInt((item % 3600) % 60);
        hour = hour < 10 ? `0${hour}` : hour;
        minute = minute < 10 ? `0${minute}` : minute;
        second = second < 10 ? `0${second}` : second;
        return `${hour}:${minute}:${second}`;
      });
      return tmp;
    },
    timeToNum(time) {
      console.log(time)
      let timeArr = time.split(":");
      let second = timeArr[2] ? parseInt(timeArr[2]) : 0;
      return parseInt(timeArr[0]) * 3600 + parseInt(timeArr[1]) * 60 + second;
    },
    handleAddPlan(item) {
      item.push([new Date("2023-12-19 00:00:00"), new Date("2023-12-19 23:59:59")]);
      this.$forceUpdate();
    },
    handleDelPlan(item, index) {
      item.splice(index, 1);
      this.$forceUpdate();
    },
    handleCutPlan(item, index) {
      item.splice(index, 1);
      this.$forceUpdate();
    },
    handleCheckRepeat() {
      if (this.isRepeat()) {
        this.$message({
          type:"error",
          message:"选择了重叠的时间范围，请检查"
        })
      }
    },
    isRepeat() {
      let repeat = false;
      let days = Object.keys(this.plan);
      for (let dayIndex = 0; dayIndex < days.length; dayIndex++) {
        let day = days[dayIndex];
        let timeArr = this.plan[day];
        if (timeArr.length > 1) {
          for (let i = 0; i < timeArr.length - 1; i++) {
            for (let j = i + 1; j < timeArr.length; j++) {
              if (
                (this.timeToNum(new Date(timeArr[j][0]).format("HH:mm:ss")) >=
                  this.timeToNum(new Date(timeArr[i][0]).format("HH:mm:ss")) &&
                  this.timeToNum(new Date(timeArr[j][0]).format("HH:mm:ss")) <=
                    this.timeToNum(new Date(timeArr[i][1]).format("HH:mm:ss"))) ||
                (this.timeToNum(new Date(timeArr[j][1]).format("HH:mm:ss")) >=
                  this.timeToNum(new Date(timeArr[i][0]).format("HH:mm:ss")) &&
                  this.timeToNum(new Date(timeArr[j][1]).format("HH:mm:ss")) <=
                    this.timeToNum(new Date(timeArr[i][1]).format("HH:mm:ss")))
              ) {
                repeat = true;
                this.errInfo.day = day;
                this.errInfo.repeatIndex = [i, j];
                console.log(this.errInfo);
                this.$forceUpdate();
                return repeat;
              }
            }
          }
        }
      }

      if (!repeat) {
        this.errInfo.day = "";
        this.errInfo.repeatIndex = [];
      }

      return repeat;
    },
    handleFinish() {
      if (this.isRepeat()) {
        this.$message({
          type:"error",
          message:"选择了重叠的时间范围，请检查"
        })
        return;
      }
      let plan = {};
      Object.keys(this.plan).forEach((day) => {
        plan[day] = this.plan[day]
          .filter((item) => item[0])
          .map((timerange) => {
            return [this.timeToNum(new Date(timerange[0]).format("HH:mm:ss")), this.timeToNum(new Date(timerange[1]).format("HH:mm:ss"))];
          });
      });
      let planInfo = {
        type: this.$props.planInfo.type,
        plan: plan,
      };
      this.$emit("planclose", planInfo);
    },
  },
};
</script>

<style lang="less" scoped>
.plan-container {
  width: 100%;
  height: 65vh;
  overflow: auto;
  position: relative;
}
ul {
  list-style: none;
  li {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 5px;
    padding-left: 2px;
    label {
      width: 100%;
      margin-bottom: 5px;
    }
    label.error {
      color: #ff1818;
    }
    p {
      width: 80%;
      margin-bottom: 5px;
      display: flex;
      align-items: center;
      position: relative;
      .z-icon-add {
        position: absolute;
        left: 210px;
        cursor: pointer;
      }
      .z-icon-close {
        cursor: pointer;
        position: absolute;
        left:180px;
        top: 5px;
        width: 20px;
      }
    }
  }
}

.btn-finish {
  position: absolute;
  bottom: 10px;
  right: 10px;
}
</style>