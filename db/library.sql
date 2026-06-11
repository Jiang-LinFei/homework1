-- =============================================================
--  图书馆信息管理系统  数据库初始化脚本
--  环境：MySQL 8.0 / 兼容 MariaDB 10.x
--  字符集：utf8mb4（支持中文及 emoji）
--  说明：执行本脚本会重建 library_db 库及全部表，并写入演示数据。
--        登录密码统一采用  MD5(明文 + 固定盐值 "lib@2024")  方式存储，
--        与后端 com.library.util.MD5Util 保持一致。
-- =============================================================

DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE library_db;

-- -------------------------------------------------------------
-- 1. 系统管理员表  sys_admin
--    负责：管理普通管理员账号、图书分类、查看全局数据
-- -------------------------------------------------------------
CREATE TABLE sys_admin (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(64)  NOT NULL COMMENT '登录密码(MD5+盐)',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员';

-- -------------------------------------------------------------
-- 2. 普通管理员表  manager
--    负责：图书入库/管理、读者登记/管理、借阅登记、归还登记、逾期提醒等日常业务
-- -------------------------------------------------------------
CREATE TABLE manager (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(64)  NOT NULL COMMENT '登录密码(MD5+盐)',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1启用 0禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_manager_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通管理员';

-- -------------------------------------------------------------
-- 3. 图书分类表  book_category
-- -------------------------------------------------------------
CREATE TABLE book_category (
    id   INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类';

-- -------------------------------------------------------------
-- 4. 图书表  book
--    total_count 馆藏总数, available_count 当前可借数量(借出/归还时同步增减)
-- -------------------------------------------------------------
CREATE TABLE book (
    id              INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    isbn            VARCHAR(20)   DEFAULT NULL COMMENT 'ISBN',
    title           VARCHAR(200)  NOT NULL COMMENT '书名',
    author          VARCHAR(100)  DEFAULT NULL COMMENT '作者',
    publisher       VARCHAR(100)  DEFAULT NULL COMMENT '出版社',
    category_id     INT           DEFAULT NULL COMMENT '分类id',
    location        VARCHAR(50)   DEFAULT NULL COMMENT '馆藏位置(书架)',
    total_count     INT           NOT NULL DEFAULT 0 COMMENT '馆藏总数',
    available_count INT           NOT NULL DEFAULT 0 COMMENT '当前可借数量',
    price           DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:1正常 0下架',
    PRIMARY KEY (id),
    KEY idx_book_title (title),
    KEY idx_book_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书';

-- -------------------------------------------------------------
-- 5. 读者表  reader
--    card_no 借阅证号(唯一), max_borrow 最大可借数量
-- -------------------------------------------------------------
CREATE TABLE reader (
    id            INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    card_no       VARCHAR(30)  NOT NULL COMMENT '借阅证号',
    name          VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender        VARCHAR(10)  DEFAULT '男' COMMENT '性别',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    reader_type   VARCHAR(20)  DEFAULT '学生' COMMENT '读者类型:学生/教师/职工',
    max_borrow    INT          NOT NULL DEFAULT 5 COMMENT '最大可借数量',
    register_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1正常 0注销',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reader_card (card_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者';

-- -------------------------------------------------------------
-- 6. 借阅记录表  borrow_record
--    status: 1=借出中  2=已归还
--    逾期 = (status=1 且 due_time < 当前时间)，运行时动态判断，便于"逾期提醒"
--    记录长期保存，做到账目清晰、有据可查
-- -------------------------------------------------------------
CREATE TABLE borrow_record (
    id          INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    book_id     INT      NOT NULL COMMENT '图书id',
    reader_id   INT      NOT NULL COMMENT '读者id',
    borrow_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
    due_time    DATETIME NOT NULL COMMENT '应还时间',
    return_time DATETIME DEFAULT NULL COMMENT '实际归还时间',
    status      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态:1借出中 2已归还',
    operator_id INT      DEFAULT NULL COMMENT '经办管理员id',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_borrow_book (book_id),
    KEY idx_borrow_reader (reader_id),
    KEY idx_borrow_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录';

-- =============================================================
--  演示数据
-- =============================================================
-- 盐值与后端 MD5Util.SALT 保持一致
SET @salt = 'lib@2024';

-- 系统管理员：admin / admin123
INSERT INTO sys_admin (username, password, real_name) VALUES
    ('admin', MD5(CONCAT('admin123', @salt)), '系统管理员');

-- 普通管理员：librarian / 123456 ，zhangsan / 123456
INSERT INTO manager (username, password, real_name, phone, status) VALUES
    ('librarian', MD5(CONCAT('123456', @salt)), '图书管理员', '13800000001', 1),
    ('zhangsan',  MD5(CONCAT('123456', @salt)), '张三',       '13800000002', 1);

-- 图书分类
INSERT INTO book_category (name) VALUES
    ('计算机'), ('文学'), ('历史'), ('科学'), ('艺术'), ('管理');

-- 图书（含可借数量，部分书已有借出）
INSERT INTO book (isbn, title, author, publisher, category_id, location, total_count, available_count, price) VALUES
    ('9787111213826', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', 1, 'A-01-01', 5, 4, 139.00),
    ('9787121362217', 'Java核心技术 卷I',  'Cay S. Horstmann', '电子工业出版社', 1, 'A-01-02', 4, 3, 119.00),
    ('9787115428028', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', 1, 'A-01-03', 6, 6, 89.00),
    ('9787111407010', '算法导论',          'Thomas H. Cormen', '机械工业出版社', 1, 'A-01-04', 3, 2, 128.00),
    ('9787020024759', '红楼梦',            '曹雪芹',           '人民文学出版社', 2, 'B-02-01', 5, 5, 59.70),
    ('9787020008735', '三国演义',          '罗贯中',           '人民文学出版社', 2, 'B-02-02', 5, 4, 49.50),
    ('9787532736973', '百年孤独',          '加西亚·马尔克斯', '上海译文出版社', 2, 'B-02-03', 4, 4, 55.00),
    ('9787100158602', '人类简史',          '尤瓦尔·赫拉利',   '中信出版社',     3, 'C-03-01', 4, 3, 68.00),
    ('9787508647357', '万历十五年',        '黄仁宇',           '中华书局',       3, 'C-03-02', 3, 3, 28.00),
    ('9787544253994', '时间简史',          '史蒂芬·霍金',     '湖南科学技术出版社', 4, 'D-04-01', 4, 4, 45.00),
    ('9787115173690', '世界艺术史',        '房龙',             '北京出版社',     5, 'E-05-01', 2, 2, 88.00),
    ('9787508660752', '高效能人士的七个习惯', '史蒂芬·柯维',   '中国青年出版社', 6, 'F-06-01', 3, 2, 59.00);

-- 读者
INSERT INTO reader (card_no, name, gender, phone, reader_type, max_borrow) VALUES
    ('R20240001', '李雷', '男', '13900000001', '学生', 5),
    ('R20240002', '韩梅梅', '女', '13900000002', '学生', 5),
    ('R20240003', '王老师', '男', '13900000003', '教师', 10),
    ('R20240004', '赵敏', '女', '13900000004', '职工', 8);

-- 借阅记录：
--   1) 李雷 借《深入理解计算机系统》借出中(未到期)
--   2) 韩梅梅 借《Java核心技术》已逾期(应还日期在过去)
--   3) 王老师 借《三国演义》已归还
--   4) 赵敏 借《高效能人士的七个习惯》借出中(未到期)
INSERT INTO borrow_record (book_id, reader_id, borrow_time, due_time, return_time, status, operator_id, remark) VALUES
    (1, 1, NOW() - INTERVAL 5 DAY,  NOW() + INTERVAL 25 DAY, NULL,                    1, 1, NULL),
    (2, 2, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 10 DAY, NULL,                    1, 1, '已逾期，需提醒'),
    (6, 3, NOW() - INTERVAL 20 DAY, NOW() + INTERVAL 10 DAY, NOW() - INTERVAL 2 DAY,  2, 1, '正常归还'),
    (12, 4, NOW() - INTERVAL 3 DAY, NOW() + INTERVAL 27 DAY, NULL,                    1, 2, NULL);
