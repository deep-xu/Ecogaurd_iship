import { resetRouter } from '@/router'

export function resetTokenAndClearUser() {
    // 退出登陆 清除用户资料
    localStorage.setItem('token', '')
    localStorage.setItem('menu', '')
    localStorage.setItem('menu1', '')
    localStorage.setItem('role', '')
    localStorage.setItem('t', '')
    localStorage.setItem('permission', '')
    localStorage.setItem('streamports', '')
    // 重设路由
    resetRouter()
}