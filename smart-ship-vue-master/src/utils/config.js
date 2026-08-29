
export const projConfig = {
  officialwebset: 'https://www.aidrive-tech.com', //官网
  kfpt: 'http://www.mindhermit.com', // 开放平台
  proxyUrl: 'ks/proxy',
  curBaseUrl: '/api', // 后台接口代理前缀
  // picprev: process.env.NODE_ENV === 'development' ? 'http://47.98.238.123:9192/staticdata' : 'staticdata',
  picprev: process.env.NODE_ENV === 'development' ? 'http://47.98.238.123:9192/staticdata' : 'staticdata',
  alertType:{
      3: "人员离岗",
      6: "打电话",
      7: "抽烟",
      10: "明火",
      11: "烟雾",
      // 13: "灭火器未摆放在作业区域",
      // 14: "静电释放时间未达到规定时长开始进行卸油作业",
      13: "灭火器",
      14:"静电夹"
  }
}