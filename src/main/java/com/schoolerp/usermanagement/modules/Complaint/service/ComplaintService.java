package com.schoolerp.usermanagement.modules.Complaint.service;

import com.schoolerp.usermanagement.modules.Complaint.requestDto.CreateComplaintRequestDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.ComplaintResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.GetAllComplaintsResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ComplaintService {

    ComplaintResponseDto createComplaint(CreateComplaintRequestDto request);

    List<GetAllComplaintsResponseDto> getAllComplaints();


}
