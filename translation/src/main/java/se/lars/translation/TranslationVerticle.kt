package se.lars.translation

import io.opentracing.Tracer
import io.vertx.core.AbstractVerticle

class TranslationVerticle(private val tracer: Tracer) : AbstractVerticle() {

    override fun start() {
        
    }
}
