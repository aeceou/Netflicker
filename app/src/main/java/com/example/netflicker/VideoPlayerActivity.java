package com.example.netflicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.Response;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONException;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoTitle;
    TextView title;
    int userLevel = 0;
    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK,FULLSCREEN;
    }
    ImageView videoBack, lock, unlock, scaling;
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    //horizontal recyclerview variables
    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlaybackIconsAdapter playbackIconsAdapter;
    RecyclerView recyclerViewIcons;
    PlaybackParameters parameters;
    DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;
    Uri uriSubtitle;
    List<Integer> difficulties;
    boolean postResponse = false;

    //horizontal recyclerview variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();
        playerView = findViewById(R.id.exoplayer_view);
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        //screenOrientation();

        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        recyclerViewIcons = findViewById(R.id.recyclerview_icon);

        title.setText(videoTitle);

        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
        filePickerDialog.setPositiveBtnName("OK");
        filePickerDialog.setNegativeBtnName("Cancel");

        //userLevelChoiceDialogFragment = new UserLevelChoiceDialogFragment(new WeakReference<VideoPlayerActivity>(this));

        //iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_level_0, "Novice"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_level_1, "Intermediate"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_level_2, "Advanced"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_level_3, "Native"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_subtitle, "Subtitle"));

        playbackIconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconsAdapter);
        playbackIconsAdapter.notifyDataSetChanged();
        playbackIconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    userLevel = 0;
                    Toast.makeText(VideoPlayerActivity.this, "Level: Novice", Toast.LENGTH_SHORT).show();
                    playbackIconsAdapter.notifyDataSetChanged();
                }
                if (position == 1) {
                    userLevel = 1;
                    Toast.makeText(VideoPlayerActivity.this, "Level: Intermediate", Toast.LENGTH_SHORT).show();
                    playbackIconsAdapter.notifyDataSetChanged();
                }
                if (position == 2) {
                    userLevel = 2;
                    Toast.makeText(VideoPlayerActivity.this, "Level: Advanced", Toast.LENGTH_SHORT).show();
                    playbackIconsAdapter.notifyDataSetChanged();
                }
                if (position == 3) {
                    userLevel = 3;
                    Toast.makeText(VideoPlayerActivity.this, "Level: Native", Toast.LENGTH_SHORT).show();
                    playbackIconsAdapter.notifyDataSetChanged();
                }
                if (position == 4) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconsAdapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playbackIconsAdapter.notifyDataSetChanged();
                    }
                }

                if (position == 5) {
                    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
                    dialogProperties.extensions = new String[]{".srt"};
                    dialogProperties.root = new File("/storage/emulated/0");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            for (String path : files) {
                                File file = new File(path);
                                uriSubtitle = Uri.parse(file.getAbsolutePath().toString());
                                //Log.e("20202989", file.getAbsolutePath());
                            }
                            playVideoSubtitle(uriSubtitle);
                        }
                    });
                }
            }
        });

        playVideo();
    }


    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        //player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();

    }

    private void playVideoSubtitle(Uri subtitle) {
        long oldPosition = player.getCurrentPosition();
        player.stop();

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "app");
            MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                    .createMediaSource(Uri.parse(String.valueOf(subtitle)), textFormat, C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource, subtitleSource);
            concatenatingMediaSource.addMediaSource(mergingMediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        //player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);
        onCues();
        playError();
    }

    private void screenOrientation() {
        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this, uri);
            bitmap = retriever.getFrameAtTime();

            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth < videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception e) {
            Log.e("MediaMetaDataRetriever", "screenOrientation");
        }
    }

    private void playError() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_back:
                if (player != null) {
                    player.release();
                }
                finish();
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "unLocked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };

    private void onCues() {
        RequestQueue requests = Volley.newRequestQueue(this);
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("aim", "difficulties");
        String URL = "http://solar.postech.ac.kr:9000/recommender";
        player.addTextOutput(new TextOutput() {
            @Override
            public void onCues(List<Cue> cues) {
                if (cues.size() > 0) {
                    Cue currentCue = cues.get(0);
                    String currentText = currentCue.text.toString();
                    ArrayList<String> words = new ArrayList<String>(Arrays.asList(currentText.split("\\s")));
                    int c = 0;
                    int wordsLength = words.size();
                    while (c < wordsLength) {
                        if (Pattern.matches("^[a-zA-Z]*$", words.get(c))) {
                            c++;
                        } else {
                            words.remove(c);
                            wordsLength--;
                        }
                    }
                    if (words.size() > 0 && !postResponse) {
                        requestMap.put("data", words);
                        JSONObject requestData = new JSONObject(requestMap);
                        getResponse(requests, URL, requestData, words);
                    }
                }
            }
        });
        player.setPlayWhenReady(true);
    }

    private void getResponse(RequestQueue requests, String URL, JSONObject requestData, ArrayList<String> query) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL, requestData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    postResponse = true;
                    List<Integer> result = new ArrayList<Integer>();
                    Iterator<String> keyItr = response.keys();
                    while(keyItr.hasNext()) {
                        String key = keyItr.next();
                        result.add(response.getInt(key));
                    }
                    int maxDifficulty = Collections.max(result);
                    int wordIdx = result.indexOf(maxDifficulty);
                    String difficultWord = query.get(wordIdx);
                    TextView recommendedText = findViewById(R.id.recommended_text);
                    recommendedText.setText(difficultWord);
                    postResponse = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                requests.start();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("Content-Type", "application/json");
                map.put("Accept", "application/json");
                return map;
            }
        };
        requests.add(jsonRequest);
    }

}
