-- 学生课程小组作业系统 数据库初始化脚本

CREATE TABLE IF NOT EXISTS students (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    student_no  TEXT    NOT NULL UNIQUE,
    password    TEXT    NOT NULL,
    name        TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    description TEXT,
    teacher     TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS course_students (
    course_id   INTEGER NOT NULL,
    student_id  INTEGER NOT NULL,
    PRIMARY KEY (course_id, student_id),
    FOREIGN KEY (course_id)  REFERENCES courses(id),
    FOREIGN KEY (student_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS groups (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id   INTEGER NOT NULL,
    name        TEXT    NOT NULL,
    leader_id   INTEGER NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (leader_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id    INTEGER NOT NULL,
    student_id  INTEGER NOT NULL,
    role        TEXT    DEFAULT 'member',
    PRIMARY KEY (group_id, student_id),
    FOREIGN KEY (group_id)   REFERENCES groups(id),
    FOREIGN KEY (student_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS messages (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id    INTEGER NOT NULL,
    sender_id   INTEGER NOT NULL,
    content     TEXT    NOT NULL,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (group_id)  REFERENCES groups(id),
    FOREIGN KEY (sender_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id    INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    description TEXT,
    status      TEXT    NOT NULL DEFAULT '待开始',
    assignee_id INTEGER,
    FOREIGN KEY (group_id)    REFERENCES groups(id),
    FOREIGN KEY (assignee_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS group_files (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id    INTEGER NOT NULL,
    uploader_id INTEGER NOT NULL,
    file_name   TEXT    NOT NULL,
    file_path   TEXT    NOT NULL,
    uploaded_at TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (group_id)    REFERENCES groups(id),
    FOREIGN KEY (uploader_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS invitations (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id    INTEGER,
    course_id   INTEGER,
    inviter_id  INTEGER NOT NULL,
    invitee_id  INTEGER NOT NULL,
    type        TEXT    NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'pending',
    message     TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (group_id)   REFERENCES groups(id),
    FOREIGN KEY (course_id)  REFERENCES courses(id),
    FOREIGN KEY (inviter_id) REFERENCES students(id),
    FOREIGN KEY (invitee_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id  INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    content     TEXT    NOT NULL,
    is_read     INTEGER NOT NULL DEFAULT 0,
    related_id  INTEGER,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 初始学生数据（密码均为 123456）
INSERT OR IGNORE INTO students (id, student_no, password, name) VALUES
(1, '2021001', '123456', '陈同学'),
(2, '2021002', '123456', '李同学'),
(3, '2021003', '123456', '王同学'),
(4, '2021004', '123456', '赵同学'),
(5, '2021005', '123456', '刘同学');

-- 课程（老师预设）
INSERT OR IGNORE INTO courses (id, name, description, teacher) VALUES
(1, '管理信息系统', '管理信息系统课程小组作业', '张老师'),
(2, '数据库原理', '数据库课程设计与实现', '李老师');

-- 学生选课
INSERT OR IGNORE INTO course_students (course_id, student_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 1), (2, 5);

-- 已有小组
INSERT OR IGNORE INTO groups (id, course_id, name, leader_id) VALUES
(1, 1, '第1组', 2),
(2, 1, '第2组', 1);

INSERT OR IGNORE INTO group_members (group_id, student_id, role) VALUES
(1, 2, 'leader'), (1, 3, 'member'),
(2, 1, 'leader'), (2, 2, 'member'), (2, 3, 'member'), (2, 4, 'member');

-- 示例聊天消息
INSERT OR IGNORE INTO messages (group_id, sender_id, content) VALUES
(2, 2, '大家周三之前把数据库设计文档发一下'),
(2, 3, '我负责PPT和汇报'),
(2, 4, '我今晚把ER图上传'),
(2, 1, '好的，我负责前端界面部分'),
(2, 1, '收到，明天下午一起讨论');

-- 示例任务
INSERT OR IGNORE INTO tasks (group_id, title, description, status, assignee_id) VALUES
(2, '需求分析文档', '完成系统需求分析', '已完成', 2),
(2, '数据库设计', '完成ER图和表结构设计', '进行中', 4),
(2, '界面开发', '完成GUI界面开发', '进行中', 1),
(2, 'PPT制作', '制作课堂展示PPT', '待开始', 3);

-- 课程公告（存入 notifications 供展示）
INSERT OR IGNORE INTO notifications (student_id, title, content, is_read) VALUES
(1, '课程公告', '周五提交需求分析', 0),
(1, '课程公告', '下周课堂展示', 0),
(1, '课程公告', '注意命名规范', 0);
