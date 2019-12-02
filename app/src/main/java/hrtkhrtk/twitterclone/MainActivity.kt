package hrtkhrtk.twitterclone

import android.content.Context
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
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

//class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
class MainActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    //private lateinit var mPostArrayList: ArrayList<Post>
    private lateinit var mPostForShowingArrayList: ArrayList<PostForShowing>
    //private lateinit var mFollowingsListWithCurrentUser: ArrayList<String>
    //private lateinit var mAdapter: PostsListAdapter
    private lateinit var mAdapter: PostForShowingsListAdapter

    //private var mFollowingsListRef: DatabaseReference? = null

    private val mEventListenerForPostsRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val posts_list_all = dataSnapshot.value as HashMap<String, String>? ?: HashMap<String, String>() // ここはnullかも

            for (user_id in posts_list_all.keys) {
                val posts_list_each = posts_list_all[user_id] as Map<String, String> // ここは必ず存在（たぶん）

                mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot_in_userRef: DataSnapshot) {
                            val data_in_userRef = snapshot_in_userRef.value as Map<String, String> // ここは必ず存在
                            val iconImageString = data_in_userRef["icon_image"]!! // ここは必ず存在
                            val nickname = data_in_userRef["nickname"]!! // ここは必ず存在

                            val bytes =
                                if (iconImageString.isNotEmpty()) {
                                    Base64.decode(iconImageString, Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            for (post_id in posts_list_each.keys) {
                                val post_each = posts_list_each[post_id] as Map<String, String> // ここは必ず存在
                                val text = post_each["text"]!! // ここは必ず存在
                                val post_each_2 = posts_list_each[post_id] as Map<String, Long> // ここは必ず存在
                                val created_at_Long = post_each_2["created_at"]!! // ここは必ず存在
                                val favoriters_list = post_each["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？

                                val postForShowing = PostForShowing(bytes, nickname, text, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                                mPostForShowingArrayList.add(postForShowing)
                                mAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(firebaseError_in_userRef: DatabaseError) {}
                    }
                )
            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }



    private val mEventListenerForFavoritesListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val favorites_list = dataSnapshot.value as ArrayList<Map<String, String>>? ?: ArrayList<Map<String, String>>()
            for (favorite_element in favorites_list) {
                mDatabaseReference.child("posts").child(favorite_element["user_id"]!!).child(favorite_element["post_id"]!!).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot_in_postRef: DataSnapshot) {
                            val data_in_postRef = snapshot_in_postRef.value as Map<String, String> // ここは必ず存在
                            val text = data_in_postRef["text"]!! // ここは必ず存在
                            val data_in_postRef_2 = snapshot_in_postRef.value as Map<String, Long> // ここは必ず存在
                            val created_at_Long = data_in_postRef_2["created_at"]!! // ここは必ず存在
                            val favoriters_list = data_in_postRef["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？

                            mDatabaseReference.child("users").child(favorite_element["user_id"]!!).addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot_in_userRef: DataSnapshot) {
                                        val data_in_userRef = snapshot_in_userRef.value as Map<String, String> // ここは必ず存在
                                        val iconImageString = data_in_userRef["icon_image"]!! // ここは必ず存在
                                        val nickname = data_in_userRef["nickname"]!! // ここは必ず存在

                                        val bytes =
                                            if (iconImageString.isNotEmpty()) {
                                                Base64.decode(iconImageString, Base64.DEFAULT)
                                            } else {
                                                byteArrayOf()
                                            }

                                        val postForShowing = PostForShowing(bytes, nickname, text, created_at_Long, favoriters_list, favorite_element["user_id"]!!, favorite_element["post_id"]!!, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                                        mPostForShowingArrayList.add(postForShowing)
                                        mAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(firebaseError_in_userRef: DatabaseError) {}
                                }
                            )
                        }

                        override fun onCancelled(firebaseError_in_postRef: DatabaseError) {}
                    }
                )
            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }



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
                                val map = dataSnapshot.value as Map<String, String>
                                val text = map["text"] ?: ""
                                val map2 = dataSnapshot.value as Map<String, Long>
                                val created_at_Long = map2["created_at"]!! // ここは必ず存在
                                val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
                                val post_id = dataSnapshot.key!!

                                var iconImageString: String? = null
                                var nickname: String? = null

                                //FirebaseDatabase.getInstance().reference.child("users").child(user_id).addListenerForSingleValueEvent(
                                mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val data = snapshot.value as Map<String, String> // ここは必ず存在
                                            iconImageString = data["icon_image"]
                                            nickname = data["nickname"]

                                            val bytes =
                                                if (iconImageString!!.isNotEmpty()) {
                                                    Base64.decode(iconImageString, Base64.DEFAULT)
                                                } else {
                                                    byteArrayOf()
                                                }

                                            val postForShowing = PostForShowing(bytes, nickname!!, text, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
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
            val map2 = dataSnapshot.value as Map<String, Long>
            val created_at_Long = map2["created_at"]!! // ここは必ず存在
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



        searchButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val searchText = searchWindow.text.toString()

            if (searchText.isEmpty()) {
                Snackbar.make(v, "入力して下さい", Snackbar.LENGTH_LONG).show()

                //これがいるのか不明
                // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mPostForShowingArrayList.clear()
                mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
                mListView.adapter = mAdapter

                title = "post一覧"

                mDatabaseReference.child("posts").addListenerForSingleValueEvent(mEventListenerForPostsRef) // ひとまずSingleValueEventで
            } else {
                //これがいるのか不明
                // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mPostForShowingArrayList.clear()
                mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
                mListView.adapter = mAdapter

                title = "検索結果"

                mDatabaseReference.child("posts").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val posts_list_all = dataSnapshot.value as HashMap<String, String>? ?: HashMap<String, String>() // ここはnullかも

                            for (user_id in posts_list_all.keys) {
                                val posts_list_each = posts_list_all[user_id] as Map<String, String> // ここは必ず存在（たぶん）
                                for (post_id in posts_list_each.keys) {
                                    val post_each = posts_list_each[post_id] as Map<String, String> // ここは必ず存在
                                    val text = post_each["text"]!! // ここは必ず存在

                                    val regex = Regex(searchText) // 参考：http://extra-vision.blogspot.com/2016/11/kotlin.html
                                    if (regex.containsMatchIn(text)) {
                                        val post_each_2 = posts_list_each[post_id] as Map<String, Long> // ここは必ず存在
                                        val created_at_Long = post_each_2["created_at"]!! // ここは必ず存在
                                        val favoriters_list = post_each["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？

                                        mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot_in_userRef: DataSnapshot) {
                                                    val data_in_userRef = snapshot_in_userRef.value as Map<String, String> // ここは必ず存在
                                                    val iconImageString = data_in_userRef["icon_image"]!! // ここは必ず存在
                                                    val nickname = data_in_userRef["nickname"]!! // ここは必ず存在

                                                    val bytes =
                                                        if (iconImageString.isNotEmpty()) {
                                                            Base64.decode(iconImageString, Base64.DEFAULT)
                                                        } else {
                                                            byteArrayOf()
                                                        }

                                                    val postForShowing = PostForShowing(bytes, nickname, text, created_at_Long, favoriters_list, user_id, post_id, mPostForShowingArrayList.size) // onChildRemovedのときもPostForShowingのpositionInArrayListへの配慮が必要
                                                    mPostForShowingArrayList.add(postForShowing)
                                                    mAdapter.notifyDataSetChanged()
                                                }

                                                override fun onCancelled(firebaseError_in_userRef: DatabaseError) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )
            }
        }



        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        //val navigationView = findViewById<NavigationView>(R.id.nav_view)
        //navigationView.setNavigationItemSelectedListener(this)
        val navHeader = findViewById<RelativeLayout>(R.id.nav_header)
        val navListView = findViewById<ListView>(R.id.nav_menu_items)
        val navFooter = findViewById<FrameLayout>(R.id.nav_footer)

        val navigationItemList = ArrayList<NavigationItem>()
        val navigationItem_01 = NavigationItem(id__nav_posts, "posts")
        val navigationItem_02 = NavigationItem(id__nav_search_posts, "search_posts")
        val navigationItem_03 = NavigationItem(id__nav_search_users, "search_users")
        val navigationItem_04 = NavigationItem(id__nav_followings_list, "followings_list")
        val navigationItem_05 = NavigationItem(id__nav_followers_list, "followers_list")
        val navigationItem_06 = NavigationItem(id__nav_favorites_list, "favorites_list")
        val navigationItem_07 = NavigationItem(id__nav_my_posts, "my_posts")
        //val navigationItem_08 = NavigationItem(id__nav_policy, "policy")

        navigationItemList.add(navigationItem_01)
        navigationItemList.add(navigationItem_02)
        navigationItemList.add(navigationItem_03)
        navigationItemList.add(navigationItem_04)
        navigationItemList.add(navigationItem_05)
        navigationItemList.add(navigationItem_06)
        navigationItemList.add(navigationItem_07)
        //navigationItemList.add(navigationItem_08)

        val navigationListAdapter = NavigationListAdapter(this)
        navigationListAdapter.setNavigationItemArrayList(navigationItemList)
        navListView.adapter = navigationListAdapter
        navigationListAdapter.notifyDataSetChanged() // これがいるか不明

        //val navigationView = findViewById<NavigationView>(R.id.nav_view)
        //navigationView.setNavigationItemSelectedListener(this)



        navListView.setOnItemClickListener { _, _, position, _ ->
            val item_id = navigationItemList[position].id

            //if (item_id == id__nav_policy) {
            //    //mToolbar.title = "policy"
            //    val intent = Intent(this@MainActivity, PolicyActivity::class.java)
            //    startActivity(intent)
            //}

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)

            if ((item_id == id__nav_posts) || (item_id == id__nav_favorites_list) || (item_id == id__nav_my_posts) || (item_id == id__nav_search_posts)) {
                // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mPostForShowingArrayList.clear()
                mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
                mListView.adapter = mAdapter


                if (item_id == id__nav_posts) {
                    searchWindow.visibility = View.GONE
                    searchButton.visibility = View.GONE

                    mToolbar.title = "posts"
                    // この中は仮置き // TODO:

                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        // removeいる？
                        // remove↓これで大丈夫？ // TODO:
                        //mDatabaseReference.child("users").child(user.uid).child("followings_list").removeEventListener(mEventListenerForFollowingsListRef)
                        mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsListRef)
                    }
                } else if (item_id == id__nav_my_posts) {
                    searchWindow.visibility = View.GONE
                    searchButton.visibility = View.GONE

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
                else if (item_id == id__nav_favorites_list) {
                    searchWindow.visibility = View.GONE
                    searchButton.visibility = View.GONE

                    mToolbar.title = "favorites_list"

                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        // removeいる？ // TODO:
                        mDatabaseReference.child("users").child(user.uid).child("favorites_list").addValueEventListener(mEventListenerForFavoritesListRef)
                    }
                }
                else if (item_id == id__nav_search_posts) {
                    title = "最初に表示されるのはpost一覧"

                    //val searchWindow = findViewById<EditText>(R.id.searchWindow)
                    //val searchButton = findViewById<Button>(R.id.searchButton)
                    searchWindow.visibility = View.VISIBLE
                    searchButton.visibility = View.VISIBLE

                    mDatabaseReference.child("posts").addListenerForSingleValueEvent(mEventListenerForPostsRef) // ひとまずSingleValueEventで
                }
            }
            else if ((item_id == id__nav_search_users) || (item_id == id__nav_followings_list) || (item_id == id__nav_followers_list)) {
                if (item_id == id__nav_search_users) {
                    val intent = Intent(this@MainActivity, UsersListActivity::class.java)
                    intent.putExtra("id", item_id)
                    startActivity(intent)
                }
                else if ((item_id == id__nav_followings_list) || (item_id == id__nav_followers_list)) {
                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        val intent = Intent(this@MainActivity, UsersListActivity::class.java)
                        intent.putExtra("id", item_id)
                        startActivity(intent)
                    }
                }
            }
        }



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
        //val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // Resumeしたときはpostsを表示する
        //onNavigationItemSelected(navigationView.menu.getItem(0))

        // これが必要か不明
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mPostForShowingArrayList.clear()
        mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
        mListView.adapter = mAdapter

        searchWindow.visibility = View.GONE
        searchButton.visibility = View.GONE

        mToolbar.title = "posts"
        // この中は仮置き // TODO:

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        // ログインしていなければログイン画面に遷移させる
        if (user == null) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        else {
            // removeいる？
            // remove↓これで大丈夫？ // TODO:
            //mDatabaseReference.child("users").child(user.uid).child("followings_list").removeEventListener(mEventListenerForFollowingsListRef)
            mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsListRef)
        }
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

    /*
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_policy) {
            //mToolbar.title = "policy"
            val intent = Intent(this@MainActivity, PolicyActivity::class.java)
            startActivity(intent)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        //if ((id == R.id.nav_posts) || (id == R.id.nav_favorites_list) || (id == R.id.nav_my_posts)) {
        //if ((id == R.id.nav_posts) || (id == R.id.nav_favorites_list) || (id == R.id.nav_my_posts) || (id == R.id.nav_favorites_list)) {
        if ((id == R.id.nav_posts) || (id == R.id.nav_favorites_list) || (id == R.id.nav_my_posts) || (id == R.id.nav_search_posts)) {
            // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            //mPostArrayList.clear()
            //mAdapter.setPostArrayList(mPostArrayList)
            mPostForShowingArrayList.clear()
            mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
            mListView.adapter = mAdapter


            if (id == R.id.nav_posts) {
                searchWindow.visibility = View.GONE
                searchButton.visibility = View.GONE

                mToolbar.title = "posts"
                // この中は仮置き // TODO:

                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                else {
                    // removeいる？
                    // remove↓これで大丈夫？ // TODO:
                    //mDatabaseReference.child("users").child(user.uid).child("followings_list").removeEventListener(mEventListenerForFollowingsListRef)
                    mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsListRef)
                }
            } else if (id == R.id.nav_my_posts) {
                searchWindow.visibility = View.GONE
                searchButton.visibility = View.GONE

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
            else if (id == R.id.nav_favorites_list) {
                searchWindow.visibility = View.GONE
                searchButton.visibility = View.GONE

                mToolbar.title = "favorites_list"

                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                else {
                    // removeいる？ // TODO:
                    mDatabaseReference.child("users").child(user.uid).child("favorites_list").addValueEventListener(mEventListenerForFavoritesListRef)
                }
            }
            else if (id == R.id.nav_search_posts) {
                title = "最初に表示されるのはpost一覧"

                //val searchWindow = findViewById<EditText>(R.id.searchWindow)
                //val searchButton = findViewById<Button>(R.id.searchButton)
                searchWindow.visibility = View.VISIBLE
                searchButton.visibility = View.VISIBLE

                mDatabaseReference.child("posts").addListenerForSingleValueEvent(mEventListenerForPostsRef) // ひとまずSingleValueEventで
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
    */
}
