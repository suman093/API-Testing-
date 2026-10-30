# DummyJSON REST API Automation Testing Framework

A ready-to-run API test automation project built using **Java 17**, **Maven**, **REST Assured**, and **TestNG** with **ExtentReports**.

## Project Highlights
- **Target API**: https://dummyjson.com/docs/products
- **Restricted POJO**: Filters JSON strictly to `id`, `title`, `description`, `category`, `price`, and `brand`.
- **CRUD Operations**: GET (All & Single), POST (Create), PUT (Update), and DELETE.
- **Specific Business Task**: Automatically fetches products from ID 4 to 10 and computes their total price.
- **Test Scenarios**: Both Positive and Negative test cases (status codes 200, 201, 400, 404).
- **Reporting**: ExtentReports HTML report configured (`target/ExtentReport.html`) and Surefire test reports.

## Prerequisites
- **JDK 17** or higher installed.
- **Apache Maven** installed (`mvn -v`).

## How to Run on Your Laptop
1. Unzip the project folder:
   ```bash
   unzip DummyJsonApiTesting.zip
   cd DummyJsonApiTesting
   ```
2. Run all tests via Maven:
   ```bash
   mvn clean test
   ```
3. View the generated test report:
   Open `target/ExtentReport.html` in any web browser.
