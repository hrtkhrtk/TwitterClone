package hrtkhrtk.twitterclone

import java.io.Serializable

class User(val bytes: ByteArray, val nickname: String, val idForSearch: String, val selfIntroduction: String, val userId: String) : Serializable {
    val iconImage: ByteArray

    init {
        iconImage = bytes.clone()
    }
}