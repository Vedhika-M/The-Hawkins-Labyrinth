package com.example.thehawkinslabyrinth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun GameoverScreen(
    navController: NavController,
    userHP:Int,
    vecnaHP:Int
) {
    val context = LocalContext.current

    var playerName by remember {
        mutableStateOf("")
    }

    var scores = remember {
        mutableStateOf<List<LeaderboardEntry>>(emptyList())
        }

    LaunchedEffect(Unit) {
        scores.value = LeaderboardManager.getScores(context)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (userHP > vecnaHP){
            Image(
                painter = painterResource(id=R.drawable.win_bg),
                contentDescription = "win background",
                contentScale = ContentScale.Crop
            )
        }else{
            Image(
                painter = painterResource(id=R.drawable.lose_bg),
                contentDescription = "lose background",
                contentScale = ContentScale.Crop
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                Text(
                    text = "Most Dominant Victories",
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
                    fontSize = 30.sp
                )

                LazyColumn {
                    items(scores.value.take(10)) { entry ->
                        Text(
                            text = "${entry.playerName} : ${entry.score}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp,
                            color = Color.Blue,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(100.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                TextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Enter your name") }
                )

                Spacer(modifier = Modifier.height(50.dp))

                ElevatedButton(
                    onClick = {
                        if (userHP > vecnaHP) {
                            LeaderboardManager.saveScore(
                                context = context,
                                playerName = playerName,
                                playerHP = userHP,
                                vecnaHP = vecnaHP
                            )
                        }
                        scores.value = LeaderboardManager.getScores(context)
                    }
                ) {
                    Text("SAVE SCORE")
                }
            }

            Button(
                onClick = {
                    navController.navigate("gamescreen")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
                shape = RectangleShape
            ) {
                Text("NEW GAME")
            }
        }
    }
}