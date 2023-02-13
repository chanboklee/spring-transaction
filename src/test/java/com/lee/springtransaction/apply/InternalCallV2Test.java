package com.lee.springtransaction.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * public 메서드만 트랜잭션 적용
 * protected, private, package-visible에는 트랜잭션이 적용되지 않는다.
 * public이 아닌곳에 @Transactional이 붙어 있으면 예외가 발생하지는 않고, 트랜잭션 적용만 무시된다.
 */
@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired CallService callService;

    @Test
    void printProxy(){
        log.info("callservice class -> {}", callService.getClass());
    }

    @Test
    void externalCallV2(){
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService(){
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService(){
            return new InternalService();
        }
    }


    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external(){
            log.info("call external");
            printTransactionInfo();
            internalService.internal();
        }

        private void printTransactionInfo(){
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction active -> {}", actualTransactionActive);
        }
    }

    static class InternalService {

        @Transactional
        public void internal(){
            log.info("call internal");
            printTransactionInfo();
        }

        private void printTransactionInfo(){
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction active -> {}", actualTransactionActive);
        }
    }
}
