package com.zosh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.ExportJob;
import com.zosh.modal.ExportJobStatus;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.request.CreateExportRequest;
import com.zosh.payload.response.ExportJobResponse;
import com.zosh.repository.ExportJobRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.service.ExportService;
import com.zosh.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ExportJobRepository exportJobRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExportJobResponse createExport(CreateExportRequest request) {
        User currentUser = userService.getCurrentUser();
        validateExportAccess(currentUser);

        ExportJob job = new ExportJob();
        job.setType(request.getType().trim().toUpperCase());
        job.setFormat(request.getFormat().trim().toUpperCase());
        job.setFiltersJson(writeJson(request.getFilters()));
        job.setCreatedBy(currentUser);
        job.setStatus(ExportJobStatus.PROCESSING);
        job.setProgress(25);
        ExportJob saved = exportJobRepository.save(job);

        saved.setFileContent(generateContent(saved.getType(), request.getFilters()));
        saved.setFileName(buildFileName(saved));
        saved.setStatus(ExportJobStatus.COMPLETED);
        saved.setProgress(100);
        saved.setExpiresAt(LocalDateTime.now().plusDays(1));
        saved = exportJobRepository.save(saved);

        return toResponse(saved);
    }

    @Override
    public ExportJobResponse getExportStatus(UUID exportId) {
        return toResponse(getJob(exportId));
    }

    @Override
    public byte[] downloadExport(UUID exportId) {
        ExportJob job = getJob(exportId);
        if (job.getStatus() != ExportJobStatus.COMPLETED) {
            throw new AccessDeniedException("Export is not ready for download");
        }
        return job.getFileContent().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getExportFileName(UUID exportId) {
        return getJob(exportId).getFileName();
    }

    private ExportJob getJob(UUID exportId) {
        return exportJobRepository.findById(exportId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Export job not found"));
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return new ExportJobResponse(job.getId(), job.getStatus(), job.getProgress(), job.getExpiresAt());
    }

    private void validateExportAccess(User user) {
        String role = user.getRole().name();
        if (!("ROLE_ADMIN".equals(role) || "ROLE_BRANCH_ADMIN".equals(role) || "ROLE_BRANCH_MANAGER".equals(role))) {
            throw new AccessDeniedException("You are not allowed to create exports");
        }
    }

    private String generateContent(String type, Map<String, Object> filters) {
        if ("STORE_LIST".equals(type)) {
            List<Store> stores = storeRepository.findAll();
            StringBuilder builder = new StringBuilder("storeId,brand,status,commissionRate\n");
            for (Store store : stores) {
                builder.append(store.getId()).append(',')
                        .append(safe(store.getBrand())).append(',')
                        .append(store.getStatus()).append(',')
                        .append(store.getCommissionRate()).append('\n');
            }
            return builder.toString();
        }
        if ("BRANCH_ORDERS".equals(type) && filters != null && filters.get("branchId") != null) {
            Long branchId = Long.valueOf(String.valueOf(filters.get("branchId")));
            StringBuilder builder = new StringBuilder("orderId,totalAmount,createdAt\n");
            orderRepository.findByBranchId(branchId).forEach(order ->
                    builder.append(order.getId()).append(',')
                            .append(order.getTotalAmount()).append(',')
                            .append(order.getCreatedAt()).append('\n'));
            return builder.toString();
        }
        return writeJson(filters == null ? Map.of("type", type) : Map.of("type", type, "filters", filters));
    }

    private String buildFileName(ExportJob job) {
        return job.getType().toLowerCase() + "-" + job.getId() + "." + job.getFormat().toLowerCase();
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize export payload", ex);
        }
    }
}
