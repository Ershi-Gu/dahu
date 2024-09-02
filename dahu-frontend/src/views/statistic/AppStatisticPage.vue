<template>
  <div id="AppStatisticPage">
    <h2>热门应用统计</h2>
    <v-charts
      :option="appCountOptions"
      style="height: 300px"
      @click="handleBarClick"
    />
    <div style="margin-bottom: 52px" />
    <h2>应用结果统计</h2>
    <div class="search-bar">
      <a-input-search
        :style="{ width: '320px' }"
        placeholder="输入 appId"
        button-text="搜索"
        size="large"
        search-button
        @search="(value) => loadAnswerResultCountData(value)"
      />
    </div>
    <div style="margin-bottom: 16px" />
    <v-charts :option="appAnswerResultCountOptions" style="height: 300px" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from "vue";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";
import {
  getAnswerResultUsingGet,
  getAppAnswerCountUsingGet,
} from "@/api/statisticController";
import VCharts from "vue-echarts";
import "echarts";
import AppAnswerCountDTO = API.AppAnswerCountDTO;
import AnswerResultCountDTO = API.AnswerResultCountDTO;

// App答题情况统计数据
const appAnswerCountList = ref<AppAnswerCountDTO[]>([]);
// 用户答题结果分析数据
const answerResultCountList = ref<AnswerResultCountDTO[]>([]);

/**
 * 加载App答题情况统计数据
 */
const loadAppAnswerCountData = async () => {
  const res = await getAppAnswerCountUsingGet();
  if (res.data.code === 0) {
    appAnswerCountList.value = res.data.data || [];
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

/**
 * 加载用户答题结果分析数据
 */
const loadAnswerResultCountData = async (appId: string) => {
  if (!appId) {
    return;
  }
  const res = await getAnswerResultUsingGet({
    appId: appId as any,
  });
  if (res.data.code === 0) {
    answerResultCountList.value = res.data.data || [];
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

// 初始化触发数据的重新加载
watchEffect(() => {
  loadAppAnswerCountData();
});

// 参数改变时触发加载
watchEffect(() => {
  loadAnswerResultCountData("");
});

// 处理柱状图点击事件
const handleBarClick = (params: any) => {
  const dataIndex = params.dataIndex;
  const appData = appAnswerCountList.value[dataIndex];

  if (appData && appData.appId) {
    copyToClipboard(String(appData.appId));
  }
};

// 复制到剪贴板
const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text).then(
    () => {
      message.success(`应用ID ${text} 已复制到剪贴板`);
    },
    (err) => {
      message.error("复制失败: " + err);
    }
  );
};

// App使用情况统计选项
const appCountOptions = computed(() => {
  return {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
      formatter: (params: any) => {
        const dataIndex = params[0].dataIndex;
        const appData = appAnswerCountList.value[dataIndex];
        return `应用名: ${appData.appName}<br/>应用ID: ${appData.appId}<br/>用户回答数: ${appData.userAnswerCount}`;
      },
    },
    legend: {},
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "value",
      boundaryGap: [0, 0.01],
    },
    yAxis: {
      name: "应用名",
      type: "category",
      data: appAnswerCountList.value.map((item) => item.appName),
    },
    series: [
      {
        name: "用户回答数",
        type: "bar",
        data: appAnswerCountList.value.map((item) => ({
          value: item.userAnswerCount,
          appName: item.appName,
          appId: item.appId,
        })),
      },
    ],
  };
});

// App结果统计选项
const appAnswerResultCountOptions = computed(() => {
  return {
    tooltip: {
      trigger: "item",
    },
    legend: {
      orient: "vertical",
      left: "left",
    },
    series: [
      {
        name: "应用答案结果分布",
        type: "pie",
        radius: "50%",
        data: answerResultCountList.value.map((item) => {
          return { value: item.resultCount, name: item.resultName };
        }),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
});
</script>

<style scoped>
.list-demo-action-layout .image-area {
  width: 183px;
  height: 119px;
  overflow: hidden;
  border-radius: 2px;
}

.list-demo-action-layout .list-demo-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--color-fill-3);
}

.list-demo-action-layout .image-area img {
  width: 100%;
}

.list-demo-action-layout .arco-list-item-action .arco-icon {
  margin: 0 4px;
}
</style>
