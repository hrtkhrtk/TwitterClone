package hrtkhrtk.twitterclone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.ArrayList

class PostForShowingsListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mPostForShowingArrayList = ArrayList<PostForShowing>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mPostForShowingArrayList.size
    }

    override fun getItem(position: Int): Any {
        //return mPostForShowingArrayList[position]

        var targetPostForShowing: PostForShowing? = null
        for (postForShowing in mPostForShowingArrayList) {
            if (position == postForShowing.positionInArrayList) {
                targetPostForShowing = postForShowing
            }
        }

        return targetPostForShowing ?: 0 // こんなんでいい？ // 「?: 0」 の 「0」に理由はない　// TODO:
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_posts, parent, false)
        }



        var targetPostForShowing: PostForShowing? = null
        for (postForShowing in mPostForShowingArrayList) {
            if (position == postForShowing.positionInArrayList) {
                targetPostForShowing = postForShowing
            }
        }



        if (targetPostForShowing != null) {

            val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
            //nicknameText.text = mPostForShowingArrayList[position].nickname
            nicknameText.text = targetPostForShowing.nickname

            val postCreatedAtText = convertView.findViewById<View>(R.id.postCreatedAtTextView) as TextView
            //postCreatedAtText.text = mPostForShowingArrayList[position].createdAt
            //postCreatedAtText.text = targetPostForShowing.createdAt
            //postCreatedAtText.text = targetPostForShowing.createdAt.toString()
            postCreatedAtText.text = getDateTime(targetPostForShowing.createdAt)

            val postText = convertView.findViewById<View>(R.id.postTextView) as TextView
            //postText.text = mPostForShowingArrayList[position].text
            postText.text = targetPostForShowing.text

            val favoritersNumberText = convertView.findViewById<View>(R.id.favoritersNumberTextView) as TextView
            //val favoritersNum = mPostForShowingArrayList[position].favoritersList.size
            val favoritersNum = targetPostForShowing.favoritersList.size
            favoritersNumberText.text = favoritersNum.toString()

            //val bytes = mPostForShowingArrayList[position].iconImage
            val bytes = targetPostForShowing.iconImage
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
                iconImageView.setImageBitmap(image)
            }


            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                // ログインしていない場合は何もしない
                //Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
            } else {
                val favoriteButton = convertView.findViewById<Button>(R.id.favoriteButton) // as Buttonを付けるとエラーになる

                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val userRef = dataBaseReference.child("users").child(user.uid)
                userRef.addValueEventListener(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userData = snapshot.value as MutableMap<String, String> // userDataは必ず存在する
                                if (userData["favorites_list"] == null) { // リストに含まれない（リストがない）
                                    favoriteButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                    favoriteButton.text = "fav"
                                } else {
                                    val existingFavoriteList = userData["favorites_list"] as ArrayList<MutableMap<String, String>>

                                    val data = mutableMapOf<String, String>()
                                    //data.put("user_id", mPostForShowingArrayList[position].userId)
                                    data.put("user_id", targetPostForShowing.userId)
                                    //data.put("post_id", mPostForShowingArrayList[position].postId)
                                    data.put("post_id", targetPostForShowing.postId)

                                    if (!(existingFavoriteList.contains(data))) { // 含まれなければ
                                        favoriteButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                        favoriteButton.text = "fav"
                                    } else { // 含まれていれば
                                        favoriteButton.setBackgroundColor(Color.parseColor("#ff0000")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                        favoriteButton.text = "unfav"
                                    }
                                }
                            }

                            override fun onCancelled(firebaseError: DatabaseError) {}
                        }
                )

                favoriteButton.visibility = View.VISIBLE
                //favoriteButton.setOnClickListener(this) // 書き方についてLesson4項目3.1参照
                favoriteButton.setOnClickListener { v ->
                    val user = FirebaseAuth.getInstance().currentUser

                    if (user == null) {
                        // ログインしていない場合は何もしない
                        Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
                    } else {
                        // Firebaseに保存する
                        val dataBaseReference = FirebaseDatabase.getInstance().reference
                        val userRef = dataBaseReference.child("users").child(user.uid)

                        userRef.addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val userData = snapshot.value as MutableMap<String, String> // userDataは必ず存在

                                        if (userData["favorites_list"] == null) {
                                            val existingFavoriteList = ArrayList<MutableMap<String, String>>()
                                            val data = mutableMapOf<String, String>()
                                            //data.put("user_id", mPostForShowingArrayList[position].userId)
                                            data.put("user_id", targetPostForShowing.userId)
                                            //data.put("post_id", mPostForShowingArrayList[position].postId)
                                            data.put("post_id", targetPostForShowing.postId)
                                            existingFavoriteList.add(data)
                                            dataBaseReference.child("users").child(user.uid).child("favorites_list").setValue(existingFavoriteList)


                                            dataBaseReference.child("posts").child(targetPostForShowing.userId).child(targetPostForShowing.postId).addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(snapshotInPostListener: DataSnapshot) {
                                                            val postData = snapshotInPostListener.value as MutableMap<String, String> // postDataは必ず存在
                                                            val existingFavoritersListInPost = postData["favoriters_list"] as ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
                                                            existingFavoritersListInPost.add(user.uid)
                                                            dataBaseReference.child("posts").child(targetPostForShowing.userId).child(targetPostForShowing.postId).child("favoriters_list").setValue(existingFavoritersListInPost)
                                                        }

                                                        override fun onCancelled(firebaseErrorInPostListener: DatabaseError) {}
                                                    }
                                            )
                                        } else {
                                            val existingFavoriteList = userData["favorites_list"] as ArrayList<MutableMap<String, String>>
                                            val data = mutableMapOf<String, String>()
                                            //data.put("user_id", mPostForShowingArrayList[position].userId)
                                            data.put("user_id", targetPostForShowing.userId)
                                            //data.put("post_id", mPostForShowingArrayList[position].postId)
                                            data.put("post_id", targetPostForShowing.postId)

                                            if (!(existingFavoriteList.contains(data))) { // 含まれなければ追加
                                                existingFavoriteList.add(data)
                                                dataBaseReference.child("users").child(user.uid).child("favorites_list").setValue(existingFavoriteList)

                                                dataBaseReference.child("posts").child(targetPostForShowing.userId).child(targetPostForShowing.postId).addListenerForSingleValueEvent(
                                                        object : ValueEventListener {
                                                            override fun onDataChange(snapshotInPostListener: DataSnapshot) {
                                                                val postData = snapshotInPostListener.value as MutableMap<String, String> // postDataは必ず存在
                                                                val existingFavoritersListInPost = postData["favoriters_list"] as ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
                                                                existingFavoritersListInPost.add(user.uid)
                                                                dataBaseReference.child("posts").child(targetPostForShowing.userId).child(targetPostForShowing.postId).child("favoriters_list").setValue(existingFavoritersListInPost)
                                                            }

                                                            override fun onCancelled(firebaseErrorInPostListener: DatabaseError) {}
                                                        }
                                                )
                                            } else { // 含まれていれば削除
                                                existingFavoriteList.remove(data) // 参考：Lesson3項目11.3
                                                dataBaseReference.child("users").child(user.uid).child("favorites_list").setValue(existingFavoriteList)

                                                dataBaseReference.child("posts").child(targetPostForShowing.userId).child(targetPostForShowing.postId).addListenerForSingleValueEvent(
                                                        object : ValueEventListener {
                                                            override fun onDataChange(snapshotInPostListener: DataSnapshot) {
                                                                val postData = snapshotInPostListener.value as MutableMap<String, String> // postDataは必ず存在
                                                                val existingFavoritersListInPost = postData["favoriters_list"] as ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
                                                                existingFavoritersListInPost.remove(user.uid)
                                                                dataBaseReference.child("posts").child(targetPostForShowing.userId).child(targetPostForShowing.postId).child("favoriters_list").setValue(existingFavoritersListInPost)
                                                            }

                                                            override fun onCancelled(firebaseErrorInPostListener: DatabaseError) {}
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    override fun onCancelled(firebaseError: DatabaseError) {}
                                }
                        )
                    }
                }
            }
        }

        //return convertView
        return convertView!!
    }

    fun setPostForShowingArrayList(postForShowingArrayList: ArrayList<PostForShowing>) {
        mPostForShowingArrayList = postForShowingArrayList
    }
}