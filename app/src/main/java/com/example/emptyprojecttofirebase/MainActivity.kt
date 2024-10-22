package com.example.emptyprojecttofirebase

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListartoDoScreen()
        }
    }

    @Preview
    @Composable
    private fun ListartoDoScreen() {
        var titulo by remember { mutableStateOf("") }
        var descricao by remember { mutableStateOf("") }
        var projetos by remember { mutableStateOf(listOf<ProjetoToDo>()) }
        var context = LocalContext.current
        var db remember { abrirBanco(context) }
        val dao = db.getToDoDao

        //Assincronidade (responsável pela)
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier.padding(
                top = 90.dp,
                start = 20.dp,
                end = 20.dp
            )
        ) {
            Text(
                text = "Criar novo projeto",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                textStyle = TextStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                textStyle = TextStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                coroutineScope.launch {
                 val  novoProjeto = projetos(
                     titulo = titulo,
                     descricao = descricao
                 )
                    dao.gravar(novoProjeto)
                    projetos = listOf(dao)
                }

            }) {
                Text(text = "Salvar", fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Projetos",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp
            )
        }
    }
}

// Entidade do banco de dados
@Entity(tableName = "tabToDo")
data class ProjetoToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val titulo: String,
    val descricao: String,
    val concluido: Boolean = false
)

// DAO
@Dao
interface ToDoDao {

    @Query("SELECT * FROM tabToDo")
    suspend fun listar(): List<ProjetoToDo>

    @Query("SELECT * FROM tabToDo WHERE id = :id")
    suspend fun buscarPorId(id: Int): ProjetoToDo

    @Upsert
    suspend fun gravar(projeto: ProjetoToDo)

    @Delete
    suspend fun excluir(projeto: ProjetoToDo)
}

// Banco de dados
@Database(entities = [ProjetoToDo::class], version = 1)
abstract class ToDoDb : RoomDatabase() {
    abstract fun getTodoDao(): ToDoDao
}

// Função para abrir banco de dados
fun abrirBanco(context: Context): ToDoDb {
    return Room.databaseBuilder(
        context.applicationContext,
        ToDoDb::class.java,
        "arquivo.db"
    ).build()
}
