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
import com.grigroviska.passwordia.TOTPGenerator
import com.grigroviska.passwordia.activities.CreateAuthenticator
import com.grigroviska.passwordia.activities.CreateLoginData
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URL
import java.util.Timer

class LoginDataAdapter(
    private var loginDataList: List<LoginData>
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

    override fun getItemCount(): Int {
        return loginDataList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionsImageView: ImageView = itemView.findViewById(R.id.options)
        private val websiteTextView: TextView = itemView.findViewById(R.id.websiteFromRoom)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameFromRoom)
        private val noteTextView: TextView = itemView.findViewById(R.id.noteFromRoom)
        private val progressBar: CircularProgressBar = itemView.findViewById(R.id.progressbar)
        private var totpJob: Job? = null
        private val totpGenerator: TOTPGenerator = TOTPGenerator()
        private var timer: Timer? = null


        fun bind(loginData: LoginData) {
            if (loginData.accountName != null && loginData.accountName != "") {
                websiteTextView.text = loginData.accountName
                val accountInitials = loginData.accountName.substring(0, 1).toUpperCase()
                val profileImage: CircleImageView = itemView.findViewById(R.id.profileImageView)
                profileImage.setImageBitmap(
                    BitmapUtils.generateInitialsBitmap(itemView.context, accountInitials, 24f, 60)
                )

                startTOTPTimer(loginData.totpKey)
            } else {
                progressBar.visibility = View.GONE
                websiteTextView.text = loginData.website?.let { getDomainFromUrl(it) }
                    ?.let { truncateString(it) }
                usernameTextView.text = loginData.userName?.let { truncateString(it) }
                val websiteInitials = loginData.website?.substring(0, 1)?.toUpperCase()
                val profileImage: CircleImageView = itemView.findViewById(R.id.profileImageView)
                profileImage.setImageBitmap(
                    BitmapUtils.generateInitialsBitmap(itemView.context, websiteInitials!!, 24f, 60)
                )
                if (loginData.notes != null && loginData.notes != "") {
                    noteTextView.visibility = View.VISIBLE
                    noteTextView.text = "Note : " + loginData.notes
                } else {
                    noteTextView.visibility = View.GONE
                }
            }

            optionsImageView.setOnClickListener {
                showBottomSheetDialog(loginData, itemView.context, itemView)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = if (loginData.accountName != null) {
                    Intent(context, CreateAuthenticator::class.java)
                } else {
                    Intent(context, CreateLoginData::class.java)
                }
                intent.putExtra("loginId", loginData.id)
                context.startActivity(intent)
            }

            itemView.findViewById<ImageView>(R.id.copyPassword).setOnClickListener {
                if (loginData.accountName == null){
                    val clipboard =
                        itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("password", loginData.password)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(itemView.context, "Copy Password!", Toast.LENGTH_SHORT).show()
                }else{

                    val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("username", usernameTextView.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(itemView.context, "Copy Totp!", Toast.LENGTH_SHORT).show()

                }
            }
        }

        private fun startTOTPTimer(totpKey: String?) {
            totpJob?.cancel()
            val progressbar: CircularProgressBar = itemView.findViewById(R.id.progressbar)
            progressbar.visibility = View.VISIBLE

            totpJob = CoroutineScope(Dispatchers.Main).launch {
                var progress = 0
                while (isActive) {
                    val totpCode = totpGenerator.generateTOTP(totpKey!!)

                    withContext(Dispatchers.Main) {
                        if (progress == 0) {
                            usernameTextView.text = totpCode
                        }

                        progress += 1
                        if (progress <= 30) {
                            progressbar.setProgressWithAnimation(progress.toFloat())
                        } else {
                            progress = 0
                            progressbar.setProgressWithAnimation(progress.toFloat())
                        }
                    }

                    delay(1000)
                }
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
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "http://$url"
            } else {
                url
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBottomSheetDialog(loginData: LoginData, context: Context, itemView: View) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.options_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val websiteName = view.findViewById<TextView>(R.id.websiteName)
        val copyEmail = view.findViewById<LinearLayout>(R.id.copyEmailLayout)
        val copyUsername = view.findViewById<LinearLayout>(R.id.copyUserNameLayout)
        val copyPassword = view.findViewById<LinearLayout>(R.id.copyPasswordLayout)
        val openWebsite = view.findViewById<LinearLayout>(R.id.openWebsiteLayout)
        val delete = view.findViewById<LinearLayout>(R.id.deleteLayout)

        if(loginData.accountName != null){

            websiteName.text = loginData.accountName
            copyEmail.visibility = View.GONE
            copyUsername.visibility = View.GONE
            copyPassword.visibility = View.GONE
            openWebsite.visibility = View.GONE


        }else{

            websiteName.text = loginData.website

        }

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
            if(loginData.accountName == null){
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
            else {

            }

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
