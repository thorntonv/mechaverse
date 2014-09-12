-- MySQL dump 10.13  Distrib 5.5.38, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: mechaverse
-- ------------------------------------------------------
-- Server version	5.5.38-0ubuntu0.14.04.1

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
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `maxInstanceCount` int(11) NOT NULL,
  `minInstanceCount` int(11) NOT NULL,
  `taskIterationCount` int(11) NOT NULL,
  `taskMaxDurationInSeconds` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `config_configproperty`
--

DROP TABLE IF EXISTS `config_configproperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `config_configproperty` (
  `config_id` bigint(20) NOT NULL,
  `configProperties_id` bigint(20) NOT NULL,
  PRIMARY KEY (`config_id`,`configProperties_id`),
  UNIQUE KEY `configProperties_id` (`configProperties_id`),
  KEY `FK32F0BDB4662F8187` (`config_id`),
  KEY `FK32F0BDB481640E9` (`configProperties_id`),
  CONSTRAINT `FK32F0BDB4662F8187` FOREIGN KEY (`config_id`) REFERENCES `config` (`id`),
  CONSTRAINT `FK32F0BDB481640E9` FOREIGN KEY (`configProperties_id`) REFERENCES `configproperty` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configproperty`
--

DROP TABLE IF EXISTS `configproperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configproperty` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `value` tinyblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance`
--

DROP TABLE IF EXISTS `instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instance` (
  `instanceId` varchar(255) NOT NULL,
  `iteration` bigint(20) NOT NULL,
  `preferredClientId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`instanceId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance_task`
--

DROP TABLE IF EXISTS `instance_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instance_task` (
  `instance_instanceId` varchar(255) NOT NULL,
  `executingTasks_id` bigint(20) NOT NULL,
  PRIMARY KEY (`instance_instanceId`,`executingTasks_id`),
  UNIQUE KEY `executingTasks_id` (`executingTasks_id`),
  KEY `FKAD98EEEF74371909` (`executingTasks_id`),
  KEY `FKAD98EEEFB7262E63` (`instance_instanceId`),
  CONSTRAINT `FKAD98EEEF74371909` FOREIGN KEY (`executingTasks_id`) REFERENCES `task` (`id`),
  CONSTRAINT `FKAD98EEEFB7262E63` FOREIGN KEY (`instance_instanceId`) REFERENCES `instance` (`instanceId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `result`
--

DROP TABLE IF EXISTS `result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `temporaryDataFilename` varchar(255) DEFAULT NULL,
  `task_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC84DC81DA5FB0EC0` (`task_id`),
  CONSTRAINT `FKC84DC81DA5FB0EC0` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `simulation`
--

DROP TABLE IF EXISTS `simulation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `simulation` (
  `simulationId` varchar(255) NOT NULL,
  `active` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `config_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`simulationId`),
  KEY `FKB3012607662F8187` (`config_id`),
  CONSTRAINT `FKB3012607662F8187` FOREIGN KEY (`config_id`) REFERENCES `config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `simulation_instance`
--

DROP TABLE IF EXISTS `simulation_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `simulation_instance` (
  `simulation_simulationId` varchar(255) NOT NULL,
  `instances_instanceId` varchar(255) NOT NULL,
  PRIMARY KEY (`simulation_simulationId`,`instances_instanceId`),
  UNIQUE KEY `instances_instanceId` (`instances_instanceId`),
  KEY `FKD96777CDBABB0795` (`simulation_simulationId`),
  KEY `FKD96777CD4AE4A8DA` (`instances_instanceId`),
  CONSTRAINT `FKD96777CD4AE4A8DA` FOREIGN KEY (`instances_instanceId`) REFERENCES `instance` (`instanceId`),
  CONSTRAINT `FKD96777CDBABB0795` FOREIGN KEY (`simulation_simulationId`) REFERENCES `simulation` (`simulationId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `clientId` varchar(255) DEFAULT NULL,
  `completionTime` datetime DEFAULT NULL,
  `instanceId` varchar(255) DEFAULT NULL,
  `iteration` bigint(20) NOT NULL,
  `iterationCount` int(11) NOT NULL,
  `simulationId` varchar(255) DEFAULT NULL,
  `startTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-09-10 23:03:23
