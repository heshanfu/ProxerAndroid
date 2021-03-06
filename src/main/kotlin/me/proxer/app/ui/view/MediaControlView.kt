package me.proxer.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import me.proxer.app.R
import me.proxer.app.util.Utils
import org.threeten.bp.LocalDateTime
import kotlin.properties.Delegates

/**
 * @author Ruben Gees
 */
class MediaControlView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val uploaderClickSubject: PublishSubject<Uploader> = PublishSubject.create()
    val translatorGroupClickSubject: PublishSubject<SimpleTranslatorGroup> = PublishSubject.create()
    val episodeSwitchSubject: PublishSubject<Int> = PublishSubject.create()
    val bookmarkSetSubject: PublishSubject<Int> = PublishSubject.create()
    val finishClickSubject: PublishSubject<Int> = PublishSubject.create()

    var textResolver: TextResourceResolver? = null
        set(value) {
            field = value

            if (value != null) {
                previous.text = value.previous()
                next.text = value.next()
                bookmarkThis.text = value.bookmarkThis()
                bookmarkNext.text = value.bookmarkNext()
            }
        }

    var uploader by Delegates.observable<Uploader?>(null) { _, _, new ->
        if (new == null) {
            uploaderRow.visibility = View.GONE
        } else {
            uploaderRow.visibility = View.VISIBLE
            uploaderText.text = new.name
        }
    }

    var translatorGroup by Delegates.observable<SimpleTranslatorGroup?>(null) { _, _, new ->
        if (new == null) {
            translatorRow.visibility = View.GONE
        } else {
            translatorRow.visibility = View.VISIBLE
            translatorGroupText.text = new.name
        }
    }

    var dateTime by Delegates.observable<LocalDateTime?>(null) { _, _, new ->
        if (new == null) {
            dateRow.visibility = View.GONE
        } else {
            dateRow.visibility = View.VISIBLE
            dateText.text = Utils.dateFormatter.format(new)
        }
    }

    var episodeInfo by Delegates.observable(SimpleEpisodeInfo(Int.MAX_VALUE, 1)) { _, _, new ->
        if (new.current <= 1) {
            previous.visibility = View.GONE
        } else {
            previous.visibility = View.VISIBLE
        }

        if (new.current >= new.amount) {
            next.visibility = View.GONE
        } else {
            next.visibility = View.VISIBLE
        }

        bookmarkNext.text = when {
            new.current < new.amount -> textResolver?.bookmarkNext()
            else -> bookmarkNext.context.getString(R.string.view_media_control_finish)
        }
    }

    private val uploaderRow: ViewGroup by bindView(R.id.uploaderRow)
    private val translatorRow: ViewGroup by bindView(R.id.translatorRow)
    private val dateRow: ViewGroup by bindView(R.id.dateRow)

    private val uploaderText: TextView by bindView(R.id.uploader)
    private val translatorGroupText: TextView by bindView(R.id.translatorGroup)
    private val dateText: TextView by bindView(R.id.date)

    private val previous: Button by bindView(R.id.previous)
    private val next: Button by bindView(R.id.next)
    private val bookmarkThis: Button by bindView(R.id.bookmarkThis)
    private val bookmarkNext: Button by bindView(R.id.bookmarkNext)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_media_control, this, true)

        uploader = null
        translatorGroup = null
        dateTime = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        uploaderText.clicks()
            .filter { uploader != null }
            .map { uploader }
            .autoDisposable(ViewScopeProvider.from(this))
            .subscribe(uploaderClickSubject)

        translatorGroupText.clicks()
            .filter { translatorGroup != null }
            .map { translatorGroup }
            .autoDisposable(ViewScopeProvider.from(this))
            .subscribe(translatorGroupClickSubject)

        previous.clicks()
            .map { episodeInfo.current - 1 }
            .autoDisposable(ViewScopeProvider.from(this))
            .subscribe(episodeSwitchSubject)

        next.clicks()
            .map { episodeInfo.current + 1 }
            .autoDisposable(ViewScopeProvider.from(this))
            .subscribe(episodeSwitchSubject)

        bookmarkThis.clicks()
            .map { episodeInfo.current }
            .autoDisposable(ViewScopeProvider.from(this))
            .subscribe(bookmarkSetSubject)

        bookmarkNext.clicks()
            .map { episodeInfo.current to episodeInfo.amount }
            .publish()
            .also { observable ->
                observable.filter { (current, amount) -> current < amount }
                    .map { (current, _) -> current + 1 }
                    .autoDisposable(ViewScopeProvider.from(this))
                    .subscribe(bookmarkSetSubject)

                observable.filter { (current, amount) -> current >= amount }
                    .map { (current, _) -> current }
                    .autoDisposable(ViewScopeProvider.from(this))
                    .subscribe(finishClickSubject)
            }
            .connect()
    }

    data class Uploader(val id: String, val name: String)
    data class SimpleTranslatorGroup(val id: String, val name: String)
    data class SimpleEpisodeInfo(val amount: Int, val current: Int)

    interface TextResourceResolver {
        fun next(): String
        fun previous(): String
        fun bookmarkThis(): String
        fun bookmarkNext(): String
    }
}
