package de.blinkt.openvpn.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.fragments.*;


public class TabActivity extends Activity {

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab vpnListTab = bar.newTab().setText(R.string.vpn_list_title);
        Tab abouttab = bar.newTab().setText(R.string.about);

        vpnListTab.setTabListener(new TabListener<VPNProfileList>("profiles", VPNProfileList.class));
        abouttab.setTabListener(new TabListener<AboutFragment>("about", AboutFragment.class));

        bar.addTab(vpnListTab);
        bar.addTab(abouttab);
    }

    protected class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private String mTag;
        private Class<T> mClass;

        public TabListener(String tag, Class<T> clz) {
            mTag = tag;
            mClass = clz;

            mFragment = getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(TabActivity.this, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(data);
    }
}
