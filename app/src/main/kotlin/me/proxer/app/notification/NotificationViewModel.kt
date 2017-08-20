package me.proxer.app.notification

import android.app.Application
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.proxer.app.MainApplication.Companion.api
import me.proxer.app.MainApplication.Companion.bus
import me.proxer.app.base.PagedContentViewModel
import me.proxer.app.util.ErrorUtils
import me.proxer.app.util.data.ResettingMutableLiveData
import me.proxer.app.util.data.UniqueQueue
import me.proxer.app.util.extension.ProxerNotification
import me.proxer.app.util.extension.buildOptionalSingle
import me.proxer.library.api.PagingLimitEndpoint

/**
 * @author Ruben Gees
 */
class NotificationViewModel(application: Application) : PagedContentViewModel<ProxerNotification>(application) {

    override val isLoginRequired = true
    override val itemsOnPage = 30

    override val endpoint: PagingLimitEndpoint<List<ProxerNotification>>
        get() = api.notifications().notifications()
                .markAsRead(page == 0)

    val deletionError = ResettingMutableLiveData<ErrorUtils.ErrorAction?>()

    private val deletionQueue = UniqueQueue<ProxerNotification>()
    private var deletionDisposable: Disposable? = null
    private var deletionAllDisposable: Disposable? = null

    init {
        bus.register(AccountNotificationEvent::class.java).subscribe()
    }

    override fun onCleared() {
        deletionAllDisposable?.dispose()
        deletionDisposable?.dispose()

        deletionAllDisposable = null
        deletionDisposable = null

        super.onCleared()
    }

    fun addItemToDelete(item: ProxerNotification) {
        deletionQueue.add(item)

        if (deletionDisposable?.isDisposed != false) {
            doItemDeletion()
        }
    }

    fun deleteAll() {
        deletionAllDisposable?.dispose()
        deletionDisposable?.dispose()

        deletionQueue.clear()

        api.notifications().deleteAllNotifications()
                .buildOptionalSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    data.value = null
                }, {
                    deletionError.value = ErrorUtils.handle(it)
                })
    }

    private fun doItemDeletion() {
        deletionAllDisposable?.dispose()
        deletionDisposable?.dispose()

        deletionQueue.poll()?.let { item ->
            deletionDisposable = api.notifications().deleteNotification(item.id)
                    .buildOptionalSingle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        data.value = data.value?.filterNot { it == item }

                        doItemDeletion()
                    }, {
                        deletionQueue.clear()

                        deletionError.value = ErrorUtils.handle(it)
                    })
        }
    }
}