package markosertic.ferit.com.mindorks.framework.kontrolabrzine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import markosertic.ferit.com.mindorks.framework.kontrolabrzine.databinding.ActivityCamerasBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CamerasActivity : AppCompatActivity(), IMarkerLoadListener {

    private lateinit var binding: ActivityCamerasBinding

    var markerLoadListener:IMarkerLoadListener?=null
     private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamerasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        markerLoadListener = this
        loadMarkersFromFirebase()

        auth=FirebaseAuth.getInstance()
        init()

    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateEvent::class.java)
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateEvent(event: UpdateEvent){
        loadMarkersFromFirebase()
    }
    private fun init(){
        markerLoadListener = this
        val layoutManager = LinearLayoutManager(this)
        binding.recycler!!.layoutManager = layoutManager
        binding.recycler!!.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        binding.ivBack!!.setOnClickListener{
            val backIntent = Intent(this, MapsActivity::class.java)
            startActivity(backIntent)}
    }

    private fun loadMarkersFromFirebase(){
        val markerModels : MutableList<CameraModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Kamere")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cameraSnapshot in snapshot.children) {
                        val cameraModel = cameraSnapshot.getValue(CameraModel::class.java)
                        cameraModel!!.key = cameraSnapshot.key
                        if(cameraModel.email == auth.currentUser?.email.toString())
                        markerModels.add(cameraModel)
                    }
                    markerLoadListener!!.onLoadMarkerSuccess(markerModels)
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onLoadMarkerSuccess(cartModelList: List<CameraModel>) {
        val adapter = RecyclerAdapter(this, cartModelList)
        binding.recycler.adapter = adapter
    }

    override fun onLoadMarkerFailed(message: String?) {
        TODO("Not yet implemented")
    }
}