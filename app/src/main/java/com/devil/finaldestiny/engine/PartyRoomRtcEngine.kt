package com.devil.finaldestiny.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RtcClientRole {
    HOST,
    SPEAKER,
    AUDIENCE
}

data class SpeakerVolumeInfo(
    val userId: String,
    val volume: Int,
    val isSpeaking: Boolean
)

class PartyRoomRtcEngine(
    private val context: Context,
    private val appId: String = "a026a65cee5e416ab0a311084b95ce35"
) {

    private val _currentRole = MutableStateFlow(RtcClientRole.AUDIENCE)
    val currentRole: StateFlow<RtcClientRole> = _currentRole.asStateFlow()

    private val _isJoinedRoom = MutableStateFlow(false)
    val isJoinedRoom: StateFlow<Boolean> = _isJoinedRoom.asStateFlow()

    private val _isMicrophoneMuted = MutableStateFlow(false)
    val isMicrophoneMuted: StateFlow<Boolean> = _isMicrophoneMuted.asStateFlow()

    private val _activeSpeakers = MutableStateFlow<List<SpeakerVolumeInfo>>(emptyList())
    val activeSpeakers: StateFlow<List<SpeakerVolumeInfo>> = _activeSpeakers.asStateFlow()

    var onUserJoined: ((String, RtcClientRole) -> Unit)? = null
    var onUserLeft: ((String) -> Unit)? = null

    fun initializeEngine() {
        // RTC Engine Initialization Hook (e.g. RtcEngine.create(context, appId, eventHandler))
    }

    fun joinPartyRoom(channelId: String, userId: String, role: RtcClientRole, token: String? = null) {
        _currentRole.value = role
        _isJoinedRoom.value = true

        // Configure client role and enable audio volume indication
        setClientRole(role)
        enableAudioVolumeIndication(200, 3)
    }

    fun switchClientRole(newRole: RtcClientRole) {
        _currentRole.value = newRole
        setClientRole(newRole)
        if (newRole == RtcClientRole.AUDIENCE) {
            muteLocalAudioStream(true)
        } else {
            muteLocalAudioStream(_isMicrophoneMuted.value)
        }
    }

    fun toggleMicrophoneMute(): Boolean {
        val newMuteState = !_isMicrophoneMuted.value
        _isMicrophoneMuted.value = newMuteState
        muteLocalAudioStream(newMuteState)
        return newMuteState
    }

    private fun setClientRole(role: RtcClientRole) {
        // Agora / WebRTC Engine role assignment logic
        // rtcEngine?.setClientRole(if (role == RtcClientRole.AUDIENCE) CLIENT_ROLE_AUDIENCE else CLIENT_ROLE_BROADCASTER)
    }

    private fun muteLocalAudioStream(muted: Boolean) {
        // rtcEngine?.muteLocalAudioStream(muted)
    }

    private fun enableAudioVolumeIndication(intervalMs: Int, smooth: Int) {
        // rtcEngine?.enableAudioVolumeIndication(intervalMs, smooth, true)
    }

    fun leavePartyRoom() {
        _isJoinedRoom.value = false
        _currentRole.value = RtcClientRole.AUDIENCE
        _isMicrophoneMuted.value = false
        _activeSpeakers.value = emptyList()
        // rtcEngine?.leaveChannel()
    }

    fun release() {
        leavePartyRoom()
        // RtcEngine.destroy()
    }
}
