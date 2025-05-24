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

    override fun getItemCount(): Int = loginDataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionsImageView: ImageView = itemView.findViewById(R.id.options)
        private val websiteTextView: TextView = itemView.findViewById(R.id.websiteFromRoom)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameFromRoom)
        private val noteTextView: TextView = itemView.findViewById(R.id.noteFromRoom)
        private val progressBar: CircularProgressBar = itemView.findViewById(R.id.progressbar)
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImageView)

        // TotpTimerManager'ı burada başlattık
        private val totpTimerManager = TotpTimerManager(
            onTotpCodeGenerated = { code ->
                // Yeni bir TOTP kodu üretildiğinde bu kod çalışır.
                // usernameTextView'ı güncelleyerek kodu gösteririz.
                usernameTextView.text = code
            },
            onTimerTick = { progress ->
                // İlerleme çubuğu güncellendiğinde bu kod çalışır.
                // progressBar'ın ilerlemesini ayarlarız.
                progressBar.progress = progress
                progressBar.visibility = View.VISIBLE // İlerleme çubuğunu görünür yap
            },
            onTimerFinished = {
                // 30 saniyelik döngü tamamlandığında bu kod çalışır.
                // İsterseniz burada ek işlemler yapabilirsiniz, örneğin bir sonraki kodu hemen göstermek gibi.
                // TotpTimerManager zaten otomatik olarak bir sonraki döngüyü başlatacaktır.
            }
        )

        fun bind(loginData: LoginData) {
            // Eğer accountName boş değilse, bu bir Authenticator (TOTP) kaydıdır.
            if (!loginData.accountName.isNullOrEmpty()) {
                setupAccountView(loginData)
            } else {
                // Değilse, genel bir giriş verisi kaydıdır.
                setupGeneralView(loginData)
            }

            optionsImageView.setOnClickListener {
                showBottomSheetDialog(loginData, itemView.context)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = if (!loginData.accountName.isNullOrEmpty()) {
                    Intent(context, CreateAuthenticator::class.java) // Authenticator düzenleme ekranı
                } else {
                    Intent(context, CreateLoginData::class.java) // Giriş verisi düzenleme ekranı
                }
                intent.putExtra("loginId", loginData.id) // ID'yi intent'e ekle
                context.startActivity(intent)
            }

            itemView.findViewById<ImageView>(R.id.copyPassword).setOnClickListener {
                copyToClipboard(loginData) // Şifreyi/TOTP'yi kopyala
            }
        }

        // Authenticator (TOTP) kayıtları için görünüm ayarları
        private fun setupAccountView(loginData: LoginData) {
            websiteTextView.text = loginData.accountName // Hesap adını göster
            val accountInitials = loginData.accountName!!.first().uppercase() // Baş harfini al
            profileImage.setImageBitmap(
                BitmapUtils.generateInitialsBitmap(itemView.context, accountInitials, 24f, 60)
            )

            loginData.totpKey?.let {
                // TOTP anahtarı varsa, sayacı başlat
                totpTimerManager.startTotpTimer(it)
            } ?: run {
                // TOTP anahtarı yoksa, sayacı durdur ve ilgili UI öğelerini temizle
                totpTimerManager.stopTotpTimer()
                progressBar.visibility = View.GONE
                usernameTextView.text = "" // TOTP kodu yerine boş bırak
            }
        }

        // Genel giriş verisi kayıtları için görünüm ayarları
        private fun setupGeneralView(loginData: LoginData) {
            totpTimerManager.stopTotpTimer() // Bu bir TOTP kaydı olmadığı için sayacı durdur
            progressBar.visibility = View.GONE // İlerleme çubuğunu gizle
            usernameTextView.text = loginData.userName?.let { truncateString(it) } // Kullanıcı adını göster

            websiteTextView.text = loginData.website?.let { getDomainFromUrl(it) }?.let { truncateString(it) } // Web sitesi adını göster
            val initials = loginData.website?.first()?.uppercase() ?: "?" // Web sitesi baş harfini al
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

        // Şifreyi/TOTP kodunu panoya kopyalama
        private fun copyToClipboard(loginData: LoginData) {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = if (loginData.accountName == null) {
                // Eğer accountName boşsa şifreyi kopyala
                ClipData.newPlainText("password", loginData.password)
            } else {
                // Aksi halde TOTP kodunu kopyala
                ClipData.newPlainText("username", usernameTextView.text)
            }
            clipboard.setPrimaryClip(clip)
            val message = if (loginData.accountName == null) "Şifre Kopyalandı!" else "TOTP Kopyalandı!"
            Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
        }

        // URL'den alan adını alma
        private fun getDomainFromUrl(url: String): String {
            return try {
                val uri = URL(url)
                val domain = uri.host.removePrefix("www.") // "www." kısmını kaldır
                "${uri.protocol}://$domain" // Protokol ile birlikte alanı döndür
            } catch (e: MalformedURLException) {
                url // Hata olursa orijinal URL'yi döndür
            }
        }

        // Metni kısaltma (belirli bir karakterden sonra "...")
        private fun truncateString(text: String): String {
            return if (text.length > 20) text.take(20) + "..." else text
        }

        // Web sitesini tarayıcıda açma
        private fun openWebsite(url: String, context: Context) {
            try {
                val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "http://$url" // Eğer protokol yoksa "http://" ekle
                } else url
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Web sitesi açılamadı", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Sheet iletişim kutusunu gösterme
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

            // Eğer bir Authenticator kaydıysa (accountName doluysa) bazı seçenekleri gizle
            if (!loginData.accountName.isNullOrEmpty()) {
                websiteName.text = loginData.accountName
                copyEmail.visibility = View.GONE
                copyUsername.visibility = View.GONE
                copyPassword.visibility = View.GONE
                openWebsite.visibility = View.GONE
            } else {
                websiteName.text = loginData.website
            }

            // Kopyalama butonları
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

            // Web sitesini açma
            openWebsite.setOnClickListener {
                loginData.website?.let { openWebsite(it, context) }
                dialog.dismiss()
            }

            // Silme işlemi
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

        // Metni panoya kopyalama yardımcı fonksiyonu
        private fun copyTextToClipboard(context: Context, label: String, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        }
    }

    fun setData(newLoginDataList: List<LoginData>) {
        val diffCallback = LoginDataDiffCallback(this.loginDataList, newLoginDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.loginDataList = newLoginDataList // Eski listeyi yeni liste ile değiştir
        diffResult.dispatchUpdatesTo(this) // Sadece değişen öğeleri RecyclerView'a bildir
    }
}