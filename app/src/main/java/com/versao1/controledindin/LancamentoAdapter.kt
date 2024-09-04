package com.versao1.controledindin.RecyclerView

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.versao1.controledindin.Lancamento
import com.versao1.controledindin.R

class LancamentoAdapter(
    private val lancamentos: List<Lancamento>,
    private val onDeleteClick: (Lancamento) -> Unit
) : RecyclerView.Adapter<LancamentoAdapter.LancamentoViewHolder>() {

    inner class LancamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomeItem: TextView = itemView.findViewById(R.id.tv_nome_item)
        val tvValorItem: TextView = itemView.findViewById(R.id.tv_valor_item)
        val tvDataItem: TextView = itemView.findViewById(R.id.tv_data_item)
        val ivDeleteItem: ImageView = itemView.findViewById(R.id.iv_delete_item)

        @SuppressLint("SetTextI18n", "DefaultLocale")

        fun bind(lancamento: Lancamento) {
            tvNomeItem.text = "${lancamento.tipo}: ${lancamento.detalhe}"
            tvValorItem.text = String.format("R$ %.2f", lancamento.valor)
            tvDataItem.text = lancamento.data

            // Configura o botão de exclusão
            ivDeleteItem.setOnClickListener {
                onDeleteClick(lancamento)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LancamentoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_lancamento, parent, false)
        return LancamentoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LancamentoViewHolder, position: Int) {
        holder.bind(lancamentos[position])
    }

    override fun getItemCount(): Int = lancamentos.size
}
