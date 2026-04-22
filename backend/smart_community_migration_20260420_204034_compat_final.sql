-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: smart_community
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `smart_community`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `smart_community` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `smart_community`;

--
-- Table structure for table `access_token`
--

DROP TABLE IF EXISTS `access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(64) NOT NULL,
  `expire_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB AUTO_INCREMENT=12011 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access_token`
--

LOCK TABLES `access_token` WRITE;
/*!40000 ALTER TABLE `access_token` DISABLE KEYS */;
INSERT INTO `access_token` VALUES (12001,1,'ACC_0001','2026-04-18 12:28:52',1,'2026-04-18 12:03:52','2026-04-18 12:03:52',0),(12002,2,'ACC_0002','2026-04-18 11:58:52',3,'2026-04-18 11:28:52','2026-04-18 11:58:52',0),(12003,3,'64eabf849c93408ba3716a09613fb4d4','2026-04-18 13:48:37',1,'2026-04-18 13:48:07','2026-04-18 13:48:07',0),(12004,1,'e515e9b16a834bc8a57ef843d8280a54','2026-04-18 16:25:52',1,'2026-04-18 16:25:22','2026-04-18 16:25:22',0),(12005,1,'cd6bd873669c4108b3bac92c679bba87','2026-04-18 16:26:10',1,'2026-04-18 16:25:40','2026-04-18 16:25:40',0),(12006,1,'e4994550b70f4c0a99573fa764fd94de','2026-04-18 16:26:17',1,'2026-04-18 16:25:47','2026-04-18 16:25:47',0),(12007,1,'40cb46a1115f4f0daf65763b8128dbc6','2026-04-18 16:35:23',1,'2026-04-18 16:34:53','2026-04-18 16:34:53',0),(12008,1,'aea703d4216c48bf8762676d4ff199a7','2026-04-18 22:02:49',1,'2026-04-18 22:02:19','2026-04-18 22:02:19',0),(12009,1,'0a70e0be4ac4417382d1d5f13a6b2b22','2026-04-19 11:43:49',1,'2026-04-19 11:43:19','2026-04-19 11:43:19',0),(12010,2,'778f4215a7054eda8dd847e4471c92f0','2026-04-19 18:21:45',1,'2026-04-19 18:21:15','2026-04-19 18:21:15',0);
/*!40000 ALTER TABLE `access_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complaint`
--

DROP TABLE IF EXISTS `complaint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `images` varchar(2000) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `reply` text,
  `reply_time` datetime DEFAULT NULL,
  `satisfaction` tinyint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=14003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complaint`
--

LOCK TABLES `complaint` WRITE;
/*!40000 ALTER TABLE `complaint` DISABLE KEYS */;
INSERT INTO `complaint` VALUES (14001,1,1,'Night noise','Construction noise after 22:00','[]',3,'Construction adjusted to daytime.','2026-04-16 12:08:52',3,'2026-04-15 12:08:52','2026-04-16 12:08:52',0),(14002,2,2,'Add parcel locker','Suggest adding parcel locker near building 2.','[]',1,NULL,NULL,NULL,'2026-04-17 12:08:52','2026-04-17 12:08:52',0);
/*!40000 ALTER TABLE `complaint` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fee_bill`
--

DROP TABLE IF EXISTS `fee_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `property_id` bigint NOT NULL,
  `bill_period` varchar(10) NOT NULL,
  `area_snapshot` decimal(10,2) NOT NULL DEFAULT '0.00',
  `unit_price` decimal(10,2) NOT NULL DEFAULT '5.00',
  `amount` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `need_points` int NOT NULL DEFAULT '0',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` tinyint NOT NULL DEFAULT '0',
  `due_date` datetime NOT NULL,
  `payment_time` datetime DEFAULT NULL,
  `transaction_id` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_property_period` (`property_id`,`bill_period`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_property_id` (`property_id`),
  KEY `idx_status` (`status`),
  KEY `idx_fee_property_status_deleted` (`property_id`,`status`,`is_deleted`),
  KEY `idx_fee_due_deleted` (`due_date`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=3004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fee_bill`
--

LOCK TABLES `fee_bill` WRITE;
/*!40000 ALTER TABLE `fee_bill` DISABLE KEYS */;
INSERT INTO `fee_bill` VALUES (3001,1001,'2026-03',98.50,5.00,360.00,0.00,360,360.00,2,'2026-03-29 12:08:52','2026-03-31 12:08:52','TXN_FEE_3001','2026-03-19 12:08:52','2026-04-19 10:57:31',0),(3002,1002,'2026-04',88.20,5.00,320.00,0.00,320,100.00,1,'2026-04-26 12:08:52','2026-04-17 12:08:52','TXN_FEE_3002','2026-04-08 12:08:52','2026-04-19 10:57:31',0),(3003,1003,'2026-04',108.30,5.00,450.00,0.00,450,0.00,0,'2026-04-16 12:08:52',NULL,NULL,'2026-04-08 12:08:52','2026-04-19 10:57:31',0);
/*!40000 ALTER TABLE `fee_bill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_comment`
--

DROP TABLE IF EXISTS `forum_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `content` text NOT NULL,
  `like_cnt` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_comment`
--

LOCK TABLES `forum_comment` WRITE;
/*!40000 ALTER TABLE `forum_comment` DISABLE KEYS */;
INSERT INTO `forum_comment` VALUES (11001,10001,2,NULL,'We can join.',1,'2026-04-15 12:08:52','2026-04-15 12:08:52',0),(11002,10002,3,NULL,'I have one drill.',2,'2026-04-16 12:08:52','2026-04-16 12:08:52',0);
/*!40000 ALTER TABLE `forum_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post`
--

DROP TABLE IF EXISTS `forum_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `board` varchar(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `view_count` int NOT NULL DEFAULT '0',
  `like_cnt` int NOT NULL DEFAULT '0',
  `reply_cnt` int NOT NULL DEFAULT '0',
  `is_top` tinyint(1) NOT NULL DEFAULT '0',
  `is_essence` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_board` (`board`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post`
--

LOCK TABLES `forum_post` WRITE;
/*!40000 ALTER TABLE `forum_post` DISABLE KEYS */;
INSERT INTO `forum_post` VALUES (10001,1,'chat','Weekend activity','Anyone join kids activity this weekend?',0,5,1,0,0,'2026-04-15 12:08:52','2026-04-15 12:08:52',0),(10002,2,'help','Need a drill','Can someone lend me a drill for half a day?',0,8,1,0,1,'2026-04-16 12:08:52','2026-04-16 12:08:52',0);
/*!40000 ALTER TABLE `forum_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post_like`
--

DROP TABLE IF EXISTS `forum_post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post_like`
--

LOCK TABLES `forum_post_like` WRITE;
/*!40000 ALTER TABLE `forum_post_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `forum_post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lost_found`
--

DROP TABLE IF EXISTS `lost_found`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lost_found` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL,
  `title` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `location` varchar(100) DEFAULT NULL,
  `contact` varchar(50) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `images` varchar(2000) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lost_found`
--

LOCK TABLES `lost_found` WRITE;
/*!40000 ALTER TABLE `lost_found` DISABLE KEYS */;
INSERT INTO `lost_found` VALUES (9001,1,1,'Lost key card','Lost near building 1 gate',NULL,'13800000001',1,'[]','2026-04-16 12:08:52','2026-04-16 12:08:52',0),(9002,2,2,'Found keychain','Found in public garden',NULL,'13800000002',1,'[]','2026-04-17 12:08:52','2026-04-17 12:08:52',0);
/*!40000 ALTER TABLE `lost_found` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lost_found_claim`
--

DROP TABLE IF EXISTS `lost_found_claim`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lost_found_claim` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lost_found_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `proof` text,
  `status` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_claim_lost_found` (`lost_found_id`),
  KEY `idx_claim_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lost_found_claim`
--

LOCK TABLES `lost_found_claim` WRITE;
/*!40000 ALTER TABLE `lost_found_claim` DISABLE KEYS */;
/*!40000 ALTER TABLE `lost_found_claim` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` longtext NOT NULL,
  `publisher` varchar(50) NOT NULL,
  `top` tinyint(1) NOT NULL DEFAULT '0',
  `attachment_urls` varchar(2000) DEFAULT NULL,
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_top_publish` (`top`,`publish_time`),
  KEY `idx_notice_deleted_publish` (`is_deleted`,`top`,`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (7001,'Elevator Maintenance','Building 1 elevator maintenance this weekend.','Property Center',1,NULL,'2026-04-16 12:08:52','2026-04-16 12:08:52','2026-04-16 12:08:52',0),(7002,'Pest Control','Public area disinfection on Friday.','Property Center',0,NULL,'2026-04-17 12:08:52','2026-04-17 12:08:52','2026-04-17 12:08:52',0),(7003,'Parking Upgrade','Parking gate system will be upgraded tonight.','Property Center',0,NULL,'2026-04-18 12:08:52','2026-04-18 12:08:52','2026-04-18 12:08:52',0),(7004,'Visitor Invite Feature Enabled','The visitor invite feature has been enabled. Please use mini program -> Smart Access -> Visitor Invite to generate a temporary code.','property-service',1,NULL,'2026-04-19 13:25:08','2026-04-19 13:25:08','2026-04-19 13:25:18',0);
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `owner_parking_quota`
--

DROP TABLE IF EXISTS `owner_parking_quota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `owner_parking_quota` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `month_key` varchar(7) NOT NULL,
  `free_hours_total` int NOT NULL DEFAULT '10',
  `free_hours_used` int NOT NULL DEFAULT '0',
  `owner_extra_hours` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_month` (`user_id`,`month_key`),
  KEY `idx_month_key` (`month_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `owner_parking_quota`
--

LOCK TABLES `owner_parking_quota` WRITE;
/*!40000 ALTER TABLE `owner_parking_quota` DISABLE KEYS */;
/*!40000 ALTER TABLE `owner_parking_quota` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_order`
--

DROP TABLE IF EXISTS `parking_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `property_id` bigint DEFAULT NULL,
  `vehicle_no` varchar(10) NOT NULL,
  `order_type` tinyint NOT NULL,
  `source_type` tinyint NOT NULL DEFAULT '1',
  `amount` decimal(10,2) NOT NULL,
  `park_hours` int DEFAULT NULL,
  `free_hours` int NOT NULL DEFAULT '0',
  `daily_cap` decimal(10,2) NOT NULL DEFAULT '30.00',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `transaction_id` varchar(64) DEFAULT NULL,
  `payment_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_vehicle_no` (`vehicle_no`),
  KEY `idx_order_type` (`order_type`,`status`),
  KEY `idx_parking_user_status_deleted` (`user_id`,`status`,`is_deleted`),
  KEY `idx_parking_payment_status_deleted` (`payment_time`,`status`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=4003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_order`
--

LOCK TABLES `parking_order` WRITE;
/*!40000 ALTER TABLE `parking_order` DISABLE KEYS */;
INSERT INTO `parking_order` VALUES (4001,1,1001,'A12345',2,1,300.00,NULL,0,30.00,'2026-03-29 12:08:52','2026-04-28 12:08:52',1,'TXN_PARK_4001',NULL,'2026-03-29 12:08:52','2026-03-29 12:08:52',0),(4002,2,1002,'B67890',1,1,20.00,NULL,0,30.00,'2026-04-17 12:08:52','2026-04-19 12:08:52',0,NULL,NULL,'2026-04-17 12:08:52','2026-04-17 12:08:52',0);
/*!40000 ALTER TABLE `parking_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_consumption_record`
--

DROP TABLE IF EXISTS `points_consumption_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_consumption_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `business_type` tinyint NOT NULL,
  `business_id` bigint NOT NULL,
  `points` int NOT NULL,
  `before_points` int NOT NULL,
  `after_points` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_consumption_user_id` (`user_id`),
  KEY `idx_consumption_business` (`business_type`,`business_id`),
  KEY `idx_consumption_create_time` (`create_time`),
  CONSTRAINT `fk_points_consumption_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_consumption_record`
--

LOCK TABLES `points_consumption_record` WRITE;
/*!40000 ALTER TABLE `points_consumption_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `points_consumption_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_recharge_record`
--

DROP TABLE IF EXISTS `points_recharge_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_recharge_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `operator_id` bigint NOT NULL,
  `amount` int NOT NULL,
  `before_points` int NOT NULL,
  `after_points` int NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recharge_user_id` (`user_id`),
  KEY `idx_recharge_operator_id` (`operator_id`),
  KEY `idx_recharge_create_time` (`create_time`),
  CONSTRAINT `fk_points_recharge_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_points_recharge_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_recharge_record`
--

LOCK TABLES `points_recharge_record` WRITE;
/*!40000 ALTER TABLE `points_recharge_record` DISABLE KEYS */;
INSERT INTO `points_recharge_record` VALUES (1,1,10,1000,0,1000,'鐗╀笟鍏呭€?,'2026-04-19 11:02:42'),(2,1,10,8999,1000,9999,'鐗╀笟鍏呭€?,'2026-04-19 22:00:02');
/*!40000 ALTER TABLE `points_recharge_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `community` varchar(50) NOT NULL,
  `building` varchar(20) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `room` varchar(20) NOT NULL,
  `owner_name` varchar(20) NOT NULL,
  `tenant_name` varchar(64) DEFAULT NULL,
  `rent_end_time` datetime DEFAULT NULL,
  `area` decimal(10,2) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `property_code` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_property_code` (`property_code`),
  KEY `idx_building` (`building`),
  KEY `idx_property_deleted_status` (`is_deleted`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2204 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property`
--

LOCK TABLES `property` WRITE;
/*!40000 ALTER TABLE `property` DISABLE KEYS */;
INSERT INTO property VALUES (1001,'Smart Garden','1','1','101','Owner-A',NULL,'2027-02-25 00:00:00',98.50,4,'2026-02-27 12:08:52','2026-04-20 13:32:25',0,'YZ010110126'),(1002,'Smart Garden','1','1','102','Owner-B',NULL,'2027-02-26 00:00:00',88.20,4,'2026-02-28 12:08:52','2026-04-20 13:32:25',0,'YZ010110226'),(1003,'Smart Garden','2','1','201','Owner-C',NULL,'2027-02-27 00:00:00',108.30,3,'2026-03-01 12:08:52','2026-04-20 13:32:25',0,'YZ020120126');
/*!40000 ALTER TABLE `property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repair_evaluation`
--

DROP TABLE IF EXISTS `repair_evaluation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repair_evaluation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `rating` tinyint NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `is_anonymous` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repair_evaluation`
--

LOCK TABLES `repair_evaluation` WRITE;
/*!40000 ALTER TABLE `repair_evaluation` DISABLE KEYS */;
INSERT INTO `repair_evaluation` VALUES (6001,5002,5,'Very fast service',0,'2026-04-15 12:08:52','2026-04-15 12:08:52',0);
/*!40000 ALTER TABLE `repair_evaluation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repair_fee_bill`
--

DROP TABLE IF EXISTS `repair_fee_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repair_fee_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `property_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `need_points` int NOT NULL DEFAULT '0',
  `paid_points` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0',
  `due_date` datetime DEFAULT NULL,
  `payment_time` datetime DEFAULT NULL,
  `transaction_id` varchar(64) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_property_status` (`property_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repair_fee_bill`
--

LOCK TABLES `repair_fee_bill` WRITE;
/*!40000 ALTER TABLE `repair_fee_bill` DISABLE KEYS */;
/*!40000 ALTER TABLE `repair_fee_bill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repair_order`
--

DROP TABLE IF EXISTS `repair_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `property_id` bigint NOT NULL,
  `service_type` tinyint NOT NULL DEFAULT '1',
  `service_major` varchar(50) DEFAULT NULL,
  `service_sub_type` varchar(100) DEFAULT NULL,
  `category` varchar(20) NOT NULL,
  `description` text NOT NULL,
  `images` varchar(2000) DEFAULT NULL,
  `before_images` varchar(2000) DEFAULT NULL,
  `after_images` varchar(2000) DEFAULT NULL,
  `charge_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `charge_remark` varchar(255) DEFAULT NULL,
  `need_outsource` tinyint(1) NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `assignee` bigint DEFAULT NULL,
  `assigned_time` datetime DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `owner_finish_confirmed` tinyint(1) NOT NULL DEFAULT '0',
  `owner_finish_time` datetime DEFAULT NULL,
  `worker_finish_confirmed` tinyint(1) NOT NULL DEFAULT '0',
  `worker_finish_time` datetime DEFAULT NULL,
  `completion_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `appointment_date` date DEFAULT NULL,
  `appointment_time_slot` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_property_id` (`property_id`),
  KEY `idx_status` (`status`),
  KEY `idx_assignee` (`assignee`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_appointment_date_slot` (`appointment_date`,`appointment_time_slot`),
  KEY `idx_repair_user_status_deleted` (`user_id`,`status`,`is_deleted`),
  KEY `idx_repair_assignee_status_deleted` (`assignee`,`status`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=5006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repair_order`
--

LOCK TABLES `repair_order` WRITE;
/*!40000 ALTER TABLE `repair_order` DISABLE KEYS */;
INSERT INTO epair_order VALUES (5001,1,1001,1,NULL,NULL,'electrical','Living room light flickers','[]',NULL,NULL,0.00,NULL,0,1,NULL,NULL,NULL,0,NULL,0,NULL,NULL,'2026-04-17 12:08:52','2026-04-17 12:08:52',0,NULL,NULL),(5002,2,1002,1,NULL,NULL,'plumbing','Kitchen sink leak','[]',NULL,NULL,0.00,NULL,0,4,20,'2026-04-14 12:08:52','completed',0,NULL,0,NULL,'2026-04-15 12:08:52','2026-04-14 12:08:52','2026-04-15 12:08:52',0,NULL,NULL),(5003,1,1001,1,NULL,NULL,'door','Door lock jammed','[]',NULL,NULL,0.00,NULL,0,2,20,'2026-04-16 12:08:52','processing',0,NULL,0,NULL,NULL,'2026-04-16 12:08:52','2026-04-18 13:59:49',0,NULL,NULL);
/*!40000 ALTER TABLE `repair_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `second_hand`
--

DROP TABLE IF EXISTS `second_hand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `second_hand` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `community` varchar(50) NOT NULL DEFAULT 'Smart Garden',
  `title` varchar(100) NOT NULL,
  `category` varchar(20) NOT NULL DEFAULT 'other',
  `description` text,
  `price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `images` varchar(2000) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `view_count` int NOT NULL DEFAULT '0',
  `report_count` int NOT NULL DEFAULT '0',
  `contact` varchar(50) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `second_hand`
--

LOCK TABLES `second_hand` WRITE;
/*!40000 ALTER TABLE `second_hand` DISABLE KEYS */;
INSERT INTO `second_hand` VALUES (8001,1,'Smart Garden','Baby Stroller','kids',NULL,280.00,'[]',1,0,0,'13800000001','2026-04-13 12:08:52','2026-04-13 12:08:52',0),(8002,2,'Smart Garden','Desk Lamp','home',NULL,60.00,'[]',2,0,0,'13800000002','2026-04-08 12:08:52','2026-04-16 12:08:52',0);
/*!40000 ALTER TABLE `second_hand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `second_hand_favorite`
--

DROP TABLE IF EXISTS `second_hand_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `second_hand_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `second_hand_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`second_hand_id`),
  KEY `idx_goods` (`second_hand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `second_hand_favorite`
--

LOCK TABLES `second_hand_favorite` WRITE;
/*!40000 ALTER TABLE `second_hand_favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `second_hand_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `second_hand_report`
--

DROP TABLE IF EXISTS `second_hand_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `second_hand_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `second_hand_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_report_goods` (`second_hand_id`),
  KEY `idx_report_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `second_hand_report`
--

LOCK TABLES `second_hand_report` WRITE;
/*!40000 ALTER TABLE `second_hand_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `second_hand_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) DEFAULT NULL,
  `unionid` varchar(64) DEFAULT NULL,
  `account` varchar(32) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `must_change_password` tinyint(1) NOT NULL DEFAULT '0',
  `role` tinyint NOT NULL DEFAULT '1',
  `nickname` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `points` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_account` (`account`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=707 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'openid_owner_001','unionid_001','YZ010110126','ZYX123456',1,1,'鍛ㄨ景杞?,NULL,'13800000001',9999,1,'2026-03-09 12:08:52','2026-04-20 16:14:58',0),(2,'openid_owner_002','unionid_002','YZ010110226','LBC123456',1,1,'鍚曞浆杈?,NULL,'13800000002',0,1,'2026-03-29 12:08:52','2026-04-20 16:14:58',0),(3,'openid_owner_003','unionid_003','XQYZ345678','OC123456',1,1,'Owner-C',NULL,'13800000003',0,1,'2026-04-13 12:08:52','2026-04-20 16:14:58',0),(10,'openid_admin_001','unionid_010','WTGL456789','987654',0,2,'Admin-A',NULL,'ENC_13900000010',0,1,'2026-01-08 12:08:52','2026-01-08 12:08:52',0),(20,'openid_worker_001','unionid_020','JZWX567890','098765',0,3,'HK-Clean-01',NULL,'13700000020',0,1,'2026-01-28 12:08:52','2026-04-20 08:18:58',0),(21,'openid_worker_002','unionid_021','JZWX567891','198765',0,3,'HK-Clean-02',NULL,'13700000021',0,1,'2016-07-07 00:00:00','2026-04-20 08:18:58',0),(22,'openid_worker_003','unionid_022','JZWX567892','298765',0,3,'HK-Clean-03',NULL,'13700000022',0,1,'2017-05-13 00:00:00','2026-04-20 08:18:58',0),(23,'openid_worker_004','unionid_023','JZWX567893','398765',0,3,'HK-Clean-04',NULL,'13700000023',0,1,'2020-07-04 00:00:00','2026-04-20 08:18:58',0),(24,'openid_worker_005','unionid_024','JZWX567894','498765',0,3,'HK-Clean-05',NULL,'13700000024',0,1,'2021-05-13 00:00:00','2026-04-20 08:18:58',0),(25,'openid_worker_006','unionid_025','JZWX567895','598765',0,3,'HK-Clean-06',NULL,'13700000025',0,1,'2009-10-24 00:00:00','2026-04-20 08:18:58',0),(26,'openid_worker_007','unionid_026','JZWX567896','698765',0,3,'HK-Clean-07',NULL,'13700000026',0,1,'2014-10-14 00:00:00','2026-04-20 08:18:58',0),(27,'openid_worker_008','unionid_027','JZWX567897','798765',0,3,'HK-Clean-08',NULL,'13700000027',0,1,'2026-10-18 00:00:00','2026-04-20 08:18:58',0),(28,'openid_worker_009','unionid_028','JZWX567898','898765',0,3,'Repair-Plumber-01',NULL,'13700000028',0,1,'2011-02-01 00:00:00','2026-04-20 08:18:58',0),(29,'openid_worker_010','unionid_029','JZWX567899','998765',0,3,'Repair-Electric-01',NULL,'13700000029',0,1,'2016-03-19 00:00:00','2026-04-20 08:18:58',0),(30,'openid_worker_011','unionid_030','JZWX567900','009765',0,3,'Repair-Appliance-01',NULL,'13700000030',0,1,'2011-03-24 00:00:00','2026-04-20 08:18:58',0),(31,'openid_worker_012','unionid_031','JZWX567901','109765',0,3,'Repair-Outsource-01',NULL,'13700000031',0,1,'2022-05-04 00:00:00','2026-04-20 08:18:58',0),(32,'seed-owner-32','seed-union-32','YZ010112225','ZJX123456',1,1,'鍛ㄥ槈鐫?,NULL,'13000001184',0,1,'2025-05-10 00:00:00','2026-04-20 16:14:58',0),(33,'seed-owner-33','seed-union-33','YZ010112321','LKJ123456',1,1,'鍚曞彲鏉?,NULL,'13000001221',0,1,'2021-03-16 00:00:00','2026-04-20 16:14:58',0),(34,'seed-owner-34','seed-union-34','YZ010112510','WBH123456',1,1,'鐜嬪浆鑸?,NULL,'13000001258',0,1,'2010-02-22 00:00:00','2026-04-20 16:14:58',0),(35,'seed-owner-35','seed-union-35','YZ010112924','LYN123456',1,1,'鏉庡溅瀹?,NULL,'13000001295',0,1,'2024-09-29 00:00:00','2026-04-20 16:14:58',0),(36,'seed-owner-36','seed-union-36','YZ010113012','HXX123456',1,1,'浣曞杞?,NULL,'13000001332',0,1,'2012-03-22 00:00:00','2026-04-20 16:14:58',0),(37,'seed-owner-37','seed-union-37','YZ010113121','SXC123456',1,1,'娌堟杈?,NULL,'13000001369',0,1,'2021-08-06 00:00:00','2026-04-20 16:14:58',0),(38,'seed-owner-38','seed-union-38','YZ010113211','ZWX123456',1,1,'閮戞枃钀?,NULL,'13000001406',0,1,'2011-09-24 00:00:00','2026-04-20 16:14:58',0),(39,'seed-owner-39','seed-union-39','YZ010113313','ZLL123456',1,1,'寮犳灄鐞?,NULL,'13000001443',0,1,'2013-02-01 00:00:00','2026-04-20 16:14:58',0),(40,'seed-owner-40','seed-union-40','YZ010113621','SYX123456',1,1,'瀛欏畤濡?,NULL,'13000001480',0,1,'2021-02-16 00:00:00','2026-04-20 16:14:58',0),(41,'seed-owner-41','seed-union-41','YZ010113722','XMX123456',1,1,'璁告槑妤?,NULL,'13000001517',0,1,'2022-06-25 00:00:00','2026-04-20 16:14:58',0),(42,'seed-owner-42','seed-union-42','YZ010113912','WXJ123456',1,1,'鍚村鏉?,NULL,'13000001554',0,1,'2012-06-04 00:00:00','2026-04-20 16:14:58',0),(43,'seed-owner-43','seed-union-43','YZ010114221','FRX123456',1,1,'鍐嫢鐞?,NULL,'13000001591',0,1,'2021-07-21 00:00:00','2026-04-20 16:14:58',0),(44,'seed-owner-44','seed-union-44','YZ010114325','QAC123456',1,1,'閽卞畨鏅?,NULL,'13000001628',0,1,'2025-12-19 00:00:00','2026-04-20 16:14:58',0),(45,'seed-owner-45','seed-union-45','YZ010114618','ZXX123456',1,1,'鍛ㄦ杞?,NULL,'13000001665',0,1,'2018-10-11 00:00:00','2026-04-20 16:14:58',0),(46,'seed-owner-46','seed-union-46','YZ010114714','LZC123456',1,1,'鍚曞瓙杈?,NULL,'13000001702',0,1,'2014-10-25 00:00:00','2026-04-20 16:14:58',0),(47,'seed-owner-47','seed-union-47','YZ010114812','HJX123456',1,1,'闊╁槈钀?,NULL,'13000001739',0,1,'2012-04-14 00:00:00','2026-04-20 16:14:58',0),(48,'seed-owner-48','seed-union-48','YZ010210222','XYX123456',1,1,'瑜氶洦濡?,NULL,'13000001776',0,1,'2022-06-24 00:00:00','2026-04-20 16:14:58',0),(49,'seed-owner-49','seed-union-49','YZ010210324','LSX123456',1,1,'鏉庢€濇',NULL,'13000001813',0,1,'2024-09-18 00:00:00','2026-04-20 16:14:58',0),(50,'seed-owner-50','seed-union-50','YZ010210420','HYX123456',1,1,'浣曚緷鐫?,NULL,'13000001850',0,1,'2020-05-21 00:00:00','2026-04-20 16:14:58',0),(51,'seed-owner-51','seed-union-51','YZ010210509','SYJ123456',1,1,'娌堝溅鏉?,NULL,'13000001887',0,1,'2009-07-04 00:00:00','2026-04-20 16:14:58',0),(52,'seed-owner-52','seed-union-52','YZ010210611','ZXH123456',1,1,'閮戝娑?,NULL,'13000001924',0,1,'2011-12-12 00:00:00','2026-04-20 16:14:58',0),(53,'seed-owner-53','seed-union-53','YZ010210714','ZXH123456',1,1,'寮犳鑸?,NULL,'13000001961',0,1,'2014-12-06 00:00:00','2026-04-20 16:14:58',0),(54,'seed-owner-54','seed-union-54','YZ010210825','ZWX123456',1,1,'鏈辨枃鐞?,NULL,'13000001998',0,1,'2025-10-10 00:00:00','2026-04-20 16:14:58',0),(55,'seed-owner-55','seed-union-55','YZ010210920','CLC123456',1,1,'闄堟灄鏅?,NULL,'13000002035',0,1,'2020-11-27 00:00:00','2026-04-20 16:14:58',0),(56,'seed-owner-56','seed-union-56','YZ010211109','XCN123456',1,1,'璁歌瘹瀹?,NULL,'13000002072',0,1,'2009-10-08 00:00:00','2026-04-20 16:14:58',0),(57,'seed-owner-57','seed-union-57','YZ010211224','JYX123456',1,1,'钂嬪畤杞?,NULL,'13000002109',0,1,'2024-05-28 00:00:00','2026-04-20 16:14:58',0),(58,'seed-owner-58','seed-union-58','YZ010211314','WMC123456',1,1,'鍚存槑杈?,NULL,'13000002146',0,1,'2014-10-04 00:00:00','2026-04-20 16:14:58',0),(59,'seed-owner-59','seed-union-59','YZ010211510','YXL123456',1,1,'鏉ㄥ鐞?,NULL,'13000002183',0,1,'2010-01-01 00:00:00','2026-04-20 16:14:58',0),(60,'seed-owner-60','seed-union-60','YZ010211622','FYX123456',1,1,'鍐偊濠?,NULL,'13000002220',0,1,'2022-01-05 00:00:00','2026-04-20 16:14:58',0),(61,'seed-owner-61','seed-union-61','YZ010211714','QHR123456',1,1,'閽辨旦鐒?,NULL,'13000002257',0,1,'2014-12-14 00:00:00','2026-04-20 16:14:58',0),(62,'seed-owner-62','seed-union-62','YZ010211825','YRX123456',1,1,'灏よ嫢濡?,NULL,'13000002294',0,1,'2025-10-07 00:00:00','2026-04-20 16:14:58',0),(63,'seed-owner-63','seed-union-63','YZ010212018','ZBX123456',1,1,'鍛ㄥ崥鐫?,NULL,'13000002331',0,1,'2018-03-09 00:00:00','2026-04-20 16:14:58',0),(64,'seed-owner-64','seed-union-64','YZ010212314','WZH123456',1,1,'鐜嬪瓙鑸?,NULL,'13000002368',0,1,'2014-08-08 00:00:00','2026-04-20 16:14:58',0),(65,'seed-owner-65','seed-union-65','YZ010212524','QKC123456',1,1,'绉﹀彲鏅?,NULL,'13000002405',0,1,'2024-03-22 00:00:00','2026-04-20 16:14:58',0),(66,'seed-owner-66','seed-union-66','YZ010212815','HYX123456',1,1,'浣曢洦杞?,NULL,'13000002442',0,1,'2015-03-27 00:00:00','2026-04-20 16:14:58',0),(67,'seed-owner-67','seed-union-67','YZ010213021','ZYX123456',1,1,'閮戜緷钀?,NULL,'13000002479',0,1,'2021-04-05 00:00:00','2026-04-20 16:14:58',0),(68,'seed-owner-68','seed-union-68','YZ010213223','ZXX123456',1,1,'鏈卞濠?,NULL,'13000002516',0,1,'2023-04-29 00:00:00','2026-04-20 16:14:58',0),(69,'seed-owner-69','seed-union-69','YZ010213321','CXR123456',1,1,'闄堟鐒?,NULL,'13000002553',0,1,'2021-06-04 00:00:00','2026-04-20 16:14:58',0),(70,'seed-owner-70','seed-union-70','YZ010213422','SWX123456',1,1,'瀛欐枃濡?,NULL,'13000002590',0,1,'2022-03-26 00:00:00','2026-04-20 16:14:58',0),(71,'seed-owner-71','seed-union-71','YZ010213512','XLX123456',1,1,'璁告灄妤?,NULL,'13000002627',0,1,'2012-01-26 00:00:00','2026-04-20 16:14:58',0),(72,'seed-owner-72','seed-union-72','YZ010213623','JSX123456',1,1,'钂嬭瘲鐫?,NULL,'13000002664',0,1,'2023-04-12 00:00:00','2026-04-20 16:14:58',0),(73,'seed-owner-73','seed-union-73','YZ010214120','QXC123456',1,1,'閽卞鏅?,NULL,'13000002701',0,1,'2020-05-07 00:00:00','2026-04-20 16:14:58',0),(74,'seed-owner-74','seed-union-74','YZ010214323','WHN123456',1,1,'鍗旦瀹?,NULL,'13000002738',0,1,'2023-09-18 00:00:00','2026-04-20 16:14:58',0),(75,'seed-owner-75','seed-union-75','YZ010214409','ZRX123456',1,1,'鍛ㄨ嫢杞?,NULL,'13000002775',0,1,'2009-06-02 00:00:00','2026-04-20 16:14:58',0),(76,'seed-owner-76','seed-union-76','YZ010214523','LAC123456',1,1,'鍚曞畨杈?,NULL,'13000002812',0,1,'2023-03-04 00:00:00','2026-04-20 16:14:58',0),(77,'seed-owner-77','seed-union-77','YZ010214615','HBX123456',1,1,'闊╁崥钀?,NULL,'13000002849',0,1,'2015-05-06 00:00:00','2026-04-20 16:14:58',0),(78,'seed-owner-78','seed-union-78','YZ010214725','WNL123456',1,1,'鐜嬪畞鐞?,NULL,'13000002886',0,1,'2025-10-04 00:00:00','2026-04-20 16:14:58',0),(79,'seed-owner-79','seed-union-79','YZ010214917','QZR123456',1,1,'绉﹀瓙鐒?,NULL,'13000002923',0,1,'2017-06-13 00:00:00','2026-04-20 16:14:58',0),(80,'seed-owner-80','seed-union-80','YZ020110109','LKX123456',1,1,'鏉庡彲妤?,NULL,'13000002960',0,1,'2009-11-28 00:00:00','2026-04-20 16:14:58',0),(81,'seed-owner-81','seed-union-81','YZ020110326','SBJ123456',1,1,'娌堝浆鏉?,NULL,'13000002997',0,1,'2026-03-28 00:00:00','2026-04-20 16:14:58',0),(82,'seed-owner-82','seed-union-82','YZ020110416','ZYH123456',1,1,'閮戦洦娑?,NULL,'13000003034',0,1,'2016-01-29 00:00:00','2026-04-20 16:14:58',0),(83,'seed-owner-83','seed-union-83','YZ020110519','ZSH123456',1,1,'寮犳€濊埅',NULL,'13000003071',0,1,'2019-08-31 00:00:00','2026-04-20 16:14:58',0),(84,'seed-owner-84','seed-union-84','YZ020110821','SXF123456',1,1,'瀛欏宄?,NULL,'13000003108',0,1,'2021-06-08 00:00:00','2026-04-20 16:14:58',0),(85,'seed-owner-85','seed-union-85','YZ020111119','WLC123456',1,1,'鍚存灄杈?,NULL,'13000003145',0,1,'2019-04-09 00:00:00','2026-04-20 16:14:58',0),(86,'seed-owner-86','seed-union-86','YZ020111212','SSX123456',1,1,'鏂借瘲钀?,NULL,'13000003182',0,1,'2012-04-10 00:00:00','2026-04-20 16:14:58',0),(87,'seed-owner-87','seed-union-87','YZ020111311','YCL123456',1,1,'鏉ㄨ瘹鐞?,NULL,'13000003219',0,1,'2011-08-04 00:00:00','2026-04-20 16:14:58',0),(88,'seed-owner-88','seed-union-88','YZ020111419','FYX123456',1,1,'鍐畤濠?,NULL,'13000003256',0,1,'2019-10-29 00:00:00','2026-04-20 16:14:58',0),(89,'seed-owner-89','seed-union-89','YZ020111621','YCX123456',1,1,'灏ゆ櫒濡?,NULL,'13000003293',0,1,'2021-04-02 00:00:00','2026-04-20 16:14:58',0),(90,'seed-owner-90','seed-union-90','YZ020111716','WXX123456',1,1,'鍗妤?,NULL,'13000003330',0,1,'2016-04-13 00:00:00','2026-04-20 16:14:58',0),(91,'seed-owner-91','seed-union-91','YZ020111826','ZYX123456',1,1,'鍛ㄦ偊鐫?,NULL,'13000003367',0,1,'2026-08-31 00:00:00','2026-04-20 16:14:58',0),(92,'seed-owner-92','seed-union-92','YZ020111919','LHJ123456',1,1,'鍚曟旦鏉?,NULL,'13000003404',0,1,'2019-07-20 00:00:00','2026-04-20 16:14:58',0),(93,'seed-owner-93','seed-union-93','YZ020112016','HRH123456',1,1,'闊╄嫢娑?,NULL,'13000003441',0,1,'2016-01-11 00:00:00','2026-04-20 16:14:58',0),(94,'seed-owner-94','seed-union-94','YZ020112613','HJX123456',1,1,'浣曞槈杞?,NULL,'13000003478',0,1,'2013-10-11 00:00:00','2026-04-20 16:14:58',0),(95,'seed-owner-95','seed-union-95','YZ020112722','SKC123456',1,1,'娌堝彲杈?,NULL,'13000003515',0,1,'2022-01-07 00:00:00','2026-04-20 16:14:58',0),(96,'seed-owner-96','seed-union-96','YZ020112922','ZBL123456',1,1,'寮犲浆鐞?,NULL,'13000003552',0,1,'2022-02-15 00:00:00','2026-04-20 16:14:58',0),(97,'seed-owner-97','seed-union-97','YZ020113021','ZYX123456',1,1,'鏈遍洦濠?,NULL,'13000003589',0,1,'2021-03-29 00:00:00','2026-04-20 16:14:58',0),(98,'seed-owner-98','seed-union-98','YZ020113121','CSR123456',1,1,'闄堟€濈劧',NULL,'13000003626',0,1,'2021-12-21 00:00:00','2026-04-20 16:14:58',0),(99,'seed-owner-99','seed-union-99','YZ020113214','SYX123456',1,1,'瀛欎緷濡?,NULL,'13000003663',0,1,'2014-02-21 00:00:00','2026-04-20 16:14:58',0),(100,'seed-owner-100','seed-union-100','YZ020113316','XYX123456',1,1,'璁稿溅妤?,NULL,'13000003700',0,1,'2016-02-22 00:00:00','2026-04-20 16:14:58',0),(101,'seed-owner-101','seed-union-101','YZ020113912','QCC123456',1,1,'閽辫瘹鏅?,NULL,'13000003737',0,1,'2012-04-19 00:00:00','2026-04-20 16:14:58',0),(102,'seed-owner-102','seed-union-102','YZ020114015','YYF123456',1,1,'灏ゅ畤宄?,NULL,'13000003774',0,1,'2015-02-03 00:00:00','2026-04-20 16:14:58',0),(103,'seed-owner-103','seed-union-103','YZ020114324','LXC123456',1,1,'鍚曞杈?,NULL,'13000003811',0,1,'2024-06-19 00:00:00','2026-04-20 16:14:58',0),(104,'seed-owner-104','seed-union-104','YZ020114717','QAR123456',1,1,'绉﹀畨鐒?,NULL,'13000003848',0,1,'2017-08-17 00:00:00','2026-04-20 16:14:58',0),(105,'seed-owner-105','seed-union-105','YZ020114811','XBX123456',1,1,'瑜氬崥濡?,NULL,'13000003885',0,1,'2011-01-16 00:00:00','2026-04-20 16:14:58',0),(106,'seed-owner-106','seed-union-106','YZ020210114','SZJ123456',1,1,'娌堝瓙鏉?,NULL,'13000003922',0,1,'2014-05-19 00:00:00','2026-04-20 16:14:58',0),(107,'seed-owner-107','seed-union-107','YZ020210216','ZJH123456',1,1,'閮戝槈娑?,NULL,'13000003959',0,1,'2016-04-12 00:00:00','2026-04-20 16:14:58',0),(108,'seed-owner-108','seed-union-108','YZ020210314','ZKH123456',1,1,'寮犲彲鑸?,NULL,'13000003996',0,1,'2014-02-20 00:00:00','2026-04-20 16:14:58',0),(109,'seed-owner-109','seed-union-109','YZ020210425','ZCX123456',1,1,'鏈辫景鐞?,NULL,'13000004033',0,1,'2025-06-04 00:00:00','2026-04-20 16:14:58',0),(110,'seed-owner-110','seed-union-110','YZ020210617','SYF123456',1,1,'瀛欓洦宄?,NULL,'13000004070',0,1,'2017-09-13 00:00:00','2026-04-20 16:14:58',0),(111,'seed-owner-111','seed-union-111','YZ020210724','XSN123456',1,1,'璁告€濆畞',NULL,'13000004107',0,1,'2024-07-09 00:00:00','2026-04-20 16:14:58',0),(112,'seed-owner-112','seed-union-112','YZ020210825','JYX123456',1,1,'钂嬩緷杞?,NULL,'13000004144',0,1,'2025-09-30 00:00:00','2026-04-20 16:14:58',0),(113,'seed-owner-113','seed-union-113','YZ020210911','WYC123456',1,1,'鍚村溅杈?,NULL,'13000004181',0,1,'2011-10-06 00:00:00','2026-04-20 16:14:58',0),(114,'seed-owner-114','seed-union-114','YZ020211114','YXL123456',1,1,'鏉ㄦ鐞?,NULL,'13000004218',0,1,'2014-12-30 00:00:00','2026-04-20 16:14:58',0),(115,'seed-owner-115','seed-union-115','YZ020211216','FWX123456',1,1,'鍐枃濠?,NULL,'13000004255',0,1,'2016-09-24 00:00:00','2026-04-20 16:14:58',0),(116,'seed-owner-116','seed-union-116','YZ020211315','QLR123456',1,1,'閽辨灄鐒?,NULL,'13000004292',0,1,'2015-08-14 00:00:00','2026-04-20 16:14:58',0),(117,'seed-owner-117','seed-union-117','YZ020211423','YSX123456',1,1,'灏よ瘲濡?,NULL,'13000004329',0,1,'2023-08-30 00:00:00','2026-04-20 16:14:58',0),(118,'seed-owner-118','seed-union-118','YZ020211513','WCX123456',1,1,'鍗瘹妤?,NULL,'13000004366',0,1,'2013-03-20 00:00:00','2026-04-20 16:14:58',0),(119,'seed-owner-119','seed-union-119','YZ020211622','ZYX123456',1,1,'鍛ㄥ畤鐫?,NULL,'13000004403',0,1,'2022-06-02 00:00:00','2026-04-20 16:14:58',0),(120,'seed-owner-120','seed-union-120','YZ020211714','LMJ123456',1,1,'鍚曟槑鏉?,NULL,'13000004440',0,1,'2014-07-16 00:00:00','2026-04-20 16:14:58',0),(121,'seed-owner-121','seed-union-121','YZ020211817','HCH123456',1,1,'闊╂櫒娑?,NULL,'13000004477',0,1,'2017-10-21 00:00:00','2026-04-20 16:14:58',0),(122,'seed-owner-122','seed-union-122','YZ020211923','WXH123456',1,1,'鐜嬪鑸?,NULL,'13000004514',0,1,'2023-09-21 00:00:00','2026-04-20 16:14:58',0),(123,'seed-owner-123','seed-union-123','YZ020212122','QHC123456',1,1,'绉︽旦鏅?,NULL,'13000004551',0,1,'2022-02-01 00:00:00','2026-04-20 16:14:58',0),(124,'seed-owner-124','seed-union-124','YZ020212321','LAN123456',1,1,'鏉庡畨瀹?,NULL,'13000004588',0,1,'2021-01-30 00:00:00','2026-04-20 16:14:58',0),(125,'seed-owner-125','seed-union-125','YZ020212418','HBX123456',1,1,'浣曞崥杞?,NULL,'13000004625',0,1,'2018-09-15 00:00:00','2026-04-20 16:14:58',0),(126,'seed-owner-126','seed-union-126','YZ020212509','SNC123456',1,1,'娌堝畞杈?,NULL,'13000004662',0,1,'2009-03-02 00:00:00','2026-04-20 16:14:58',0),(127,'seed-owner-127','seed-union-127','YZ020213109','XBX123456',1,1,'璁稿浆妤?,NULL,'13000004699',0,1,'2009-08-03 00:00:00','2026-04-20 16:14:58',0),(128,'seed-owner-128','seed-union-128','YZ020213209','JYX123456',1,1,'钂嬮洦鐫?,NULL,'13000004736',0,1,'2009-07-06 00:00:00','2026-04-20 16:14:58',0),(129,'seed-owner-129','seed-union-129','YZ020213410','SYH123456',1,1,'鏂戒緷娑?,NULL,'13000004773',0,1,'2010-02-12 00:00:00','2026-04-20 16:14:58',0),(130,'seed-owner-130','seed-union-130','YZ020213519','YYH123456',1,1,'鏉ㄥ溅鑸?,NULL,'13000004810',0,1,'2019-01-24 00:00:00','2026-04-20 16:14:58',0),(131,'seed-owner-131','seed-union-131','YZ020213715','QXC123456',1,1,'閽辨鏅?,NULL,'13000004847',0,1,'2015-04-24 00:00:00','2026-04-20 16:14:58',0),(132,'seed-owner-132','seed-union-132','YZ020214320','WML123456',1,1,'鐜嬫槑鐞?,NULL,'13000004884',0,1,'2020-12-27 00:00:00','2026-04-20 16:14:58',0),(133,'seed-owner-133','seed-union-133','YZ020214411','ZCX123456',1,1,'璧垫櫒濠?,NULL,'13000004921',0,1,'2011-08-09 00:00:00','2026-04-20 16:14:58',0),(134,'seed-owner-134','seed-union-134','YZ020214519','QXR123456',1,1,'绉﹀鐒?,NULL,'13000004958',0,1,'2019-08-11 00:00:00','2026-04-20 16:14:58',0),(135,'seed-owner-135','seed-union-135','YZ020214613','XYX123456',1,1,'瑜氭偊濡?,NULL,'13000004995',0,1,'2013-04-16 00:00:00','2026-04-20 16:14:58',0),(136,'seed-owner-136','seed-union-136','YZ020214711','LHX123456',1,1,'鏉庢旦妤?,NULL,'13000005032',0,1,'2011-09-14 00:00:00','2026-04-20 16:14:58',0),(137,'seed-owner-137','seed-union-137','YZ020214819','ZYX123456',1,1,'鍛ㄥ畤杞?,NULL,'13000005069',0,1,'2019-04-29 00:00:00','2026-04-20 16:14:58',0),(138,'seed-owner-138','seed-union-138','YZ020214923','SAJ123456',1,1,'娌堝畨鏉?,NULL,'13000005106',0,1,'2023-12-30 00:00:00','2026-04-20 16:14:58',0),(139,'seed-owner-139','seed-union-139','YZ030110113','ZNH123456',1,1,'寮犲畞鑸?,NULL,'13000005143',0,1,'2013-10-01 00:00:00','2026-04-20 16:14:58',0),(140,'seed-owner-140','seed-union-140','YZ030110223','ZXX123456',1,1,'鏈辨鐞?,NULL,'13000005180',0,1,'2023-05-25 00:00:00','2026-04-20 16:14:58',0),(141,'seed-owner-141','seed-union-141','YZ030110324','CZC123456',1,1,'闄堝瓙鏅?,NULL,'13000005217',0,1,'2024-05-14 00:00:00','2026-04-20 16:14:58',0),(142,'seed-owner-142','seed-union-142','YZ030110719','WBC123456',1,1,'鍚村浆杈?,NULL,'13000005254',0,1,'2019-07-20 00:00:00','2026-04-20 16:14:58',0),(143,'seed-owner-143','seed-union-143','YZ030111019','FYX123456',1,1,'鍐緷濠?,NULL,'13000005291',0,1,'2019-02-05 00:00:00','2026-04-20 16:14:58',0),(144,'seed-owner-144','seed-union-144','YZ030111119','QYR123456',1,1,'閽卞溅鐒?,NULL,'13000005328',0,1,'2019-08-11 00:00:00','2026-04-20 16:14:58',0),(145,'seed-owner-145','seed-union-145','YZ030111209','YXX123456',1,1,'灏ゅ濡?,NULL,'13000005365',0,1,'2009-02-11 00:00:00','2026-04-20 16:14:58',0),(146,'seed-owner-146','seed-union-146','YZ030111418','ZWX123456',1,1,'鍛ㄦ枃鐫?,NULL,'13000005402',0,1,'2018-09-02 00:00:00','2026-04-20 16:14:58',0),(147,'seed-owner-147','seed-union-147','YZ030111524','LLJ123456',1,1,'鍚曟灄鏉?,NULL,'13000005439',0,1,'2024-10-14 00:00:00','2026-04-20 16:14:58',0),(148,'seed-owner-148','seed-union-148','YZ030111709','WCH123456',1,1,'鐜嬭瘹鑸?,NULL,'13000005476',0,1,'2009-09-27 00:00:00','2026-04-20 16:14:58',0),(149,'seed-owner-149','seed-union-149','YZ030111817','ZYX123456',1,1,'璧靛畤鐞?,NULL,'13000005513',0,1,'2017-11-09 00:00:00','2026-04-20 16:14:58',0),(150,'seed-owner-150','seed-union-150','YZ030111913','QMC123456',1,1,'绉︽槑鏅?,NULL,'13000005550',0,1,'2013-08-17 00:00:00','2026-04-20 16:14:58',0),(151,'seed-owner-151','seed-union-151','YZ030112013','XCF123456',1,1,'瑜氭櫒宄?,NULL,'13000005587',0,1,'2013-12-17 00:00:00','2026-04-20 16:14:58',0),(152,'seed-owner-152','seed-union-152','YZ030112110','LXN123456',1,1,'鏉庡瀹?,NULL,'13000005624',0,1,'2010-10-30 00:00:00','2026-04-20 16:14:58',0),(153,'seed-owner-153','seed-union-153','YZ030112221','HYX123456',1,1,'浣曟偊杞?,NULL,'13000005661',0,1,'2021-04-16 00:00:00','2026-04-20 16:14:58',0),(154,'seed-owner-154','seed-union-154','YZ030112426','ZRX123456',1,1,'閮戣嫢钀?,NULL,'13000005698',0,1,'2026-10-19 00:00:00','2026-04-20 16:14:58',0),(155,'seed-owner-155','seed-union-155','YZ030112515','ZAL123456',1,1,'寮犲畨鐞?,NULL,'13000005735',0,1,'2015-02-05 00:00:00','2026-04-20 16:14:58',0),(156,'seed-owner-156','seed-union-156','YZ030112920','XZX123456',1,1,'璁稿瓙妤?,NULL,'13000005772',0,1,'2020-06-17 00:00:00','2026-04-20 16:14:58',0),(157,'seed-owner-157','seed-union-157','YZ030113215','SCH123456',1,1,'鏂借景娑?,NULL,'13000005809',0,1,'2015-12-13 00:00:00','2026-04-20 16:14:58',0),(158,'seed-owner-158','seed-union-158','YZ030113326','YBH123456',1,1,'鏉ㄥ浆鑸?,NULL,'13000005846',0,1,'2026-12-24 00:00:00','2026-04-20 16:14:58',0),(159,'seed-owner-159','seed-union-159','YZ030113525','QSC123456',1,1,'閽辨€濇櫒',NULL,'13000005883',0,1,'2025-07-28 00:00:00','2026-04-20 16:14:58',0),(160,'seed-owner-160','seed-union-160','YZ030113711','WYN123456',1,1,'鍗溅瀹?,NULL,'13000005920',0,1,'2011-05-26 00:00:00','2026-04-20 16:14:58',0),(161,'seed-owner-161','seed-union-161','YZ030114015','HWX123456',1,1,'闊╂枃钀?,NULL,'13000005957',0,1,'2015-09-08 00:00:00','2026-04-20 16:14:58',0),(162,'seed-owner-162','seed-union-162','YZ030114514','LMX123456',1,1,'鏉庢槑妤?,NULL,'13000005994',0,1,'2014-02-02 00:00:00','2026-04-20 16:14:58',0),(163,'seed-owner-163','seed-union-163','YZ030114712','SXJ123456',1,1,'娌堝鏉?,NULL,'13000006031',0,1,'2012-05-03 00:00:00','2026-04-20 16:14:58',0),(164,'seed-owner-164','seed-union-164','YZ030115024','ZRX123456',1,1,'鏈辫嫢鐞?,NULL,'13000006068',0,1,'2024-03-04 00:00:00','2026-04-20 16:14:58',0),(165,'seed-owner-165','seed-union-165','YZ030210409','JXX123456',1,1,'钂嬫杞?,NULL,'13000006105',0,1,'2009-07-04 00:00:00','2026-04-20 16:14:58',0),(166,'seed-owner-166','seed-union-166','YZ030210521','WZC123456',1,1,'鍚村瓙杈?,NULL,'13000006142',0,1,'2021-10-08 00:00:00','2026-04-20 16:14:58',0),(167,'seed-owner-167','seed-union-167','YZ030210714','YKL123456',1,1,'鏉ㄥ彲鐞?,NULL,'13000006179',0,1,'2014-12-27 00:00:00','2026-04-20 16:14:58',0),(168,'seed-owner-168','seed-union-168','YZ030210823','FCX123456',1,1,'鍐景濠?,NULL,'13000006216',0,1,'2023-01-16 00:00:00','2026-04-20 16:14:58',0),(169,'seed-owner-169','seed-union-169','YZ030210916','QBR123456',1,1,'閽卞浆鐒?,NULL,'13000006253',0,1,'2016-09-18 00:00:00','2026-04-20 16:14:58',0),(170,'seed-owner-170','seed-union-170','YZ030211013','YYX123456',1,1,'灏ら洦濡?,NULL,'13000006290',0,1,'2013-01-03 00:00:00','2026-04-20 16:14:58',0),(171,'seed-owner-171','seed-union-171','YZ030211219','ZYX123456',1,1,'鍛ㄤ緷鐫?,NULL,'13000006327',0,1,'2019-10-27 00:00:00','2026-04-20 16:14:58',0),(172,'seed-owner-172','seed-union-172','YZ030211323','LYJ123456',1,1,'鍚曞溅鏉?,NULL,'13000006364',0,1,'2023-05-25 00:00:00','2026-04-20 16:14:58',0),(173,'seed-owner-173','seed-union-173','YZ030211422','HXH123456',1,1,'闊╁娑?,NULL,'13000006401',0,1,'2022-08-05 00:00:00','2026-04-20 16:14:58',0),(174,'seed-owner-174','seed-union-174','YZ030211624','ZWX123456',1,1,'璧垫枃鐞?,NULL,'13000006438',0,1,'2024-02-13 00:00:00','2026-04-20 16:14:58',0),(175,'seed-owner-175','seed-union-175','YZ030211919','LCN123456',1,1,'鏉庤瘹瀹?,NULL,'13000006475',0,1,'2019-09-10 00:00:00','2026-04-20 16:14:58',0),(176,'seed-owner-176','seed-union-176','YZ030212011','HYX123456',1,1,'浣曞畤杞?,NULL,'13000006512',0,1,'2011-11-20 00:00:00','2026-04-20 16:14:58',0),(177,'seed-owner-177','seed-union-177','YZ030212123','SMC123456',1,1,'娌堟槑杈?,NULL,'13000006549',0,1,'2023-01-04 00:00:00','2026-04-20 16:14:58',0),(178,'seed-owner-178','seed-union-178','YZ030212420','ZYX123456',1,1,'鏈辨偊濠?,NULL,'13000006586',0,1,'2020-03-28 00:00:00','2026-04-20 16:14:58',0),(179,'seed-owner-179','seed-union-179','YZ030212511','CHR123456',1,1,'闄堟旦鐒?,NULL,'13000006623',0,1,'2011-11-29 00:00:00','2026-04-20 16:14:58',0),(180,'seed-owner-180','seed-union-180','YZ030212610','SRX123456',1,1,'瀛欒嫢濡?,NULL,'13000006660',0,1,'2010-03-18 00:00:00','2026-04-20 16:14:58',0),(181,'seed-owner-181','seed-union-181','YZ030213012','SXH123456',1,1,'鏂芥娑?,NULL,'13000006697',0,1,'2012-05-07 00:00:00','2026-04-20 16:14:58',0),(182,'seed-owner-182','seed-union-182','YZ030213124','YZH123456',1,1,'鏉ㄥ瓙鑸?,NULL,'13000006734',0,1,'2024-04-27 00:00:00','2026-04-20 16:14:58',0),(183,'seed-owner-183','seed-union-183','YZ030213324','QKC123456',1,1,'閽卞彲鏅?,NULL,'13000006771',0,1,'2024-03-08 00:00:00','2026-04-20 16:14:58',0),(184,'seed-owner-184','seed-union-184','YZ030213418','YCF123456',1,1,'灏よ景宄?,NULL,'13000006808',0,1,'2018-05-19 00:00:00','2026-04-20 16:14:58',0),(185,'seed-owner-185','seed-union-185','YZ030213613','ZYX123456',1,1,'鍛ㄩ洦杞?,NULL,'13000006845',0,1,'2013-12-05 00:00:00','2026-04-20 16:14:58',0),(186,'seed-owner-186','seed-union-186','YZ030214125','QXR123456',1,1,'绉︽鐒?,NULL,'13000006882',0,1,'2025-02-11 00:00:00','2026-04-20 16:14:58',0),(187,'seed-owner-187','seed-union-187','YZ030214415','HSX123456',1,1,'浣曡瘲鐫?,NULL,'13000006919',0,1,'2015-12-20 00:00:00','2026-04-20 16:14:58',0),(188,'seed-owner-188','seed-union-188','YZ030214511','SCJ123456',1,1,'娌堣瘹鏉?,NULL,'13000006956',0,1,'2011-10-16 00:00:00','2026-04-20 16:14:58',0),(189,'seed-owner-189','seed-union-189','YZ030214625','ZYH123456',1,1,'閮戝畤娑?,NULL,'13000006993',0,1,'2025-12-16 00:00:00','2026-04-20 16:14:58',0),(190,'seed-owner-190','seed-union-190','YZ030214815','ZCX123456',1,1,'鏈辨櫒鐞?,NULL,'13000007030',0,1,'2015-08-21 00:00:00','2026-04-20 16:14:58',0),(191,'seed-owner-191','seed-union-191','YZ030215015','SYF123456',1,1,'瀛欐偊宄?,NULL,'13000007067',0,1,'2015-09-20 00:00:00','2026-04-20 16:14:58',0),(192,'seed-owner-192','seed-union-192','YZ040110214','JRX123456',1,1,'钂嬭嫢杞?,NULL,'13000007104',0,1,'2014-11-18 00:00:00','2026-04-20 16:14:58',0),(193,'seed-owner-193','seed-union-193','YZ040110317','WAC123456',1,1,'鍚村畨杈?,NULL,'13000007141',0,1,'2017-07-19 00:00:00','2026-04-20 16:14:58',0),(194,'seed-owner-194','seed-union-194','YZ040110716','QZR123456',1,1,'閽卞瓙鐒?,NULL,'13000007178',0,1,'2016-03-11 00:00:00','2026-04-20 16:14:58',0),(195,'seed-owner-195','seed-union-195','YZ040110818','YJX123456',1,1,'灏ゅ槈濡?,NULL,'13000007215',0,1,'2018-08-24 00:00:00','2026-04-20 16:14:58',0),(196,'seed-owner-196','seed-union-196','YZ040111017','ZCX123456',1,1,'鍛ㄨ景鐫?,NULL,'13000007252',0,1,'2017-07-03 00:00:00','2026-04-20 16:14:58',0),(197,'seed-owner-197','seed-union-197','YZ040111123','LBJ123456',1,1,'鍚曞浆鏉?,NULL,'13000007289',0,1,'2023-03-22 00:00:00','2026-04-20 16:14:58',0),(198,'seed-owner-198','seed-union-198','YZ040111316','WSH123456',1,1,'鐜嬫€濊埅',NULL,'13000007326',0,1,'2016-02-12 00:00:00','2026-04-20 16:14:58',0),(199,'seed-owner-199','seed-union-199','YZ040111825','HWX123456',1,1,'浣曟枃杞?,NULL,'13000007363',0,1,'2025-05-18 00:00:00','2026-04-20 16:14:58',0),(200,'seed-owner-200','seed-union-200','YZ040111910','SLC123456',1,1,'娌堟灄杈?,NULL,'13000007400',0,1,'2010-10-15 00:00:00','2026-04-20 16:14:58',0),(201,'seed-owner-201','seed-union-201','YZ040112120','ZCL123456',1,1,'寮犺瘹鐞?,NULL,'13000007437',0,1,'2020-10-05 00:00:00','2026-04-20 16:14:58',0),(202,'seed-owner-202','seed-union-202','YZ040112324','CMR123456',1,1,'闄堟槑鐒?,NULL,'13000007474',0,1,'2024-12-05 00:00:00','2026-04-20 16:14:58',0),(203,'seed-owner-203','seed-union-203','YZ040112425','SCX123456',1,1,'瀛欐櫒濡?,NULL,'13000007511',0,1,'2025-12-16 00:00:00','2026-04-20 16:14:58',0),(204,'seed-owner-204','seed-union-204','YZ040112823','SRH123456',1,1,'鏂借嫢娑?,NULL,'13000007548',0,1,'2023-12-15 00:00:00','2026-04-20 16:14:58',0),(205,'seed-owner-205','seed-union-205','YZ040113020','FBX123456',1,1,'鍐崥鐞?,NULL,'13000007585',0,1,'2020-11-30 00:00:00','2026-04-20 16:14:58',0),(206,'seed-owner-206','seed-union-206','YZ040113321','WZN123456',1,1,'鍗瓙瀹?,NULL,'13000007622',0,1,'2021-09-27 00:00:00','2026-04-20 16:14:58',0),(207,'seed-owner-207','seed-union-207','YZ040113414','ZJX123456',1,1,'鍛ㄥ槈杞?,NULL,'13000007659',0,1,'2014-01-10 00:00:00','2026-04-20 16:14:58',0),(208,'seed-owner-208','seed-union-208','YZ040113513','LKC123456',1,1,'鍚曞彲杈?,NULL,'13000007696',0,1,'2013-02-22 00:00:00','2026-04-20 16:14:58',0),(209,'seed-owner-209','seed-union-209','YZ040113722','WBL123456',1,1,'鐜嬪浆鐞?,NULL,'13000007733',0,1,'2022-09-14 00:00:00','2026-04-20 16:14:58',0),(210,'seed-owner-210','seed-union-210','YZ040114016','XYX123456',1,1,'瑜氫緷濡?,NULL,'13000007770',0,1,'2016-10-23 00:00:00','2026-04-20 16:14:58',0),(211,'seed-owner-211','seed-union-211','YZ040114118','LYX123456',1,1,'鏉庡溅妤?,NULL,'13000007807',0,1,'2018-10-21 00:00:00','2026-04-20 16:14:58',0),(212,'seed-owner-212','seed-union-212','YZ040114224','HXX123456',1,1,'浣曞鐫?,NULL,'13000007844',0,1,'2024-06-11 00:00:00','2026-04-20 16:14:58',0),(213,'seed-owner-213','seed-union-213','YZ040114421','ZWH123456',1,1,'閮戞枃娑?,NULL,'13000007881',0,1,'2021-11-18 00:00:00','2026-04-20 16:14:58',0),(214,'seed-owner-214','seed-union-214','YZ040114526','ZLH123456',1,1,'寮犳灄鑸?,NULL,'13000007918',0,1,'2026-09-10 00:00:00','2026-04-20 16:14:58',0),(215,'seed-owner-215','seed-union-215','YZ040114614','ZSX123456',1,1,'鏈辫瘲鐞?,NULL,'13000007955',0,1,'2014-08-02 00:00:00','2026-04-20 16:14:58',0),(216,'seed-owner-216','seed-union-216','YZ040114713','CCC123456',1,1,'闄堣瘹鏅?,NULL,'13000007992',0,1,'2013-12-27 00:00:00','2026-04-20 16:14:58',0),(217,'seed-owner-217','seed-union-217','YZ040114915','XMN123456',1,1,'璁告槑瀹?,NULL,'13000008029',0,1,'2015-09-13 00:00:00','2026-04-20 16:14:58',0),(218,'seed-owner-218','seed-union-218','YZ040210215','SYX123456',1,1,'鏂芥偊钀?,NULL,'13000008066',0,1,'2015-12-30 00:00:00','2026-04-20 16:14:58',0),(219,'seed-owner-219','seed-union-219','YZ040210309','YHL123456',1,1,'鏉ㄦ旦鐞?,NULL,'13000008103',0,1,'2009-09-04 00:00:00','2026-04-20 16:14:58',0),(220,'seed-owner-220','seed-union-220','YZ040210614','YBX123456',1,1,'灏ゅ崥濡?,NULL,'13000008140',0,1,'2014-11-22 00:00:00','2026-04-20 16:14:58',0),(221,'seed-owner-221','seed-union-221','YZ040210913','LZJ123456',1,1,'鍚曞瓙鏉?,NULL,'13000008177',0,1,'2013-02-24 00:00:00','2026-04-20 16:14:58',0),(222,'seed-owner-222','seed-union-222','YZ040211016','HJH123456',1,1,'闊╁槈娑?,NULL,'13000008214',0,1,'2016-01-27 00:00:00','2026-04-20 16:14:58',0),(223,'seed-owner-223','seed-union-223','YZ040211112','WKH123456',1,1,'鐜嬪彲鑸?,NULL,'13000008251',0,1,'2012-08-24 00:00:00','2026-04-20 16:14:58',0),(224,'seed-owner-224','seed-union-224','YZ040211315','QBC123456',1,1,'绉﹀浆鏅?,NULL,'13000008288',0,1,'2015-03-19 00:00:00','2026-04-20 16:14:58',0),(225,'seed-owner-225','seed-union-225','YZ040211417','XYF123456',1,1,'瑜氶洦宄?,NULL,'13000008325',0,1,'2017-07-13 00:00:00','2026-04-20 16:14:58',0),(226,'seed-owner-226','seed-union-226','YZ040211719','SYC123456',1,1,'娌堝溅杈?,NULL,'13000008362',0,1,'2019-05-01 00:00:00','2026-04-20 16:14:58',0),(227,'seed-owner-227','seed-union-227','YZ040211817','ZXX123456',1,1,'閮戝钀?,NULL,'13000008399',0,1,'2017-04-23 00:00:00','2026-04-20 16:14:58',0),(228,'seed-owner-228','seed-union-228','YZ040211916','ZXL123456',1,1,'寮犳鐞?,NULL,'13000008436',0,1,'2016-10-02 00:00:00','2026-04-20 16:14:58',0),(229,'seed-owner-229','seed-union-229','YZ040212226','SSX123456',1,1,'瀛欒瘲濡?,NULL,'13000008473',0,1,'2026-06-30 00:00:00','2026-04-20 16:14:58',0),(230,'seed-owner-230','seed-union-230','YZ040212722','YXH123456',1,1,'鏉ㄥ鑸?,NULL,'13000008510',0,1,'2022-12-25 00:00:00','2026-04-20 16:14:58',0),(231,'seed-owner-231','seed-union-231','YZ040212818','FYX123456',1,1,'鍐偊鐞?,NULL,'13000008547',0,1,'2018-11-14 00:00:00','2026-04-20 16:14:58',0),(232,'seed-owner-232','seed-union-232','YZ040212924','QHC123456',1,1,'閽辨旦鏅?,NULL,'13000008584',0,1,'2024-12-23 00:00:00','2026-04-20 16:14:58',0),(233,'seed-owner-233','seed-union-233','YZ040213012','YRF123456',1,1,'灏よ嫢宄?,NULL,'13000008621',0,1,'2012-10-28 00:00:00','2026-04-20 16:14:58',0),(234,'seed-owner-234','seed-union-234','YZ040213425','HXX123456',1,1,'闊╂钀?,NULL,'13000008658',0,1,'2025-02-12 00:00:00','2026-04-20 16:14:58',0),(235,'seed-owner-235','seed-union-235','YZ040213718','QKR123456',1,1,'绉﹀彲鐒?,NULL,'13000008695',0,1,'2018-11-01 00:00:00','2026-04-20 16:14:58',0),(236,'seed-owner-236','seed-union-236','YZ040213819','XCX123456',1,1,'瑜氳景濡?,NULL,'13000008732',0,1,'2019-12-28 00:00:00','2026-04-20 16:14:58',0),(237,'seed-owner-237','seed-union-237','YZ040214218','ZYH123456',1,1,'閮戜緷娑?,NULL,'13000008769',0,1,'2018-06-27 00:00:00','2026-04-20 16:14:58',0),(238,'seed-owner-238','seed-union-238','YZ040214311','ZYH123456',1,1,'寮犲溅鑸?,NULL,'13000008806',0,1,'2011-07-31 00:00:00','2026-04-20 16:14:58',0),(239,'seed-owner-239','seed-union-239','YZ040214515','CXC123456',1,1,'闄堟鏅?,NULL,'13000008843',0,1,'2015-06-09 00:00:00','2026-04-20 16:14:58',0),(240,'seed-owner-240','seed-union-240','YZ040214617','SWF123456',1,1,'瀛欐枃宄?,NULL,'13000008880',0,1,'2017-03-13 00:00:00','2026-04-20 16:14:58',0),(241,'seed-owner-241','seed-union-241','YZ040214722','XLN123456',1,1,'璁告灄瀹?,NULL,'13000008917',0,1,'2022-11-30 00:00:00','2026-04-20 16:14:58',0),(242,'seed-owner-242','seed-union-242','YZ040214818','JSX123456',1,1,'钂嬭瘲杞?,NULL,'13000008954',0,1,'2018-11-14 00:00:00','2026-04-20 16:14:58',0),(243,'seed-owner-243','seed-union-243','YZ050110113','YML123456',1,1,'鏉ㄦ槑鐞?,NULL,'13000008991',0,1,'2013-12-08 00:00:00','2026-04-20 16:14:58',0),(244,'seed-owner-244','seed-union-244','YZ050110213','FCX123456',1,1,'鍐櫒濠?,NULL,'13000009028',0,1,'2013-06-20 00:00:00','2026-04-20 16:14:58',0),(245,'seed-owner-245','seed-union-245','YZ050110317','QXR123456',1,1,'閽卞鐒?,NULL,'13000009065',0,1,'2017-03-14 00:00:00','2026-04-20 16:14:58',0),(246,'seed-owner-246','seed-union-246','YZ050110422','YYX123456',1,1,'灏ゆ偊濡?,NULL,'13000009102',0,1,'2022-03-20 00:00:00','2026-04-20 16:14:58',0),(247,'seed-owner-247','seed-union-247','YZ050110516','WHX123456',1,1,'鍗旦妤?,NULL,'13000009139',0,1,'2016-11-14 00:00:00','2026-04-20 16:14:58',0),(248,'seed-owner-248','seed-union-248','YZ050110611','ZRX123456',1,1,'鍛ㄨ嫢鐫?,NULL,'13000009176',0,1,'2011-11-12 00:00:00','2026-04-20 16:14:58',0),(249,'seed-owner-249','seed-union-249','YZ050110725','LAJ123456',1,1,'鍚曞畨鏉?,NULL,'13000009213',0,1,'2025-06-25 00:00:00','2026-04-20 16:14:58',0),(250,'seed-owner-250','seed-union-250','YZ050110826','HBH123456',1,1,'闊╁崥娑?,NULL,'13000009250',0,1,'2026-04-01 00:00:00','2026-04-20 16:14:58',0),(251,'seed-owner-251','seed-union-251','YZ050111115','QZC123456',1,1,'绉﹀瓙鏅?,NULL,'13000009287',0,1,'2015-04-28 00:00:00','2026-04-20 16:14:58',0),(252,'seed-owner-252','seed-union-252','YZ050111226','XJF123456',1,1,'瑜氬槈宄?,NULL,'13000009324',0,1,'2026-06-10 00:00:00','2026-04-20 16:14:58',0),(253,'seed-owner-253','seed-union-253','YZ050111309','LKN123456',1,1,'鏉庡彲瀹?,NULL,'13000009361',0,1,'2009-07-27 00:00:00','2026-04-20 16:14:58',0),(254,'seed-owner-254','seed-union-254','YZ050111514','SBC123456',1,1,'娌堝浆杈?,NULL,'13000009398',0,1,'2014-08-23 00:00:00','2026-04-20 16:14:58',0),(255,'seed-owner-255','seed-union-255','YZ050111723','ZSL123456',1,1,'寮犳€濈惓',NULL,'13000009435',0,1,'2023-12-29 00:00:00','2026-04-20 16:14:58',0),(256,'seed-owner-256','seed-union-256','YZ050111817','ZYX123456',1,1,'鏈变緷濠?,NULL,'13000009472',0,1,'2017-04-28 00:00:00','2026-04-20 16:14:58',0),(257,'seed-owner-257','seed-union-257','YZ050112018','SXX123456',1,1,'瀛欏濡?,NULL,'13000009509',0,1,'2018-12-05 00:00:00','2026-04-20 16:14:58',0),(258,'seed-owner-258','seed-union-258','YZ050112216','JWX123456',1,1,'钂嬫枃鐫?,NULL,'13000009546',0,1,'2016-01-14 00:00:00','2026-04-20 16:14:58',0),(259,'seed-owner-259','seed-union-259','YZ050112326','WLJ123456',1,1,'鍚存灄鏉?,NULL,'13000009583',0,1,'2026-08-04 00:00:00','2026-04-20 16:14:58',0),(260,'seed-owner-260','seed-union-260','YZ050112420','SSH123456',1,1,'鏂借瘲娑?,NULL,'13000009620',0,1,'2020-03-20 00:00:00','2026-04-20 16:14:58',0),(261,'seed-owner-261','seed-union-261','YZ050112715','QMC123456',1,1,'閽辨槑鏅?,NULL,'13000009657',0,1,'2015-05-26 00:00:00','2026-04-20 16:14:58',0),(262,'seed-owner-262','seed-union-262','YZ050112813','YCF123456',1,1,'灏ゆ櫒宄?,NULL,'13000009694',0,1,'2013-11-03 00:00:00','2026-04-20 16:14:58',0),(263,'seed-owner-263','seed-union-263','YZ050112925','WXN123456',1,1,'鍗瀹?,NULL,'13000009731',0,1,'2025-06-02 00:00:00','2026-04-20 16:14:58',0),(264,'seed-owner-264','seed-union-264','YZ050113021','ZYX123456',1,1,'鍛ㄦ偊杞?,NULL,'13000009768',0,1,'2021-10-27 00:00:00','2026-04-20 16:14:58',0),(265,'seed-owner-265','seed-union-265','YZ050113321','WAL123456',1,1,'鐜嬪畨鐞?,NULL,'13000009805',0,1,'2021-07-10 00:00:00','2026-04-20 16:14:58',0),(266,'seed-owner-266','seed-union-266','YZ050113525','QNR123456',1,1,'绉﹀畞鐒?,NULL,'13000009842',0,1,'2025-07-11 00:00:00','2026-04-20 16:14:58',0),(267,'seed-owner-267','seed-union-267','YZ050113618','XXX123456',1,1,'瑜氭濡?,NULL,'13000009879',0,1,'2018-05-09 00:00:00','2026-04-20 16:14:58',0),(268,'seed-owner-268','seed-union-268','YZ050113722','LZX123456',1,1,'鏉庡瓙妤?,NULL,'13000009916',0,1,'2022-12-21 00:00:00','2026-04-20 16:14:58',0),(269,'seed-owner-269','seed-union-269','YZ050113909','SKJ123456',1,1,'娌堝彲鏉?,NULL,'13000009953',0,1,'2009-08-14 00:00:00','2026-04-20 16:14:58',0),(270,'seed-owner-270','seed-union-270','YZ050114116','ZBH123456',1,1,'寮犲浆鑸?,NULL,'13000009990',0,1,'2016-11-20 00:00:00','2026-04-20 16:14:58',0),(271,'seed-owner-271','seed-union-271','YZ050114610','JXX123456',1,1,'钂嬪杞?,NULL,'13000010027',0,1,'2010-01-16 00:00:00','2026-04-20 16:14:58',0),(272,'seed-owner-272','seed-union-272','YZ050115011','FSX123456',1,1,'鍐瘲濠?,NULL,'13000010064',0,1,'2011-01-03 00:00:00','2026-04-20 16:14:58',0),(273,'seed-owner-273','seed-union-273','YZ050210116','QCR123456',1,1,'閽辫瘹鐒?,NULL,'13000010101',0,1,'2016-06-19 00:00:00','2026-04-20 16:14:58',0),(274,'seed-owner-274','seed-union-274','YZ050210212','YYX123456',1,1,'灏ゅ畤濡?,NULL,'13000010138',0,1,'2012-02-15 00:00:00','2026-04-20 16:14:58',0),(275,'seed-owner-275','seed-union-275','YZ050210521','LXJ123456',1,1,'鍚曞鏉?,NULL,'13000010175',0,1,'2021-04-14 00:00:00','2026-04-20 16:14:58',0),(276,'seed-owner-276','seed-union-276','YZ050210611','HYH123456',1,1,'闊╂偊娑?,NULL,'13000010212',0,1,'2011-03-17 00:00:00','2026-04-20 16:14:58',0),(277,'seed-owner-277','seed-union-277','YZ050210823','ZRX123456',1,1,'璧佃嫢鐞?,NULL,'13000010249',0,1,'2023-09-02 00:00:00','2026-04-20 16:14:58',0),(278,'seed-owner-278','seed-union-278','YZ050210909','QAC123456',1,1,'绉﹀畨鏅?,NULL,'13000010286',0,1,'2009-06-10 00:00:00','2026-04-20 16:14:58',0),(279,'seed-owner-279','seed-union-279','YZ050211026','XBF123456',1,1,'瑜氬崥宄?,NULL,'13000010323',0,1,'2026-02-17 00:00:00','2026-04-20 16:14:58',0),(280,'seed-owner-280','seed-union-280','YZ050211416','ZJX123456',1,1,'閮戝槈钀?,NULL,'13000010360',0,1,'2016-02-05 00:00:00','2026-04-20 16:14:58',0),(281,'seed-owner-281','seed-union-281','YZ050211521','ZKL123456',1,1,'寮犲彲鐞?,NULL,'13000010397',0,1,'2021-05-31 00:00:00','2026-04-20 16:14:58',0),(282,'seed-owner-282','seed-union-282','YZ050211618','ZCX123456',1,1,'鏈辫景濠?,NULL,'13000010434',0,1,'2018-06-28 00:00:00','2026-04-20 16:14:58',0),(283,'seed-owner-283','seed-union-283','YZ050211718','CBR123456',1,1,'闄堝浆鐒?,NULL,'13000010471',0,1,'2018-11-05 00:00:00','2026-04-20 16:14:58',0),(284,'seed-owner-284','seed-union-284','YZ050212022','JYX123456',1,1,'钂嬩緷鐫?,NULL,'13000010508',0,1,'2022-12-29 00:00:00','2026-04-20 16:14:58',0),(285,'seed-owner-285','seed-union-285','YZ050212423','FWX123456',1,1,'鍐枃鐞?,NULL,'13000010545',0,1,'2023-02-28 00:00:00','2026-04-20 16:14:58',0),(286,'seed-owner-286','seed-union-286','YZ050212510','QLC123456',1,1,'閽辨灄鏅?,NULL,'13000010582',0,1,'2010-04-08 00:00:00','2026-04-20 16:14:58',0),(287,'seed-owner-287','seed-union-287','YZ050212709','WCN123456',1,1,'鍗瘹瀹?,NULL,'13000010619',0,1,'2009-06-21 00:00:00','2026-04-20 16:14:58',0),(288,'seed-owner-288','seed-union-288','YZ050212921','LMC123456',1,1,'鍚曟槑杈?,NULL,'13000010656',0,1,'2021-07-21 00:00:00','2026-04-20 16:14:58',0),(289,'seed-owner-289','seed-union-289','YZ050213010','HCX123456',1,1,'闊╂櫒钀?,NULL,'13000010693',0,1,'2010-10-24 00:00:00','2026-04-20 16:14:58',0),(290,'seed-owner-290','seed-union-290','YZ050213113','WXL123456',1,1,'鐜嬪鐞?,NULL,'13000010730',0,1,'2013-01-26 00:00:00','2026-04-20 16:14:58',0),(291,'seed-owner-291','seed-union-291','YZ050213320','QHR123456',1,1,'绉︽旦鐒?,NULL,'13000010767',0,1,'2020-03-29 00:00:00','2026-04-20 16:14:58',0),(292,'seed-owner-292','seed-union-292','YZ050213425','XRX123456',1,1,'瑜氳嫢濡?,NULL,'13000010804',0,1,'2025-08-07 00:00:00','2026-04-20 16:14:58',0),(293,'seed-owner-293','seed-union-293','YZ050213613','HBX123456',1,1,'浣曞崥鐫?,NULL,'13000010841',0,1,'2013-11-10 00:00:00','2026-04-20 16:14:58',0),(294,'seed-owner-294','seed-union-294','YZ050213725','SNJ123456',1,1,'娌堝畞鏉?,NULL,'13000010878',0,1,'2025-02-22 00:00:00','2026-04-20 16:14:58',0),(295,'seed-owner-295','seed-union-295','YZ050213817','ZXH123456',1,1,'閮戞娑?,NULL,'13000010915',0,1,'2017-04-28 00:00:00','2026-04-20 16:14:58',0),(296,'seed-owner-296','seed-union-296','YZ050214026','ZJX123456',1,1,'鏈卞槈鐞?,NULL,'13000010952',0,1,'2026-08-16 00:00:00','2026-04-20 16:14:58',0),(297,'seed-owner-297','seed-union-297','YZ050214119','CKC123456',1,1,'闄堝彲鏅?,NULL,'13000010989',0,1,'2019-07-11 00:00:00','2026-04-20 16:14:58',0),(298,'seed-owner-298','seed-union-298','YZ050214215','SCF123456',1,1,'瀛欒景宄?,NULL,'13000011026',0,1,'2015-02-05 00:00:00','2026-04-20 16:14:58',0),(299,'seed-owner-299','seed-union-299','YZ050214314','XBN123456',1,1,'璁稿浆瀹?,NULL,'13000011063',0,1,'2014-07-19 00:00:00','2026-04-20 16:14:58',0),(300,'seed-owner-300','seed-union-300','YZ050214418','JYX123456',1,1,'钂嬮洦杞?,NULL,'13000011100',0,1,'2018-01-03 00:00:00','2026-04-20 16:14:58',0),(301,'seed-owner-301','seed-union-301','YZ050214521','WSC123456',1,1,'鍚存€濊景',NULL,'13000011137',0,1,'2021-09-23 00:00:00','2026-04-20 16:14:58',0),(302,'seed-owner-302','seed-union-302','YZ050214610','SYX123456',1,1,'鏂戒緷钀?,NULL,'13000011174',0,1,'2010-01-30 00:00:00','2026-04-20 16:14:58',0),(303,'seed-owner-303','seed-union-303','YZ050214721','YYL123456',1,1,'鏉ㄥ溅鐞?,NULL,'13000011211',0,1,'2021-10-24 00:00:00','2026-04-20 16:14:58',0),(304,'seed-owner-304','seed-union-304','YZ050214814','FXX123456',1,1,'鍐濠?,NULL,'13000011248',0,1,'2014-10-29 00:00:00','2026-04-20 16:14:58',0),(305,'seed-owner-305','seed-union-305','YZ060110323','LCJ123456',1,1,'鍚曡瘹鏉?,NULL,'13000011285',0,1,'2023-09-25 00:00:00','2026-04-20 16:14:58',0),(306,'seed-owner-306','seed-union-306','YZ060110423','HYH123456',1,1,'闊╁畤娑?,NULL,'13000011322',0,1,'2023-02-25 00:00:00','2026-04-20 16:14:58',0),(307,'seed-owner-307','seed-union-307','YZ060110523','WMH123456',1,1,'鐜嬫槑鑸?,NULL,'13000011359',0,1,'2023-05-19 00:00:00','2026-04-20 16:14:58',0),(308,'seed-owner-308','seed-union-308','YZ060110719','QXC123456',1,1,'绉﹀鏅?,NULL,'13000011396',0,1,'2019-10-14 00:00:00','2026-04-20 16:14:58',0),(309,'seed-owner-309','seed-union-309','YZ060110820','XYF123456',1,1,'瑜氭偊宄?,NULL,'13000011433',0,1,'2020-09-16 00:00:00','2026-04-20 16:14:58',0),(310,'seed-owner-310','seed-union-310','YZ060111113','SAC123456',1,1,'娌堝畨杈?,NULL,'13000011470',0,1,'2013-11-06 00:00:00','2026-04-20 16:14:58',0),(311,'seed-owner-311','seed-union-311','YZ060111224','ZBX123456',1,1,'閮戝崥钀?,NULL,'13000011507',0,1,'2024-06-30 00:00:00','2026-04-20 16:14:58',0),(312,'seed-owner-312','seed-union-312','YZ060111319','ZNL123456',1,1,'寮犲畞鐞?,NULL,'13000011544',0,1,'2019-10-02 00:00:00','2026-04-20 16:14:58',0),(313,'seed-owner-313','seed-union-313','YZ060111523','CZR123456',1,1,'闄堝瓙鐒?,NULL,'13000011581',0,1,'2023-10-19 00:00:00','2026-04-20 16:14:58',0),(314,'seed-owner-314','seed-union-314','YZ060111614','SJX123456',1,1,'瀛欏槈濡?,NULL,'13000011618',0,1,'2014-01-03 00:00:00','2026-04-20 16:14:58',0),(315,'seed-owner-315','seed-union-315','YZ060112123','YSH123456',1,1,'鏉ㄦ€濊埅',NULL,'13000011655',0,1,'2023-01-22 00:00:00','2026-04-20 16:14:58',0),(316,'seed-owner-316','seed-union-316','YZ060112218','FYX123456',1,1,'鍐緷鐞?,NULL,'13000011692',0,1,'2018-09-03 00:00:00','2026-04-20 16:14:58',0),(317,'seed-owner-317','seed-union-317','YZ060112516','WXN123456',1,1,'鍗瀹?,NULL,'13000011729',0,1,'2016-07-02 00:00:00','2026-04-20 16:14:58',0),(318,'seed-owner-318','seed-union-318','YZ060112820','HSX123456',1,1,'闊╄瘲钀?,NULL,'13000011766',0,1,'2020-03-13 00:00:00','2026-04-20 16:14:58',0),(319,'seed-owner-319','seed-union-319','YZ060113112','QMR123456',1,1,'绉︽槑鐒?,NULL,'13000011803',0,1,'2012-05-19 00:00:00','2026-04-20 16:14:58',0),(320,'seed-owner-320','seed-union-320','YZ060113209','XCX123456',1,1,'瑜氭櫒濡?,NULL,'13000011840',0,1,'2009-09-04 00:00:00','2026-04-20 16:14:58',0),(321,'seed-owner-321','seed-union-321','YZ060113325','LXX123456',1,1,'鏉庡妤?,NULL,'13000011877',0,1,'2025-09-20 00:00:00','2026-04-20 16:14:58',0),(322,'seed-owner-322','seed-union-322','YZ060113414','HYX123456',1,1,'浣曟偊鐫?,NULL,'13000011914',0,1,'2014-05-01 00:00:00','2026-04-20 16:14:58',0),(323,'seed-owner-323','seed-union-323','YZ060113624','ZRH123456',1,1,'閮戣嫢娑?,NULL,'13000011951',0,1,'2024-11-30 00:00:00','2026-04-20 16:14:58',0),(324,'seed-owner-324','seed-union-324','YZ060113916','CNC123456',1,1,'闄堝畞鏅?,NULL,'13000011988',0,1,'2016-02-11 00:00:00','2026-04-20 16:14:58',0),(325,'seed-owner-325','seed-union-325','YZ060114120','XZN123456',1,1,'璁稿瓙瀹?,NULL,'13000012025',0,1,'2020-01-24 00:00:00','2026-04-20 16:14:58',0),(326,'seed-owner-326','seed-union-326','YZ060114215','JJX123456',1,1,'钂嬪槈杞?,NULL,'13000012062',0,1,'2015-12-22 00:00:00','2026-04-20 16:14:58',0),(327,'seed-owner-327','seed-union-327','YZ060114516','YBL123456',1,1,'鏉ㄥ浆鐞?,NULL,'13000012099',0,1,'2016-01-30 00:00:00','2026-04-20 16:14:58',0),(328,'seed-owner-328','seed-union-328','YZ060114610','FYX123456',1,1,'鍐洦濠?,NULL,'13000012136',0,1,'2010-05-15 00:00:00','2026-04-20 16:14:58',0),(329,'seed-owner-329','seed-union-329','YZ060114717','QSR123456',1,1,'閽辨€濈劧',NULL,'13000012173',0,1,'2017-03-12 00:00:00','2026-04-20 16:14:58',0),(330,'seed-owner-330','seed-union-330','YZ060115014','ZXX123456',1,1,'鍛ㄥ鐫?,NULL,'13000012210',0,1,'2014-09-06 00:00:00','2026-04-20 16:14:58',0),(331,'seed-owner-331','seed-union-331','YZ060210322','WLH123456',1,1,'鐜嬫灄鑸?,NULL,'13000012247',0,1,'2022-07-31 00:00:00','2026-04-20 16:14:58',0),(332,'seed-owner-332','seed-union-332','YZ060210522','QCC123456',1,1,'绉﹁瘹鏅?,NULL,'13000012284',0,1,'2022-12-04 00:00:00','2026-04-20 16:14:58',0),(333,'seed-owner-333','seed-union-333','YZ060210625','XYF123456',1,1,'瑜氬畤宄?,NULL,'13000012321',0,1,'2025-01-06 00:00:00','2026-04-20 16:14:58',0),(334,'seed-owner-334','seed-union-334','YZ060210714','LMN123456',1,1,'鏉庢槑瀹?,NULL,'13000012358',0,1,'2014-04-13 00:00:00','2026-04-20 16:14:58',0),(335,'seed-owner-335','seed-union-335','YZ060211119','ZHL123456',1,1,'寮犳旦鐞?,NULL,'13000012395',0,1,'2019-08-21 00:00:00','2026-04-20 16:14:58',0),(336,'seed-owner-336','seed-union-336','YZ060211423','SBX123456',1,1,'瀛欏崥濡?,NULL,'13000012432',0,1,'2023-09-14 00:00:00','2026-04-20 16:14:58',0),(337,'seed-owner-337','seed-union-337','YZ060211619','JXX123456',1,1,'钂嬫鐫?,NULL,'13000012469',0,1,'2019-10-20 00:00:00','2026-04-20 16:14:58',0),(338,'seed-owner-338','seed-union-338','YZ060211725','WZJ123456',1,1,'鍚村瓙鏉?,NULL,'13000012506',0,1,'2025-11-22 00:00:00','2026-04-20 16:14:58',0),(339,'seed-owner-339','seed-union-339','YZ060212013','FCX123456',1,1,'鍐景鐞?,NULL,'13000012543',0,1,'2013-12-05 00:00:00','2026-04-20 16:14:58',0),(340,'seed-owner-340','seed-union-340','YZ060212121','QBC123456',1,1,'閽卞浆鏅?,NULL,'13000012580',0,1,'2021-06-06 00:00:00','2026-04-20 16:14:58',0),(341,'seed-owner-341','seed-union-341','YZ060212224','YYF123456',1,1,'灏ら洦宄?,NULL,'13000012617',0,1,'2024-05-07 00:00:00','2026-04-20 16:14:58',0),(342,'seed-owner-342','seed-union-342','YZ060212319','WSN123456',1,1,'鍗€濆畞',NULL,'13000012654',0,1,'2019-04-16 00:00:00','2026-04-20 16:14:58',0),(343,'seed-owner-343','seed-union-343','YZ060212909','QLR123456',1,1,'绉︽灄鐒?,NULL,'13000012691',0,1,'2009-11-23 00:00:00','2026-04-20 16:14:58',0),(344,'seed-owner-344','seed-union-344','YZ060213011','XSX123456',1,1,'瑜氳瘲濡?,NULL,'13000012728',0,1,'2011-09-20 00:00:00','2026-04-20 16:14:58',0),(345,'seed-owner-345','seed-union-345','YZ060213122','LCX123456',1,1,'鏉庤瘹妤?,NULL,'13000012765',0,1,'2022-03-29 00:00:00','2026-04-20 16:14:58',0),(346,'seed-owner-346','seed-union-346','YZ060213318','SMJ123456',1,1,'娌堟槑鏉?,NULL,'13000012802',0,1,'2018-06-06 00:00:00','2026-04-20 16:14:58',0),(347,'seed-owner-347','seed-union-347','YZ060213412','ZCH123456',1,1,'閮戞櫒娑?,NULL,'13000012839',0,1,'2012-06-02 00:00:00','2026-04-20 16:14:58',0),(348,'seed-owner-348','seed-union-348','YZ060213620','ZYX123456',1,1,'鏈辨偊鐞?,NULL,'13000012876',0,1,'2020-07-06 00:00:00','2026-04-20 16:14:58',0),(349,'seed-owner-349','seed-union-349','YZ060213725','CHC123456',1,1,'闄堟旦鏅?,NULL,'13000012913',0,1,'2025-01-01 00:00:00','2026-04-20 16:14:58',0),(350,'seed-owner-350','seed-union-350','YZ060213920','XAN123456',1,1,'璁稿畨瀹?,NULL,'13000012950',0,1,'2020-07-11 00:00:00','2026-04-20 16:14:58',0),(351,'seed-owner-351','seed-union-351','YZ060214220','SXX123456',1,1,'鏂芥钀?,NULL,'13000012987',0,1,'2020-12-10 00:00:00','2026-04-20 16:14:58',0),(352,'seed-owner-352','seed-union-352','YZ060214525','QKR123456',1,1,'閽卞彲鐒?,NULL,'13000013024',0,1,'2025-04-19 00:00:00','2026-04-20 16:14:58',0),(353,'seed-owner-353','seed-union-353','YZ060214617','YCX123456',1,1,'灏よ景濡?,NULL,'13000013061',0,1,'2017-11-20 00:00:00','2026-04-20 16:14:58',0),(354,'seed-owner-354','seed-union-354','YZ060214709','WBX123456',1,1,'鍗浆妤?,NULL,'13000013098',0,1,'2009-11-11 00:00:00','2026-04-20 16:14:58',0),(355,'seed-owner-355','seed-union-355','YZ060214818','ZYX123456',1,1,'鍛ㄩ洦鐫?,NULL,'13000013135',0,1,'2018-01-04 00:00:00','2026-04-20 16:14:58',0),(356,'seed-owner-356','seed-union-356','YZ060214922','LSJ123456',1,1,'鍚曟€濇澃',NULL,'13000013172',0,1,'2022-07-26 00:00:00','2026-04-20 16:14:58',0),(357,'seed-owner-357','seed-union-357','YZ060215023','HYH123456',1,1,'闊╀緷娑?,NULL,'13000013209',0,1,'2023-01-28 00:00:00','2026-04-20 16:14:58',0),(358,'seed-owner-358','seed-union-358','YZ070110214','ZXX123456',1,1,'璧靛鐞?,NULL,'13000013246',0,1,'2014-05-14 00:00:00','2026-04-20 16:14:58',0),(359,'seed-owner-359','seed-union-359','YZ070110419','XWF123456',1,1,'瑜氭枃宄?,NULL,'13000013283',0,1,'2019-09-30 00:00:00','2026-04-20 16:14:58',0),(360,'seed-owner-360','seed-union-360','YZ070110516','LLN123456',1,1,'鏉庢灄瀹?,NULL,'13000013320',0,1,'2016-11-28 00:00:00','2026-04-20 16:14:58',0),(361,'seed-owner-361','seed-union-361','YZ070110713','SCC123456',1,1,'娌堣瘹杈?,NULL,'13000013357',0,1,'2013-08-31 00:00:00','2026-04-20 16:14:58',0),(362,'seed-owner-362','seed-union-362','YZ070110913','ZML123456',1,1,'寮犳槑鐞?,NULL,'13000013394',0,1,'2013-05-17 00:00:00','2026-04-20 16:14:58',0),(363,'seed-owner-363','seed-union-363','YZ070111009','ZCX123456',1,1,'鏈辨櫒濠?,NULL,'13000013431',0,1,'2009-06-03 00:00:00','2026-04-20 16:14:58',0),(364,'seed-owner-364','seed-union-364','YZ070111114','CXR123456',1,1,'闄堝鐒?,NULL,'13000013468',0,1,'2014-08-14 00:00:00','2026-04-20 16:14:58',0),(365,'seed-owner-365','seed-union-365','YZ070111320','XHX123456',1,1,'璁告旦妤?,NULL,'13000013505',0,1,'2020-04-15 00:00:00','2026-04-20 16:14:58',0),(366,'seed-owner-366','seed-union-366','YZ070111726','YNH123456',1,1,'鏉ㄥ畞鑸?,NULL,'13000013542',0,1,'2026-12-26 00:00:00','2026-04-20 16:14:58',0),(367,'seed-owner-367','seed-union-367','YZ070111820','FXX123456',1,1,'鍐鐞?,NULL,'13000013579',0,1,'2020-06-30 00:00:00','2026-04-20 16:14:58',0),(368,'seed-owner-368','seed-union-368','YZ070111910','QZC123456',1,1,'閽卞瓙鏅?,NULL,'13000013616',0,1,'2010-12-09 00:00:00','2026-04-20 16:14:58',0),(369,'seed-owner-369','seed-union-369','YZ070112015','YJF123456',1,1,'灏ゅ槈宄?,NULL,'13000013653',0,1,'2015-08-06 00:00:00','2026-04-20 16:14:58',0),(370,'seed-owner-370','seed-union-370','YZ070112116','WKN123456',1,1,'鍗彲瀹?,NULL,'13000013690',0,1,'2016-04-02 00:00:00','2026-04-20 16:14:58',0),(371,'seed-owner-371','seed-union-371','YZ070112215','ZCX123456',1,1,'鍛ㄨ景杞?,NULL,'13000013727',0,1,'2015-02-15 00:00:00','2026-04-20 16:14:58',0),(372,'seed-owner-372','seed-union-372','YZ070112315','LBC123456',1,1,'鍚曞浆杈?,NULL,'13000013764',0,1,'2015-01-19 00:00:00','2026-04-20 16:14:58',0),(373,'seed-owner-373','seed-union-373','YZ070112421','HYX123456',1,1,'闊╅洦钀?,NULL,'13000013801',0,1,'2021-07-26 00:00:00','2026-04-20 16:14:58',0),(374,'seed-owner-374','seed-union-374','YZ070112522','WSL123456',1,1,'鐜嬫€濈惓',NULL,'13000013838',0,1,'2022-11-22 00:00:00','2026-04-20 16:14:58',0),(375,'seed-owner-375','seed-union-375','YZ070112718','QYR123456',1,1,'绉﹀溅鐒?,NULL,'13000013875',0,1,'2018-11-22 00:00:00','2026-04-20 16:14:58',0),(376,'seed-owner-376','seed-union-376','YZ070112824','XXX123456',1,1,'瑜氬濡?,NULL,'13000013912',0,1,'2024-08-02 00:00:00','2026-04-20 16:14:58',0),(377,'seed-owner-377','seed-union-377','YZ070113022','HWX123456',1,1,'浣曟枃鐫?,NULL,'13000013949',0,1,'2022-07-22 00:00:00','2026-04-20 16:14:58',0),(378,'seed-owner-378','seed-union-378','YZ070113220','ZSH123456',1,1,'閮戣瘲娑?,NULL,'13000013986',0,1,'2020-01-11 00:00:00','2026-04-20 16:14:58',0),(379,'seed-owner-379','seed-union-379','YZ070113319','ZCH123456',1,1,'寮犺瘹鑸?,NULL,'13000014023',0,1,'2019-02-25 00:00:00','2026-04-20 16:14:58',0),(380,'seed-owner-380','seed-union-380','YZ070113419','ZYX123456',1,1,'鏈卞畤鐞?,NULL,'13000014060',0,1,'2019-04-28 00:00:00','2026-04-20 16:14:58',0),(381,'seed-owner-381','seed-union-381','YZ070113610','SCF123456',1,1,'瀛欐櫒宄?,NULL,'13000014097',0,1,'2010-04-22 00:00:00','2026-04-20 16:14:58',0),(382,'seed-owner-382','seed-union-382','YZ070113912','WHC123456',1,1,'鍚存旦杈?,NULL,'13000014134',0,1,'2012-07-03 00:00:00','2026-04-20 16:14:58',0),(383,'seed-owner-383','seed-union-383','YZ070114117','YAL123456',1,1,'鏉ㄥ畨鐞?,NULL,'13000014171',0,1,'2017-09-17 00:00:00','2026-04-20 16:14:58',0),(384,'seed-owner-384','seed-union-384','YZ070114224','FBX123456',1,1,'鍐崥濠?,NULL,'13000014208',0,1,'2024-05-06 00:00:00','2026-04-20 16:14:58',0),(385,'seed-owner-385','seed-union-385','YZ070114311','QNR123456',1,1,'閽卞畞鐒?,NULL,'13000014245',0,1,'2011-06-04 00:00:00','2026-04-20 16:14:58',0),(386,'seed-owner-386','seed-union-386','YZ070114422','YXX123456',1,1,'灏ゆ濡?,NULL,'13000014282',0,1,'2022-05-06 00:00:00','2026-04-20 16:14:58',0),(387,'seed-owner-387','seed-union-387','YZ070114521','WZX123456',1,1,'鍗瓙妤?,NULL,'13000014319',0,1,'2021-02-26 00:00:00','2026-04-20 16:14:58',0),(388,'seed-owner-388','seed-union-388','YZ070114609','ZJX123456',1,1,'鍛ㄥ槈鐫?,NULL,'13000014356',0,1,'2009-02-13 00:00:00','2026-04-20 16:14:58',0),(389,'seed-owner-389','seed-union-389','YZ070114714','LKJ123456',1,1,'鍚曞彲鏉?,NULL,'13000014393',0,1,'2014-06-06 00:00:00','2026-04-20 16:14:58',0),(390,'seed-owner-390','seed-union-390','YZ070114920','WBH123456',1,1,'鐜嬪浆鑸?,NULL,'13000014430',0,1,'2020-08-21 00:00:00','2026-04-20 16:14:58',0),(391,'seed-owner-391','seed-union-391','YZ070210113','QSC123456',1,1,'绉︽€濇櫒',NULL,'13000014467',0,1,'2013-01-02 00:00:00','2026-04-20 16:14:58',0),(392,'seed-owner-392','seed-union-392','YZ070210224','XYF123456',1,1,'瑜氫緷宄?,NULL,'13000014504',0,1,'2024-04-29 00:00:00','2026-04-20 16:14:58',0),(393,'seed-owner-393','seed-union-393','YZ070210311','LYN123456',1,1,'鏉庡溅瀹?,NULL,'13000014541',0,1,'2011-02-05 00:00:00','2026-04-20 16:14:58',0),(394,'seed-owner-394','seed-union-394','YZ070210509','SXC123456',1,1,'娌堟杈?,NULL,'13000014578',0,1,'2009-06-18 00:00:00','2026-04-20 16:14:58',0),(395,'seed-owner-395','seed-union-395','YZ070210612','ZWX123456',1,1,'閮戞枃钀?,NULL,'13000014615',0,1,'2012-12-06 00:00:00','2026-04-20 16:14:58',0),(396,'seed-owner-396','seed-union-396','YZ070210723','ZLL123456',1,1,'寮犳灄鐞?,NULL,'13000014652',0,1,'2023-01-30 00:00:00','2026-04-20 16:14:58',0),(397,'seed-owner-397','seed-union-397','YZ070210821','ZSX123456',1,1,'鏈辫瘲濠?,NULL,'13000014689',0,1,'2021-04-17 00:00:00','2026-04-20 16:14:58',0),(398,'seed-owner-398','seed-union-398','YZ070210918','CCR123456',1,1,'闄堣瘹鐒?,NULL,'13000014726',0,1,'2018-03-09 00:00:00','2026-04-20 16:14:58',0),(399,'seed-owner-399','seed-union-399','YZ070211015','SYX123456',1,1,'瀛欏畤濡?,NULL,'13000014763',0,1,'2015-11-08 00:00:00','2026-04-20 16:14:58',0),(400,'seed-owner-400','seed-union-400','YZ070211422','SYH123456',1,1,'鏂芥偊娑?,NULL,'13000014800',0,1,'2022-06-01 00:00:00','2026-04-20 16:14:58',0),(401,'seed-owner-401','seed-union-401','YZ070211812','YBF123456',1,1,'灏ゅ崥宄?,NULL,'13000014837',0,1,'2012-07-16 00:00:00','2026-04-20 16:14:58',0),(402,'seed-owner-402','seed-union-402','YZ070212519','QBR123456',1,1,'绉﹀浆鐒?,NULL,'13000014874',0,1,'2019-05-28 00:00:00','2026-04-20 16:14:58',0),(403,'seed-owner-403','seed-union-403','YZ070212722','LSX123456',1,1,'鏉庢€濇',NULL,'13000014911',0,1,'2022-02-24 00:00:00','2026-04-20 16:14:58',0),(404,'seed-owner-404','seed-union-404','YZ070212824','HYX123456',1,1,'浣曚緷鐫?,NULL,'13000014948',0,1,'2024-01-16 00:00:00','2026-04-20 16:14:58',0),(405,'seed-owner-405','seed-union-405','YZ070212911','SYJ123456',1,1,'娌堝溅鏉?,NULL,'13000014985',0,1,'2011-04-30 00:00:00','2026-04-20 16:14:58',0),(406,'seed-owner-406','seed-union-406','YZ070213022','ZXH123456',1,1,'閮戝娑?,NULL,'13000015022',0,1,'2022-11-27 00:00:00','2026-04-20 16:14:58',0),(407,'seed-owner-407','seed-union-407','YZ070213114','ZXH123456',1,1,'寮犳鑸?,NULL,'13000015059',0,1,'2014-07-19 00:00:00','2026-04-20 16:14:58',0),(408,'seed-owner-408','seed-union-408','YZ070213326','CLC123456',1,1,'闄堟灄鏅?,NULL,'13000015096',0,1,'2026-05-08 00:00:00','2026-04-20 16:14:58',0),(409,'seed-owner-409','seed-union-409','YZ070213525','XCN123456',1,1,'璁歌瘹瀹?,NULL,'13000015133',0,1,'2025-02-05 00:00:00','2026-04-20 16:14:58',0),(410,'seed-owner-410','seed-union-410','YZ070213909','YXL123456',1,1,'鏉ㄥ鐞?,NULL,'13000015170',0,1,'2009-02-01 00:00:00','2026-04-20 16:14:58',0),(411,'seed-owner-411','seed-union-411','YZ070214011','FYX123456',1,1,'鍐偊濠?,NULL,'13000015207',0,1,'2011-02-15 00:00:00','2026-04-20 16:14:58',0),(412,'seed-owner-412','seed-union-412','YZ070214114','QHR123456',1,1,'閽辨旦鐒?,NULL,'13000015244',0,1,'2014-06-29 00:00:00','2026-04-20 16:14:58',0),(413,'seed-owner-413','seed-union-413','YZ070214213','YRX123456',1,1,'灏よ嫢濡?,NULL,'13000015281',0,1,'2013-01-04 00:00:00','2026-04-20 16:14:58',0),(414,'seed-owner-414','seed-union-414','YZ070214319','WAX123456',1,1,'鍗畨妤?,NULL,'13000015318',0,1,'2019-08-07 00:00:00','2026-04-20 16:14:58',0),(415,'seed-owner-415','seed-union-415','YZ070214410','ZBX123456',1,1,'鍛ㄥ崥鐫?,NULL,'13000015355',0,1,'2010-07-10 00:00:00','2026-04-20 16:14:58',0),(416,'seed-owner-416','seed-union-416','YZ070214610','HXH123456',1,1,'闊╂娑?,NULL,'13000015392',0,1,'2010-04-03 00:00:00','2026-04-20 16:14:58',0),(417,'seed-owner-417','seed-union-417','YZ070214711','WZH123456',1,1,'鐜嬪瓙鑸?,NULL,'13000015429',0,1,'2011-11-08 00:00:00','2026-04-20 16:14:58',0),(418,'seed-owner-418','seed-union-418','YZ070214823','ZJX123456',1,1,'璧靛槈鐞?,NULL,'13000015466',0,1,'2023-04-01 00:00:00','2026-04-20 16:14:58',0),(419,'seed-owner-419','seed-union-419','YZ080110219','HYX123456',1,1,'浣曢洦杞?,NULL,'13000015503',0,1,'2019-05-31 00:00:00','2026-04-20 16:14:58',0),(420,'seed-owner-420','seed-union-420','YZ080110414','ZYX123456',1,1,'閮戜緷钀?,NULL,'13000015540',0,1,'2014-02-04 00:00:00','2026-04-20 16:14:58',0),(421,'seed-owner-421','seed-union-421','YZ080110614','ZXX123456',1,1,'鏈卞濠?,NULL,'13000015577',0,1,'2014-05-14 00:00:00','2026-04-20 16:14:58',0),(422,'seed-owner-422','seed-union-422','YZ080110811','SWX123456',1,1,'瀛欐枃濡?,NULL,'13000015614',0,1,'2011-08-31 00:00:00','2026-04-20 16:14:58',0),(423,'seed-owner-423','seed-union-423','YZ080110916','XLX123456',1,1,'璁告灄妤?,NULL,'13000015651',0,1,'2016-05-25 00:00:00','2026-04-20 16:14:58',0),(424,'seed-owner-424','seed-union-424','YZ080111024','JSX123456',1,1,'钂嬭瘲鐫?,NULL,'13000015688',0,1,'2024-09-28 00:00:00','2026-04-20 16:14:58',0),(425,'seed-owner-425','seed-union-425','YZ080111124','WCJ123456',1,1,'鍚磋瘹鏉?,NULL,'13000015725',0,1,'2024-09-20 00:00:00','2026-04-20 16:14:58',0),(426,'seed-owner-426','seed-union-426','YZ080111213','SYH123456',1,1,'鏂藉畤娑?,NULL,'13000015762',0,1,'2013-07-16 00:00:00','2026-04-20 16:14:58',0),(427,'seed-owner-427','seed-union-427','YZ080111319','YMH123456',1,1,'鏉ㄦ槑鑸?,NULL,'13000015799',0,1,'2019-08-25 00:00:00','2026-04-20 16:14:58',0),(428,'seed-owner-428','seed-union-428','YZ080111418','FCX123456',1,1,'鍐櫒鐞?,NULL,'13000015836',0,1,'2018-02-16 00:00:00','2026-04-20 16:14:58',0),(429,'seed-owner-429','seed-union-429','YZ080111620','YYF123456',1,1,'灏ゆ偊宄?,NULL,'13000015873',0,1,'2020-01-27 00:00:00','2026-04-20 16:14:58',0),(430,'seed-owner-430','seed-union-430','YZ080111713','WHN123456',1,1,'鍗旦瀹?,NULL,'13000015910',0,1,'2013-05-08 00:00:00','2026-04-20 16:14:58',0),(431,'seed-owner-431','seed-union-431','YZ080111912','LAC123456',1,1,'鍚曞畨杈?,NULL,'13000015947',0,1,'2012-09-03 00:00:00','2026-04-20 16:14:58',0),(432,'seed-owner-432','seed-union-432','YZ080112210','ZXX123456',1,1,'璧垫濠?,NULL,'13000015984',0,1,'2010-08-10 00:00:00','2026-04-20 16:14:58',0),(433,'seed-owner-433','seed-union-433','YZ080112516','LKX123456',1,1,'鏉庡彲妤?,NULL,'13000016021',0,1,'2016-10-14 00:00:00','2026-04-20 16:14:58',0),(434,'seed-owner-434','seed-union-434','YZ080112626','HCX123456',1,1,'浣曡景鐫?,NULL,'13000016058',0,1,'2026-10-23 00:00:00','2026-04-20 16:14:58',0),(435,'seed-owner-435','seed-union-435','YZ080112816','ZYH123456',1,1,'閮戦洦娑?,NULL,'13000016095',0,1,'2016-11-10 00:00:00','2026-04-20 16:14:58',0),(436,'seed-owner-436','seed-union-436','YZ080112910','ZSH123456',1,1,'寮犳€濊埅',NULL,'13000016132',0,1,'2010-01-30 00:00:00','2026-04-20 16:14:58',0),(437,'seed-owner-437','seed-union-437','YZ080113009','ZYX123456',1,1,'鏈变緷鐞?,NULL,'13000016169',0,1,'2009-01-31 00:00:00','2026-04-20 16:14:58',0),(438,'seed-owner-438','seed-union-438','YZ080113222','SXF123456',1,1,'瀛欏宄?,NULL,'13000016206',0,1,'2022-01-14 00:00:00','2026-04-20 16:14:58',0),(439,'seed-owner-439','seed-union-439','YZ080113322','XXN123456',1,1,'璁告瀹?,NULL,'13000016243',0,1,'2022-06-26 00:00:00','2026-04-20 16:14:58',0),(440,'seed-owner-440','seed-union-440','YZ080113415','JWX123456',1,1,'钂嬫枃杞?,NULL,'13000016280',0,1,'2015-09-24 00:00:00','2026-04-20 16:14:58',0),(441,'seed-owner-441','seed-union-441','YZ080113521','WLC123456',1,1,'鍚存灄杈?,NULL,'13000016317',0,1,'2021-04-08 00:00:00','2026-04-20 16:14:58',0),(442,'seed-owner-442','seed-union-442','YZ080113616','SSX123456',1,1,'鏂借瘲钀?,NULL,'13000016354',0,1,'2016-11-03 00:00:00','2026-04-20 16:14:58',0),(443,'seed-owner-443','seed-union-443','YZ080114025','YCX123456',1,1,'灏ゆ櫒濡?,NULL,'13000016391',0,1,'2025-09-10 00:00:00','2026-04-20 16:14:58',0),(444,'seed-owner-444','seed-union-444','YZ080114117','WXX123456',1,1,'鍗妤?,NULL,'13000016428',0,1,'2017-10-31 00:00:00','2026-04-20 16:14:58',0),(445,'seed-owner-445','seed-union-445','YZ080114325','LHJ123456',1,1,'鍚曟旦鏉?,NULL,'13000016465',0,1,'2025-07-27 00:00:00','2026-04-20 16:14:58',0),(446,'seed-owner-446','seed-union-446','YZ080114520','WAH123456',1,1,'鐜嬪畨鑸?,NULL,'13000016502',0,1,'2020-05-22 00:00:00','2026-04-20 16:14:58',0),(447,'seed-owner-447','seed-union-447','YZ080114721','QNC123456',1,1,'绉﹀畞鏅?,NULL,'13000016539',0,1,'2021-01-07 00:00:00','2026-04-20 16:14:58',0),(448,'seed-owner-448','seed-union-448','YZ080114921','LZN123456',1,1,'鏉庡瓙瀹?,NULL,'13000016576',0,1,'2021-02-22 00:00:00','2026-04-20 16:14:58',0),(449,'seed-owner-449','seed-union-449','YZ080210119','SKC123456',1,1,'娌堝彲杈?,NULL,'13000016613',0,1,'2019-07-08 00:00:00','2026-04-20 16:14:58',0),(450,'seed-owner-450','seed-union-450','YZ080210420','ZYX123456',1,1,'鏈遍洦濠?,NULL,'13000016650',0,1,'2020-06-18 00:00:00','2026-04-20 16:14:58',0),(451,'seed-owner-451','seed-union-451','YZ080210524','CSR123456',1,1,'闄堟€濈劧',NULL,'13000016687',0,1,'2024-01-22 00:00:00','2026-04-20 16:14:58',0),(452,'seed-owner-452','seed-union-452','YZ080210724','XYX123456',1,1,'璁稿溅妤?,NULL,'13000016724',0,1,'2024-01-16 00:00:00','2026-04-20 16:14:58',0),(453,'seed-owner-453','seed-union-453','YZ080210923','WXJ123456',1,1,'鍚存鏉?,NULL,'13000016761',0,1,'2023-03-27 00:00:00','2026-04-20 16:14:58',0),(454,'seed-owner-454','seed-union-454','YZ080211022','SWH123456',1,1,'鏂芥枃娑?,NULL,'13000016798',0,1,'2022-11-02 00:00:00','2026-04-20 16:14:58',0),(455,'seed-owner-455','seed-union-455','YZ080211111','YLH123456',1,1,'鏉ㄦ灄鑸?,NULL,'13000016835',0,1,'2011-07-12 00:00:00','2026-04-20 16:14:58',0),(456,'seed-owner-456','seed-union-456','YZ080211217','FSX123456',1,1,'鍐瘲鐞?,NULL,'13000016872',0,1,'2017-06-04 00:00:00','2026-04-20 16:14:58',0),(457,'seed-owner-457','seed-union-457','YZ080211320','QCC123456',1,1,'閽辫瘹鏅?,NULL,'13000016909',0,1,'2020-08-31 00:00:00','2026-04-20 16:14:58',0),(458,'seed-owner-458','seed-union-458','YZ080211514','WMN123456',1,1,'鍗槑瀹?,NULL,'13000016946',0,1,'2014-11-21 00:00:00','2026-04-20 16:14:58',0),(459,'seed-owner-459','seed-union-459','YZ080211617','ZCX123456',1,1,'鍛ㄦ櫒杞?,NULL,'13000016983',0,1,'2017-11-22 00:00:00','2026-04-20 16:14:58',0),(460,'seed-owner-460','seed-union-460','YZ080211721','LXC123456',1,1,'鍚曞杈?,NULL,'13000017020',0,1,'2021-05-02 00:00:00','2026-04-20 16:14:58',0),(461,'seed-owner-461','seed-union-461','YZ080211911','WHL123456',1,1,'鐜嬫旦鐞?,NULL,'13000017057',0,1,'2011-07-03 00:00:00','2026-04-20 16:14:58',0),(462,'seed-owner-462','seed-union-462','YZ080212010','ZRX123456',1,1,'璧佃嫢濠?,NULL,'13000017094',0,1,'2010-06-15 00:00:00','2026-04-20 16:14:58',0),(463,'seed-owner-463','seed-union-463','YZ080212224','XBX123456',1,1,'瑜氬崥濡?,NULL,'13000017131',0,1,'2024-02-26 00:00:00','2026-04-20 16:14:58',0),(464,'seed-owner-464','seed-union-464','YZ080212618','ZJH123456',1,1,'閮戝槈娑?,NULL,'13000017168',0,1,'2018-09-05 00:00:00','2026-04-20 16:14:58',0),(465,'seed-owner-465','seed-union-465','YZ080212720','ZKH123456',1,1,'寮犲彲鑸?,NULL,'13000017205',0,1,'2020-01-12 00:00:00','2026-04-20 16:14:58',0),(466,'seed-owner-466','seed-union-466','YZ080212818','ZCX123456',1,1,'鏈辫景鐞?,NULL,'13000017242',0,1,'2018-03-07 00:00:00','2026-04-20 16:14:58',0),(467,'seed-owner-467','seed-union-467','YZ080213118','XSN123456',1,1,'璁告€濆畞',NULL,'13000017279',0,1,'2018-08-05 00:00:00','2026-04-20 16:14:58',0),(468,'seed-owner-468','seed-union-468','YZ080213418','SXX123456',1,1,'鏂藉钀?,NULL,'13000017316',0,1,'2018-06-23 00:00:00','2026-04-20 16:14:58',0),(469,'seed-owner-469','seed-union-469','YZ080213614','FWX123456',1,1,'鍐枃濠?,NULL,'13000017353',0,1,'2014-09-06 00:00:00','2026-04-20 16:14:58',0),(470,'seed-owner-470','seed-union-470','YZ080213722','QLR123456',1,1,'閽辨灄鐒?,NULL,'13000017390',0,1,'2022-09-12 00:00:00','2026-04-20 16:14:58',0),(471,'seed-owner-471','seed-union-471','YZ080213815','YSX123456',1,1,'灏よ瘲濡?,NULL,'13000017427',0,1,'2015-05-09 00:00:00','2026-04-20 16:14:58',0),(472,'seed-owner-472','seed-union-472','YZ080213921','WCX123456',1,1,'鍗瘹妤?,NULL,'13000017464',0,1,'2021-04-17 00:00:00','2026-04-20 16:14:58',0),(473,'seed-owner-473','seed-union-473','YZ080214125','LMJ123456',1,1,'鍚曟槑鏉?,NULL,'13000017501',0,1,'2025-01-24 00:00:00','2026-04-20 16:14:58',0),(474,'seed-owner-474','seed-union-474','YZ080214219','HCH123456',1,1,'闊╂櫒娑?,NULL,'13000017538',0,1,'2019-09-17 00:00:00','2026-04-20 16:14:58',0),(475,'seed-owner-475','seed-union-475','YZ080214415','ZYX123456',1,1,'璧垫偊鐞?,NULL,'13000017575',0,1,'2015-08-01 00:00:00','2026-04-20 16:14:58',0),(476,'seed-owner-476','seed-union-476','YZ080214519','QHC123456',1,1,'绉︽旦鏅?,NULL,'13000017612',0,1,'2019-11-27 00:00:00','2026-04-20 16:14:58',0),(477,'seed-owner-477','seed-union-477','YZ080214810','HBX123456',1,1,'浣曞崥杞?,NULL,'13000017649',0,1,'2010-10-15 00:00:00','2026-04-20 16:14:58',0),(478,'seed-owner-478','seed-union-478','YZ080214910','SNC123456',1,1,'娌堝畞杈?,NULL,'13000017686',0,1,'2010-03-23 00:00:00','2026-04-20 16:14:58',0),(479,'seed-owner-479','seed-union-479','YZ090110113','ZZL123456',1,1,'寮犲瓙鐞?,NULL,'13000017723',0,1,'2013-12-20 00:00:00','2026-04-20 16:14:58',0),(480,'seed-owner-480','seed-union-480','YZ090110511','XBX123456',1,1,'璁稿浆妤?,NULL,'13000017760',0,1,'2011-06-29 00:00:00','2026-04-20 16:14:58',0),(481,'seed-owner-481','seed-union-481','YZ090110711','WSJ123456',1,1,'鍚存€濇澃',NULL,'13000017797',0,1,'2011-08-10 00:00:00','2026-04-20 16:14:58',0),(482,'seed-owner-482','seed-union-482','YZ090110921','YYH123456',1,1,'鏉ㄥ溅鑸?,NULL,'13000017834',0,1,'2021-07-18 00:00:00','2026-04-20 16:14:58',0),(483,'seed-owner-483','seed-union-483','YZ090111316','WLN123456',1,1,'鍗灄瀹?,NULL,'13000017871',0,1,'2016-03-30 00:00:00','2026-04-20 16:14:58',0),(484,'seed-owner-484','seed-union-484','YZ090111422','ZSX123456',1,1,'鍛ㄨ瘲杞?,NULL,'13000017908',0,1,'2022-09-01 00:00:00','2026-04-20 16:14:58',0),(485,'seed-owner-485','seed-union-485','YZ090111524','LCC123456',1,1,'鍚曡瘹杈?,NULL,'13000017945',0,1,'2024-09-17 00:00:00','2026-04-20 16:14:58',0),(486,'seed-owner-486','seed-union-486','YZ090111615','HYX123456',1,1,'闊╁畤钀?,NULL,'13000017982',0,1,'2015-02-02 00:00:00','2026-04-20 16:14:58',0),(487,'seed-owner-487','seed-union-487','YZ090111717','WML123456',1,1,'鐜嬫槑鐞?,NULL,'13000018019',0,1,'2017-11-17 00:00:00','2026-04-20 16:14:58',0),(488,'seed-owner-488','seed-union-488','YZ090111814','ZCX123456',1,1,'璧垫櫒濠?,NULL,'13000018056',0,1,'2014-10-02 00:00:00','2026-04-20 16:14:58',0),(489,'seed-owner-489','seed-union-489','YZ090111913','QXR123456',1,1,'绉﹀鐒?,NULL,'13000018093',0,1,'2013-06-08 00:00:00','2026-04-20 16:14:58',0),(490,'seed-owner-490','seed-union-490','YZ090112114','LHX123456',1,1,'鏉庢旦妤?,NULL,'13000018130',0,1,'2014-12-30 00:00:00','2026-04-20 16:14:58',0),(491,'seed-owner-491','seed-union-491','YZ090112322','SAJ123456',1,1,'娌堝畨鏉?,NULL,'13000018167',0,1,'2022-11-04 00:00:00','2026-04-20 16:14:58',0),(492,'seed-owner-492','seed-union-492','YZ090112422','ZBH123456',1,1,'閮戝崥娑?,NULL,'13000018204',0,1,'2022-01-24 00:00:00','2026-04-20 16:14:58',0),(493,'seed-owner-493','seed-union-493','YZ090112514','ZNH123456',1,1,'寮犲畞鑸?,NULL,'13000018241',0,1,'2014-05-29 00:00:00','2026-04-20 16:14:58',0),(494,'seed-owner-494','seed-union-494','YZ090112616','ZXX123456',1,1,'鏈辨鐞?,NULL,'13000018278',0,1,'2016-09-30 00:00:00','2026-04-20 16:14:58',0),(495,'seed-owner-495','seed-union-495','YZ090112714','CZC123456',1,1,'闄堝瓙鏅?,NULL,'13000018315',0,1,'2014-10-31 00:00:00','2026-04-20 16:14:58',0),(496,'seed-owner-496','seed-union-496','YZ090112921','XKN123456',1,1,'璁稿彲瀹?,NULL,'13000018352',0,1,'2021-02-04 00:00:00','2026-04-20 16:14:58',0),(497,'seed-owner-497','seed-union-497','YZ090113122','WBC123456',1,1,'鍚村浆杈?,NULL,'13000018389',0,1,'2022-05-11 00:00:00','2026-04-20 16:14:58',0),(498,'seed-owner-498','seed-union-498','YZ090113421','FYX123456',1,1,'鍐緷濠?,NULL,'13000018426',0,1,'2021-01-21 00:00:00','2026-04-20 16:14:58',0),(499,'seed-owner-499','seed-union-499','YZ090113509','QYR123456',1,1,'閽卞溅鐒?,NULL,'13000018463',0,1,'2009-07-26 00:00:00','2026-04-20 16:14:58',0),(500,'seed-owner-500','seed-union-500','YZ090113616','YXX123456',1,1,'灏ゅ濡?,NULL,'13000018500',0,1,'2016-10-17 00:00:00','2026-04-20 16:14:58',0),(501,'seed-owner-501','seed-union-501','YZ090113811','ZWX123456',1,1,'鍛ㄦ枃鐫?,NULL,'13000018537',0,1,'2011-08-31 00:00:00','2026-04-20 16:14:58',0),(502,'seed-owner-502','seed-union-502','YZ090113920','LLJ123456',1,1,'鍚曟灄鏉?,NULL,'13000018574',0,1,'2020-03-21 00:00:00','2026-04-20 16:14:58',0),(503,'seed-owner-503','seed-union-503','YZ090114024','HSH123456',1,1,'闊╄瘲娑?,NULL,'13000018611',0,1,'2024-08-01 00:00:00','2026-04-20 16:14:58',0),(504,'seed-owner-504','seed-union-504','YZ090114312','QMC123456',1,1,'绉︽槑鏅?,NULL,'13000018648',0,1,'2012-09-07 00:00:00','2026-04-20 16:14:58',0),(505,'seed-owner-505','seed-union-505','YZ090114418','XCF123456',1,1,'瑜氭櫒宄?,NULL,'13000018685',0,1,'2018-02-23 00:00:00','2026-04-20 16:14:58',0),(506,'seed-owner-506','seed-union-506','YZ090114511','LXN123456',1,1,'鏉庡瀹?,NULL,'13000018722',0,1,'2011-05-06 00:00:00','2026-04-20 16:14:58',0),(507,'seed-owner-507','seed-union-507','YZ090114621','HYX123456',1,1,'浣曟偊杞?,NULL,'13000018759',0,1,'2021-06-20 00:00:00','2026-04-20 16:14:58',0),(508,'seed-owner-508','seed-union-508','YZ090114924','ZAL123456',1,1,'寮犲畨鐞?,NULL,'13000018796',0,1,'2024-03-05 00:00:00','2026-04-20 16:14:58',0),(509,'seed-owner-509','seed-union-509','YZ090115018','ZBX123456',1,1,'鏈卞崥濠?,NULL,'13000018833',0,1,'2018-02-02 00:00:00','2026-04-20 16:14:58',0),(510,'seed-owner-510','seed-union-510','YZ090210120','CNR123456',1,1,'闄堝畞鐒?,NULL,'13000018870',0,1,'2020-04-29 00:00:00','2026-04-20 16:14:58',0),(511,'seed-owner-511','seed-union-511','YZ090210221','SXX123456',1,1,'瀛欐濡?,NULL,'13000018907',0,1,'2021-08-20 00:00:00','2026-04-20 16:14:58',0),(512,'seed-owner-512','seed-union-512','YZ090210312','XZX123456',1,1,'璁稿瓙妤?,NULL,'13000018944',0,1,'2012-02-01 00:00:00','2026-04-20 16:14:58',0),(513,'seed-owner-513','seed-union-513','YZ090210724','YBH123456',1,1,'鏉ㄥ浆鑸?,NULL,'13000018981',0,1,'2024-05-02 00:00:00','2026-04-20 16:14:58',0),(514,'seed-owner-514','seed-union-514','YZ090210820','FYX123456',1,1,'鍐洦鐞?,NULL,'13000019018',0,1,'2020-02-09 00:00:00','2026-04-20 16:14:58',0),(515,'seed-owner-515','seed-union-515','YZ090211016','YYF123456',1,1,'灏や緷宄?,NULL,'13000019055',0,1,'2016-01-01 00:00:00','2026-04-20 16:14:58',0),(516,'seed-owner-516','seed-union-516','YZ090211114','WYN123456',1,1,'鍗溅瀹?,NULL,'13000019092',0,1,'2014-07-04 00:00:00','2026-04-20 16:14:58',0),(517,'seed-owner-517','seed-union-517','YZ090211313','LXC123456',1,1,'鍚曟杈?,NULL,'13000019129',0,1,'2013-09-05 00:00:00','2026-04-20 16:14:58',0),(518,'seed-owner-518','seed-union-518','YZ090211420','HWX123456',1,1,'闊╂枃钀?,NULL,'13000019166',0,1,'2020-04-21 00:00:00','2026-04-20 16:14:58',0),(519,'seed-owner-519','seed-union-519','YZ090211917','LMX123456',1,1,'鏉庢槑妤?,NULL,'13000019203',0,1,'2017-06-01 00:00:00','2026-04-20 16:14:58',0),(520,'seed-owner-520','seed-union-520','YZ090212021','HCX123456',1,1,'浣曟櫒鐫?,NULL,'13000019240',0,1,'2021-01-22 00:00:00','2026-04-20 16:14:58',0),(521,'seed-owner-521','seed-union-521','YZ090212221','ZYH123456',1,1,'閮戞偊娑?,NULL,'13000019277',0,1,'2021-11-06 00:00:00','2026-04-20 16:14:58',0),(522,'seed-owner-522','seed-union-522','YZ090212324','ZHH123456',1,1,'寮犳旦鑸?,NULL,'13000019314',0,1,'2024-02-15 00:00:00','2026-04-20 16:14:58',0),(523,'seed-owner-523','seed-union-523','YZ090212426','ZRX123456',1,1,'鏈辫嫢鐞?,NULL,'13000019351',0,1,'2026-01-30 00:00:00','2026-04-20 16:14:58',0),(524,'seed-owner-524','seed-union-524','YZ090212710','XNN123456',1,1,'璁稿畞瀹?,NULL,'13000019388',0,1,'2010-04-28 00:00:00','2026-04-20 16:14:58',0),(525,'seed-owner-525','seed-union-525','YZ090212809','JXX123456',1,1,'钂嬫杞?,NULL,'13000019425',0,1,'2009-01-10 00:00:00','2026-04-20 16:14:58',0),(526,'seed-owner-526','seed-union-526','YZ090212911','WZC123456',1,1,'鍚村瓙杈?,NULL,'13000019462',0,1,'2011-06-03 00:00:00','2026-04-20 16:14:58',0),(527,'seed-owner-527','seed-union-527','YZ090213324','QBR123456',1,1,'閽卞浆鐒?,NULL,'13000019499',0,1,'2024-02-29 00:00:00','2026-04-20 16:14:58',0),(528,'seed-owner-528','seed-union-528','YZ090213625','ZYX123456',1,1,'鍛ㄤ緷鐫?,NULL,'13000019536',0,1,'2025-09-22 00:00:00','2026-04-20 16:14:58',0),(529,'seed-owner-529','seed-union-529','YZ090213826','HXH123456',1,1,'闊╁娑?,NULL,'13000019573',0,1,'2026-11-28 00:00:00','2026-04-20 16:14:58',0),(530,'seed-owner-530','seed-union-530','YZ090213924','WXH123456',1,1,'鐜嬫鑸?,NULL,'13000019610',0,1,'2024-07-28 00:00:00','2026-04-20 16:14:58',0),(531,'seed-owner-531','seed-union-531','YZ090214015','ZWX123456',1,1,'璧垫枃鐞?,NULL,'13000019647',0,1,'2015-05-21 00:00:00','2026-04-20 16:14:58',0),(532,'seed-owner-532','seed-union-532','YZ090214211','XSF123456',1,1,'瑜氳瘲宄?,NULL,'13000019684',0,1,'2011-03-07 00:00:00','2026-04-20 16:14:58',0),(533,'seed-owner-533','seed-union-533','YZ090214413','HYX123456',1,1,'浣曞畤杞?,NULL,'13000019721',0,1,'2013-04-04 00:00:00','2026-04-20 16:14:58',0),(534,'seed-owner-534','seed-union-534','YZ090214714','ZXL123456',1,1,'寮犲鐞?,NULL,'13000019758',0,1,'2014-09-08 00:00:00','2026-04-20 16:14:58',0),(535,'seed-owner-535','seed-union-535','YZ090214913','CHR123456',1,1,'闄堟旦鐒?,NULL,'13000019795',0,1,'2013-06-02 00:00:00','2026-04-20 16:14:58',0),(536,'seed-owner-536','seed-union-536','YZ090215024','SRX123456',1,1,'瀛欒嫢濡?,NULL,'13000019832',0,1,'2024-02-19 00:00:00','2026-04-20 16:14:58',0),(537,'seed-owner-537','seed-union-537','YZ100110214','JBX123456',1,1,'钂嬪崥鐫?,NULL,'13000019869',0,1,'2014-05-10 00:00:00','2026-04-20 16:14:58',0),(538,'seed-owner-538','seed-union-538','YZ100110425','SXH123456',1,1,'鏂芥娑?,NULL,'13000019906',0,1,'2025-08-21 00:00:00','2026-04-20 16:14:58',0),(539,'seed-owner-539','seed-union-539','YZ100110510','YZH123456',1,1,'鏉ㄥ瓙鑸?,NULL,'13000019943',0,1,'2010-10-27 00:00:00','2026-04-20 16:14:58',0),(540,'seed-owner-540','seed-union-540','YZ100110611','FJX123456',1,1,'鍐槈鐞?,NULL,'13000019980',0,1,'2011-04-15 00:00:00','2026-04-20 16:14:58',0),(541,'seed-owner-541','seed-union-541','YZ100111114','LSC123456',1,1,'鍚曟€濊景',NULL,'13000020017',0,1,'2014-03-03 00:00:00','2026-04-20 16:14:58',0),(542,'seed-owner-542','seed-union-542','YZ100111219','HYX123456',1,1,'闊╀緷钀?,NULL,'13000020054',0,1,'2019-08-22 00:00:00','2026-04-20 16:14:58',0),(543,'seed-owner-543','seed-union-543','YZ100111318','WYL123456',1,1,'鐜嬪溅鐞?,NULL,'13000020091',0,1,'2018-07-18 00:00:00','2026-04-20 16:14:58',0),(544,'seed-owner-544','seed-union-544','YZ100111622','XWX123456',1,1,'瑜氭枃濡?,NULL,'13000020128',0,1,'2022-12-16 00:00:00','2026-04-20 16:14:58',0),(545,'seed-owner-545','seed-union-545','YZ100111709','LLX123456',1,1,'鏉庢灄妤?,NULL,'13000020165',0,1,'2009-12-14 00:00:00','2026-04-20 16:14:58',0),(546,'seed-owner-546','seed-union-546','YZ100111814','HSX123456',1,1,'浣曡瘲鐫?,NULL,'13000020202',0,1,'2014-04-21 00:00:00','2026-04-20 16:14:58',0),(547,'seed-owner-547','seed-union-547','YZ100111911','SCJ123456',1,1,'娌堣瘹鏉?,NULL,'13000020239',0,1,'2011-08-03 00:00:00','2026-04-20 16:14:58',0),(548,'seed-owner-548','seed-union-548','YZ100112018','ZYH123456',1,1,'閮戝畤娑?,NULL,'13000020276',0,1,'2018-08-08 00:00:00','2026-04-20 16:14:58',0),(549,'seed-owner-549','seed-union-549','YZ100112123','ZMH123456',1,1,'寮犳槑鑸?,NULL,'13000020313',0,1,'2023-03-15 00:00:00','2026-04-20 16:14:58',0),(550,'seed-owner-550','seed-union-550','YZ100112313','CXC123456',1,1,'闄堝鏅?,NULL,'13000020350',0,1,'2013-10-22 00:00:00','2026-04-20 16:14:58',0),(551,'seed-owner-551','seed-union-551','YZ100112610','JRX123456',1,1,'钂嬭嫢杞?,NULL,'13000020387',0,1,'2010-05-26 00:00:00','2026-04-20 16:14:58',0),(552,'seed-owner-552','seed-union-552','YZ100112710','WAC123456',1,1,'鍚村畨杈?,NULL,'13000020424',0,1,'2010-01-10 00:00:00','2026-04-20 16:14:58',0),(553,'seed-owner-553','seed-union-553','YZ100113019','FXX123456',1,1,'鍐濠?,NULL,'13000020461',0,1,'2019-06-11 00:00:00','2026-04-20 16:14:58',0),(554,'seed-owner-554','seed-union-554','YZ100113123','QZR123456',1,1,'閽卞瓙鐒?,NULL,'13000020498',0,1,'2023-04-20 00:00:00','2026-04-20 16:14:58',0),(555,'seed-owner-555','seed-union-555','YZ100113225','YJX123456',1,1,'灏ゅ槈濡?,NULL,'13000020535',0,1,'2025-01-26 00:00:00','2026-04-20 16:14:58',0),(556,'seed-owner-556','seed-union-556','YZ100113715','WSH123456',1,1,'鐜嬫€濊埅',NULL,'13000020572',0,1,'2015-05-08 00:00:00','2026-04-20 16:14:58',0),(557,'seed-owner-557','seed-union-557','YZ100113821','ZYX123456',1,1,'璧典緷鐞?,NULL,'13000020609',0,1,'2021-04-02 00:00:00','2026-04-20 16:14:58',0),(558,'seed-owner-558','seed-union-558','YZ100114009','XXF123456',1,1,'瑜氬宄?,NULL,'13000020646',0,1,'2009-12-30 00:00:00','2026-04-20 16:14:58',0),(559,'seed-owner-559','seed-union-559','XQYZU000000559','YZ123456',1,1,'涓氫富1944',NULL,'13000020683',0,1,'2014-06-21 00:00:00','2026-04-20 16:14:58',0),(560,'seed-owner-560','seed-union-560','XQYZU000000560','YZ123456',1,1,'涓氫富1945',NULL,'13000020720',0,1,'2023-09-25 00:00:00','2026-04-20 16:14:58',0),(561,'seed-owner-561','seed-union-561','XQYZU000000561','YZ123456',1,1,'涓氫富1947',NULL,'13000020757',0,1,'2013-04-27 00:00:00','2026-04-20 16:14:58',0),(562,'seed-owner-562','seed-union-562','XQYZU000000562','YZ123456',1,1,'涓氫富1949',NULL,'13000020794',0,1,'2012-09-01 00:00:00','2026-04-20 16:14:58',0),(563,'seed-owner-563','seed-union-563','XQYZU000000563','YZ123456',1,1,'涓氫富1950',NULL,'13000020831',0,1,'2016-03-30 00:00:00','2026-04-20 16:14:58',0),(564,'seed-owner-564','seed-union-564','XQYZU000000564','YZ123456',1,1,'涓氫富1951',NULL,'13000020868',0,1,'2021-07-15 00:00:00','2026-04-20 16:14:58',0),(565,'seed-owner-565','seed-union-565','XQYZU000000565','YZ123456',1,1,'涓氫富1953',NULL,'13000020905',0,1,'2024-03-10 00:00:00','2026-04-20 16:14:58',0),(566,'seed-owner-566','seed-union-566','XQYZU000000566','YZ123456',1,1,'涓氫富1954',NULL,'13000020942',0,1,'2021-04-11 00:00:00','2026-04-20 16:14:58',0),(567,'seed-owner-567','seed-union-567','XQYZU000000567','YZ123456',1,1,'涓氫富1956',NULL,'13000020979',0,1,'2017-02-08 00:00:00','2026-04-20 16:14:58',0),(568,'seed-owner-568','seed-union-568','XQYZU000000568','YZ123456',1,1,'涓氫富1958',NULL,'13000021016',0,1,'2023-04-05 00:00:00','2026-04-20 16:14:58',0),(569,'seed-owner-569','seed-union-569','XQYZU000000569','YZ123456',1,1,'涓氫富1960',NULL,'13000021053',0,1,'2019-09-19 00:00:00','2026-04-20 16:14:58',0),(570,'seed-owner-570','seed-union-570','XQYZU000000570','YZ123456',1,1,'涓氫富1961',NULL,'13000021090',0,1,'2018-04-28 00:00:00','2026-04-20 16:14:58',0),(571,'seed-owner-571','seed-union-571','XQYZU000000571','YZ123456',1,1,'涓氫富1963',NULL,'13000021127',0,1,'2017-03-21 00:00:00','2026-04-20 16:14:58',0),(572,'seed-owner-572','seed-union-572','XQYZU000000572','YZ123456',1,1,'涓氫富1964',NULL,'13000021164',0,1,'2025-09-05 00:00:00','2026-04-20 16:14:58',0),(573,'seed-owner-573','seed-union-573','XQYZU000000573','YZ123456',1,1,'涓氫富1965',NULL,'13000021201',0,1,'2023-07-11 00:00:00','2026-04-20 16:14:58',0),(574,'seed-owner-574','seed-union-574','XQYZU000000574','YZ123456',1,1,'涓氫富1969',NULL,'13000021238',0,1,'2014-12-28 00:00:00','2026-04-20 16:14:58',0),(575,'seed-owner-575','seed-union-575','XQYZU000000575','YZ123456',1,1,'涓氫富1971',NULL,'13000021275',0,1,'2011-09-06 00:00:00','2026-04-20 16:14:58',0),(576,'seed-owner-576','seed-union-576','XQYZU000000576','YZ123456',1,1,'涓氫富1972',NULL,'13000021312',0,1,'2024-09-18 00:00:00','2026-04-20 16:14:58',0),(577,'seed-owner-577','seed-union-577','XQYZU000000577','YZ123456',1,1,'涓氫富1973',NULL,'13000021349',0,1,'2023-12-12 00:00:00','2026-04-20 16:14:58',0),(578,'seed-owner-578','seed-union-578','XQYZU000000578','YZ123456',1,1,'涓氫富1974',NULL,'13000021386',0,1,'2024-05-19 00:00:00','2026-04-20 16:14:58',0),(579,'seed-owner-579','seed-union-579','XQYZU000000579','YZ123456',1,1,'涓氫富1975',NULL,'13000021423',0,1,'2025-11-23 00:00:00','2026-04-20 16:14:58',0),(580,'seed-owner-580','seed-union-580','XQYZU000000580','YZ123456',1,1,'涓氫富1976',NULL,'13000021460',0,1,'2009-05-01 00:00:00','2026-04-20 16:14:58',0),(581,'seed-owner-581','seed-union-581','XQYZU000000581','YZ123456',1,1,'涓氫富1977',NULL,'13000021497',0,1,'2011-03-10 00:00:00','2026-04-20 16:14:58',0),(582,'seed-owner-582','seed-union-582','XQYZU000000582','YZ123456',1,1,'涓氫富1978',NULL,'13000021534',0,1,'2012-12-21 00:00:00','2026-04-20 16:14:58',0),(583,'seed-owner-583','seed-union-583','XQYZU000000583','YZ123456',1,1,'涓氫富1980',NULL,'13000021571',0,1,'2016-04-17 00:00:00','2026-04-20 16:14:58',0),(584,'seed-owner-584','seed-union-584','XQYZU000000584','YZ123456',1,1,'涓氫富1981',NULL,'13000021608',0,1,'2018-01-21 00:00:00','2026-04-20 16:14:58',0),(585,'seed-owner-585','seed-union-585','XQYZU000000585','YZ123456',1,1,'涓氫富1982',NULL,'13000021645',0,1,'2014-07-04 00:00:00','2026-04-20 16:14:58',0),(586,'seed-owner-586','seed-union-586','XQYZU000000586','YZ123456',1,1,'涓氫富1983',NULL,'13000021682',0,1,'2024-10-19 00:00:00','2026-04-20 16:14:58',0),(587,'seed-owner-587','seed-union-587','XQYZU000000587','YZ123456',1,1,'涓氫富1984',NULL,'13000021719',0,1,'2026-06-30 00:00:00','2026-04-20 16:14:58',0),(588,'seed-owner-588','seed-union-588','XQYZU000000588','YZ123456',1,1,'涓氫富1986',NULL,'13000021756',0,1,'2021-07-04 00:00:00','2026-04-20 16:14:58',0),(589,'seed-owner-589','seed-union-589','XQYZU000000589','YZ123456',1,1,'涓氫富1987',NULL,'13000021793',0,1,'2013-08-28 00:00:00','2026-04-20 16:14:58',0),(590,'seed-owner-590','seed-union-590','XQYZU000000590','YZ123456',1,1,'涓氫富1988',NULL,'13000021830',0,1,'2025-11-03 00:00:00','2026-04-20 16:14:58',0),(591,'seed-owner-591','seed-union-591','XQYZU000000591','YZ123456',1,1,'涓氫富1989',NULL,'13000021867',0,1,'2024-07-18 00:00:00','2026-04-20 16:14:58',0),(592,'seed-owner-592','seed-union-592','XQYZU000000592','YZ123456',1,1,'涓氫富1991',NULL,'13000021904',0,1,'2022-06-24 00:00:00','2026-04-20 16:14:58',0),(593,'seed-owner-593','seed-union-593','XQYZU000000593','YZ123456',1,1,'涓氫富1992',NULL,'13000021941',0,1,'2019-11-28 00:00:00','2026-04-20 16:14:58',0),(594,'seed-owner-594','seed-union-594','XQYZU000000594','YZ123456',1,1,'涓氫富1996',NULL,'13000021978',0,1,'2023-01-23 00:00:00','2026-04-20 16:14:58',0),(595,'seed-owner-595','seed-union-595','XQYZU000000595','YZ123456',1,1,'涓氫富1997',NULL,'13000022015',0,1,'2023-08-28 00:00:00','2026-04-20 16:14:58',0),(596,'seed-owner-596','seed-union-596','XQYZU000000596','YZ123456',1,1,'涓氫富2002',NULL,'13000022052',0,1,'2026-07-24 00:00:00','2026-04-20 16:14:58',0),(597,'seed-owner-597','seed-union-597','XQYZU000000597','YZ123456',1,1,'涓氫富2003',NULL,'13000022089',0,1,'2026-08-17 00:00:00','2026-04-20 16:14:58',0),(598,'seed-owner-598','seed-union-598','XQYZU000000598','YZ123456',1,1,'涓氫富2004',NULL,'13000022126',0,1,'2026-12-22 00:00:00','2026-04-20 16:14:58',0),(599,'seed-owner-599','seed-union-599','XQYZU000000599','YZ123456',1,1,'涓氫富2006',NULL,'13000022163',0,1,'2009-05-21 00:00:00','2026-04-20 16:14:58',0),(600,'seed-owner-600','seed-union-600','XQYZU000000600','YZ123456',1,1,'涓氫富2008',NULL,'13000022200',0,1,'2018-09-02 00:00:00','2026-04-20 16:14:58',0),(601,'seed-owner-601','seed-union-601','XQYZU000000601','YZ123456',1,1,'涓氫富2009',NULL,'13000022237',0,1,'2022-07-15 00:00:00','2026-04-20 16:14:58',0),(602,'seed-owner-602','seed-union-602','XQYZU000000602','YZ123456',1,1,'涓氫富2010',NULL,'13000022274',0,1,'2020-10-26 00:00:00','2026-04-20 16:14:58',0),(603,'seed-owner-603','seed-union-603','XQYZU000000603','YZ123456',1,1,'涓氫富2011',NULL,'13000022311',0,1,'2013-02-11 00:00:00','2026-04-20 16:14:58',0),(604,'seed-owner-604','seed-union-604','XQYZU000000604','YZ123456',1,1,'涓氫富2012',NULL,'13000022348',0,1,'2019-01-05 00:00:00','2026-04-20 16:14:58',0),(605,'seed-owner-605','seed-union-605','XQYZU000000605','YZ123456',1,1,'涓氫富2013',NULL,'13000022385',0,1,'2014-10-15 00:00:00','2026-04-20 16:14:58',0),(606,'seed-owner-606','seed-union-606','XQYZU000000606','YZ123456',1,1,'涓氫富2014',NULL,'13000022422',0,1,'2021-10-03 00:00:00','2026-04-20 16:14:58',0),(607,'seed-owner-607','seed-union-607','XQYZU000000607','YZ123456',1,1,'涓氫富2015',NULL,'13000022459',0,1,'2009-01-17 00:00:00','2026-04-20 16:14:58',0),(608,'seed-owner-608','seed-union-608','XQYZU000000608','YZ123456',1,1,'涓氫富2016',NULL,'13000022496',0,1,'2010-03-20 00:00:00','2026-04-20 16:14:58',0),(609,'seed-owner-609','seed-union-609','XQYZU000000609','YZ123456',1,1,'涓氫富2017',NULL,'13000022533',0,1,'2014-12-20 00:00:00','2026-04-20 16:14:58',0),(610,'seed-owner-610','seed-union-610','XQYZU000000610','YZ123456',1,1,'涓氫富2021',NULL,'13000022570',0,1,'2014-12-28 00:00:00','2026-04-20 16:14:58',0),(611,'seed-owner-611','seed-union-611','XQYZU000000611','YZ123456',1,1,'涓氫富2023',NULL,'13000022607',0,1,'2009-09-22 00:00:00','2026-04-20 16:14:58',0),(612,'seed-owner-612','seed-union-612','XQYZU000000612','YZ123456',1,1,'涓氫富2027',NULL,'13000022644',0,1,'2018-07-07 00:00:00','2026-04-20 16:14:58',0),(613,'seed-owner-613','seed-union-613','XQYZU000000613','YZ123456',1,1,'涓氫富2030',NULL,'13000022681',0,1,'2019-12-03 00:00:00','2026-04-20 16:14:58',0),(614,'seed-owner-614','seed-union-614','XQYZU000000614','YZ123456',1,1,'涓氫富2032',NULL,'13000022718',0,1,'2025-12-05 00:00:00','2026-04-20 16:14:58',0),(615,'seed-owner-615','seed-union-615','XQYZU000000615','YZ123456',1,1,'涓氫富2033',NULL,'13000022755',0,1,'2026-03-04 00:00:00','2026-04-20 16:14:58',0),(616,'seed-owner-616','seed-union-616','XQYZU000000616','YZ123456',1,1,'涓氫富2034',NULL,'13000022792',0,1,'2025-08-20 00:00:00','2026-04-20 16:14:58',0),(617,'seed-owner-617','seed-union-617','XQYZU000000617','YZ123456',1,1,'涓氫富2035',NULL,'13000022829',0,1,'2012-04-16 00:00:00','2026-04-20 16:14:58',0),(618,'seed-owner-618','seed-union-618','XQYZU000000618','YZ123456',1,1,'涓氫富2036',NULL,'13000022866',0,1,'2020-11-23 00:00:00','2026-04-20 16:14:58',0),(619,'seed-owner-619','seed-union-619','XQYZU000000619','YZ123456',1,1,'涓氫富2037',NULL,'13000022903',0,1,'2025-11-28 00:00:00','2026-04-20 16:14:58',0),(620,'seed-owner-620','seed-union-620','XQYZU000000620','YZ123456',1,1,'涓氫富2038',NULL,'13000022940',0,1,'2014-06-28 00:00:00','2026-04-20 16:14:58',0),(621,'seed-owner-621','seed-union-621','XQYZU000000621','YZ123456',1,1,'涓氫富2042',NULL,'13000022977',0,1,'2017-03-31 00:00:00','2026-04-20 16:14:58',0),(622,'seed-owner-622','seed-union-622','XQYZU000000622','YZ123456',1,1,'涓氫富2043',NULL,'13000023014',0,1,'2014-12-15 00:00:00','2026-04-20 16:14:58',0),(623,'seed-owner-623','seed-union-623','XQYZU000000623','YZ123456',1,1,'涓氫富2045',NULL,'13000023051',0,1,'2023-05-23 00:00:00','2026-04-20 16:14:58',0),(624,'seed-owner-624','seed-union-624','XQYZU000000624','YZ123456',1,1,'涓氫富2046',NULL,'13000023088',0,1,'2015-04-22 00:00:00','2026-04-20 16:14:58',0),(625,'seed-owner-625','seed-union-625','XQYZU000000625','YZ123456',1,1,'涓氫富2051',NULL,'13000023125',0,1,'2019-01-14 00:00:00','2026-04-20 16:14:58',0),(626,'seed-owner-626','seed-union-626','XQYZU000000626','YZ123456',1,1,'涓氫富2053',NULL,'13000023162',0,1,'2025-09-09 00:00:00','2026-04-20 16:14:58',0),(627,'seed-owner-627','seed-union-627','XQYZU000000627','YZ123456',1,1,'涓氫富2056',NULL,'13000023199',0,1,'2014-07-15 00:00:00','2026-04-20 16:14:58',0),(628,'seed-owner-628','seed-union-628','XQYZU000000628','YZ123456',1,1,'涓氫富2057',NULL,'13000023236',0,1,'2016-03-09 00:00:00','2026-04-20 16:14:58',0),(629,'seed-owner-629','seed-union-629','XQYZU000000629','YZ123456',1,1,'涓氫富2058',NULL,'13000023273',0,1,'2020-06-17 00:00:00','2026-04-20 16:14:58',0),(630,'seed-owner-630','seed-union-630','XQYZU000000630','YZ123456',1,1,'涓氫富2059',NULL,'13000023310',0,1,'2011-11-04 00:00:00','2026-04-20 16:14:58',0),(631,'seed-owner-631','seed-union-631','XQYZU000000631','YZ123456',1,1,'涓氫富2061',NULL,'13000023347',0,1,'2014-10-22 00:00:00','2026-04-20 16:14:58',0),(632,'seed-owner-632','seed-union-632','XQYZU000000632','YZ123456',1,1,'涓氫富2062',NULL,'13000023384',0,1,'2011-05-18 00:00:00','2026-04-20 16:14:58',0),(633,'seed-owner-633','seed-union-633','XQYZU000000633','YZ123456',1,1,'涓氫富2063',NULL,'13000023421',0,1,'2024-03-30 00:00:00','2026-04-20 16:14:58',0),(634,'seed-owner-634','seed-union-634','XQYZU000000634','YZ123456',1,1,'涓氫富2065',NULL,'13000023458',0,1,'2018-06-03 00:00:00','2026-04-20 16:14:58',0),(635,'seed-owner-635','seed-union-635','XQYZU000000635','YZ123456',1,1,'涓氫富2068',NULL,'13000023495',0,1,'2021-11-20 00:00:00','2026-04-20 16:14:58',0),(636,'seed-owner-636','seed-union-636','XQYZU000000636','YZ123456',1,1,'涓氫富2070',NULL,'13000023532',0,1,'2014-06-28 00:00:00','2026-04-20 16:14:58',0),(637,'seed-owner-637','seed-union-637','XQYZU000000637','YZ123456',1,1,'涓氫富2073',NULL,'13000023569',0,1,'2014-09-20 00:00:00','2026-04-20 16:14:58',0),(638,'seed-owner-638','seed-union-638','YZ110212213','SWX123456',1,1,'鏂芥枃钀?,NULL,'13000023606',0,1,'2013-12-10 00:00:00','2026-04-20 16:14:58',0),(639,'seed-owner-639','seed-union-639','YZ110212323','YLL123456',1,1,'鏉ㄦ灄鐞?,NULL,'13000023643',0,1,'2023-03-03 00:00:00','2026-04-20 16:14:58',0),(640,'seed-owner-640','seed-union-640','YZ110212419','FSX123456',1,1,'鍐瘲濠?,NULL,'13000023680',0,1,'2019-01-25 00:00:00','2026-04-20 16:14:58',0),(641,'seed-owner-641','seed-union-641','YZ110212517','QCR123456',1,1,'閽辫瘹鐒?,NULL,'13000023717',0,1,'2017-09-13 00:00:00','2026-04-20 16:14:58',0),(642,'seed-owner-642','seed-union-642','YZ110212717','WMX123456',1,1,'鍗槑妤?,NULL,'13000023754',0,1,'2017-08-29 00:00:00','2026-04-20 16:14:58',0),(643,'seed-owner-643','seed-union-643','YZ110212811','ZCX123456',1,1,'鍛ㄦ櫒鐫?,NULL,'13000023791',0,1,'2011-02-22 00:00:00','2026-04-20 16:14:58',0),(644,'seed-owner-644','seed-union-644','YZ110212918','LXJ123456',1,1,'鍚曞鏉?,NULL,'13000023828',0,1,'2018-08-09 00:00:00','2026-04-20 16:14:58',0),(645,'seed-owner-645','seed-union-645','YZ110213113','WHH123456',1,1,'鐜嬫旦鑸?,NULL,'13000023865',0,1,'2013-12-31 00:00:00','2026-04-20 16:14:58',0),(646,'seed-owner-646','seed-union-646','YZ110213217','ZRX123456',1,1,'璧佃嫢鐞?,NULL,'13000023902',0,1,'2017-12-15 00:00:00','2026-04-20 16:14:58',0),(647,'seed-owner-647','seed-union-647','YZ110213314','QAC123456',1,1,'绉﹀畨鏅?,NULL,'13000023939',0,1,'2014-06-27 00:00:00','2026-04-20 16:14:58',0),(648,'seed-owner-648','seed-union-648','YZ110213510','LNN123456',1,1,'鏉庡畞瀹?,NULL,'13000023976',0,1,'2010-12-25 00:00:00','2026-04-20 16:14:58',0),(649,'seed-owner-649','seed-union-649','YZ110213809','ZJX123456',1,1,'閮戝槈钀?,NULL,'13000024013',0,1,'2009-09-24 00:00:00','2026-04-20 16:14:58',0),(650,'seed-owner-650','seed-union-650','YZ110214225','SYX123456',1,1,'瀛欓洦濡?,NULL,'13000024050',0,1,'2025-08-20 00:00:00','2026-04-20 16:14:58',0),(651,'seed-owner-651','seed-union-651','YZ110214313','XSX123456',1,1,'璁告€濇',NULL,'13000024087',0,1,'2013-11-30 00:00:00','2026-04-20 16:14:58',0),(652,'seed-owner-652','seed-union-652','YZ110214425','JYX123456',1,1,'钂嬩緷鐫?,NULL,'13000024124',0,1,'2025-01-04 00:00:00','2026-04-20 16:14:58',0),(653,'seed-owner-653','seed-union-653','YZ110214523','WYJ123456',1,1,'鍚村溅鏉?,NULL,'13000024161',0,1,'2023-04-07 00:00:00','2026-04-20 16:14:58',0),(654,'seed-owner-654','seed-union-654','YZ110214615','SXH123456',1,1,'鏂藉娑?,NULL,'13000024198',0,1,'2015-09-09 00:00:00','2026-04-20 16:14:58',0),(655,'seed-owner-655','seed-union-655','YZ110214714','YXH123456',1,1,'鏉ㄦ鑸?,NULL,'13000024235',0,1,'2014-11-21 00:00:00','2026-04-20 16:14:58',0),(656,'seed-owner-656','seed-union-656','YZ110214811','FWX123456',1,1,'鍐枃鐞?,NULL,'13000024272',0,1,'2011-07-11 00:00:00','2026-04-20 16:14:58',0),(657,'seed-owner-657','seed-union-657','YZ110214915','QLC123456',1,1,'閽辨灄鏅?,NULL,'13000024309',0,1,'2015-10-19 00:00:00','2026-04-20 16:14:58',0),(658,'seed-owner-658','seed-union-658','YZ120110113','WCN123456',1,1,'鍗瘹瀹?,NULL,'13000024346',0,1,'2013-03-20 00:00:00','2026-04-20 16:14:58',0),(659,'seed-owner-659','seed-union-659','YZ120110211','ZYX123456',1,1,'鍛ㄥ畤杞?,NULL,'13000024383',0,1,'2011-02-24 00:00:00','2026-04-20 16:14:58',0),(660,'seed-owner-660','seed-union-660','YZ120110515','WXL123456',1,1,'鐜嬪鐞?,NULL,'13000024420',0,1,'2015-06-25 00:00:00','2026-04-20 16:14:58',0),(661,'seed-owner-661','seed-union-661','YZ120110615','ZYX123456',1,1,'璧垫偊濠?,NULL,'13000024457',0,1,'2015-06-08 00:00:00','2026-04-20 16:14:58',0),(662,'seed-owner-662','seed-union-662','YZ120110812','XRX123456',1,1,'瑜氳嫢濡?,NULL,'13000024494',0,1,'2012-07-17 00:00:00','2026-04-20 16:14:58',0),(663,'seed-owner-663','seed-union-663','YZ120110912','LAX123456',1,1,'鏉庡畨妤?,NULL,'13000024531',0,1,'2012-01-14 00:00:00','2026-04-20 16:14:58',0),(664,'seed-owner-664','seed-union-664','YZ120111012','HBX123456',1,1,'浣曞崥鐫?,NULL,'13000024568',0,1,'2012-04-01 00:00:00','2026-04-20 16:14:58',0),(665,'seed-owner-665','seed-union-665','YZ120111124','SNJ123456',1,1,'娌堝畞鏉?,NULL,'13000024605',0,1,'2024-12-12 00:00:00','2026-04-20 16:14:58',0),(666,'seed-owner-666','seed-union-666','YZ120111209','ZXH123456',1,1,'閮戞娑?,NULL,'13000024642',0,1,'2009-01-01 00:00:00','2026-04-20 16:14:58',0),(667,'seed-owner-667','seed-union-667','YZ120111310','ZZH123456',1,1,'寮犲瓙鑸?,NULL,'13000024679',0,1,'2010-08-02 00:00:00','2026-04-20 16:14:58',0),(668,'seed-owner-668','seed-union-668','YZ120111809','JYX123456',1,1,'钂嬮洦杞?,NULL,'13000024716',0,1,'2009-08-19 00:00:00','2026-04-20 16:14:58',0),(669,'seed-owner-669','seed-union-669','YZ120111923','WSC123456',1,1,'鍚存€濊景',NULL,'13000024753',0,1,'2023-09-28 00:00:00','2026-04-20 16:14:58',0),(670,'seed-owner-670','seed-union-670','YZ120112409','YWX123456',1,1,'灏ゆ枃濡?,NULL,'13000024790',0,1,'2009-05-31 00:00:00','2026-04-20 16:14:58',0),(671,'seed-owner-671','seed-union-671','YZ120112725','LCJ123456',1,1,'鍚曡瘹鏉?,NULL,'13000024827',0,1,'2025-12-20 00:00:00','2026-04-20 16:14:58',0),(672,'seed-owner-672','seed-union-672','YZ120112925','WMH123456',1,1,'鐜嬫槑鑸?,NULL,'13000024864',0,1,'2025-12-29 00:00:00','2026-04-20 16:14:58',0),(673,'seed-owner-673','seed-union-673','YZ120113216','XYF123456',1,1,'瑜氭偊宄?,NULL,'13000024901',0,1,'2016-02-09 00:00:00','2026-04-20 16:14:58',0),(674,'seed-owner-674','seed-union-674','YZ120113510','SAC123456',1,1,'娌堝畨杈?,NULL,'13000024938',0,1,'2010-03-04 00:00:00','2026-04-20 16:14:58',0),(675,'seed-owner-675','seed-union-675','YZ120113624','ZBX123456',1,1,'閮戝崥钀?,NULL,'13000024975',0,1,'2024-04-27 00:00:00','2026-04-20 16:14:58',0),(676,'seed-owner-676','seed-union-676','YZ120113715','ZNL123456',1,1,'寮犲畞鐞?,NULL,'13000025012',0,1,'2015-01-09 00:00:00','2026-04-20 16:14:58',0),(677,'seed-owner-677','seed-union-677','YZ120114019','SJX123456',1,1,'瀛欏槈濡?,NULL,'13000025049',0,1,'2019-03-08 00:00:00','2026-04-20 16:14:58',0),(678,'seed-owner-678','seed-union-678','YZ120114214','JCX123456',1,1,'钂嬭景鐫?,NULL,'13000025086',0,1,'2014-08-02 00:00:00','2026-04-20 16:14:58',0),(679,'seed-owner-679','seed-union-679','YZ120114425','SYH123456',1,1,'鏂介洦娑?,NULL,'13000025123',0,1,'2025-01-04 00:00:00','2026-04-20 16:14:58',0),(680,'seed-owner-680','seed-union-680','YZ120114620','FYX123456',1,1,'鍐緷鐞?,NULL,'13000025160',0,1,'2020-07-07 00:00:00','2026-04-20 16:14:58',0),(681,'seed-owner-681','seed-union-681','YZ120114715','QYC123456',1,1,'閽卞溅鏅?,NULL,'13000025197',0,1,'2015-06-15 00:00:00','2026-04-20 16:14:58',0),(682,'seed-owner-682','seed-union-682','YZ120114816','YXF123456',1,1,'灏ゅ宄?,NULL,'13000025234',0,1,'2016-05-06 00:00:00','2026-04-20 16:14:58',0),(683,'seed-owner-683','seed-union-683','YZ120115023','ZWX123456',1,1,'鍛ㄦ枃杞?,NULL,'13000025271',0,1,'2023-12-17 00:00:00','2026-04-20 16:14:58',0),(684,'seed-owner-684','seed-union-684','YZ120210118','LLC123456',1,1,'鍚曟灄杈?,NULL,'13000025308',0,1,'2018-11-16 00:00:00','2026-04-20 16:14:58',0),(685,'seed-owner-685','seed-union-685','YZ120210217','HSX123456',1,1,'闊╄瘲钀?,NULL,'13000025345',0,1,'2017-01-10 00:00:00','2026-04-20 16:14:58',0),(686,'seed-owner-686','seed-union-686','YZ120210516','QMR123456',1,1,'绉︽槑鐒?,NULL,'13000025382',0,1,'2016-04-25 00:00:00','2026-04-20 16:14:58',0),(687,'seed-owner-687','seed-union-687','YZ120210921','SHJ123456',1,1,'娌堟旦鏉?,NULL,'13000025419',0,1,'2021-05-26 00:00:00','2026-04-20 16:14:58',0),(688,'seed-owner-688','seed-union-688','YZ120211018','ZRH123456',1,1,'閮戣嫢娑?,NULL,'13000025456',0,1,'2018-03-10 00:00:00','2026-04-20 16:14:58',0),(689,'seed-owner-689','seed-union-689','YZ120211113','ZAH123456',1,1,'寮犲畨鑸?,NULL,'13000025493',0,1,'2013-02-25 00:00:00','2026-04-20 16:14:58',0),(690,'seed-owner-690','seed-union-690','YZ120211221','ZBX123456',1,1,'鏈卞崥鐞?,NULL,'13000025530',0,1,'2021-02-20 00:00:00','2026-04-20 16:14:58',0),(691,'seed-owner-691','seed-union-691','YZ120211322','CNC123456',1,1,'闄堝畞鏅?,NULL,'13000025567',0,1,'2022-01-31 00:00:00','2026-04-20 16:14:58',0),(692,'seed-owner-692','seed-union-692','YZ120211518','XZN123456',1,1,'璁稿瓙瀹?,NULL,'13000025604',0,1,'2018-10-04 00:00:00','2026-04-20 16:14:58',0),(693,'seed-owner-693','seed-union-693','YZ120211626','JJX123456',1,1,'钂嬪槈杞?,NULL,'13000025641',0,1,'2026-12-12 00:00:00','2026-04-20 16:14:58',0),(694,'seed-owner-694','seed-union-694','YZ120211823','SCX123456',1,1,'鏂借景钀?,NULL,'13000025678',0,1,'2023-08-14 00:00:00','2026-04-20 16:14:58',0),(695,'seed-owner-695','seed-union-695','YZ120212011','FYX123456',1,1,'鍐洦濠?,NULL,'13000025715',0,1,'2011-09-25 00:00:00','2026-04-20 16:14:58',0),(696,'seed-owner-696','seed-union-696','YZ120212611','HWH123456',1,1,'闊╂枃娑?,NULL,'13000025752',0,1,'2011-06-25 00:00:00','2026-04-20 16:14:58',0),(697,'seed-owner-697','seed-union-697','YZ120212813','ZSX123456',1,1,'璧佃瘲鐞?,NULL,'13000025789',0,1,'2013-04-24 00:00:00','2026-04-20 16:14:58',0),(698,'seed-owner-698','seed-union-698','YZ120212923','QCC123456',1,1,'绉﹁瘹鏅?,NULL,'13000025826',0,1,'2023-07-08 00:00:00','2026-04-20 16:14:58',0),(699,'seed-owner-699','seed-union-699','YZ120213009','XYF123456',1,1,'瑜氬畤宄?,NULL,'13000025863',0,1,'2009-10-21 00:00:00','2026-04-20 16:14:58',0),(700,'seed-owner-700','seed-union-700','YZ120213123','LMN123456',1,1,'鏉庢槑瀹?,NULL,'13000025900',0,1,'2023-04-23 00:00:00','2026-04-20 16:14:58',0),(701,'seed-owner-701','seed-union-701','YZ120213317','SXC123456',1,1,'娌堝杈?,NULL,'13000025937',0,1,'2017-10-25 00:00:00','2026-04-20 16:14:58',0),(702,'seed-owner-702','seed-union-702','YZ120213615','ZRX123456',1,1,'鏈辫嫢濠?,NULL,'13000025974',0,1,'2015-10-08 00:00:00','2026-04-20 16:14:58',0),(703,'seed-owner-703','seed-union-703','YZ120213709','CAR123456',1,1,'闄堝畨鐒?,NULL,'13000026011',0,1,'2009-08-17 00:00:00','2026-04-20 16:14:58',0),(704,'seed-owner-704','seed-union-704','YZ120214610','YYF123456',1,1,'灏ら洦宄?,NULL,'13000026048',0,1,'2010-10-14 00:00:00','2026-04-20 16:14:58',0),(705,'seed-owner-705','seed-union-705','YZ120214712','WSN123456',1,1,'鍗€濆畞',NULL,'13000026085',0,1,'2012-04-27 00:00:00','2026-04-20 16:14:58',0),(706,'seed-owner-706','seed-union-706','YZ120215016','HXX123456',1,1,'闊╁钀?,NULL,'13000026122',0,1,'2016-02-25 00:00:00','2026-04-20 16:14:58',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_property`
--

DROP TABLE IF EXISTS `user_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_property` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `property_id` bigint NOT NULL,
  `relation` varchar(10) NOT NULL DEFAULT 'family',
  `is_primary` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_property` (`user_id`,`property_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_property_id` (`property_id`),
  KEY `idx_user_primary_deleted` (`user_id`,`is_primary`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=2690 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_property`
--

LOCK TABLES `user_property` WRITE;
/*!40000 ALTER TABLE `user_property` DISABLE KEYS */;
INSERT INTO `user_property` VALUES (2001,1,1001,'self',1,'2026-03-09 12:08:52','2026-03-09 12:08:52',0),(2002,2,1002,'self',1,'2026-03-29 12:08:52','2026-03-29 12:08:52',0),(2003,3,1003,'family',0,'2026-04-13 12:08:52','2026-04-13 12:08:52',0),(2004,21,1004,'self',1,'2016-07-07 00:00:00','2016-07-07 00:00:00',0),(2005,22,1005,'self',1,'2017-05-13 00:00:00','2017-05-13 00:00:00',0),(2006,23,1007,'self',1,'2020-07-04 00:00:00','2020-07-04 00:00:00',0),(2007,24,1009,'self',1,'2021-05-13 00:00:00','2021-05-13 00:00:00',0),(2008,25,1010,'self',1,'2009-10-24 00:00:00','2009-10-24 00:00:00',0),(2009,26,1013,'self',1,'2014-10-14 00:00:00','2014-10-14 00:00:00',0),(2010,27,1015,'self',1,'2026-10-18 00:00:00','2026-10-18 00:00:00',0),(2011,28,1016,'self',1,'2011-02-01 00:00:00','2011-02-01 00:00:00',0),(2012,29,1018,'self',1,'2016-03-19 00:00:00','2016-03-19 00:00:00',0),(2013,30,1022,'self',1,'2011-03-24 00:00:00','2011-03-24 00:00:00',0),(2014,31,1024,'self',1,'2022-05-04 00:00:00','2022-05-04 00:00:00',0),(2015,32,1025,'self',1,'2025-05-10 00:00:00','2025-05-10 00:00:00',0),(2016,33,1026,'self',1,'2021-03-16 00:00:00','2021-03-16 00:00:00',0),(2017,34,1028,'self',1,'2010-02-22 00:00:00','2010-02-22 00:00:00',0),(2018,35,1032,'self',1,'2024-09-29 00:00:00','2024-09-29 00:00:00',0),(2019,36,1033,'self',1,'2012-03-22 00:00:00','2012-03-22 00:00:00',0),(2020,37,1034,'self',1,'2021-08-06 00:00:00','2021-08-06 00:00:00',0),(2021,38,1035,'self',1,'2011-09-24 00:00:00','2011-09-24 00:00:00',0),(2022,39,1036,'self',1,'2013-02-01 00:00:00','2013-02-01 00:00:00',0),(2023,40,1039,'self',1,'2021-02-16 00:00:00','2021-02-16 00:00:00',0),(2024,41,1040,'self',1,'2022-06-25 00:00:00','2022-06-25 00:00:00',0),(2025,42,1042,'self',1,'2012-06-04 00:00:00','2012-06-04 00:00:00',0),(2026,43,1045,'self',1,'2021-07-21 00:00:00','2021-07-21 00:00:00',0),(2027,44,1046,'self',1,'2025-12-19 00:00:00','2025-12-19 00:00:00',0),(2028,45,1049,'self',1,'2018-10-11 00:00:00','2018-10-11 00:00:00',0),(2029,46,1050,'self',1,'2014-10-25 00:00:00','2014-10-25 00:00:00',0),(2030,47,1051,'self',1,'2012-04-14 00:00:00','2012-04-14 00:00:00',0),(2031,48,1055,'self',1,'2022-06-24 00:00:00','2022-06-24 00:00:00',0),(2032,49,1056,'self',1,'2024-09-18 00:00:00','2024-09-18 00:00:00',0),(2033,50,1057,'self',1,'2020-05-21 00:00:00','2020-05-21 00:00:00',0),(2034,51,1058,'self',1,'2009-07-04 00:00:00','2009-07-04 00:00:00',0),(2035,52,1059,'self',1,'2011-12-12 00:00:00','2011-12-12 00:00:00',0),(2036,53,1060,'self',1,'2014-12-06 00:00:00','2014-12-06 00:00:00',0),(2037,54,1061,'self',1,'2025-10-10 00:00:00','2025-10-10 00:00:00',0),(2038,55,1062,'self',1,'2020-11-27 00:00:00','2020-11-27 00:00:00',0),(2039,56,1064,'self',1,'2009-10-08 00:00:00','2009-10-08 00:00:00',0),(2040,57,1065,'self',1,'2024-05-28 00:00:00','2024-05-28 00:00:00',0),(2041,58,1066,'self',1,'2014-10-04 00:00:00','2014-10-04 00:00:00',0),(2042,59,1068,'self',1,'2010-01-01 00:00:00','2010-01-01 00:00:00',0),(2043,60,1069,'self',1,'2022-01-05 00:00:00','2022-01-05 00:00:00',0),(2044,61,1070,'self',1,'2014-12-14 00:00:00','2014-12-14 00:00:00',0),(2045,62,1071,'self',1,'2025-10-07 00:00:00','2025-10-07 00:00:00',0),(2046,63,1073,'self',1,'2018-03-09 00:00:00','2018-03-09 00:00:00',0),(2047,64,1076,'self',1,'2014-08-08 00:00:00','2014-08-08 00:00:00',0),(2048,65,1078,'self',1,'2024-03-22 00:00:00','2024-03-22 00:00:00',0),(2049,66,1081,'self',1,'2015-03-27 00:00:00','2015-03-27 00:00:00',0),(2050,67,1083,'self',1,'2021-04-05 00:00:00','2021-04-05 00:00:00',0),(2051,68,1085,'self',1,'2023-04-29 00:00:00','2023-04-29 00:00:00',0),(2052,69,1086,'self',1,'2021-06-04 00:00:00','2021-06-04 00:00:00',0),(2053,70,1087,'self',1,'2022-03-26 00:00:00','2022-03-26 00:00:00',0),(2054,71,1088,'self',1,'2012-01-26 00:00:00','2012-01-26 00:00:00',0),(2055,72,1089,'self',1,'2023-04-12 00:00:00','2023-04-12 00:00:00',0),(2056,73,1094,'self',1,'2020-05-07 00:00:00','2020-05-07 00:00:00',0),(2057,74,1096,'self',1,'2023-09-18 00:00:00','2023-09-18 00:00:00',0),(2058,75,1097,'self',1,'2009-06-02 00:00:00','2009-06-02 00:00:00',0),(2059,76,1098,'self',1,'2023-03-04 00:00:00','2023-03-04 00:00:00',0),(2060,77,1099,'self',1,'2015-05-06 00:00:00','2015-05-06 00:00:00',0),(2061,78,1100,'self',1,'2025-10-04 00:00:00','2025-10-04 00:00:00',0),(2062,79,1102,'self',1,'2017-06-13 00:00:00','2017-06-13 00:00:00',0),(2063,80,1104,'self',1,'2009-11-28 00:00:00','2009-11-28 00:00:00',0),(2064,81,1106,'self',1,'2026-03-28 00:00:00','2026-03-28 00:00:00',0),(2065,82,1107,'self',1,'2016-01-29 00:00:00','2016-01-29 00:00:00',0),(2066,83,1108,'self',1,'2019-08-31 00:00:00','2019-08-31 00:00:00',0),(2067,84,1111,'self',1,'2021-06-08 00:00:00','2021-06-08 00:00:00',0),(2068,85,1114,'self',1,'2019-04-09 00:00:00','2019-04-09 00:00:00',0),(2069,86,1115,'self',1,'2012-04-10 00:00:00','2012-04-10 00:00:00',0),(2070,87,1116,'self',1,'2011-08-04 00:00:00','2011-08-04 00:00:00',0),(2071,88,1117,'self',1,'2019-10-29 00:00:00','2019-10-29 00:00:00',0),(2072,89,1119,'self',1,'2021-04-02 00:00:00','2021-04-02 00:00:00',0),(2073,90,1120,'self',1,'2016-04-13 00:00:00','2016-04-13 00:00:00',0),(2074,91,1121,'self',1,'2026-08-31 00:00:00','2026-08-31 00:00:00',0),(2075,92,1122,'self',1,'2019-07-20 00:00:00','2019-07-20 00:00:00',0),(2076,93,1123,'self',1,'2016-01-11 00:00:00','2016-01-11 00:00:00',0),(2077,94,1129,'self',1,'2013-10-11 00:00:00','2013-10-11 00:00:00',0),(2078,95,1130,'self',1,'2022-01-07 00:00:00','2022-01-07 00:00:00',0),(2079,96,1132,'self',1,'2022-02-15 00:00:00','2022-02-15 00:00:00',0),(2080,97,1133,'self',1,'2021-03-29 00:00:00','2021-03-29 00:00:00',0),(2081,98,1134,'self',1,'2021-12-21 00:00:00','2021-12-21 00:00:00',0),(2082,99,1135,'self',1,'2014-02-21 00:00:00','2014-02-21 00:00:00',0),(2083,100,1136,'self',1,'2016-02-22 00:00:00','2016-02-22 00:00:00',0),(2084,101,1142,'self',1,'2012-04-19 00:00:00','2012-04-19 00:00:00',0),(2085,102,1143,'self',1,'2015-02-03 00:00:00','2015-02-03 00:00:00',0),(2086,103,1146,'self',1,'2024-06-19 00:00:00','2024-06-19 00:00:00',0),(2087,104,1150,'self',1,'2017-08-17 00:00:00','2017-08-17 00:00:00',0),(2088,105,1151,'self',1,'2011-01-16 00:00:00','2011-01-16 00:00:00',0),(2089,106,1154,'self',1,'2014-05-19 00:00:00','2014-05-19 00:00:00',0),(2090,107,1155,'self',1,'2016-04-12 00:00:00','2016-04-12 00:00:00',0),(2091,108,1156,'self',1,'2014-02-20 00:00:00','2014-02-20 00:00:00',0),(2092,109,1157,'self',1,'2025-06-04 00:00:00','2025-06-04 00:00:00',0),(2093,110,1159,'self',1,'2017-09-13 00:00:00','2017-09-13 00:00:00',0),(2094,111,1160,'self',1,'2024-07-09 00:00:00','2024-07-09 00:00:00',0),(2095,112,1161,'self',1,'2025-09-30 00:00:00','2025-09-30 00:00:00',0),(2096,113,1162,'self',1,'2011-10-06 00:00:00','2011-10-06 00:00:00',0),(2097,114,1164,'self',1,'2014-12-30 00:00:00','2014-12-30 00:00:00',0),(2098,115,1165,'self',1,'2016-09-24 00:00:00','2016-09-24 00:00:00',0),(2099,116,1166,'self',1,'2015-08-14 00:00:00','2015-08-14 00:00:00',0),(2100,117,1167,'self',1,'2023-08-30 00:00:00','2023-08-30 00:00:00',0),(2101,118,1168,'self',1,'2013-03-20 00:00:00','2013-03-20 00:00:00',0),(2102,119,1169,'self',1,'2022-06-02 00:00:00','2022-06-02 00:00:00',0),(2103,120,1170,'self',1,'2014-07-16 00:00:00','2014-07-16 00:00:00',0),(2104,121,1171,'self',1,'2017-10-21 00:00:00','2017-10-21 00:00:00',0),(2105,122,1172,'self',1,'2023-09-21 00:00:00','2023-09-21 00:00:00',0),(2106,123,1174,'self',1,'2022-02-01 00:00:00','2022-02-01 00:00:00',0),(2107,124,1176,'self',1,'2021-01-30 00:00:00','2021-01-30 00:00:00',0),(2108,125,1177,'self',1,'2018-09-15 00:00:00','2018-09-15 00:00:00',0),(2109,126,1178,'self',1,'2009-03-02 00:00:00','2009-03-02 00:00:00',0),(2110,127,1184,'self',1,'2009-08-03 00:00:00','2009-08-03 00:00:00',0),(2111,128,1185,'self',1,'2009-07-06 00:00:00','2009-07-06 00:00:00',0),(2112,129,1187,'self',1,'2010-02-12 00:00:00','2010-02-12 00:00:00',0),(2113,130,1188,'self',1,'2019-01-24 00:00:00','2019-01-24 00:00:00',0),(2114,131,1190,'self',1,'2015-04-24 00:00:00','2015-04-24 00:00:00',0),(2115,132,1196,'self',1,'2020-12-27 00:00:00','2020-12-27 00:00:00',0),(2116,133,1197,'self',1,'2011-08-09 00:00:00','2011-08-09 00:00:00',0),(2117,134,1198,'self',1,'2019-08-11 00:00:00','2019-08-11 00:00:00',0),(2118,135,1199,'self',1,'2013-04-16 00:00:00','2013-04-16 00:00:00',0),(2119,136,1200,'self',1,'2011-09-14 00:00:00','2011-09-14 00:00:00',0),(2120,137,1201,'self',1,'2019-04-29 00:00:00','2019-04-29 00:00:00',0),(2121,138,1202,'self',1,'2023-12-30 00:00:00','2023-12-30 00:00:00',0),(2122,139,1204,'self',1,'2013-10-01 00:00:00','2013-10-01 00:00:00',0),(2123,140,1205,'self',1,'2023-05-25 00:00:00','2023-05-25 00:00:00',0),(2124,141,1206,'self',1,'2024-05-14 00:00:00','2024-05-14 00:00:00',0),(2125,142,1210,'self',1,'2019-07-20 00:00:00','2019-07-20 00:00:00',0),(2126,143,1213,'self',1,'2019-02-05 00:00:00','2019-02-05 00:00:00',0),(2127,144,1214,'self',1,'2019-08-11 00:00:00','2019-08-11 00:00:00',0),(2128,145,1215,'self',1,'2009-02-11 00:00:00','2009-02-11 00:00:00',0),(2129,146,1217,'self',1,'2018-09-02 00:00:00','2018-09-02 00:00:00',0),(2130,147,1218,'self',1,'2024-10-14 00:00:00','2024-10-14 00:00:00',0),(2131,148,1220,'self',1,'2009-09-27 00:00:00','2009-09-27 00:00:00',0),(2132,149,1221,'self',1,'2017-11-09 00:00:00','2017-11-09 00:00:00',0),(2133,150,1222,'self',1,'2013-08-17 00:00:00','2013-08-17 00:00:00',0),(2134,151,1223,'self',1,'2013-12-17 00:00:00','2013-12-17 00:00:00',0),(2135,152,1224,'self',1,'2010-10-30 00:00:00','2010-10-30 00:00:00',0),(2136,153,1225,'self',1,'2021-04-16 00:00:00','2021-04-16 00:00:00',0),(2137,154,1227,'self',1,'2026-10-19 00:00:00','2026-10-19 00:00:00',0),(2138,155,1228,'self',1,'2015-02-05 00:00:00','2015-02-05 00:00:00',0),(2139,156,1232,'self',1,'2020-06-17 00:00:00','2020-06-17 00:00:00',0),(2140,157,1235,'self',1,'2015-12-13 00:00:00','2015-12-13 00:00:00',0),(2141,158,1236,'self',1,'2026-12-24 00:00:00','2026-12-24 00:00:00',0),(2142,159,1238,'self',1,'2025-07-28 00:00:00','2025-07-28 00:00:00',0),(2143,160,1240,'self',1,'2011-05-26 00:00:00','2011-05-26 00:00:00',0),(2144,161,1243,'self',1,'2015-09-08 00:00:00','2015-09-08 00:00:00',0),(2145,162,1248,'self',1,'2014-02-02 00:00:00','2014-02-02 00:00:00',0),(2146,163,1250,'self',1,'2012-05-03 00:00:00','2012-05-03 00:00:00',0),(2147,164,1253,'self',1,'2024-03-04 00:00:00','2024-03-04 00:00:00',0),(2148,165,1257,'self',1,'2009-07-04 00:00:00','2009-07-04 00:00:00',0),(2149,166,1258,'self',1,'2021-10-08 00:00:00','2021-10-08 00:00:00',0),(2150,167,1260,'self',1,'2014-12-27 00:00:00','2014-12-27 00:00:00',0),(2151,168,1261,'self',1,'2023-01-16 00:00:00','2023-01-16 00:00:00',0),(2152,169,1262,'self',1,'2016-09-18 00:00:00','2016-09-18 00:00:00',0),(2153,170,1263,'self',1,'2013-01-03 00:00:00','2013-01-03 00:00:00',0),(2154,171,1265,'self',1,'2019-10-27 00:00:00','2019-10-27 00:00:00',0),(2155,172,1266,'self',1,'2023-05-25 00:00:00','2023-05-25 00:00:00',0),(2156,173,1267,'self',1,'2022-08-05 00:00:00','2022-08-05 00:00:00',0),(2157,174,1269,'self',1,'2024-02-13 00:00:00','2024-02-13 00:00:00',0),(2158,175,1272,'self',1,'2019-09-10 00:00:00','2019-09-10 00:00:00',0),(2159,176,1273,'self',1,'2011-11-20 00:00:00','2011-11-20 00:00:00',0),(2160,177,1274,'self',1,'2023-01-04 00:00:00','2023-01-04 00:00:00',0),(2161,178,1277,'self',1,'2020-03-28 00:00:00','2020-03-28 00:00:00',0),(2162,179,1278,'self',1,'2011-11-29 00:00:00','2011-11-29 00:00:00',0),(2163,180,1279,'self',1,'2010-03-18 00:00:00','2010-03-18 00:00:00',0),(2164,181,1283,'self',1,'2012-05-07 00:00:00','2012-05-07 00:00:00',0),(2165,182,1284,'self',1,'2024-04-27 00:00:00','2024-04-27 00:00:00',0),(2166,183,1286,'self',1,'2024-03-08 00:00:00','2024-03-08 00:00:00',0),(2167,184,1287,'self',1,'2018-05-19 00:00:00','2018-05-19 00:00:00',0),(2168,185,1289,'self',1,'2013-12-05 00:00:00','2013-12-05 00:00:00',0),(2169,186,1294,'self',1,'2025-02-11 00:00:00','2025-02-11 00:00:00',0),(2170,187,1297,'self',1,'2015-12-20 00:00:00','2015-12-20 00:00:00',0),(2171,188,1298,'self',1,'2011-10-16 00:00:00','2011-10-16 00:00:00',0),(2172,189,1299,'self',1,'2025-12-16 00:00:00','2025-12-16 00:00:00',0),(2173,190,1301,'self',1,'2015-08-21 00:00:00','2015-08-21 00:00:00',0),(2174,191,1303,'self',1,'2015-09-20 00:00:00','2015-09-20 00:00:00',0),(2175,192,1305,'self',1,'2014-11-18 00:00:00','2014-11-18 00:00:00',0),(2176,193,1306,'self',1,'2017-07-19 00:00:00','2017-07-19 00:00:00',0),(2177,194,1310,'self',1,'2016-03-11 00:00:00','2016-03-11 00:00:00',0),(2178,195,1311,'self',1,'2018-08-24 00:00:00','2018-08-24 00:00:00',0),(2179,196,1313,'self',1,'2017-07-03 00:00:00','2017-07-03 00:00:00',0),(2180,197,1314,'self',1,'2023-03-22 00:00:00','2023-03-22 00:00:00',0),(2181,198,1316,'self',1,'2016-02-12 00:00:00','2016-02-12 00:00:00',0),(2182,199,1321,'self',1,'2025-05-18 00:00:00','2025-05-18 00:00:00',0),(2183,200,1322,'self',1,'2010-10-15 00:00:00','2010-10-15 00:00:00',0),(2184,201,1324,'self',1,'2020-10-05 00:00:00','2020-10-05 00:00:00',0),(2185,202,1326,'self',1,'2024-12-05 00:00:00','2024-12-05 00:00:00',0),(2186,203,1327,'self',1,'2025-12-16 00:00:00','2025-12-16 00:00:00',0),(2187,204,1331,'self',1,'2023-12-15 00:00:00','2023-12-15 00:00:00',0),(2188,205,1333,'self',1,'2020-11-30 00:00:00','2020-11-30 00:00:00',0),(2189,206,1336,'self',1,'2021-09-27 00:00:00','2021-09-27 00:00:00',0),(2190,207,1337,'self',1,'2014-01-10 00:00:00','2014-01-10 00:00:00',0),(2191,208,1338,'self',1,'2013-02-22 00:00:00','2013-02-22 00:00:00',0),(2192,209,1340,'self',1,'2022-09-14 00:00:00','2022-09-14 00:00:00',0),(2193,210,1343,'self',1,'2016-10-23 00:00:00','2016-10-23 00:00:00',0),(2194,211,1344,'self',1,'2018-10-21 00:00:00','2018-10-21 00:00:00',0),(2195,212,1345,'self',1,'2024-06-11 00:00:00','2024-06-11 00:00:00',0),(2196,213,1347,'self',1,'2021-11-18 00:00:00','2021-11-18 00:00:00',0),(2197,214,1348,'self',1,'2026-09-10 00:00:00','2026-09-10 00:00:00',0),(2198,215,1349,'self',1,'2014-08-02 00:00:00','2014-08-02 00:00:00',0),(2199,216,1350,'self',1,'2013-12-27 00:00:00','2013-12-27 00:00:00',0),(2200,217,1352,'self',1,'2015-09-13 00:00:00','2015-09-13 00:00:00',0),(2201,218,1355,'self',1,'2015-12-30 00:00:00','2015-12-30 00:00:00',0),(2202,219,1356,'self',1,'2009-09-04 00:00:00','2009-09-04 00:00:00',0),(2203,220,1359,'self',1,'2014-11-22 00:00:00','2014-11-22 00:00:00',0),(2204,221,1362,'self',1,'2013-02-24 00:00:00','2013-02-24 00:00:00',0),(2205,222,1363,'self',1,'2016-01-27 00:00:00','2016-01-27 00:00:00',0),(2206,223,1364,'self',1,'2012-08-24 00:00:00','2012-08-24 00:00:00',0),(2207,224,1366,'self',1,'2015-03-19 00:00:00','2015-03-19 00:00:00',0),(2208,225,1367,'self',1,'2017-07-13 00:00:00','2017-07-13 00:00:00',0),(2209,226,1370,'self',1,'2019-05-01 00:00:00','2019-05-01 00:00:00',0),(2210,227,1371,'self',1,'2017-04-23 00:00:00','2017-04-23 00:00:00',0),(2211,228,1372,'self',1,'2016-10-02 00:00:00','2016-10-02 00:00:00',0),(2212,229,1375,'self',1,'2026-06-30 00:00:00','2026-06-30 00:00:00',0),(2213,230,1380,'self',1,'2022-12-25 00:00:00','2022-12-25 00:00:00',0),(2214,231,1381,'self',1,'2018-11-14 00:00:00','2018-11-14 00:00:00',0),(2215,232,1382,'self',1,'2024-12-23 00:00:00','2024-12-23 00:00:00',0),(2216,233,1383,'self',1,'2012-10-28 00:00:00','2012-10-28 00:00:00',0),(2217,234,1387,'self',1,'2025-02-12 00:00:00','2025-02-12 00:00:00',0),(2218,235,1390,'self',1,'2018-11-01 00:00:00','2018-11-01 00:00:00',0),(2219,236,1391,'self',1,'2019-12-28 00:00:00','2019-12-28 00:00:00',0),(2220,237,1395,'self',1,'2018-06-27 00:00:00','2018-06-27 00:00:00',0),(2221,238,1396,'self',1,'2011-07-31 00:00:00','2011-07-31 00:00:00',0),(2222,239,1398,'self',1,'2015-06-09 00:00:00','2015-06-09 00:00:00',0),(2223,240,1399,'self',1,'2017-03-13 00:00:00','2017-03-13 00:00:00',0),(2224,241,1400,'self',1,'2022-11-30 00:00:00','2022-11-30 00:00:00',0),(2225,242,1401,'self',1,'2018-11-14 00:00:00','2018-11-14 00:00:00',0),(2226,243,1404,'self',1,'2013-12-08 00:00:00','2013-12-08 00:00:00',0),(2227,244,1405,'self',1,'2013-06-20 00:00:00','2013-06-20 00:00:00',0),(2228,245,1406,'self',1,'2017-03-14 00:00:00','2017-03-14 00:00:00',0),(2229,246,1407,'self',1,'2022-03-20 00:00:00','2022-03-20 00:00:00',0),(2230,247,1408,'self',1,'2016-11-14 00:00:00','2016-11-14 00:00:00',0),(2231,248,1409,'self',1,'2011-11-12 00:00:00','2011-11-12 00:00:00',0),(2232,249,1410,'self',1,'2025-06-25 00:00:00','2025-06-25 00:00:00',0),(2233,250,1411,'self',1,'2026-04-01 00:00:00','2026-04-01 00:00:00',0),(2234,251,1414,'self',1,'2015-04-28 00:00:00','2015-04-28 00:00:00',0),(2235,252,1415,'self',1,'2026-06-10 00:00:00','2026-06-10 00:00:00',0),(2236,253,1416,'self',1,'2009-07-27 00:00:00','2009-07-27 00:00:00',0),(2237,254,1418,'self',1,'2014-08-23 00:00:00','2014-08-23 00:00:00',0),(2238,255,1420,'self',1,'2023-12-29 00:00:00','2023-12-29 00:00:00',0),(2239,256,1421,'self',1,'2017-04-28 00:00:00','2017-04-28 00:00:00',0),(2240,257,1423,'self',1,'2018-12-05 00:00:00','2018-12-05 00:00:00',0),(2241,258,1425,'self',1,'2016-01-14 00:00:00','2016-01-14 00:00:00',0),(2242,259,1426,'self',1,'2026-08-04 00:00:00','2026-08-04 00:00:00',0),(2243,260,1427,'self',1,'2020-03-20 00:00:00','2020-03-20 00:00:00',0),(2244,261,1430,'self',1,'2015-05-26 00:00:00','2015-05-26 00:00:00',0),(2245,262,1431,'self',1,'2013-11-03 00:00:00','2013-11-03 00:00:00',0),(2246,263,1432,'self',1,'2025-06-02 00:00:00','2025-06-02 00:00:00',0),(2247,264,1433,'self',1,'2021-10-27 00:00:00','2021-10-27 00:00:00',0),(2248,265,1436,'self',1,'2021-07-10 00:00:00','2021-07-10 00:00:00',0),(2249,266,1438,'self',1,'2025-07-11 00:00:00','2025-07-11 00:00:00',0),(2250,267,1439,'self',1,'2018-05-09 00:00:00','2018-05-09 00:00:00',0),(2251,268,1440,'self',1,'2022-12-21 00:00:00','2022-12-21 00:00:00',0),(2252,269,1442,'self',1,'2009-08-14 00:00:00','2009-08-14 00:00:00',0),(2253,270,1444,'self',1,'2016-11-20 00:00:00','2016-11-20 00:00:00',0),(2254,271,1449,'self',1,'2010-01-16 00:00:00','2010-01-16 00:00:00',0),(2255,272,1453,'self',1,'2011-01-03 00:00:00','2011-01-03 00:00:00',0),(2256,273,1454,'self',1,'2016-06-19 00:00:00','2016-06-19 00:00:00',0),(2257,274,1455,'self',1,'2012-02-15 00:00:00','2012-02-15 00:00:00',0),(2258,275,1458,'self',1,'2021-04-14 00:00:00','2021-04-14 00:00:00',0),(2259,276,1459,'self',1,'2011-03-17 00:00:00','2011-03-17 00:00:00',0),(2260,277,1461,'self',1,'2023-09-02 00:00:00','2023-09-02 00:00:00',0),(2261,278,1462,'self',1,'2009-06-10 00:00:00','2009-06-10 00:00:00',0),(2262,279,1463,'self',1,'2026-02-17 00:00:00','2026-02-17 00:00:00',0),(2263,280,1467,'self',1,'2016-02-05 00:00:00','2016-02-05 00:00:00',0),(2264,281,1468,'self',1,'2021-05-31 00:00:00','2021-05-31 00:00:00',0),(2265,282,1469,'self',1,'2018-06-28 00:00:00','2018-06-28 00:00:00',0),(2266,283,1470,'self',1,'2018-11-05 00:00:00','2018-11-05 00:00:00',0),(2267,284,1473,'self',1,'2022-12-29 00:00:00','2022-12-29 00:00:00',0),(2268,285,1477,'self',1,'2023-02-28 00:00:00','2023-02-28 00:00:00',0),(2269,286,1478,'self',1,'2010-04-08 00:00:00','2010-04-08 00:00:00',0),(2270,287,1480,'self',1,'2009-06-21 00:00:00','2009-06-21 00:00:00',0),(2271,288,1482,'self',1,'2021-07-21 00:00:00','2021-07-21 00:00:00',0),(2272,289,1483,'self',1,'2010-10-24 00:00:00','2010-10-24 00:00:00',0),(2273,290,1484,'self',1,'2013-01-26 00:00:00','2013-01-26 00:00:00',0),(2274,291,1486,'self',1,'2020-03-29 00:00:00','2020-03-29 00:00:00',0),(2275,292,1487,'self',1,'2025-08-07 00:00:00','2025-08-07 00:00:00',0),(2276,293,1489,'self',1,'2013-11-10 00:00:00','2013-11-10 00:00:00',0),(2277,294,1490,'self',1,'2025-02-22 00:00:00','2025-02-22 00:00:00',0),(2278,295,1491,'self',1,'2017-04-28 00:00:00','2017-04-28 00:00:00',0),(2279,296,1493,'self',1,'2026-08-16 00:00:00','2026-08-16 00:00:00',0),(2280,297,1494,'self',1,'2019-07-11 00:00:00','2019-07-11 00:00:00',0),(2281,298,1495,'self',1,'2015-02-05 00:00:00','2015-02-05 00:00:00',0),(2282,299,1496,'self',1,'2014-07-19 00:00:00','2014-07-19 00:00:00',0),(2283,300,1497,'self',1,'2018-01-03 00:00:00','2018-01-03 00:00:00',0),(2284,301,1498,'self',1,'2021-09-23 00:00:00','2021-09-23 00:00:00',0),(2285,302,1499,'self',1,'2010-01-30 00:00:00','2010-01-30 00:00:00',0),(2286,303,1500,'self',1,'2021-10-24 00:00:00','2021-10-24 00:00:00',0),(2287,304,1501,'self',1,'2014-10-29 00:00:00','2014-10-29 00:00:00',0),(2288,305,1506,'self',1,'2023-09-25 00:00:00','2023-09-25 00:00:00',0),(2289,306,1507,'self',1,'2023-02-25 00:00:00','2023-02-25 00:00:00',0),(2290,307,1508,'self',1,'2023-05-19 00:00:00','2023-05-19 00:00:00',0),(2291,308,1510,'self',1,'2019-10-14 00:00:00','2019-10-14 00:00:00',0),(2292,309,1511,'self',1,'2020-09-16 00:00:00','2020-09-16 00:00:00',0),(2293,310,1514,'self',1,'2013-11-06 00:00:00','2013-11-06 00:00:00',0),(2294,311,1515,'self',1,'2024-06-30 00:00:00','2024-06-30 00:00:00',0),(2295,312,1516,'self',1,'2019-10-02 00:00:00','2019-10-02 00:00:00',0),(2296,313,1518,'self',1,'2023-10-19 00:00:00','2023-10-19 00:00:00',0),(2297,314,1519,'self',1,'2014-01-03 00:00:00','2014-01-03 00:00:00',0),(2298,315,1524,'self',1,'2023-01-22 00:00:00','2023-01-22 00:00:00',0),(2299,316,1525,'self',1,'2018-09-03 00:00:00','2018-09-03 00:00:00',0),(2300,317,1528,'self',1,'2016-07-02 00:00:00','2016-07-02 00:00:00',0),(2301,318,1531,'self',1,'2020-03-13 00:00:00','2020-03-13 00:00:00',0),(2302,319,1534,'self',1,'2012-05-19 00:00:00','2012-05-19 00:00:00',0),(2303,320,1535,'self',1,'2009-09-04 00:00:00','2009-09-04 00:00:00',0),(2304,321,1536,'self',1,'2025-09-20 00:00:00','2025-09-20 00:00:00',0),(2305,322,1537,'self',1,'2014-05-01 00:00:00','2014-05-01 00:00:00',0),(2306,323,1539,'self',1,'2024-11-30 00:00:00','2024-11-30 00:00:00',0),(2307,324,1542,'self',1,'2016-02-11 00:00:00','2016-02-11 00:00:00',0),(2308,325,1544,'self',1,'2020-01-24 00:00:00','2020-01-24 00:00:00',0),(2309,326,1545,'self',1,'2015-12-22 00:00:00','2015-12-22 00:00:00',0),(2310,327,1548,'self',1,'2016-01-30 00:00:00','2016-01-30 00:00:00',0),(2311,328,1549,'self',1,'2010-05-15 00:00:00','2010-05-15 00:00:00',0),(2312,329,1550,'self',1,'2017-03-12 00:00:00','2017-03-12 00:00:00',0),(2313,330,1553,'self',1,'2014-09-06 00:00:00','2014-09-06 00:00:00',0),(2314,331,1556,'self',1,'2022-07-31 00:00:00','2022-07-31 00:00:00',0),(2315,332,1558,'self',1,'2022-12-04 00:00:00','2022-12-04 00:00:00',0),(2316,333,1559,'self',1,'2025-01-06 00:00:00','2025-01-06 00:00:00',0),(2317,334,1560,'self',1,'2014-04-13 00:00:00','2014-04-13 00:00:00',0),(2318,335,1564,'self',1,'2019-08-21 00:00:00','2019-08-21 00:00:00',0),(2319,336,1567,'self',1,'2023-09-14 00:00:00','2023-09-14 00:00:00',0),(2320,337,1569,'self',1,'2019-10-20 00:00:00','2019-10-20 00:00:00',0),(2321,338,1570,'self',1,'2025-11-22 00:00:00','2025-11-22 00:00:00',0),(2322,339,1573,'self',1,'2013-12-05 00:00:00','2013-12-05 00:00:00',0),(2323,340,1574,'self',1,'2021-06-06 00:00:00','2021-06-06 00:00:00',0),(2324,341,1575,'self',1,'2024-05-07 00:00:00','2024-05-07 00:00:00',0),(2325,342,1576,'self',1,'2019-04-16 00:00:00','2019-04-16 00:00:00',0),(2326,343,1582,'self',1,'2009-11-23 00:00:00','2009-11-23 00:00:00',0),(2327,344,1583,'self',1,'2011-09-20 00:00:00','2011-09-20 00:00:00',0),(2328,345,1584,'self',1,'2022-03-29 00:00:00','2022-03-29 00:00:00',0),(2329,346,1586,'self',1,'2018-06-06 00:00:00','2018-06-06 00:00:00',0),(2330,347,1587,'self',1,'2012-06-02 00:00:00','2012-06-02 00:00:00',0),(2331,348,1589,'self',1,'2020-07-06 00:00:00','2020-07-06 00:00:00',0),(2332,349,1590,'self',1,'2025-01-01 00:00:00','2025-01-01 00:00:00',0),(2333,350,1592,'self',1,'2020-07-11 00:00:00','2020-07-11 00:00:00',0),(2334,351,1595,'self',1,'2020-12-10 00:00:00','2020-12-10 00:00:00',0),(2335,352,1598,'self',1,'2025-04-19 00:00:00','2025-04-19 00:00:00',0),(2336,353,1599,'self',1,'2017-11-20 00:00:00','2017-11-20 00:00:00',0),(2337,354,1600,'self',1,'2009-11-11 00:00:00','2009-11-11 00:00:00',0),(2338,355,1601,'self',1,'2018-01-04 00:00:00','2018-01-04 00:00:00',0),(2339,356,1602,'self',1,'2022-07-26 00:00:00','2022-07-26 00:00:00',0),(2340,357,1603,'self',1,'2023-01-28 00:00:00','2023-01-28 00:00:00',0),(2341,358,1605,'self',1,'2014-05-14 00:00:00','2014-05-14 00:00:00',0),(2342,359,1607,'self',1,'2019-09-30 00:00:00','2019-09-30 00:00:00',0),(2343,360,1608,'self',1,'2016-11-28 00:00:00','2016-11-28 00:00:00',0),(2344,361,1610,'self',1,'2013-08-31 00:00:00','2013-08-31 00:00:00',0),(2345,362,1612,'self',1,'2013-05-17 00:00:00','2013-05-17 00:00:00',0),(2346,363,1613,'self',1,'2009-06-03 00:00:00','2009-06-03 00:00:00',0),(2347,364,1614,'self',1,'2014-08-14 00:00:00','2014-08-14 00:00:00',0),(2348,365,1616,'self',1,'2020-04-15 00:00:00','2020-04-15 00:00:00',0),(2349,366,1620,'self',1,'2026-12-26 00:00:00','2026-12-26 00:00:00',0),(2350,367,1621,'self',1,'2020-06-30 00:00:00','2020-06-30 00:00:00',0),(2351,368,1622,'self',1,'2010-12-09 00:00:00','2010-12-09 00:00:00',0),(2352,369,1623,'self',1,'2015-08-06 00:00:00','2015-08-06 00:00:00',0),(2353,370,1624,'self',1,'2016-04-02 00:00:00','2016-04-02 00:00:00',0),(2354,371,1625,'self',1,'2015-02-15 00:00:00','2015-02-15 00:00:00',0),(2355,372,1626,'self',1,'2015-01-19 00:00:00','2015-01-19 00:00:00',0),(2356,373,1627,'self',1,'2021-07-26 00:00:00','2021-07-26 00:00:00',0),(2357,374,1628,'self',1,'2022-11-22 00:00:00','2022-11-22 00:00:00',0),(2358,375,1630,'self',1,'2018-11-22 00:00:00','2018-11-22 00:00:00',0),(2359,376,1631,'self',1,'2024-08-02 00:00:00','2024-08-02 00:00:00',0),(2360,377,1633,'self',1,'2022-07-22 00:00:00','2022-07-22 00:00:00',0),(2361,378,1635,'self',1,'2020-01-11 00:00:00','2020-01-11 00:00:00',0),(2362,379,1636,'self',1,'2019-02-25 00:00:00','2019-02-25 00:00:00',0),(2363,380,1637,'self',1,'2019-04-28 00:00:00','2019-04-28 00:00:00',0),(2364,381,1639,'self',1,'2010-04-22 00:00:00','2010-04-22 00:00:00',0),(2365,382,1642,'self',1,'2012-07-03 00:00:00','2012-07-03 00:00:00',0),(2366,383,1644,'self',1,'2017-09-17 00:00:00','2017-09-17 00:00:00',0),(2367,384,1645,'self',1,'2024-05-06 00:00:00','2024-05-06 00:00:00',0),(2368,385,1646,'self',1,'2011-06-04 00:00:00','2011-06-04 00:00:00',0),(2369,386,1647,'self',1,'2022-05-06 00:00:00','2022-05-06 00:00:00',0),(2370,387,1648,'self',1,'2021-02-26 00:00:00','2021-02-26 00:00:00',0),(2371,388,1649,'self',1,'2009-02-13 00:00:00','2009-02-13 00:00:00',0),(2372,389,1650,'self',1,'2014-06-06 00:00:00','2014-06-06 00:00:00',0),(2373,390,1652,'self',1,'2020-08-21 00:00:00','2020-08-21 00:00:00',0),(2374,391,1654,'self',1,'2013-01-02 00:00:00','2013-01-02 00:00:00',0),(2375,392,1655,'self',1,'2024-04-29 00:00:00','2024-04-29 00:00:00',0),(2376,393,1656,'self',1,'2011-02-05 00:00:00','2011-02-05 00:00:00',0),(2377,394,1658,'self',1,'2009-06-18 00:00:00','2009-06-18 00:00:00',0),(2378,395,1659,'self',1,'2012-12-06 00:00:00','2012-12-06 00:00:00',0),(2379,396,1660,'self',1,'2023-01-30 00:00:00','2023-01-30 00:00:00',0),(2380,397,1661,'self',1,'2021-04-17 00:00:00','2021-04-17 00:00:00',0),(2381,398,1662,'self',1,'2018-03-09 00:00:00','2018-03-09 00:00:00',0),(2382,399,1663,'self',1,'2015-11-08 00:00:00','2015-11-08 00:00:00',0),(2383,400,1667,'self',1,'2022-06-01 00:00:00','2022-06-01 00:00:00',0),(2384,401,1671,'self',1,'2012-07-16 00:00:00','2012-07-16 00:00:00',0),(2385,402,1678,'self',1,'2019-05-28 00:00:00','2019-05-28 00:00:00',0),(2386,403,1680,'self',1,'2022-02-24 00:00:00','2022-02-24 00:00:00',0),(2387,404,1681,'self',1,'2024-01-16 00:00:00','2024-01-16 00:00:00',0),(2388,405,1682,'self',1,'2011-04-30 00:00:00','2011-04-30 00:00:00',0),(2389,406,1683,'self',1,'2022-11-27 00:00:00','2022-11-27 00:00:00',0),(2390,407,1684,'self',1,'2014-07-19 00:00:00','2014-07-19 00:00:00',0),(2391,408,1686,'self',1,'2026-05-08 00:00:00','2026-05-08 00:00:00',0),(2392,409,1688,'self',1,'2025-02-05 00:00:00','2025-02-05 00:00:00',0),(2393,410,1692,'self',1,'2009-02-01 00:00:00','2009-02-01 00:00:00',0),(2394,411,1693,'self',1,'2011-02-15 00:00:00','2011-02-15 00:00:00',0),(2395,412,1694,'self',1,'2014-06-29 00:00:00','2014-06-29 00:00:00',0),(2396,413,1695,'self',1,'2013-01-04 00:00:00','2013-01-04 00:00:00',0),(2397,414,1696,'self',1,'2019-08-07 00:00:00','2019-08-07 00:00:00',0),(2398,415,1697,'self',1,'2010-07-10 00:00:00','2010-07-10 00:00:00',0),(2399,416,1699,'self',1,'2010-04-03 00:00:00','2010-04-03 00:00:00',0),(2400,417,1700,'self',1,'2011-11-08 00:00:00','2011-11-08 00:00:00',0),(2401,418,1701,'self',1,'2023-04-01 00:00:00','2023-04-01 00:00:00',0),(2402,419,1705,'self',1,'2019-05-31 00:00:00','2019-05-31 00:00:00',0),(2403,420,1707,'self',1,'2014-02-04 00:00:00','2014-02-04 00:00:00',0),(2404,421,1709,'self',1,'2014-05-14 00:00:00','2014-05-14 00:00:00',0),(2405,422,1711,'self',1,'2011-08-31 00:00:00','2011-08-31 00:00:00',0),(2406,423,1712,'self',1,'2016-05-25 00:00:00','2016-05-25 00:00:00',0),(2407,424,1713,'self',1,'2024-09-28 00:00:00','2024-09-28 00:00:00',0),(2408,425,1714,'self',1,'2024-09-20 00:00:00','2024-09-20 00:00:00',0),(2409,426,1715,'self',1,'2013-07-16 00:00:00','2013-07-16 00:00:00',0),(2410,427,1716,'self',1,'2019-08-25 00:00:00','2019-08-25 00:00:00',0),(2411,428,1717,'self',1,'2018-02-16 00:00:00','2018-02-16 00:00:00',0),(2412,429,1719,'self',1,'2020-01-27 00:00:00','2020-01-27 00:00:00',0),(2413,430,1720,'self',1,'2013-05-08 00:00:00','2013-05-08 00:00:00',0),(2414,431,1722,'self',1,'2012-09-03 00:00:00','2012-09-03 00:00:00',0),(2415,432,1725,'self',1,'2010-08-10 00:00:00','2010-08-10 00:00:00',0),(2416,433,1728,'self',1,'2016-10-14 00:00:00','2016-10-14 00:00:00',0),(2417,434,1729,'self',1,'2026-10-23 00:00:00','2026-10-23 00:00:00',0),(2418,435,1731,'self',1,'2016-11-10 00:00:00','2016-11-10 00:00:00',0),(2419,436,1732,'self',1,'2010-01-30 00:00:00','2010-01-30 00:00:00',0),(2420,437,1733,'self',1,'2009-01-31 00:00:00','2009-01-31 00:00:00',0),(2421,438,1735,'self',1,'2022-01-14 00:00:00','2022-01-14 00:00:00',0),(2422,439,1736,'self',1,'2022-06-26 00:00:00','2022-06-26 00:00:00',0),(2423,440,1737,'self',1,'2015-09-24 00:00:00','2015-09-24 00:00:00',0),(2424,441,1738,'self',1,'2021-04-08 00:00:00','2021-04-08 00:00:00',0),(2425,442,1739,'self',1,'2016-11-03 00:00:00','2016-11-03 00:00:00',0),(2426,443,1743,'self',1,'2025-09-10 00:00:00','2025-09-10 00:00:00',0),(2427,444,1744,'self',1,'2017-10-31 00:00:00','2017-10-31 00:00:00',0),(2428,445,1746,'self',1,'2025-07-27 00:00:00','2025-07-27 00:00:00',0),(2429,446,1748,'self',1,'2020-05-22 00:00:00','2020-05-22 00:00:00',0),(2430,447,1750,'self',1,'2021-01-07 00:00:00','2021-01-07 00:00:00',0),(2431,448,1752,'self',1,'2021-02-22 00:00:00','2021-02-22 00:00:00',0),(2432,449,1754,'self',1,'2019-07-08 00:00:00','2019-07-08 00:00:00',0),(2433,450,1757,'self',1,'2020-06-18 00:00:00','2020-06-18 00:00:00',0),(2434,451,1758,'self',1,'2024-01-22 00:00:00','2024-01-22 00:00:00',0),(2435,452,1760,'self',1,'2024-01-16 00:00:00','2024-01-16 00:00:00',0),(2436,453,1762,'self',1,'2023-03-27 00:00:00','2023-03-27 00:00:00',0),(2437,454,1763,'self',1,'2022-11-02 00:00:00','2022-11-02 00:00:00',0),(2438,455,1764,'self',1,'2011-07-12 00:00:00','2011-07-12 00:00:00',0),(2439,456,1765,'self',1,'2017-06-04 00:00:00','2017-06-04 00:00:00',0),(2440,457,1766,'self',1,'2020-08-31 00:00:00','2020-08-31 00:00:00',0),(2441,458,1768,'self',1,'2014-11-21 00:00:00','2014-11-21 00:00:00',0),(2442,459,1769,'self',1,'2017-11-22 00:00:00','2017-11-22 00:00:00',0),(2443,460,1770,'self',1,'2021-05-02 00:00:00','2021-05-02 00:00:00',0),(2444,461,1772,'self',1,'2011-07-03 00:00:00','2011-07-03 00:00:00',0),(2445,462,1773,'self',1,'2010-06-15 00:00:00','2010-06-15 00:00:00',0),(2446,463,1775,'self',1,'2024-02-26 00:00:00','2024-02-26 00:00:00',0),(2447,464,1779,'self',1,'2018-09-05 00:00:00','2018-09-05 00:00:00',0),(2448,465,1780,'self',1,'2020-01-12 00:00:00','2020-01-12 00:00:00',0),(2449,466,1781,'self',1,'2018-03-07 00:00:00','2018-03-07 00:00:00',0),(2450,467,1784,'self',1,'2018-08-05 00:00:00','2018-08-05 00:00:00',0),(2451,468,1787,'self',1,'2018-06-23 00:00:00','2018-06-23 00:00:00',0),(2452,469,1789,'self',1,'2014-09-06 00:00:00','2014-09-06 00:00:00',0),(2453,470,1790,'self',1,'2022-09-12 00:00:00','2022-09-12 00:00:00',0),(2454,471,1791,'self',1,'2015-05-09 00:00:00','2015-05-09 00:00:00',0),(2455,472,1792,'self',1,'2021-04-17 00:00:00','2021-04-17 00:00:00',0),(2456,473,1794,'self',1,'2025-01-24 00:00:00','2025-01-24 00:00:00',0),(2457,474,1795,'self',1,'2019-09-17 00:00:00','2019-09-17 00:00:00',0),(2458,475,1797,'self',1,'2015-08-01 00:00:00','2015-08-01 00:00:00',0),(2459,476,1798,'self',1,'2019-11-27 00:00:00','2019-11-27 00:00:00',0),(2460,477,1801,'self',1,'2010-10-15 00:00:00','2010-10-15 00:00:00',0),(2461,478,1802,'self',1,'2010-03-23 00:00:00','2010-03-23 00:00:00',0),(2462,479,1804,'self',1,'2013-12-20 00:00:00','2013-12-20 00:00:00',0),(2463,480,1808,'self',1,'2011-06-29 00:00:00','2011-06-29 00:00:00',0),(2464,481,1810,'self',1,'2011-08-10 00:00:00','2011-08-10 00:00:00',0),(2465,482,1812,'self',1,'2021-07-18 00:00:00','2021-07-18 00:00:00',0),(2466,483,1816,'self',1,'2016-03-30 00:00:00','2016-03-30 00:00:00',0),(2467,484,1817,'self',1,'2022-09-01 00:00:00','2022-09-01 00:00:00',0),(2468,485,1818,'self',1,'2024-09-17 00:00:00','2024-09-17 00:00:00',0),(2469,486,1819,'self',1,'2015-02-02 00:00:00','2015-02-02 00:00:00',0),(2470,487,1820,'self',1,'2017-11-17 00:00:00','2017-11-17 00:00:00',0),(2471,488,1821,'self',1,'2014-10-02 00:00:00','2014-10-02 00:00:00',0),(2472,489,1822,'self',1,'2013-06-08 00:00:00','2013-06-08 00:00:00',0),(2473,490,1824,'self',1,'2014-12-30 00:00:00','2014-12-30 00:00:00',0),(2474,491,1826,'self',1,'2022-11-04 00:00:00','2022-11-04 00:00:00',0),(2475,492,1827,'self',1,'2022-01-24 00:00:00','2022-01-24 00:00:00',0),(2476,493,1828,'self',1,'2014-05-29 00:00:00','2014-05-29 00:00:00',0),(2477,494,1829,'self',1,'2016-09-30 00:00:00','2016-09-30 00:00:00',0),(2478,495,1830,'self',1,'2014-10-31 00:00:00','2014-10-31 00:00:00',0),(2479,496,1832,'self',1,'2021-02-04 00:00:00','2021-02-04 00:00:00',0),(2480,497,1834,'self',1,'2022-05-11 00:00:00','2022-05-11 00:00:00',0),(2481,498,1837,'self',1,'2021-01-21 00:00:00','2021-01-21 00:00:00',0),(2482,499,1838,'self',1,'2009-07-26 00:00:00','2009-07-26 00:00:00',0),(2483,500,1839,'self',1,'2016-10-17 00:00:00','2016-10-17 00:00:00',0),(2484,501,1841,'self',1,'2011-08-31 00:00:00','2011-08-31 00:00:00',0),(2485,502,1842,'self',1,'2020-03-21 00:00:00','2020-03-21 00:00:00',0),(2486,503,1843,'self',1,'2024-08-01 00:00:00','2024-08-01 00:00:00',0),(2487,504,1846,'self',1,'2012-09-07 00:00:00','2012-09-07 00:00:00',0),(2488,505,1847,'self',1,'2018-02-23 00:00:00','2018-02-23 00:00:00',0),(2489,506,1848,'self',1,'2011-05-06 00:00:00','2011-05-06 00:00:00',0),(2490,507,1849,'self',1,'2021-06-20 00:00:00','2021-06-20 00:00:00',0),(2491,508,1852,'self',1,'2024-03-05 00:00:00','2024-03-05 00:00:00',0),(2492,509,1853,'self',1,'2018-02-02 00:00:00','2018-02-02 00:00:00',0),(2493,510,1854,'self',1,'2020-04-29 00:00:00','2020-04-29 00:00:00',0),(2494,511,1855,'self',1,'2021-08-20 00:00:00','2021-08-20 00:00:00',0),(2495,512,1856,'self',1,'2012-02-01 00:00:00','2012-02-01 00:00:00',0),(2496,513,1860,'self',1,'2024-05-02 00:00:00','2024-05-02 00:00:00',0),(2497,514,1861,'self',1,'2020-02-09 00:00:00','2020-02-09 00:00:00',0),(2498,515,1863,'self',1,'2016-01-01 00:00:00','2016-01-01 00:00:00',0),(2499,516,1864,'self',1,'2014-07-04 00:00:00','2014-07-04 00:00:00',0),(2500,517,1866,'self',1,'2013-09-05 00:00:00','2013-09-05 00:00:00',0),(2501,518,1867,'self',1,'2020-04-21 00:00:00','2020-04-21 00:00:00',0),(2502,519,1872,'self',1,'2017-06-01 00:00:00','2017-06-01 00:00:00',0),(2503,520,1873,'self',1,'2021-01-22 00:00:00','2021-01-22 00:00:00',0),(2504,521,1875,'self',1,'2021-11-06 00:00:00','2021-11-06 00:00:00',0),(2505,522,1876,'self',1,'2024-02-15 00:00:00','2024-02-15 00:00:00',0),(2506,523,1877,'self',1,'2026-01-30 00:00:00','2026-01-30 00:00:00',0),(2507,524,1880,'self',1,'2010-04-28 00:00:00','2010-04-28 00:00:00',0),(2508,525,1881,'self',1,'2009-01-10 00:00:00','2009-01-10 00:00:00',0),(2509,526,1882,'self',1,'2011-06-03 00:00:00','2011-06-03 00:00:00',0),(2510,527,1886,'self',1,'2024-02-29 00:00:00','2024-02-29 00:00:00',0),(2511,528,1889,'self',1,'2025-09-22 00:00:00','2025-09-22 00:00:00',0),(2512,529,1891,'self',1,'2026-11-28 00:00:00','2026-11-28 00:00:00',0),(2513,530,1892,'self',1,'2024-07-28 00:00:00','2024-07-28 00:00:00',0),(2514,531,1893,'self',1,'2015-05-21 00:00:00','2015-05-21 00:00:00',0),(2515,532,1895,'self',1,'2011-03-07 00:00:00','2011-03-07 00:00:00',0),(2516,533,1897,'self',1,'2013-04-04 00:00:00','2013-04-04 00:00:00',0),(2517,534,1900,'self',1,'2014-09-08 00:00:00','2014-09-08 00:00:00',0),(2518,535,1902,'self',1,'2013-06-02 00:00:00','2013-06-02 00:00:00',0),(2519,536,1903,'self',1,'2024-02-19 00:00:00','2024-02-19 00:00:00',0),(2520,537,1905,'self',1,'2014-05-10 00:00:00','2014-05-10 00:00:00',0),(2521,538,1907,'self',1,'2025-08-21 00:00:00','2025-08-21 00:00:00',0),(2522,539,1908,'self',1,'2010-10-27 00:00:00','2010-10-27 00:00:00',0),(2523,540,1909,'self',1,'2011-04-15 00:00:00','2011-04-15 00:00:00',0),(2524,541,1914,'self',1,'2014-03-03 00:00:00','2014-03-03 00:00:00',0),(2525,542,1915,'self',1,'2019-08-22 00:00:00','2019-08-22 00:00:00',0),(2526,543,1916,'self',1,'2018-07-18 00:00:00','2018-07-18 00:00:00',0),(2527,544,1919,'self',1,'2022-12-16 00:00:00','2022-12-16 00:00:00',0),(2528,545,1920,'self',1,'2009-12-14 00:00:00','2009-12-14 00:00:00',0),(2529,546,1921,'self',1,'2014-04-21 00:00:00','2014-04-21 00:00:00',0),(2530,547,1922,'self',1,'2011-08-03 00:00:00','2011-08-03 00:00:00',0),(2531,548,1923,'self',1,'2018-08-08 00:00:00','2018-08-08 00:00:00',0),(2532,549,1924,'self',1,'2023-03-15 00:00:00','2023-03-15 00:00:00',0),(2533,550,1926,'self',1,'2013-10-22 00:00:00','2013-10-22 00:00:00',0),(2534,551,1929,'self',1,'2010-05-26 00:00:00','2010-05-26 00:00:00',0),(2535,552,1930,'self',1,'2010-01-10 00:00:00','2010-01-10 00:00:00',0),(2536,553,1933,'self',1,'2019-06-11 00:00:00','2019-06-11 00:00:00',0),(2537,554,1934,'self',1,'2023-04-20 00:00:00','2023-04-20 00:00:00',0),(2538,555,1935,'self',1,'2025-01-26 00:00:00','2025-01-26 00:00:00',0),(2539,556,1940,'self',1,'2015-05-08 00:00:00','2015-05-08 00:00:00',0),(2540,557,1941,'self',1,'2021-04-02 00:00:00','2021-04-02 00:00:00',0),(2541,558,1943,'self',1,'2009-12-30 00:00:00','2009-12-30 00:00:00',0),(2542,559,1944,'self',1,'2014-06-21 00:00:00','2026-04-20 09:46:41',1),(2543,560,1945,'self',1,'2023-09-25 00:00:00','2026-04-20 09:46:41',1),(2544,561,1947,'self',1,'2013-04-27 00:00:00','2026-04-20 09:46:41',1),(2545,562,1949,'self',1,'2012-09-01 00:00:00','2026-04-20 09:46:41',1),(2546,563,1950,'self',1,'2016-03-30 00:00:00','2026-04-20 09:46:41',1),(2547,564,1951,'self',1,'2021-07-15 00:00:00','2026-04-20 09:46:41',1),(2548,565,1953,'self',1,'2024-03-10 00:00:00','2026-04-20 09:46:41',1),(2549,566,1954,'self',1,'2021-04-11 00:00:00','2026-04-20 09:46:41',1),(2550,567,1956,'self',1,'2017-02-08 00:00:00','2026-04-20 09:46:41',1),(2551,568,1958,'self',1,'2023-04-05 00:00:00','2026-04-20 09:46:41',1),(2552,569,1960,'self',1,'2019-09-19 00:00:00','2026-04-20 09:46:41',1),(2553,570,1961,'self',1,'2018-04-28 00:00:00','2026-04-20 09:46:41',1),(2554,571,1963,'self',1,'2017-03-21 00:00:00','2026-04-20 09:46:41',1),(2555,572,1964,'self',1,'2025-09-05 00:00:00','2026-04-20 09:46:41',1),(2556,573,1965,'self',1,'2023-07-11 00:00:00','2026-04-20 09:46:41',1),(2557,574,1969,'self',1,'2014-12-28 00:00:00','2026-04-20 09:46:41',1),(2558,575,1971,'self',1,'2011-09-06 00:00:00','2026-04-20 09:46:41',1),(2559,576,1972,'self',1,'2024-09-18 00:00:00','2026-04-20 09:46:41',1),(2560,577,1973,'self',1,'2023-12-12 00:00:00','2026-04-20 09:46:41',1),(2561,578,1974,'self',1,'2024-05-19 00:00:00','2026-04-20 09:46:41',1),(2562,579,1975,'self',1,'2025-11-23 00:00:00','2026-04-20 09:46:41',1),(2563,580,1976,'self',1,'2009-05-01 00:00:00','2026-04-20 09:46:41',1),(2564,581,1977,'self',1,'2011-03-10 00:00:00','2026-04-20 09:46:41',1),(2565,582,1978,'self',1,'2012-12-21 00:00:00','2026-04-20 09:46:41',1),(2566,583,1980,'self',1,'2016-04-17 00:00:00','2026-04-20 09:46:41',1),(2567,584,1981,'self',1,'2018-01-21 00:00:00','2026-04-20 09:46:41',1),(2568,585,1982,'self',1,'2014-07-04 00:00:00','2026-04-20 09:46:41',1),(2569,586,1983,'self',1,'2024-10-19 00:00:00','2026-04-20 09:46:41',1),(2570,587,1984,'self',1,'2026-06-30 00:00:00','2026-04-20 09:46:41',1),(2571,588,1986,'self',1,'2021-07-04 00:00:00','2026-04-20 09:46:41',1),(2572,589,1987,'self',1,'2013-08-28 00:00:00','2026-04-20 09:46:41',1),(2573,590,1988,'self',1,'2025-11-03 00:00:00','2026-04-20 09:46:41',1),(2574,591,1989,'self',1,'2024-07-18 00:00:00','2026-04-20 09:46:41',1),(2575,592,1991,'self',1,'2022-06-24 00:00:00','2026-04-20 09:46:41',1),(2576,593,1992,'self',1,'2019-11-28 00:00:00','2026-04-20 09:46:41',1),(2577,594,1996,'self',1,'2023-01-23 00:00:00','2026-04-20 09:46:41',1),(2578,595,1997,'self',1,'2023-08-28 00:00:00','2026-04-20 09:46:41',1),(2579,596,2002,'self',1,'2026-07-24 00:00:00','2026-04-20 09:46:41',1),(2580,597,2003,'self',1,'2026-08-17 00:00:00','2026-04-20 09:46:41',1),(2581,598,2004,'self',1,'2026-12-22 00:00:00','2026-04-20 09:46:41',1),(2582,599,2006,'self',1,'2009-05-21 00:00:00','2026-04-20 09:46:41',1),(2583,600,2008,'self',1,'2018-09-02 00:00:00','2026-04-20 09:46:41',1),(2584,601,2009,'self',1,'2022-07-15 00:00:00','2026-04-20 09:46:41',1),(2585,602,2010,'self',1,'2020-10-26 00:00:00','2026-04-20 09:46:41',1),(2586,603,2011,'self',1,'2013-02-11 00:00:00','2026-04-20 09:46:41',1),(2587,604,2012,'self',1,'2019-01-05 00:00:00','2026-04-20 09:46:41',1),(2588,605,2013,'self',1,'2014-10-15 00:00:00','2026-04-20 09:46:41',1),(2589,606,2014,'self',1,'2021-10-03 00:00:00','2026-04-20 09:46:41',1),(2590,607,2015,'self',1,'2009-01-17 00:00:00','2026-04-20 09:46:41',1),(2591,608,2016,'self',1,'2010-03-20 00:00:00','2026-04-20 09:46:41',1),(2592,609,2017,'self',1,'2014-12-20 00:00:00','2026-04-20 09:46:41',1),(2593,610,2021,'self',1,'2014-12-28 00:00:00','2026-04-20 09:46:41',1),(2594,611,2023,'self',1,'2009-09-22 00:00:00','2026-04-20 09:46:41',1),(2595,612,2027,'self',1,'2018-07-07 00:00:00','2026-04-20 09:46:41',1),(2596,613,2030,'self',1,'2019-12-03 00:00:00','2026-04-20 09:46:41',1),(2597,614,2032,'self',1,'2025-12-05 00:00:00','2026-04-20 09:46:41',1),(2598,615,2033,'self',1,'2026-03-04 00:00:00','2026-04-20 09:46:41',1),(2599,616,2034,'self',1,'2025-08-20 00:00:00','2026-04-20 09:46:41',1),(2600,617,2035,'self',1,'2012-04-16 00:00:00','2026-04-20 09:46:41',1),(2601,618,2036,'self',1,'2020-11-23 00:00:00','2026-04-20 09:46:41',1),(2602,619,2037,'self',1,'2025-11-28 00:00:00','2026-04-20 09:46:41',1),(2603,620,2038,'self',1,'2014-06-28 00:00:00','2026-04-20 09:46:41',1),(2604,621,2042,'self',1,'2017-03-31 00:00:00','2026-04-20 09:46:41',1),(2605,622,2043,'self',1,'2014-12-15 00:00:00','2026-04-20 09:46:41',1),(2606,623,2045,'self',1,'2023-05-23 00:00:00','2026-04-20 09:46:41',1),(2607,624,2046,'self',1,'2015-04-22 00:00:00','2026-04-20 09:46:41',1),(2608,625,2051,'self',1,'2019-01-14 00:00:00','2026-04-20 09:46:41',1),(2609,626,2053,'self',1,'2025-09-09 00:00:00','2026-04-20 09:46:41',1),(2610,627,2056,'self',1,'2014-07-15 00:00:00','2026-04-20 09:46:41',1),(2611,628,2057,'self',1,'2016-03-09 00:00:00','2026-04-20 09:46:41',1),(2612,629,2058,'self',1,'2020-06-17 00:00:00','2026-04-20 09:46:41',1),(2613,630,2059,'self',1,'2011-11-04 00:00:00','2026-04-20 09:46:41',1),(2614,631,2061,'self',1,'2014-10-22 00:00:00','2026-04-20 09:46:41',1),(2615,632,2062,'self',1,'2011-05-18 00:00:00','2026-04-20 09:46:41',1),(2616,633,2063,'self',1,'2024-03-30 00:00:00','2026-04-20 09:46:41',1),(2617,634,2065,'self',1,'2018-06-03 00:00:00','2026-04-20 09:46:41',1),(2618,635,2068,'self',1,'2021-11-20 00:00:00','2026-04-20 09:46:41',1),(2619,636,2070,'self',1,'2014-06-28 00:00:00','2026-04-20 09:46:41',1),(2620,637,2073,'self',1,'2014-09-20 00:00:00','2026-04-20 09:46:41',1),(2621,638,2075,'self',1,'2013-12-10 00:00:00','2013-12-10 00:00:00',0),(2622,639,2076,'self',1,'2023-03-03 00:00:00','2023-03-03 00:00:00',0),(2623,640,2077,'self',1,'2019-01-25 00:00:00','2019-01-25 00:00:00',0),(2624,641,2078,'self',1,'2017-09-13 00:00:00','2017-09-13 00:00:00',0),(2625,642,2080,'self',1,'2017-08-29 00:00:00','2017-08-29 00:00:00',0),(2626,643,2081,'self',1,'2011-02-22 00:00:00','2011-02-22 00:00:00',0),(2627,644,2082,'self',1,'2018-08-09 00:00:00','2018-08-09 00:00:00',0),(2628,645,2084,'self',1,'2013-12-31 00:00:00','2013-12-31 00:00:00',0),(2629,646,2085,'self',1,'2017-12-15 00:00:00','2017-12-15 00:00:00',0),(2630,647,2086,'self',1,'2014-06-27 00:00:00','2014-06-27 00:00:00',0),(2631,648,2088,'self',1,'2010-12-25 00:00:00','2010-12-25 00:00:00',0),(2632,649,2091,'self',1,'2009-09-24 00:00:00','2009-09-24 00:00:00',0),(2633,650,2095,'self',1,'2025-08-20 00:00:00','2025-08-20 00:00:00',0),(2634,651,2096,'self',1,'2013-11-30 00:00:00','2013-11-30 00:00:00',0),(2635,652,2097,'self',1,'2025-01-04 00:00:00','2025-01-04 00:00:00',0),(2636,653,2098,'self',1,'2023-04-07 00:00:00','2023-04-07 00:00:00',0),(2637,654,2099,'self',1,'2015-09-09 00:00:00','2015-09-09 00:00:00',0),(2638,655,2100,'self',1,'2014-11-21 00:00:00','2014-11-21 00:00:00',0),(2639,656,2101,'self',1,'2011-07-11 00:00:00','2011-07-11 00:00:00',0),(2640,657,2102,'self',1,'2015-10-19 00:00:00','2015-10-19 00:00:00',0),(2641,658,2104,'self',1,'2013-03-20 00:00:00','2013-03-20 00:00:00',0),(2642,659,2105,'self',1,'2011-02-24 00:00:00','2011-02-24 00:00:00',0),(2643,660,2108,'self',1,'2015-06-25 00:00:00','2015-06-25 00:00:00',0),(2644,661,2109,'self',1,'2015-06-08 00:00:00','2015-06-08 00:00:00',0),(2645,662,2111,'self',1,'2012-07-17 00:00:00','2012-07-17 00:00:00',0),(2646,663,2112,'self',1,'2012-01-14 00:00:00','2012-01-14 00:00:00',0),(2647,664,2113,'self',1,'2012-04-01 00:00:00','2012-04-01 00:00:00',0),(2648,665,2114,'self',1,'2024-12-12 00:00:00','2024-12-12 00:00:00',0),(2649,666,2115,'self',1,'2009-01-01 00:00:00','2009-01-01 00:00:00',0),(2650,667,2116,'self',1,'2010-08-02 00:00:00','2010-08-02 00:00:00',0),(2651,668,2121,'self',1,'2009-08-19 00:00:00','2009-08-19 00:00:00',0),(2652,669,2122,'self',1,'2023-09-28 00:00:00','2023-09-28 00:00:00',0),(2653,670,2127,'self',1,'2009-05-31 00:00:00','2009-05-31 00:00:00',0),(2654,671,2130,'self',1,'2025-12-20 00:00:00','2025-12-20 00:00:00',0),(2655,672,2132,'self',1,'2025-12-29 00:00:00','2025-12-29 00:00:00',0),(2656,673,2135,'self',1,'2016-02-09 00:00:00','2016-02-09 00:00:00',0),(2657,674,2138,'self',1,'2010-03-04 00:00:00','2010-03-04 00:00:00',0),(2658,675,2139,'self',1,'2024-04-27 00:00:00','2024-04-27 00:00:00',0),(2659,676,2140,'self',1,'2015-01-09 00:00:00','2015-01-09 00:00:00',0),(2660,677,2143,'self',1,'2019-03-08 00:00:00','2019-03-08 00:00:00',0),(2661,678,2145,'self',1,'2014-08-02 00:00:00','2014-08-02 00:00:00',0),(2662,679,2147,'self',1,'2025-01-04 00:00:00','2025-01-04 00:00:00',0),(2663,680,2149,'self',1,'2020-07-07 00:00:00','2020-07-07 00:00:00',0),(2664,681,2150,'self',1,'2015-06-15 00:00:00','2015-06-15 00:00:00',0),(2665,682,2151,'self',1,'2016-05-06 00:00:00','2016-05-06 00:00:00',0),(2666,683,2153,'self',1,'2023-12-17 00:00:00','2023-12-17 00:00:00',0),(2667,684,2154,'self',1,'2018-11-16 00:00:00','2018-11-16 00:00:00',0),(2668,685,2155,'self',1,'2017-01-10 00:00:00','2017-01-10 00:00:00',0),(2669,686,2158,'self',1,'2016-04-25 00:00:00','2016-04-25 00:00:00',0),(2670,687,2162,'self',1,'2021-05-26 00:00:00','2021-05-26 00:00:00',0),(2671,688,2163,'self',1,'2018-03-10 00:00:00','2018-03-10 00:00:00',0),(2672,689,2164,'self',1,'2013-02-25 00:00:00','2013-02-25 00:00:00',0),(2673,690,2165,'self',1,'2021-02-20 00:00:00','2021-02-20 00:00:00',0),(2674,691,2166,'self',1,'2022-01-31 00:00:00','2022-01-31 00:00:00',0),(2675,692,2168,'self',1,'2018-10-04 00:00:00','2018-10-04 00:00:00',0),(2676,693,2169,'self',1,'2026-12-12 00:00:00','2026-12-12 00:00:00',0),(2677,694,2171,'self',1,'2023-08-14 00:00:00','2023-08-14 00:00:00',0),(2678,695,2173,'self',1,'2011-09-25 00:00:00','2011-09-25 00:00:00',0),(2679,696,2179,'self',1,'2011-06-25 00:00:00','2011-06-25 00:00:00',0),(2680,697,2181,'self',1,'2013-04-24 00:00:00','2013-04-24 00:00:00',0),(2681,698,2182,'self',1,'2023-07-08 00:00:00','2023-07-08 00:00:00',0),(2682,699,2183,'self',1,'2009-10-21 00:00:00','2009-10-21 00:00:00',0),(2683,700,2184,'self',1,'2023-04-23 00:00:00','2023-04-23 00:00:00',0),(2684,701,2186,'self',1,'2017-10-25 00:00:00','2017-10-25 00:00:00',0),(2685,702,2189,'self',1,'2015-10-08 00:00:00','2015-10-08 00:00:00',0),(2686,703,2190,'self',1,'2009-08-17 00:00:00','2009-08-17 00:00:00',0),(2687,704,2199,'self',1,'2010-10-14 00:00:00','2010-10-14 00:00:00',0),(2688,705,2200,'self',1,'2012-04-27 00:00:00','2012-04-27 00:00:00',0),(2689,706,2203,'self',1,'2016-02-25 00:00:00','2016-02-25 00:00:00',0);
/*!40000 ALTER TABLE `user_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vehicle`
--

DROP TABLE IF EXISTS `user_vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_vehicle` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `vehicle_no` varchar(10) NOT NULL,
  `is_visitor` tinyint(1) NOT NULL DEFAULT '0',
  `host_user_id` bigint DEFAULT NULL,
  `parking_deadline` datetime DEFAULT NULL,
  `remind_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_vehicle` (`user_id`,`vehicle_no`,`is_visitor`),
  KEY `idx_vehicle_no` (`vehicle_no`),
  KEY `idx_host_user` (`host_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vehicle`
--

LOCK TABLES `user_vehicle` WRITE;
/*!40000 ALTER TABLE `user_vehicle` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_vehicle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitor_blacklist`
--

DROP TABLE IF EXISTS `visitor_blacklist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitor_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blacklist_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitor_blacklist`
--

LOCK TABLES `visitor_blacklist` WRITE;
/*!40000 ALTER TABLE `visitor_blacklist` DISABLE KEYS */;
/*!40000 ALTER TABLE `visitor_blacklist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitor_invite`
--

DROP TABLE IF EXISTS `visitor_invite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitor_invite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `host_user_id` bigint NOT NULL,
  `visitor_name` varchar(20) NOT NULL,
  `visitor_phone` varchar(20) NOT NULL,
  `code` varchar(64) NOT NULL,
  `validity_type` varchar(20) NOT NULL DEFAULT 'SINGLE_2H',
  `visit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime NOT NULL,
  `max_uses` int NOT NULL DEFAULT '1',
  `used_count` int NOT NULL DEFAULT '0',
  `used_time` datetime DEFAULT NULL,
  `share_link` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_host_user_id` (`host_user_id`),
  KEY `idx_invite_expire` (`expire_time`),
  KEY `idx_invite_phone` (`visitor_phone`),
  KEY `idx_invite_deleted_expire` (`is_deleted`,`expire_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitor_invite`
--

LOCK TABLES `visitor_invite` WRITE;
/*!40000 ALTER TABLE `visitor_invite` DISABLE KEYS */;
INSERT INTO `visitor_invite` VALUES (13001,1,'Visitor-A','13600000001','GHTJY9','SINGLE_2H','2026-04-18 18:28:00','2026-04-18 20:19:58',1,0,NULL,'/pages/door/verify?code=GHTJY9','2026-04-18 11:38:52','2026-04-18 18:19:58',0),(13002,2,'Visitor-B','13600000002','INV-B001','SINGLE_2H','2026-04-18 18:14:31','2026-04-19 12:08:52',2,1,'2026-04-18 11:48:52',NULL,'2026-04-18 10:08:52','2026-04-18 11:48:52',0),(13003,1,'Visitor-C','13600000003','INV-C001','SINGLE_2H','2026-04-18 18:14:31','2026-04-18 11:08:52',1,0,NULL,NULL,'2026-04-18 09:08:52','2026-04-18 09:08:52',0),(13004,1,'WU','19672554567','INVITE-0816abf1','SINGLE_2H','2026-04-18 18:14:31','2026-04-19 16:26:13',1,0,NULL,NULL,'2026-04-18 16:26:13','2026-04-18 16:26:13',0),(13005,1,'鐞崇惓','19542865673','XJ26RR','SINGLE_2H','2026-04-18 18:28:00','2026-04-18 20:19:06',1,0,NULL,'/pages/door/verify?code=XJ26RR','2026-04-18 18:19:06','2026-04-18 18:53:18',1),(13006,1,'鐞崇惓','19542865673','V4N5XM','TODAY_END','2026-04-18 09:02:00','2026-04-18 23:59:59',1,1,'2026-04-18 18:54:07','/pages/door/verify?code=V4N5XM','2026-04-18 18:53:54','2026-04-18 18:54:07',0),(13007,1,'鐞崇惓','19542865673','F76KHZ','SINGLE_2H','2026-04-18 22:12:00','2026-04-19 00:02:40',1,0,NULL,'/pages/door/verify?code=F76KHZ','2026-04-18 22:02:40','2026-04-18 22:02:40',0),(13008,1,'鐞崇惓','19542865673','AHBQDV','SINGLE_2H','2026-04-18 22:03:00','2026-04-19 00:03:15',1,0,NULL,'/pages/door/verify?code=AHBQDV','2026-04-18 22:03:15','2026-04-18 22:03:15',0),(13009,1,'WU','19672554567','9U7FYZ','SINGLE_2H','2026-04-18 22:08:00','2026-04-19 00:08:15',1,0,NULL,'/pages/door/verify?code=9U7FYZ','2026-04-18 22:08:15','2026-04-18 22:08:15',0),(13010,1,'Visitor-C','13600000003','JYFRQ6','SINGLE_2H','2026-04-19 12:16:00','2026-04-19 14:07:00',1,0,NULL,'/pages/door/verify?code=JYFRQ6','2026-04-19 12:07:00','2026-04-19 12:07:00',0),(13011,2,'寰井','19696342728','QFLJRD','SINGLE_2H','2026-04-19 18:21:00','2026-04-19 20:22:02',1,0,NULL,'/pages/door/verify?code=QFLJRD','2026-04-19 18:22:02','2026-04-19 18:22:02',0);
/*!40000 ALTER TABLE `visitor_invite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitor_notify`
--

DROP TABLE IF EXISTS `visitor_notify`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitor_notify` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `host_user_id` bigint NOT NULL,
  `invite_id` bigint NOT NULL,
  `visitor_name` varchar(20) NOT NULL,
  `content` varchar(255) NOT NULL,
  `read_flag` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_notify_host` (`host_user_id`),
  KEY `idx_notify_invite` (`invite_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitor_notify`
--

LOCK TABLES `visitor_notify` WRITE;
/*!40000 ALTER TABLE `visitor_notify` DISABLE KEYS */;
INSERT INTO `visitor_notify` VALUES (1,1,13006,'鐞崇惓','鎮ㄧ殑璁垮銆愮惓鐞炽€戝凡杩涘叆灏忓尯',0,'2026-04-18 18:54:07','2026-04-18 18:54:07',0);
/*!40000 ALTER TABLE `visitor_notify` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `worker_staffing`
--

DROP TABLE IF EXISTS `worker_staffing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `worker_staffing` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL,
  `staff_type` tinyint NOT NULL DEFAULT '2',
  `position` varchar(50) DEFAULT NULL,
  `shift_group` varchar(20) DEFAULT NULL,
  `certificates` varchar(2000) DEFAULT NULL,
  `specialties` varchar(2000) DEFAULT NULL,
  `max_daily_orders` int NOT NULL DEFAULT '5',
  `current_status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_worker_id` (`worker_id`),
  KEY `idx_staff_type_status` (`staff_type`,`current_status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `worker_staffing`
--

LOCK TABLES `worker_staffing` WRITE;
/*!40000 ALTER TABLE `worker_staffing` DISABLE KEYS */;
INSERT INTO `worker_staffing` VALUES (1,20,1,'housekeeping-fixed','A','瀹舵斂鏈嶅姟璇?housekeeping_cert','鏃ュ父淇濇磥,鍏ㄥ眿澶ф壂闄?鍘ㄦ埧娣卞害娓呮磥,鍗敓闂存秷姣?housekeeping,cleaning',6,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(2,21,1,'housekeeping-fixed','A','瀹舵斂鏈嶅姟璇?housekeeping_cert','鏃ュ父淇濇磥,鐜荤拑鎿︽嫮,鍦版澘鎵撹湣,娌欏彂鍦版娓呮礂,housekeeping,cleaning',6,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(3,22,1,'housekeeping-fixed','B','瀹舵斂鏈嶅姟璇?cleaning_cert,appliance_clean_cert','瀹剁數娓呮礂,绌鸿皟娓呮礂,娌圭儫鏈烘竻娲?娲楄。鏈烘竻娲?cleaning,appliance',6,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(4,23,1,'housekeeping-fixed','B','瀹舵斂鏈嶅姟璇?cleaning_cert,appliance_clean_cert','瀹剁數娓呮礂,鍐扮娓呮礂,鐑按鍣ㄩ櫎鍨?鍑€姘村櫒鏁呴殰,cleaning,appliance',6,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(5,24,1,'housekeeping-fixed','C','鍏昏€佹姢鐞嗚瘉,caregiver_cert','鍏昏€佹姢鐞?鑰佷汉闄姢,鍔╂荡,搴峰鎸夋懇,care,nurse',5,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(6,25,1,'housekeeping-fixed','C','瀹舵斂鏈嶅姟璇?涓撻」鏈嶅姟璇?special_service_cert','涓撻」鏈嶅姟,闄よ灗鏈嶅姟,寮€鑽掍繚娲?瀹跺涵鏀剁撼鏁寸悊,special,cleaning',5,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(7,26,1,'housekeeping-fixed','A','瀹舵斂鏈嶅姟璇?housekeeping_cert','鏃ュ父淇濇磥,瀹犵墿鎶ょ悊,寮€鑽掍繚娲?housekeeping,cleaning',6,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(8,27,1,'housekeeping-fixed','B','瀹舵斂鏈嶅姟璇?cleaning_cert','鏃ュ父淇濇磥,瀹剁數娓呮礂,鐜荤拑鎿︽嫮,鍦版澘鎵撹湣,cleaning',6,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(9,28,2,'repair-plumber','A','姘村伐璇?plumber_cert','姘寸數缁翠慨,姘寸婕忔按,椹《鐤忛€?涓嬫按閬撳牭濉?鐑按鍣ㄦ晠闅?plumber,water,drain',8,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(10,29,3,'repair-electrician','A','鐢靛伐璇?electrician_cert','姘寸數缁翠慨,鐢佃矾璺抽椄,鎻掑骇鎹熷潖,鐏叿缁翠慨,绾胯矾鑰佸寲鏇存崲,electric,power',8,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(11,30,4,'repair-appliance','B','瀹剁數缁翠慨璇?appliance_cert','瀹剁數缁翠慨,绌鸿皟涓嶅埗鍐?鍐扮涓嶅埗鍐?娲楄。鏈轰笉杞?娌圭儫鏈烘晠闅?appliance',8,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0),(12,31,5,'repair-outsource','OUT','澶栧寘璧勮川,outsource_company_qualification','鎴垮眿缁撴瀯,瀹跺叿缁翠慨,鏅鸿兘璁惧,鍏朵粬,涓撻」鏈嶅姟,outsource,cooperation',15,1,'2026-04-20 08:18:58','2026-04-20 08:18:58',0);
/*!40000 ALTER TABLE `worker_staffing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'smart_community'
--

--
-- Dumping routines for database 'smart_community'
--
/*!50003 DROP FUNCTION IF EXISTS `fn_initial_char` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE FUNCTION `fn_initial_char`(ch VARCHAR(8)) RETURNS varchar(1) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN

  DECLARE gbk_bytes VARBINARY(8);

  DECLARE byte1 INT;

  DECLARE byte2 INT;

  DECLARE code_no INT;



  IF ch IS NULL OR ch = '' THEN

    RETURN '';

  END IF;



  IF ch REGEXP '^[A-Za-z]$' THEN

    RETURN UPPER(ch);

  END IF;



  SET gbk_bytes = CONVERT(ch USING gbk);

  IF LENGTH(gbk_bytes) < 2 THEN

    RETURN '';

  END IF;



  SET byte1 = CONV(HEX(SUBSTRING(gbk_bytes, 1, 1)), 16, 10);

  SET byte2 = CONV(HEX(SUBSTRING(gbk_bytes, 2, 1)), 16, 10);

  SET code_no = (byte1 - 160) * 100 + (byte2 - 160);



  IF code_no >= 1601 AND code_no < 1637 THEN RETURN 'A'; END IF;

  IF code_no >= 1637 AND code_no < 1833 THEN RETURN 'B'; END IF;

  IF code_no >= 1833 AND code_no < 2078 THEN RETURN 'C'; END IF;

  IF code_no >= 2078 AND code_no < 2274 THEN RETURN 'D'; END IF;

  IF code_no >= 2274 AND code_no < 2302 THEN RETURN 'E'; END IF;

  IF code_no >= 2302 AND code_no < 2433 THEN RETURN 'F'; END IF;

  IF code_no >= 2433 AND code_no < 2594 THEN RETURN 'G'; END IF;

  IF code_no >= 2594 AND code_no < 2787 THEN RETURN 'H'; END IF;

  IF code_no >= 2787 AND code_no < 3106 THEN RETURN 'J'; END IF;

  IF code_no >= 3106 AND code_no < 3212 THEN RETURN 'K'; END IF;

  IF code_no >= 3212 AND code_no < 3472 THEN RETURN 'L'; END IF;

  IF code_no >= 3472 AND code_no < 3635 THEN RETURN 'M'; END IF;

  IF code_no >= 3635 AND code_no < 3722 THEN RETURN 'N'; END IF;

  IF code_no >= 3722 AND code_no < 3730 THEN RETURN 'O'; END IF;

  IF code_no >= 3730 AND code_no < 3858 THEN RETURN 'P'; END IF;

  IF code_no >= 3858 AND code_no < 4027 THEN RETURN 'Q'; END IF;

  IF code_no >= 4027 AND code_no < 4086 THEN RETURN 'R'; END IF;

  IF code_no >= 4086 AND code_no < 4390 THEN RETURN 'S'; END IF;

  IF code_no >= 4390 AND code_no < 4558 THEN RETURN 'T'; END IF;

  IF code_no >= 4558 AND code_no < 4684 THEN RETURN 'W'; END IF;

  IF code_no >= 4684 AND code_no < 4925 THEN RETURN 'X'; END IF;

  IF code_no >= 4925 AND code_no < 5249 THEN RETURN 'Y'; END IF;

  IF code_no >= 5249 AND code_no < 5600 THEN RETURN 'Z'; END IF;

  RETURN 'X';

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_name_initials` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE FUNCTION `fn_name_initials`(name_text VARCHAR(128)) RETURNS varchar(64) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN

  DECLARE i INT DEFAULT 1;

  DECLARE n INT DEFAULT 0;

  DECLARE ch VARCHAR(8);

  DECLARE out_text VARCHAR(64) DEFAULT '';

  DECLARE latin_open TINYINT DEFAULT 0;

  DECLARE char_init VARCHAR(1);



  SET name_text = TRIM(COALESCE(name_text, ''));

  SET n = CHAR_LENGTH(name_text);

  WHILE i <= n DO

    SET ch = SUBSTRING(name_text, i, 1);

    IF ch REGEXP '^[A-Za-z]$' THEN

      IF latin_open = 0 THEN

        SET out_text = CONCAT(out_text, UPPER(ch));

        SET latin_open = 1;

      END IF;

    ELSE

      SET latin_open = 0;

      SET char_init = fn_initial_char(ch);

      IF char_init <> '' THEN

        SET out_text = CONCAT(out_text, char_init);

      END IF;

    END IF;

    SET i = i + 1;

  END WHILE;



  IF out_text = '' THEN

    SET out_text = 'YZ';

  END IF;

  RETURN out_text;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-20 20:40:35


