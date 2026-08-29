<template>
  <div class="p-2">
    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="el-icon-bottom"
              @click="handleSync()"
              >Sync Camera</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-refresh"
              @click="handleQuery()"
              >refresh</el-button
            >
          </el-col>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="channelList">
        <el-table-column label="Camera Id" align="center" prop="channelId" />
        <el-table-column
          label="Camera Name"
          align="center"
          prop="channelName"
        />
        <el-table-column
          label="Manufacturer"
          align="center"
          prop="manufacturer"
        />
        <el-table-column label="Address" align="center" prop="address" />
        <el-table-column label="Camera Status" align="center" prop="status">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" mini type="success">
              online
            </el-tag>
            <el-tag v-else-if="row.status === 2" mini type="warning">
              idle
            </el-tag>
            <el-tag v-else mini type="danger"> offline </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Group" align="center" prop="channelGroupName" />
        <el-table-column
          label="Options"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-tooltip content="Modify" placement="top">
              <el-button link type="text" @click="handleUpdate(scope.row)">
                <i class="z-icon-edit"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Play" placement="top">
              <el-button
                link
                type="text"
                icon="el-icon-video-play"
                @click="handleLoadLive(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog
      :title="dialog.title"
      :visible.sync="dialog.visible"
      append-to-body
      @close="cancel"
    >
      <el-form
        ref="shipFormRef"
        :model="form"
        :rules="rules"
        label-width="auto"
      >
        <el-form-item label="Camera Id" prop="channelId">
          <el-input v-model="form.channelId" readonly />
        </el-form-item>
        <el-form-item label="Camera Name" prop="channelName">
          <el-input
            v-model="form.channelName"
            placeholder="Please enter the Camera Name"
          />
        </el-form-item>
        <el-form-item label="manufacturer" prop="manufacturer">
          <el-input v-model="form.manufacturer" readonly />
        </el-form-item>
        <el-form-item label="address" prop="address">
          <el-input v-model="form.address" readonly />
        </el-form-item>
        <el-form-item label="channelGroup" prop="channelGroup">
          <el-select
            v-model="form.channelGroup"
            placeholder="Please select the channelGroup"
          >
            <el-option
              v-for="item in dictList"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            >
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm"
            >Confirm</el-button
          >
          <el-button @click="cancel">Cancel</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      title="Video Preview"
      :visible.sync="videoDialog"
      append-to-body
      @close="handleClose"
    >
      <el-form :model="videoForm" :inline="true">
        <el-form-item label="Type">
          <el-radio-group v-model="videoForm.type">
            <el-radio :label="1" value="1">Live</el-radio>
            <el-radio :label="0" value="0">PlayBack</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="videoForm.type === 0">
          <el-row>
            <el-col :span="8">
              <el-date-picker
                v-model="videoForm.playDate"
                type="date"
                placeholder="Select Date"
                value-format="yyyy-MM-dd"
              >
              </el-date-picker>
            </el-col>
            <el-col :span="8">
              <el-time-select
                placeholder="Start Time"
                v-model="videoForm.startTime"
                :picker-options="{
                  start: '00:00',
                  step: '00:15',
                  end: '24:00',
                }"
              />
            </el-col>
            <el-col :span="8">
              <el-time-select
                placeholder="End Time"
                v-model="videoForm.endTime"
                :picker-options="{
                  start: '00:00',
                  step: '00:15',
                  end: '24:00',
                  minTime: videoForm.startTime,
                }"
              />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleLoadVideo">Play</el-button>
        </el-form-item>
      </el-form>
      <div>
        <SmartFlvPlayer
            v-if="videoDialog&&flvStreamUrl!==''"
            :src="flvStreamUrl"
            :is-live="videoForm.type===1"
            :max-width="800"
        />
      </div>
<!--      <div class="player-wrapper" id="playerContainer">-->
<!--        <video id="livePlay" class="video" muted="true" autoplay="true" playsinline></video>-->
<!--      </div>-->
    </el-dialog>
  </div>
</template>

<script>
import {nvrApis} from "@/api/nvrApis";
import {nvrChannelApis} from "@/api/nvrChannelApis";
import {dict} from "@/api/system";
import SmartFlvPlayer from "@/components/page/nvrChannel/SmartFlvPlayer.vue";

export default {
  components: {SmartFlvPlayer, CifFlvPlayer: SmartFlvPlayer},
  props: ["nvrId"],
  data() {
    return {
      channelList: [],
      deleteList: [],
      loading: false,
      dialog: { title: "Add", visible: false },
      rules: {
        channelGroup: [
          {
            required: true,
            message: "Please enter the channelGroup",
            trigger: "blur",
          },
        ],
        name: [
          {
            required: true,
            message: "Please enter the Camera Name",
            trigger: "blur",
          },
        ],
      },
      form: {},
      dictList: [],
      buttonLoading: false,
      videoDialog: false,
      videoForm: {
        nvrId: undefined,
        channelId: undefined,
        type: 1,
        timeRange: [],
        playDate: undefined,
        startTime: undefined,
        endTime: undefined,
        liveUrl: undefined,
      },
      isPlaying: false, // 判断是否正在播放直播
      notSpeedCount: 0, //记录播放速度为0的次数
      flvVideo: null, //播放器
      flvStreamUrl:'',
    };
  },
  methods: {
    handleUpdate(row) {
      this.dialog.visible = true;
      this.dialog.title = "Modify";
      this.form = row;
      this.handleLoadDict();
    },
    async handleLoadDict() {
      let queryParams = {
        dictType: "channel_group",
        pageNum: 1,
        pageSize: 100,
      };
      let { rows } = await dict.getList(queryParams);
      this.dictList = rows;
    },
    async handleQuery() {
      this.channelList = await nvrApis.getVoidList(this.nvrId);
    },
    async handleLoadChannel(nvrId) {
      this.channelList = await nvrApis.getVoidList(nvrId);
    },
    async submitForm() {
      try {
        await this.$refs.shipFormRef.validate();
        this.buttonLoading = true;
        let data = {};
        if (this.form.id) {
          data = await nvrChannelApis.updateChannel(this.form);
        }
        this.dialog.visible = false;
        await this.handleQuery();
        this.$message.success(data.msg);
      } catch (error) {
      } finally {
        this.buttonLoading = false;
      }
    },
    cancel() {
      this.dialog.visible = false;
      this.form = {};
    },
    handleLoadVideo() {
      if(this.videoForm.type===0){
        if(!this.videoForm.playDate||!this.videoForm.startTime ||!this.videoForm.endTime){
          this.$message.warning("Please enter the Play Time");
          return;
        }
      }
      this.videoForm.timeRange=[];
      this.videoForm.timeRange[0]=this.videoForm.playDate+" "+this.videoForm.startTime+":00";
      this.videoForm.timeRange[1]=this.videoForm.playDate+" "+this.videoForm.endTime+":00";
      if(this.videoForm.type===0){
        nvrApis.checkPlayVideo(this.videoForm).then(res => {
          if(res.data===0){
            this.$message.warning("No recording resources are available within this time frame");
          }else{
            this.getLiveStream();
          }
        });
      }else{
        this.getLiveStream();
      }
    },
    getLiveStream() {
      nvrApis.getLiveStream(this.videoForm).then((res) => {
        if(res.data){
          this.flvStreamUrl=res.data;
        }else{
          this.$message.error("No playback content");
        }
      });
    },
    handleSync() {
      this.$confirm("Are you sure to Sync this Channel?", "Tip", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
        type: "warning",
      }).then(async () => {
        try {
          await nvrChannelApis.syncChannel(this.nvrId);
          this.$message.success("Sync successfully");
          await this.handleQuery();
        } catch (error) {
          this.$message.error("Sync failed");
        }
      });
    },
    handleLoadLive(row) {
      this.videoForm.nvrId = row.nvrId;
      this.videoForm.channelId = row.channelId;
      this.videoForm.timeRange = [];
      this.videoForm.liveUrl = undefined;
      this.videoDialog = true;
    },
    handleClose() {
      this.flvStreamUrl=null;
      this.videoDialog = false;
    },
  },
  mounted() {
    this.handleQuery();
  },
};
</script>

<style lang="less" scoped>
.el-pagination {
  position: relative;
}
.pagination {
  text-align: right;
  margin-top: 20px;
}
</style>
