package io.throughaglass.semantixsparkchallenge

import java.io.{File, PrintWriter}

import org.apache.spark.rdd._

object SparkChallengeCore {
  def process(rdd: RDD[String], pw: PrintWriter) = {
    println("\n===================================Start Core API with RDD===================================")
    pw.write("\n===================================Start Core API with RDD===================================")
    val startTime = System.currentTimeMillis

    // Número​ ​de​ ​hosts​ ​únicos
    val uniqueHostsNum = getUniqueHosts(rdd)
    println("Número​ ​de​ ​hosts​ ​únicos: " + uniqueHostsNum)
    pw.write("\nNúmero​ ​de​ ​hosts​ ​únicos: " + uniqueHostsNum)

    val reqHttp404 = filterReqByHttp404(rdd)
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
      println("  URL: " + result._2 + "\n   Acessos: " + result._1)
      pw.write("\n  URL: " + result._2 + "\n   Acessos: " + result._1)
    })

    // Quantidade​ ​de​ ​erros​ ​404​ ​por​ ​dia
    val http404ByDay = groupUrlsByDay(reqHttp404)
    println("\nQuantidade​ ​de​ ​erros​ ​404​ ​por​ ​dia: ")
    pw.write("\n\nQuantidade​ ​de​ ​erros​ ​404​ ​por​ ​dia: ")

    http404ByDay.foreach(x => {
      println("  Dia: " + x._1 + "  Quantidade: " + x._2)
      pw.write("\n  Dia: " + x._1 + "  Quantidade: " + x._2)
    })

    // O​ ​total​ ​de​ ​bytes​ ​retornados
    val totalBytes = getSumReqBytes(rdd)
    println("\nO​ ​total​ ​de​ ​bytes​ ​retornados: " + totalBytes.toLong)
    pw.write("\n\nO​ ​total​ ​de​ ​bytes​ ​retornados: " + totalBytes.toLong)


    val totalTime = System.currentTimeMillis - startTime
    println("\nTempo total: %1d ms".format(totalTime))
    pw.write("\n\nTempo total: %1d ms".format(totalTime))

    println("\n===================================End Core API with RDD===================================")
    pw.write("\n===================================End Core API with RDD===================================")
  }

  def getUniqueHosts(rdd: RDD[String]) = {
    rdd.map(line => line.split("\\s")(0))
      .distinct()
      .count()
  }

  def filterReqByHttp404(rdd: RDD[String]) = {
    rdd.filter(!_.contains("alyssa.p"))
       .map(_.split("\\s-\\s-\\s")(1).reverse
             .split("\\s", 2)(1).reverse)
       .filter(_.contains("\" 404"))
       .map(_.reverse
             .split("\\s", 2)(1).reverse)
  }

  def getTop5Http404Urls(rdd: RDD[String])= {
    rdd.map(_.split("\"GET\\s|\"POST\\s|\"HEAD\\s")(1).reverse
             .split("\"", 2)(1).reverse
             .replace(" HTTP/1.0", ""))
       .map((_, 1))
       .reduceByKey(_+_)
       .map(x => (x._2, x._1))
       .sortByKey()
       .top(5)
  }

  def groupUrlsByDay(rdd: RDD[String]) = {
    rdd.map(_.split("]\\s")(0)
             .split("\\[")(1)
             .split(":", 2)(0))
       .map((_, 1))
       .reduceByKey(_+_)
       .collect()
  }

  def getSumReqBytes(rdd: RDD[String]) = {
    rdd.map(_.reverse.split("\\s", 2)(0).reverse)
       .filter(!_.matches("alyssa.p|-"))
       .map(_.toLong)
       .sum()
  }
}
