package com.ehsan.service;

import com.ehsan.dto.AccountDto;
import com.ehsan.dto.TransferDto;
import com.ehsan.entitty.Account;
import com.ehsan.entitty.Currency;
import com.ehsan.entitty.Transfer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FinancialService {

    private static EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("test");

    public void transfer(TransferDto transferDto) {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("javax.persistence.lock.timeout", 1000L);

        //Transfer amount must be positive
        if (transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Amount must be positive.");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            //Retrieve and lock debit account
            Account debitAccount = em.find(Account.class, transferDto.getDebitAccountId());
            if(debitAccount == null)
                throw new RuntimeException("Source account is not provided.");
            em.lock(debitAccount, LockModeType.PESSIMISTIC_WRITE, properties);

            //Retrieve and lock credit account
            Account creditAccount = em.find(Account.class, transferDto.getCreditAccountId());
            if(creditAccount == null)
                throw new RuntimeException("Destination account is not provided.");
            em.lock(creditAccount, LockModeType.PESSIMISTIC_WRITE, properties);

            //Retrieve currency
            Currency currency = em.find(Currency.class, transferDto.getCurrencyId());
            if(currency == null)
                throw new RuntimeException("Currency is not provided.");

            //Currency must be equal for both accounts and input transferDto
            if(!debitAccount.getCurrency().equals(currency) || !creditAccount.getCurrency().equals(currency))
                throw new RuntimeException("Currencies do not match.");

            //Debit account's balance must be positive after withdraw
            if (debitAccount.getBalance().subtract(transferDto.getAmount()).compareTo(BigDecimal.ZERO) < 0)
                throw new RuntimeException("Account balance is not enough.");

            //update accounts' balances
            debitAccount.setBalance(debitAccount.getBalance().subtract(transferDto.getAmount()));
            creditAccount.setBalance(creditAccount.getBalance().add(transferDto.getAmount()));

            //create a transfer record
            Transfer transfer = new Transfer();
            transfer.setAmount(transferDto.getAmount());
            transfer.setDebitAccount(debitAccount);
            transfer.setDebitAccount(creditAccount);
            transfer.setCurrency(currency);

            //save data to database
            em.persist(debitAccount);
            em.persist(creditAccount);
            em.persist(transfer);
            em.getTransaction().commit();


        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw new RuntimeException(e.getMessage());
        }
        finally {
            em.close();
        }
    }

    //retrieve an account
    public Account getAccount(Long id)
    {
        EntityManager em = emf.createEntityManager();
        return em.find(Account.class, id);
    }

    //create a new account
    public Account createAccount(AccountDto accountDto) {
        EntityManager em = emf.createEntityManager();
        Account account = new Account();
        account.setBalance(accountDto.getBalance());
        account.setCurrency(em.find(Currency.class, accountDto.getCurrencyId()));
        em.getTransaction().begin();
        em.persist(account);
        em.getTransaction().commit();
        return account;
    }
}
