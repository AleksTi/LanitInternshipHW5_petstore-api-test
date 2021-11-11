package org.example.api.store;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.example.model.Order;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class StoreApiTest {
    private int id;
    private Order order;

    @BeforeClass
    private void prepare() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("my.properties"));
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/")
                .addHeader("api_key", System.getProperty("api.key"))
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
        RestAssured.filters(new ResponseLoggingFilter());
    }

    //оформить заказ на питомца
    @Test (priority = 1)
    public void placeOrderTest() throws InterruptedException {
        order = new Order();
        id = new Random().nextInt(1000);
        order.setId(id);
        order.setPetId(new Random().nextInt(1000));
        order.setQuantity(1);
        order.setShipDate("2021-11-11");
        order.setStatus("Placed");
        order.setComplete(true);
        given()
                .body(order)
                .when()
                .post("/store/order")
                .then()
                .statusCode(200);
    }

    //найти оформленный заказ
    @Test(priority = 2)
    public void checkOrderTest() throws InterruptedException {
        Order actualOrder =
                given()
                        .pathParam("orderId", id)
                        .when()
                        .get("/store/order/{orderId}")
                        .then()
                        .statusCode(200)
                        .extract().body().as(Order.class);
        Assert.assertEquals(actualOrder.getId(), order.getId());
    }

    //удалить заказ
    @Test (priority = 3)
    public void deleteOrderTest() {

        given()
                .pathParam("orderId", id)
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .statusCode(200);
    }

    //проверить удаление заказа
    @Test (priority = 4)
    public void checkDeleteOrderTest() {
        given()
                .pathParam("orderId", id)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(404);
    }

    @Test (priority = 5)
    public void checkInventory(){
        Map orders =
                given()
                        .when()
                        .get("/store/inventory")
                        .then()
                        .statusCode(200)
                        .extract().body().as(Map.class);
        Assert.assertTrue(orders.containsKey("sold"),"Inventory не содержит статус sold");
    }
}

