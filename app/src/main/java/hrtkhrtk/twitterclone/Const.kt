package hrtkhrtk.twitterclone

import java.text.SimpleDateFormat
import java.util.*

const val NameKEY = "name"          // Preferenceに表示名を保存する時のキー

fun getDateTime(data_Long: Long, pattern: String = "yyyy/MM/dd HH:mm:ss"): String? {
    // 参考：Lesson3「引数にはデフォルト値を指定することができます」
    // 参考：https://qiita.com/emboss369/items/5a3ddea301cbf79d971a
    // 参考：https://stackoverflow.com/questions/47250263/kotlin-convert-timestamp-to-datetime
    try {
        val sdf = SimpleDateFormat(pattern)
        val netDate = Date(data_Long)
        return sdf.format(netDate)
    } catch (e: Exception) {
        return e.toString()
    }
}
