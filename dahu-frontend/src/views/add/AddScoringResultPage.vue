<template>
  <div id="addScoringResultPage">
    <a-button type="primary" @click="goBack" style="margin-bottom: 16px">
      &lt; 返回上一页
    </a-button>
    <h2 style="margin-bottom: 32px">设置评分</h2>
    <a-form
      :model="form"
      :style="{ width: '480px' }"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item label="应用 id">
        {{ appId }}
      </a-form-item>
      <a-form-item label="应用名称">
        {{ data.appName }}
      </a-form-item>
      <a-form-item v-if="updateId" label="修改评分 id">
        {{ updateId }}
      </a-form-item>
      <a-form-item field="resultName" label="结果名称">
        <a-input v-model="form.resultName" placeholder="请输入结果名称" />
      </a-form-item>
      <a-form-item field="resultDesc" label="结果描述">
        <a-input v-model="form.resultDesc" placeholder="请输入结果描述" />
      </a-form-item>
      <a-form-item field="resultPicture" label="结果图标">
        <a-input
          v-model="form.resultPicture"
          placeholder="请输入结果图标地址"
        />
      </a-form-item>
      <a-form-item
        v-if="APP_TYPE_MAP[data.appType] === '测评类'"
        field="resultProp"
        label="结果集"
      >
        <a-input-tag
          v-model="form.resultProp"
          placeholder="请输入一个结果集后按一次回车，例如AAA回车"
          allow-clear
        />
      </a-form-item>
      <a-form-item
        v-if="APP_TYPE_MAP[data.appType] === '得分类'"
        field="resultScoreRange"
        label="分数线"
      >
        <a-input-number
          v-model="form.resultScoreRange"
          placeholder="请输入分数线"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px">
          提交
        </a-button>
      </a-form-item>
    </a-form>
    <h2 style="margin-bottom: 32px">评分管理</h2>
    <ScoringResultTable :appId="appId" :doUpdate="doUpdate" ref="tableRef" />
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref, watchEffect, withDefaults } from "vue";
import API from "@/api";
import { useRouter } from "vue-router";
import ScoringResultTable from "@/views/add/components/ScoringResultTable.vue";
import {
  addScoringResultUsingPost,
  editScoringResultUsingPost,
} from "@/api/scoringResultController";
import message from "@arco-design/web-vue/es/message";
import { APP_TYPE_MAP } from "@/constant/app";
import { getAppVoByIdUsingGet } from "@/api/appController";

interface Props {
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const router = useRouter();

// 返回上一个页面
const goBack = () => {
  router.back();
};

const tableRef = ref();

// 表单参数
const form = ref({
  resultDesc: "",
  resultName: "",
  resultPicture: "",
} as API.ScoringResultAddRequest);

const updateId = ref<any>();

const doUpdate = (scoringResult: API.ScoringResultVO) => {
  updateId.value = scoringResult.id;
  form.value = scoringResult;
};

// APP 应用
const data = ref<API.AppVO>({});

/**
 * 加载应用数据
 */
const loadAppData = async () => {
  const res = await getAppVoByIdUsingGet({
    id: props.appId as any,
  });
  if (res.data.code === 0) {
    data.value = res.data.data as any;
  } else {
    message.error("获取应用数据失败，" + res.data.message);
  }
};

// 加载应用数据
watchEffect(() => {
  loadAppData();
});

/**
 * 提交
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  let res: any;
  // 如果是修改
  if (updateId.value) {
    res = await editScoringResultUsingPost({
      id: updateId.value as any,
      ...form.value,
    });
  } else {
    // 创建
    res = await addScoringResultUsingPost({
      appId: props.appId as any,
      ...form.value,
    });
  }
  if (res.data.code === 0) {
    message.success("操作成功");
  } else {
    message.error("操作失败，" + res.data.message);
  }
  if (tableRef.value) {
    tableRef.value.loadData();
    updateId.value = undefined;
  }
};
</script>
