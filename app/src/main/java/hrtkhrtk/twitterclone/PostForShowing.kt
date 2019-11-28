package hrtkhrtk.twitterclone

import java.io.Serializable
import java.util.ArrayList

class PostForShowing(
    val bytes: ByteArray,
    val nickname: String,
    val text: String,
    val createdAt: String,
    val favoritersList: ArrayList<String>,
    val userId: String,
    val postId: String,
    val positionInArrayList: Int) : Serializable {

    val iconImage: ByteArray

    init {
        iconImage = bytes.clone()
    }
}