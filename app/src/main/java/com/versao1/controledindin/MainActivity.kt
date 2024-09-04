package com.versao1.controledindin

import android.app.DatePickerDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.versao1.controledindin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Variáveis de ligação e banco de dados
    private lateinit var binding: ActivityMainBinding
    private lateinit var banco: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout e inicializa a ligação
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cria ou abre o banco de dados
        banco = openOrCreateDatabase("minhascontas.sqlite", MODE_PRIVATE, null)
        banco.execSQL("CREATE TABLE IF NOT EXISTS cadastro (_id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, detalhe TEXT, valor REAL, data TEXT)")

        // Configura os Spinners, listeners de botão, e o formatador de valor
        setupSpinners()
        setButtonListener()
        setupDatePicker()
        setupValueFormatting()
    }

    // Configura os Spinners de tipo e detalhe
    private fun setupSpinners() {
        // Carrega os tipos a partir dos recursos
        val tipos = resources.getStringArray(R.array.tipo)
        val tipoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipo.adapter = tipoAdapter

        // Listener para quando o usuário seleciona um tipo
        binding.spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Atualiza o Spinner de detalhes com base no tipo selecionado
                val selectedTipo = parent.getItemAtPosition(position).toString()
                updateDetalheSpinner(selectedTipo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Atualiza o Spinner de detalhes com base no tipo selecionado
    private fun updateDetalheSpinner(tipo: String) {
        val detalheArray = when (tipo) {
            "Crédito" -> R.array.detalhe
            "Débito" -> R.array.sp_debito
            else -> R.array.detalhe
        }

        // Adapta e define o Spinner de detalhes
        val detalheAdapter = ArrayAdapter.createFromResource(
            this,
            detalheArray,
            android.R.layout.simple_spinner_item
        )
        detalheAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDetalhe.adapter = detalheAdapter
    }

    // Configura o formatador de valor monetário para o campo de valor
    private fun setupValueFormatting() {
        val locale = Locale("pt", "BR")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        // Adiciona um TextWatcher para formatar a entrada do valor conforme o usuário digita
        binding.tvValor.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Verifica se a string mudou para evitar loop infinito
                if (s.toString() != current) {
                    binding.tvValor.removeTextChangedListener(this)

                    // Remove a formatação anterior para evitar problemas com a conversão
                    val cleanString = s.toString().replace("[R$,.]".toRegex(), "").trim()
                    val parsed = if (cleanString.isNotEmpty()) cleanString.toDouble() else 0.0
                    val formatted = currencyFormat.format(parsed / 100)

                    current = formatted
                    binding.tvValor.setText(formatted)
                    binding.tvValor.setSelection(formatted.length)

                    binding.tvValor.addTextChangedListener(this)
                }
            }
        })
    }

    // Configura os listeners dos botões
    private fun setButtonListener() {
        // Listener do botão para lançar um novo registro no banco de dados
        binding.btLancar.setOnClickListener {
            val tipo = binding.spTipo.selectedItem.toString()
            val detalhe = binding.spDetalhe.selectedItem.toString()
            val data = binding.edDate.text.toString()

            // Extrai o valor, remove os caracteres de formatação, e converte para Double
            val valorStr = binding.tvValor.text.toString().replace("[R$,.]".toRegex(), "").trim()
            val valor = valorStr.toDoubleOrNull()?.div(100)

            if (valor != null) {
                // Insere o registro no banco de dados em uma thread separada
                lifecycleScope.launch(Dispatchers.IO) {
                    banco.execSQL(
                        "INSERT INTO cadastro (tipo, detalhe, valor, data) VALUES (?, ?, ?, ?)",
                        arrayOf(tipo, detalhe, valor, data)
                    )
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Lançamento realizado com sucesso!", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Mostra um aviso se o valor for inválido
                Toast.makeText(this, "Valor inválido!", Toast.LENGTH_LONG).show()
            }
        }

        // Listener do botão para visualizar os lançamentos
        binding.buttonVerLancamentos.setOnClickListener {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
        }

        // Listener do botão para calcular e mostrar o saldo atual
        binding.buttonSaldo.setOnClickListener {
            calcularEShowSaldo()
        }
    }

    // Configura o DatePicker para a seleção de data
    private fun setupDatePicker() {
        binding.edDate.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            // Abre o DatePickerDialog para seleção de data
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    binding.edDate.setText(selectedDate)
                },
                ano,
                mes,
                dia
            )

            datePickerDialog.show()
        }
    }

    // Calcula e exibe o saldo atual
    private fun calcularEShowSaldo() {
        lifecycleScope.launch(Dispatchers.IO) {
            val totalCredito = obterTotalPorTipo("Crédito")
            val totalDebito = obterTotalPorTipo("Débito")
            val saldo = totalCredito - totalDebito

            // Exibe o saldo em um Toast
            launch(Dispatchers.Main) {
                val mensagem = String.format("Saldo Atual: R$ %.2f", saldo)
                Toast.makeText(this@MainActivity, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Obtém o total de valores de um determinado tipo (Crédito ou Débito)
    private fun obterTotalPorTipo(tipo: String): Double {
        val cursor = banco.rawQuery(
            "SELECT SUM(valor) FROM cadastro WHERE tipo = ?",
            arrayOf(tipo)
        )
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = if (cursor.isNull(0)) 0.0 else cursor.getDouble(0)
        }
        cursor.close()
        return total
    }
}



