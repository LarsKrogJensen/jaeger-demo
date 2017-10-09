package se.lars.translation

import io.grpc.stub.StreamObserver
import io.opentracing.Tracer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.grpc.VertxServerBuilder
import se.lars.common.logger
import se.lars.translation.TranslationGrpc.TranslationImplBase


class TranslationVerticle(private val tracer: Tracer) : AbstractVerticle() {
    private val log = logger<TranslationVerticle>()

    override fun start(startFuture: Future<Void>) {
        val rpcServer = VertxServerBuilder
            .forAddress(vertx, "localhost", 8080)
            .addService(TranslationService())
            .build()

        // Start is asynchronous
        rpcServer.start { result ->
            if (result.succeeded()) {
                log.info("Translation Grpc service started")
                startFuture.succeeded()
            }
            else {
                log.error("Failed to start grpc service")
                startFuture.fail(result.cause())
            }

        }
    }
}

class TranslationService : TranslationImplBase() {
    private val log = logger<TranslationService>()

    override fun translate(request: TranslationRequest, responseObserver: StreamObserver<TranslationReply>) {
        log.info("Translating")
        responseObserver.onNext(TranslationReply.newBuilder().setTest("hej hoopp").build())
    }
}
