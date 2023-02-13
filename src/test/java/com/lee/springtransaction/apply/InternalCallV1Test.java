package com.lee.springtransaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 메서드 앞에 별도의 참조가 없으면 this라는 뜻으로 자신의 인스턴스를 가리킨다.
 * 자기 자신을 가리키므로, 실제 대상 객체(target)의 인스턴스를 뜻한다.
 * 결과적으로 이러한 내부 호출은 프록시를 거치지 않는다.
 * 따라서 트랜잭션을 적용할 수 없다.
 * 결과적으로 target에 있는 internal()을 직접 호출하게 된 것이다.
 * 가장 단순한 방법은 내부 호출을 피하기 위해 internal() 메서드를 별도의 클래스로 분리하는 것이다.
 */
@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy(){
        log.info("callservice class -> {}", callService.getClass());
    }

    @Test
    void internalCall(){
        callService.internal();
    }

    @Test
    void externalCall(){
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService(){
            return new CallService();
        }
    }


    @Slf4j
    static class CallService {

        public void external(){
            log.info("call external");
            printTransactionInfo();
            internal();
        }

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
