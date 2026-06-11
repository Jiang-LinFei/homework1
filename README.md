# 图书馆信息管理系统

JavaWeb 课程设计。基于 JSP + Servlet + JDBC 实现的图书馆信息管理系统，面向**普通管理员**与**系统管理员**两类用户，覆盖图书入库、读者登记、借阅登记、归还登记、图书查询、逾期提醒等图书室日常业务，做到账目清晰、有据可查。

## 技术栈

| 项目 | 版本/说明 |
| ---- | -------- |
| JDK | 1.8（源码以 `--release 8` 编译） |
| Web 容器 | Tomcat 9.0 |
| 数据库 | MySQL 8.0（兼容 MariaDB 10.x） |
| 视图 | JSP + JSTL 1.2 |
| 前端 | 原生 HTML / CSS / JavaScript（无第三方框架） |
| 数据访问 | 原生 JDBC（mysql-connector-java 8.0.18） |

## 功能模块

- **登录鉴权**：按角色（普通管理员 / 系统管理员）登录，密码采用 `MD5(明文+盐)` 存储；`AuthFilter` 统一拦截未登录访问，系统管理专属功能仅系统管理员可见。
- **工作台**：馆藏图书、登记读者、借出中、逾期未还等关键指标一览，并展示逾期提醒明细。
- **图书管理**：图书入库（新增）、编辑、删除、按书名/作者/ISBN/分类的查询与分页。
- **读者管理**：读者登记、编辑、注销、按证号/姓名/电话的查询与分页。
- **借阅登记**：选择图书与读者借出，自动校验库存与读者可借上限，事务保证库存与借阅记录一致。
- **归还登记**：办理归还，库存自动 +1。
- **借阅记录 / 逾期提醒**：按状态（借出中/已归还）筛选；逾期 = 借出中且超过应还日期，运行时动态判断。
- **系统管理（仅系统管理员）**：普通管理员账号的增删改查、图书分类维护。

## 目录结构

```
db/library.sql                 数据库初始化脚本（建库 + 演示数据）
src/com/library/
    entity/                    实体类（含 LoginUser 会话对象、PageBean 分页对象）
    util/   DBUtil/MD5Util     数据库连接、密码加密
    dao/                       数据访问层（JDBC）
    filter/                    编码过滤器、登录/权限过滤器
    servlet/                   控制器
    db.properties              数据库连接配置
WebContent/
    login.jsp / index.jsp      登录页 / 入口页
    WEB-INF/web.xml            Web 配置（Servlet/Filter 用注解注册）
    WEB-INF/views/             业务页面（受保护，由 Servlet 转发）
    WEB-INF/lib/               依赖 jar（mysql-connector、jstl）
    css/ js/                   静态资源
```

## 部署运行

1. **初始化数据库**（MySQL 8.0）：
   ```bash
   mysql -uroot -p < db/library.sql
   ```
   脚本会创建 `library_db` 库及全部表，并写入演示数据。

2. **修改连接配置**（如与默认不同）：编辑 `src/com/library/...` 同级的 `src/db.properties`，
   设置 `jdbc.url` / `jdbc.username` / `jdbc.password`。

3. **编译**（输出到 `WebContent/WEB-INF/classes`，并把配置文件一并拷入）：
   ```bash
   javac --release 8 -encoding UTF-8 \
     -cp "$TOMCAT/lib/servlet-api.jar:$TOMCAT/lib/jsp-api.jar:WebContent/WEB-INF/lib/*" \
     -d WebContent/WEB-INF/classes $(find src -name "*.java")
   cp src/db.properties WebContent/WEB-INF/classes/
   ```

4. **部署到 Tomcat 9**：将 `WebContent` 目录作为 web 应用（例如复制为 `$TOMCAT/webapps/library`），
   启动 Tomcat 后访问 `http://localhost:8080/library/`。

> 也可直接在 Eclipse 中以 Dynamic Web Project 导入：源码目录 `src`，Web 资源目录 `WebContent`。

## 演示账号

| 角色 | 账号 | 密码 |
| ---- | ---- | ---- |
| 系统管理员 | `admin` | `admin123` |
| 普通管理员 | `librarian` | `123456` |
| 普通管理员 | `zhangsan` | `123456` |
