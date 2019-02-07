package com.kaplanbora.models


case class SimilarArtistName(name: String)

case class Tag(name: String)

case class Stats(listeners: String, playcount: String)

case class Artist(name: String,
                  mbid: String,
                  stats: Stats,
                  similarArtists: Seq[SimilarArtistName],
                  tags: Seq[Tag])

case class CountedArtist(artist: String, count: Long)

case class MinifiedArtist(name: String,
                          listeners: Long,
                          tags: Seq[String],
                          similarArtists: Seq[String])

case class EnrichedArtist(name: String,
                          count: Long,
                          listeners: Long,
                          tags: Seq[String],
                          similarArtists: Seq[String])
