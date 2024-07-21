const { generateService } = require("@umijs/openapi");

generateService({
  requestLibPath: "import request from '@/globalRequest'",
  schemaPath: "http://localhost:8081/api/v2/api-docs",
  serversPath: "./src",
});
