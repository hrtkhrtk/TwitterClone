package hrtkhrtk.twitterclone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
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
                convertView = mLayoutInflater!!.inflate(R.layout.list_user_detail, parent, false)!!
            }

            /*
            FirebaseDatabase.getInstance().reference.child("users").child(mUserId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<String, String>

                        val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
                        nicknameText.text = data["nickname"]

                        val idForSearchText = convertView!!.findViewById<View>(R.id.idForSearchTextView) as TextView
                        idForSearchText.text = data["id_for_search"]

                        // TODO: うまくいけば続きを書く


                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )
            */

            val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
            nicknameText.text = mUserDetail.nickname

            val idForSearchText = convertView!!.findViewById<View>(R.id.idForSearchTextView) as TextView
            idForSearchText.text = mUserDetail.idForSearch

            // TODO: うまくいけば続きを書く



        } else if (getItemViewType(position) == TYPE_POST) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_posts, parent, false)!!
            }

            /*
            FirebaseDatabase.getInstance().reference.child("posts").child(mUserId).addListenerForSingleValueEvent( // SingleValueEventでいい？
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // ↓ここに来るときには
                        // snapshot.valueがnullになることは（たぶん）ないが、一応この書き方にする
                        val data = snapshot.value as HashMap<String, String>? ?: HashMap<String, String>()

                        if (data.size != 0) { // 空じゃなければ // たぶん、ここに来るのは、空じゃない場合だろうけど
                            val keySet = data.keys
                            val keyList = keySet as ArrayList<String> // だいぶ雑 // TODO:
                            val key = keyList[position - 1] // こんなんでいい？

                            val eachPostData = data[key] as Map<String, String>

                            val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
                            nicknameText.text = eachPostData["nickname"]


                        }



                    }
                }
            )
            */

            //val post = mPostArrayList[position - 1]

            val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
            nicknameText.text = mPostArrayList[position-1].nickname

            val postCreatedAtText = convertView.findViewById<View>(R.id.postCreatedAtTextView) as TextView
            postCreatedAtText.text = mPostArrayList[position-1].createdAt

            val postText = convertView.findViewById<View>(R.id.postTextView) as TextView
            postText.text = mPostArrayList[position-1].text

            val favoritersNumberText = convertView.findViewById<View>(R.id.favoritersNumberTextView) as TextView
            val favoritersNum = mPostArrayList[position-1].favoritersList.size
            favoritersNumberText.text = favoritersNum.toString()

            // TODO: うまくいけば続きも書く。続きもPostsListAdapter.kt

        }

        return convertView!!
    }
}