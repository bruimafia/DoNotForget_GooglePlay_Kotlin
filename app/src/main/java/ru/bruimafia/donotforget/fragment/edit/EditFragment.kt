package ru.bruimafia.donotforget.fragment.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.swnishan.materialdatetimepicker.datepicker.MaterialDatePickerDialog
import com.swnishan.materialdatetimepicker.datepicker.MaterialDatePickerView
import com.swnishan.materialdatetimepicker.timepicker.MaterialTimePickerDialog
import com.swnishan.materialdatetimepicker.timepicker.MaterialTimePickerView
import io.reactivex.disposables.CompositeDisposable
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.FragmentEditBinding
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants
import ru.bruimafia.donotforget.util.NoteAdapter
import ru.bruimafia.donotforget.util.SharedPreferencesManager

class EditFragment : Fragment(), OnClickMethod {

    private val args: EditFragmentArgs by navArgs()
    private val viewModel: EditViewModel by viewModels { EditViewModelFactory((App.instance).repository) }
    private lateinit var binding: FragmentEditBinding
    private lateinit var navController: NavController
    private var id: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = args.noteID
        Log.d(Constants.TAG, "args id = $id")
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

        viewModel.setNote(id)
        viewModel.isFullVersion.set(SharedPreferencesManager.isFullVersion)
    }

    override fun onCreateNote(note: Note) {
        viewModel.create(note)
        navController.popBackStack()
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
            .setColorSwatch(ColorSwatch._100)
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

}