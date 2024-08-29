package com.versao1.controledindin

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.versao1.controledindin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var banco: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Abre ou cria o banco de dados no diretório correto
        banco = openOrCreateDatabase("minhascontas.sqlite", MODE_PRIVATE, null)
        banco.execSQL("CREATE TABLE IF NOT EXISTS cadastro (_id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, detalhe TEXT, valor REAL, data TEXT)")

        // Configura os Spinners
        setupSpinners()

        // Configura os botões
        setButtonListener()
    }

    private fun setupSpinners() {
        // Configurar o primeiro Spinner (Crédito/Débito)
        val tipos = resources.getStringArray(R.array.tipo)
        val tipoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipo.adapter = tipoAdapter

        // Configurar o comportamento do Spinner de tipo
        binding.spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedTipo = parent.getItemAtPosition(position).toString()

                // Atualizar o segundo Spinner baseado na seleção
                updateDetalheSpinner(selectedTipo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun updateDetalheSpinner(tipo: String) {
        val detalheArray = when (tipo) {
            "Crédito" -> R.array.detalhe
            "Débito" -> R.array.sp_debito
            else -> R.array.detalhe // fallback para evitar erro
        }

        val detalheAdapter = ArrayAdapter.createFromResource(
            this,
            detalheArray,
            android.R.layout.simple_spinner_item
        )
        detalheAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDetalhe.adapter = detalheAdapter
    }

    private fun setButtonListener() {
        binding.btLancar.setOnClickListener {
            // Lógica para lançar uma nova entrada no banco de dados
            val tipo = binding.spTipo.selectedItem.toString()
            val detalhe = binding.spDetalhe.selectedItem.toString()
            val valor = binding.tvValor.text.toString().toDouble()
            val data = binding.editTextDate.text.toString()

            // Insere os dados na tabela 'cadastro'
            banco.execSQL("INSERT INTO cadastro (tipo, detalhe, valor, data) VALUES (?, ?, ?, ?)",
                arrayOf(tipo, detalhe, valor, data))
        }

        binding.buttonVerLancamentos.setOnClickListener {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
        }

       /* binding.buttonSaldo.setOnClickListener {
            val intent = Intent(this, SaldoActivity::class.java)
            startActivity(intent)
        }*/
    }
}
