package com.jwheeler.data.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

class DataVerticle : AbstractVerticle() {

    override fun start(fut: Future<Void>) {
        val router = Router.router(vertx)

        router.route("/").handler { routingContext ->
            val response = routingContext.response()
            response
                    .putHeader("content-type", "text/html")
                    .sendFile("assets/index.html")
        }

        router.route("/static/*").handler(StaticHandler.create("assets/static"))
        router.route("/*").handler(StaticHandler.create("assets"))

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8080)!!
                ) { result ->
                    if (result.succeeded()) {
                        fut.complete()
                    } else {
                        fut.fail(result.cause())
                    }
                }
    }
}