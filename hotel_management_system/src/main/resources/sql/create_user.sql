-- =====================================================================
-- 一次性脚本：创建普通用户 HOTEL，并授予运行业务所需的权限
-- 执行方式：以 sys as sysdba 登录后整段执行
-- 之后请用 HOTEL 用户登录，再去执行 schema.sql 建表
-- =====================================================================

-- 如果已存在则先清掉（首次执行会 ORA-01918，可忽略）
BEGIN
    EXECUTE IMMEDIATE 'DROP USER HOTEL CASCADE';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 创建用户
CREATE USER HOTEL IDENTIFIED BY hotel123;

-- 基础权限：登录 + 建表/序列/视图等 + 不限表空间
GRANT CONNECT, RESOURCE TO HOTEL;
GRANT UNLIMITED TABLESPACE TO HOTEL;

-- 显式权限（CONNECT/RESOURCE 默认已包含，但显式列出便于排查）
GRANT CREATE SESSION   TO HOTEL;
GRANT CREATE TABLE     TO HOTEL;
GRANT CREATE SEQUENCE  TO HOTEL;
GRANT CREATE VIEW      TO HOTEL;
GRANT CREATE PROCEDURE TO HOTEL;   -- F_NOW() 函数需要
GRANT CREATE TRIGGER   TO HOTEL;   -- 留作扩展用，本项目暂不创建触发器

-- 验证
SELECT username, account_status FROM dba_users WHERE username = 'HOTEL';
