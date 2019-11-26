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
    private lateinit var mPostArrayList: ArrayList<Post>
    private lateinit var mAdapter: PostsListAdapter

    private var mFollowingsListRef: DatabaseReference? = null

    private val mEventListenerForFollowingsListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // 仮置き

            /*
            val followings_list = dataSnapshot as Array<String>? // こんなんで大丈夫？ // 空（null）もあり得る // 参考：Lesson3項目5.7「配列」
            if (followings_list != null) {
                for (following_user_id in followings_list) {
                    Log.d("test191126n01 user_id", following_user_id)
                    // following_user_idのuserのpostを表示する
                }
            }
            */
            // TODO:
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }





    private val mEventListenerForMyPosts = object : ChildEventListener { // 仮置き // TODO:
        /*
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser // 外置きして大丈夫？
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(user!!.uid)
        userRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            }
        )
        */

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val text = map["text"] ?: ""
            val created_at = map["created_at"] ?: ""
            val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
            val post_id = dataSnapshot.key!!

            //val iconImage: ByteArray
            //val nickname: String
            var iconImageString: String? = null
            var nickname: String? = null

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            val user_id = user!!.uid
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            iconImageString = data!!["icon_image"] as String
                            nickname = data["nickname"] as String
                            Log.d("test191126n10 icon", iconImageString)
                            Log.d("test191126n10 nickname", nickname)

                            val bytes =
                                    if (iconImageString!!.isNotEmpty()) {
                                        Base64.decode(iconImageString, Base64.DEFAULT)
                                    } else {
                                        byteArrayOf()
                                    }

                            val post = Post(bytes, nickname!!, text, created_at, favoriters_list, user_id, post_id)
                            mPostArrayList.add(post)
                            mAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
            )

            /*
            val bytes =
                if (iconImageString!!.isNotEmpty()) {
                    Base64.decode(iconImageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val post = Post(bytes, nickname!!, text, created_at, favoriters_list, user_id, post_id)
            mPostArrayList.add(post)
            mAdapter.notifyDataSetChanged()
            */
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
        mAdapter = PostsListAdapter(this)
        mPostArrayList = ArrayList<Post>()
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

        if (id == R.id.nav_posts) {
            mToolbar.title = "posts"
        } else if (id == R.id.nav_search_posts) {
            mToolbar.title = "search_posts"
        } else if (id == R.id.nav_search_users) {
            mToolbar.title = "search_users"
        } else if (id == R.id.nav_followings_list) {
            mToolbar.title = "followings_list"
        } else if (id == R.id.nav_followers_list) {
            mToolbar.title = "followers_list"
        } else if (id == R.id.nav_favorites_list) {
            mToolbar.title = "favorites_list"
        } else if (id == R.id.nav_my_posts) {
            mToolbar.title = "my_posts"
        } else if (id == R.id.nav_policy) {
            mToolbar.title = "policy"
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        if ((id == R.id.nav_posts) || (id == R.id.nav_favorites_list) || (id == R.id.nav_my_posts)) {
            // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            mPostArrayList.clear()
            mAdapter.setPostArrayList(mPostArrayList)
            mListView.adapter = mAdapter

            if (id == R.id.nav_my_posts) {
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
                    mDatabaseReference.child("posts").child(user.uid)!!.addChildEventListener(mEventListenerForMyPosts)
                }


            }

        }

        return true
    }
}
