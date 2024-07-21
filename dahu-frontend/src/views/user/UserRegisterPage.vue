<template>
  <div id="userRegisterPage">
    <h2 style="margin-bottom: 16px">用户注册</h2>
    <a-form
      :model="form"
      :style="{ width: '420px', margin: '0 auto' }"
      layout="vertical"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不小于 8 位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        tooltip="确认密码不小于 8 位"
        label="确认密码"
      >
        <a-input-password
          v-model="form.checkPassword"
          placeholder="请输入确认密码"
        />
      </a-form-item>
      <a-form-item field="captchaCode" label="验证码">
        <a-space fill>
          <div style="display: flex">
            <a-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              style="flex: 1; margin-right: 8px"
            />
            <img
              class="captcha-image"
              :src="captchaImage"
              @click="fetchCaptcha"
              style="cursor: pointer"
            />
          </div>
        </a-space>
      </a-form-item>
      <a-form-item>
        <div
          style="
            display: flex;
            width: 100%;
            align-items: center;
            justify-content: space-between;
          "
        >
          <a-button type="primary" html-type="submit" style="width: 120px">
            注册
          </a-button>
          <a-link href="/user/login">老用户登录</a-link>
        </div>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import API from "@/api";
import { userRegisterUsingPost } from "@/api/userController";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { getCaptchaUsingGet } from "@/api/captchaController";

const router = useRouter();

const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  captchaCode: "",
  token: "", // 增加 captchaToken 字段
} as API.UserRegisterRequest);

const captchaImage = ref("");

/**
 * 获取验证码
 */
const fetchCaptcha = async () => {
  const res = await getCaptchaUsingGet();
  if (res.data.code === 0 && res.data.data) {
    captchaImage.value = `data:image/png;base64,${res.data.data.captchaBase64}`;
    form.token = res.data.data.token; // 存储 token
  } else {
    message.error("获取验证码失败，" + res.data.message);
  }
};

/**
 * 提交
 */
const handleSubmit = async () => {
  const res = await userRegisterUsingPost(form);
  if (res.data.code === 0) {
    message.success("注册成功");
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    const errorMessage = res.data.message || "注册失败";
    message.error(errorMessage);
  }
};

onMounted(() => {
  fetchCaptcha();
});
</script>

<style scoped>
.captcha-image {
  width: 100px;
  height: 40px;
  cursor: pointer;
}
</style>
