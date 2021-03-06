package me.proxer.app.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import kotterknife.KotterKnife
import me.proxer.app.MainApplication.Companion.refWatcher
import me.proxer.app.util.extension.androidUri
import me.proxer.app.util.extension.openHttpPage
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import okhttp3.HttpUrl
import org.jetbrains.anko.bundleOf
import kotlin.properties.Delegates

/**
 * @author Ruben Gees
 */
@Suppress("UnnecessaryAbstractClass")
abstract class BaseDialog : DialogFragment() {

    val dialogLifecycleOwner: LifecycleOwner = object : LifecycleOwner {

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry ?: LifecycleRegistry(this).also {
                lifecycleRegistry = it
            }
        }
    }

    private var customTabsHelper by Delegates.notNull<CustomTabsHelperFragment>()
    private var lifecycleRegistry: LifecycleRegistry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customTabsHelper = CustomTabsHelperFragment.attachTo(this)
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val result = super.onGetLayoutInflater(savedInstanceState)

        if (dialog != null) {
            lifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        onDialogCreated(savedInstanceState)

        return result
    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            lifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
    }

    override fun onResume() {
        super.onResume()

        if (dialog != null) {
            lifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onPause() {
        super.onPause()

        if (dialog != null) {
            lifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
    }

    override fun onStop() {
        if (dialog != null) {
            lifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }

        super.onStop()
    }

    override fun onDestroyView() {
        if (dialog != null) {
            lifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }

        KotterKnife.reset(this)

        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()

        refWatcher.watch(this)
    }

    open fun onDialogCreated(savedInstanceState: Bundle?) = Unit

    fun setLikelyUrl(url: HttpUrl): Boolean {
        return customTabsHelper.mayLaunchUrl(url.androidUri(), bundleOf(), emptyList())
    }

    fun showPage(url: HttpUrl, forceBrowser: Boolean = false) {
        customTabsHelper.openHttpPage(requireActivity(), url, forceBrowser)
    }

    protected fun requireArguments() = arguments ?: throw IllegalStateException("arguments are null")
    protected fun requireTargetFragment() = targetFragment ?: throw IllegalStateException("targetFragment is null")
}
