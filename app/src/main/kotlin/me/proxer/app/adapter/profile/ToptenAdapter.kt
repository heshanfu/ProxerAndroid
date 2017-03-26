package me.proxer.app.adapter.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import me.proxer.app.R
import me.proxer.app.adapter.base.PagingAdapter
import me.proxer.library.entitiy.user.TopTenEntry
import me.proxer.library.util.ProxerUrls

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class ToptenAdapter : PagingAdapter<TopTenEntry>() {

    var callback: TopTenAdapterCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_topten_entry, parent, false))

    override fun destroy() {
        super.destroy()

        callback = null
    }

    inner class ViewHolder(itemView: View) : PagingViewHolder<TopTenEntry>(itemView) {

        private val image: ImageView by bindView(R.id.image)
        private val title: TextView by bindView(R.id.title)

        init {
            itemView.setOnClickListener {
                withSafeAdapterPosition {
                    callback?.onTopTenEntryClick(list[it])
                }
            }
        }

        override fun bind(item: TopTenEntry) {
            title.text = item.name

            Glide.with(image.context)
                    .load(ProxerUrls.entryImage(item.id).toString())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(image)
        }
    }

    interface TopTenAdapterCallback {
        fun onTopTenEntryClick(item: TopTenEntry) {

        }
    }
}