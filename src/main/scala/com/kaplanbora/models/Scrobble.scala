package com.kaplanbora.models


case class Scrobble(artist: String,
                    artist_mbid: String,
                    album: String,
                    album_mbid: String,
                    track: String,
                    track_mbid: String,
                    uts: Long,
                    utc_time: String)

