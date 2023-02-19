package com.lee.springtransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}