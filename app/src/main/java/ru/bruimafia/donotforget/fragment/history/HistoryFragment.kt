package ru.bruimafia.donotforget.fragment.history

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.FragmentHistoryBinding
import ru.bruimafia.donotforget.fragment.edit.EditViewModelFactory
import ru.bruimafia.donotforget.fragment.tasks.OnItemClickListener
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.NoteAdapter
import ru.bruimafia.donotforget.util.SharedPreferencesManager
import java.util.concurrent.TimeUnit


class HistoryFragment : Fragment() {

    private val disposable = CompositeDisposable()
    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((App.instance).repository)
    }
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var navController: NavController
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        viewModel.isFullVersion.set(SharedPreferencesManager.isFullVersion)

        navController = findNavController(view)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        adapter = NoteAdapter(object : OnItemClickListener {
            override fun onItemClick(note: Note) {
                val action = HistoryFragmentDirections.actionHistoryFragmentToEditFragment(note.id)
                navController.navigate(action)
            }

            override fun onItemLongClick(note: Note): Boolean {
                showRecoverDialog(note.id)
                return true
            }
        })

        binding.recycler.layoutManager = LinearLayoutManager(view.context)
        binding.recycler.itemAnimator = DefaultItemAnimator()
        binding.recycler.adapter = adapter

        disposable.add(viewModel.getNotesInHistory()
            .subscribeOn(Schedulers.io())
            .delay(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { viewModel.isLoading.set(true) }
            .subscribe { notes: List<Note> ->
                adapter.setData(notes.toMutableList())
                viewModel.notes.set(notes)
                viewModel.isLoading.set(false)
            })
    }

    private fun showRecoverDialog(id: Long) {
        val dialog = Dialog(binding.root.context)
        dialog.setContentView(R.layout.dialog_alert)
        (dialog.findViewById<View>(R.id.text) as TextView).setText(R.string.question_restore)
        (dialog.findViewById<View>(R.id.btn_ok) as TextView).setText(R.string.restore)
        dialog.findViewById<View>(R.id.btn_ok).setOnClickListener {
            viewModel.recover(id)
            dialog.cancel()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.cancel() }
        dialog.show()
    }

}