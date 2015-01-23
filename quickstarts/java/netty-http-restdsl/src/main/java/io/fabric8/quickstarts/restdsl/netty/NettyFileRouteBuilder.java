package io.fabric8.quickstarts.restdsl.netty;

import org.apache.camel.builder.RouteBuilder;

/**
 * Serve static files with Netty.
 * todo Taariq : Replace with HttpStaticFileServerHandler
 */
public class NettyFileRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
       from("netty-http:http://0.0.0.0:9003/index.html").setBody(simple("resource:classpath:public/index.html"));
    }
}
