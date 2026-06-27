package dev.ujhhgtg.wekit.ui.content

import coil3.ImageLoader
import coil3.request.CachePolicy
import dev.ujhhgtg.wekit.utils.HostInfo

val GlobalImageLoader by lazy {
    ImageLoader.Builder(HostInfo.application)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()
}
