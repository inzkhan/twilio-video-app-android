package com.twilio.video.app.e2eTest

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION_DEFAULT
import com.twilio.video.app.data.Preferences.VIDEO_DIMENSIONS
import com.twilio.video.app.screen.clickSettingsMenuItem
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.assertTextIsDisplayedRetry
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.getStringArray
import com.twilio.video.app.util.retryEspressoAction
import com.twilio.video.app.util.scrollAndClickView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@E2ETest
class PreferencesTest : BaseE2ETest() {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun it_should_assert_correct_default_bandwidth_preferences() {
        retryEspressoAction { clickSettingsMenuItem() }

        assertTextIsDisplayedRetry(getString(R.string.settings_title))

        scrollAndClickView(getString(R.string.settings_title_advanced), R.id.recycler_view)

        assertTextIsDisplayedRetry(getString(R.string.settings_title_advanced))

        assertDefaultAdvancedSettings()

        scrollAndClickView(getString(R.string.settings_title_bandwidth_profile), R.id.recycler_view)

        assertTextIsDisplayedRetry(getString(R.string.settings_title_bandwidth_profile))

        assertDefaultBandwidthProfileSettings()
    }

    private fun assertDefaultAdvancedSettings() {
        val defaultDimensions = VIDEO_DIMENSIONS[VIDEO_CAPTURE_RESOLUTION_DEFAULT.toInt()]
        assertDefaultValue(getString(R.string.settings_screen_video_resolution),
                "${defaultDimensions.width}x${defaultDimensions.height}")
    }

    fun assertDefaultBandwidthProfileSettings() {
        assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_mode),
                getStringArray(R.array.settings_screen_bandwidth_profile_modes)[1])

        assertDefaultValue(getString(R.string.settings_screen_max_subscription_bitrate),
                Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT.toString())

        assertDefaultValue(getString(R.string.settings_screen_max_video_tracks),
                Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT.toString())

        assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_dominant_speaker_priority),
                getStringArray(R.array.settings_screen_bandwidth_profile_dominant_speaker_priorities)[2])

        assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_track_switch_mode),
                Preferences.SERVER_DEFAULT)

        assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_low_track_priority),
                Preferences.SERVER_DEFAULT)

        assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_standard_track_priority),
                Preferences.SERVER_DEFAULT)

        assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_high_track_priority),
                Preferences.SERVER_DEFAULT)
    }

    private fun assertDefaultValue(preferenceTitle: String, preferenceValue: String) {
        onView(withId(R.id.recycler_view))
                .perform(scrollTo<ViewHolder>(hasDescendant(withText(preferenceTitle))))
        onView(withText(preferenceTitle)).check(matches(hasSibling(withText(preferenceValue))))
    }
}
