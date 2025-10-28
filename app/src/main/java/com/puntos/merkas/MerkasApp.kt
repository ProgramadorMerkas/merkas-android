package com.puntos.merkas

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MerkasApp : Application() {
    override fun onCreate() {
        super.onCreate()

        MapLibre.getInstance(
            this,
            "pJD8cJKKgMxqpoeJulK5",
            WellKnownTileServer.MapTiler
        )
    }
}