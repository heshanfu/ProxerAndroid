package me.proxer.app.profile.bookmark

import android.support.v4.view.ViewCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import me.proxer.app.GlideRequests
import me.proxer.app.R
import me.proxer.app.base.BaseAdapter
import me.proxer.app.util.extension.*
import me.proxer.app.util.view.GlideGrayscaleTransformation
import me.proxer.library.entitiy.ucp.Bookmark
import me.proxer.library.util.ProxerUrls

/**
 * @author Ruben Gees
 */
class BookmarkAdapter(private val glide: GlideRequests) : BaseAdapter<Bookmark, BookmarkAdapter.ViewHolder>() {

    val clickSubject: PublishSubject<Bookmark> = PublishSubject.create<Bookmark>()
    val longClickSubject: PublishSubject<Pair<ImageView, Bookmark>> = PublishSubject.create<Pair<ImageView, Bookmark>>()
    val removeClickSubject: PublishSubject<Bookmark> = PublishSubject.create<Bookmark>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])
    override fun onViewRecycled(holder: ViewHolder) = glide.clear(holder.image)

    override fun areItemsTheSame(old: Bookmark, new: Bookmark) = old.entryId == new.entryId
    override fun areContentsTheSame(old: Bookmark, new: Bookmark) = old.id == new.id

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val title: TextView by bindView(R.id.title)
        internal val medium: TextView by bindView(R.id.medium)
        internal val image: ImageView by bindView(R.id.image)
        internal val episode: TextView by bindView(R.id.episode)
        internal val language: ImageView by bindView(R.id.language)
        internal val remove: ImageButton by bindView(R.id.remove)

        init {
            itemView.setOnClickListener {
                withSafeAdapterPosition(this) {
                    clickSubject.onNext(data[it])
                }
            }

            itemView.setOnLongClickListener {
                withSafeAdapterPosition(this) {
                    longClickSubject.onNext(image to data[it])
                }

                true
            }

            remove.setOnClickListener {
                withSafeAdapterPosition(this) {
                    removeClickSubject.onNext(data[it])
                }
            }

            remove.setOnLongClickListener {
                it.toastBelow(R.string.fragment_bookmarks_delete_hint)

                true
            }

            remove.setImageDrawable(IconicsDrawable(remove.context)
                    .icon(CommunityMaterial.Icon.cmd_bookmark_remove)
                    .colorRes(R.color.icon)
                    .sizeDp(48)
                    .paddingDp(12))
        }

        fun bind(item: Bookmark) {
            ViewCompat.setTransitionName(image, "bookmark_${item.id}")

            val availabilityIndicator = AppCompatResources.getDrawable(episode.context, when (item.isAvailable) {
                true -> R.drawable.ic_circle_green
                false -> R.drawable.ic_circle_red
            })

            title.text = item.name
            medium.text = item.medium.toAppString(medium.context)
            episode.text = item.chapterName ?: item.category.toEpisodeAppString(episode.context, item.episode)

            episode.setCompoundDrawablesWithIntrinsicBounds(null, null, availabilityIndicator, null)
            language.setImageDrawable(item.language.toGeneralLanguage().toAppDrawable(language.context))

            glide.load(ProxerUrls.entryImage(item.entryId).toString())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply {
                        if (!item.isAvailable) {
                            transform(GlideGrayscaleTransformation())
                        }
                    }
                    .into(image)
        }
    }
}