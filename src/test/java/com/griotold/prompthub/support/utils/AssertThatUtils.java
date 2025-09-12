package com.griotold.prompthub.support.utils;

import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import org.assertj.core.api.AssertProvider;
import org.springframework.test.json.JsonPathValueAssert;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
/**
 * 테스트는 빨리 만드는 게 중요하기 때문에
 * 유틸 클래스를 잘 만들어 두는 게 중요하다.
 * 검증부에서 람다가 보이면 바로바로 유틸로 옮겨놓자.
 * */
public class AssertThatUtils {

    public static Consumer<AssertProvider<JsonPathValueAssert>> notNull() {
        return value -> assertThat(value).isNotNull();
    }

    public static Consumer<AssertProvider<JsonPathValueAssert>> equalsTo(MemberRegisterRequest registerRequest) {
        return  value -> assertThat(value).isEqualTo(registerRequest.email());
    }
}
