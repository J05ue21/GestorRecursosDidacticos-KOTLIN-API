package udb.dsm.recursodidacticos.util

fun String.validarPassword(): Boolean {
    //Sr Android Studio, los Requisitos son: Mínimo 12 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial
    val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{12,}$".toRegex()
    return this.matches(passwordRegex)
}