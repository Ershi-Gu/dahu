<template>
  <a-card class="appCard" hoverable @click="doCardClick">
    <template #actions>
      <!--      <span class="icon-hover"> <IconThumbUp /> </span>-->
      <span class="icon-hover" @click="doShare"> <IconShareInternal /> </span>
    </template>
    <template #cover>
      <div
        :style="{
          height: '204px',
          overflow: 'hidden',
        }"
      >
        <img
          :style="{ width: '100%', transform: 'translateY(-20px)' }"
          :alt="app.appName"
          :src="app.appIcon"
        />
      </div>
    </template>
    <a-card-meta :title="app.appName" :description="app.appDesc">
      <template #avatar>
        <div
          :style="{ display: 'flex', alignItems: 'center', color: '#1D2129' }"
        >
          <a-avatar
            :size="24"
            :image-url="app.user?.userAvatar"
            :style="{ marginRight: '8px' }"
          />
          <a-typography-text>
            {{ app.user?.userName ?? "无名" }}
          </a-typography-text>
        </div>
      </template>
    </a-card-meta>
  </a-card>
  <ShareModal :link="shareLink" title="应用分享" ref="shareModalRef" />
</template>

<script setup lang="ts">
import { IconShareInternal } from "@arco-design/web-vue/es/icon";
import API from "@/api";
import { defineProps, ref, withDefaults } from "vue";
import { useRouter } from "vue-router";
import ShareModal from "@/components/ShareModal.vue";

interface Props {
  app: API.AppVO;
}

// 资源默认值
const props = withDefaults(defineProps<Props>(), {
  app: () => {
    return {};
  },
});

// 路由管理器
const router = useRouter();

// 点击卡片跳转到详情页
const doCardClick = () => {
  router.push(`/app/detail/${props.app.id}`);
};

// 分享弹窗的引用
const shareModalRef = ref();

// 动态拼接分享链接
const shareLink = `${window.location.protocol}//${window.location.host}/app/detail/${props.app.id}`;

// 分享
const doShare = (e: Event) => {
  if (shareModalRef.value) {
    shareModalRef.value.openModal();
  }
  // 阻止冒泡，防止跳转到详情页
  e.stopPropagation();
};
</script>
<style scoped>
.appCard {
  cursor: pointer;
}

.icon-hover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transition: all 0.1s;
}

.icon-hover:hover {
  background-color: rgb(var(--gray-2));
}
</style>
