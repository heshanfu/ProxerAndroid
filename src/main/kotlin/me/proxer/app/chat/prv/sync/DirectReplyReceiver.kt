package me.proxer.app.chat.prv.sync

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.RemoteInput
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import me.proxer.app.MainApplication.Companion.messengerDao
import me.proxer.app.util.extension.getSafeCharSequence
import me.proxer.app.util.extension.subscribeAndLogErrors

/**
 * @author Ruben Gees
 */
class DirectReplyReceiver : BroadcastReceiver() {

    companion object {
        const val REMOTE_REPLY_EXTRA = "remote_reply"

        private const val CONFERENCE_ID_EXTRA = "conference_id"

        fun getPendingIntent(context: Context, conferenceId: Long): PendingIntent {
            val intent = Intent(context, DirectReplyReceiver::class.java)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .apply { putExtra(CONFERENCE_ID_EXTRA, conferenceId) }

            return PendingIntent.getBroadcast(context, conferenceId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val conferenceId = intent.getLongExtra(CONFERENCE_ID_EXTRA, -1)

        Completable
            .fromAction {
                messengerDao.insertMessageToSend(getMessageText(intent), conferenceId)

                val unreadMap = messengerDao.getUnreadConferences()
                    .associate {
                        it to messengerDao.getMostRecentMessagesForConference(it.id, it.unreadMessageAmount)
                            .asReversed()
                    }
                    .plus(messengerDao.getConference(conferenceId) to emptyList())

                MessengerNotifications.showOrUpdate(context, unreadMap)
                MessengerWorker.enqueueSynchronization()
            }
            .subscribeOn(Schedulers.io())
            .subscribeAndLogErrors()
    }

    private fun getMessageText(intent: Intent) = RemoteInput.getResultsFromIntent(intent)
        .getSafeCharSequence(REMOTE_REPLY_EXTRA)
        .toString()
}
