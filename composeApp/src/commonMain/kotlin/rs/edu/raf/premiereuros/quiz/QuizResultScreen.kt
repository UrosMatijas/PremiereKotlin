package rs.edu.raf.premiereuros.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuizResultRoute(
    score: Float,
    correctAnswers: Int,
    incorrectAnswers: Int,
    usedSeconds: Int,
    remainingSeconds: Int,
    onDone: () -> Unit
) {
    QuizResultScreen(
        score = score,
        correctAnswers = correctAnswers,
        incorrectAnswers = incorrectAnswers,
        usedSeconds = usedSeconds,
        remainingSeconds = remainingSeconds,
        onDone = onDone
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    score: Float,
    correctAnswers: Int,
    incorrectAnswers: Int,
    usedSeconds: Int,
    remainingSeconds: Int,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Quiz Result") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val roundedScore = (score * 100).toInt() / 100f
            Text("Score: $roundedScore")
            Text("Correct answers: $correctAnswers")
            Text("Incorrect answers: $incorrectAnswers")
            Text("Time used: ${usedSeconds}s")
            Text("Time remaining: ${remainingSeconds}s")

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}
