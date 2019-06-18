package com.github.doomsdayrs.apps.shosetsu.ui.novel;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.Doomsdayrs.api.novelreader_core.main.DefaultScrapers;
import com.github.Doomsdayrs.api.novelreader_core.services.core.dep.Formatter;
import com.github.doomsdayrs.apps.shosetsu.R;
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database;
import com.github.doomsdayrs.apps.shosetsu.backend.settings.SettingsController;
import com.github.doomsdayrs.apps.shosetsu.ui.listeners.NovelFragmentChapterViewHideBar;
import com.github.doomsdayrs.apps.shosetsu.backend.async.NovelFragmentChapterViewLoad;
import com.github.doomsdayrs.apps.shosetsu.variables.Settings;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * This file is part of Shosetsu.
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see https://www.gnu.org/licenses/ .
 * ====================================================================
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
public class NovelFragmentChapterView extends AppCompatActivity {
    private ScrollView scrollView;
    public TextView textView;
    public ProgressBar progressBar;
    public Formatter formatter;
    public String URL;
    private String novelURL;
    public String text = null;

    private MenuItem bookmark;


    private int a = 0;
    private boolean bookmarked;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("text", text);
        outState.putString("url", URL);
        outState.putInt("formatter", formatter.getID());
        outState.putString("novelURL", novelURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_chapter_view, menu);
        // Night mode
        menu.findItem(R.id.chapter_view_nightMode).setChecked(!SettingsController.isReaderLightMode());

        // Bookmark
        bookmark = menu.findItem(R.id.chapter_view_bookmark);
        bookmarked = SettingsController.isBookMarked(URL);
        if (bookmarked) {
            bookmark.setIcon(R.drawable.ic_bookmark_black_24dp);
            int y = SettingsController.getYBookmark(URL);
            Log.d("Loaded Scroll", Integer.toString(y));
            scrollView.setScrollY(y);
        }
        return true;
    }

    private void setThemeMode() {
        scrollView.setBackgroundColor(Settings.ReaderTextBackgroundColor);
        textView.setBackgroundColor(Settings.ReaderTextBackgroundColor);
        textView.setTextColor(Settings.ReaderTextColor);
    }


    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        Log.d("item", item.toString());
        switch (item.getItemId()) {
            case R.id.chapter_view_nightMode: {
                if (!item.isChecked()) {
                    SettingsController.swapReaderColor();
                    setThemeMode();
                } else {
                    SettingsController.swapReaderColor();
                    setThemeMode();
                }
                item.setChecked(!item.isChecked());
                return true;
            }
            case R.id.chapter_view_textSize: {
                return true;
            }
            case R.id.chapter_view_bookmark: {
                JSONObject jsonObject = new JSONObject();
                try {
                    int y = scrollView.getScrollY();
                    Log.d("ScrollSave", Integer.toString(y));
                    jsonObject.put("y", y);
                    bookmarked = SettingsController.toggleBookmarkChapter(URL, jsonObject);
                    if (bookmarked)
                        bookmark.setIcon(R.drawable.ic_bookmark_black_24dp);
                    else bookmark.setIcon(R.drawable.ic_bookmark_border_black_24dp);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OnCreate", "NovelFragmentChapterView");
        setContentView(R.layout.fragment_novel_chapter_view);
        {
            progressBar = findViewById(R.id.fragment_novel_chapter_view_progress);
            novelURL = getIntent().getStringExtra("novelURL");
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            formatter = DefaultScrapers.formatters.get(getIntent().getIntExtra("formatter", -1) - 1);
            scrollView = findViewById(R.id.fragment_novel_scroll);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (bookmarked)
                        if (a % 5 == 0) {
                            int y = scrollView.getScrollY();
                            JSONObject jsonObject = new JSONObject();
                            Log.d("ScrollSave", Integer.toString(y));
                            try {
                                jsonObject.put("y", y);
                                SettingsController.setScroll(URL, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else a++;
                });
            } else {
                scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                    if (bookmarked)
                        if (a % 5 == 0) {
                            int y = scrollView.getScrollY();
                            JSONObject jsonObject = new JSONObject();
                            Log.d("ScrollSave", Integer.toString(y));
                            try {
                                jsonObject.put("y", y);
                                SettingsController.setScroll(URL, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else a++;
                });
            }
            textView = findViewById(R.id.fragment_novel_chapter_view_text);
            textView.setOnClickListener(new NovelFragmentChapterViewHideBar(toolbar));
        }

        setThemeMode();

        if (savedInstanceState != null) {
            URL = savedInstanceState.getString("url");
            formatter = DefaultScrapers.formatters.get(savedInstanceState.getInt("formatter") - 1);
            text = savedInstanceState.getString("text");
        } else URL = getIntent().getStringExtra("url");
        Log.d("URL", Objects.requireNonNull(URL));

        if (getIntent().getBooleanExtra("downloaded", false))
            text = Objects.requireNonNull(Database.getSaved(novelURL, URL)).replaceAll("\n", "\n\n");
        else if (text == null)
            if (URL != null) {
                new NovelFragmentChapterViewLoad(progressBar).execute(this);
            }

        textView.setText(text);
    }
}