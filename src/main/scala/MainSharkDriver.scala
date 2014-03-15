package spark.starter


import org.apache.spark._
import SparkContext._
import scala.util.parsing.json._
import scala.io.Source
import shark._

object MainSparkDriver {

  def main(args: Array[String]) {

	val WAREHOUSE_PATH = "/tmp/sharkWarehouse" // Stored on HDFS
	val METASTORE_PATH = "/tmp/sharkMetaStore" // Stored on local

	val sc = SharkEnv.initWithSharkContext("TestRunner: " + "Shark", "spark://master:7077")

	// Set the environment settings
	sc.runSql("set javax.jdo.option.ConnectionURL=jdbc:derby:;databaseName=" + METASTORE_PATH + ";create=true")
	sc.runSql("set hive.metastore.warehouse.dir=" + WAREHOUSE_PATH)

	sc.sql("drop table if exists src11")
	sc.sql("CREATE EXTERNAL TABLE src11(key INT, value STRING)")
	sc.sql("LOAD DATA LOCAL INPATH '${env:HIVE_HOME}/examples/files/kv1.txt' INTO TABLE src11")
	sc.runSql("drop table if exists src11_cached")
  sc.runSql("CREATE TABLE src11_cached AS SELECT * FROM src11")

	// runSql returns a type ResultSet
	println("Count from shark server"+sc.runSql("SELECT COUNT(*) FROM src11"))

	//TableRDD
	val rawRDD = sc.sql2rdd("SELECT * FROM src11_cached")
	println("rawRDD Type="+rawRDD)
	println("Number of entries in RDD="+rawRDD.count) 

	///MappedRDD
	val normalRDD = rawRDD.map(x => (x.getInt(0),x.getString(1)))
	println("normalRDD Type="+normalRDD)
  println("Number of entries in normalRDD="+normalRDD.count)	

	// Print the output to the console
	println("Printing SELECT * FROM src11_cached limit 10 to console with a limit of 5")
	sc.sql2console("SELECT * FROM src11_cached limit 10",5)	
  }
}
