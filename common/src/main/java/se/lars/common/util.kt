package se.lars.common

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

fun delay(from: Int = 100, to: Int = 500) {
    val random = Random()

    Thread.sleep((random.nextInt(to - from) + from).toLong())
}

inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)


inline fun <reified T : Any> Vertx.deploy(options: DeploymentOptions) {
    this.deployVerticle(T::class.java.name, options)
}

inline fun <reified T : Any> Vertx.deploy() {
    this.deployVerticle(T::class.java.name)
}