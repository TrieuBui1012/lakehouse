package org.example

import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.SparkConf


object Extract {
  val logger: Logger = LogManager.getLogger(Extract.getClass)
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Extract spark 3.3.2 job builtin")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
      .set("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionCatalog")
      .set("spark.sql.files.ignoreCorruptFiles", "true")
      .set("spark.hadoop.fs.s3a.access.key", "minio")
      .set("spark.hadoop.fs.s3a.secret.key", "minio123")
      .set("spark.hadoop.fs.s3a.endpoint", "https://minio.minio-tenant-1.svc.cluster.local:443")
      .set("spark.hadoop.fs.s3a.path.style.access", "true")

    val spark = SparkSession.builder.config(conf).getOrCreate()

    val dataPath = "s3a://raw/mobile_performance"
    val df: DataFrame = spark.read
      .option("header", "true")
      .option("mergeSchema", "true")
      .option("recursiveFileLookup", "true")
      .parquet(dataPath)

    logger.info("SCHEMA: ")
    df.printSchema()

    logger.info("COUNT: ")
    val count = df.count()
    logger.info(s"Total records: $count")

    logger.info("DATA PREVIEW: ")
    df.show(20, truncate=false)

    println("STATISTICS: ")
    df.describe().show()

    println("FINISHED!!!")
    spark.stop()
  }
}
