import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    fun login(id: String, password: String) {
        viewModelScope.launch {
            val response = RetrofitClient.apiService.login(AuthRequest(id, password))
            if (response.isSuccessful) {
                val token = response.body()?.token ?: return@launch
                prefs.edit().putString("key", token).apply()
                _status.value = "로그인 성공"
            } else {
                _status.value = "로그인 실패: ${response.code()}"
            }
        }
    }

    fun logout() {
        prefs.edit().remove("key").apply()
        _status.value = "로그아웃 완료"
    }

    fun register(id: String, password: String) {
        viewModelScope.launch {
            val response = RetrofitClient.apiService.register(AuthRequest(id, password))
            if (response.isSuccessful) {
                val token = response.body()?.token ?: return@launch
                prefs.edit().putString("key", token).apply()
                _status.value = "회원가입 성공"
            } else {
                _status.value = "회원가입 실패: ${response.code()}"
            }
        }
    }

    fun fetchMyPage() {
        viewModelScope.launch {
            val token = prefs.getString("key", "") ?: ""
            try {
                val response = RetrofitClient.apiService.getMyPage("Bearer $token")
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "응답 없음"
                    _status.value = "mypage: $message"
                } else {
                    _status.value = "mypage 실패: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _status.value = "예외 발생: ${e.localizedMessage}"
            }
        }
    }

    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            val token = prefs.getString("key", "") ?: ""
            try {
                val response = RetrofitClient.apiService.createPost(
                    token = "Bearer $token",
                    post = PostRequest(title, content)
                )
                if (response.isSuccessful) {
                    _status.value = "게시글 등록 성공"
                } else {
                    _status.value = "등록 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _status.value = "에러 발생: ${e.localizedMessage}"
            }
        }
    }
}
