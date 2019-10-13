import axios from 'axios'
import store from '../store'
import { getToken, setToken, getRefreshToken, getTenantCode } from '@/utils/auth'
import { isNotEmpty } from '@/utils/util'
import { refreshToken } from '@/api/admin/login'
import { Message } from 'element-ui'
import errorCode from '@/const/errorCode'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css'
// progress bar style

const whiteList = ['/auth/authentication/removeToken']// 白名单

// 超时时间
axios.defaults.timeout = 30000
// 跨域请求，允许保存cookie
axios.defaults.withCredentials = true
NProgress.configure({ showSpinner: false })// NProgress Configuration
// HTTPrequest拦截
axios.interceptors.request.use(config => {
  NProgress.start() // start progress bar
  if (store.getters.access_token && whiteList.indexOf(config.url) === -1) {
    const authorization = config.headers['Authorization']
    if (authorization === undefined || authorization.indexOf('Basic') === -1) {
      config.headers['Authorization'] = 'Bearer ' + getToken() // 让每个请求携带token
    }
  }
  // 增加租户编号请求头
  const tenantCode = config.headers['Tenant-Code']
  if (tenantCode === undefined) {
    config.headers['Tenant-Code'] = getTenantCode()
  }
  return config
}, error => {
  return Promise.reject(error)
})

// HTTPresponse拦截
axios.interceptors.response.use(data => {
  NProgress.done()
  return data
}, error => {
  NProgress.done()
  if (error.response) {
    const originalRequest = error.config
    const currentRefreshToken = getRefreshToken()
    // 接口返回401
    // 已经重试过
    // 自动刷新token
    if ((error.response.status === 401 || error.response.status === 403) && !originalRequest._retry && isNotEmpty(currentRefreshToken)) {
      // 退出请求
      if (originalRequest.url.indexOf('removeToken') !== -1) {
        return
      }
      return refreshToken().then(response => {
        // 保存新的token
        setToken(response.data.access_token)
        store.commit('SET_ACCESS_TOKEN', response.data.access_token)
        // 带上新的token
        originalRequest.headers['Authorization'] = 'Bearer ' + response.data.access_token
        // 重新请求
        return axios(originalRequest)
      }).catch(() => {
        // 刷新失败，执行退出
        store.dispatch('LogOut').then(() => { location.reload() })
      })
    } else if (error.response.status === 423) {
      Message({ message: '演示环境不能操作', type: 'warning' })
    } else {
      // 其它错误则弹出提示
      const { code, data } = error.response.data
      const errMsg = data || errorCode[code] || errorCode['default']
      // TODO 跳转到对应的404、500提示页面
      Message({ message: errMsg, type: 'error' })
    }
  }
  return Promise.reject(new Error(error))
})

export default axios
