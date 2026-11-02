package udb.dsm.recursodidacticos.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

                Toast.makeText(this, "Acción de Docente: Crear Recurso", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Docente: Editar/Eliminar ${recurso.titulo}", Toast.LENGTH_SHORT).show()
        } else {
            // Si entra como estudiante, solamente puede ver el detalle, calificar o agregar a favoritos
            Toast.makeText(this, "Estudiante: Ver detalle o calificar ${recurso.titulo}", Toast.LENGTH_SHORT).show()
        }
    }
}