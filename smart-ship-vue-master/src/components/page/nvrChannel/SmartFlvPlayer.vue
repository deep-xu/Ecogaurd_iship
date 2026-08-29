<template>
  <div class="smart-flv-player" ref="playerRoot">
    <div
        v-show="!error"
        ref="container"
        class="player-wrapper"
        :style="{
        width: finalWidth + 'px',
        height: finalHeight + 'px'
      }"
    >
      <video
          ref="videoPlayer"
          muted
          autoplay
          playsinline
          webkit-playsinline
          x5-playsinline
          class="video-element"
      ></video>
    </div>
    <div v-if="error" class="error-message">⚠️ {{ error }}</div>
  </div>
</template>

<script>
export default {
  name: 'SmartFlvPlayer',
  props: {
    src: { type: String, required: true },
    isLive: { type: Boolean, default: true },
    // 新增：最大显示宽度（由父组件传入，如弹窗宽度 - padding）
    maxWidth: { type: Number, default: 800 }
  },
  data() {
    return {
      player: null,
      videoWidth: 0,
      videoHeight: 0,
      error: null,
      currentSrc: null
    };
  },
  computed: {
    // 根据原始分辨率和规则，计算应显示的宽高
    displaySize() {
      const w = this.videoWidth;
      const h = this.videoHeight;

      // 1. CIF 352x288 → 强制 4:3 (384x288)
      if (w === 352 && h === 288) {
        return { width: 384, height: 288, fixed: true };
      }

      // 2. 720x576：常见两种 DAR
      if (w === 720 && h === 576) {
        // 判断：若用于标清 PAL，可能是 4:3 或 16:9
        // 这里保守按 16:9 处理（更常见于现代设备）
        // 也可根据业务约定，或通过其他方式传入 DAR
        const dar169 = 1920 / 1080; // ≈1.777
        const dar43 = 4 / 3;        // ≈1.333
        // 默认按 16:9（你可根据实际调整）
        return { width: 720, height: 576, fixed: false, targetRatio: dar169 };
      }

      // 3. 其他分辨率（如 1920x1080）→ 按原始比例自适应
      return { width: w, height: h, fixed: false, targetRatio: w / h };
    },

    // 最终渲染尺寸
    finalWidth() {
      const { width, height, fixed, targetRatio } = this.displaySize;

      if (fixed) {
        return Math.min(width, this.maxWidth);
      }

      // 自适应：按比例缩放至不超过 maxWidth
      const ratio = targetRatio || width / height;
      return Math.min(this.maxWidth, width);
    },

    finalHeight() {
      const { width, height, fixed, targetRatio } = this.displaySize;

      if (fixed) {
        // 固定尺寸（如 CIF）
        const scale = Math.min(this.maxWidth / width, 1);
        return height * scale;
      }

      // 自适应高度
      const ratio = targetRatio || width / height;
      return this.finalWidth / ratio;
    }
  },
  watch: {
    src: {
      handler(newVal) {
        if (newVal && newVal !== this.currentSrc) {
          this.resetAndInit();
        }
      },
      immediate: true
    }
  },
  mounted() {
    if (this.src) {
      this.resetAndInit();
    }
  },
  beforeDestroy() {
    this.destroyPlayer();
  },
  methods: {
    async resetAndInit() {
      await this.destroyPlayer();
      this.error = null;
      this.videoWidth = 0;
      this.videoHeight = 0;
      this.currentSrc = this.src;

      this.$nextTick(() => {
        this.initPlayer();
      });
    },

    initPlayer() {
      const videoEl = this.$refs.videoPlayer;
      if (!videoEl || !this.src) return;

      const mpegts = window.mpegts || (typeof require === 'function' ? require('mpegts.js') : null);
      if (!mpegts) {
        this.error = 'The browser does not support FLV playback';
        return;
      }

      try {
        this.player = mpegts.createPlayer({
          type: 'flv',
          url: this.src,
          isLive: this.isLive,
          ...(this.isLive
              ? {
                liveBufferLatencyChasing: true,
                liveBufferLatencyMaxLatency: 1.0
              }
              : {})
        });

        this.player.attachMediaElement(videoEl);
        this.player.load();
        this.player.play().catch(console.warn);

        // 获取真实分辨率
        const onMetadata = () => {
          this.videoWidth = videoEl.videoWidth;
          this.videoHeight = videoEl.videoHeight;
          console.log(`[FLV] 分辨率: ${this.videoWidth}x${this.videoHeight}`);
        };

        videoEl.addEventListener('loadedmetadata', onMetadata, { once: true });

        this.player.on(mpegts.Events.ERROR, (e, info) => {
          this.error = info.msg || 'Playback failed';
        });
      } catch (err) {
        this.error = 'Initialization failed';
      }
    },

    async destroyPlayer() {
      if (this.player) {
        try {
          this.player.unload();
          this.player.detachMediaElement();
          this.player.destroy();
        } catch (e) {}
        this.player = null;
      }

      const videoEl = this.$refs.videoPlayer;
      if (videoEl) {
        videoEl.pause();
        videoEl.src = '';
        videoEl.load();
      }
    }
  }
};
</script>

<style scoped>
.smart-flv-player {
  display: block;
  text-align: center; /* 居中 */
}

.player-wrapper {
  margin: 0 auto; /* 水平居中 */
  overflow: hidden;
  position: relative;
}

.video-element {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain; /* 关键：保持比例，完整显示 */
}
</style>