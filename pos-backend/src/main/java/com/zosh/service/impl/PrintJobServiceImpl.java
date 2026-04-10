package com.zosh.service.impl;

import com.zosh.modal.PrintJob;
import com.zosh.modal.PrintJobStatus;
import com.zosh.modal.User;
import com.zosh.payload.request.CreatePrintJobRequest;
import com.zosh.payload.response.PrintJobResponse;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PrintJobRepository;
import com.zosh.repository.ShiftReportRepository;
import com.zosh.service.PrintJobService;
import com.zosh.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrintJobServiceImpl implements PrintJobService {

    private final PrintJobRepository printJobRepository;
    private final OrderRepository orderRepository;
    private final ShiftReportRepository shiftReportRepository;
    private final UserService userService;

    @Override
    @Transactional
    public PrintJobResponse createPrintJob(CreatePrintJobRequest request) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getBranch() == null) {
            throw new AccessDeniedException("You are not associated with a branch");
        }
        String type = request.getType().trim().toUpperCase();
        validateReference(type, request.getReferenceId());

        PrintJob job = new PrintJob();
        job.setType(type);
        job.setReferenceId(request.getReferenceId());
        job.setPrinterId(request.getPrinterId());
        job.setCreatedBy(currentUser);
        job.setStatus(PrintJobStatus.QUEUED);
        PrintJob saved = printJobRepository.save(job);
        saved.setStatus(PrintJobStatus.COMPLETED);
        saved = printJobRepository.save(saved);

        return new PrintJobResponse(
                saved.getId(),
                saved.getType(),
                saved.getReferenceId(),
                saved.getPrinterId(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    private void validateReference(String type, String referenceId) {
        if ("INVOICE".equals(type)) {
            orderRepository.findById(Long.valueOf(referenceId))
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Order not found"));
            return;
        }
        if ("SHIFT_SUMMARY".equals(type)) {
            shiftReportRepository.findById(Long.valueOf(referenceId))
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Shift report not found"));
            return;
        }
        throw new IllegalArgumentException("Unsupported print job type");
    }
}
