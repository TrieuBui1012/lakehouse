package org.example

import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SaveMode._
import com.amazonaws.SDKGlobalConfiguration


object App {
  val logger: Logger = LogManager.getLogger(App.getClass)
  def main(args: Array[String]): Unit = {
    // Cấu hình Spark
    val conf = new SparkConf()
      .setAppName("hudi trips table spark 3.3.2 job builtin")
//      .setMaster("local[*]")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
      .set("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionCatalog")
      .set("spark.hadoop.fs.s3a.access.key", "minioadmin")
      .set("spark.hadoop.fs.s3a.secret.key", "minioadmin")
      .set("spark.hadoop.fs.s3a.endpoint", "http://minio-service.default.svc.cluster.local:9000")
//      .set("spark.hadoop.fs.s3a.access.key", "ROOTUSER")
//      .set("spark.hadoop.fs.s3a.secret.key", "CHANGEME123")
//      .set("spark.hadoop.fs.s3a.endpoint", "http://127.0.0.1:9000")
      .set("spark.hadoop.fs.s3a.path.style.access", "true")

//    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(conf).getOrCreate()
    logger.info("S3 URL: https://minio.minio-tenant-1.svc.cluster.local:443")
    // Đọc file từ S3
    logger.info("Bắt đầu đọc file từ S3...")
    val dataPath = "s3a://openlake/taxi-data.csv"
    val rdd1: DataFrame = spark.read.option("header", "true").option("inferSchema", "true").csv(dataPath)

    rdd1.printSchema()
    rdd1.show(10)

    val hudiTablePath = "s3a://openlake/hudi/tables/hudi_trips_table"
    val tableName = "hudi_trips_table"
    val basePath = "s3a://openlake/hudi/tables/hudi_trips_table"


    rdd1.write.format("hudi").
      option("hoodie.datasource.write.partitionpath.field", "rate_code").
      option("hoodie.table.name", tableName).
      option("hoodie.datasource.write.hive_style_partitioning","true").
      option("hoodie.datasource.meta.sync.enable", "true").
      option("hoodie.datasource.hive_sync.mode", "hms").
      option("hoodie.datasource.hive_sync.metastore.uris", "thrift://hive-metastore.metastore.svc.cluster.local:9083").
      mode(Overwrite).
      save(basePath)
    println(s"Hudi table written to S3 at: $hudiTablePath")

    val hudiDf: DataFrame = spark.read
      .format("hudi")
      .load(hudiTablePath)

    // Hiển thị dữ liệu
    hudiDf.show(10)
    hudiDf.printSchema()

  }
}
