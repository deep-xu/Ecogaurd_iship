export default {
    data(){
        return {
          mqttclient:null
        }
    },
    methods: {
      connectMqtt() {
        let hostname = process.env.NODE_ENV === "production"
            ? window.location.hostname : "47.98.183.192";
        this.mqttclient = mqtt.connect(`ws://${hostname}:8083/mqtt`);
      }
    },
    beforeDestroy(){
      if (this.mqttclient.connected) {
        try {
          this.mqttclient.end(false, () => {
            // console.log('disconnected successfully')
          })
        } catch (error) {
          // console.log('disconnect error:', error)
        }
      }
    }
}