# Spring Boot + Maven 项目编程规范

## 1. 项目结构规范

### 1.1 标准分层结构
```
src/main/java/{basePackage}/
├── controller/      # 控制器层，处理HTTP请求
├── service/         # 业务逻辑层
│   └── impl/        # 业务逻辑实现
├── mapper/          # 数据访问层（MyBatis）或 repository（JPA）
├── entity/          # 实体类（数据库映射）
├── dto/             # 数据传输对象
│   ├── request/     # 请求DTO
│   └── response/    # 响应DTO
├── vo/              # 视图对象
├── config/          # 配置类
├── common/          # 公共模块
│   ├── constant/    # 常量定义
│   ├── enums/       # 枚举类
│   ├── exception/   # 自定义异常
│   └── util/        # 工具类
├── interceptor/     # 拦截器
├── filter/          # 过滤器
└── aspect/          # 切面（AOP）

src/main/resources/
├── application.yml           # 主配置文件
├── application-dev.yml       # 开发环境配置
├── application-prod.yml      # 生产环境配置
├── application-test.yml      # 测试环境配置
├── mapper/                   # MyBatis XML映射文件
├── static/                   # 静态资源
└── templates/                # 模板文件
```

### 1.2 包命名规范
- 基础包名：`com.{company}.{project}`
- 全部小写，使用点分隔
- 示例：`com.example.userservice`

---

## 2. 命名规范

### 2.1 类命名
- **Controller**: `{模块}Controller`，如 `UserController`
- **Service接口**: `{模块}Service`，如 `UserService`
- **Service实现**: `{模块}ServiceImpl`，如 `UserServiceImpl`
- **Mapper/Repository**: `{模块}Mapper` 或 `{模块}Repository`，如 `UserMapper`
- **Entity**: 使用业务名词，如 `User`, `Order`
- **DTO**: `{模块}{用途}DTO`，如 `UserCreateDTO`, `UserResponseDTO`
- **VO**: `{模块}VO`，如 `UserVO`
- **配置类**: `{功能}Config`，如 `RedisConfig`, `WebConfig`
- **工具类**: `{功能}Util` 或 `{功能}Utils`，如 `DateUtil`
- **常量类**: `{模块}Constant`，如 `SystemConstant`
- **枚举类**: `{含义}Enum`，如 `StatusEnum`, `UserTypeEnum`

### 2.2 方法命名
- **Controller方法**: 使用RESTful风格
  - 查询列表：`list{Resource}` 或 `get{Resource}List`
  - 查询单个：`get{Resource}ById`
  - 创建：`create{Resource}` 或 `add{Resource}`
  - 更新：`update{Resource}`
  - 删除：`delete{Resource}`
  
- **Service方法**: 体现业务逻辑
  - 查询：`get`, `find`, `query`, `list`
  - 保存：`save`, `create`, `add`
  - 更新：`update`, `modify`
  - 删除：`delete`, `remove`
  - 判断：`is`, `has`, `exists`, `check`
  - 转换：`convert`, `transform`, `parse`

### 2.3 变量命名
- 使用驼峰命名法（camelCase）
- 布尔类型变量使用 `is`, `has`, `can`, `should` 开头
- 集合类型添加复数或 `List`, `Map` 后缀
- 示例：`userName`, `isActive`, `userList`, `userMap`

### 2.4 常量命名
- 全部大写，使用下划线分隔
- 示例：`MAX_SIZE`, `DEFAULT_PAGE_SIZE`, `SUCCESS_CODE`

---

## 3. 代码风格规范

### 3.1 注解顺序
类注解顺序：
```java
@Component / @Service / @Controller / @RestController / @Repository
@Slf4j
@RequiredArgsConstructor
@Validated
其他注解
```

方法注解顺序：
```java
@Override
@Transactional
@GetMapping / @PostMapping / etc.
@Validated
@ApiOperation (Swagger)
其他注解
```

### 3.2 依赖注入
- **优先使用构造器注入**（配合 Lombok 的 `@RequiredArgsConstructor`）
- 避免使用 `@Autowired` 字段注入

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
}
```

### 3.3 返回值统一封装
所有接口返回统一的响应格式：

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
```

### 3.4 分页参数统一
```java
@Data
public class PageQuery {
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;
}
```

---

## 4. 注释规范

### 4.1 类注释
```java
/**
 * 用户服务实现类
 * 负责用户的CRUD操作及业务逻辑处理
 *
 * @author 作者名
 * @since 2025-12-13
 */
@Service
public class UserServiceImpl implements UserService {
}
```

### 4.2 方法注释
```java
/**
 * 根据用户ID查询用户信息
 *
 * @param userId 用户ID
 * @return 用户信息
 * @throws BusinessException 用户不存在时抛出
 */
public UserVO getUserById(Long userId) {
}
```

### 4.3 复杂逻辑注释
- 对于复杂的业务逻辑，添加行内注释说明
- 使用中文注释，清晰表达意图

---

## 5. 异常处理规范

### 5.1 自定义业务异常
```java
public class BusinessException extends RuntimeException {
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

### 5.2 全局异常处理器
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.error("系统异常，请联系管理员");
    }
}
```

### 5.3 异常抛出规范
- **Service层**：抛出具体的业务异常
- **Controller层**：不处理异常，交由全局异常处理器
- 使用 `Objects.requireNonNull()` 或 `Assert` 进行参数校验

---

## 6. 数据库操作规范

### 6.1 实体类规范
```java
@Data
@TableName("t_user")  // MyBatis-Plus
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic  // 逻辑删除
    private Integer deleted;
}
```

### 6.2 表命名规范
- 表名：`t_{模块}_{表名}`，如 `t_user`, `t_order`
- 字段名：小写下划线分隔，如 `user_name`, `create_time`
- 主键：统一使用 `id`
- 必备字段：`create_time`, `update_time`, `deleted`（逻辑删除）

### 6.3 SQL编写规范
- 禁止使用 `SELECT *`，明确指定字段
- 分页查询必须使用 MyBatis-Plus 的 `Page` 或 PageHelper
- 复杂查询优先使用 XML 方式，简单查询使用注解
- 大批量操作使用批处理

### 6.4 事务管理
```java
@Transactional(rollbackFor = Exception.class)
public void createOrder(OrderCreateDTO dto) {
    // 业务逻辑
}
```
- 只在 Service 层使用 `@Transactional`
- 明确指定 `rollbackFor = Exception.class`
- 避免事务方法调用事务方法

---

## 7. API 设计规范

### 7.1 RESTful API 规范
```java
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    
    // GET /api/users - 查询列表
    @GetMapping
    public Result<List<UserVO>> listUsers(UserQueryDTO query) {
    }
    
    // GET /api/users/{id} - 查询单个
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
    }
    
    // POST /api/users - 创建
    @PostMapping
    public Result<Long> createUser(@RequestBody @Validated UserCreateDTO dto) {
    }
    
    // PUT /api/users/{id} - 更新
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, 
                                    @RequestBody @Validated UserUpdateDTO dto) {
    }
    
    // DELETE /api/users/{id} - 删除
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
    }
}
```

### 7.2 URL 命名规范
- 使用小写字母和短横线分隔：`/api/user-orders`
- 使用名词复数形式：`/api/users`
- 资源嵌套不超过2层：`/api/users/{userId}/orders`

### 7.3 参数校验
```java
@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度为3-20")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", 
             message = "密码至少8位，包含大小写字母和数字")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

---

## 8. 日志规范

### 8.1 日志级别使用
- **ERROR**: 系统错误、异常信息
- **WARN**: 警告信息，如参数校验失败
- **INFO**: 重要业务流程节点，如用户登录、订单创建
- **DEBUG**: 调试信息，开发环境使用

### 8.2 日志格式
```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    public void createUser(UserCreateDTO dto) {
        log.info("开始创建用户，username: {}", dto.getUsername());
        
        try {
            // 业务逻辑
            log.info("用户创建成功，userId: {}", userId);
        } catch (Exception e) {
            log.error("用户创建失败，username: {}, 错误信息: {}", 
                      dto.getUsername(), e.getMessage(), e);
            throw new BusinessException("用户创建失败");
        }
    }
}
```

### 8.3 日志注意事项
- 使用 `@Slf4j` 而非手动创建 Logger
- 使用占位符 `{}` 而非字符串拼接
- 敏感信息（密码、身份证等）不记录到日志
- 异常日志必须打印堆栈信息

---

## 9. 配置文件规范

### 9.1 application.yml 结构
```yaml
spring:
  profiles:
    active: dev
  application:
    name: user-service
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db_name?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

server:
  port: 8080
  servlet:
    context-path: /

logging:
  level:
    root: INFO
    com.example: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 9.2 配置项规范
- 敏感信息使用环境变量：`${ENV_VAR:default_value}`
- 不同环境使用不同配置文件：`application-{env}.yml`
- 自定义配置使用 `@ConfigurationProperties`

---

## 10. Maven 依赖管理规范

### 10.1 pom.xml 结构
```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.1.5</spring-boot.version>
    <lombok.version>1.18.30</lombok.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
</properties>

<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- 其他依赖按功能分组，添加注释 -->
</dependencies>
```

### 10.2 依赖管理原则
- 使用 Spring Boot 的依赖管理，避免手动指定版本
- 同类依赖集中管理，添加注释说明
- 定期更新依赖版本，关注安全漏洞

---

## 11. 其他最佳实践

### 11.1 常量定义
```java
public class SystemConstant {
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;
    
    private SystemConstant() {
        // 私有构造，防止实例化
    }
}
```

### 11.2 枚举使用
```java
@Getter
@AllArgsConstructor
public enum StatusEnum {
    ACTIVE(1, "激活"),
    INACTIVE(0, "未激活");
    
    private final Integer code;
    private final String desc;
    
    public static StatusEnum fromCode(Integer code) {
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知状态码: " + code);
    }
}
```

### 11.3 工具类规范
- 工具类使用静态方法，私有构造
- 方法单一职责，可复用
- 添加完整的注释和参数校验

### 11.4 DTO 和 Entity 转换
- 使用 MapStruct 或 BeanUtils 进行转换
- 避免手动 set/get 赋值
- Service 层负责转换，Controller 不直接操作 Entity

---

## 12. 代码检查清单

提交代码前检查：
- [ ] 代码格式化（使用 IDE 格式化功能）
- [ ] 无编译错误和警告
- [ ] 添加必要的注释
- [ ] 参数校验完整
- [ ] 异常处理完善
- [ ] 日志记录合理
- [ ] 事务配置正确
- [ ] 单元测试通过
- [ ] 敏感信息已移除
- [ ] SQL 性能已优化

---

**遵循以上规范，将使你的 Spring Boot 项目代码更加规范、可维护和高效！**
