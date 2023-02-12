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
 * 1. 우선순위 규칙 -> 대부분 더 구체적인 것이 우선순위를 가진다.
 * - 1. 클래스의 메서드(우선순위가 가장 높다)
 * - 2. 클래스의 타입
 * - 3. 인터페이스의 메서드
 * - 4. 인터페이스의 타입(우선순위가 가장 낮다)
 * -> but, 인터페이스에 @Transactional 사용하는 것은 스프링 공식 메뉴얼에서 권장하지 않는 방법
 * -> AOP를 적용하는 방식에 따라서 인터페이스 애노테이션을 두면 AOP가 적용되지 않는 경우가 있기 때문
 * -> 가급적 구체 클래스에 @Transactional을 사용
 *
 * 2. 클래스에 적용하면 메서드는 자동 적용
 */
@SpringBootTest
public class TransactionLevelTest {

    @Autowired LevelService levelService;

    @Test
    void orderTest(){
        levelService.write();
        levelService.read();
    }

    @TestConfiguration
    static class TransactionLevelTestConfig {
        @Bean
        LevelService levelService(){
            return new LevelService();
        }
    }

    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        @Transactional
        public void write(){
            log.info("call write");
            printTransactionInfo();
        }

        public void read(){
            log.info("call read");
            printTransactionInfo();
        }

        private void printTransactionInfo(){
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction active -> {}", actualTransactionActive);
            boolean currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("transaction readOnly -> {}", currentTransactionReadOnly);
        }
    }
}
