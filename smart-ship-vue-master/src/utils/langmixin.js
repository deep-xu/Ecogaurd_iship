export default {
    data() {
      return {
        lang: 'zh'
      }
    },
    beforeMount() {
      if (localStorage.getItem('lang')) {
        this.lang = localStorage.getItem('lang')
      }
    },
    // watch:{
    //   '$i18n.locale'(newlang){
    //     this.lang = newlang
    //   }
    // },
  }