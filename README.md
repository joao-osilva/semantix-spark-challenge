# Semantix Spark Challenge

## Questões
#### 1. Qual​ ​o​ ​objetivo​ ​do​ ​comando​ ​​cache​ ​​em​ ​Spark?

O cache é utilizado como uma forma de otimizar o acesso a um RDD que é utilizado várias vezes. Sem o cache o RDD será reprocessado toda vez que uma ação for executa nele. É possivel utilizar cache em um RDD de duas maneiras:

  - `cache()`: persiste o RDD em memória
  - `persist(level: StorageLevel)`: persiste o RDD de acordo com o StorageLevel especificado(ex: memória, disco, memória/disco)

Alguns dos cenários interessantes para utilização são:

  - Reutilização do RDD de maneira iterativa por algoritmos de ML
  - Reutilização do RDD por aplicação Spark standalone
  - Quando o processamento do RDD é custoso, seja buscando os dados na rede ou realizando transformações

#### 2. O​ ​mesmo​ ​código​ ​implementado​ ​em​ ​Spark​ ​é​ ​normalmente​ ​mais​ ​rápido​ ​que​ ​a​ ​implementação​ ​equivalente​ ​em MapReduce.​ ​Por​ ​quê?

Isso ocorre por dois motivos:

  - O Spark realiza todo o processamento em memória, o que evita operações desnecessárias/custosas de I/O no disco
  - A engine de processamento do Spark utiliza um modelo avançado de DAG(directed acyclic graph) que permite que cada job tenha vários passos, algo muito comum em processamentos complexos ou iterativos. O modelo MapReduce divide o processamento em 2 passos, map e reduce, que fazem apenas um nivel de agregação no dado e salvam em disco. Cada rodada de execução do MapReduce é independente, o que o torna ineficiente para algoritmos/processamentos iterativos já o resultado tem que ser lido/escrito no filesystem. O DAG utilizado pelo Spark leva em consideração todos os passos necessário para o processamento, podendo assim otimiza-los globalmente, como por exemplo salvar os resultados intermediários em memória.

#### 3. Qual​ ​é​ ​a​ ​função​ ​do​ ​​SparkContext​?

O SparkContext é criado por toda aplicação Spark e representa a conexão com um cluster, que pode ser um cluster Spark ou um gerenciador de recurso como o YARN/Mesos/Kubernetes

algumas de suas caracteristicas são:

  - Apenas um SparkContext pode estar ativo por JVM
  - É necessário parar um SparkContext antes de começar outro

Após estabelecida a conexão, o Spark manda a aplicação para os nós que irão executa-la, e por fim o SparkContext envia as tarefas para que os nós as executem.


#### 4. Explique​ ​com​ ​suas​ ​palavras​ ​​ ​o​ ​que​ ​é​ ​​Resilient​ ​Distributed​ ​Datasets​​ ​(RDD).

É um conjunto de dados/elementos que estão divididos em vários nós de um cluster e podem ser utilizados em paralelo, eles podem ser criados de duas maneiras: paralelizando uma coleção já existente no seu programa, ou através de referencia a um repositório externo(HDFS) de dados.

#### 5. GroupByKey​ ​​é​ ​menos​ ​eficiente​ ​que​ ​​reduceByKey​ ​​em​ ​grandes​ ​dataset.​ ​Por​ ​quê?

A função reduceByKey faz a combinação de pares com a mesma chaves na mesma máquina, realizando assim uma pré-agregação antes de realizar o embaralhamento. Já a função groupByKey realiza o embaralhamento direto(sem a pré-agregação), o que acaba aumentando consideravelmente a quantidade de dados trafegado na rede, e prejudica a performance quando temos um dataset grande.

#### 6. Explique​ ​o​ ​que​ ​o​ ​código​ ​Scala​ ​abaixo​ ​faz.
  ```scala
    val​​ ​​textFile​​ ​​=​​ ​​sc​.​textFile​(​"hdfs://..."​)
    val​​ ​​counts​​ ​​=​​ ​​textFile​.​flatMap​(​line​​ ​​=>​​ ​​line​.​split​(​"​ ​"​))
                         .​map​(​word​​​​=>​​​​(​word​,​​​​1​))
                         .​reduceByKey​(​_​​ ​​+​​ ​​_​)
    counts​.​saveAsTextFile​(​"hdfs://..."​)
  ```

Descrição do código:
  - Utiliza o SparkContext para ler um arquivo no HDFS e transforma-lo em um RDD
  - Lê cada linha do arquivo e as separa por espaço
  - Para cada palavra encontrada está criando uma tuple e mapeando o número 1
  - Agrega todos os valores para encontrar o número de ocorrências de cada chave
  - Salva o resultado em um arquivo de texto no HDFS

## Pré-requisitos

A solução foi desenvolvida utilizando:
  - Scala 2.11.8
  - Apache Spark 2.3.1
  - Sbt 1.1.6

Portanto para executa-lá é necessário instalar:
  - **Java**
    - Verifique se já o possui:

      `$ java -version`

    - Caso não, faça o download [aqui](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

    - Configure a váriavel de ambiente **JAVA_HOME** apontando para o diretório de instalação do Java

  - **Scala**
    - No MacOS instale utilizando o gerenciador de pacotes homebrew:

      `$ brew install scala`

    - Caso utilize outro SO siga os passos [aqui](https://www.scala-lang.org/download/)

  - **Apache Spark**
    - No MacOS instale utilizando o gerenciador de pacotes homebrew:

      `$ brew install apache-spark`

    - Caso utilize outro SO siga os passos [aqui](https://spark.apache.org/downloads.html)

  - **Sbt**
    - No MacOS instale utilizando o gerenciador de pacotes homebrew:

      `$ brew install sbt`

    - Caso utilize outro SO siga os passos [aqui](https://www.scala-sbt.org/download.html)

Faça o download dos arquivos com os dados que serão utilizados:
  - [NASA_access_log_Jul95.gz​](http://ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz)

  - [NASA_access_log_Aug95.gz](http://ita.ee.lbl.gov/traces/NASA_access_log_Aug95.gz)

## Construa

Clone o repositório

  `$ git clone https://github.com/joao-osilva/semantix-spark-challenge.git`

Vá até o diretório criado:

  `$ cd semantix-spark-challenge`

Compile e construa o artefato:

  `$ sbt compile assembly`

Ele será gerado em `target/scala-2.11/semantix-spark-challenge-assembly-x.x.jar`

## Execute

Copie o artefato e os arquivos de dado para o mesmo diretório:

  ```
    /home/temp
    _
    |__semantix-spark-challenge-assembly-x.x.jar
    |__NASA_access_log_Jul95.gz
    |__NASA_access_log_Aug95.gz
  ```

Envie a aplicação para o cluster Spark local utilizando o script `bin/spark-submit`(localizado no diretório de instalação do Spark), passando como parâmetro o artefato e o diretório dos arquivos:

  ```
  $ spark-submit \
     semantix-spark-challenge-assembly-x.x.jar \
     "/home/temp/NASA_access_log_*"
  ```  

Observações
  - Utilize o "*" no final do caminho para ler todos os arquivos no diretório
  - Caso nenhum parâmetro seja informado, o programa irá buscar no caminho relativo arquivos com o prefixo "*NASA_access_log_*"

Os resultados da execução podem ser encontrados no arquivo `resultados.txt`
