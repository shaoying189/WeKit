package dev.ujhhgtg.wekit.hooks.items.contacts

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Forum
import com.composables.icons.materialsymbols.outlined.Search
import dev.ujhhgtg.wekit.hooks.api.core.WeDatabaseApi
import dev.ujhhgtg.wekit.hooks.api.core.models.MessageType
import dev.ujhhgtg.wekit.hooks.api.core.models.WeMessage
import dev.ujhhgtg.wekit.hooks.api.ui.WeContactPrefsScreenApi
import dev.ujhhgtg.wekit.hooks.core.HookItem
import dev.ujhhgtg.wekit.hooks.core.SwitchHookItem
import dev.ujhhgtg.wekit.hooks.items.chat.ChatInputBarEnhancements
import dev.ujhhgtg.wekit.ui.content.AlertDialogContent
import dev.ujhhgtg.wekit.ui.content.TextButton
import dev.ujhhgtg.wekit.ui.utils.showComposeDialog
import dev.ujhhgtg.wekit.utils.android.currentWxId
import dev.ujhhgtg.wekit.utils.android.showToast
import dev.ujhhgtg.wekit.utils.formatEpoch

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

        val groupName = WeDatabaseApi.getGroup(groupId)?.displayName ?: "<群名获取失败>"

        showToast(activity, "正在查询消息历史...")

        val messages = WeDatabaseApi.getMessagesFromSender(groupId, memberId)
        val memberName = WeDatabaseApi
            .getGroupMemberDisplayName(groupId, memberId)
            .ifEmpty { WeDatabaseApi.getFriend(memberId)?.displayName ?: "<成员名获取失败>" }

        showComposeDialog(activity) {
            AlertDialogContent(
                title = { Text("「$memberName」在「$groupName」中的消息") },
                text = {
                    MessageHistoryContent(
                        messages = messages,
                        modifier = Modifier.fillMaxSize(),
                        onMessageClick = { _ ->
                            // TODO: implement – e.g. copy content, jump to chat, show raw XML
                        }
                    )
                },
                confirmButton = { TextButton(onDismiss) { Text("关闭") } }
            )
        }

        return true
    }
}

// ─── Private composables ─────────────────────────────────────────────────────

@Composable
private fun MessageHistoryContent(
    messages: List<WeMessage>,
    modifier: Modifier = Modifier,
    onMessageClick: (WeMessage) -> Unit
) {
    if (messages.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Forum,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "暂无消息记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        return
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredMessages = remember(searchQuery, messages) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { message ->
                message.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Search Bar ────────────────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索消息内容") },
            leadingIcon = { Icon(MaterialSymbols.Outlined.Search, contentDescription = "Search") },
            singleLine = true
        )

        val textCount = remember(filteredMessages) { filteredMessages.count { it.type?.isText ?: false } }
        val otherCount = filteredMessages.size - textCount

        // ── Stats header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "共 ${filteredMessages.size} 条",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (otherCount > 0) {
                Text(
                    text = "文字 $textCount  ·  特殊 $otherCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        // ── Content container (Scroll list or Empty state) ────────────────────
        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "没有找到匹配的消息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredMessages, key = { it.msgId }) { message ->
                    MessageItem(message = message, onClick = onMessageClick)
                }
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: WeMessage,
    onClick: (WeMessage) -> Unit
) {
    val isText = message.type?.isText ?: false
    val formattedTime = remember(message.createTime) { formatEpoch(message.createTime, true) }
    val typeLabel = remember(message.typeCode) {
        message.type?.displayName ?: "未知类型: ${message.typeCode}"
    }

    val bodyText: String? = remember(message.content, message.isSend, message.typeCode) {
        when {
            isText -> message.content.stripGroupPrefix()
            message.type == MessageType.APP -> message.content
                .lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() && !it.startsWith('<') }
            else -> null
        }
    }

    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = { onClick(message) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f)
                    )
                }
            }

            if (bodyText != null) {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isText) contentColor else contentColor.copy(alpha = 0.7f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun String.stripGroupPrefix(): String {
    val nl = indexOf('\n')
    return if (nl in 1..64 && this[nl - 1] == ':') substring(nl + 1) else this
}
