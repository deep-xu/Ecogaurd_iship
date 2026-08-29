import axios from 'axios'
import router from '@/router'
import { Message } from 'element-ui'
import { showLoading, closeLoading } from '@/utils/loading'
import { resetTokenAndClearUser } from '@/utils'

const CancelToken = axios.CancelToken;
const source = CancelToken.source();

const service = axios.create({
  baseURL: window.location.origin,
  timeout: 60 * 1000 * 10,
})
service.defaults.headers.common['clientid'] = 'e5cd7e4891bf95d1d19206ce24a7b32e'
let lang = localStorage.getItem('lang') || 'zh'

service.interceptors.request.use(config => {
  showLoading()
  if (localStorage.getItem('t')) {
    config.headers.Authorization = 'Bearer ' + localStorage.getItem('t')
  } 
  // else if (!localStorage.getItem('t') && config.url.indexOf("user/login") < 0) {
  //   let errorMsg = { msg: 'cancelCache' + Math.random(), data: [] }
  //   config.cancelToken = source.token
  //   source.token.reason = errorMsg // 这一行的作用是每次传递的errorMsg都是不一样的，如果没有这一行，那么，即使source.cancel(errorMsg)中有传递不同的参数，接收的时候也是一样的
  //   // cancel函数可以不用传参，也可以传入取消后执行的操作，取消后可提示用户需要登录
  //   source.cancel(errorMsg)
  //   // 定时器1.5s后自动跳转到登录页
  //   // setTimeout(() => { window.location.href = "http://" + window.location.host }, 1500);

  // }

  return config
}, (error) => Promise.reject(error))

service.interceptors.response.use(response => {
  let requestUrl = response.request.responseURL;
  if (requestUrl.indexOf('/ks/proxy/logo') > 0) {
    return response.data
  }
  closeLoading()
  const res = response.data
  // 这里是接口处理的一个示范，可以根据自己的项目需求更改
  // 错误处理
  if (typeof res == "object") {
    if (res.error_code != 0 && res.message && res.message[lang]) {
      // if(res.message[lang].indexOf("人脸得分过低") >= 0){
      //   Message.error({
      //     message: res.message[lang] + `当前得分${res.data[0].toFixed(2)}, 人脸质量标准得分${res.data[1]}！`,
      //     duration: 3000
      //   })
      // } else {
      //   Message.error({
      //     message: res.message[lang],
      //     duration: 3000
      //   })
      // }
      return Promise.reject(res)
    } else if (res.code == 400 && res.msg) {
      // Message.error({
      //   message: res.msg,
      // })
      return Promise.reject(res)
    } else if (res.code == 401 && res.msg) {
      Message.error('登录过期，请重新登录');
      resetTokenAndClearUser()
      router.push('login');
    } else if (res.error_code == 0 && res.message && res.message[lang] && response.config.method != "get" && response.config.url.indexOf("login") < 0) {
      // Message.closeAll();
      // Message.success({
      //   message: res.message[lang],
      // })
      // return res
    }
  } else {
    // if (res.indexOf('alert') > 0) {
    //   resetTokenAndClearUser()
    //   router.push('login')
    // }
  }

  // 如果接口正常，直接返回数据
  return res.data && typeof (res.data) == "object" ? res.data : res
}, (error) => {
  closeLoading()
  if (error.response && error.response.status == 401) {
    resetTokenAndClearUser()
    router.push('login');
  } else if (error.response && error.response.status == 404) {
    Message.closeAll();
    if (error.request.responseURL.indexOf('/algsjson/') < 0) {
      Message.error({
        message: '404! 接口未找到！',
      })
    }
  } else if (error.response && error.response.status == 500 && error.response.config.url.indexOf('reboot') < 0) {
    Message.closeAll();
    Message.error({
      message: '网络错误，请稍后再试',
    })
    return Promise.reject()
  }

  return Promise.reject(error)
})

export default service
