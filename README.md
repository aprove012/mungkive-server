# mungkive-server 클라이언트 설정 예시

Android 앱에서 `mungkive-server`와 통신하기 위한 기본 설정입니다.

---

## 필수 설정 요약

### 1. `build.gradle.kts` (Module-level)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.10"
}

dependencies {
    // JSON 직렬화
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Retrofit2 + GSON
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Retrofit 요청 로그 확인용
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // 코루틴
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

### 2. `settings.gradle.kts` (루트 설정)

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("application")
}
```

---

### 3. `AndroidManifest.xml`

```xml
<application
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
</application>
```

---

### 4. `res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

> 가지고 있는 도메인이 없어서서, 위 설정으로로 **HTTP 통신을 허용**해야 합니다.

---

## 코드 설명
- AuthRequest, AuthResponse, MypageResponse, PostRequest
  --
  데이터 클래스, 직렬화 필수, 반드시 서버의 데이터 클래스와 동일해야 함
  
- ApiService.kt
  --
  로그인 post
  ```
  @POST("/login") //http 연결
  suspend fun login(@Body request: AuthRequest): Response<AuthResponse> // 보내는 body는 AuthRequest 데이터 클래스, 서버에서 받는 값은 AuthResponse 데이터 클래스
  ```  
  인증 후 기능
  ```
  @GET("/mypage")
    suspend fun getMyPage(
        @Header("Authorization") token: String // 연결하려면 token 인증
    ): Response<MypageResponse> // 성공하면 서버에서 해당되는 데이터 클래스 리턴
  ```
- AuthViewModel
  --
  본 코드의 주석 확인
  ```
  import kotlinx.coroutines.launch
  // 코루틴으로 비동기 통신

  //AndroidViewModel로 application context 전달 후 SharedPreferences사용
  class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("auth", Context.MODE_PRIVATE) // 토큰 로컬 저장
    private val _status = MutableStateFlow("") // 임시 상태확인용 text
    val status = _status.asStateFlow()

    fun login(id: String, password: String) {
        viewModelScope.launch {
            val response = RetrofitClient.apiService.login(AuthRequest(id, password)) // apiservice로 통신신
            if (response.isSuccessful) {
                val token = response.body()?.token ?: return@launch // 응답에서 jwt 토큰 꺼내기
                prefs.edit().putString("key", token).apply() // 로컬에 토큰 저장
                _status.value = "로그인 성공"
            } else {
                _status.value = "로그인 실패: ${response.code()}"
            }
        }
    }
    ```
## 참고
- Retrofit 설정 시 `Kotlinx Serialization` 사용
- 아이디와 패스워드 입력 후 회원가입 눌러야 아이디 저장됩니다.
