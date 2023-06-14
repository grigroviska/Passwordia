package com.grigroviska.passwordia.adapter

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.activities.CreateLoginData
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel
import java.net.MalformedURLException
import java.net.URL

class loginDataAdapter(
    private var loginDataList: List<LoginData>
) : RecyclerView.Adapter<loginDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_login_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loginData = loginDataList[position]
        holder.bind(loginData)
    }

    override fun getItemCount(): Int {
        return loginDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionsImageView: ImageView = itemView.findViewById(R.id.options)
        private val websiteTextView: TextView = itemView.findViewById(R.id.websiteFromRoom)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameFromRoom)

        fun bind(loginData: LoginData) {
            websiteTextView.text = loginData.website?.let { getDomainFromUrl(it) }
                ?.let { truncateString(it) }
            usernameTextView.text = loginData.userName?.let { truncateString(it) }

            optionsImageView.setOnClickListener {
                showBottomSheetDialog(loginData, itemView.context)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, CreateLoginData::class.java)
                intent.putExtra("loginId", loginData.id)
                context.startActivity(intent)
            }

            itemView.findViewById<ImageView>(R.id.copyPassword).setOnClickListener {
                val clipboard =
                    itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("password", loginData.password)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(itemView.context, "Copy Password!", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun openWebsite(url: String, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBottomSheetDialog(loginData: LoginData, context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.options_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val websiteName = view.findViewById<TextView>(R.id.websiteName)
        val copyEmail = view.findViewById<LinearLayout>(R.id.copyEmailLayout)
        val copyUsername = view.findViewById<LinearLayout>(R.id.copyUserNameLayout)
        val copyPassword = view.findViewById<LinearLayout>(R.id.copyPasswordLayout)
        val openWebsite = view.findViewById<LinearLayout>(R.id.openWebsiteLayout)
        val delete = view.findViewById<LinearLayout>(R.id.deleteLayout)

        websiteName.text = loginData.website

        copyEmail.setOnClickListener {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("email", loginData.userName)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, context.getString(R.string.email_copied), Toast.LENGTH_SHORT)
                .show()
            bottomSheetDialog.dismiss()
        }

        copyUsername.setOnClickListener {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("username", loginData.userName)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                context.getString(R.string.username_copied),
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }

        copyPassword.setOnClickListener {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("password", loginData.password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                context.getString(R.string.copy_password_message),
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }

        openWebsite.setOnClickListener {
            val url = loginData.website
            if (url != null) {
                openWebsite(url, context)
            }
            bottomSheetDialog.dismiss()
        }

        delete.setOnClickListener {
            val viewModel =
                ViewModelProvider(context as ViewModelStoreOwner).get(LoginViewModel::class.java)

            val alertDialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete))
                .setMessage(context.getString(R.string.are_you_sure_you_want_to_delete_this_login_data))
                .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                    viewModel.deleteLoginData(loginData)
                    Toast.makeText(
                        context,
                        context.getString(R.string.login_data_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    bottomSheetDialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }

        bottomSheetDialog.show()
    }

    fun setData(loginDataList: List<LoginData>) {
        this.loginDataList = loginDataList
        notifyDataSetChanged()
    }

}
