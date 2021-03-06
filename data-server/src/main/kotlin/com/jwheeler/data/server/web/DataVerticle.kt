package com.jwheeler.data.server.web

import com.jwheeler.data.server.external.Valve
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler

class DataVerticle : AbstractVerticle() {

    private val valve = Valve()

    override fun start(fut: Future<Void>) {
        val router = Router.router(vertx)

        router.route("/").handler { routingContext ->
            val response = routingContext.response()
            response
                    .putHeader("content-type", "text/html")
                    .sendFile("assets/index.html")
        }

        router.get("/api/valve").handler(this::getValve)
        router.route("/static/*").handler(StaticHandler.create("assets/static"))
        router.route("/*").handler(StaticHandler.create("assets"))

        valve.subscribe()

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

    private fun getValve(routingContext: RoutingContext) {
        routingContext.response()
                .putHeader("content-type", "application-json; charset=utf-8")
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .end(Json.encodePrettily(ValveInfo(valve.valveStatus)))
    }
}