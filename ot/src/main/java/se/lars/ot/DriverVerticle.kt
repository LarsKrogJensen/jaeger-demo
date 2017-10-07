package se.lars.ot

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.kotlin.core.json.JsonObject

class DriverVerticle : AbstractVerticle() {

    override fun start(startFuture: Future<Void>) {

        vertx.setPeriodic(500) {
            val msg = JsonObject("betofferId" to 123)
            vertx.eventBus().publish("ot", msg)
        }
    }
}
