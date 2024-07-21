import axios from "axios";
import { Message } from "@arco-design/web-vue";

const myAxios = axios.create({
  baseURL: "http://localhost:8081",
  // 请求超时时间
  timeout: 100000,
  // 允许cookie种植
  withCredentials: true,
  // 请求头
  // headers: { authorization: "0e069c755af14b2bb39dfc2f0ff32cb4" },
});

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 从localStorage中获取token
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = `${token}`;
    }
    return config;
  },
  function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    console.log(response);

    const { data } = response;
    // 未登录
    if (data.code === 40100) {
      // 不是获取登录状态的请求以及进入非允许未登录页面，跳转到登录页面
      if (
        !response.request.responseURL.indexOf("/user/get/login") &&
        !window.location.pathname.includes("/user/login")
      ) {
        window.location.href = `/user/login?redirect=${window.location.href}`;
      }
    }

    return response;
  },
  function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    return Promise.reject(error);
  }
);

export default myAxios;
