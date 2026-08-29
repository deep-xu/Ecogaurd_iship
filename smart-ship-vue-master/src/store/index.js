import Vue from 'vue'
import Vuex from 'vuex'
import dailog from './modules/dailog'
import menu from './modules/menu'
import app from "./modules/app"
Vue.use(Vuex);
const store = new Vuex.Store({
    modules:{
        dailog,
        menu,
        app
    }
});
export default store;