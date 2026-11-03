package udb.dsm.recursodidacticos.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import udb.dsm.recursodidacticos.R
import udb.dsm.recursodidacticos.databinding.ActivityResourceListBinding
import udb.dsm.recursodidacticos.model.Recurso
import udb.dsm.recursodidacticos.network.ApiService

class ResourceListActivity : AppCompatActivity(), ResourceAdapter.OnItemClickListener {

    private lateinit var binding: ActivityResourceListBinding
    private lateinit var resourceAdapter: ResourceAdapter
    private var listaCompletaRecursos: List<Recurso> = listOf()
    private var rolUsuario: String = "Estudiante" // por defecto

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://6a13dbc76c7db8aac0537076.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // oecuperar el rol enviado desde el Login
        rolUsuario = intent.getStringExtra("ROL_USUARIO") ?: "Estudiante"

        configurarPermisosPorRol()
        configurarRecyclerView()
        configurarBuscador()
    }

    override fun onResume() {
        super.onResume()
        // cargar o recargar los datos cada vez que la actividad vuelve a mostrarse
        obtenerRecursosDesdeApi()
    }

    private fun configurarPermisosPorRol() {
        if (rolUsuario.equals("Estudiante", ignoreCase = true)) {

            // el estudiante solo tiene acceso a ver, buscar, calificar y marcas sus favoritos
            binding.fabAgregarRecurso.visibility = View.GONE
        }
        else {
            // el docente tiene acceso al botón de crear recurso
            binding.fabAgregarRecurso.visibility = View.VISIBLE
            binding.fabAgregarRecurso.setOnClickListener {
                mostrarDialogoFormularioRecurso(null) // null indica que es un nuevo registro
            }
        }
    }

    private fun configurarRecyclerView() {
        resourceAdapter = ResourceAdapter(listOf(), this)
        binding.recyclerViewRecursos.apply {
            layoutManager = LinearLayoutManager(this@ResourceListActivity)
            adapter = resourceAdapter
        }
    }

    private fun obtenerRecursosDesdeApi() {
        apiService.obtenerRecursos().enqueue(object : Callback<List<Recurso>> {
            override fun onResponse(call: Call<List<Recurso>>, response: Response<List<Recurso>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaCompletaRecursos = response.body()!!
                    resourceAdapter.actualizarLista(listaCompletaRecursos)
                } else {
                    Toast.makeText(this@ResourceListActivity, "Error al cargar recursos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Recurso>>, t: Throwable) {
                Toast.makeText(this@ResourceListActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarBuscador() {
        binding.searchViewRecursos.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarRecursos(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarRecursos(newText)
                return true
            }
        })
    }

    private fun filtrarRecursos(query: String?) {
        val textoBusqueda = query?.lowercase()?.trim() ?: ""
        val listaFiltrada = if (textoBusqueda.isEmpty()) {
            listaCompletaRecursos
        } else {
            listaCompletaRecursos.filter {
                it.titulo.lowercase().contains(textoBusqueda) ||
                        it.tipo.lowercase().contains(textoBusqueda)
            }
        }
        resourceAdapter.actualizarLista(listaFiltrada)
    }

    override fun onItemClick(recurso: Recurso) {
        if (rolUsuario.equals("Docente", ignoreCase = true)) {
            // si es docente, se muestra Editar o Eliminar
            val opciones = arrayOf("Editar Recurso", "Eliminar Recurso")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Gestión de Recurso: ${recurso.titulo}")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> mostrarDialogoFormularioRecurso(recurso) // para ditar
                        1 -> confirmarEliminacion(recurso) // pa eliminar
                    }
                }
                .show()
        } else {
            // Si entra como estudiante, solamente puede ver el detalle, calificar o agregar a favoritos
            Toast.makeText(this, "Estudiante: Ver detalle o calificar ${recurso.titulo}", Toast.LENGTH_SHORT).show()
        }
    }

    fun mostrarDialogoFormularioRecurso(recursoAEditar: Recurso?) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val inflater = layoutInflater
        val vista = inflater.inflate(R.layout.dialog_recurso_form, null) // O creas los campos por código si prefieres

        // Referencias a los campos del diálogo
        val etTitulo = vista.findViewById<android.widget.EditText>(R.id.etDialogTitulo)
        val etTipo = vista.findViewById<android.widget.EditText>(R.id.etDialogTipo)
        val etDescripcion = vista.findViewById<android.widget.EditText>(R.id.etDialogDescripcion)
        val etEnlace = vista.findViewById<EditText>(R.id.etDialogEnlace)
        val etImagen = vista.findViewById<android.widget.EditText>(R.id.etDialogImagen)

        // Si estamos editando, rellenamos los campos con los datos actuales
        if (recursoAEditar != null) {
            etTitulo.setText(recursoAEditar.titulo)
            etTipo.setText(recursoAEditar.tipo)
            etDescripcion.setText(recursoAEditar.descripcion)
            etEnlace.setText(recursoAEditar.enlace)
            etImagen.setText(recursoAEditar.imagen)
        }

        builder.setView(vista)
            .setTitle(if (recursoAEditar == null) "Crear Nuevo Recurso" else "Editar Recurso")
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                val tipo = etTipo.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()
                val enlace = etEnlace.text.toString().trim()
                val imagen = etImagen.text.toString().trim()

                if (titulo.isEmpty() || tipo.isEmpty()) {
                    Toast.makeText(this, "Es necesario el Título y el tipo de recurso", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (recursoAEditar == null) {
                    // crear
                    val nuevoRecurso = Recurso(
                        id = "",
                        titulo = titulo,
                        tipo = tipo,
                        descripcion = descripcion,
                        enlace = enlace,
                        imagen = imagen,
                        rating = 0.0
                    )
                    ejecutarCrearRecurso(nuevoRecurso)
                } else {
                    // actualizar
                    val recursoActualizado = Recurso(
                        id = recursoAEditar.id,
                        titulo = titulo,
                        tipo = tipo,
                        descripcion = descripcion,
                        enlace = enlace,
                        imagen = imagen,
                        rating = recursoAEditar.rating
                    )
                    ejecutarActualizarRecurso(recursoAEditar.id, recursoActualizado)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarCrearRecurso(recurso: Recurso) {
        apiService.crearRecurso(recurso).enqueue(object : retrofit2.Callback<Recurso> {
            override fun onResponse(call: retrofit2.Call<Recurso>, response: retrofit2.Response<Recurso>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ResourceListActivity, "Recurso fue creado exitosamente!!", Toast.LENGTH_SHORT).show()
                    obtenerRecursosDesdeApi() // Recargar lista
                } else {
                    Toast.makeText(this@ResourceListActivity, "Hubi un error al crear recurso", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<Recurso>, t: Throwable) {
                Toast.makeText(this@ResourceListActivity, "Error de conexion: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun ejecutarActualizarRecurso(id: String, recurso: Recurso) {
        apiService.actualizarRecurso(id, recurso).enqueue(object : retrofit2.Callback<Recurso> {
            override fun onResponse(call: retrofit2.Call<Recurso>, response: retrofit2.Response<Recurso>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ResourceListActivity, "Se actualizo el recurso", Toast.LENGTH_SHORT).show()
                    obtenerRecursosDesdeApi()
                } else {
                    Toast.makeText(this@ResourceListActivity, "Hubo un error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<Recurso>, t: Throwable) {
                Toast.makeText(this@ResourceListActivity, "Error de conexion: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmarEliminacion(recurso: Recurso) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Recurso")
            .setMessage("¿Estás seguro de eliminar '${recurso.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                ejecutarEliminarRecurso(recurso.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarEliminarRecurso(id: String) {
        apiService.eliminarRecurso(id).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ResourceListActivity, "El recurso ha sido eliminado", Toast.LENGTH_SHORT).show()
                    obtenerRecursosDesdeApi()
                } else {
                    Toast.makeText(this@ResourceListActivity, "Hubo un error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Toast.makeText(this@ResourceListActivity, "Error en la conexion: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}