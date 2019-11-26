package hrtkhrtk.twitterclone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UsersListActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mUserArrayList: ArrayList<User>
    private lateinit var mAdapter: UsersListAdapter


    private val mEventListenerForUsersList = object : ChildEventListener { // 仮置き // TODO:
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            Log.d("test191127n01", "test191127n01")

            val map = dataSnapshot.value as Map<String, String>
            val iconImageString = map["icon_image"] ?: ""
            val nickname = map["nickname"] ?: ""
            val idForSearch = map["id_for_search"] ?: ""
            val selfIntroduction = map["self_introduction"] ?: ""
            val userId = dataSnapshot.key!!

            val bytes =
                if (iconImageString.isNotEmpty()) {
                    Base64.decode(iconImageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val userClassInstance = User(bytes, nickname, idForSearch, selfIntroduction, userId)
            mUserArrayList.add(userClassInstance)
            mAdapter.notifyDataSetChanged()

            //for (user in mUserArrayList) { // テスト // 本来不要
            //    Log.d("test191127n02", user.userId)
            //}
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {} // TODO:

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {} // TODO:

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }



    private val mEventListenerForFollowingsFollowersListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val list = dataSnapshot.value as ArrayList<String>? ?: ArrayList<String>()
            for (user_id in list) {
                mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //val map = dataSnapshot.value as Map<String, String>
                                val map = snapshot.value as Map<String, String>

                                val iconImageString = map["icon_image"] ?: ""
                                val nickname = map["nickname"] ?: ""
                                val idForSearch = map["id_for_search"] ?: ""
                                val selfIntroduction = map["self_introduction"] ?: ""
                                //val userId = dataSnapshot.key!!
                                val userId = snapshot.key!!

                                val bytes =
                                        if (iconImageString.isNotEmpty()) {
                                            Base64.decode(iconImageString, Base64.DEFAULT)
                                        } else {
                                            byteArrayOf()
                                        }

                                val userClassInstance = User(bytes, nickname, idForSearch, selfIntroduction, userId)
                                mUserArrayList.add(userClassInstance)
                                mAdapter.notifyDataSetChanged()
                            }
                            override fun onCancelled(firebaseError: DatabaseError) {}
                        }
                )
            }
        }
        override fun onCancelled(firebaseError: DatabaseError) {}
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)
        mToolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(mToolbar)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = UsersListAdapter(this)
        mUserArrayList = ArrayList<User>()
        mAdapter.notifyDataSetChanged()

        val extras = intent.extras
        val id = extras.get("id") as Int

        //これがいるのか不明
        // Userのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mUserArrayList.clear()
        mAdapter.setUserArrayList(mUserArrayList)
        mListView.adapter = mAdapter

        if (id == R.id.nav_search_users) {
            mToolbar.title = "search_users（ひとまずuser一覧）" // TODO:

            mDatabaseReference.child("users").addChildEventListener(mEventListenerForUsersList)
        }
        else if (id == R.id.nav_followings_list) {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser!! // ログインしていないとここには来ない
            mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsFollowersListRef)
        }
        else if (id == R.id.nav_followers_list) {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser!! // ログインしていないとここには来ない
            mDatabaseReference.child("users").child(user.uid).child("followers_list").addValueEventListener(mEventListenerForFollowingsFollowersListRef)
        }
    }
}
