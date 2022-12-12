package com.example.textscan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.textscan.ui.theme.TextScanTheme
import com.github.demidko.aot.MorphologyTag
import com.github.demidko.aot.WordformMeaning.lookupForMeanings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TextScanTheme {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppScreen()
                }
            }
        }
    }
}

@Composable
private fun AppScreen() {
    val context = LocalContext.current

    var value by remember { mutableStateOf("") }

    var readyValue by remember { mutableStateOf("") }

    var listWords by remember { mutableStateOf<List<String>?>(null) }

    val otherList = remember { listOf(",", ".", "!", "?", "`", "'", "\"", ":", "--") }

    var morphologyTags by remember { mutableStateOf(emptyMap<String, List<MorphologyTag>>()) }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = value,
            valueChange = { value = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            valueChange = { readyValue = value }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (readyValue.isNotEmpty()) {
            Text(text = "Предложение: $readyValue")

            var replacedList = remember(readyValue) { readyValue }

            otherList.forEach {
                replacedList = replacedList.replace(it, "")
            }

            listWords = remember(readyValue) {
                replacedList.trim().split(" ")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LaunchedEffect(key1 = listWords) {
            launch {
                withContext(Dispatchers.Main) {
                    if (!listWords.isNullOrEmpty()) {
                        listWords?.forEach {
                            val meanings = lookupForMeanings(it)
                            morphologyTags = morphologyTags + Pair(
                                it,
                                meanings.firstOrNull()?.morphology ?: listOf()
                            )
                        }
                    }

                    println(morphologyTags)
                }
            }
        }

        if (morphologyTags.isNotEmpty()) {
            Text(text = "Разбор: ")

            Spacer(modifier = Modifier.height(8.dp))

            morphologyTags.forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = "${it.key} -")

                    if (it.value.isEmpty()) {
                        Text(text = " Слово не известно")
                    } else {
                        it.value.forEach { morphologyTag ->
                            val uriString = when (morphologyTag.name) {
                                MorphologyTag.Noun.name -> "https://ru.wikipedia.org/wiki/%D0%98%D0%BC%D1%8F_%D1%81%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B8%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5#:~:text=%D0%98%CC%81%D0%BC%D1%8F%20%D1%81%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B8%CC%81%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5%20(%D0%B8%D0%BB%D0%B8%20%D0%BF%D1%80%D0%BE%D1%81%D1%82%D0%BE%20%D1%81%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B8%CC%81%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5),%D0%BD%D0%B0%20%D0%B2%D0%BE%D0%BF%D1%80%D0%BE%D1%81%D1%8B%20%C2%AB%D0%BA%D1%82%D0%BE%3F%C2%BB%20%D0%B8%D0%BB%D0%B8%20%C2%AB%D1%87%D1%82%D0%BE%3F%C2%BB"
                                MorphologyTag.FirstPerson.name -> ""
                                else -> ""
                            }

                            val intentRules = remember(it.value) {
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(uriString)
                                )
                            }

                            Text(
                                text = " $morphologyTag,",
                                color = Color.Blue,
                                modifier = Modifier
                                    .clickable(MutableInteractionSource(), null) {
                                        context.startActivity(intentRules)
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


    }
}

@Composable
fun CustomTextField(
    value: String,
    valueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = valueChange,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CustomButton(
    valueChange: () -> Unit
) {
    Button(
        onClick = { valueChange() }
    ) {
        Text(text = "Готово")
    }
}




