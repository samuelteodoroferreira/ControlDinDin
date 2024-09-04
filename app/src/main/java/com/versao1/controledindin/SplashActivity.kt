package com.versao1.controledindin
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o layout da Splash Screen
        setContentView(R.layout.activity_splash)

        // Simula um tempo de carregamento
        Thread {
            Thread.sleep(2000)  // Duração de 2 segundos
            // Navega para a MainActivity após o carregamento
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Finaliza a SplashActivity
        }.start()
    }
}
