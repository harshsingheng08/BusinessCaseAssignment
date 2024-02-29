package com.demo.businesscase.adapter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.demo.businesscase.R
import com.demo.businesscase.model.Item
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class HorizontalPanelAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items=ArrayList<Item>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PROFILE -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_horizontal_panel, parent, false)
            )

            VIEW_TYPE_MAP -> MapViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_map_screen, parent, false)
            )

            VIEW_TYPE_DATA -> DataViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_data, parent, false)
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ViewHolder -> {
                holder.bind(context, item)
            }

            is MapViewHolder -> {
                val thisMap: GoogleMap? = holder.mapCurrent
                if (thisMap != null)
                    thisMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(40.69754, -74.309329)))
                holder.bind(context, item)
            }

            is DataViewHolder -> {
                holder.bind(context, item)
            }
        }
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            "profile" -> VIEW_TYPE_PROFILE
            "map" -> VIEW_TYPE_MAP
            "data" -> VIEW_TYPE_DATA
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_profile)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_email)

        fun bind(context: Context, item: Item) {
            Glide.with(context)
                .load(item.content.image ?: "")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)) // Optional: Cache image
                .into(ivProfile)
            tvName.text = item.content.name ?: ""
            tvEmail.text = item.content.email ?: ""
        }
    }

    inner class MapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mapView: MapView = itemView.findViewById(R.id.mapView)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var mapCurrent: GoogleMap? = null

        init {
            // mapView.onResume()
        }

        fun bind(context: Context, item: Item) {
            tvTitle.text = item.content.title ?: ""
            val lat=item.content.lat
            val lng=item.content.lng
            mapView.onCreate(Bundle())
            mapView.onResume()

            mapView.getMapAsync { googleMap ->
                MapsInitializer.initialize(context)
                mapCurrent = googleMap
                val latitude= (lat ?: "0.0").toDouble()
                val longitude= (lng ?: "0.0").toDouble()
                val location = LatLng(latitude, longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
                googleMap.addMarker(MarkerOptions().position(location).title("My Location"))
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {return@getMapAsync

                }
                mapCurrent?.isMyLocationEnabled=true
            }
        }
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvSource: TextView = itemView.findViewById(R.id.tv_source)
        private val tvValue: TextView = itemView.findViewById(R.id.tv_value)
        fun bind(context: Context, item: Item) {
            tvName.text = item.content.titleData ?: ""
            tvSource.text = item.content.source ?: ""
            tvValue.text = item.content.value ?: ""
        }
    }

    fun setItems(list:ArrayList<Item>){
        this.items=list
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_PROFILE = 0
        private const val VIEW_TYPE_MAP = 1
        private const val VIEW_TYPE_DATA = 2
    }
}
