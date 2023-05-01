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
    private var currentPlayer: Char = 'X'

    private lateinit var gameMode: GameMode
    private lateinit var gameStatusText: TextView

    private lateinit var gameBoard: Array<Char>
    private lateinit var buttons: Array<Button>

    inline fun <reified T : Serializable> Intent.serializable(key: String) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
            key,
            T::class.java
        )
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

    private fun showPlayerFirstDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Who goes first?")
            .setCancelable(false)
            .setPositiveButton("Player") { _, _ ->
                // Oyuncu önce başlar, herhangi bir değişiklik yapmaya gerek yok
            }
            .setNegativeButton("Computer") { _, _ ->
                computerMove()
            }
            .show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStatusText = findViewById<TextView>(R.id.statusTextView)
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        gameMode = GameMode.values()[intent.getIntExtra("gameMode", 0)]

        gameBoard = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9')

        buttons = Array(gridLayout.childCount) { i ->
            val button = gridLayout.getChildAt(i) as Button
            // button.text = gameBoard[i].toString()
            button
        }

        gameStatusText.text = getString(R.string.player_turn, currentPlayer)

        if (gameMode == GameMode.PvC) {
            showPlayerFirstDialog()
        }
    }

    private fun showGameOverDialog() {
        val builder = AlertDialog.Builder(this)

        val dialog = builder.setTitle("Game Over")
            .setMessage("Do you want to play again or exit?")
            .setPositiveButton("New Game") { _, _ ->
                finish()
                startActivity(intent)
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
                        gameStatusText.text = getString(R.string.player_wins, currentPlayer)
                    } else {
                        gameStatusText.text = "Draw"
                    }
                    showGameOverDialog()
                } else {
                    currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
                    gameStatusText.text = getString(R.string.player_turn, currentPlayer)

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


    private fun computerMove() {

        // val randomCellIndex = computerMoveRandom(gameBoard, currentPlayer)

        val calculatedCellIndex = computerMoveMinMax(gameBoard, currentPlayer)

        buttons[calculatedCellIndex].text = currentPlayer.toString()

        if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10 || isBoardFull(
                gameBoard
            )
        ) {
            // Add winner information to the gameStatusText
            if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10) {
                gameStatusText.text = getString(R.string.player_wins, currentPlayer)
            } else {
                gameStatusText.text = "Draw"
            }
            showGameOverDialog()
        } else {
            currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
            gameStatusText.text = getString(R.string.player_turn, currentPlayer)

            if (gameMode == GameMode.CvC) {
                computerMove()
            }
        }
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
}