// @ts-ignore
/* eslint-disable */
import request from '@/globalRequest';

/** getCaptcha GET /api/captcha/get */
export async function getCaptchaUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseCaptchaVO_>('/api/captcha/get', {
    method: 'GET',
    ...(options || {}),
  });
}
