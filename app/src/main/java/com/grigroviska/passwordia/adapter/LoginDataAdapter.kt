package com.grigroviska.passwordia.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.grigroviska.passwordia.LoginDataDiffCallback
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.activities.CreateAuthenticator
import com.grigroviska.passwordia.activities.CreateLoginData
import com.grigroviska.passwordia.activities.TotpTimerManager
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import de.hdodenhof.circleimageview.CircleImageView
import java.net.MalformedURLException
import java.net.URL
import androidx.core.net.toUri

class LoginDataAdapter(
    private var loginDataList: List<LoginData>,
    private val onFavoriteClick: (LoginData) -> Unit
) : RecyclerView.Adapter<LoginDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_login_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loginData = loginDataList[position]
        holder.bind(loginData)
    }

    override fun getItemCount(): Int = loginDataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionsImageView: ImageView = itemView.findViewById(R.id.options)
        private val websiteTextView: TextView = itemView.findViewById(R.id.websiteFromRoom)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameFromRoom)
        private val noteTextView: TextView = itemView.findViewById(R.id.noteFromRoom)
        private val progressBar: CircularProgressBar = itemView.findViewById(R.id.progressbar)
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImageView)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)

        private val totpTimerManager = TotpTimerManager(
            onTotpCodeGenerated = { code ->
                usernameTextView.text = code
            },
            onTimerTick = { progress ->
                progressBar.progress = progress
                progressBar.visibility = View.VISIBLE
            },
            onTimerFinished = {

            }
        )

        fun bind(loginData: LoginData) {
            if (!loginData.accountName.isNullOrEmpty()) {
                setupAccountView(loginData)
            } else {
                setupGeneralView(loginData)
            }

            optionsImageView.setOnClickListener {
                showBottomSheetDialog(loginData, itemView.context)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = if (!loginData.accountName.isNullOrEmpty()) {
                    Intent(context, CreateAuthenticator::class.java)
                } else {
                    Intent(context, CreateLoginData::class.java)
                }
                intent.putExtra("loginId", loginData.id)
                context.startActivity(intent)
            }

            itemView.findViewById<ImageView>(R.id.copyPassword).setOnClickListener {
                copyToClipboard(loginData)
            }

            favoriteIcon.setOnClickListener {
                loginData.isFavorite = !loginData.isFavorite
                updateFavoriteIcon(loginData.isFavorite)
                onFavoriteClick.invoke(loginData)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                favoriteIcon.setImageResource(R.drawable.favorited)
            } else {
                favoriteIcon.setImageResource(R.drawable.favorite)
            }
        }

        private fun setupAccountView(loginData: LoginData) {
            websiteTextView.text = loginData.accountName
            val accountInitials = loginData.accountName!!.first().uppercase()
            profileImage.setImageBitmap(
                BitmapUtils.generateInitialsBitmap(itemView.context, accountInitials, 24f, 60)
            )

            loginData.totpKey?.let {
                totpTimerManager.startTotpTimer(it)
            } ?: run {
                totpTimerManager.stopTotpTimer()
                progressBar.visibility = View.GONE
                usernameTextView.text = ""
            }
        }

        private fun setupGeneralView(loginData: LoginData) {
            totpTimerManager.stopTotpTimer()
            progressBar.visibility = View.GONE
            usernameTextView.text = loginData.userName?.let { truncateString(it) }

            websiteTextView.text = loginData.website?.let { getDomainFromUrl(it) }?.let { truncateString(it) } // Web sitesi adını göster
            val initials = loginData.website?.first()?.uppercase() ?: "?"
            profileImage.setImageBitmap(
                BitmapUtils.generateInitialsBitmap(itemView.context, initials, 24f, 60)
            )
            if (!loginData.notes.isNullOrEmpty()) {
                noteTextView.visibility = View.VISIBLE
                noteTextView.text = "Not: ${loginData.notes}" // Not varsa göster
            } else {
                noteTextView.visibility = View.GONE // Not yoksa gizle
            }
        }

        private fun copyToClipboard(loginData: LoginData) {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = if (loginData.accountName == null) {
                ClipData.newPlainText("password", loginData.password)
            } else {
                ClipData.newPlainText("username", usernameTextView.text)
            }
            clipboard.setPrimaryClip(clip)
            val message = if (loginData.accountName == null) "Şifre Kopyalandı!" else "TOTP Kopyalandı!"
            Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
        }

        private fun getDomainFromUrl(url: String): String {
            return try {
                val uri = URL(url)
                val domain = uri.host.removePrefix("www.")
                "${uri.protocol}://$domain"
            } catch (e: MalformedURLException) {
                url
            }
        }

        private fun truncateString(text: String): String {
            return if (text.length > 20) text.take(20) + "..." else text
        }

        private fun openWebsite(url: String, context: Context) {
            try {
                val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "http://$url"
                } else url
                val intent = Intent(Intent.ACTION_VIEW, validUrl.toUri())
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Web sitesi açılamadı", Toast.LENGTH_SHORT).show()
            }
        }

        @SuppressLint("MissingInflatedId")
        private fun showBottomSheetDialog(loginData: LoginData, context: Context) {
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.options_bottom_sheet, null)
            dialog.setContentView(view)

            val websiteName = view.findViewById<TextView>(R.id.websiteName)
            val copyEmail = view.findViewById<LinearLayout>(R.id.copyEmailLayout)
            val copyUsername = view.findViewById<LinearLayout>(R.id.copyUserNameLayout)
            val copyPassword = view.findViewById<LinearLayout>(R.id.copyPasswordLayout)
            val openWebsite = view.findViewById<LinearLayout>(R.id.openWebsiteLayout)
            val delete = view.findViewById<LinearLayout>(R.id.deleteLayout)
            val favoriteToggle = view.findViewById<TextView>(R.id.favoriteText)

            if (loginData.isFavorite) {
                favoriteToggle.text = context.getString(R.string.remove_from_favorites)
            } else {
                favoriteToggle.text = context.getString(R.string.add_to_favorites)
            }

            if (!loginData.accountName.isNullOrEmpty()) {
                websiteName.text = loginData.accountName
                copyEmail.visibility = View.GONE
                copyUsername.visibility = View.GONE
                copyPassword.visibility = View.GONE
                openWebsite.visibility = View.GONE
            } else {
                websiteName.text = loginData.website
            }

            copyEmail.setOnClickListener {
                copyTextToClipboard(context, "email", loginData.userName ?: "")
                Toast.makeText(context, context.getString(R.string.email_copied), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            copyUsername.setOnClickListener {
                copyTextToClipboard(context, "username", loginData.userName ?: "")
                Toast.makeText(context, context.getString(R.string.username_copied), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            copyPassword.setOnClickListener {
                copyTextToClipboard(context, "password", loginData.password ?: "")
                Toast.makeText(context, context.getString(R.string.copy_password_message), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            openWebsite.setOnClickListener {
                loginData.website?.let { openWebsite(it, context) }
                dialog.dismiss()
            }

            favoriteToggle.setOnClickListener {
                loginData.isFavorite = !loginData.isFavorite
                updateFavoriteIcon(loginData.isFavorite)
                onFavoriteClick.invoke(loginData)
                dialog.dismiss()
            }

            delete.setOnClickListener {
                val viewModel = ViewModelProvider(context as ViewModelStoreOwner)[LoginViewModel::class.java]
                AlertDialog.Builder(context)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.are_you_sure_you_want_to_delete_this_login_data)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        viewModel.deleteLoginData(loginData)
                        Toast.makeText(context, R.string.login_data_deleted, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.no) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
            }

            dialog.show()
        }

        private fun copyTextToClipboard(context: Context, label: String, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        }
    }

    fun setData(newLoginDataList: List<LoginData>) {
        val diffCallback = LoginDataDiffCallback(this.loginDataList, newLoginDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.loginDataList = newLoginDataList
        diffResult.dispatchUpdatesTo(this)
    }
}