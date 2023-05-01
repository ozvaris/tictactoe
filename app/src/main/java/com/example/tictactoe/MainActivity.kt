package com.example.tictactoe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

enum class GameMode {
    PvP,
    PvC,
    CvC
}

class MainActivity : AppCompatActivity() {

    private lateinit var menu: Menu
    private var selectedDifficulty: Int = R.id.easy
    private var boardSize: Int = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pvpButton = findViewById<Button>(R.id.pvpButton)
        val pvcButton = findViewById<Button>(R.id.pvcButton)
        // val cvcButton = findViewById<Button>(R.id.cvcButton)
        val exitGameButton = findViewById<Button>(R.id.exitButton)

        pvpButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("gameMode", GameMode.PvP.ordinal)
            startActivity(intent)
        }

        pvcButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("gameMode", GameMode.PvC.ordinal)
            startActivity(intent)
        }

        // cvcButton.setOnClickListener {
        //     val intent = Intent(this, GameActivity::class.java)
        //     intent.putExtra("gameMode", GameMode.CvC.ordinal)
        //     startActivity(intent)
        // }

        exitGameButton.setOnClickListener {
            finish()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(selectedDifficulty)?.isChecked = true
        this.menu = menu
        return true
    }

    private fun setDifficultyCheckState(checkedItemId: Int, uncheckedItemId1: Int, uncheckedItemId2: Int) {
        menu.findItem(checkedItemId).isChecked = false
        menu.findItem(uncheckedItemId1).isChecked = false
        menu.findItem(uncheckedItemId2).isChecked = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.easy, R.id.medium, R.id.hard -> {
                setDifficultyCheckState(R.id.hard, R.id.easy, R.id.medium)
                item.isChecked = true
                selectedDifficulty = item.itemId
                // Seçenek değiştirildiğinde yapılacak işlemler
                true
            }
        }

        return when (item.itemId) {
            R.id.easy -> {
                // Easy seçeneği seçildiğinde yapılacak işlemler
                true
            }
            R.id.medium -> {
                // Medium seçeneği seçildiğinde yapılacak işlemler
                boardSize = 6
                true
            }
            R.id.hard -> {
                // Hard seçeneği seçildiğinde yapılacak işlemler
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
