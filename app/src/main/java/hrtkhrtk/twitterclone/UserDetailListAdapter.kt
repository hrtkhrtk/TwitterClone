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

class UserDetailListAdapter(context: Context, private val mUserDetail: UserDetail, private val mPostArrayList: ArrayList<Post>) : BaseAdapter() {
    companion object {
        private val TYPE_USER = 0
        private val TYPE_POST = 1
    }

    private var mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int { // TODO: （こんなのでいいか確認）
        /*
        var total = 1
        //var flag = 0 // これでいい？

        FirebaseDatabase.getInstance().reference.child("posts").child(mUserId).addListenerForSingleValueEvent( // SingleValueEventでいい？
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as HashMap<String, String>? ?: HashMap<String, String>()
                    Log.d("test191127n10", data.size.toString())
                    //return (1 + data.size)
                    total = (1 + data.size)
                    //flag = 1
                }

                override fun onCancelled(firebaseError: DatabaseError) {
                    Log.d("test191127n11", 1.toString())
                    //return 1 // エラーが起こった時は、postは表示しない感じでいいと思う
                    total = 1 // エラーが起こった時は、postは表示しない感じでいいと思う
                    //flag = 1
                }
            }
        )

        Log.d("test191127n12", total.toString())
        return total
        */

        return (1 + mPostArrayList.size)
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
                //Log.d("test191127n03", user.uid)
                //Log.d("test191127n04", userId)
                //Log.d("test191127n05", (user.uid != userId).toString())

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

            val nicknameText = convertView.findViewById<View>(R.id.nicknameTextView) as TextView
            nicknameText.text = mPostArrayList[position-1].nickname

            val postCreatedAtText = convertView.findViewById<View>(R.id.postCreatedAtTextView) as TextView
            postCreatedAtText.text = mPostArrayList[position-1].createdAt

            val postText = convertView.findViewById<View>(R.id.postTextView) as TextView
            postText.text = mPostArrayList[position-1].text

            val favoritersNumberText = convertView.findViewById<View>(R.id.favoritersNumberTextView) as TextView
            val favoritersNum = mPostArrayList[position-1].favoritersList.size
            favoritersNumberText.text = favoritersNum.toString()

            val bytes = mPostArrayList[position-1].iconImage
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
                iconImageView.setImageBitmap(image)
            }
        }

        return convertView!!
    }
}