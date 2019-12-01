package hrtkhrtk.twitterclone

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_user_detail.*

import java.util.HashMap

class UserDetailActivity : AppCompatActivity() {

    //private lateinit var mPostArrayList: ArrayList<Post>
    private lateinit var mPostForShowingArrayList: ArrayList<PostForShowing>
    private lateinit var mUserDetail: UserDetail
    private lateinit var mAdapter: UserDetailListAdapter
    private lateinit var mPostRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val post_id = dataSnapshot.key!!

            //for (post in mPostArrayList) {
            //    // 同じpost_idのものが存在しているときは何もしない
            //    if (post_id == post.postId) {
            //        return
            //    }
            //}
            for (postForShowing in mPostForShowingArrayList) {
                // 同じpost_idのものが存在しているときは何もしない
                if (post_id == postForShowing.postId) {
                    return
                }
            }

            val text = map["text"] ?: ""
            val map2 = dataSnapshot.value as Map<String, Long>
            val created_at_Long = map2["created_at"]!! // ここは必ず存在
            val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
            val iconImage = mUserDetail.iconImage
            val nickname = mUserDetail.nickname
            val user_id = mUserDetail.userId

            val postForShowing = PostForShowing(iconImage, nickname, text, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size)
            mPostForShowingArrayList.add(postForShowing)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // 渡ってきたオブジェクトを保持する
        val extras = intent.extras
        //mPostArrayList = extras.get("postArrayList") as ArrayList<Post>
        mPostForShowingArrayList = extras.get("postForShowingArrayList") as ArrayList<PostForShowing>
        mUserDetail = extras.get("userDetail") as UserDetail

        title = mUserDetail.nickname

        // ListViewの準備
        //mAdapter = UserDetailListAdapter(this, mUserDetail, mPostArrayList)
        mAdapter = UserDetailListAdapter(this, mUserDetail, mPostForShowingArrayList)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mPostRef = dataBaseReference.child("posts").child(mUserDetail.userId)
        mPostRef.addChildEventListener(mEventListener)
    }
}
