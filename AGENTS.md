#智能体宪法与工程铁律 (AGENTS.md)

> **📌 智能体定位**：你是一名精通现代全栈开发（Vue 3 + Spring Boot 3 + MySQL 8）的顶级资深全栈架构师兼代码审查员。在参与本项目的代码编写、调试与重构时，你必须严格遵守以下宪法与工程军规。

---

## 1. 核心技术栈与版本基准 (Tech Stack Versions)


### 前端 (Frontend)

- **框架**：Vue 3（Composition API，`<script setup lang="ts">`）
- **构建工具**：Vite (v5+)
- **语言**：TypeScript（Strict 严格模式）
- **样式**：Tailwind CSS (v3+)
- **状态与通信**：Pinia (v2+)、Axios（带拦截器封装）

### 后端 (Backend)

- **语言**：Java 17（优先使用 Record、Pattern Matching、Stream 等现代语法）
- **框架**：Spring Boot 3.5.16
- **数据访问**：MyBatis 3.5+（配合 MyBatis-Spring-Boot-Starter 3.0+，使用 XML 映射文件 + Mapper 接口的方式）
- **分页插件**：PageHelper 6.x
- **参数校验**：Jakarta Validation（`jakarta.validation-api`）
- **辅助工具**：Lombok、MapStruct（Entity/DTO/VO 转换）
- **事务管理**：Spring `@Transactional`（明确 `rollbackFor`，禁止滥用）

### 数据与基础设施 (Infra)

- **数据库**：MySQL 8.0+（InnoDB 引擎，`utf8mb4_unicode_ci` 字符集）
- **缓存**：Redis 7.0+
- **网关与反向代理**：Nginx 1.24+

---

## 2. 绝对不可逾越的编码红线 (Negative Constraints)


* ❌ **禁止私自脑补字段：所有实体字段、Java DTO/VO 属性、MyBatis 映射文件中的列名与 resultMap 属性、数据库列名与前端 TypeScript Interface 必须 100% 镜像对齐 @docs/01-requirements/GLOSSARY.md 与 @sql/schema.sql。
* ❌ **禁止使用浮点数表示金额：全链路（数据库、后端 DTO/Service、前端状态）统一使用无符号整数（单位：分，priceCents / price_cents），严禁使用 float / double。前端仅在向用户展示时除以 100。
* ❌ ** 禁止裸露 Controller 入参：Controller 接收的所有入参对象前必须加 @Valid 注解，必须在 DTO 中配置 Jakarta Validation 注解与中文错误提示。
* ❌ **禁止平行越权 (IDOR 漏洞)：更新、下架、预订、删除商品时，必须在 Service 层强制校验 sellerId == currentUserId，严禁仅凭商品主键 ID 直接修改。
* ❌ **禁止 Entity 穿透前端：严禁将 MyBatis 映射的实体对象直接通过 Controller 返回给前端，必须通过专门的 VO（View Object）进行数据脱敏隔离。
* ❌ **禁止内联样式与 any：前端禁止在 Vue 组件中写内联 style="..."，必须使用 Tailwind CSS 语义化原子类；TypeScript 严禁使用 any。
* ❌ **禁止 XML 中拼接 SQL 字符串：MyBatis Mapper XML 中严禁使用 ${} 进行参数拼接，必须使用 #{} 预编译占位符，防止 SQL 注入；动态排序、动态表名等特殊场景需使用白名单校验。
* ❌ **禁止在 Mapper 接口使用 @Select/@Update 等注解编写复杂 SQL：简单 CRUD 可用注解，凡是涉及多表关联、动态条件、<foreach> 循环等场景，必须写入 XML 映射文件统一维护。
* ❌ **禁止绕过 @MapperScan 或 Mapper 接口命名规范：Mapper 接口必须以 Mapper 结尾，且必须被 @MapperScan 或 @Mapper 显式扫描，禁止在 Service 中通过 SqlSession 手动获取 Mapper。
* ❌ ** 禁止缺少 resultMap 或开启驼峰映射后仍手写全字段映射：多表关联、字段名与属性名不一致的查询结果必须显式定义 <resultMap>；已开启 mapUnderscoreToCamelCase 的单表查询可直接复用实体映射，避免冗余。
* ❌ **禁止在批量操作中循环调用单条 SQL：批量插入/更新必须使用 <foreach> 配合 INSERT INTO ... VALUES (...),(...) 或 ExecutorType.BATCH，严禁在 Service 层 for 循环单条执行。
* ❌ **禁止忽略 @Transactional 传播与回滚规则：涉及多表写操作的 Service 方法必须标注 @Transactional(rollbackFor = Exception.class)，严禁仅依赖默认的 RuntimeException 回滚。
* ❌ **禁止使用物理外键与级联删除：数据库层禁止建立 FOREIGN KEY 与 ON DELETE CASCADE，关联完整性由 Service 层保证，删除采用逻辑删除（is_deleted 标记）。
* ❌ **禁止在 MyBatis 中使用 SELECT ：所有查询必须显式列出所需列名，防止表结构变更导致的隐式耦合与性能损耗。

---

## 3. 统一响应格式封装 (Unified Response Envelope)

所有 Controller 对外暴露的 HTTP 接口，**必须**统一返回 `Result<T>` 包装对象，严禁直接返回裸数据、裸 VO、裸 `Map` 或 `ResponseEntity` 泛型体。前端 Axios 拦截器亦基于该统一结构进行解包与错误提示。

### 3.1 泛型响应类定义

必须定义全局唯一的泛型响应类 `Result<T>`，位于 `backend/src/main/java/com/campusswap/common/Result.java`，字段固定为：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `int` | 业务状态码（非 HTTP 状态码） |
| `message` | `String` | 提示信息（成功/失败原因，中文） |
| `data` | `T` | 业务数据载荷，可为 `null` |
| `timestamp` | `long` | 响应生成时间戳（毫秒，`System.currentTimeMillis()`） |

```java
package com.campusswap.common;

import lombok.Getter;

/**
 * 全局统一响应封装。
 * 所有 Controller 必须返回 Result<T>，禁止裸露返回业务数据。
 */
@Getter
public class Result<T> {

    private final int code;
    private final String message;
    private final T data;
    private final long timestamp;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }
}