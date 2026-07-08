package com.skillswap.service.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Conversation;
import com.skillswap.domain.entity.Message;
import com.skillswap.domain.entity.User;
import com.skillswap.domain.enums.MessageStatus;
import com.skillswap.domain.enums.MessageType;
import com.skillswap.domain.enums.RequestStatus;
import com.skillswap.domain.enums.SessionStatus;
import com.skillswap.dto.chat.OnlineStatusDTO;
import com.skillswap.dto.chat.ReadReceiptDTO;
import com.skillswap.dto.chat.TypingIndicatorDTO;
import com.skillswap.dto.request.chat.CreateMessageRequest;
import com.skillswap.dto.response.chat.ConversationResponse;
import com.skillswap.dto.response.chat.MessageResponse;
import com.skillswap.repository.ConversationRepository;
import com.skillswap.repository.MessageRepository;
import com.skillswap.repository.SessionRepository;
import com.skillswap.repository.SkillRequestRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.user.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default real-time chat service implementation.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Duration EDIT_WINDOW = Duration.ofMinutes(15);
    private static final Duration ONLINE_TTL = Duration.ofMinutes(5);
    private static final String ONLINE_KEY_PREFIX = "chat:online:";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int MAX_ATTACHMENT_URL_LENGTH = 500;
    private static final Set<SessionStatus> ACTIVE_SESSION_STATUSES = Set.of(
            SessionStatus.ACCEPTED,
            SessionStatus.RESCHEDULED);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SkillRequestRepository skillRequestRepository;
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    public ChatServiceImpl(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            SkillRequestRepository skillRequestRepository,
            SessionRepository sessionRepository,
            SimpMessagingTemplate messagingTemplate,
            StringRedisTemplate redisTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.skillRequestRepository = skillRequestRepository;
        this.sessionRepository = sessionRepository;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(CreateMessageRequest request) {
        User sender = findUser(currentUserId());
        User receiver = findUser(request.receiverId());
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Cannot send a message to yourself");
        }
        validateChatAllowed(sender.getId(), receiver.getId());
        validateMessagePayload(request);

        Conversation conversation = resolveConversation(sender, receiver, request.conversationId());
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(resolveContent(request))
                .attachmentUrl(normalizeNullable(request.attachmentUrl()))
                .messageType(request.messageType())
                .status(MessageStatus.SENT)
                .edited(false)
                .deleted(false)
                .build();
        Message saved = messageRepository.save(message);
        conversation.setLastMessage(saved);
        conversation.setUnreadCount((int) messageRepository.countByConversationIdAndReceiverIdAndStatusAndDeletedFalse(
                conversation.getId(),
                receiver.getId(),
                MessageStatus.SENT));
        conversationRepository.save(conversation);

        MessageResponse response = toMessageResponse(saved);
        messagingTemplate.convertAndSend("/topic/messages/" + conversation.getId(), response);
        messagingTemplate.convertAndSend("/topic/messages", response);
        return response;
    }

    @Override
    @Transactional
    public MessageResponse editMessage(Long messageId, CreateMessageRequest request) {
        Message message = findMessage(messageId);
        requireSender(message);
        if (message.isDeleted()) {
            throw new BadRequestException("Deleted messages cannot be edited");
        }
        if (message.getCreatedAt().plus(EDIT_WINDOW).isBefore(Instant.now())) {
            throw new BadRequestException("Messages can only be edited within 15 minutes");
        }
        validateMessagePayload(request);
        message.setContent(resolveContent(request));
        message.setAttachmentUrl(normalizeNullable(request.attachmentUrl()));
        message.setMessageType(request.messageType());
        message.setEdited(true);
        Message saved = messageRepository.save(message);
        MessageResponse response = toMessageResponse(saved);
        messagingTemplate.convertAndSend("/topic/messages/" + saved.getConversation().getId(), response);
        return response;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = findMessage(messageId);
        requireSender(message);
        message.setDeleted(true);
        message.setContent(null);
        message.setAttachmentUrl(null);
        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/messages/" + message.getConversation().getId(), toMessageResponse(message));
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId) {
        Conversation conversation = findConversation(conversationId);
        requireParticipant(conversation);
        Long readerId = currentUserId();
        messageRepository.markConversationAsRead(conversationId, readerId, MessageStatus.SENT, MessageStatus.READ);
        conversation.setUnreadCount((int) messageRepository.countByConversationIdAndReceiverIdAndStatusAndDeletedFalse(
                conversationId,
                readerId,
                MessageStatus.SENT));
        conversationRepository.save(conversation);
        publishReadReceipt(new ReadReceiptDTO(conversationId, readerId));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long conversationId) {
        Conversation conversation = findConversation(conversationId);
        requireParticipant(conversation);
        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversations(Integer page, Integer size) {
        return conversationRepository.findRecentByUser(currentUserId(), pageRequest(page, size))
                .map(this::toConversationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationHistory(Long conversationId, Integer page, Integer size) {
        Conversation conversation = findConversation(conversationId);
        requireParticipant(conversation);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageRequest(page, size))
                .map(this::toMessageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> searchMessages(Long conversationId, String query, Integer page, Integer size) {
        Conversation conversation = findConversation(conversationId);
        requireParticipant(conversation);
        String normalizedQuery = normalizeNullable(query);
        if (normalizedQuery == null) {
            throw new BadRequestException("Search query is required");
        }
        return messageRepository.searchMessages(conversationId, normalizedQuery, pageRequest(page, size))
                .map(this::toMessageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return messageRepository.countByReceiverIdAndStatusAndDeletedFalse(currentUserId(), MessageStatus.SENT);
    }

    @Override
    public void publishTyping(TypingIndicatorDTO indicator) {
        if (!indicator.senderId().equals(currentUserId())) {
            throw new AccessDeniedException("Cannot send typing indicator for another user");
        }
        Conversation conversation = findConversation(indicator.conversationId());
        requireParticipant(conversation);
        messagingTemplate.convertAndSend("/topic/typing/" + indicator.conversationId(), indicator);
        messagingTemplate.convertAndSend("/topic/typing", indicator);
    }

    @Override
    @Transactional
    public void publishReadReceipt(ReadReceiptDTO receipt) {
        if (!receipt.readerId().equals(currentUserId())) {
            throw new AccessDeniedException("Cannot send read receipt for another user");
        }
        Conversation conversation = findConversation(receipt.conversationId());
        requireParticipant(conversation);
        messagingTemplate.convertAndSend("/topic/messages/" + receipt.conversationId() + "/read", receipt);
    }

    @Override
    public void markOnline(Long userId) {
        redisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, "online", ONLINE_TTL);
        messagingTemplate.convertAndSend("/topic/status", new OnlineStatusDTO(userId, true, Instant.now()));
    }

    @Override
    public void markOffline(Long userId) {
        redisTemplate.delete(ONLINE_KEY_PREFIX + userId);
        messagingTemplate.convertAndSend("/topic/status", new OnlineStatusDTO(userId, false, Instant.now()));
    }

    private Conversation resolveConversation(User sender, User receiver, Long conversationId) {
        if (conversationId != null) {
            Conversation conversation = findConversation(conversationId);
            requireParticipant(conversation);
            if (!isParticipant(conversation, receiver.getId())) {
                throw new BadRequestException("Receiver is not part of this conversation");
            }
            return conversation;
        }
        return conversationRepository.findBetweenUsers(sender.getId(), receiver.getId())
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .participantOne(sender)
                        .participantTwo(receiver)
                        .build()));
    }

    private void validateChatAllowed(Long firstUserId, Long secondUserId) {
        boolean acceptedRequest = skillRequestRepository.existsAcceptedBetweenUsers(
                firstUserId,
                secondUserId,
                RequestStatus.ACCEPTED);
        boolean activeSession = sessionRepository.existsActiveBetweenUsers(
                firstUserId,
                secondUserId,
                ACTIVE_SESSION_STATUSES);
        if (!acceptedRequest && !activeSession) {
            throw new AccessDeniedException("Chat requires an accepted skill request or active session");
        }
    }

    private void validateMessagePayload(CreateMessageRequest request) {
        String content = normalizeNullable(request.content());
        String attachmentUrl = normalizeNullable(request.attachmentUrl());
        if (content == null && attachmentUrl == null) {
            throw new BadRequestException("Message content or attachment is required");
        }
        if (request.messageType() == MessageType.TEXT && content == null) {
            throw new BadRequestException("Text messages require content");
        }
        if ((request.messageType() == MessageType.IMAGE || request.messageType() == MessageType.FILE)
                && attachmentUrl == null) {
            throw new BadRequestException("Attachment URL is required for file and image messages");
        }
        if (attachmentUrl != null && attachmentUrl.length() > MAX_ATTACHMENT_URL_LENGTH) {
            throw new BadRequestException("Attachment URL is too long");
        }
    }

    private String resolveContent(CreateMessageRequest request) {
        if (request.messageType() == MessageType.VOICE_NOTE && request.voiceNoteMetadata() != null) {
            return normalizeNullable(request.voiceNoteMetadata());
        }
        return normalizeNullable(request.content());
    }

    private Conversation findConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    private Message findMessage(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void requireSender(Message message) {
        if (!message.getSender().getId().equals(currentUserId())) {
            throw new AccessDeniedException("Only the sender can modify this message");
        }
    }

    private void requireParticipant(Conversation conversation) {
        if (!isParticipant(conversation, currentUserId())) {
            throw new AccessDeniedException("Conversation is not visible to this user");
        }
    }

    private boolean isParticipant(Conversation conversation, Long userId) {
        return conversation.getParticipantOne().getId().equals(userId)
                || conversation.getParticipantTwo().getId().equals(userId);
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        Long userId = currentUserId();
        return new ConversationResponse(
                conversation.getId(),
                conversation.getParticipantOne().getId(),
                conversation.getParticipantOne().getFullName(),
                conversation.getParticipantTwo().getId(),
                conversation.getParticipantTwo().getFullName(),
                conversation.getLastMessage() == null ? null : toMessageResponse(conversation.getLastMessage()),
                messageRepository.countByConversationIdAndReceiverIdAndStatusAndDeletedFalse(
                        conversation.getId(),
                        userId,
                        MessageStatus.SENT),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getReceiver().getId(),
                message.getReceiver().getFullName(),
                message.isDeleted() ? null : message.getContent(),
                message.isDeleted() ? null : message.getAttachmentUrl(),
                message.getMessageType(),
                message.getStatus(),
                message.isEdited(),
                message.isDeleted(),
                message.getReadAt(),
                message.getCreatedAt(),
                message.getUpdatedAt());
    }

    private Pageable pageRequest(Integer page, Integer size) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AccessDeniedException("Authenticated user is required");
        }
        return principal.getId();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
