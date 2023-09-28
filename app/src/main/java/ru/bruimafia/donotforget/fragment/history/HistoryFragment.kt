package ru.bruimafia.donotforget.fragment.history

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.FragmentHistoryBinding
import ru.bruimafia.donotforget.fragment.tasks.OnItemClickListener
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.repository.remote.FirebaseManager
import ru.bruimafia.donotforget.util.NoteAdapter
import ru.bruimafia.donotforget.util.SharedPreferencesManager
import kotlin.math.roundToInt


class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((App.instance).repository)
    }
    private lateinit var binding: FragmentHistoryBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
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

        viewModel.getNotesInHistory().observe(viewLifecycleOwner) {
            viewModel.isLoading.set(true)
            adapter.setData(it.toMutableList())
            viewModel.notes.set(it)
            viewModel.isLoading.set(false)
        }

        binding.banner.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.banner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                bannerAd = loadBannerAd(adSize)
            }
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

    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
        return binding.banner.apply {
            setAdSize(adSize)
            setAdUnitId(resources.getString(R.string.ads_yandex_banner_history_id))
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