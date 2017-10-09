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
            .forAddress(vertx, "0.0.0.0", 8080)
            .addService(TranslationService())
            .build()

        // Start is asynchronous
        rpcServer.start { result ->
            if (result.succeeded()) {
                println("Translation Grpc service started")
                startFuture.succeeded()
            }
            else {
                println("Failed to start grpc service")
                startFuture.fail(result.cause())
            }

        }
    }
}

class TranslationService : TranslationImplBase() {
    override fun translate(request: TranslationRequest, responseObserver: StreamObserver<TranslationReply>) {
        responseObserver.onNext(TranslationReply.newBuilder().setTest("hej hoopp").build())
    }
}
