<template>
  <div>
    <el-form
      ref="formData"
      :model="formData"
      label-position="top"
      :rules="formRule"
      style="width: 90%; margin: 0 auto; padding-top: 20rem"
    >
      <el-form-item label="User Name" prop="username">
        <el-input
          v-model="formData.username"
          :readonly="formData.type == 'edit'"
          :maxlength="20"
        ></el-input>
      </el-form-item>
      <el-form-item label="Password" prop="password" v-if="formData.type == 'edit'">
        <el-input
          clearable
          type="password"
          v-model="formData.password"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="Password"
        prop="password"
        v-else
        :rules="{ required: true, message: 'Please input password', trigger: 'blur' }"
      >
        <el-input
          clearable
          type="password"
          v-model="formData.password"
        ></el-input>
      </el-form-item>
      <el-form-item label="Permission" prop="permission" v-if="formData.username != 'admin'">
        <el-radio-group v-model="formData.permission">
          <el-radio :label="20">Admin</el-radio>
          <el-radio :label="10">User</el-radio>
          <!-- <el-radio :label="1">访客</el-radio> -->
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Device" prop="device" v-if="formData.permission == 10">
        <el-checkbox-group v-model="formData.device">
          <el-checkbox v-for="(device, id) in devices" :label="id" :key="id">
            <i
              class="z-icon-shebei"
              :class="{
                online: device && device.status == 1,
                offline: device && device.status == 0,
                delete: !device,
              }"
            ></i>
            <span v-if="device">{{ (device && device.name) || id }}</span>
            <span v-else style="color: red">{{
              (device && device.name) || id
            }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="formData.loading"
          :loading="formData.loading"
          type="primary"
          @click="handleSave"
          >Save</el-button
        >
      </el-form-item>
    </el-form>
  </div>
</template>
  <script>
import { user } from "@/api/user.js";
import langmixin from "@/utils/langmixin.js";
export default {
  mixins: [langmixin],
  props: ["formDataInit", "devices"],
  data() {
    return {
      api: user,
      oriData: {
        type: "add",
        username: "",
        password: "",
        permission: 10,
        device: [],
      },
      formData: {
        loading: false,
        type: "add",
        username: "",
        password: "",
        permission: 10,
        device: [],
      },
      formRule: {
        username: [
          { required: true, message: "Please input username", trigger: "blur" },
        ],
      },
    };
  },
  beforeMount() {
    this.handleReset();
  },
  methods: {
    handleReset() {
      if (this.formDataInit.type == "add") {
        this.formData = Object.assign(this.formData, this.oriData);
      } else {
        this.oriData.type = "edit";
        this.oriData.id = this.formDataInit.id;
        this.oriData.username = this.formDataInit.username;
        this.oriData.permission = this.formDataInit.permission;
        this.oriData.device = this.formDataInit.device || [];
        this.formData = Object.assign(this.formData, this.oriData);
      }
    },

    handleSave() {
      this.$refs["formData"].validate((valid) => {
        if (valid) {
          this.formData.loading = true;
          let params = {
            username: this.formData.username,
            permission: this.formData.permission,
          };
          if (this.formData.id) {
            params.id = this.formData.id;
          }
          if (this.formData.password) {
            params.password = this.formData.password;
          }
          if (params.permission == 10) {
            params.device = this.formData.device;
          } else if (params.permission == 20) {
            params.device = null;
          }
          let api = null;
          if (params.id) {
            api = this.api.edit;
          } else {
            api = this.api.add;
          }
          console.log(params);
          api(params)
            .then((res) => {
              this.formData.loading = false;
              if (
                this.formData.password != this.oriData.password ||
                this.formData.permission != this.oriData.permission
              ) {
                this.$message({
                  type: "success",
                  message: "The system will go to the login page in 2 seconds",
                });
                setTimeout(() => {
                  this.$router.push("/login");
                }, 2000);
              } else {
                this.$emit("close-edit-userpermission");
                this.$message({
                  type: "success",
                  message: "Save success",
                });
              }
            })
            .catch((err) => {
              this.formData.loading = false;
              if (err && err.message && err.message[this.lang]) {
                this.$message({
                  type: "error",
                  message: err.message[this.lang],
                });
              } else {
                this.$message({
                  type: "error",
                  message: "Update fail",
                });
              }
            });
        }
      });
    },
  },
};
</script>
<style scoped lang="less">
/deep/.el-checkbox{
  display: flex;
  align-items: center;
  margin-bottom: 2px;
}
/deep/.el-checkbox__label {
  width: 330px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}
.z-icon-shebei {
  color: #333;
  font-size: 16px;
  position: relative;
  top: 2px;
  margin-right: 4px;
}
.z-icon-shebei.online {
  color: #19d519;
}
.z-icon-shebei.offline {
  color: orange;
}
.z-icon-shebei.delete {
  color: red;
}
</style>