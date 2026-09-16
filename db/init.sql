-- 创建数据库
CREATE DATABASE IF NOT EXISTS tutor_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE tutor_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt 加密）',
    email       VARCHAR(100) NOT NULL UNIQUE COMMENT '注册邮箱',
    phone       VARCHAR(20)  NOT NULL COMMENT '联系电话',
    role        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0家长 1教员 2管理员',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户基础信息表';

-- 家教需求表
CREATE TABLE IF NOT EXISTS tutor_demand
(
    demand_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '需求主键',
    publisher_id BIGINT        NOT NULL COMMENT '发布者ID（外键→user.user_id）',
    subject      VARCHAR(20)   NOT NULL COMMENT '科目（如数学、英语）',
    grade        VARCHAR(20)   NOT NULL COMMENT '年级（如小学五年级、高三）',
    description  TEXT          NOT NULL COMMENT '需求详细描述',
    salary       DECIMAL(10,2) NOT NULL COMMENT '期望薪资（元/小时）',
    contact      VARCHAR(100)  NOT NULL COMMENT '家长联系方式（审核后对教员可见）',
    publish_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    status       TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1已发布 2已关闭',
    CONSTRAINT fk_demand_user FOREIGN KEY (publisher_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='家长发布的家教需求表';

-- 家教简历表
CREATE TABLE IF NOT EXISTS tutor_resume
(
    resume_id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '简历主键',
    publisher_id    BIGINT        NOT NULL COMMENT '发布者ID（外键→user.user_id）',
    subjects        VARCHAR(100)  NOT NULL COMMENT '可教科目（多选用逗号分隔）',
    experience      TEXT COMMENT '教学经验描述',
    available_time  VARCHAR(200) COMMENT '可授课时间段（如周末上午）',
    expected_salary DECIMAL(10,2) COMMENT '期望薪资（元/小时）',
    introduction    TEXT COMMENT '个人简介',
    certificates    VARCHAR(200) COMMENT '资格证书文件路径（JPG/PNG/PDF）',
    publish_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1已发布 2已关闭',
    CONSTRAINT fk_resume_user FOREIGN KEY (publisher_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='教员发布的家教简历表';

-- 插入管理员用户（密码：admin123）
INSERT INTO `user` (username, password, email, phone, role, status, create_time)
VALUES ('admin', '$2a$10$DtsHUppHl567bkHh7fPtveL9kAJzYbNAJL/PRuen3K05/vCq9REde', 'admin@example.com', '13800138000', 2, 1, NOW());

-- 插入测试家长用户（密码：parent123）
INSERT INTO `user` (username, password, email, phone, role, status, create_time)
VALUES ('parent1', '$2a$10$3JpQSEP3S6XaNBy0kPpyY.Dbc92eG1jgKmen9m0zotSe8R/OaypcW', 'parent1@example.com', '13800138001', 0, 1, NOW());

-- 插入测试教员用户（密码：tutor123）
INSERT INTO `user` (username, password, email, phone, role, status, create_time)
VALUES ('tutor1', '$2a$10$U.B3Fm7wd08Bvu9PRcbl8OZqpeC0lYpiqQZeF.8beoUztr9o6Ur7e', 'tutor1@example.com', '13800138002', 1, 1, NOW());

-- 插入测试家教需求
INSERT INTO tutor_demand (publisher_id, subject, grade, description, salary, contact, status)
VALUES
(2, '数学', '初中二年级', '初二学生，数学基础一般，需要辅导函数和几何知识', 80.00, '13800138001', 1),
(2, '英语', '小学五年级', '五年级学生，英语口语和听力需要提高', 60.00, '13800138001', 1);

-- 插入测试家教简历
INSERT INTO tutor_resume (publisher_id, subjects, experience, available_time, expected_salary, introduction, status)
VALUES
(3, '数学,物理', '本科毕业，有3年家教经验，擅长初中数学和物理教学', '周末全天，工作日晚上', 100.00, '认真负责，有耐心', 1),
(3, '英语', '英语专业八级，有5年英语教学经验', '周末上午，工作日晚上', 120.00, '专业英语教师', 1);
