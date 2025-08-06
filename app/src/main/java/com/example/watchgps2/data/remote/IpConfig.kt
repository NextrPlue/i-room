import android.content.Context
import java.io.FileInputStream
import java.util.Properties

object IpConfig {
    private var baseIp: String = "127.0.0.1"  // 기본값

    fun initialize(context: Context) {
        try {
            val props = Properties()
            val inputStream = context.assets.open("ip_config.properties")
            props.load(inputStream)
            baseIp = props.getProperty("BASE_IP")
        } catch (e: Exception) {
            throw RuntimeException("ip_config.properties 읽기 실패: ${e.message}")
        }
    }

    fun getBaseUrl(): String {
        val port = "8080"
        return "http://$baseIp:$port"
    }
}