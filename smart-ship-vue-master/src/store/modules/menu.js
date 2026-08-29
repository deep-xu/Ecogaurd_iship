import Vue from 'vue'
const state={
    role: localStorage.getItem("role") || '',
    menuObj: {},
    menuTree: [],
};
const getters={
    menuTree(state){
        return state.menuTree
    }
}
const mutations={
    setMenus(state, items) {
        let menuTree = []
        menuFilter(state.role, items,  menuTree)
        state.menuTree = menuTree.sort((a,b) => a.id - b.id);
    },
    setMenuObj(state,items){
        state.menuObj = {...items};
    },

    setMenuRole(state, role) {
        state.role = role
    }
}

function menuFilter(role, menuOri, menuTree){
    for (let i = 0; i < menuOri.length; i++){
        if (!role || !menuOri[i].role || menuOri[i].role.indexOf(role) >= 0){
            let menu = {
                id:menuOri[i].id,
                icon: menuOri[i].icon,
                title: menuOri[i].title,
                index: menuOri[i].index,
                path: menuOri[i].path,
                disabled: menuOri[i].disabled || ""
            }
            menuTree.push(menu);
            if(menuOri[i].children){
                menu.children = [];
                menuFilter(role, menuOri[i].children, menu.children)
            }
        }
    }

}
const actions={
    
}
export default{
    namespaced:true,
    getters,
    mutations,
    actions,
    state
}