// @ts-ignore
/* eslint-disable */
import request from '@/globalRequest';

/** getAppAnswerCount GET /api/app/statistic/app_answer_count */
export async function getAppAnswerCountUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListAppAnswerCountDTO_>('/api/app/statistic/app_answer_count', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getAnswerResult GET /api/app/statistic/app_answer_result_count */
export async function getAnswerResultUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAnswerResultUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListAnswerResultCountDTO_>(
    '/api/app/statistic/app_answer_result_count',
    {
      method: 'GET',
      params: {
        ...params,
      },
      ...(options || {}),
    },
  );
}
