package pointer.Pointer_Spring.user.apple.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pointer.Pointer_Spring.report.repository.BlockedUserRepository;
import pointer.Pointer_Spring.security.TokenProvider;
import pointer.Pointer_Spring.user.apple.utils.AppleJwtUtils;
import pointer.Pointer_Spring.user.domain.Image;
import pointer.Pointer_Spring.user.domain.User;
import pointer.Pointer_Spring.user.dto.TokenDto;
import pointer.Pointer_Spring.user.dto.UserDto;
import pointer.Pointer_Spring.user.repository.ImageRepository;
import pointer.Pointer_Spring.user.repository.UserRepository;
import pointer.Pointer_Spring.validation.CustomException;
import pointer.Pointer_Spring.validation.ExceptionCode;

import java.util.Optional;

@Service
@PropertySource("classpath:application.properties")
public class AppleAuthServiceImpl {

    private final AppleJwtUtils appleJwtUtils;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final BlockedUserRepository blockedUserRepository;
    private final ImageRepository imageRepository;

    private final Integer CHECK = 1;
    private final Integer COMPLETE = 2;

    @Value("${kakao.web.redirectURI}")
    private String webRedirectUri;

    @Value("${default.profile.image.path}")
    private String profileImg;

    @Value("${default.background.image.path}")
    private String backgroundImg;

    public AppleAuthServiceImpl(AppleJwtUtils appleJwtUtils, UserRepository userRepository, TokenProvider tokenProvider, AuthenticationManager authenticationManager, BlockedUserRepository blockedUserRepository, ImageRepository imageRepository) {
        this.appleJwtUtils = appleJwtUtils;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.blockedUserRepository = blockedUserRepository;
        this.imageRepository = imageRepository;
    }


    /**
     * @description public key 발급
     */
    @Transactional
    public Object login(String identityToken) {
        Claims claims = appleJwtUtils.getClaimsBy(identityToken);
        String sub = (String) claims.get("sub"); // apple user id
        String email = (String) claims.get("email");

        Optional<User> findUser = userRepository.findByEmailAndStatus(email,1);
        User user;
        if(findUser.isEmpty()) {
            if (blockedUserRepository.existsByEmail(email)) {
                return new UserDto.UserResponse(ExceptionCode.SIGNUP_LIMITED_ID);
            }

            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            user = new User(email, email, email,
                    encoder.encode("1111"), User.SignupType.APPLE, null); // password
            userRepository.save(user);
            imageRepository.save(new Image(profileImg, Image.ImageType.PROFILE, user));
            imageRepository.save(new Image(backgroundImg, Image.ImageType.BACKGROUND, user));

        } else if (findUser.get().getType().equals(User.SignupType.KAKAO)) { // email 중복
            return new UserDto.UserResponse(ExceptionCode.SIGNUP_DUPLICATED_EMAIL);
        }
        else {
            user = findUser.get();
        }

        ExceptionCode exception;
        if (user.getId().equals(user.getEmail()) || user.getCheckId() < COMPLETE) { // 회원가입 : email
            exception = ExceptionCode.SIGNUP_CREATED_OK;
        }
        else {
            exception = ExceptionCode.SIGNUP_COMPLETE;
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        "1111" // password
                )
        );

        TokenDto tokenDto = createToken(authentication, user.getUserId());

        return new UserDto.TokenResponse(exception, tokenDto);
    }

    public TokenDto createToken(Authentication authentication, Long userId) { // token 발급

        String accessToken = tokenProvider.createToken(authentication, Boolean.FALSE); // access
        String refreshToken = tokenProvider.createToken(authentication, Boolean.TRUE); // refresh

        return TokenDto.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}