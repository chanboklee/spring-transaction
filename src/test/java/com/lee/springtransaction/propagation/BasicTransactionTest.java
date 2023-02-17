package com.lee.springtransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTransactionTest {

    @Autowired
    PlatformTransactionManager transactionManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit(){
        log.info("트랜잭션 시작");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        transactionManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback(){
        log.info("트랜잭션 시작");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        transactionManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void double_commit(){
        log.info("트랜잭션1 시작");
        TransactionStatus transaction1 = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        transactionManager.commit(transaction1);

        log.info("트랜잭션2 시작");
        TransactionStatus transaction2 = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        transactionManager.commit(transaction2);
    }

    @Test
    void double_commit_rollback(){
        log.info("트랜잭션1 시작");
        TransactionStatus transaction1 = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        transactionManager.commit(transaction1);

        log.info("트랜잭션2 시작");
        TransactionStatus transaction2 = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백 시작");
        transactionManager.rollback(transaction2);
    }

    /**
     * 외부 트랜잭션과 내부 트랜잭션이 하나의 물리 트랜잭션으로 묶이는 것
     * 외부 트랜잭션만 물리 트랜잭션을 시작하고 커밋한다.
     * 처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리
     */
    @Test
    void inner_commit(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction() -> {}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction() -> {}", inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        transactionManager.commit(inner);

        log.info("외부 트랜잭션 커밋");
        transactionManager.commit(outer);
    }

    @Test
    void outer_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = transactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 커밋");
        transactionManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        transactionManager.rollback(outer);
    }

    /**
     * [내부 트랜잭션]
     * 트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동
     * 내부 트랜잭션은 신규 트랜잭션이 아니기 때문에 실제 롤백을 호출하지 않는다.
     * 실제 커넥션에 커밋이나 롤백을 호출하면 물리 트랜잭션이 끝나버린다.
     * 내부 트랜잭션은 물리 트랜잭션을 롤백하지 않는 대신에 트랜잭션 동기화 매니저에 'rollbackOnly=true' 라는 표시를 한다.
     *
     * [외부 트랜잭션]
     * 외부 트랜잭션은 신규 트랜잭션이다. 따라서 DB 커넥션에 실제 커밋을 호출해야 한다.
     * 이때 먼저 트랜잭션 동기화 매니저에 롤백 전용(rollbackOnly=true) 표시가 있는지 확인한다.
     * 롤백 전용 표시가 있으면 물리 트랜잭션을 커밋하는 것이 아니라 롤백한다.
     *
     * [정리]
     * 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
     * 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.
     *
     * 논리 트랜잭션이 하나라도 롤백되면 물리 트랜잭션은 롤백된다.
     * 내부 논리 트랜잭션이 롤백되면 롤백 전용 마크를 표시한다.
     * 외부 트랜잭션을 커밋할 때 롤백 전용 마크를 확인한다. 롤백 전용 마크가 표시되어 있으면 물리 트랜잭션을 롤백하고
     * UnexpectedRollbackException 예외를 던진다.
     */
    @Test
    void inner_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = transactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = transactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 롤백");
        transactionManager.rollback(inner); // rollback-only marked

        log.info("외부 트랜잭션 커밋");
        transactionManager.commit(outer);
        Assertions.assertThatThrownBy(() -> transactionManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }
}
