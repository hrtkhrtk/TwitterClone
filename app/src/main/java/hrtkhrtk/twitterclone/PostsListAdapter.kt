package hrtkhrtk.twitterclone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class PostsListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mPostArrayList = ArrayList<Post>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mPostArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mPostArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_posts, parent, false)
        }

        val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
        nicknameText.text = mPostArrayList[position].nickname

        val postCreatedAtText = convertView.findViewById<View>(R.id.postCreatedAtTextView) as TextView
        postCreatedAtText.text = mPostArrayList[position].createdAt

        val postText = convertView.findViewById<View>(R.id.postTextView) as TextView
        postText.text = mPostArrayList[position].text

        val favoritersNumberText = convertView.findViewById<View>(R.id.favoritersNumberTextView) as TextView
        val favoritersNum = mPostArrayList[position].favoritersList.size
        favoritersNumberText.text = favoritersNum.toString()

        val bytes = mPostArrayList[position].iconImage
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
            iconImageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setPostArrayList(postArrayList: ArrayList<Post>) {
        mPostArrayList = postArrayList
    }
}