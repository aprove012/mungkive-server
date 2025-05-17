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

## 참고
- Retrofit 설정 시 `Kotlinx Serialization` 사용
