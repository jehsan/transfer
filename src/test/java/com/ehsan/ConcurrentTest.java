package com.ehsan;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.ehsan.entitty.Account;
import com.ehsan.dto.AccountDto;
import com.ehsan.dto.TransferDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.math.BigDecimal;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ConcurrentTestRunner.class)
public class ConcurrentTest {

    private Response response = null;
    private static Account account1 = null;
    private static Account account2 = null;

    @Before
    public void createAccount1 () throws IOException {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        AccountDto accountDto = new AccountDto();
        accountDto.setBalance(BigDecimal.valueOf(2300.23));
        accountDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        String writeValueAsString = new ObjectMapper().writeValueAsString(accountDto);
        System.err.println(writeValueAsString);
        builder.setBody(writeValueAsString);
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/create-account");
        assertEquals("Unexpected Http Status", 200, response.getStatusCode());
        String prettyPrint = response.getBody().prettyPrint();
        account1 = new ObjectMapper().readValue(prettyPrint, Account.class);
        assertNotEquals("Parsing Account Failed!", null, account1);
    }

    @Before
    public void createSecondAccount2() throws IOException {
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

    //test concurrent api call with 7 threads
    @Test
    @ThreadCount(7)
    public void test_08() throws JsonProcessingException {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(account1.getId());
        transferDto.setCreditAccountId(account2.getId());
        transferDto.setAmount(BigDecimal.valueOf(100));
        transferDto.setCurrencyId(1);
        builder.setContentType(ContentType.JSON);
        builder.setBody(new ObjectMapper().writeValueAsString(transferDto));
        RequestSpecification requestSpec = builder.build();
        response = given().spec(requestSpec).post("api/transfer");
    }

    @After
    public void after_01_getAccounts() throws IOException {
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
    @After
    public void after_02_checkAccountsBalances()
    {
        assertEquals("Sum of account balances has changed!", BigDecimal.valueOf(2600.46), account1.getBalance().add(account2.getBalance()));
    }
    }
