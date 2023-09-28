package ru.bruimafia.donotforget.fragment.tasks

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.FragmentTasksBinding
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants
import ru.bruimafia.donotforget.util.NoteAdapter
import ru.bruimafia.donotforget.util.SharedPreferencesManager
import kotlin.math.roundToInt


class TasksFragment : Fragment(), OnClickOptionsMenu {

    private val viewModel: TasksViewModel by viewModels {
        TasksViewModelFactory((App.instance).repository)
    }
    private lateinit var binding: FragmentTasksBinding
    private lateinit var navController: NavController
    private lateinit var adapter: NoteAdapter

    private var bannerAd: BannerAdView? = null
    private val adSize: BannerAdSize
        get() {
            var adWidthPixels = binding.banner.width
            if (adWidthPixels == 0) {
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()

            return BannerAdSize.stickySize(requireActivity(), adWidth)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasks, container, false)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding.view = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        navController = findNavController(view)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        if (requireActivity().intent != null) {
            if (requireActivity().intent.getLongExtra(Constants.NOTE_ID, -1L) != -1L) {
                val id = requireActivity().intent.getLongExtra(Constants.NOTE_ID, -1L)
                val action = TasksFragmentDirections.actionTasksFragmentToEditFragment(id)
                navController.navigate(action)
                requireActivity().intent.removeExtra(Constants.NOTE_ID)
            }
        }

        adapter = NoteAdapter(object : OnItemClickListener {
            override fun onItemClick(note: Note) {
                val action = TasksFragmentDirections.actionTasksFragmentToEditFragment(note.id)
                navController.navigate(action)
            }

            override fun onItemLongClick(note: Note): Boolean {
                showDeleteDialog(note.id)
                return true
            }
        })
        binding.recycler.layoutManager = LinearLayoutManager(view.context)
        binding.recycler.itemAnimator = DefaultItemAnimator()
        binding.recycler.adapter = adapter

        viewModel.isOrderById.set(SharedPreferencesManager.isOrderById)

        viewModel.getNotes().observe(viewLifecycleOwner) {
            viewModel.isLoading.set(true)
            adapter.setData(it.toMutableList())
            viewModel.notesForScreen.set(it)
            viewModel.isLoading.set(false)
        }

        showFirstLaunch()

        binding.banner.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.banner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                bannerAd = loadBannerAd(adSize)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)

        if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_YES)
            menu.findItem(R.id.onChangeTheme).setTitle(R.string.light_theme)
        else
            menu.findItem(R.id.onChangeTheme).setTitle(R.string.dark_theme)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.onSort -> {
                onChangeSort()
                return true
            }

            R.id.onChangeTheme -> {
                onChangeTheme()
                return true
            }

            //R.id.onBuy -> return true
        }

        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog(id: Long) {
        val dialog = Dialog(binding.root.context)
        dialog.setContentView(R.layout.dialog_alert)
        (dialog.findViewById<View>(R.id.text) as TextView).setText(R.string.question_delete)
        (dialog.findViewById<View>(R.id.btn_ok) as TextView).setText(R.string.delete)
        dialog.findViewById<View>(R.id.btn_ok).setOnClickListener {
            viewModel.delete(id)
            dialog.cancel()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.cancel() }
        dialog.show()
    }

    private fun showFirstLaunch() {
        if (SharedPreferencesManager.isFirstLaunch) {
            val dialog = Dialog(binding.root.context)
            dialog.setContentView(R.layout.dialog_first_launch)
            dialog.findViewById<View>(R.id.btn_ok).setOnClickListener {
                SharedPreferencesManager.isFirstLaunch = false
                dialog.cancel()
            }
            dialog.show()
        }
    }

    private fun notificationAboutOrder(orderById: Boolean) {
        if (orderById)
            Snackbar.make(binding.root, getString(R.string.snackbar_order_by_id), BaseTransientBottomBar.LENGTH_LONG).show() else Snackbar.make(
            binding.root,
            getString(R.string.snackbar_order_by_relevance),
            BaseTransientBottomBar.LENGTH_LONG
        ).show()
    }

    override fun onChangeSort() {
        val orderById: Boolean = SharedPreferencesManager.isOrderById
        SharedPreferencesManager.isOrderById = !orderById
        viewModel.isOrderById.set(!orderById)

        viewModel.getNotes().observe(viewLifecycleOwner) {
            viewModel.isLoading.set(true)
            adapter.setData(it.toMutableList())
            viewModel.notesForScreen.set(it)
            viewModel.isLoading.set(false)
        }

        notificationAboutOrder(!orderById)
    }

    override fun onChangeTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SharedPreferencesManager.isLightTheme = !SharedPreferencesManager.isLightTheme
            App.instance.checkCurrentTheme()
        } else
            Snackbar.make(binding.root, getString(R.string.snackbar_no_change_theme), BaseTransientBottomBar.LENGTH_SHORT).show()
    }

    override fun onCreateNote() {
        navController.navigate(R.id.action_tasksFragment_to_editFragment)
    }

    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
        return binding.banner.apply {
            setAdSize(adSize)
            setAdUnitId(resources.getString(R.string.ads_yandex_banner_tasks_id))
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {}
                override fun onAdFailedToLoad(adRequestError: AdRequestError) {}
                override fun onAdClicked() {}
                override fun onLeftApplication() {}
                override fun onReturnedToApplication() {}
                override fun onImpression(impressionData: ImpressionData?) {}
            })
            loadAd(AdRequest.Builder().build())
        }
    }

}