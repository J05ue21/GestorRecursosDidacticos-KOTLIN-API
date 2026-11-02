package udb.dsm.recursodidacticos.view

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import udb.dsm.recursodidacticos.databinding.ActivityRegisterBinding
import udb.dsm.recursodidacticos.model.Usuario
import udb.dsm.recursodidacticos.network.ApiService
import udb.dsm.recursodidacticos.util.validarPassword

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // Instancia Retrofit para guardar el usuario en MockAPI
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://6a13dbc76c7db8aac0537076.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        //un spinner para eligir el rol de usuario [Estudiante / Docente]
        val roles = arrayOf("Estudiante", "Docente")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRol.adapter = adapter

        binding.btnRegister.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // capturando el rol seleccionado
            val rol = binding.spinnerRol.selectedItem.toString()

            // validar campos vacíos
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // verificar que las contraseñas coincidan
            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // validar requisitos estrictos con tu función de extensión ValidationUtils
            if (!password.validarPassword()) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener mín. 12 caracteres, mayúscula, minúscula, número y carác. especial",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            //  si los datos son validos y la contraseña es correcto, procedemos al registro
            registrarEnFirebase(email, password, nombre, rol)
        }
    }

    private fun registrarEnFirebase(email: String, password: String, nombre: String, rol: String) {

        //realizar ajustes para poder elegir entre rol Estudiate / Docente ******************
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // si el registro fue exitoso en Firebase, guardamos el rol y datos en MockAPI
                    val nuevoUsuario = Usuario(id = "", nombre = nombre, email = email, password = password, rol = rol)
                    guardarEnMockApi(nuevoUsuario)
                }
                else {
                    Toast.makeText(this, "Error en Firebase: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun guardarEnMockApi(usuario: Usuario) {
        apiService.crearUsuario(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()

                    finish() // cierra la actividad y regresa al Login
                } else {
                    Toast.makeText(this@RegisterActivity, "Error al guardar tu usuario en MockAPI", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}