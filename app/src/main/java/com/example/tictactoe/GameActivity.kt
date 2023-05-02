package com.example.tictactoe

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import java.io.Serializable
import kotlin.random.Random
class GameActivity : AppCompatActivity() {

    private lateinit var centerScoreText: TextView
    private lateinit var drawScoreText: TextView

    private var difficulty: Difficulty = Difficulty.MEDIUM
    private var currentPlayer: Char = 'X'

    private var player1Name: String = "Player1"
    private var player2Name: String = "Player2"
    private var player1ShortName: String = "P1"
    private var player2ShortName: String = "P2"

    private var scoreName1: String = player1ShortName
    private var scoreName2: String = player2ShortName
    private var score1: Int = 0
    private var score2: Int = 0
    private var drawScore: Int = 0

    private var scoreText: String = "$player1ShortName $score1-$score2 $player2ShortName"

    private lateinit var gameMode: GameMode
    private lateinit var gameStatusText: TextView

    private lateinit var gameBoard: Array<Char>
    private lateinit var buttons: Array<Button>

    // inline fun <reified T : Serializable> Intent.serializable(key: String) = when {
    //     Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
    //         key,
    //         T::class.java
    //     )
    //     else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    // }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStatusText = findViewById<TextView>(R.id.statusTextView)
        drawScoreText = findViewById<TextView>(R.id.drawScoreTextView)
        centerScoreText = findViewById<TextView>(R.id.centerScoreTextView)
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        gameMode = GameMode.values()[intent.getIntExtra("gameMode", 0)]
        difficulty = Difficulty.values()[intent.getIntExtra("difficulty", 0)]
        gameBoard = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9')

        buttons = Array(gridLayout.childCount) { i ->
            val button = gridLayout.getChildAt(i) as Button
            // button.text = gameBoard[i].toString()
            button
        }

        gameStatusText.text = getString(R.string.player_turn, getPlayerName())

        if (gameMode == GameMode.PvC) {
            showPlayerFirstDialog()
        }
    }

    private fun getPlayerName() : String {
        return if (currentPlayer == 'X') player1Name else player2Name
    }

    private fun showGameOverDialog(resultMessage: String) {
        val builder = AlertDialog.Builder(this)

        val dialog = builder.setTitle("Game Over")
            .setMessage("$resultMessage\nDo you want to play again or exit?")
            .setPositiveButton("New Game") { _, _ ->
                resetGame()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val window = dialog.window
            if (window != null) {
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                layoutParams.y = resources.getDimensionPixelSize(R.dimen.dialog_margin_top) // Add this value to your dimensions file
                window.attributes = layoutParams
            }
        }

        dialog.show()
    }

    private fun resetGame() {
        gameBoard = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9')
        currentPlayer = 'X'
        gameStatusText.text = getString(R.string.player_turn, getPlayerName())

        for (i in buttons.indices) {
            buttons[i].text = ""
        }

        if (gameMode == GameMode.PvC) {
            showPlayerFirstDialog()
        } else if (gameMode == GameMode.CvC) {
            computerMove()
        }
    }

    fun onCellClick(view: View) {
        if (gameMode != GameMode.CvC) {
            val button = view as Button
            val cellIndex = buttons.indexOf(button)

            if (gameBoard[cellIndex] != 'X' && gameBoard[cellIndex] != 'O') {
                gameBoard[cellIndex] = currentPlayer
                button.text = currentPlayer.toString()

                if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10 || isBoardFull(
                        gameBoard
                    )
                ) {
                    // Add winner information to the gameStatusText
                    if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10) {
                        gameStatusText.text = getString(R.string.player_wins, getPlayerName())
                        updateScores(getWinner())
                    } else {
                        gameStatusText.text = "Draw"
                        updateScores(Winner.Draw)
                    }
                    showGameOverDialog(gameStatusText.text.toString())
                } else {
                    currentPlayer = nextPlayer()
                    gameStatusText.text = getString(R.string.player_turn, getPlayerName())

                    if (gameMode == GameMode.PvC) {
                        computerMove()
                    }
                }
            }
        }
    }

    fun minimax(board: Array<Char>, depth: Int, isMaxPlayer: Boolean): Int {
        val score = evaluateBoard(board)

        if (score == 10) {
            return score - depth
        }

        if (score == -10) {
            return score + depth
        }

        if (isBoardFull(board)) {
            return 0
        }

        if (isMaxPlayer) {
            var bestScore = Int.MIN_VALUE

            for (i in 0..8) {
                if (board[i] != 'X' && board[i] != 'O') {
                    board[i] = 'X'

                    val currentScore = minimax(board, depth + 1, false)

                    board[i] = (i + 49).toChar()

                    bestScore = maxOf(bestScore, currentScore)
                }
            }

            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0..8) {
                if (board[i] != 'X' && board[i] != 'O') {
                    board[i] = 'O'

                    val currentScore = minimax(board, depth + 1, true)

                    board[i] = (i + 49).toChar()

                    bestScore = minOf(bestScore, currentScore)
                }
            }

            return bestScore
        }
    }

    fun computerMoveMinMax(board: Array<Char>, symbol: Char): Int{
        var bestScore: Int
        var bestMove = -1

        if (symbol == 'X') {
            bestScore = Int.MIN_VALUE
        } else {
            bestScore = Int.MAX_VALUE
        }

        for (i in 0..8) {
            if (board[i] != 'X' && board[i] != 'O') {
                board[i] = symbol

                val currentScore = minimax(board, 0, symbol == 'O')

                board[i] = (i + 49).toChar()

                if (symbol == 'X') {
                    if (currentScore > bestScore) {
                        bestScore = currentScore
                        bestMove = i
                    }
                } else {
                    if (currentScore < bestScore) {
                        bestScore = currentScore
                        bestMove = i
                    }
                }
            }
        }

        board[bestMove] = symbol
        return bestMove
    }

    fun computerMoveRandom(board: Array<Char>, symbol: Char): Int{
        val availableCells =
            gameBoard.filter { it != 'X' && it != 'O' }.map { it.toString().toInt() - 1 }
        val randomCellIndex = availableCells[Random.nextInt(availableCells.size)]

        board[randomCellIndex] = symbol

        return randomCellIndex
    }

    private fun computerMove(randomPlay: Boolean? = false) {

        var calculatedCellIndex : Int = 0

        if(randomPlay == true) {
            calculatedCellIndex = computerMoveRandom(gameBoard, currentPlayer)
        }
        else {
            calculatedCellIndex = when (difficulty) {
                Difficulty.EASY -> {
                    val randomThreshold = 0.2
                    if (Random.nextDouble() < randomThreshold) {
                        computerMoveRandom(gameBoard, currentPlayer)
                    } else {
                        computerMoveMinMax(gameBoard, currentPlayer)
                    }
                }
                Difficulty.MEDIUM -> {
                    val randomThreshold = 0.5
                    if (Random.nextDouble() < randomThreshold) {
                        computerMoveRandom(gameBoard, currentPlayer)
                    } else {
                        computerMoveMinMax(gameBoard, currentPlayer)
                    }
                }
                Difficulty.HARD -> computerMoveMinMax(gameBoard, currentPlayer)
            }
        }

        buttons[calculatedCellIndex].text = currentPlayer.toString()

        if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10 || isBoardFull(
                gameBoard
            )
        ) {
            // Add winner information to the gameStatusText
            if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10) {
                gameStatusText.text = getString(R.string.player_wins, getPlayerName())
                updateScores(getWinner())
            } else {
                gameStatusText.text = "Draw"
                updateScores(Winner.Draw)
            }
            showGameOverDialog(gameStatusText.text.toString())
        } else {
            currentPlayer = nextPlayer()
            gameStatusText.text = getString(R.string.player_turn, getPlayerName())

            if (gameMode == GameMode.CvC) {
                computerMove()
            }
        }
    }

    private fun getWinner(): Winner {
        return if (currentPlayer == 'X') Winner.X else Winner.O
    }
    private fun nextPlayer(): Char {
        return if (currentPlayer == 'X') 'O' else 'X'
    }

    override fun onResume() {
        super.onResume()

        if (gameMode == GameMode.CvC) {
            computerMove()
        }
    }

    private fun evaluateBoard(board: Array<Char>): Int {
        for (row in 0..2) {
            if (board[row * 3] == board[row * 3 + 1] && board[row * 3 + 1] == board[row * 3 + 2]) {
                return if (board[row * 3] == 'X') 10 else -10
            }
        }

        for (col in 0..2) {
            if (board[col] == board[col + 3] && board[col + 3] == board[col + 6]) {
                return if (board[col] == 'X') 10 else -10
            }
        }

        if (board[0] == board[4] && board[4] == board[8]) {
            return if (board[0] == 'X') 10 else -10
        }

        if (board[2] == board[4] && board[4] == board[6]) {
            return if (board[2] == 'X') 10 else -10
        }

        return 0
    }

    private fun isBoardFull(board: Array<Char>): Boolean {
        return board.all { it == 'X' || it == 'O' }
    }

    private fun setScoreText () {
        centerScoreText.text = "$scoreName1 $score1-$score2 $scoreName2"
    }
    private fun showPlayerFirstDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Who goes first?")
            .setCancelable(false)
            .setPositiveButton("Player") { _, _ ->
                // Oyuncu önce başlar, herhangi bir değişiklik yapmaya gerek yok
                player1Name = "Player"
                player2Name = "Computer"

                player1ShortName = "P"
                player2ShortName = "C"

                scoreName1 = player1ShortName
                scoreName2 = player2ShortName

                setScoreText()
            }
            .setNegativeButton("Computer") { _, _ ->
                player1Name = "Computer"
                player2Name = "Player"

                player1ShortName = "C"
                player2ShortName = "P"

                scoreName1 = player2ShortName
                scoreName2 = player1ShortName

                setScoreText()

                computerMove(true)
            }
            .show()


    }
    private fun updateScores(winner: Winner) {
        when (winner) {
            Winner.X -> {
                when (gameMode) {
                    GameMode.PvC -> {
                        // player1ShortName P ise ve kazanan X ise Player kazanmıştır
                        if (gameMode == GameMode.PvC) if (player1ShortName == "P") score1++ else score2++
                    }
                    else -> {score1++}
                }

                setScoreText()
            }
            Winner.O -> {
                when (gameMode) {
                    GameMode.PvC -> {
                        // player1ShortName P ise ve kazanan 0 ise Player kazanmıştır
                        if (player1ShortName == "P") score2++ else score1++
                    }

                    else -> {score2++}
                }

                setScoreText()
            }
            Winner.Draw -> {
                drawScore++
                drawScoreText.text = "Draw : $drawScore"
            }
        }
    }

    
}