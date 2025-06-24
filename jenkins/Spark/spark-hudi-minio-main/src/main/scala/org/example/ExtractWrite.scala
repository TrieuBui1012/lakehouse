package org.example

import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SaveMode.Overwrite
import org.apache.spark.sql.functions


object ExtractWrite {
  val logger: Logger = LogManager.getLogger(Extract.getClass)
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Extract Write spark 3.3.2 job builtin")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
      .set("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionCatalog")
      .set("spark.sql.files.ignoreCorruptFiles", "true")
      .set("spark.hadoop.fs.s3a.access.key", "minio")
      .set("spark.hadoop.fs.s3a.secret.key", "minio123")
      .set("spark.hadoop.fs.s3a.endpoint", "https://minio.minio-tenant-1.svc.cluster.local:443")
      .set("spark.hadoop.fs.s3a.path.style.access", "true")

    val spark = SparkSession.builder.config(conf).getOrCreate()

    val dataPath = "s3a://raw/mobile_performance/2024070[1-7]"
    val df: DataFrame = spark.read
      .option("header", "true")
      .option("mergeSchema", "true")
      .option("recursiveFileLookup", "true")
      .option("inferSchema", "true")
      .parquet(dataPath)

    logger.info("SCHEMA: ")
    df.printSchema()

    logger.info("COUNT: ")
    val count = df.count()
    logger.info(s"Total records: $count")

    logger.info("DATA PREVIEW: ")
    df.show(20, truncate=false)

//    println("STATISTICS: ")
//    df.describe().show()

    logger.info("WRITE")
    df.withColumn("date", functions.substring(functions.col("event_timestamp"), 1, 10))
      .write.format("hudi")
      .option("hoodie.datasource.write.partitionpath.field", "date")
      .option("hoodie.table.name", "mobile_performance")
      .option("hoodie.datasource.write.hive_style_partitioning","true")
      .option("hoodie.datasource.meta.sync.enable", "true")
      .option("hoodie.datasource.hive_sync.mode", "hms")
      .option("hoodie.datasource.hive_sync.metastore.uris", "thrift://hive-metastore.metastore.svc.cluster.local:9083")
      .option("hoodie.datasource.write.partitionpath.field", "date")
      .option("hoodie.table.name", "mobile_performance")
      .option("hoodie.write.markers.style", "direct")
      .mode(Overwrite)
      .save("hdfs://voltemon.datalakehkh.viettel.com.vn/tmp")

    println("FINISHED!!!")
    spark.stop()
  }
}
