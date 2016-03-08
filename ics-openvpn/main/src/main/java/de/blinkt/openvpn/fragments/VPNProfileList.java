/*
 * Copyright (c) 2012-2014 Arne Schwabe
 * Distributed under the GNU GPL v2. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import de.blinkt.openvpn.*;
import de.blinkt.openvpn.activities.ConfigConverter;
import de.blinkt.openvpn.activities.FileSelect;
import de.blinkt.openvpn.activities.VPNPreferences;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNClient;
import de.blinkt.openvpn.core.Device;
import de.blinkt.openvpn.core.NetworkConfig;
import de.blinkt.openvpn.activities.LoginActivity;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Timer;
import java.util.TimerTask;

public class VPNProfileList extends ListFragment {

	public final static int RESULT_VPN_DELETED = Activity.RESULT_FIRST_USER;

	private static final int MENU_ADD_PROFILE = Menu.FIRST;

	private static final int START_VPN_CONFIG = 92;
	//private static final int SELECT_PROFILE = 43;
	//private static final int IMPORT_PROFILE = 231;
    private static final int FILE_PICKER_RESULT_KITKAT = 392;

	private static final int MENU_IMPORT_PROFILE = Menu.FIRST +1;

	// update task.
	private UpdateTask upTask = null;
    Timer timer = new Timer();


    class VPNArrayAdapter extends ArrayAdapter<VpnProfile> {

		public VPNArrayAdapter(Context context, int resource,
				int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			View titleview = v.findViewById(R.id.vpn_list_item_left);
			titleview.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					VpnProfile profile =(VpnProfile) getListAdapter().getItem(position);
					startVPN(profile);
				}
			});

			View settingsview = v.findViewById(R.id.quickedit_settings);
			settingsview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					VpnProfile editProfile = (VpnProfile) getListAdapter().getItem(position);
					editVPN(editProfile);

				}
			});

			return v;
		}
	}








	private ArrayAdapter<VpnProfile> mArrayadapter;

	protected VpnProfile mEditProfile=null;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
        timer.schedule(task, 1000, 10000);
	}

	/*class MiniImageGetter implements ImageGetter {


		@Override
		public Drawable getDrawable(String source) {
			Drawable d=null;
			if ("ic_menu_add".equals(source))
				d = getActivity().getResources().getDrawable(R.drawable.ic_menu_add_grey);
			else if("ic_menu_archive".equals(source))
				d = getActivity().getResources().getDrawable(R.drawable.ic_menu_import_grey);
			
			
			
			if(d!=null) {
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}else{
				return null;
			}
		}
	}*/


	/*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.vpn_profile_list, container,false);

		TextView newvpntext = (TextView) v.findViewById(R.id.add_new_vpn_hint);
		TextView importvpntext = (TextView) v.findViewById(R.id.import_vpn_hint);
		
		
		
		newvpntext.setText(Html.fromHtml(getString(R.string.add_new_vpn_hint),new MiniImageGetter(),null));
		importvpntext.setText(Html.fromHtml(getString(R.string.vpn_import_hint),new MiniImageGetter(),null));
		
        

		return v;

	}*/

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter();
	}

    static class VpnProfileNameComparator implements Comparator<VpnProfile> {

        @Override
        public int compare(VpnProfile lhs, VpnProfile rhs) {
            if (lhs == rhs)
                // Catches also both null
                return 0;

            if (lhs == null)
                return -1;
            if (rhs == null)
                return 1;

            if (lhs.mName == null)
                return -1;
            if (rhs.mName == null)
                return 1;

            return lhs.mName.compareTo(rhs.mName);
        }

    }

	private void setListAdapter() {
		mArrayadapter = new VPNArrayAdapter(getActivity(),R.layout.vpn_list_item,R.id.vpn_item_title);
		Collection<VpnProfile> allvpn = getPM().getProfiles();

        TreeSet<VpnProfile> sortedset = new TreeSet<VpnProfile>(new VpnProfileNameComparator());
        sortedset.addAll(allvpn);
        mArrayadapter.addAll(sortedset);

        setListAdapter(mArrayadapter);
	}


/*
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_ADD_PROFILE, 0 , R.string.menu_add_profile)
		.setIcon(R.drawable.ic_menu_add)
		.setAlphabeticShortcut('a')
		.setTitleCondensed(getActivity().getString(R.string.add))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |  MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, MENU_IMPORT_PROFILE, 0,  R.string.menu_import)
		.setIcon(R.drawable.ic_menu_import)
		.setAlphabeticShortcut('i')
		.setTitleCondensed(getActivity().getString(R.string.menu_import_short))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT );
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == MENU_ADD_PROFILE) {
			onAddProfileClicked();
			return true;
		} else if (itemId == MENU_IMPORT_PROFILE) {
            boolean startOldFileDialog=true;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    startOldFileDialog = ! startFilePicker();

            if (startOldFileDialog)
			    startImportConfig();

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean startFilePicker() {

       Intent i = Utils.getFilePickerIntent(getActivity(), Utils.FileType.OVPN_CONFIG);
        if (i!=null) {
            startActivityForResult(i, FILE_PICKER_RESULT_KITKAT);
            return true;
        } else
            return false;
    }

    private void startImportConfig() {
		Intent intent = new Intent(getActivity(),FileSelect.class);
		intent.putExtra(FileSelect.NO_INLINE_SELECTION, true);
		intent.putExtra(FileSelect.WINDOW_TITLE, R.string.import_configuration_file);
		startActivityForResult(intent, SELECT_PROFILE);
	}





	private void onAddProfileClicked() {
		Context context = getActivity();
		if (context != null) {
			final EditText entry = new EditText(context);
			entry.setSingleLine();

			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(R.string.menu_add_profile);
			dialog.setMessage(R.string.add_profile_name_prompt);
			dialog.setView(entry);


			dialog.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = entry.getText().toString();
					if (getPM().getProfileByName(name)==null) {
						VpnProfile profile = new VpnProfile(name);
						addProfile(profile);
						editVPN(profile);
					} else {
						Toast.makeText(getActivity(), R.string.duplicate_profile_name, Toast.LENGTH_LONG).show();
					}
				}


			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			dialog.create().show();
		}

	}
*/

	private void addProfile(VpnProfile profile) {
		getPM().addProfile(profile);
		getPM().saveProfileList(getActivity());
		getPM().saveProfile(getActivity(),profile);
		mArrayadapter.add(profile);
	}

	private ProfileManager getPM() {
		return ProfileManager.getInstance(getActivity());
	}

        private VPNClient getVC() {
            return VPNClient.getInstance();
        }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == RESULT_VPN_DELETED){
			if(mArrayadapter != null && mEditProfile !=null)
				mArrayadapter.remove(mEditProfile);
		}

		if(resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == START_VPN_CONFIG) {
			String configuredVPN = data.getStringExtra(VpnProfile.EXTRA_PROFILEUUID);

			VpnProfile profile = ProfileManager.get(getActivity(),configuredVPN);
			getPM().saveProfile(getActivity(), profile);
			// Name could be modified, reset List adapter
			setListAdapter();

		}/* else if(requestCode== SELECT_PROFILE) {
            String fileData = data.getStringExtra(FileSelect.RESULT_DATA);
            Uri uri = new Uri.Builder().path(fileData).scheme("file").build();

            startConfigImport(uri);
		} else if(requestCode == IMPORT_PROFILE) {
			String profileUUID = data.getStringExtra(VpnProfile.EXTRA_PROFILEUUID);
			mArrayadapter.add(ProfileManager.get(getActivity(), profileUUID));
		} else if(requestCode == FILE_PICKER_RESULT_KITKAT) {
            if (data != null) {
                Uri uri = data.getData();
                startConfigImport(uri);
            }
        }*/

	}

    /*private void startConfigImport(Uri uri) {
        Intent startImport = new Intent(getActivity(),ConfigConverter.class);
        startImport.setAction(ConfigConverter.IMPORT_PROFILE);
        startImport.setData(uri);
        startActivityForResult(startImport, IMPORT_PROFILE);
    }*/

    private void editVPN(VpnProfile profile) {
		mEditProfile = profile;
		//Intent vprefintent = new Intent(getActivity(),VPNPreferences.class)
		//.putExtra(getActivity().getPackageName() + ".profileUUID", profile.getUUID().toString())

		//startActivityForResult(vprefintent,START_VPN_CONFIG);
        String userId = getVC().getUserInfo(getActivity());
        if (userId == null || "".equals(userId)) redirectMain();
        String netId = profile.getUUID().toString();
        new Thread(new DeviceThread(userId, netId)).start();
	}

	private void startVPN(VpnProfile profile) {

        /** Fetch user and send connection status to server **/
        String userId = getVC().getUserInfo(getActivity());
        if (userId == null || "".equals(userId)) redirectMain();
		getPM().saveProfile(getActivity(), profile);

		upTask = new UpdateTask(userId, profile.getUUID().toString());
		upTask.execute();

		Intent intent = new Intent(getActivity(),LaunchVPN.class);
		intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
		intent.setAction(Intent.ACTION_MAIN);
		startActivity(intent);
	}

    private void redirectMain() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    class DeviceThread implements Runnable {
        private String userId;
        private String netId;

        public DeviceThread(String uId, String nId) {
            userId = uId;
            netId = nId;
        }

        public ArrayList<String> showDevice() {
            ArrayList<String> items = new ArrayList<String>();
            try {
                List<Device> devices = getVC().device(getActivity(), userId, netId);
                for (Device dev : devices) {
                    items.add(dev.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return items;
        }

        public void run() {
            Bundle bd = new Bundle();
            bd.putStringArrayList("items", showDevice());
            Message message = new Message();
            message.what = 2;
            message.setData(bd);
            handler.sendMessage(message);
        }
    }

    /* create Handler */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    setListAdapter();
                    break;
                case 2:
                    Bundle bd = msg.getData();
                    List<String> list = bd.getStringArrayList("items");
                    String[] items = list.toArray(new String[list.size()]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Device List").setItems(items, null).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    TimerTask task = new TimerTask() {

        public boolean updateNetwork(String userId) {
            boolean up = false;
            VpnProfile vpnp = getPM().getLastConnectedVpn();
            NetworkConfig config = getVC().network(getActivity(), userId);
            //getVC().update(getActivity(), "", vpnp == null ? null : vpnp.getUUIDString());
            Set<String> remoteVpn = getPM().getProfilesByKey();
            Set<String> localVpn = config.getNetworksKey();
            Set<String> tmpVpn = new HashSet<String>();
            tmpVpn.clear();
            tmpVpn.addAll(localVpn);
            tmpVpn.removeAll(remoteVpn);
            for (String pUUID : tmpVpn) {
                up = true;
                getPM().removeProfile(getActivity(), getPM().get(getActivity(), pUUID));
            }
            tmpVpn.clear();
            tmpVpn.addAll(remoteVpn);
            tmpVpn.removeAll(localVpn);
            for (String pUUID : tmpVpn) {
                up = true;
                addProfile(getPM().get(getActivity(), pUUID));
            }

            return up;
        }

        public void run() {
            String userId = getVC().getUserInfo(getActivity());
            if (userId == null || "".equals(userId) {
                timer.cancel();
                redirectMain();
            }
            if (updateNetwork()) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }
    };

    /**
     * Represents an asynchronous connection/disconnection task.
     */
    public class UpdateTask extends AsyncTask<Void, Void, Boolean> {
		private final String userId;
		private final String netId;

		public UpdateTask(String uId, String nId) {
			userId = uId;
			netId = nId;
		}

        @Override
        protected Boolean doInBackground(Void... params) {
			try {
    		    getVC().update(getActivity(), userId, netId);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            upTask = null;
        }

		@Override
		protected void onCancelled() {
			upTask = null;
		}
    }
}
