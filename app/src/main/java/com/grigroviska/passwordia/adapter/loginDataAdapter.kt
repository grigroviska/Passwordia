package com.grigroviska.passwordia.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.model.LoginData
import java.net.MalformedURLException
import java.net.URL

class loginDataAdapter(private var loginDataList: List<LoginData>) :
    RecyclerView.Adapter<loginDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_login_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loginData = loginDataList[position]
        // Verileri ViewHolder öğelerine atayın
        holder.itemView.findViewById<TextView>(R.id.websiteFromRoom).text = truncateString(getDomainFromUrl(loginData.website))
        holder.itemView.findViewById<TextView>(R.id.usernameFromRoom).text = truncateString(loginData.userName)

        holder.itemView.findViewById<ImageView>(R.id.copyPassword).setOnClickListener {
            val clipboard = holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("password", loginData.password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(holder.itemView.context, "Password Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return loginDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder öğelerini burada tanımlayın
    }

    private fun getDomainFromUrl(url: String): String {
        return try {
            val domain = URL(url).host
            val protocol = URL(url).protocol
            val prefix = if (protocol == "http" || protocol == "https") "$protocol://" else ""
            val withoutWww = domain.removePrefix("www.")
            prefix + withoutWww
        } catch (e: MalformedURLException) {
            url
        }
    }


    private fun truncateString(text: String): String {
        return if (text.length > 20) {
            text.substring(0, 20) + "..."
        } else {
            text
        }
    }
}
