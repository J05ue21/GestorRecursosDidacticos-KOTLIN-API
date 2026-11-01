package udb.dsm.recursodidacticos.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("rol") val rol: String
)
