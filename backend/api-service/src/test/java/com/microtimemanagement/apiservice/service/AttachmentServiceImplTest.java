package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.AttachmentDTO;
import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.model.Attachment;
import com.microtimemanagement.apiservice.repository.AttachmentRepository;
import com.microtimemanagement.apiservice.service.impl.AttachmentServiceImpl;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Attachment Service Tests")
@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    private static final String UID = "owner-uid-1";

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(currentUserProvider.currentUid()).thenReturn(UID);
    }

    @Test
    @DisplayName("create stamps the current user and persists the attachment")
    void createStampsOwner() {
        Mockito.when(attachmentRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        AttachmentDTO dto = attachmentService.create(AttachmentDTO.builder()
                .parentType(AttachmentOwnerType.ACTIVITY).parentId("a1")
                .name("shot.png").contentType("image/png").sizeBytes(1024L)
                .dataBase64("data:image/png;base64,AAAA").build());

        assertThat(dto.getName()).isEqualTo("shot.png");
        ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
        Mockito.verify(attachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getUid()).isEqualTo(UID);
    }

    @Test
    @DisplayName("create rejects files larger than 5 MB")
    void rejectsTooLarge() {
        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> attachmentService.create(AttachmentDTO.builder()
                        .parentType(AttachmentOwnerType.ACTIVITY).parentId("a1")
                        .sizeBytes(6L * 1024 * 1024).build()));
        Mockito.verify(attachmentRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @DisplayName("list is scoped to the current user + parent")
    void listScoped() {
        Mockito.when(attachmentRepository
                        .findByParentTypeAndParentIdAndUidAndIsActiveTrueOrderByCreatedAtAsc(
                                AttachmentOwnerType.ACTIVITY, "a1", UID))
                .thenReturn(List.of(Attachment.builder().id("f1").name("a").uid(UID).build()));

        assertThat(attachmentService.listForParent(AttachmentOwnerType.ACTIVITY, "a1"))
                .extracting(AttachmentDTO::getId).containsExactly("f1");
    }
}
