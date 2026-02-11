package servi.work.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService {

    private val smtpHost = "smtp.gmail.com"
    private val smtpPort = "465" 
    private val user = "2025serviya@gmail.com"
    private val appPassword = "dvxoazvkjaslmstm" 

    suspend fun enviarEmailBienvenida(emailDestino: String, nombre: String, otp: String): Boolean {
        return enviarEmail(emailDestino, "🛡️ [ServiWork] Protocolo de Seguridad - Código $otp", """
            <h2>¡Hola, $nombre!</h2>
            <p>Tu código de verificación de ServiWork es:</p>
            <div style="background-color: #f4f4f4; padding: 20px; font-size: 24px; font-weight: bold; letter-spacing: 5px; text-align: center;">$otp</div>
            <p>Si no solicitaste esto, ignorá este mensaje.</p>
        """.trimIndent())
    }

    suspend fun enviarEmailRecuperacion(emailDestino: String, otp: String): Boolean {
        return enviarEmail(emailDestino, "🛡️ [ServiWork] Recuperación de Acceso - Código $otp", """
            <h2>Protocolo de Recuperación</h2>
            <p>Se ha solicitado un cambio de contraseña para tu cuenta de ServiWork.</p>
            <p>Tu código de seguridad es:</p>
            <div style="background-color: #f4f4f4; padding: 20px; font-size: 24px; font-weight: bold; letter-spacing: 5px; text-align: center;">$otp</div>
            <p>Ingresá este código en la App para establecer tu nueva clave.</p>
            <p>Si no fuiste vos, te recomendamos asegurar tu cuenta.</p>
        """.trimIndent())
    }

    suspend fun enviarNotificacionCambioPassword(emailDestino: String): Boolean {
        return enviarEmail(emailDestino, "🛡️ [ServiWork] Contraseña Actualizada", """
            <h2>¡Protocolo de Seguridad Completado!</h2>
            <p>Te informamos que la contraseña de tu cuenta en ServiWork ha sido cambiada con éxito.</p>
            <p>Si no realizaste este cambio, contactá a soporte de inmediato para bloquear el búnker.</p>
        """.trimIndent())
    }

    private suspend fun enviarEmail(emailDestino: String, asunto: String, htmlBody: String): Boolean {
        return withContext(Dispatchers.IO) {
            val props = Properties().apply {
                put("mail.smtp.host", smtpHost)
                put("mail.smtp.port", smtpPort)
                put("mail.smtp.auth", "true")
                put("mail.smtp.ssl.enable", "true") 
                put("mail.smtp.socketFactory.port", smtpPort)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory") 
                put("mail.smtp.ssl.trust", "*")
                put("mail.smtp.from", user)
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.connectiontimeout", "15000")
                put("mail.smtp.timeout", "15000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(user, appPassword.trim())
                }
            })

            val containerBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                        <div style="background-color: #1a1a1a; color: #fff; padding: 20px; text-align: center;">
                            <h1>SERVIWORK</h1>
                        </div>
                        <div style="padding: 30px; text-align: center;">
                            $htmlBody
                        </div>
                        <div style="background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 12px; color: #777;">
                            © 2026 ServiWork Argentina.
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(user, "ServiWork Soporte"))
                    addRecipient(Message.RecipientType.TO, InternetAddress(emailDestino))
                    subject = asunto
                    setSentDate(Date())
                    setContent(containerBody, "text/html; charset=utf-8")
                }
                Transport.send(message)
                true
            } catch (e: Exception) {
                Log.e("EmailService", "Error SMTP: ${e.message}")
                false
            }
        }
    }

    fun generarCodigoOTP(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..8).map { allowedChars.random() }.joinToString("")
    }
}
