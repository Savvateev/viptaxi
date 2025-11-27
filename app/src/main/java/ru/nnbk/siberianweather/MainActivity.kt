package ru.nnbk.siberianweather

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.nnbk.siberianweather.databinding.ActivityMainBinding
import android.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity

var str: String = " "
var status: Char = ' '



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        runSshCommandWithRetry()
        println()
        if (status != ' ') {
            val intent = Intent(this@MainActivity, YandexMapActivity::class.java)
            intent.putExtra("PORT_STATUS", status)
            startActivity(intent)
        }
    }

    fun runSshCommandWithRetry() {
        lifecycleScope.launch {
            try {
                str = sshCommand("interface print")
                status = getPortStatus(str, getText(R.string.port) as String)
                println()
                // Здесь можно обработать str, если нужно
            } catch (e: Exception) {
                showRetryDialog()
            }
        }
    }

    private fun showRetryDialog() {
        AlertDialog.Builder(this)
            .setMessage("Сервис временно недоступен")
            .setCancelable(false)
            .setPositiveButton("Повторить") { dialog, _ ->
                dialog.dismiss()
                runSshCommandWithRetry()
            }
            .setNegativeButton("Выход") { _, _ ->
                ActivityCompat.finishAffinity(this);
                System.exit(0);
            }
            .show()
    }
}