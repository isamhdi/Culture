package com.culture.tracker.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.culture.tracker.R

@Composable
fun OnboardingScreen(onDone: (name: String?) -> Unit) {
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.pousse_logo),
                contentDescription = null,
                modifier = Modifier.size(88.dp),
            )
            Text(
                "Bienvenue sur Pousse",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                "Comment veux-tu qu'on t'appelle ? On s'en sert de temps en temps dans les messages d'accueil. Tu peux aussi passer cette étape.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Prénom (optionnel)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onDone(name.takeIf { it.isNotBlank() }) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continuer")
            }
            TextButton(onClick = { onDone(null) }) {
                Text("Passer cette étape")
            }
        }
    }
}
