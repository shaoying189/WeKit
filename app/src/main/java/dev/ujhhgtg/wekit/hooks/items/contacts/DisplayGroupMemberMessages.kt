package dev.ujhhgtg.wekit.hooks.items.contacts

import android.app.Activity
import android.content.Intent
import com.tencent.mm.chatroom.ui.SelectedMemberChattingRecordUI
import dev.ujhhgtg.wekit.hooks.api.ui.WeContactPrefsScreenApi
import dev.ujhhgtg.wekit.hooks.core.HookItem
import dev.ujhhgtg.wekit.hooks.core.SwitchHookItem
import dev.ujhhgtg.wekit.hooks.items.chat.ChatInputBarEnhancements
import dev.ujhhgtg.wekit.utils.android.currentWxId

@HookItem(
    name = "查看群成员消息历史",
    categories = ["联系人与群组", "联系人详情页面"],
    description = "在联系人与群组详情页面添加入口, 可查看任意群成员的全部历史消息"
)
object DisplayGroupMemberMessages : SwitchHookItem(), WeContactPrefsScreenApi.IContactInfoProvider {

    private const val PREF_KEY = "member_msg"

    override fun onEnable() {
        WeContactPrefsScreenApi.addProvider(this)
    }

    override fun onDisable() {
        WeContactPrefsScreenApi.removeProvider(this)
    }

    override fun getContactInfoItem(activity: Activity): List<WeContactPrefsScreenApi.ContactInfoItem> {
        val wxId = activity.currentWxId ?: return emptyList()
        if (wxId.endsWith("@chatroom")) return emptyList()

        return listOf(
            WeContactPrefsScreenApi.ContactInfoItem(
                key = PREF_KEY,
                title = "查看群消息历史",
                position = 1
            )
        )
    }

    override fun onItemClick(activity: Activity, key: String): Boolean {
        if (key != PREF_KEY) return false

        val groupId = ChatInputBarEnhancements.currentConv
        val memberId = activity.currentWxId ?: return true

        activity.startActivity(Intent(activity, SelectedMemberChattingRecordUI::class.java).apply {
            putExtra("RoomInfo_Id", groupId)
            putExtra("room_member", memberId)
            putExtra("title", "查看群成员消息历史")
        })

        return true
    }
}
