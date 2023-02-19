package com.lee.springtransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;

    /**
     * memberService        -> @Transactional : OFF
     * memberRepository     -> @Transactional : ON
     * logRepository        -> @Transactional : ON
     */
    @Test
    void outerTransactionOff_success(){
        // given
        String username = "outerTransactionOff_success";

        // when
        memberService.joinV1(username);

        // then -> 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService        -> @Transactional : OFF
     * memberRepository     -> @Transactional : ON
     * logRepository        -> @Transactional : ON Exception
     */
    @Test
    void outerTransactionOff_fail(){
        // given
        String username = "로그예외_outerTransactionOff_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                        .isInstanceOf(RuntimeException.class);

        // then -> 멤버는 커밋, 로그는 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService        -> @Transactional : ON
     * memberRepository     -> @Transactional : OFF
     * logRepository        -> @Transactional : OFF
     */
    @Test
    void singleTransaction(){
        // given
        String username = "singleTransaction";

        // when
        memberService.joinV1(username);

        // then -> 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService        -> @Transactional : ON
     * memberRepository     -> @Transactional : ON
     * logRepository        -> @Transactional : ON
     */
    @Test
    void outerTransaction_success(){
        // given
        String username = "outerTransaction_success";

        // when
        memberService.joinV1(username);

        // then -> 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService        -> @Transactional : ON
     * memberRepository     -> @Transactional : ON
     * logRepository        -> @Transactional : ON Exception
     */
    @Test
    void outerTransactionOn_fail(){
        // given
        String username = "로그예외_outerTransactionOn_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then -> 모든 데이터가 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService        -> @Transactional : ON
     * memberRepository     -> @Transactional : ON
     * logRepository        -> @Transactional : ON Exception
     *
     * [정리]
     * 논리 트랜잭션 중 하나라도 롤백되면 전체 트랜잭션은 롤백된다.
     * 내부 트랜잭션이 롤백되었는데, 외부 트랜잭션이 커밋되면 UnexpectedRollbackException 예외 발생
     * rollbackOnly 상황에서 커밋이 발생하면 UnexpectedRollbackException 예외 발생
     *
     * 정상 흐름으로 반환하였어도 LogRepository에서 이미 rollbackOnly marked하였으므로 롤백된다.
     */
    @Test
    void recoverException_fail(){
        // given
        String username = "로그예외_recoverException_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // then -> 모든 데이터가 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }
}