package hrtkhrtk.twitterclone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_signup.*

import java.util.HashMap

class SignupActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mSignupListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginAfterSignupListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()

        // アカウント作成処理のリスナー
        mSignupListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                // ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                loginAfterSignup(email, password)
            } else {

                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        // ログイン処理のリスナー
        mLoginAfterSignupListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                val user = mAuth.currentUser
                val usersRef = mDataBaseReference.child("users").child(user!!.uid)

                // アカウント作成の時は表示名をFirebaseに保存する // 保存をするのはここじゃなくてもいい気がするが
                val nickname = nicknameText.text.toString()
                val email =emailText.text.toString()
                val id_for_search = idForSearchText.text.toString()
                val status = 0.toString() // 0:お試しユーザー、1:サブスクユーザー
                //val data = HashMap<String, String>()
                val data = HashMap<String, Any>()
                data["nickname"] = nickname
                data["email"] = email
                data["id_for_search"] = id_for_search
                data["created_at"] = ServerValue.TIMESTAMP
                data["status"] = status
                usersRef.setValue(data)
                usersRef.addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val dataInListener = snapshot.value as Map<String, Long>
                                val created_at_InListener = dataInListener["created_at"] ?: ((-1).toLong()) // (-1)の値に意味はない
                                if (created_at_InListener >= 0) {
                                    val available_to = created_at_InListener + 1000*60*5 // 5分後
                                    val data_to_update_InListener = HashMap<String, Any>()
                                    data_to_update_InListener["available_to"] = available_to
                                    usersRef.updateChildren(data_to_update_InListener)
                                } else {
                                    val available_to = (-1).toLong() // (-1)の値に意味はない
                                    val data_to_update_InListener = HashMap<String, Any>()
                                    data_to_update_InListener["available_to"] = available_to
                                    usersRef.updateChildren(data_to_update_InListener)
                                }
                            }

                            override fun onCancelled(firebaseError: DatabaseError) {}
                        }
                )




                val idForSearchListRef = mDataBaseReference.child("id_for_search_list").child(id_for_search)
                val data2 = HashMap<String, String>()
                data2["user_id"] = user.uid
                idForSearchListRef.setValue(data2)

                // 表示名をPrefarenceに保存する
                saveName(nickname)

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE

                val intent = Intent(this, RegisteringActivity::class.java)
                startActivity(intent)

            } else {
                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        // UIの準備
        title = "Sign up"

        signupButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val nickname = nicknameText.text.toString()
            val id_for_search = idForSearchText.text.toString()

            //if (email.length != 0 && password.length >= 6 && nickname.length != 0) {
            if (email.length != 0 && password.length >= 6 && nickname.length != 0 && id_for_search.length != 0) {
                signup(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun signup(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mSignupListener)
    }

    private fun loginAfterSignup(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginAfterSignupListener)
    }

    private fun saveName(name: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }
}
