package com.proxerme.app.helper

import android.app.Activity
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.v7.widget.Toolbar
import android.view.View
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.proxerme.app.R
import com.proxerme.app.interfaces.OnActivityListener
import com.proxerme.app.manager.UserManager
import com.proxerme.library.info.ProxerUrlHolder
import java.util.*

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */

class MaterialDrawerHelper : OnActivityListener {

    companion object {
        const val ITEM_NEWS = 0L
        const val ITEM_CHAT = 1L
        const val ITEM_ANIME = 2L
        const val ITEM_MANGA = 3L
        const val ITEM_DONATE = 10L
        const val ITEM_SETTINGS = 11L

        const val ACCOUNT_GUEST = 100L
        const val ACCOUNT_LOGIN = 101L
        const val ACCOUNT_USER = 102L
        const val ACCOUNT_LOGOUT = 103L

        private const val STATE_CURRENT_DRAWER_ITEM_ID = "material_drawer_helper_current_id"

        @IntDef(ITEM_NEWS, ITEM_CHAT, ITEM_DONATE, ITEM_SETTINGS, ITEM_ANIME, ITEM_MANGA)
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD,
                AnnotationTarget.VALUE_PARAMETER)
        annotation class DrawerItem

        @IntDef(ACCOUNT_GUEST, ACCOUNT_LOGIN, ACCOUNT_USER, ACCOUNT_LOGOUT)
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD,
                AnnotationTarget.VALUE_PARAMETER)
        annotation class AccountItem
    }

    private val header: AccountHeader
    private val drawer: Drawer

    private var currentId: Long

    private val itemClickCallback: (id: Long) -> Boolean
    private val accountClickCallback: (id: Long) -> Boolean

    constructor(context: Activity, toolbar: Toolbar, savedInstanceState: Bundle?,
                itemClickCallback: (id: Long) -> Boolean = { false },
                accountClickCallback: (id: Long) -> Boolean = { false }) {
        this.itemClickCallback = itemClickCallback
        this.accountClickCallback = accountClickCallback

        header = buildAccountHeader(context, savedInstanceState)
        drawer = buildDrawer(context, toolbar, header, savedInstanceState)

        currentId = savedInstanceState?.getLong(STATE_CURRENT_DRAWER_ITEM_ID) ?: -1L
    }

    override fun onBackPressed(): Boolean {
        if (isDrawerOpen()) {
            drawer.closeDrawer()

            return true
        } else if (currentId != ITEM_NEWS) {
            select(ITEM_NEWS)

            return true
        } else {
            return false
        }
    }

    fun saveInstanceState(outState: Bundle) {
        outState.putLong(STATE_CURRENT_DRAWER_ITEM_ID, currentId)

        header.saveInstanceState(outState)
        drawer.saveInstanceState(outState)
    }

    fun isDrawerOpen(): Boolean {
        return drawer.isDrawerOpen
    }

    fun select(@DrawerItem id: Long) {
        drawer.setSelection(id)
    }

    fun refreshHeader(context: Activity) {
        header.profiles = generateAccountItems(context)
        drawer.recyclerView.adapter.notifyDataSetChanged()
    }

    private fun buildAccountHeader(context: Activity, savedInstanceState: Bundle?): AccountHeader {
        return AccountHeaderBuilder()
                .withActivity(context)
                .withCompactStyle(true)
                .withHeaderBackground(R.color.colorAccent)
                .withOnAccountHeaderListener { view, profile, current ->
                    onAccountItemClick(view, profile, current)
                }
                .withSavedInstance(savedInstanceState)
                .withProfiles(generateAccountItems(context))
                .build()
    }

    private fun generateAccountItems(context: Activity): List<IProfile<*>> {
        val user = UserManager.user

        if (user == null) {
            return arrayListOf(
                    ProfileDrawerItem()
                            .withName(context.getString(R.string.drawer_account_guest))
                            .withIcon(R.mipmap.ic_launcher)
                            .withIdentifier(ACCOUNT_GUEST),
                    ProfileSettingDrawerItem()
                            .withName(context.getString(R.string.drawer_account_login))
                            .withIconTinted(true)
                            .withIcon(CommunityMaterial.Icon.cmd_account_key)
                            .withIdentifier(ACCOUNT_LOGIN))
        } else {
            return arrayListOf(
                    ProfileDrawerItem().withName(user.username)
                            .withIdentifier(ACCOUNT_USER)
                            .withIcon(ProxerUrlHolder.getUserImageUrl(user.imageId)),
                    ProfileSettingDrawerItem()
                            .withName(context.getString(R.string.drawer_account_logout))
                            .withIcon(CommunityMaterial.Icon.cmd_account_remove)
                            .withIconTinted(true)
                            .withIdentifier(ACCOUNT_LOGOUT)
            )
        }
    }

    private fun buildDrawer(context: Activity, toolbar: Toolbar, accountHeader: AccountHeader,
                            savedInstanceState: Bundle?): Drawer {
        return DrawerBuilder(context)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withDrawerItems(generateDrawerItems())
                .withStickyDrawerItems(generateStickyDrawerItems())
                .withOnDrawerItemClickListener { view, id, item ->
                    onDrawerItemClick(view, id, item)
                }
                .withShowDrawerOnFirstLaunch(true)
                .withActionBarDrawerToggleAnimated(true)
                .withTranslucentStatusBar(true)
                .withSavedInstance(savedInstanceState)
                .build()
    }

    private fun generateDrawerItems(): List<IDrawerItem<*, *>> {
        return arrayListOf(
                PrimaryDrawerItem()
                        .withName(R.string.drawer_item_news)
                        .withIcon(CommunityMaterial.Icon.cmd_newspaper)
                        .withSelectedTextColorRes(R.color.colorPrimary)
                        .withSelectedIconColorRes(R.color.colorPrimary)
                        .withBadgeStyle(BadgeStyle()
                                .withColorRes(R.color.colorPrimary)
                                .withTextColorRes(android.R.color.white))
                        .withIdentifier(ITEM_NEWS),
                PrimaryDrawerItem()
                        .withName(R.string.drawer_item_chat)
                        .withIcon(CommunityMaterial.Icon.cmd_message_text)
                        .withSelectedTextColorRes(R.color.colorPrimary)
                        .withSelectedIconColorRes(R.color.colorPrimary)
                        .withBadgeStyle(BadgeStyle()
                                .withColorRes(R.color.colorPrimary)
                                .withTextColorRes(android.R.color.white))
                        .withIdentifier(ITEM_CHAT),
                PrimaryDrawerItem()
                        .withName(R.string.drawer_item_anime)
                        .withIcon(CommunityMaterial.Icon.cmd_television)
                        .withSelectedTextColorRes(R.color.colorPrimary)
                        .withSelectedIconColorRes(R.color.colorPrimary)
                        .withBadgeStyle(BadgeStyle()
                                .withColorRes(R.color.colorPrimary)
                                .withTextColorRes(android.R.color.white))
                        .withIdentifier(ITEM_ANIME),
                PrimaryDrawerItem()
                        .withName(R.string.drawer_item_manga)
                        .withIcon(CommunityMaterial.Icon.cmd_book_open_variant)
                        .withSelectedTextColorRes(R.color.colorPrimary)
                        .withSelectedIconColorRes(R.color.colorPrimary)
                        .withBadgeStyle(BadgeStyle()
                                .withColorRes(R.color.colorPrimary)
                                .withTextColorRes(android.R.color.white))
                        .withIdentifier(ITEM_MANGA)
        )
    }

    private fun generateStickyDrawerItems(): ArrayList<IDrawerItem<*, *>> {
        return arrayListOf(
                PrimaryDrawerItem()
                        .withName(R.string.drawer_item_donate)
                        .withIcon(CommunityMaterial.Icon.cmd_gift)
                        .withSelectedTextColorRes(R.color.colorPrimary)
                        .withSelectedIconColorRes(R.color.colorPrimary)
                        .withSelectable(false)
                        .withIdentifier(ITEM_DONATE.toLong()),
                PrimaryDrawerItem()
                        .withName(R.string.drawer_item_settings)
                        .withIcon(CommunityMaterial.Icon.cmd_settings)
                        .withSelectedTextColorRes(R.color.colorPrimary)
                        .withSelectedIconColorRes(R.color.colorPrimary)
                        .withIdentifier(ITEM_SETTINGS))
    }

    private fun onDrawerItemClick(view: View?, id: Int, item: IDrawerItem<*, *>): Boolean {
        if (item.identifier != currentId) {
            if (item.isSelectable) {
                currentId = item.identifier
            }

            return itemClickCallback.invoke(item.identifier)
        }

        return true
    }

    private fun onAccountItemClick(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
        return accountClickCallback.invoke(profile.identifier)
    }

}
