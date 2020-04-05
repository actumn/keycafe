-- MySQL dump 10.13  Distrib 5.6.17, for Win64 (x86_64)
--
-- Host: 203.252.182.105    Database: cmdb
-- ------------------------------------------------------
-- Server version	5.6.22-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attached_file_table`
--

DROP TABLE IF EXISTS `attached_file_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attached_file_table` (
  `seqNum` int(11) NOT NULL AUTO_INCREMENT,
  `contentID` int(11) DEFAULT NULL,
  `filePath` varchar(256) DEFAULT NULL,
  `fileName` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`seqNum`),
  UNIQUE KEY `seqNum_UNIQUE` (`seqNum`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attached_file_table`
--

LOCK TABLES `attached_file_table` WRITE;
/*!40000 ALTER TABLE `attached_file_table` DISABLE KEYS */;
INSERT INTO `attached_file_table` VALUES (1,1,'./server-file-path/cats','test.jpg'),(2,1,'./server-file-path/cats','test1.jpg'),(3,2,'./server-file-path/cats','test4.jpg'),(4,3,'./server-file-path/cats','test.jpg'),(5,3,'./server-file-path/cats','test1.jpg'),(6,4,'./server-file-path/cats','test.jpg'),(7,4,'./server-file-path/cats','test1.jpg'),(8,4,'./server-file-path/cats','test4.jpg'),(9,5,'./server-file-path/mlim','1_교과목_학습성과.hwp'),(10,6,'./server-file-path/mlim','thumb_IMG_2527_1024.jpg'),(11,7,'./server-file-path/mlim','thumb_IMG_2528_1024.jpg'),(12,9,'./server-file-path/cats','DSC_0525.JPG'),(13,10,'./server-file-path/cats','DSC_0523.JPG'),(14,10,'./server-file-path/cats','DSC_0524.JPG'),(15,10,'./server-file-path/cats','DSC_0526.JPG'),(16,10,'./server-file-path/cats','DSC_0527.JPG'),(17,11,'./server-file-path/user-0','DSC_0523.JPG'),(18,12,'./server-file-path/user-1','DSC_0524.JPG'),(19,13,'./server-file-path/user-2','DSC_0525.JPG'),(20,14,'./server-file-path/user-3','DSC_0526.JPG'),(21,15,'./server-file-path/user-4','DSC_0527.JPG'),(22,16,'./server-file-path/user-5','test.jpg'),(23,17,'./server-file-path/user-6','test1.jpg'),(24,18,'./server-file-path/user-7','test2.jpg'),(25,19,'./server-file-path/user-8','test3.jpg'),(26,20,'./server-file-path/user-9','test4.jpg');
/*!40000 ALTER TABLE `attached_file_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_table`
--

DROP TABLE IF EXISTS `friend_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `friend_table` (
  `userName` varchar(80) NOT NULL,
  `friendName` varchar(80) NOT NULL,
  PRIMARY KEY (`userName`,`friendName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_table`
--

LOCK TABLES `friend_table` WRITE;
/*!40000 ALTER TABLE `friend_table` DISABLE KEYS */;
INSERT INTO `friend_table` VALUES ('cats','ccslab'),('cats','mlim'),('ccslab','cats'),('ccslab','mlim'),('mlim','cats');
/*!40000 ALTER TABLE `friend_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sns_attach_access_history_table`
--

DROP TABLE IF EXISTS `sns_attach_access_history_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sns_attach_access_history_table` (
  `userName` varchar(80) NOT NULL,
  `date` date NOT NULL,
  `writerName` varchar(80) NOT NULL,
  `accessCount` int(11) DEFAULT '0',
  PRIMARY KEY (`userName`,`date`,`writerName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sns_attach_access_history_table`
--

LOCK TABLES `sns_attach_access_history_table` WRITE;
/*!40000 ALTER TABLE `sns_attach_access_history_table` DISABLE KEYS */;
INSERT INTO `sns_attach_access_history_table` VALUES ('user-0','2015-09-11','user-4',4),('user-0','2015-09-11','user-5',3),('user-0','2015-09-12','user-3',2),('user-0','2015-09-12','user-4',10),('user-0','2015-09-12','user-5',4),('user-0','2015-09-13','user-3',2),('user-0','2015-09-13','user-4',3),('user-0','2015-09-13','user-5',4),('user-0','2015-09-14','user-3',1),('user-0','2015-09-14','user-4',2),('user-0','2015-09-14','user-5',3),('user-0','2015-09-14','user-6',3),('user-0','2015-09-15','user-4',3),('user-0','2015-09-15','user-5',4),('user-0','2015-09-15','user-6',4),('user-0','2015-09-15','user-7',1),('user-0','2015-09-16','user-3',3),('user-0','2015-09-16','user-4',1),('user-0','2015-09-16','user-5',1),('user-0','2015-09-16','user-6',2),('user-0','2015-09-17','user-1',1),('user-0','2015-09-17','user-4',3),('user-0','2015-09-17','user-5',1),('user-0','2015-09-17','user-6',1),('user-0','2015-09-18','user-3',1),('user-0','2015-09-18','user-4',5),('user-0','2015-09-18','user-5',3),('user-1','2015-09-11','user-2',1),('user-1','2015-09-11','user-4',6),('user-1','2015-09-11','user-5',8),('user-1','2015-09-11','user-6',1),('user-1','2015-09-12','user-3',1),('user-1','2015-09-12','user-4',1),('user-1','2015-09-12','user-5',1),('user-1','2015-09-12','user-6',2),('user-1','2015-09-12','user-7',1),('user-1','2015-09-13','user-3',1),('user-1','2015-09-13','user-4',4),('user-1','2015-09-13','user-5',6),('user-1','2015-09-14','user-3',1),('user-1','2015-09-14','user-4',2),('user-1','2015-09-14','user-5',4),('user-1','2015-09-14','user-6',3),('user-1','2015-09-15','user-3',2),('user-1','2015-09-15','user-4',1),('user-1','2015-09-15','user-5',2),('user-1','2015-09-15','user-6',3),('user-1','2015-09-16','user-3',1),('user-1','2015-09-16','user-4',3),('user-1','2015-09-16','user-5',3),('user-1','2015-09-16','user-7',1),('user-1','2015-09-17','user-3',1),('user-1','2015-09-17','user-4',3),('user-1','2015-09-17','user-5',5),('user-1','2015-09-17','user-6',2),('user-1','2015-09-18','user-3',2),('user-1','2015-09-18','user-4',7),('user-1','2015-09-18','user-5',1),('user-1','2015-09-18','user-6',3),('user-2','2015-09-11','user-3',4),('user-2','2015-09-11','user-4',4),('user-2','2015-09-11','user-5',2),('user-2','2015-09-12','user-3',2),('user-2','2015-09-12','user-4',3),('user-2','2015-09-12','user-5',3),('user-2','2015-09-12','user-6',1),('user-2','2015-09-13','user-2',1),('user-2','2015-09-13','user-3',1),('user-2','2015-09-13','user-4',5),('user-2','2015-09-13','user-6',1),('user-2','2015-09-14','user-2',1),('user-2','2015-09-14','user-4',2),('user-2','2015-09-14','user-5',1),('user-2','2015-09-14','user-6',2),('user-2','2015-09-15','user-2',1),('user-2','2015-09-15','user-3',2),('user-2','2015-09-15','user-4',2),('user-2','2015-09-15','user-5',7),('user-2','2015-09-15','user-6',1),('user-2','2015-09-16','user-3',4),('user-2','2015-09-16','user-4',7),('user-2','2015-09-16','user-5',7),('user-2','2015-09-16','user-6',2),('user-2','2015-09-17','user-3',1),('user-2','2015-09-17','user-4',5),('user-2','2015-09-17','user-5',2),('user-2','2015-09-17','user-6',1),('user-2','2015-09-18','user-2',1),('user-2','2015-09-18','user-3',1),('user-2','2015-09-18','user-4',5),('user-2','2015-09-18','user-5',4),('user-2','2015-09-18','user-6',6),('user-3','2015-09-11','user-3',1),('user-3','2015-09-11','user-4',5),('user-3','2015-09-11','user-5',7),('user-3','2015-09-11','user-8',1),('user-3','2015-09-12','user-4',5),('user-3','2015-09-12','user-5',5),('user-3','2015-09-13','user-3',2),('user-3','2015-09-13','user-4',3),('user-3','2015-09-13','user-5',2),('user-3','2015-09-13','user-6',1),('user-3','2015-09-13','user-7',1),('user-3','2015-09-14','user-3',1),('user-3','2015-09-14','user-4',2),('user-3','2015-09-14','user-5',6),('user-3','2015-09-14','user-6',1),('user-3','2015-09-14','user-7',1),('user-3','2015-09-15','user-3',1),('user-3','2015-09-15','user-4',4),('user-3','2015-09-15','user-5',5),('user-3','2015-09-16','user-3',4),('user-3','2015-09-16','user-4',3),('user-3','2015-09-16','user-5',3),('user-3','2015-09-16','user-6',3),('user-3','2015-09-17','user-3',1),('user-3','2015-09-17','user-4',5),('user-3','2015-09-17','user-5',5),('user-3','2015-09-17','user-6',3),('user-3','2015-09-18','user-2',1),('user-3','2015-09-18','user-3',2),('user-3','2015-09-18','user-4',6),('user-3','2015-09-18','user-5',7),('user-3','2015-09-18','user-6',4),('user-4','2015-09-11','user-3',1),('user-4','2015-09-11','user-4',1),('user-4','2015-09-11','user-5',5),('user-4','2015-09-11','user-6',1),('user-4','2015-09-12','user-4',3),('user-4','2015-09-12','user-5',5),('user-4','2015-09-13','user-3',1),('user-4','2015-09-13','user-4',1),('user-4','2015-09-13','user-5',3),('user-4','2015-09-13','user-6',2),('user-4','2015-09-14','user-3',1),('user-4','2015-09-14','user-4',5),('user-4','2015-09-14','user-5',2),('user-4','2015-09-15','user-3',1),('user-4','2015-09-15','user-4',2),('user-4','2015-09-15','user-5',2),('user-4','2015-09-15','user-6',1),('user-4','2015-09-16','user-3',1),('user-4','2015-09-16','user-4',3),('user-4','2015-09-16','user-5',4),('user-4','2015-09-16','user-6',2),('user-4','2015-09-17','user-3',2),('user-4','2015-09-17','user-4',6),('user-4','2015-09-17','user-5',1),('user-4','2015-09-17','user-6',1),('user-4','2015-09-18','user-3',2),('user-4','2015-09-18','user-4',2),('user-4','2015-09-18','user-5',3),('user-5','2015-09-11','user-3',2),('user-5','2015-09-11','user-4',4),('user-5','2015-09-11','user-5',5),('user-5','2015-09-11','user-6',2),('user-5','2015-09-11','user-7',1),('user-5','2015-09-12','user-3',3),('user-5','2015-09-12','user-4',2),('user-5','2015-09-12','user-5',4),('user-5','2015-09-13','user-2',2),('user-5','2015-09-13','user-4',1),('user-5','2015-09-13','user-5',1),('user-5','2015-09-14','user-4',7),('user-5','2015-09-14','user-5',3),('user-5','2015-09-14','user-6',1),('user-5','2015-09-15','user-4',1),('user-5','2015-09-15','user-5',1),('user-5','2015-09-15','user-6',1),('user-5','2015-09-16','user-4',2),('user-5','2015-09-16','user-5',4),('user-5','2015-09-16','user-6',3),('user-5','2015-09-17','user-3',1),('user-5','2015-09-17','user-4',5),('user-5','2015-09-17','user-5',2),('user-5','2015-09-17','user-6',3),('user-5','2015-09-17','user-7',1),('user-5','2015-09-18','user-4',4),('user-5','2015-09-18','user-5',1),('user-5','2015-09-18','user-6',2),('user-5','2015-09-18','user-7',1),('user-6','2015-09-11','user-3',4),('user-6','2015-09-11','user-4',1),('user-6','2015-09-11','user-5',4),('user-6','2015-09-11','user-6',1),('user-6','2015-09-12','user-3',1),('user-6','2015-09-12','user-4',5),('user-6','2015-09-12','user-5',5),('user-6','2015-09-12','user-6',1),('user-6','2015-09-13','user-3',2),('user-6','2015-09-13','user-4',4),('user-6','2015-09-13','user-5',6),('user-6','2015-09-13','user-6',5),('user-6','2015-09-14','user-2',1),('user-6','2015-09-14','user-4',2),('user-6','2015-09-14','user-5',2),('user-6','2015-09-15','user-4',5),('user-6','2015-09-15','user-5',7),('user-6','2015-09-15','user-6',3),('user-6','2015-09-16','user-4',2),('user-6','2015-09-16','user-5',3),('user-6','2015-09-16','user-6',2),('user-6','2015-09-17','user-3',1),('user-6','2015-09-17','user-4',6),('user-6','2015-09-17','user-5',1),('user-6','2015-09-17','user-6',1),('user-6','2015-09-17','user-7',1),('user-6','2015-09-18','user-3',2),('user-6','2015-09-18','user-4',1),('user-6','2015-09-18','user-5',2),('user-7','2015-09-11','user-3',1),('user-7','2015-09-11','user-4',5),('user-7','2015-09-11','user-5',6),('user-7','2015-09-11','user-6',2),('user-7','2015-09-11','user-7',1),('user-7','2015-09-12','user-3',3),('user-7','2015-09-12','user-4',2),('user-7','2015-09-12','user-5',2),('user-7','2015-09-12','user-6',2),('user-7','2015-09-13','user-2',1),('user-7','2015-09-13','user-3',3),('user-7','2015-09-13','user-4',2),('user-7','2015-09-13','user-5',3),('user-7','2015-09-13','user-6',3),('user-7','2015-09-14','user-3',3),('user-7','2015-09-14','user-4',4),('user-7','2015-09-14','user-5',9),('user-7','2015-09-14','user-6',1),('user-7','2015-09-14','user-7',1),('user-7','2015-09-15','user-2',1),('user-7','2015-09-15','user-3',1),('user-7','2015-09-15','user-4',3),('user-7','2015-09-15','user-5',3),('user-7','2015-09-15','user-6',1),('user-7','2015-09-16','user-3',1),('user-7','2015-09-16','user-4',2),('user-7','2015-09-16','user-5',4),('user-7','2015-09-16','user-6',3),('user-7','2015-09-17','user-3',3),('user-7','2015-09-17','user-4',4),('user-7','2015-09-17','user-5',3),('user-7','2015-09-17','user-6',1),('user-7','2015-09-18','user-3',2),('user-7','2015-09-18','user-4',3),('user-7','2015-09-18','user-5',2),('user-7','2015-09-18','user-6',1),('user-8','2015-09-11','user-3',1),('user-8','2015-09-11','user-4',2),('user-8','2015-09-11','user-5',3),('user-8','2015-09-11','user-6',2),('user-8','2015-09-12','user-4',4),('user-8','2015-09-12','user-5',2),('user-8','2015-09-12','user-6',2),('user-8','2015-09-12','user-7',1),('user-8','2015-09-13','user-3',2),('user-8','2015-09-13','user-4',6),('user-8','2015-09-13','user-5',2),('user-8','2015-09-14','user-3',1),('user-8','2015-09-14','user-4',1),('user-8','2015-09-14','user-5',5),('user-8','2015-09-14','user-6',2),('user-8','2015-09-15','user-4',3),('user-8','2015-09-15','user-5',4),('user-8','2015-09-15','user-6',1),('user-8','2015-09-16','user-3',2),('user-8','2015-09-16','user-4',3),('user-8','2015-09-16','user-5',4),('user-8','2015-09-16','user-6',2),('user-8','2015-09-17','user-3',1),('user-8','2015-09-17','user-4',2),('user-8','2015-09-17','user-5',4),('user-8','2015-09-17','user-6',2),('user-8','2015-09-17','user-7',1),('user-8','2015-09-18','user-3',1),('user-8','2015-09-18','user-4',2),('user-8','2015-09-18','user-5',2),('user-8','2015-09-18','user-6',1),('user-9','2015-09-11','user-4',5),('user-9','2015-09-11','user-5',5),('user-9','2015-09-11','user-6',2),('user-9','2015-09-12','user-1',1),('user-9','2015-09-12','user-2',1),('user-9','2015-09-12','user-3',1),('user-9','2015-09-12','user-4',7),('user-9','2015-09-12','user-5',3),('user-9','2015-09-13','user-3',1),('user-9','2015-09-13','user-4',5),('user-9','2015-09-13','user-5',1),('user-9','2015-09-13','user-6',1),('user-9','2015-09-14','user-3',4),('user-9','2015-09-14','user-4',7),('user-9','2015-09-14','user-5',4),('user-9','2015-09-14','user-6',2),('user-9','2015-09-15','user-3',1),('user-9','2015-09-15','user-4',6),('user-9','2015-09-15','user-5',5),('user-9','2015-09-15','user-7',1),('user-9','2015-09-16','user-3',3),('user-9','2015-09-16','user-4',6),('user-9','2015-09-16','user-5',1),('user-9','2015-09-16','user-7',1),('user-9','2015-09-17','user-2',1),('user-9','2015-09-17','user-3',2),('user-9','2015-09-17','user-4',3),('user-9','2015-09-17','user-5',3),('user-9','2015-09-17','user-6',1),('user-9','2015-09-18','user-2',1),('user-9','2015-09-18','user-3',1),('user-9','2015-09-18','user-4',5),('user-9','2015-09-18','user-6',1);
/*!40000 ALTER TABLE `sns_attach_access_history_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sns_content_table`
--

DROP TABLE IF EXISTS `sns_content_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sns_content_table` (
  `seqNum` int(11) NOT NULL AUTO_INCREMENT,
  `creationTime` datetime DEFAULT NULL,
  `userName` varchar(80) DEFAULT NULL,
  `textMessage` varchar(256) DEFAULT NULL,
  `numAttachedFiles` int(11) DEFAULT NULL,
  `replyOf` int(11) DEFAULT NULL,
  `levelOfDisclosure` int(11) DEFAULT NULL,
  PRIMARY KEY (`seqNum`),
  UNIQUE KEY `seqNum_UNIQUE` (`seqNum`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sns_content_table`
--

LOCK TABLES `sns_content_table` WRITE;
/*!40000 ALTER TABLE `sns_content_table` DISABLE KEYS */;
INSERT INTO `sns_content_table` VALUES (1,'2015-03-25 14:45:25','cats','test',2,0,0),(2,'2015-05-01 14:49:15','cats','thumbnail test',1,0,0),(3,'2015-05-01 14:51:09','cats','create 2 thumbnail images',2,0,0),(4,'2015-05-01 14:55:19','cats','thumbnail test with 3 images',3,0,0),(5,'2015-05-01 14:58:45','mlim','attachment with a non-image file',1,0,0),(6,'2015-05-05 16:59:09','mlim','다중 다운로드 테스트',1,0,3),(7,'2015-05-05 17:00:39','mlim','친구와 공유 콘텐츠',1,0,2),(8,'2015-05-14 15:34:48','cats','파일 첨부 테스트',0,0,0),(9,'2015-05-14 16:15:51','cats','고용량사진 첨부',1,0,0),(10,'2015-05-14 16:23:33','cats','고용량 사진 여러장',4,0,0),(11,'2015-09-15 22:10:07','user-0','user-0 이미지 업로드',1,0,0),(12,'2015-09-15 22:11:29','user-1','user-1 이미지 첨부',1,0,0),(13,'2015-09-15 22:12:31','user-2','user-2 이미지 업로드',1,0,0),(14,'2015-09-15 22:13:13','user-3','user-3 이미지 업로드',1,0,0),(15,'2015-09-15 22:14:02','user-4','user-4 이미지 업로드',1,0,0),(16,'2015-09-15 22:14:43','user-5','user-5 이미지 업로드',1,0,0),(17,'2015-09-15 22:15:26','user-6','user-6 이미지 업로드',1,0,0),(18,'2015-09-15 22:15:59','user-7','user-7 이미지 업로드',1,0,0),(19,'2015-09-15 22:16:38','user-8','user-8 이미지 업로드',1,0,0),(20,'2015-09-15 22:17:05','user-9','user-9 이미지 업로드',1,0,0);
/*!40000 ALTER TABLE `sns_content_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_table`
--

DROP TABLE IF EXISTS `user_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_table` (
  `seqNum` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(80) NOT NULL,
  `password` varchar(80) DEFAULT NULL,
  `creationTime` datetime(1) DEFAULT NULL,
  PRIMARY KEY (`seqNum`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_table`
--

LOCK TABLES `user_table` WRITE;
/*!40000 ALTER TABLE `user_table` DISABLE KEYS */;
INSERT INTO `user_table` VALUES (1,'cats','*C03B7EE37168C6EC446B0956AF3E2842D7397645','2015-02-23 08:59:46.0'),(2,'임민규','*DCA83ACD74AAE79F91F3592929506563D7FE9F96','2015-02-23 09:00:24.0'),(3,'mlim','*DCA83ACD74AAE79F91F3592929506563D7FE9F96','2015-02-23 09:01:01.0'),(4,'ccslab','*2DF481F2C08CE390E8016973BB43BDDF2AB354F3','2015-02-23 09:01:08.0'),(5,'user-0','*EAAA9D6D415B5AA975BFFA2AF640D8C213E08056','2015-09-15 21:50:01.0'),(6,'user-1','*22236DABAA0E102AD7CB2B1FF04FBD6E62864235','2015-09-15 21:50:16.0'),(7,'user-2','*6B5A2311FEC65B8A85BE980A3B9F04A136A238BD','2015-09-15 21:50:30.0'),(8,'user-3','*D23E6A5496046FA86024BFD157136CD3A0E3B901','2015-09-15 21:50:40.0'),(9,'user-4','*C885112C4BF64543FF8A6BDBF15ADD5D8DE9624E','2015-09-15 21:50:48.0'),(10,'user-5','*10A8D8C9C0E0A9240F494CCD857F2B0BD7E85153','2015-09-15 21:50:57.0'),(11,'user-6','*C0402B262E32AD448D2D40EA6C92641862B5F03A','2015-09-15 21:51:06.0'),(12,'user-7','*667F7092D7A7D86B082EA35C8E20DA48ECA148B3','2015-09-15 21:51:14.0'),(13,'user-8','*E90CF7D778C59C46C6088A2543149FD1417F4E84','2015-09-15 21:51:22.0'),(14,'user-9','*ABCA1B42B2582CE956A0E883C4AB19DB88570F20','2015-09-15 21:51:30.0');
/*!40000 ALTER TABLE `user_table` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-22 16:09:39
