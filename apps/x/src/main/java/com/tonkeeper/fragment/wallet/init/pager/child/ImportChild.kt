package com.tonkeeper.fragment.wallet.init.pager.child

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import com.tonapps.tonkeeperx.R
import com.tonkeeper.extensions.launch
import com.tonkeeper.fragment.wallet.init.InitModel
import org.ton.mnemonic.Mnemonic
import uikit.base.BaseFragment
import uikit.extensions.hideKeyboard
import uikit.extensions.scrollDown
import uikit.extensions.scrollToBottom
import uikit.extensions.scrollToTop
import uikit.extensions.scrollToView
import uikit.extensions.scrollView
import uikit.widget.LoaderView
import uikit.widget.WordFormView
import uikit.widget.WordHintView

class ImportChild: BaseFragment(R.layout.fragment_import) {

    companion object {

        private const val TESTNET_KEY = "testnet"

        fun newInstance(testnet: Boolean): ImportChild {
            val fragment = ImportChild()
            fragment.arguments = Bundle().apply {
                putBoolean(TESTNET_KEY, testnet)
            }
            return fragment
        }
    }

    private val parentFeature: InitModel by viewModels({ requireParentFragment() })
    private val mnemonicWords = Mnemonic.mnemonicWords()

    private lateinit var wordContent: NestedScrollView
    private lateinit var wordFormView: WordFormView
    private lateinit var wordHintView: WordHintView
    private lateinit var nextButton: Button
    private lateinit var loaderView: LoaderView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wordContent = view.findViewById(R.id.word_content)

        wordFormView = view.findViewById(R.id.word_form)
        wordFormView.isValidValue = mnemonicWords::contains
        wordFormView.doOnTextChanged = { text ->
            parentFeature.requestHint(text)
        }
        wordFormView.doOnFocusInput = { input, _ ->
            wordContent.scrollView(input)
        }
        wordFormView.doOnChange = { _, empty ->
            nextButton.isEnabled = empty == 0
            if (empty == 0) {
                wordContent.scrollDown(true)
            }
        }

        wordHintView = view.findViewById(R.id.word_hint)
        wordHintView.doOnClickText = { word ->
            wordFormView.setWord(word)
            wordHintView.hide()
        }

        nextButton = view.findViewById(R.id.wallet_next)
        nextButton.setOnClickListener {
            val words = wordFormView.getWords()
            parentFeature.setWords(words)
        }

        loaderView = view.findViewById(R.id.wallet_loading)

        parentFeature.hintWords.launch(this) { words ->
            if (words.isEmpty()) {
                wordHintView.hide()
            } else {
                wordHintView.setWords(words)
            }
        }

        parentFeature.validWords.launch(this) { isValid ->
            nextButton.isEnabled = isValid
        }

        parentFeature.loading.launch(this) { loading ->
            if (loading) {
                setLoadingState()
            } else {
                setDefaultState()
            }
        }
    }

    private fun setLoadingState() {
        nextButton.isEnabled = false
        nextButton.text = ""
        wordFormView.hideKeyboard()

        loaderView.visibility = View.VISIBLE
        loaderView.resetAnimation()
    }

    private fun setDefaultState() {
        nextButton.isEnabled = true
        nextButton.text = getString(R.string.continue_action)

        loaderView.visibility = View.GONE
        loaderView.stopAnimation()
    }

    override fun onResume() {
        super.onResume()
        wordFormView.focus()
    }
}