package com.ehsan.entitty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class Account {

  @Id
  @Column(name = "id",nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Min(0)
  @Column(name = "balance",nullable = false)
  @NotNull(message = "Balance cannot be null")
  private BigDecimal balance;

  @ManyToOne
  @JoinColumn(name = "currency_id")
  @NotNull(message = "currency must be provided")
  private Currency currency;

  //version field to manage concurrent access
  @Version
  private Integer version;

  public Account(){
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}