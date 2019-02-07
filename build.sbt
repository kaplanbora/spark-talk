name := "Spark Template"
scalaVersion := "2.11.12"

val sparkCore      = "org.apache.spark" %% "spark-core"      % "2.4.0"
val sparkSql       = "org.apache.spark" %% "spark-sql"       % "2.4.0"

libraryDependencies ++= Seq(
  sparkCore,
  sparkSql
)
