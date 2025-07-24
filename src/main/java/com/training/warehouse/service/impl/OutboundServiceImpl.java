package com.training.warehouse.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.repository.UserRepository;
import com.training.warehouse.service.MailService;
import org.springframework.stereotype.Service;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.OutboundService;
import com.training.warehouse.service.PdfService;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OutboundServiceImpl implements OutboundService {
    private final OutboundRepository outboundRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
//    private final InboundRepository inboundRepository;
//    private final InboundAttachmentRepository inboundAttachmentRepository;
//    private final FileStoreService fileStoreService;
//    private final PdfService pdfService;

    @Override
    public byte[] confirmOutboundById(long outboundId) {
        return new byte[0];
    }

    @Override
    @Transactional
    public void alertDelayedOutbounds() {
        List<OutboundEntity> delayedOutbounds = outboundRepository.findAllRiskDelayedOutbounds();
        for(OutboundEntity delayOutbound : delayedOutbounds){

            LocalDateTime expectedDate = delayOutbound.getExpectedShippingDate();
            UserEntity user = delayOutbound.getUser();
            System.out.println(user);
            String content = "Outbound is expected at "+expectedDate;
//            mailService.sendMail(user.getEmail(),"Risk Delayed Outbound",content);
            System.out.println("Gửi mail tới user: "+user.getId()+" "+user.getEmail());
        }
    }
//
//    @Override
//    public byte[] confirmOutboundById(long outboundId) {
//        Optional<OutboundEntity> outboundResult = this.outboundRepository.findById(outboundId);
//        if (!outboundResult.isPresent()) {
//            throw new NotFoundException(ExceptionMessage.OUTBOUND_NOT_FOUND);
//        }
//        OutboundEntity outbound = outboundResult.get();
//        if (outbound.isConfirmed()) {
//            throw new BadRequestException(ExceptionMessage.OUTBOUND_CONFIRMED);
//        }
//        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(outbound.getInboundId());
//        if (!inboundResult.isPresent()) {
//            throw new NotFoundException(ExceptionMessage.INBOUND_NOT_FOUND);
//        }
//        List<InboundAttachmentEntity> inboundAttachments = this.inboundAttachmentRepository
//                .findByInboundId(outbound.getInboundId());
//        Map<String, byte[]> files = new HashMap<>();
//        inboundAttachments.forEach((attachment) -> {
//            byte[] file = fileStoreService.getFile(
//                    FileStoreService.INBOUND_BUCKET,
//                    attachment.getFilePath(),
//                    attachment.getFileName());
//            files.put(attachment.getFileName(), file);
//        });
//        byte[] mergedFile = this.pdfService.mergeWithBookmarks(files);
//        outbound.setConfirmed(true);
//        this.outboundRepository.save(outbound);
//        return mergedFile;
//    }
}
