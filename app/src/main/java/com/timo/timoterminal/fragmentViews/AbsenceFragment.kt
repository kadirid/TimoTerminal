package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.fragment.app.commit
import com.google.android.material.button.MaterialButton
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAbsenceBinding
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.AbsenceFragmentViewModel
import org.koin.android.ext.android.inject
import androidx.core.graphics.toColorInt

private const val ARG_USERID = "userId"
private const val ARG_EDITOR_ID = "editorId"

class AbsenceFragment : Fragment() {
    private var userId: Long? = null
    private var editorId: String? = null
    private var first: Boolean = true

    private lateinit var binding: FragmentAbsenceBinding

    private val viewModel: AbsenceFragmentViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
            editorId = it.getString(ARG_EDITOR_ID)
        }
        viewModel.userId = userId ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAbsenceBinding.inflate(inflater, container, false)

        binding.fragmentAbsenceRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Connect search field to view model
        binding.searchAbsenceEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.liveSearchQuery.postValue(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Observe favorites and update only the star icons in the already-rendered list
        viewModel.liveFavoriteAbsenceTypeIds.observe(viewLifecycleOwner) { favIds ->
            val count = binding.buttonContainer.childCount
            for (i in 0 until count) {
                val container = binding.buttonContainer.getChildAt(i) as? LinearLayout ?: continue
                val tagVal = container.tag ?: continue
                val atId: Long = when (tagVal) {
                    is Long -> tagVal
                    is Int -> tagVal.toLong()
                    is String -> tagVal.toLongOrNull() ?: continue
                    else -> continue
                }
                // find the favorite button by its specific tag
                val favBtn = container.findViewWithTag<MaterialButton>("favBtn_${atId}")
                favBtn?.setIconResource(if (favIds.contains(atId)) R.drawable.baseline_star_24 else R.drawable.baseline_star_empty_24)
            }
        }

        // Observe favorite entities and render them in the horizontal favorites bar
        viewModel.liveFavoriteAbsenceEntities.observe(viewLifecycleOwner) { favList ->
            // Control visibility of favorites section based on whether there are favorites
            val hasFavorites = !favList.isNullOrEmpty()
            binding.favoritesSection.visibility = if (hasFavorites) View.VISIBLE else View.GONE
            binding.favoritesScrollView.visibility = if (hasFavorites) View.VISIBLE else View.GONE
            binding.favoritesDivider.visibility = if (hasFavorites) View.VISIBLE else View.GONE

            // Clear existing favorites
            binding.favoritesContainer.removeAllViews()
            if (!hasFavorites) return@observe

            for (absenceType in favList) {
                // container for each favorite tile so gradient can be applied behind the button
                val favContainer = LinearLayout(requireContext())
                favContainer.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    56.dpToPx()
                ).apply {
                    rightMargin = 12.dpToPx()
                }
                favContainer.orientation = LinearLayout.VERTICAL
                favContainer.gravity = android.view.Gravity.CENTER_VERTICAL

                // gradient background for the tile (shorter, rounded)
                val baseColorInt = try { absenceType.color.toColorInt() } catch (_: IllegalArgumentException) { "#2C5AA0".toColorInt() }
                val fadedLeft = Color.argb(
                    (Color.alpha(baseColorInt) * 0.6f).toInt(),
                    Color.red(baseColorInt),
                    Color.green(baseColorInt),
                    Color.blue(baseColorInt)
                )
                val gradientDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12.dpToPx().toFloat()
                    colors = intArrayOf(fadedLeft, baseColorInt)
                    orientation = GradientDrawable.Orientation.LEFT_RIGHT
                }
                favContainer.background = gradientDrawable

                // short text button centered
                val favButton = MaterialButton(requireContext())
                favButton.text = absenceType.name
                favButton.textSize = 14f
                favButton.setPadding(20.dpToPx(), 0, 20.dpToPx(), 0)
                favButton.background = null
                favButton.minimumHeight = 0
                favButton.minimumWidth = 0
                favButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                // adapt text color
                val fontColor = Utils.adaptFontColorToBackground(baseColorInt)
                favButton.setTextColor(fontColor)

                favButton.setOnClickListener {
                    val frag = if (absenceType.startStop) {
                        AbsenceStartStopFragment.newInstance(
                            userId = userId ?: -1L,
                            absenceTypeId = absenceType.id
                        )
                    } else {
                        AbsenceFormFragment.newInstance(
                            userId = userId ?: -1L,
                            editorId = editorId ?: "",
                            absenceTypeId = absenceType.id
                        )
                    }
                    parentFragmentManager.commit {
                        addToBackStack(null)
                        replace(R.id.fragment_container_view, frag)
                    }
                }

                favContainer.addView(favButton)
                binding.favoritesContainer.addView(favContainer)
            }
        }

        observeLiveData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)?.hideLoadMask()

        viewModel.loadForAbsence()
    }

    private fun observeLiveData() {
        viewModel.liveHideMask.value = false
        viewModel.liveHideMask.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.hideLoadMask()
                viewModel.liveHideMask.value = false
            }
        }
        viewModel.liveShowMask.value = false
        viewModel.liveShowMask.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.showLoadMask()
                viewModel.liveShowMask.value = false
            }
        }
        viewModel.liveShowOfflineNotice.value = false
        viewModel.liveShowOfflineNotice.observe(viewLifecycleOwner) {
            if (it == true) {
                requireActivity().runOnUiThread {
                    Utils.showMessage(
                        parentFragmentManager,
                        "Hinweis\n\nDaten sind eventuell nicht vollständig"
                    )
                }
                viewModel.liveShowOfflineNotice.value = false
            }
        }
        // don't pre-clear liveAbsenceTypes here; let the ViewModel populate it
        viewModel.liveAbsenceTypes.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                first = false
                binding.buttonContainer.removeAllViews()
                val favoriteIds = viewModel.liveFavoriteAbsenceTypeIds.value ?: emptySet()
                for (absenceType in list) {
                    // Create container LinearLayout for button + icons
                    val container = LinearLayout(requireContext())
                    // tag the container with the absenceType id so favorite updates can find it later
                    container.tag = absenceType.id
                    container.layoutParams = LinearLayout.LayoutParams(
                        GridLayout.LayoutParams.MATCH_PARENT,
                        70.dpToPx()
                    ).apply {
                        bottomMargin = 12.dpToPx()
                    }
                    container.orientation = LinearLayout.HORIZONTAL
                    container.gravity = android.view.Gravity.CENTER_VERTICAL
                    // give the whole container horizontal padding so the gradient also shows behind icons
                    container.setPadding(12.dpToPx(), 0, 12.dpToPx(), 0)

                    // Create main button (text only) - make the button transparent so the container's background shows through
                    val button = MaterialButton(requireContext())
                    button.text = absenceType.name
                    // corner radius is handled by the container background gradient
                    button.textSize = 16f
                    button.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                    // keep left padding for text, right padding not necessary because icons sit on the right
                    button.setPadding(24.dpToPx(), 0, 0, 0)

                    // Parse base color and compute faded-left (60% opacity) and full-right colors
                    val baseColorInt = try {
                        absenceType.color.toColorInt()
                    } catch (_: IllegalArgumentException) {
                        // fallback color
                        "#2C5AA0".toColorInt()
                    }

                    val fadedLeft = Color.argb(
                        (Color.alpha(baseColorInt) * 0.6f).toInt(),
                        Color.red(baseColorInt),
                        Color.green(baseColorInt),
                        Color.blue(baseColorInt)
                    )

                    // Create gradient drawable and apply to the whole container so icons appear on the gradient too
                    val gradientDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 16.dpToPx().toFloat()
                        // left = faded (60% opacity), right = full color
                        colors = intArrayOf(fadedLeft, baseColorInt)
                        orientation = GradientDrawable.Orientation.LEFT_RIGHT
                    }
                    container.background = gradientDrawable

                    // Make the button background transparent so the container's gradient is visible behind it
                    button.background = null

                    // Decide font/icon color based on background luminance
                    val fontColor = Utils.adaptFontColorToBackground(baseColorInt)

                    // Apply font color (icons will be tinted after their creation)
                    button.setTextColor(fontColor)
                    button.setRippleColorResource(android.R.color.transparent)
                    button.elevation = 0f

                    val buttonParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                    button.layoutParams = buttonParams

                    button.setOnClickListener {
                        val frag = if (absenceType.startStop){
                            AbsenceStartStopFragment.newInstance(
                                userId = userId ?: -1L,
                                absenceTypeId = absenceType.id
                            )
                        } else {
                            AbsenceFormFragment.newInstance(
                                userId = userId ?: -1L,
                                editorId = editorId ?: "",
                                absenceTypeId = absenceType.id
                            )
                        }
                        parentFragmentManager.commit {
                            addToBackStack(null)
                            replace(R.id.fragment_container_view, frag)
                        }
                    }

                    // Create icon placeholder view
                    val iconPlaceholder = MaterialButton(requireContext())
                    iconPlaceholder.icon = null
                    iconPlaceholder.setIconResource(R.drawable.baseline_edit_24)
                    iconPlaceholder.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                    iconPlaceholder.iconPadding = 0
                    iconPlaceholder.text = ""
                    // iconTint will be applied based on computed fontColor below
                    iconPlaceholder.cornerRadius = 0
                    iconPlaceholder.elevation = 0f
                    iconPlaceholder.setRippleColorResource(android.R.color.transparent)
                    iconPlaceholder.insetTop = 0
                    iconPlaceholder.insetBottom = 0
                    iconPlaceholder.iconTint =  ColorStateList.valueOf(fontColor)
                    // Remove extra paddings/minimums so the view is truly 44x44 and the mask becomes perfectly circular
                    iconPlaceholder.minimumWidth = 0
                    iconPlaceholder.minimumHeight = 0
                    iconPlaceholder.setPadding(0, 0, 0, 0)

                    val iconParams = LinearLayout.LayoutParams(
                        44.dpToPx(),
                        44.dpToPx()
                    ).apply {
                        marginStart = 12.dpToPx()
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    iconPlaceholder.layoutParams = iconParams
                    iconPlaceholder.background = null

                    // Create favorite toggle button
                    val favouriteIsSet = favoriteIds.contains(absenceType.id)
                    val favoriteButton = MaterialButton(requireContext())
                    // tag the favorite button so it can be found later by the favorites observer
                    favoriteButton.tag = "favBtn_${absenceType.id}"
                    favoriteButton.icon = null
                    favoriteButton.setIconResource(
                        if (favouriteIsSet)
                            R.drawable.baseline_star_24
                        else
                            R.drawable.baseline_star_empty_24
                    )
                    favoriteButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                    favoriteButton.iconPadding = 0
                    favoriteButton.text = ""
                    favoriteButton.cornerRadius = 0
                    favoriteButton.elevation = 0f
                    favoriteButton.insetTop = 0
                    favoriteButton.insetBottom = 0
                    favoriteButton.iconTint =  ColorStateList.valueOf(fontColor)
                    favoriteButton.minimumWidth = 0
                    favoriteButton.minimumHeight = 0
                    favoriteButton.setPadding(0, 0, 0, 0)

                    val favoriteParams = LinearLayout.LayoutParams(
                        44.dpToPx(),
                        44.dpToPx()
                    ).apply {
                        marginStart = 12.dpToPx()
                        marginEnd = 12.dpToPx()
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    favoriteButton.layoutParams = favoriteParams
                    favoriteButton.background = null

                    favoriteButton.setOnClickListener {
                        val willBeMarked = ! (viewModel.liveFavoriteAbsenceTypeIds.value?.contains(absenceType.id) ?: false)
                        // Optimistic UI toggle: only change the icon of this button immediately
                        favoriteButton.setIconResource(if (willBeMarked) R.drawable.baseline_star_24 else R.drawable.baseline_star_empty_24)
                        // Persist change
                        viewModel.setMarkedAsFavorite(absenceType.id, willBeMarked)
                    }

                    // Apply icon tint using computed fontColor so icons/fonts adapt to background
                    iconPlaceholder.icon?.setTint(fontColor)
                    favoriteButton.icon?.setTint(fontColor)

                    // Create a subtle circular ripple using the font color (transparentized)
                    val rippleAlpha = 0x66 // ~40% alpha
                    val rippleColor = Color.argb(
                        rippleAlpha,
                        Color.red(fontColor),
                        Color.green(fontColor),
                        Color.blue(fontColor)
                    )
                    val mask = ShapeDrawable(OvalShape())
                    val rippleDrawable = RippleDrawable(ColorStateList.valueOf(rippleColor), null, mask)

                    // Set as foreground on newer APIs so the ripple overlays the gradient without replacing background
                    favoriteButton.foreground = rippleDrawable

                    // Add views to container
                    container.addView(button)
                    container.addView(iconPlaceholder)
                    container.addView(favoriteButton)

                    binding.buttonContainer.addView(container)
                }
                // do not clear liveAbsenceTypes here; keep the rendered list intact

                // optional: check for error entries and show UI later if needed
            }
        }
        viewModel.liveLatestOpen.value = null
        viewModel.liveLatestOpen.observe(viewLifecycleOwner) {
            if (it != null) {
                if (first) {
                    first = false
                    val frag = AbsenceStartStopFragment.newInstance(
                        userId = userId ?: -1L,
                        absenceTypeId = it.getLong("absenceTypeId"),
                        it
                    )
                    parentFragmentManager.commit {
                        addToBackStack(null)
                        replace(R.id.fragment_container_view, frag)
                    }
                } else {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun Int.dpToPx(): Int = Utils.dpToPx(this, resources.displayMetrics.density)

    companion object {
        @JvmStatic
        fun newInstance(userId: Long, editor: String) =
            AbsenceFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putString(ARG_EDITOR_ID, editor)
                }
            }
    }
}
