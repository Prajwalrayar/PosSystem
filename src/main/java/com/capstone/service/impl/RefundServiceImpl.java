package com.capstone.service.impl;

import com.capstone.mapper.RefundMapper;
import com.capstone.model.Branch;
import com.capstone.model.Order;
import com.capstone.model.Refund;
import com.capstone.model.Users;
import com.capstone.payload.dto.RefundDto;
import com.capstone.repository.OrderRepository;
import com.capstone.repository.RefundRepository;
import com.capstone.service.RefundService;
import com.capstone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final UserService userService;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    @Override
    public RefundDto createRefund(RefundDto refund) throws Exception {
        Users cashier = userService.getCurrentUser();
        Order order = orderRepository.findById(refund.getOrder().getId()).orElseThrow(()
                -> new Exception("Order not found")
        );
        Branch branch = order.getBranch();
        Refund createdRefund = Refund.builder()
                .order(order)
                .cashier(cashier)
                .branch(branch)
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .createdAt(refund.getCreatedAt())
                .build();
        Refund savedRefund = refundRepository.save(createdRefund);
        return RefundMapper.toDTO(savedRefund);
    }

    @Override
    public List<RefundDto> getAllRefunds() {
        return refundRepository.findAll().stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundsByCashier(Long cashierId) {
        return refundRepository.findByCashierId(cashierId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundsByShiftReport(Long shiftReportId) {
        return refundRepository.findByShiftReportId(shiftReportId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundsByCashierAndDateRange(Long cashierId, LocalDateTime from,
                                                           LocalDateTime to) {
        return refundRepository.findByCashierIdAndCreatedAtBetween(cashierId,from,to).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundsByBranch(Long branchId) {
        return refundRepository.findByBranchId(branchId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RefundDto getRefundById(Long refundId) throws Exception {
        return refundRepository.findById(refundId)
                .map(RefundMapper::toDTO).orElseThrow(
                        ()-> new Exception("Refund not Found")
                );
    }

    @Override
    public void deleteRefund(Long refundId) throws Exception {
        this. getRefundById(refundId);
        refundRepository.deleteById(refundId);
    }
}
