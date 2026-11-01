package udb.dsm.recursodidacticos.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import udb.dsm.recursodidacticos.model.Recurso
import udb.dsm.recursodidacticos.model.Usuario

interface ApiService {

    //  definiendo los metodos API para obtener, crear, actualizar y eliminar RECURSOS

    @GET("recursos")
    fun obtenerRecursos(): Call<List<Recurso>>

    @GET("recursos/{id}")
    fun obtenerRecursoPorId(@Path("id") id: String): Call<Recurso>

    @POST("recursos")
    fun crearRecurso(@Body recurso: Recurso): Call<Recurso>

    @PUT("recursos/{id}")
    fun actualizarRecurso(@Path("id") id: String, @Body recurso: Recurso): Call<Recurso>

    @DELETE("recursos/{id}")
    fun eliminarRecurso(@Path("id") id: String): Call<Void>

    //  para validar roles y credenciales de USUARIOS desde MockAPI

    @GET("usuarios")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @POST("usuarios")
    fun  crearUsuario(@Body usuario: Usuario): Call<Usuario>
}