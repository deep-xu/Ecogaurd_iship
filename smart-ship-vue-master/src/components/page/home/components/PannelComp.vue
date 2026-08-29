<template>
  <div class="pannel" :class="classNames">
    <i class="icon-corner top-left"></i>
    <i class="icon-corner top-right"></i>
    <i class="icon-corner bottom-left"></i>
    <i class="icon-corner bottom-right"></i>
    <div class="pannel-header">
      <h3>{{ title }}</h3>
    </div>
    <div class="pannel-body">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  props: ["classNames", "title"],
};
</script>

<style lang="less" scoped>
.pannel {
  width: 100%;
  border: 1px solid rgba(72, 175, 255, 1);
  background-color: rgb(0 88 179 / 20%);
  position: relative;
  .pannel-header {
    height: 32rem;
    width: 100%;
    background: url(../imgs/pannel-header-bg.png) no-repeat;
    background-size: 100% 100%;
    color: #00e2fd;
    font-size: 14rem;
    display: flex;
    justify-content: center;
    align-items: center;
    padding-left: 0;
  }
  .pannel-header h3{
    padding-left: 0;
  }
  .pannel-header h3:before{
    display: none;
  }
  .pannel-body {
    width: 100%;
    height: calc(100% - 32rem);
    overflow-y: auto;
  }
}
.pannel1 {
  height: 65%;
}
.pannel2 {
  height: calc(35% - 20rem);
}
.pannel3 {
  height: 65%;
  padding: 8rem;
  box-sizing: border-box;
  .pannel-header {
    display: none;
  }
  .pannel-body {
    height: 100%;
  }
}
.pannel4 {
  height: calc(35% - 20rem);
  .pannel-header {
    background: url(../imgs/pannel4-header-bg.png) no-repeat;
    background-size: 100% 100%;
  }
}
.pannel5 {
  height: 100%;
}

.icon-corner {
  position: absolute;
  width: 14rem;
  height: 3rem;
  background-color: #00e2fd;
}
.icon-corner:before{
  position: absolute;
  content: '';
  width: 3rem;
  height: 16rem;
  background-color: #00e2fd;
}
.icon-corner.top-left {
  left: 0;
  top: -2rem;
}
.icon-corner.top-left::before{
  left: -2rem;
  top: 0;
}
.icon-corner.top-right {
  right: 0;
  top: -2rem;
}
.icon-corner.top-right::before{
  right: -2rem;
  top: 0;
}
.icon-corner.bottom-left {
  left: 0;
  bottom: -2rem;
}
.icon-corner.bottom-left::before{
  left: -2rem;
  bottom: 0;
}
.icon-corner.bottom-right {
  right: 0;
  bottom: -2rem;
}
.icon-corner.bottom-right::before{
  right: -2rem;
  bottom: 0;
}
</style>