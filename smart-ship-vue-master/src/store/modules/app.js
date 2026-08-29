import Vue from 'vue'
const state={
    isCollapse: false,  // false: 展开，true 折叠
    userRole: "admin"
};
const getters={
    isCollapse(state){
        return state.isCollapse
    },
    userRole(state){
        return state.userRole
    }
}
const mutations={
    setCollapse(state, status) { 
        state.isCollapse = status;
    },
    setUserRole(state, val){
        state.userRole = val;
    }
}
const actions={
    setUserRole({commit}, val){
        commit('setUserRole', val);
    },
}
export default{
    namespaced:true,
    getters,
    mutations,
    actions,
    state
}