package com.ehsan.controller;

import com.ehsan.entitty.Account;
import com.ehsan.dto.AccountDto;
import com.ehsan.dto.TransferDto;
import com.ehsan.service.FinancialService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

@Path("api")
public class FinancialApi {
    public FinancialApi() {
        super();
    }

    private FinancialService financialService = new FinancialService();
    private ObjectMapper mapper = new ObjectMapper();

    @POST
    @Consumes("application/json")
    @Path("/transfer")
    public Response transferMoney(TransferDto transferDto) {
        try{
            financialService.transfer(transferDto);
        }catch (Exception e)
        {
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @POST
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/create-account")
    public Response createAccount(AccountDto accountDto) {
        try{
            Account account = financialService.createAccount(accountDto);
            return Response.ok().entity(account).build();
            }catch (Exception e)
                {
                    return Response.status(Response.Status.EXPECTATION_FAILED).entity(e.getMessage()).build();
                }
    }

    @GET
    @Produces("application/json")
    @Path("/get-account")
    public Response getAccount(@QueryParam("id") Long id) throws JsonProcessingException {
        try{
            Account account = financialService.getAccount(id);
            return Response.ok().entity(account).build();
        }catch (Exception e)
        {
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(e.getMessage()).build();
        }
    }

}