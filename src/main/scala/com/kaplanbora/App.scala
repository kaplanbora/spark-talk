package com.kaplanbora

import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import com.kaplanbora.models._


object App {
  val spark: SparkSession = SparkSession.builder()
    .master("local[*]")
    .appName("sparktalk-lastfm")
    .getOrCreate()

  import spark.implicits._

  def readScrobbles(location: String): Dataset[Scrobble] = 
    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(location)
      .na.fill("N/A")
      .as[Scrobble]

  def readArtists(location: String): Dataset[Artist] = 
    spark.read
      .json(location)
      .selectExpr(
        "artist.name",
        "artist.mbid",
        "artist.stats",
        "artist.similar.artist as similarArtists",
        "artist.tags.tag as tags"
      )
      .na.drop()
      .as[Artist]
  
  def findTopArtistsRDD(scrobbles: Dataset[Scrobble]): RDD[CountedArtist] = 
    scrobbles.rdd
      .filter(scrobble => scrobble.utc_time.contains("2018"))
      .map(scrobble => (scrobble.artist, 1))
      .reduceByKey((count1, count2) => count1 + count2)
      .sortBy ({ case (artist, count) => count }, ascending = false)
      .map { case (artist, count) => CountedArtist(artist, count) }

  def findTopArtistsDataset(scrobbles: Dataset[Scrobble]): Dataset[CountedArtist] = 
    scrobbles
      .filter(scrobble => scrobble.utc_time.contains("2018"))
      .groupByKey(_.artist)
      .count
      .select($"value".as("artist"), $"count(1)".as("count"))
      .as[CountedArtist]
      .orderBy($"count".desc)

  def findTopArtistsDataframe(scrobbles: Dataset[Scrobble]): DataFrame = 
    scrobbles
      .where($"utc_time".contains(lit("2018")))
      .groupBy($"artist")
      .count
      .orderBy($"count".desc)

  def minifyArtists(artists: Dataset[Artist]): Dataset[MinifiedArtist] = 
    artists.map { artist =>
      MinifiedArtist(
        artist.name,
        artist.stats.listeners.toLong,
        artist.tags.map(_.name),
        artist.similarArtists.map(_.name)
      )
    }

  def main(args: Array[String]): Unit = {
    val scrobbles: Dataset[Scrobble] = readScrobbles("scrobbles.csv")
    scrobbles.printSchema()
    scrobbles.show()

    val artists: Dataset[Artist] = readArtists("artistinfo.json")
    artists.show()

    val topArtists: Dataset[CountedArtist] = findTopArtistsDataset(scrobbles)
    topArtists.show()

    val minifiedArtists: Dataset[MinifiedArtist] = minifyArtists(artists)
    minifiedArtists.show()

    val enrichedArtists: Dataset[EnrichedArtist] =
      topArtists
        .join(minifiedArtists, $"artist" === $"name")
        .orderBy($"count".desc)
        .as[EnrichedArtist]

    enrichedArtists.show()

    enrichedArtists
      .map(artist => (artist.name, artist.similarArtists.head))
      .take(10)
      .foreach(r => println(s"Artist: ${r._1}, Recommendation: ${r._2}"))
  }
}
