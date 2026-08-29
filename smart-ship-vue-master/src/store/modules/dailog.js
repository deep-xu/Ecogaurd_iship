const state={
    showUser:false,
    isShowLoading: false, // 全局 loading
};
const getters={
    isShow(state){
       return state.showUser;
    }
}
const mutations={
    hide(state){
        state.showUser=false;
    },
    show(state){
        state.showUser=true;
    },
    setLoading(state, isShowLoading) {
        state.isShowLoading = isShowLoading
    },
}
const actions={
    showDailog({commit}){
        commit('show');
    },
    hideDailog({commit}){
        commit('hide');
    }
}
export default{
    namespaced:true,
    getters,
    mutations,
    actions,
    state
}