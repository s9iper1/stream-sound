package com.byteshaft.streamsound.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

public class AppGlobals extends Application {

    // Chnages you should do here.
    public static String USER_NAME = "pti-social-media";
    /// urls
    // channel id for youtube will be like example https://www.youtube.com/channel/UCF0pVplsI8R5kcAqgtoRqoA
    // the channelid will be  after /channel/
    public static String facebookUrl = "https://m.facebook.com/YOUR_USERNAME";
    public static String twitterUrl = "https://m.twitter.com/YOUR_USERNAME";
    public static String youtubeUrl = "https://m.youtube.com/channel/Channel_Id?app=mobile";
    public static String instagramUrl = "https://www.instagram.com/YOUR_USERNAME";
    // the soundcloud app client id
    // you can create new id here http://soundcloud.com/you/apps/new
    public static final String CLIENT_KEY = "d15e89ac63aed800d452231a67207696";

    private static ArrayList<Integer> sSongsIdsArray;
    private static HashMap<Integer, String> sSongsTitleHashMap;
    private static HashMap<Integer, String> sStreamUrls;
    private static HashMap<Integer, String> sSongDurationMap;
    private static HashMap<Integer, String> sGenreHashMap;
    private static HashMap<Integer, String> sSongImageUrls;
    private static HashMap<Integer, String > sArtistHashMap;

    private static Context sContext;
    public static final String ADD_CLIENT_ID = "?client_id=";
    public static final String SOUND_URL = "sound_url";
    private static boolean sControlsVisible = false;
    private static boolean sIsSongCompleted = false;
    private static int sCurrentPlayingSong = 0;
    public static final String KEY_USER_ID_STATUS = "user_id";
    public static String apiUrl;
    public static final String KEY_ID = "user";
    private static String nextUrl = "";
    private static Bitmap imageBitMap = null;
    private static boolean sChangeFromLayout = false;
    private static boolean sChangeFromPlayer = false;
    private static boolean sRunningFromList = false;
    private static boolean sRunningFirstSong = false;
    private static boolean sNotificationVisibitlity = false;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        apiUrl = String.format("http://api.soundcloud.com/resolve.json?url=" +
                "https://soundcloud.com/%s&client_id=d15e89ac63aed800d452231a67207696", USER_NAME);
    }

    public static Context getContext() {
        return sContext;
    }

    public static void initializeAllDataSets() {
        sSongsIdsArray = new ArrayList<>();
        sSongsTitleHashMap = new HashMap<>();
        sStreamUrls = new HashMap<>();
        sSongDurationMap = new HashMap<>();
        sGenreHashMap = new HashMap<>();
        sSongImageUrls = new HashMap<>();
        sArtistHashMap = new HashMap<>();
    }

    public static void addSongId(int id) {
        sSongsIdsArray.add(id);
    }

    public static ArrayList<Integer> getsSongsIdsArray() {
        return sSongsIdsArray;
    }

    public static void addTitleToHashMap(int id, String value) {
        sSongsTitleHashMap.put(id, value);
    }

    public static HashMap<Integer, String> getTitlesHashMap() {
        return sSongsTitleHashMap;
    }

    public static void addStreamUrlsToHashMap(int id, String value) {
        sStreamUrls.put(id, value);
    }

    public static HashMap<Integer, String> getStreamUrlsHashMap() {
        return sStreamUrls;
    }

    public static void addDurationHashMap(int id, String value) {
        sSongDurationMap.put(id, value);
    }

    public static HashMap<Integer, String> getDurationHashMap() {
        return sSongDurationMap;
    }

    public static void addGenreHashMap(int id, String value) {
        sGenreHashMap.put(id, value);
    }

    public static HashMap<Integer, String> getGenreHashMap() {
        return sGenreHashMap;
    }

    public static void addSongImageUrlHashMap(int id, String value) {
        sSongImageUrls.put(id, value);
    }

    public static HashMap<Integer, String> getSongImageUrlHashMap() {
        return sSongImageUrls;
    }

    public static void addSongArtistHashMap(int id, String value) {
        sArtistHashMap.put(id, value);
    }

    public static HashMap<Integer, String> getSongArtistHashMap() {
        return sArtistHashMap;
    }

    public static void setControlsVisible(boolean status) {
        sControlsVisible = status;
    }

    public static boolean getControlsVisibility() {
        return sControlsVisible;
    }

    public static void setSongCompleteStatus(boolean value) {
        sIsSongCompleted = value;
    }

    public static boolean isSongCompleted() {
        return sIsSongCompleted;
    }

    public static void setCurrentPlayingSong(int currentSong) {
        sCurrentPlayingSong = currentSong;
    }

    public static int getCurrentPlayingSong() {
        return sCurrentPlayingSong;
    }

    public static void setNextUrl(String url) {
        nextUrl = url;
    }

    public static String getNextUrl() {
        return nextUrl;
    }

    public static void setCurrentPlayingSongBitMap(Bitmap bitMap) {
        imageBitMap = bitMap;
    }

    public static Bitmap getCurrentPlayingSongBitMap() {
        return imageBitMap;
    }

    public static void setChangeFromLayout(boolean value) {
        sChangeFromLayout = value;
    }

    public static boolean isChangeFromLayout() {
        return sChangeFromLayout;
    }

    public static void setChangeFromPlayer(boolean value) {
        sChangeFromPlayer = value;
    }

    public static boolean isChangeFromPlayer() {
        return sChangeFromPlayer;
    }

    public static void setRunningFromList(boolean status) {
        sRunningFromList = status;
    }

    public static boolean isRunningFromList() {
        return sRunningFromList;
    }

    public static void setRunningFirstSong(boolean status) {
        sRunningFirstSong = status;
    }

    public static boolean isRunningFirstSong() {
        return sRunningFirstSong;
    }

    public static void setNotificationVisibility(boolean visibility) {
        sNotificationVisibitlity = visibility;
    }

    public static boolean isNotificationVisible() {
        return sNotificationVisibitlity;
    }

}
