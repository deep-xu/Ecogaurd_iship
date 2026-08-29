<template>
  <div class="video-player-panel" id="home-video-container">
    <div class="play-controls">
      <div class="layout-controls">
        <i class="z-icon-onevideo" @click="changeLayout(1)"></i>
        <i class="z-icon-fourvideo" @click="changeLayout(4)"></i>
        <i class="z-icon-ninevideo" @click="changeLayout(9)"></i>
        <i class="el-icon-s-operation" @click="changeLayout(16)"></i>
        <el-tooltip placement="bottom">
          <i class="z-icon-quanping" @click="handleQuanping()"></i>
          <div slot="content">Full screen</div>
        </el-tooltip>
      </div>
      <div class="play-info">
        <span class="camera-count">Playing: {{ playingCameras.length }}/{{ currentLayout }} Cameras</span>
        <el-button
            type="danger"
            size="small"
            @click="stopAllPlayers">
          Stop All
        </el-button>
      </div>
    </div>

    <div class="play-area-container">
      <div class="play-area" :class="`layout-${currentLayout}`">
        <div
            v-for="index in currentLayout"
            :key="index"
            class="player-container"
            :class="{ 'has-camera': getCameraByPosition(index-1) }">

          <div v-if="getCameraByPosition(index-1)" class="player-wrapper">
            <div class="player-header">
              <span>{{ getCameraByPosition(index-1).label }}</span>
              <span class="player-close" @click="removeCamera(getCameraByPosition(index-1).id)">
                <i class="el-icon-close"></i>
              </span>
            </div>

            <!-- mpegts.js 播放器容器 -->
            <video
                :id="`video-${getCameraByPosition(index-1).id}`"
                class="player-content"
                muted
                autoplay>
              Your browser does not support video playback
            </video>
          </div>

          <div v-else class="player-placeholder">
            <div class="empty-player">
              <i class="el-icon-video-camera" style="font-size: 24px; margin-bottom: 10px;"></i>
              <div>Idle Screen</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 引入 mpegts.js
import mpegts from 'mpegts.js';
import {nvrApis} from "@/api/nvrApis";

export default {
  name: 'CameraVideo',
  props: {
    playingCameras: {
      type: Array,
      default: () => []
    },
  },
  data() {
    return {
      currentLayout:4,
      mpegtsPlayers: {} // 存储 mpegts.js 播放器实例
    }
  },
  computed: {
  },
  watch: {
    // 监听播放摄像头变化
    playingCameras: {
      handler(newCameras, oldCameras) {
        // 销毁不再播放的摄像头播放器
        oldCameras.forEach(camera => {
          if (!newCameras.some(c => c.id === camera.id)) {
            this.destroyMpegtsPlayer(camera.id);
          }
        });

        // 创建新播放器
        this.$nextTick(() => {
          newCameras.forEach(camera => {
            if (!this.mpegtsPlayers[camera.id]) {
              this.playVideo(camera);
            }
          });
        });
      },
      deep: true
    },
    // 监听布局变化
    currentLayout(newLayout, oldLayout) {
      // 如果新布局比旧布局小，需要移除超出布局的摄像头
      if (newLayout < oldLayout && this.playingCameras.length > newLayout) {
        const camerasToRemove = this.playingCameras.slice(newLayout);
        camerasToRemove.forEach(camera => {
          this.removeCamera(camera.id);
        });
        this.$message.warning(`The layout has been switched to${newLayout}screen，and cameras that exceed the layout have automatically stopped`);
      }
    }
  },
  mounted() {
    // 检查浏览器支持情况
    if (!mpegts.getFeatureList().mseLivePlayback) {
      this.$message.error('The current browser does not support MSE live streaming playback. Please use a modern browser such as Chrome, Firefox, Safari, or Edge');
      return;
    }

    // 初始化播放器
    this.$nextTick(() => {
      this.playingCameras.forEach(camera => {
        this.playVideo(camera);
      });
    });
  },
  beforeDestroy() {
    // 组件销毁前销毁所有播放器
    Object.keys(this.mpegtsPlayers).forEach(cameraId => {
      this.destroyMpegtsPlayer(cameraId);
    });
  },
  methods: {
    handleQuanping() {
      const ele = document.querySelector("#home-video-container"); // 注意 这里要填一个容器 就是你要让他全屏的容器
      if (ele.requestFullscreen) {
        ele.requestFullscreen();
      } else if (ele.mozRequestFullScreen) {
        ele.mozRequestFullScreen();
      } else if (ele.webkitRequestFullScreen) {
        ele.webkitRequestFullScreen();
      }
    },
    // 根据位置获取摄像头
    getCameraByPosition(position) {
      return this.playingCameras[position] || null;
    },

    // 切换布局
    changeLayout(layout) {
      this.currentLayout = layout;
    },

    // 播放视频 - 使用 mpegts.js
    async playVideo(camera) {
      // 先检查是否已达到最大播放数量
      if (this.playingCameras.length > this.currentLayout) {
        this.$message.warning(`The maximum playback count has been reached (${this.currentLayout})，Unable to play more cameras`);
        return;
      }
      const res=await nvrApis.getLiveStream({nvrId:camera.nvrId,channelId:camera.channelId,type:1})
      // 先销毁之前的播放器
      this.destroyMpegtsPlayer(camera.id);

      const videoElement = document.getElementById(`video-${camera.id}`);

      if (videoElement && mpegts.isSupported()) {

        try {
          // 创建 mpegts.js 播放器实例
          const player = mpegts.createPlayer({
            type: 'flv', // 指定媒体类型为 flv
            url: res.data, // 视频流地址
            isLive: true, // 开启直播模式
            hasAudio: false, // 关闭声音（摄像头通常无音频）
            cors: true // 开启跨域访问
          }, {
            // 播放器配置选项
            enableWorker: false, // 启用分离的线程进行转换
            enableStashBuffer: false, // 关闭IO隐藏缓冲区以减少延迟
            stashInitialSize: 128, // 减少首帧等待时长
            lazyLoad: false, // 关闭懒加载模式（针对实时视频流）
            lazyLoadMaxDuration: 0.2, // 懒加载的最大时长
            deferLoadAfterSourceOpen: false, // 在MediaSource sourceopen事件触发后加载
            liveBufferLatencyChasing: true, // 启用实时缓冲区延迟追赶
            liveBufferLatencyMaxLatency: 1.5, // 可接受的最大缓冲区延迟
            liveBufferLatencyMinRemain: 0.3 // 要保持的最小缓冲区延迟
          });

          // 绑定视频元素并播放
          player.attachMediaElement(videoElement);
          player.load();
          player.play();

          // 存储播放器实例
          this.mpegtsPlayers[camera.id] = player;

          // 监听错误事件
          player.on(mpegts.Events.ERROR, (error) => {
            console.error(`摄像头 ${camera.label} 播放错误:`, error);
            // this.$message.error(`Camera ${camera.label} playback failed, please check the video stream address`);
            // 错误处理：根据错误类型决定是否重试
            if (error.type === 'NetworkError') {
              console.log('网络错误，尝试重新连接...');
            }
          });

          // 监听媒体信息事件
          player.on(mpegts.Events.MEDIA_INFO, (mediaInfo) => {
            console.log(`摄像头 ${camera.label} 媒体信息:`, mediaInfo);
          });

          // 监听统计信息事件（可用于监控播放状态）
          player.on(mpegts.Events.STATISTICS_INFO, (statisticsInfo) => {
            // 可以在这里监控播放状态，比如用于断流检测
            // console.log('播放统计:', statisticsInfo);
          });

          // 监听加载完成事件（可用于处理流中断）
          player.on(mpegts.Events.LOADING_COMPLETE, () => {
            console.log(`摄像头 ${camera.label} 流加载完成，可能是流中断`);
            // 这里可以添加重连逻辑
          });

        } catch (error) {
          console.error(`创建播放器失败 (${camera.label}):`, error);
          this.$message.error(`Failed to create player: ${camera.label}`);
        }
      } else {
        this.$message.error(`Cannot find video element or browser does not support it mpegts.js: ${camera.label}`);
      }
    },

    // 销毁 mpegts.js 播放器
    destroyMpegtsPlayer(cameraId) {
      if (this.mpegtsPlayers[cameraId]) {
        try {
          const player = this.mpegtsPlayers[cameraId];
          player.pause();
          player.unload();
          player.detachMediaElement();
          player.destroy();
          delete this.mpegtsPlayers[cameraId];
        } catch (error) {
          console.error(`销毁播放器失败 (${cameraId}):`, error);
        }
      }
    },

    // 移除摄像头
    removeCamera(cameraId) {
      this.destroyMpegtsPlayer(cameraId);
      this.$emit('camera-removed', cameraId);
    },

    // 停止所有播放器
    stopAllPlayers() {
      Object.keys(this.mpegtsPlayers).forEach(cameraId => {
        this.destroyMpegtsPlayer(cameraId);
      });
      this.$emit('stop-all');
    }
  }
}
</script>

<style scoped>
.video-player-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.play-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.layout-controls {
  display: flex;
  align-items: center;
  color: #00e2fd;
  i {
    margin-right: 5rem;
    font-size: 18rem;
    cursor: pointer;
  }
}

.layout-label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.play-info {
  display: flex;
  align-items: center;
}

.camera-count {
  font-size: 14px;
  color: #606266;
  margin-right: 15px;
}

.play-area-container {
  flex: 1;
  background-color: #000;
  border-radius: 4px;
  padding: 10px;
  overflow: hidden;
}

.play-area {
  width: 100%;
  height: 100%;
  display: grid;
  gap: 10px;
}

/* 单画面布局 */
.layout-1 {
  grid-template-columns: 1fr;
  grid-template-rows: 1fr;
}

/* 4画面布局 */
.layout-4 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
}

/* 9画面布局 */
.layout-9 {
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr;
}

/* 16画面布局 */
.layout-16 {
  grid-template-columns: 1fr 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr 1fr;
}

.player-container {
  position: relative;
  background-color: #1a1a1a;
  border-radius: 4px;
  overflow: hidden;
  border: 2px solid transparent;
}

.player-container.has-camera {
  border-color: #409EFF;
}

.player-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
  font-size: 14px;
}

.player-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 5px 10px;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  z-index: 10;
}

.player-close {
  cursor: pointer;
}

.player-content {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持视频比例并填满容器 */
}

.empty-player {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
  flex-direction: column;
}

.player-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>