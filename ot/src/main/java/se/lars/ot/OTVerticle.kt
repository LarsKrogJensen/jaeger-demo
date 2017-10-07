package se.lars.ot

import io.opentracing.Tracer
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import org.apache.kafka.clients.producer.ProducerConfig.*
import se.lars.common.delay
import se.lars.common.kafkaHeaderFormat

class OTVerticle(private val tracer: Tracer) : AbstractVerticle() {
    private lateinit var producer: KafkaProducer<String, JsonObject>

    override fun start() {

        vertx.eventBus().consumer<JsonObject>("ot") {
            handleEvent(it)
        }

        val props = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ACKS_CONFIG to "1",
            KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
            VALUE_SERIALIZER_CLASS_CONFIG to "io.vertx.kafka.client.serialization.JsonObjectSerializer"
        )
        producer = KafkaProducer.create<String, JsonObject>(vertx, props)
    }

    private fun handleEvent(message: Message<JsonObject>) {
        tracer.buildSpan("handleBetOfferChange")
            .withTag("betofferid", message.body().getInteger("betofferId"))
            .startActive()
            .use { span ->
                println("received msg ${message.body()} span ${span}")
                queryDb();
                delay(10, 100)
                processData()
                sendToOfe(message)
            }
    }

    private fun sendToOfe(message: Message<JsonObject>) {
        val record = KafkaProducerRecord.create<String, JsonObject>("ot", "aa", message.body())
        tracer.inject(tracer.activeSpan().context(), kafkaHeaderFormat, record.record().headers())
        producer.write(record)
    }

    private fun queryDb() {
        tracer.buildSpan("queryDB")
            .startActive()
            .use {
                delay()
            }

    }

    private fun processData() {
        tracer.buildSpan("processData")
            .startActive()
            .use {
                delay()
            }
    }
}
