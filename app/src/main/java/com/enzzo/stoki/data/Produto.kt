package com.enzzo.stoki.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "produtos",
    indices = [Index(value = ["codigo_barras"], unique = true)]
)
data class Produto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "codigo_barras")
    val codigoBarras: String?,

    @ColumnInfo(name = "foto_uri")
    val fotoUri: String?,

    @ColumnInfo(name = "categoria")
    val categoria: String,

    @ColumnInfo(name = "quantidade_estoque")
    var quantidadeEstoque: Int,

    @ColumnInfo(name = "preco_custo")
    val precoCusto: Double,

    @ColumnInfo(name = "preco_venda")
    val precoVenda: Double,

    @ColumnInfo(name = "data_atualizacao")
    val dataAtualizacao: Long
)