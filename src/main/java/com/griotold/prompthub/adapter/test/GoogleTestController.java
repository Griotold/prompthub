package com.griotold.prompthub.adapter.test;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 구글 OAuth2 테스트용 임시 컨트롤러
 * 인가코드를 받기 위한 콜백 엔드포인트
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/auth/google")
public class GoogleTestController {

    @GetMapping("/callback")
    public String getAuthorizationCode(@RequestParam String code) {
        log.info("구글 인가코드 받음: {}", code);
        return String.format("""
            <html>
            <head>
                <title>구글 인가코드 받기 성공!</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; }
                    .container { max-width: 800px; margin: 0 auto; }
                    .code-box { 
                        background-color: #f5f5f5; 
                        padding: 20px; 
                        border-radius: 5px; 
                        margin: 20px 0;
                        word-break: break-all;
                        font-family: 'Courier New', monospace;
                    }
                    .copy-btn { 
                        background-color: #4285f4; 
                        color: white; 
                        border: none; 
                        padding: 10px 20px; 
                        border-radius: 5px; 
                        cursor: pointer; 
                    }
                    .instructions {
                        background-color: #e8f5e8;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>구글 인가코드 받기 성공!</h1>
                    
                    <div class="instructions">
                        <h3>다음 단계:</h3>
                        <ol>
                            <li>아래 인가코드를 복사하세요</li>
                            <li>Postman 또는 Insomnia를 여세요</li>
                            <li><code>POST /api/v1/auth/google/login</code> 호출하세요</li>
                            <li>Body에 인가코드를 넣어서 테스트하세요</li>
                        </ol>
                    </div>
                    
                    <h3>인가코드:</h3>
                    <div class="code-box" id="code">%s</div>
                    
                    <button class="copy-btn" onclick="copyCode()">인가코드 복사</button>
                    
                    <h3>API 테스트 예시:</h3>
                    <div class="code-box">
POST http://localhost:8080/api/v1/auth/google/login
Content-Type: application/json
{
  "authorizationCode": "%s"
}
                    </div>
                    
                    <script>
                        function copyCode() {
                            const code = document.getElementById('code').textContent;
                            navigator.clipboard.writeText(code).then(() => {
                                alert('인가코드가 복사되었습니다!');
                            });
                        }
                    </script>
                </div>
            </body>
            </html>
            """, code, code);
    }
}

