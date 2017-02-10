package com.ibm.hbase

/**
  * Created by yliang on 2/9/17.
  */
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.spark.SparkContext
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Put
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.SparkConf

/**
  * This is a simple example of BulkPut with Spark Streaming
  *
  * Usage:
  * open two terminal, and log into same cluster
  * In terminal 1, run nc -lk 9999
  * In terminal 2, run spark-submit command to run this program
  * Back to terminal 1, and input data, the data will be inserted into HBase
  *
  */
object ScalaHBaseStreamingBulkPutExample {
  def main(args: Array[String]) {
    if (args.length < 4) {
      println("HBaseStreamingBulkPutExample " +
        "{host} {port} {tableName} {columnFamily} are missing an argument")
      return
    }

    val host = args(0)
    val port = args(1)
    val tableName = args(2)
    val columnFamily = args(3)

    val sparkConf = new SparkConf().setAppName("HBaseStreamingBulkPutExample " +
      tableName + " " + columnFamily)
    val sc = new SparkContext(sparkConf)
    try {
      val ssc = new StreamingContext(sc, Seconds(1))

      val lines = ssc.socketTextStream(host, port.toInt)

      val conf = HBaseConfiguration.create()

      val hbaseContext = new HBaseContext(sc, conf)

      hbaseContext.streamBulkPut[String](lines,
        TableName.valueOf(tableName),
        (putRecord) => {
          if (putRecord.length() > 0) {
            val put = new Put(Bytes.toBytes(putRecord))
            put.addColumn(Bytes.toBytes("c"), Bytes.toBytes("foo"), Bytes.toBytes("bar"))
            put
          } else {
            null
          }
        })
      ssc.start()
      ssc.awaitTerminationOrTimeout(60000)
    } finally {
      sc.stop()
    }
  }
}

