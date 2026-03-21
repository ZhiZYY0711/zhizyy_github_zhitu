# Nacos 配置中心内容

在 Nacos 控制台（http://localhost:8848/nacos）中，分别在 `dev` 和 `prod` 命名空间下创建以下配置。

**重要提示：**
- Spring Boot 3.x 使用 `spring.data.redis.*` 而不是 `spring.redis.*`
- 密码字段建议使用引号包裹：`password: "123456"`
- 配置格式必须选择 `YAML`

---

## dev 命名空间（IDE 本地开发）

### zhitu-db.yaml

```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:15432/zhitu_cloud
    username: postgres
    password: 123456
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
```

### zhitu-redis.yaml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: "123456"
      database: 0
      timeout: 10s
      lettuce:
        pool:
          min-idle: 0
          max-idle: 8
          max-active: 8
          max-wait: -1ms
```

### zhitu-minio.yaml

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: zhitu-files
```

---

## prod 命名空间（Docker 容器部署）

> 命名空间 ID：`9f47ef46-0532-4144-9352-de979a04b191`

### zhitu-db.yaml

```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://postgres:5432/zhitu_cloud
    username: postgres
    password: 123456
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
```

### zhitu-redis.yaml

```yaml
spring:
  data:
    redis:
      host: redis
      port: 6379
      password: "123456"
      database: 0
      timeout: 10s
      lettuce:
        pool:
          min-idle: 0
          max-idle: 8
          max-active: 8
          max-wait: -1ms
```

### zhitu-minio.yaml

```yaml
minio:
  endpoint: http://minio:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: zhitu-files
```
