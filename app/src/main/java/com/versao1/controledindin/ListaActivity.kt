package com.versao1.controledindin

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ListaActivity : AppCompatActivity() {

    private lateinit var banco: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_activity) // Certifique-se de que você tenha um layout para essa Activity

        // Inicialize o banco de dados
        banco = openOrCreateDatabase("minhascontas.sqlite", MODE_PRIVATE, null)

        // Consulta e exibe os dados
        val cursor = banco.rawQuery("SELECT * FROM cadastro", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val detalhe = cursor.getString(cursor.getColumnIndexOrThrow("detalhe"))
                val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
                val data = cursor.getString(cursor.getColumnIndexOrThrow("data"))

                // Aqui você pode adicionar código para exibir os dados em uma lista ou outro componente de UI
                Log.d("ListaActivity", "ID: $id, Tipo: $tipo, Detalhe: $detalhe, Valor: $valor, Data: $data")
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}
