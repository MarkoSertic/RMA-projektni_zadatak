package markosertic.ferit.com.mindorks.framework.kontrolabrzine

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import markosertic.ferit.com.mindorks.framework.kontrolabrzine.databinding.ActivityMainBinding
import markosertic.ferit.com.mindorks.framework.kontrolabrzine.databinding.ActivityUserBinding
import java.lang.Exception

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        setUserInfo()
        btnClicks()
    }

    private fun btnClicks() {
        binding.tvProfileSignOut.setOnClickListener{
            signOutUser()
        }
        binding.btnDeactivate.setOnClickListener{
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Do you want to deactivate your account")

                .setNegativeButton("Cancel") { dialog, _ ->dialog.dismiss() }

                .setPositiveButton("Deactivate") { dialog, _ ->
                    dialog.dismiss()
                    auth.currentUser?.delete()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    signOutUser()
                }
                .create()
            dialog.show()
        }
        binding.btnMap.setOnClickListener{
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        }
    }



    private fun signOutUser() {
    auth.signOut()
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        Toast.makeText(this,"Sign outali ste se", Toast.LENGTH_SHORT).show()
    }

    private fun setUserInfo() {
        binding.etProfileEmail.setText(auth.currentUser?.email)
    }
    private fun checkIfUserIsLoggedIn(){
        if(auth.currentUser ==null){
            val i = Intent(this@UserActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        checkIfUserIsLoggedIn()
    }
}