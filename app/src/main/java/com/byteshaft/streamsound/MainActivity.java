package com.byteshaft.streamsound;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.byteshaft.streamsound.adapter.SongsAdapter;
import com.byteshaft.streamsound.fragments.PlayerFragment;
import com.byteshaft.streamsound.fragments.SocialMediaFragment;
import com.byteshaft.streamsound.service.NotificationService;
import com.byteshaft.streamsound.service.PlayService;
import com.byteshaft.streamsound.utils.AppGlobals;
import com.byteshaft.streamsound.utils.Constants;
import com.byteshaft.streamsound.utils.Helpers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ProgressDialog mProgressDialog;
    private ListView mListView;
    public ImageView mPlayerControl;
    private ImageView buttonNext;
    private ImageView buttonPrevious;
    public RelativeLayout controls_layout;
    public SeekBar seekBar;
    private int songLength;
    public int updateValue;
    private int songLengthInSeconds;
    public TextView bufferingTextView;
    private int preLast;
    private boolean loadMoreRunning = false;
    private boolean hiddenByRefresh = false;
    private int mLastFirstVisibleItem;
    private boolean scrollingUp = false;
    public boolean controlsDownByScroll = false;
    public SongsAdapter songsAdapter;
    private AudioManager audioManager;
    public TextView timeTextView;
    private View mBaseView;
    private static PlayerListFragment sInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(getIconForEach(i));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlayService.getInstance().stopSelf();
        NotificationService.getsInstance().stopSelf();
    }

    private int getIconForEach(int tabNum) {
        switch (tabNum) {
            case 0:
                return R.drawable.play_list;
            case 1:
                return R.drawable.player;
            case 2:
                return R.drawable.social;
            default:
                return R.drawable.play_list;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            System.out.println(position);
            switch (position) {
                case 0:
                    return new PlayerListFragment();
                case 1:
                    return PlayerFragment.getFragment();
                case 2:
                    return SocialMediaFragment.getFragment();
                default:
                    return new PlayerListFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "";
//                case 1:
//                    return "SECTION 2";
//                case 2:
//                    return "SECTION 3";
//            }
//            return null;
//        }
    }

    class PlayerListFragment extends Fragment implements View.OnClickListener {


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mBaseView = inflater.inflate(R.layout.activity_main, container, false);
            System.out.println("PlayerListFragment");
            sInstance = this;
            audioManager = (AudioManager) AppGlobals.getContext().getSystemService(Context.AUDIO_SERVICE);
            mListView = (ListView) mBaseView.findViewById(R.id.song_list);
            mListView.setSmoothScrollbarEnabled(true);
            controls_layout = (RelativeLayout) mBaseView.findViewById(R.id.now_playing_controls_header);
            /// Media Controls
            mPlayerControl = (ImageView) mBaseView.findViewById(R.id.play_pause_button);
            buttonNext = (ImageView) mBaseView.findViewById(R.id.next_button);
            buttonPrevious = (ImageView) mBaseView.findViewById(R.id.previous_button);
            seekBar = (SeekBar) mBaseView.findViewById(R.id.nowPlayingSeekBar);
            bufferingTextView = (TextView) mBaseView.findViewById(R.id.buffering);
            timeTextView = (TextView) mBaseView.findViewById(R.id.time_progress);
            buttonNext.setOnClickListener(this);
            buttonPrevious.setOnClickListener(this);
            AppGlobals.initializeAllDataSets();
            new GetSoundDetailsTask().execute();
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                boolean seek = false;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seek = fromUser;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seek) {
                        PlayService.sMediaPlayer.seekTo((int) TimeUnit.SECONDS.toMillis(seekBar.getProgress()));
                        PlayService.sMediaPlayer.start();
                    }
                }
            });

            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    final ListView lw = mListView;

                    if (view.getId() == lw.getId()) {
                        final int currentFirstVisibleItem = lw.getFirstVisiblePosition();

                        if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                            scrollingUp = false;
                        } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                            scrollingUp = true;
                            if (controlsDownByScroll) {
                                if (PlayService.sMediaPlayer != null &&
                                        PlayService.sMediaPlayer.isPlaying() && audioManager.isMusicActive()) {
                                    animateBottomUp();
                                    controlsDownByScroll = false;
                                }
                            }
                        }
                        mLastFirstVisibleItem = currentFirstVisibleItem;
                    }

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    switch (view.getId()) {
                        case R.id.song_list:
                            final int lastItem = firstVisibleItem + visibleItemCount;
                            if (lastItem == totalItemCount) {
                                if (preLast != lastItem && !loadMoreRunning) { //to avoid multiple calls for last item
                                    Log.d("Last", "Last");
                                    preLast = lastItem;
                                    loadMoreRunning = true;
                                    if (!AppGlobals.getNextUrl().trim().isEmpty()) {
                                        new GetSoundDetailsTask().execute();
                                        if (AppGlobals.getControlsVisibility()) {
                                            animateControlsDown();
                                            hiddenByRefresh = true;
                                        }
                                    }
                                }
                            } else if (lastItem == (totalItemCount - 1)) {
                                System.out.println("scrollingUp" + scrollingUp);
                                if (!scrollingUp) {
                                    animateControlsDown();
                                    controlsDownByScroll = true;
                                }
                            }
                    }

                }
            });

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String url = AppGlobals.getStreamUrlsHashMap().
                            get(Integer.valueOf(String.valueOf(parent.getItemAtPosition(position))));
                    String formattedUrl = String.format("%s%s%s", url,
                            AppGlobals.ADD_CLIENT_ID, AppGlobals.CLIENT_KEY);
                    AppGlobals.setCurrentPlayingSong((Integer) parent.getItemAtPosition(position));
                    songLength = Integer.valueOf(AppGlobals.getDurationHashMap()
                            .get(Integer.valueOf(String.valueOf(parent.getItemAtPosition(position)))));
                    playSong(formattedUrl);
                }
            });
            mPlayerControl.setOnClickListener(this);
            return mBaseView;
        }

        @Override
        public void onPause() {
            super.onPause();
            System.out.println("OK");
        }

        private void animateControlsDown() {
            Animation bottomDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.bottom_down);
            controls_layout.startAnimation(bottomDown);
            controls_layout.setVisibility(View.GONE);
            AppGlobals.setControlsVisible(false);
        }

        private void playSong(String formattedUrl) {
            Picasso.with(getActivity()).load(AppGlobals.getSongImageUrlHashMap()
                    .get(AppGlobals.getCurrentPlayingSong())).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    AppGlobals.setCurrentPlayingSongBitMap(bitmap);


                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    System.out.println(errorDrawable.getState());

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
            if (PlayService.sMediaPlayer != null) {
                PlayService.sMediaPlayer.stop();
                PlayService.sMediaPlayer.reset();
                PlayService.updateHandler.removeCallbacks(PlayService.timerRunnable);
            }
            songLengthInSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(songLength);
            updateValue = songLengthInSeconds / 100;
            seekBar.setMax(songLengthInSeconds);
            seekBar.setProgress(0);
            animateBottomUp();
            UpdateUiHelpers.setSeekBarIndeterminate();
            AppGlobals.setSongCompleteStatus(false);
            Intent intent = new Intent(getActivity().getApplicationContext(), PlayService.class);
            intent.putExtra(AppGlobals.SOUND_URL, formattedUrl);
            intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            getActivity().startService(intent);
        }

        public void animateBottomUp() {
            System.out.println(AppGlobals.getControlsVisibility());
            if (!AppGlobals.getControlsVisibility()) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.bottom_up);
                controls_layout.startAnimation(bottomUp);
                controls_layout.setVisibility(View.VISIBLE);
                AppGlobals.setControlsVisible(true);
            }

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_pause_button:
                    PlayService.togglePlayPause();
                    break;
                case R.id.next_button:
                    nextSong();
                    break;
                case R.id.previous_button:
                    previousSong();
                    break;
            }
        }

        public void previousSong() {
            int previousSongIndex = (AppGlobals.getsSongsIdsArray()
                    .indexOf(AppGlobals.getCurrentPlayingSong())) - 1;
            if (previousSongIndex != -1) {
                seekBar.setProgress(0);
                int songId = AppGlobals.getsSongsIdsArray().get(previousSongIndex);
                AppGlobals.setCurrentPlayingSong(songId);
                songLength = Integer.valueOf(AppGlobals.getDurationHashMap()
                        .get(songId));
                String url = AppGlobals.getStreamUrlsHashMap().
                        get(songId);
                String formattedUrl = getFormattedUrl(url);
                playSong(formattedUrl);
            }
        }

        public void nextSong() {
            System.out.println(AppGlobals.getsSongsIdsArray()
                    .indexOf(AppGlobals.getCurrentPlayingSong()));
            int nextSongIndex = (AppGlobals.getsSongsIdsArray()
                    .indexOf(AppGlobals.getCurrentPlayingSong())) + 1;
            System.out.println(nextSongIndex);
            if (nextSongIndex < AppGlobals.getsSongsIdsArray().size()) {
                seekBar.setProgress(0);
                int songId = AppGlobals.getsSongsIdsArray().get(nextSongIndex);
                AppGlobals.setCurrentPlayingSong(songId);
                songLength = Integer.valueOf(AppGlobals.getDurationHashMap()
                        .get(songId));
                String url = AppGlobals.getStreamUrlsHashMap().
                        get(songId);
                String formattedUrl = getFormattedUrl(url);
                playSong(formattedUrl);
            }
        }

        private String getFormattedUrl(String url) {
            return String.format("%s%s%s", url,
                    AppGlobals.ADD_CLIENT_ID, AppGlobals.CLIENT_KEY);
        }

        class GetSoundDetailsTask extends AsyncTask<String, String, ArrayList<Integer>> {

            private boolean noInternet = false;
            private String targetUrl;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage("loading ...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            protected ArrayList<Integer> doInBackground(String... params) {
                int responseCode = 0;
                if (Helpers.isNetworkAvailable() && Helpers.isInternetWorking()) {
                    JsonParser jsonParser = new JsonParser();
                    if (!Helpers.userIdStatus()) {
                        int urlReply;
                        try {
                            urlReply = Helpers.getRequest(AppGlobals.apiUrl);
                            if (urlReply == 302) {
                                JsonObject jsonObj = jsonParser.parse(Helpers.getParsedString())
                                        .getAsJsonObject();
                                if (!jsonObj.get("location").isJsonNull()) {
                                    String resultUrl = jsonObj.get("location").getAsString();
                                    urlReply = Helpers.getRequest(resultUrl);
                                    if (urlReply == HttpURLConnection.HTTP_OK) {
                                        JsonObject json = jsonParser.parse(Helpers.getParsedString())
                                                .getAsJsonObject();
                                        Helpers.userId(json.get("id").getAsString());
                                        Helpers.userIdAcquired(true);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (loadMoreRunning) {
                        targetUrl = AppGlobals.getNextUrl();
                    } else {
                        targetUrl = String.format("http://api.soundcloud.com/users/" +
                                        "%s/tracks.json?client_id=%s&limit=20&linked_partitioning=1",
                                Helpers.getUserId(),
                                AppGlobals.CLIENT_KEY);
                    }
                    try {
                        responseCode = Helpers.getRequest(targetUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JsonObject mainData = jsonParser.parse(Helpers.getParsedString())
                                .getAsJsonObject();
                        JsonArray jsonArray = mainData.get("collection").getAsJsonArray();
                        if (mainData.has("next_href")) {
                            if (!mainData.get("next_href").isJsonNull()) {
                                String nextUrl = mainData.get("next_href").getAsString();
                                AppGlobals.setNextUrl(nextUrl);
                            }
                        }
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            if (!AppGlobals.getsSongsIdsArray().contains(jsonObject.get("id").getAsInt())) {
                                int currentSongId = jsonObject.get("id").getAsInt();
                                AppGlobals.addSongId(currentSongId);
                                if (!jsonObject.get("title").isJsonNull()) {
                                    AppGlobals.addTitleToHashMap(currentSongId, jsonObject.get("title")
                                            .getAsString());
                                }
                                if (!jsonObject.get("stream_url").isJsonNull()) {
                                    AppGlobals.addStreamUrlsToHashMap(currentSongId, jsonObject.get("stream_url")
                                            .getAsString());
                                }
                                if (!jsonObject.get("duration").isJsonNull()) {
                                    AppGlobals.addDurationHashMap(currentSongId, jsonObject.get("duration")
                                            .getAsString());
                                }
                                if (!jsonObject.get("genre").isJsonNull()) {
                                    AppGlobals.addGenreHashMap(currentSongId, jsonObject.get("genre")
                                            .getAsString());
                                }
                                if (!jsonObject.get("artwork_url").isJsonNull()) {
                                    AppGlobals.addSongImageUrlHashMap(currentSongId,
                                            jsonObject.get("artwork_url").getAsString());
                                }
                                JsonObject jsonElements = jsonObject.get("user").getAsJsonObject();
                                if (!jsonElements.get("username").isJsonNull()) {
                                    AppGlobals.addSongArtistHashMap(currentSongId,
                                            jsonElements.get("username").getAsString());
                                }
                            }

                        }

                    }
                } else {
                    noInternet = true;
                }
                return AppGlobals.getsSongsIdsArray();
            }

            @Override
            protected void onPostExecute(ArrayList<Integer> songIdsArray) {
                super.onPostExecute(songIdsArray);
                mProgressDialog.dismiss();
                if (!loadMoreRunning) {
                    songsAdapter = new SongsAdapter(getActivity().getApplicationContext(), R.layout.single_row,
                            songIdsArray, getActivity());
                }
                if (noInternet) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("No Internet");
                    alertDialogBuilder
                            .setMessage("please check your network connection.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialogBuilder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new GetSoundDetailsTask().execute();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                if (loadMoreRunning) {
                    songsAdapter.notifyDataSetChanged();
                } else {
                    mListView.setAdapter(songsAdapter);
                }
                if (AppGlobals.getsSongsIdsArray().size() > 0) {
                    AppGlobals.setCurrentPlayingSong(AppGlobals.getsSongsIdsArray().get(0));
                }
                mListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.smoothScrollToPosition(preLast);
                    }
                });
                if (hiddenByRefresh) {
                    animateBottomUp();
                    hiddenByRefresh = false;
                }
                loadMoreRunning = false;

            }
        }
    }
}
