/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.quickstarts.restdsl.netty;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * Customer related routes.
 */
public class CustomerRouteBuilder extends RouteBuilder{

    @Override
    public void configure() throws Exception {

        // Handle CustomerNotFoundException to return 404
        onException(CustomerNotFoundException.class)
        .handled(true)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
        .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
        .setBody().constant("Customer not found");

        onException(JsonParseException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .setBody().constant("Invalid json data");

        // configure netty-http as the component for the rest DSL
        // and we enable JSON binding mode for all requests by default, with Customer update overriding to use auto
        restConfiguration().component("netty-http")
                .endpointProperty("matchOnUriPrefix", "true")
                .endpointProperty("bootstrapConfiguration", "#serverBootstrapConfiguration")
                .host("localhost").port(9003).bindingMode(RestBindingMode.json)
            // and output using pretty print
            .dataFormatProperty("prettyPrint", "true");

        rest("/customers").get().outTypeList(Customer.class).to("bean:customerService?method=listCustomers")
            .put().bindingMode(RestBindingMode.auto).type(Customer.class).outType(Customer.class).
                to("bean:customerService?method=updateCustomer")
            .post().bindingMode(RestBindingMode.auto).type(Customer.class).outType(Customer.class).
                to("bean:customerService?method=addCustomer")
            .get("/{id}").outType(Customer.class).to("bean:customerService?method=getCustomer(${header.id})")
            .delete("/{id}").outType(Customer.class).to("bean:customerService?method=deleteCustomer(${header.id})")
            .get("/{id}/orders").to("bean:customerService?method=listOrders(${header.id})")
            .get("/orders/{id}").to("bean:customerService?method=getOrder(${header.id})")
            .get("/products/{id}").to("bean:customerService?method=getProduct(${header.id})");
    }
}
