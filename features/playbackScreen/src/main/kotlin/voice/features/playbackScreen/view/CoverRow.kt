package voice.features.playbackScreen.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.ImmutableFile
import voice.core.ui.formatTime
import voice.features.playbackScreen.BookPlayViewState

@Composable
internal fun CoverRow(
  cover: ImmutableFile?,
  sleepTimerState: BookPlayViewState.SleepTimerViewState,
  onPlayClick: () -> Unit,
  playedTime: kotlin.time.Duration,
  duration: kotlin.time.Duration,
  playing: Boolean,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  Box(modifier) {
    if (context.resources.getIdentifier("recorder", "drawable", context.packageName) != 0) {
      ReelToReelPlayer(
        playedTime = playedTime,
        duration = duration,
        playing = playing,
        onDoubleClick = onPlayClick,
      )
    } else {
      Cover(onDoubleClick = onPlayClick, cover = cover)
    }

    when (sleepTimerState) {
      BookPlayViewState.SleepTimerViewState.Disabled -> Unit
      is BookPlayViewState.SleepTimerViewState.Enabled -> {
        Text(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 8.dp)
            .background(
              color = Color(0x7E000000),
              shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
          text = when (sleepTimerState) {
            is BookPlayViewState.SleepTimerViewState.Enabled.WithDuration -> formatTime(
              timeMs = sleepTimerState.leftDuration.inWholeMilliseconds,
            )
            BookPlayViewState.SleepTimerViewState.Enabled.WithEndOfChapter -> stringResource(R.string.end_of_chapter)
          },
          color = Color.White,
        )
      }
    }
  }
}
