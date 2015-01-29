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
package io.fabric8.quickstarts.restdsl.netty4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Customer related services.
 */
public class CustomerService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    long currentId = 123;
    long currentOrderId = 223;
    Map<Long, Customer> customers = new HashMap();
    Map<Long, Order> orders = new HashMap();
    Map<Long, Product> products = new HashMap();

    /**
     * Constructor is used to create some dummy data.
     */
    public CustomerService() {
        init();
    }

    /**
     * Returns the customer with the given id, {@code null} if not found.
     * @param id Customers ID.
     * @return the customer with the given id, {@code null} if not found.
     */
    public Customer getCustomer(String id) {
        LOG.info("Invoking getCustomer, Customer id is: {}", id);
        long idNumber = Long.parseLong(id);
        Customer c = customers.get(idNumber);
        return c;
    }

    /**
     * Updates customer.
     *
     * @param customer the customer
     * @return the old customer before it was updated,
     */
    public Customer updateCustomer(Customer customer) {
        LOG.info("Invoking updateCustomer, Customer name is: {}", customer.getName());

        if (customers.get(customer.getId()) == null) {
           throw new CustomerNotFoundException();
        }
        return customers.put(customer.getId(), customer);
    }

    /**
     * Creates a new customer.
     *
     * @param customer the customer.
     */
    public Customer addCustomer(Customer customer) {
        LOG.info("Invoking addCustomer, Customer name is: {}", customer.getName());
        customer.setId(++currentId);

        customers.put(customer.getId(), customer);

        return customer;
    }

    /**
     * Deletes the customer.
     *
     * @param id customerId to delete.
     * @return Returns the customer matching the given id, null if there was no match.
     */
    public Customer deleteCustomer(String id) {
        LOG.info("Invoking deleteCustomer, Customer id is: {}", id);
        long idNumber = Long.parseLong(id);

        return customers.remove(idNumber);
    }

    /**
     * Returns the order matching the id.
     *
     * @param orderId Order id.
     * @return The matching order if it exists, {@code null} otherwise.
     */
    public Order getOrder(String orderId) {
        LOG.info("Invoking getOrder, Order id is: {}", orderId);
        long idNumber = Long.parseLong(orderId);
        Order c = orders.get(idNumber);
        return c;
    }

    /**
     * Returns the product matching the id, null it was not found.
     * @param productId product id.
     */
    public Product getProduct(String productId) {
        LOG.info("Invoking getProduct, Product id is: {}", productId);
        long idNumber = Long.parseLong(productId);
        Product c = products.get(idNumber);
        return c;
    }

    /**
     * Create or update order.
     *
     * @param order The new or updated order.
     * @return Returns the previous value if it exists, null otherwise.
     */
    public Order updateOrder(Order order) {
        if (order.getCustomerId() == 0 || !customers.containsKey(order.getCustomerId())) {
            throw new CustomerNotFoundException();
        }
        LOG.info("Invoking updateOrder, Customer id is: {}, order description is: {}", order.getDescription());
        order.setId(++currentOrderId);

        return orders.put(order.getId(), order);
    }

    /**
     * List customers.
     */
    public Collection<Customer> listCustomers() {
        return customers.values();
    }

    /**
     * List all orders.
     */
    public Collection<Order> listOrders() {
        return orders.values();
    }

    /**
     * List all orders for the given customerId
     *
     * @param customerId customerId the order belongs to.
     */
    public Collection<Order> listOrders(String customerId) {
        LOG.info("Invoking getOrders, Customer id is: {}", customerId);

        long idNumber = Long.parseLong(customerId);
        if (!customers.containsKey(idNumber)) {
            throw new CustomerNotFoundException();
        }

        Collection<Order> answer = new ArrayList();

        for (Order order : orders.values()) {
            if (idNumber == order.getCustomerId())
                answer.add(order);
        }
        return answer;
    }

    /**
     * The init method is used by the constructor to insert a Customer and Order object into the local data map
     * for testing purposes.
     */
    final void init() {
        Customer c = new Customer();
        c.setName("John");
        c.setId(123);
        customers.put(c.getId(), c);

        Product p = new Product();
        p.setId(323);
        p.setDescription("product 323");
        products.put(p.getId(), p);

        Order o = new Order();
        o.setDescription("order 223");
        o.setId(223);
        o.setCustomerId(123);
        o.setProducts(products);
        orders.put(o.getId(), o);
    }
}
