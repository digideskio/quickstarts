# REST using the Camel Rest DSL, Netty HTTP QuickStart

This quick start demonstrates how to create a RESTful (Camel-Netty-Http) web service using Camel Rest DSL and run it as a standalone Java application.

### Building this example

The example comes as source code and pre-built binaries with the fabric8 distribution.

To try the example you do not need to build from source first. Although building from source allows you to modify the source code, and re-deploy the changes to fabric. See more details on the fabric8 website about the [developer workflow](http://fabric8.io/gitbook/developer.html).

To build from the source code:

1. Change your working directory to `quickstarts/java/camel-netty-http-rest` directory.
1. Run `mvn clean install` to build the quickstart.

After building from the source code, you can upload the changes to the fabric container:

1. It is assumed that you have already created a fabric and are logged into a container called `root`.
1. Change your working directory to `quickstarts/java/camel-netty-http-rest` directory.
1. Run `mvn fabric8:deploy` to upload the quickstart to the fabric container.

If you run the `fabric:deploy` command for the first then, it will ask you for the username and password to login the fabric container.
And then store this information in the local Maven settings file. You can find more details about this on the fabric8 website about the [Maven Plugin](http://fabric8.io/gitbook/mavenPlugin.html).


## Access services using a web browser

There's an index page you can use to try some of the services defined.
http://localhost:9003/public/index.html

Use this URL to display the JSON representation for customer 123:

    http://localhost:9003/customers/123

You can also access the JSON representation for order 223 ...

    http://localhost:9003/customers/orders/223

### To run a command-line utility:

You can use a command-line utility, such as cURL or wget, to perform the HTTP requests.  We have provided a few files with sample XML representations in `src/test/resources`, so we will use those for testing our services.

1. Open a command prompt and change directory to `camel-netty-http-rest`.
2. Run the following curl commands (curl commands may not be available on all platforms):

    * List customers

            curl http://localhost:9003/customers

    * Create a customer

            curl -X POST http://localhost:9003/customers -H "Content-Type: text/xml" -d @src/test/resources/add_customer.xml

    * Retrieve the customer instance with id 123

            curl http://localhost:9003/customers/123

    * Update the customer instance with id 123

            curl -X PUT http://localhost:9003/customers -H "Content-type:application/xml" -d @src/test/resources/update_customer.xml

    * Delete the customer instance with id 123

            curl -X DELETE http://localhost:9003/customers/123