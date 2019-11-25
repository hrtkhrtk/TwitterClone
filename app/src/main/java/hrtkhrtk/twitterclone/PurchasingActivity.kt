package hrtkhrtk.twitterclone

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class PurchasingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchasing)

        // UIの初期設定
        title = "購入ページ"
    }
}
