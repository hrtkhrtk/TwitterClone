package hrtkhrtk.twitterclone

import java.io.Serializable
import java.util.ArrayList

class UserDetail(
    val bytesForBackgroundImage: ByteArray,
    val bytesForIconImage: ByteArray,
    val nickname: String,
    val idForSearch: String,
    val selfIntroduction: String,
    //val createdAt: String,
    val createdAt: Long,
    val followingsList: ArrayList<String>,
    val followersList: ArrayList<String>,
    val userId: String
) : Serializable {
    val backgroundImage: ByteArray
    val iconImage: ByteArray

    init {
        backgroundImage = bytesForBackgroundImage.clone()
        iconImage = bytesForIconImage.clone()
    }
}