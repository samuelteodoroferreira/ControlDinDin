package com.versao1.controledindin

import android.app.DatePickerDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.versao1.controledindin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

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

        // Configura os botões para adicionar salvamento no banco
        setButtonListener()

        // Configura o DatePicker
        setupDatePicker()
    }

    private fun setupSpinners() {
        val tipos = resources.getStringArray(R.array.tipo)
        val tipoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipo.adapter = tipoAdapter

        binding.spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selectedTipo = parent.getItemAtPosition(position).toString()
                updateDetalheSpinner(selectedTipo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateDetalheSpinner(tipo: String) {
        val detalheArray = when (tipo) {
            "Crédito" -> R.array.detalhe
            "Débito" -> R.array.sp_debito
            else -> R.array.detalhe
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
            val tipo = binding.spTipo.selectedItem.toString()
            val detalhe = binding.spDetalhe.selectedItem.toString()
            val data = binding.edDate.text.toString()
            val valorStr = binding.tvValor.text.toString()

            // Verifica se o valor pode ser convertido para Double
            val valor = valorStr.toDoubleOrNull()

            if (valor != null) {
                // Executa a operação de banco de dados em uma thread secundária
                lifecycleScope.launch(Dispatchers.IO) {
                    banco.execSQL(
                        "INSERT INTO cadastro (tipo, detalhe, valor, data) VALUES (?, ?, ?, ?)",
                        arrayOf(tipo, detalhe, valor, data)
                    )
                    // Mostra uma mensagem de sucesso na thread principal
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Lançamento realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Exibe uma mensagem de erro
                Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonVerLancamentos.setOnClickListener {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDatePicker() {
        binding.edDate.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.edDate.setText(selectedDate)
                },
                ano,
                mes,
                dia
            )

            datePickerDialog.show()
        }
    }
}
