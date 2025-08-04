package com.training.warehouse.service;

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

public interface InboundService {
    void deleteInboundById(long inboundId);
    UpdateInboundByIdResponse updateInboundById(long id, UpdateInboundByIdRequest request);
    CreateInboundResponse createInbound(UserEntity user, CreateInboundRequest request);
    GetInventoryResponse getInventory(GetInventoryRequest query);
    GetInboundByIdResponse getInboundById(long id);
    GetInboundsResponse getInbounds(GetInboundsRequest query);
    ImportInboundDataResponse importInboundData(UserEntity user, ImportInboundDataRequest request);
    GetInboundAttachmentDownloadUrlResponse getInboundAttachmentDownloadUrl(long inboundId, long attachmentId);
}
