<template>
  <a-button type="outline" @click="handleClick">AI 生成题目</a-button>
  <a-drawer
    :width="400"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    unmountOnClose
  >
    <template #title>AI 生成题目</template>
    <div>
      <a-form
        :model="form"
        label-align="left"
        auto-label-width
        @submit="handleSubmit"
      >
        <a-form-item label="应用 id">
          {{ appId }}
        </a-form-item>
        <a-form-item label="应用名称">
          {{ appData.appName }}
        </a-form-item>
        <a-form-item field="questionNumber" label="题目数量">
          <a-input-number
            min="0"
            max="20"
            v-model="form.questionNumber"
            placeholder="请输入题目数量"
          />
        </a-form-item>
        <a-form-item field="optionNumber" label="选项数量">
          <a-input-number
            min="0"
            max="6"
            v-model="form.optionNumber"
            placeholder="请输入选项数量"
          />
        </a-form-item>
        <a-form-item>
          <a-button
            :loading="submitting"
            type="primary"
            html-type="submit"
            style="width: 120px"
          >
            {{ submitting ? "生成中" : "一键生成" }}
          </a-button>
          <a-button
            :loading="sseSubmitting"
            style="width: 120px"
            @click="handleSSESubmit"
          >
            {{ sseSubmitting ? "生成中" : "实时生成" }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { defineProps, reactive, ref, withDefaults } from "vue";
import API from "@/api";
import { aiGenerateQuestionUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";
import { Message } from "@arco-design/web-vue";
import { EventSourcePolyfill } from "event-source-polyfill";

interface Props {
  appId: string;
  appData: API.AppVO;
  onSuccess?: (result: API.QuestionContentDTO[]) => void;
  onSSESuccess?: (result: API.QuestionContentDTO) => void;
  onSSEStart?: (event: any) => void;
  onSSEClose?: (event: any) => void;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const form = reactive({
  optionNumber: 2,
  questionNumber: 5,
} as API.AiGenerateQuestionRequest);

// 抽屉可见变量
const visible = ref(false);
// 提交按钮状态变量
const submitting = ref(false);
const sseSubmitting = ref(false);
// 抽屉状态处理
const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

/**
 * 提交
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  submitting.value = true;
  const loadingMessage = Message.loading({
    content: "正在生成题目...请勿关闭当前页面",
    duration: 100000,
  });
  const res = await aiGenerateQuestionUsingPost({
    appId: props.appId as any,
    ...form,
  });
  loadingMessage.close();
  // 使用类型断言告诉编译器 res.data 不会为 undefined
  const resData = res.data as { code: number; data: any[]; message: string };
  if (resData.code === 0 && resData.data.length > 0) {
    if (props.onSuccess) {
      props.onSuccess(resData.data);
    } else {
      message.success("生成题目成功");
    }
    // 关闭抽屉
    handleCancel();
  } else {
    message.error("操作失败，" + res.data.message);
  }
  submitting.value = false;
};

/**
 * 提交（实时生成）
 */
const handleSSESubmit = async () => {
  if (!props.appId) {
    return;
  }
  sseSubmitting.value = true;
  console.log(sseSubmitting.value);
  // 创建 SSE 请求，axios不支持sse，需要手动编写
  const eventSource = new EventSourcePolyfill(
    "http://localhost:8081/api/question/ai_generate/sse" +
      `?appId=${props.appId}&optionNumber=${form.optionNumber}&questionNumber=${form.questionNumber}`,
    {
      headers: {
        Authorization: localStorage.getItem("token") || "",
      },
    }
  );
  let first = true;
  // 接收到消息时触发
  eventSource.onmessage = function (event) {
    if (first) {
      handleCancel();
      first = !first;
    }
    props.onSSESuccess?.(JSON.parse(event.data));
  };
  // 报错或连接关闭时触发
  eventSource.onerror = function (event) {
    console.log("关闭连接");
    props.onSSEClose?.(event);
    eventSource.close();
  };
  eventSource.onopen = function (event) {
    console.log("建立连接");
    props.onSSEStart?.(event);
    handleCancel();
  };
  sseSubmitting.value = false;
};
</script>
