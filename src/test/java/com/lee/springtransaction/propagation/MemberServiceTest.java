package com.lee.springtransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
     * memberService        -> @Transactional : Off
     * memberRepository     -> @Transactional : In
     * logRepository        -> @Transactional : In
     */
    @Test
    void outerTransactionOff_success(){
        // given
        String username = "outerTransactionOff_success";

        // when
        memberService.joinV1(username);

        // when -> 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
        // then

    }
}