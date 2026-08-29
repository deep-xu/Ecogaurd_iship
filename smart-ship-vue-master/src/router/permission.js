import router from './index.js'
import store from '../store'
// import {getMenu} from '../api/cms';
import { createRoute } from "./createRoutes"

//使用钩子函数对路由进行权限跳转
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('t');
    const menu = localStorage.getItem('menu');
    if (to.path == '/login' || to.path == '/live') {
        next(); 
    } else if (!token) {
        next('/login');
    } else {
        // 简单的判断IE10及以下不进入富文本编辑器，该组件不兼容
        // if (navigator.userAgent.indexOf('MSIE') > -1 && to.path === '/editor') {
        //     Vue.prototype.$alert('vue-quill-editor组件不兼容IE10及以下浏览器，请使用更高版本的浏览器查看', '浏览器不兼容通知', {
        //         confirmButtonText: '确定'
        //     });
        // } else {
        //     next();
        // }
        // if (store.state.menu.role) {

        // }
        if (!menu || menu == '[]') {
            // 菜单
            let menuList = JSON.parse(localStorage.getItem('menu1') || '[]').map(item => {
                if (item.path === '/') {
                    return item.children[0]
                }
                return item;
            })
            const menucfg = menuList.map(({ name, path, children = [], meta, icon, component }) => {
                let item = {
                    id: name, icon, title: meta.title, path: component, index: path
                }
                const childrenList = children.map(({ name: id, path, meta, icon, children, component }) => {
                    return { id, path: component, icon, title: meta.title, index: path }
                })
                if (children.length > 0) {
                    item.children = childrenList
                }
                return item;
            })

            // getMenu({roleID: "f647d090-6fa3-4b15-8cc2-bd014f8f12ff"}).then(data => {
                // const menu = JSON.parse(data.data);  
                let menu = menucfg;   

                localStorage.setItem('menu', JSON.stringify(menu));
                const routers = createRoute(menu);
                router.addRoutes(routers);
                next({ ...to, replace: true });
            // })
        } else  {
            const menuArr = JSON.parse(menu);
            if (Object.getOwnPropertyNames(store.state.menu.menuTree).length == 2 && menuArr.length > 0){
                let routers = createRoute(menuArr);
                router.addRoutes(routers);
                next({ ...to, replace: true });
            } else {
                next()
            }        
        } 
    }
})

