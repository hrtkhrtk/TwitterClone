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

//class UserDetailListAdapter(context: Context, private val mUserDetail: UserDetail, private val mPostArrayList: ArrayList<Post>) : BaseAdapter() {
class UserDetailListAdapter(context: Context, private val mUserDetail: UserDetail, private val mPostForShowingArrayList: ArrayList<PostForShowing>) : BaseAdapter() {
    companion object {
        private val TYPE_USER = 0
        private val TYPE_POST = 1
    }

    private var mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        //return (1 + mPostArrayList.size)
        return (1 + mPostForShowingArrayList.size)
    }



    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_USER
        } else {
            TYPE_POST
        }
    }



    override fun getViewTypeCount(): Int {
        return 2
    }



    override fun getItem(position: Int): Any {
        return 0 // 特に返すものはない // これでいい？
        // 参考：http://ytdk.jp/android/app/listview/
        // 「AdapterのgetItemで、「このIndexにはこういったデータ(アイテム)が入っているよ」というのを返却します。
        // getViewの際にデータを取り出す場合などでよく使われます。」
    }



    override fun getItemId(position: Int): Long {
        return 0 // 特に返すものはない // これでいい？
        // 参考：Lesson8のQuestionDetailListAdapter.kt
        // 参考：http://ytdk.jp/android/app/listview/
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_USER) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_user_detail, parent, false)!!
            }

            val nicknameText = convertView.findViewById<View>(R.id.nicknameTextView) as TextView
            nicknameText.text = mUserDetail.nickname

            val idForSearchText = convertView.findViewById<View>(R.id.idForSearchTextView) as TextView
            idForSearchText.text = mUserDetail.idForSearch

            val createdAtText = convertView.findViewById<View>(R.id.createdAtTextView) as TextView
            createdAtText.text = mUserDetail.createdAt

            val followingsNumberText = convertView.findViewById<View>(R.id.followingsNumberTextView) as TextView
            val followingsNum = mUserDetail.followingsList.size
            followingsNumberText.text = followingsNum.toString()

            val followersNumberText = convertView.findViewById<View>(R.id.followersNumberTextView) as TextView
            val followersNum = mUserDetail.followersList.size
            followersNumberText.text = followersNum.toString()

            val bytesForBackgroundImage = mUserDetail.backgroundImage
            if (bytesForBackgroundImage.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytesForBackgroundImage, 0, bytesForBackgroundImage.size).copy(Bitmap.Config.ARGB_8888, true)
                val backgroundImageView = convertView.findViewById<View>(R.id.backgroundImageView) as ImageView
                backgroundImageView.setImageBitmap(image)
            }

            val bytesForIconImage = mUserDetail.iconImage
            if (bytesForIconImage.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytesForIconImage, 0, bytesForIconImage.size).copy(Bitmap.Config.ARGB_8888, true)
                val iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
                iconImageView.setImageBitmap(image)
            }

            val userId = mUserDetail.userId
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                //Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
            } else if (user.uid != userId) { // 自分じゃなければ
                val followButton = convertView.findViewById<Button>(R.id.followButton) // as Buttonを付けるとエラーになる

                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val currentUserRef = dataBaseReference.child("users").child(user.uid)
                currentUserRef.addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData = snapshot.value as MutableMap<String, String>

                            if (userData!!["followings_list"] == null) {
                                followButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                followButton.text = "follow"
                            }
                            else {
                                val existingFollowingsList = userData!!["followings_list"] as ArrayList<String>

                                if (!(existingFollowingsList.contains(userId))) { // 含まれなければ
                                    followButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                    followButton.text = "follow"
                                }
                                else { // 含まれていれば
                                    followButton.setBackgroundColor(Color.parseColor("#ff0000")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                    followButton.text = "unfollow"
                                }
                            }
                        }
                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )

                followButton.visibility = View.VISIBLE

                //followButton.setOnClickListener(this) // 書き方についてLesson4項目3.1参照
                followButton.setOnClickListener { v ->
                    // ログイン済みのユーザーを取得する
                    val userInFollowButton = FirebaseAuth.getInstance().currentUser

                    if (userInFollowButton == null) {
                        // ログインしていない場合は何もしない
                        Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
                    } else {
                        // Firebaseに保存する
                        val dataBaseReferenceInFollowButton = FirebaseDatabase.getInstance().reference
                        val currentUserRefInFollowButton = dataBaseReferenceInFollowButton.child("users").child(user.uid)
                        val followeeUserRefInFollowButton = dataBaseReferenceInFollowButton.child("users").child(userId)

                        var userDataInFollowButton : MutableMap<String, String>? = null

                        currentUserRefInFollowButton.addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    userDataInFollowButton = snapshot.value as MutableMap<String, String>

                                    if (userDataInFollowButton!!["followings_list"] == null) { // リストに入っていない（リストがない）
                                        val existingFollowingsListInCurrentUser = ArrayList<String>()
                                        existingFollowingsListInCurrentUser.add(userId)
                                        dataBaseReference.child("users").child(user.uid).child("followings_list").setValue(existingFollowingsListInCurrentUser)

                                        followeeUserRefInFollowButton.addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val userData = snapshot.value as MutableMap<String, String>
                                                    //val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>
                                                    val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>? ?: ArrayList<String>()
                                                    existingFollowersListInFolloweeUser.add(user.uid)
                                                    dataBaseReference.child("users").child(userId).child("followers_list").setValue(existingFollowersListInFolloweeUser)
                                                }
                                                override fun onCancelled(firebaseError: DatabaseError) {}
                                            }
                                        )
                                    }
                                    else {
                                        val existingFollowingsListInCurrentUser = userDataInFollowButton!!["followings_list"] as ArrayList<String>

                                        if (!(existingFollowingsListInCurrentUser.contains(userId))) { // 含まれなければ追加
                                            existingFollowingsListInCurrentUser.add(userId)
                                            dataBaseReference.child("users").child(user.uid).child("followings_list").setValue(existingFollowingsListInCurrentUser)

                                            followeeUserRefInFollowButton.addListenerForSingleValueEvent(
                                                object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val userData = snapshot.value as MutableMap<String, String>
                                                        //val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>
                                                        val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>? ?: ArrayList<String>()
                                                        existingFollowersListInFolloweeUser.add(user.uid)
                                                        dataBaseReference.child("users").child(userId).child("followers_list").setValue(existingFollowersListInFolloweeUser)
                                                    }
                                                    override fun onCancelled(firebaseError: DatabaseError) {}
                                                }
                                            )
                                        }
                                        else { // 含まれていれば削除
                                            existingFollowingsListInCurrentUser.remove(userId) // 参考：Lesson3項目11.3
                                            dataBaseReference.child("users").child(user.uid).child("followings_list").setValue(existingFollowingsListInCurrentUser)

                                            followeeUserRefInFollowButton.addListenerForSingleValueEvent(
                                                object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val userData = snapshot.value as MutableMap<String, String>
                                                        val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>
                                                        existingFollowersListInFolloweeUser.remove(user.uid)
                                                        dataBaseReference.child("users").child(userId).child("followers_list").setValue(existingFollowersListInFolloweeUser)
                                                    }
                                                    override fun onCancelled(firebaseError: DatabaseError) {}
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
            } else if (user.uid == userId) { // 自分の時
                val followButton = convertView.findViewById<Button>(R.id.followButton) // as Buttonを付けるとエラーになる
                followButton.visibility = View.GONE
            }




        } else if (getItemViewType(position) == TYPE_POST) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_posts, parent, false)!!
            }


            var targetPostForShowing: PostForShowing? = null
            for (postForShowing in mPostForShowingArrayList) {
                //if (position == postForShowing.positionInArrayList) {
                if ((position - 1) == postForShowing.positionInArrayList) {
                    targetPostForShowing = postForShowing
                }
            }



            if (targetPostForShowing != null) {

                val nicknameText = convertView.findViewById<View>(R.id.nicknameTextView) as TextView
                //nicknameText.text = mPostArrayList[position-1].nickname
                nicknameText.text = targetPostForShowing.nickname

                val postCreatedAtText = convertView.findViewById<View>(R.id.postCreatedAtTextView) as TextView
                //postCreatedAtText.text = mPostArrayList[position-1].createdAt
                postCreatedAtText.text = targetPostForShowing.createdAt

                val postText = convertView.findViewById<View>(R.id.postTextView) as TextView
                //postText.text = mPostArrayList[position-1].text
                postText.text = targetPostForShowing.text

                val favoritersNumberText = convertView.findViewById<View>(R.id.favoritersNumberTextView) as TextView
                //val favoritersNum = mPostArrayList[position-1].favoritersList.size
                val favoritersNum = targetPostForShowing.favoritersList.size
                favoritersNumberText.text = favoritersNum.toString()

                //val bytes = mPostArrayList[position-1].iconImage
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
                                    //Log.d("test191127n33", "test191127n33")
                                    val userData = snapshot.value as MutableMap<String, String> // userDataは必ず存在する
                                    if (userData["favorites_list"] == null) { // リストに含まれない（リストがない）
                                        Log.d("test191127n34", "test191127n34")
                                        favoriteButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                        favoriteButton.text = "fav"
                                    } else {
                                        //Log.d("test191127n35", "test191127n35")
                                        val existingFavoriteList = userData["favorites_list"] as ArrayList<MutableMap<String, String>>

                                        val data = mutableMapOf<String, String>()
                                        //data.put("user_id", mPostForShowingArrayList[position-1].userId)
                                        data.put("user_id", targetPostForShowing.userId)
                                        //data.put("post_id", mPostForShowingArrayList[position-1].postId)
                                        data.put("post_id", targetPostForShowing.postId)

                                        if (!(existingFavoriteList.contains(data))) { // 含まれなければ
                                            Log.d("test191127n36", "test191127n36")
                                            Log.d("test191127n100", data["user_id"])
                                            Log.d("test191127n101", data["post_id"])
                                            //val test = mPostForShowingArrayList
                                            favoriteButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                            favoriteButton.text = "fav"
                                        } else { // 含まれていれば
                                            Log.d("test191127n37", "test191127n37")
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
                                                //data.put("user_id", mPostForShowingArrayList[position-1].userId)
                                                data.put("user_id", targetPostForShowing.userId)
                                                //data.put("post_id", mPostForShowingArrayList[position-1].postId)
                                                data.put("post_id", targetPostForShowing.postId)
                                                existingFavoriteList.add(data)
                                                dataBaseReference.child("users").child(user.uid).child("favorites_list").setValue(existingFavoriteList)
                                            } else {
                                                val existingFavoriteList = userData["favorites_list"] as ArrayList<MutableMap<String, String>>
                                                val data = mutableMapOf<String, String>()
                                                //data.put("user_id", mPostForShowingArrayList[position-1].userId)
                                                data.put("user_id", targetPostForShowing.userId)
                                                //data.put("post_id", mPostForShowingArrayList[position-1].postId)
                                                data.put("post_id", targetPostForShowing.postId)

                                                if (!(existingFavoriteList.contains(data))) { // 含まれなければ追加
                                                    existingFavoriteList.add(data)
                                                    dataBaseReference.child("users").child(user.uid).child("favorites_list").setValue(existingFavoriteList)
                                                } else { // 含まれていれば削除
                                                    existingFavoriteList.remove(data) // 参考：Lesson3項目11.3
                                                    dataBaseReference.child("users").child(user.uid).child("favorites_list").setValue(existingFavoriteList)
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
        }

        return convertView!!
    }
}