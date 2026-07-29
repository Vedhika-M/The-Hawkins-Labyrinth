package com.example.thehawkinslabyrinth

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(
            context,
            R.raw.bg_countdown_music
        )
    }

    val moveHistory = remember {
        mutableStateListOf<Move>()
    }

    val board = remember {
        List(5) {
            List(5) {
                Cell(
                    unlockRequirement = (1..10).random(),
                    isUpsideDown = listOf(true, false).random()
                )
            }
        }
    }

    val allCells = (0..4)
        .flatMap { row ->
            (0..4).map { col ->
                row to col
            }
        }
        .shuffled()

    val immuneCells = allCells.take(5)

    val psychicCells = allCells.drop(5).take(5)
    var psycho by remember {
        mutableStateOf(false)
    }

    var playerRow by remember {
        mutableIntStateOf(4)
    }

    var playerCol by remember {
        mutableIntStateOf(0)
    }

    var diceRoll by remember {
        mutableIntStateOf(0)
    }

    var gameStarted by remember {
        mutableStateOf(false)
    }

    val exitRow = remember { (0..4).random() }
    val exitCol = remember { (0..4).random() }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    var userHP by remember {
        mutableIntStateOf(150)
    }
    var vecnaHP by remember {
        mutableIntStateOf(5)
    }

    var showRedFlash by remember {
        mutableStateOf(false)
    }
    var showGreenFlash by remember {
        mutableStateOf(false)
    }

    var isRolling by remember {
        mutableStateOf(false)
    }
    val rotation = remember {
        Animatable(0f)
    }
    val scope = rememberCoroutineScope()

    var immunity by remember{
        mutableIntStateOf(0)
    }

    var lastState by remember{
        mutableStateOf<GameState?>(null)
    }

    var isReplay by remember {
        mutableStateOf(false)
    }

    val offsetX = remember { Animatable(0f) }

    suspend fun shake() {
        repeat(3) {
            offsetX.animateTo(
                20f,
                tween(20)
            )
            offsetX.animateTo(
                -20f,
                tween(20)
            )
        }
        offsetX.animateTo(
            0f,
            tween(20)
        )
    }

    suspend fun replayGame() {
        isReplay = true
        for (move in moveHistory) {
            playerRow = move.row
            playerCol = move.col
            userHP = move.hp
            diceRoll = move.diceRoll

            delay(1000.milliseconds)
        }
        isReplay = false
    }

    fun undoMove(){
        lastState?.let{
            playerRow = it.row
            playerCol = it.col
            userHP = it.hp
            immunity = it.immunity
            psycho = it.psycho
        }
    }

    fun rollDice() {
        if (isRolling) return
        isRolling = true
        scope.launch {
                launch {
                    rotation.snapTo(0f)
                    rotation.animateTo(
                        targetValue = 1080f,
                        animationSpec = tween(1000)
                    )
                }
            delay(1000.milliseconds)
            diceRoll = (1..10).random()
            isRolling = false
        }
    }

    fun moveToCell(row: Int, col: Int) {
        if (!gameStarted) {
            gameStarted = true
        }

        val adjacent = (playerRow - row).absoluteValue + (playerCol - col).absoluteValue == 1

        if (!adjacent) {
            scope.launch {
                shake()
            }
            return
        }

        val cell = board[row][col]

        if (diceRoll < cell.unlockRequirement) {
            scope.launch {
                shake()
            }
            return
        }

        lastState = GameState(
            row = playerRow,
            col = playerCol,
            hp = userHP,
            immunity = immunity,
            psycho = psycho
        )

        playerRow = row
        playerCol = col

        moveHistory.add(Move(playerRow,playerCol,userHP,diceRoll,!cell.isUpsideDown))

        if (!cell.isUnlocked.value) {
            cell.isUnlocked.value = true

            if (Pair(row,col) in immuneCells){
                immunity+=1
            }
            if (Pair(row,col) in psychicCells){
                psycho = true
            }else {
                psycho = false
            }

            if (psycho){
                scope.launch {
                    showGreenFlash = true
                    delay(400.milliseconds)
                    showGreenFlash = false
                }
            }

            if (cell.isUpsideDown) {
                if (immunity>0){
                    userHP-=5
                    immunity-=1
                    scope.launch {
                        showRedFlash = true
                        delay(200.milliseconds)
                        showRedFlash = false
                    }
                }else {
                    userHP -= 15
                    scope.launch {
                        showRedFlash = true
                        delay(400.milliseconds)
                        showRedFlash = false
                    }
                }
            }
        }

        if (row == exitRow && col == exitCol) {
            scope.launch {
                replayGame()
                navController.navigate(
                    "gameoverscreen/$userHP/$vecnaHP"
                )
            }
        }

    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_image),
            contentDescription = "background image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer{
                translationX = offsetX.value
            }
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Row(
                modifier = Modifier.size(screenWidth, (screenHeight - screenWidth) / 2)
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "VECNA",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 40.sp
                    )
                    Text(
                        text = "HP : $vecnaHP",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 40.sp
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "YOU",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 40.sp
                    )
                    Text(
                        text = "HP : $userHP",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 40.sp
                    )
                }
            }
            Box(
                modifier = Modifier.size(screenWidth, screenWidth)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    for (i in 0..4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenWidth / 5)
                        ) {
                            for (j in 0..4) {
                                val cell = board[i][j]
                                val cellColor =Color.DarkGray
                                ElevatedButton(
                                    onClick = {
                                        moveToCell(i, j)
                                        diceRoll = 0
                                    },
                                    modifier = Modifier.size(
                                        screenWidth/5,
                                        screenWidth/5
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = cellColor,
                                        contentColor = Color.Black
                                    ),
                                    shape = RectangleShape,
                                    elevation = ButtonDefaults.elevatedButtonElevation(
                                        defaultElevation = 5.dp
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            i == 4 && j == 0 -> Image(
                                                painter = painterResource(id = R.drawable.start),
                                                contentDescription = "start block",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            i == exitRow && j == exitCol && cell.isUnlocked.value -> Image(
                                                painter = painterResource(id = R.drawable.retreat),
                                                contentDescription = "exit block",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            !cell.isUpsideDown && cell.isUnlocked.value -> Image(
                                                painter = painterResource(id = R.drawable.normal),
                                                contentDescription = "normal block",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            cell.isUpsideDown && cell.isUnlocked.value -> Image(
                                                painter = painterResource(id = R.drawable.upsidedown),
                                                contentDescription = "upsidedown block",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            !cell.isUnlocked.value -> Image(
                                                painter = painterResource(id = R.drawable.block),
                                                contentDescription = "mystery block",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            else -> Image(
                                                painter = painterResource(id = R.drawable.block),
                                                contentDescription = "mystery block",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        if (i == playerRow && j == playerCol) {
                                            Image(
                                                painter = painterResource(id = R.drawable.spider),
                                                contentDescription = "spider image",
                                                modifier = Modifier
                                                    .fillMaxSize(0.7f)
                                                    .align(Alignment.Center),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        if (psycho && !cell.isUnlocked.value){
                                            Text(
                                                text = "${cell.unlockRequirement}",
                                                modifier = Modifier.fillMaxSize(),
                                                fontSize = 20.sp,
                                                textAlign = TextAlign.Center,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.size(
                    screenWidth,
                    (screenHeight - screenWidth) / 3
                ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { rollDice() },
                    modifier = Modifier.size(
                        screenWidth / 2,
                        (screenHeight - screenWidth) / 3
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Casino,
                        contentDescription = "dice",
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer {
                                rotationZ = rotation.value
                            }
                    )
                }
                Text(
                    text = "DICE : $diceRoll",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 50.sp
                )
            }
            ElevatedButton(
                modifier = Modifier.size(
                    screenWidth,
                    (screenHeight - screenWidth) / 6
                ),
                onClick = {
                    board[playerRow][playerCol].isUnlocked.value = false
                    undoMove()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent.copy(alpha=0.5f),
                    contentColor = Color.White
                ),
                shape = RectangleShape
            ) {
                Text(
                    text = "UNDO",
                    fontSize = 30.sp
                )
            }

            LaunchedEffect(Unit) {
                moveHistory.add(
                    Move(
                        4,
                        0,
                        150,
                        0,
                        true
                    )
                )
            }

            LaunchedEffect(gameStarted) {
                if (gameStarted) {
                    mediaPlayer.isLooping = true
                    mediaPlayer.start()
                    while (true) {
                        vecnaHP += 1
                        delay(1000.milliseconds)
                    }
                }
            }
            DisposableEffect(Unit) {
                onDispose{
                    mediaPlayer.release()
                }
            }
        }
    }

    if (showRedFlash){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color=Color.Red.copy(alpha=0.4f))
        )
    }
    if (showGreenFlash){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color=Color.Green.copy(alpha=0.4f))
        )
    }
}