package dev.ujhhgtg.wekit.features.items.chat

import android.content.Context
import android.view.MenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.ujhhgtg.reflekt.reflekt
import dev.ujhhgtg.wekit.dexkit.abc.IResolveDex
import dev.ujhhgtg.wekit.dexkit.dsl.dexMethod
import dev.ujhhgtg.wekit.features.core.ClickableFeature
import dev.ujhhgtg.wekit.features.core.Feature
import dev.ujhhgtg.wekit.preferences.WePrefs.Companion.prefOption
import dev.ujhhgtg.wekit.ui.content.AlertDialogContent
import dev.ujhhgtg.wekit.ui.content.Button
import dev.ujhhgtg.wekit.ui.content.TextButton
import dev.ujhhgtg.wekit.ui.utils.showComposeDialog
import dev.ujhhgtg.wekit.utils.reflection.BInt

@Feature(name = "移除消息菜单项", categories = ["聊天"], description = "从消息的长按菜单中移除指定名称的菜单项")
object RemoveChatMessageContextMenuItems : ClickableFeature(), IResolveDex {

    // although there are multiple addMenuItem() methods, i only found the usage of those two in the context menu of chat messages
    private val methodAddMenuItem1 by dexMethod {
        matcher {
            declaredClass {
                addFieldForType(List::class.javaObjectType)
                addFieldForType(CharSequence::class.java)
                addFieldForType(Context::class.java)
            }

            name = "add"
            paramTypes(
                BInt,
                BInt,
                BInt,
                CharSequence::class.java
            )
            returnType(MenuItem::class.java)
        }
    }
    private val methodAddMenuItem2 by dexMethod {
        matcher {
            declaredClass(methodAddMenuItem1.method.declaringClass)
            paramTypes(
                BInt,
                BInt,
                BInt,
                CharSequence::class.java,
                BInt
            )
            returnType(MenuItem::class.java)
        }
    }

    private var removedItemNames by prefOption(
        "removed_menu_item_names",
        "收藏,总结,提醒,翻译,搜一搜,编辑,打开,相关表情,合拍,查看专辑,静音播放,听筒播放,背景播放,从当前听"
    )

    override fun onEnable() {
        listOf(methodAddMenuItem1, methodAddMenuItem2).forEach {
            it.hookAfter {
                val name = args[3] as CharSequence
                val removedNames = removedItemNames.split(',')

                if (removedNames.contains(name)) {
                    val list = thisObject.reflekt()
                        .firstField { type = List::class }
                        .get()!! as ArrayList<*>
                    list.removeAt(list.size - 1)
                }
            }
        }
    }

    override fun onClick(context: Context) {
        showComposeDialog(context) {
            var removedNames by remember { mutableStateOf(removedItemNames) }
            AlertDialogContent(
                title = { Text("移除消息菜单项") },
                text = {
                    TextField(
                        value = removedNames,
                        onValueChange = { removedNames = it },
                        label = { Text("要移除的菜单项名称 (以逗号分割):") })
                },
                dismissButton = { TextButton(onDismiss) { Text("取消") } },
                confirmButton = {
                    Button(onClick = {
                        removedItemNames = removedNames
                        onDismiss()
                    }) { Text("确定") }
                })
        }
    }
}
