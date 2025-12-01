package com.kaiza.trucopoint

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var dupla1Score = 0
    private var dupla2Score = 0
    private var maxScore = 12
    private var dupla1Nome = "Nós"
    private var dupla2Nome = "Eles"
    
    // Controle de pontos consecutivos de 1
    private var dupla1ConsecutiveOnes = 0
    private var dupla2ConsecutiveOnes = 0
    private var mediaPlayer: MediaPlayer? = null
    
    // Controle de áudio aos 11 pontos
    private var dupla1Played11Sound = false
    private var dupla2Played11Sound = false

    private lateinit var dupla1ScoreText: TextView
    private lateinit var dupla2ScoreText: TextView
    private lateinit var dupla1NameText: TextView
    private lateinit var dupla2NameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Carregar dados salvos
        loadData()

        // Inicializar views
        dupla1ScoreText = findViewById(R.id.dupla1Score)
        dupla2ScoreText = findViewById(R.id.dupla2Score)
        dupla1NameText = findViewById(R.id.dupla1Name)
        dupla2NameText = findViewById(R.id.dupla2Name)

        // Botões Dupla 1
        findViewById<ImageButton>(R.id.btnDupla1Add1).setOnClickListener { addPoints(1, 1) }
        findViewById<ImageButton>(R.id.btnDupla1Add3).setOnClickListener { addPoints(1, 3) }
        findViewById<ImageButton>(R.id.btnDupla1Remove).setOnClickListener { removePoint(1) }

        // Botões Dupla 2
        findViewById<ImageButton>(R.id.btnDupla2Add1).setOnClickListener { addPoints(2, 1) }
        findViewById<ImageButton>(R.id.btnDupla2Add3).setOnClickListener { addPoints(2, 3) }
        findViewById<ImageButton>(R.id.btnDupla2Remove).setOnClickListener { removePoint(2) }

        // Botões de editar
        findViewById<ImageButton>(R.id.btnEditDupla1).setOnClickListener { showEditDupla1Dialog() }
        findViewById<ImageButton>(R.id.btnEditDupla2).setOnClickListener { showEditDupla2Dialog() }

        // Botão resetar
        findViewById<Button>(R.id.btnReset).setOnClickListener { resetGame() }

        // Botão compartilhar app
        findViewById<Button>(R.id.btnShareApp).setOnClickListener { shareApp() }

        updateUI()

    }
    private fun addPoints(dupla: Int, points: Int) {
        if (dupla == 1) {
            dupla1Score = (dupla1Score + points).coerceAtMost(maxScore)
            // Rastrear pontos consecutivos de 1
            if (points == 1) {
                dupla1ConsecutiveOnes++
                dupla2ConsecutiveOnes = 0 // Resetar contador da outra dupla
                if (dupla1ConsecutiveOnes == 4) {
                    playTrucoSound()
                    dupla1ConsecutiveOnes = 0
                }
            } else {
                dupla1ConsecutiveOnes = 0
            }
        } else {
            dupla2Score = (dupla2Score + points).coerceAtMost(maxScore)
            // Rastrear pontos consecutivos de 1
            if (points == 1) {
                dupla2ConsecutiveOnes++
                dupla1ConsecutiveOnes = 0 // Resetar contador da outra dupla
                if (dupla2ConsecutiveOnes == 4) {
                    playTrucoSound()
                    dupla2ConsecutiveOnes = 0
                }
            } else {
                dupla2ConsecutiveOnes = 0
            }
        }
        
        // Verificar se atingiu 11 pontos para tocar som especial
        if (dupla == 1 && dupla1Score == 11 && !dupla1Played11Sound) {
            play11PointsSound()
            dupla1Played11Sound = true
        } else if (dupla == 2 && dupla2Score == 11 && !dupla2Played11Sound) {
            play11PointsSound()
            dupla2Played11Sound = true
        }
        
        saveData()
        updateUI()
        checkGameEnd()
    }

    private fun removePoint(dupla: Int) {
        if (dupla == 1 && dupla1Score > 0) {
            dupla1Score--
            dupla1ConsecutiveOnes = 0 // Resetar contador ao remover ponto
            // Resetar flag de áudio de 11 pontos se voltar abaixo de 11
            if (dupla1Score < 11) {
                dupla1Played11Sound = false
            }
        } else if (dupla == 2 && dupla2Score > 0) {
            dupla2Score--
            dupla2ConsecutiveOnes = 0 // Resetar contador ao remover ponto
            // Resetar flag de áudio de 11 pontos se voltar abaixo de 11
            if (dupla2Score < 11) {
                dupla2Played11Sound = false
            }
        }
        saveData()
        updateUI()
    }

    private fun updateUI() {
        dupla1ScoreText.text = dupla1Score.toString()
        dupla2ScoreText.text = dupla2Score.toString()
        dupla1NameText.text = dupla1Nome
        dupla2NameText.text = dupla2Nome
    }

    private fun checkGameEnd() {
        if (dupla1Score >= maxScore) {
            showGameEndDialog(dupla1Nome)
        } else if (dupla2Score >= maxScore) {
            showGameEndDialog(dupla2Nome)
        }
    }

    private fun showGameEndDialog(vencedor: String) {
        AlertDialog.Builder(this)
            .setTitle("Fim de Jogo!")
            .setMessage("$vencedor venceu!")
            .setPositiveButton("Nova Partida") { _, _ -> resetGame() }
            .setCancelable(false)
            .show()
    }

    private fun resetGame() {
        dupla1Score = 0
        dupla2Score = 0
        dupla1ConsecutiveOnes = 0
        dupla2ConsecutiveOnes = 0
        dupla1Played11Sound = false
        dupla2Played11Sound = false
        saveData()
        updateUI()
    }
    
    private fun playTrucoSound() {
        try {
            // Liberar MediaPlayer anterior se existir
            mediaPlayer?.release()
            
            // Criar novo MediaPlayer com o áudio
            mediaPlayer = MediaPlayer.create(this, R.raw.truco_sound)
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun play11PointsSound() {
        try {
            // Liberar MediaPlayer anterior se existir
            mediaPlayer?.release()
            
            // Criar novo MediaPlayer com o áudio de 11 pontos
            mediaPlayer = MediaPlayer.create(this, R.raw.truco_sound2)
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun showEditDupla1Dialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_names, null)
        val editDupla1Nome = dialogView.findViewById<EditText>(R.id.editDupla1Nome)
        val editDupla2Nome = dialogView.findViewById<EditText>(R.id.editDupla2Nome)
        val labelDupla2 = dialogView.findViewById<TextView>(R.id.labelDupla2)

        editDupla1Nome.setText(dupla1Nome)
        editDupla2Nome.visibility = android.view.View.GONE
        labelDupla2.visibility = android.view.View.GONE

        AlertDialog.Builder(this)
            .setTitle("Editar Nome - Dupla 1")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                dupla1Nome = editDupla1Nome.text.toString()
                saveData()
                updateUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDupla2Dialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_names, null)
        val editDupla1Nome = dialogView.findViewById<EditText>(R.id.editDupla1Nome)
        val editDupla2Nome = dialogView.findViewById<EditText>(R.id.editDupla2Nome)
        val labelDupla1 = dialogView.findViewById<TextView>(R.id.labelDupla1)

        editDupla1Nome.visibility = android.view.View.GONE
        labelDupla1.visibility = android.view.View.GONE
        editDupla2Nome.setText(dupla2Nome)

        AlertDialog.Builder(this)
            .setTitle("Editar Nome - Dupla 2")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                dupla2Nome = editDupla2Nome.text.toString()
                saveData()
                updateUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveData() {
        val prefs = getSharedPreferences("TrucoData", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("dupla1Score", dupla1Score)
            putInt("dupla2Score", dupla2Score)
            putString("dupla1Nome", dupla1Nome)
            putString("dupla2Nome", dupla2Nome)
            apply()
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences("TrucoData", MODE_PRIVATE)
        dupla1Score = prefs.getInt("dupla1Score", 0)
        dupla2Score = prefs.getInt("dupla2Score", 0)
        dupla1Nome = prefs.getString("dupla1Nome", "Nós") ?: "Nós"
        dupla2Nome = prefs.getString("dupla2Nome", "Eles") ?: "Eles"
    }

    private fun shareApp() {
        // URL da Play Store - você deve atualizar com o link real após publicar
        val packageName = packageName
        val playStoreUrl = "https://play.google.com/store/apps/details?id=$packageName"
        
        val mensagem = """
            🃏 *TrucoPoint* - Contador de Truco 🃏
            
            Baixe o melhor app para contar pontos no truco!
            
            $playStoreUrl
        """.trimIndent()

        try {
            // Tentar abrir WhatsApp diretamente
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/?text=${Uri.encode(mensagem)}")
            startActivity(intent)
        } catch (e: Exception) {
            // Se o WhatsApp não estiver instalado, usar compartilhamento genérico
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, mensagem)
            startActivity(Intent.createChooser(shareIntent, "Compartilhar TrucoPoint"))
        }
    }
}