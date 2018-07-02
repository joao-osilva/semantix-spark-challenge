package io.throughaglass.semantixsparkchallenge

import java.io.{File, PrintWriter}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import scala.util.Failure

object SparkChallengeApp extends App{
  val inputFile = if(args.isEmpty) "./NASA_access_log_*" else args(0)
print(inputFile)
  Runner.run(inputFile)
}

object Runner {
  def run(inputFile: String): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val pw = new PrintWriter(new File("resultados.txt" ), "UTF-8")
    println("Lendo arquivos: " + inputFile  + "\n")
    pw.write("Lendo arquivos: " + inputFile + "\n")

    val spark = SparkSession.builder()
                            .appName("SemantixSparkChallenge")
                            .master("local[*]")
                            .getOrCreate()

    try {
      val rdd = spark.sparkContext.textFile(inputFile)

      // using core API with RDD
      SparkChallengeCore.process(rdd, pw)

      // using SQL API with Dataset
      SparkChallengeWithSQL.process(rdd, spark, pw)
    } catch {
      case ex: Exception => {
        println(s"Unknown exception: $ex")
        pw.write(s"\nUnknown exception: $ex")
        Failure(ex)
      }
    } finally {
      spark.close()
      pw.close()
    }
  }
}
