package se.lars.ofe

import io.opentracing.Tracer
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.consumer.KafkaConsumerRecord
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import se.lars.common.delay
import se.lars.common.kafkaHeaderFormat

class OFEVerticle(private val tracer: Tracer) : AbstractVerticle() {
    private lateinit var consumer: KafkaConsumer<String, JsonObject>
    private lateinit var producer: KafkaProducer<String, JsonObject>

    override fun start() {
        val consumerProps = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            VALUE_DESERIALIZER_CLASS_CONFIG to "io.vertx.kafka.client.serialization.JsonObjectDeserializer",
            ENABLE_AUTO_COMMIT_CONFIG to "true",
            GROUP_ID_CONFIG to "ofe-1"
        )
        consumer = KafkaConsumer.create<String, JsonObject>(vertx, consumerProps)
        consumer.handler(this::handleEvent)
        consumer.subscribe("ot")

        val producerProps = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.ACKS_CONFIG to "1",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "io.vertx.kafka.client.serialization.JsonObjectSerializer"
        )
        producer = KafkaProducer.create<String, JsonObject>(vertx, producerProps)
    }

    private fun handleEvent(record: KafkaConsumerRecord<String, JsonObject>) {
        tracer.buildSpan("processOddChange")
            .asChildOf(tracer.extract(kafkaHeaderFormat, record.record().headers()))
            .startActive()
            .use {
                println("Processing key=${record.key()},value=${record.value()},partition=${record.partition()},offset=${record.offset()},headers=${record.record().headers()}")
                processData()
            }
    }


    private fun sendToPush(message: Message<JsonObject>) {
        val record = KafkaProducerRecord.create<String, JsonObject>("push", "aa", message.body())
        tracer.inject(tracer.activeSpan().context(), kafkaHeaderFormat, record.record().headers())
        producer.write(record)
    }


    private fun processData() {
        tracer.buildSpan("processData")
            .startActive()
            .use {
                delay()
            }
    }
}
