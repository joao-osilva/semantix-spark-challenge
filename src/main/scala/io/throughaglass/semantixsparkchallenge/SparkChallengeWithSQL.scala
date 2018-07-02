package io.throughaglass.semantixsparkchallenge

import java.io.{File, PrintWriter}

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object SparkChallengeWithSQL {
  case class HttpData(host: String, timestamp: String, requestUrl: String, requestStatus: String, requestBytes: String)

  def mapToHttpData(line: String) = {
    if (line.split("\\s").length > 1) {
      val host = line.split(" - - \\[")(0)

      val timestamp = line.split(" - - \\[")(1)
                          .split("]")(0)
                          .split(":")(0)

      val requestUrl = line.split("] \"")(1).reverse
                           .split("\\s", 2)(1)
                           .split("\\s", 2)(1).reverse
                           .replace("GET", "")
                           .replace("POST", "")
                           .replace("HEAD", "")
                           .replace("HTTP/1.0\"", "")
                           .trim()

      val requestStatus = line.reverse.split("\\s")(1)
                                      .split("\\s")(0).reverse

      val requestBytes = line.reverse.split("\\s")(0).reverse

      HttpData(host, timestamp, requestUrl, requestStatus, requestBytes)
    } else {
      HttpData("invalid_data", "-", "-", "-", "-")
    }
  }

  def process(rdd: RDD[String], spark: SparkSession, pw: PrintWriter) = {
    println("\n===================================Start SQL API with Dataset===================================")
    pw.write("\n===================================Start SQL API with Dataset===================================")
    val startTime = System.currentTimeMillis

    val httpData = rdd.map(mapToHttpData)

    import spark.implicits._
    val httpDataDS = httpData.toDS

    // Número​ ​de​ ​hosts​ ​únicos
    val uniqueHostsNum = getUniqueHosts(httpDataDS)
    println("Número​ ​de​ ​hosts​ ​únicos: " + uniqueHostsNum.length)
    pw.write("\nNúmero​ ​de​ ​hosts​ ​únicos: " + uniqueHostsNum.length)

    val reqHttp404 = filterReqByHttp404(httpDataDS)
    reqHttp404.cache()

    // O​ ​total​ ​de​ ​erros​ ​404
    val errors404Num = reqHttp404.count()
    println("\nO​ ​total​ ​de​ ​erros​ ​404: " + errors404Num)
    pw.write("\n\nO​ ​total​ ​de​ ​erros​ ​404: " + errors404Num)

    // Os​ ​5​ ​URLs​ ​que​ ​mais​ ​causaram​ ​erro​ ​404
    val top5Http404Urls = getTop5Http404Urls(reqHttp404)
    println("\nAs​ ​5​ ​URLs​ ​que​ ​mais​ ​causaram​ ​erro​ ​404: ")
    pw.write("\n\nAs​ ​5​ ​URLs​ ​que​ ​mais​ ​causaram​ ​erro​ ​404: ")

    top5Http404Urls.foreach(result => {
      println("  URL: " + result(1) + "\n   Acessos: " + result(0))
      pw.write("\n  URL: " + result(0) + "\n   Acessos: " + result(1))
    })

    // Quantidade​ ​de​ ​erros​ ​404​ ​por​ ​dia
    val errorsByDay = groupUrlsByDay(reqHttp404)

    println("\nQuantidade​ ​de​ ​erros​ ​404​ ​por​ ​dia: ")
    pw.write("\n\nQuantidade​ ​de​ ​erros​ ​404​ ​por​ ​dia: ")

    errorsByDay.foreach(result => {
      println("  Dia: " + result(0) + "  Quantidade: " + result(1))
      pw.write("\n  Dia: " + result(0) + "  Quantidade: " + result(1))
    })

    // O​ ​total​ ​de​ ​bytes​ ​retornados
    val totalBytes = getSumReqBytes(httpDataDS)
    println("\nO​ ​total​ ​de​ ​bytes​ ​retornados: " + totalBytes(0).getDouble(0).toLong)
    pw.write("\n\nO​ ​total​ ​de​ ​bytes​ ​retornados: " + totalBytes(0).getDouble(0).toLong)


    val totalTime = System.currentTimeMillis - startTime
    println("\nTempo total: %1d ms".format(totalTime))
    pw.write("\n\nTempo total: %1d ms".format(totalTime))

    println("\n===================================End SQL API with Dataset===================================")
    pw.write("\n===================================End SQL API with Dataset===================================")
  }

  def getUniqueHosts(ds: Dataset[HttpData]) = {
    ds.groupBy("host").count().collect()
  }

  def filterReqByHttp404(ds: Dataset[HttpData]) = {
      ds.filter("requestStatus = 404")
        .select("requestUrl", "timestamp")
  }

  def getTop5Http404Urls(ds: DataFrame)= {
    ds.groupBy("requestUrl")
      .count()
      .orderBy(desc("count"))
      .take(5)
  }

  def groupUrlsByDay(ds: DataFrame) = {
    ds.groupBy("timestamp")
      .count()
      .orderBy(asc("timestamp"))
      .collect()
  }

  def getSumReqBytes(ds: Dataset[HttpData]) = {
    ds.agg(sum("requestBytes"))
      .collect()
  }
}
