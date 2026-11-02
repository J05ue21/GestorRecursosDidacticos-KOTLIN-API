package udb.dsm.recursodidacticos.util

fun String.validarPassword(): Boolean {
    //Sr Android Studio, los Requisitos son: Mínimo 12 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial
    val passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[-!@#\$%_.&*])[A-Za-z0-9!@#\$%_.&*-]{12,}\$".toRegex()
    /*  Detalle de la Regex implementada:
        1. Tener al menos 12 caracteres de longitud (pueden ser más).
        2. Contener al menos una letra mayúscula (A-Z).
        3. Contener al menos una letra minúscula (a-z).
        4. Contener al menos un número (0-9).
        5. Contener al menos un carácter especial de la siguiente lista: -, !, @, #, $, %, _, ., &, *.
        6. No permitir ningún otro carácter fuera de los mencionados anteriormente.
    * */
    return this.matches(passwordRegex)
}