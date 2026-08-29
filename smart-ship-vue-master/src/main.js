import Vue from 'vue'
import App from './App.vue'
import router from './router'
import './router/permission';
import axios from 'axios';
import ElementUI from 'element-ui';

import 'element-ui/lib/theme-chalk/index.css'; // 默认主题
import locale from 'element-ui/lib/locale/lang/en'; 
import '@/assets/css/pc/index.less'
import './components/common/directives';
import './assets/iconfont/iconfont.css';  // 字体图标
import * as echarts from 'echarts'
import "babel-polyfill";
import store from './store';

import {extendjs, resizeFontsize} from './utils/utils'
import "./components/page/common/commonComponents.js"

Vue.prototype.$echarts = echarts

Vue.config.productionTip = false
Vue.use(ElementUI, { locale , size: 'small'});
Vue.prototype.$axios = axios;

/** 设置提示语言 */
var langCode = localStorage.getItem("lang");
if(langCode != "en"){
    localStorage.setItem("lang", "en");
}

/** 扩展js方法 */
extendjs();
resizeFontsize();

const app = new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')