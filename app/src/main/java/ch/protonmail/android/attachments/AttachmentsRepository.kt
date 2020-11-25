/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package ch.protonmail.android.attachments

import ch.protonmail.android.activities.messageDetails.repository.MessageDetailsRepository
import ch.protonmail.android.api.ProtonMailApiManager
import ch.protonmail.android.api.models.AttachmentHeaders
import ch.protonmail.android.api.models.room.messages.Attachment
import ch.protonmail.android.api.models.room.messages.Message
import ch.protonmail.android.core.Constants
import ch.protonmail.android.core.UserManager
import ch.protonmail.android.crypto.AddressCrypto
import kotlinx.coroutines.withContext
import me.proton.core.util.kotlin.DispatcherProvider
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class AttachmentsRepository @Inject constructor(
    val dispatchers: DispatcherProvider,
    private val apiManager: ProtonMailApiManager,
    private val armorer: Armorer,
    private val messageDetailsRepository: MessageDetailsRepository,
    private val userManager: UserManager
) {

    suspend fun upload(attachment: Attachment, crypto: AddressCrypto): Result {
        val fileContent = attachment.getFileContent()
        return uploadAttachment(attachment, crypto, fileContent)
    }

    suspend fun uploadPublicKey(username: String, message: Message, crypto: AddressCrypto): Result {
        val address = userManager.getUser(username).getAddressById(message.addressID).toNewAddress()
        val primaryKey = address.keys.primaryKey
        requireNotNull(primaryKey)

        val publicKey = crypto.buildArmoredPublicKey(primaryKey.privateKey)
        val publicKeyFingerprint = crypto.getFingerprint(publicKey)
        val fingerprintCharsUppercase = publicKeyFingerprint.substring(0, 8).toUpperCase()

        val attachment = Attachment().apply {
            fileName = "publickey - " + address.email + " - 0x" + fingerprintCharsUppercase + ".asc"
            mimeType = "application/pgp-keys"
            setMessage(message)
        }
        return uploadAttachment(attachment, crypto, publicKey.toByteArray())
    }

    private suspend fun uploadAttachment(attachment: Attachment, crypto: AddressCrypto, fileContent: ByteArray) =
        withContext(dispatchers.Io) {
            val headers = attachment.headers
            val mimeType = requireNotNull(attachment.mimeType)
            val filename = requireNotNull(attachment.fileName)

            val encryptedAttachment = crypto.encrypt(fileContent, filename)
            val signedFileContent = armorer.unarmor(crypto.sign(fileContent))

            val attachmentMimeType = MediaType.parse(mimeType)
            val octetStreamMimeType = MediaType.parse("application/octet-stream")
            val keyPackage = RequestBody.create(attachmentMimeType, encryptedAttachment.keyPacket)
            val dataPackage = RequestBody.create(attachmentMimeType, encryptedAttachment.dataPacket)
            val signature = RequestBody.create(octetStreamMimeType, signedFileContent)

            val response = if (isAttachmentInline(headers)) {
                requireNotNull(headers)

                apiManager.uploadAttachmentInlineBlocking(
                    attachment,
                    attachment.messageId,
                    contentIdFormatted(headers),
                    keyPackage,
                    dataPackage,
                    signature
                )
            } else {
                apiManager.uploadAttachmentBlocking(
                    attachment,
                    keyPackage,
                    dataPackage,
                    signature
                )
            }

            if (response.code == Constants.RESPONSE_CODE_OK) {
                attachment.attachmentId = response.attachmentID
                attachment.keyPackets = response.attachment.keyPackets
                attachment.signature = response.attachment.signature
                attachment.isUploaded = true
                messageDetailsRepository.saveAttachment(attachment)
                return Result.Success
            }

            return Result.Failure(response.error)
        }

    private fun contentIdFormatted(headers: AttachmentHeaders): String {
        val contentId = headers.contentId
        val parts = contentId.split("<").dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size > 1) {
            return parts[1].replace(">", "")
        }
        return contentId
    }

    private fun isAttachmentInline(headers: AttachmentHeaders?) =
        headers != null &&
            headers.contentDisposition.contains("inline") &&
            headers.contentId != null


    sealed class Result {
        object Success : Result()
        data class Failure(val error: String) : Result()
    }

}
