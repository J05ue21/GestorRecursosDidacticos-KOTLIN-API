package udb.dsm.recursodidacticos.model

import com.google.gson.annotations.SerializedName
data class Recurso(
    @SerializedName("id") val id: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("enlace") val enlace: String = "",
    @SerializedName("imagen") val imagen: String,
    @SerializedName("rating") val rating: Double?
)
