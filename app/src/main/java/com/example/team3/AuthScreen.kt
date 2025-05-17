import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val status by viewModel.status.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("ID") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.padding(vertical = 8.dp)) {
            Button(onClick = { viewModel.login(id, password) }, modifier = Modifier.weight(1f)) {
                Text("로그인")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.logout() }, modifier = Modifier.weight(1f)) {
                Text("로그아웃")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.register(id, password) }, modifier = Modifier.weight(1f)) {
                Text("회원가입")
            }
        }
        Button(onClick = { viewModel.fetchMyPage() }, modifier = Modifier.fillMaxWidth()) {
            Text("마이페이지 요청")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("게시글 작성", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )
        Button(
            onClick = { viewModel.createPost(title, content) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("게시글 저장")
        }

        Text("상태: $status", modifier = Modifier.padding(top = 16.dp), fontSize = 15.sp)
    }
}
