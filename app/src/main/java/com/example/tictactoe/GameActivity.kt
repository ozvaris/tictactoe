package com.example.tictactoe

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStatusText = findViewById<TextView>(R.id.statusTextView)
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        gameMode = GameMode.values()[intent.getIntExtra("gameMode", 0)]

        gameBoard = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9')

        buttons = Array(gridLayout.childCount) { i ->
            val button = gridLayout.getChildAt(i) as Button
            button.text = gameBoard[i].toString()
            button
        }

        gameStatusText.text = getString(R.string.player_turn, currentPlayer)
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
                    gameStatusText.text = "Game Over"
                } else {
                    currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
                    gameStatusText.text = getString(R.string.player_turn, currentPlayer)

                    if (gameMode == GameMode.PvC && currentPlayer == 'O') {
                        computerMove()
                    }
                }
            }
        }
    }

    private fun computerMove() {
        val availableCells =
            gameBoard.filter { it != 'X' && it != 'O' }.map { it.toString().toInt() - 1 }
        val randomCellIndex = availableCells[Random.nextInt(availableCells.size)]

        gameBoard[randomCellIndex] = currentPlayer
        buttons[randomCellIndex].text = currentPlayer.toString()

        if (evaluateBoard(gameBoard) == 10 || evaluateBoard(gameBoard) == -10 || isBoardFull(
                gameBoard
            )
        ) {
            gameStatusText.text = "Game Over"
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