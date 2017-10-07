package se.lars.ot

//import com.uber.jaeger.Configuration

import com.codahale.metrics.SharedMetricRegistries
import com.uber.jaeger.dropwizard.Configuration
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
        .protocol(HttpInfluxdbProtocol("http", "core01", 8086, "admin", "53CR3TP455W0RD", "metrics"))
        .build()
    reporter.start(10, TimeUnit.SECONDS)

    val config = Configuration("OT",
                               false,
                               com.uber.jaeger.Configuration.SamplerConfiguration(ConstSampler.TYPE, 1),
                               null)
    config.setMetricRegistry(metricRegistry)

    val tracer = config.tracerBuilder
        .registerInjector(kafkaHeaderFormat, KafkaHeaderCodec())
        .registerExtractor(kafkaHeaderFormat, KafkaHeaderCodec())
        .build()

    GlobalTracer.register(tracer)
    val vertx = Vertx.vertx()

    vertx.deployVerticle(OTVerticle(tracer))
    vertx.deployVerticle(DriverVerticle())
}

private fun configureVertxLogging() {
    System.setProperty("vertx.logger-delegate-factory-class-name",
                       "io.vertx.core.logging.SLF4JLogDelegateFactory")
}
