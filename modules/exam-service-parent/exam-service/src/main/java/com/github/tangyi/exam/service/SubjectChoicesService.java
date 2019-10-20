package com.github.tangyi.exam.service;

import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.core.constant.CommonConstant;
import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.exam.api.constants.AnswerConstant;
import com.github.tangyi.exam.api.dto.SubjectDto;
import com.github.tangyi.exam.api.module.ExaminationSubject;
import com.github.tangyi.exam.api.module.SubjectChoices;
import com.github.tangyi.exam.api.module.SubjectOption;
import com.github.tangyi.exam.mapper.SubjectChoicesMapper;
import com.github.tangyi.exam.utils.SubjectUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 选择题service
 *
 * @author tangyi
 * @date 2018/11/8 21:23
 */
@AllArgsConstructor
@Service
public class SubjectChoicesService extends CrudService<SubjectChoicesMapper, SubjectChoices>
        implements BaseSubjectService {

    private final SubjectOptionService subjectOptionService;

    private final ExaminationSubjectService examinationSubjectService;

    /**
     * 查找题目
     *
     * @param subjectChoices subjectChoices
     * @return SubjectChoices
     * @author tangyi
     * @date 2019/1/3 14:24
     */
    @Override
    @Cacheable(value = "subjectChoices#" + CommonConstant.CACHE_EXPIRE, key = "#subjectChoices.id")
    public SubjectChoices get(SubjectChoices subjectChoices) {
        SubjectChoices subject = super.get(subjectChoices);
        // 查找选项信息
        if (subject != null) {
            SubjectOption subjectOption = new SubjectOption();
            subjectOption.setSubjectChoicesId(subject.getId());
            List<SubjectOption> options = subjectOptionService.getBySubjectChoicesId(subjectOption);
            subject.setOptions(options);
        }
        return subject;
    }

    /**
     * 根据考试ID查询题目数
     *
     * @param subjectChoices subjectChoices
     * @return int
     * @author tangyi
     * @date 2019/01/23 20:19
     */
    int getExaminationTotalSubject(SubjectChoices subjectChoices) {
        return 0;
    }

    /**
     * 新增
     *
     * @param subjectChoices subjectChoices
     * @return int
     * @author tangyi
     * @date 2019/01/23 20:03
     */
    @Override
    @Transactional
    public int insert(SubjectChoices subjectChoices) {
        return super.insert(subjectChoices);
    }

    /**
     * 更新题目
     *
     * @param subjectChoices subjectChoices
     * @return int
     * @author tangyi
     * @date 2019/1/3 14:24
     */
    @Override
    @Transactional
    @CacheEvict(value = "subjectChoices", key = "#subjectChoices.id")
    public int update(SubjectChoices subjectChoices) {
        // 更新选项
        List<SubjectOption> options = subjectChoices.getOptions();
        if (CollectionUtils.isNotEmpty(options)) {
            SubjectOption subjectOption = new SubjectOption();
            subjectOption.setSubjectChoicesId(subjectChoices.getId());
            subjectOptionService.deleteBySubjectChoicesId(subjectOption);
            // 初始化
            options.forEach(option -> {
                option.setCommonValue(subjectChoices.getCreator(), subjectChoices.getApplicationCode(),
                        subjectChoices.getTenantCode());
                option.setSubjectChoicesId(subjectChoices.getId());
            });
            // 批量插入
            subjectOptionService.insertBatch(options);
        }
        return super.update(subjectChoices);
    }

    /**
     * 根据上一题ID查询下一题
     *
     * @param examinationId  examinationId
     * @param subjectChoices subjectChoices
     * @return SubjectChoices
     * @author tangyi
     * @date 2019-09-14 16:47
     */
    @Transactional
    public SubjectChoices getByPreviousId(Long examinationId, SubjectChoices subjectChoices) {
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setExaminationId(examinationId);
        examinationSubject.setSubjectId(subjectChoices.getId());
        examinationSubject = examinationSubjectService.getByPreviousId(examinationSubject);
        if (examinationSubject == null)
            return null;
        return this.getSubjectChoicesById(examinationSubject.getSubjectId());
    }

    /**
     * 根据当前题目ID查询上一题
     *
     * @param examinationId  examinationId
     * @param subjectChoices subjectChoices
     * @return SubjectChoices
     * @author tangyi
     * @date 2019/10/07 20:40:16
     */
    @Transactional
    public SubjectChoices getPreviousByCurrentId(Long examinationId, SubjectChoices subjectChoices) {
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setExaminationId(examinationId);
        examinationSubject.setSubjectId(subjectChoices.getId());
        examinationSubject = examinationSubjectService.getPreviousByCurrentId(examinationSubject);
        if (examinationSubject == null)
            return null;
        return this.getSubjectChoicesById(examinationSubject.getSubjectId());
    }

    /**
     * 删除题目
     *
     * @param subjectChoices subjectChoices
     * @return int
     * @author tangyi
     * @date 2019/1/3 14:24
     */
    @Override
    @Transactional
    @CacheEvict(value = "subjectChoices", key = "#subjectChoices.id")
    public int delete(SubjectChoices subjectChoices) {
        int update;
        if ((update = super.delete(subjectChoices)) > 0)
            this.deleteOptionAndRelation(subjectChoices.getId());
        return update;
    }

    /**
     * 物理删除
     *
     * @param subjectChoices subjectChoices
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:44
     */
    @Transactional
    @CacheEvict(value = "subjectChoices", key = "#subjectChoices.id")
    public int physicalDelete(SubjectChoices subjectChoices) {
        int update;
        if ((update = this.dao.physicalDelete(subjectChoices)) > 0)
            this.deleteOptionAndRelation(subjectChoices.getId());
        return update;
    }

    /**
     * 批量删除题目
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/1/3 14:24
     */
    @Override
    @Transactional
    @CacheEvict(value = "subjectChoices", allEntries = true)
    public int deleteAll(Long[] ids) {
        int update;
        if ((update = super.deleteAll(ids)) > 0) {
            for (Long id : ids)
                this.deleteOptionAndRelation(id);
        }
        return update;
    }

    /**
     * 物理批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:44
     */
    @Transactional
    @CacheEvict(value = "subjectChoices", allEntries = true)
    public int physicalDeleteAll(Long[] ids) {
        int update;
        if ((update = this.dao.physicalDeleteAll(ids)) > 0) {
            for (Long id : ids)
                this.deleteOptionAndRelation(id);
        }
        return update;
    }

    /**
     * 删除题目的选项和与考试的关联
     *
     * @param subjectId subjectId
     * @author tangyi
     * @date 2019/06/16 22:09
     */
    @Transactional
    public void deleteOptionAndRelation(Long subjectId) {
        // 删除选项
        SubjectOption option = new SubjectOption();
        option.setSubjectChoicesId(subjectId);
        subjectOptionService.deleteBySubjectChoicesId(option);
        // 删除关联关系
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setSubjectId(subjectId);
        examinationSubjectService.deleteBySubjectId(examinationSubject);
    }

    /**
     * 根据ID查询
     *
     * @param id id
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/16 17:36
     */
    @Override
    public SubjectDto getSubject(Long id) {
        SubjectChoices subjectChoices = new SubjectChoices();
        subjectChoices.setId(id);
        return SubjectUtil.subjectChoicesToDto(this.get(subjectChoices));
    }

    /**
     * 根据上一题ID查询下一题
     *
     * @param examinationId examinationId
     * @param previousId    previousId
     * @param nextType      0：下一题，1：上一题
     * @return SubjectDto
     * @author tangyi
     * @date 2019/09/14 16:35
     */
    @Override
    @Transactional
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long previousId, Integer nextType) {
        SubjectChoices subjectChoices = new SubjectChoices();
        subjectChoices.setId(previousId);
        if (AnswerConstant.NEXT.equals(nextType)) {
            subjectChoices = this.getByPreviousId(examinationId, subjectChoices);
        } else {
            subjectChoices = this.getPreviousByCurrentId(examinationId, subjectChoices);
        }
        return SubjectUtil.subjectChoicesToDto(subjectChoices);
    }

    /**
     * 保存
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 17:50
     */
    @Override
    @Transactional
    public int insertSubject(SubjectDto subjectDto) {
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        subjectChoices.setAnswer(subjectDto.getAnswer().getAnswer());
        subjectChoices.setChoicesType(subjectDto.getType());
        return this.insert(subjectChoices);
    }

    /**
     * 更新，包括更新选项
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 17:50
     */
    @Override
    @Transactional
    public int updateSubject(SubjectDto subjectDto) {
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        // 参考答案
        subjectChoices.setAnswer(subjectDto.getAnswer().getAnswer());
        return this.update(subjectChoices);
    }

    /**
     * 删除
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 17:50
     */
    @Override
    @Transactional
    public int deleteSubject(SubjectDto subjectDto) {
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        return this.delete(subjectChoices);
    }

    /**
     * 物理删除
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:50
     */
    @Override
    @Transactional
    public int physicalDeleteSubject(SubjectDto subjectDto) {
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        return this.physicalDelete(subjectChoices);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 17:50
     */
    @Override
    @Transactional
    public int deleteAllSubject(Long[] ids) {
        return this.deleteAll(ids);
    }

    /**
     * 物理删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:51
     */
    @Override
    @Transactional
    public int physicalDeleteAllSubject(Long[] ids) {
        return this.physicalDeleteAll(ids);
    }

    /**
     * 查询列表
     *
     * @param subjectDto subjectDto
     * @return List
     * @author tangyi
     * @date 2019/06/16 18:16
     */
    @Override
    public List<SubjectDto> findSubjectList(SubjectDto subjectDto) {
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        return SubjectUtil.subjectChoicesToDto(this.findList(subjectChoices));
    }

    /**
     * 查询分页列表
     *
     * @param pageInfo   pageInfo
     * @param subjectDto subjectDto
     * @return PageInfo
     * @author tangyi
     * @date 2019/06/16 18:16
     */
    @Override
    public PageInfo<SubjectDto> findSubjectPage(PageInfo pageInfo, SubjectDto subjectDto) {
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        // 选择题类型：单选或多选
        if (subjectDto.getType() != null)
            subjectChoices.setChoicesType(subjectDto.getType());
        PageInfo<SubjectChoices> subjectChoicesPageInfo = this.findPage(pageInfo, subjectChoices);
        List<SubjectDto> subjectDtos = SubjectUtil.subjectChoicesToDto(subjectChoicesPageInfo.getList());
        PageInfo<SubjectDto> subjectDtoPageInfo = new PageInfo<>();
        subjectDtoPageInfo.setList(subjectDtos);
        subjectDtoPageInfo.setTotal(subjectChoicesPageInfo.getTotal());
        subjectDtoPageInfo.setPageSize(subjectChoicesPageInfo.getPageSize());
        subjectDtoPageInfo.setPageNum(subjectChoicesPageInfo.getPageNum());
        return subjectDtoPageInfo;
    }

    /**
     * 根据ID批量查询
     *
     * @param ids ids
     * @return List
     * @author tangyi
     * @date 2019/06/16 18:16
     */
    @Override
    public List<SubjectDto> findSubjectListById(Long[] ids) {
        return SubjectUtil.subjectChoicesToDto(this.findListById(ids));
    }

    /**
     * 根据题目ID查询题目信息和选项
     *
     * @param subjectId subjectId
     * @return SubjectChoices
     * @author tangyi
     * @date 2019/10/07 21:03:43
     */
    private SubjectChoices getSubjectChoicesById(Long subjectId) {
        SubjectChoices subjectChoices = new SubjectChoices();
        subjectChoices.setId(subjectId);
        subjectChoices = this.get(subjectChoices);
        SubjectOption subjectOption = new SubjectOption();
        subjectOption.setSubjectChoicesId(subjectChoices.getId());
        List<SubjectOption> options = subjectOptionService.getBySubjectChoicesId(subjectOption);
        subjectChoices.setOptions(options);
        return subjectChoices;
    }
}
