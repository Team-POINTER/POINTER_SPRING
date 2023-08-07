package pointer.Pointer_Spring.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pointer.Pointer_Spring.common.response.BaseResponse;
import pointer.Pointer_Spring.security.CurrentUser;
import pointer.Pointer_Spring.security.TokenProvider;
import pointer.Pointer_Spring.security.UserPrincipal;
import pointer.Pointer_Spring.user.apple.service.AppleAuthServiceImpl;
import pointer.Pointer_Spring.user.domain.User;
import pointer.Pointer_Spring.user.dto.*;
import pointer.Pointer_Spring.user.repository.UserRepository;
import pointer.Pointer_Spring.user.service.AuthServiceImpl;

import java.util.Optional;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authServiceImpl;

    //  test
    //private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AppleAuthServiceImpl appleAuthService;

    @PostMapping("/auth/test")
    public ResponseEntity<Object> test(@RequestBody KakaoRequestDto signUpRequest) {

        Optional<User> findUser = userRepository.findByEmailAndStatus(signUpRequest.getEmail(), 1);
        User user;
        if (findUser.isEmpty()) {

            // {id}ENCODED_PASSWORD 형태
            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

            user = new User(signUpRequest.getEmail(), signUpRequest.getId(), signUpRequest.getName(),
                    encoder.encode("1111"), User.SignupType.KAKAO, "test"); // password
            userRepository.save(user);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), "1111");

            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //System.out.println("SecurityContextHolder.getContext().toString() = " + SecurityContextHolder.getContext().toString());

            TokenDto tokenDto = authServiceImpl.createToken(authentication, user.getUserId());
            user.setToken(tokenDto.getRefreshToken());
            userRepository.save(user);
            return new ResponseEntity<>(tokenDto, HttpStatus.OK);
        }
        user = findUser.get();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "1111");

        Authentication authentication = authenticationManager.authenticate(token);

        TokenDto tokenDto = authServiceImpl.createToken(authentication, user.getUserId());
        tokenDto.setId(user.getId());
        tokenDto.setUserId(user.getUserId());
        user.setToken(tokenDto.getRefreshToken());
        userRepository.save(user);

        return new ResponseEntity<>(tokenDto, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<Object> test2() {
        return new ResponseEntity<>("success!", HttpStatus.OK);
    }

    @GetMapping("/auth/kakao")
    public Object kakaoLogin(@RequestParam String code) {
        String accessToken = authServiceImpl.getKakaoAccessToken(code, false);
        return new ResponseEntity<>(authServiceImpl.kakaoCheck(accessToken), HttpStatus.OK);
    }

    @GetMapping("/auth/kakao/web")
    public Object webKakaoLogin(@RequestParam String code) {
        String accessToken = authServiceImpl.getKakaoAccessToken(code, true);
        return new ResponseEntity<>(authServiceImpl.webKakaoCheck(accessToken), HttpStatus.OK);
    }

    // real
    @PostMapping("/auth/login") // kakao social login
    public ResponseEntity<Object> login(@RequestBody TokenRequest tokenRequest) {
        return new ResponseEntity<>(authServiceImpl.kakaoCheck(tokenRequest.getAccessToken()), HttpStatus.OK);
    }

    @PostMapping("/auth/login/web")// kakao social signup
    public Object webKakaoLogin(@RequestBody TokenRequest tokenRequest) {
        return new ResponseEntity<>(authServiceImpl.webKakaoCheck(tokenRequest.getAccessToken()), HttpStatus.OK);
    }

    @PostMapping("/auth/login/apple")
    public BaseResponse<TokenDto> loginApple(@RequestBody AppleLoginRequest appleLoginRequest) {
        return new BaseResponse<>(appleAuthService.login(appleLoginRequest.getIdentityToken()));
    }

    @PostMapping("/user/reissue") // token 재발급
    public ResponseEntity<Object> reissue(@CurrentUser UserPrincipal userPrincipal) {
        return new ResponseEntity<>(authServiceImpl.reissue(userPrincipal), HttpStatus.OK);
    }

    @PostMapping("/user/agree") // 동의
    public ResponseEntity<Object> saveAgree(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.UserAgree agree) {
        return new ResponseEntity<>(authServiceImpl.saveAgree(userPrincipal, agree), HttpStatus.OK);
    }

    @PostMapping("/user/marketing") // 마케팅 상태 변경
    public ResponseEntity<Object> updateMarketing(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.UserMarketing marketing) {
        return new ResponseEntity<>(authServiceImpl.updateMarketing(userPrincipal, marketing), HttpStatus.OK);
    }

    @PostMapping("/user/id") // id 저장
    public ResponseEntity<Object> saveId(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.BasicUser info) {
        return new ResponseEntity<>(authServiceImpl.saveId(userPrincipal, info), HttpStatus.OK);
    }

    @PostMapping("/user/checkId") // 중복 확인
    public ResponseEntity<Object> checkId(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.BasicUser info) {
        return new ResponseEntity<>(authServiceImpl.checkId(userPrincipal, info), HttpStatus.OK);
    }

    @PostMapping("/user/logout") // 로그아웃
    public ResponseEntity<Object> logout(@CurrentUser UserPrincipal userPrincipal) {
        return new ResponseEntity<>(authServiceImpl.logout(userPrincipal), HttpStatus.OK);
    }

    @DeleteMapping("/user/resign") // 회원 탈퇴
    public ResponseEntity<Object> resign(@CurrentUser UserPrincipal userPrincipal) {
        return new ResponseEntity<>(authServiceImpl.resign(userPrincipal), HttpStatus.OK);
    }
}
