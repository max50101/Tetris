package com.bignerdranch.android.tetris

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bignerdranch.android.tetris.storage.AppPreferences
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess
//Класс основного меню , содержит 3 кнопки начало игры сброс рекорда и выход с программы
class MainActivity : AppCompatActivity() {
    var tvHightScore:TextView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val btnExit: Button =findViewById<Button>(R.id.btn_exit)
        val btnNewGame=findViewById<Button>(R.id.btn_new_game)
        val btnResetScore=findViewById<Button>(R.id.btn_reset_score)
        tvHightScore=findViewById<TextView>(R.id.tv_high_score)
        btnExit.setOnClickListener(this::handleExiEvent)
        btnNewGame.setOnClickListener(this::onBtnNewGameClick)
        btnResetScore.setOnClickListener(this::onBtnResetScore)
        val preferences=AppPreferences(this)
        tvHightScore?.text="High score: ${preferences.getHighScore()}"

    }
    private fun onBtnResetScore(view:View){
        val preferences=AppPreferences(this)
         preferences.clearHighScore()
        //Snackbar.make(view,"Score Successfully reset",Snackbar.LENGTH_SHORT).show()
        Toast.makeText(this,"Score Successfully reset",Toast.LENGTH_SHORT).show()
        tvHightScore?.text="High score: ${preferences.getHighScore()}"
    }
    private fun onBtnNewGameClick(view:View){
        val intent: Intent =Intent(this,GameActivity::class.java)
        startActivity(intent)
    }
   private  fun handleExiEvent(view: View){
        exitProcess(0)
    }
}
