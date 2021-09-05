package markosertic.ferit.com.mindorks.framework.kontrolabrzine

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import markosertic.ferit.com.mindorks.framework.kontrolabrzine.databinding.ActivityMapsBinding
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationClient: FusedLocationProviderClient



    companion object{
        private const val LOCATION_REQUEST_CODE = 1}
    private fun  bitmapDescriptorFromVector(context: Context, vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        val canvas =  Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth=FirebaseAuth.getInstance()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)

        binding.btnRecycler.setOnClickListener{
            val i = Intent(this, CamerasActivity::class.java)
            startActivity(i)
        }
    }

    val cameraModels:MutableList<CameraModel> = ArrayList()
    private fun loadCamerasFromFirebase(){
        FirebaseDatabase.getInstance().getReference("Kamere").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    for (cameraSnapshot in snapshot.children) {
                        val cameraModel = cameraSnapshot.getValue(CameraModel::class.java)
                        cameraModel!!.key = cameraSnapshot.key
                        cameraModels.add(cameraModel)
                        val latlong = LatLng(
                            cameraModel.latitude!!.toDouble(),
                            cameraModel.longitude!!.toDouble()
                        )
                        if (cameraModel.email == auth.currentUser?.email) {
                            mMap.addMarker(
                                MarkerOptions().position(latlong).title(cameraModel.name).icon(bitmapDescriptorFromVector(this@MapsActivity, R.drawable.crvena_kamera)))

                        } else {
                            mMap.addMarker(
                                MarkerOptions().position(latlong).title(cameraModel.name)
                                    .icon(bitmapDescriptorFromVector(this@MapsActivity, R.drawable.zuta_kamera))
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,11f))

            }
        }

        // Add a marker in Sydney and move the camera
     loadCamerasFromFirebase()



        mMap.setOnMapLongClickListener {

            var mp:MediaPlayer
            mp=MediaPlayer.create(this,R.raw.camerashuttersoundeffect)
            mp.start()

            var newName: String = ""
            val input = EditText(this)
            input.hint = "Unesite ime kamere"
            input.inputType = InputType.TYPE_CLASS_TEXT

            val dialog = AlertDialog.Builder(this)
                .setTitle("Unesite ime nove kamere")
                .setView(input)
                .setNegativeButton("Odustani") { dialog, _ ->dialog.dismiss() }

                .setPositiveButton("Unesi") { dialog, _ ->
                    dialog.dismiss()
                    newName = input.text.toString()
                    val db = FirebaseDatabase.getInstance()
                    val newCamera = DataCameraModel()
                    val key = (cameraModels.size + 1).toString()
                    newCamera.latitude = it.latitude.toString()
                    newCamera.longitude = it.longitude.toString()
                    newCamera.name = newName
                    newCamera.email = auth.currentUser?.email.toString()
                    db.getReference("Kamere")
                        .child("0${key}").setValue(newCamera)
                    loadCamerasFromFirebase()
                }
                .create()
            dialog.show()
        }

        mMap.setOnInfoWindowLongClickListener {
            var cameraName: String = it.title!!.toString()
            var key = ""
            var newName = ""
            var email = ""
            val input = EditText(this)
            input.hint = "Unesite ime kamere"
            input.inputType = InputType.TYPE_CLASS_TEXT
            val db = FirebaseDatabase.getInstance()
            for (camera in cameraModels) {
                if (camera.name == cameraName) {
                    key = camera.key.toString()
                    email = camera.email.toString()
                }

            }
            if (email == auth.currentUser?.email) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Želite li obrisati kameru, ili promijeniti ime kamere?")
                    .setNegativeButton("Obriši") { dialog, _ ->
                        dialog.dismiss()
                        db.getReference("Kamere")
                            .child(key)
                            .removeValue()
                        loadCamerasFromFirebase()
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(
                            intent,
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                        )
                        overridePendingTransition(0, 0)

                    }
                    .setPositiveButton("Promijeni ime") { dialog, _ ->
                        dialog.dismiss()

                        val subdialog = AlertDialog.Builder(this)
                            .setTitle("Unesite novo ime kamere")
                            .setView(input)
                            .setNegativeButton("Odustani") { subdialog, _ ->
                                subdialog.dismiss()
                            }
                            .setPositiveButton("Unesi") { subdialog, _ ->
                                subdialog.dismiss()

                                newName = input.text.toString()
                                db.getReference("Kamere")
                                    .child(key)
                                    .child("name").setValue(newName)
                                db.getReference("Kamere")
                                    .child(key)
                                    .child("email").setValue(auth.currentUser?.email.toString())
                                loadCamerasFromFirebase()
                                finish()
                                overridePendingTransition(0, 0)
                                startActivity(
                                    intent,
                                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                                )
                                overridePendingTransition(0, 0)
                            }
                            .create()
                        subdialog.show()
                    }
                    .create()
                dialog.show()
            }else Toast.makeText(this,"Ne možete mijenjati tuđe kamere", Toast.LENGTH_SHORT).show()
        }

    }
}