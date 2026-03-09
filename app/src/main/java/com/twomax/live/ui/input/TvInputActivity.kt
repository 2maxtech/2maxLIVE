package com.twomax.live.ui.input

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*

/**
 * Dedicated input activity for Android TV / Google TV.
 *
 * Launched via ActivityResultLauncher when a form field is selected.
 * A new Activity window is the only reliable way to show GBoard on TV —
 * we set SOFT_INPUT_STATE_ALWAYS_VISIBLE both in the manifest and
 * programmatically, and call toggleSoftInput after a short delay.
 */
class TvInputActivity : Activity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_VALUE = "value"
        const val EXTRA_IS_PASSWORD = "is_password"
        const val EXTRA_HINT = "hint"
        const val RESULT_VALUE = "result_value"
    }

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Reinforce soft-input mode programmatically in addition to the manifest flag
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val value = intent.getStringExtra(EXTRA_VALUE) ?: ""
        val isPassword = intent.getBooleanExtra(EXTRA_IS_PASSWORD, false)
        val hint = intent.getStringExtra(EXTRA_HINT) ?: ""

        val cardColor  = Color.parseColor("#1E1E3A")
        val accent     = Color.parseColor("#7C4DFF")
        val textColor  = Color.parseColor("#EEEEFF")
        val hintColor  = Color.parseColor("#555577")
        val inputBg    = Color.parseColor("#0D0D1A")

        // Full-screen semi-transparent scrim
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.argb(200, 0, 0, 10))
        }

        // Card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cardColor)
            setPadding(56, 48, 56, 48)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Title label
        card.addView(TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(textColor)
            setPadding(0, 0, 0, 20)
        })

        // EditText — native view, best IME support on TV
        editText = EditText(this).apply {
            setText(value)
            setSelection(value.length)
            setTextColor(textColor)
            setHintTextColor(hintColor)
            this.hint = hint
            textSize = 17f
            setPadding(28, 20, 28, 20)
            setBackgroundColor(inputBg)
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_DONE

            if (isPassword) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    confirm(text.toString()); true
                } else false
            }
        }
        card.addView(editText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        // Button row
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 28, 0, 0)
        }
        buttonRow.addView(Button(this).apply {
            text = "Cancel"
            setTextColor(Color.parseColor("#9999BB"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { setResult(RESULT_CANCELED); finish() }
        })
        buttonRow.addView(Button(this).apply {
            text = "   OK   "
            setTextColor(textColor)
            setBackgroundColor(accent)
            setOnClickListener { confirm(editText.text.toString()) }
        })
        card.addView(buttonRow)

        val cardParams = FrameLayout.LayoutParams(900, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        root.addView(card, cardParams)
        setContentView(root)

        // Request focus on the EditText immediately
        editText.requestFocus()

        // Belt-and-suspenders keyboard trigger:
        // 1) post() runs after layout is attached to the window
        // 2) toggleSoftInput is more aggressive than showSoftInput on TV
        editText.post {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    private fun confirm(value: String) {
        setResult(RESULT_OK, Intent().putExtra(RESULT_VALUE, value))
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}
