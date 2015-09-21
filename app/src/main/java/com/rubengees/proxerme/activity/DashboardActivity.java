package com.rubengees.proxerme.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.rubengees.proxerme.R;
import com.rubengees.proxerme.fragment.NewsFragment;

import java.util.ArrayList;

/**
 * TODO: Describe Class
 *
 * @author Ruben Gees
 */
public class DashboardActivity extends MainActivity {

    private static final int DRAWER_ID_NEWS = 0;
    private static final int DRAWER_ID_INFO = 10;
    private static final int DRAWER_ID_DONATE = 11;
    private static final int DRAWER_ID_SETTINGS = 12;

    private static final String STATE_CURRENT_DRAWER_ITEM_ID = "current_drawer_item_id";

    private Toolbar toolbar;
    private FrameLayout contentContainer;

    private Drawer drawer;

    private int currentDrawerItemId = -1;

    private Drawer.OnDrawerListener onDrawerListener = new Drawer.OnDrawerListener() {
        @Override
        public void onDrawerOpened(View view) {

        }

        @Override
        public void onDrawerClosed(View view) {

        }

        @Override
        public void onDrawerSlide(View view, float v) {

        }
    };

    private Drawer.OnDrawerItemClickListener onDrawerItemClickListener =
            new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {
                    int id = iDrawerItem.getIdentifier();

                    if (id != currentDrawerItemId) {
                        if (iDrawerItem.isSelectable()) {
                            currentDrawerItemId = id;
                        }

                        return handleOnDrawerItemClick(id);
                    }

                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (savedInstanceState != null) {
            currentDrawerItemId = savedInstanceState.getInt(STATE_CURRENT_DRAWER_ITEM_ID);
        }

        findViews();
        initViews();
        initDrawer(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        drawer.saveInstanceState(outState);
        outState.putInt(STATE_CURRENT_DRAWER_ITEM_ID, currentDrawerItemId);
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        contentContainer = (FrameLayout) findViewById(R.id.activity_main_content_container);
    }

    private void initViews() {
        setSupportActionBar(toolbar);
    }

    private void initDrawer(@Nullable Bundle savedInstanceState) {
        drawer = new DrawerBuilder(this).withDrawerItems(generateDrawerItems())
                .withStickyDrawerItems(generateStickyDrawerItems())
                .withOnDrawerListener(onDrawerListener)
                .withOnDrawerItemClickListener(onDrawerItemClickListener)
                .withShowDrawerOnFirstLaunch(true).withSelectedItem(DRAWER_ID_NEWS)
                .withActionBarDrawerToggleAnimated(true).withHasStableIds(true)
                .withSavedInstance(savedInstanceState).withToolbar(toolbar).build();

        if(savedInstanceState == null) {
            drawer.setSelection(DRAWER_ID_NEWS);
        }
    }

    private ArrayList<IDrawerItem> generateDrawerItems() {
        ArrayList<IDrawerItem> result = new ArrayList<>(1);

        result.add(new PrimaryDrawerItem().withName("News")
                .withIcon(GoogleMaterial.Icon.gmd_dashboard)
                .withSelectedTextColorRes(R.color.primary).withSelectedIconColorRes(R.color.primary)
                .withIconTintingEnabled(true).withIdentifier(DRAWER_ID_NEWS));

        return result;
    }

    private ArrayList<IDrawerItem> generateStickyDrawerItems() {
        ArrayList<IDrawerItem> result = new ArrayList<>(1);

        result.add(new PrimaryDrawerItem().withName("Info")
                .withIcon(GoogleMaterial.Icon.gmd_info).withSelectedTextColorRes(R.color.primary)
                .withSelectedIconColorRes(R.color.primary).withIconTintingEnabled(true)
                .withIdentifier(DRAWER_ID_INFO));

        result.add(new PrimaryDrawerItem().withName("Donate")
                .withIcon(GoogleMaterial.Icon.gmd_attach_money)
                .withSelectedTextColorRes(R.color.primary).withSelectedIconColorRes(R.color.primary)
                .withIconTintingEnabled(true).withSelectable(false)
                .withIdentifier(DRAWER_ID_DONATE));

        result.add(new PrimaryDrawerItem().withName("Settings")
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withSelectedTextColorRes(R.color.primary).withSelectedIconColorRes(R.color.primary)
                .withIconTintingEnabled(true).withIdentifier(DRAWER_ID_SETTINGS));

        return result;
    }

    public void setFragment(@NonNull Fragment fragment, @NonNull String title){
        setTitle(title);

        setFragment(fragment);
    }

    public void setFragment(@NonNull Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_content_container,
                fragment).commit();
    }

    private boolean handleOnDrawerItemClick(int id) {
        switch (id) {
            case DRAWER_ID_NEWS:
                setFragment(NewsFragment.newInstance(), "News");
                return false;
            case DRAWER_ID_INFO:
                return false;
            case DRAWER_ID_DONATE:
                return true;
            case DRAWER_ID_SETTINGS:
                return false;
            default:
                return true;
        }
    }
}
