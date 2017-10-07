package se.lars.common

import com.uber.jaeger.SpanContext
import com.uber.jaeger.propagation.Extractor
import com.uber.jaeger.propagation.Injector
import io.opentracing.propagation.Format
import org.apache.kafka.common.header.Headers
import java.util.*

val kafkaHeaderFormat = object : Format<Headers> {}

private const val SPAN_CONTEXT_KEY = "uber-trace-id"
private const val BAGGAGE_KEY_PREFIX = "uberctx-"

class KafkaHeaderCodec : Injector<Headers>, Extractor<Headers> {
    override fun extract(headers: Headers): SpanContext? {
        var context: SpanContext? = null
        val baggage: MutableMap<String, String> = mutableMapOf();

        for (header in headers) {
            val key = header.key().toLowerCase(Locale.ROOT)
            if (key == SPAN_CONTEXT_KEY) {
                context = SpanContext.contextFromString(String(header.value())).withBaggage(baggage)
            } else if (key.startsWith(BAGGAGE_KEY_PREFIX)) {
                baggage.put(header.key().removePrefix(BAGGAGE_KEY_PREFIX), String(header.value()))
            }
        }
        return context
    }

    override fun inject(spanContext: SpanContext, carrier: Headers) {
        carrier.add(SPAN_CONTEXT_KEY, spanContext.contextAsString().toByteArray())
        for ((key, value) in spanContext.baggageItems()) {
            carrier.add(BAGGAGE_KEY_PREFIX + key, value.toByteArray())
        }
    }
}
