package com.proxerme.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.ImageView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.proxerme.app.R
import com.proxerme.app.fragment.ProfileFragment
import com.proxerme.app.fragment.ToptenFragment
import com.proxerme.library.info.ProxerUrlHolder

class UserActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_USERNAME = "extra_username"
        private const val EXTRA_IMAGE_ID = "extra_image_id"

        fun navigateTo(context: Activity, userId: String?, username: String?, imageId: String) {
            if (userId.isNullOrBlank() && username.isNullOrBlank()) {
                return
            }

            val intent = Intent(context, UserActivity::class.java)
                    .apply {
                        this.putExtra(EXTRA_USER_ID, userId)
                        this.putExtra(EXTRA_USERNAME, username)
                        this.putExtra(EXTRA_IMAGE_ID, imageId)
                    }

            context.startActivity(intent)
        }
    }

    private var userId: String? = null
    private var username: String? = null
    private lateinit var imageId: String
    private var sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val collapsingToolbar: CollapsingToolbarLayout by bindView(R.id.collapsingToolbar)
    private val viewPager: ViewPager by bindView(R.id.viewPager)
    private val profileImage: ImageView by bindView(R.id.profileImage)
    private val tabs: TabLayout by bindView(R.id.tabs)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user)
        setSupportActionBar(toolbar)

        userId = intent.getStringExtra(EXTRA_USER_ID)
        username = intent.getStringExtra(EXTRA_USERNAME)
        imageId = intent.getStringExtra(EXTRA_IMAGE_ID)
        viewPager.adapter = sectionsPagerAdapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = username
        collapsingToolbar.isTitleEnabled = false
        tabs.setupWithViewPager(viewPager)

        profileImage.setOnClickListener {
            ImageDetailActivity.navigateTo(this, it as ImageView,
                    ProxerUrlHolder.getUserImageUrl(imageId))
        }

        if (imageId.isBlank()) {
            profileImage.setImageDrawable(IconicsDrawable(profileImage.context)
                    .icon(CommunityMaterial.Icon.cmd_account)
                    .sizeDp(32)
                    .colorRes(R.color.colorPrimary))
        } else {
            Glide.with(this)
                    .load(ProxerUrlHolder.getUserImageUrl(imageId))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(profileImage)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(fragmentManager: FragmentManager) :
            FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> ProfileFragment.newInstance(userId, username)
                1 -> ToptenFragment.newInstance(userId, username)
                else -> throw RuntimeException("Unknown index passed")
            }
        }

        override fun getCount() = 2

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.fragment_profile_title)
                1 -> getString(R.string.fragment_topten_title)
                else -> throw RuntimeException("Unknown index passed")
            }
        }
    }
}