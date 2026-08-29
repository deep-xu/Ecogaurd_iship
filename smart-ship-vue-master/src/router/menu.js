export const menucfg = [
  {
    id: 1000,
    icon: 'z-icon-index',
    title: 'Home',
    index: 'home',
    path:'anything'
  },
  {
    id: 2000,
    icon: 'z-icon-oridata',
    title: 'History',
    index: 'alarm',
    path: 'alarm/index.vue'
  }, 
  {
    id: 3000,
    icon: 'z-icon-idc_device_model',
    title: 'Source',
    index: 'device',
    path: 'device/device.vue'
  }, 
  {
    id: 4000,
    icon: 'z-icon-idc_device_model',
    title: 'Ship',
    index: 'ship',
    path: 'ship/ship.vue'
  }, 
  // {
  //   id: 4000,
  //   icon: 'z-icon-alg',
  //   title: '算法仓库',
  //   index: 'algs',
  //   path: 'algs/algs.vue'
  // }, 
  // {
  //   id: 5000,
  //   icon: 'z-icon-shangbao',
  //   title: '数据推送',
  //   index: 'dataUpload',
  //   role: ["common", "admin"],
  //   children: [
  //     {
  //       id: 5001,
  //       title: 'HTTP',
  //       index: 'httpSetting',
  //       path: 'dataUpload/http/index.vue',
  //       role: ["common", "admin"],
  //     },
  //     {
  //       id: 5002,
  //       title: '企业微信',
  //       index: 'wechatSetting',
  //       path: 'dataUpload/wechatSetting/wechatuser.vue',
  //       role: ["common", "admin"],
  //     }
  //   ]
  // },
  {
    id: 9000,
    icon: 'z-icon-libmanage',
    title: 'Algorithm',
    index: 'libmanage',
    children: [
      {
        id: 9001,
        title: 'Face Algorithm',
        index: 'facelib',
        path: 'system/facelib/faceList.vue'
      },
      {
        id: 9002,
        title: 'Work clothes Algorithm',
        index: 'workclothes',
        path: 'system/workclothes/index.vue'
      },
      {
        id: 9003,
        title: 'PPE Algorithm',
        index: 'ppe',
        path: 'system/ppe/ppelist.vue'
      },
      {
        id: 9004,
        title: 'Open Algorithm',
        index: 'openlib',
        path: 'system/openlib/openlibList.vue'
      },
    ]
  },
  {
    id: 10000,
    icon: 'z-icon-system',
    title: 'System Settings',
    index: 'system',
    role: ["common", "admin"],
    children: [
      {
        id: 10001,
        title: 'User Management',
        index: 'user',
        role: ["admin"],
        path: 'system/userManager/userManager.vue'
      },
      {
        id: 10002,
        title: '角色管理',
        index: 'role',
        role: ["admin"],
        path: 'system/role/role.vue'
      },
      {
        id: 10003,
        title: '菜单管理',
        index: 'menu',
        role: ["admin"],
        path: 'system/menu/menu.vue'
      },
      // {
      //   id: 10002,
      //   title: '系统升级',
      //   index: 'sysupgrade',
      //   role: ["common", "admin"],
      //   path: 'system/sysupgrade/index.vue'
      // },
      {
        id: 10004,
        title: 'Operation System',
        index: 'maintenance',
        role: ["common", "admin"],
        path: 'system/maintenance/index.vue'
      },
      {
        id: 10005,
        title: 'Platform Upgrade',
        index: 'sysupgrade',
        role: ["admin"],
        path: 'system/sysupgrade/index.vue'
      },   
      {
        id: 10007,
        title: 'Costom Logo',
        index: 'customlogo',
        role: ["admin"],
        path: 'system/customlogo.vue'
      },
    ]
  }
]