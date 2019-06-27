package com.example.tmate;

import android.content.Context;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;

public class KakaoSDKAdapter extends KakaoAdapter {
    @Override

    public ISessionConfig getSessionConfig() {

        return new ISessionConfig() {
            // 로그인 시에 인증 타입을 지정합니다.
            @Override
            public AuthType[] getAuthTypes() {
                // Auth Type
                return new AuthType[] {AuthType.KAKAO_ACCOUNT};
            }

            // 로그인 웹뷰에서 pause와 resume시에 타이머를 설정하여, CPU의 소모를 절약 할 지의 여부를 지정합니다.

            // true로 지정할 경우, 로그인 웹뷰의 onPuase()와 onResume()에 타이머를 설정해야 합니다.
            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }

            // 로그인 시 토큰을 저장할 때의 암호화 여부를 지정합니다.
            @Override
            public boolean isSecureMode() {
                return false;
            }

            // 일반 사용자가 아닌 Kakao와 제휴 된 앱에서 사용되는 값입니다.

            // 값을 지정하지 않을 경우, ApprovalType.INDIVIDUAL 값으로 사용됩니다.
            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }
            // 로그인 웹뷰에서 email 입력 폼의 데이터를 저장할 지 여부를 지정합니다.

            @Override
            public boolean isSaveFormData() {
                return true;
            }
        };
    }

    // Application이 가지고 있는 정보를 얻기 위한 인터페이스 입니다.
    @Override
    public IApplicationConfig getApplicationConfig() {
        return new IApplicationConfig() {
            @Override
            public Context getApplicationContext() {
                return GlobalApplication.getGlobalApplicationContext();
            }
        };
    }
}
