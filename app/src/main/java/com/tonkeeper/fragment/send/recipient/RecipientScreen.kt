package com.tonkeeper.fragment.send.recipient

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.viewModels
import com.tonkeeper.R
import com.tonkeeper.api.shortAddress
import com.tonkeeper.extensions.clipboardText
import com.tonkeeper.fragment.send.SendScreenEffect
import com.tonkeeper.fragment.send.SendScreenFeature
import com.tonkeeper.fragment.send.pager.PagerScreen
import ton.TonAddress
import uikit.widget.InputView

class RecipientScreen: PagerScreen<RecipientScreenState, RecipientScreenEffect, RecipientScreenFeature>(R.layout.fragment_send_recipient) {

    companion object {
        fun newInstance() = RecipientScreen()
    }

    override val feature: RecipientScreenFeature by viewModels()

    private lateinit var addressInput: InputView
    private lateinit var commentInput: InputView
    private lateinit var nextButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressInput = view.findViewById(R.id.address)
        addressInput.doOnTextChange = { feature.requestAddressCheck(it) }
        addressInput.doOnIconClick = {
            addressInput.hideKeyboard()
            sendFeature.sendEffect(SendScreenEffect.OpenCamera)
        }
        addressInput.doOnButtonClick = { paste() }

        commentInput = view.findViewById(R.id.comment)
        commentInput.doOnTextChange = {
            sendFeature.setComment(it)
        }

        nextButton = view.findViewById(R.id.next)
        nextButton.setOnClickListener {
            sendFeature.nextPage()
        }
    }

    fun setAddress(address: String?) {
        addressInput.text = address ?: ""
    }

    fun setComment(comment: String?) {
        commentInput.text = comment ?: ""
    }

    override fun newUiState(state: RecipientScreenState) {
        setAddressState(state.addressState)

        sendFeature.setAddress(state.address)
        sendFeature.setName(state.name)
    }

    private fun setAddressState(state: RecipientScreenState.AddressState) {
        nextButton.isEnabled = state == RecipientScreenState.AddressState.VALID

        if (state == RecipientScreenState.AddressState.LOADING) {
            addressInput.loading = true
            addressInput.error = false
            return
        }

        addressInput.loading = false
        addressInput.error = state == RecipientScreenState.AddressState.INVALID
    }

    private fun paste() {
        val text = context?.clipboardText()
        if (!text.isNullOrEmpty()) {
            setAddress(text)
        }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            addressInput.focus()
            sendFeature.setHeaderTitle(getString(R.string.recipient))
            sendFeature.setHeaderSubtitle(null)
        } else {
            addressInput.hideKeyboard()
        }
    }
}