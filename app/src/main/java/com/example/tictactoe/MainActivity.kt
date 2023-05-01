package com.example.tictactoe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

enum class GameMode {
    PvP,
    PvC,
    CvC
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pvpButton = findViewById<Button>(R.id.pvpButton)
        val pvcButton = findViewById<Button>(R.id.pvcButton)
        val cvcButton = findViewById<Button>(R.id.cvcButton)
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

        cvcButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("gameMode", GameMode.CvC.ordinal)
            startActivity(intent)
        }

        exitGameButton.setOnClickListener {
            finish()
        }
    }
}
