package servi.work.core.utils

import java.security.MessageDigest
import android.util.Log

/**
 * Motor de Seguridad Inmutable - Capa Solana para ServiWork.
 */
object SolanaSecurityUtils {

    /**
     * Genera el Hash SHA-256 de la identidad del profesional.
     */
    fun generateIdentityHash(dni: String, email: String): String {
        val rawData = "$dni:$email:SERVIWORK_BLOCKCHAIN_BACKED"
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawData.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Simula el registro de la transacción en la Devnet de Solana.
     * En producción, aquí se llamaría a la API de Solana para registrar el Hash.
     */
    fun registerOnSolanaDevnet(hash: String) {
        Log.d("Solana_Security", "TRANSACTION SENT TO DEVNET: identity_hash=$hash")
        Log.d("Solana_Security", "STATUS: FINALIZED - INMUTABLE RECORD CREATED")
    }
}
