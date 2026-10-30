package com.dummyjson.tests;

import com.dummyjson.models.Product;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(TestListener.class)
public class ProductApiTests {

    private static final String BASE_URL = "https://dummyjson.com";
    private static final String PRODUCTS_ENDPOINT = "/products";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    // -------------------------------------------------------------
    // POSITIVE TESTS - READ (GET)
    // -------------------------------------------------------------

    @Test(priority = 1, description = "Positive: Retrieve all products and validate restricted fields")
    public void testGetAllProducts() {
        Response response = given()
                .when()
                .get(PRODUCTS_ENDPOINT)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("products", notNullValue())
                .extract().response();

        List<Product> products = response.jsonPath().getList("products", Product.class);
        Assert.assertFalse(products.isEmpty(), "Products list should not be empty");

        Product firstProduct = products.get(0);
        Assert.assertNotNull(firstProduct.getId());
        Assert.assertNotNull(firstProduct.getTitle());
        Assert.assertNotNull(firstProduct.getDescription());
        Assert.assertNotNull(firstProduct.getCategory());
        Assert.assertNotNull(firstProduct.getPrice());
        System.out.println("First Product details: " + firstProduct);
    }

    @Test(priority = 2, description = "Positive: Retrieve single product by valid ID (id=1) and validate fields")
    public void testGetSingleProductPositive() {
        Product product = given()
                .pathParam("id", 1)
                .when()
                .get(PRODUCTS_ENDPOINT + "/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().as(Product.class);

        Assert.assertEquals(product.getId().intValue(), 1);
        Assert.assertNotNull(product.getTitle());
        Assert.assertNotNull(product.getDescription());
        Assert.assertNotNull(product.getCategory());
        Assert.assertTrue(product.getPrice() > 0);
        System.out.println("Retrieved Single Product: " + product);
    }

    // -------------------------------------------------------------
    // TASK: Calculate total price of products with id 4 to 10
    // -------------------------------------------------------------

    @Test(priority = 3, description = "Calculate and assert total price of products with id 4 to 10")
    public void testCalculateTotalPriceForId4To10() {
        double totalPrice = 0.0;
        System.out.println("--- Calculating Prices for Products ID 4 to 10 ---");

        for (int id = 4; id <= 10; id++) {
            Product product = given()
                    .pathParam("id", id)
                    .when()
                    .get(PRODUCTS_ENDPOINT + "/{id}")
                    .then()
                    .statusCode(200)
                    .extract().as(Product.class);

            Assert.assertNotNull(product.getPrice(), "Price should not be null for product ID: " + id);
            System.out.printf("Product ID %d [%s]: $%.2f%n", product.getId(), product.getTitle(), product.getPrice());
            totalPrice += product.getPrice();
        }

        System.out.printf("Total price of products with ID 4 to 10: $%.2f%n", totalPrice);
        Assert.assertTrue(totalPrice > 0, "Total price should be greater than 0");
    }

    // -------------------------------------------------------------
    // POSITIVE TESTS - CREATE (POST)
    // -------------------------------------------------------------

    @Test(priority = 4, description = "Positive: Create product with valid JSON payload from file")
    public void testCreateProductValid() {
        File payloadFile = new File("src/test/resources/payloads/valid_product.json");

        Product createdProduct = given()
                .contentType(ContentType.JSON)
                .body(payloadFile)
                .when()
                .post(PRODUCTS_ENDPOINT + "/add")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract().as(Product.class);

        Assert.assertNotNull(createdProduct.getId(), "New product ID should be generated");
        Assert.assertEquals(createdProduct.getTitle(), "Sony WH-1000XM5 Wireless Headphones");
        Assert.assertEquals(createdProduct.getCategory(), "electronics");
        Assert.assertEquals(createdProduct.getPrice(), Double.valueOf(399.99));
        Assert.assertEquals(createdProduct.getBrand(), "Sony");
        System.out.println("Product successfully created: " + createdProduct);
    }

    // -------------------------------------------------------------
    // POSITIVE TESTS - UPDATE (PUT)
    // -------------------------------------------------------------

    @Test(priority = 5, description = "Positive: Update product details (PUT) and validate updated fields")
    public void testUpdateProduct() {
        Product updatePayload = new Product();
        updatePayload.setTitle("Updated Premium Wireless Headphones");
        updatePayload.setPrice(349.99);

        Product updatedProduct = given()
                .contentType(ContentType.JSON)
                .pathParam("id", 1)
                .body(updatePayload)
                .when()
                .put(PRODUCTS_ENDPOINT + "/{id}")
                .then()
                .statusCode(200)
                .extract().as(Product.class);

        Assert.assertEquals(updatedProduct.getId().intValue(), 1);
        Assert.assertEquals(updatedProduct.getTitle(), "Updated Premium Wireless Headphones");
        Assert.assertEquals(updatedProduct.getPrice(), Double.valueOf(349.99));
        System.out.println("Product successfully updated: " + updatedProduct);
    }

    // -------------------------------------------------------------
    // POSITIVE TESTS - DELETE (DELETE)
    // -------------------------------------------------------------

    @Test(priority = 6, description = "Positive: Delete a product and verify response flags")
    public void testDeleteProduct() {
        given()
                .pathParam("id", 1)
                .when()
                .delete(PRODUCTS_ENDPOINT + "/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("isDeleted", equalTo(true))
                .body("deletedOn", notNullValue());

        System.out.println("Product ID 1 deleted successfully.");
    }

    // -------------------------------------------------------------
    // NEGATIVE TESTS
    // -------------------------------------------------------------

    @Test(priority = 7, description = "Negative: Retrieve non-existent product ID (404 Not Found)")
    public void testGetNonExistentProduct() {
        given()
                .pathParam("id", 999999)
                .when()
                .get(PRODUCTS_ENDPOINT + "/{id}")
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test(priority = 8, description = "Negative: Update non-existent product ID (404 Not Found)")
    public void testUpdateNonExistentProduct() {
        Product updatePayload = new Product();
        updatePayload.setTitle("Ghost Product");

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 999999)
                .body(updatePayload)
                .when()
                .put(PRODUCTS_ENDPOINT + "/{id}")
                .then()
                .statusCode(404)
                .body("message", containsStringIgnoringCase("not found"));
    }

    @Test(priority = 9, description = "Negative: Delete non-existent product ID (404 Not Found)")
    public void testDeleteNonExistentProduct() {
        given()
                .pathParam("id", 999999)
                .when()
                .delete(PRODUCTS_ENDPOINT + "/{id}")
                .then()
                .statusCode(404)
                .body("message", containsStringIgnoringCase("not found"));
    }

    @Test(priority = 10, description = "Negative: POST product with malformed JSON syntax (400 Bad Request)")
    public void testCreateProductWithMalformedJson() {
        File malformedFile = new File("src/test/resources/payloads/invalid_product_malformed.json");

        given()
                .contentType(ContentType.JSON)
                .body(malformedFile)
                .when()
                .post(PRODUCTS_ENDPOINT + "/add")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }
}
