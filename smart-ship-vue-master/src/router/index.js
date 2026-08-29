import Vue from 'vue';
import Router from 'vue-router';

Vue.use(Router);

const commonRoutes = [
  {
    path: '/',
    redirect: '/eco/dashboard'
  },{
    path: '/login',
    component: resolve => require(['../components/page/login/login.vue'], resolve),
    meta: { title: '登录' }
  },{
    path: '/eco/:view?',
    name: 'eco',
    component: resolve => require(['../components/page/eco/EcoApp.vue'], resolve),
    meta: { title: 'Ecoguard Vigil' }
  },{
    path: '/live',
    component: resolve => require(['../components/page/live/live.vue'], resolve),
    meta: { title: '实时画面' }
  },
  {
    path: '/404',
    component: resolve => require(['../components/page/404.vue'], resolve),
    meta: { title: '404' }
  },
  {
    path: '/403',
    component: resolve => require(['../components/page/403.vue'], resolve),
    meta: { title: '403' }
  },
  {
    path: '/',
    component: resolve => require(['../components/layout/layout1.vue'], resolve),
    meta: { title: '' },
    children: [{
      path: '/home',
      component: resolve => require(['../components/page/home/home.vue'], resolve),
      props: true
    }]
  }
]

const createRouter = () => new Router({
  routes: commonRoutes
})


const router = createRouter()

export default router

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher
}

