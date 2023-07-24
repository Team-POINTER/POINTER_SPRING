package pointer.Pointer_Spring.report.service;

import pointer.Pointer_Spring.report.domain.Report;
import pointer.Pointer_Spring.report.domain.UserReport;
import pointer.Pointer_Spring.report.dto.ReportDto;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    ReportDto.UserReportResponse saveUserReport(ReportDto.UserReportRequest reportRequest);
    List<ReportDto.UserReportResponse> getUserReports(Long userId);
    ReportDto.UserReportResponse getUserReport(Long userId, Long targetUserId);
    List<ReportDto.UserReportResponse> getUserReportsByTarget(Long targetUserId);

    ReportDto.ReportResponse saveReport(ReportDto.ReportRequest reportRequest);
    List<ReportDto.ReportResponse> getReports(Long userId);
    //Report getReport(Long userId, Long targetUserId);
    public List<ReportDto.ReportResponse> getReportsByTarget(Long targetUserId);


    void deleteContents(Long reportId);
    void permanentRestrictionByUserReport(Long userReportId);
    void permanentRestrictionByOtherReport(Long reportId);
    void temporalRestriction(Long reportId);
}