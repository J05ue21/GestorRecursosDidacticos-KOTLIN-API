package udb.dsm.recursodidacticos.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import udb.dsm.recursodidacticos.databinding.ActivityLoginBinding
import udb.dsm.recursodidacticos.model.Usuario
import udb.dsm.recursodidacticos.network.ApiService

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://6a13dbc76c7db8aac0537076.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Botón para iniciar sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            iniciarSesionFirebase(email, password)
        }

        // Texto o botón para ir a la pantalla de registro si no tiene cuenta
        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSesionFirebase(email: String, pass: String) {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso en Firebase, ahora consultamos el rol en MockAPI
                    verificarRolEnMockApi(email)
                } else {
                    Toast.makeText(this, "Error de acceso: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun verificarRolEnMockApi(emailUser: String) {
        apiService.obtenerUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    // se busca al usuario que coincide con el correo logueado
                    val usuarioEncontrado = usuarios?.find { it.email.equals(emailUser, ignoreCase = true) }

                    if (usuarioEncontrado != null) {
                        val rol = usuarioEncontrado.rol
                        Toast.makeText(this@LoginActivity, "Bienvenido (${rol})", Toast.LENGTH_SHORT).show()

                        // TODO: redireccionamiento al RecyclerView principal pasando el rol y:
                        // Si es "Docente" le habilitamos el CRUD o
                        // si es "Estudiante" solo ver, sus favoritos y calificaciones

                    }
                    else {
                        Toast.makeText(this@LoginActivity, "Tu usuario no tiene un rol asignado", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(this@LoginActivity, "Hubo un error al verificar tu usuario en el servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error durante la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}