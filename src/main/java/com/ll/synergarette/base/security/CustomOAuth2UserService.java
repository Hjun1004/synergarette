package com.ll.synergarette.base.security;

import com.ll.synergarette.boundedContext.member.entity.Member;
import com.ll.synergarette.boundedContext.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User.getAttributes());

        String oauthId = oAuth2User.getName();

        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        String username ;
        if(providerTypeCode.equals("NAVER")){
            // 소셜 로그인 한 oAuth2User 정보에서 네이버만의 response 정보를 가져오고 그 안에서 id 정보만 빼와서 username에 활용
            OAuth2NaverUserInfo oAuth2NaverUserInfo = new OAuth2NaverUserInfo((Map<String, Object>) oAuth2User.getAttributes().get("response"));
            String naverOauthId = oAuth2NaverUserInfo.getId();
            username = providerTypeCode + "__%s".formatted(naverOauthId);
        }
        else{
            username = providerTypeCode + "__%s".formatted(oauthId);
        }

        Member member = memberService.whenSocialLogin(providerTypeCode, username).getData();

        return new CustomOAuth2User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
    }
}

class CustomOAuth2User extends User implements OAuth2User{
    public CustomOAuth2User(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    @Override
    public Map<String, Object> getAttributes(){
        return null;
    }

    @Override
    public String getName(){
        return getUsername();
    }

}
