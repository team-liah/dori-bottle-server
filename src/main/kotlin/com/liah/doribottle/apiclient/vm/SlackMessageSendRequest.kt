package com.liah.doribottle.apiclient.vm

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.liah.doribottle.constant.DoriConstant
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.machine.dto.MachineDto

data class SlackMessageSendRequest(
    val blocks: List<Block>,
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Block(
        val type: String,
        val text: Text? = null,
        val accessory: Accessory? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Text(
        val type: String,
        val emoji: Boolean? = null,
        val text: String,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Accessory(
        val type: String,
        @JsonProperty("image_url")
        val imageUrl: String? = null,
        @JsonProperty("alt_text")
        val altText: String? = null,
        val text: Text? = null,
        val value: String? = null,
        val url: String? = null,
        @JsonProperty("action_id")
        val actionId: String? = null,
    )

    companion object {
        fun forMachine(
            type: SlackMessageType,
            adminBaseUrl: String,
            machine: MachineDto,
        ): SlackMessageSendRequest {
            val machineDetailLink =
                when (machine.type) {
                    MachineType.VENDING -> "$adminBaseUrl/machine/vending/edit/${machine.id}"
                    MachineType.COLLECTION -> "$adminBaseUrl/machine/collection/edit/${machine.id}"
                    else -> "https://www.admin.doribottle-id.co.kr"
                }
            val title =
                when (type) {
                    SlackMessageType.MACHINE_LACK_OF_CUP -> "컵 부족 (${machine.cupAmounts.toFloat() / machine.capacity * 100}%)"
                    SlackMessageType.MACHINE_FULL_OF_CUP -> "컵 포화 (${machine.cupAmounts.toFloat() / machine.capacity * 100}%)"
                    SlackMessageType.MACHINE_STATE_CHANGE -> "상태 변경"
                }

            return SlackMessageSendRequest(
                blocks =
                    listOf(
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "plain_text",
                                    emoji = true,
                                    text = "⚠\uFE0F ${machine.type.title} $title",
                                ),
                        ),
                        Block(
                            type = "divider",
                        ),
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "mrkdwn",
                                    text =
                                        "*<$machineDetailLink|${machine.name}>*\n" +
                                            "*No* : ${machine.no}\n" +
                                            "*주소* : ${machine.address.address1} ${machine.address.address2}\n" +
                                            "*컵 개수* : ${machine.cupAmounts} / ${machine.capacity}\n" +
                                            "*상태* : ${machine.state.title}",
                                ),
                            accessory =
                                Accessory(
                                    type = "image",
                                    imageUrl =
                                        machine.imageUrl
                                            ?: if (machine.type == MachineType.VENDING) {
                                                DoriConstant.RENTAL_MACHINE_DEFAULT_IMAGE_URL
                                            } else {
                                                DoriConstant.RETURN_MACHINE_DEFAULT_IMAGE_URL
                                            },
                                    altText = "Machine image",
                                ),
                        ),
                    ),
            )
        }
    }
}
