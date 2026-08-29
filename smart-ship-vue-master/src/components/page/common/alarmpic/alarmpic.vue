<template>
  <div class="alarmPic" ref="container">
    <img :src="src" ref="image" />
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script>
export default {
  name: "alarmpic",
  props: ["src", "alerts", "roi", "polys"],
  data() {
    return {
      imgInfo: {
        width: null, // 图片宽
        height: null, // 图片高
        canvasWidth: null, // 画布宽
        canvasHeight: null, // 画布高
      },
    };
  },
  watch: {
    src() {
      this.imageChange();
    },
  },
  mounted() {
    this.imageChange();
  },
  methods: {
    imageChange() {
      var img = document.createElement("img");
      img.src = this.$props.src;
      img.onload = () => {
        this.imgInfo = Object.assign(this.imgInfo, this.getimgInfo(img));
        // 画框
        this.setAlerts();
        // roi
        this.setRoi();
        // polys
        this.setPolys();
      };
    },
    // 获取图片宽高，设置canvas宽高
    getimgInfo(img) {
      const canvas = this.$refs["canvas"]; // width: 768 height: 768*imgheight/imgwidth
      const container = this.$refs["container"];
      const image = this.$refs["image"];
      let top = 0,
        left = 0;
      let imgInfo = {
        width: img.width,
        height: img.height,
        canvasWidth: 0,
        canvasHeight: 0,
      };
      if (
        img.width / container.offsetWidth >
        img.height / container.offsetHeight
      ) {
        imgInfo.canvasWidth = Math.floor(container.offsetWidth);
        imgInfo.canvasHeight = Math.floor(
          container.offsetWidth / (img.width / img.height)
        );
        top = Math.floor(container.offsetHeight - imgInfo.canvasHeight) / 2;
      } else {
        imgInfo.canvasHeight = Math.floor(container.offsetHeight);
        imgInfo.canvasWidth = Math.floor(
          container.offsetHeight * (img.width / img.height)
        );
        left = Math.floor(container.offsetWidth - imgInfo.canvasWidth) / 2;
      }

      image.style.position = "absolute";
      image.style.width = imgInfo.canvasWidth + "px";
      image.style.height = imgInfo.canvasHeight + "px";
      image.style.left = left + "px";
      image.style.top = top + "px";
      canvas.width = imgInfo.canvasWidth;
      canvas.height = imgInfo.canvasHeight;
      canvas.style.left = left + "px";
      canvas.style.top = top + "px";

      return imgInfo;
    },
    drawPolygons(points) {
      function polygon(points, context) {
        context.beginPath();
        context.moveTo(points[0][0], points[0][1]);

        for (var i = 1; i < points.length; i++) {
          context.lineTo(points[i][0], points[i][1]);
        }
        context.closePath();
        context.fill();
        context.stroke();
      }

      // 获得 canvas.context
      var canvas = document.getElementById("quad");
      var context = canvas.getContext("2d");
      context.fillStyle = "white"; // 内部使用白色，如不指定，默认为黑色
      context.strokeStyle = "#008"; // 深蓝色外边框
      context.lineWidth = 2; // 2个像素宽
      polygon(points, context);
    },
    setAlerts() {
      if (this.$props.alerts && this.$props.alerts.length > 0) {
        let color = [255, 0, 0];
        this.$props.alerts.forEach((item) => {
          let position = item.position;
          let coordinates = {
            x: Math.round(
              (position[0] * this.imgInfo.canvasWidth) / this.imgInfo.width
            ),
            y: Math.round(
              (position[1] * this.imgInfo.canvasHeight) / this.imgInfo.height
            ),
            x1: Math.round(
              (position[2] * this.imgInfo.canvasWidth) / this.imgInfo.width
            ),
            y1: Math.round(
              (position[3] * this.imgInfo.canvasHeight) / this.imgInfo.height
            ),
            width: Math.round(
              ((position[2] - position[0]) * this.imgInfo.canvasWidth) /
                this.imgInfo.width
            ),
            height: Math.round(
              ((position[3] - position[1]) * this.imgInfo.canvasHeight) /
                this.imgInfo.height
            ),
          };
          let context = this.$refs["canvas"].getContext("2d");
          let font = Math.floor((this.imgInfo.canvasWidth / 1000) * 20);
          context.font = font + "px Arial bolder";
          context.fillStyle = "rgb(" + color.join(",") + ")";
          context.fillText(
            item.label,
            coordinates.x + 2,
            coordinates.y1 - font
          );
          context.strokeStyle = "rgb(" + color.join(",") + ")";
          context.strokeRect(
            coordinates.x,
            coordinates.y,
            coordinates.width,
            coordinates.height
          );
        });
      }
    },
    setRoi() {
      if (this.$props.roi && this.$props.roi.length > 0) {
      }
    },
    setPolys() {
      if (this.$props.polys && this.$props.polys.length > 0) {
        let color = JSON.parse(JSON.stringify(this.$props.polys[0][0])).reverse();
        let polys = JSON.parse(JSON.stringify(this.$props.polys[0][1]));
        var context = this.$refs["canvas"].getContext("2d")
        context.fillStyle = "rgba(" + color.join(",") + ", 0)"; // 内部使用透明色
        context.strokeStyle = "rgb(" + color.join(",") + ")"; 
        context.lineWidth = 1; 

        polys.forEach((item) => {
          let points = item.map((point) => {
            let x = Math.round(
              (point[0] * this.imgInfo.canvasWidth) / this.imgInfo.width
            );
            let y = Math.round(
              (point[1] * this.imgInfo.canvasHeight) / this.imgInfo.height
            );
            return [x,y]
          });
          context.beginPath();
          context.moveTo(points[0][0], points[0][1]);
          for (var i = 1; i < points.length; i++) {
            context.lineTo(points[i][0], points[i][1]);
          }
          context.closePath();
          context.fill();
          context.stroke();
        });
      }
    },
  },
};
</script>

<style lang="less" scoped>
.alarmPic {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  img {
    max-width: 100%;
    max-height: 100%;
  }
  canvas {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1;
  }
}
</style>