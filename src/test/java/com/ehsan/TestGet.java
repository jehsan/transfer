package com.ehsan;

import com.ehsan.entitty.Account;
import com.ehsan.dto.AccountDto;
import com.ehsan.dto.TransferDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import static com.jayway.restassured.RestAssured.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGet {

    private Response response = null; //Response object
    private JsonPath jp = null; //JsonPath object

    private static Account account1 = null;
    private static Account account2 = null;

    @Before
    public void test_00_setup (){
        RestAssured.baseURI = "http://localhost:8080";
        given().contentType(ContentType.JSON);
    }

    @Test
    public void test_01_createFirstAccount () throws IOException {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        AccountDto accountDto = new AccountDto();
        accountDto.setBalance(BigDecimal.valueOf(2300.23));
        accountDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        String writeValueAsString = new ObjectMapper().writeValueAsString(accountDto);
        builder.setBody(writeValueAsString);
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/create-account");
        assertEquals("Unexpected Http Status", 200, response.getStatusCode());
        String bodyAsSting = response.getBody().print();
        account1 = new ObjectMapper().readValue(bodyAsSting, Account.class);
        assertNotEquals("Parsing Account Failed!", null, account1);
    }

    @Test
    public void test_02_createSecondAccount () throws IOException {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        AccountDto accountDto = new AccountDto();
        accountDto.setBalance(BigDecimal.valueOf(300.23));
        accountDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        String writeValueAsString = new ObjectMapper().writeValueAsString(accountDto);
        System.err.println(writeValueAsString);
        builder.setBody(writeValueAsString);
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/create-account");
        assertEquals("Unexpected Http Status", 200, response.getStatusCode());
        String prettyPrint = response.getBody().prettyPrint();
        account2 = new ObjectMapper().readValue(prettyPrint, Account.class);
        assertNotEquals("Parsing Account Failed!", null, account2);
    }

    @Test
    public void test_03_validTransferMoney () throws JsonProcessingException {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(account1.getId());
        transferDto.setCreditAccountId(account2.getId());
        transferDto.setAmount(BigDecimal.valueOf(150));
        transferDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        builder.setBody(new ObjectMapper().writeValueAsString(transferDto));
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/transfer");
        assertEquals("Unexpected Http Status", 200, response.getStatusCode());
    }

    @Test
    public void test_04_getAccounts () throws IOException {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(account1.getId());
        transferDto.setCreditAccountId(account2.getId());
        transferDto.setAmount(BigDecimal.valueOf(150));
        transferDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        builder.setBody(transferDto);
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).get("api/get-account?id=" + account1.getId());
        account1 = new ObjectMapper().readValue(response.getBody().print(), Account.class);
        response = given().spec(requestSpec).get("api/get-account?id=" + account2.getId());
        account2 = new ObjectMapper().readValue(response.getBody().print(), Account.class);
        assertEquals("Unexpected Http Status", 200, response.getStatusCode());
    }

    @Test
    public void test_05_invalidTransferCurrency() throws IOException {
        BigDecimal account1Balance = account1.getBalance();
        BigDecimal account2Balance = account2.getBalance();
        RequestSpecBuilder builder = new RequestSpecBuilder();
        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(account1.getId());
        transferDto.setCreditAccountId(account2.getId());
        transferDto.setAmount(BigDecimal.valueOf(150));
        transferDto.setCurrencyId(-12);
        builder.setContentType(ContentType.JSON);
        builder.setBody(new ObjectMapper().writeValueAsString(transferDto));
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/transfer");
        assertEquals("Unexpected Http Status", javax.ws.rs.core.Response.Status.EXPECTATION_FAILED.getStatusCode(), response.getStatusCode());
        assertEquals("Currency must not be provided", "Currency is not provided.", response.getBody().print());
        test_04_getAccounts();
        assertEquals("Balance changed!", account1Balance, account1.getBalance());
        assertEquals("Balance changed!", account2Balance, account2.getBalance());
    }

    @Test
    public void test_06_mismatchTransferCurrency() throws IOException {
        BigDecimal account1Balance = account1.getBalance();
        BigDecimal account2Balance = account2.getBalance();
        RequestSpecBuilder builder = new RequestSpecBuilder();
        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(account1.getId());
        transferDto.setCreditAccountId(account2.getId());
        transferDto.setAmount(BigDecimal.valueOf(150));
        transferDto.setCurrencyId(2);
        builder.setContentType(ContentType.JSON);
        builder.setBody(new ObjectMapper().writeValueAsString(transferDto));
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/transfer");
        assertEquals("Unexpected Http Status", javax.ws.rs.core.Response.Status.EXPECTATION_FAILED.getStatusCode(), response.getStatusCode());
        assertEquals("Currency mismatch failed!", "Currencies do not match.", response.getBody().print());
        test_04_getAccounts();
        assertEquals("Balance changed!", account1Balance, account1.getBalance());
        assertEquals("Balance changed!", account2Balance, account2.getBalance());
    }

    @Test
    public void test_07_insufficientBalance() throws IOException {
        BigDecimal account1Balance = account1.getBalance();
        BigDecimal account2Balance = account2.getBalance();
        RequestSpecBuilder builder = new RequestSpecBuilder();
        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(account1.getId());
        transferDto.setCreditAccountId(account2.getId());
        transferDto.setAmount(BigDecimal.valueOf(4150));
        transferDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        builder.setBody(new ObjectMapper().writeValueAsString(transferDto));
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/transfer");
        assertEquals("Unexpected Http Status", javax.ws.rs.core.Response.Status.EXPECTATION_FAILED.getStatusCode(), response.getStatusCode());
        assertEquals("Currency mismatch failed!", "Account balance is not enough.", response.getBody().print());
        test_04_getAccounts();
        assertEquals("Balance changed!", account1Balance, account1.getBalance());
        assertEquals("Balance changed!", account2Balance, account2.getBalance());
    }

}
