package io.github.chrislo27.toolboks.registry

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.lazysound.LazySoundLoader


object AssetRegistry : Disposable {

    private const val LOAD_STATE_NONE = 0
    private const val LOAD_STATE_LOADING = 1
    private const val LOAD_STATE_DONE = 2

    val manager: AssetManager = AssetManager()
    val unmanagedAssets: MutableMap<String, Any> = mutableMapOf()
    val assetMap: Map<String, String> = mutableMapOf()

    private val assetLoaders: MutableList<IAssetLoader> = mutableListOf()

    private var loadingState: Int = LOAD_STATE_NONE

    init {
        manager.setLoader(LazySound::class.java, LazySoundLoader(manager.fileHandleResolver))
    }

    fun bindAsset(key: String, file: String) {
        if (key.startsWith(Toolboks.TOOLBOKS_ASSET_PREFIX)) {
            throw IllegalArgumentException("$key starts with the Toolboks asset prefix, which is ${Toolboks.TOOLBOKS_ASSET_PREFIX}")
        }
        if (assetMap.containsKey(key)) {
            throw IllegalArgumentException("$key has already been bound to ${assetMap[key]}")
        }

        (assetMap as MutableMap)[key] = file
    }

    internal fun bindToolboksAsset(keyWithoutPrefix: String, file: String) {
        val key = Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix
        if (assetMap.containsKey(key)) {
            throw IllegalArgumentException("$key has already been bound to ${assetMap[key]}")
        }

        (assetMap as MutableMap)[key] = file
    }

    fun addAssetLoader(loader: IAssetLoader) {
        assetLoaders += loader
        val map = mutableMapOf<String, String>()
        loader.addUnmanagedAssets(map)
        val allStartingWithPrefix = map.keys.filter{ it.startsWith(Toolboks.TOOLBOKS_ASSET_PREFIX) }
        if (allStartingWithPrefix.isNotEmpty()) {
            throw IllegalArgumentException("$allStartingWithPrefix start with the Toolboks asset prefix, which is ${Toolboks.TOOLBOKS_ASSET_PREFIX}")
        }

        unmanagedAssets.putAll(map)
    }

    fun load(delta: Float): Float {
        if (loadingState == LOAD_STATE_NONE) {
            loadingState = LOAD_STATE_LOADING

            assetLoaders.forEach {
                it.addManagedAssets(manager)
            }
        }

        if (manager.update((delta * 1000).coerceIn(0f, Int.MAX_VALUE.toFloat()).toInt())) {
            loadingState = LOAD_STATE_DONE
        }

        return manager.progress
    }

    fun loadBlocking() {
        while (load(Int.MAX_VALUE.toFloat()) < 1f);
    }

    operator inline fun <reified T> get(key: String): T {
        return manager.get(assetMap[key], T::class.java) ?:
                throw IllegalArgumentException(
                        if (manager.isLoaded(assetMap[key])) // this might never happen, actually
                            "Asset was wrong type: key $key, got ${T::class.java.canonicalName}," +
                                    " should be ${manager.getAssetType(assetMap[key]).canonicalName}"
                        else
                            "Asset not loaded/found: $key"
                                              )
    }

    override fun dispose() {
        unmanagedAssets.values.filterIsInstance(Disposable::class.java).forEach(Disposable::dispose)
        manager.dispose()
    }

    interface IAssetLoader {

        fun addManagedAssets(manager: AssetManager)

        fun addUnmanagedAssets(assets: MutableMap<String, *>)

    }

}