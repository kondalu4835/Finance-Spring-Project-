package com.finance.service;

import com.finance.dto.FinancialRecordDto;
import com.finance.entity.FinancialRecord;
import com.finance.entity.RecordType;
import com.finance.entity.User;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public FinancialRecordDto.PageResponse list(
            RecordType type, String category,
            LocalDate dateFrom, LocalDate dateTo,
            int page, int size) {

        Page<FinancialRecord> pg = recordRepository.findAllWithFilters(
                type, category, dateFrom, dateTo,
                PageRequest.of(page, size)
        );
        return FinancialRecordDto.PageResponse.builder()
                .content(pg.getContent().stream().map(this::mapToDto).toList())
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();
    }

    public FinancialRecordDto.Response getById(Long id) {
        return mapToDto(findActiveOrThrow(id));
    }

    public FinancialRecordDto.Response create(FinancialRecordDto.CreateRequest req) {
        User currentUser = currentUser();
        FinancialRecord record = recordRepository.save(FinancialRecord.builder()
                .amount(req.getAmount())
                .type(req.getType())
                .category(req.getCategory())
                .date(req.getDate())
                .notes(req.getNotes())
                .createdBy(currentUser)
                .build());
        return mapToDto(record);
    }

    @Transactional
    public FinancialRecordDto.Response update(Long id, FinancialRecordDto.UpdateRequest req) {
        FinancialRecord record = findActiveOrThrow(id);
        User currentUser = currentUser();

        if (req.getAmount()   != null) record.setAmount(req.getAmount());
        if (req.getType()     != null) record.setType(req.getType());
        if (req.getCategory() != null) record.setCategory(req.getCategory());
        if (req.getDate()     != null) record.setDate(req.getDate());
        if (req.getNotes()    != null) record.setNotes(req.getNotes());
        record.setUpdatedBy(currentUser);

        return mapToDto(recordRepository.save(record));
    }

    @Transactional
    public void softDelete(Long id) {
        FinancialRecord record = findActiveOrThrow(id);
        record.setDeletedAt(LocalDateTime.now());
        record.setUpdatedBy(currentUser());
        recordRepository.save(record);
    }

    private FinancialRecord findActiveOrThrow(Long id) {
        return recordRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    private FinancialRecordDto.Response mapToDto(FinancialRecord r) {
        return FinancialRecordDto.Response.builder()
                .id(r.getId())
                .amount(r.getAmount())
                .type(r.getType())
                .category(r.getCategory())
                .date(r.getDate())
                .notes(r.getNotes())
                .createdByName(r.getCreatedBy() != null ? r.getCreatedBy().getName() : null)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
