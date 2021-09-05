package markosertic.ferit.com.mindorks.framework.kontrolabrzine

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder


class RecyclerAdapter(
    private val context:Context,
    private val MarkerModelList: List<CameraModel>

): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tv_name: TextView?=null
        var tv_lat: TextView?=null
        var tv_long: TextView?=null
        var iv_delete: ImageView?=null
        init{
            tv_name = itemView.findViewById(R.id.tv_name) as TextView
            tv_lat = itemView.findViewById(R.id.tv_lat_num) as TextView
            tv_long = itemView.findViewById(R.id.tv_long_num) as TextView
            iv_delete = itemView.findViewById(R.id.iv_delete) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.recycler_item,parent,false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv_name!!.text = StringBuilder().append(MarkerModelList[position].name)
        holder.tv_lat!!.text = StringBuilder().append(MarkerModelList[position].latitude)
        holder.tv_long!!.text = StringBuilder().append(MarkerModelList[position].longitude)
        holder.iv_delete!!.setOnClickListener{_ ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("Obriši?")
                .setMessage("Želite li obrisati kameru?")
                .setNegativeButton("Odustani") {dialog,_ ->dialog.dismiss()}
                .setPositiveButton("Obriši") {dialog,_ ->dialog.dismiss()
                    notifyItemRemoved(position)
                    FirebaseDatabase.getInstance()
                        .getReference("Kamere")
                        .child(MarkerModelList[position].key!!)
                        .removeValue()
                        .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateEvent())}
                }
                .create()
            dialog.show()

        }
    }

    override fun getItemCount(): Int {
        return MarkerModelList.size
    }
}