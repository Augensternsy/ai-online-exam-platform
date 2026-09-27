/*
Navicat MySQL Data Transfer

Source Server         : wsy
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : xzs

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2023-06-13 15:50:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_exam_violation_log`
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_violation_log`;
CREATE TABLE `t_exam_violation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `exam_paper_id` int(11) DEFAULT NULL,
  `exam_paper_name` varchar(255) DEFAULT NULL,
  `violation_type` varchar(100) DEFAULT NULL,
  `violation_detail` varchar(500) DEFAULT NULL,
  `violation_count` int(11) DEFAULT NULL,
  `violation_time` datetime DEFAULT NULL,
  `is_handled` bit(1) DEFAULT 0,
  `handle_result` varchar(500) DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_exam_paper_id` (`exam_paper_id`),
  KEY `idx_violation_time` (`violation_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Table structure for `t_exam_paper`
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_paper`;
CREATE TABLE `t_exam_paper` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `subject_id` int(11) DEFAULT NULL,
  `paper_type` int(11) DEFAULT NULL,
  `grade_level` int(11) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `question_count` int(11) DEFAULT NULL,
  `suggest_time` int(11) DEFAULT NULL,
  `limit_start_time` datetime DEFAULT NULL,
  `limit_end_time` datetime DEFAULT NULL,
  `frame_text_content_id` int(11) DEFAULT NULL,
  `create_user` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `task_exam_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_exam_paper
-- ----------------------------
INSERT INTO t_exam_paper VALUES ('1', '2023数据结构考试', '1', '6', '4', '150', '3', '10', null, null, '4', '4', '2023-05-20 19:56:56', '', '1');
INSERT INTO t_exam_paper VALUES ('2', '第一期', '4', '6', '1', '200', '3', '5', null, null, '17', '4', '2023-05-21 18:30:26', '', '2');
INSERT INTO t_exam_paper VALUES ('3', '高等数学考试', '2', '1', '4', '180', '3', '10', null, null, '18', '4', '2023-05-21 19:23:51', '', null);
INSERT INTO t_exam_paper VALUES ('4', '密室大逃脱脑筋急转弯', '3', '4', '1', '270', '3', '20', '2023-05-21 00:00:00', '2023-06-20 00:00:00', '22', '4', '2023-05-21 19:29:08', '', null);
INSERT INTO t_exam_paper VALUES ('5', '高智商运动会​知识竞赛', '5', '1', '1', '100', '2', '10', null, null, '25', '4', '2023-05-21 19:40:11', '', null);
INSERT INTO t_exam_paper VALUES ('6', '想一想', '3', '6', '1', '170', '2', '2', null, null, '35', '4', '2023-05-21 20:16:04', '', '2');

-- ----------------------------
-- Table structure for `t_exam_paper_answer`
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_paper_answer`;
CREATE TABLE `t_exam_paper_answer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_paper_id` int(11) DEFAULT NULL,
  `paper_name` varchar(255) DEFAULT NULL,
  `paper_type` int(11) DEFAULT NULL,
  `subject_id` int(11) DEFAULT NULL,
  `system_score` int(11) DEFAULT NULL,
  `user_score` int(11) DEFAULT NULL,
  `paper_score` int(11) DEFAULT NULL,
  `question_correct` int(11) DEFAULT NULL,
  `question_count` int(11) DEFAULT NULL,
  `do_time` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `create_user` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `task_exam_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_exam_paper_answer
-- ----------------------------
INSERT INTO t_exam_paper_answer VALUES ('56', '2', '第一期', '6', '4', '100', '180', '200', '2', '3', '37', '2', '19', '2023-05-25 02:25:17', null, '2');
INSERT INTO t_exam_paper_answer VALUES ('63', '5', '高智商运动会​知识竞赛', '1', '5', '0', '0', '100', '1', '2', '2', '2', '22', '2023-06-02 19:03:32', null, null);

-- ----------------------------
-- Table structure for `t_exam_paper_question_customer_answer`
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_paper_question_customer_answer`;
CREATE TABLE `t_exam_paper_question_customer_answer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `question_id` int(11) DEFAULT NULL,
  `exam_paper_id` int(11) DEFAULT NULL,
  `exam_paper_answer_id` int(11) DEFAULT NULL,
  `question_type` int(11) DEFAULT NULL,
  `subject_id` int(11) DEFAULT NULL,
  `customer_score` int(11) DEFAULT NULL,
  `question_score` int(11) DEFAULT NULL,
  `question_text_content_id` int(11) DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `text_content_id` int(11) DEFAULT NULL,
  `do_right` bit(1) DEFAULT NULL,
  `create_user` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `item_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=172 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_exam_paper_question_customer_answer
-- ----------------------------
INSERT INTO t_exam_paper_question_customer_answer VALUES ('153', '9', '2', '56', '2', '4', '0', '0', '16', 'A,B,C,D', null, '', '19', '2023-05-25 02:25:17', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('154', '7', '2', '56', '5', '4', '80', '100', '14', null, '48', '', '19', '2023-05-25 02:25:17', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('155', '8', '2', '56', '1', '4', '100', '100', '15', 'B', null, '', '19', '2023-05-25 02:25:17', '3');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('156', '14', '5', '57', '3', '5', '100', '100', '24', 'B', null, '', '19', '2023-05-25 02:25:31', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('157', '13', '5', '57', '1', '5', '0', '0', '23', 'B', null, '', '19', '2023-05-25 02:25:31', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('158', '14', '5', '58', '3', '5', '0', '100', '24', null, null, '', '19', '2023-05-25 03:01:56', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('159', '13', '5', '58', '1', '5', '0', '0', '23', null, null, '', '19', '2023-05-25 03:01:56', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('160', '14', '5', '58', '3', '5', '0', '100', '24', null, null, '', '19', '2023-05-28 15:55:19', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('161', '13', '5', '58', '1', '5', '0', '0', '23', null, null, '', '19', '2023-05-28 15:55:19', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('162', '14', '5', '59', '3', '5', '0', '100', '24', null, null, '', '19', '2023-05-28 15:55:48', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('163', '13', '5', '59', '1', '5', '0', '0', '23', null, null, '', '19', '2023-05-28 15:55:48', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('164', '14', '5', '60', '3', '5', '100', '100', '24', 'B', null, '', '19', '2023-06-02 18:40:17', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('165', '13', '5', '60', '1', '5', '0', '0', '23', 'A', null, '', '19', '2023-06-02 18:40:17', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('166', '14', '5', '61', '3', '5', '100', '100', '24', 'B', null, '', '21', '2023-06-02 18:47:39', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('167', '13', '5', '61', '1', '5', '0', '0', '23', 'B', null, '', '21', '2023-06-02 18:47:39', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('168', '14', '5', '62', '3', '5', '0', '100', '24', 'A', null, '', '22', '2023-06-02 18:56:22', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('169', '13', '5', '62', '1', '5', '0', '0', '23', 'B', null, '', '22', '2023-06-02 18:56:22', '2');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('170', '14', '5', '63', '3', '5', '0', '100', '24', 'A', null, '', '22', '2023-06-02 19:03:32', '1');
INSERT INTO t_exam_paper_question_customer_answer VALUES ('171', '13', '5', '63', '1', '5', '0', '0', '23', 'B', null, '', '22', '2023-06-02 19:03:32', '2');

-- ----------------------------
-- Table structure for `t_message`
-- ----------------------------
DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `send_user_id` int(11) DEFAULT NULL,
  `send_user_name` varchar(255) DEFAULT NULL,
  `send_real_name` varchar(255) DEFAULT NULL,
  `receive_user_count` int(11) DEFAULT NULL,
  `read_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_message
-- ----------------------------
INSERT INTO t_message VALUES ('1', '关于数据结构考试的通知', '请务必完成数据结构考试', '2023-05-21 18:36:41', '4', 'admin', '2', '1', '1');
INSERT INTO t_message VALUES ('2', '关于数据结构考试通知', '请务必完成数据结构考试', '2023-05-21 18:37:29', '4', 'admin', '2', '2', '0');
INSERT INTO t_message VALUES ('3', '关于名侦探学院考试通知', '关于名侦探学院考试通知', '2023-06-02 19:08:41', '4', 'admin', '管理', '2', '0');

-- ----------------------------
-- Table structure for `t_message_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_message_user`;
CREATE TABLE `t_message_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_id` int(11) DEFAULT NULL,
  `receive_user_id` int(11) DEFAULT NULL,
  `receive_user_name` varchar(255) DEFAULT NULL,
  `receive_real_name` varchar(255) DEFAULT NULL,
  `readed` bit(1) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `read_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_message_user
-- ----------------------------
INSERT INTO t_message_user VALUES ('1', '1', '17', 'Natasha', '王诗颖', '', '2023-05-21 18:36:41', '2023-05-21 20:32:42');
INSERT INTO t_message_user VALUES ('2', '2', '5', 'student', 'stu', '', '2023-05-21 18:37:29', null);
INSERT INTO t_message_user VALUES ('3', '2', '16', 'Stella', '陈若央', '', '2023-05-21 18:37:29', null);
INSERT INTO t_message_user VALUES ('4', '3', '12', 'ggg', '郭文韬', '', '2023-06-02 19:08:41', null);
INSERT INTO t_message_user VALUES ('5', '3', '13', 'ppp', '蒲熠星', '', '2023-06-02 19:08:41', null);

-- ----------------------------
-- Table structure for `t_question`
-- ----------------------------
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `question_type` int(11) DEFAULT NULL,
  `subject_id` int(11) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `grade_level` int(11) DEFAULT NULL,
  `difficult` int(11) DEFAULT NULL,
  `correct` text,
  `info_text_content_id` int(11) DEFAULT NULL,
  `create_user` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_question
-- ----------------------------
INSERT INTO t_question VALUES ('1', '2', '1', '50', '4', '3', 'D', '1', '4', '1', '2023-05-20 19:51:49', '');
INSERT INTO t_question VALUES ('2', '1', '1', '50', '4', '3', 'B', '2', '4', '1', '2023-05-20 19:52:57', '');
INSERT INTO t_question VALUES ('3', '2', '1', '50', '4', '3', 'B,C', '3', '4', '1', '2023-05-20 19:53:58', '');
INSERT INTO t_question VALUES ('4', '1', '2', '60', '4', '3', 'C', '11', '4', '1', '2023-05-21 17:57:48', '');
INSERT INTO t_question VALUES ('5', '1', '2', '60', '4', '4', 'B', '12', '4', '1', '2023-05-21 17:59:20', '');
INSERT INTO t_question VALUES ('6', '2', '2', '60', '4', '4', 'A,B,C', '13', '4', '1', '2023-05-21 18:19:37', '');
INSERT INTO t_question VALUES ('7', '5', '4', '100', '1', '3', '52', '14', '4', '1', '2023-05-21 18:25:55', '');
INSERT INTO t_question VALUES ('8', '1', '4', '100', '1', '2', 'B', '15', '4', '1', '2023-05-21 18:27:25', '');
INSERT INTO t_question VALUES ('9', '2', '4', '0', '1', '3', 'A,B,C,D', '16', '4', '1', '2023-05-21 18:28:50', '');
INSERT INTO t_question VALUES ('10', '5', '3', '100', '1', '4', '正在瞄准目标', '19', '4', '1', '2023-05-21 19:27:08', '');
INSERT INTO t_question VALUES ('11', '5', '3', '80', '1', '3', '蛋', '20', '4', '1', '2023-05-21 19:27:41', '');
INSERT INTO t_question VALUES ('12', '5', '3', '90', '1', '4', '报纸', '21', '4', '1', '2023-05-21 19:28:12', '');
INSERT INTO t_question VALUES ('13', '1', '5', '0', '1', '3', 'B', '23', '4', '1', '2023-05-21 19:36:43', '');
INSERT INTO t_question VALUES ('14', '3', '5', '100', '1', '5', 'B', '24', '4', '1', '2023-05-21 19:39:06', '');
INSERT INTO t_question VALUES ('15', '3', '2', '10', '4', '1', 'A', '50', '4', '1', '2023-06-02 18:59:11', '');

-- ----------------------------
-- Table structure for `t_subject`
-- ----------------------------
DROP TABLE IF EXISTS `t_subject`;
CREATE TABLE `t_subject` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `level_name` varchar(255) DEFAULT NULL,
  `item_order` int(11) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_subject
-- ----------------------------
INSERT INTO t_subject VALUES ('1', '数据结构', '4', '大四', null, '');
INSERT INTO t_subject VALUES ('2', '高等数学', '4', '大四', null, '');
INSERT INTO t_subject VALUES ('3', '密室大逃脱', '1', '大一', null, '');
INSERT INTO t_subject VALUES ('4', '名侦探学院', '1', '大一', null, '');
INSERT INTO t_subject VALUES ('5', '高智商运动会', '1', '大一', null, '');

-- ----------------------------
-- Table structure for `t_task_exam`
-- ----------------------------
DROP TABLE IF EXISTS `t_task_exam`;
CREATE TABLE `t_task_exam` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `grade_level` int(11) DEFAULT NULL,
  `frame_text_content_id` int(11) DEFAULT NULL,
  `create_user` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `create_user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_task_exam
-- ----------------------------
INSERT INTO t_task_exam VALUES ('1', '2023数据结构考试', '4', '5', '4', '2023-05-20 19:57:35', '', 'admin');
INSERT INTO t_task_exam VALUES ('2', '名侦探学院考试', '1', '26', '4', '2023-05-21 19:54:06', '', 'admin');

-- ----------------------------
-- Table structure for `t_task_exam_customer_answer`
-- ----------------------------
DROP TABLE IF EXISTS `t_task_exam_customer_answer`;
CREATE TABLE `t_task_exam_customer_answer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_exam_id` int(11) DEFAULT NULL,
  `create_user` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `text_content_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_task_exam_customer_answer
-- ----------------------------
INSERT INTO t_task_exam_customer_answer VALUES ('1', '1', '3', '2023-05-20 20:04:48', '6');
INSERT INTO t_task_exam_customer_answer VALUES ('2', '1', '5', '2023-05-20 21:48:54', '7');
INSERT INTO t_task_exam_customer_answer VALUES ('3', '1', '6', '2023-05-21 00:10:23', '8');
INSERT INTO t_task_exam_customer_answer VALUES ('4', '1', '7', '2023-05-21 00:20:15', '9');
INSERT INTO t_task_exam_customer_answer VALUES ('5', '1', '8', '2023-05-21 01:26:05', '10');
INSERT INTO t_task_exam_customer_answer VALUES ('6', '2', '14', '2023-05-21 19:55:25', '28');
INSERT INTO t_task_exam_customer_answer VALUES ('7', '1', '17', '2023-05-21 21:41:31', '36');
INSERT INTO t_task_exam_customer_answer VALUES ('8', '2', '11', '2023-05-25 01:37:56', '44');
INSERT INTO t_task_exam_customer_answer VALUES ('9', '2', '19', '2023-05-25 02:25:17', '49');

-- ----------------------------
-- Table structure for `t_text_content`
-- ----------------------------
DROP TABLE IF EXISTS `t_text_content`;
CREATE TABLE `t_text_content` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_text_content
-- ----------------------------
INSERT INTO t_text_content VALUES ('1', '{\"titleContent\":\"设计一个判别表达式中括号是否配对的算法采用()数据结构最佳\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"<p class=\\\"ueditor-p\\\">顺序表</p>\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"链表\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"队列\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"栈\",\"score\":null,\"itemUuid\":null}],\"correct\":\"\"}', '2023-05-20 19:51:49');
INSERT INTO t_text_content VALUES ('2', '{\"titleContent\":\"在一个无序数据序列上进行查找,分别采用以下算法,速度最快的是\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"折半查找\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"哈希表查找\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"二叉排序树查找\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"顺序查找\",\"score\":null,\"itemUuid\":null}],\"correct\":\"B\"}', '2023-05-20 19:52:57');
INSERT INTO t_text_content VALUES ('3', '{\"titleContent\":\"线性结构有什么\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"链表\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"队列\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"字符串\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"顺序表\",\"score\":null,\"itemUuid\":null}],\"correct\":\"\"}', '2023-05-20 19:53:58');
INSERT INTO t_text_content VALUES ('4', '[{\"name\":\"选择题\",\"questionItems\":[{\"id\":2,\"itemOrder\":1},{\"id\":3,\"itemOrder\":2},{\"id\":1,\"itemOrder\":3}]}]', '2023-05-20 19:56:56');
INSERT INTO t_text_content VALUES ('5', '[{\"examPaperId\":1,\"examPaperName\":\"2023数据结构考试\",\"itemOrder\":null}]', '2023-05-20 19:57:35');
INSERT INTO t_text_content VALUES ('6', '[{\"examPaperId\":1,\"examPaperAnswerId\":1,\"status\":2}]', '2023-05-20 20:04:48');
INSERT INTO t_text_content VALUES ('7', '[{\"examPaperId\":1,\"examPaperAnswerId\":2,\"status\":2}]', '2023-05-20 21:48:54');
INSERT INTO t_text_content VALUES ('8', '[{\"examPaperId\":1,\"examPaperAnswerId\":3,\"status\":2}]', '2023-05-21 00:10:23');
INSERT INTO t_text_content VALUES ('9', '[{\"examPaperId\":1,\"examPaperAnswerId\":4,\"status\":2}]', '2023-05-21 00:20:15');
INSERT INTO t_text_content VALUES ('10', '[{\"examPaperId\":1,\"examPaperAnswerId\":5,\"status\":2}]', '2023-05-21 01:26:05');
INSERT INTO t_text_content VALUES ('11', '{\"titleContent\":\"<p class=\\\"ueditor-p\\\">lim(x-&gt;0)&nbsp; (sinx-x)/x^3</p>\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"0\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"1/3\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"-1/6\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"1/6\",\"score\":null,\"itemUuid\":null}],\"correct\":\"C\"}', '2023-05-21 17:57:48');
INSERT INTO t_text_content VALUES ('12', '{\"titleContent\":\"曲线y=x^2与曲线y=alnx(a不等于0)相切,则a=\",\"analyze\":\"<p>无</p>\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"e\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"2e\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"3e\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"4e\",\"score\":null,\"itemUuid\":null}],\"correct\":\"B\"}', '2023-05-21 17:59:20');
INSERT INTO t_text_content VALUES ('13', '{\"titleContent\":\"<p class=\\\"ueditor-p\\\">下列在x-&gt;0情况下为x的高阶无穷小的有</p>\",\"analyze\":\"<p>无</p>\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"cosx-1\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"sinxtanx\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"tan-x\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"sinxcosx\",\"score\":null,\"itemUuid\":null}],\"correct\":\"\"}', '2023-05-21 18:19:37');
INSERT INTO t_text_content VALUES ('14', '{\"titleContent\":\"<span style=\\\"font-weight: 600; color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">普通钢琴有多少白键</span>\",\"analyze\":\"无\",\"questionItemObjects\":[],\"correct\":\"52\"}', '2023-05-21 18:25:55');
INSERT INTO t_text_content VALUES ('15', '{\"titleContent\":\"以下三个选项中，哪一个职称级别最高？\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"<span style=\\\"font-weight: 600; color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">大副</span>\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"<span style=\\\"font-weight: 600; color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">船长&nbsp;</span>\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"<span style=\\\"font-weight: 600; color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">水手</span>\",\"score\":null,\"itemUuid\":null}],\"correct\":\"B\"}', '2023-05-21 18:27:25');
INSERT INTO t_text_content VALUES ('16', '{\"titleContent\":\"<strong><span style=\\\"background-color: #FFFFFF;\\\">以下属于</span></strong><span style=\\\"font-weight: 600; color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">四大洋的是？</span>\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"<span style=\\\"color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">大西洋</span>\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"<span style=\\\"color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">北冰洋</span>\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"C\",\"content\":\"<span style=\\\"color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">太平洋</span>\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"D\",\"content\":\"<span style=\\\"color: #121212; font-family: -apple-system, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, &quot;Source Han Sans SC&quot;, &quot;Noto Sans CJK SC&quot;, &quot;WenQuanYi Micro Hei&quot;, sans-serif; font-size: medium; background-color: #FFFFFF;\\\">印度洋</span>\",\"score\":null,\"itemUuid\":null}],\"correct\":\"\"}', '2023-05-21 18:28:50');
INSERT INTO t_text_content VALUES ('17', '[{\"name\":\"快问快答\",\"questionItems\":[{\"id\":9,\"itemOrder\":1},{\"id\":7,\"itemOrder\":2},{\"id\":8,\"itemOrder\":3}]}]', '2023-05-21 18:30:26');
INSERT INTO t_text_content VALUES ('18', '[{\"name\":\"选择题\",\"questionItems\":[{\"id\":5,\"itemOrder\":1},{\"id\":6,\"itemOrder\":2},{\"id\":4,\"itemOrder\":3}]}]', '2023-05-21 19:23:51');
INSERT INTO t_text_content VALUES ('19', '{\"titleContent\":\"<p class=\\\"ueditor-p\\\">小王在哨所站岗时,明明看到有敌人悄悄向他摸过来,为什么他却睁一只眼闲一只眼</p>\",\"analyze\":\"无\",\"questionItemObjects\":[],\"correct\":\"正在瞄准目标\"}', '2023-05-21 19:27:08');
INSERT INTO t_text_content VALUES ('20', '{\"titleContent\":\"世界上每一件东西加热都会熔化,唯独一样东西一加热便凝固请问是什么东西\",\"analyze\":\"无\",\"questionItemObjects\":[],\"correct\":\"蛋\"}', '2023-05-21 19:27:41');
INSERT INTO t_text_content VALUES ('21', '{\"titleContent\":\"制造日期与有效日期是同一天的产品是什么?\",\"analyze\":\"无\",\"questionItemObjects\":[],\"correct\":\"报纸\"}', '2023-05-21 19:28:12');
INSERT INTO t_text_content VALUES ('22', '[{\"name\":\"简答题\",\"questionItems\":[{\"id\":11,\"itemOrder\":1},{\"id\":10,\"itemOrder\":2},{\"id\":12,\"itemOrder\":3}]}]', '2023-05-21 19:29:08');
INSERT INTO t_text_content VALUES ('23', '{\"titleContent\":\"现代第一届夏季奥运会在哪举行?&nbsp;\",\"analyze\":\"无\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"罗马\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"雅典\",\"score\":null,\"itemUuid\":null}],\"correct\":\"B\"}', '2023-05-21 19:36:43');
INSERT INTO t_text_content VALUES ('24', '{\"titleContent\":\"国际奥委会的第一位中国委员是于再清\",\"analyze\":\"国际奥委会的第一位中国委员是王正廷\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"是\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"否\",\"score\":null,\"itemUuid\":null}],\"correct\":\"B\"}', '2023-05-21 19:39:06');
INSERT INTO t_text_content VALUES ('25', '[{\"name\":\"请答题\",\"questionItems\":[{\"id\":14,\"itemOrder\":1},{\"id\":13,\"itemOrder\":2}]}]', '2023-05-21 19:40:11');
INSERT INTO t_text_content VALUES ('26', '[{\"examPaperId\":2,\"examPaperName\":\"第一期\",\"itemOrder\":null},{\"examPaperId\":6,\"examPaperName\":\"想一想\",\"itemOrder\":null}]', '2023-05-21 19:54:06');
INSERT INTO t_text_content VALUES ('27', '58', '2023-05-21 19:55:25');
INSERT INTO t_text_content VALUES ('28', '[{\"examPaperId\":2,\"examPaperAnswerId\":6,\"status\":2}]', '2023-05-21 19:55:25');
INSERT INTO t_text_content VALUES ('29', '蛋', '2023-05-21 19:56:56');
INSERT INTO t_text_content VALUES ('30', null, '2023-05-21 19:56:56');
INSERT INTO t_text_content VALUES ('31', null, '2023-05-21 19:56:56');
INSERT INTO t_text_content VALUES ('32', '蛋', '2023-05-21 19:57:55');
INSERT INTO t_text_content VALUES ('33', null, '2023-05-21 19:57:55');
INSERT INTO t_text_content VALUES ('34', '报纸', '2023-05-21 19:57:55');
INSERT INTO t_text_content VALUES ('35', '[{\"name\":\"简答题\",\"questionItems\":[{\"id\":11,\"itemOrder\":1},{\"id\":12,\"itemOrder\":2}]}]', '2023-05-21 20:16:04');
INSERT INTO t_text_content VALUES ('36', '[{\"examPaperId\":1,\"examPaperAnswerId\":11,\"status\":2}]', '2023-05-21 21:41:31');
INSERT INTO t_text_content VALUES ('37', '111', '2023-05-22 04:46:44');
INSERT INTO t_text_content VALUES ('38', null, '2023-05-22 04:46:44');
INSERT INTO t_text_content VALUES ('39', null, '2023-05-22 04:46:44');
INSERT INTO t_text_content VALUES ('40', '34343', '2023-05-22 04:50:04');
INSERT INTO t_text_content VALUES ('41', null, '2023-05-22 04:50:04');
INSERT INTO t_text_content VALUES ('42', null, '2023-05-22 04:50:04');
INSERT INTO t_text_content VALUES ('43', '54', '2023-05-25 01:37:56');
INSERT INTO t_text_content VALUES ('44', '[{\"examPaperId\":2,\"examPaperAnswerId\":53,\"status\":2}]', '2023-05-25 01:37:56');
INSERT INTO t_text_content VALUES ('45', '蛋', '2023-05-25 01:38:30');
INSERT INTO t_text_content VALUES ('46', '看', '2023-05-25 01:38:30');
INSERT INTO t_text_content VALUES ('47', '报纸', '2023-05-25 01:38:30');
INSERT INTO t_text_content VALUES ('48', '54', '2023-05-25 02:25:17');
INSERT INTO t_text_content VALUES ('49', '[{\"examPaperId\":2,\"examPaperAnswerId\":56,\"status\":2}]', '2023-05-25 02:25:17');
INSERT INTO t_text_content VALUES ('50', '{\"titleContent\":\"1+1=2\",\"analyze\":\"计算题\",\"questionItemObjects\":[{\"prefix\":\"A\",\"content\":\"是\",\"score\":null,\"itemUuid\":null},{\"prefix\":\"B\",\"content\":\"否\",\"score\":null,\"itemUuid\":null}],\"correct\":\"A\"}', '2023-06-02 18:59:11');

-- ----------------------------
-- Table structure for `t_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_uuid` varchar(36) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `birth_day` datetime DEFAULT NULL,
  `user_level` int(11) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `modify_time` datetime DEFAULT NULL,
  `last_active_time` datetime DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `wx_open_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO t_user VALUES ('1', 'd2d29da2-dcb3-4013-b874-727626236f47', 'student', 'D1AGFL+Gx37t0NPG4d6biYP5Z31cNbwhK5w1lUeiHB2zagqbk8efYfSjYoh1Z/j1dkiRjHU+b0EpwzCh8IGsksJjzD65ci5LsnodQVf4Uj6D3pwoscXGqmkjjpzvSJbx42swwNTA+QoDU8YLo7JhtbUK2X0qCjFGpd+8eJ5BGvk=', '学生', '18', '1', '2019-09-01 16:00:00', '1', '19171171610', '1', '1', 'http://rufzdmzng.hn-bkt.clouddn.com/FqU6NQCfsulDw3wnZK9aDglYA6wf', '2019-09-07 18:55:02', '2020-02-04 08:26:54', null, '', null);
INSERT INTO t_user VALUES ('2', '52045f5f-a13f-4ccc-93dd-f7ee8270ad4c', 'admin', 'D1AGFL+Gx37t0NPG4d6biYP5Z31cNbwhK5w1lUeiHB2zagqbk8efYfSjYoh1Z/j1dkiRjHU+b0EpwzCh8IGsksJjzD65ci5LsnodQVf4Uj6D3pwoscXGqmkjjpzvSJbx42swwNTA+QoDU8YLo7JhtbUK2X0qCjFGpd+8eJ5BGvk=', '管理员', '30', '1', '2019-09-07 18:56:07', null, null, '3', '1', null, '2019-09-07 18:56:21', null, null, '', null);
INSERT INTO t_user VALUES ('3', '48f54001-3918-4413-96e3-914762423ee6', 'Natasha', 'SZKac7HEEFqeNlEBObq0xA9I/QHAZlHn/ov2sgTNvab0Ea36T1rH4oUrkmti/HIj8CWi8iGmtwoT8muNKjJTcbLxHtR3FfcFRw7NkXxTkOCCQbtPR92KRkaelHK03mrFqRyxxdelPZz/EtZo0lRi9pA9r5Bo1z8xi4G2mKf/UQY=', '王诗颖', null, '2', null, '4', null, '1', '1', 'http://rufzdmzng.hn-bkt.clouddn.com/FqU6NQCfsulDw3wnZK9aDglYA6wf', '2023-05-20 18:48:37', '2023-05-21 18:07:57', '2023-05-20 18:48:37', '', null);
INSERT INTO t_user VALUES ('4', 'd4edee1e-cab4-4809-a6ec-f763e09f6ec8', 'admin', 'hH0xfyGdCkvQfjsMWfR4xOGsX2ooz6GQoknZ7ErlQCjAcb/XeLSkM2D375pVkxu7U9T6Y9V7g6FDRW2ydPDnKjsEvG8+CkwAYa4aGuqHTbXrX/kXoHO0c/2Y5SEO8Yu5xbdUvY6HsTvgETWqfdER8+cD/5aW7cfGQUHpYq89dNY=', '管理', null, null, null, null, null, '3', '1', null, '2023-05-20 18:57:10', '2023-05-21 19:52:35', '2023-05-20 18:57:10', '', null);
INSERT INTO t_user VALUES ('5', 'effdd072-714b-4af4-8f65-1d94c2bed14d', 'student', 'l5jUxmXzbadqXVCi94ykspDf2cRC9lTt+O4apdcQwJDn9Ure+zaNpemOJ0gXvQrKS+VnFvDhPbWbLk2EK7aSpQELlJA++ceH/WQXYghT9EN2Hqo2K08zPCII+wlJLAqw2rh89JekMHYGvi43EuL21ggSwUIITWzDwdxNIzZcRdE=', 'stu', null, null, null, '4', null, '1', '2', null, '2023-05-20 21:37:14', '2023-05-21 20:03:53', '2023-05-20 21:37:14', '', null);
INSERT INTO t_user VALUES ('8', '48d56155-9627-47a5-8e0f-7b2f8f364db4', 'au', 'c+FqxBuMlq30kw0WOnJgsNUZEAxVpYnOP6CGElXkbAFDpxUXSLo3MYUuDewJWFLnDRZ0KXgpEuMW7idupdOzm94hbWSkUvI8ORK8KzLj2YxVuGqv2Z05iVEF9Y1DgYEcvOglrgcXYeN1mMu8tTvQQE1e0PkDszWURH2BKRhWMi8=', '111', null, null, null, '4', null, '1', '1', null, '2023-05-21 00:26:40', null, '2023-05-21 00:26:40', '', null);
INSERT INTO t_user VALUES ('9', '35d04265-ba04-4083-9bec-e08954c8f240', 'yyqx', 'gE8wCy9B9xfbdDbUqqkYpITpmazs4JXcc73Kd4+PXm+aRoonuTzio2UEY+R9xEJ8mr0wIvP6orxTM0xr2hJQ+PLgLhcZ0siz9kbdNOegVWjtODcpdbrmUEAaB907oFoB+nIF+sro2zUwkF/ZYT9TfC2ulYXl9Q/ybekPx6p7yyw=', '易烊千玺', null, '1', null, '2', null, '1', '1', 'http://rufzdmzng.hn-bkt.clouddn.com/FqAxPv5YV5sZ3ouU_F4V50nMjeFy', '2023-05-21 01:34:52', '2023-05-21 18:09:22', '2023-05-21 01:34:52', '', null);
INSERT INTO t_user VALUES ('10', 'b0247ee5-d544-4cd9-9c10-0266ea3efd5b', 'bjt', 'RcGsUgPBomqsMhBh7TBfakhWbNx/ep8o8bL1cp26+kNer8HKnDgglQ3oFlaQDTBoZ27BsROSMWhIljhvOiANWYzLPinl9ihg0GgjAunTsowDNQfWvbClzkTdKwbsgkktk804AsoGltbjZ2gt/o9omp0iQkFmRHaYHSOLzKBi6T4=', '白敬亭', null, '1', null, '3', null, '1', '1', null, '2023-05-21 18:08:40', '2023-05-21 18:09:15', '2023-05-21 18:08:40', '', null);
INSERT INTO t_user VALUES ('11', '7e1edde3-f291-41db-aa72-0f0c1073eb1d', 'zjw', 'Ku/DJjbbRhbzHl3FagIs9SR43sIte3y9l/J+g5HBCduNJI+0MNoERALiPGmiS2fsNssotSyjr52tg8x9mCse9Rd0fzwFxcZo1hYWojvoWuTF1NlsZQRI+2iq5m6dQhc54PZKNC4jC2e5gCR6ePffraCYsYx9ZT8OCawLHnPGQjY=', '周峻纬', null, '1', null, '1', null, '1', '1', 'http://rufzdmzng.hn-bkt.clouddn.com/FrvcQH-HXwAYXC8StnoQZYY_1Vuq', '2023-05-21 18:10:47', null, '2023-05-21 18:10:47', '', null);
INSERT INTO t_user VALUES ('12', 'f8428367-f53d-43ac-aa0f-ff3bcc2af5b0', 'ggg', 'J8Nvzn+qjO1imGvG/RntjHNmlkv3l9lnc9Hvp3Q5Jk7ceXlhImHJpGTxxyvuYJazfF/Gy6sh1YaTaJDy6FEN5/SbX76ccVURds6iu18IqqPSRbZfHjseqFbDoGLXHjhrwonqSTarPSLD8TunSTJUs9jMcatd9nalOt+RI0GSXlM=', '郭文韬', null, '1', null, '1', null, '1', '1', null, '2023-05-21 18:11:18', '2023-05-21 18:11:55', '2023-05-21 18:11:18', '', null);
INSERT INTO t_user VALUES ('13', '5a8dcbc5-5cf6-456d-9955-52e82cb82edf', 'ppp', 'D0q6SjTGinSAWVdqoUoy4GIzNLU7fm9pX18bueTg5n+R/jZctR0FBYcoK9nlRwgj0ic5Jog1QN3aNcdKmmzeEMXUWCft+xNb99r8aO4uNLS5BGGAJzO9mDSArzU/xCpJ6Os71IJ1C30DgX1M1qjVBYvuWh5HXeb8ZkFDKr/M5Jw=', '蒲熠星', null, '1', null, '1', null, '1', '1', null, '2023-05-21 18:11:35', '2023-05-21 18:11:47', '2023-05-21 18:11:35', '', null);
INSERT INTO t_user VALUES ('14', '0b073774-2119-4df1-b817-b344e4497bd7', 'hz', 'ok+F+yFPNKNvjmedT2rmtvCdWij+zKZ0ysj2y9GujoPrSC5hesvu7lLW261UG0TIZFqBwbsuSFtCxtS5TpDsRUBdBbCgHkqHVC1e84QI0lX64iTS4oM6KJ9+u8vYfliI6ThzXGg8LzhkAw5+CDUqfW65UemaMD2PKp+hAu1eKJo=', '黄子弘凡', null, '1', null, '1', null, '1', '1', null, '2023-05-21 18:12:27', null, '2023-05-21 18:12:27', '', null);
INSERT INTO t_user VALUES ('15', '99d6495a-76bc-440a-b0e7-c50de47920d9', 'zhtsl', 'oV3bqKFRz3YceAu53zh+BJY6iheuzPy9t5UcOqWl+//Un96yYCCI/aq5BcfWHJ2y3AwHxmvGvip4XUh8oTjMlJoq7R5ycfrpA6Hz64MEPrWS3k18LAvZ6mgRnZkL1V3GfCDzLLxMO2QPSHV/xDErcUwczYcFvq9MPlvcvMNw4yA=', '子回头是浪', null, '2', null, '3', null, '1', '1', null, '2023-05-21 18:13:48', '2023-05-21 18:15:30', '2023-05-21 18:13:48', '', null);
INSERT INTO t_user VALUES ('16', '63a18ac1-b25c-4581-8aa7-ea036d3c5328', 'Stella', 'FtRoOf+s9gaPZtz6Od8LPUpK9h+2Df2ucpaY1RIzuNKWl+PbjqPVmAHrKDJeSnAfDGqNWs8KPkgdxJPo5lHpUCdKHTTk7SDe6c5kDf2I9iZUos8Wi7Jp6OdwWYNrpGEpRgI1V1Gzv3Qh3dTvXeXiTrodH4djtqtnnqtZDKYbZ2U=', '陈若央', null, '2', null, '4', null, '1', '1', null, '2023-05-21 18:15:12', '2023-05-21 18:15:18', '2023-05-21 18:15:12', '', null);
INSERT INTO t_user VALUES ('17', 'a49202b2-235c-4290-acb1-fff653f193a1', 'Natasha', 'kRbly+w9mYmxRHurRdgZJq6cS/AFldnXGUKJ/YaXwgtqjyeEf+suXkKediVzgkol24nX5C7oYxoPoxoHQESC6+FkvOfUywoss9rJh4ZusK7+4St6rKBqvl3ll4sblXEOVsFKPchXEf+f/kTju7ZNA5rf+baAtmOIGUn9sMg+Glw=', '王诗颖', null, '2', null, '4', null, '1', '1', null, '2023-05-21 18:16:42', null, '2023-05-21 18:16:42', '', null);
INSERT INTO t_user VALUES ('18', '77b50344-54d8-4daf-bf87-92eee034a4b3', 'teacher', 'niW7dZrlN17wt9ebE9eHLeeB7NnjCLml8+Zt9o8pzTI0EJhuF6e6jwSR+xXgsnK7lYR4SGAskkCHjaoXcLexk5ltvEknEUKCm+K9R+WUVOj5nhV6YtMqLK+c/o/UBRsev8F2FwF/r2V8hJrnv1otG7ZVi7rcCWGyJkD44/h/chU=', '王老师', null, '2', null, null, null, '3', '2', null, '2023-05-21 20:03:38', '2023-06-02 18:58:04', '2023-05-21 20:03:38', '', null);
INSERT INTO t_user VALUES ('19', '8159b0e0-a809-4b02-bc3a-529e4f8bbc1b', 'zjw', 'I0WCELW271YbEy6JqmaxKlykXW4kXKDZ5mkL3oJ5SjzvbuPkoNN3KHdrF+GWtbmpCP75m1KJclIE7/v1wSREfFfv/mvS+Jnb2v9BweQVja9gsqsXIrN9r06XspYocck31NOggmLEJVhjqpNFMX6bnK4eHw0JrZoIj1DTh9DzLV8=', '周峻纬', null, null, null, '1', null, '1', '1', 'http://rufzdmzng.hn-bkt.clouddn.com/FrvcQH-HXwAYXC8StnoQZYY_1Vuq', '2023-05-25 02:24:13', null, '2023-05-25 02:24:13', '', null);
INSERT INTO t_user VALUES ('22', 'c18cd8bf-b5bc-4dd6-8362-2d9b67530c6c', 'augent', 'HFLtnCDiMFz02yC7Fkg8Gd6k37Mt+LNgRvwyM9lYcptCKjP6sD8EhiZxySr89XtyL9oNq1a1VaO+JvXSh/FZ+iTqpZ4d1HDo/EjP8TrmO8xhH1/rXay1yV+fajSW2enX3OU0A9YbbFFGki//2oTy1zx3NwhVSokLBnhgrRlIZOQ=', '郭文韬', null, null, null, '1', null, '1', '1', 'http://rufzdmzng.hn-bkt.clouddn.com/FloLlmVK-nwH7EWQXmb99vG7j91O', '2023-06-02 18:55:41', '2023-06-02 18:56:58', '2023-06-02 18:55:41', '', null);
INSERT INTO t_user VALUES ('23', '62b9dfbd-9907-48c9-bf39-c9140fd23845', 'aue', 'AeVx8C7MwUJuqv1HBKbIEMyviehyAl9cA9FFxjz6UNEKl4Myo6zafx++PtNa3DxYSQdfVODc7fKYs+mIeftAIvgr8/haqyIe8pk2QeAJKA4lSGWX1RZVmlKkWLZf84GeHkqPf9D05vNSMui1z4Ob5beLqT+1pz7boLlNlpJfXn4=', '王老北', null, null, null, '4', null, '1', '1', null, '2023-06-02 18:57:41', null, '2023-06-02 18:57:41', '', null);

-- ----------------------------
-- Table structure for `t_user_event_log`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_event_log`;
CREATE TABLE `t_user_event_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) DEFAULT NULL,
  `content` text,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=237 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_user_event_log
-- ----------------------------
INSERT INTO t_user_event_log VALUES ('193', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-25 02:17:44');
INSERT INTO t_user_event_log VALUES ('194', '11', 'zjw', '周峻纬', 'zjw 登录了智能在线考试系统', '2023-05-25 02:23:11');
INSERT INTO t_user_event_log VALUES ('195', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-25 02:23:48');
INSERT INTO t_user_event_log VALUES ('196', '19', 'zjw', '周峻纬', 'zjw 登录了智能在线考试系统', '2023-05-25 02:24:21');
INSERT INTO t_user_event_log VALUES ('197', '19', 'zjw', '周峻纬', 'zjw 提交试卷：第一期 得分：10 耗时：37 秒', '2023-05-25 02:25:17');
INSERT INTO t_user_event_log VALUES ('198', '19', 'zjw', '周峻纬', 'zjw 提交试卷：高智商运动会​知识竞赛 得分：10 耗时：4 秒', '2023-05-25 02:25:31');
INSERT INTO t_user_event_log VALUES ('201', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-25 03:22:53');
INSERT INTO t_user_event_log VALUES ('202', '19', 'zjw', '周峻纬', 'zjw 登录了智能在线考试系统', '2023-05-28 14:48:54');
INSERT INTO t_user_event_log VALUES ('203', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-28 14:49:07');
INSERT INTO t_user_event_log VALUES ('204', '19', 'zjw', '周峻纬', 'zjw 登录了智能在线考试系统', '2023-05-28 15:50:54');
INSERT INTO t_user_event_log VALUES ('205', '19', 'zjw', '周峻纬', 'zjw 提交试卷：高智商运动会​知识竞赛 得分：0 耗时：13 秒', '2023-05-28 15:55:19');
INSERT INTO t_user_event_log VALUES ('207', '19', 'zjw', '周峻纬', 'zjw 登出了智能在线考试系统', '2023-05-28 15:58:36');
INSERT INTO t_user_event_log VALUES ('208', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-28 16:13:12');
INSERT INTO t_user_event_log VALUES ('209', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-28 16:17:30');
INSERT INTO t_user_event_log VALUES ('210', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-05-28 16:18:41');
INSERT INTO t_user_event_log VALUES ('211', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-06-02 18:36:44');
INSERT INTO t_user_event_log VALUES ('212', '12', 'ggg', '郭文韬', 'ggg 登录了智能在线考试系统', '2023-06-02 18:37:27');
INSERT INTO t_user_event_log VALUES ('213', '12', 'ggg', '郭文韬', 'ggg 登出了智能在线考试系统', '2023-06-02 18:39:57');
INSERT INTO t_user_event_log VALUES ('214', '19', 'zjw', '周峻纬', 'zjw 登录了智能在线考试系统', '2023-06-02 18:40:00');
INSERT INTO t_user_event_log VALUES ('216', '19', 'zjw', '周峻纬', 'zjw 登出了智能在线考试系统', '2023-06-02 18:40:30');
INSERT INTO t_user_event_log VALUES ('224', '22', 'augent', null, '欢迎 augent 注册来到智能在线考试系统', '2023-06-02 18:55:41');
INSERT INTO t_user_event_log VALUES ('225', '22', 'augent', null, 'augent 登录了智能在线考试系统', '2023-06-02 18:55:46');
INSERT INTO t_user_event_log VALUES ('226', '22', 'augent', null, 'augent 提交试卷：高智商运动会​知识竞赛 得分：0 耗时：24 秒', '2023-06-02 18:56:22');
INSERT INTO t_user_event_log VALUES ('227', '22', 'augent', '郭文韬', 'augent 更新了个人资料', '2023-06-02 18:56:58');
INSERT INTO t_user_event_log VALUES ('228', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-06-02 18:57:08');
INSERT INTO t_user_event_log VALUES ('229', '22', 'augent', '郭文韬', 'augent 登录了智能在线考试系统', '2023-06-02 19:03:17');
INSERT INTO t_user_event_log VALUES ('230', '22', 'augent', '郭文韬', 'augent 提交试卷：高智商运动会​知识竞赛 得分：0 耗时：2 秒', '2023-06-02 19:03:32');
INSERT INTO t_user_event_log VALUES ('231', '22', 'augent', '郭文韬', 'augent 登出了智能在线考试系统', '2023-06-02 19:03:49');
INSERT INTO t_user_event_log VALUES ('232', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-06-02 19:04:08');
INSERT INTO t_user_event_log VALUES ('233', '4', 'admin', '管理', 'admin 登出了智能在线考试系统', '2023-06-02 19:13:20');
INSERT INTO t_user_event_log VALUES ('234', '22', 'augent', '郭文韬', 'augent 登录了智能在线考试系统', '2023-06-04 01:04:49');
INSERT INTO t_user_event_log VALUES ('235', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-06-04 01:22:33');
INSERT INTO t_user_event_log VALUES ('236', '4', 'admin', '管理', 'admin 登录了智能在线考试系统', '2023-06-04 01:58:27');

-- ----------------------------
-- Table structure for `t_user_token`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_token`;
CREATE TABLE `t_user_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `token` varchar(36) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `wx_open_id` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of t_user_token
-- ----------------------------

-- ============================================================
-- Demo 演示数据（面试展示用）
-- 学科：Java 开发技术；试卷：Java 基础能力测试（20 题 / 100 分）
-- ============================================================
INSERT INTO t_subject VALUES (6, 'Java 开发技术', 1, '大一', 6, b'0');
INSERT INTO t_text_content VALUES (100, '{"titleContent": "下列数据类型中，不属于 Java 基本数据类型的是", "analyze": "Java 基本数据类型包括 byte、short、int、long、float、double、char、boolean；String 是引用类型（类）。", "questionItemObjects": [{"prefix": "A", "content": "int", "score": null, "itemUuid": null}, {"prefix": "B", "content": "float", "score": null, "itemUuid": null}, {"prefix": "C", "content": "String", "score": null, "itemUuid": null}, {"prefix": "D", "content": "boolean", "score": null, "itemUuid": null}], "correct": "C"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (100, 1, 6, 5, 1, 1, 'C', 100, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (101, '{"titleContent": "关于 JDK、JRE、JVM 三者关系，下列说法正确的是", "analyze": "JDK（开发工具包）包含 JRE（运行环境），JRE 包含 JVM（虚拟机）和核心类库。", "questionItemObjects": [{"prefix": "A", "content": "JRE 包含 JDK", "score": null, "itemUuid": null}, {"prefix": "B", "content": "JDK 包含 JRE，JRE 包含 JVM", "score": null, "itemUuid": null}, {"prefix": "C", "content": "JVM 包含 JRE", "score": null, "itemUuid": null}, {"prefix": "D", "content": "三者完全相同", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (101, 1, 6, 5, 1, 1, 'B', 101, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (102, '{"titleContent": "Java 应用程序正确的入口方法声明是", "analyze": "入口方法必须被 public、static、void 修饰，且参数为 String 数组。", "questionItemObjects": [{"prefix": "A", "content": "public void main(String[] args)", "score": null, "itemUuid": null}, {"prefix": "B", "content": "public static int main(String[] args)", "score": null, "itemUuid": null}, {"prefix": "C", "content": "public static void main(String[] args)", "score": null, "itemUuid": null}, {"prefix": "D", "content": "static private void main()", "score": null, "itemUuid": null}], "correct": "C"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (102, 1, 6, 5, 1, 1, 'C', 102, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (103, '{"titleContent": "Java 中用于声明类继承关系的关键字是", "analyze": "类继承使用 extends；实现接口使用 implements。", "questionItemObjects": [{"prefix": "A", "content": "implements", "score": null, "itemUuid": null}, {"prefix": "B", "content": "extends", "score": null, "itemUuid": null}, {"prefix": "C", "content": "super", "score": null, "itemUuid": null}, {"prefix": "D", "content": "import", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (103, 1, 6, 5, 1, 1, 'B', 103, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (104, '{"titleContent": "执行 String s = \\"abc\\"; s.concat(\\"def\\"); 后，变量 s 的值为", "analyze": "String 是不可变类，concat 返回新字符串，原对象不变；接收返回值需重新赋值。", "questionItemObjects": [{"prefix": "A", "content": "abcdef", "score": null, "itemUuid": null}, {"prefix": "B", "content": "abc", "score": null, "itemUuid": null}, {"prefix": "C", "content": "def", "score": null, "itemUuid": null}, {"prefix": "D", "content": "运行时报错", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (104, 1, 6, 5, 1, 2, 'B', 104, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (105, '{"titleContent": "ArrayList 与 LinkedList 的主要区别是", "analyze": "ArrayList 基于动态数组，随机访问快；LinkedList 基于双向链表，增删元素快。", "questionItemObjects": [{"prefix": "A", "content": "ArrayList 底层是链表，LinkedList 底层是数组", "score": null, "itemUuid": null}, {"prefix": "B", "content": "ArrayList 底层是动态数组，LinkedList 底层是双向链表", "score": null, "itemUuid": null}, {"prefix": "C", "content": "两者底层结构完全相同", "score": null, "itemUuid": null}, {"prefix": "D", "content": "LinkedList 不允许存放重复元素", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (105, 1, 6, 5, 1, 2, 'B', 105, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (106, '{"titleContent": "下列属于 Java 面向对象三大特性的有", "analyze": "面向对象三大特性为封装、继承、多态；编译是程序构建过程，不属于特性。", "questionItemObjects": [{"prefix": "A", "content": "封装", "score": null, "itemUuid": null}, {"prefix": "B", "content": "继承", "score": null, "itemUuid": null}, {"prefix": "C", "content": "多态", "score": null, "itemUuid": null}, {"prefix": "D", "content": "编译", "score": null, "itemUuid": null}], "correct": "A,B,C"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (106, 2, 6, 5, 1, 1, 'A,B,C', 106, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (107, '{"titleContent": "Java 中 int 类型无论在 32 位还是 64 位平台上都占用 4 个字节。", "analyze": "int 的字节宽度由 Java 虚拟机规范统一规定为 4 字节，与操作系统位数无关。", "questionItemObjects": [{"prefix": "A", "content": "正确", "score": null, "itemUuid": null}, {"prefix": "B", "content": "错误", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (107, 3, 6, 5, 1, 1, 'A', 107, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (108, '{"titleContent": "Spring Boot 启动类上最核心的组合注解是", "analyze": "@SpringBootApplication 组合了 @SpringBootConfiguration、@EnableAutoConfiguration、@ComponentScan。", "questionItemObjects": [{"prefix": "A", "content": "@SpringBootApplication", "score": null, "itemUuid": null}, {"prefix": "B", "content": "@Controller", "score": null, "itemUuid": null}, {"prefix": "C", "content": "@Component", "score": null, "itemUuid": null}, {"prefix": "D", "content": "@Repository", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (108, 1, 6, 5, 1, 1, 'A', 108, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (109, '{"titleContent": "在未替换默认容器时，Spring Boot 内嵌的 Web 服务器是", "analyze": "spring-boot-starter-web 默认引入并启动 Tomcat；Jetty、Undertow 需要手动替换。", "questionItemObjects": [{"prefix": "A", "content": "Jetty", "score": null, "itemUuid": null}, {"prefix": "B", "content": "Tomcat", "score": null, "itemUuid": null}, {"prefix": "C", "content": "Undertow", "score": null, "itemUuid": null}, {"prefix": "D", "content": "Netty", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (109, 1, 6, 5, 1, 1, 'B', 109, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (110, '{"titleContent": "application.yml 中修改服务端口应配置的属性是", "analyze": "通过 server.port 指定内嵌容器监听端口，如 server.port: 8081。", "questionItemObjects": [{"prefix": "A", "content": "server.port", "score": null, "itemUuid": null}, {"prefix": "B", "content": "spring.port", "score": null, "itemUuid": null}, {"prefix": "C", "content": "service.port", "score": null, "itemUuid": null}, {"prefix": "D", "content": "web.port", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (110, 1, 6, 5, 1, 1, 'A', 110, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (111, '{"titleContent": "下列注解用于映射 HTTP GET 请求的是", "analyze": "@GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的简写。", "questionItemObjects": [{"prefix": "A", "content": "@PostMapping", "score": null, "itemUuid": null}, {"prefix": "B", "content": "@GetMapping", "score": null, "itemUuid": null}, {"prefix": "C", "content": "@PutMapping", "score": null, "itemUuid": null}, {"prefix": "D", "content": "@DeleteMapping", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (111, 1, 6, 5, 1, 1, 'B', 111, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (112, '{"titleContent": "Spring 中最常用于自动装配 Bean 的注解是", "analyze": "@Autowired 默认按类型注入，可配合 @Qualifier 指定具体 Bean 名称。", "questionItemObjects": [{"prefix": "A", "content": "@Autowired", "score": null, "itemUuid": null}, {"prefix": "B", "content": "@Override", "score": null, "itemUuid": null}, {"prefix": "C", "content": "@Deprecated", "score": null, "itemUuid": null}, {"prefix": "D", "content": "@SuppressWarnings", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (112, 1, 6, 5, 1, 2, 'A', 112, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (113, '{"titleContent": "下列属于 Spring Boot 优点的有", "analyze": "自动配置、内嵌容器、starter 起步依赖是 Spring Boot 的核心优势；业务代码仍需自行编写。", "questionItemObjects": [{"prefix": "A", "content": "自动配置，减少繁琐 XML", "score": null, "itemUuid": null}, {"prefix": "B", "content": "内嵌 Web 容器，开箱即用", "score": null, "itemUuid": null}, {"prefix": "C", "content": "提供 starter 依赖，简化构建配置", "score": null, "itemUuid": null}, {"prefix": "D", "content": "项目无需编写任何代码即可运行业务", "score": null, "itemUuid": null}], "correct": "A,B,C"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (113, 2, 6, 5, 1, 2, 'A,B,C', 113, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (114, '{"titleContent": "@RestController 的作用等价于 @Controller 与 @ResponseBody 组合使用。", "analyze": "@RestController 标注后，该控制器所有方法返回值默认直接写入响应体（JSON）。", "questionItemObjects": [{"prefix": "A", "content": "正确", "score": null, "itemUuid": null}, {"prefix": "B", "content": "错误", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (114, 3, 6, 5, 1, 1, 'A', 114, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (115, '{"titleContent": "下列 SQL 关键字用于查询数据的是", "analyze": "SELECT 用于查询；INSERT 新增、UPDATE 修改、DELETE 删除。", "questionItemObjects": [{"prefix": "A", "content": "INSERT", "score": null, "itemUuid": null}, {"prefix": "B", "content": "UPDATE", "score": null, "itemUuid": null}, {"prefix": "C", "content": "SELECT", "score": null, "itemUuid": null}, {"prefix": "D", "content": "DELETE", "score": null, "itemUuid": null}], "correct": "C"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (115, 1, 6, 5, 1, 1, 'C', 115, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (116, '{"titleContent": "MySQL 中适合存储可变长度字符串的类型是", "analyze": "VARCHAR 按实际长度存储，节省空间；CHAR 为定长类型。", "questionItemObjects": [{"prefix": "A", "content": "CHAR", "score": null, "itemUuid": null}, {"prefix": "B", "content": "VARCHAR", "score": null, "itemUuid": null}, {"prefix": "C", "content": "INT", "score": null, "itemUuid": null}, {"prefix": "D", "content": "DATE", "score": null, "itemUuid": null}], "correct": "B"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (116, 1, 6, 5, 1, 1, 'B', 116, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (117, '{"titleContent": "查询年龄大于 18 的学生，WHERE 子句书写正确的是", "analyze": "SQL 中大于比较运算符为 >，条件关键字为 WHERE。", "questionItemObjects": [{"prefix": "A", "content": "WHERE age > 18", "score": null, "itemUuid": null}, {"prefix": "B", "content": "WHERE age equals 18", "score": null, "itemUuid": null}, {"prefix": "C", "content": "WHERE age == 18", "score": null, "itemUuid": null}, {"prefix": "D", "content": "WHEN age > 18", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (117, 1, 6, 5, 1, 1, 'A', 117, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (118, '{"titleContent": "下列属于 SQL 聚合函数的有", "analyze": "常见聚合函数包括 COUNT、SUM、AVG、MAX、MIN。", "questionItemObjects": [{"prefix": "A", "content": "COUNT", "score": null, "itemUuid": null}, {"prefix": "B", "content": "SUM", "score": null, "itemUuid": null}, {"prefix": "C", "content": "AVG", "score": null, "itemUuid": null}, {"prefix": "D", "content": "MAX", "score": null, "itemUuid": null}], "correct": "A,B,C,D"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (118, 2, 6, 5, 1, 1, 'A,B,C,D', 118, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (119, '{"titleContent": "MySQL 的 InnoDB 存储引擎支持事务和外键约束。", "analyze": "InnoDB 支持 ACID 事务、行级锁和外键；MyISAM 不支持事务与外键。", "questionItemObjects": [{"prefix": "A", "content": "正确", "score": null, "itemUuid": null}, {"prefix": "B", "content": "错误", "score": null, "itemUuid": null}], "correct": "A"}', '2026-09-26 09:00:00');
INSERT INTO t_question VALUES (119, 3, 6, 5, 1, 1, 'A', 119, 2, 1, '2026-09-26 09:00:00', b'0');
INSERT INTO t_text_content VALUES (199, '[{"name": "一、Java 基础知识", "questionItems": [{"id": 100, "itemOrder": 1}, {"id": 101, "itemOrder": 2}, {"id": 102, "itemOrder": 3}, {"id": 103, "itemOrder": 4}, {"id": 104, "itemOrder": 5}, {"id": 105, "itemOrder": 6}, {"id": 106, "itemOrder": 7}, {"id": 107, "itemOrder": 8}]}, {"name": "二、Spring Boot 基础", "questionItems": [{"id": 108, "itemOrder": 1}, {"id": 109, "itemOrder": 2}, {"id": 110, "itemOrder": 3}, {"id": 111, "itemOrder": 4}, {"id": 112, "itemOrder": 5}, {"id": 113, "itemOrder": 6}, {"id": 114, "itemOrder": 7}]}, {"name": "三、MySQL 基础", "questionItems": [{"id": 115, "itemOrder": 1}, {"id": 116, "itemOrder": 2}, {"id": 117, "itemOrder": 3}, {"id": 118, "itemOrder": 4}, {"id": 119, "itemOrder": 5}]}]', '2026-09-26 09:00:00');
INSERT INTO t_exam_paper VALUES (100, 'Java 基础能力测试', 6, 1, 1, 100, 20, 30, NULL, NULL, 199, 2, '2026-09-26 09:00:00', b'0', NULL);
ALTER TABLE t_subject AUTO_INCREMENT = 1000;
ALTER TABLE t_text_content AUTO_INCREMENT = 1000;
ALTER TABLE t_question AUTO_INCREMENT = 1000;
ALTER TABLE t_exam_paper AUTO_INCREMENT = 1000;
