package com.tonkeeper.api

import com.squareup.moshi.adapter
import com.tonkeeper.Global
import com.tonkeeper.R
import com.tonkeeper.core.Coin
import io.tonapi.infrastructure.Serializer
import io.tonapi.models.Account
import io.tonapi.models.AccountAddress
import io.tonapi.models.AccountEvent
import io.tonapi.models.ImagePreview
import io.tonapi.models.JettonBalance
import io.tonapi.models.JettonPreview
import io.tonapi.models.JettonSwapAction
import io.tonapi.models.JettonVerificationType
import io.tonapi.models.NftItem
import io.tonapi.models.TokenRates
import kotlinx.coroutines.delay
import ton.SupportedTokens
import ton.extensions.toUserFriendly
import kotlin.math.abs

private val nftItemPreviewSizes = arrayOf(
    "250x250", "500x500", "100x100"
)

fun TokenRates.to(toCurrency: String, value: Float): Float {
    val price = prices?.get(toCurrency) ?: return 0f
    return price.toFloat() * value
}

suspend fun <R> withRetry(
    times: Int = 5,
    delay: Long = 1000,
    block: () -> R
): R? {
    for (i in 0 until times) {
        try {
            return block()
        } catch (ignored: Throwable) {}
        delay(delay)
    }
    return null
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> toJSON(obj: T?): String {
    if (obj == null) {
        return ""
    }
    return Serializer.moshi.adapter<T>().toJson(obj)
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> fromJSON(json: String): T {
    return Serializer.moshi.adapter<T>().fromJson(json)!!
}

val AccountEvent.fee: Long
    get() {
        if (0 > extra) {
            return abs(extra)
        }
        return 0
    }

val AccountEvent.refund: Long
    get() {
        if (0 < extra) {
            return extra
        }
        return 0
    }

val JettonPreview.isTon: Boolean
    get() {
        return address == SupportedTokens.TON.code
    }

val JettonBalance.isTon: Boolean
    get() {
        return jetton.isTon
    }

fun Account.asJettonBalance(): JettonBalance {
    val icon = Global.tonCoinUrl
    return JettonBalance(
        balance = balance.toString(),
        walletAddress = AccountAddress(
            address = address,
            isScam = false,
            isWallet = true,
            name = name,
            icon = icon,
        ),
        jetton = JettonPreview(
            address = SupportedTokens.TON.code,
            name = SupportedTokens.TON.code,
            symbol = SupportedTokens.TON.code,
            decimals = 9,
            image = icon,
            verification = JettonVerificationType.whitelist
        )
    )
}

val JettonSwapAction.jettonPreview: JettonPreview?
    get() {
        return jettonMasterIn ?: jettonMasterOut
    }

val JettonSwapAction.amount: String
    get() {
        if (amountIn.isEmpty()) {
            return amountOut
        }
        return amountIn
    }

val JettonSwapAction.ton: Long
    get() {
        return tonIn ?: tonOut ?: 0
    }

val AccountAddress.nameOrAddress: String
    get() {
        if (!name.isNullOrBlank()) {
            return name!!
        }
        return address.toUserFriendly().shortAddress
    }

val AccountAddress.iconURL: String?
    get() = icon

val String.shortAddress: String
    get() {
        if (length < 8) return this
        return substring(0, 4) + "…" + substring(length - 4, length)
    }

val JettonBalance.address: String
    get() = jetton.address.toUserFriendly(wallet = false)

val JettonBalance.symbol: String
    get() = jetton.symbol

val JettonBalance.parsedBalance: Float
    get() = Coin.parseFloat(balance, jetton.decimals)

fun NftItem.imageBySize(size: String): ImagePreview? {
    return previews?.firstOrNull { it.resolution == size }
}

val NftItem.imageURL: String
    get() {
        for (size in nftItemPreviewSizes) {
            val preview = imageBySize(size)
            if (preview != null) {
                return preview.url
            }
        }
        return previews?.lastOrNull()?.url ?: ""
    }

val NftItem.title: String
    get() {
        val metadataName = metadata["name"] as? String
        if (metadataName != null) {
            return metadataName
        }
        return collection?.name ?: ""
    }

val NftItem.description: String?
    get() {
        val metadataName = metadata["description"] as? String
        if (metadataName != null) {
            return metadataName
        }
        return null
    }

val NftItem.collectionName: String
    get() {
        return collection?.name ?: ""
    }

val NftItem.collectionDescription: String?
    get() {
        return collection?.description
    }

val NftItem.ownerAddress: String?
    get() {
        return owner?.address?.toUserFriendly()
    }