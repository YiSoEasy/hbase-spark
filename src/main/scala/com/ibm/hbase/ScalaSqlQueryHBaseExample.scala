package com.ibm.hbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.datasources.hbase.HBaseTableCatalog
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by yliang on 1/11/17.
  * This is an example to run spark sql on exist hbase table
  * Use spark-submit to run this program, notice that this
  * program will query table t1 in hbase, need to create t1
  * before running this program
  */
object ScalaSqlQueryHBaseExample {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("HBaseSparkSqlExample ")
    val sc = new SparkContext(sparkConf)
    val conf = HBaseConfiguration.create()
    new HBaseContext(sc, conf)
    val sqlContext: SQLContext = new SQLContext(sc)

    def hbaseTable1Catalog =
      s"""{
          |"table":{"namespace":"default", "name":"t1"},
          |"rowkey":"key",
          |"columns":{
          |"KEY_FIELD":{"cf":"rowkey", "col":"key", "type":"string"},
          |"A_FIELD":{"cf":"c", "col":"a", "type":"string"},
          |"B_FIELD":{"cf":"c", "col":"b", "type":"string"}
          |}
          |}""".stripMargin

    val df: DataFrame = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map(HBaseTableCatalog.tableCatalog -> hbaseTable1Catalog))
    df.registerTempTable("hbaseTable1")

    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "(KEY_FIELD = 'get1' or KEY_FIELD = 'get2' or KEY_FIELD = 'get3')")

    results.show
  }
}
