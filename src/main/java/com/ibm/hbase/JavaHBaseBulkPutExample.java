package com.ibm.hbase;

/**
 * Created by yliang on 2/2/17.
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

/**
 * This is a simple example of putting records in HBase
 * with the bulkPut function.
 */
public class JavaHBaseBulkPutExample {


  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("JavaHBaseBulkPutExample  " +
          "{tableName} {columnFamily}");
      return;
    }

    String tableName = args[0];
    String columnFamily = args[1];

    SparkConf sparkConf = new SparkConf().setAppName("JavaHBaseBulkPutExample " + tableName);
    JavaSparkContext jsc = new JavaSparkContext(sparkConf);

    try {
      List<String> list = new ArrayList<String>(5);
      list.add("1," + columnFamily + ",a,1");
      list.add("2," + columnFamily + ",a,2");
      list.add("3," + columnFamily + ",a,3");
      list.add("4," + columnFamily + ",a,4");
      list.add("5," + columnFamily + ",a,5");

      JavaRDD<String> rdd = jsc.parallelize(list);

      Configuration conf = HBaseConfiguration.create();

      JavaHBaseContext hbaseContext = new JavaHBaseContext(jsc, conf);

      hbaseContext.bulkPut(rdd,
          TableName.valueOf(tableName),
          new PutFunction());
    } finally {
      jsc.stop();
    }
  }

  public static class PutFunction implements Function<String, Put> {

    private static final long serialVersionUID = 1L;

    public Put call(String v) throws Exception {
      String[] cells = v.split(",");
      Put put = new Put(Bytes.toBytes(cells[0]));

      put.addColumn(Bytes.toBytes(cells[1]), Bytes.toBytes(cells[2]),
          Bytes.toBytes(cells[3]));
      return put;
    }

  }
}
