package me.proxer.app.profile.comment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import me.proxer.app.R
import me.proxer.app.base.PagedContentFragment
import me.proxer.app.media.MediaActivity
import me.proxer.app.profile.ProfileActivity
import me.proxer.app.util.extension.unsafeLazy
import me.proxer.library.enums.Category
import org.jetbrains.anko.bundleOf
import kotlin.properties.Delegates

/**
 * @author Ruben Gees
 */
class ProfileCommentFragment : PagedContentFragment<ParsedUserComment>() {

    companion object {
        private const val CATEGORY_ARGUMENT = "category"

        fun newInstance() = ProfileCommentFragment().apply {
            arguments = bundleOf()
        }
    }

    override val emptyDataMessage = R.string.error_no_data_comments
    override val isSwipeToRefreshEnabled = false
    override val pagingThreshold = 3

    override val viewModel by unsafeLazy { ProfileCommentViewModelProvider.get(this, userId, username, category) }

    override val hostingActivity: ProfileActivity
        get() = activity as ProfileActivity

    private val userId: String?
        get() = hostingActivity.userId

    private val username: String?
        get() = hostingActivity.username

    private var category: Category?
        get() = requireArguments().getSerializable(CATEGORY_ARGUMENT) as? Category
        set(value) {
            requireArguments().putSerializable(CATEGORY_ARGUMENT, value)

            viewModel.category = value
        }

    override val layoutManager by unsafeLazy { LinearLayoutManager(context) }
    override var innerAdapter by Delegates.notNull<ProfileCommentAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        innerAdapter = ProfileCommentAdapter(savedInstanceState)

        innerAdapter.titleClickSubject
            .autoDisposable(this.scope())
            .subscribe {
                MediaActivity.navigateTo(requireActivity(), it.entryId, it.entryName, it.category)
            }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        IconicsMenuInflaterUtil.inflate(inflater, context, R.menu.fragment_user_comments, menu, true)

        when (category) {
            Category.ANIME -> menu.findItem(R.id.anime).isChecked = true
            Category.MANGA -> menu.findItem(R.id.manga).isChecked = true
            else -> menu.findItem(R.id.all).isChecked = true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.anime -> category = Category.ANIME
            R.id.manga -> category = Category.MANGA
            R.id.all -> category = null
        }

        item.isChecked = true

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        innerAdapter.saveInstanceState(outState)
    }
}
