# JWT 证书目录

此目录存放 JWT RSA 密钥对，不提交到 Git。

生成命令（在 Git Bash 或 WSL 中执行）：

```bash
cd backend/zhitu-auth/src/main/resources/certs
openssl genrsa -out jwt-private.pem 2048
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
```
