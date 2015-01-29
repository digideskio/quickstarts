# REST using the Rest DSL, Camel Netty Http QuickStart

This quick start demonstrates how to create a RESTful (Camel-Netty-Http) web service using Camel Rest DSL and run it as a standalone Java application.

### From the source code

1. Run `mvn install`
2. Run `mvn camel:run` to run the example as a standalone Spring based Camel application.


### todo polish docs
Once it's running you can test with CURL or your browser

Index : http://localhost:9003/index.html

List customers : http://localhost:9003/customers

List orders for Customer 123 : http://localhost:9003/customers/123/orders

Get customer 123 : http://localhost:9003/customers/123

Get order 223 : http://localhost:9003/customers/orders/223

Get product 323 : http://localhost:9003/customers/products/323
