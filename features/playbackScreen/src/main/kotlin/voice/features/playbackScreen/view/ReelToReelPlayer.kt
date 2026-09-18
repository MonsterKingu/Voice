package voice.features.playbackScreen.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

private const val RECORDER_WIDTH = 704f
private const val RECORDER_HEIGHT = 753f
private const val REEL_SIZE = 330f

private const val LEFT_REEL_CENTER_X = 206f
private const val LEFT_REEL_CENTER_Y = 260f
private const val RIGHT_REEL_CENTER_X = 502f
private const val RIGHT_REEL_CENTER_Y = 260f

private const val CORE_RADIUS_FACTOR = 0.22f
private const val OUTER_RADIUS_FACTOR = 0.50f
private const val BASE_DEGREES_PER_SECOND = 180f

@Composable
internal fun ReelToReelPlayer(
  playedTime: Duration,
  duration: Duration,
  playing: Boolean,
  onDoubleClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val progress = if (duration > Duration.ZERO) {
    (playedTime / duration).toFloat().coerceIn(0f, 1f)
  } else {
    0f
  }

  var leftRotation by remember { mutableFloatStateOf(0f) }
  var rightRotation by remember { mutableFloatStateOf(0f) }
  val currentProgress by rememberUpdatedState(progress)

  LaunchedEffect(playing) {
    if (!playing) return@LaunchedEffect

    var previousFrameNanos = 0L
    while (isActive) {
      withFrameNanos { frameNanos ->
        if (previousFrameNanos != 0L) {
          val deltaSeconds = (frameNanos - previousFrameNanos) / 1_000_000_000f
          leftRotation += reelDegreesPerSecond(currentProgress, left = true) * deltaSeconds
          rightRotation += reelDegreesPerSecond(currentProgress, left = false) * deltaSeconds
        }
        previousFrameNanos = frameNanos
      }
    }
  }

  val context = LocalContext.current
  val leftReel = reelResourceId(context, reelPercentage(progress, left = true))
  val rightReel = reelResourceId(context, reelPercentage(progress, left = false))
  val recorder = resourceId(context, "recorder")

  Box(
    modifier = modifier.pointerInput(Unit) {
      detectTapGestures(onDoubleTap = { onDoubleClick() })
    },
  ) {
    BoxWithConstraints {
      val scale = min(
        maxWidth.value / RECORDER_WIDTH,
        maxHeight.value / RECORDER_HEIGHT,
      )

      Box(
        modifier = Modifier.size(
          width = (RECORDER_WIDTH * scale).dp,
          height = (RECORDER_HEIGHT * scale).dp,
        ),
      ) {
        if (recorder != 0) {
          Image(
            painter = painterResource(recorder),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize(),
          )
        }

        if (leftReel != 0) {
          Image(
            painter = painterResource(leftReel),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
              .size((REEL_SIZE * scale).dp)
              .offset(
                x = ((LEFT_REEL_CENTER_X - REEL_SIZE / 2f) * scale).dp,
                y = ((LEFT_REEL_CENTER_Y - REEL_SIZE / 2f) * scale).dp,
              )
              .graphicsLayer { rotationZ = -leftRotation },
          )
        }

        if (rightReel != 0) {
          Image(
            painter = painterResource(rightReel),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
              .size((REEL_SIZE * scale).dp)
              .offset(
                x = ((RIGHT_REEL_CENTER_X - REEL_SIZE / 2f) * scale).dp,
                y = ((RIGHT_REEL_CENTER_Y - REEL_SIZE / 2f) * scale).dp,
              )
              .graphicsLayer { rotationZ = -rightRotation },
          )
        }
      }
    }
  }
}

private fun reelDegreesPerSecond(progress: Float, left: Boolean): Float {
  val tapeFraction = if (left) 1f - progress else progress
  val radius = CORE_RADIUS_FACTOR + (OUTER_RADIUS_FACTOR - CORE_RADIUS_FACTOR) * tapeFraction
  val middleRadius = (CORE_RADIUS_FACTOR + OUTER_RADIUS_FACTOR) / 2f
  return BASE_DEGREES_PER_SECOND * middleRadius / max(radius, 0.01f)
}

private fun reelPercentage(progress: Float, left: Boolean): Int {
  val tapeProgress = if (left) 1f - progress else progress
  return when {
    tapeProgress < 0.125f -> 0
    tapeProgress < 0.375f -> 25
    tapeProgress < 0.625f -> 50
    tapeProgress < 0.875f -> 75
    else -> 100
  }
}

private fun resourceId(context: android.content.Context, name: String): Int =
  context.resources.getIdentifier(name, "drawable", context.packageName)

private fun reelResourceId(context: android.content.Context, percentage: Int): Int =
  resourceId(context, "reel_$percentage")
