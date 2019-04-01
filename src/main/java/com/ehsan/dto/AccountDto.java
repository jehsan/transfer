package com.ehsan.dto;

import java.math.BigDecimal;

public class AccountDto {

    private BigDecimal balance;
    private Integer currencyId;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }
}
