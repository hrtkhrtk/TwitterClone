package hrtkhrtk.twitterclone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.FloatingActionButton
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.content.Intent
import android.support.v7.widget.Toolbar
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mToolbar: Toolbar

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    //private lateinit var mPostArrayList: ArrayList<Post>
    private lateinit var mPostForShowingArrayList: ArrayList<PostForShowing>
    //private lateinit var mFollowingsListWithCurrentUser: ArrayList<String>
    //private lateinit var mAdapter: PostsListAdapter
    private lateinit var mAdapter: PostForShowingsListAdapter

    //private var mFollowingsListRef: DatabaseReference? = null

    private val mEventListenerForFollowingsListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // TODO:

            // 仮置き
            val followings_list = dataSnapshot.value as ArrayList<String>? ?: ArrayList<String>()

            val user = FirebaseAuth.getInstance().currentUser!! // ここはログインユーザしか来ない
            var followings_list_with_current_user = followings_list
            followings_list_with_current_user.add(user.uid)

                for (user_id in followings_list_with_current_user) {
                    mDatabaseReference.child("posts").child(user_id).addChildEventListener(
                        object : ChildEventListener {
                            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                                //val map = dataSnapshot.value as Map<String, String>
                                //val map = dataSnapshot.value as Map<String, Any>
                                val map = dataSnapshot.value as Map<*, *>
                                val text = map["text"] ?: "" as String // なぜか効かない
                                //val created_at = map["created_at"] ?: ""
                                val created_at_Long = map["created_at"] as Long // ここは必ず存在
                                //val created_at_Long = map["created_at"]!!.toLong() // ここは必ず存在
                                val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
                                val post_id = dataSnapshot.key!!

                                var iconImageString: String? = null
                                var nickname: String? = null

                                FirebaseDatabase.getInstance().reference.child("users").child(user_id).addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val data = snapshot.value as Map<String, String> // ここは必ず存在
                                            iconImageString = data["icon_image"]
                                            nickname = data["nickname"]
                                            //Log.d("test191126n40 icon", iconImageString)
                                            //Log.d("test191126n40 nickname", nickname)

                                            val bytes =
                                                if (iconImageString!!.isNotEmpty()) {
                                                    Base64.decode(iconImageString, Base64.DEFAULT)
                                                } else {
                                                    byteArrayOf()
                                                }

                                            //val post = Post(bytes, nickname!!, text, created_at, favoriters_list, user_id, post_id)
                                            //mPostArrayList.add(post)
                                            //val postForShowing = PostForShowing(bytes, nickname!!, text, created_at, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                                            //val postForShowing = PostForShowing(bytes, nickname!!, text, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                                            val postForShowing = PostForShowing(bytes, nickname!!, text as String, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                                            mPostForShowingArrayList.add(postForShowing)
                                            mAdapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(firebaseError: DatabaseError) {}
                                    }
                                )
                            }

                            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

                            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

                            override fun onCancelled(databaseError: DatabaseError) {}
                        }
                    )
                }

            //}

        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }



    private val mEventListenerForMyPosts = object : ChildEventListener { // 仮置き // TODO:
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val text = map["text"] ?: ""
            //val created_at = map["created_at"] ?: ""
            //val created_at_Long = map["created_at"] as Long // ここは必ず存在
            val created_at_Long = map["created_at"]!!.toLong() // ここは必ず存在
            val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
            val post_id = dataSnapshot.key!!

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            val user_id = user!!.uid
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<String, String> // 必ず存在
                        val iconImageString = data["icon_image"] as String
                        val nickname = data["nickname"] as String

                        val bytes =
                            if (iconImageString.isNotEmpty()) {
                                Base64.decode(iconImageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }

                        //val post = Post(bytes, nickname, text, created_at, favoriters_list, user_id, post_id)
                        //mPostArrayList.add(post)
                        //val postForShowing = PostForShowing(bytes, nickname, text, created_at, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                        val postForShowing = PostForShowing(bytes, nickname, text, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                        mPostForShowingArrayList.add(postForShowing)
                        mAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { _ ->
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@MainActivity, PostSendActivity::class.java)
                startActivity(intent)
            }
        }

        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)



        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        //mAdapter = PostsListAdapter(this)
        mAdapter = PostForShowingsListAdapter(this)
        //mPostArrayList = ArrayList<Post>()
        mPostForShowingArrayList = ArrayList<PostForShowing>()
        //mFollowingsListWithCurrentUser = ArrayList<String>()
        mAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // Resumeしたときはpostsを表示する
        onNavigationItemSelected(navigationView.menu.getItem(0))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_SettingActivity) {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        else if (id == R.id.action_PurchasingActivity) {
            val intent = Intent(this, PurchasingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId


        if (id == R.id.nav_search_posts) {
            mToolbar.title = "search_posts"
        } else if (id == R.id.nav_favorites_list) {
            mToolbar.title = "favorites_list"
        } else if (id == R.id.nav_policy) {
            //mToolbar.title = "policy"
            val intent = Intent(this@MainActivity, PolicyActivity::class.java)
            startActivity(intent)
        }


        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        if ((id == R.id.nav_posts) || (id == R.id.nav_favorites_list) || (id == R.id.nav_my_posts)) {
            // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            //mPostArrayList.clear()
            //mAdapter.setPostArrayList(mPostArrayList)
            mPostForShowingArrayList.clear()
            mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
            mListView.adapter = mAdapter


            if (id == R.id.nav_posts) {
                mToolbar.title = "posts"
                // この中は仮置き // TODO:

                Log.d("test191127n10", "test191127n10")

                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Log.d("test191127n11", "test191127n11")
                    // removeいる？
                    // remove↓これで大丈夫？ // TODO:
                    //mDatabaseReference.child("users").child(user.uid).child("followings_list").removeEventListener(mEventListenerForFollowingsListRef)
                    mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsListRef)
                }
            } else if (id == R.id.nav_my_posts) {
                mToolbar.title = "my_posts"
                // この中は仮置き // TODO:

                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                else {
                    //mDatabaseReference.child("posts").child(user.uid)!!.addValueEventListener(mEventListenerForMyPosts)
                    // removeいる？
                    // remove↓これで大丈夫？ // TODO:
                    //mDatabaseReference.child("posts").child(user.uid).removeEventListener(mEventListenerForMyPosts)
                    mDatabaseReference.child("posts").child(user.uid).addChildEventListener(mEventListenerForMyPosts)
                }
            }

        }
        else if ((id == R.id.nav_search_users) || (id == R.id.nav_followings_list) || (id == R.id.nav_followers_list)) {
            if (id == R.id.nav_search_users) {
                val intent = Intent(this@MainActivity, UsersListActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            else if ((id == R.id.nav_followings_list) || (id == R.id.nav_followers_list)) {
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this@MainActivity, UsersListActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                }
            }
        }


        return true
    }
}
