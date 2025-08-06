import java.io.FileInputStream
import java.util.Properties

object IpConfig {
    private val props = Properties()

    init {
        try {
            val path = "ip_config.properties"  // 프로젝트 루트 기준
            val input = FileInputStream(path)
            props.load(input)
        } catch (e: Exception) {
            throw RuntimeException("ip_config.properties 파일을 찾을 수 없거나 읽을 수 없습니다: ${e.message}")
        }
    }

    val BASE_IP: String = props.getProperty("BASE_IP")
    val PORT = "8080"

    val BASE_URL: String
        get() = "http://$BASE_IP:$PORT"
}