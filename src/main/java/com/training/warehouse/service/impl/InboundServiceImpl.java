package com.training.warehouse.service.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;

import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.repository.projection.InboundProjection;
import com.training.warehouse.repository.projection.InventoryProjection;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.InboundService;

import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.dto.request.GetInboundsRequest;
import com.training.warehouse.dto.request.GetInventoryRequest;
import com.training.warehouse.dto.request.ImportInboundDataRequest;
import com.training.warehouse.dto.request.UpdateInboundByIdRequest;
import com.training.warehouse.dto.response.CreateInboundResponse;
import com.training.warehouse.dto.response.GetInboundAttachmentDownloadUrlResponse;
import com.training.warehouse.dto.response.GetInboundByIdResponse;
import com.training.warehouse.dto.response.GetInboundsResponse;
import com.training.warehouse.dto.response.GetInventoryResponse;
import com.training.warehouse.dto.response.ImportInboundDataResponse;
import com.training.warehouse.dto.response.UpdateInboundByIdResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;

@Service
@AllArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final FileStoreService fileStoreService;
    private final InboundRepository inboundRepository;
    private final OutboundRepository outboundRepository;
    private final InboundAttachmentRepository inboundAttachmentRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInboundById(long inboundId) {
        Optional<InboundEntity> inboundResult = inboundRepository.findById(inboundId);
        if (!inboundResult.isPresent()) {
            throw new NotFoundException("inbound not found");
        }
        List<OutboundEntity> outboundEntities = outboundRepository.findByInboundId(inboundId);
        if (outboundEntities.size() > 0) {
            throw new BadRequestException("cannot delete inbound");
        }
        InboundEntity inbound = inboundResult.get();
        List<InboundAttachmentEntity> attachments = inbound.getAttachments();
        attachments.forEach(attachment -> {
            inboundAttachmentRepository.deleteById(attachment.getId());
        });
        inboundRepository.deleteById(inboundId);
        attachments.forEach(attachment -> {
            fileStoreService.deleteFile(FileStoreService.INBOUND_BUCKET, attachment.getFilePath(),
                    attachment.getFileName());
        });
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateInboundResponse createInbound(UserEntity user, CreateInboundRequest request) {
        Optional<InboundEntity> existingInbound = this.inboundRepository.findByInvoice(request.getInvoice());
        if (existingInbound.isPresent()) {
            throw new BadRequestException("invoice already exists");
        }
        List<String> filePaths = new ArrayList<>();
        request.getAttachments().stream().forEach((e) -> {
            String path = UUID.randomUUID().toString();
            filePaths.add(path);
            this.fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET, path, e);
        });
        InboundEntity newInbound = this.inboundRepository.save(
            InboundEntity.builder()
                .invoice(request.getInvoice())
                .productType(ProductType.fromString(request.getProductType()))
                .supplierCd(SupplierCd.fromCode(request.getSupplierCd()))
                .receiveDate(request.getReceiveDate())
                .status(OrderStatus.fromValue(request.getOrderStatus()))
                .quantity(request.getQuantity())
                .user(user)
                .build()
        );
        for (int i = 0; i < request.getAttachments().size(); i++) {
            this.inboundAttachmentRepository.save(
                InboundAttachmentEntity.builder()
                    .fileName(request.getAttachments().get(i).getOriginalFilename())
                    .filePath(filePaths.get(i))
                    .inboundId(newInbound.getId())
                    .build()
            );
        }
        return CreateInboundResponse.builder().id(newInbound.getId()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateInboundByIdResponse updateInboundById(long id, UpdateInboundByIdRequest request) {
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(id);
        if (inboundResult.isEmpty()) {
            throw new NotFoundException("inbound not found");
        }
        InboundEntity inbound = inboundResult.get();
        Optional<OutboundEntity> outboundResult = this.outboundRepository.findFirstByInboundId(inbound.getId());
        if (outboundResult.isPresent()) {
            throw new BadRequestException("inbound is not editable");
        }
        Optional<InboundEntity> existingInbound = this.inboundRepository.findByInvoice(request.getInvoice());
        if (existingInbound.isPresent() && existingInbound.get().getId() != id) {
            throw new BadRequestException("invoice already exists");
        }
        List<InboundAttachmentEntity> inboundAttachments = this.inboundAttachmentRepository.findByInboundId(inbound.getId());
        if (inboundAttachments.size() + request.getAttachments().size() > 5) {
            throw new BadRequestException("too many attachments");
        }
        List<String> filePaths = new ArrayList<>();
        request.getAttachments().stream().forEach((e) -> {
            String path = UUID.randomUUID().toString();
            filePaths.add(path);
            this.fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET, path, e);
        });
        for (int i = 0; i < request.getAttachments().size(); i++) {
            this.inboundAttachmentRepository.save(
                InboundAttachmentEntity.builder()
                    .fileName(request.getAttachments().get(i).getOriginalFilename())
                    .filePath(filePaths.get(i))
                    .inboundId(inbound.getId())
                    .build()
            );
        }
        inbound.setInvoice(request.getInvoice());
        inbound.setProductType(ProductType.fromString(request.getProductType()));
        inbound.setSupplierCd(SupplierCd.fromCode(request.getSupplierCd()));
        inbound.setReceiveDate(request.getReceiveDate());
        inbound.setStatus(OrderStatus.fromValue(request.getOrderStatus()));
        inbound.setQuantity(request.getQuantity());
        InboundEntity updatedInbound = this.inboundRepository.save(inbound);
        return UpdateInboundByIdResponse.builder()
            .id(updatedInbound.getId())
            .build();
    }

    @Override
    public GetInventoryResponse getInventory(GetInventoryRequest query) {
        List<InventoryProjection> inventories = this.inboundRepository.findInventoryNative(
            query.getLimit(), 
            query.getLimit() * (query.getPage() - 1)
        );
        long total = this.inboundRepository.countInventoryNative();
        return GetInventoryResponse.builder()
            .page(query.getPage())
            .limit(query.getLimit())
            .total(total)
            .inventories(inventories.stream().map((e) -> {
                return GetInventoryResponse.InventoryProjectionRepsonse.builder()
                    .id(e.getId())
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .invoice(e.getInvoice())
                    .productType(e.getProductType())
                    .SupplierCd(e.getSupplierCd())
                    .receiveDate(e.getReceiveDate())
                    .quantity(e.getQuantity())
                    .inventory(e.getInventory())
                    .build();
            }).collect(Collectors.toList()))
            .build();
    }

    @Override
    public GetInboundByIdResponse getInboundById(long id) {
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(id);
        if (inboundResult.isEmpty()) {
            throw new NotFoundException("inbound not found");
        }
        InboundEntity inbound = inboundResult.get();
        List<InboundAttachmentEntity> attachments = this.inboundAttachmentRepository.findByInboundId(inbound.getId());
        return GetInboundByIdResponse.builder()
            .id(inbound.getId())
            .invoice(inbound.getInvoice())
            .productType(inbound.getProductType().name())
            .supplierCd(inbound.getSupplierCd().name())
            .receiveDate(inbound.getReceiveDate())
            .orderStatus(inbound.getStatus().name())
            .quantity(inbound.getQuantity())
            .createdAt(inbound.getCreatedAt())
            .updatedAt(inbound.getUpdatedAt())
            .creator(GetInboundByIdResponse.InboundCreatorResponse.builder()
                .email(inbound.getUser().getEmail())
                .fullName(inbound.getUser().getFullName())
                .build())
            .attachments(attachments.stream().map((e) -> {
                return GetInboundByIdResponse.InboundAttachmentResponse.builder()
                    .id(e.getId())
                    .fileName(e.getFileName())
                    .build();
            }).collect(Collectors.toList()))
            .build();
    }

    @Override
    public GetInboundsResponse getInbounds(GetInboundsRequest query) {
        List<InboundProjection> inbounds = this.inboundRepository.findInboundNative(
            query.getLimit(), (query.getPage() - 1) * query.getLimit(), query.getDirection()
        );
        long total = this.inboundRepository.count();
        return GetInboundsResponse.builder()
            .page(query.getPage())
            .limit(query.getLimit())
            .total(total)
            .inbounds(inbounds.stream()
                .map((e) -> {
                    List<InboundAttachmentEntity> attachments = this.inboundAttachmentRepository.findByInboundId(e.getId());
                    return GetInboundByIdResponse.builder()
                        .id(e.getId())
                        .invoice(e.getInvoice())
                        .productType(e.getProductType())
                        .supplierCd(e.getSupplierCd())
                        .receiveDate(e.getReceiveDate())
                        .orderStatus(OrderStatus.fromValue(e.getOrderStatus()).name())
                        .quantity(e.getQuantity())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .creator(GetInboundByIdResponse.InboundCreatorResponse.builder()
                            .email(e.getCreatorEmail())
                            .fullName(e.getCreatorFullName())
                            .build())
                        .attachments(attachments.stream().map((attachment) -> {
                            return GetInboundByIdResponse.InboundAttachmentResponse.builder()
                                .id(attachment.getId())
                                .fileName(attachment.getFileName())
                                .build();
                        }).collect(Collectors.toList()))
                        .build();
                }).collect(Collectors.toList())
            ).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportInboundDataResponse importInboundData(UserEntity user, ImportInboundDataRequest request) {
        MultipartFile dataFile = request.getDataFiles().get(0);
        String fileName = dataFile.getOriginalFilename();
        List<String> allowContentType = List.of(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("filename is not valid");
        }
        String cleanedFileName = Paths.get(fileName).getFileName().toString();
        if (!cleanedFileName.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new BadRequestException("filename is not valid");
        }
        String contentType = URLConnection.guessContentTypeFromName(cleanedFileName);
        if (contentType == null || !allowContentType.contains(contentType)) {
            throw new BadRequestException("filetype is not allowed");
        }
        try (Workbook workbook = new XSSFWorkbook(dataFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            long rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount < 2) {
                throw new BadRequestException("file must contain at least 1 data row");
            }
            long currentRow = 0;
            while (sheet.iterator().hasNext()) {
                currentRow++;
                Row row = sheet.iterator().next();
                Cell invoiceCell = row.getCell(0);
                Cell productTypeCell = row.getCell(1);
                Cell supplierCdCell = row.getCell(2);
                Cell receiveDateCell = row.getCell(3);
                Cell statusCell = row.getCell(4);
                Cell quantityCell = row.getCell(5);
                if (row.getRowNum() == 0) {
                    if (invoiceCell == null 
                            || invoiceCell.getCellType() != CellType.STRING 
                            || invoiceCell.getStringCellValue() == null 
                            || !"invoice".equalsIgnoreCase(invoiceCell.getStringCellValue().trim())) {
                        throw new BadRequestException("column 1 must be 'invoice'");
                    }
                    if (productTypeCell == null 
                            || productTypeCell.getCellType() != CellType.STRING 
                            || productTypeCell.getStringCellValue() == null 
                            || !"product type".equalsIgnoreCase(productTypeCell.getStringCellValue().trim())) {
                        throw new BadRequestException("column 2 must be 'product type'");
                    }
                    if (supplierCdCell == null 
                            || supplierCdCell.getCellType() != CellType.STRING 
                            || supplierCdCell.getStringCellValue() == null 
                            || !"supplier cd".equalsIgnoreCase(supplierCdCell.getStringCellValue().trim())) {
                        throw new BadRequestException("column 3 must be 'supplier cd'");
                    }
                    if (receiveDateCell == null 
                            || receiveDateCell.getCellType() != CellType.STRING 
                            || receiveDateCell.getStringCellValue() == null 
                            || !"receive date".equalsIgnoreCase(receiveDateCell.getStringCellValue().trim())) {
                        throw new BadRequestException("column 4 must be 'receive date'");
                    }
                    if (statusCell == null 
                            || statusCell.getCellType() != CellType.STRING 
                            || statusCell.getStringCellValue() == null 
                            || !"status".equalsIgnoreCase(statusCell.getStringCellValue().trim())) {
                        throw new BadRequestException("column 5 must be 'status'");
                    }
                    if (quantityCell == null 
                            || quantityCell.getCellType() != CellType.STRING 
                            || quantityCell.getStringCellValue() == null 
                            || !"quantity".equalsIgnoreCase(quantityCell.getStringCellValue().trim())) {
                        throw new BadRequestException("column 6 must be 'quantity'");
                    }
                    continue;
                }
                if (invoiceCell == null 
                    || invoiceCell.getCellType() != CellType.STRING
                    || invoiceCell.getStringCellValue() == null
                    || invoiceCell.getStringCellValue().isBlank()) {
                    throw new BadRequestException("invoice must be a string at row " + currentRow);
                }
                if (productTypeCell == null 
                    || productTypeCell.getCellType() != CellType.STRING
                    || productTypeCell.getStringCellValue() == null
                    || productTypeCell.getStringCellValue().isBlank()) {
                    throw new BadRequestException("product type must be a string at row " + currentRow);
                }
                if (supplierCdCell == null 
                    || supplierCdCell.getCellType() != CellType.STRING
                    || supplierCdCell.getStringCellValue() == null
                    || supplierCdCell.getStringCellValue().isBlank()) {
                    throw new BadRequestException("supplier cd must be a string at row " + currentRow);
                }
                if (receiveDateCell == null 
                    || receiveDateCell.getCellType() != CellType.NUMERIC
                    || !DateUtil.isCellDateFormatted(receiveDateCell)) {
                    throw new BadRequestException("receive date must be a date at row " + currentRow);
                }
                if (statusCell == null
                    || statusCell.getCellType() != CellType.NUMERIC
                    || (statusCell.getNumericCellValue() < 0 && statusCell.getNumericCellValue() > 2)) {
                    throw new BadRequestException("status must be a number and between [0, 2] at row " + currentRow);
                }
                if (quantityCell == null 
                    || quantityCell.getCellType() != CellType.NUMERIC
                    || quantityCell.getNumericCellValue() < 0) {
                    throw new BadRequestException("quantity must be a positive number at row " + currentRow);
                }
                Optional<InboundEntity> existingInbound = this.inboundRepository.findByInvoice(invoiceCell.getStringCellValue().trim());
                if (existingInbound.isPresent()) {
                    throw new BadRequestException("invoice " + invoiceCell.getStringCellValue().trim() + " already exists at row " + currentRow);
                }
                InboundEntity newInbound = InboundEntity.builder()
                    .invoice(invoiceCell.getStringCellValue().trim())
                    .productType(ProductType.fromString(productTypeCell.getStringCellValue().trim()))
                    .supplierCd(SupplierCd.fromCode(supplierCdCell.getStringCellValue().trim()))
                    .receiveDate(receiveDateCell.getLocalDateTimeCellValue())
                    .status(OrderStatus.fromValue((long) statusCell.getNumericCellValue()))
                    .quantity((long) quantityCell.getNumericCellValue())
                    .user(user)
                    .build();
                this.inboundRepository.save(newInbound);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return ImportInboundDataResponse.builder().message("success").build();
    }

    @Override
    public GetInboundAttachmentDownloadUrlResponse getInboundAttachmentDownloadUrl(long inboundId, long attachmentId) {
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(inboundId);
        if (inboundResult.isEmpty()) {
            throw new NotFoundException("inbound not found");
        }
        Optional<InboundAttachmentEntity> attachmentResult = this.inboundAttachmentRepository.findById(attachmentId);
        if (attachmentResult.isEmpty()) {
            throw new NotFoundException("attachment not found");
        }
        InboundAttachmentEntity attachment = attachmentResult.get();
        if (attachment.getInboundId() != inboundId) {
            throw new BadRequestException("attachment does not belong to this inbound");
        }
        String downloadUrl = this.fileStoreService.getPresignedDownloadUrl(FileStoreService.INBOUND_BUCKET, attachment.getFilePath(), attachment.getFileName());
        return GetInboundAttachmentDownloadUrlResponse.builder()
            .url(downloadUrl)
            .build();
    }
}
