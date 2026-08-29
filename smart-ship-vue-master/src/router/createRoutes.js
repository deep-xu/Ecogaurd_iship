import { asyncRoutes } from '@/router'
import store from '../store'

// 将菜单信息转成对应的路由信息 动态添加
export const createRoute = (menu) => {
    let menuObj = {};
    let routers = [
        {
            path: '/',
            component: resolve => require(['@/components/layout/layout.vue'], resolve),
            meta: { title: '' },
            children: []
        }
        // ,{
        //     path: '/',
        //     component: resolve => require(['@/components/layout/layout1.vue'], resolve),
        //     meta: { title: '' },
        //     children: [{
        //         path: '/dashboard',
        //         component: resolve => require(['@/components/page/dashboard/index.vue'], resolve),
        //         meta: { title: '' },
        //     }]
        // }
    ];

    recursionMenu(menu, menuObj, routers);

    // 找不到则跳转到404
    routers.push({
        path: '*',
        redirect: '/404'
    })

    store.commit('menu/setMenus', menu); // 生成菜单
    store.commit("menu/setMenuObj", menuObj)
    return routers
}

function recursionMenu(menu, menuObj, routers) {
    // let role = localStorage.getItem("role")
    let permission = localStorage.getItem("permission")
    menu.forEach(item => {
        if (item.index || (item.children && item.children.length > 0)) {
            menuObj[item.index] = item
        }
        if (item.path ) {
            let router = {
                path: '/' + item.index,
                component: () => import(`@/components/page/${item.path}`),
                meta: item
            };
            routers[0].children.push(router);
        }
        // if (!role || 
        //     (!item.role && !item.permission) || 
        //     (item.role && item.role.indexOf(role) >= 0) || 
        //     (item.permission && item.permission.indexOf(permission) >= 0)){
        //     // if(item.index != "dashboard"){
        //         if (item.path ) {
        //             let router = {
        //                 path: '/' + item.index,
        //                 component: () => import(`@/components/page/${item.path}`),
        //                 meta: item
        //             };
        //             routers[0].children.push(router);
        //         }
        //     // }
            
        // }
        

        if (item.children && item.children.length > 0) {
            recursionMenu(item.children, menuObj, routers)
        }
    });

}
