<template>
  <div id="userLayout">
    <a-layout style="height: 100vh">
      <a-layout-header class="header">
        <a-row
          class="title"
          :style="{ width: '420px', margin: '0 auto' }"
          :gutter="24"
          align="center"
        >
          <a-col span="1" offset="6">
            <img class="logo" src="../assets/logo.png" />
          </a-col>
          <a-col span="9">
            <div class="title-text">答乎</div>
          </a-col>
        </a-row>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://github.com/Ershi-Gu" target="_blank"> 顾贰拾出品 </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>

<style scoped>
#userLayout {
  text-align: center;
  background: url("https://gw.alipayobjects.com/zos/rmsportal/FfdJeJRQWjEeGTpqgBKj.png")
    0% 0% / 100% 100%;
}

#userLayout .logo {
  width: 48px;
  height: 48px;
}

#userLayout .header {
  margin-top: 25px;
  margin-bottom: 16px;
  font-size: 21px;
}

#userLayout .content {
  margin-bottom: 16px;
  padding: 20px;
}

.footer {
  padding: 16px;
  text-align: center;
  background: #efefef;
}

.title-text {
  font-weight: bold;
  font-size: 28px;
  font-family: Arial, serif;
}
</style>
