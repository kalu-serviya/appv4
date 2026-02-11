package servi.work.core.utils

import java.security.SecureRandom

/**
 * Utilidades de validación y generación de seguridad para el búnker ServiWork.
 */
object ValidationUtils {
    private val charPool = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluyendo O, 0, I, 1 por legibilidad
    private val random = SecureRandom()

    /**
     * Genera un código OTP alfanumérico formateado (Ej: ABCD-1234)
     */
    fun generateOtpCode(): String {
        val part1 = (1..4).map { charPool[random.nextInt(charPool.length)] }.joinToString("")
        val part2 = (1..4).map { charPool[random.nextInt(charPool.length)] }.joinToString("")
        return "$part1-$part2"
    }
}
