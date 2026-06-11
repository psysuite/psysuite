package org.albaspazio.psysuite

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import org.albaspazio.core.ui.show1MethodDialog
import org.albaspazio.core.updater.Constants
import org.albaspazio.core.updater.UpdateManager
import org.albaspazio.core.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple splash - just show and move to MainActivity
        Handler().postDelayed({
            go2main()
        }, 2000L) // 1.5 second splash
    }

    private fun go2main(){
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()    // close splash activity
    }
}






















/*

//        var mStimuliHandler = Handler()
//        mStimuliHandler.postDelayed({
//            // Start main activity
//            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            // close splash activity
//            finish()
//        }, 2000)
 */