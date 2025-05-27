import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.viewModel.BackupDialogViewModel
import com.grigroviska.passwordia.viewModel.BackupRestoreState

class BackupDialog : DialogFragment() {

    private lateinit var viewModel: BackupDialogViewModel
    private lateinit var backupPasswordEditText: EditText
    private lateinit var exportButton: Button
    private lateinit var importButton: Button

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            uri?.let {
                val password = backupPasswordEditText.text.toString()
                if (password.isNotEmpty()) {
                    viewModel.exportData(password, it)
                } else {
                    Toast.makeText(context, "Password cannot be empty for export.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val password = backupPasswordEditText.text.toString()
                if (password.isNotEmpty()) {
                    viewModel.importData(password, it)
                } else {
                    Toast.makeText(context, "Password cannot be empty for import.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.back_up, container, false)

        backupPasswordEditText = view.findViewById(R.id.backupPassword)
        exportButton = view.findViewById(R.id.exportButton)
        importButton = view.findViewById(R.id.importButton)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(BackupDialogViewModel::class.java)

        exportButton.setOnClickListener {
            val password = backupPasswordEditText.text.toString()
            if (password.isNotEmpty()) {
                createFileLauncher.launch("passwordia_backup.ntw")
            } else {
                backupPasswordEditText.error = "Password is required"
                Toast.makeText(context, "Please enter a password for export.", Toast.LENGTH_SHORT).show()
            }
        }

        importButton.setOnClickListener {
            val password = backupPasswordEditText.text.toString()
            if (password.isNotEmpty()) {
                openFileLauncher.launch(arrayOf("application/octet-stream")) // Filter for your backup file type
            } else {
                backupPasswordEditText.error = "Password is required"
                Toast.makeText(context, "Please enter a password for import.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.backupState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BackupRestoreState.Loading -> {
                    exportButton.isEnabled = false
                    importButton.isEnabled = false
                    Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()
                }
                is BackupRestoreState.Success -> {
                    exportButton.isEnabled = true
                    importButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()

                }
                is BackupRestoreState.Error -> {
                    exportButton.isEnabled = true
                    importButton.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is BackupRestoreState.Idle -> {
                    exportButton.isEnabled = true
                    importButton.isEnabled = true
                }
            }
        }

        viewModel.restoreState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BackupRestoreState.Loading -> {
                    exportButton.isEnabled = false
                    importButton.isEnabled = false
                    Toast.makeText(context, "Restoring...", Toast.LENGTH_SHORT).show()
                }
                is BackupRestoreState.Success -> {
                    exportButton.isEnabled = true
                    importButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is BackupRestoreState.Error -> {
                    exportButton.isEnabled = true
                    importButton.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is BackupRestoreState.Idle -> {
                    exportButton.isEnabled = true
                    importButton.isEnabled = true
                }
            }
        }
    }
}