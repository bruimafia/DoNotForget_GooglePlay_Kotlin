package ru.bruimafia.donotforget.fragment.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.swnishan.materialdatetimepicker.datepicker.MaterialDatePickerDialog
import com.swnishan.materialdatetimepicker.datepicker.MaterialDatePickerView
import com.swnishan.materialdatetimepicker.timepicker.MaterialTimePickerDialog
import com.swnishan.materialdatetimepicker.timepicker.MaterialTimePickerView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.FragmentEditBinding
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants
import ru.bruimafia.donotforget.util.SharedPreferencesManager
import kotlin.math.roundToInt


class EditFragment : Fragment(), OnClickMethod {

    private val args: EditFragmentArgs by navArgs()
    private val viewModel: EditViewModel by viewModels { EditViewModelFactory((App.instance).repository) }
    private lateinit var binding: FragmentEditBinding
    private lateinit var navController: NavController
    private var id: Long = -1L

    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null
    private var bannerAd: BannerAdView? = null
    private val adSize: BannerAdSize
        get() {
            val screenHeight = resources.displayMetrics.run { heightPixels / density }.roundToInt()
            var adWidthPixels = binding.banner.width
            if (adWidthPixels == 0) {
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()
            val maxAdHeight = screenHeight / 2

            return BannerAdSize.inlineSize(requireActivity(), adWidth, maxAdHeight)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = args.noteID
        Log.d(Constants.TAG, "From EditFragment::class -> args id = $id")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(requireView(), savedInstanceState)

        navController = findNavController(view)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        binding.view = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.setNote(id)
        viewModel.isFullVersion.set(SharedPreferencesManager.isFullVersion)
        Log.d(Constants.TAG, "From EditFragment::class -> note color = ${viewModel.note.get()?.color}")

        binding.banner.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.banner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                bannerAd = loadBannerAd(adSize)
            }
        })

        interstitialAdLoader = InterstitialAdLoader(requireActivity()).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    this@EditFragment.interstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {}
            })
        }

        loadInterstitialAd()
    }

    override fun onCreateNote(note: Note) {
        viewModel.create(note)
        navController.popBackStack()
        showAd()
    }

    override fun onUpdateNote(note: Note) {
        viewModel.update(note)
        navController.popBackStack()
    }

    override fun onDeleteNote(id: Long) {
        viewModel.delete(id)
        navController.popBackStack()
    }

    override fun onRecoverNote(id: Long) {
        viewModel.recover(id)
        navController.popBackStack()
    }

    override fun onChooseColor() {
        MaterialColorPickerDialog.Builder(requireContext())
            .setTitle(getString(R.string.color_choose))
            .setColorShape(ColorShape.SQAURE)
            .setColorRes(resources.getIntArray(R.array.colorPresets))
            .setTickColorPerCard(true)
            .setColorListener { color, _ -> viewModel.note.get()!!.color = color }
            .show()
    }

    override fun onChooseDate() {
        val builder = MaterialDatePickerDialog.Builder
            .setTitle(getString(R.string.date_choose))
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.ok))
            .setDateFormat(MaterialDatePickerView.DateFormat.DD_MMMM_YYYY)
            .setFadeAnimation(350L, 1050L, .3f, .7f)
            .setTheme(R.style.ThemeOverlay_Dialog_DatePicker)
            .build()

        builder.setOnDatePickListener { selectedDate -> viewModel.setDate(selectedDate) }
        builder.show(childFragmentManager, MaterialDatePickerDialog::class.simpleName)
    }

    override fun onChooseTime() {
        val builder = MaterialTimePickerDialog.Builder
            .setTitle(getString(R.string.time_choose))
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.ok))
            .setTimeConvention(MaterialTimePickerView.TimeConvention.HOURS_24)
            .setFadeAnimation(350L, 1050L, .3f, .7f)
            .setTheme(R.style.ThemeOverlay_Dialog_TimePicker)
            .build()

        builder.setOnTimePickListener { selectedTime -> viewModel.setTime(selectedTime) }
        builder.show(childFragmentManager, MaterialTimePickerDialog::class.simpleName)
    }

    private fun loadInterstitialAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder(resources.getString(R.string.ads_yandex_interstitialAd_unitId)).build()
        interstitialAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun showAd() {
        interstitialAd?.apply {
            setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}
                override fun onAdFailedToShow(adError: AdError) {}

                override fun onAdDismissed() {
                    interstitialAd?.setAdEventListener(null)
                    interstitialAd = null
                    loadInterstitialAd()
                }

                override fun onAdClicked() {}
                override fun onAdImpression(impressionData: ImpressionData?) {}
            })
            show(requireActivity())
        }
    }

    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
        return binding.banner.apply {
            setAdSize(adSize)
            setAdUnitId(resources.getString(R.string.ads_yandex_banner_edit_id))
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

    private fun destroyInterstitialAd() {
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAdLoader?.setAdLoadListener(null)
        interstitialAdLoader = null
        destroyInterstitialAd()
    }

}