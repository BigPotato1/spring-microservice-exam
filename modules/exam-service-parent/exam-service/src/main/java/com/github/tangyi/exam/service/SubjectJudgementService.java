package com.github.tangyi.exam.service;

import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.exam.api.dto.SubjectDto;
import com.github.tangyi.exam.api.module.SubjectJudgement;
import com.github.tangyi.exam.mapper.SubjectJudgementMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 判断题Service
 *
 * @author tangyi
 * @date 2019-07-16 13:02
 */
@AllArgsConstructor
@Slf4j
@Service
public class SubjectJudgementService extends CrudService<SubjectJudgementMapper, SubjectJudgement>
        implements BaseSubjectService {

    /**
     * 根据ID查询
     *
     * @param id id
     * @return SubjectDto
     * @author tangyi
     * @date 2019-07-16 13:06
     */
    @Override
    public SubjectDto getSubject(Long id) {
        return null;
    }

    /**
     * 根据上一题ID查询下一题
     *
     * @param examinationId examinationId
     * @param previousId    previousId
     * @param nextType      0：下一题，1：上一题
     * @return SubjectDto
     * @author tangyi
     * @date 2019-09-14 17:03
     */
    @Override
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long previousId, Integer nextType) {
        return null;
    }

    /**
     * 查询列表
     *
     * @param subjectDto subjectDto
     * @return List<SubjectDto>
     * @author tangyi
     * @date 2019-07-16 13:08
     */
    @Override
    public List<SubjectDto> findSubjectList(SubjectDto subjectDto) {
        return null;
    }

    /**
     * 查询分页
     *
     * @param pageInfo   pageInfo
     * @param subjectDto subjectDto
     * @return PageInfo<SubjectDto>
     * @author tangyi
     * @date 2019-07-16 13:08
     */
    @Override
    public PageInfo<SubjectDto> findSubjectPage(PageInfo pageInfo, SubjectDto subjectDto) {
        return null;
    }

    /**
     * 根据ID批量查询
     *
     * @param ids ids
     * @return List<SubjectDto>
     * @author tangyi
     * @date 2019-07-16 13:09
     */
    @Override
    public List<SubjectDto> findSubjectListById(Long[] ids) {
        return null;
    }

    /**
     * 保存
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019-07-16 13:10
     */
    @Override
    public int insertSubject(SubjectDto subjectDto) {
        return 0;
    }

    /**
     * 更新
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019-07-16 13:10
     */
    @Override
    public int updateSubject(SubjectDto subjectDto) {
        return 0;
    }

    /**
     * 删除
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019-07-16 13:10
     */
    @Override
    public int deleteSubject(SubjectDto subjectDto) {
        return 0;
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019-07-16 13:10
     */
    @Override
    public int deleteAllSubject(Long[] ids) {
        return 0;
    }

    /**
     * 物理删除
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019-07-16 13:10
     */
    @Override
    public int physicalDeleteSubject(SubjectDto subjectDto) {
        return 0;
    }

    /**
     * 批量物理删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019-07-16 13:11
     */
    @Override
    public int physicalDeleteAllSubject(Long[] ids) {
        return 0;
    }
}
