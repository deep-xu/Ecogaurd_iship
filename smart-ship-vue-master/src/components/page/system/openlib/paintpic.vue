<template>
  <div class="paintPic" ref="container">
    <img :src="src" ref="image" />
    <canvas ref="canvas"></canvas>
  </div>
</template>
  
  <script>
export default {
  name: "paintPic",
  props: ["src", "roi"],
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
      if(this.$props.src){
        var img = document.createElement("img");
        img.src = this.$props.src;
        img.onload = () => {
          this.imgInfo = Object.assign(this.imgInfo, this.getimgInfo(img));
          this.setRoi();
        };
      }
    },
    // 获取图片宽高，设置canvas宽高
    getimgInfo(img) {
      const canvas = this.$refs["canvas"]; // width: 768 height: 768*imgheight/imgwidth
      const container = this.$refs["container"];
      const image = this.$refs["image"];
      if(!container || !canvas || !image){
        return
      }
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

    setRoi() {
      let canvas = this.$refs["canvas"];
      if(!canvas) return
      if (this.$props.roi && this.$props.roi.length > 0) {
        var context = this.$refs["canvas"].getContext("2d");
        context.fillStyle = "rgba(255,0,0, 0)"; // 内部使用透明色
        context.strokeStyle = "rgb(2,252,54)";
        context.lineWidth = 1;
        let points = this.$props.roi.map((point) => {
          let x = Math.round(
            (point[0] * this.imgInfo.canvasWidth) / this.imgInfo.width
          );
          let y = Math.round(
            (point[1] * this.imgInfo.canvasHeight) / this.imgInfo.height
          );
          return [x, y];
        });
        context.beginPath();
        context.moveTo(points[0][0], points[0][1]);
        for (var i = 1; i < points.length; i++) {
          context.lineTo(points[i][0], points[i][1]);
        }
        context.closePath();
        context.fill();
        context.stroke();
      }
    },
  },
};
</script>
  
  <style lang="less" scoped>
.paintPic {
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