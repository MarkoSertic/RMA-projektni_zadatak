package markosertic.ferit.com.mindorks.framework.kontrolabrzine

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import markosertic.ferit.com.mindorks.framework.kontrolabrzine.databinding.ActivityMainBinding
import java.lang.Exception


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private var firstTimeUser=true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonClicks()

        auth=FirebaseAuth.getInstance()


    }
    private fun buttonClicks(){

        binding.btnLogin.setOnClickListener {
            firstTimeUser = false
            createOrLoginUser()
        }
        binding.btnRegister.setOnClickListener{
            firstTimeUser=true
            createOrLoginUser()
        }

    }

    private fun createOrLoginUser() {
       val email=binding.etEmailLogin.text.toString()
        val password = binding.etPasswordLogin.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            GlobalScope.launch(Dispatchers.IO) {
                try {

                    if (firstTimeUser){
                        auth.createUserWithEmailAndPassword(email, password).await()
                    }else auth.signInWithEmailAndPassword(email, password).await()

                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,"Ulogirani ste", Toast.LENGTH_SHORT).show()
                        val i = Intent(this@MainActivity, UserActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkIfUserIsLoggedIn(){
        if(auth.currentUser !=null){
            val i = Intent(this@MainActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        checkIfUserIsLoggedIn()
    }
}