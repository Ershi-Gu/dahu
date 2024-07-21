module.exports = {
  root: true,
  env: {
    node: true,
  },
  extends: [
    "plugin:vue/vue3-essential",
    "eslint:recommended",
    "@vue/typescript/recommended",
    "plugin:prettier/recommended", // 添加 Prettier 插件
  ],
  parserOptions: {
    ecmaVersion: 2020,
  },
  plugins: ["prettier"],
  rules: {
    // 禁用可能与 Prettier 冲突的 ESLint 规则
    "prettier/prettier": "error", // 使用 Prettier 的规则检测
    "no-console": process.env.NODE_ENV === "production" ? "warn" : "off",
    "no-debugger": process.env.NODE_ENV === "production" ? "warn" : "off",
    "linebreak-style": "off", // 可能与 Prettier 的行尾冲突规则禁用
    "@typescript-eslint/ban-ts-comment": "off", // 禁用禁止使用 @ts-ignore 的规则"
  },
};
