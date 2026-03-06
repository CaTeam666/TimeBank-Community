package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.EvidenceQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.EvidenceService;
import com.example.aiend.vo.EvidenceListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务存证控制器
 *
 * @author AI-End Team
 * @since 2024-12-26
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    /**
     * 获取存证列表
     *
     * @param queryDTO 查询参数
     * @return 分页存证列表
     */
    @GetMapping("/list")
    public Result<PageResponseDTO<EvidenceListVO>> getEvidenceList(EvidenceQueryDTO queryDTO) {
        log.info("收到获取存证列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<EvidenceListVO> result = evidenceService.getEvidenceList(queryDTO);
        return Result.success(result);
    }
}
