package com.gallery.thekami.ui.viewer

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackErrorTest {
    @Test
    fun missingFile() {
        assertEquals(PlaybackError.Missing, classifyPlaybackError(PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND))
    }

    @Test
    fun unsupportedCodecOrResolution() {
        assertEquals(PlaybackError.Unsupported, classifyPlaybackError(PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED))
        assertEquals(PlaybackError.Unsupported, classifyPlaybackError(PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES))
        assertEquals(PlaybackError.Unsupported, classifyPlaybackError(PlaybackException.ERROR_CODE_DECODER_INIT_FAILED))
    }

    @Test
    fun everythingElseIsGeneric() {
        assertEquals(PlaybackError.Generic, classifyPlaybackError(PlaybackException.ERROR_CODE_UNSPECIFIED))
    }
}
