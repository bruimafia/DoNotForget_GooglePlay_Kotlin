package ru.bruimafia.donotforget.dialog.sync

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.DialogSyncBinding
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.repository.remote.FirebaseManager
import ru.bruimafia.donotforget.util.Constants
import ru.bruimafia.donotforget.util.SharedPreferencesManager


class SyncDialog : DialogFragment(), OnClickMethod {

    private lateinit var binding: DialogSyncBinding
    var isLogin = ObservableField(false)
    var lastSync = ObservableField(0L)

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private var job: Job = Job()
    private var notesRemote: List<Note> = listOf()
    private var notesLocal: List<Note> = listOf()
    private var totalList: MutableList<Note> = mutableListOf()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(Constants.TAG, "From SyncDialog::class -> true " + result.resultCode.toString())
            handleResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))
        } else
            Log.d(Constants.TAG, "From SyncDialog::class -> false " + result.resultCode.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_sync, container, false)
        binding.view = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.gso_request_id_token))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            isLogin.set(true)
            FirebaseManager.init()
            Log.d(Constants.TAG, "From SyncDialog::class -> already isLogin ${auth.currentUser!!.uid}")
        }

        lastSync.set(SharedPreferencesManager.lastSync)
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                isLogin.set(true)
                Log.d(Constants.TAG, "From SyncDialog::class handleResult() -> isLogin")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(Constants.TAG, "From SyncDialog::class handleResult() -> isSuccessful")
                        FirebaseManager.init()
                    }
                }
            }
        }
    }

    override fun onSignIn() {
        launcher.launch(googleSignInClient.signInIntent)
    }

    override fun onSync() {
        binding.progress.startAnimation()

        CoroutineScope(Dispatchers.Main + job).launch {
            notesRemote = FirebaseManager.getAll()
            delay(5000)
            notesLocal = App.instance.repository.getAll().first()
            //printInfoList()
            comparison()
            //printInfoFinishList()
            updateRepositories()
            delay(1000)
            val time = System.currentTimeMillis()
            SharedPreferencesManager.lastSync = time
            lastSync.set(time)
            binding.progress.revertAnimation()
        }
    }

    private fun printInfoList() {
        for (note in notesRemote)
            Log.d(Constants.TAG, "From SyncDialog::class Compare -> remote = $note")
        for (note in notesLocal)
            Log.d(Constants.TAG, "From SyncDialog::class Compare -> local = $note")
    }

    private fun printInfoFinishList() {
        Log.d(Constants.TAG, "From SyncDialog::class Compare -> ________________________________")
        Log.d(Constants.TAG, "From SyncDialog::class Compare -> total_list size = ${totalList.size}")
        for (note in totalList)
            Log.d(Constants.TAG, "From SyncDialog::class Compare -> total_list = $note")
    }

    private fun comparison() {
        totalList.addAll(notesRemote)

        for (localeNote in notesLocal) {
            Log.d(Constants.TAG, "From SyncDialog::class Compare -> total_list.contains(localeNote id = ${localeNote.id}) = ${totalList.contains(localeNote)}")
            if (!totalList.contains(localeNote))
                implantation(localeNote)
        }
    }

    private fun implantation(localeNote: Note) {
        val list: MutableList<Note> = totalList
        var similarId = false
        var note = Note()

        for (element in list) {
            if (localeNote.id == element.id) {
                similarId = true
                note = element
            }
        }

        if (!similarId) {
            Log.d(Constants.TAG, "From SyncDialog::class Compare -> id != id")
            totalList.add(localeNote)
        }

        if (similarId) {
            Log.d(Constants.TAG, "From SyncDialog::class Compare -> id == id")
            if (localeNote.dateUpdate > note.dateUpdate) {
                totalList[totalList.indexOf(note)] = localeNote
            }
        }
    }

    private suspend fun updateRepositories() {
        for (note in totalList) {
            App.instance.repository.create(note)
            FirebaseManager.createOrUpdate(note)
        }
        Log.d(Constants.TAG, "From SyncDialog::class Compare -> ____________________________")
        Log.d(Constants.TAG, "From SyncDialog::class Compare -> synchronization finished")
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

}