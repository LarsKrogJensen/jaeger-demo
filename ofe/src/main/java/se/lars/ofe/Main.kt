package se.lars.ofe

import com.codahale.metrics.SharedMetricRegistries
import com.uber.jaeger.Configuration
import com.uber.jaeger.Configuration.SamplerConfiguration
import com.uber.jaeger.dropwizard.StatsFactory
import com.uber.jaeger.samplers.ConstSampler
import io.opentracing.util.GlobalTracer
import io.vertx.core.Vertx
import metrics_influxdb.HttpInfluxdbProtocol
import metrics_influxdb.InfluxdbReporter
import se.lars.common.KafkaHeaderCodec
import se.lars.common.kafkaHeaderFormat
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    configureVertxLogging()
    val metricRegistry = SharedMetricRegistries.getOrCreate("default")
    val reporter = InfluxdbReporter.forRegistry(metricRegistry)
        .protocol(HttpInfluxdbProtocol("core01", 8086, "metrics"))
        .build()
    reporter.start(10, TimeUnit.SECONDS)

    val ofeId = args[0]
    val config = Configuration(ofeId,
                               SamplerConfiguration(ConstSampler.TYPE, 1),
                               null)
    config.setStatsFactory(StatsFactory(metricRegistry))

    val tracer = config.tracerBuilder
        .registerInjector(kafkaHeaderFormat, KafkaHeaderCodec())
        .registerExtractor(kafkaHeaderFormat, KafkaHeaderCodec())
        .build()

    GlobalTracer.register(tracer)
    val vertx = Vertx.vertx()


    vertx.deployVerticle(OFEVerticle(tracer, ofeId))
}

private fun configureVertxLogging() {
    System.setProperty("vertx.logger-delegate-factory-class-name",
                       "io.vertx.core.logging.SLF4JLogDelegateFactory")
}
