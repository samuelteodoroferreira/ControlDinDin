package com.versao1.controledindin

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaActivity : AppCompatActivity() {

    private lateinit var banco: SQLiteDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var lancamentoAdapter: LancamentoAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Corrigido para o layout correto da tela
        setContentView(R.layout.lista_activity)

        // Inicializa o banco de dados
        banco = openOrCreateDatabase("minhascontas.sqlite", MODE_PRIVATE, null)

        // Inicializa a RecyclerView com o ID correto do layout 'lista_activity'
        recyclerView = findViewById(R.id.recyclerView)  // Use o ID correto do RecyclerView no layout
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configura o botão de voltar ! Decidir se tirou o permance (coisa Inútil
        val buttonVoltar = findViewById<Button>(R.id.button_voltar)
        buttonVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Carrega os dados do banco de dados e configura o adapter
        carregarLancamentos()
    }

    private fun carregarLancamentos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val lancamentos = obterLancamentos()

            launch(Dispatchers.Main) {
                lancamentoAdapter = LancamentoAdapter(lancamentos) { lancamento: Lancamento ->
                    excluirLancamento(lancamento)
                }
                recyclerView.adapter = lancamentoAdapter
            }
        }
    }

    private fun obterLancamentos(): List<Lancamento> {
        val lancamentos = mutableListOf<Lancamento>()
        val cursor = banco.rawQuery("SELECT _id, tipo, detalhe, valor, data FROM cadastro", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val detalhe = cursor.getString(cursor.getColumnIndexOrThrow("detalhe"))
                val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
                val data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
                lancamentos.add(Lancamento(id, tipo, detalhe, valor, data))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lancamentos
    }

    private fun excluirLancamento(lancamento: Lancamento) {
        lifecycleScope.launch(Dispatchers.IO) {
            banco.execSQL("DELETE FROM cadastro WHERE _id = ?", arrayOf(lancamento.id))
            carregarLancamentos()  // Recarrega os lançamentos após exclusão
        }
    }
}
