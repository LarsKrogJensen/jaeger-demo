package se.lars.translation

import com.codahale.metrics.SharedMetricRegistries.getOrCreate
import com.uber.jaeger.Configuration
import com.uber.jaeger.Configuration.SamplerConfiguration
import com.uber.jaeger.dropwizard.StatsFactory
import com.uber.jaeger.samplers.ConstSampler
import io.vertx.core.Vertx.vertx
import metrics_influxdb.HttpInfluxdbProtocol
import metrics_influxdb.InfluxdbReporter
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    configureVertxLogging()
    val metricRegistry = getOrCreate("default")
    val reporter = InfluxdbReporter.forRegistry(metricRegistry)
        .protocol(HttpInfluxdbProtocol("core01", 8086, "metrics"))
        .build()
    reporter.start(10, TimeUnit.SECONDS)

    val config = Configuration("OT",
                               SamplerConfiguration(ConstSampler.TYPE, 1),
                               null)
    config.setStatsFactory(StatsFactory(metricRegistry))

    val tracer = config.tracerBuilder
//        .registerInjector(se.lars.common.kafkaHeaderFormat, se.lars.common.KafkaHeaderCodec())
//        .registerExtractor(se.lars.common.kafkaHeaderFormat, se.lars.common.KafkaHeaderCodec())
        .build()

    io.opentracing.util.GlobalTracer.register(tracer)
    val vertx = vertx()


    vertx.deployVerticle(TranslationVerticle(tracer))
}

private fun configureVertxLogging() {
    System.setProperty("vertx.logger-delegate-factory-class-name",
                       "io.vertx.core.logging.SLF4JLogDelegateFactory")
}