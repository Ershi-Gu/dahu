<template>
  <a-row class="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="titleBar">
            <img class="logo" src="../assets/logo.png" />
            <div class="title">答乎</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <div v-if="loginUserStore.loginUser.id" class="right-align">
        <a-space>
          {{ loginUserStore.loginUser?.userName }}
          <a-dropdown trigger="hover">
            <a-avatar
              :size="32"
              :image-url="loginUserStore.loginUser?.userAvatar"
            />
            <template #content>
              <a-doption @click="logout">注销登录</a-doption>
            </template>
          </a-dropdown>
        </a-space>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { userLoginUserStore } from "@/store/userStore";
import checkAccess from "@/access/checkAccess";
import { userLogoutUsingPost } from "@/api/userController";
import message from "@arco-design/web-vue/es/message";

const router = useRouter();

// 获取登录用户信息
const loginUserStore = userLoginUserStore();

// 当前选中的菜单项
const selectedKeys = ref(["/"]);

// 点击菜单跳转到对应页面
const doMenuClick = (key: string) => {
  router.push({ path: key });
};

// 路由跳转时自动更新菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});

// 展示在菜单栏的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (!checkAccess(loginUserStore.loginUser, item.meta?.access as string)) {
      return false;
    }
    return true;
  });
});

const logout = async () => {
  let res: any;
  res = await userLogoutUsingPost();
  if (res.data.code === 0) {
    // 清除用户登录信息
    loginUserStore.logout();
    console.log(res.data);
    message.loading("即将跳转到登录页面");
    setTimeout(() => {
      router.push("/user/login");
    }, 1500);
  } else {
    message.error("操作失败，" + res.data.message);
  }
};
</script>

<style scoped>
.titleBar {
  display: flex;
  align-items: center;
}

.title {
  margin-left: 16px;
  color: black;
}

.logo {
  height: 48px;
}

.right-align {
  margin-right: 32px; /* 你可以根据需要调整这个值 */
}
</style>
