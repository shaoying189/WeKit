package dev.ujhhgtg.wekit.hooks.items.chat

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.ujhhgtg.wekit.dexkit.abc.IResolvesDex
import dev.ujhhgtg.wekit.dexkit.dsl.dexMethod
import dev.ujhhgtg.wekit.hooks.core.ClickableHookItem
import dev.ujhhgtg.wekit.hooks.core.HookItem
import dev.ujhhgtg.wekit.preferences.WePrefs
import dev.ujhhgtg.wekit.ui.content.AlertDialogContent
import dev.ujhhgtg.wekit.ui.content.Button
import dev.ujhhgtg.wekit.ui.content.TextButton
import dev.ujhhgtg.wekit.ui.utils.showComposeDialog
import dev.ujhhgtg.wekit.utils.android.showToast
import org.luckypray.dexkit.DexKitBridge

@HookItem(path = "聊天/伪装语音时长", description = "预设定伪装发送语音显示的时长")
object FakeVoiceDuration : ClickableHookItem(), IResolvesDex {

    private val methodVoiceRecorderGetLength by dexMethod()
    private const val KEY_DURATION = "fake_voice_duration"

    override fun onEnable() {
        methodVoiceRecorderGetLength.hookBefore {
            result = WePrefs.getLongOrDef(KEY_DURATION, 0L)
        }
    }

    override fun onClick(context: Context) {
        showComposeDialog(context) {
            var durationInput by remember { mutableStateOf(WePrefs.getLongOrDef(KEY_DURATION, 0).toString()) }
            AlertDialogContent(
                title = { Text("伪装语音时长") },
                text = {
                    TextField(
                        value = durationInput,
                        onValueChange = { durationInput = it.filter { c -> c.isDigit() } },
                        label = { Text("语音时长 (毫秒)") })
                },
                dismissButton = {
                    TextButton(onDismiss) { Text("取消") }
                },
                confirmButton = {
                    Button(onClick = {
                        val durationMs = durationInput.toLongOrNull()
                        if (durationMs == null) {
                            showToast("时长格式不正确!")
                            return@Button
                        }

                        WePrefs.putLong(KEY_DURATION, durationMs)
                        onDismiss()
                    }) { Text("确定") }
                })
        }
    }

    override fun resolveDex(dexKit: DexKitBridge) {
        methodVoiceRecorderGetLength.find(dexKit) {
            matcher {
                declaredClass {
                    usingEqStrings("MicroMsg.SceneVoice.Recorder", "Stop file success: ")
                }
                returnType = "long"
            }
        }
    }
}
