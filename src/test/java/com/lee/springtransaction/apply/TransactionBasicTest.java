package com.lee.springtransaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class TransactionBasicTest {

    @Autowired BasicService basicService;

    @Test
    void proxyCheck(){
        log.info("aop class -> {}", basicService.getClass());
        // aop가 적용되었는지 확인할 수 있다.
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    @Test
    void transactionTest(){
        basicService.transaction();
        basicService.nonTransaction();
    }

    @TestConfiguration
    static class TransactionBasicConfig {
        @Bean
        BasicService basicService(){
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService{

        @Transactional
        public void transaction(){
            log.info("call transaction");
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction active -> {}", actualTransactionActive);
        }

        public void nonTransaction(){
            log.info("call non transaction");
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction active -> {}", actualTransactionActive);
        }
    }
}
