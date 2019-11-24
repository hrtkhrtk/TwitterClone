package hrtkhrtk.twitterclone

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.content.Intent

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

import java.util.HashMap

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()

        // ログイン処理のリスナー
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                val user = mAuth.currentUser
                val usersRef = mDataBaseReference.child("users").child(user!!.uid)

                usersRef.addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["nickname"] as String)
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE

                // Activityを閉じる
                finish()

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
        title = "Log in"

        loginButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6) {
                login(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        toSignupActivityButton.setOnClickListener { v ->
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    private fun saveName(name: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }
}
